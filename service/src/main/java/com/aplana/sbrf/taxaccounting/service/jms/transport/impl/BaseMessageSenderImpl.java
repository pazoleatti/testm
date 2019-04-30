package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("jms")
public class BaseMessageSenderImpl implements MessageSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendMessage(String fileName) {
        System.out.println(fileName);
        jmsTemplate.convertAndSend(fileName);
    }
}