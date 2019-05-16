package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflData;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ДТО для данных раздела 3
 */
@Getter
@Setter
public class NdflPersonDeductionDTO extends NdflData<Long> {

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

    // Код вычета (Графа 3)
    private String typeCode;

    // Документ о праве на налоговый вычет.Тип (Графа 4)
    private String notifType;

    // Документ о праве на налоговый вычет.Дата (Графа 5)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date notifDate;

    // Документ о праве на налоговый вычет.Номер (Графа 6)
    private String notifNum;

    // Документ о праве на налоговый вычет.Код источника (Графа 7)
    private String notifSource;

    // Документ о праве на налоговый вычет.Сумма (Графа 8)
    private BigDecimal notifSumm;

    // Начисленный доход.Дата (Графа 10)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date incomeAccrued;

    // Начисленный доход.Код дохода (Графа 11)
    private String incomeCode;

    // Начисленный доход.Сумма (Графа 12)
    private BigDecimal incomeSumm;

    // Применение вычета.Предыдущий период.Дата (Графа 13)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date periodPrevDate;

    // Применение вычета.Предыдущий период.Сумма (Графа 14)
    private BigDecimal periodPrevSumm;

    // Применение вычета.Текущий период.Дата (Графа 15)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date periodCurrDate;

    // Применение вычета.Текущий период.Сумма (Графа 16)
    private BigDecimal periodCurrSumm;

    private String inp;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public NdflPersonDeduction toDeduction() {
        NdflPersonDeduction deduction = new NdflPersonDeduction();
        deduction.setId(id);
        deduction.setNdflPersonId(ndflPersonId);
        deduction.setOperationId(operationId);
        if (rowNum != null && !rowNum.isEmpty()) {
            deduction.setRowNum(new BigDecimal(rowNum));
        }
        deduction.setSourceId(sourceId);
        deduction.setTypeCode(typeCode);
        deduction.setNotifType(notifType);
        deduction.setNotifDate(notifDate);
        deduction.setNotifNum(notifNum);
        deduction.setNotifSource(notifSource);
        deduction.setNotifSumm(notifSumm);
        deduction.setIncomeAccrued(incomeAccrued);
        deduction.setIncomeCode(incomeCode);
        deduction.setIncomeSumm(incomeSumm);
        deduction.setPeriodPrevDate(periodPrevDate);
        deduction.setPeriodPrevSumm(periodPrevSumm);
        deduction.setPeriodCurrDate(periodCurrDate);
        deduction.setPeriodCurrSumm(periodCurrSumm);
        return deduction;
    }
}
