package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Класс для опционального хранения объектов с правами доступа.
 *
 * @param <T> тип объекта для хранения
 */
public class Permissive<T> {

    private boolean permission = true;
    private T value = null;

    private Permissive(boolean permission) {
        this.permission = permission;
    }

    private Permissive(T value) {
        this.value = value;
    }

    public static <T> Permissive<T> forbidden() {
        return new Permissive<>(false);
    }

    public static <T> Permissive<T> of(T value) {
        return new Permissive<>(value);
    }

    @JsonProperty("permission")
    public boolean hasPermission() {
        return permission;
    }

    @JsonProperty("value")
    public T value() {
        if (!permission) return null;
        return value;
    }
}
