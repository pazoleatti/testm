package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

/**
 * Результат создания задачи для планировщика
 * @author dloshkarev
 */
public class CreateTaskResult extends AbstractResultWithErrors {
    private static final long serialVersionUID = 3183924734177126798L;

    /**
     * Идентификатор новой задачи
     */
    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
