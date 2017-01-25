package com.aplana.sbrf.taxaccounting.service;

/**
 * Сервис отправки JMS-сообщений
 */
public interface MessageService {

    /**
     * Получение данных из MQ КСШ
     */
    void getRateMessages();
}
