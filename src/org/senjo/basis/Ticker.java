/* Copyright 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Простейший класс для замера и журналирования времени выполнения некоторого алгоритма.
 * Пример использования:
 * <pre>
 * Ticker ticker = new Ticker();
 * ...{code}...
 * Log.trace("Code done " + ticker); // Выведет в журнал: "Code done (1,23ms)"</pre>
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2018, change 2018-10-12, release */
public final class Ticker {
	private static DecimalFormat format = new DecimalFormat( "(#,##0.00ms)",
			DecimalFormatSymbols.getInstance(Locale.ROOT) );

// Format создаёт алгоритм разбора не во время создания, а во время первого исполнения
	static { format.format(1.23f); } // Исполняем фиктивный разбор сразу

	private long tick;
	public Ticker() { reset(); }

	public void reset() { tick = System.nanoTime(); }
	@Override public String toString() { return toString(tick); }

	/** Формирует строку вида "(1,23ms)" с временем от указанного до текущего момента. */
	public static String toString(long nanoStart) {
		return format.format( (System.nanoTime() - nanoStart) / 1000000f ); }

	/** Формирует строку вида "(1,23ms)" с промежутком времени указанным в аргументе. */
	public static String toStringEx(long nano) {
		return format.format( nano / 1000000f ); }
}


