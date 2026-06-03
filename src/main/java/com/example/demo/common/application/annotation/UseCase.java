package com.example.demo.common.application.annotation;

import java.lang.annotation.*;

/**
 * Annotation marking a use case class.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseCase {
}
