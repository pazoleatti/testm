package com.aplana.sbrf.taxaccounting.model;

/**
 * Специфичный вывод наименований периодов в форме налоговой формы
 * http://jira.aplana.com/browse/SBRFACCTAX-6399
 * User: fmukhametdinov
 * Date: 20.03.14
 * Time: 18:55
 */
public enum ReportPeriodSpecificNave {
    FIRST(21, "1 квартал"),
    SECOND(31, "2 квартал"),
    THIRD(33, "3 квартал"),
    FORTH(34, "4 квартал"),;

    final private int id;
    final private String name;

    ReportPeriodSpecificNave(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ReportPeriodSpecificNave fromId(int id) {
        for (ReportPeriodSpecificNave name : values()) {
            if (name.id == id) {
                return name;
            }
        }

        throw new IllegalArgumentException("Wrong ReportPeriodSpecificNave id: " + id);
    }
}
