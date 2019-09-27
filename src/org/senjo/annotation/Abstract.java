package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Абстрактный метод. Предполагается переопределение данного метода в наследнике.
 * Однако, метод имеет примитивную реализацию по умолчанию, поэтому класс будет работать
 * и без переопределения, но его реализация будет неполноценной, часть функциональности
 * будет ограничена. */
@Target(METHOD) @Retention(RetentionPolicy.CLASS)
public @interface Abstract { }


