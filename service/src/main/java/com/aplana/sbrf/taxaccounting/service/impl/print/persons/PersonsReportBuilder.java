package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAddress;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class PersonsReportBuilder extends AbstractReportBuilder {
    private final static int DATA_ROW_INDEX = 7;

    // Список ФЛ
    private List<RefBookPerson> persons;
    // Фильтр по ФЛ
    private RefBookPersonFilter filter;
    // Отображаемые столбцы
    private List<String> header = asList("№ п/п", "ИД ФЛ", "Важность", "Фамилия", "Имя", "Отчество", "Тип ДУЛ", "Серия и № ДУЛ", "Гражданство",
            "Статус НП", "ИНН в РФ", "ИНН в Стране гражданства", "СНИЛС", "Адрес в РФ", "Адрес за пределами РФ", "Система-источник", "Начало действия",
            "Окончание действия", "ИД версии");

    private int curRowIndex;
    private final static FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy");
    private Map<String, CellStyle> cellStyleCache = new HashMap<>();

    public PersonsReportBuilder(List<RefBookPerson> persons, RefBookPersonFilter filter) {
        super("Физические_лица_", ".xlsx");
        this.persons = persons;
        this.filter = filter;

        workBook = new SXSSFWorkbook();
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
        this.sheet = workBook.createSheet("Физические лица");
        sheet.setRowSumsBelow(false);
    }

    @Override
    protected void cellAlignment() {
        sheet.setColumnWidth(0, 1600);
        sheet.setColumnWidth(1, 3500);
        sheet.setColumnWidth(2, 3500);
        super.cellAlignment();
        for (int i = 3; i < header.size(); i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) > 10000) {
                sheet.setColumnWidth(i, 10000);
            } else if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }
    }

    @Override
    protected void fillHeader() {
        row1();
        row2();
        row3();
        row4();
    }

    @Override
    protected void createTableHeaders() {
        CellStyle style = getTableHeaderStyle();
        Row row6 = sheet.createRow(6);
        for (int colIndex = 0; colIndex < header.size(); colIndex++) {
            Cell cell = row6.createCell(colIndex);
            cell.setCellValue(header.get(colIndex));
            cell.setCellStyle(style);
        }
    }

    @Override
    protected void createDataForTable() {
        curRowIndex = DATA_ROW_INDEX;
        for (RefBookPerson person : persons) {
            createRowValues(person);
            curRowIndex++;
        }
    }

    private void row1() {
        CellStyle styleHeader = workBook.createCellStyle();
        styleHeader.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
        Font fontHeader = workBook.createFont();
        fontHeader.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontHeader.setFontHeightInPoints((short) 14);
        styleHeader.setFont(fontHeader);
        Row row1 = sheet.createRow(0);
        Cell cell = row1.createCell(0);
        cell.setCellStyle(styleHeader);
        cell.setCellValue(String.format("Реестр физических лиц %s", FastDateFormat.getInstance("dd.MM.yyyy").format(new Date())));
    }

    private void row2() {
        Row row2 = sheet.createRow(1);
        Cell cell = row2.createCell(0);
        cell.setCellValue("Реквизиты физического лица");
        cell.setCellStyle(getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(1, 1, 3, 18);
        sheet.addMergedRegion(region);
        cell = row2.createCell(3);
        if (filter != null) {
            List<String> values = new ArrayList<>();
            addIfNotEmpty("Фамилия", filter.getLastName(), values);
            addIfNotEmpty("Имя", filter.getFirstName(), values);
            addIfNotEmpty("Отчество", filter.getMiddleName(), values);
            addIfNotEmpty("Дата рождения", filter.getBirthDateFrom(), filter.getBirthDateTo(), values);
            addIfNotEmpty("Серия и № ДУЛ", filter.getDocumentNumber(), values);
            addIfNotEmpty("ИД ФЛ", filter.getId(), values);
            cell.setCellValue(values.isEmpty() ? "не заданы" : Joiner.on(", ").join(values));
        }
    }

    private void row3() {
        Row row3 = sheet.createRow(2);
        Cell cell = row3.createCell(0);
        cell.setCellValue("Адрес (в РФ, за пределами РФ)");
        cell.setCellStyle(getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(2, 2, 3, 18);
        sheet.addMergedRegion(region);
        cell = row3.createCell(3);
        if (filter != null) {
            List<String> values = new ArrayList<>();
            cell.setCellValue(values.isEmpty() ? "не заданы" : Joiner.on(", ").join(values));
        }
    }

    private void row4() {
        Row row4 = sheet.createRow(3);
        Cell cell = row4.createCell(0);
        cell.setCellValue("Параметры отображения версий");
        cell.setCellStyle(getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(3, 3, 3, 18);
        sheet.addMergedRegion(region);
        cell = row4.createCell(3);
        if (filter != null) {
            List<String> values = new ArrayList<>();
            if (filter.getVersionDate() != null) {
                values.add("Версия на дату " + dateFormat.format(filter.getVersionDate()));
            } else {
                values.add("Все версии");
            }
            if (filter.getDuplicates() != null) {
                values.add("Дубликаты " + (filter.getDuplicates() ? "отображаются" : "не отображаются"));
            }
            cell.setCellValue(values.isEmpty() ? "не заданы" : Joiner.on(", ").join(values));
        }
    }

    private void createRowValues(RefBookPerson person) {
        sheet.createRow(curRowIndex);
        createCellValue(0, curRowIndex - DATA_ROW_INDEX + 1, "rn", CellType.NUMBERIC);
        createCellValue(1, getFLId(person), "flId", CellType.STRING);
        createCellValue(2, person.isVip() != null && person.isVip() ? "VIP" : "Не VIP", "vip", CellType.STRING);
        createCellValue(3, person.getLastName(), "lastName", CellType.STRING);
        createCellValue(4, person.getFirstName(), "firstName", CellType.STRING);
        createCellValue(5, person.getMiddleName(), "middleName", CellType.STRING);
        createCellValue(6, getDocName(person), "docName", CellType.STRING);
        createCellValue(7, getDocNumber(person), "docNumber", CellType.STRING);
        createCellValue(8, getCitizenship(person), "citizenship", CellType.STRING);
        createCellValue(9, person.getTaxpayerState() != null ? person.getTaxpayerState().getCode() : null, "taxpayerState", CellType.STRING);
        createCellValue(10, getInn(person), "inn", CellType.STRING);
        createCellValue(11, getInnForeign(person), "innForeign", CellType.STRING);
        createCellValue(12, getSnils(person), "snils", CellType.STRING);
        createCellValue(13, getRussianAddress(person), "address", CellType.STRING);
        createCellValue(14, getForeignAddress(person), "foreignAddress", CellType.STRING);
        createCellValue(15, person.getSource() != null ? person.getSource().getName() : null, "source", CellType.STRING);
        createCellValue(16, person.getVersion(), "version", CellType.DATE);
        createCellValue(17, person.getVersionEnd(), "versionEnd", CellType.DATE);
        createCellValue(18, person.getId(), "id", CellType.STRING);
    }

    private String getCitizenship(RefBookPerson person) {
        return person.getCitizenship() != null ? "(" + person.getCitizenship().getCode() + ") " + person.getCitizenship().getName() : null;
    }

    private String getDocName(RefBookPerson person) {
        return getPermissiveValue(person.getDocNameForJson());
    }

    private String getDocNumber(RefBookPerson person) {
        return getPermissiveValue(person.getDocNumberForJson());
    }

    private String getInn(RefBookPerson person) {
        return getPermissiveValue(person.getInnForJson());
    }

    private String getInnForeign(RefBookPerson person) {
        return getPermissiveValue(person.getInnForeignForJson());
    }

    private String getSnils(RefBookPerson person) {
        return getPermissiveValue(person.getSnilsForJson());
    }

    private String getPermissiveValue(Permissive<String> permissive) {
        if (permissive != null) {
            return permissive.hasPermission() ? permissive.value() : "Доступ ограничен";
        }
        return null;
    }

    private String getRussianAddress(RefBookPerson person) {
        if (person.getAddress() != null) {
            if (person.getAddressForJson().hasPermission()) {
                return person.getAddress().getAddressType() == 0 ? getAddressString(person.getAddress()) : null;
            } else {
                return "Доступ ограничен";
            }
        }
        return null;
    }

    private String getForeignAddress(RefBookPerson person) {
        if (person.getAddress() != null) {
            if (person.getAddressForJson().hasPermission()) {
                return person.getAddress().getAddressType() == 1 ? getAddressString(person.getAddress()) : null;
            } else {
                return "Доступ ограничен";
            }
        }
        return null;
    }

    private String getAddressString(RefBookAddress address) {
        List<String> values = new ArrayList<>();
        if (address.getAddressType() == 0) {
            addIfNotEmpty(address.getPostalCode(), values);
            addIfNotEmpty(address.getRegionCode(), values);
            addIfNotEmpty(address.getDistrict(), values);
            addIfNotEmpty(address.getCity(), values);
            addIfNotEmpty(address.getLocality(), values);
            addIfNotEmpty(address.getStreet(), values);
            addIfNotEmpty(address.getHouse(), values);
            addIfNotEmpty(address.getBuild(), values);
            addIfNotEmpty(address.getAppartment(), values);
        } else {
            if (address.getCountry() != null) {
                addIfNotEmpty(address.getCountry().getName(), values);
            }
            addIfNotEmpty(address.getAddress(), values);
        }
        return Joiner.on(", ").join(values);
    }

    private Cell createCellValue(int colIndex, Object value, String propName, CellType cellType) {
        Cell cell = sheet.getRow(curRowIndex).createCell(colIndex);
        if (value != null) {
            switch (cellType) {
                case STRING:
                    if (!isEmpty(value.toString())) {
                        cell.setCellValue(value.toString());
                    }
                    break;
                case NUMBERIC:
                    cell.setCellValue(((Number) value).doubleValue());
                    break;
                case DATE:
                    cell.setCellValue(dateFormat.format((Date) value));
                    break;
            }
        }
        cell.setCellStyle(getCellStyle(propName, cellType));
        return cell;
    }

    private String getFLId(RefBookPerson person) {
        if (person.getOldId() != null && person.getRecordId() != null) {
            return person.getOldId() + (person.getOldId().equals(person.getRecordId()) ? "" : " (Дубл.)");
        }
        return null;
    }

    private CellStyle getCellStyle(String colPropName, CellType cellType) {
        String cacheKey = colPropName + "_" + cellType.name();
        CellStyle cellStyle = cellStyleCache.get(cacheKey);
        if (cellStyle == null) {
            cellStyle = workBook.createCellStyle();
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            DataFormat dataFormat = workBook.createDataFormat();
            switch (cellType) {
                case STRING:
                    cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
                    cellStyle.setWrapText(true);
                    break;
                case NUMBERIC:
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    cellStyle.setWrapText(true);
                    cellStyle.setDataFormat(dataFormat.getFormat("@"));
                    break;
                case DATE:
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyle.setDataFormat(dataFormat.getFormat("dd.MM.yyyy"));
                    break;
            }
            cellStyleCache.put(cacheKey, cellStyle);
        }
        return cellStyle;
    }

    private CellStyle getBoldSmallStyle() {
        CellStyle style = cellStyleCache.get("table_header");
        if (style == null) {
            style = workBook.createCellStyle();
            style.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
            Font fontBoldSmall = workBook.createFont();
            fontBoldSmall.setBoldweight(Font.BOLDWEIGHT_BOLD);
            fontBoldSmall.setFontHeightInPoints((short) 11);
            style.setFont(fontBoldSmall);
            cellStyleCache.put("table_header", style);
        }
        return style;
    }

    private CellStyle getTableHeaderStyle() {
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle tableHeaderCellStyle = workBook.createCellStyle();
        tableHeaderCellStyle.setFont(font);
        tableHeaderCellStyle.setBorderRight(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setBorderTop(CellStyle.BORDER_THIN);
        tableHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        tableHeaderCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        tableHeaderCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        tableHeaderCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        tableHeaderCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        tableHeaderCellStyle.setWrapText(true);
        return tableHeaderCellStyle;
    }

    @Override
    protected void fillFooter() {
    }

    private void addIfNotEmpty(String string, List<String> values) {
        if (!isEmpty(string)) {
            values.add(string);
        }
    }

    private void addIfNotEmpty(String fieldName, String value, List<String> values) {
        if (!isEmpty(value)) {
            values.add(fieldName + ": " + value);
        }
    }

    private void addIfNotEmpty(String fieldName, Date dateFrom, Date dateTo, List<String> values) {
        if (dateFrom != null || dateTo != null) {
            values.add(fieldName + ": с " +
                    (dateFrom != null ? dateFormat.format(dateFrom) : "-") + " по " +
                    (dateTo != null ? dateFormat.format(dateTo) : "-"));
        }
    }

    enum CellType {
        STRING,
        NUMBERIC,
        DATE
    }
}
