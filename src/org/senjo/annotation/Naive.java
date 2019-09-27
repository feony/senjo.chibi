package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Наивный метод. Работает с общими межпоточными полями данных как с локальными,
 * т.е. предполагается, что синхронизацию уже включили до вызова данного метода,
 * либо она вообще не нужна. */
@Target({TYPE, METHOD, FIELD}) @Retention(RetentionPolicy.CLASS)
public @interface Naive {
	/** Класс или группа классов, блокировка которых должна быть активной для правильной
	 * работы данного метода. */
	Class<?>[] value() default { };
}


