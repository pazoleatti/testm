package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отчетов для НФ/декларации
 *
 * @author lhaziev
 */
public enum ReportType {

    EXCEL(0, "XLSM", "Формирование отчета %s в XLSM-формате", 3L, 103L),
    CSV(1, "CSV", "Формирование отчета %s в CSV-формате", 4L, 104L),
    SPECIFIC_REPORT(3, "SPECIFIC", "Формирование специфического отчета \"%s\" %s", 22L, 122L),

    EXCEL_DEC(0, "XLSX", "Формирование отчета %s в XLSX-формате", 5L, 105L),
    XML_DEC(1, "XML", "Расчет %s", 6L, 106L),
    PDF_DEC(2, "PDF", "Создание формы предварительного просмотра %s", 7L, 107L),
    JASPER_DEC(3, "JASPER", "", 8L, 108L),
    SPECIFIC_REPORT_DEC(4, "SPECIFIC", "Формирование отчета \"%s\" %s", 26L, 126L),
    UPDATE_TEMPLATE_DEC(5, "UPDATE_TEMPLATE_DEC", "Обновление макета", 0L, 0L), //формально является задачей
    EDIT_FILE_COMMENT_DEC(6, "EDIT_FILE_COMMENT", "Добавление файлов и комментариев", 0L, 0L), //формально является задачей, нужна для работы с модальным окном "Файлы и комментарии"

    CHECK_DEC(0, "CHECK_DECLARATION", "Проверка %s", 14L, 114L),
    ACCEPT_DEC(1, "ACCEPT_DECLARATION", "Принятие %s", 15L, 115L),
    IMPORT_TF_DEC(2, "IMPORT_TF_DECLARATION", "Импорт ТФ из каталога загрузки", 0L, 0L), //формально является задачей, блокирует форму при импорт из каталога загрузки
    DELETE_DEC(5, "DELETE_DECLARATION", "Удаление налоговой формы", 0L, 0L), //формально является задачей,

    CREATE_FORMS_DEC(3, "CREATE_FORMS_DEC", "Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s%s\", Подразделение: \"%s\"", 28L, 128L),
    CREATE_REPORTS_DEC(4, "CREATE_REPORTS_DEC", "Формирование отчетности: Вид отчетности: \"%s\", Период: \"%s%s\", Подразделение: \"%s\"", 29L, 129L),

    CONSOLIDATE_FD(0, "CONSOLIDATE_FORM_DATA", "Консолидация %s", 16L, 116L),
    CALCULATE_FD(1, "CALCULATE_FORM_DATA", "Расчет %s", 17L, 117L),
    IMPORT_FD(2, "IMPORT_FORM_DATA", "Загрузка XLSM-файла с формы экземпляра налоговой формы", 18L, 118L), //импорт из режима редактирования
    CHECK_FD(3, "CHECK_FORM_DATA", "Проверка %s", 19L, 119L),
    MOVE_FD(4, "MOVE_FORM_DATA", "Изменение состояния %s", 20L, 120L),
    IMPORT_TF_FD(5, "IMPORT_TF_FORM_DATA", "Импорт ТФ из каталога загрузки", 0L, 0L), //формально является задачей, блокирует форму при импорт из каталога загрузки
    EDIT_FD(6, "EDIT_FORM_DATA", "Редактирование %s", 0L, 0L), //формально является задачей, нужна для блокирки формы в режиме редартирования/при консолидации приемника
    DELETE_FD(7, "DELETE_FORM_DATA", "Удаление %s", 0L, 0L), //формально является задачей
    EDIT_FILE_COMMENT(8, "EDIT_FILE_COMMENT", "Добавление файлов и комментариев", 0L, 0L), //формально является задачей, нужна для работы с модальным окном "Файлы и комментарии"
    REFRESH_FD(9, "REFRESH", "Обновление %s", 21L, 121L),
    DELETE_REPORT_FD(10, "DELETE_REPORT_FD", "Удаление отчетов", 0L, 0L), //формально является задачей

    ZIP_IFRS(0, "Zip", "", 9L, 109L),

    ARCHIVE_AUDIT(0, "ARCHIVE_AUDIT", "", 10L, 110L),
    CSV_AUDIT(1, "CSV_AUDIT", "Формирование файла с данными журнала аудита по параметрам поиска", 11L, 111L),

    LOAD_ALL_TF(1, "LoadAllTF", "Импорт транспортного файла из каталога загрузки", 13L, 113L),

    EXCEL_REF_BOOK(0, "XLSX", "Формирование отчета справочника \"%s\" в XLSX-формате", 23L, 123L),
    CSV_REF_BOOK(1, "CSV", "Формирование отчета справочника \"%s\" в CSV-формате", 24L, 124L),
    SPECIFIC_REPORT_REF_BOOK(3, "SPECIFIC", "Формирование специфического отчета \"%s\" справочника \"%s\"", 25L, 125L),
    IMPORT_REF_BOOK(4, "IMPORT_REF_BOOK", "Загрузка данных из файла в справочник \"%s\"", 27L, 127L),
    EDIT_REF_BOOK(5, "EDIT_REF_BOOK", "Редактирование справочника \"%s\"", 0L, 0L), //формально является задачей

    GET_ALL_DEPARTMENT_CHANGES(0, "GET_ALL_CHANGES", "Получение списка всех изменений ", 0L, 0L), //формально является задачей
    SEND_DEPARTMENT_CHANGE(1, "SEND_CHANGE", "Отправка текущего изменения подразделения", 0L, 0L); //формально является задачей

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
