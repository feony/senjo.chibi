/* Copyright 2018-2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.basis;

import static org.senjo.basis.Helper.unsafe;

import java.util.NoSuchElementException;
import org.senjo.annotation.*;

/** Абстракция для реализации блокировки потоков до наступления определённого события.
 * Сами условия блокировки и разблокировки реализуются в наследнике. Данный класс
 * обеспечивает блокировку, разблокировку по требованию наследника, а также хранение
 * всех заблокированных потоков.
 * @see Gateway
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2018-01, change 2019-02-12 */
@Synchronized abstract class ABarrier extends AdaptDeque<Thread> {
	final ABasket owner;

	ABarrier()              { this.owner = this ; }
	ABarrier(ABasket owner) { this.owner = owner; }

/*TODO Разбить метод park на два: сначала регистрация (сохранение потока), потом собственно
 * парковка. Парковка может быть interruptible. Для этого через sun.misc.SharedSecrets
 * из java.nio нужно пробраться к методу Thread#blockedOn(Interruptible), и установить
 * в него себя. В случае прерывания чужим потоком будет вызван метод этого интерфейса
 * и передан поток, который нужно прервать. После штатной разблокировки нужно убрать
 * себя тем же методом, записав null. */

	@Looper final void park(@NotNull Thread thread, long wakeup) {
		ABasket owner = this.owner;
		dequePush(thread);
		owner.unsync();
		unsafe.park(true, wakeup);
		owner.sync();
	}

	/**
	 * @param count — максимальное число припаркованных здесь потоков, которые нужно
	 *        распарковать; -1 — распарковать все припаркованные потоки.
	 * @return возвращает число успешно распаркованных потоков. */
	@Naive final int unpark(int count) {
		int result = -1;
		while (++result != count)
			try { unsafe.unpark(dequeTake()); }
			catch (NoSuchElementException ex) { break; }
		return result;
	}
}


