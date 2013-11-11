package com.aplana.sbrf.taxaccounting.scheduler.core.mdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(
        activationConfig = {@ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"
        ), @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "jms/transportQueue"
        )}
)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class CommandListenerMDB implements MessageListener {
    private static final Log LOG = LogFactory.getLog(CommandListenerMDB.class);

    @Override
    public void onMessage(Message message) {
        LOG.info("onMessage started");
        /*try {
            TextMessage textMessage = (TextMessage) message;
            TaskContext context = TaskUtils.contextToObject(textMessage.getText());
            LOG.info("jndi: " + context.getJndi());
            Map<String, TaskParam> customProps = context.getParams();
            LOG.info("id: " + (Integer) customProps.get("id").getTypifiedValue());
            LOG.info("isFalse: " + (Boolean) customProps.get("isFalse").getTypifiedValue());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
