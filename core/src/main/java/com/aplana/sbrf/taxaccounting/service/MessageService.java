package com.aplana.sbrf.taxaccounting.service;

import java.util.Map;

/**
 * Сервис отправки JMS-сообщений
 */
public interface MessageService {
    /**
     * Отправка файлов JMS-сообщениями
     * @param filesMap
     * @return Количество отправленных файлов
     */
    public int sendFiles(Map<String, byte[]> filesMap);
}
