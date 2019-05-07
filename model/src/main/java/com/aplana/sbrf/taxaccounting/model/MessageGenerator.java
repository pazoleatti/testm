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
