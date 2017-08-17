package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.error.ExceptionMessage;
import com.aplana.sbrf.taxaccounting.model.error.MessageType;

/**
 * Сервис для формирования сообщения об ошибке для клиента
 */
public interface ErrorService {
    /**
     * Возвращает обёртку для сообщений об ошибке на клиент
     * Обрабатываются вложенные исключения. Учитываются настройки системы.
     *
     * @param messageType тип сообщения
     * @param messageCode текст сообщения
     * @param exception исключение
     * @return экземпляр класса-обёртки
     */
    ExceptionMessage getExceptionMessage(MessageType messageType, String messageCode, Exception exception);
}
