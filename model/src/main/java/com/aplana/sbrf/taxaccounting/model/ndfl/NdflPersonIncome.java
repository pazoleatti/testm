package com.aplana.sbrf.taxaccounting.model.ndfl;


import com.aplana.sbrf.taxaccounting.model.util.NdflComparator;
import com.aplana.sbrf.taxaccounting.model.util.RnuNdflStringComparator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

/**
 * Сведения о доходах физического лица
 */
@Getter
@Setter
@ToString
public class NdflPersonIncome extends NdflPersonOperation {

    public static final Integer ACCRUED_ROW_TYPE = 100;
    public static final Integer PAYOUT_ROW_TYPE = 200;
    public static final Integer OTHER_ROW_TYPE = 300;

    // Доход.Вид.Код (Графа 4) (КодДох)
    protected String incomeCode;

    // Доход.Вид.Признак (Графа 5)
    protected String incomeType;

    // Доход.Дата.Начисление (Графа 6)
    protected Date incomeAccruedDate;

    // Доход.Дата.Выплата (Графа 7)
    protected Date incomePayoutDate;

    // Доход.Источник выплаты.ОКТМО (Графа 8)
    protected String oktmo;

    // Доход.Источник выплаты.КПП (Графа 9)
    protected String kpp;

    // Доход.Сумма.Начисление (Графа 10)
    protected BigDecimal incomeAccruedSumm;

    // Доход.Сумма.Выплата (Графа 11)
    protected BigDecimal incomePayoutSumm;

    // Сумма вычета (Графа 12)
    protected BigDecimal totalDeductionsSumm;

    // Налоговая база (Графа 13)
    protected BigDecimal taxBase;

    // НДФЛ.Процентная ставка (Графа 14)
    protected Integer taxRate;

    // НДФЛ.Расчет.Дата (Графа 15)
    protected Date taxDate;

    // НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
    protected BigDecimal calculatedTax;

    // НДФЛ.Расчет.Сумма.Удержанный (Графа 17) (НУ)
    protected BigDecimal withholdingTax;

    // НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
    protected BigDecimal notHoldingTax;

    // НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
    protected BigDecimal overholdingTax;

    // НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
    protected Long refoundTax;

    // НДФЛ.Перечисление в бюджет.Срок (Графа 21)
    protected Date taxTransferDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Дата (Графа 22)
    protected Date paymentDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Номер (Графа 23)
    protected String paymentNumber;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
    protected BigDecimal taxSumm;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    protected Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    protected String modifiedBy;

    /**
     * Поле, используемое для сортировки Раздела 2. Хранит дату первого начисления в рамках этой операции (набора строк формы с одинаковым ID операции)
     */
    protected Date operationDate;

    /**
     * Поле, используемое для сортировки Раздела 2. Показывает дату действия, отражаемого в этой строке.
     */
    protected Date actionDate;

    /**
     * Тип строки. Поле, используемое для сортировки Раздела 2
     */
    protected Integer rowType;

    public static final String TABLE_NAME = "ndfl_person_income";
    public static final String SEQ = "seq_ndfl_person_income";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "oktmo", "kpp", "income_code", "income_type",
            "income_accrued_date", "income_payout_date", "income_accrued_summ", "income_payout_summ", "total_deductions_summ",
            "tax_base", "tax_rate", "tax_date", "calculated_tax", "withholding_tax", "not_holding_tax", "overholding_tax",
            "refound_tax", "tax_transfer_date", "payment_date", "payment_number", "tax_summ", "source_id", "modified_date", "modified_by", "asnu_id",
            "operation_date", "action_date", "row_type", "oper_info_id"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "oktmo", "kpp", "incomeCode", "incomeType",
            "incomeAccruedDate", "incomePayoutDate", "incomeAccruedSumm", "incomePayoutSumm", "totalDeductionsSumm",
            "taxBase", "taxRate", "taxDate", "calculatedTax", "withholdingTax", "notHoldingTax", "overholdingTax",
            "refoundTax", "taxTransferDate", "paymentDate", "paymentNumber", "taxSumm", "sourceId", "modifiedDate", "modifiedBy", "asnuId",
            "operationDate", "actionDate", "rowType", "operInfoId"};

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getSeq() {
        return SEQ;
    }

    @Override
    public String[] getColumns() {
        return COLUMNS;
    }

    @Override
    public String[] getFields() {
        return FIELDS;
    }

    /**
     * Является ли строка фиктивной
     */
    public boolean isDummy() {
        return "0".equals(operationId) && equalsZero(taxBase) && taxRate == 0 &&
                (equalsZero(incomeAccruedSumm) && equalsZero(calculatedTax) ||
                        equalsZero(incomePayoutSumm) && equalsZero(withholdingTax));
    }

    private boolean equalsZero(BigDecimal x) {
        return x != null && new BigDecimal("0").compareTo(x) == 0;
    }

    /**
     * Получение компаратора для сортировки сведений о доходах физического лица {@link NdflPersonIncome}
     *
     * @param <T> тип объекта {@link NdflPerson} или его наследник
     * @return компаратор {@link NdflComparator} для сортировки {@link NdflPersonIncome}
     */
    public static <T extends NdflPerson> Comparator<NdflPersonIncome> getComparator() {
        return new NdflComparator<NdflPersonIncome>() {
            @Override
            public int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                int operationDateComp = compareValues(o1.getOperationDate(), o2.getOperationDate(), null);
                if (operationDateComp != 0) {
                    return operationDateComp;
                }

                int operationIdComp = compareValues(o1.operationId, o2.operationId, RnuNdflStringComparator.INSTANCE);
                if (operationIdComp != 0) {
                    return operationIdComp;
                }

                int actionDateComp = compareValues(o1.getActionDate(), o2.getActionDate(), null);
                if (actionDateComp != 0) {
                    return actionDateComp;
                }

                return compareValues(o1.getRowType(), o2.getRowType(), null);
            }
        };
    }
}
