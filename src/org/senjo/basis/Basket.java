/* Copyright 2016, 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static java.lang.reflect.Modifier.*;
import static org.senjo.basis.Helper.unsafe;
import static org.senjo.engine.BasketEngine.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import org.senjo.annotation.*;
import org.senjo.support.LogEx;

/** Статические варианты методов корзинки фруктов {@link ABasket}.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2019-02-25, release */
@SuppressWarnings("rawtypes")
public final class Basket {
	static final int offset = doOffset(ABasket.class, "basket");
	@Unsafe int basket;

	public Basket() { this.basket = 0; }
	public Basket(int push) { this.basket = push; }

	public void   sync() { doSync  (this, offset, basket); }
	public void unsync() { doUnsync(this, offset, basket); }



	public static boolean $Exist(int basket, int mask) { return (basket & mask) != 0; }
	public static boolean $Empty(int basket, int mask) { return (basket & mask) == 0; }
	public static boolean $Every(int basket, int mask) { return (basket & mask) == mask; }
	public static boolean $Every(int basket, int mask, int model) {
		return (basket & mask) == model; }
	public static boolean $State(int basket, int emptyMask, int everyMask) {
		return (basket & (emptyMask|everyMask)) == everyMask; }

	public static int $Mask(int basket, int mask) { return basket & mask; }

	public static int $Push(int basket, int mask) { return basket | mask; }
	public static int $Take(int basket, int mask) { return basket &~mask; }
	public static int $Swap(int basket, int takeMask, int pushMask) {
		return basket & ~takeMask | pushMask; }

	public static int $Turn(int basket, int mask, boolean state) {
		return state ? $Push(basket, mask) : $Take(basket, mask); }
	public static int $Turn(int basket, int mask, int model) {
		return basket & ~mask | model & mask; }

	/** Барьер чтения — команда CPU. Гарантируется, что все переменные читаемые после
	 * данного барьера будут иметь кешированную версию значений не старее, чем прочитанные
	 * перед данным барьером. Реализация: Данный барьер просто сбрасывает все кеш-линии
	 * текущего ядра CPU, которые ранее были захвачены другими ядрами на запись, ведь в этих
	 * кеш-линиях данного ядра ещё нет последних изменений сделанных в параллельных ядрах.
	 * Для чтения данных из сброшенных кеш-линий кешу придётся загрузить заново их уже
	 * изменённые версии.<p/>
	 * Используется для синхронизации. Ядро процессора читает значения и выполняет команды
	 * по мере возможности и для оптимизации постоянно меняет порядок этих действий. Также
	 * ядро процессора для ускорения использует кешированные значения, даже если они уже
	 * были изменены другим ядром. Барьер позволяет, например, гарантированно прочитать
	 * признак от другого потока строго перед чтением всех дополнительных значений
	 * от другого потока, причём версии значений после барьера точно будут не старее,
	 * чем значение признака.<p/>
	 * Любой механизм блокировки сам расставляет барьеры. Барьеры нужны только для
	 * многопоточной работы без блокировки. */
	@Synchronized public static final void syncLoad() { unsafe.loadFence (); }

	/** Барьер записи — команда CPU. Гарантируется, что все значения записываемые в память
	 * после данного барьера будут записаны только после записи всех значений в память перед
	 * данным барьером. Реализация: Данный барьер просто помечает все значения в кеше записи
	 * ядра особым флагом, а новые значения попадаемые в кеш записи не будут писаться
	 * в кеш-линии ядра, пока не будут вытеснены все помеченные этим флагом значения.<p/>
	 * Используется для синхронизации. Ядро процессора пишет значения в кеш через отложенный
	 * кеш записи. Кеш записи для оптимизации пишет значения в кеш-линии по мере возможности
	 * не соблюдая порядок их поступления. Барьер позволяет, например, гарантированно
	 * записать признак для другого потока только после записи всех дополнительных значений
	 * для него.<p/>
	 * Любой механизм блокировки сам расставляет барьеры. Барьеры нужны только для
	 * многопоточной работы без блокировки. */
	@Synchronized public static final void syncStore() { unsafe.storeFence(); }

	public static final class Sync {
//		public static final boolean s_existSync(Object target, int offset, int mask) {
//			throw new UnsupportedOperationException(); }
		/*...*/
	}



//======== Debug : Инструментарий для отладки ================================================//
	public static final class Debug {
		ArrayList<Item> list = new ArrayList<Item>();

		public Debug() { }

		/** Автоматически пытается разобрать параметры корзинки через отражение. */
		private Debug(@NotNull ABasket target) {
			Class type = target.getClass();
			do {
				Field[] fields = type.getDeclaredFields();
				int index = -1, count = fields.length;
				while (++index < count) {
					Field field = fields[index];
					if ( field.getType() == Integer.TYPE
							&& $Every(field.getModifiers(), PROTECTED|STATIC|FINAL)
							&& "fin".equals(field.getName()) ) break; }
				while (++index < count) {
					Field field = fields[index];
					if (!$Every(field.getModifiers(), STATIC|FINAL)) continue;
					if (field.getType() == Integer.TYPE) try {
							field.setAccessible(true);
							add(field.getInt(target), field.getName()); }
						catch (ReflectiveOperationException ex) {
							LogEx.fault("Can't parse Basket consts for debug", ex); }
					//TODO Нужно добавить ещё обработку LONG'ов
				}
				type = type.getSuperclass();
			} while (type != ABasket.class);
		}

		public Debug add(Debug parent) { list.addAll(parent.list); return this; }
		public Debug add(int mask, String name) { return add(mask, mask, name); }
		public Debug add(int mask, int value, String name) {
			list.add(new Item(name, mask, value)); return this; }

		public String print(ABasket target) { return print(target.basket); }
		public String print(int basket) {
			final StringBuilder result = new StringBuilder().append('[');
			boolean delimiter = false;

			build: {
				if (basket == 0) break build;
				for (Item item : list) {
					if (!item.has(basket)) continue;
					basket &= ~item.mask;
					if (delimiter) result.append(','); else delimiter = true;
					result.append(item.name);
				}

				if (basket == 0) break build;
				for ( int index = ABasket.fin, mask = 1 << index;
						mask != 0; --index, mask >>= 1 ) {
					if ((basket&mask) == 0) continue;
					if (delimiter) result.append(','); else delimiter = true;
					result.append("f"+index); }
			}
			return result.append(']').toString();
		}

		static class Item {
			final String name;
			final int mask, value;
			Item(String name, int mask, int value) {
				this.name = name; this.mask = mask; this.value = value; }
			boolean has(int basket) { return ((basket&mask) == value); }
		}
	}

	private static HashMap<Class, Debug> debugs;
	private static Debug makeDebug(ABasket target) {
		Debug debug;
		Class type = target.getClass();
		if (debugs == null) { debugs = new HashMap<Class,Debug>(); debug = null; }
		else debug = debugs.get(type);
		if (debug == null) debugs.put(type, debug = new Debug(target));
		return debug;
	}

	/** Возвращает словестное перечисление флагов находящихся в данный момент в корзинке
	 * target, если эти флаги объявлены в классах согласно правилам.
	 * <p/>
	 * Поле-константа считается флагом, если:<ul>
	 * <li>объявлена после константы protected static final int fin;</li>
	 * <li>содержит модификаторы static final;</li>
	 * <li>имеет тип int или long.</li></ul> */
	public static String toString(ABasket target) {
		return makeDebug(target).print(target); }
//	public static String toString(ABasket target, int basket) {
//		return makeDebug(target).print(basket); }
}


