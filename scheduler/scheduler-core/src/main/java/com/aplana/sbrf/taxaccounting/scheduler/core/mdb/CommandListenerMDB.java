package com.aplana.sbrf.taxaccounting.scheduler.core.mdb;

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

    @Override
    public void onMessage(Message message) {
        System.out.println("onMessage started");
        /*try {
            TextMessage textMessage = (TextMessage) message;
            TaskContext context = TaskUtils.contextToObject(textMessage.getText());
            System.out.println("jndi: " + context.getJndi());
            Map<String, TaskParam> customProps = context.getParams();
            System.out.println("id: " + (Integer) customProps.get("id").getTypifiedValue());
            System.out.println("isFalse: " + (Boolean) customProps.get("isFalse").getTypifiedValue());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
