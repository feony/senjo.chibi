/* Copyright 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.tests;

import static org.junit.jupiter.api.Assertions.*;
import static java.lang.System.out;

import org.junit.jupiter.api.Test;
import org.senjo.data.Phrase;

/**
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2019-10-27 */
class UPhrase {
	private static final boolean NOVISUAL = true;

	@Test final void visual() {
		if (NOVISUAL) return;
		for (int x = -100; x != 110; x++) out.println(phrase(x));
	}

	@Test final void test() {
		assertPhrase("Приплыло -99 кораблей.", -99);
		assertPhrase("Приплыло -42 корабля." , -42);
		assertPhrase("Приплыл -31 корабль."  , -31);
		assertPhrase("Приплыло -12 кораблей.", -12);
		assertPhrase("Приплыло -4 корабля."  ,  -4);
		assertPhrase("Приплыло 0 кораблей."  ,   0);
		assertPhrase("Приплыл 1 корабль."    ,   1);
		assertPhrase("Приплыло 3 корабля."   ,   3);
		assertPhrase("Приплыло 9 кораблей."  ,   9);
		assertPhrase("Приплыло 14 кораблей." ,  14);
		assertPhrase("Приплыло 39 кораблей." ,  39);
		assertPhrase("Приплыл 41 корабль."   ,  41);
		assertPhrase("Приплыло 64 корабля."  ,  64);
	}

/* Возможный формат:
	has_ship: Приплыл{form:count,| |о |)}{count} корабл{form:count,|ь|я|ей|}.
	has_ship: 'Приплыл' form(count, | |о |) count 'корабл' form(count, |ь|я|ей|) '.'
	has_ship: Сегодня form(приплыл[|о] {count} корабл[ь|я|ей])
 */

	private static final void assertPhrase(String expected, int count) {
		assertEquals(expected, phrase(count), "Phrase count=" + count); }

	private static final String phrase(int count) {
		return new Phrase().set(count).form("Приплыл", " ","о ")
				.number().form(" корабл", "ь","я","ей").end('.');
	}
}


