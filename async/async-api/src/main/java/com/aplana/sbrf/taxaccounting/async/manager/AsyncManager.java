package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;

import java.util.Map;

/**
 * Интерфейс для асинхронного выполнения задач
 * @author dloshkarev
 */
public interface AsyncManager {
    /**
     * Добавляет задачу в очередь выполнения
     * @param taskTypeId идентификатор типа задачи, которому соответствует jndi-класса обработчика. Хранится в бд.
     * @param params параметры задачи. Предполагается, что вызывающая часть и код-исполнитель знают друг о друге и передаваемых параметрах.
     */
    BalancingVariants executeAsync(long taskTypeId, Map<String, Object> params) throws AsyncTaskException;
}
