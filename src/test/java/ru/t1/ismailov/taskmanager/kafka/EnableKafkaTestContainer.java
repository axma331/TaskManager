package ru.t1.ismailov.taskmanager.kafka;

import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ContextConfiguration(initializers = KafkaTestContainer.Initializer.class)
public @interface EnableKafkaTestContainer {
}
