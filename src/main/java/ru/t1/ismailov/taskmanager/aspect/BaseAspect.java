package ru.t1.ismailov.taskmanager.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public abstract class BaseAspect {

    protected String getClassAndMethodName(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        return "%s.%s".formatted(
                signature.getDeclaringType().getSimpleName(),
                signature.getName()
        );
    }
}
