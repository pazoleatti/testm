package com.aplana.sbrf.taxaccounting.jms;

import com.aplana.sbrf.taxaccounting.service.MappingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Слушатель JMS-сообщений при загрузке транспортных файлов
 *
 * @author Dmitriy Levykin
 */
public class TransportMessageListener implements MessageListener {

    @Autowired
    MappingService mappingService;

    public static final String FILENAME_PROPERTY_NAME = "FILENAME";
    public static final String DATA_PROPERTY_NAME = "DATA";

    private static final Log logger = LogFactory.getLog(TransportMessageListener.class);

    @Override
    public void onMessage(Message message) {
        logger.debug("onMessage: " + message);
        if (message == null || !(message instanceof MapMessage)) {
            return;
        }

        MapMessage mm = (MapMessage) message;

        try {
            String fileName = mm.getString(FILENAME_PROPERTY_NAME);
            byte[] bodyFile = mm.getBytes(DATA_PROPERTY_NAME);

            logger.debug("fileName = " + fileName);
            logger.debug("bodyFile.length = " + bodyFile.length);

            mappingService.addFormData(fileName, bodyFile);

        } catch (JMSException e) {
            logger.error("Retrieving error message: " + e.getMessage(), e);
        }
    }
}
