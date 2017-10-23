package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы асинхронных задач с привязкой их к обработчикам в таблице ASYNC_TASK_TYPE
 *
 * @author lhaziev
 */
public enum AsyncTaskType {
    //Асинхронные задачи
    XML_DEC(1, "XML", "Расчет НФ. %s", 6L),
    CREATE_REPORTS_DEC(4, "CREATE_REPORTS_DEC", "Выгрузка отчетности: Вид отчетности: \"%s\", Период: \"%s%s\", Подразделение: \"%s\"", 29L),
    CREATE_FORMS_DEC(3, "CREATE_FORMS_DEC", "Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s%s\", Подразделение: \"%s\"", 28L),
    CHECK_DEC(0, "CHECK_DECLARATION", "Проверка НФ. %s", 14L),
    ACCEPT_DEC(1, "ACCEPT_DECLARATION", "Принятие НФ. %s", 15L),
    EXCEL_DEC(0, "XLSX", "Формирование XLSX-отчета для НФ. ", 5L),
    PDF_DEC(2, "PDF", "Создание формы предварительного просмотра. %s", 7L),
    SPECIFIC_REPORT_DEC(4, "SPECIFIC", "Формирование отчета \"%s\" %s", 26L),
    LOAD_ALL_TF(1, "LoadAllTF", "Импорт транспортного файла из каталога загрузки", 13L),
    SPECIFIC_REPORT_REF_BOOK(3, "SPECIFIC", "Формирование специфического отчета \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\"", 25L),
    EXCEL_REF_BOOK(0, "XLSX", "Формирование отчета справочника \"%s\" в XLSX-формате. Версия: %s, Фильтр: \"%s\"", 23L),
    CSV_REF_BOOK(1, "CSV", "Формирование отчета справочника \"%s\" в CSV-формате. Версия: %s, Фильтр: \"%s\"", 24L),
    TEST(-1, "TEST", "Тестовая задача %s", -1L),

    //Типы отчетов
    JASPER_DEC(3, "JASPER", ""),

    //Псевдозадачи, которые тут чтобы использоваться в общих механизмах
    UPDATE_TEMPLATE_DEC(5, "UPDATE_TEMPLATE_DEC", "Обновление макета"), //формально является задачей
    EDIT_FILE_COMMENT_DEC(6, "EDIT_FILE_COMMENT", "Добавление файлов и комментариев"), //формально является задачей, нужна для работы с модальным окном "Файлы и комментарии"
    IMPORT_TF_DEC(2, "IMPORT_TF_DECLARATION", "Импорт ТФ из каталога загрузки"), //формально является задачей, блокирует форму при импорт из каталога загрузки
    DELETE_DEC(5, "DELETE_DECLARATION", "Удаление налоговой формы"), //формально является задачей,
    TO_CREATE_DEC(6, "MOVE_TO_CREATE", "Возврат в Создана");

    private int id;
    private String name;
    private String description;
    private Long asyncTaskTypeId;

    AsyncTaskType(int id, String name, String description, Long asyncTaskTypeId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.asyncTaskTypeId = asyncTaskTypeId;
    }

    AsyncTaskType(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public static AsyncTaskType getByAsyncTaskTypeId(long id) {
        for (AsyncTaskType item : values()) {
            if (item.getAsyncTaskTypeId() == id) {
                return item;
            }
        }
        return null;
    }
}