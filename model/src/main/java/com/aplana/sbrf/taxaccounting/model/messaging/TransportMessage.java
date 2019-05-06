package com.aplana.sbrf.taxaccounting.model.messaging;

import com.aplana.sbrf.taxaccounting.model.BlobDto;
import com.aplana.sbrf.taxaccounting.model.DepartmentName;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.joda.time.LocalDateTime;

/**
 * Транспортное сообщение, участвующее в обмене между подсистемами.
 */
@Data
public class TransportMessage {

    /**
     * Уникальный идентификатор.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * Уникальный uuid, указанный в теле xml-сообщения.
     */
    private String messageUuid;
    /**
     * Дата и время сообщения.
     */
    private LocalDateTime dateTime;
    /**
     * Тип сообщения.
     */
    private TransportMessageType type;
    /**
     * ID системы-отправителя.
     */
    private Subsystem senderSubsystem;
    /**
     * ID системы-получателя.
     */
    private Subsystem receiverSubsystem;
    /**
     * Тип данных в теле сообщения.
     */
    private TransportMessageContentType contentType;
    /**
     * Статус обработки сообщения.
     */
    private TransportMessageState state;
    /**
     * Данные о файле, который передавался через папку обмена.
     */
    private BlobDto blob;
    /**
     * Имя исходного файла, который отправлялся в ФНС.
     */
    private String sourceFileName;
    /**
     * Инициатор создания сообщения (пользователь/система).
     */
    private TAUser initiatorUser;
    /**
     * Текст дополнительного пояснения.
     */
    private String explanation;
    /**
     * Краткая информация о форме, к которой привязано сообщение.
     */
    private DeclarationShortInfo declaration;
    /**
     * Имеется ли поле "body" в базе.
     */
    @JsonIgnore
    private boolean hasBody;

    public boolean hasBody() {
        return hasBody;
    }

    /**
     * Генерирует название файла для тела сообщения.
     */
    @JsonProperty("bodyFileName")
    public String getBodyFileName() {
        if (!hasBody) return null;

        String prefix;
        switch (this.contentType) {
            case RECEIPT_DOCUMENT:
                prefix = "TaxMessageReceipt";
                break;
            case NDFL6:
            case NDFL2_1:
            case NDFL2_2:
                prefix = "TaxMessageDocument";
                break;
            default:
                prefix = "TaxMessageTechDocument";
        }

        return prefix + "_" + this.id + ".xml";
    }
}
