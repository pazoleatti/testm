package com.aplana.sbrf.taxaccounting.async.balancing;

/**
 * Варианты, определяющие, в какую очередь будет направлена задача
 * @author dloshkarev
 */
public enum BalancingVariants {
    SHORT("Очередь задач с коротким сроком выполнения"),
    LONG("Очередь задач с длительным сроком выполнения");

    private String name;

    BalancingVariants(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
