package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Создает название периода
 */
@Component
public class DepartmentReportPeriodFormatter {

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
}
