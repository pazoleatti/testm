package com.aplana.sbrf.taxaccounting.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: avanteev
 */
public final class MessageGenerator {
    public static String getFDMsg(String mainStr, String formTypeName, String kindName, String departmentName, Integer periodOrder, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison){
        if (rpComparison == null) {
            return String.format(COMPLETE_FORM,
                    mainStr,
                    formTypeName,
                    kindName,
                    departmentName,
                    reportPeriod.getReportPeriod().getName(),
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    reportPeriod.getCorrectionDate() != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())) : "",
                    manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format(COMPLETE_FORM_WITH_RP_COMPARISON,
                    mainStr,
                    formTypeName,
                    kindName,
                    departmentName,
                    reportPeriod.getReportPeriod().getName(),
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    rpComparison.getReportPeriod().getName(),
                    rpComparison.getReportPeriod().getTaxPeriod().getYear(),
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    reportPeriod.getCorrectionDate() != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())) : "",
                    manual ? "ручного ввода" : "автоматическая");
        }
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, String departmentName, Integer periodOrder, boolean manual, String reportPeriodName, Date correctionDate, String rpComparisonName){
        return String.format(COMPLETE_FORM_STRINGS_ONLY,
                mainStr,
                formTypeName,
                kindName,
                departmentName,
                reportPeriodName,
                rpComparisonName != null ? rpComparisonName : "",
                periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                correctionDate != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(correctionDate)) : "",
                manual ? "ручного ввода" : "автоматическая");
    }

    public static String mesSpeck(TaxType taxType){
        return  taxType == TaxType.DEAL || taxType == TaxType.ETR ? "формы" : "налоговые формы";
    }

    private static final String COMPLETE_FORM =
            "%s: Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\",%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_STRINGS_ONLY   =
            "%s: Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s\",%s%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_WITH_RP_COMPARISON =
            "%s: Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\", Период сравнения: %s %d,%s%s Версия: \"%s\"";
    private static final String MONTH = " Месяц: %s,";
    private static final String CORRECTION_DATE  = " Дата сдачи корректировки: %s,";
    private static final String COMPARISON_PERIOD = "Период сравнения: %s,";

    protected static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");
}
