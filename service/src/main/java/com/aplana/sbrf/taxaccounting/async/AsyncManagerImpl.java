package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;

/**
 * Реализация менеджера асинхронных задач на Spring и без использования ejb и jms
 * При добавлении асинхронной задачи, сохраняет ее параметры в БД в сериализованном виде, а выполнением задач занимается специализированный класс {@link AsyncTaskThreadContainer.AsyncTaskQueueProcessor}
 * @author dloshkarev
 */
@Service
public class AsyncManagerImpl implements AsyncManager {
    private static final Log LOG = LogFactory.getLog(AsyncManagerImpl.class);

    @Autowired
    private AsyncTaskDao asyncTaskDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public AsyncTask getAsyncTaskBean(Long taskTypeId) throws AsyncTaskException {
        AsyncTaskTypeData asyncTaskType = asyncTaskDao.getTaskData(taskTypeId);

        if (asyncTaskType == null) {
            throw new AsyncTaskException("Параметры асинхронной задачи с идентификатором " + taskTypeId + " не найдены в таблице ASYNC_TASK_TYPE");
        }

        if (asyncTaskType.getHandlerClassName().startsWith("ejb")) {
            throw new AsyncTaskException("Некорректный формат имени для класса-исполнителя");
        }

        AsyncTask task;
        try {
            task = applicationContext.getBean(asyncTaskType.getHandlerClassName(), AsyncTask.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AsyncTaskException("Не найден класс-обработчик асинхронной задачи с идентификатором " + taskTypeId + ", указанным в таблице ASYNC_TASK_TYPE", e);
        } catch (Exception e) {
            throw new AsyncTaskException("Непредвиденная ошибка при получении класса-обработчика асинхронной задачи с идентификатором " + taskTypeId + ", указанным в таблице ASYNC_TASK_TYPE", e);
        }
        return task;
    }

    @Override
    public void executeAsync(long taskTypeId, Map<String, Object> params, BalancingVariants balancingVariant) throws AsyncTaskException {
        LOG.debug("Async task creation has been started");

        try {
            // Проверка параметров
            checkParams(params);
            // Получение и проверка класса обработчика задачи
            getAsyncTaskBean(taskTypeId);
            // Сохранение в очереди асинхронных задач - запись в БД
            asyncTaskDao.addTask(taskTypeId, balancingVariant, params);

            LOG.info(String.format("Задача с ключом %s помещена в очередь %s", params.get(AsyncTask.RequiredParams.LOCKED_OBJECT.name()), balancingVariant.name()));
            LOG.debug("Async task creation has been finished successfully");
        } catch (Exception e) {
            LOG.error("Async task creation has been failed!", e);
            throw new AsyncTaskException(e);
        }
    }

    @Override
    public BalancingVariants checkCreate(long taskTypeId, Map<String, Object> params) throws AsyncTaskException {
        try {
            return getAsyncTaskBean(taskTypeId).checkTaskLimit(params);
        } catch (Exception e) {
            throw new AsyncTaskException(e);
        }
    }

    /**
     * Проверяем обязательные параметры. Они должны быть заполнены и содержать значение правильного типа
     * @param params параметры
     */
    private void checkParams(Map<String, Object> params) throws AsyncTaskSerializationException {
        if (params == null) {
            throw new IllegalArgumentException("Параметры асинхронной задачи не могут быть пустыми!");
        }

        for (AsyncTask.RequiredParams key : AsyncTask.RequiredParams.values()) {
            if (!params.containsKey(key.name())) {
                throw new IllegalArgumentException("Не указан обязательный параметр \"" + key.name() + "\"!");
            }
            if (!key.getClazz().isInstance(params.get(key.name()))) {
                throw new IllegalArgumentException("Обязательный параметр \"" + key.name() + "\" имеет неправильный тип " + params.get(key.name()).getClass().getName() + "! Должен быть: " + key.getClazz().getName());
            }
        }

        for (Map.Entry<String, Object> param : params.entrySet()) {
            //Все параметры должны быть сериализуемы
            if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                throw new AsyncTaskSerializationException("Параметр \"" + param.getKey() + "\" не поддерживает сериализацию!");
            }
        }
    }
}
