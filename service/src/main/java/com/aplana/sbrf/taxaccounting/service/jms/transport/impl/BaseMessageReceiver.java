package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.EdoMessageService;
import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Получатель сообщений.
 */
@Profile({"jms", "development"})
@Component
public class BaseMessageReceiver {

    private static final Log LOG = LogFactory.getLog(BaseMessageReceiver.class);

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Autowired
    private EdoMessageService edoMessageService;

    @JmsListener(destination = JmsBaseConfig.TO_NDFL_QUEUE_NAME, id="messageReceiver", containerFactory = "jmsListenerContainerFactory")
    public void handleMessage(Message<String> message) {
        final String messageContent = message.getPayload();
        System.out.println("Raw message: " + messageContent);
        System.out.println("Headers: " + message.getHeaders());
        edoMessageService.accept(messageContent);
    }

    public void switchJmsListener() {
        jmsListenerEndpointRegistry.stop();
    }
}
