package com.aplana.sbrf.taxaccounting.model;

/**
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
    private final String name;

    private Months(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Months fromId(int monthId) {
        for (Months month : values()) {
            if (month.id == monthId) {
                return month;
            }
        }
        throw new IllegalArgumentException("Wrong Month id: " + monthId);
    }
}
