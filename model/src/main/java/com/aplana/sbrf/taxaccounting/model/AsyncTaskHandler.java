package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Created by lhaziev on 29.06.2015.
 */
public interface AsyncTaskHandler {

    /**
     * Создание блокировки для задачи
     * @return
     */
    LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo);

    /**
     * Проверока наличия запущенных задач, по которым требуется удалить блокировку
     * @param reportType
     * @param userInfo
     * @param logger
     * @return
     */
    boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger);

    /**
     * Действии при наличии запущенных задач, по которым требуется удалить блокировку
     */
    void executePostCheck();

    /**
     * Отмена задач/удаление отчетов перед запуском текущей задачи
     * @param reportType
     * @param userInfo
     */
    void interruptTask(ReportType reportType, TAUserInfo userInfo);

    /**
     * Получение названия задачи
     * @param reportType
     * @param userInfo
     * @return
     */
    String getTaskName(ReportType reportType, TAUserInfo userInfo);
}
