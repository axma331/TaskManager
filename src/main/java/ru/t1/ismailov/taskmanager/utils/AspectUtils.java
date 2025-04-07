package ru.t1.ismailov.taskmanager.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class AspectUtils {

    public static String getClassAndMethodName(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        return "%s.%s".formatted(
                signature.getDeclaringType().getSimpleName(),
                signature.getName()
        );
    }
}
