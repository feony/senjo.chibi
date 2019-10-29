/* Copyright 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.senjo.basis.ABasket;

/**
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2019-09-27 */
class UBasket extends ABasket {

	@BeforeEach void clean() { swap(ALL, First|Second);  }
	
	@Test void test() {
		push(First|Second|Third);
		take(Third|Fourth);
		turn(Second|Fourth, false);
		swap(Third, First);
		assertEquals(First, mask(ALL), "Push/Take/Turn/Swap fault");
	}

	@Test void testPush() {
		assertTrue (push(First|Fourth), "Push changed the basket fault"  );
		assertFalse(push(First|Fourth), "Push unchanged the basket fault");
		assertEquals(First|Second|Fourth, mask(ALL), "Push fault"); }

	@Test void testTake() {
		assertTrue (take(First|Fourth), "Take changed the basket fault"  );
		assertFalse(take(First|Fourth), "Take unchanged the basket fault");
		take(First|Fourth);
		assertEquals(Second, mask(ALL), "Take fault"); }

	@Test void testTurn() {
		assertTrue (turn(First |Third , true ), "Turn changed the basket #1 fault");
		assertTrue (turn(Second|Fourth, false), "Turn changed the basket #2 fault");
		assertFalse(turn(Second, false), "Turn unchanged the basket #3 fault");
		assertEquals(First|Third, mask(ALL), "Turn flag fault");

		assertTrue (turn(ALL, Second|Fourth), "Turn changed the basket #4 fault");
		assertFalse(turn(Second, Second), "Turn unchanged the basket #5 fault");
		assertEquals(Second|Fourth, mask(ALL), "Turn mask fault");

		assertTrue (turn(hFirst), "Turn changed hubrid fault");
		assertFalse(turn(hFirst), "Turn unchanged hubrid fault");
		assertEquals(First, mask(ALL), "Turn hybrid fault");
	}

	@Test void testSwap() {
		assertTrue(swap(First|Third, Second|Fourth), "Swap changed the basket #1 fault");
		assertFalse(swap(First, Second), "Swap unchanged the basket #2 fault");
		assertEquals(Second|Fourth, mask(ALL), "Swap #1 fault");

		assertTrue(swap(First|Second, Third), "Swap changed the basket #3 fault");
		assertFalse(swap(Second, Third), "Swap unchanged the basket #4 fault");
		assertEquals(Third|Fourth, mask(ALL), "Swap #2 fault");
	}

	@Test void testExist() {
		assertTrue (exist(First), "Exist single yes fault");
		assertFalse(exist(Third), "Exist single no fault");
		assertTrue (exist(First|Second), "Exist multi all fault");
		assertTrue (exist(First|Fourth), "Exist multi several fault");
		assertFalse(exist(Third|Fourth), "Exist multi no fault");
	}

	@Test void testEmpty() {
		assertFalse(empty(First), "Empty single no fault");
		assertTrue (empty(Third), "Empty single yes fault");
		assertFalse(empty(First|Second), "Empty multi no fault");
		assertFalse(empty(First|Fourth), "Empty multi several fault");
		assertTrue (empty(Third|Fourth), "Empty multi all fault");
	}

	@Test void testEvery() {
		assertTrue (every(First), "Every single yes fault");
		assertFalse(every(Third), "Every single no fault");
		assertTrue (every(First|Second), "Every multi all fault");
		assertFalse(every(First|Fourth), "Every multi several fault");
		assertFalse(every(Third|Fourth), "Every multi no fault");

		assertTrue (every(First|Second, First|Second), "Every mask all true fault");
		assertFalse(every(First|Fourth, First|Fourth), "Every mask all false fault");
		assertTrue (every(First|Third, First), "Every mask one true fault");
		assertFalse(every(First|Third, Third), "Every mask one false fault");
		assertTrue (every(Third|Fourth, None), "Every mask no true fault");
		assertFalse(every(Theta|Second, None), "Every mask no false fault");
	}

	@Test void testState() {
		assertTrue (state(Fourth|Third, Second|First), "State true empty and true exist fault");
		assertFalse(state(Third, Fourth), "State true empty and false exist fault");
		assertFalse(state(First, Second), "State false empty and true exist fault");
		assertFalse(state(First, Fourth), "State false empty and false exist fault");

		assertFalse(state(hFirst ), "State hybrid false fault");
		assertTrue (state(hDouble), "State hybrid true fault");
	}

	@Test void testHybrid() {
		int mask = First|Second|Third;
		int item = First|Third;
		long hybrid = ((long)item << 32) | mask;
		assertEquals(hybrid, hybrid(mask, item), "Hybrid fault");
	}

	protected static final int fin = ABasket.fin-4;

	private static final int Alpha = 1<<fin+1;
	private static final int Beta  = 1<<fin+2;
	private static final int Gamma = 1<<fin+3;
	private static final int Delta = 1<<fin+4;

	private static final int Epsilon = 1<<0;
	private static final int Zeta    = 1<<1;
	private static final int Eta     = 1<<2;
	private static final int Theta   = 1<<3;

	private static final int First  = Alpha|Epsilon;
	private static final int Second = Beta |Zeta   ;
	private static final int Third  = Gamma|Eta    ;
	private static final int Fourth = Delta|Theta  ;

	private static final long hFirst  = hybrid(First|Second|Third|Fourth, First );
	private static final long hDouble = hybrid(First|Second|Third|Fourth, First|Second);
}


