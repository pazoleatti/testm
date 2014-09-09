package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

/**
 * Сервис отправки email сообщений
 */
public interface EmailService {

    /**
     * Отправка сообщения
     * @param destinations адреса получателей
     * @param subject тема письма
     * @param text текст письма
     */
    void send(List<String> destinations, String subject, String text);
}
