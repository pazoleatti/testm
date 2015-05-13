package com.aplana.sbrf.taxaccounting.async.balancing;

/**
 * Варианты, определяющие, в какую очередь будет направлена задача
 * @author dloshkarev
 */
public enum BalancingVariants {
    SHORT("Кратковременные операции"),
    LONG("Длительные операции");

    private String name;

    BalancingVariants(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
