package com.aplana.sbrf.taxaccounting.model.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * Тип Транспортного сообщения.
 */
@AllArgsConstructor
public enum TransportMessageType {

    OUTGOING(0),
    INCOMING(1);

    private int intValue;

    public static TransportMessageType fromInt(int intValue) {
        for (TransportMessageType type : values()) {
            if (type.intValue == intValue) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.valueOf(intValue));
    }

    @JsonValue
    public int getIntValue() {
        return intValue;
    }
}
