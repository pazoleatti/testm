package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отченов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel", 3L, 103L),
    CSV(1, "CSV", 4L, 104L),

    EXCEL_DEC(0, "Excel", 5L, 105L),
    XML_DEC(1, "XML", 6L, 116L),
    PDF_DEC(2, "PDF", 7L, 107L),
    JASPER_DEC(3, "JASPER", 8L, 108L),

    ARCHIVE_AUDIT(0, "ARCHIVE_AUDIT", 10L, 110L),
    CSV_AUDIT(1, "CSV_AUDIT", 11l, 111L),

    ZIP_IFRS(0, "Zip", 9L, 109L),

    UPLOAD_TF(0, "UploadTF", 12L, 112L),
    LOAD_ALL_TF(1, "LoadAllTF", 13L, 113L);

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

    public Long getAsyncTaskTypeId(boolean isProductionMode) {
        if (isProductionMode)
            return asyncTaskTypeId;
        else
            return devModeAsyncTaskTypeId;
    }

    public void setAsyncTaskTypeId(Long asyncTaskTypeId) {
        this.asyncTaskTypeId = asyncTaskTypeId;
    }

    public void setDevModeAsyncTaskTypeId(Long devModeAsyncTaskTypeId) {
        this.devModeAsyncTaskTypeId = devModeAsyncTaskTypeId;
    }
}
