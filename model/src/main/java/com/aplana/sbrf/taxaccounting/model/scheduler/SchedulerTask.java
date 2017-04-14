package com.aplana.sbrf.taxaccounting.model.scheduler;

/**
 * @author lhaziev
 */
public enum SchedulerTask {
    CLEAR_BLOB_DATA(1), // Задача очистки файлового хранилища
    CLEAR_LOCK_DATA(2), // Задача удаления истекших блокировок
    CLEAR_TEMP_DIR(3); // Задача очистки каталога временных файлов

    private long schedulerTaskId;

    SchedulerTask(int schedulerTaskId) {
        this.schedulerTaskId = schedulerTaskId;
    }

    public long getSchedulerTaskId() {
        return schedulerTaskId;
    }

    /**
     * Возвращает событие формы по идентификатору.
     *
     * @param taskId идентификатор задачи
     * @return событие
     */
    public static SchedulerTask getByTaskId(long taskId) {
        SchedulerTask schedulerTask = null;

        for (SchedulerTask task : SchedulerTask.values()) {
            if (task.getSchedulerTaskId() == taskId) {
                schedulerTask = task;
                break;
            }
        }

        if (schedulerTask != null) {
            return schedulerTask;
        } else {
            throw new IllegalArgumentException("SchedulerTask with id " + taskId + " doesn't exist.");
        }
    }
}
