package com.aplana.sbrf.taxaccounting.async.mdb;

import com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskTypeEntity;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncInterruptionManagerLocal;
import com.aplana.sbrf.taxaccounting.async.persistence.AsyncTaskPersistenceServiceLocal;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/**
 * Обработчик асинхронных задач из очереди с быстрым выполнением
 * @author dloshkarev
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/shortAsyncQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ShortAsyncMDB implements MessageListener {

    private static final Log log = LogFactory.getLog(ShortAsyncMDB.class);

    @EJB
    private AsyncTaskPersistenceServiceLocal persistenceService;

    @EJB
    private AsyncInterruptionManagerLocal interruptionManager;

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            log.error("Incorrect message type!");
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
                //Получаем данные задачи
                AsyncTask task = (AsyncTask) ic.lookup(taskType.getHandlerJndi());
                Map<String, Object> params = asyncMdbObject.getParams();
                //Сохраняем данные о потоке-исполнителе в менеджере, для того чтобы можно было остановить поток
                //interruptionManager.addTask((String) params.get(LOCKED_OBJECT.name()), Thread.currentThread());
                task.execute(params);
            } else {
                log.error("Unexpected empty message content. Instance of com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject cannot be null!");
            }
        } catch (JMSException e) {
            log.error("Incorrect parameters data! It must be instance of com.aplana.sbrf.taxaccounting.async.entity.AsyncMdbObject but it wasn't", e);
        } catch (AsyncTaskPersistenceException e) {
            log.error("Task parameters with id = " + taskTypeId + " were not found!", e);
        } catch (NamingException e) {
            log.error("Async task handler was not found! JNDI = " + taskType.getHandlerJndi(), e);
        }
    }
}
