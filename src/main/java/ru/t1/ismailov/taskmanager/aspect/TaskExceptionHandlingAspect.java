package ru.t1.ismailov.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.utils.AspectUtils;

@Aspect
@Component
@Slf4j
public class TaskExceptionHandlingAspect {

    @AfterThrowing(
            pointcut = "@within(ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler)",
            throwing = "ex"
    )
    public void logUnexpectedExceptions(JoinPoint jp, Throwable ex) {
        log.error("System error in {} method: {}", AspectUtils.getClassAndMethodName(jp), ex.getMessage(), ex);
    }

    @AfterThrowing(
            pointcut = "execution(* ru.t1.ismailov.taskmanager.service.NotificationService.sendStatusChangeEmail(..))",
            throwing = "ex"
    )
    public void logEmailSendingException(JoinPoint jp, Throwable ex) {
        log.error("Error when sending email in {} - {}",
                AspectUtils.getClassAndMethodName(jp),
                ex.getMessage(),
                ex
        );
    }
}