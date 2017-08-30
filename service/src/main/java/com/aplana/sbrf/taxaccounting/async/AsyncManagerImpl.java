package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Реализация менеджера асинхронных задач на Spring и без использования ejb и jms
 * При добавлении асинхронной задачи, сохраняет ее параметры в БД в сериализованном виде, а выполнением задач занимается специализированный класс {@link AsyncTaskThreadContainer.AsyncTaskLongQueueProcessor} или {@link AsyncTaskThreadContainer.AsyncTaskShortQueueProcessor}
 * @author dloshkarev
 */
@Component
public class AsyncManagerImpl implements AsyncManager {
    private static final Log LOG = LogFactory.getLog(AsyncManagerImpl.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private AsyncTaskDao asyncTaskDao;

    @Override
    public AsyncTask getAsyncTaskBean(Long taskTypeId) throws AsyncTaskException {
        AsyncTaskTypeData asyncTaskType = asyncTaskDao.getTaskData(taskTypeId);

        if (asyncTaskType == null) {
            throw new AsyncTaskException("Cannot find parameters for async task with id " + taskTypeId + " in database table ASYNC_TASK_TYPE");
        }

        if (asyncTaskType.getHandlerClassName().startsWith("ejb")) {
            throw new AsyncTaskException("Incorrect name for bean-executor");
        }

        AsyncTask task;
        try {
            task = applicationContext.getBean(asyncTaskType.getHandlerClassName(), AsyncTask.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AsyncTaskException("Cannot find bean-executor for task type with id " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
        } catch (Exception e) {
            throw new AsyncTaskException("Unexpected error during get bean-executor for task type " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
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
            AsyncTask task = getAsyncTaskBean(taskTypeId);
            if (applicationInfo.isProductionMode()) {
                // Сохранение в очереди асинхронных задач - запись в БД
                asyncTaskDao.addTask(taskTypeId, balancingVariant, params);
            } else {
                //Сразу запускаем класс-обработчик чтобы не сбивать очередь на стендах
                task.execute(params);
            }

            LOG.info(String.format("Task with key %s was put in queue %s", params.get(AsyncTask.RequiredParams.LOCKED_OBJECT.name()), balancingVariant.name()));
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

    @Transactional
    @Override
    public AsyncTaskData reserveTask(String node, int timeout, BalancingVariants balancingVariants, int maxTasksPerNode) {
        int rowsUpdated = asyncTaskDao.lockTask(node, timeout, balancingVariants, maxTasksPerNode);
        if (rowsUpdated != 0) {
            LOG.debug(String.format("Node '%s' reserve tasks: %s", node, rowsUpdated));
        }
        if (rowsUpdated > 1) {
            throw new IllegalArgumentException("Incorrect tasks reserved per node!");
        }
        if (rowsUpdated == 1) {
            return asyncTaskDao.getLockedTask(node, balancingVariants);
        }
        return null;
    }

    /**
     * Проверяем обязательные параметры. Они должны быть заполнены и содержать значение правильного типа
     * @param params параметры
     */
    private void checkParams(Map<String, Object> params) throws AsyncTaskSerializationException {
        if (params == null) {
            throw new IllegalArgumentException("Async task parameters cannot be empty!");
        }

        for (AsyncTask.RequiredParams key : AsyncTask.RequiredParams.values()) {
            if (!params.containsKey(key.name())) {
                throw new IllegalArgumentException("Required attribute \"" + key.name() + "\" is empty!");
            }
            if (!key.getClazz().isInstance(params.get(key.name()))) {
                throw new IllegalArgumentException("Required attribute \"" + key.name() + "\" has incorrect type " + params.get(key.name()).getClass().getName() + "! Must be: " + key.getClazz().getName());
            }
        }

        for (Map.Entry<String, Object> param : params.entrySet()) {
            //Все параметры должны быть сериализуемы
            if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                throw new AsyncTaskSerializationException("Attribute \"" + param.getKey() + "\" doesn't support serialization!");
            }
        }
    }
}
