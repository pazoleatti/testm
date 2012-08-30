package com.aplana.sbrf.taxaccounting.log;

/**
 * Интрефейс используется объектом {@Logger} для того, 
 * чтобы провести некоторое преобразование над строками, записываемыми в журнал
 * Например при выполнении проверок содержимого строк - указывать номер текущей строки и т.п.
 */
public interface LogMessageDecorator {
	String getDecoratedMessage(String message);
}
