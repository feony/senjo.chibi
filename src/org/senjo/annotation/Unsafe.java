package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Данное поле класса используется в отражении или в неуправляемом коде. Имя данного поля
 * можно менять только с учётом всех мест, где оно используется неявно. */
@Target(FIELD) @Retention(RetentionPolicy.CLASS)
public @interface Unsafe { }


