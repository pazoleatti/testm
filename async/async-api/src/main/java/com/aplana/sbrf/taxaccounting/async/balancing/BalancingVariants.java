package com.aplana.sbrf.taxaccounting.async.balancing;

/**
 * Варианты, определяющие, в какую очередь будет направлена задача
 * @author dloshkarev
 */
public enum BalancingVariants {
    /** Очередь задач с коротким сроком выполнения */
    SHORT,
    /** Очередь задач с длительным сроком выполнения */
    LONG
}
