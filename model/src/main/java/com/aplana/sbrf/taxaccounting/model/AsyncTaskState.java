package com.aplana.sbrf.taxaccounting.model;

/**
 * Статусы асинхронных задач
 * @author dloshkarev
 */
public enum AsyncTaskState {
    IN_QUEUE(1, "В очереди на выполнение"),
    STARTED(2, "Началось выполнение"),
    SAVING_MSGS(3, "Выполняется сохранение уведомлений"),
    SENDING_MSGS(4, "Выполняется рассылка уведомлений"),
    SENDING_ERROR_MSGS(5, "Произошла ошибка. Выполняется рассылка уведомлений"),
    SOURCE_FORM_CHECK(6, "Проверка форм-источников"),
    FORM_CHECK(7, "Проверка данных налоговой формы"),
    FORM_STATUS_CHANGE(8, "Изменение состояния налоговой формы"),
    SAVING_REPORT(9, "Сохранение отчета в базе данных"),
    BUILDING_REPORT(10, "Формирование отчета"),
    FILLING_JASPER(11, "Заполнение Jasper-макета"),
    FILLING_XLSX_REPORT(12, "Заполнение XLSX-отчета"),
    SAVING_XML(13, "Сохранение XML-файла в базе данных"),
    PREPARE_TEMP_FILE(14, "Создание временного файла для записи расчета"),
    BUILDING_XML(15, "Формирование XML-файла"),
    GET_FORM_DATA(16, "Получение данных налоговой формы"),
    FILLING_PDF(17, "Заполнение PDF-файла"),
    SAVING_PDF(18, "Сохранение PDF-файла в базе данных"),
    SAVING_JASPER(19, "Сохранение Jasper-макета в базе данных"),
    SAVING_XLSX(20, "Сохранение XLSX в базе данных"),
    CHECK_XSD(21, "Выполнение проверок XSD-файла"),
    FILES_UPLOADING(22, "Загрузка файлов"),
    FIAS_IMPORT(23, "Импорт справочника ФИАС"),
    CANCELLED(-1, "Задача отменена, ожидается завершение");

    private int id;
    private String text;

    AsyncTaskState(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }

    public static AsyncTaskState getById(int id) {
        for (AsyncTaskState item : values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
