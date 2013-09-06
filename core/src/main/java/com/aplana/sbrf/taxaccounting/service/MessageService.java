package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.migration.MigrationSendResult;

/**
 * Сервис отправки JMS-сообщений
 */
public interface MessageService {
    /**
     * Отправка файлов JMS-сообщениями
     * @return Результат миграции
     */
    public MigrationSendResult sendFiles();
}
