package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.t1.ismailov.taskmanager.annotation.MeasureExecutionTime;
import ru.t1.ismailov.taskmanager.utils.AspectUtils;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingTaskAspect {

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
                AspectUtils.getClassAndMethodName(jp),
                Arrays.toString(args));
    }

    @AfterReturning(
            pointcut = "execution(!void ru.t1.ismailov.taskmanager.service.TaskService.*(..))",
            returning = "result"
    )
    public void logAfterReturningServiceMethod(JoinPoint jp, Object result) {
        log.info("Method {} executed successfully with result: {}",
                AspectUtils.getClassAndMethodName(jp),
                result);
    }

    @Around("@annotation(measure)")
    public Object logMeasureExecutionTime(ProceedingJoinPoint jp,
                                          MeasureExecutionTime measure) throws Throwable {
        String classAndMethodName = AspectUtils.getClassAndMethodName(jp);
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
