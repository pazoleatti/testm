package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxFormType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Создает название периода
 */
@Component
@ScriptExposed
public class DepartmentReportPeriodFormatter {

    private static final FastDateFormat DD_MM_YYYY = FastDateFormat.getInstance("dd.MM.yyyy");

    /**
     * Формирует название периода с учетом того, является ли период корректировочным
     * @param departmentReportPeriod    период для которого формируется название
     * @param formatExp	                шаблон форматирования даты
     * @return строка с сообщением о корректировочномм приоде, если период не корректировочный возвращается пустая
     * строка
     */
    public String formatPeriodName(DepartmentReportPeriod departmentReportPeriod, String formatExp) {
        String corrPeriodMessage = "";
        SimpleDateFormat formatter = new SimpleDateFormat(formatExp);
        if (departmentReportPeriod.getCorrectionDate() != null) {
            corrPeriodMessage = ", с датой сдачи корректировки " + formatter.format(departmentReportPeriod.getCorrectionDate());
        }
        return departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear() + ", " + departmentReportPeriod.getReportPeriod().getName() + corrPeriodMessage;
    }

    /**
     * Формирование строки полного описания периода.
     * Пример: "2019: первый квартал (корр. 21.04.2019): 6-НДФЛ"
     *
     * @param departmentReportPeriod период подразделения, по которому необходимо получить полное описание
     * @return строка описания периода
     */
    public String getPeriodDescription(DepartmentReportPeriod departmentReportPeriod) {
        return getPeriodString(departmentReportPeriod, true);
    }

    /**
     * Формирование строки краткого описания периода.
     * Используется в том случае, когда строку корректировки необходимо описать отдельно.
     * Пример: "2019: первый квартал: 6-НДФЛ"
     *
     * @param departmentReportPeriod период подразделения, по которому необходимо получить полное описание
     * @return строка описания периода
     */
    public String getPeriodDescriptionWithoutCorrectionDate(DepartmentReportPeriod departmentReportPeriod) {
        return getPeriodString(departmentReportPeriod, false);
    }

    private String getPeriodString(DepartmentReportPeriod departmentReportPeriod, boolean withCorrectionDate) {
        ReportPeriod reportPeriod = departmentReportPeriod.getReportPeriod();

        TaxFormType refBookFormType = TaxFormType.getById(reportPeriod.getReportPeriodTaxFormTypeId());

        String correctionDate = withCorrectionDate && departmentReportPeriod.getCorrectionDate() != null
                ? " (корр. " + DD_MM_YYYY.format(departmentReportPeriod.getCorrectionDate()) + ")"
                : "";

        Object[] period = {reportPeriod.getTaxPeriod().getYear(), reportPeriod.getName() + correctionDate, refBookFormType.getCode()};
        return StringUtils.join(period, ": ");
    }
}
