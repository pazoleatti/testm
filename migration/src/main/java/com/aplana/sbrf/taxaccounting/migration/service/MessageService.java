package com.aplana.sbrf.taxaccounting.migration.service;

import javax.jms.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * @author Alexander Ivanov
 * @date 19.08.13
 */
public class MessageService {

	public static final String FILENAME_PROPERTY_NAME = "FILENAME";
	public static final String DATA_PROPERTY_NAME = "DATA";

	private static final Log logger = LogFactory.getLog(MessageService.class);

    private Queue queue;
    private ConnectionFactory connectionFactory;
    private Connection connection;

    /**
     * Оправка единичного сообщения с формированным файлом
     * @param fileName имя файла
     * @param bodyFile содержимое файла
     * @throws JMSException
     */
    public void sendMessage(final String fileName, final byte[] bodyFile) throws JMSException {

        connection = connectionFactory.createConnection();

        logger.debug("About to put message on queue. Queue[" + queue + "]");
        logger.debug("FileName[" + fileName + "]");
        logger.debug("BodyFileLength[" + bodyFile.length + "]");

        try {

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MapMessage map = session.createMapMessage();
            map.setString(FILENAME_PROPERTY_NAME, fileName);
            map.setBytes(DATA_PROPERTY_NAME, bodyFile);
            MessageProducer queueSender = session.createProducer(queue);
            queueSender.send(map);

        } catch (JMSException e) {
            logger.error("Error occured in sendMessage() " + e.getMessage(), e);
        }
    }

    /**
     * Отправка множества сообщений
     * @param files мапа с именем и содержимым будущего файла
     * @return Количество корректно отправленных сообщений
     * @throws JMSException
     */
    public Integer sendMessagePack(Map<String, byte[]> files) throws JMSException {
        Integer coutSendedFiles = 0;
        connection = connectionFactory.createConnection();
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
                logger.debug("File Name [" + file.getKey() + "] with Data File Length [" + file.getValue().length + "] is sended.");
            }
        } catch (JMSException e) {
            logger.error("Error occured in sendMessagePack() " + e.getMessage(), e);
        }
        return coutSendedFiles;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Queue getQueue() {
        return queue;
    }
}
