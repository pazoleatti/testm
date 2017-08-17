package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
    private final String formCode;
    private final String departmentCode;
    private final String reportPeriodCode;
    private final Integer year;
    private final Integer month;
    private final String kpp;
    private final String asnuCode;
    private final String guid;
    private Integer declarationTypeId; //ToDo потом нужно будет переделать на код формы???
    private final boolean isFNS;
    private final Long declarationDataId;

    /**
     * Маппинг периодов для НДФЛ
     */
    private static final Map<String, String> periodNdflMap = new HashMap<String, String>();

    static {
        periodNdflMap.put("21", "21");
        periodNdflMap.put("32", "31");
        periodNdflMap.put("33", "33");
        periodNdflMap.put("34", "34");
    }

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
    public TransportDataParam(String formCode, String departmentCode, String reportPeriodCode, Integer year, Integer month, String kpp, String asnuCode, String guid, Integer declarationTypeId, boolean isFNS, Long declarationDataId) {
        this.formCode = formCode;
        this.departmentCode = departmentCode;
        this.reportPeriodCode = reportPeriodCode;
        this.year = year;
        this.month = month;
        this.kpp = kpp;
        this.asnuCode = asnuCode;
        this.guid = guid;
        this.declarationTypeId = declarationTypeId;
        this.isFNS = isFNS;
        this.declarationDataId = declarationDataId;
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
        return new TransportDataParam(formCode, departmentCode, reportPeriodCode, year, month, null, null, null, null, false, null);
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


    public static class SAXHandler extends DefaultHandler {
        private Map<String, Map<String, String>> values;
        private Map<String, List<String>> tagAttrNames;

        public SAXHandler(Map<String, List<String>> tagAttrNames) {
            this.tagAttrNames = tagAttrNames;
        }

        public Map<String, Map<String, String>> getValues() {
            return values;
        }

        @Override
        public void startDocument() throws SAXException {
            values = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
                values.put(entry.getKey(), new HashMap<String, String>());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
                if (entry.getKey().equals(qName)) {
                    for (String attrName: entry.getValue()) {
                        values.get(qName).put(attrName, attributes.getValue(attrName));
                    }
                }
            }
        }
    }

    public static final String TAG_DOCUMENT = "Документ";
    public static final String ATTR_PERIOD = "Период";
    public static final String ATTR_YEAR = "ОтчетГод";

    /**
     * Параметры ТФ, получаемые из имени ТФ
     *
     * @param name Имя ТФ
     */
    public static TransportDataParam valueOfDec(String name, InputStream inputStream) {
        Integer declarationTypeId;
        String departmentCode ;
        String reportPeriodCode;
        String asnuCode = null;
        String guid = null;
        String kpp = null;
        Integer year = null;
        boolean isFNS = false;
        if (name != null && name.toLowerCase().endsWith(NAME_EXTENSION_DEC)
                & name.length() == NAME_LENGTH_QUARTER_DEC) {
            declarationTypeId = 100;
            departmentCode = name.substring(0, 17).replaceFirst("_*", "").trim();
            reportPeriodCode = name.substring(21, 23).replaceAll("_", "").trim();
            if (reportPeriodCode != null && !reportPeriodCode.isEmpty() && periodNdflMap.containsKey(reportPeriodCode)) {
                reportPeriodCode = periodNdflMap.get(reportPeriodCode);
            }
            asnuCode = name.substring(17, 21).replaceFirst("_", "").trim();
            guid = name.substring(27, 59).replaceAll("_", "").trim();
            try {
                year = Integer.parseInt(name.substring(23, 27));
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        } else {
            throw new IllegalArgumentException(String.format(NAME_FORMAT_ERROR_DEC, name));
        }
        return new TransportDataParam(null, departmentCode, reportPeriodCode, year, null, kpp, asnuCode, guid, declarationTypeId, isFNS, null);
    }

    /**
     * Проверка соответствия имени ТФ формату АСНУ
     *
     * @param name Имя ТФ
     */
    public static boolean isValidDecName(String name, InputStream inputStream) {
        try {
            TransportDataParam.valueOfDec(name, inputStream);
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

    public boolean isFNS() {
        return isFNS;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
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
