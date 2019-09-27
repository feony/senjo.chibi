/* Copyright 2016, 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.senjo.annotation.*;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import sun.misc.Unsafe;

/** Специфичные вспомогательные статические методы общего назначения.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2019-02-12, release */
@SuppressWarnings("rawtypes")
public class Helper {
	/** Метод упрощает написание своей реализации проверки на эквивалентность.
	 * Он выполняет все типичные начальные проверки и возвращает true, если результат
	 * проверки объектов на эквивалентность уже очевиден.
	 * <br/><i>Пример:</i>
	 * <pre><code> @Override public boolean equals(Object obj) {
	 *   if (prepareEquals(this, obj, true)) return this == obj;
	 *   final Type that = (Type)obj;
	 * 
	 *   return this.x == that.x && this.y == that.y;
	 * }</code></pre>
	 * @param model — текущий экземпляр класса;
	 * @param target — проверяемый на эквивалентность экземпляр;
	 * @param checkHashCode — если true, то дополнительно проверит совпадение hash-кодов
	 * двух объектов; если они не совпадут, то вернёт true — объекты не эквивалентны.
	 * @return Ответ уже очевиден, проверка не нужна. Если target того же класса, что
	 * и model; их хеши (если проверялись) совпадают, но значения указывают на разные
	 * объекты в памяти и требуется сравнение их содержимого, то возвращается false;
	 * иначе ответ уже очевиден и возвращается true. При ответе true объекты эквивалентны
	 * только в том случае, если они тождественно равны. */
	public static boolean prepareEquals(Object model, Object target, boolean checkHashCode){
		return target == model || target == null
				|| checkHashCode && model.hashCode() != target.hashCode()
				|| !model.getClass().isInstance(target); }

	/** Метод упрощает написание своей реализации проверки на эквивалентность.
	 * Он выполняет все типичные начальные проверки и возвращает типизированный объект
	 * target, если результат проверки объектов на эквивалентность ещё не очевиден и нужна
	 * дополнительное сравнение содержимого этих объектов.
	 * <br/><i>Пример:</i>
	 * <pre><code> @Override public boolean equals(Object obj) {
	 *   final Type that = prepareEquals(this, obj, true);
	 *   return that != null ? that.x == this.x && that.y == this.y : obj == this;
	 * }</code></pre>
	 * @param checkHashCode — если true, то дополнительно проверит совпадение hash-кодов
	 * двух объектов; если они не совпадут, то вернёт объект — т.е. объекты не эквивалентны.
	 * @return Если target того же класса, что и model; их хеши (если проверялись)
	 * совпадают, но значения указывают на разные объекты в памяти и требуется сравнение
	 * их содержимого, то возвращается target приведённый к классу model для дальнейшего
	 * сравнения; иначе ответ уже очевиден и возвращается null. При ответе null объекты
	 * эквивалентны только в том случае, если они тождественно равны. */
	@SuppressWarnings("unchecked")
	public static <T> T prepareEqualsEx( @Nullable T model, @Nullable Object target,
			boolean checkHashCode ) {
		return target == model || target == null || !model.getClass().isInstance(target)
					|| checkHashCode && model.hashCode() != target.hashCode()
				? null : (T)target; }

	/** Возвращает специфичный интерфейс Iterable для переданного массива. Его можно
	 * перебрать конструкцией {@code for(T item : source)}, но только один раз, т.к.
	 * фактически интерфейс сразу является и Iterator'ом. Применяется в подобных
	 * случаях:
	 * <pre><code> T[] array;
	 * List<T> list;
	 * ...
	 * Iterable<T> source = condition ? list : iterate(array);
	 * for (T item : source) {
	 *   ...
	 * }</code></pre> */
	public static <T> Iterable<T> iterate (@Nullable T[] array) {
		return new ArrayIterator<T>(array); }
	public static <T> Iterator<T> iterator(@Nullable T[] array) {
		return new ArrayIterator<T>(array); }
	public static <T> Iterable<T> iterate (@Nullable T[] array, int length) {
		return new ArrayIterator<T>(array, length); }
	public static <T> Iterator<T> iterator(@Nullable T[] array, int length) {
		return new ArrayIterator<T>(array, length); }

	private static class ArrayIterator<T> implements Iterable<T>, Iterator<T> {
		private final T[] array ;
		private final int length;
		private int nextIndex = -1;
		ArrayIterator(T[] array) { this(array, array != null ? array.length : 0); }
		ArrayIterator(T[] array, int length) { this.array  = array; this.length = length; }

		@Override public Iterator<T> iterator() {
			if (nextIndex >= 0) throw new RuntimeException("Can't get second");
			nextIndex = 0; return this; }
		@Override public boolean hasNext() { return nextIndex < length; }
		@Override public T next() { return array[nextIndex++]; }
	}

	public static <T> Iterable<T> iterateOne(@Nullable T item) {
		return new SingleIterator<T>(item); }
	public static <T> Iterator<T> iteratorOne(@Nullable T item) {
		return new SingleIterator<T>(item); }

	private static class SingleIterator<T> implements Iterable<T>, Iterator<T> {
		private final T item;
		private boolean closed;
		SingleIterator(T item) { this.item = item; }

		@Override public boolean hasNext() { return !closed; }
		@Override public T next() {
			if (closed) throw new NoSuchElementException();
			closed = true; return item; }
		@Override public Iterator<T> iterator() { return this; }
	}

	public static <T> Iterator<T> iteratorEmpty() { return Collections.emptyIterator(); }

	public static class IterableEx<T> extends ABasket implements Iterable<T> {
		private Iterable<T> source;
		private Iterator<T> target;
		IterableEx(Iterable<T> source) { this.source = source; }

		public boolean isEmpty() {
			if (exist(KnowEmpty)) return exist(IsEmpty);
			if (target == null) target = source.iterator();
			boolean result = target.hasNext();
			push(KnowEmpty | (result ? 0 : IsEmpty));
			return result; }

		@Override public Iterator<T> iterator() {
			if (target == null) return source.iterator();
			Iterator<T> result = target; target = null; return result; }

		private static final int KnowEmpty = 1 << 0;
		private static final int   IsEmpty = 1 << 1;
	}



	public static HashBuilder prepareHash() { return new HashBuilder(); }

//XXX Если метод приживётся, то документировать его
	public static HashBuilder prepareHash(@Nullable Object value) {
		return new HashBuilder().use(value); }

	public static class HashBuilder {
		private int result = 0;
		public HashBuilder use(Object value) {
			next(value != null ? value.hashCode() : 0); return this; }
		public HashBuilder use(int value) { next(value); return this; }
		public HashBuilder use(long value) {
			next((int)( value ^ (value >>> 32) )); return this; }

		private void next(int hash) {
			if (result != 0) result = result << 1 | result >>> 31;
			result ^= hash; }

		public int end() { return result; }
	}



//======== Вытаскивание защищённых системных библиотек Unsafe и Secret ===================//
	public static final Unsafe         unsafe = unsafeHack();
	public static final JavaLangAccess secret = SharedSecrets.getJavaLangAccess();
	public static final Vandal         vandal = new Vandal();

	private static Unsafe unsafeHack() { try {
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe)field.get(null);
	} catch (Exception ex) { throw Base.Illegal("Can't hack get for Unsafe. " + ex); } }

	public static int unsafeOffset(Class type, String fieldName) { try {
		return (int)unsafe.objectFieldOffset(type.getDeclaredField(fieldName));
	} catch (Exception ex) { throw Base.Illegal("Can't take offset for field. " + ex); } }

	public static long unsafeOffsetStatic(Class type, String fieldName) { try {
		return (int)unsafe.staticFieldOffset(type.getDeclaredField(fieldName));
	} catch (Exception ex) { throw Base.Illegal("Can't take offset for field. " + ex); } }

	/** Если в указанном поле нет значения (is null), то безопасно устанавливает новое
	 * значение в это поля. Гарантируется атамарность, т.е. если другой поток одновременно
	 * перед установкой успеет подложить новое значение вместо null, то данная установка
	 * будет отменена и возвращён false, чужое значение не потеряется затиранием. */
	public static <T> boolean unsafePush(Object instance, long offset, T value) {
		return unsafe.compareAndSwapObject(instance, offset, null, value); }

	/** Если в указанном поле есть значение (not null), то безопасно забирает и стирает
	 * его из поля. Гарантируется атамарность, т.е. если другой поток одновременно перед
	 * обнулением значения поля успеет подложить новое значение вместо старого, то будет
	 * возвращено и стёрто именно новое значение, оно не потеряется затиранием на null. */
	@SuppressWarnings("unchecked")
	public static <T> T unsafeTake(Object instance, long offset) {
		Object result = unsafe.getObjectVolatile(instance, offset);
		if (result == null) return null;
		while (!unsafe.compareAndSwapObject(instance, offset, result, null))
			result = unsafe.getObject(instance, (long)offset);
		return (T)result; }


	private final static int StackTraceOffset = unsafeOffset(Throwable.class, "stackTrace");

	/** Заполняет в исключение стек вызовов вырезая указанное число верхних записей. */
	static <Type extends Throwable> Type fillStackTrace(Type target, int consumeDepth) {
		if (consumeDepth <= 0) return target;
		int depth = Math.max(secret.getStackTraceDepth(target) - consumeDepth, 0);
		StackTraceElement[] result = new StackTraceElement[depth];

		while (--depth >= 0)
			result[depth] = secret.getStackTraceElement(target, depth + consumeDepth);
		unsafe.putObject(target, (long)StackTraceOffset, result);
		return target;
	}

	/** Хулиганский синглтон, который в целях ускорения производительности и экономии
	 * ресурсов выполняет над сторонними объектами небезопасные действия.
	 * Использовать только с полным пониманием производимых действий. */
	@SuppressWarnings("unchecked")
	public static class Vandal {
		private static int    stringGutOffset = unsafeOffset(String   .class, "value"      );
		private static int arrayListGutOffset = unsafeOffset(ArrayList.class, "elementData");
		public void setIndex(AEnumerator<?> target, int index) { target.index = index; }

		/** Выпотрошить жертву victim. Здесь извлекает из строки оригинал (не копию)
		 * зафиксированного массива символов, содержимое которого ни в коем случае
		 * не должно быть изменено.<br/>
		 * Преимущество: не тратится время и память на создание копии массива символов. */
		public char[] disembowel(@NotNull String victim) {
			return (char[])unsafe.getObject(victim, (long)stringGutOffset); }

		/** Выпотрошить жертву victim. Здесь извлекает из списка оригинал (не копию)
		 * массива данных, содержимое которого менять опасно и по логике извне меняться
		 * вообще не должно.<br/>
		 * Преимущество: не тратится время и память на создание копии массива данных. */
		public <T> T[] disembowel(ArrayList<T> victim) {
			return (T[])unsafe.getObject(victim, (long)arrayListGutOffset); }

		/** Обрезать стек выброса (исключения). Удаляет из стека выброса часть методов,
		 * чтобы казалось что экземпляр был создан ниже по стеку. Актуально с логической
		 * точки зрения, когда реально экземпляр создаётся гораздо глубже, чем произошло
		 * предшествующее тому событие, которое этим выбросом и описывается. */
		public <Type extends Throwable> Type cutStackTop(Type victim, int cutDepth) {
			return fillStackTrace(victim, cutDepth); }
	}
}


