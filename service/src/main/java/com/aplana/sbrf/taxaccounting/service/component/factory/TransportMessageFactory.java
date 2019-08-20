package com.aplana.sbrf.taxaccounting.service.component.factory;

import com.aplana.sbrf.taxaccounting.model.jms.BaseMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;

public interface TransportMessageFactory {

    /**
     * Создание экземляра Транспортного Сообщения для отправки в ЭДО с предопределенными для этого параметрами:
     * - статус: отправлено
     * - тип: исходящее
     * - получатель сообщения: ЭДО
     * - отправитель сообщения: НДФЛ
     * - UUID сообщения: UUID сообщения, определенный в XML-объекте
     * - Дата и время сообщения: Дата и время сообщения, определеное в XML-объекте
     *
     * @param baseXmlMessage XML объект сообщения
     * @param messageBody тело сообщения
     * @return экземпляр Транспортного Сообщения
     */
    TransportMessage createOutcomeMessageToEdo(BaseMessage baseXmlMessage, String messageBody);
}
