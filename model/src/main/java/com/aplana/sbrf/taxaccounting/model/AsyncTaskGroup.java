package com.aplana.sbrf.taxaccounting.model;

/**
 *  Группы асинхронных задач. Задачи из одной группы не должны быть запущены одновременно. Используется
 *  когда раличные операции блокирующиеся по разным сущностям должны получить взаимоисключающий
 *  доступ к другому объекту.
 */
public enum AsyncTaskGroup {
    // Задачи сгруппированные по доступу к справочнику ФЛ
    REF_BOOK_PERSON(1);

    private int id;

    AsyncTaskGroup(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static AsyncTaskGroup getById(int id) {
        for (AsyncTaskGroup value : values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        return null;
    }
}
