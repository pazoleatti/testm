package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отченов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel", 3L),
    CSV(1, "CSV", 4L),
    PDF(2, "PDF", 5L),
    JASPER(3, "JASPER", 6L);

    private int id;
    private String name;
    private Long asyncTaskTypeId;

    ReportType(int id, String name, Long asyncTaskTypeId) {
        this.id = id;
        this.name = name;
        this.asyncTaskTypeId = asyncTaskTypeId;
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

    public Long getAsyncTaskTypeId() {
        return asyncTaskTypeId;
    }

    public void setAsyncTaskTypeId(Long asyncTaskTypeId) {
        this.asyncTaskTypeId = asyncTaskTypeId;
    }
}
