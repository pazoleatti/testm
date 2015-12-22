package com.aplana.sbrf.taxaccounting.model;

/**
 * Параметры ТФ, получаемые из имени ТФ
 * <Код налоговой формы><Код подразделения><Код периода><Календарный год><Месяц>
 *
 * @author Dmitriy Levykin
 */
public class TransportDataParam {
    public static final int NAME_LENGTH_QUARTER = 36;
    public static final int NAME_LENGTH_MONTH = 38;
    public static final String NAME_EXTENSION = ".rnu";
    public static final String NAME_FORMAT_ERROR = "Имя транспортного файла «%s» не соответствует формату «<Код налоговой формы><Код подразделения><Код периода><Календарный год><Месяц>.rnu»!";

    private final String formCode;
    private final String departmentCode;
    private final String reportPeriodCode;
    private final Integer year;
    private final Integer month;

    /**
     * Параметры ТФ, получаемые из имени ТФ
     *
     * @param formCode         Код налоговой формы
     * @param departmentCode   Код подразделения в нотации СБФР
     * @param reportPeriodCode Код периода
     * @param year             Календарный год
     * @param month            Месяц, может быть null
     */
    public TransportDataParam(String formCode, String departmentCode, String reportPeriodCode, Integer year, Integer month) {
        this.formCode = formCode;
        this.departmentCode = departmentCode;
        this.reportPeriodCode = reportPeriodCode;
        this.year = year;
        this.month = month;
    }

    /**
     * Параметры ТФ, получаемые из имени ТФ
     *
     * @param name Имя ТФ
     */
    public static TransportDataParam valueOf(String name) {
        if (name == null || !name.toLowerCase().endsWith(NAME_EXTENSION)
                || name.length() != NAME_LENGTH_QUARTER && name.length() != NAME_LENGTH_MONTH) {
            throw new IllegalArgumentException(String.format(NAME_FORMAT_ERROR, name));
        }
        // убрать нижние черточки слева от кода
        String formCode = name.substring(0, 9).replaceFirst("(_*)(^_*)", "").trim();
        String reportPeriodCode = name.substring(26, 28).replaceAll("_", "").trim();
        String departmentCode = name.substring(9, 26).replaceFirst("_*", "").trim();
        Integer year = null;
        Integer month = null;

        try {
            year = Integer.parseInt(name.substring(28, 32));
        } catch (NumberFormatException nfe) {
            // Ignore
        }
        if (name.length() == NAME_LENGTH_MONTH) {
            try {
                month = Integer.parseInt(name.substring(32, 34).replaceAll("_", "").trim());
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        }
        return new TransportDataParam(formCode, departmentCode, reportPeriodCode, year, month);
    }

    /**
     * Проверка соответствия имени ТФ формату АСНУ
     *
     * @param name Имя ТФ
     */
    public static boolean isValidName(String name) {
        try {
            TransportDataParam.valueOf(name);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Код налоговой формы
     */
    public String getFormCode() {
        return formCode;
    }

    /**
     * Код подразделения
     */
    public String getDepartmentCode() {
        return departmentCode;
    }

    /**
     * Код периода
     */
    public String getReportPeriodCode() {
        return reportPeriodCode;
    }

    /**
     * Календарный год
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Месяц, может быть null
     */
    public Integer getMonth() {
        return month;
    }

    @Override
    public String toString() {
        return "TransportDataParam{" +
                "formCode='" + formCode + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", reportPeriodCode='" + reportPeriodCode + '\'' +
                ", year=" + year +
                ", month=" + month +
                '}';
    }
}
