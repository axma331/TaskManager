package ru.t1.ismailov.taskmanager.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.t1.ismailov.taskmanager.annotation.MeasureExecutionTime;
import ru.t1.ismailov.taskmanager.utils.AspectUtils;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingTaskAspect {

    @Autowired
    private final AspectUtils utils;

    @Before(
            "@within(ru.t1.ismailov.taskmanager.annotation.LogRequest) && " +
                    "within(@org.springframework.web.bind.annotation.RestController *)"
    )
    public void logHttpRequest(JoinPoint jp) {
        var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();

        Object[] args = jp.getArgs();

        log.info("Processing HTTP {} request in {} with args: {}",
                httpMethod,
                utils.getClassAndMethodName(jp),
                Arrays.toString(args));
    }

    @AfterReturning(
            pointcut = "execution(!void ru.t1.ismailov.taskmanager.service.TaskService.*(..))",
            returning = "result"
    )
    public void logAfterReturningServiceMethod(JoinPoint jp, Object result) {
        log.info("Method {} executed successfully with result: {}",
                utils.getClassAndMethodName(jp),
                result);
    }

    @Around("@annotation(measure)")
    public Object logMeasureExecutionTime(ProceedingJoinPoint jp,
                                          MeasureExecutionTime measure) throws Throwable {
        String classAndMethodName = utils.getClassAndMethodName(jp);
        boolean logOnError = measure.logOnError();
        long startTime = System.currentTimeMillis();

        try {
            Object result = jp.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("{} executed after {} ms", classAndMethodName, executionTime);

            return result;
        } catch (Throwable ex) {
            if (logOnError) {
                long executionTime = System.currentTimeMillis() - startTime;
                log.error("Error in {} after {} ms", classAndMethodName, executionTime, ex);
            }
            throw ex;
        }
    }
}
