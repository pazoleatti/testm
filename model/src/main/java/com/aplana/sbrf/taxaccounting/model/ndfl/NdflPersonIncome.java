package com.aplana.sbrf.taxaccounting.model.ndfl;


import com.aplana.sbrf.taxaccounting.model.util.NdflComparator;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.RnuNdflStringComparator;

import java.math.BigDecimal;
import java.util.*;

/**
 * Сведения о доходах физического лица
 *
 * @author Andrey Drunk
 */
public class NdflPersonIncome extends NdflPersonOperation {

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
    protected Long taxSumm;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    protected Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    protected String modifiedBy;

    public static final String TABLE_NAME = "ndfl_person_income";
    public static final String SEQ = "seq_ndfl_person_income";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "oktmo", "kpp", "income_code", "income_type",
            "income_accrued_date", "income_payout_date", "income_accrued_summ", "income_payout_summ", "total_deductions_summ",
            "tax_base", "tax_rate", "tax_date", "calculated_tax", "withholding_tax", "not_holding_tax", "overholding_tax",
            "refound_tax", "tax_transfer_date", "payment_date", "payment_number", "tax_summ", "source_id", "modified_date", "modified_by", "asnu_id"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "oktmo", "kpp", "incomeCode", "incomeType",
            "incomeAccruedDate", "incomePayoutDate", "incomeAccruedSumm", "incomePayoutSumm", "totalDeductionsSumm",
            "taxBase", "taxRate", "taxDate", "calculatedTax", "withholdingTax", "notHoldingTax", "overholdingTax",
            "refoundTax", "taxTransferDate", "paymentDate", "paymentNumber", "taxSumm", "sourceId", "modifiedDate", "modifiedBy", "asnuId"};

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

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public String getIncomeType() {
        return incomeType;
    }

    public void setIncomeType(String incomeType) {
        this.incomeType = incomeType;
    }


    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public Date getIncomeAccruedDate() {
        return incomeAccruedDate;
    }

    public void setIncomeAccruedDate(Date incomeAccruedDate) {
        this.incomeAccruedDate = incomeAccruedDate;
    }

    public Date getIncomePayoutDate() {
        return incomePayoutDate;
    }

    public void setIncomePayoutDate(Date incomePayoutDate) {
        this.incomePayoutDate = incomePayoutDate;
    }

    public BigDecimal getIncomeAccruedSumm() {
        return incomeAccruedSumm;
    }

    public void setIncomeAccruedSumm(BigDecimal incomeAccruedSumm) {
        this.incomeAccruedSumm = incomeAccruedSumm;
    }

    public BigDecimal getIncomePayoutSumm() {
        return incomePayoutSumm;
    }

    public void setIncomePayoutSumm(BigDecimal incomePayoutSumm) {
        this.incomePayoutSumm = incomePayoutSumm;
    }

    public BigDecimal getTotalDeductionsSumm() {
        return totalDeductionsSumm;
    }

    public void setTotalDeductionsSumm(BigDecimal totalDeductionsSumm) {
        this.totalDeductionsSumm = totalDeductionsSumm;
    }

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }

    public Date getTaxDate() {
        return taxDate;
    }

    public void setTaxDate(Date taxDate) {
        this.taxDate = taxDate;
    }

    public BigDecimal getCalculatedTax() {
        return calculatedTax;
    }

    public void setCalculatedTax(BigDecimal calculatedTax) {
        this.calculatedTax = calculatedTax;
    }

    public BigDecimal getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(BigDecimal withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public BigDecimal getNotHoldingTax() {
        return notHoldingTax;
    }

    public void setNotHoldingTax(BigDecimal notHoldingTax) {
        this.notHoldingTax = notHoldingTax;
    }

    public BigDecimal getOverholdingTax() {
        return overholdingTax;
    }

    public void setOverholdingTax(BigDecimal overholdingTax) {
        this.overholdingTax = overholdingTax;
    }

    public Long getRefoundTax() {
        return refoundTax;
    }

    public void setRefoundTax(Long refoundTax) {
        this.refoundTax = refoundTax;
    }

    public Date getTaxTransferDate() {
        return taxTransferDate;
    }

    public void setTaxTransferDate(Date taxTransferDate) {
        this.taxTransferDate = taxTransferDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public Long getTaxSumm() {
        return taxSumm;
    }

    public void setTaxSumm(Long taxSumm) {
        this.taxSumm = taxSumm;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return "NdflPersonIncomeFilter{" +
                ", incomeCode='" + incomeCode + '\'' +
                ", incomeType='" + incomeType + '\'' +
                ", oktmo='" + oktmo + '\'' +
                ", kpp='" + kpp + '\'' +
                ", incomeAccruedDate=" + incomeAccruedDate +
                ", incomePayoutDate=" + incomePayoutDate +
                ", incomeAccruedSumm=" + incomeAccruedSumm +
                ", incomePayoutSumm=" + incomePayoutSumm +
                ", totalDeductionsSumm=" + totalDeductionsSumm +
                ", taxBase=" + taxBase +
                ", taxRate=" + taxRate +
                ", taxDate=" + taxDate +
                ", calculatedTax=" + calculatedTax +
                ", withholdingTax=" + withholdingTax +
                ", notHoldingTax=" + notHoldingTax +
                ", overholdingTax=" + overholdingTax +
                ", refoundTax=" + refoundTax +
                ", taxTransferDate=" + taxTransferDate +
                ", paymentDate=" + paymentDate +
                ", paymentNumber='" + paymentNumber + '\'' +
                ", taxSumm=" + taxSumm +
                '}';
    }

    /**
     * Получение компаратора для сортировки сведений о доходах физического лица {@link NdflPersonIncome}
     *
     * @param ndflPerson физическое лицо
     * @param <T>        тип объекта {@link NdflPerson} или его наследник
     * @return компаратор {@link NdflComparator} для сортировки {@link NdflPersonIncome}
     */
    public static <T extends NdflPerson> Comparator<NdflPersonIncome> getComparator(final T ndflPerson) {
        return new NdflComparator<NdflPersonIncome>() {
            @Override
            public int compare(NdflPersonIncome o1, NdflPersonIncome o2) {
                Map<Pair<String, String>, Date> operationDates = getOperationDates();
                int operationDateComp = compareValues(operationDates.get(new Pair(o1.operationId, ndflPerson.getInp())), operationDates.get(new Pair(o2.operationId, ndflPerson.getInp())), null);
                if (operationDateComp != 0) {
                    return operationDateComp;
                }

                int operationIdComp = compareValues(o1.operationId, o2.operationId, RnuNdflStringComparator.INSTANCE);
                if (operationIdComp != 0) {
                    return operationIdComp;
                }

                int actionDateComp = compareValues(getActionDate(o1), getActionDate(o2), null);
                if (actionDateComp != 0) {
                    return actionDateComp;
                }

                return compareValues(getRowType(o1), getRowType(o2), null);
            }

            /**
             * Получить дату действия
             *
             * @param income объект строки дохода
             * @return вычисленная дата действия
             */
            private Date getActionDate(NdflPersonIncome income) {
                if (income.taxDate != null) {
                    return income.taxDate;
                } else {
                    return income.paymentDate;
                }
            }

            /**
             * Получить тип строки дохода
             *
             * @param income объект строки дохода
             * @return значение типа
             */
            private Integer getRowType(NdflPersonIncome income) {
                if (income.incomeAccruedDate != null) {
                    return 100;
                } else if (income.incomePayoutDate != null) {
                    return 200;
                }
                return 300;
            }

            /**
             * Получить мапу дат операций для каждой пары К = (идентификатор операции, Уникальный код клиента)
             *
             * @return ассоциативный массив дат операций по Map<K,V>, где V - дата операции или {@code null}
             */
            private Map<Pair<String, String>, Date> getOperationDates() {
                Map<Pair<String, String>, List<NdflPersonIncome>> incomesGroupedByOperationAndInp = new HashMap<>();


                for (NdflPersonIncome income : ndflPerson.getIncomes()) {
                    Pair operationAndInpKey = new Pair(income.getOperationId(), ndflPerson.getInp());
                    List<NdflPersonIncome> operationAndInpGroup = incomesGroupedByOperationAndInp.get(operationAndInpKey);
                    if (operationAndInpGroup == null) {
                        operationAndInpGroup = new ArrayList<>();
                    }
                    operationAndInpGroup.add(income);
                    incomesGroupedByOperationAndInp.put(operationAndInpKey, operationAndInpGroup);
                }

                Map<Pair<String, String>, Date> operationDates = new HashMap<>();

                for (Map.Entry<Pair<String, String>, List<NdflPersonIncome>> entry : incomesGroupedByOperationAndInp.entrySet()) {
                    Pair<String, String> key = entry.getKey();
                    List<NdflPersonIncome> group = entry.getValue();
                    List<Date> incomeAccruedDates = new ArrayList<>();
                    List<Date> incomePayoutDates = new ArrayList<>();
                    List<Date> paymentDates = new ArrayList<>();

                    for (NdflPersonIncome item : group) {
                        if (item.getIncomeAccruedDate() != null) {
                            incomeAccruedDates.add(item.getIncomeAccruedDate());
                        }
                        if (item.getIncomePayoutDate() != null) {
                            incomePayoutDates.add(item.getIncomePayoutDate());
                        }
                        if (item.getPaymentDate() != null) {
                            paymentDates.add(item.getPaymentDate());
                        }
                    }

                    if (!incomeAccruedDates.isEmpty()) {
                        Collections.sort(incomeAccruedDates);
                        operationDates.put(key, incomeAccruedDates.get(0));
                        continue;
                    }
                    if (!incomePayoutDates.isEmpty()) {
                        Collections.sort(incomePayoutDates);
                        operationDates.put(key, incomePayoutDates.get(0));
                        continue;
                    }
                    if (!paymentDates.isEmpty()) {
                        Collections.sort(paymentDates);
                        operationDates.put(key, paymentDates.get(0));
                        continue;
                    }

                    operationDates.put(key, null);
                }
                return operationDates;
            }
        };
    }
}
