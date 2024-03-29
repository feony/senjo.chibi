﻿# Библиотека Senjo Chibi

Библиотека шустрых и компактных вспомогательных механизмов для разработчика. Также содержит базовые фундаментальные механизмы и интерфейсы использующиеся в нескольких более тяжёлых библиотеках Senjo.

_(добавить общие мысли о назначении данной библиотеки)_

## Модули

* [**Base (Основа)**](base.md) — набор простых статических методов сокращающих типичные операции (синтаксический сахар). Например, можно проверить не пустая ли строка, в том числе и на `null`, одним методом `isExist(String)`. Ещё можно сравнить два объекта, в том числе `null`, например на совпадение значений методом `eq(Object, Object)`. И многое другое.

* [**Basket («Корзинка Фруктов»)**](basket.md) — этот базовый класс даёт наследнику компактный механизм управления приватными флагами и короткой опасной синхронизацией любому классу реализующему алгоритм по принципу конечного автомата, т.е. почти для любого сложного класса. В памяти занимает 4 байта, доступно 30 флагов, флаги задаются масками в константах, при установке и проверке их можно комбинировать, есть методы для быстрой синхронной межпоточной работы с флагами, ключи блокировки (mutex) можно расширять с помощью этих же флагов.

* [**Enumerator (Перечислитель)**](enumerator.md) — этот абстрактный класс реализует более богатую и удобную, хоть и немного медленнее в работе, обёртку для стандартного интерфейса `Iterable`. Класс `Enumerator` похож на одноимённый интерфейс в C# и позволяет реализовать проверку и переход к следующему элементу одним методом `boolean next([int])`. Конечно, экземпляр этого класса можно использовать в операторе `for (T : enumerator)`.
