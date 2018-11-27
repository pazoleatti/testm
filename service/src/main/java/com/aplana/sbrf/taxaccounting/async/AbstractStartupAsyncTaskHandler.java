package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Map;

/**
 * Обработчик для разных этапов в ходе постановки в очередь асинхронной задачи.
 * Выполняет логику, специфичную для разных типов асинхронных задач.
 *
 * @author dloshkarev
 */
public abstract class AbstractStartupAsyncTaskHandler {

    /**
     * Создание блокировки на объекте выполнения задачи
     *
     * @param lockKey  ключ блокировки, по которому она будет установлена
     * @param taskType тип асинхронной задачи, которая будет выполнена
     * @param user     пользователь, который инициировал операцию
     * @return null, если была установлена новая блокировка, иначе возвращается ранее установленная блокировка на этом объъекте
     */
    protected abstract LockData lockObject(String lockKey, AsyncTaskType taskType, TAUserInfo user);

    /**
     * Проверока существования асинронных задач, выполнение которым мешает постановке в очередь текущей задачи
     *
     * @param taskType тип асинхронной задачи, которая будет выполнена
     * @param user     пользователь, который инициировал операцию
     * @param logger   логгер с сообщениями о ходе выполнения операции
     * @return true, если задачи существуют
     */
    protected boolean checkExistTasks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
        return false;
    }

    /**
     * Выполняет специфичную логику, которая необходима в случае наличия задач, мешающих постановке в очередь текущей асинхронной задаче
     */
    protected void postCheckProcessing() {
    }

    /**
     * Выполняет специфичную логику связанную с отменой найденных задач, мешающих постановке в очередь текущей асинхронной задаче
     *
     * @param taskType тип асинхронной задачи, которая будет выполнена
     * @param user     пользователь, который инициировал операцию
     */
    protected void interruptTasks(AsyncTaskType taskType, TAUserInfo user) {
    }

    /**
     * Проверока существования блокировок, которые мешают постановке в очередь текущей задачи
     *
     * @param taskType тип асинхронной задачи, которая будет выполнена
     * @param user     пользователь, который инициировал операцию
     * @param logger   логгер с сообщениями о ходе выполнения операции
     * @return true, если задачи существуют
     */
    protected boolean checkLocks(AsyncTaskType taskType, TAUserInfo user, Logger logger) {
        return false;
    }

    /**
     * Выполняет проверку возможности выполения задачи, если выполнение задачи возможно, то возвращает в какой очереди выполнять иначе выбрасывает исключение
     *
     * @param taskDescription описание задачи
     * @param user            пользователь, который выполняет задачу
     * @param params          произвольные параметры для выполнения задачи
     * @return очередь, в которой будет выполнена задача
     * @throws AsyncTaskException в случае невозможности выполнения задачи
     */
    public AsyncQueue findTaskQueue(String taskDescription, TAUserInfo user, Map<String, Object> params) throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    public String createLockExistErrorMessage() {
        return "";
    }

    /**
     * Выполняет логику после создания задачи
     *
     * @param taskData данные выполняемой задачи
     * @param logger   логгер с сообщениями о ходе выполнения операции
     */
    void afterTaskCreated(AsyncTaskData taskData, Logger logger) {
        // в будущем все сообщения будут соответствовать одному шаблону
        String template;
        if (taskData.getType() == AsyncTaskType.IDENTIFY_PERSON) {
            template = AsyncTask.CREATE_IDETNTIFY_TASK;
        } else {
            template = AsyncTask.CREATE_TASK;
        }
        String message = String.format(template, taskData.getDescription());
        logger.info(message.replaceAll("\"\"", "\""));
    }
}