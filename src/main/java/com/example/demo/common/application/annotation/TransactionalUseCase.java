package com.example.demo.common.application.annotation;

import java.lang.annotation.*;

/**
 * Annotation marking a transactional use case class.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseCase // Meta-annotation: anything marked @TransactionalUseCase is also a @UseCase
public @interface TransactionalUseCase {
}
