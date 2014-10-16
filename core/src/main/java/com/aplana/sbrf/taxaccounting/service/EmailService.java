package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

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

    /**
     * Проверка авторизации с заданными параметрами
     * @param server хост
     * @param port порт
     * @param login логин
     * @param password пароль
     * @param logger логгер для вывода ошибок
     * @return true - если авторизация прошла успешно, иначе false
     */
     boolean testAuth(String server, String port, String login, String password, Logger logger);
}
