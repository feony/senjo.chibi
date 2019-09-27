/* Copyright 2016, 2018, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import org.senjo.annotation.*;

/** (временно сокращённая публичная версия библиотеки работы с текстом)
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version 2016, change 2018-10-12, release */
public final class Text {
	/** Возвращает имя класса и краткую приписку части хеш кода переданного экземпляра. */
	public static @NotNull String textInstance(@Nullable Object target) {
		return textInstance(new StringBuilder(24), target, 3).toString(); }

	/** Возвращает имя класса и краткую приписку части хеш кода переданного экземпляра.
	 * @param suffixWidth — ширина суффикса в символах, должна быть больше нуля. */
	public static @NotNull String textInstance(@Nullable Object target, int suffixWidth) {
		return textInstance(new StringBuilder(24), target, suffixWidth).toString(); }

	public static StringBuilder textInstance( @NotNull StringBuilder out,
			@Nullable Object target ) { return textInstance(out, target, 3); }

	public static StringBuilder textInstance( @NotNull StringBuilder out,
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
}


