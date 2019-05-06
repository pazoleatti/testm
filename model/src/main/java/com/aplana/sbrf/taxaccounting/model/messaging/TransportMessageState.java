package com.aplana.sbrf.taxaccounting.model.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * Статус Транспортного сообщения.
 */
@AllArgsConstructor
public enum TransportMessageState {
    /**
     * Подтверждено
     */
    CONFIRMED(1),
    /**
     * Ошибка
     */
    ERROR(2),
    /**
     * Отправлено
     */
    SENT(3),
    /**
     * Получено
     */
    RECEIVED(4),
    /**
     * Дубликат
     */
    DUPLICATE(5);

    private int intValue;

    public static TransportMessageState fromInt(int intValue) {
        for (TransportMessageState state : values()) {
            if (state.intValue == intValue) {
                return state;
            }
        }
        throw new IllegalArgumentException(String.valueOf(intValue));
    }

    @JsonValue
    public int getIntValue() {
        return intValue;
    }
}
