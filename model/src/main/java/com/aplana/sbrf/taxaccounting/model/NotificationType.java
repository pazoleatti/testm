package com.aplana.sbrf.taxaccounting.model;

/**
 * Тип отчета
 * @author lhaziev
 */
public enum NotificationType {
    DEFAULT(0),
    REF_BOOK_REPORT(1);

    private int id;

    NotificationType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Получение типа отчета по id
     * @param id
     * @return
     */
    public static NotificationType fromId(int id) {
        for(NotificationType notificationType: values()) {
            if (notificationType.getId() == id) {
                return notificationType;
            }
        }
        throw new IllegalArgumentException("Wrong NotificationType id: " + id);
    }
}
