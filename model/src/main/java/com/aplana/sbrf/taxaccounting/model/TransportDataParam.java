package com.aplana.sbrf.taxaccounting.model;

import java.util.regex.Pattern;

/**
 * Параметры ТФ, получаемые из имени ТФ
 * <Код налоговой формы><Код подразделения><Код периода><Календарный год><Месяц>
 *
 * @author Dmitriy Levykin
 */
public class TransportDataParam {
    public static final int NAME_LENGTH_QUARTER_DEC = 63;
    public static final int NAME_LENGTH_QUARTER = 36;
    public static final int NAME_LENGTH_MONTH = 38;
    public static final String NAME_EXTENSION_DEC = ".xml";
    public static final String NAME_EXTENSION = ".rnu";
    public static final String NAME_FORMAT_ERROR_DEC = "Имя транспортного файла «%s» не соответствует формату «<Код подразделения><Код АСНУ (тип доходов)><Код периода><Календарный год><GUID>.xml»!";
    public static final String NAME_FORMAT_ERROR = "Имя транспортного файла «%s» не соответствует формату «<Код налоговой формы><Код подразделения><Код периода><Календарный год><Месяц>.rnu»!";
    public static final String NO_RASCHSV_PATTERN = "NO_RASCHSV_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
    private final String formCode;
    private final String departmentCode;
    private final String reportPeriodCode;
    private final Integer year;
    private final Integer month;
    private final String kpp;
    private final String asnuCode;
    private final String guid;
    Integer declarationTypeId; //ToDщ потом нужно будет переделать на код формы???


    /**
     * Параметры ТФ, получаемые из имени ТФ
     *
     * @param formCode         Код налоговой формы
     * @param departmentCode   Код подразделения в нотации СБФР
     * @param reportPeriodCode Код периода
     * @param year             Календарный год
     * @param month            Месяц, может быть null
     * @param asnuCode         Код АСНУ
     * @param guid             GUID ТФ
     * @param declarationTypeId тип декларации
     */
    public TransportDataParam(String formCode, String departmentCode, String reportPeriodCode, Integer year, Integer month, String kpp, String asnuCode, String guid, Integer declarationTypeId) {
        this.formCode = formCode;
        this.departmentCode = departmentCode;
        this.reportPeriodCode = reportPeriodCode;
        this.year = year;
        this.month = month;
        this.kpp = kpp;
        this.asnuCode = asnuCode;
        this.guid = guid;
        this.declarationTypeId = declarationTypeId;
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
        return new TransportDataParam(formCode, departmentCode, reportPeriodCode, year, month, null, null, null, null);
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
     * Параметры ТФ, получаемые из имени ТФ
     *
     * @param name Имя ТФ
     */
    public static TransportDataParam valueOfDec(String name) {
        Integer declarationTypeId;
        String departmentCode ;
        String reportPeriodCode;
        String asnuCode = null;
        String guid = null;
        String kpp = null;
        Integer year = null;
        Pattern pattern = Pattern.compile(NO_RASCHSV_PATTERN);
        if (name != null && name.toLowerCase().endsWith(NAME_EXTENSION_DEC)
                & name.length() == NAME_LENGTH_QUARTER_DEC) {
            declarationTypeId = 100;
            departmentCode = name.substring(0, 17).replaceFirst("_*", "").trim();
            reportPeriodCode = name.substring(21, 23).replaceAll("_", "").trim();
            asnuCode = name.substring(17, 21).replaceFirst("_", "").trim();
            guid = name.substring(27, 59).replaceAll("_", "").trim();
            try {
                year = Integer.parseInt(name.substring(23, 27));
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        } else if (pattern.matcher(name).matches()) {
            declarationTypeId = 200;
            departmentCode = "18_0000_00";
            reportPeriodCode = "21";
            year = 2016;
            kpp = name.replaceAll(NO_RASCHSV_PATTERN, "$4");
        } else {
            throw new IllegalArgumentException(String.format(NAME_FORMAT_ERROR_DEC, name));
        }
        return new TransportDataParam(null, departmentCode, reportPeriodCode, year, null, kpp, asnuCode, guid, declarationTypeId);
    }

    /**
     * Проверка соответствия имени ТФ формату АСНУ
     *
     * @param name Имя ТФ
     */
    public static boolean isValidDecName(String name) {
        try {
            TransportDataParam.valueOfDec(name);
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

    public String getGuid() {
        return guid;
    }

    public String getAsnuCode() {
        return asnuCode;
    }

    public Integer getDeclarationTypeId() {
        return declarationTypeId;
    }

    public String getKpp() {
        return kpp;
    }

    @Override
    public String toString() {
        return "TransportDataParam{" +
                "formCode='" + formCode + '\'' +
                ", departmentCode='" + departmentCode + '\'' +
                ", reportPeriodCode='" + reportPeriodCode + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", asnuCode=" + asnuCode +
                ", guid=" + guid +
                ", declarationTypeId=" + declarationTypeId +
                ", kpp=" + kpp +
                '}';
    }
}
