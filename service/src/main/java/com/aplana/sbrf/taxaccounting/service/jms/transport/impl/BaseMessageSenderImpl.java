package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"jms", "development"})
public class BaseMessageSenderImpl implements MessageSender {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void sendMessage(String fileName) {
        Configuration configuration = configurationService.fetchByEnum(ConfigurationParam.JNDI_QUEUE_OUT);
        if (configuration == null) {
            throw new IllegalStateException("не задан конфигурационный параметр: \"" +
                    ConfigurationParam.JNDI_QUEUE_OUT.getCaption() + "\"");
        }

        System.out.println(fileName);
        jmsTemplate.convertAndSend(configuration.getValue(), fileName);
    }
}
