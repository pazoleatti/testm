package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflData;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ДТО для данных раздела 4
 */
@Getter
@Setter
public class NdflPersonPrepaymentDTO extends NdflData<Long> {

    /**
     * Ссылка на физлицо
     */
    private Long ndflPersonId;

    /**
     * Идентификатор операции
     */
    protected String operationId;

    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected String rowNum;

    // Сумма фиксированного авансового платежа (Графа 4)
    private BigDecimal summ;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления
    private String notifNum;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date notifDate;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)
    private String notifSource;

    private String inp;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public NdflPersonPrepayment toPrepayment() {
        NdflPersonPrepayment prepayment = new NdflPersonPrepayment();
        prepayment.setId(id);
        prepayment.setNdflPersonId(ndflPersonId);
        prepayment.setOperationId(operationId);
        if (rowNum != null && !rowNum.isEmpty()) {
            prepayment.setRowNum(new BigDecimal(rowNum));
        }
        prepayment.setSourceId(sourceId);
        prepayment.setSumm(summ);
        prepayment.setNotifNum(notifNum);
        prepayment.setNotifDate(notifDate);
        prepayment.setNotifSource(notifSource);
        return prepayment;
    }
}
