package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Map;

/**
 * Интерфейс асинхронной задачи, выполняющей некую бизнес логику асинхронно
 *
 * @author dloshkarev
 */
public interface AsyncTask {

    String LOCK_INFO_MSG = "Запрашиваемая операция уже запущена %s пользователем %s. Вы добавлены в список получателей оповещения о выполнении данной операции.";
    String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";
    String CREATE_IDETNTIFY_TASK = "Поставлена в очередь на исполнение операция \"%s\"";
    String CANCEL_MSG = "Запрашиваемая операция \"%s\" уже запущена (находится в очереди на выполнение). Отменить задачу и создать новую?";
    String RESTART_MSG = "Запрашиваемая операция \"%s\" уже запущена (выполняется Системой). При ее отмене результат не будет сохранен. Отменить задачу и создать новую?";

    String CHECK_TASK = "Выполнение операции \"%s\" невозможно, т.к. %s";
    String CANCEL_TASK = "Пользователем \"%s\" отменена операция \"%s\". Причина отмены: %s";
    String LOCK_CURRENT = "\"%s\" пользователем \"%s\" запущена операция \"%s\"";
    String CANCEL_TASK_IN_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция уже выполняется Системой.";
    String CANCEL_TASK_NOT_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция находится в очереди на выполнение.";

    /**
     * Выполнение бизнес-логики задачи
     *
     * @param taskData данные задачи, которую надо выполнить
     */
    void execute(AsyncTaskData taskData);

    /**
     * Определяет очередь размещения задачи: кратковременная или длительная.
     *
     * @param taskDescription описание задачи
     * @param user            пользователь, который выполняет задачу
     * @param params          произвольные параметры для выполнения задачи
     * @return очередь, в которой будет выполнена задача
     * @throws AsyncTaskException в случае невозможности выполнения задачи
     */
    AsyncQueue defineTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params) throws AsyncTaskException;

    /**
     * Возвращает описание задачи с указание конкретных объектов, над которыми она выполняется. Шаблон описания берется из {@link AsyncTaskType}
     *
     * @param userInfo текущий пользователь
     * @param params   произвольные параметры для выполнения задачи
     * @return описание задачи
     */
    String createDescription(TAUserInfo userInfo, Map<String, Object> params);
}