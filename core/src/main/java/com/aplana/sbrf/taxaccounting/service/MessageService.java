package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.migration.MigrationSendResult;

/**
 * Сервис отправки JMS-сообщений
 */
public interface MessageService {
    /**
     * Отправка файлов JMS-сообщениями
     *
     * @param rnus список видов РНУ
     * @param year список годов за которые РНУ сформированы
     * @return Результат миграции
     */
    MigrationSendResult sendFiles(long[] rnus, long[] year);
}
