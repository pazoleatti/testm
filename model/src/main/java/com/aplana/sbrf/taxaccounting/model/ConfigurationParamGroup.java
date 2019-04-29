package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Перечисление групп параметров приложения
 */
@Getter
@AllArgsConstructor
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

    /**
     * Имя параметра
     */
    private String caption;
    private int index;
}
