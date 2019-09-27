package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Стабильный метод. Гарантируется, что реализация метода не допускает выброс исключений.
 * Предполагается, что этот признак описывает требование к алгоритму данного метода.
 * А требование диктуется реализацией других методов использующих данный метод. */
@Target(METHOD) @Retention(RetentionPolicy.CLASS)
public @interface Stable { }


