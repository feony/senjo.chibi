/* Copyright 2017, 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static org.senjo.engine.BasketEngine.*;

import org.senjo.annotation.Synchronized;

/** Расширение корзинки фруктов {@link ABasket}. Содержит альтернативные синхронные методы
 * чтения и записи флагов в корзинке.
 * <pre><code> if (pushSync(Closed|Released)) {
 *   ...release resources ...
 * }</code></pre>
 * вместо
 * <pre><code> sync();
 * boolean release = push(Closed|Released));
 * unsunc();
 * if (release) {
 *   ...release resources ...
 * }</code></pre>
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2017, change 2018-10-12, release */
public class ABasketSync extends ABasket {
	/** Синхронно проверить наличие любого флага из указанных в маске.
	 * <br/>Важно! Чтение не активирует блокировку, а лишь считывает самое свежее значение,
	 * даже если корзинка кем-то уже заблокирована.
	 * <p/><u>Пример</u>: проверить, чтобы существовал первый, второй или оба флага:
	 * <br/><pre>01010101 => existSync(First|Second) => return true</pre> */
	@Synchronized protected final boolean existSync(int mask) {
		return doExistSync(this, offset, mask); }

	/** Синхронно проверить отсутствие всех флагов указанных в маске.
	 * <br/>Важно! Чтение не активирует блокировку, а лишь считывает самое свежее значение,
	 * даже если корзинка кем-то уже заблокирована.
	 * <p/><u>Пример</u>: проверить, чтобы отсутствовали строго оба флага:
	 * <br/><pre>01010101 => emptySync(First|Second) => return false</pre> */
	@Synchronized protected final boolean emptySync(int mask) {
		return doEmptySync(this, offset, mask); }

	/** Синхронно проверить наличие всех флагов указанных в маске.
	 * <br/>Важно! Чтение не активирует блокировку, а лишь считывает самое свежее значение,
	 * даже если корзинка кем-то уже заблокирована.
	 * <p/><u>Пример</u>: проверить, чтобы существовали строго оба флага:
	 * <br/><pre>01010101 => existSync(First|Second) => return false</pre> */
	@Synchronized protected final boolean everySync(int mask) {
		return doEverySync(this, offset, mask); }

	/** Синхронно проверить состояние флагов указанных в маске с состоянием в модели.
	 * <br/>Важно! Чтение не активирует блокировку, а лишь считывает самое свежее значение,
	 * даже если корзинка кем-то уже заблокирована.
	 * <p/><u>Пример</u>: проверить, чтобы среди трёх флагов были только первый
	 * и третий:
	 * <pre>01010101 => every(First|Second|Third, First|Third) => return true</pre> */
	@Synchronized protected final boolean everySync(int mask, int model) {
		return doEverySync(this, offset, mask, model); }

	/** Синхронно проверить строгое отсутствие всех флагов emptyMask и наличие everyMask.
	 * <br/>Важно! Чтение не активирует блокировку, а лишь считывает самое свежее значение,
	 * даже если корзинка кем-то уже заблокирована.
	 * <p/><u>Пример</u>: проверить, чтобы второй флаг отсутствовал, и при этом
	 * первый с третьим присутствовали:<br/>
	 * <pre>01010101 => stateSync(Second, First|Third) => return true</pre> */
	@Synchronized protected final boolean stateSync(int emptyMask, int everyMask) {
		return doStateSync(this, offset, emptyMask, everyMask); }

	/** Синхронно определяет и возвращает флаги отфильтрованные по указанной маске.
	 * <br/><pre>01010101 => flags(First|Third|Fourth) => return First|Third</pre> */
	@Synchronized protected final int maskSync(int mask) {
		return doMaskSync(this, offset, mask); }



	/** Синхронно установить (положить) флаги по маске; если флаг уже есть, он там
	 * и останется.
	 * <p/><u>Пример</u>: добавить в корзину два флага:
	 * <br/><pre>01010101 => pushSync(First|Second) => 01010111</pre>
	 * @return true, если после операции состояние корзинки изменилось */
	@Synchronized protected final boolean pushSync(int mask) {
		return doPushSync(this, offset, basket, mask); }

	/** Синхронно снять (забрать) флаги по маске; если флага нет, он забран не будет.
	 * <p/><u>Пример</u>: забрать из корзины два флага:
	 * <br/><pre>01010101 => takeSync(Third|Fourth) => 01010001</pre>
	 * @return true, если после операции состояние корзинки изменилось */
	@Synchronized protected final boolean takeSync(int mask) {
		return doTakeSync(this, offset, basket, mask); }

	/** Синхронно снять (забрать) одни флаги и установить (положить) другие указанные
	 * в масках. Результат аналогичен вызову двух методов подряд: {@link #takeSync(int)}
	 * и {@link #pushSync(int)}.
	 * <p/><u>Пример</u>: забрать два флага и положить два других:
	 * <pre>01010101 => swap(First|Second, Third|Fourth) => 01011100</pre>
	 * @return true, если после операции состояние корзинки изменилось */
	@Synchronized protected final boolean swapSync(int takeMask, int pushMask) {
		return doSwapSync(this, offset, basket, takeMask, pushMask); }

	/** Синхронно установить (положить) флаги по маске в состояние, указанное в state.
	 * <br/><pre>01010101 => turn(11110000, true) => 11110101</pre>
	 * @return true, если после операции состояние корзинки изменилось */
	@Synchronized protected final boolean turnSync(int mask, boolean state) {
		return doTurnSync(this, offset, basket, mask, state); }

	/** Синхронно установить (положить) флаги по маске в соответствии с моделью.
	 * <br/><pre>01010101 => turn(11110000, 11000000) => 11000101</pre>
	 * @return true, если после операции состояние корзинки изменилось */
	@Synchronized protected final boolean turnSync(int mask, int model) {
		return doTurnSync(this, offset, basket, mask, model); }


	/** Захватывает указанные пользовательские мониторы по алгоритму SpinLock. Все изменения
	 * пишутся в корзинку синхронно, только когда главный монитор тоже свободен, однако
	 * после выполнения метода главный монитор захвачен не будет. Алгоритм будет ожидать,
	 * пока не освободятся строго все захватываемые мониторы, а также главный монитор.
	 * <p/>Ожидание требуется только для захвата мониторов, для отпуска захваченного
	 * мотитора ждать не нужно. Поэтому отпустить ранее захваченные пользовательские
	 * мониторы можно методом {@link #takeSync(monitor)}.
	 * @see #sync() */
	@Synchronized protected final void grabSync(int monitor) {
		doGrabSync(this, offset, basket, monitor); }

//FIXME Добавить ещё метод boolean grabSync(int monitor, int timeout)!
}


