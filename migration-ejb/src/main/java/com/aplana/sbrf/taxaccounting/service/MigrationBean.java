package com.aplana.sbrf.taxaccounting.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Map;

/**
 * EJB-модуль миграции
 */
@Stateless
@Local(MessageServiceLocal.class)
public class MigrationBean implements MessageService {

    private final Log logger = LogFactory.getLog(getClass());
    private static final String FILENAME_PROPERTY_NAME = "FILENAME";
    private static final String DATA_PROPERTY_NAME = "DATA";
    private static final String JMS_FACTORY = "jms/transportConnectionFactory";
    private static final String JMS_QUEUE = "jms/transportQueue";

    /**
     * Отправка множества сообщений
     *
     * @param files мапа с именем и содержимым будущего файла
     * @return Количество корректно отправленных сообщений
     * @throws javax.jms.JMSException
     */
    private int sendMessagePack(Map<String, byte[]> files) throws JMSException {
        int coutSendedFiles = 0;

        ConnectionFactory connectionFactory;
        Queue queue;

        try {
            InitialContext jndiContext = new InitialContext();
            connectionFactory = (ConnectionFactory) jndiContext.lookup(JMS_FACTORY);
            queue = (Queue) jndiContext.lookup(JMS_QUEUE);
        } catch (NamingException e) {
            logger.error("Resources \"" + JMS_FACTORY + "\" or \"" + JMS_QUEUE + "\"  not found. " + e.getMessage(), e);
            return 0;
        }

        Connection connection = connectionFactory.createConnection();
        logger.debug("About to put message on queue. Queue[" + queue + "]");
        try {

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer queueSender = session.createProducer(queue);
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                MapMessage map = session.createMapMessage();
                map.setString(FILENAME_PROPERTY_NAME, file.getKey());
                map.setBytes(DATA_PROPERTY_NAME, file.getValue());
                queueSender.send(map);
                coutSendedFiles++;
                logger.debug("File [name=" + file.getKey() + ", size=" + file.getValue().length + "] is sended.");
            }
            return coutSendedFiles;
        } catch (JMSException e) {
            logger.error("Error occured in sendMessagePack() " + e.getMessage(), e);
        }
        return 0;
    }


    @Override
    public int sendFiles(Map<String, byte[]> map) {
        logger.debug("Send files ("+map.size()+")");
        try {
            return sendMessagePack(map);
        } catch (JMSException e) {
            logger.error("Error by sending messages with transport files data. " + e.getMessage());
        }
        return 0;
    }
}