package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

/** Асинхронный метод. Вообще не работает с общими межпоточными полями данных, потому
 * метод как не использует синхронизацию сам, так и не требует её включения извне. */
@Target({TYPE, METHOD, FIELD}) @Retention(RetentionPolicy.CLASS)
public @interface Unsync { }


