package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskTypeEntity;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.async.persistence.AsyncTaskPersistenceServiceLocal;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;

@Stateless
@Local(AsyncManagerLocal.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AsyncManagerBean implements AsyncManager {

    private final Log log = LogFactory.getLog(getClass());

    @Resource(name = "jms/shortAsyncConnectionFactory")
    private ConnectionFactory shortAsyncConnectionFactory;

    @Resource(name = "jms/shortAsyncQueue")
    private Queue shortAsyncQueue;

    @Resource(name = "jms/longAsyncConnectionFactory")
    private ConnectionFactory longAsyncConnectionFactory;

    @Resource(name = "jms/longAsyncQueue")
    private Queue longAsyncQueue;

    @EJB
    private AsyncTaskPersistenceServiceLocal persistenceService;

    @Override
    public BalancingVariants executeAsync(long taskTypeId, Map<String, Object> params) throws AsyncTaskException {
        log.debug("Async task creation has been started");
        ConnectionFactory connectionFactory;
        Queue queue;
        Connection connection = null;

        try {
            // Проверка параметров
            checkParams(params);
            // Проверка данных задачи
            AsyncTask task = checkTaskData(taskTypeId);
            BalancingVariants balancingVariant = task.checkTaskLimit(params);
            if (balancingVariant == BalancingVariants.SHORT) {
                connectionFactory = shortAsyncConnectionFactory;
                queue = shortAsyncQueue;
            } else {
                connectionFactory = longAsyncConnectionFactory;
                queue = longAsyncQueue;
            }

            //Формирование сообщения в очередь
            AsyncMdbObject asyncMdbObject = new AsyncMdbObject();
            asyncMdbObject.setTaskTypeId(taskTypeId);
            asyncMdbObject.setParams(params);

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);

            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setObject(asyncMdbObject);
            messageProducer.send(objectMessage);
            log.info(String.format("Задача с ключом %s помещена в очередь %s", params.get(LOCKED_OBJECT.name()), balancingVariant.name()));
            log.debug("Async task creation has been finished successfully");
            return balancingVariant;
        } catch (ServiceLoggerException e) {
            throw e;
        } catch (EJBException e) {
            e.printStackTrace();
            int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
            if (i != -1) {
                throw (ServiceLoggerException)ExceptionUtils.getThrowableList(e).get(i);
            }
            throw e;
        } catch (Exception e) {
            log.error("Async task creation has been failed!", e);
            throw new AsyncTaskException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (JMSException ignored) { }
            }
        }
    }

    private AsyncTask checkTaskData(long taskTypeId) {
        String jndi = null;
        try {
            AsyncTaskTypeEntity taskType = persistenceService.getTaskTypeById(taskTypeId);
            jndi = taskType.getHandlerJndi();
            InitialContext ic = new InitialContext();
            return (AsyncTask) ic.lookup(jndi);
        } catch (AsyncTaskPersistenceException e) {
            throw new IllegalArgumentException("Параметры асинхронной задачи с идентификатором " + taskTypeId + " не найдены в таблице ASYNC_TASK_TYPE", e);
        } catch (NamingException e) {
            throw new IllegalArgumentException("Не найден класс-обработчик c JNDI-именем " + jndi + ", указанным в таблице ASYNC_TASK_TYPE для записи с id = " + taskTypeId, e);
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
