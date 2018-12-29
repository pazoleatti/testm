package com.aplana.sbrf.taxaccounting.service.component.operation;

import com.aplana.sbrf.taxaccounting.model.OperationType;

import java.util.Map;

/**
 * Делегирует создание описания асинхронной задачи соответствующим бинам создающим описание
 */
public interface AsyncTaskDescriptor {
    /**
     * Создать описание асинхронной задачи
     *
     * @param params параметры для формирования описания
     * @return строку описания
     */
    String createDescription(Map<String, Object> params, OperationType operationType);

}
