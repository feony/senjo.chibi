/* Copyright 2018-2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.support;

import static org.senjo.basis.Helper.*;
import static org.senjo.support.Log.Level.*;
import org.senjo.annotation.*;
import org.senjo.data.APhrase;

/** Абстрактный интерфейс журнала. Реализуется пользователем в удобном для него виде,
 * используется ядром системы для записей в журнал пользователя.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-01-20, change 2019-03-05, fix 2019-03-14 */
public abstract class Log {
	/** Предел задаётся жёстко в самом начале и должен помочь интерпретатору выбрасывать
	 * часть кода, которая из-за этой константы никогда не будет выполняться. */
	private final int limit;
	/** Текущий режим работы указывает, какие уровни журналирования ожидаются в настоящий
	 * момент. В любой момент пользователь может изменить уровень журналирования. */
	private int mode = 99;

	protected Log() { this(99, 99); }
	protected Log(int mode, int limit) { this.mode = mode; this.limit = limit; }

/*         10             9     8     7     5      4
 *       SEVERE       |WARNIN|INFO|CONFIG|FINE | FINER
 * FATAL, Fault, Error, Warn, info, hint, trace, debug, easy.
 *                 10     9     8     7     5      4      1
 *          15           14  13-12 11-10   9-7     6
 * 
 * 
 * 1000÷... SEVERE  
 * 900÷1000 WARNING 11100÷11111 -> 896÷992 -> 992 WARNING
 * 800÷ 900 INFO    11001÷11100 -> 800÷896 -> 800 INFO
 * 700÷ 800 CONFIG  10101÷11001 -> 672÷800 ->
 * 
 * 1000÷... SEVERE  
 * 900÷1000 WARNING 1110÷1111 -> 896÷960 -> 960 WARNING
 * 800÷ 900 INFO    1100÷1110 -> 768÷896 -> 896 INFO
 * 700÷ 800 CONFIG  1010÷1100 -> 640÷768 -> 768 CONFIG */

	/**
	 * @param level — уровень данной записи в журнале;
	 * @param point — точка кода, из которой ведётся запись;
	 * @param message — полезное сообщение записываемое в журнал;
	 * @param ex — выброс, содержит исключение или просто {@link Stack} для трассировки. */
	protected abstract void log( @NotNull Level level, @NotNull StackTraceElement point,
			@Nullable String message, @Nullable Throwable ex );
	public final void log(Level level, int depth, String message, Throwable error) {
		if (!need(level)) return;
		log(level, secret.getStackTraceElement(new Throwable(), depth+1), message, error); }
	public final void log(Level level, int depth, String message, boolean trace) {
		if (!need(level)) return;
		Throwable ex = trace ? new Stack(depth+1) : new Throwable();
		log(level, secret.getStackTraceElement(ex, depth+1), message, trace ? ex : null); }

	public final boolean need(Level level) {
		final int rate = level.ordinal(); return rate <= limit && rate <= mode; }
	public final boolean isInfo () { return need(Info ); }
	public final boolean isTrace() { return need(Trace); }
	public final boolean isDebug() { return need(Debug); }

	public final void fatal(String message) { log(Fatal, 1, message, null); }
	public final void fault(String message) { log(Fault, 1, message, null); }
	public final void error(String message) { log(Error, 1, message, null); }
//	public final void alert(String message) { log(Alert, 1, message, null); }
	public final void warn (String message) { log(Warn , 1, message, null); }
	public final void info (String message) { log(Info , 1, message, null); }
	public final void hint (String message) { log(Hint , 1, message, null); }
	public final void trace(String message) { log(Trace, 1, message, null); }
	public final void debug(String message) { log(Debug, 1, message, null); }
	public final void log(Level level, String message) { log(level, 1, message, null); }

	public final void fatal(String message, Throwable ex) { log(Fatal, 1, message, ex); }
	public final void fault(String message, Throwable ex) { log(Fault, 1, message, ex); }
	public final void error(String message, Throwable ex) { log(Error, 1, message, ex); }
	public final void warn (String message, Throwable ex) { log(Warn , 1, message, ex); }
	public final void info (String message, Throwable ex) { log(Info , 1, message, ex); }
	public final void hint (String message, Throwable ex) { log(Hint , 1, message, ex); }
	public final void trace(String message, Throwable ex) { log(Trace, 1, message, ex); }
	public final void debug(String message, Throwable ex) { log(Debug, 1, message, ex); }
	public final void log(Level level, String message, Throwable ex) {
		log(level, 1, message, ex); }

	public final void fatal(String message, boolean trace) { log(Fatal, 1, message, trace);}
	public final void fault(String message, boolean trace) { log(Fault, 1, message, trace);}
	public final void error(String message, boolean trace) { log(Error, 1, message, trace);}
	public final void warn (String message, boolean trace) { log(Warn , 1, message, trace);}
	public final void info (String message, boolean trace) { log(Info , 1, message, trace);}
	public final void hint (String message, boolean trace) { log(Hint , 1, message, trace);}
	public final void trace(String message, boolean trace) { log(Trace, 1, message, trace);}
	public final void debug(String message, boolean trace) { log(Debug, 1, message, trace);}
	public final void log(Level level, String message, boolean trace) {
		log(level, 1, message, trace); }

	public Buffer fatalEx(             ) { return new Buffer(Fatal, 1); }
	public Buffer fatalEx(String prefix) { return new Buffer(Fatal, 1).add(prefix); }
	public Buffer fatalEx(Throwable ex ) { return new Buffer(Fatal, 1, ex); }
	public Buffer faultEx(             ) { return new Buffer(Fault, 1); }
	public Buffer faultEx(String prefix) { return new Buffer(Fault, 1).add(prefix); }
	public Buffer warnEx (             ) { return new Buffer(Warn , 1); }
	public Buffer warnEx (String prefix) { return new Buffer(Warn , 1).add(prefix); }
	public Buffer infoEx (             ) { return new Buffer(Info , 1); }
	public Buffer infoEx (String prefix) { return new Buffer(Info , 1).add(prefix); }
	public Buffer hintEx (             ) { return new Buffer(Hint , 1); }
	public Buffer hintEx (String prefix) { return new Buffer(Hint , 1).add(prefix); }
	public Buffer traceEx(             ) { return new Buffer(Trace, 1); }
	public Buffer traceEx(String prefix) { return new Buffer(Trace, 1).add(prefix); }
	public Buffer debugEx(             ) { return new Buffer(Debug, 1); }
	public Buffer debugEx(String prefix) { return new Buffer(Debug, 1).add(prefix); }
	public Buffer logEx(Level level) { return new Buffer(level, 1); }
	public Buffer logEx(Level level, String prefix) {
		return new Buffer(level, 1).add(prefix); }

	static final Log hollow = new Log(-1, -1) {
		@Override public void log( Level level, StackTraceElement point, String message,
				Throwable ex) { }
		};

	static final Log console = new Log(99, 99) {
		@Override public void log( Level level, StackTraceElement point, String message,
				Throwable ex ) {
			System.out.println(level.text + ": " + message);
			if (ex != null) ex.printStackTrace(System.err); }
	};

	public final class Buffer extends APhrase<Buffer, Void> {
		private final Level level;
		private StackTraceElement point;
		private Throwable ex;

		Buffer(Level level, int depth               ) { this(level, depth, false, null); }
		Buffer(Level level, int depth, boolean trace) { this(level, depth, trace, null); }
		Buffer(Level level, int depth, Throwable ex ) { this(level, depth, false,  ex ); }
		private Buffer(Level level, int depth, boolean trace, Throwable ex) { super(64);
			this.level = level;
			this.ex    = trace ? new Stack(depth+2) : ex;
			this.point = secret.getStackTraceElement(trace ? ex : new Throwable(), depth+2);
		}

		@Override protected Void apply(CharSequence data) {
			if (!need(level)) return null;
			log(level, point, data.toString(), ex); return null; }

		public Buffer trace (  ) { this.ex = new Stack(1); return this; }
		public Buffer thrown(Throwable ex) { this.ex = ex; return this; }
	}

	public enum Level {
		/** Фатальный сбой, который нарушает работу всей системы вообще */  Fatal("FATAL"),
		/** Программный сбой из-за ошибок в алгоритме программы */          Fault("FAULT"),
		/** Ошибка не в коде: сбой данных, настроек, доступов и т.п. */     Error("Error"),
		/** Предупреждение о штатном допустимом сбое: нет сигнала и т.п. */ Warn (" Warn"),
		/** Информативное полезное сообщение в процессе работы системы */   Info (" info"),
		/** Расширенная вспомогательная информация о настройках и данных */ Hint (" hint"),
		/** Подробное отслеживание процесса изменения и анализа данных */   Trace("trace"),
		/** Детальная информация о внутренних процессах для отладки кода */ Debug("debug"),
		/** Прочая избыточная информация */                                 Easy (" easy");

		public final String text;
		private Level(String text) { this.text = text; }
	}

	static @NotNull Log    instance = Log.console;
	public static @NotNull Log instance() { return instance; }

	protected static void initialize(Log mainLog) {
		Log.instance = mainLog != null ? mainLog : hollow; }


	protected static final class Stack extends Throwable {
		private static final long serialVersionUID = 1L;
		public final int cutDepth;
		private StackTraceElement[] stack;

		Stack(int cutDepth) { this.cutDepth = cutDepth; }

		@Override public StackTraceElement[] getStackTrace() {
			if (stack != null) return stack;

			int depth = secret.getStackTraceDepth(this);
			StackTraceElement[] result = new StackTraceElement[depth - cutDepth];
			for (int index = cutDepth; index != depth; ++index)
				result[index-cutDepth] = secret.getStackTraceElement(this, index);
			return this.stack = result; }

		@Override public void setStackTrace(StackTraceElement[] stack) {
			this.stack = stack; }

		public void printStack(StringBuilder out) {
			int depth = secret.getStackTraceDepth(this);
			out.append("trace:");
			for (int index = cutDepth; index != depth; ++index) out.append("\tat ")
					.append(secret.getStackTraceElement(this, index)).append('\n');
		}
	}
}


