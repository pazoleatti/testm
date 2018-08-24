package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Map;

/**
 * Интерфейс задачи, выполняющей некую бизнес логику асинхронно
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
    String RESTART_LINKED_TASKS_MSG = "Запуск операции приведет к отмене некоторых ранее запущенных операций. Операции, уже выполняемые Системой, выполнятся до конца, но результат их выполнения не будет сохранен. Продолжить?";
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
     * Выполняет проверку возможности выполения задачи, если выполнение задачи возможно, то возвращает в какой очереди выполнять иначе выбрасывает исключение
     *
     * @param taskDescription описание задачи
     * @param user пользователь, который выполняет задачу
     * @param params произвольные параметры для выполнения задачи
     * @return очередь, в которой будет выполнена задача
     */
    AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params) throws AsyncTaskException;

    /**
     * Метод возвращает описание задачи с указание конкретных объектов, над которыми она выполняется. Шаблон описания берется из {@link AsyncTaskType}
     *
     * @param userInfo текущий пользователь
     * @param params произвольные параметры для выполнения задачи
     * @return описание задачи
     */
    String getDescription(TAUserInfo userInfo, Map<String, Object> params);
}
