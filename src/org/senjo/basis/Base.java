/* Copyright 2016, 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import java.time.Instant;
import java.util.*;
import org.senjo.annotation.*;

/** Примитивные базовые и необходимые практически в любом коде статические методы.
 * Подключаются как "import static *", используются без указания имени класса.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2018-10-12, release */
@SuppressWarnings("unchecked")
public final class Base {
/*TODO Exist и empty не совсем корректная пара, т.к. exist по смыслу подразумевает
 * существование самого объекта, а empty говорит о его содержимом. Можно заменить
 * на термины:
 * * exist => store (хранить, запасать), keep (хранить , держать   )
 * * empty => blank (пустой , чистый  ), idle (холостой, не занятый) */

	/** Возвращает истину, если переданная строка отсутствует (null) или пустая. */
	public static boolean empty(@Nullable String value) {
		return value == null || value.isEmpty(); }

	/** Возвращает истину, если переданная строка отсутствует (null) или пустая. */
	public static boolean isEmpty(@Nullable String value) {
		return value == null || value.isEmpty(); }

	/** Возвращает истину, если переданный массив символов отсутствует (null) или пустой. */
	public static boolean isEmpty(@Nullable char[] value) {
		return value == null || value.length == 0; }

	/** Возвращает истину, если любая из переданных строк отсутствует (null) или пустая. */
	public static boolean empty(@Nullable String value1, @Nullable String value2) {
		return value1 == null || value2 == null || value1.isEmpty() || value2.isEmpty(); }

	/** Возвращает истину, если переданный массив отсутствует (null) или пустой. */
	public static <T> boolean empty(@Nullable T[] value) {
		return value == null || value.length == 0; }

	/** Возвращает истину, если переданная коллекция отсутствует (null) или пустая. */
	public static boolean empty(@Nullable Collection<?> value) {
		return value == null || value.isEmpty(); }

	/** Возвращает истину, если переданная карта отсутствует (null) или пустая. */
	public static boolean empty(@Nullable Map<?,?> value) {
		return value == null || value.isEmpty(); }

	/** Возвращает истину, если переданное число отсутствует (null) или равно нулю. */
	public static boolean empty(@Nullable Long value) {
		return value == null || value == 0; }

	/** Возвращает истину, если переданное число отсутствует (null) или равно нулю. */
	public static boolean empty(@Nullable Integer value) {
		return value == null || value == 0; }


/*XXX В чём разница между exist и exists? Окончание -s в подобных словах ставится для третьего
 * лица единственного числа. Верно писать: He lives, she runs, it eats, he has, she goes,
 * it exists. Но в программировании это окончание как-то не к месту, не в нём суть, да и лицо
 * явно не задано: "ты" или "он"... Так что убрать эти окончания. */
	/** Возвращает истину, если переданная строка задана (not null) и не пустая. */
	public static boolean exists(@Nullable String value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если переданная строка задана (not null) и не пустая. */
	public static boolean isExist(@Nullable String value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если все переданные строки заданы (not null) и не пустые. */
	public static boolean exists(@Nullable String value1, @Nullable String value2) {
		return value1 != null && value2 != null && !value1.isEmpty() && !value2.isEmpty(); }

	/** Возвращает истину, если переданный массив задан (not null) и не пустой. */
	public static <T> boolean exists(@Nullable T[] value) {
		return value != null && value.length != 0; }

	/** Возвращает истину, если переданный массив задан (not null) и не пустой. */
	public static boolean exists(@Nullable int[] value) {
		return value != null && value.length != 0; }

	/** Возвращает истину, если переданная коллекция задана (not null) и не пустая. */
	public static boolean exists(@Nullable Collection<?> value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если переданная коллекция задана (not null) и не пустая. */
	public static boolean isExist(@Nullable Collection<?> value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если переданная карта задана (not null) и не пуста. */
	public static boolean exists(@Nullable Map<?,?> value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если переданная карта задана (not null) и не пуста. */
	public static boolean isExist(@Nullable Map<?,?> value) {
		return value != null && !value.isEmpty(); }

	/** Возвращает истину, если переданное число задано (not null) и не равно нулю. */
	public static boolean exists(@Nullable Long value) {
		return value != null && value != 0; }

	/** Возвращает истину, если переданное число задано (not null) и не равно нулю. */
	public static boolean exists(@Nullable Integer value) {
		return value != null && value != 0; }

	/** Возвращает истину, если переданный флаг задан (not null) и равен true. */
	public static boolean exists(@Nullable Boolean value) {
		return value != null && value.booleanValue(); }


	/** <pre>left = right</pre>
	 * <u>Equals</u> — возвращает true, если первое значение left эквивалентно второму значению
	 * right. Если оба значения null, то также возвращается true. */
	public static <T> boolean eq(@Nullable T left, @Nullable T right) {
		return left != null ? right != null && left.equals(right) : right == null; }

	/** <pre>left <> right</pre>
	 * <u>Not Equals</u> — возвращает true, если первое значение left не эквивалентно второму
	 * значению right. Если только одно из значений null, то также возвращается true. */
	public static <T> boolean ne(@Nullable T left, @Nullable T right) {
		return left != null ? right != null && !left.equals(right) : right != null; }

	/** <pre>left < right</pre>
	 * <u>Less Than</u> — возвращает true, если первое значение left меньше второго значения
	 * right. Если хотя бы одно из значений null, то возвращается false. */
	public static <T extends Comparable<T>> boolean lt(@Nullable T left, @Nullable T right) {
		return left != null && right != null && left.compareTo(right) < 0; }

	/** <pre>left <= right</pre>
	 * <u>Less than or Equal</u> — возвращает true, если первое значение left меньше или равно
	 * второму значению right. Если оба значения null, то также возвращается true. */
	public static <T extends Comparable<T>> boolean le(@Nullable T left, @Nullable T right) {
		return left != null ? right != null && left.compareTo(right) <= 0 : right == null; }

	/** <pre>left > right</pre>
	 * <u>Greater Than</u> — возвращает true, если первое значение left больше второго значения
	 * right. Если хотя бы одно из значений null, то возвращается false. */
	public static <T extends Comparable<T>> boolean gt(@Nullable T left, @Nullable T right) {
		return left != null && right != null && left.compareTo(right) > 0; }

	/** <pre>left >= right</pre>
	 * <u>Greater than or Equal</u> — возвращает true, если первое значение left больше
	 * или равно второму значению right. Если оба значения null, то также возвращается true. */
	public static <T extends Comparable<T>> boolean ge(@Nullable T left, @Nullable T right) {
		return left != null ? right != null && left.compareTo(right) >= 0 : right == null; }



	/** Аналог оператора assert. Требует истинного значения аргумента, иначе выбрасывает
	 * исключение недопустимного состояния.
	 * @throws IllegalStateException — недопустимо ложное условие */
	public static void argue(boolean conclusion) throws IllegalStateException {
		if (!conclusion) throw new IllegalStateException(); }

	/** Вписать значение в границы. Если target находится внутри границ min÷max, то возвращается
	 * его значение. Если target меньше min, то возвращается min. Если target больше max,
	 * то возвращается max. */
	public static int fitIntoBounds(int min, int target, int max) {
		return min <= target ? target <= max ? target : max : min; }

// Не добавляю этот метод, т.к. он провоцирует лениться прокомментировать исключение сообщением
//	/** Создаёт и возвращает новое исключение IllegalStateException. */
//	public static final IllegalStateException Illegal() throws IllegalStateException {
//		return new IllegalStateException(); }

	/** Создаёт и возвращает исключение {@link IllegalStateException} с указанным
	 * в аргументе сообщением. */
	public static IllegalStateException Illegal(String message) {
		return Helper.fillStackTrace(new IllegalStateException(message), 1); }

/*TODO Можно сделать, что этот метод никогда не будет возвращать исключение, а будет его
 * выбрасывать. Так и code assist будет работать, если написать перед методом throw,
 * и сокращённо можно писать без throw. */
	/** Создаёт и возвращает актуальное исключение по переданному аргументу. Если значение
	 * enum существует, то возвращает исключение {@link IllegalStateException} с указанным
	 * в аргументе сообщением:<br/> {@code «Unsupported state EnumType.name»};
	 * иначе возвращает исключение {@link NullPointerException} с сообщением:
	 * {@code «Unacceptable state null»} */
	public static <E extends Enum<?>> RuntimeException Illegal(@Nullable E value)
			throws IllegalStateException, NullPointerException {
		RuntimeException result = value == null
				? new NullPointerException("Unacceptable state null")
				: new IllegalStateException( "Unsupported state "
						+ value.getClass().getSimpleName() + '.' + value.name() );
		return Helper.fillStackTrace(result, 1); }

	/** Создаёт и возвращает исключение {@link IllegalStateException} с сообщением, что
	 * в корзинке под указанной маской хранится недопустимое состояние. */
	public static IllegalStateException Illegal(@NotNull ABasket basket, int mask) {
		return Helper.fillStackTrace(new IllegalStateException(
				"Illegal state of the basket under the mask " + Integer.toString(mask, 16)
					+ "=>" + Integer.toString(basket.mask(mask), 16) ), 1); }

	/** Создаёт и возвращает исключение {@link UnsupportedOperationException} с сообщением,
	 * что метод или часть кода ещё не реализованы. */
	public static UnsupportedOperationException Missing() {
		return Helper.fillStackTrace(new UnsupportedOperationException(
				"Method or part of code not yet implemented" ), 1); }



	/** Возвращает живое значение. Если переданная строка задана (not null),
	 * то возвращается она, иначе пустая строка. */
	public static @NotNull String live(@Nullable String value) {
		return value != null ? value : ""; }

	/** Возвращает живое значение. Если переданный список задан (not null), то возвращается
	 * он, иначе пустой список (только для чтения). */
	public static @NotNull <T> List<T> live(@Nullable List<T> value) {
		return value != null ? value : (List<T>)Collections.emptyList(); }

	/** Возвращает живое значение. Если переданная коллекция задана (not null), то возвращается
	 * она, иначе пустая коллекция (только для чтения). */
	public static @NotNull <T> Collection<T> live(@Nullable Collection<T> value) {
		return value != null ? value : (Collection<T>)Collections.emptyList(); }

	/** Возвращает живое значение. Если переданная карта задана (not null), то возвращается она,
	 * иначе пустая карта (только для чтения). */
	public static @NotNull <K, V> Map<K, V> live(@Nullable Map<K, V> value) {
		return value != null ? value : (Map<K,V>)Collections.emptyMap(); }

	/** Возвращает живое значение. Если переданная переменная задана (not null), то возвращается
	 * она, иначе создаётся и возвращается значение "0". */
	public static @NotNull Long live(@Nullable Long value) {
		return value != null ? value : 0L; }

	/** Возвращает живое значение. Если переданная переменная задана (not null), то возвращается
	 * она, иначе создаётся и возвращается значение "0". */
	public static @NotNull Integer live(@Nullable Integer value) {
		return value != null ? value : 0; }

	/** Возвращает живое значение. Если переданный флаг задан (not null), то возвращается он,
	 * иначе создаётся и возвращается значение "true". */
	public static @NotNull Boolean live(@Nullable Boolean value) {
		return value != null ? value : Boolean.TRUE; }

	/** Возвращает живое значение. Если переданный момент времени задан (not null), то
	 * возвращается он, иначе создаётся и возвращается текущий момент времени
	 * {@link Instant#now()}.
	 * @param value — момент времени, который требуется оживить по текущему моменту
	 * @return Гарантированный NotNull момент времени; в крайнем случае текущий момент времени*/
	public static @NotNull Instant live(@Nullable Instant value) {
		return value != null ? value : Instant.now(); }

	public static @Nullable String dead(@Nullable String value) {
		return exists(value) ? value : null; }

	/** Возвращает живое значение или последнее. Если первое значение задано (not null),
	 * то возвращается оно, иначе второе, даже если оно отсутствует (null). */
	public static @Nullable <T> T coalesce(@Nullable T value1, @Nullable T value2) {
		return value1 != null ? value1 : value2; }

	/** Сравнивает два объекта. Если один из них null, то он будет считаться наименьшим.
	 * Если оба объекта null, то они равны. */
	public static <T extends Comparable<T>> int compare(@Nullable T first, @Nullable T second) {
		return first == null ? second == null ? 0 : -1
				: second == null ? 1 : first.compareTo(second); }

	/** Возвращает первое not null значение или null по индексу из двух массивов. Если
	 * массив задан, то index не должен выходить за его границы. Если первый массив
	 * существует и его элемент index задан, то возвращается значение этого элемента.
	 * Иначе аналогично проверяется второй массив. Если и из него не удаётся получить
	 * значение, то возвращается null. */
	public static <T> T coalesce( int index, @Nullable T[] array1,
			@Nullable T[] array2 ) {
		if (array1 != null && array1[index] != null) return array1[index];
		if (array2 != null && array2[index] != null) return array2[index];
		return null; }

//XXX В идеологии самой Java "equal(null, null) == true". См. Objects#equals



	/** Сравнивает биты двух чисел по маске.
	 * @return true, если все биты, позиции коротых отмечены единицами в mask, совпадают
	 * в model и target. */
	public static boolean equal(int model, int target, int mask) {
		return ((model ^ target) & mask) == 0; }

	/** Сравнивает биты двух чисел по маске.
	 * @return true, если все биты, позиции коротых отмечены единицами в mask, совпадают
	 * в model и target. */
	public static boolean equal(long model, long target, long mask) {
		return ((model ^ target) & mask) == 0; }

	/** Сравнивает два объекта на эквивалентность {@code (null == null)}.
	 * Если оба объекта {@code null}, то возвращается {@code true}. */
	public static boolean equal(@Nullable Object model, @Nullable Object target) {
		return model != null && target != null
				? model.equals(target) : model == null ^ target != null; }

/*FIXME Ошибка в названии: данный метод сравнивает более жёстко, чем equal, но называется мягче.
 * Заменить на identical (тождественный, идентичный) или на same (такой же). */
	/** Сравнивает два объекта на существование и эквивалентность {@code (null != null)}.
	 * Если хотя бы один объект {@code null}, то возвращается {@code false}. */
	public static boolean alike(@Nullable Object model, @Nullable Object target) {
		return model != null && target != null ? model.equals(target) : false; }

	/** Сравнивает две строки на существование и эквивалентность {@code (null != null)}
	 * без учёта регистра символов. Если хотя бы один объект {@code null}, то возвращается
	 * {@code false}. */
	public static boolean alikeEasy(@Nullable String model, @Nullable String target) {
		return model != null && target != null ? model.equalsIgnoreCase(target) : false; }

	/** Сравнивает две строки на существование и эквивалентность {@code (null != null)}
	 * без учёта регистра символов. Если хотя бы один объект {@code null}, то возвращается
	 * {@code false}. Игнорирует указанные в аргументах символы.
	 * <pre>
	 * alikeEasy("this_is_any_cast", "tHisIsAnyCasT" , '_'     ) => true
	 * alikeEasy("run:the_stop_tag", "run_TheStopTag", '_', ':') => true</pre>
	 * @param ignores — символы, которые нужно игнорировать при сравнении двух строк */
	public static boolean alikeEasy( @Nullable String model, @Nullable String target,
			char ... ignores ) {
		if (model == null || target == null) return false;
		if (model.equals(target)) return true;
		int indexModel = -1, indexTarget = -1;
		int countModel = model.length(), countTarget = target.length();
		boolean finish = false;
		char charModel = 0, charTarget = 0;
		while (true) {
			skip: while (true) {
				if (++indexModel >= countModel) { finish = true; break; }
				charModel = model.charAt(indexModel);
				for (char ignore : ignores) if (ignore == charModel) continue skip;
				break; }
			skip: while (true) {
				if (++indexTarget >= countTarget) { finish = true; break; }
				charTarget = target.charAt(indexTarget);
				for (char ignore : ignores) if (ignore == charTarget) continue skip;
				break; }
			if (finish) break;
			if (Character.toLowerCase(charModel) != Character.toLowerCase(charTarget))
				return false;
		}
		return indexModel >= countModel && indexTarget >= countTarget;
	}

	/** Возвращает true если значение существует и больше нуля. Т.е. 0, как и null,
	 * считается отсутствием конкретного значения. */
	public static boolean positive(@Nullable Long value) {
		return value != null && value > 0; }

	/** Возвращает hashCode переданного объекта если он существует, или 0 если объекта
	 * не существует (null). */
	public static int hash(@Nullable Object value) {
		return value != null ? value.hashCode() : 0; }

	/** Возвращает скомбинированный hashCode переданной пары объектов, если они существуют,
	 * или 0, если объекты не существуют (null). */
	public static int hash(@Nullable Object first, @Nullable Object second) {
		int result = hash(first);
		if (result != 0) result = result << 1 | result >>> 31;
		return result ^ hash(second); }

	/** Если объект существует, то возвращает текстовое представление объекта по методу
	 * {@code toString()}, иначе возвращает текст "{@code <null>}". */
	public static @NotNull String str(@Nullable Object value) { return str(value, "<null>"); }

	/** Если объект существует, то возвращает текстовое представление объекта по методу
	 * {@code toString()}, иначе возвращает значение аргумента nullText. */
	public static @Nullable String str(@Nullable Object value, @Nullable String nullText) {
		return value != null ? value.toString() : nullText; }



	public static final void close(@Nullable AutoCloseable target) throws Exception {
		if (target != null) target.close(); }

	public static @Nullable String concat(@Nullable String first, @Nullable String second) {
		return concat(first, second, null); }
	public static @Nullable String concat( @Nullable String first, @Nullable String second,
			@Nullable String divider ) {
		if (first  == null) return second;
		if (second == null) return first ;
		return divider == null ? first + second : first + divider + second; }



	/** Преобразует коллекцию с объектными простыми данными в массив примитивов. */
	public static @NotNull int[] arrayOfInt(@Nullable Collection<Integer> source) {
		if (empty(source)) return EMPTY_INT_ARRAY;
		int[] result = new int[source.size()];
		int index = -1;
		for (int item : source) result[++index] = item;
		return result; }

	/** Преобразует коллекцию с объектными простыми данными в массив примитивов. */
	public static @NotNull short[] arrayOfShort(@Nullable Collection<Short> source) {
		if (empty(source)) return EMPTY_SHORT_ARRAY;
		short[] result = new short[source.size()];
		int index = -1;
		for (short item : source) result[++index] = item;
		return result; }

	/** Преобразует коллекцию с объектными простыми данными в массив примитивов. */
	public static @NotNull char[] arrayOfChar(@Nullable Collection<Character> source) {
		if (empty(source)) return EMPTY_CHAR_ARRAY;
		char[] result = new char[source.size()];
		int index = -1;
		for (char item : source) result[++index] = item;
		return result; }

	private static int  [] EMPTY_INT_ARRAY   = new int  [0];
	private static short[] EMPTY_SHORT_ARRAY = new short[0];
	private static char [] EMPTY_CHAR_ARRAY  = new char [0];

	/** Если source существует (not null) и в нём существуют хотя бы один элемент,
	 * то возвращает Iterator от него; иначе возвращает null. */
	public static @Nullable <T> Iterator<T> iterator(Iterable<T> source) {
		Iterator<T> result = source != null ? source.iterator() : null;
		return result != null && result.hasNext() ? result : null; }

	private static final Random random = new Random();
	public static int randomInt(         ) { return random.nextInt(     ); }
	public static int randomInt(int bound) { return random.nextInt(bound); }
	public static int randomInt(int startIn, int stopEx) {
		return random.nextInt(stopEx - startIn) + startIn; }



	/** Если target можно привести к классу type, то приводит и возвращает его,
	 * иначе возвращает null. */
	public static @Nullable <T> T as(@Nullable Object target, @NotNull Class<T> type) {
		return target != null && type.isInstance(target) ? (T)target : null; }



//	public static ValueSize sizeOf(@Nullable Object target) {
//		return ValueSize.sizeOf(target); }
//	public static ValueSize sizeOf(@Nullable Object target, ValueSize.Bitness bitness) {
//		return ValueSize.sizeOf(target, bitness); }
}


