package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.model.Configuration;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.exception.ConfigurationParameterAbsentException;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"jms", "development"})
public class BaseMessageSenderImpl implements MessageSender {
    private static final Log LOG = LogFactory.getLog(BaseMessageSenderImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void sendMessage(String message) throws ConfigurationParameterAbsentException {
        Configuration configuration = configurationService.fetchByEnum(ConfigurationParam.JNDI_QUEUE_OUT);
        if (configuration == null || StringUtils.isEmpty(configuration.getValue())) {
            throw new ConfigurationParameterAbsentException("не задан конфигурационный параметр: \"" +
                    ConfigurationParam.JNDI_QUEUE_OUT.getCaption() + "\"");
        }

        LOG.debug(String.format("Попытка отправить сообщение '%s' в очередь%s", message, configuration.getValue()));
        try {
            jmsTemplate.convertAndSend(configuration.getValue(), message);
        } catch (JmsException e) {
            throw new RuntimeException(e);
        }
        LOG.info(String.format("Сообщение '%s' успешно отправлено в очередь %s", message, configuration.getValue()));
    }
}
