package com.aplana.sbrf.taxaccounting.model.messaging;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * Тип содержимого Транспортного сообщения.
 */
@AllArgsConstructor
public enum TransportMessageContentType {
    /**
     * Неизвестно.
     */
    UNKNOWN(0),
    /**
     * Квитанция о приёме.
     */
    RECEIPT_DOCUMENT(1),
    /**
     * Уведомление об отказе.
     */
    REJECTION_NOTICE(2),
    /**
     * Уведомление об уточнении.
     */
    CORRECTION_NOTICE(3),
    /**
     * Извещение о вводе (окончательном приёме).
     */
    ENTRY_NOTICE(4),
    /**
     * Реестр принятых документов.
     */
    RECEIVED_DOCUMENTS_REGISTRY(5),
    /**
     * Протокол приёма 2-НДФЛ.
     */
    NDFL2_ACCEPTANCE_PROTOCOL(6),
    /**
     * Сообщение об ошибке.
     */
    ERROR_MESSAGE(7),
    /**
     * Технологическая квитанция.
     */
    TECH_RECEIPT(8),
    /**
     * 6-НДФЛ.
     */
    NDFL6(11),
    /**
     * 2-НДФЛ (1).
     */
    NDFL2_1(12),
    /**
     * 2-НДФЛ (2).
     */
    NDFL2_2(13);

    private int intValue;

    public static TransportMessageContentType fromInt(int intValue) {
        for (TransportMessageContentType type : values()) {
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
