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
    CONFIRMED(1, "Подтверждено"),
    /**
     * Ошибка
     */
    ERROR(2, "Ошибка"),
    /**
     * Отправлено
     */
    SENT(3, "Отправлено"),
    /**
     * Получено
     */
    RECEIVED(4, "Получено"),
    /**
     * Дубликат
     */
    DUPLICATE(5, "Дубликат"),

    /**
     * Отменено
     */
    CANCELED(6, "Отменено");

    private int intValue;
    private String text;

    public static TransportMessageState fromInt(int intValue) {
        for (TransportMessageState state : values()) {
            if (state.intValue == intValue) {
                return state;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус транспортного сообщения \"" + intValue + "\"");
    }

    public String getText() {
        return text;
    }

    @JsonValue
    public int getIntValue() {
        return intValue;
    }
}
