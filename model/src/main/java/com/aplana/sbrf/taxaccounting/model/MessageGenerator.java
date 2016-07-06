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
        CORRECTION("Изменения по корректировке"),
        CORRECTION_WITH_CONTROL("Изменения по корректировке (с контрольными полями)");

        VERSION(String message) {
            this.message = message;
        }

        /**
         * http://conf.aplana.com/pages/viewpage.action?pageId=20385407#id-ПараметрыэкземпляраНФ-ПараметрыэкземпляраНФ
         * @param isCorrection признак корректиррующего периода, true - корректирующий
         * @param isManual признак ручного ввода, true - ручного ввода, false - автоматическая
         * @param isShowChecked признак контрольных полей, используется только для формирования печатных представлений.
         *                      true - показывать контрольные поля
         * @param isAbsolute выставляется только при пчати НФ, для остальных по умолчанию true
         * @return версия
         */
        public static VERSION getVersion(boolean isCorrection, boolean isManual, boolean isShowChecked, boolean isAbsolute){
            if (isCorrection){
                if (isAbsolute){
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

    /**
     *
     * @param isShowChecked только для печати НФ
     * @param isAbsolute только для печати НФ, в остальных случаях по умолчанию true
     * @return
     */
    public static String getFDMsg(String mainStr, FormData formData, String departmentName, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison, boolean isShowChecked, boolean isAbsolute){
        return getFDMsg(mainStr, formData.getFormType().getName(), formData.getKind().getTitle(), formData.isAccruing(), departmentName, formData.getPeriodOrder(), manual, reportPeriod, rpComparison, isShowChecked, isAbsolute);
    }

    public static String getFDMsg(String mainStr, FormData formData, String departmentName, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison){
        return getFDMsg(mainStr, formData, departmentName, manual, reportPeriod, rpComparison, false, true);
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, String departmentName, Integer periodOrder, boolean manual, String reportPeriodName, String fullRpComparisonName, int year, Integer commpYear, Date correctionDate){
        return getFDMsg(mainStr, formTypeName, kindName, departmentName, periodOrder, manual, reportPeriodName, fullRpComparisonName, year, commpYear, correctionDate, false, true);
    }

    private static String getFDMsg(String mainStr, String formTypeName, String kindName, String departmentName, Integer periodOrder, boolean manual, String reportPeriodName, String fullRpComparisonName, int year, Integer commpYear, Date correctionDate, boolean isShowChecked, boolean isAbsolute){
        if (fullRpComparisonName == null) {
            return String.format(COMPLETE_FORM,
                    mainStr,
                    kindName,
                    formTypeName,
                    departmentName,
                    reportPeriodName,
                    year,
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    correctionDate != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.get().format(correctionDate)) : "",
                    VERSION.getVersion(correctionDate!= null, manual, isShowChecked, isAbsolute)).trim();
        } else {
            return String.format(COMPLETE_FORM_WITH_RP_COMPARISON,
                    mainStr,
                    kindName,
                    formTypeName,
                    departmentName,
                    reportPeriodName,
                    year,
                    fullRpComparisonName,
                    commpYear,
                    periodOrder != null ? String.format(MONTH,  Formats.getRussianMonthNameWithTier(periodOrder)): "",
                    correctionDate != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.get().format(correctionDate)) : "",
                    VERSION.getVersion(correctionDate!= null, manual, isShowChecked, isAbsolute)).trim();
        }
    }

    private static String getFDMsg(String mainStr, String formTypeName, String kindName, boolean isAccruing, String departmentName, Integer periodOrder, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison, boolean isShowChecked, boolean isAbsolute){
        return getFDMsg(mainStr, formTypeName, kindName, departmentName, periodOrder, manual,
                isAccruing ? reportPeriod.getReportPeriod().getAccName() : reportPeriod.getReportPeriod().getName(),
                rpComparison != null ? isAccruing ? rpComparison.getReportPeriod().getAccName() : rpComparison.getReportPeriod().getName() : null,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                rpComparison != null ? rpComparison.getReportPeriod().getTaxPeriod().getYear() : null,
                reportPeriod.getCorrectionDate(),
                isShowChecked, isAbsolute
        );
    }

    public static String getFDMsg(String mainStr, String formTypeName, String kindName, boolean isAccruing, String departmentName, Integer periodOrder, boolean manual, DepartmentReportPeriod reportPeriod, DepartmentReportPeriod rpComparison){
        return getFDMsg(mainStr, formTypeName, kindName, isAccruing, departmentName, periodOrder, manual, reportPeriod, rpComparison, false, true);
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
                correctionDate != null ? String.format(CORRECTION_DATE, SDF_DD_MM_YYYY.get().format(correctionDate)) : "",
                VERSION.getVersion(correctionDate != null, manual, false, false)).trim();
    }

    public static String getDDMsg(String mainStr, String decTypeName, String departmentName, DepartmentReportPeriod drp, String tax, String kpp){
        return String.format(COMPLETE_DECLARATION,
                mainStr,
                decTypeName,
                departmentName,
                drp.getReportPeriod().getName(),
                drp.getReportPeriod().getTaxPeriod().getYear(),
                drp.getCorrectionDate() != null ? String.format(DD_CORRECTION_DATE, SDF_DD_MM_YYYY.get().format(drp.getCorrectionDate())) : "",
                tax != null ? String.format(TAX_AUTHORITY, tax) : "",
                kpp != null ? String.format(KPP, kpp) : "").trim();
    }

    public static String mesSpeckPlural(TaxType taxType){
        return taxType.isTax() ? "налоговые формы" : "формы";
    }

    public static String mesSpeckPluralD(TaxType taxType){
        return taxType.isTax() ? "налоговых форм" : "форм";
    }

    public static String mesSpeckSingle(TaxType taxType){
        return taxType.isTax() ? "налоговая форма" : "форма";
    }

    public static String mesSpeckSingleD(TaxType taxType){
        return taxType.isTax() ? "налоговой формы" : "формы";
    }

    private static final String COMPLETE_FORM =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\",%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_STRINGS_ONLY   =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s\",%s%s%s Версия: \"%s\"";
    private static final String COMPLETE_FORM_WITH_RP_COMPARISON =
            "%s Тип: \"%s\", Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\", Период сравнения: \"%s %d\",%s%s Версия: \"%s\"";
    private static final String MONTH = " Месяц: \"%s\",";
    private static final String CORRECTION_DATE  = " Дата сдачи корректировки: %s,";
    private static final String DD_CORRECTION_DATE  = ", Дата сдачи корректировки: %s";
    private static final String COMPARISON_PERIOD = " Период сравнения: \"%s\", ";
    private static final String COMPLETE_DECLARATION =
            "%s Вид: \"%s\", Подразделение: \"%s\", Период: \"%s %d\"%s%s%s";
    private static final String TAX_AUTHORITY = ", Налоговый орган: \"%s\"";
    private static final String KPP = ", КПП: \"%s\"";

    private static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public String toString() {
        return super.toString();
    }
}
