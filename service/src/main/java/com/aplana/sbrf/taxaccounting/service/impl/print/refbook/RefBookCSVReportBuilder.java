package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import au.com.bytecode.opencsv.CSVWriter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для формирования CSV-отчета справочника
 */
public class RefBookCSVReportBuilder extends AbstractRefBookReportBuilder {

    private CSVWriter csvWriter;

    // Записи справочника
    private List<Map<String, RefBookValue>> records;
    // Записи справочника, иерархического
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<>();

    public RefBookCSVReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Date version, String searchPattern, boolean exactSearch, final RefBookAttribute sortAttribute) {
        super(refBook, attributes, version, searchPattern, exactSearch, sortAttribute);

        this.records = records;
        if (refBook.isHierarchic()) {
            attributes.add(levelAttribute);
            for (Map<String, RefBookValue> record : records) {
                Map<String, RefBookValue> parent = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceObject();
                Long parentId = null;
                if (parent != null) {
                    parentId = parent.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
                }
                if (!hierarchicRecords.containsKey(parentId)) {
                    hierarchicRecords.put(parentId, new ArrayList<Map<String, RefBookValue>>());
                }
                hierarchicRecords.get(parentId).add(record);
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
    protected File createTempFile() {
        String fileName = refBook.getName().replace(' ', '_');
        if (refBook.isVersioned()) {
            fileName = fileName + "_" + FastDateFormat.getInstance("dd.MM.yyyy").format(version);
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
            List<String> headersNames = new ArrayList<>();
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

    /**
     * Формирует в отчете информационные строки с параметрами поиска
     */
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

    /**
     * Формирует рекурсивно строки таблицу для иерархического справочника
     */
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
                        result = FastDateFormat.getInstance("dd.MM.yyyy").format(value.getDateValue());
                    }
                }
                break;
            case STRING:
                // excel удаляет ведущие нули в числовых значениях. если в ячейке записаны только цифры, значение ячейки в формате "Общий"
                //воспринимается как числовое. решение: добавить табуляцию в начало https://stackoverflow.com/a/18133595
                if (value.getStringValue() != null) {
                    result = "\t" + value.getStringValue();
                }
                break;
            case REFERENCE:
                if (value.getReferenceObject() != null) {
                    result = getCellValue(value.getReferenceObject().get(attribute.getAlias()), attribute);
                }
                break;
            default:
                result = "undefined";
        }
        return result;
    }

    /**
     * Формирует строку таблицы с данынми отчета
     */
    private void createRow(Map<String, RefBookValue> record) {
        List<String> oneRow = new ArrayList<>();
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
