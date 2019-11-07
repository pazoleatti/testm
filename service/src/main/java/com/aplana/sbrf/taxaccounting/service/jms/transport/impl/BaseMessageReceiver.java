package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.EdoMessageService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Получатель сообщений.
 */
@Profile({"jms", "development"})
@Component
public class BaseMessageReceiver implements MessageListener {

    private static final Log LOG = LogFactory.getLog(BaseMessageReceiver.class);

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Autowired
    private EdoMessageService edoMessageService;

    @Autowired
    private TransactionHelper transactionHelper;

    @Override
    public void onMessage(final javax.jms.Message message) {


        transactionHelper.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                String messageContent;
                try {
                    messageContent = ((TextMessage) message).getText();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
                LOG.debug("Получено сообщение в очередь " + JmsBaseConfig.TO_NDFL_QUEUE_JNDI_NAME + ": " + messageContent);
                edoMessageService.accept(messageContent);
                return null;
            }

        });
    }

    public void switchJmsListener() {
        jmsListenerEndpointRegistry.stop();
    }
}
