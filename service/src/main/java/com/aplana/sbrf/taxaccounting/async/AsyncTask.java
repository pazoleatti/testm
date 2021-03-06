package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Map;

/**
 * Интерфейс асинхронной задачи, выполняющей некую бизнес логику асинхронно
 */
public interface AsyncTask {

    String LOCK_INFO_MSG = "Запрашиваемая операция уже запущена %s пользователем %s. Вы добавлены в список получателей оповещения о выполнении данной операции.";
    String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";
    String CREATE_IDENTIFY_TASK = "Поставлена в очередь на исполнение операция \"%s\"";
    String CANCEL_MSG = "Запрашиваемая операция \"%s\" уже запущена (находится в очереди на выполнение). Отменить задачу и создать новую?";
    String RESTART_MSG = "Запрашиваемая операция \"%s\" уже запущена (выполняется Системой). При ее отмене результат не будет сохранен. Отменить задачу и создать новую?";

    String CHECK_TASK = "Невозможно выполнение задачи %s. Критерии возможности выполнения задач задаются в конфигурационных параметрах. За разъяснениями обратитесь к Администратору.";
    String CANCEL_TASK = "Пользователем \"%s\" отменена операция \"%s\". Причина отмены: %s";
    String LOCK_CURRENT = "\"%s\" пользователем \"%s\" запущена операция \"%s\"";

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
     * Возвращает описание задачи с указание конкретных объектов, над которыми она выполняется. Шаблон описания берется из {@link AsyncTaskType}.
     * В новом механизме асинок планируется что шаблон описания будет браться из
     * {@link com.aplana.sbrf.taxaccounting.service.component.operation.DeclarationDataAsyncTaskDescriptor}, поэтому этот
     * метод устарел. Позже нужно выпилить описание также из {@link AsyncTaskType}, т.к. в постановке в определении данных у типа
     * асинхронной задачи нет описания.
     *
     * @param userInfo текущий пользователь
     * @param params   произвольные параметры для выполнения задачи
     * @return описание задачи
     */
    @Deprecated
    String createDescription(TAUserInfo userInfo, Map<String, Object> params);
}