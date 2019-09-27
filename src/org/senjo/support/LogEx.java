/* Copyright 2018, 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.support;

import static org.senjo.support.Log.*;
import static org.senjo.support.Log.Level.*;
import org.senjo.support.Log.Buffer;

/** Статический интерфейс пользовательского журнала. Пишет записи в основной журнал,
 * если он заранее был задан методом {@link Log#exInitialize(Log)}.
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-01-20, change 2019-03-05, fix 2019-03-14 */
public final class LogEx {
	public static void fault(String message) { instance.log(Fault, 1, message, null); }
	public static void warn (String message) { instance.log(Warn , 1, message, null); }
	public static void info (String message) { instance.log(Info , 1, message, null); }
	public static void trace(String message) { instance.log(Trace, 1, message, null); }
	public static void debug(String message) { instance.log(Debug, 1, message, null); }

//	public static void trace(Object obj) { instance.log(Trace, 1, Text.textEx(obj), null); }

	public static void fault(String message, Throwable ex) {
		instance.log(Fault, 1, message, ex); }
	public static void warn (String message, Throwable ex) {
		instance.log(Warn , 1, message, ex); }
	public static void trace(String message, Throwable ex) {
		instance.log(Trace, 1, message, ex); }

	public static void fault(String message, boolean trace) {
		instance.log(Fault, 1, message, trace); }
	public static void warn (String message, boolean trace) {
		instance.log(Warn , 1, message, trace); }
	public static void info (String message, boolean trace) {
		instance.log(Info , 1, message, trace); }
	public static void trace(String message, boolean trace) {
		instance.log(Trace, 1, message, trace); }
	public static void debug(String message, boolean trace) {
		instance.log(Debug, 1, message, trace); }

	public static Buffer faultEx() { return instance.new Buffer(Fault, 1); }
	public static Buffer faultEx(String prefix) {
		return instance.new Buffer(Fault, 1).add(prefix); }
	public static Buffer warnEx () { return instance.new Buffer(Warn , 1); }
	public static Buffer warnEx (String prefix) {
		return instance.new Buffer(Warn , 1).add(prefix); }
	public static Buffer infoEx () { return instance.new Buffer(Info , 1); }
	public static Buffer infoEx (String prefix) {
		return instance.new Buffer(Info , 1).add(prefix); }
	public static Buffer traceEx() { return instance.new Buffer(Trace, 1); }
	public static Buffer traceEx(String prefix) {
		return instance.new Buffer(Trace, 1).add(prefix); }
	public static Buffer debugEx() { return instance.new Buffer(Debug, 1); }
	public static Buffer debugEx(String prefix) {
		return instance.new Buffer(Debug, 1).add(prefix); }
}


