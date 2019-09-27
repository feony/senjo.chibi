package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Пользователь метода. Класс, для которого предназначен даный метод. Предполагается, что
 * для штатной работы алгоритма данный метод может вызывать только указанный класс. */
@Target(METHOD) @Retention(RetentionPolicy.CLASS)
public @interface Caller {
	Class<?>[] value();
}


