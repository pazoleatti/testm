package com.aplana.sbrf.taxaccounting.service.scheduler;

import com.aplana.sbrf.taxaccounting.model.annotation.AplanaScheduled;

import java.util.Date;

/**
 * Created by lhaziev on 14.04.2017.
 */
public interface SchedulerService {

    void shutdownAllTasks();

    /**
     * Добавляет/обновляет выполнение методов помеченных {@link AplanaScheduled} в планировщик
     */
    void updateAllTask();

    /**
     * Получить дату следующего вызова
     * @param settingCode
     * @return
     */
    Date nextExecutionTime(String settingCode);
}
