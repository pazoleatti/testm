package com.aplana.sbrf.taxaccounting.model;

/**
 * Статусы общие для всех версионируемых объектов
 * @author dloshkarev
 */
public enum VersionedObjectStatus {
    /** Обычная версия */
    NORMAL(0),
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

    public static VersionedObjectStatus getStatusById(int id) {
        for (VersionedObjectStatus item : VersionedObjectStatus.values()) {
            if (item.getId() == id) return item;
        }
        throw new RuntimeException("Статус с указанным идентификатором не найден");
    }
}
