/* Copyright 2018, 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.data;

import static org.senjo.basis.Base.Illegal;
import static org.senjo.basis.Helper.vandal;
import static org.senjo.basis.Text.textInstance;

import org.senjo.annotation.*;
import org.senjo.basis.ABasket;

/**
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2019-03-14, beta */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class APhrase<This extends APhrase, Result> extends ABasket {
	private final StringBuilder out;
	private long number;

	protected abstract Result apply(CharSequence data);

	protected APhrase(int capacity) { out = new StringBuilder(capacity); }
	protected APhrase(StringBuilder out) { this.out = out; }

	public final This add(String text ) { out.append(text ); return (This)this; }
	public final This add(char   ch   ) { out.append(ch   ); return (This)this; }
	public final This add(char ch1, char ch2) {
		out.append(ch1).append(ch2); return (This)this; }
	public final This add(long   value) { out.append(value); return (This)this; }
	public final This hex(int value) {
		out.append(Integer.toHexString(value)); return (This)this; }
	public final This format(String format, Object ... args) {
		out.append(String.format(format, args)); return (This)this; }
	public final This div(char ch) { if (!push(Divider)) out.append(ch); return (This)this; }
	public final This div(char ch1, char ch2) {
		if (!push(Divider)) out.append(ch1).append(ch2); return (This)this; }
	public final This rediv() { take(Divider); return (This)this; }
	public final This instance(Object target) { textInstance(out, target); return (This)this; }

	public final This put(String text) {
		if (text != null) out.append(text); return (This)this; }
	public final This put(char ch) {
		if (ch != '\0') out.append(ch); return (This)this; }
	public final This put(char ch1, char ch2) {
		if (ch1 != '\0') out.append(ch1);
		if (ch2 != '\0') out.append(ch2); return (This)this; }

	public final This set(long number) {
		this.number = number;
		if (number < 0) number = -number;
		int mod = (int)(number % 10);
		turn( mForm, (mod-1 & ~3) != 0 || (number%100 & ~7) == 8
				? FormPlural : mod == 1 ? FormSingle : FormDual );
		return (This)this; }
	public final This number(long number) { set(number); add(number); return (This)this; }
	public final This number() { add(number); return (This)this; }
	public final This number(boolean spaces) {
		if (spaces) out.append(' '); out.append(number);
		if (spaces) out.append(' '); return (This)this; }

	private final This _form( @Nullable String single, @Nullable String dual,
			@Nullable String plural ) { String text;
		switch (mask(mForm)) {
		case FormSingle: text = single; break;
		case FormDual  : text = dual  ; break;
		case FormPlural: text = plural; break;
		default: throw Illegal(this, mForm); }
		put(text); return (This)this; }
	public final This form(String single, String plural) {
		return _form(single, plural, plural); }
	public final This form( @Nullable String word, @Nullable String single,
			@Nullable String plural) { put(word); return _form(single, plural, plural); }

	/** Строит числовую фразу подставляя правильное окончание слов по отношению к числу.
	 * Число предварительно пишется методом {@link #number(long)} или тихо устанавливается
	 * методом {@link #set(long)}.
	 * <p/>Пример фраз: «Созрел 1 подсолнух», «Созрели 4 подсолнуха», «Созрели 300
	 * подсолнухов». Использование:<br/>{@code #set(21).form("Созрел",
	 * "","и").number(true).form("подсолнух", "","а","ов").end();}
	 * @param single — окончание слова для единственного числа;
	 * @param dual   — окончание слова для двойственного числа;
	 * @param plural — окончание слова для множественного числа. */
	public final This form( @Nullable String word, @Nullable String single,
			@Nullable String dual, @Nullable String plural ) {
		out.append(word); return _form(single, dual, plural); }

	public final This form(long number, @NotNull String format) {
		int code;
		{	long value = number >= 0 ? number : -number;
			int mod = (int)(value % 10);
			code = (mod-1 & ~3) != 0 || (value%100 & ~7) == 8 ? 2 : mod == 1 ? 0 : 1; }

		char[] data = vandal.disembowel(format);
		int start = 0, cell = 0, write = 0;
		boolean opened = false;
		for (int index = 0, stop = data.length; index != stop; ++index) {
			char ch = data[index];
			if (opened) {
				switch (ch) {
				case ']': if (write == 0) { index = start; write = 1; }
				          else opened = false;
				          break;
				case '@': out.append(number); write = 2; break;
				case '|': start = index; ++cell;
				          if (write != 0 || cell == code) ++write; break;
				default:  if (write == 1) out.append(ch); }
			} else if (ch == '[') { opened = true;
				start = index; cell = 0; write = code == 0 ? 1 : 0;
			} else out.append(ch);
		}
		return (This)this;
	}

	/** Добавляет текст в зависимости от значения установленного числа. Если число
	 * в диапазоне от min до max, то подставляет текст под индексом value-min,
	 * иначе подставляет текст с последним индексом. */
	public final This choise( @NotNull Choise mode, int min, int max, String ... texts ) {
		long value = number;
		if (min <= value&&value <= max) out.append(texts[(int)(value-min)]);
		else switch (mode) {
		case Number: number();                   break;
		case Last  : put(texts[texts.length-1]); break;
		default    : throw Illegal(mode); }
		return (This)this;
	}

	public final Result end() { return apply(out); }
	public final Result end(String text) { return apply(out.append(text)); }
	public final Result end(char ch) { return apply(out.append(ch)); }

	public enum Choise { Number, Last }


//======== Basket : Постоянные для корзинки фруктов ======================================//
	protected static final int fin = ABasket.fin-3;

	private static final int Divider    = 1<<fin+1;
	private static final int mForm      = 3<<fin+2;
	private static final int FormSingle = 1<<fin+2;
	private static final int FormDual   = 2<<fin+2;
	private static final int FormPlural = 3<<fin+2;
}


