package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отченов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel", 3L, 103L),
    CSV(1, "CSV", 4L, 104L),
    PDF(2, "PDF", 5L, 105L),
    JASPER(3, "JASPER", 6L, 106L);

    private int id;
    private String name;
    private Long asyncTaskTypeId;
    private Long devModeAsyncTaskTypeId;

    ReportType(int id, String name, Long asyncTaskTypeId, Long devModeAsyncTaskTypeId) {
        this.id = id;
        this.name = name;
        this.asyncTaskTypeId = asyncTaskTypeId;
        this.devModeAsyncTaskTypeId = devModeAsyncTaskTypeId;
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

    public Long getAsyncTaskTypeId(boolean isDevelopmentMode) {
        if (isDevelopmentMode)
            return devModeAsyncTaskTypeId;
        else
            return asyncTaskTypeId;
    }

    public void setAsyncTaskTypeId(Long asyncTaskTypeId) {
        this.asyncTaskTypeId = asyncTaskTypeId;
    }

    public void setDevModeAsyncTaskTypeId(Long devModeAsyncTaskTypeId) {
        this.devModeAsyncTaskTypeId = devModeAsyncTaskTypeId;
    }
}
