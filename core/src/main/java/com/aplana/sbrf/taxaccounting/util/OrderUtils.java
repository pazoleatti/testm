package com.aplana.sbrf.taxaccounting.util;

import java.util.Collections;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Ordered;

public final class OrderUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private OrderUtils() {
	}

	/**
	 * Сортирует коллекцию по возрастанию значения поля order,
	 * после чего обновляет у всех элементов коллекции значение поля order, приравнивая его номеру
	 * элемента в отсортированном массиве (начиная с 1)
	 * @param list список элементов для пересортировки
	 */
	public static void reorder(List<? extends Ordered> list) {
		Collections.sort(list, new OrderComparator());
		int i = 0;
		for (Ordered o: list) {
			o.setOrder(++i);
		}
	}
}
