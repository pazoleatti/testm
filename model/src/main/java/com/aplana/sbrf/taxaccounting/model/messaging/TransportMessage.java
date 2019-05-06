package com.aplana.sbrf.taxaccounting.model.messaging;

import com.aplana.sbrf.taxaccounting.model.BlobDto;
import com.aplana.sbrf.taxaccounting.model.DepartmentName;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import com.aplana.sbrf.taxaccounting.model.TAUser;
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
     * ID формы, с которой связано сообщение.
     */
    private Long declarationId;
    /**
     * Подразделение формы.
     */
    private DepartmentName department;
}
