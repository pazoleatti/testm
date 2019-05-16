package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflData;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ДТО для данных раздела 2 "Сведения о доходах и НДФЛ"
 * Для корректной обработки на фронтенде значения типа BigDecimal сериализуются в виде строк.
 */
@Getter
@Setter
public class NdflPersonIncomeDTO extends NdflData<Long> {

    /**
     * Ссылка на физлицо
     */
    private Long ndflPersonId;

    // Доход.Вид.Код (Графа 4) (КодДох)
    private String incomeCode;

    // Доход.Вид.Признак (Графа 5)
    private String incomeType;

    // Доход.Дата.Начисление (Графа 6)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date incomeAccruedDate;

    // Доход.Дата.Выплата (Графа 7)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date incomePayoutDate;

    // Доход.Источник выплаты.ОКТМО (Графа 8)
    private String oktmo;

    // Доход.Источник выплаты.КПП (Графа 9)
    private String kpp;

    // Доход.Сумма.Начисление (Графа 10)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal incomeAccruedSumm;

    // Доход.Сумма.Выплата (Графа 11)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal incomePayoutSumm;

    // Сумма вычета (Графа 12)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal totalDeductionsSumm;

    // Налоговая база (Графа 13)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal taxBase;

    // НДФЛ.Процентная ставка (Графа 14)
    private Integer taxRate;

    // НДФЛ.Расчет.Дата (Графа 15)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date taxDate;

    // НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal calculatedTax;

    // НДФЛ.Расчет.Сумма.Удержанный (Графа 17) (НУ)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal withholdingTax;

    // НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal notHoldingTax;

    // НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal overholdingTax;

    // НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
    private Long refoundTax;

    // НДФЛ.Перечисление в бюджет.Срок (Графа 21)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date taxTransferDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Дата (Графа 22)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date paymentDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Номер (Графа 23)
    private String paymentNumber;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal taxSumm;

    /**
     * Идентификатор операции
     */
    protected String operationId;

    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected String rowNum;

    private String inp;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public NdflPersonIncome toIncome() {
        NdflPersonIncome income = new NdflPersonIncome();
        income.setId(id);
        income.setNdflPersonId(ndflPersonId);
        income.setOperationId(operationId);
        if (rowNum != null && !rowNum.isEmpty()) {
            income.setRowNum(new BigDecimal(rowNum));
        }
        income.setIncomeCode(incomeCode);
        income.setIncomeType(incomeType);
        income.setIncomeAccruedDate(incomeAccruedDate);
        income.setIncomePayoutDate(incomePayoutDate);
        income.setOktmo(oktmo);
        income.setKpp(kpp);
        income.setIncomeAccruedSumm(incomeAccruedSumm);
        income.setIncomePayoutSumm(incomePayoutSumm);
        income.setTotalDeductionsSumm(totalDeductionsSumm);
        income.setTaxBase(taxBase);
        income.setTaxRate(taxRate);
        income.setTaxDate(taxDate);
        income.setCalculatedTax(calculatedTax);
        income.setWithholdingTax(withholdingTax);
        income.setNotHoldingTax(notHoldingTax);
        income.setOverholdingTax(overholdingTax);
        income.setRefoundTax(refoundTax);
        income.setTaxTransferDate(taxTransferDate);
        income.setPaymentDate(paymentDate);
        income.setPaymentNumber(paymentNumber);
        income.setTaxSumm(taxSumm);
        income.setOperationId(operationId);
        income.setSourceId(sourceId);
        return income;
    }
}
