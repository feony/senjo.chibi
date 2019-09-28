/* Copyright 2016, 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static org.senjo.basis.Base.Illegal;
import static org.senjo.engine.BasketEngine.*;

import java.util.concurrent.locks.Lock;
import org.senjo.annotation.*;

/** Корзинка фруктов, флагов или пирожков — не важно как называть — в общем некоторых
 * сущностей, которые можно класть в корзинку «{@link #push(int)}», забирать из неё
 * «{@link #take(int)}» и проверять их наличие в ней «{@link #exist(int)},
 * {@link #empty(int)}» и др. Сущность определяется маской одного или нескольких битов
 * в числе. Маски могут быть как непересекающиеся {@code 0x001,0x010}, так и пересекающиеся
 * {@code 0x001,0x011,0x110} — всё зависит от поставленной задачи. Корзинка упрощает
 * хранение и работу внутреннего состояния конечного автомата, коим являются многие классы.
 * <p/>
 * <u>Синхронизация</u>. Само собой разумеется, если корзинка используется несколькими
 * потоками одновременно, то каждая операция чтения и изменения должна синхронизироваться,
 * т.к. меняются не байты целиком, а отдельные биты в одном и мот же байте. И тут никакой
 * volatile не поможет, т.к. над переменной производятся операции чтение+изменение+запись.
 * А т.к. эти операции очень короткие и могут вызываться группами подряд, то только конечный
 * разработчик в наружном классе имеет возможность эффективно сгруппировать вызовы нужных
 * операций корзинки в общий блок синхронизации. В ноябре 2017 года наконец-то добавлены
 * шустрые методы синхронизации. Также добавлены альтернативные синхронные методы чтения
 * и записи данных в классе {@link ABasketSync}.
 * <p/>
 * <u>Отладка</u>. Наглядное состояние корзинки можно увидеть с помощью метода
 * {@link Basket#toString(ABasket)}.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2018-10-12, release */
public abstract class ABasket {
	static final int offset = doOffset(ABasket.class, "basket");
	@Unsafe int basket;

	/** Проверить наличие любого флага из указанных в маске.
	 * <p/><u>Пример</u>: проверить, чтобы существовал первый, второй или оба флага:
	 * <br/><pre>0101'0101 => exist(First|Second) => return true</pre> */
	@Naive protected final boolean exist(int mask) { return (basket & mask) != 0; }
	/** Проверить отсутствие всех флагов указанных в маске.
	 * <p/><u>Пример</u>: проверить, чтобы отсутствовали оба флага:
	 * <br/><pre>0101'0101 => empty(First|Second) => return false</pre> */
	@Naive protected final boolean empty(int mask) { return (basket & mask) == 0; }
	/** Проверить наличие всех флагов указанных в маске.
	 * <p/><u>Пример</u>: проверить, чтобы строго существовали оба флага:
	 * <br/><pre>0101'0101 => every(First|Second) => return false</pre> */
	@Naive protected final boolean every(int mask) { return (basket & mask) == mask; }
	/** Проверить состояние флагов указанных в маске с состоянием в модели.
	 * <p/><u>Пример</u>: проверить, чтобы среди трёх флагов были только первый
	 * и третий:<br/>
	 * <pre>0101'0101 => every(First|Second|Third, First|Third) => return true</pre> */
	@Naive protected final boolean every(int mask, int model) {
		return (basket & mask) == model; }
	/** Проверить строгое отсутствие всех флагов из emptyMask и наличие из everyMask.
	 * <p/><u>Пример</u>: проверить, чтобы второй флаг отсутствовал, и при этом
	 * первый с третьим присутствовали:<br/>
	 * <pre>0101'0101 => state(Second, First|Third) => return true</pre> */
	@Naive protected final boolean state(int emptyMask, int everyMask) {
		return (basket & (emptyMask|everyMask)) == everyMask; }

	/** Возвращает флаги отфильтрованные по указанной маске.
	 * <br/><pre>0101'0101 => mask(First|Third|Fourth) => return First|Third</pre> */
	@Naive protected final int mask(int mask) { return basket & mask; }


	/** Установить (положить) флаги по маске; если флаг уже есть, он там и останется.
	 * <p/><u>Пример</u>: добавить в корзину два флага:
	 * <pre>0101'0101 => push(First|Second) => 0101'0111</pre>
	 * @return true, если состояние корзинки изменилось, в неё удалось положить новый флаг;
	 *         false, если все добавляемые в корзинку флаги уже были в ней до добавления. */
	@Naive protected final boolean push(int mask) {
		int before = basket; return before != (basket = before | mask); }
	/** Снять (забрать) флаги по маске; если флага нет, он забран не будет.
	 * <p/><u>Пример</u>: забрать из корзины два флага:
	 * <pre>0101'0101 => take(Third|Fourth) => 0101'0001</pre>
	 * @return true, если состояние корзинки изменилось, из неё удалось забрать хранимый
	 *         флаг; false, если ни одного из забираемых флагов в корзинке не было. */
	@Naive protected final boolean take(int mask) {
		int before = basket; return before != (basket = before & ~mask); }
	/** Снять одни флаги и установить другие указанные в масках; аналогично вызову
	 * двух методов подряд: {@link #take(int)} и {@link #push(int)}.
	 * <p/><u>Пример</u>: забрать два флага и положить два других:
	 * <pre>0101'0101 => swap(First|Second, Third|Fourth) => 0101'1100</pre> */
	@Naive protected final boolean swap(int takeMask, int pushMask) {
		int before = basket; return before != (basket = before & ~takeMask | pushMask); }

	/** Установить флаги по маске в состояние, указанное в state. Меняет в корзинке все
	 * флаги, которые отмечены параметром {@code mask} на значение указанное в параметре
	 * {@code state}.
	 * <br/><pre>0101'0101 => turn(1111'0000, true) => 1111'0101</pre> */
	@Naive protected final boolean turn(int mask, boolean state) {
		return state ? push(mask) : take(mask); }
	/** Установить флаги по маске в соответствии с моделью. Меняет в корзинке все флаги,
	 * которые отмечены параметром {@code mask} на те значения, которые указаны в параметре
	 * {@code model}.
	 * <br/><pre>0101'0101 => turn(1111'0000, 1100'0000) => 1100'0101</pre> */
	@Naive protected final boolean turn(int mask, int model) {
		int before = basket;
		return before != (basket = basket & ~mask | model & mask); }

//======== Работа с упаковками ===========================================================//
	/** Преобразовать маску и модель в гибридную модель. */
	protected static final long hybrid(int mask, int model) {
		return (long)model<<32 | mask; }

	/** Преобразовать маску в упаковку. */
	protected static final long packet(int mask) {
		if (mask == 0) throw Illegal("Mask can't be empty");
		long result = mask;
		int seek = 0;
		while ((mask & 0x3F) == 0) { seek += 6; mask >>= 6; }
		while ((mask & 0x01) == 0) { ++seek; mask >>= 1; }
		return ((long)seek << 32) | result; }

	/** Установить флаги по гибридной модели. Экспериментальный метод, аналогичен методу
	 * {@link #turn(int, int)}, но два параметра объединены в один, позволяет хранить
	 * два параметра в одной константе исходного кода. Объединить параметры можно методом
	 * {@link #hybrid(int, int)}.
	 * <br/><pre>0101'0101 => turn(0000'1111.0000'1100) => 0101'1100</pre> */
	@Naive protected final boolean turn(long hybrid) {
		return turn((int)hybrid, (int)(hybrid >> 32)); }

	/** Проверить наличие гибрида в корзинке. Гибрид содержит в себе значение из нескольких
	 * бит и маску этого значения.
	 * <p/><u>Пример</u>: проверить, чтобы в корзинке 4 бита (5÷2) находились в указанном
	 * состоянии (0101):
	 * <br/><pre> long FiveType = hybrid(0011'1100, 0001'0100); // тип пятый в маске 5÷2
	 * 0101'0101 => exist(FiveType) => return true</pre> */
	@Naive protected final boolean state(long hybrid) {
		return every((int)hybrid, (int)(hybrid >> 32)); }

	/** Установить флаги по маске, в том числе по расширенным. Гибридные значения и смещения
	 * в расширенных масках при этом игнорируются, а вместо них используются явно указанные.
	 * <br/><pre>0101'0101 => turn(0000'1111'0000'1100) => 0101'1100</pre> */
	@Naive protected final boolean turn(long exmask, int value) {
		return turn((int)exmask, value); }

	/** Возвращает смещённое (восстановленное) значение хранимое в указанной упаковке. */
	@Naive protected final int pack(long packet) {
		return (basket & (int)packet) >> (packet>>32); }

	/** Сохраняет значение с предварительным смещением по указанной упаковке. */
	@Naive protected final boolean pack(long packet, int value) {
		int before = basket, mask = (int)packet;
		return before != (basket = basket & ~mask | (value << (packet>>32)) & mask); }


//======== Специальные флаги =============================================================//
	/** Маска описывающая всё множество флагов. */
	protected static final int ALL  =-1;
	/** Маска описывающая отсутствие каких-либо флагов. */
	protected static final int None = 0;
	/** Граница занятых флагов базовым классом. Содержит номер первого свободного бита. */
	protected static final int fin  = BasketFin;



//======== SpinLock : циклическая блокировка ресурса =====================================//
/*   Это такая блокировка, которая не уводит поток в спячку при неудачной попытке захватить
 * монитор. Взамен она в цикле продолжает пытаться захватить монитор. Такая блокировка
 * эффективна для очень коротких операций, например добавление/удаление элемента в списке.
 * 
 *   Коротко о скоростях. Тест из десяти потоков, работающих с одной глобальной переменной:
 *  (  время  )  : ==== с сильной конкуренцией ==== : ==== вообще без конкуренции ==== :
 *  (обработки)  : счётчик в стеке : счётчик в куче : счётчик в стеке : счётчик в куче :
 * без блокировки:                 :     1.82±0.44  :     0.39±0.003  :                :
 * SpinLock      :    26.56±0.35   :    29.80±0.14  :     5.54±0.02   :                :
 * ReentrantLock :    32.03±0.60   :    35.39±0.30  :    18.07±0.30   :                :
 * syncronized   :    52.08±0.90   :    52.07±1.24  :     9.74±0.33   :                :
 * 
 *   SpinLock по тестам при конкуренции получился незначительно быстрее, без конкуренции
 * результат вкуснее. Тут стоит учесть, что результаты теста захватывают не только саму
 * блокировку, но и выполнение простой задачи. Также первые тесты выполнялись для очень
 * сильной конкуренции, когда из десяти потоков одновременно могли выполняться лишь 1,5÷2;
 * остальное время они конфликтовали и боролись за вход в общую зону. В основном прирост
 * скорости достигается за счёт отсутствия проверок, очередей, усыпления и пробуждения
 * потоков.
 *   Главным преимуществом данной реализации считаю отсутствие необходимости создавать
 * объект блокировки, как это нужно для ReentrantLock'а. Реализация требует столько же
 * памяти, сколько для syncronized (1 бит), это позволяет процессору реже сбрасывать
 * кеш-линии, а по скорости и гибкости работает чуть шустрее ReentrantLock, однако не имеет
 * дополнительных проверок. При наличии большого количества примитивных объектов, каждый
 * из которых нужно блокировать для коротких, простых и наглядных операций — это удобно
 * и экономно.
 *   Конечно же данная реализация не имеет и не будет иметь Condition'ов и прочих сигналов,
 * чтобы оставаться примитивной и максимально шустрой. */

	/** Захватывает монитор по алгоритму SpinLock. Нужно понимать, что это максимально
	 * быстрая, а не максимально безопасная операция. Второй вложенный вызов блокировки
	 * в том же потоке повесит этот монитор по deadlock'у.<p/>
	 * Этот вариант синхронизации можно использовать только в примитивных задачах,
	 * для блокировки короткого и очевидного куска кода. */
	@Synchronized protected final void sync() { doSync(this, offset, basket); }

	/** Захватывает главный монитор и указанные дополнительные мониторы по алгоритму
	 * SpinLock. Алгоритм будет ожидать, пока не освободятся строго все из захватываемых
	 * мониторов.
	 * @param monitor — маска мониторов, которые нужно захватить вместе с главным монитором. 
	 * @see #sync() */
	@Synchronized protected final void sync(int monitor) {
		doSync(this, offset, basket, monitor); }

//FIXME Добавить ещё метод boolean sync(int monitor, int timeout)

	/** Освобождает ранее захваченный монитор по алгоритму SpinLock. Нужно понимать, что
	 * это максимально быстрая, а не максимально безопасная операция. Неоправданный вызов
	 * разблокировки в чужом потоке (без предварительной блокировки) преждевременно
	 * разблокирует монитор захваченный другим потоком. */
	@Synchronized protected final void unsync() { doUnsync(this, offset, basket); }

	/** Освобождает ранее захваченные главный и пользовательские мониторы по алгоритму
	 * SpinLock.
	 * @see #unsync() */
	@Synchronized protected final void unsync(int monitor) {
		doUnsync(this, offset, basket, monitor); }

	/** Переактивирует sync, включая перед ним переданный в аргументе lock. Метод служит,
	 * чтобы исключить возникновение deadlock'ов, соблюдая строгий порядок активации:
	 * lock всегда включается перед sync'ом. При этом метод для скорости сначала пытается
	 * активировать lock без снятия sync'а, и только если при активном sync'е lock уже
	 * кем-то занят, тогда освобождает sync и уходит в ожидание захвата lock'а.
	 * <p/>Перед вызовом данного метода sync должен быть захвачен данным потоком.
	 * По завершении метода гарантируется, что lock и sync будут захвачены данным потоком
	 * в таком порядке. По возможности sync освобождаться не будет. При необходимости
	 * sync будет освобождён и захвачен повторно после lock'а. */
	protected final void resync(Lock lock) {
		if (lock.tryLock()) return;
		doUnsync(this, offset, basket); lock.lock(); doSync(this, offset, basket); }

	/** Примитивный класс для удобства блокировки-разблокировки. Блокировка выглядит немного
	 * нагляднее и не разрывается на две части, что исключает ошибку забыть одну из частей.
	 * Пример:
	 * <pre><code> try (Sync s = syncer()) {
	 *   ...
	 * }</code></pre>
	 * вместо
	 * <pre><code> sync();
	 * try {
	 *   ...
	 * } finally {
	 *   unsync();
	 * }</code></pre> */
	protected final Sync syncer() { return new Sync(this); }

	protected final Sync syncer(boolean sync) { return new Sync(sync ? this : null); }

	protected final static class Sync implements AutoCloseable {
		private ABasket owner;
		private Sync(ABasket owner) {
			this.owner = owner;
			if (owner != null) doSync(owner, offset, owner.basket); }
		@Override public void close() {
			ABasket owner = this.owner;
			if (owner == null) return;
			doUnsync(owner, offset, owner.basket); this.owner = null; } }


	protected String basketDebug() { return Basket.toString(this); }
}


