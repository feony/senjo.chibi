package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;
import org.senjo.basis.ABasket;

/** Признак, что метод класса или сам класс вцелом является потокобезопасным. Внутри
 * используется синхронизация, в аргументе можно указать сторонние классы, которые тоже
 * блокируются внутри метода или класса. */
@Target({TYPE, METHOD}) @Retention(RetentionPolicy.CLASS)
public @interface Synchronized {
	/** Класс или группа классов, которые могут блокироваться. Блокировка этих классов
	 * должна быть выключена при вызове данного метода. Иначе возможен deadlock. */
	Class<? extends ABasket>[] value() default { };
}


