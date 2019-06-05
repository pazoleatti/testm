package com.aplana.sbrf.taxaccounting.service.util;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Формирует сообшения с изменениями полей при редактировании строк формы РНУ
 */
public class NdflRowEditChangelogBuilder {
    private int section;
    private Map<String, String> valuesByFieldBefore;
    private Map<String, String> valuesByFieldAfter;
    public String notificationMessage;
    private List<String> changedFields = new ArrayList<>();

    public NdflRowEditChangelogBuilder(NdflPersonIncome incomeBefore, NdflPersonIncome incomeAfter) {
        this.section = 2;
        this.valuesByFieldBefore = toMap(incomeBefore);
        this.valuesByFieldAfter = toMap(incomeAfter);
        findChanges();
    }

    public NdflRowEditChangelogBuilder(NdflPersonDeduction deductionBefore, NdflPersonDeduction deductionAfter) {
        this.section = 3;
        this.valuesByFieldBefore = toMap(deductionBefore);
        this.valuesByFieldAfter = toMap(deductionAfter);
        findChanges();
    }

    public NdflRowEditChangelogBuilder(NdflPersonPrepayment prepaymentBefore, NdflPersonPrepayment prepaymentAfter) {
        this.section = 4;
        this.valuesByFieldBefore = toMap(prepaymentBefore);
        this.valuesByFieldAfter = toMap(prepaymentAfter);
        findChanges();
    }

    private void findChanges() {
        for (String fieldName : valuesByFieldBefore.keySet()) {
            String before = valuesByFieldBefore.get(fieldName);
            String after = valuesByFieldAfter.get(fieldName);
            if (!Objects.equals(before, after)) {
                changedFields.add(fieldName);
            }
        }
    }

    public boolean hasChanges() {
        return !changedFields.isEmpty();
    }

    public List<String> build(long declarationDataId, BigDecimal rowNumBefore, BigDecimal rowNumAfter) {
        List<String> changelog = new ArrayList<>();
        if (!changedFields.isEmpty()) {
            notificationMessage = "Для формы " + declarationDataId + " выполнена замена значений в разделе " + section;
            if (!Objects.equals(rowNumBefore, rowNumAfter)) {
                changelog.add("Выполнено изменение параметров, указанных пользователем: " + StringUtils.join(changedFields, ", ", "\"") + "." +
                        " В ходе выполнения сортировки строк, изменен номер отредактированной строки: \"" + rowNumBefore + "\"->\"" + rowNumAfter + "\".");
            } else {
                changelog.add("Выполнено изменение параметров, указанных пользователем: " + StringUtils.join(changedFields, ", ", "\"") + ".");
            }
            for (String fieldName : changedFields) {
                String valueBefore = valuesByFieldBefore.get(fieldName);
                String valueAfter = valuesByFieldAfter.get(fieldName);
                changelog.add("Раздел " + section + ". Строка " + rowNumBefore + ". Выполнена замена значения \"" + fieldName + "\": \"" + valueBefore + "\"→\"" + valueAfter + "\".");
            }
        }
        return changelog;
    }

    private Map<String, String> toMap(NdflPersonIncome income) {
        Map<String, String> values = new HashMap<>();
        values.put("КПП", format(income.getKpp()));
        values.put("ОКТМО", format(income.getOktmo()));
        values.put("Код дохода", format(income.getIncomeCode()));
        values.put("Признак дохода", format(income.getIncomeType()));
        values.put("Дата начисления", format(income.getIncomeAccruedDate()));
        values.put("Дата выплаты", format(income.getIncomePayoutDate()));
        values.put("Начислено", format(income.getIncomeAccruedSumm()));
        values.put("Выплачено", format(income.getIncomePayoutSumm()));
        values.put("Налоговая база", format(income.getTaxBase()));
        values.put("Сумма вычета", format(income.getTotalDeductionsSumm()));
        values.put("Процентная ставка, %", format(income.getTaxRate()));
        values.put("Дата расчета", format(income.getTaxDate()));
        values.put("Исчисленный", format(income.getCalculatedTax()));
        values.put("Не удержанный", format(income.getNotHoldingTax()));
        values.put("Удержанный", format(income.getWithholdingTax()));
        values.put("Излишне удержанный", format(income.getOverholdingTax()));
        values.put("Возвращённый НП", format(income.getRefoundTax()));
        values.put("Срок", formatPossibleZeroDate(income.getTaxTransferDate()));
        values.put("Дата ПП", format(income.getPaymentDate()));
        values.put("Сумма ПП", format(income.getTaxSumm()));
        values.put("Номер ПП", format(income.getPaymentNumber()));
        return values;
    }

    private Map<String, String> toMap(NdflPersonDeduction deduction) {
        Map<String, String> values = new HashMap<>();
        values.put("Код вычета", format(deduction.getTypeCode()));
        values.put("Тип документа", format(deduction.getNotifType()));
        values.put("Код источника", format(deduction.getNotifSource()));
        values.put("Номер документа", format(deduction.getNotifNum()));
        values.put("Дата уведомления", format(deduction.getNotifDate()));
        values.put("Сумма документа", format(deduction.getNotifSumm()));
        values.put("Код дохода", format(deduction.getIncomeCode()));
        values.put("Дата дохода", format(deduction.getIncomeAccrued()));
        values.put("Сумма дохода", format(deduction.getIncomeSumm()));
        values.put("Сумма вычета (с начала налогового периода):", format(deduction.getPeriodPrevSumm()));
        values.put("Дата заявления о применении вычета", format(deduction.getPeriodPrevDate()));
        values.put("Сумма вычета (в текущем отчетном периоде):", format(deduction.getPeriodCurrSumm()));
        values.put("Дата применения вычета", format(deduction.getPeriodCurrDate()));
        return values;
    }

    private Map<String, String> toMap(NdflPersonPrepayment prepayment) {
        Map<String, String> values = new HashMap<>();
        values.put("Сумма", format(prepayment.getSumm()));
        values.put("Номер уведомления", format(prepayment.getNotifNum()));
        values.put("Код НО", format(prepayment.getNotifSource()));
        values.put("Дата уведомления", format(prepayment.getNotifDate()));
        return values;
    }

    private String format(Date date) {
        return DateUtils.commonDateFormat(date, "__");
    }

    private String formatPossibleZeroDate(Date date) {
        return DateUtils.formatPossibleZeroDate(date, "__");
    }

    private String format(Object object) {
        return object == null || object.toString().isEmpty() ? "__" : object.toString();
    }
}
