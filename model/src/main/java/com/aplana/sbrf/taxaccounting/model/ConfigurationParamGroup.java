package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление групп параметров приложения
 */
public enum ConfigurationParamGroup {

    /**
     * Для параметров из "Администрирование - Конфигурационные параметры - Общие параметры"
     */
    COMMON("Общие параметры", 0),
    FORM("Параметры загрузки налоговых форм", 1),
    EMAIL("Электронная почта", 2),
    ASYNC("Параметры асинхронных заданий", 3),

    /**
     * Для параметров из "Налоги - Общие параметры"
     */
    COMMON_PARAM("Общие параметры", 4);

    private String caption;
    private int index;

    private ConfigurationParamGroup(String caption, int index) {
        this.caption = caption;
        this.index = index;
    }

    /**
     * Имя параметра
     */
    public String getCaption() {
        return caption;
    }

    public int getIndex() {
        return index;
    }
}
