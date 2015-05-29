package com.aplana.sbrf.taxaccounting.async.balancing;

/**
 * Варианты, определяющие, в какую очередь будет направлена задача
 * @author dloshkarev
 */
public enum BalancingVariants {
    SHORT(1, "Кратковременные задачи"),
    LONG(2, "Длительные задачи");

    private int id;
    private String name;

    BalancingVariants(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    BalancingVariants getById(int id) {
        for (BalancingVariants item : values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
