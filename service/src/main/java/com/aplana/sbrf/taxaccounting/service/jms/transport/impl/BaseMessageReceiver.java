package com.aplana.sbrf.taxaccounting.service.jms.transport.impl;

import com.aplana.sbrf.taxaccounting.model.jms.TaxMessageReceipt;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.jms.JmsBaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

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
    private TransactionHelper transactionHelper;

    @JmsListener(destination = JmsBaseConfig.TO_NDFL_QUEUE, id="messageReceiver", containerFactory = "jmsListenerContainerFactory")
    public void handleMessage(Message<String> message) {
        final String messageContent = message.getPayload();
        transactionHelper.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(TaxMessageReceipt.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                    StringReader messageReader = new StringReader(messageContent);
                    TaxMessageReceipt taxMessageReceipt = (TaxMessageReceipt) jaxbUnmarshaller.unmarshal(messageReader);
                } catch (JAXBException e) {
                    LOG.warn("Ошибка парсинга XML сообщения: " + messageContent, e);
                }
                return null;
            }
        });

        System.out.println("Raw message: " + messageContent);
        System.out.println("Headers: " + message.getHeaders());
    }

    public void switchJmsListener() {
        jmsListenerEndpointRegistry.stop();
    }
}
