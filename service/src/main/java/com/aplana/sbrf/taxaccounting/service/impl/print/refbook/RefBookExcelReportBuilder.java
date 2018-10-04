package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.BatchIterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для построения XLSX-отчета по линейным и иерархическим справочникам
 */
public class RefBookExcelReportBuilder extends AbstractRefBookReportBuilder {

    private static final Log LOG = LogFactory.getLog(RefBookExcelReportBuilder.class);

    private int rowNumber = 3;
    private int cellNumber = 0;

    // Записи для иерархического справочника
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<>();
    // Записи справочника, загружаются по мере необходимости
    private BatchIterator dataIterator;
    // Утилита для формирования стилей ячейки
    private CellStyleBuilder cellStyleBuilder;

    private RefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version,
                                      String searchPattern, boolean exactSearch, RefBookAttribute sortAttribute) {
        super(refBook, attributes, version, searchPattern, exactSearch, sortAttribute);

        workBook = new SXSSFWorkbook();
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
        String sheetName = refBook.getName().replaceAll("[/\\[\\]\\*\\:\\?\\\\]", "_"); //Убираем недостимые символы в названии листа
        sheetName = sheetName.length() > 31 ? sheetName.substring(0, 31) : sheetName;
        this.sheet = workBook.createSheet(sheetName);
        sheet.setRowSumsBelow(false);
        sheet.getLastRowNum();
        cellStyleBuilder = new CellStyleBuilder();
    }

    public RefBookExcelReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                     boolean exactSearch, RefBookAttribute sortAttribute, List<Map<String, RefBookValue>> records) {
        this(refBook, attributes, version, searchPattern, exactSearch, sortAttribute);
        if (refBook.isHierarchic()) {
            attributes.add(levelAttribute);
            // Для иерархических справочников записи упорядочиваем по родительскому узлу
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
            versionCell.setCellValue("Дата актуальности: " + FastDateFormat.getInstance("dd.MM.yyyy").format(version));
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
                    } else if (value.getNumberValue() instanceof BigDecimal) {
                        BigDecimal bd = (BigDecimal) value.getNumberValue();
                        if (bd != null) {
                            cell.setCellValue(attribute.getPrecision() > 0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                        }
                    } else {
                        cell.setCellValue(value.getNumberValue().longValue());
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

        private Map<String, CellStyle> cellStyleMap = new HashMap<>();

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
                        style.setDataFormat(dataFormat.getFormat("dd.MM.yyyy"));
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
    protected void flush(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            workBook.write(out);
        } finally {
            IOUtils.closeQuietly(out);
            // SXSSFWorkbook создаёт временные файлы, надо удалять
            //((SXSSFWorkbook) workBook).dispose(); // c версии poi 3.9
            try {
                deleteSXSSFTempFiles((SXSSFWorkbook) workBook);
            } catch (Exception e) {
                LOG.warn("Временнный файл не был удален.");
            }
        }
    }

    @Override
    protected File createTempFile() {
        String fileName = refBook.getName().replace(' ', '_');
        if (refBook.isVersioned()) {
            fileName = fileName + "_" + FastDateFormat.getInstance("dd.MM.yyyy").format(version);
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

    /**
     * Удаляет временные файлы, создаваемые при работе с SXSSFWorkbook
     */
    private void deleteSXSSFTempFiles(SXSSFWorkbook workbook) throws NoSuchFieldException, IllegalAccessException {

        int numberOfSheets = workbook.getNumberOfSheets();

        //iterate through all sheets (each sheet as a temp file)
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheetAt = workbook.getSheetAt(i);

            //delete only if the sheet is written by stream
            if (sheetAt instanceof SXSSFSheet) {
                SheetDataWriter sdw = (SheetDataWriter) getPrivateAttribute(sheetAt, "_writer");
                File f = (File) getPrivateAttribute(sdw, "_fd");

                if (!f.delete()) {
                    LOG.warn(String.format("Временнный файл %s не был удален.", f.getName()));
                }
            }
        }
    }

    private Object getPrivateAttribute(Object containingClass, String fieldToGet) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = containingClass.getClass().getDeclaredField(fieldToGet);
        declaredField.setAccessible(true);
        return declaredField.get(containingClass);
    }
}