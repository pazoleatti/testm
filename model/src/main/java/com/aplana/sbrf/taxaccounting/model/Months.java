package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление месяцев. Нумерация идет с 1.
 *
 * @author fmukhametdinov
 */
public enum Months {

    JANUARY(1, "Январь"),
    FEBRUARY(2, "Февраль"),
    MARCH(3, "Март"),
    APRIL(4, "Апрель"),
    MAY(5, "Май"),
    JUNE(6, "Июнь"),
    JULY(7, "Июль"),
    AUGUST(8, "Август"),
    SEPTEMBER(9, "Сентябрь"),
    OCTOBER(10, "Октябрь"),
    NOVEMBER(11, "Ноябрь"),
    DECEMBER(12, "Декабрь");

    private final int id;
    private final String title;

    private Months(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static Months fromId(int monthId) {
		for (Months month : values()) {
			if (month.getId() == monthId) {
				return month;
			}
		}
		throw new IllegalArgumentException("Wrong monthId: " + monthId);
    }
}
