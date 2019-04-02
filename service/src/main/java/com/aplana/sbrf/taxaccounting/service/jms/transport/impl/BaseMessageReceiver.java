package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * Получатель сообщений.
 */
public class BaseMessageReceiver {

    @JmsListener(destination = JmsBaseConfig.TO_NDFL_QUEUE, id="messageReceiver", containerFactory = "fundConnectionFactory")
    public void handleMessage(Message<String> message) {
        System.out.println("Raw message: " + message.getPayload());
        System.out.println("Headers: " + message.getHeaders());
    }
}
