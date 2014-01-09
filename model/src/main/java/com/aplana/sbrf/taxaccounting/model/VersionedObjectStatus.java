package com.aplana.sbrf.taxaccounting.model;

/**
 * Статусы общие для всех версионируемых объектов
 * @author dloshkarev
 */
public enum VersionedObjectStatus {
    /** Обычная версия */
    NORMAL(0),
    /** Версия помеченная на удаление */
    DELETED(-1),
    /** Черновик версии */
    DRAFT(1),
    /** Фиктивная версия */
    FAKE(2);

    private int id;

    private VersionedObjectStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static VersionedObjectStatus fromId(int statusId) {
        for (VersionedObjectStatus status : values()) {
            if (status.getId() == statusId) {
                return status;
            }
        }
        throw new IllegalArgumentException("Wrong VersionedObjectStatus id: " + statusId);
    }
}
