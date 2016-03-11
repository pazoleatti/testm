package com.aplana.sbrf.taxaccounting.model;

/**
 * Объект-хранилище данных для получения сложности задач формирования специфичных отчетов НФ/декларации/справочников в скрипте
 * @author lhaziev
 */
public class ScriptTaskComplexityHolder {

    /**
     * Псевдоним специфичного отчета
     */
    private String alias;

    /**
     * Скрипт должен вернуть число, рассчитанное значение
     */
    private Long value;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
