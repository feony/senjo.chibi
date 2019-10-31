/* Copyright 2018-2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import java.util.concurrent.TimeUnit;
import org.senjo.annotation.Synchronized;

/** Затвор для потоков. Позволяет закрывать и открывать абстрактный затвор, тем самым
 * пропуская или временно приостанавливая до открытия исполнительные потоки (threads).
 * 
 *  Использование:<pre>
 * control: gateway.open();  // открыть шлюз, все потоки будут свободно проходить
 * thread1: gateway.join();  // поток 1, свободно проходит через шлюз и продолжает работу
 * control: gateway.close(); // закрыть шлюз, теперь все потоки будут останавливаться
 * thread1: gateway.join();  // поток 1, замораживается войдя в шлюз и ждёт сигнала
 * thread2: gateway.join();  // поток 2, замораживается войдя в шлюз и ждёт сигнала
 * control: gateway.open();  // открыть шлюз, остановленные потоки 1 и 2 продолжат работу
 * </pre>
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-01, change 2019-03-01 */
@Synchronized public final class Gateway extends ABarrier {
	public Gateway(boolean opened) { if (opened) push(Opened); }

	/** Открыть затвор. Все приостановленные потоки будут пропущены, все приходящие будут
	 * пропускаться без задержки.
	 * @return число потоков, которые ждали и теперь прошли через затвор */
	@Synchronized public int open() { try { sync();
		return push(Opened) ? unpark(-1) : 0;
	} finally { unsync(); } }

	/** Закрыть затвор. Все приходящие потоки будут задерживаться, пока затвор не будет
	 * вновь открыть. */
	@Synchronized public void close() { takeSync(Opened); }

	/** Пропустить через закрытый затвор один поток, если кто-то есть в очереди.
	 * @return true — потоки есть в ожидании и один был пропущен; false — потоков
	 *         в ожидании нету. */
	@Synchronized public boolean pass() { return pass(1) > 0; }

	/** Пропустить через закрытый затвор все потоки, если кто-то есть в очереди.
	 * @return число потоков, которые были в ожидании и теперь прошли через затвор. */
	@Synchronized public int passAll() { return pass(-1); }

	/** Пропустить потоки через закрытый затвор, если кто-то есть в очереди.
	 * @param count — число потоков, которое следует пропустить через закрытый затвор;
	 *        -1 — пропустить через затвор все ждущие потоки.
	 * @return число потоков прошедших через затвор. */
	@Synchronized public int pass(int count) { try { sync();
		return exist(Opened) ? 0 : unpark(count);
	} finally { unsync(); } }

	/** Пройти через затвор. Поток выйдет из этого метода только если затвор открыт
	 * или когда он откроется. */
	@Synchronized public void join() { try { sync();
		if (exist(Opened)) return;
		park(Thread.currentThread(), 0);
	} finally { unsync(); } }

	/** Пройти через затвор с таймаутом. Поток выйдет из этого метода если затвор открыт,
	 * когда он откроется или по указанному таймауту с результатом false. */
	public boolean join(long await, TimeUnit unit) {
		return join(unit.toMillis(await)); }

	/** Пройти через затвор с таймаутом. Поток выйдет из этого метода если затвор открыт,
	 * когда он откроется или по указанному таймауту с результатом false. */
	@Synchronized public boolean join(long millis) { try { sync();
		if (exist(Opened)) return true;
		if (millis <= 0) return false;
		long wakeup = System.currentTimeMillis() + millis;
		park(Thread.currentThread(), wakeup);
		return System.currentTimeMillis() < wakeup;
	} finally { unsync(); } }

	private static final int Opened = 1;
}


