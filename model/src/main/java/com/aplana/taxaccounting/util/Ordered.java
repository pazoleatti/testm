package com.aplana.taxaccounting.util;

/**
 * Интерфейс, представляющий сущность, у которой задан порядковый номер.
 * Используется для реализации модельных классов, которые должны содержаться в упорядоченных коллекциях
 */
public interface Ordered {
	int getOrder();
	void setOrder(int order);
}
