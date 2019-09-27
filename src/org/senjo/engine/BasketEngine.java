/* Copyright 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.engine;

import static org.senjo.basis.Helper.*;
import static org.senjo.basis.Text.textInstance;
import static org.senjo.support.LogEx.*;
import org.senjo.basis.Ticker;

/** Управляющий код корзинки. Вынесен в первую очередь для того, чтобы в одном классе можно
 * было реализовывать несколько назависимых корзинок или модифицировать режим/состав.
 * Короче говоря, для гибкости.<p/>
 * Я так и не понял прикола, но при тесте потерь выяснилось, что вынесенный в статические
 * функции код корзинки работает быстрее, чем в родных методах вообще без выноса. Может
 * это провоцирует оптимизатор работать лучше или даёт ему больше свободы, иначе нет идей.
 * Тесты разных подходов проводились одновременно.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-02-12, change 2019-02-25, release */
@SuppressWarnings("rawtypes")
public final class BasketEngine {
	public static final int doOffset(Class type, String fieldName) {
		return unsafeOffset(type, fieldName); }

	/** Проверить, в состоянии корзинки basket один из флагов mask присутствует */
	public static final boolean doExist(int basket, int mask) {
		return (basket & mask) != 0; }
	/** Проверить, в состоянии корзинки basket все флаги mask отсутствуют */
	public static final boolean doEmpty(int basket, int mask) {
		return (basket & mask) == 0; }
	/** Проверить, в состоянии корзинки basket все флаги mask присутствуют */
	public static final boolean doEvery(int basket, int mask) {
		return (basket & mask) == mask; }
	/** Проверить, в состоянии корзинки basket наличие всех флагов mask соответствует
	 * состоянию model */
	public static final boolean doEvery(int basket, int mask, int model) {
		return (basket & mask) == model; }
	/** Проверить, в состоянии корзинки basket все флаги emptyMask отсутствуют и все флаги
	 * everyMask присутствуют */
	public static final boolean doState(int basket, int emptyMask, int everyMask) {
		return (basket & (emptyMask|everyMask)) == everyMask; }
	/** Вернуть из корзинки basker текущие состояния флагов из mask */
	public static final int     doMask (int basket, int mask) { return basket & mask; }

	/** Добавить в корзинку owner/offset с состоянием basket все флаги mask */
	public static final boolean doPush(Object owner, int offset, int basket, int mask) {
		int result = basket | mask;
		if (result == basket) return false;
		unsafe.putInt(owner, (long)offset, result);
		return true; }

	/** Забрать из корзинки owner/offset с состоянием basket все флаги mask */
	public static final boolean doTake(Object owner, int offset, int basket, int mask) {
		int result = basket & ~mask;
		if (result == basket) return false;
		unsafe.putInt(owner, (long)offset, result);
		return true; }

	/** В корзинке owner/offset с состоянием basket забрать флаги takeMask и положить флаги
	 * pushMask. Флаги указанные в обоих масках будут добавлены. */
	public static final boolean doSwap( Object owner, int offset, int basket,
			int takeMask, int pushMask ) {
		int result = basket & ~takeMask | pushMask;
		if (result == basket) return false;
		unsafe.putInt(owner, (long)offset, result);
		return true; }

	/** В корзинке owner/offset с состоянием basket установить все флаги mask в состояние
	 * state (true – положить, false – забрать) */
	public static final boolean doTurn( Object owner, int offset, int basket,
			int mask, boolean state ) {
		int result = state ? basket | mask : basket & ~mask;
		if (result == basket) return false;
		unsafe.putInt(owner, (long)offset, result);
		return true; }

	/** В корзинке owner/offset с состоянием basket установить все флаги mask в состояния
	 * соответствующие карте model */
	public static final boolean doTurn( Object owner, int offset, int basket,
			int mask, int model ) {
		int result = basket & ~mask | model & mask;
		if (result == basket) return false;
		unsafe.putInt(owner, (long)offset, result);
		return true; }

	/** В корзинке owner/offset с состоянием basket установить гибридные значения hybrid.
	 * Hybrid объединяет в себе маску используемых бит и значение внутри этой маски. */
	public static final boolean doTurn(Object owner, int offset, int basket, long hybrid) {
		return doTurn(owner, offset, basket, (int)(hybrid >> 32), (int)hybrid); }

	/** В корзинке owner/offset с состоянием basket захватить системный монитор включив
	 * тем самым режим синхронизации потоков */
	public static final void doSync(Object owner, int offset, int basket) {
		basket &= ~Monitor;
		while (true) {
			if (unsafe.compareAndSwapInt(owner, offset, basket, Monitor|basket)) return;
			basket = await(owner, offset, basket, Monitor); }
	}

	/** В корзинке owner/offset с состоянием basket захватить системный монитор
	 * и пользовательские мониторы monitor включив тем самым режим синхронизации потоков */
	public static final void doSync(Object owner, int offset, int basket, int monitor) {
		/* Не используем барьер на чтение, а принимаем в параметре возможно устаревшее
		 * значение из кеша. CAS, как и барьер, всё равно загрузит актуальную версию
		 * переменной, и если она отличается, то вторым запросом мы всё равно прочитаем
		 * уже актуальное значение. */
		monitor |= Monitor; basket &= ~monitor;
		while (true) {
			if (unsafe.compareAndSwapInt(owner, offset, basket, monitor|basket)) return;
			basket = await(owner, offset, basket, monitor); }
	}

	/** В корзинке owner/offset с состоянием basket синхронно захватить только
	 * пользовательские мониторы monitor включив тем самым пользовательский режим
	 * синхронизации потоков. Захват сработает только когда системный монитор свободен. */
	public static final void doGrabSync(Object owner, int offset, int basket, int monitor) {
		basket &= ~(Monitor|monitor);
		while (true) {
			if (unsafe.compareAndSwapInt(owner, offset, basket, monitor|basket)) return;
			basket = await(owner, offset, basket, monitor); } }

//	/** В корзинке owner/offset с состоянием basket синхронно освободить только
//	 * пользовательские мониторы monitor отключит тем самым пользовательский режим
//	 * синхронизации потоков. Захват сработает только когда системный монитор свободен.
//	 * Алгоритм метод ничем не отличается от #takeSync(monitor). */
//	public static final void doLoseSync(Object owner, int offset, int basket, int monitor) {
//		basket &= ~Monitor;
//		while (true) {
//			if (unsafe.compareAndSwapInt(owner, offset, basket, ~monitor&basket)) return;
//			basket = await(owner, offset, basket, Monitor); } }

	/** В корзинке owner/offset с состоянием basket отпустить системный монитор отключив
	 * тем самым режим синхронизации потоков */
	public static final void doUnsync(Object owner, int offset, int basket) {
		/* Считаем/доверяем, что блокировка уже точно есть, а значит и basket у нас самый
		 * актуальный. Сначала ставим барьер записи, чтобы выгрузить все кешированные
		 * изменения остальным процессорам. А потом опасно (но мы и так в блокировке сидим)
		 * отпускаем блокировку путём снятия флага Monitor. */
		unsafe.storeFence();
		unsafe.putInt(owner, (long)offset, ~Monitor&basket); }

	/** В корзинке owner/offset с состоянием basket отпустить системный монитор
	 * и пользовательские мониторы monitor отключив тем самым режим синхронизации потоков */
	public static final void doUnsync(Object owner, int offset, int basket, int monitor) {
		unsafe.storeFence();
		unsafe.putInt(owner, (long)offset, ~(Monitor|monitor)&basket); }


	/** Синхронно проверить, в состоянии корзинки owner/offset один из флагов mask
	 * присутствует */
	public static final boolean doExistSync(Object owner, int offset, int mask) {
		int basket = unsafe.getInt(owner, (long)offset);
		return (basket & mask) != 0; }
	/** Синхронно проверить, в состоянии корзинки owner/offset все флаги mask отсутствуют */
	public static final boolean doEmptySync(Object owner, int offset, int mask) {
		int basket = unsafe.getInt(owner, (long)offset);
		return (basket & mask) == 0; }
	/** Синхронно проверить, в состоянии корзинки owner/offset все флаги mask присутствуют*/
	public static final boolean doEverySync(Object owner, int offset, int mask) {
		int basket = unsafe.getInt(owner, (long)offset);
		return (basket & mask) == mask; }
	/** Синхронно проверить, в состоянии корзинки owner/offset наличие всех флагов mask
	 * соответствует состоянию model */
	public static final boolean doEverySync(Object owner, int offset, int mask, int model) {
		int basket = unsafe.getInt(owner, (long)offset);
		return (basket & mask) == model; }
	/** Синхронно проверить, в состоянии корзинки owner/offset все флаги emptyMask
	 * отсутствуют и все флаги everyMask присутствуют */
	public static final boolean doStateSync( Object owner, int offset,
			int emptyMask, int everyMask ) {
		int basket = unsafe.getInt(owner, (long)offset);
		return (basket & (emptyMask|everyMask)) == everyMask; }
	/** Синхронно вернуть из корзинки owner/offset текущие состояния флагов из mask */
	public static final int     doMaskSync (Object owner, int offset, int mask) {
		int basket = unsafe.getInt(owner, (long)offset);
		return basket & mask; }


	/** Синхронно добавить в корзинку owner/offset с состоянием basket все флаги mask */
	public static final boolean doPushSync(Object owner, int offset, int basket, int mask) {
		basket &= ~Monitor;
		while (true) {
			int result = basket | mask;
			if (unsafe.compareAndSwapInt(owner, offset, basket, result))
				return basket != result;
			basket = await(owner, offset, basket, Monitor); } }

	/** Синхронно забрать из корзинки owner/offset с состоянием basket все флаги mask */
	public static final boolean doTakeSync(Object owner, int offset, int basket, int mask) {
		basket &= ~Monitor;
		while (true) {
			int result = basket & ~mask;
			if (unsafe.compareAndSwapInt(owner, offset, basket, result))
				return basket != result;
			basket = await(owner, offset, basket, Monitor); } }

	/** Синхронно в корзинке owner/offset с состоянием basket забрать флаги takeMask
	 * и положить флаги pushMask. Флаги указанные в обоих масках будут добавлены. */
	public static final boolean doSwapSync( Object owner, int offset, int basket,
			int takeMask, int pushMask ) {
		basket &= ~Monitor;
		while (true) {
			int result = basket & ~takeMask | pushMask;
			if (unsafe.compareAndSwapInt(owner, offset, basket, result))
				return basket != result;
			basket = await(owner, offset, basket, Monitor); } }

	/** Синхронно в корзинке owner/offset с состоянием basket установить все флаги mask
	 * в состояние state (true – положить, false – забрать) */
	public static final boolean doTurnSync( Object owner, int offset, int basket,
			int mask, boolean state ) {
		basket &= ~Monitor;
		while (true) {
			int result = state ? basket | mask : basket & ~mask;
			if (unsafe.compareAndSwapInt(owner, offset, basket, result))
				return basket != result;
			basket = await(owner, offset, basket, Monitor); } }

	/** Синхронно в корзинке owner/offset с состоянием basket установить все флаги mask
	 * в состояния соответствующие карте model */
	public static final boolean doTurnSync( Object owner, int offset, int basket,
			int mask, int model ) {
		basket &= ~Monitor;
		while (true) {
			int result = basket & ~mask | model & mask;
			if (unsafe.compareAndSwapInt(owner, offset, basket, result))
				return basket != result;
			basket = await(owner, offset, basket, Monitor); } }


	/** Системный метод. Ожидает освобождение корзинки и возвращает её актуальное значение.
	 * Важно понимать, независимо от результата метод возвращает управление только тогда,
	 * когда в корзинке все флаги по маске monitor станут свободными.
	 * @param owner — целевой объект содержащий в себе корзинку;
	 * @param offset — адресное смещение поля корзинки внутри целевого объекта;
	 * @param source — исходное значение корзинки, которое не прошло CAS операцию;
	 * @param monitor — все мониторы, освобождения которых нужно дождаться. */
	private static final int await(Object owner, int offset, int source, int monitor) {
		int update = unsafe.getInt(owner, (long)offset);
		if (update != source && (update & monitor) == 0) return update;
		/* Если после CAS выяснилось, что mutex уже занят другим потоком, то просим ядро
		 * переключаться на другой поток, нужно ждать пока mutex не будет освобождён. */
		int miss = -AllowableParkCount;
		long tick = System.nanoTime();
		do {
			update = unsafe.getIntVolatile(owner, offset);
			if ((update & monitor) == 0) {
				if (miss >= 0)
					info("SpinLock " + textInstance(owner) + ( (miss&0x10000000) == 0
							? " waited for " + (miss + AllowableParkCount) + " cycles "
							: " relive after deadlock " ) + Ticker.toString(tick));
				return update; }
			if (miss > 0) {
				int cycles = (int)((System.nanoTime()-tick) >>> 34);
				if ((miss&0xF0000000) == 0)
					if (cycles == 0) ++miss; else miss = 0x10000000;
				else if (cycles != (miss&0x0FFFFFFF)) {
					String suffix = " in SpinLock unit of " + textInstance(owner)
							+ ". It waiting too long " + Ticker.toString(tick);
					if (miss != 0x10000000) fault("DEADLOCK #" + cycles + suffix);
					else fault("DEADLOCK" + suffix, vandal.cutStackTop(new Throwable(), 3));
					miss = 0x10000000|cycles; }
			} else ++miss;
			unsafe.park(false, 1); // Парковка на 1 наносекунду (минимально)
		} while (true);
	}

	/** Допустимое число микропарковок Thread'а, которые не будут журналироваться. */
	private static final int AllowableParkCount = 48;



	/** Граница занятых флагов базовым классом. Содержит номер первого свободного бита. */
	public static final int BasketFin = 30;
	/** Флаг монитора для синхронизации между потоками. *//* Для гармоничности кода
	 * блокировки, бит флага монитора должен совпадать с битом знака целого числа. */
	public static final int Monitor = 1<<31;
}


