package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TaskExceptionHandlingAspect {

    @AfterThrowing(
            pointcut = "@within(ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler)",
            throwing = "ex"
    )
    public void logUnexpectedExceptions(JoinPoint jp, Throwable ex) {
        log.error("System error in {} method: {}", getClassAndMethodName(jp), ex.getMessage(), ex);
    }

    private static String getClassAndMethodName(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        return "%s.%s".formatted(
                signature.getDeclaringType().getSimpleName(),
                signature.getName()
        );
    }
}