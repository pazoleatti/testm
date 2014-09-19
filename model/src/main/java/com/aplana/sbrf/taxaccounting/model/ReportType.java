package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отченов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel"),
    CSV(1, "CSV"),
    PDF(2, "PDF"),
    JASPER(3, "JASPER");

    private int id;
    private String name;

    ReportType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
