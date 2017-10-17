package com.aplana.sbrf.taxaccounting.model.error;

/**
 * Тип сообщения
 */
public enum MessageType {
    //Непредвиденная ошибка
    ERROR,
    //Бизнес-ошибка (фактически просто сообщение для пользователя)
    BUSINESS_ERROR,
    //Предупреждение?
    WARNING,
    //Успешно
    SUCCESS,
    //Ошибка + список логов
    MULTI_ERROR
}
