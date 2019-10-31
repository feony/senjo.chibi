/* Copyright 2016, 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import java.lang.reflect.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import org.senjo.annotation.*;
import org.senjo.data.Phrase;

/** (временно сокращённая публичная версия библиотеки работы с текстом)
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2018-10-12, release */
public final class Text {
//======== Object into Text through reflection ===========================================//
	private static final String NullText = "null";
	/** Возвращает текстовое представление строки. Если строка существует, то
	 * оборачивает её содержимое в кавычки, иначе возвращает текст {@code null} без кавычек.*/
	public static @NotNull String text(@Nullable String value) {
		return value != null ? '"' + value + '"' : NullText; }
	/** Возвращает текстовое представление числа. Если число существует, то возвращает
	 * его текстовое представление, иначе возвращает текст {@code null} без кавычек. */
	public static @NotNull String text(@Nullable Long value) {
		return value != null ? Long.toString(value) : NullText; }
	/** Возвращает текстовое представление числа. Если число существует, то возвращает
	 * его текстовое представление, иначе возвращает текст {@code null}. */
	public static @NotNull String text(@Nullable Integer value) {
		return value != null ? Integer.toString(value) : NullText; }
	/** Возвращает текстовое представление значения epoch. */
	public static @NotNull String textEpoch(long epoch) {
		return text(Instant.ofEpochMilli(epoch)); }
	/** Возвращает текстовое представление момента времени. Если момент времени существует,
	 * то возвращает его текстовое представление, иначе возвращает текст {@code null}. */
	public static @NotNull String text(@Nullable TemporalAccessor value) {
		return value != null ? SimpleTextContext.instant.format(value) : NullText; }
	/** Возвращает простое текстовое представление объекта. Если объект существует, то
	 * преобразует его содержимое в текстовое представление с помощью заданного formatter'а
	 * в {@link #SimpleTextContext}} и оборачивает его в одинарные кавычки, иначе возвращает
	 * текст {@code null}. Также см. {@link #textEx(Object)}. */
	public static @NotNull String text(@Nullable Object value) {
		return inner_text(SimpleTextContext, null, value, SimpleTextContext.width); }
//	/** Возвращает простое текстовое представление объекта. Если объект не существует,
//	 * то возвращает текст из аргумента nullValue. */
//	public static @Nullable String text(@Nullable Object value, @Nullable String nullValue) {
//		return value == null ? nullValue : inner_text( SimpleTextContext, null, value,
//				SimpleTextContext.width ); }

	/** Возвращает расширенное текстовое представление объекта распознавая через
	 * отражение значения его полей и подобъектов. Если объект не существует,
	 * то возвращает текст «null» без кавычек. Использует контекст обработки по
	 * умолчанию. */
	public static @NotNull String textEx(@Nullable Object value) {
		return textEx(value, DefaultTextContext); }
	/** Возвращает расширенное текстовое представление объекта распознавая через отражение
	 * значения его полей и подобъектов. Если объект не существует, то возвращает текст
	 * {@code null} без кавычек. Использует указанный контекст обработки. */
	public static @NotNull String textEx(@Nullable Object value, @NotNull Context context) {
		StringBuilder result = new StringBuilder(48);
		inner_text(context, result, value, context.width);
		return result.toString(); }

	/** 
	 * DELME Сомневаюсь, что такая функция будет востребована. Если понадобится,
	 * то delme нужно убрать, иначе убрать саму функцию. */
	public static @NotNull StringBuilder textEx( @Nullable StringBuilder out,
			@Nullable Object value, @Nullable Context context ) {
		if (out     == null) out     = new StringBuilder();
		if (context == null) context = DefaultTextContext;
		inner_text(context, out, value, context.width);
		return out; }

//TODO Добавить учёт повторения объектов, нет смысла выводить их по нескольку раз, можно опционально через Context!
//XXX Возможно стоит вынести этот метод в отдельный класс...
	private static String inner_text( @NotNull Context context,
			@Nullable StringBuilder out, @Nullable Object value, int width ) {
		if (value == null)
			if (out == null) return NullText; else out.append((String)null);
		else if (value instanceof Number || value instanceof Boolean)
			if (out == null) return value.toString(); else out.append(value.toString());
		else if (value instanceof String)
			if (out == null) return '"' + (String)value + '"';
			else out.append('"').append((String)value).append('"');
		else if (value instanceof Enum)
			if (out == null) return value.getClass().getSimpleName() + '#' + value;
			else out.append(value.getClass().getSimpleName()).append('#').append(value);
		else if (value instanceof TemporalAccessor)
			if (out == null) return '\'' + context.format((TemporalAccessor)value) + '\'';
			else out.append('\'').append(context.format((TemporalAccessor)value)).append('\'');
		else if (value instanceof Collection)
			if (out == null) return '@' + value.getClass().getSimpleName() + '[' + ']';
			else { //XXX Если width==0, то не нужно разбирать список, просто написать его тип
				out.append('[');
				int limit = context.collectionLimit;
				Iterator iterator = ((Iterable)value).iterator();
				if (iterator.hasNext()) {
					inner_text(context, out, iterator.next(), width-1);
					while (iterator.hasNext()) {
						out.append(',').append(' ');
						if (--limit == 0) { out.append('…'); break; }
						inner_text(context, out, iterator.next(), width); }
				}
				out.append(']'); }
		else {
			Class<?> type = value.getClass();
			if (type.equals(Object.class)) if (out == null) return "@Object";
				else { out.append("@Object"); return null; }

			Method toStringMethod;
			try { toStringMethod = type.getDeclaredMethod("toString"); }
			catch (NoSuchMethodException ex) { toStringMethod = null; }
			if (toStringMethod != null) if (out == null) return value.toString();
				else { out.append('\'').append(value.toString()).append('\''); return null; }

			if (out == null) return '@' + value.getClass().getSimpleName();
			if (context.depth <= 0 || width <= 0) {
				out.append('@').append(value.getClass().getSimpleName()); return null; }

			if (type.isArray()) {
				out.append('[');
				int count = Array.getLength(value), limit = context.limitArray(count);
				for (int index = 0; index < limit; ++index) {
					if (index > 0) out.append(',').append(' ');
					inner_text(context, out, Array.get(value, index), width-1); }
				if (limit != count) out.append(',').append(' ').append('…');
				out.append(']'); return null; }

			char delimiter = 0;
			out.append('{');
			int l_depth = context.depth;
			do {
				Field[] fields = type.getDeclaredFields();
				Field.setAccessible(fields, true);
				for (Field field : fields) {
					if (Modifier.isStatic(field.getModifiers())) continue;
					if (delimiter != 0) out.append(delimiter).append(' ');
					delimiter = ',';
					out.append(field.getName()).append(':').append(' ');
					try { inner_text(context, out, field.get(value), width-1); }
					catch (IllegalAccessException ex) { out.append("<no access>"); }
				}
				if (delimiter != 0) delimiter = ';';
				type = type.getSuperclass();
				if (type.equals(Object.class)) break;
			} while (--l_depth > 0);
			if (l_depth <= 0) {
				if (delimiter != 0) out.append(delimiter).append(' ');
				out.append('…'); }
			out.append('}'); }
		return null;
	}


	public static final Context Context() { return DefaultTextContext.clone(); }
	public static final Context DefaultTextContext = new Context();
	public static final Context  SimpleTextContext = Context().bounds(0, 0);

	public static final class Context extends ABasket implements Cloneable {
		/** См. {@linkplain #width(int)} */
		private byte width = 2;
		/** См. {@linkplain #depth(int)} */
		private byte depth = 3;
//		/** Поле на будущее для расчёта объёма памяти занимаемого объектом. */
//		private byte incDepthForSizeOf = 0;
		/** Ограничение на отображаемое количество элементов из массива */
		private short arrayLimit      = 64;
		/** Ограничение на отображаемое количество элементов из любой коллекции */
		private short collectionLimit = 64;
//		/** Ограничение на отображаемое количество элементов из карты */
//		private short mapLimit        = 24;
//		/** Автоматический перенос строк, ширина текста */
//		private short lineWidth       = 88;
//		/** Скрывать количество перечисляемых элементов, если их число меньше указанного */
//		private short iterationCountHide = 5;
//		private NumberFormat number = NumberFormat.getInstance();
		private DateTimeFormatter instant = DateTimeFormatter.ISO_LOCAL_DATE_TIME
				.withZone(ZoneId.systemDefault());

		private Context() { }

		/** Ограничение ширины разбора объекта. Максимальное число вложенных переходов
		 * от очередного текущего объекта к подобъектам по ссылкам, хранящимся в полях
		 * текущего объекта. */
		public Context width(int value) { width = (byte)value; return this; }
		/** Ограничение глубины разбора объекта. Максимальное число вложенных переходов
		 * на родительский класс от очередного текущего класса для поиска полей с данными
		 * или реализованного метода {@link #toString}. Если глубина разбираемого объекта
		 * больше заданного ограничения, то в итог запишется троиточие. Ограничение глубины
		 * действует на объекты по всей ширине. */
		public Context depth(int value) { depth = (byte)value; return this; }
		/** Максимальные границы разбора.
		 * @param width — максимальное число переходов по ссылкам полей объекта
		 * @param depth — максимальное число переходов в иерархии наследования */
		public Context bounds(int width, int depth) {
			this.width = (byte)width; this.depth = (byte)depth; return this; }
		/** Ограничение на отображаемое количество элементов из массива. */
		public Context array     (int limit) { arrayLimit     = (short)limit; return this; }
		/** Ограничение на отображаемое количество элементов из любой коллекции. */
		public Context collection(int limit) { collectionLimit= (short)limit; return this; }
//		/** Ограничение на отображаемое количество элементов из карты. */
//		public Context map       (int limit) { mapLimit       = (short)limit; return this; }
//		public Context limitAll  (int limit) {
//			arrayLimit = collectionLimit = mapLimit = (short)limit; return this; }
//		public Context multiline (int width) {
//			if (width > 0) { push(FormatMultiline); lineWidth = (short)width; }
//			else take(FormatMultiline);
//			return this; }
		public Context multiline (boolean enabled) {
			turn(FormatMultiline, enabled); return this; }

//		public Context number(NumberFormat value) { number = value; return this; }
//		public Context number(String pattern) {
//			number = new DecimalFormat(pattern); return this; }

		public Context useToString      (boolean enabled) {
			turn(UseToString      , enabled); return this; }
		public Context markClassToString(boolean enabled) {
			turn(MarkClassToString, enabled); return this; }
		public Context markClassArray   (boolean enabled) {
			turn(MarkClassArray   , enabled); return this; }
		public Context markClassAsPrefix(boolean enabled) {
			turn(MarkClassAsPrefix, enabled); return this; }

		@Override public Context clone() {
			try { return (Context)super.clone(); }
			catch (CloneNotSupportedException ex) {
				throw new RuntimeException("Can't clone cloneable class", ex); } }

		private int limitArray(int length) {
			return 0 < arrayLimit&&arrayLimit < length ? arrayLimit : length; }

		private String format(TemporalAccessor value) { return instant.format(value); }
//		private String format(double           value) { return number .format(value); }
//		private String format(long             value) { return number .format(value); }

		private static final int UseToString         = 1 << 0;
		/** Приписывать имя класса к объектам отображаемых через их реализацию метода
		 * {@link Object#toString()} */
		private static final int MarkClassToString   = 1 << 1;
		/** Приписывать имя класса к массивам */
		private static final int MarkClassArray      = 1 << 2;
//		/** Приписывать имя класса к коллекциям */
//		private static final int MarkClassCollection = 1 << 3;
//		/** Приписывать имя класса к картам */
//		private static final int MarkClassMap        = 1 << 4;
//		/** Приписывать имя класса к примитивным и базовым классам кроме boolean, int и float:
//		 * Number, String, Instant, etc. */
//		private static final int MarkClassCommon     = 1 << 5;
//		/** Приписывать имя класса к перечисляемым значениям Enum */
//		private static final int MarkClassEnum       = 1 << 6;
//		/** Приписывать имя класса к остальным классам */
//		private static final int MarkClassOther      = 1 << 7;
		/** Приписывать имя класса перед, а не после значения */
		private static final int MarkClassAsPrefix   = 1 << 8;
		/** Формировать многострочный форматированный текст */
		private static final int FormatMultiline     = 1 << 9;
//		/** Выводить размер перебираемого объекта, если он больше заданного
//		 * в {@link iterationCountHide} */
//		private static final int IterationCount      = 1 <<10;
	}
//^^^^^^^^ Object into Text ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//



//======== An Object into short hash text ================================================//
	/** Возвращает имя класса и краткую приписку части хеш кода переданного экземпляра. */
	public static @NotNull String hashName(@Nullable Object target) {
		return hashName(new StringBuilder(24), target, 3).toString(); }

	/** Возвращает имя класса и краткую приписку части хеш кода переданного экземпляра.
	 * @param suffixWidth — ширина суффикса в символах, должна быть больше нуля. */
	public static @NotNull String hashName(@Nullable Object target, int suffixWidth) {
		return hashName(new StringBuilder(24), target, suffixWidth).toString(); }

	public static StringBuilder hashName( @NotNull StringBuilder out,
			@Nullable Object target ) { return hashName(out, target, 3); }

	public static StringBuilder hashName( @NotNull StringBuilder out,
			@Nullable Object target, int suffixWidth ) {
		if (target == null) { out.append("null"); return out; }
		out.append(target.getClass().getSimpleName()).append('•');
		int hash = target.hashCode() & ~(1<<31);
		do { // Равновероятно определяет тип символа: цифра, знак, буква,
			int seek, size, part = hash & 0x3; hash >>>= 2; // ...буква с засечками.
			switch (part) {
			case  0: seek =  0; size = 10; break;
			case  1: seek = 10; size = 34; break;
			case  2: seek = 44; size = 52; break;
			default: seek = 96; size =104; break; }
			out.append(HashDump[seek + hash % size]);
			if (--suffixWidth == 0) return out;
			hash /= size;
		} while (true);
	}

	private static final char[] HashDump =
			( "0123456789!@#$%^&*№?;:~=≠≤≥+-±×÷/|\\¢£¤¥€§‼∆∑"
			+ "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
			+ "ĀāḀḁḂḃḆḇĈĉḈḉḊḋḎḏĒēḘḙḞḟƑƒḠḡĢģĤĥḤḥĪīĮįĴĵǇǉḰḱĶķĹĺḺḻḾḿṂṃ"
			+ "ŃńṈṉŌōỌọṔṕṖṗɊɋǬǭŔŕṞṟŚśṨṩŤťṮṯŪūṲṳṼṽṾṿẂẃẈẉẊẋẌẍÝýẎẏẐẑẔẕ" ).toCharArray();
//^^^^^^^^ An Object into short hash text ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//



//======== Text alingment ================================================================//
	public static String align(long text, int size, char space) {
		return align(Long.toString(text), size, false, 1f, space); }
	public static String align(@NotNull String text, int length) {
		return align(text, length, false, 1f, ' '); }
	public static String align(@NotNull String text, int length, boolean crop) {
		return align(text, length,  crop, 1f, ' '); }
	public static String align(@NotNull String text, int length, float align) {
		return align(text, length, false, align, ' '); }
	public static String align( @NotNull String text, int length, boolean crop,
			float align ) { return align(text, length,  crop, align, ' '); }
	/** Выравнивает указанный текст в области указанного размера заполняя оставшееся
	 * пространство указанным символом. Примеры:
	 * <code><pre> align("Hello", 10)                         => "     Hello"
	 * align("world", 9, false, .5)               => "  world  "
	 * align("System error message", 10, true, 0) => "System err"
	 * align(" Caption ", 15, true, .5, '=')      => "=== Caption ==="
	 * </pre></code>
	 * @param text — исходный текст, который нужно выравнить
	 * @param length — длина текстовой области, в которой нужно выравнить заданный текст
	 * @param crop [false] — обрезка исходного текста, если он не помещается в область
	 * заданного размера. При длине (размере) области меньше, чем длина текста:
	 * если true, то размер области не изменится, а непоместившийся текст отсечётся
	 * в соответствии с заданным выравниванием, иначе вернётся исходная строка,
	 * т.е. область увеличится до длины заданного текста.
	 * @param align [1f] — координация выравнивания: 0f – по левому краю,
	 * 1f – по правому краю, 0.5f – по центру, 1.5f – вытеснит текст за правую границу
	 * области на половину длины области.
	 * @param space [' '] — символ заполнения свободной от располагаемого текстом
	 * области. */
	public static String align( @NotNull String text, int length, boolean crop,
			float align, char space ) {
		int textLength = text.length();
		if (textLength == length || !crop && length < textLength) return text;

		int left = (int)((length - textLength) * align), right = left+textLength;
		if (left <= 0 && length <= right) return text.substring(-left, -left + length);

		final char[] result = new char[length], textData = text.toCharArray();

		for (int index = 0; index < length; index++)
			result[index] = left <= index && index < right
					? textData[index - left] : space;
		return new String(result);
	}

	public static String align(int length) { return align(length, ' '); }
	public static String align(int length, char space) {
		final char[] result = new char[length];
		while (--length >= 0) result[length] = space;
		return new String(result); }

//^^^^^^^^ Text alingment ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//



	private static char[] mark = new char[] {' ','k','M','G','T','P','E','Z','Y'};
	public static String shortNumber(final long value) {
		if (value < 0) return " ??? ";
		int charCount = 0;
		long l_value = value;
		while (l_value != 0) { l_value /= 10; charCount++; }

		l_value = value;
		int index = 4;
		final char[] result = new char[5];
		result[index] = mark[(charCount-1) / 3];
		if (charCount <= 3) {
			do {
				result[--index] = (char)('0' + l_value % 10);
				l_value /= 10;
			} while (l_value != 0);
			while (--index >= 0) result[index] = ' ';
		} else {
			for (int i = charCount - 4; i >= 0; i--) l_value /= 10;
			int point = (charCount-1) % 3 + 1;
			while (--index >= 0) if (point != index) {
				result[index] = (char)('0' + l_value % 10);
				l_value /= 10;
			} else result[index] = '.';
		}
		return new String(result);
	}

	public static Phrase phrase() { return new Phrase(); }
	public static Phrase phrase(String text) { return new Phrase().add(text); }
}


