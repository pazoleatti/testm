package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.jms.TaxMessageReceipt;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;

/**
 * Сервис для обработки сообщений, полученных от ЭДО
 */
public interface EdoResponseHandler {

    /**
     * Обработка ответной технологической квитанции
     *
     * @param transportMessage Транспортное сообщение, привязанное к ответной технологической квитанации
     * @param taxMessageReceipt Представление XML-сообщение от ЭДО, являющиеся ответной технологической квитанцией
     */
    void handleTechReceipt(TransportMessage transportMessage, TaxMessageReceipt taxMessageReceipt);
}
