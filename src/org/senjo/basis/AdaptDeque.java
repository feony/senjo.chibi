/* Copyright 2018-2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static org.senjo.basis.Base.Illegal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.senjo.annotation.Naive;

/** Подстраивающаяся внутренняя очередь. Предполагается, но не гарантируется, хранение
 * в данной очереди минимального числа элементов используемых наследником и скрытых снаружи.
 * Т.е. практически всегда эта очередь пуста или содержит всего один элемент. Исходя
 * из этого очередь старается занимать как можно меньше памяти. Минимум один объём регистра
 * в дополнение к корзинке. Также механизм съедает один байт у корзинки.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-08, change 2019-02-07 */
@SuppressWarnings("unchecked")
@Naive public abstract class AdaptDeque<T> extends ABasketSync {
	private Object target;

	@Naive protected final boolean dequeEmpty() { return every(mMode, EmptyMode); }

	/** Число элементов в списке */
	@Naive protected final int dequeSize() { switch (mask(mMode)) {
	case EmptyMode : return 0;
	case SingleMode: return 1;
	case ArrayMode : {
		int delta = pack(pEnd) - pack(pStart);
		return delta > 0 ? delta : 8 - delta; }
	case DequeMode : return ((ArrayDeque)target).size();
	default: throw Illegal(this, mMode); } }

	/** Добавить элемент в конец списка */
	@Naive protected final void dequePush(T item) { add(item, true); }

	/** Добавить элемент в начало списка */
	@Naive protected final void dequeShift(T item) { add(item, false); }

	@Naive private final void add(T item, boolean forward) {
		switch (mask(mMode)) {
		case EmptyMode : target = item; turn(mMode, SingleMode); break;
		case SingleMode: { Object[] array = new Object[8];
			if (forward) {
				array[0] = target; array[1] = item;
				turn(mMode|pStart|pEnd, ArrayMode|InitForward );
			} else {
				array[7] = target; array[6] = item;
				turn(mMode|pStart|pEnd, ArrayMode|InitBackward); }
			target = array;
			break; }
		case ArrayMode: { T[] array = (T[])target;
			int start = pack(pStart), end = pack(pEnd);
			if (start == end) {
				ArrayDeque<T> deque = new ArrayDeque<T>(24);
				do { deque.offer(array[start]); } while (++start != 8);
				for (start = 0; start != end; ++start) deque.offer(array[start]);
				if (forward) deque.offer(item); else deque.offerFirst(item);
				target = deque;
				turn(mMode|pStart|pEnd, DequeMode);
			} else if (forward) {
				array[end] = item;
				pack(pEnd, ++end == 8 ? 0 : end);
			} else /*backward*/ {
				pack(pStart, --start >= 0 ? start : (start = 7));
				array[start] = item; }
			break; }
		case DequeMode: ((ArrayDeque)target).push(item); break; }
	}

	/** Забрать элемент с начала списка (очередь) */
	@Naive protected final  T dequeTake() { return search(true, true); }

	/** Забрать элемент с конца списка (стек) */
	@Naive protected final T dequePop() { return search(false, true); }

	/** Найти элемент.
	 * @param forward — направление движения; true ищет с начала, как у списка; false ищет
	 *        с конца, как у стека. */
	@Naive private final T search(boolean forward, boolean remove) {
		switch (mask(mMode)) {
		case EmptyMode : throw new NoSuchElementException();
		case SingleMode: {
			T result = (T)target;
			if (remove) { target = null; turn(mMode, EmptyMode); }
			return result; }
		case ArrayMode: {
			int start = pack(pStart), end = pack(pEnd);
			T[] array = (T[])target;
			T result;
			if (forward) {
				result = array[start];
				if (remove) {
					array[start] = null;
					if (++start == 8) start = 0;
					if (start != end) pack(pStart, start); else erase();
				}
			} else {
				result = array[--end >= 0 ? end : (end=7)];
				if (remove) {
					array[end] = null;
					if (start != end) pack(pEnd, end); else erase();
				}
			}
			return result; }
		case DequeMode: {
			ArrayDeque<T> deque = (ArrayDeque<T>)target;
			T result;
			if (remove) {
				   result = forward ? deque.removeFirst() : deque.removeLast();
				if (deque.isEmpty()) erase();
			} else result = forward ? deque.  peekFirst() : deque.  peekLast();
			return result; }
		default: throw Illegal(this, mMode); }
	}

	/** Забрать все элементы списка разом */
	@Naive protected final Collection<T> dequeScrub() {
		Collection<T> result;
		switch (mask(mMode)) {
		case EmptyMode:
			result = Collections.emptySet();
			break;
		case SingleMode:
			result = Collections.singleton((T)target);
			break;
		case ArrayMode: {
			int start = pack(pStart), end = pack(pEnd);
			T[] array = (T[])target;
			int size = start < end ? end - start : 8 + start - end;

//XXX Можно вместо ArrayList придумать свою коллекцию в которую класть оригинальный массив
			ArrayList<T> list = new ArrayList<T>(size);
			do list.add(array[start]); while (++start < 8);
			start = 0;
			for (start = 0; start != end; ++start) list.add(array[start]);
			result = list;
			break; }
		case DequeMode: {
			result = (ArrayDeque<T>)target;
			break; }
		default: throw Illegal(this, mMode); }
		erase();
		return result;
	}

	/** Очистить. Удалить безвозвратно все элементы из списка */
	@Naive private final void erase() { target = null; turn(mMode|pStart|pEnd, EmptyMode); }



//======== Basket : Постоянные для корзинки фруктов ======================================//
	protected static final int fin = ABasketSync.fin-8;
	/** Маска режима хранения очереди */
	private static final int      mMode   = 3<<fin+1;
	/** Режим хранения очереди: очередь пустая, а потому физически отсутствует */
	private static final int  EmptyMode   = 0<<fin+1;
	/** Режим хранения очереди: очередь из одного элемента, а потому в target хранится
	 * сам элемент очереди, а сама очередь физически отсутствует */
	private static final int SingleMode   = 1<<fin+1;
	/** Режим хранения очереди: очередь из экономного массива. Индексы начала и конца
	 * очереди хранятся прямо в корзинке, а в target хранится массив-контейнер из восьми
	 * ячеек */
	private static final int  ArrayMode   = 2<<fin+1;
	/** Режим хранения очереди: расточительный, в target хранится расширяющийся экземпляр
	 * очереди */
	private static final int  DequeMode   = 3<<fin+1;
	/** Пакет трёхбитного числа: первый элемент очереди внутри экономного массива */
	private static final long pStart      = packet(7<<fin+3);
	/** Пакет трёхбитного числа: последний элемент очереди внутри экономного массива */
	private static final long pEnd        = packet(7<<fin+6);
	/** Инициализация индексов при создании экономного массива из двух элементов очереди
	 * записанных в прямом направлении */
	private static final int InitForward  = 2<<fin+6;
	/** Инициализация индексов при создании экономного массива из двух элементов очереди
	 * записанных в обратном направлении */
	private static final int InitBackward = 6<<fin+3;
}


