package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

/**
 * Типы состояний задач
 * @author dloshkarev
 */
public enum TaskState {
    SCHEDULED(1, "Активна"),
    SUSPENDED(2, "Остановлена"),
    CANCELLED(3, "Отменена"),
    COMPLETE(4, "Завершена"),
    RUNNING(5, "Выполняется"),
    INVALID(-1, "Некорректный"),
    UNKNOWN(-2, "Неизвестный");

    private int id;
    private String name;

    private TaskState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TaskState getStateById(int id) {
        for (TaskState state : TaskState.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
