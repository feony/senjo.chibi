package org.senjo.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Положительное число. Значение помеченного поля строго больше нуля. */
@Target({FIELD, PARAMETER, LOCAL_VARIABLE}) @Retention(RetentionPolicy.CLASS)
public @interface Positive { }


