package com.aplana.sbrf.taxaccounting.jms;

import javax.jms.*;

/**
 * Слушатель JMS-сообщений
 *
 * @author Dmitriy Levykin
 */
public class JmsMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        System.out.println(">>> "+message);
    }
}
