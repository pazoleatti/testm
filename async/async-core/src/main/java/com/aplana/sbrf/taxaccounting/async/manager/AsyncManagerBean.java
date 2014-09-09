package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
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

    private final Log LOG = LogFactory.getLog(getClass());

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
        LOG.debug("Async task creation has been started");
        ConnectionFactory connectionFactory;
        Queue queue;
        Connection connection = null;
        if (balancingVariant == BalancingVariants.SHORT) {
            connectionFactory = shortAsyncConnectionFactory;
            queue = shortAsyncQueue;
        } else {
            connectionFactory = longAsyncConnectionFactory;
            queue = longAsyncQueue;
        }

        try {
            AsyncMdbObject asyncMdbObject = new AsyncMdbObject();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                //Все параметры должны быть сериализуемы
                if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                    throw new AsyncTaskSerializationException("Parameter \"" + param.getKey() + "\" doesn't support serialization!");
                }
            }
            asyncMdbObject.setTaskTypeId(taskTypeId);
            asyncMdbObject.setParams(params);

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);

            ObjectMessage objectMessage = session.createObjectMessage();
            objectMessage.setObject(asyncMdbObject);
            messageProducer.send(objectMessage);
            LOG.debug("Async task creation has been finished successfully");
        } catch (JMSException e) {
            LOG.error("Async task creation has been failed!", e);
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
}
