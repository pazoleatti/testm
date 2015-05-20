package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отчетов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel", "", 3L, 103L),
    CSV(1, "CSV", "", 4L, 104L),

    EXCEL_DEC(0, "Excel", "Формирование печатного представления %s", 5L, 105L),
    XML_DEC(1, "XML", "Расчет %s", 6L, 106L),
    PDF_DEC(2, "PDF", "Формирование формы предварительного просмотра %s", 7L, 107L),
    JASPER_DEC(3, "JASPER", "", 8L, 108L),

    CHECK_DEC(0, "CHECK_DECLARATION", "Проверка %s", 14L, 114L),

    ZIP_IFRS(0, "Zip", "", 9L, 109L),

    ARCHIVE_AUDIT(0, "ARCHIVE_AUDIT", "", 10L, 110L),
    CSV_AUDIT(1, "CSV_AUDIT", "", 11l, 111L),

    UPLOAD_TF(0, "UploadTF", "", 12L, 112L),
    LOAD_ALL_TF(1, "LoadAllTF", "", 13L, 113L);


    public static final String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";
    public static final String CHECK_TASK = "Выполнение операции \"%s\" невозможно, т.к. %s";
    public static final String CANCEL_TASK = "Операция \"%s\" отменена"; // ToDo поменять текс когда добавят в постановке http://conf.aplana.com/pages/viewpage.action?pageId=19661926

    private int id;
    private String name;
    private String description;
    private Long asyncTaskTypeId;
    private Long devModeAsyncTaskTypeId;

    ReportType(int id, String name, String description, Long asyncTaskTypeId, Long devModeAsyncTaskTypeId) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
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
