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

    public Permissive() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permissive)) return false;

        Permissive<?> that = (Permissive<?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
