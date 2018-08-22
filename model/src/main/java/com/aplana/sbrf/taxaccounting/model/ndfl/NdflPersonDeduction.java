package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.util.NdflComparator;

import java.math.BigDecimal;
import java.util.*;

/**
 * Стандартные, социальные и имущественные налоговые вычеты (Раздел 3)
 * @author Andrey Drunk
 */
public class NdflPersonDeduction extends NdflPersonOperation {

    // Код вычета (Графа 3)
    private String typeCode;

    // Документ о праве на налоговый вычет.Тип (Графа 4)
    private String notifType;

    // Документ о праве на налоговый вычет.Дата (Графа 5)
    private Date notifDate;

    // Документ о праве на налоговый вычет.Номер (Графа 6)
    private String notifNum;

    // Документ о праве на налоговый вычет.Код источника (Графа 7)
    private String notifSource;

    // Документ о праве на налоговый вычет.Сумма (Графа 8)
    private BigDecimal notifSumm;

    // Начисленный доход.Дата (Графа 10)
    private Date incomeAccrued;

    // Начисленный доход.Код дохода (Графа 11)
    private String incomeCode;

    // Начисленный доход.Сумма (Графа 12)
    private BigDecimal incomeSumm;

    // Применение вычета.Предыдущий период.Дата (Графа 13)
    private Date periodPrevDate;

    // Применение вычета.Предыдущий период.Сумма (Графа 14)
    private BigDecimal periodPrevSumm;

    // Применение вычета.Текущий период.Дата (Графа 15)
    private Date periodCurrDate;

    // Применение вычета.Текущий период.Сумма (Графа 16)
    private BigDecimal periodCurrSumm;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public static final String TABLE_NAME = "ndfl_person_deduction";
    public static final String SEQ = "seq_ndfl_person_deduction";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "type_code",
            "notif_type", "notif_date", "notif_num", "notif_source",
            "notif_summ", "income_accrued", "income_code", "income_summ",
            "period_prev_date", "period_prev_summ",
            "period_curr_date", "period_curr_summ", "source_id", "modified_date", "modified_by", "asnu_id"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "typeCode",
            "notifType", "notifDate", "notifNum", "notifSource",
            "notifSumm", "incomeAccrued", "incomeCode", "incomeSumm",
            "periodPrevDate", "periodPrevSumm",
            "periodCurrDate", "periodCurrSumm", "sourceId", "modifiedDate", "modifiedBy", "asnuId"};

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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    public Date getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(Date notifDate) {
        this.notifDate = notifDate;
    }

    public String getNotifNum() {
        return notifNum;
    }

    public void setNotifNum(String notifNum) {
        this.notifNum = notifNum;
    }

    public String getNotifSource() {
        return notifSource;
    }

    public void setNotifSource(String notifSource) {
        this.notifSource = notifSource;
    }

    public BigDecimal getNotifSumm() {
        return notifSumm;
    }

    public void setNotifSumm(BigDecimal notifSumm) {
        this.notifSumm = notifSumm;
    }

    public Date getIncomeAccrued() {
        return incomeAccrued;
    }

    public void setIncomeAccrued(Date incomeAccrued) {
        this.incomeAccrued = incomeAccrued;
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public BigDecimal getIncomeSumm() {
        return incomeSumm;
    }

    public void setIncomeSumm(BigDecimal incomeSumm) {
        this.incomeSumm = incomeSumm;
    }

    public Date getPeriodPrevDate() {
        return periodPrevDate;
    }

    public void setPeriodPrevDate(Date periodPrevDate) {
        this.periodPrevDate = periodPrevDate;
    }

    public BigDecimal getPeriodPrevSumm() {
        return periodPrevSumm;
    }

    public void setPeriodPrevSumm(BigDecimal periodPrevSumm) {
        this.periodPrevSumm = periodPrevSumm;
    }

    public Date getPeriodCurrDate() {
        return periodCurrDate;
    }

    public void setPeriodCurrDate(Date periodCurrDate) {
        this.periodCurrDate = periodCurrDate;
    }

    public BigDecimal getPeriodCurrSumm() {
        return periodCurrSumm;
    }

    public void setPeriodCurrSumm(BigDecimal periodCurrSumm) {
        this.periodCurrSumm = periodCurrSumm;
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
        return "NdflPersonDeduction{" +
                ", typeCode='" + typeCode + '\'' +
                ", notifType='" + notifType + '\'' +
                ", notifDate=" + notifDate +
                ", notifNum='" + notifNum + '\'' +
                ", notifSource='" + notifSource + '\'' +
                ", notifSumm=" + notifSumm +
                ", incomeAccrued=" + incomeAccrued +
                ", incomeCode='" + incomeCode + '\'' +
                ", incomeSumm=" + incomeSumm +
                ", periodPrevDate=" + periodPrevDate +
                ", periodPrevSumm=" + periodPrevSumm +
                ", periodCurrDate=" + periodCurrDate +
                ", periodCurrSumm=" + periodCurrSumm +
                '}';
    }

    /**
     * Получение компаратора для сортировки сведений о налоговых вычетах физического лица {@link NdflPersonDeduction}
     *
     * @param ndflPerson физическое лицо
     * @param <T>        тип объекта {@link NdflPerson} или его наследник
     * @return компаратор {@link NdflComparator} для сортировки {@link NdflPersonDeduction}
     */
    public static <T extends NdflPerson> Comparator<NdflPersonDeduction> getComparator(final T ndflPerson){
        return new NdflComparator<NdflPersonDeduction>() {
            @Override
            public int compare(NdflPersonDeduction o1, NdflPersonDeduction o2) {
                final List<String> operationIdOrderList = getOperationIdOrderList(ndflPerson);
                int incomeAccruedComp = compareValues(o1.incomeAccrued, o2.incomeAccrued, null);
                if (incomeAccruedComp != 0) {
                    return incomeAccruedComp;
                }

                int operationIdComp = compareValues(o1.operationId, o2.operationId, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return operationIdOrderList.indexOf(s1) - operationIdOrderList.indexOf(s2);
                    }
                });
                if (operationIdComp != 0) {
                    return operationIdComp;
                }

                return compareValues(o1.periodCurrDate, o2.periodCurrDate, null);
            }

            /**
             * Получение списка идентификаторов операций для сведений о доходах физического лица (Раздел 2) {@link NdflPersonIncome#operationId}
             *
             * @param ndflPerson физическое лицо
             * @return список идентификаторов операций свединий о доходах ФЛ (Раздел 2) в порядке их следования в ndflPerson.incomes
             */
            private List<String> getOperationIdOrderList(T ndflPerson){
                Set<String> operationIdOrderList = new LinkedHashSet<>();
                for (NdflPersonIncome income : ndflPerson.getIncomes()){
                    operationIdOrderList.add(income.getOperationId());
                }
                return new ArrayList<>(operationIdOrderList);
            }
        };
    }

    /**
     * Сортировка списка объектов {@link NdflPersonDeduction} на основе компаратора с обновлением номеров строк. Начало нумерации
     * начинается с минимального номера строки из {@link NdflPerson#getIncomes()} или 1
     *
     * @param ndflPerson объект {@link NdflPerson}, содержащий {@link List<NdflPersonDeduction>}
     * @return отсортированный список {@link List<NdflPersonDeduction>} с номерами строк, идущими по возрастанию порядка сортировки
     */
    public static List<NdflPersonDeduction> sortAndUpdateRowNum(NdflPerson ndflPerson) {
        Collections.sort(ndflPerson.getDeductions(), getComparator(ndflPerson));
        BigDecimal deductionRowNum = getMinRowNum(ndflPerson.getDeductions());
        for (NdflPersonDeduction deduction : ndflPerson.getDeductions()) {
            deduction.setRowNum(deductionRowNum);
            deductionRowNum = deductionRowNum != null ? deductionRowNum.add(new BigDecimal("1")) : null;
        }
        return ndflPerson.getDeductions();
    }
}
