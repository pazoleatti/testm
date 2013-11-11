package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

/**
 * Типы состояний задач
 * @author dloshkarev
 */
public enum TaskState {
    SCHEDULED(1, "Запланирована"),
    SUSPENDED(2, "Приостановлена"),
    CANCELLED(3, "Остановлена"),
    COMPLETE(4, "Завершена"),
    RUNNING(5, "Выполняется"),
    INVALID(-1, "Некорректный"),
    UNKNOWN(-2, "Неизвестный");

    private Integer id;
    private String name;

    private TaskState(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TaskState getStateById(Integer id) {
        for (TaskState state : TaskState.values()) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
