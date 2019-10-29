/* Copyright 2016, 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static org.senjo.basis.Base.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.senjo.annotation.Abstract;

/** Класс перечисления значений некоторого множества (полный аналог из C#).
 * <br/>Также позволяет упрощённо перечислять элементы через оператор:
 * <pre>for (T item : enumerator)</pre> В отличие от принятого в Java итератора
 * реализован удобнее: метод {@link #next()} по возможности перемещает перечислитель
 * к следующему элементу множества и возвращает истину в случае успеха. Остальные
 * getter'ы возвращают значения относящиеся текущему элементу множества. Getter'ы
 * создаются и реализуются в каждом случае свои.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2016, change 2018-10-12, fix 2019-01-25, release */
@SuppressWarnings("unchecked")
public abstract class AEnumerator<T> extends ABasket implements Iterable<T> {
	int index = -1;

	protected AEnumerator() { push(Prestart); }

//======== Методы для переопределения конечным наследником, реализующим логику ===========//
	/** Сместиться к следующему элементу, если они ещё остались в наборе. В случае
	 * успеха возвращает true. Ответ false расценивается как отсутствие очередных
	 * элементов для перечисления.
	 * @param nextIndex — порядковый индекс нового элемента, к которому осуществляется
	 * переход. База сама считает индексы, в большинстве случаев это удобнее, т.к. либо
	 * наследник переходит к следующему элементу логически и ему не нужен индекс
	 * (LinkedList), либо он сам вынужден считать индекс, чтобы перейти к следующему
	 * элементу (ArrayList). Порядковый индекс текущего элемента всегда доступен через
	 * свойство {@link #index()}, а также он передаётся в этом параметре, его можно
	 * использовать по желанию. */
	protected abstract boolean next(int nextIndex);

	/** Возвращает текущий элемент перечисляемого источника. *//* Решил тут не ставить
	 * проверку на версию и определённость элемента, т.к. тут всегда возвращается сам
	 * перечислитель, который всегда определён и не меняется, меняется его состояние. */
	@Abstract public T item() { return (T)this; }

	/** Метод по возможности возвращает количество элементов в перечислителе.
	 * Если количество неизвестно, то возвращается переданный аргумент fake. Предполагается,
	 * что реализация перечислителя переопределит данный метод, если он сразу знает число
	 * элементов. Пример использования:
	 * <pre><code>list = new ArrayList<>(enumerator.size(32));</code></pre>
	 * – если число элементов известно, то список сразу проинициализируется правильным
	 * числом ячеек.
	 * @param fake — если количество перечисляемых элементов неизвестно, то вернётся
	 * это значение агрумента. */
	@Abstract public int size(int fake) { return exist(Finished) ? index : fake; }

//^^^^^^^^ Методы для переопределения конечным наследником, реализующим логику ^^^^^^^^^^^//
//======== Методы, которые может использовать конечный наследник для управления ==========//
	/** Установить флаг, что текущий элемент последний. Для простого перемещения
	 * вперёд метод {@link #next(int)} вызываться больше не будет, так что в нём можно
	 * не реализовывать проверку на последний элемент и никогда не возвращать false. */
	protected void setLatest() { push(Last); }

	/** Устанавливает перечислитель пустым, внутренний метод {@link #next(int)} вызываться
	 * не будет, внешний {@link #next()} сразу вернёт {@code false}. */
	protected void setEmpty() { push(Previewed|Finished); index = 0; }

//	/** Устанавливает новый index */
//	protected void setIndex(int index) { this.index = index; }

//^^^^^^^^ Методы, которые может использовать конечный наследник для управления ^^^^^^^^^^//

	/** Проверить версию источника, что она не изменилась с момента запуска перечислителя.
	 * Переопределяемый метод. Если данные в источнике изменились, то перечислитель при
	 * попытке продолжить перечисление выбросит исключение данным методом. */
	@Abstract void assertVersion() { }

	/** Проверить доступность элемента. Выбрасывает исключение, если перечислитель стоит
	 * за границей допустимого диапазона элементов; например перед первым или после
	 * последнего. */
	protected final void assertItem() {
		if (empty(Prestart|Previewed|Finished)) return;
		String submessage;
		if      (exist(Previewed)) submessage = "Call Iterator#next() is required";
		else if (exist(Finished )) submessage = "Enumeration is finished";
		else                       submessage = "Enumeration not started";
		throw new NoSuchElementException("Data is unavailable. " + submessage); }

	/** Текущая позиция перечислителя. До перебора элементов index=-1, после перебора
	 * всех элементов index=size. В некоторых случаях фактическая позиция может
	 * не совпадать с логической, тогда выбрасывается исключение. Например такое может
	 * быть при вызове метода isEmpty до перечисления, тогда объект не знает, есть ли
	 * элементы и фактически переходит к первому элементу, но логически он остаётся
	 * в начальном состоянии. Метод #next() не логически перейдёт к первому элементу,
	 * а фактически уже будет на нём. Аналогичная ситуация будет при вызове метода
	 * hasNext порождённого итератора: значения элемента недоступны между вызовами
	 * методов {@link Iterator#hasNext()} и {@link Iterator#next()}. */
	public final int index() { assertItem(); return index; }

	/** Автоматически проверяет наличие элементов в перечислителе и возвращает {@code true},
	 * если перечислитель пустой, т.е. первый же {@link #next()} даст {@code false}. */
	public final boolean isEmpty() {
		if (index == -1) _next(true);
		return exist(Finished) && index == 0; }

	/** Перейти к следующему элементу. Возвращает {@code true}, если следующий элемент
	 * существует и переход к нему успешно выполнен. */
	public final boolean next() { return _next(false); }

	/** Создаёт и возвращает ограниченный итератор. Возвращаемый {@link Iterator}
	 * предназначен только для оператора {code for(:)}! Весь механизм не предназначен
	 * для работы с текущим элементом между вызовами методов {@link Iterator#hasNext()}
	 * и {@link Iterator#next()}, т.к. первый метод реально переводит перечислитель
	 * на следующий элемент, после его вызова перечислитель предоставляет новое значение. */
	@Override public Iterator<T> iterator() { return new InnerIterator<T>(this); }

	/** Внутренний переход к следующему элементу перечисления. Учитывает возможные заранее
	 * кешированные состояния, проверяет версию источника. */
	private final boolean _next(boolean preview) { try {
		if (exist(Finished|Previewed|Last)) { // Есть закешированное состояние
			if (exist(Finished )) return false;
			if (take (Previewed)) return true ;
			// Нет ни Finished, ни Previewed — значит это чистый Last
			swap(Last, Finished); return false;
		} else { // Готового состояния нет, нужно его запросить у наследника
			assertVersion(); //XXX Можно добавить флажок NeedAssert
			if (next(++index)) return true; else { push(Finished); return false; }
		}
	} finally { if (preview) push(Previewed); else take(Prestart); } }


//======== Basket : работа с корзинкой флагов ============================================//
	protected static final int fin = ABasket.fin-7;
	/** Состояние предпросмотра. Метод {@link #next()} не должен выполнять переход,
	 * метод {@link #index} должен вернуть index-1 или исключение. */
	protected static final int Previewed = 1<<fin+1;
	protected static final int  Premoved = 1<<fin+2;
	protected static final int Postmoved = 1<<fin+3;
	/** Перебор элементов ещё не начат, перечислитель не указывает на реальный элемент. */
	protected static final int Prestart  = 1<<fin+4;
	/** Перебор элементов закончен, перечислитель не указывает на реальный элемент. */
	protected static final int Finished  = 1<<fin+5;
	/** Текущий элемент является последним, метод {@link #next()} наследника не должен
	 * вызываться! */
	protected static final int Last      = 1<<fin+6;
	protected static final int Iterating = 1<<fin+7;
//^^^^^^^^ Basket : работа с корзинкой флагов ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//


	/** Надёжный перечислитель, дополнительно проверяет, чтобы в момент перебора элементов
	 * источник данных не менялся и чтобы нельзя было извлекать из Enumerator'а два
	 * Iterator'а сразу: при создании второго первый аннулируется. Как побочный эффект,
	 * перечислитель нагружен кодом больше и работает медленнее. */
	public static abstract class Reliable<T> extends AEnumerator<T> {
		private int version;
		private InnerIterator<T> iterator;

		protected Reliable() { this(0); }
		protected Reliable(int version) { this.version = version; }

//region: Методы для переопределения конечным наследником реализующим логику .........//
//====================================================================================//
		@Abstract public Reliable<T> reset() {
			take(ALL); super.index = -1;
			version = getVersion();
			return this; }

		@Abstract protected int getVersion() { return 0; }

//endr Методы для переопределения конечным наследником реализующим логику ^^^^^^^^^^^^//

		@Override final void assertVersion() {
			if (version == getVersion()) return;
			throw Illegal("Source data has been changed by another"); }

		@Override public final Iterator<T> iterator() {
			if (iterator != null) iterator.owner = null;
			if (exist(Finished)) reset();
			return iterator = (InnerIterator<T>)super.iterator(); }
	}

	/** Гуляющий перечислитель с возможностью свободно перемещаться по элементам источника
	 * данных в любом направлении. */
	public static abstract class Walker<T> extends Reliable<T> {
		protected abstract boolean shift(int index, int count);
		@Override protected boolean next(int baseIndex) { return shift(baseIndex, 1); }

		private final boolean _shift(int count) {
			return shift(super.index += count, count); }

		/** Сместиться на указанное число элементов count. Метод #next() аналогичен вызову
		 * метода {@code shift(1)}. */
		public final boolean shift(int count) {
			if (exist(Previewed)) { --count; take(Previewed); }
			if (count > 0) { // Движение вперёд
				if (empty(Last|Finished)) return _shift(count);
				else if (exist(Last)) { swap(Last, Finished); ++index; return false; }
				else return false; }
			if (count < 0) { // Движение назад
				if (-count > index) { _shift(-index-1); return false; }
				else return _shift(count); }
			return true;
		}

		/** Встать на элемент с порядковым номером index. */
		public final boolean seek(int index) {
			if (index < 0) throw new ArrayIndexOutOfBoundsException(index);
			return shift(index - super.index);
		}

		public final boolean prev() { return shift(-1); }

		@Override public Walker<T> reset() { super.reset(); return this; }

//TODO Хотелось бы сделать ещё Walker<T> clone() { ... }
	}

	/** Перечислитель с возможностью модифицировать источник данных. */
	public static abstract class Editor<T, I> extends Walker<T> {
		/** Вставляет новый элемент перед текущим элементом. Позиция остаётся на текущем
		 * элементе. */
		public abstract void prepend(I item);
		/** Вставляет новый элемент после текущего элемента. Позиция остаётся на текущем
		 * элементе. */
		public abstract void  append(I item);
		/** Заменяет текущий элемент на новый. */
		public abstract void replace(I item);
		/** Удаляет текущий элемент. Позиция становится неопределённой, как бы между
		 * элементами. */
		public abstract void remove();
	}



//======== Implementations : Примитивные реализации перечислителя ========================//
	public static final <T> AEnumerator<T> makeEmpty() { return EMPTY; }
	private static final AEnumerator EMPTY = new AEnumerator() {
		@Override protected boolean next(int index) { return false; }
		@Override public int size(int fake) { return 0; } };

	public static final <T> AEnumerator<T> makeSingle(T value) {
		return new Simple<T>(value); }
	private static class Simple<T> extends AEnumerator<T> {
		private final T item;
		private Simple(T value) { this.item = value; }

		@Override protected boolean next(int index) { return index == 0; }
		@Override public T item() { return index == 0 ? item : null; }
		@Override public int size(int fake) { return 1; }
	}

	public final static <T> AEnumerator<T> makeMultiple(T ... values) {
		return new Multiple<T>(values); }
	private static class Multiple<T> extends AEnumerator<T> {
		private final T[] items;
		private Multiple(T ... values) { this.items = values; }

		@Override protected boolean next(int index) { return index != items.length; }
		@Override public T item() { return items[index()]; }
		@Override public int size(int fake) { return items.length; }
	}


	private static class InnerIterator<T> implements Iterator<T> {
		/** Внутренняя ссылка на перечислитель. Некоторые реализации могут сбрасывать
		 * связь iterator'а обнуляя это поле. */
		private AEnumerator<T> owner;
		InnerIterator(AEnumerator<T> owner) { this.owner = owner; }

		@Override public final boolean hasNext() { return owner._next(true); }

		@Override public final T next() {
			if (owner._next(false)) return owner.item();
			throw new NoSuchElementException(); }
	}
}



/* С планированием данного механизма у меня возникли сложные выборы. Самый главный:
 * На что больше должен походить класс Enumerator, на перечислитель (Iterator) или
 * на контейнер (List, Iterable)? Самая изначальная задача была в возможности создания
 * более простых реализаций перечислителя и в более простом использовании вне "for(:)",
 * т.е. изначально он был скорее обёрткой для Iterator'а. Однако этот интерфейс крайне
 * ограниченный: можно идти только вперёд поэлементно, нельзя начать сначала, нельзя
 * клонировать, нельзя проверить свойства (index, isFirst, isEmpty, isLast). Тем не
 * менее для перечисления в операторе for всё равно приходилось создавать ещё один
 * вспомогательный класс перечислителя, т.к. имена методов совпадают, а логика их работы
 * кардинально отличается: Enumerator.next пытается перейти к следующему элементу, а
 * Iterator.next не допускает такого и падает.
 * 
 * Сейчас потребности и пожелания в возможностях Enumerator'а ростут. Хочется сделать
 * его способным двигаться в обратном направлении, перепрыгивать элементы, удалять и
 * вставлять элементы, делать различные проверки, создавать клоны или сбрасывать.
 * Реализовать Iterator внутри Enumerator'а сложно из-за пересечения методов. Их можно
 * сделать так: hasNext(), next(), move(), item()
 * 
 * За отдельный класс Iterator'а:
 * + иначе получается сильный сбой в логике, метод #hasNext вынужден перейти
 *   к следующему элементу, чтобы ответить, существует ли он; при совмещении после
 *   #hasNext метод #item вернёт следующий элемент, и состояние элемента будет
 *   на следующем элементе; частично это можно решить выбросом исключения при попытке
 *   вызвать между #hasNext и #next какой-либо другой метод;
 * 
 * За совмещённый класс Iterable + Iterator:
 * + меньше потребление ресурсов, всё умещается в один объект;
 * + даже в исходной логике Java ошибка, т.к. Iterator содержит метод remove,
 *   но пользоваться им невозможно при создании Iterator'а в операторе for;
 */


