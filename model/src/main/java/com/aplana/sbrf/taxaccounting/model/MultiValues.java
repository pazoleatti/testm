package com.aplana.sbrf.taxaccounting.model;

/**
 * Интерфейс для объектов передаваемых в in-запросы в качестве параметров
 * @author Denis Loshkarev
 */
public interface MultiValues {
    /**
     * Возвращает объект как текстовое представление, которое будет использоваться в in-запросе
     * Например: in (x,x,x), где x может быть просто числом, парой чисел (1,2) или всем чем угодно
     * @return часть in-запроса
     */
    String getInQuery();
}
