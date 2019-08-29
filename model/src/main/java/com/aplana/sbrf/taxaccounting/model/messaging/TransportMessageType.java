package com.aplana.sbrf.taxaccounting.model.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * Тип Транспортного сообщения.
 */
@AllArgsConstructor
public enum TransportMessageType {

    OUTGOING(0, "Исходящее"),
    INCOMING(1, "Входящее");

    private int intValue;
    private String text;

    public static TransportMessageType fromInt(int intValue) {
        for (TransportMessageType type : values()) {
            if (type.intValue == intValue) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.valueOf(intValue));
    }

    public String getText() {
        return text;
    }

    @JsonValue
    public int getIntValue() {
        return intValue;
    }
}
