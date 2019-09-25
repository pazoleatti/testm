package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;

/**
 * Типы асинхронных задач
 */
@Getter
public enum AsyncTaskType {
    TEST(-1L, "TEST", "Тестовая задача %s"),
    EXCEL_DEC(5L, "XLSX", "Формирование XLSX-отчета для НФ."),
    XML_DEC(6L, "XML", "Расчет НФ. %s"),
    PDF_DEC(7L, "PDF", "Создание формы предварительного просмотра. %s"),
    IDENTIFY_PERSON(8L, "IDENTIFY_PERSON", "Идентификация ФЛ %s"),
    CONSOLIDATE(9L, "CONSOLIDATE", "Расчет НФ. %s"),
    SEND_EDO(10L, "SEND_EDO", "Отправка ЭД в ЭДО"),
    LOAD_TRANSPORT_FILE(12L, "LoadTF", "Импорт транспортного файла"),
    LOAD_ALL_TRANSPORT_DATA(13L, "LoadAllTransporData", "Импорт из каталога загрузки"),
    CHECK_DEC(14L, "CHECK_DECLARATION", "Проверка формы"),
    ACCEPT_DEC(15L, "ACCEPT_DECLARATION", "Принятие формы"),
    EXCEL_REF_BOOK(23L, "XLSX", "Формирование отчета справочника \"%s\" в XLSX-формате"),
    CSV_REF_BOOK(24L, "CSV", "Формирование отчета справочника \"%s\" в CSV-формате"),
    SPECIFIC_REPORT_REF_BOOK(25L, "SPECIFIC", "Формирование специфического отчета \"%s\" справочника \"%s\": Версия: %s, Фильтр: \"%s\""),
    SPECIFIC_REPORT_DEC(26L, "SPECIFIC", "Формирование отчета \"%s\" %s"),
    CREATE_FORMS_DEC(28L, "CREATE_FORMS_DEC", "Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s%s\", Подразделение: \"%s\""),
    IMPORT_DECLARATION_EXCEL(30L, "ImportDecExcel", "Загрузка данных в ПНФ РНУ НДФЛ"),
    EXCEL_TEMPLATE_DEC(31L, "EXCEL_TEMPLATE_DECLARATION", "Выгрузка данных налоговой формы в виде шаблона ТФ (Excel)"),
    DELETE_DEC(32L, "DELETE_DECLARATION", "Удаление налоговой формы. %s"),
    IMPORT_REF_BOOK_XML(33L, "ImportRefBookXml", "Загрузка данных в справочник \"%s\" из xml"),
    UPDATE_PERSONS_DATA(34L, "UPDATE_PERSONS_DATA", "Обновление данных ФЛ формы: № %s, Период %s, Подразделение %s, Вид \"Консолидированная\""),
    CREATE_APPLICATION_2(36L, "CREATE_APPLICATION_2", "Формирование файла Приложения 2 для декларации по налогу на прибыль за %s год"),
    EXCEL_PERSONS(37L, "XLSX", "Выгрузка файла данных Реестра физических лиц в XLSX-формате"),
    EXCEL_DEPARTMENT_CONFIGS(38L, "XLSX", "Выгрузка настроек подразделений в файл формата XLSX"),
    IMPORT_DEPARTMENT_CONFIGS(39L, "IMPORT_DEPARTMENT_CONFIGS", "Загрузка настроек подразделений из Excel файла"),
    CREATE_NOT_HOLDING_TAX_NOTIFICATIONS(40L, "CREATE_NOT_HOLDING_TAX_NOTIFICATIONS", "Формирование Уведомлений о неудержанном налоге"),
    EXPORT_REPORTS(41L, "EXPORT_REPORTS", "Выгрузка отчетности"),
    UPDATE_DOC_STATE(42L, "UPDATE_DOC_STATE", "Изменение состояния ЭД"),
    CREATE_NOTIFICATIONS_LOGS(43L, "CREATE_NOTIFICATIONS_LOGS", "Выгрузка протоколов по оповещениям за: %s"),
    CREATE_2NDFL_FL(44L, "CREATE_2NDFL_FL", "Формирование ОНФ 2-НДФЛ(ФЛ)"),
    UNLOAD_LIST(45L, "XLSX", "Выгрузка списка источники-приемники в файл формата XLSX"),
    EXPORT_TRANSPORT_MESSAGES(46L,"EXPORT_TRANSPORT_MESSAGES","Выгрузка транспортных сообщений в Excel"),

    //Псевдозадачи, которые тут чтобы использоваться в общих механизмах
    IMPORT_TF_DEC("IMPORT_TF_DECLARATION", "Импорт ТФ из каталога загрузки"); //формально является задачей, блокирует форму при импорт из каталога загрузки

    private Long id;
    private String name;
    private String description;

    AsyncTaskType(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    AsyncTaskType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static AsyncTaskType getByAsyncTaskTypeId(long id) {
        for (AsyncTaskType item : values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
