package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Класс для построения XLSX-отчета по линейным и иерархическим справочникам
 */
public class RefBookExcelReportBuilder extends AbstractReportBuilder {

    private static final Log LOG = LogFactory.getLog(RefBookExcelReportBuilder.class);

    private static final String DATE_FORMAT = "dd.MM.yyyy";
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

    private int rowNumber = 3;
    private int cellNumber = 0;

    private List<RefBookAttribute> attributes;
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<>();
    private RefBook refBook;
    private CellStyleBuilder cellStyleBuilder;
    private Date version;
    private String searchPattern;
    private boolean exactSearch;
    private RefBookAttribute sortAttribute;
    private BatchIterator dataIterator;

    public static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    public RefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version,
                                     String searchPattern, boolean exactSearch, RefBookAttribute sortAttribute) {
        initWorkbook();
        String sheetName = refBook.getName().replaceAll("[/\\[\\]\\*\\:\\?\\\\]", "_"); //Убираем недостимые символы в названии листа
        sheetName = sheetName.length() > 31 ? sheetName.substring(0, 31) : sheetName;
        this.sheet = workBook.createSheet(sheetName);
        sheet.setRowSumsBelow(false);
        sheet.getLastRowNum();
        this.refBook = refBook;
        this.attributes = attributes;
        this.version = version;
        this.searchPattern = searchPattern;
        this.exactSearch = exactSearch;
        this.sortAttribute = sortAttribute;
        cellStyleBuilder = new CellStyleBuilder();
    }

    protected void initWorkbook() {
        this.workBook = new XSSFWorkbook();
    }

    public RefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                     boolean exactSearch, RefBookAttribute sortAttribute, PagingResult<Map<String, RefBookValue>> records) {
        this(refBook, attributes, version, searchPattern, exactSearch, sortAttribute);
        if (refBook.isHierarchic()) {
            attributes.add(levelAttribute);
            // Для иерархических справочников записи упорядочиваем по родительскому узлу
            for (Map<String, RefBookValue> record : records) {
                Long parent_id = record.get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
                if (!hierarchicRecords.containsKey(parent_id)) {
                    hierarchicRecords.put(parent_id, new ArrayList<Map<String, RefBookValue>>());
                }
                hierarchicRecords.get(parent_id).add(record);
            }
        }

    }

    public RefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                     boolean exactSearch, RefBookAttribute sortAttribute, BatchIterator dataIterator) {
        this(refBook, attributes, version, searchPattern, exactSearch, sortAttribute);
        this.dataIterator = dataIterator;
    }

    @Override
    protected void createTableHeaders() {
        if (LOG.isDebugEnabled())
            LOG.info("Initialize table headers " + getClass());
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setBorderBottom(CellStyle.BORDER_THIN);
        cs.setBorderTop(CellStyle.BORDER_THIN);
        cs.setBorderRight(CellStyle.BORDER_THIN);
        cs.setBorderLeft(CellStyle.BORDER_THIN);
        cs.setFillForegroundColor(IndexedColors.GREEN.index);
        cs.setFillBackgroundColor(IndexedColors.GREEN.index);
        cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cs.setWrapText(true);

        Row row = sheet.createRow(rowNumber);
        for (RefBookAttribute attribute : attributes) {
            fillWidth(cellNumber, cellWidthMin);
            Cell cell = row.createCell(cellNumber++);
            cell.setCellStyle(cs);
            cell.setCellValue(attribute.getName());
        }

        cellNumber = 0;
        rowNumber = rowNumber + 1;
    }

    @Override
    protected void fillHeader() {
        if (LOG.isDebugEnabled())
            LOG.info("Initialize file header. " + getClass());
        CellStyle cs = workBook.createCellStyle();
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cs.setFont(font);
        cs.setAlignment(CellStyle.ALIGN_LEFT);

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(cellNumber++);
        cell.setCellValue("Справочник: " + refBook.getName());
        cell.setCellStyle(cs);
        cellNumber = 0;

        int rowNum = 1;
        if (refBook.isVersioned()) {
            Row versionRow = sheet.createRow(rowNum);
            Cell versionCell = versionRow.createCell(cellNumber);
            versionCell.setCellValue("Дата актуальности: " + new SimpleDateFormat(DATE_FORMAT).format(version));
            versionCell.setCellStyle(cs);
            cellNumber = 0;
            rowNum++;
        }
        if (searchPattern != null && !searchPattern.isEmpty()) {
            Row filterRow = sheet.createRow(rowNum);
            Cell filterCell = filterRow.createCell(cellNumber);
            filterCell.setCellValue("Параметр поиска: \"" + searchPattern + "\"" + (exactSearch ? " (по точному совпадению)" : ""));
            filterCell.setCellStyle(cs);
            cellNumber = 0;
        }

        cellNumber = 0;
        rowNumber = rowNumber + 1;
    }

    @Override
    protected void createDataForTable() {
        sheet.createFreezePane(0, rowNumber);
        if (!refBook.isHierarchic()) {
            while (dataIterator.hasNext()) {
                checkInterrupted();
                createRow(dataIterator.getNextRecord());
            }
        } else {
            printNode(null, 0);
        }
    }

    private void printNode(Long parent_id, int level) {
        level++;
        if (hierarchicRecords.containsKey(parent_id)) {
            Collections.sort(hierarchicRecords.get(parent_id), comparator);
            for (Map<String, RefBookValue> record : hierarchicRecords.get(parent_id)) {
                record.put(levelAttribute.getAlias(), new RefBookValue(RefBookAttributeType.NUMBER, new BigDecimal(level)));
                createRow(record);
                int startRow = rowNumber;
                printNode(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), level);
                if (level < 8 && startRow != rowNumber) {
                    sheet.groupRow(startRow, rowNumber - 1);
                }
            }
        }
    }

    /**
     * Проставляет значение атрибута в ячейку в зависимости от его типа
     *
     * @param cell      ячейка в которую будет проставлено значение
     * @param value     объект, из которого надо извлечь значение
     * @param attribute атрибут, значение которого надо извлечь
     */
    private void setCellValue(Cell cell, RefBookValue value, RefBookAttribute attribute) {
        switch (value.getAttributeType()) {
            case NUMBER:
                if (value.getNumberValue() != null) {
                    if (Formats.BOOLEAN.equals(attribute.getFormat())) {
                        cell.setCellValue(value.getNumberValue().longValue() > 0 ? "Да" : "Нет");
                    } else {
                        BigDecimal bd = (BigDecimal) value.getNumberValue();
                        if (bd != null) {
                            cell.setCellValue(attribute.getPrecision() > 0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                        }
                    }
                }
                break;
            case DATE:
                if (value.getDateValue() != null) {
                    cell.setCellValue(value.getDateValue());
                }
                break;
            case STRING:
                if (value.getStringValue() != null) {
                    cell.setCellValue(value.getStringValue());
                }
                break;
            case REFERENCE:
                if (value.getReferenceObject() != null) {
                    setCellValue(cell, value.getReferenceObject().get(attribute.getAlias()), attribute);
                }
                break;
            default:
                cell.setCellValue("undefined");
                break;
        }
    }

    /**
     * Добавляет строку в отчет
     *
     * @param record данные строки
     */
    private void createRow(Map<String, RefBookValue> record) {
        Row row = sheet.createRow(rowNumber++);
        for (RefBookAttribute attribute : attributes) {
            RefBookAttribute attr;
            RefBookValue value = record.get(attribute.getAlias());
            Cell cell = row.createCell(cellNumber);
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                attr = attribute.getRefBookAttribute();
            } else {
                attr = attribute;
            }
            if (value != null) {
                setCellValue(cell, value, attr);
            }
            cell.setCellStyle(cellStyleBuilder.createCellStyle(attr, attribute.getAlias()));
            cellNumber++;
        }
        cellNumber = 0;
    }

    private final class CellStyleBuilder {

        private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

        private CellStyleBuilder() {
        }

        /**
         * Получить стриль для ячейки excel'я.
         */
        public CellStyle createCellStyle(RefBookAttribute attribute, String alias) {
            return createCellStyle(attribute, alias, null);
        }

        /**
         * Получить стриль для ячейки excel'я.
         *
         * @param subKey дополнительное значение для ключа (что бы получить стили дельт)
         */
        public CellStyle createCellStyle(RefBookAttribute attribute, String alias, String subKey) {
            String key = alias + (subKey != null && !subKey.isEmpty() ? subKey : "");
            if (cellStyleMap.containsKey(key))
                return cellStyleMap.get(key);
            DataFormat dataFormat = workBook.createDataFormat();
            CellStyle style = workBook.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);

            String format = attribute.getFormat() != null ? attribute.getFormat().getFormat() : "";
            switch (attribute.getAttributeType()) {
                case DATE:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    if (format.isEmpty()) {
                        style.setDataFormat(dataFormat.getFormat(sdf.get().toPattern()));
                    } else {
                        style.setDataFormat(dataFormat.getFormat(format));
                    }
                    break;
                case NUMBER:
                    if (!Formats.BOOLEAN.equals(attribute.getFormat())) {
                        style.setAlignment(CellStyle.ALIGN_RIGHT);
                        style.setWrapText(true);
                        style.setDataFormat(dataFormat.getFormat(getPrecision(attribute.getPrecision())));
                        break;
                    }
                case STRING:
                    style.setAlignment(CellStyle.ALIGN_LEFT);
                    style.setWrapText(true);
                    break;
                case REFERENCE:
                    throw new SecurityException("Неправильный тип атрибута");
            }

            cellStyleMap.put(key, style);
            return style;
        }
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
        fileName = fileName + ".xlsx";
        //Используется такой подход, т.к. File.createTempFile в именах генерирует случайные числа. Здесь запись
        //выполняется в 1 и тот же файл. Но записываются всегда актуальные данные
        return new File(TMP_DIR, fileName);
    }

    @Override
    protected void cellAlignment() {
        for (int i = 0; i < attributes.size(); i++) {
            widthCellsMap.put(i, attributes.get(i).getWidth());
        }
        super.cellAlignment();
    }

    /*
     * Patterns for printing in Excel. "###," shows that we must grouping by 3 characters
     */
    public static String getPrecision(int number) {
        StringBuilder str = new StringBuilder("#,##0");
        if (number > 0) {
            str.append(".");
            for (int i = 0; i < number; i++)
                str.append("0");
        }
        return str.toString();
    }

    private void checkInterrupted() {
        if (Thread.interrupted()) {
            LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
            throw new TAInterruptedException();
        }
    }
}