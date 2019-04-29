package com.aplana.sbrf.taxaccounting.model;

/**
 * Сущность, для которой применяются права доступа.
 */
public interface SecuredEntity {
    /**
     * Возвращает битовую маску текущих прав.
     */
    long getPermissions();

    /**
     * Устанавливает текущие права.
     */
    void setPermissions(long permissions);
}
