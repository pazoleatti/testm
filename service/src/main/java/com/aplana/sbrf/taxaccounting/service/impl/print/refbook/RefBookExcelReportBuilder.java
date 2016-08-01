package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.XlsxReportMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: avanteev
 * Класс для построения отчета по "Журналу аудита"
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

    public static final String FILE_NAME = "Отчет_";

    private List<Map<String, RefBookValue>> records;
    private Map<Long, List<Map<String, RefBookValue>>> hierarchicRecords = new HashMap<Long, List<Map<String, RefBookValue>>>();
    private Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValues;
    private RefBook refBook;
    private List<RefBookAttribute> refBookAttributeList;
    private CellStyleBuilder cellStyleBuilder;
    private Date version;
    private String filter;
    private RefBookAttribute sortAttribute;

    public RefBookExcelReportBuilder(RefBook refBook, List<Map<String, RefBookValue>> records, Map<Long, Pair<RefBookAttribute, Map<Long, RefBookValue>>> dereferenceValues, Date version, String filter, final RefBookAttribute sortAttribute) {
        super(FILE_NAME, ".xlsx");
        this.workBook = new XSSFWorkbook();
        String sheetName = refBook.getName().replaceAll("[/\\[\\]\\*\\:\\?\\\\]", "_"); //Убираем недостимые символы в названии листа
        this.sheet = workBook.createSheet(sheetName.length()>31?sheetName.substring(0, 31):sheetName);
        sheet.setRowSumsBelow(false);
        sheet.getLastRowNum();
        this.refBook = refBook;
        this.records = records;
        this.dereferenceValues = dereferenceValues;
        this.version = version;
        this.filter = filter;
        this.sortAttribute= sortAttribute;
        cellStyleBuilder = new CellStyleBuilder();
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
    protected void createTableHeaders() {
        if (LOG.isDebugEnabled())
            LOG.debug("Initialize table headers " + getClass());
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
        for (RefBookAttribute attribute : refBookAttributeList){
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
            LOG.debug("Initialize file header. " + getClass());
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

        if (refBook.isVersioned()) {
            Row versionRow = sheet.createRow(1);
            Cell versionCell = versionRow.createCell(cellNumber);
            versionCell.setCellValue("Дата актуальности: " + new SimpleDateFormat(DATE_FORMAT).format(version));
            versionCell.setCellStyle(cs);
            cellNumber = 0;
        }
        if (filter != null && !filter.isEmpty()) {
            Row filterRow = sheet.createRow(1);
            Cell filterCell = filterRow.createCell(cellNumber);
            filterCell.setCellValue("Фильтр: " + filter);
            filterCell.setCellStyle(cs);
            cellNumber = 0;
        }

        cellNumber = 0;
        rowNumber = rowNumber + 2;
    }

    @Override
    protected void createDataForTable() {
        if (LOG.isDebugEnabled())
            LOG.debug("Fill data for table. " + getClass() + "Data size: " + records.size());

        sheet.createFreezePane(0, rowNumber);
        if (!refBook.isHierarchic()) {
            for (Map<String, RefBookValue> record : records) {
                createRow(record);
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

    private void createRow(Map<String, RefBookValue> record) {
        //if (LOG.isDebugEnabled())
        //    LOG.debug("Data table " + record);
        Row row = sheet.createRow(rowNumber++);
        RefBookAttribute attr;
        for(RefBookAttribute attribute: refBookAttributeList) {
            RefBookValue value = record.get(attribute.getAlias());
            Cell cell = row.createCell(cellNumber);
            if (value != null) {
                if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                    attr = dereferenceValues.get(attribute.getId()).getFirst();
                    if (value.getReferenceValue() != null) {
                        value = dereferenceValues.get(attribute.getId()).getSecond().get(value.getReferenceValue());
                    } else {
                        value = null;
                    }
                } else {
                    attr = attribute;
                }
                cell.setCellStyle(cellStyleBuilder.createCellStyle(attr, attribute.getAlias()));
                if (value != null)
                    switch (value.getAttributeType()) {
                        case NUMBER:
                            if (value.getNumberValue() != null) {
                                if (Formats.BOOLEAN.equals(attr.getFormat())) {
                                    cell.setCellValue(value.getNumberValue().longValue()>0?"Да":"Нет");
                                } else {
                                    BigDecimal bd = (BigDecimal) value.getNumberValue();
                                    if (bd != null) {
                                        cell.setCellValue(attr.getPrecision() > 0 ? Double.parseDouble(bd.toString()) : bd.longValue());
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
                            if (value.getStringValue() != null)
                                cell.setCellValue(value.getStringValue());
                            break;
                        default:
                            cell.setCellValue("undefined");
                            break;
                    }
            }
            cellNumber++;
        }

        cellNumber = 0;
    }

    private final class CellStyleBuilder{

        private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

        private CellStyleBuilder() {
        }

        /**
         * Получить стриль для ячейки excel'я.
         *
         */
        public CellStyle createCellStyle(RefBookAttribute attribute, String alias) {
            return createCellStyle(attribute, alias, null);
        }

        /**
         * Получить стриль для ячейки excel'я.
         *
         * @param subKey дополнительное значение для ключа (что бы получить стили дельт)
         */
        public CellStyle createCellStyle(RefBookAttribute attribute, String alias, String subKey){
            String key = alias + (subKey != null && !subKey.isEmpty() ? subKey : "");
            if (cellStyleMap.containsKey(key))
                return cellStyleMap.get(key);
            DataFormat dataFormat = workBook.createDataFormat();
            CellStyle style = workBook.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);

            String format = attribute.getFormat()!=null?attribute.getFormat().getFormat():"";
            switch (attribute.getAttributeType()){
                case DATE:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    if(format.isEmpty()){
                        style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.sdf.get().toPattern()));
                    } else{
                        style.setDataFormat(dataFormat.getFormat(format));
                    }
                    break;
                case NUMBER:
                    if (!Formats.BOOLEAN.equals(attribute.getFormat())) {
                        style.setAlignment(CellStyle.ALIGN_RIGHT);
                        style.setWrapText(true);
                        style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.getPrecision(attribute.getPrecision())));
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
    protected void cellAlignment() {
        for (int i = 0; i < refBookAttributeList.size(); i++){
            widthCellsMap.put(i, refBookAttributeList.get(i).getWidth());
        }
        super.cellAlignment();
    }
}