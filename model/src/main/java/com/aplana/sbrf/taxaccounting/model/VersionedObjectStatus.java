package com.aplana.sbrf.taxaccounting.model;

/**
 * Статусы общие для всех версионируемых объектов
 * @author dloshkarev
 */
public enum VersionedObjectStatus {
    /** Обычная версия */
    NORMAL(0),
    /** Версия помеченная на удаление */
    DELETED(1),
    /** Черновик версии */
    DRAFT(2),
    /** Фиктивная версия */
    FAKE(3);

    private int id;

    private VersionedObjectStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
