package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.List;

/**
 * Сервис отправки email сообщений
 */
public interface EmailService {

    /**
     * Отправка сообщения
     *
     * @param destinations адреса получателей
     * @param subject      тема письма
     * @param text         текст письма
     */
    void send(List<String> destinations, String subject, String text);

    /**
     * Проверка авторизации почтового клиента
     *
     * @param userInfo информация о пользователе
     * @return uuid идентификатор логгера
     */
    String checkAuthAccess(TAUserInfo userInfo);
}
