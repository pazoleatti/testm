package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отчетов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "XLSM", "Формирование XLSM-файла %s", 3L, 103L),
    CSV(1, "CSV", "Формирование CSV-файла %s", 4L, 104L),
    SPECIFIC_REPORT(3, "SPECIFIC", "Формирование отчета \"%s\" %s", 22L, 122L),

    EXCEL_DEC(0, "XLSX", "Формирование печатного представления %s", 5L, 105L),
    XML_DEC(1, "XML", "Расчет %s", 6L, 106L),
    PDF_DEC(2, "PDF", "Формирование формы предварительного просмотра %s", 7L, 107L),
    JASPER_DEC(3, "JASPER", "", 8L, 108L),

    CHECK_DEC(0, "CHECK_DECLARATION", "Проверка %s", 14L, 114L),
    ACCEPT_DEC(1, "ACCEPT_DECLARATION", "Принятие %s", 15L, 115L),

    CONSOLIDATE_FD(0, "CONSOLIDATE_FORM_DATA", "Консолидация %sформы", 16L, 116L),
    CALCULATE_FD(1, "CALCULATE_FORM_DATA", "Расчет %sформы", 17L, 117L),
    IMPORT_FD(2, "IMPORT_FORM_DATA", "Загрузка XLSM-файла с формы экземпляра налоговой формы", 18L, 118L), //импорт из режима редактирования
    CHECK_FD(3, "CHECK_FORM_DATA", "Проверка %sформы", 19L, 119L),
    MOVE_FD(4, "MOVE_FORM_DATA", "Изменение состояния %sформы", 20L, 120L),
    IMPORT_TF_FD(5, "IMPORT_TF_FORM_DATA", "Импорт ТФ из каталога загрузки", 0L, 0L), //формально является задачей, блокирует форму при импорт из каталога загрузки
    EDIT_FD(6, "EDIT_FORM_DATA", "Редактирование %sформы", 0L, 0L), //формально является задачей, нужна для блокирки формы в режиме редартирования/при консолидации приемника
    DELETE_FD(7, "DELETE_FORM_DATA", "Удаление %sформы", 0L, 0L), //формально является задачей
    EDIT_FILE_COMMENT(8, "EDIT_FILE_COMMENT", "Прикрепление файлов формы и редактирование комментариев", 0L, 0L), //формально является задачей, нужна для работы с модальным окном "Файлы и комментарии"
    REFRESH_FD(9, "REFRESH", "Обновление %sформы", 21L, 121L),
    DELETE_REPORT_FD(10, "DELETE_REPORT_FD", "Удаление отчетов", 0L, 0L), //формально является задачей

    ZIP_IFRS(0, "Zip", "", 9L, 109L),

    ARCHIVE_AUDIT(0, "ARCHIVE_AUDIT", "", 10L, 110L),
    CSV_AUDIT(1, "CSV_AUDIT", "Формирование zip-архива с csv-файлом журнала аудита по параметрам поиска", 11L, 111L),

    LOAD_ALL_TF(1, "LoadAllTF", "Импорт транспортного файла из каталога загрузки", 13L, 113L),

    EXCEL_REF_BOOK(0, "XLSX", "Формирование XLSX-отчета справочника \"%s\"", 23L, 123L),
    CSV_REF_BOOK(1, "CSV", "Формирование CSV-отчета справочника \"%s\"", 24L, 124L),
    SPECIFIC_REPORT_REF_BOOK(3, "SPECIFIC", "Формирование отчета \"%s\" справочника \"%s\"", 25L, 125L);

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

    public Long getAsyncTaskTypeId() {
		return asyncTaskTypeId;
	}

    public void setAsyncTaskTypeId(Long asyncTaskTypeId) {
        this.asyncTaskTypeId = asyncTaskTypeId;
    }

	public Long getDevModeAsyncTaskTypeId() {
		return devModeAsyncTaskTypeId;
	}

	public void setDevModeAsyncTaskTypeId(Long devModeAsyncTaskTypeId) {
        this.devModeAsyncTaskTypeId = devModeAsyncTaskTypeId;
    }
}
