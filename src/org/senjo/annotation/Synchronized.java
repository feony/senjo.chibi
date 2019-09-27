package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Признак, что метод класса или сам класс вцелом является потокобезопасным. */
@Target({TYPE, METHOD}) @Retention(RetentionPolicy.CLASS)
public @interface Synchronized { }


