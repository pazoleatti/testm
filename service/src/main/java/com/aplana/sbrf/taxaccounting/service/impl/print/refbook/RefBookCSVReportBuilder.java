package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class RefBookCSVReportBuilder extends AbstractReportBuilder {

    public static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final RefBookAttribute levelAttribute = new RefBookAttribute() {
        {
            setAlias("level");
            setName("Уровень");
            setAttributeType(RefBookAttributeType.NUMBER);
            setPrecision(0);
            setWidth(3);
        }
    };

    private final Comparator<Map<String, RefBookValue>> comparator = new Comparator<Map<String, RefBookValue>>() {
        @Override
        public int compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
            if (sortAttribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                String s1 = o1.get(sortAttribute.getAlias()).getStringValue();
                String s2 = o2.get(sortAttribute.getAlias()).getStringValue();
                return s1.compareToIgnoreCase(s2);
            } else if (sortAttribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                BigDecimal d1 = (BigDecimal) o1.get(sortAttribute.getAlias()).getNumberValue();
                BigDecimal d2 = (BigDecimal) o2.get(sortAttribute.getAlias()).getNumberValue();
                return d1.compareTo(d2);
            }
            return 0;
        }
    };


    private CSVWriter csvWriter;

    private List<RefBookAttribute> attributes;
    private List<Map<String, RefBookValue>> records;
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<Long, List<Map<String, RefBookValue>>>();
    private Map<Long, Map<Long, String>> dereferenceValues;
    private RefBook refBook;
    private Date version;
    private RefBookAttribute sortAttribute;
    private String searchPattern;
    private Boolean exactSearch;

    public RefBookCSVReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Date version, String searchPattern, boolean exactSearch, final RefBookAttribute sortAttribute) {
        super("acctax", ".csv");
        this.refBook = refBook;
        this.attributes = attributes;
        this.records = records;
        this.version = version;
        this.searchPattern = searchPattern;
        this.exactSearch = exactSearch;
        this.sortAttribute = sortAttribute;
        if (refBook.isHierarchic()) {
            attributes.add(levelAttribute);
            Long parent_id;
            for (Map<String, RefBookValue> record : records) {
                parent_id = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                if (!hierarchicRecords.containsKey(parent_id)) {
                    hierarchicRecords.put(parent_id, new ArrayList<Map<String, RefBookValue>>());
                }
                hierarchicRecords.get(parent_id).add(record);
            }
        }
    }

    @Override
    protected void createTableHeaders() {
    }

    @Override
    protected void fillHeader() {
    }

    @Override
    protected void createDataForTable() {
    }

    @Override
    protected void fillFooter() {
    }

    @Override
    protected File createTempFile() throws IOException {
        String fileName = refBook.getName().replace(' ', '_');
        if (refBook.isVersioned()) {
            fileName = fileName + "_" + sdf.get().format(version);
        }
        fileName = fileName + ".csv";
        //Используется такой подход, т.к. File.createTempFile в именах генерирует случайные числа. Здесь запись
        //выполняется в 1 и тот же файл. Но записываются всегда актуальные данные
        return new File(TMP_DIR, fileName);
    }

    @Override
    protected void flush(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        csvWriter = new CSVWriter(bufferedWriter, ';');
        try {
            List<String> headersNames = new ArrayList<String>();
            fillHeaderInfo();
            for (RefBookAttribute attribute : attributes) {
                headersNames.add(attribute.getName());
            }
            csvWriter.writeNext(headersNames.toArray(new String[headersNames.size()]));
            if (!refBook.isHierarchic()) {
                for (Map<String, RefBookValue> record : records) {
                    createRow(record);
                }
            } else {
                printNode(null, 0);
            }
        } finally {
            csvWriter.close();
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(fileWriter);
        }
    }

    private void fillHeaderInfo() {
        List<String> headerInfo = new ArrayList<>();

        headerInfo.add("Справочник: " + refBook.getName());
        if (refBook.isVersioned()) {
            headerInfo.add("Дата актуальности: " + new SimpleDateFormat("dd.MM.yyyy").format(version));
        }
        headerInfo.add("Параметр поиска: " + (searchPattern != null && !searchPattern.isEmpty() ? searchPattern + (exactSearch ? "(по точному совпадению)" : "") : "не задан"));

        for (int i = 0; i < 4; i++) {
            if (i < headerInfo.size()) {
                csvWriter.writeNext(new String[]{headerInfo.get(i)});
            } else {
                csvWriter.writeNext(new String[]{""});
            }
        }

    }

    private void printNode(Long parent_id, int level) {
        level++;
        if (hierarchicRecords.containsKey(parent_id)) {
            Collections.sort(hierarchicRecords.get(parent_id), comparator);
            for (Map<String, RefBookValue> record : hierarchicRecords.get(parent_id)) {
                record.put(levelAttribute.getAlias(), new RefBookValue(RefBookAttributeType.NUMBER, new BigDecimal(level)));
                createRow(record);
                printNode(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), level);
            }
        }
    }

    /**
     * Проставляет значение атрибута в ячейку в зависимости от его типа
     *
     * @param value     объект, из которого надо извлечь значение
     * @param attribute атрибут, значение которого надо извлечь
     */
    private String getCellValue(RefBookValue value, RefBookAttribute attribute) {
        String result = "";
        switch (value.getAttributeType()) {
            case NUMBER:
                if (value.getNumberValue() != null) {
                    result = value.getNumberValue().toString();
                }
                break;
            case DATE:
                if (value.getDateValue() != null) {
                    if (attribute.getFormat() != null) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                attribute.getFormat().getFormat());
                        result = simpleDateFormat.format(value.getDateValue());
                    } else {
                        result = sdf.get().format(value.getDateValue());
                    }
                }
                break;
            case STRING:
                if (value.getStringValue() != null) {
                    result = "\t" + value.getStringValue();
                }
                break;
            case REFERENCE:
                if (value.getReferenceObject() != null) {
                    getCellValue(value.getReferenceObject().get(attribute.getAlias()), attribute);
                }
                break;
            default:
                result = "undefined";
        }
        return result;
    }

    private void createRow(Map<String, RefBookValue> record) {
        List<String> oneRow = new ArrayList<String>();
        for (RefBookAttribute attribute : attributes) {
            RefBookValue value = record.get(attribute.getAlias());
            String tableCell;
            if (value == null) {
                tableCell = "";
            } else {
                RefBookAttribute attr;
                if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                    attr = attribute.getRefBookAttribute();
                } else {
                    attr = attribute;
                }
                tableCell = getCellValue(value, attr);
            }
            oneRow.add(tableCell);
        }
        csvWriter.writeNext(oneRow.toArray(new String[oneRow.size()]));
    }
}
