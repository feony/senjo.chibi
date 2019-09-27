package org.senjo.annotation;

import java.lang.annotation.*;

/** Переданный аргумент (объект) будет поглощён и уничтожен (испорчен или заимствован).
 * Дальнейшее его использование после вызова метода запрещено. Например, это может быть большой
 * массив, содержимое которого не копируется, а запоминается и используется оригинал. */
@Target(ElementType.PARAMETER) @Retention(RetentionPolicy.CLASS)
public @interface Devour { }


