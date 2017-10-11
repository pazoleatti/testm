package com.aplana.sbrf.taxaccounting.model;

/**
 * Варианты, определяющие, в какую очередь будет направлена задача
 * @author dloshkarev
 */
public enum AsyncQueue {
	/** 1 - Кратковременные задачи */
	SHORT(1, "Очередь кратковременных задач"),
	/** 2 - Длительные задачи */
    LONG(2, "Очередь длительных задач");

    private int id;
    private String name;

    AsyncQueue(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public static AsyncQueue getById(int id) {
        for (AsyncQueue item : values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
