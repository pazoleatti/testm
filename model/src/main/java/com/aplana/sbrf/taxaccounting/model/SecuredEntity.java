package com.aplana.sbrf.taxaccounting.model;

/**
 * Сущность, для которой применяются права доступа.
 *
 * @author <a href="mailto:ogalkin@aplana.com>Олег Галкин</a>
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