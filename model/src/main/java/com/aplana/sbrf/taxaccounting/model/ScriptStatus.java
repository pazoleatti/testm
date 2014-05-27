package com.aplana.sbrf.taxaccounting.model;

/**
 * Статус выполнения
 * @author Dmitriy Levykin
 */
public enum ScriptStatus {
    /**
     * Выполнен успешно
     */
    SUCCESS,
    /**
     * Статус по-умолчанию
     */
    DEFAULT,
    /**
     * Файл пропущен
     */
    SKIP;
}
