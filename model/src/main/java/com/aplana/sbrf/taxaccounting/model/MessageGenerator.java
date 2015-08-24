package com.aplana.sbrf.taxaccounting.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: avanteev
 */
public final class MessageGenerator {

    private enum VERSION{
        AUTO("Автоматическая"),
        AUTO_WITH_CONTROL("Автоматическая (с контрольными полями)"),
        MANUAL("Ручного ввода"),
        MANUAL_WITH_CONTROL("Ручного ввода (с контрольными полями)"),
        ABSOLUTE("Абсолютные значения"),
        ABSOLUTE_WITH_CONTROL("Абсолютные значения (с контрольными полями)"),
        CORRECTION("Корректировка"),
        CORRECTION_WITH_CONTROL("Корректировка (с контрольными полями)");

        VERSION(String message) {
            this.message = message;
        }

        /**
         *
         * @param isCorrection признак корректиррующего периода, true - корректирующий
         * @param isManual признак ручного ввода, true - ручного ввода, false - автоматическая
         * @param isShowChecked признак контрольных полей, используется только для формирования печатных представлений.
         *                      true - показывать контрольные поля
         * @return версия
         */
        public static VERSION getVersion(boolean isCorrection, boolean isManual, boolean isShowChecked){
            if (isCorrection){
                if (isManual){
                    return isShowChecked ? ABSOLUTE_WITH_CONTROL : ABSOLUTE;
                } else {
                    return isShowChecked ? CORRECTION_WITH_CONTROL : CORRECTION;
                }
            } else {
                if (isManual){
                    return isShowChecked ? MANUAL_WITH_CONTROL : MANUAL;
                } else {
                    return isShowChecked ? AUTO_WITH_CONTROL : AUTO;
                }
            }
        }

        private String message;

        @Override
        public String toString() {
            return message;
        }
    }

    public static String getFDMsg(String mainStr, FormData formData, String departmentName, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison, boolean isShowChecked){
        return getFDMsg(mainStr, formData.getFormType().getName(), formData.getKind().getName(), formData.isAccruing(), departmentName, formData.getPeriodOrder(), manual, reportPeriod, rpComparison, isShowChecked);
    }

    public static String getFDMsg(String mainStr, FormData formData, String departmentName, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison){
        return getFDMsg(mainStr, formData, departmentName, manual, reportPeriod, rpComparison, false);
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, boolean isAccruing, String departmentName, Integer periodOrder, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison, boolean isShowChecked){
        if (rpComparison == null) {
            return String.format(COMPLETE_FORM,
                    mainStr,
                    kindName,
                    formTypeName,
                    departmentName,
                    isAccruing ? reportPeriod.getReportPeriod().getAccName() : reportPeriod.getReportPeriod().getName(),
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    reportPeriod.getCorrectionDate() != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())) : "",
                    VERSION.getVersion(reportPeriod.getCorrectionDate()!= null, manual, isShowChecked)).trim();
        } else {
            return String.format(COMPLETE_FORM_WITH_RP_COMPARISON,
                    mainStr,
                    kindName,
                    formTypeName,
                    departmentName,
                    isAccruing ? reportPeriod.getReportPeriod().getAccName() : reportPeriod.getReportPeriod().getName(),
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                    isAccruing ? rpComparison.getReportPeriod().getAccName() : rpComparison.getReportPeriod().getName(),
                    rpComparison.getReportPeriod().getTaxPeriod().getYear(),
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    reportPeriod.getCorrectionDate() != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate())) : "",
                    VERSION.getVersion(reportPeriod.getCorrectionDate()!= null, manual, isShowChecked)).trim();
        }
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, boolean isAccruing, String departmentName, Integer periodOrder, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison){
        return getFDMsg(mainStr, formTypeName, kindName, isAccruing, departmentName, periodOrder, manual, reportPeriod, rpComparison, false);
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, String departmentName, Integer periodOrder, boolean manual, String reportPeriodName, Date correctionDate, String rpComparisonName){
        return String.format(COMPLETE_FORM_STRINGS_ONLY,
                mainStr,
                kindName,
                formTypeName,
                departmentName,
                reportPeriodName,
                rpComparisonName != null && !rpComparisonName.isEmpty() ? String.format(COMPARISON_PERIOD, rpComparisonName) : "",
                periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                correctionDate != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.format(correctionDate)) : "",
                VERSION.getVersion(correctionDate != null, manual, false)).trim();
    }

    public static String getDDMsg(String mainStr, String decTypeName, String departmentName, DepartmentReportPeriod drp, String tax, String kpp){
        return String.format(COMPLETE_DECLATATION,
                mainStr,
                decTypeName,
                departmentName,
                drp.getReportPeriod().getName(),
                drp.getReportPeriod().getTaxPeriod().getYear(),
                drp.getCorrectionDate() != null ? String.format(DD_CORRECTION_DATE, SDF_DD_MM_YYYY.format(drp.getCorrectionDate())) : "",
                tax != null ? String.format(TAX_AUTHORITY, tax) : "",
                kpp != null ? String.format(KPP, kpp) : "").trim();
    }

    public static String mesSpeckPlural(TaxType taxType){
        return  taxType == TaxType.DEAL || taxType == TaxType.ETR ? "формы" : "налоговые формы";
    }

    public static String mesSpeckPluralD(TaxType taxType){
        return  taxType == TaxType.DEAL || taxType == TaxType.ETR ? "форм" : "налоговых форм";
    }

    public static String mesSpeckSingle(TaxType taxType){
        return  taxType == TaxType.DEAL || taxType == TaxType.ETR ? "форма" : "налоговая форма";
    }

    public static String mesSpeckSingleD(TaxType taxType){
        return  taxType == TaxType.DEAL || taxType == TaxType.ETR ? "формы" : "налоговой формы";
    }

    private static final String COMPLETE_FORM =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\",%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_STRINGS_ONLY   =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s\",%s%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_WITH_RP_COMPARISON =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\", Период сравнения: %s %d,%s%s Версия: \"%s\"";
    private static final String MONTH = " Месяц: %s,";
    private static final String CORRECTION_DATE  = " Дата сдачи корректировки: %s, ";
    private static final String DD_CORRECTION_DATE  = " ,Дата сдачи корректировки: %s";
    private static final String COMPARISON_PERIOD = " Период сравнения: \"%s\", ";
    private static final String COMPLETE_DECLATATION =
            "%s Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\"%s%s%s";
    private static final String TAX_AUTHORITY = " ,Налоговый орган: \"%s\"";
    private static final String KPP = " ,КПП: \"%s\"";

    protected static final SimpleDateFormat SDF_DD_MM_YYYY = new SimpleDateFormat("dd.MM.yyyy");
}
