package com.aplana.sbrf.taxaccounting.dao.impl.cache;

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
     * Отчётный периот
     */
    public static final String TAX_PERIOD = "TaxPeriod";
    /**
     * Пользователь
     */
    public static final String USER = "User";
    /**
     * Подразделение
     */
    public static final String DEPARTMENT = "Department";

    private CacheConstants() {
    }
}