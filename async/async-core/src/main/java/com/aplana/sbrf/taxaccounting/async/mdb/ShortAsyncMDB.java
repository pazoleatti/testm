package com.aplana.sbrf.taxaccounting.async.mdb;

import com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskTypeEntity;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.persistence.AsyncTaskPersistenceServiceLocal;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Обработчик асинхронных задач из очереди с быстрым выполнением
 * @author dloshkarev
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/shortAsyncQueue")})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ShortAsyncMDB implements MessageListener {

    private final Log LOG = LogFactory.getLog(getClass());

    @EJB
    private AsyncTaskPersistenceServiceLocal persistenceService;

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            LOG.error("Incorrect message type!");
            return;
        }

        Long taskTypeId = null;
        AsyncTaskTypeEntity taskType = null;
        try {
            AsyncMdbObject asyncMdbObject = (AsyncMdbObject) ((ObjectMessage) message).getObject();
            //Получаем идентификатор типа задачи из параметров
            if (asyncMdbObject != null) {
                taskTypeId = asyncMdbObject.getTaskTypeId();
                //Получаем информацию о типе задачи
                taskType = persistenceService.getTaskTypeById(taskTypeId);
                //Запускаем класс-исполнитель
                InitialContext ic = new InitialContext();
                AsyncTask task = (AsyncTask) ic.lookup(taskType.getHandlerJndi());
                LOG.debug("Task with type \"" + taskType.getName() + "\" is starting in the short task queue");
                task.execute(asyncMdbObject.getParams());
            } else {
                LOG.error("Unexpected empty message content. Instance of com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject cannot be null!");
            }
        } catch (JMSException e) {
            LOG.error("Incorrect parameters data! It must be instance of com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject but it wasn't", e);
        } catch (AsyncTaskPersistenceException e) {
            LOG.error("Task parameters with id = " + taskTypeId + " were not found!", e);
        } catch (NamingException e) {
            LOG.error("Async task handler was not found! JNDI = " + taskType.getHandlerJndi(), e);
        }
    }
}
