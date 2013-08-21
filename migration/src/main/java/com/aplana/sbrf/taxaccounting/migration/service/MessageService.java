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
    private final Log logger = LogFactory.getLog(getClass());

    private Queue fileQueue;
    private ConnectionFactory conFactory;
    private Connection connection;

    /**
     * Оправка единичного сообщения с формированным файлом
     * @param fileName имя файла
     * @param bodyFile содержимое файла
     * @throws JMSException
     */
    public void sendMessage(final String fileName, final byte[] bodyFile) throws JMSException {

        connection = conFactory.createConnection();

        logger.debug("About to put message on queue. Queue[" + fileQueue + "]");
        logger.debug("FileName[" + fileName + "]");
        logger.debug("BodyFileLength[" + bodyFile.length + "]");

        try {

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MapMessage map = session.createMapMessage();
            map.setString("FILENAME", fileName);
            map.setBytes("DATA", bodyFile);
            MessageProducer queueSender = session.createProducer(fileQueue);
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
        connection = conFactory.createConnection();
        logger.debug("About to put message on queue. Queue[" + fileQueue + "]");
        try {

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer queueSender = session.createProducer(fileQueue);
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                MapMessage map = session.createMapMessage();
                map.setString("FILENAME", file.getKey());
                map.setBytes("DATA", file.getValue());
                queueSender.send(map);
                coutSendedFiles++;
                logger.debug("File Name [" + file.getKey() + "] with Data File Length [" + file.getValue().length + "] is sended.");
            }
        } catch (JMSException e) {
            logger.error("Error occured in sendMessagePack() " + e.getMessage(), e);
        }
        return coutSendedFiles;
    }

    public ConnectionFactory getConFactory() {
        return conFactory;
    }

    public void setConFactory(ConnectionFactory conFactory) {
        this.conFactory = conFactory;
    }

    public void setFileQueue(Queue fileQueue) {
        this.fileQueue = fileQueue;
    }

    public Queue getFileQueue() {
        return fileQueue;
    }
}
