package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import java.io.Serializable;
import java.util.Map;

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

    @Override
    public void executeAsync(long taskTypeId, Map<String, Object> params, BalancingVariants balancingVariant) throws AsyncTaskException {
        log.debug("Async task creation has been started");
        ConnectionFactory connectionFactory;
        Queue queue;
        Connection connection = null;

        try {
            if (balancingVariant == BalancingVariants.SHORT) {
                connectionFactory = shortAsyncConnectionFactory;
                queue = shortAsyncQueue;
            } else {
                connectionFactory = longAsyncConnectionFactory;
                queue = longAsyncQueue;
            }
            checkParams(params);
            AsyncMdbObject asyncMdbObject = new AsyncMdbObject();
            asyncMdbObject.setTaskTypeId(taskTypeId);
            asyncMdbObject.setParams(params);

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);

            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setObject(asyncMdbObject);
            messageProducer.send(objectMessage);
            log.debug("Async task creation has been finished successfully");
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

    /**
     * Проверяем обязательные параметры. Они должны быть заполнены и содержать значение правильного типа
     * @param params параметры
     */
    private void checkParams(Map<String, Object> params) throws AsyncTaskSerializationException {
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
