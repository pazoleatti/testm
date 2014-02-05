package com.aplana.gwt.client;

/**
 * Представление месяца для использования в элементе для выбора даты.
 *
 * @see com.aplana.gwt.client.SimpleDatePicker
 * @author Vitaliy Samolovskikh
 */
public enum Month {
	JANUARY("январь", 1),
	FEBRUARY("февраль", 2),
	MARCH("март", 3),
	APRIL("апрель", 4),
	MAY("май", 5),
	JUNE("июнь", 6),
	JULY("июль", 7),
	AUGUST("август", 8),
	SEPTEMBER("сентябрь", 9),
	OCTOBER("октябрь", 10),
	NOVEMBER("ноябрь", 11),
	DECEMBER("декабрь", 12);

	private final String name;
	private final int order;

	Month(String name, int order) {
		this.name = name;
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public String getStringOrder() {
		return String.valueOf(order);
	}

	public static Month findByOrder(int order) {
		for (Month month : Month.values()) {
			if (month.order == order) {
				return month;
			}
		}
		throw new IllegalArgumentException("Can't find month with order " + order);
	}

	public static Month findByOrder(String order) {
		return findByOrder(Integer.valueOf(order));
	}
}
