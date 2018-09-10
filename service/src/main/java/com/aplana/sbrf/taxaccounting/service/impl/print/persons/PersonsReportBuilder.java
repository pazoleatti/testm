package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.service.impl.print.persons.StyleBuilder.CellType;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class PersonsReportBuilder extends AbstractReportBuilder {
    private final static int DATA_ROW_INDEX = 7;

    // Список ФЛ
    private List<RefBookPerson> persons;
    // Фильтр по ФЛ
    private RefBookPersonFilter filter;
    // Отображаемые столбцы
    private List<String> header = asList("№ п/п", "ИД ФЛ", "Важность", "Фамилия", "Имя", "Отчество", "Дата рождения", "Тип ДУЛ", "Серия и № ДУЛ", "Гражданство",
            "Статус НП", "ИНН в РФ", "ИНН в Стране гражданства", "СНИЛС", "Адрес в РФ", "Адрес за пределами РФ", "Система-источник", "Начало действия",
            "Окончание действия", "ИД версии");

    private int curRowIndex;
    private final static FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy");
    private StyleBuilder styleBuilder;

    public PersonsReportBuilder(List<RefBookPerson> persons, RefBookPersonFilter filter) {
        super("tmp_физические_лица_", ".xlsx");
        this.persons = persons;
        this.filter = filter;

        workBook = new SXSSFWorkbook();
        styleBuilder = new StyleBuilder(workBook);
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
        this.sheet = workBook.createSheet("Физические лица");
        sheet.setRowSumsBelow(false);
    }

    @Override
    protected void cellAlignment() {
        sheet.setColumnWidth(0, 1600);
        sheet.setColumnWidth(1, 3500);
        sheet.setColumnWidth(2, 3500);
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
        // TODO нужно сделать позже
        /*row1();
        row2();
        row3();
        row4();*/
    }

    @Override
    protected void createTableHeaders() {
        CellStyle style = styleBuilder.getTableHeaderStyle();
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
            createRowValues(new ReportPerson(person));
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
        cell.setCellStyle(styleBuilder.getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(1, 1, 3, header.size() - 1);
        sheet.addMergedRegion(region);
        cell = row2.createCell(3);
        if (filter != null) {
            List<String> values = new ArrayList<>();
            addIfNotEmpty(values, "Фамилия", filter.getLastName());
            addIfNotEmpty(values, "Имя", filter.getFirstName());
            addIfNotEmpty(values, "Отчество", filter.getMiddleName());
            addIfNotEmpty(values, "Дата рождения", filter.getBirthDateFrom(), filter.getBirthDateTo());
            addIfNotEmpty(values, "Серия и № ДУЛ", filter.getDocumentNumber());
            addIfNotEmpty(values, "ИД ФЛ", filter.getId());
            cell.setCellValue(values.isEmpty() ? "не заданы" : Joiner.on(", ").join(values));
        }
    }

    private void row3() {
        Row row3 = sheet.createRow(2);
        Cell cell = row3.createCell(0);
        cell.setCellValue("Адрес (в РФ, за пределами РФ)");
        cell.setCellStyle(styleBuilder.getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(2, 2, 3, header.size() - 1);
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
        cell.setCellStyle(styleBuilder.getBoldSmallStyle());

        CellRangeAddress region = new CellRangeAddress(3, 3, 3, header.size() - 1);
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

    private void createRowValues(ReportPerson reportPerson) {
        sheet.createRow(curRowIndex);
        int colIndex = 0;
        createCellValue(colIndex++, curRowIndex - DATA_ROW_INDEX + 1, "rn", CellType.NUMERIC);
        createCellValue(colIndex++, reportPerson.getFLId(), "flId", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getVip(), "vip", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getLastName(), "lastName", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getFirstName(), "firstName", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getMiddleName(), "middleName", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getBirthDay(), "birthDate", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getDocName(), "docName", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getDocNumber(), "docNumber", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getCitizenship(), "citizenship", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getTaxpayerState(), "taxpayerState", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getInn(), "inn", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getInnForeign(), "innForeign", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getSnils(), "snils", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getRussianAddress(), "address", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getForeignAddress(), "foreignAddress", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getSource(), "source", CellType.STRING);
        createCellValue(colIndex++, reportPerson.getVersion(), "version", CellType.DATE);
        createCellValue(colIndex++, reportPerson.getVersionEnd(), "versionEnd", CellType.DATE);
        createCellValue(colIndex++, reportPerson.getId(), "id", CellType.STRING);
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
                case NUMERIC:
                    cell.setCellValue(((Number) value).doubleValue());
                    break;
                case DATE:
                    cell.setCellValue(dateFormat.format((Date) value));
                    break;
            }
        }
        cell.setCellStyle(styleBuilder.getCellStyle(propName, cellType));
        return cell;
    }

    @Override
    protected void fillFooter() {
    }

    private void addIfNotEmpty(List<String> values, String fieldName, String value) {
        if (!isEmpty(value)) {
            values.add(fieldName + ": " + value);
        }
    }

    private void addIfNotEmpty(List<String> values, String fieldName, Date dateFrom, Date dateTo) {
        if (dateFrom != null || dateTo != null) {
            values.add(fieldName + ": с " +
                    (dateFrom != null ? dateFormat.format(dateFrom) : "-") + " по " +
                    (dateTo != null ? dateFormat.format(dateTo) : "-"));
        }
    }
}
