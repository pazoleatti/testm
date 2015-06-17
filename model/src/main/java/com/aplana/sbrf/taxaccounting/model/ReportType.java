package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отчетов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "Excel", "Формирование Excel отчета", 3L, 103L),
    CSV(1, "CSV", "Формирование CSV отчета", 4L, 104L),

    EXCEL_DEC(0, "Excel", "Формирование печатного представления %s", 5L, 105L),
    XML_DEC(1, "XML", "Расчет %s", 6L, 106L),
    PDF_DEC(2, "PDF", "Формирование формы предварительного просмотра %s", 7L, 107L),
    JASPER_DEC(3, "JASPER", "", 8L, 108L),

    CHECK_DEC(0, "CHECK_DECLARATION", "Проверка %s", 14L, 114L),
    ACCEPT_DEC(1, "ACCEPT_DECLARATION", "Принятие %s", 15L, 115L),

    CONSOLIDATE_FD(0, "CONSOLIDATE_FORM_DATA", "Консолидация %sформы", 16L, 116L),
    CALCULATE_FD(1, "CALCULATE_FORM_DATA", "Расчет %sформы", 17L, 117L),
    IMPORT_FD(2, "IMPORT_FORM_DATA", "Загрузка XLSM-файла с формы экземпляра налоговой формы", 18L, 118L), //импорт из режима редактирования
    CHECK_FD(3, "CHECK_FORM_DATA", "Проверка %sформы", 19L, 119L),
    MOVE_FD(4, "MOVE_FORM_DATA", "Изменение состояния НФ", 20L, 120L),
    IMPORT_TF_FD(5, "IMPORT_TF_FORM_DATA", "Импорт транспортного файла из каталога загрузки", 0L, 0L), //формально является задачей, блокирует форму при импорт из каталога загрузки
    EDIT_FD(6, "EDIT_FORM_DATA", "Редактирование %sформы", 0L, 0L), //формально является задачей, нужна для блокирки формы в режиме редартирования/при консолидации приемника

    ZIP_IFRS(0, "Zip", "", 9L, 109L),

    ARCHIVE_AUDIT(0, "ARCHIVE_AUDIT", "", 10L, 110L),
    CSV_AUDIT(1, "CSV_AUDIT", "", 11l, 111L),

    UPLOAD_TF(0, "UploadTF", "", 12L, 112L),
    LOAD_ALL_TF(1, "LoadAllTF", "", 13L, 113L);


    public static final String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";
    public static final String CHECK_TASK = "Выполнение операции \"%s\" невозможно, т.к. %s";
    public static final String CANCEL_TASK = "Пользователем \"%s\" отменена операция \"%s\"";

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
