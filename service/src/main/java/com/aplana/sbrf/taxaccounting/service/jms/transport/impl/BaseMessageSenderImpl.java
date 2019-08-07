package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"jms", "development"})
public class BaseMessageSenderImpl implements MessageSender {
    private static final Log LOG = LogFactory.getLog(BaseMessageSenderImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendMessage(String message) {
        LOG.debug("Попытка отправить сообщение '" + message + "' в очередь" + JmsBaseConfig.FROM_NDFL_QUEUE_NAME);
        jmsTemplate.convertAndSend(JmsBaseConfig.FROM_NDFL_QUEUE_NAME, message);
        LOG.info("Сообщение '" + message + "' успешно отправлено в очередь " + JmsBaseConfig.FROM_NDFL_QUEUE_NAME);
    }
}
