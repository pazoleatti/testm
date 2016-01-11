package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class RefBookCSVReportBuilder extends AbstractReportBuilder {

    public static final String FILE_NAME = "Отчет_";

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

    private List<Map<String, RefBookValue>> records;
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<Long, List<Map<String, RefBookValue>>>();
    private Map<Long, Map<Long, String>> dereferenceValues;
    private RefBook refBook;
    private List<RefBookAttribute> refBookAttributeList;
    private RefBookAttribute sortAttribute;

    public RefBookCSVReportBuilder(RefBook refBook, List<Map<String, RefBookValue>> records, Map<Long, Map<Long, String>> dereferenceValues, RefBookAttribute sortAttribute) {
        super("acctax", ".csv");
        this.records = records;
        this.refBook = refBook;
        this.dereferenceValues = dereferenceValues;
        this.sortAttribute = sortAttribute;
        refBookAttributeList = new LinkedList<RefBookAttribute>();
        if (refBook.isHierarchic()) {
            refBookAttributeList.add(levelAttribute);
            Long parent_id;
            for(Map<String, RefBookValue> record: records) {
                parent_id = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                if (!hierarchicRecords.containsKey(parent_id)) {
                    hierarchicRecords.put(parent_id, new ArrayList<Map<String, RefBookValue>>());
                }
                hierarchicRecords.get(parent_id).add(record);
            }
        }
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.isVisible()) {
                refBookAttributeList.add(attribute);
            }
        }
    }

    @Override
    protected void createTableHeaders() {}

    @Override
    protected void fillHeader() {}

    @Override
    protected void createDataForTable() {}

    @Override
    protected void fillFooter() {}

    @Override
    protected String flush() throws IOException {
        File file = File.createTempFile(FILE_NAME, ".csv");
        csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(file)), ';');
        try {
            List<String> headersNames = new ArrayList<String>();
            for (RefBookAttribute attribute : refBookAttributeList) {
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
        }

        return file.getAbsolutePath();
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

    private void createRow(Map<String, RefBookValue> record) {
        List<String> oneRow = new ArrayList<String>();
        for (RefBookAttribute attribute : refBookAttributeList) {
            RefBookValue value = record.get(attribute.getAlias());
            String tableCell;
            if (value == null) {
                tableCell = "";
            } else {
                switch (value.getAttributeType()) {
                    case NUMBER:
                        if (value.getNumberValue() == null) tableCell = "";
                        else tableCell = value.getNumberValue().toString();
                        break;
                    case DATE:
                        if (value.getDateValue() == null) tableCell = "";
                        else {
                            if (attribute.getFormat() != null) {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                        attribute.getFormat().getFormat());
                                tableCell = simpleDateFormat.format(value.getDateValue());
                            } else {
                                tableCell = value.getDateValue().toString();
                            }
                        }
                        break;
                    case STRING:
                        if (value.getStringValue() == null) tableCell = "";
                        else tableCell = value.getStringValue();
                        break;
                    case REFERENCE:
                        if (value.getReferenceValue() == null) tableCell = "";
                        else {
                            tableCell = dereferenceValues.get(attribute.getId()).get(value.getReferenceValue());
                        }
                        break;
                    default:
                        tableCell = "undefined";
                        break;
                }
            }
            oneRow.add(tableCell);
        }
        csvWriter.writeNext(oneRow.toArray(new String[oneRow.size()]));
    }
}
