package com.aplana.sbrf.taxaccounting.util;

import java.util.Comparator;

/**
 * Класс-компаратор для сортировки элементов, реализующих интерфейс {@link Ordered},
 * по возрастанию значения поля {@link Ordered#getOrder()}
 */
public class OrderComparator implements Comparator<Ordered> {

	@Override
	public int compare(Ordered o1, Ordered o2) {
		int order1 = o1.getOrder(),
			order2 = o2.getOrder();
		if (order1 < order2) {
			return -1;
		} else if (order1 == order2) {
			return 0;
		} else {
			return 1;
		}
	}

}
