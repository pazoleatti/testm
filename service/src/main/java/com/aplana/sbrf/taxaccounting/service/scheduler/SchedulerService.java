package com.aplana.sbrf.taxaccounting.service.scheduler;

import java.util.Date;

/**
 * Сервис для работы с планировщиком задач
 */
public interface SchedulerService {

    /**
     * Завершение работы всех запущенных задач
     */
    void shutdownAllTasks();

    /**
     * Добавляет/обновляет выполнение методов этого сервиса в планировщик
     */
    void updateAllTask();

    /**
     * Получить дату следующего вызова
     *
     * @param settingCode код конфигурационного параметра из таблицы CONFIGURATION
     * @return время выполнения задачи, если задача заведена, в противном случае null
     */
    Date nextExecutionTime(String settingCode);
}
