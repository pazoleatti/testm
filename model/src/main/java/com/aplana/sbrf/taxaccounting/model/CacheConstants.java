package com.aplana.sbrf.taxaccounting.model;

/**
 * Класс содержит перечень констант использующихся при кэшировании dao-запросов
 */
public final class CacheConstants {

    /**
     * Макет налоговой формы
     */
    public static final String DECLARATION_TEMPLATE = "DeclarationTemplate";
    /**
     * Скрипт для конкретного события
     */
    public static final String DECLARATION_TEMPLATE_EVENT_SCRIPT = "DeclarationTemplateEventScript";
    /**
     * Вид налоговой формы
     */
    public static final String DECLARATION_TYPE = "DeclarationType";
    /**
     * Отчётный период подразделения
     */
    public static final String DEPARTMENT_REPORT_PERIOD = "DepartmentReportPeriod";
    /**
     * Пользователь
     */
    public static final String USER = "User";
    /**
     * Подразделение
     */
    public static final String DEPARTMENT = "Department";
    /**
     * Справочник
     */
    public static final String REF_BOOK = "RefBook";
    /**
     * Атрибут справочника
     */
    public static final String REF_BOOK_ATTRIBUTE = "RefBookAttribute";

    private CacheConstants() {
    }
}