package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.messaging.Message;

/**
 * Получатель сообщений.
 */
@Profile("jms")
public class BaseMessageReceiver {

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @JmsListener(destination = JmsBaseConfig.TO_NDFL_QUEUE, id="messageReceiver", containerFactory = "edoJmsListenerContainerFactory")
    public void handleMessage(Message<String> message) {
        System.out.println("Raw message: " + message.getPayload());
        System.out.println("Headers: " + message.getHeaders());
    }

    public void switchJmsListener() {
        jmsListenerEndpointRegistry.stop();
    }
}
