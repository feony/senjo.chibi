package org.senjo.annotation;

import java.lang.annotation.*;

/** Метод нештатно работает с синхронизацией, в этом смысле он возвращает управление
 * не в том же состоянии, в котором получил его. Метод либо получает управление наивно,
 * а возвращает с включённой синхронизацией, либо получает синхронизировано, а возвращает
 * со снятой синхронизацией, либо как-то ещё издевается над статусом синхронизации.
 * Его деятельность обязательно должна быть очевидно описана в имени метода, например:
 * {@code storeAndUnsync()}, {@code syncAndRestore()}. */
@Target(ElementType.METHOD) @Retention(RetentionPolicy.CLASS)
public @interface VandalSync { }


