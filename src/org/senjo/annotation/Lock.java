package org.senjo.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;
import org.senjo.basis.ABasket;

/** Запрет использования синхронизации по указанным классам. Предполагается вызов данного
 * метода с уже включённой синхронизацией. */
@Target(METHOD) @Retention(RetentionPolicy.CLASS)
public @interface Lock {
	Class<? extends ABasket>[] inner() default { };
	Class<? extends ABasket>[] outer() default { };
}


