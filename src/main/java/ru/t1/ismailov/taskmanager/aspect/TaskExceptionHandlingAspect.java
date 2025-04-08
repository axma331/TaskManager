package ru.t1.ismailov.taskmanager.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.t1.ismailov.taskmanager.utils.AspectUtils;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskExceptionHandlingAspect {

    @Autowired
    private final AspectUtils utils;

    @AfterThrowing(
            pointcut = "@within(ru.t1.ismailov.taskmanager.annotation.TaskExceptionHandler)",
            throwing = "ex"
    )
    public void logUnexpectedExceptions(JoinPoint jp, Throwable ex) {
        log.error("System error in {} method: {}", utils.getClassAndMethodName(jp), ex.getMessage(), ex);
    }

    @AfterThrowing(
            pointcut = "@annotation(ru.t1.ismailov.taskmanager.annotation.MailExceptionHandler)",
            throwing = "ex"
    )
    public void logEmailSendingException(JoinPoint jp, Throwable ex) {
        log.error("Error when sending email in {} - {}",
                utils.getClassAndMethodName(jp),
                ex.getMessage(),
                ex
        );
    }
}