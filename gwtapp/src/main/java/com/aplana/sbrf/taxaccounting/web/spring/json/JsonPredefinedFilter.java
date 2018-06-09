package com.aplana.sbrf.taxaccounting.web.spring.json;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Предустановленные фильтры для всех сущностей.
 */
public enum JsonPredefinedFilter {
    /**
     * Пустой фильтр
     */
    NONE(""),
    /**
     * Только идентификаторы
     */
    ID_ONLY("id"),

    /**
     * Стандартный набор полей
     */
    REF_BOOK_META("id", "name", "hierarchic", "readOnly"),
    // Для справочников c общими полями
    ABSTRACT_REF_BOOK("id"),
    ABSTRACT_REF_BOOK_CODE_NAME(ABSTRACT_REF_BOOK, "code", "name"),
    // Абстрактный класс для версионируемых справочников
    ABSTRACT_VERSIONED_REFERENCE(ABSTRACT_REF_BOOK, "recordId", "version", "versionEnd");

    private String[] fields;

    public String[] getFields() {
        return fields;
    }

    JsonPredefinedFilter(String... fields) {
        this.fields = fields;
    }

    JsonPredefinedFilter(JsonPredefinedFilter parentFilter, String... fields) {
        this.fields = ArrayUtils.addAll(parentFilter.getFields(), fields);
    }
}