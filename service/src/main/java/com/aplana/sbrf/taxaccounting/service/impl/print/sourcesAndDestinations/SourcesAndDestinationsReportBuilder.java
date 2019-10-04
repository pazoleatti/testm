package com.aplana.sbrf.taxaccounting.service.impl.print.sourcesAndDestinations;

import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import com.aplana.sbrf.taxaccounting.service.impl.print.sourcesAndDestinations.StyleBuilder.CellType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Формирует excel файл из выгрузки списка источники-приемники
 */
public class SourcesAndDestinationsReportBuilder extends AbstractReportBuilder {
    // начальная строка с данными
    private final static int DATA_ROW_INDEX = 1;

    // Записи настроек источников и приемников
    private List<Relation> relationList;
    // Отображаемые столбцы
    private List<String> header = asList("Номер", "Налог", "Источник/ приемник", "Номер формы", "Подразделение", "Дата сдачи корректировки",
            "Тип формы", "Вид формы", "Год", "Вид отчетности", "Состояние формы");

    private int curRowIndex;
    private StyleBuilder styleBuilder;

    public SourcesAndDestinationsReportBuilder(List<Relation> relationList, long declarationDataId) {
        super("tmp_источники_и_приемники_" + FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()), ".xlsx");
        this.relationList = relationList;

        workBook = new SXSSFWorkbook();
        styleBuilder = new StyleBuilder(workBook);
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);

        this.sheet = workBook.createSheet(StringUtils.substring(Long.toString(declarationDataId), 0, 31));
        sheet.setRowSumsBelow(false);
    }

    @Override
    protected void cellAlignment() {
        List<Integer> widthList = Arrays.asList(10, 12, 14, 14, 42, 16, 21, 16, 12, 20, 20);
        for (int i = 0; i < header.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, widthList.get(i) * 269);
        }
    }

    @Override
    protected void fillHeader() {
    }

    @Override
    protected void createTableHeaders() {
        CellStyle style = styleBuilder.getTableHeaderStyle();
        Row row6 = sheet.createRow(0);
        for (int colIndex = 0; colIndex < header.size(); colIndex++) {
            Cell cell = row6.createCell(colIndex);
            cell.setCellValue(header.get(colIndex));
            cell.setCellStyle(style);
        }
    }

    @Override
    protected void createDataForTable() {
        curRowIndex = DATA_ROW_INDEX;
        int curReportNumberRow = 1;
        for (Relation relation : relationList) {
            createRowValues(curReportNumberRow, relation);
            curRowIndex++;
            curReportNumberRow++;
        }
    }

    private void createRowValues(int number, Relation relation) {
        sheet.createRow(curRowIndex);
        int colIndex = 0;
        createCellValue(colIndex++, number, "startDate", CellType.INTEGER, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, relation.getTaxType().getName(), "taxName", CellType.STRING, CellStyle.ALIGN_LEFT);
        if (relation.isSource()) {
            createCellValue(colIndex++, "источник", "sources", CellType.STRING, CellStyle.ALIGN_LEFT);
        } else {
            createCellValue(colIndex++, "приемник", "destinations", CellType.STRING, CellStyle.ALIGN_LEFT);
        }
        createCellValue(colIndex++, relation.getDeclarationDataId(), "declarationDataId", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, relation.getFullDepartmentName(), "fullDepartmentName", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, relation.getCorrectionDate(), "correctionDate", CellType.DATE, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, relation.getDeclarationTemplate().getDeclarationFormKind().getName(), "typeForm", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, relation.getDeclarationTypeName(), "typeForm", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, relation.getYear(), "year", CellType.STRING, CellStyle.ALIGN_CENTER);
        createCellValue(colIndex++, relation.getFormTypeCode(), "typeReport", CellType.STRING, CellStyle.ALIGN_LEFT);
        createCellValue(colIndex++, relation.getDeclarationState().getTitle(), "statusForm", CellType.STRING, CellStyle.ALIGN_LEFT);
    }

    private Cell createCellValue(int colIndex, Object value, String propName, CellType cellType, short align) {
        Cell cell = sheet.getRow(curRowIndex).createCell(colIndex);
        if (value != null) {
            switch (cellType) {
                case STRING:
                    if (!isEmpty(value.toString())) {
                        cell.setCellValue(value.toString());
                    }
                    break;
                case INTEGER:
                    Number number = value instanceof String ? Integer.valueOf((String) value) : (Number) value;
                    cell.setCellValue(number.doubleValue());
                    break;
                case DATE:
                    cell.setCellValue((Date) value);
                    break;
            }
        }
        cell.setCellStyle(styleBuilder.getCellStyle(propName, cellType, align));
        return cell;
    }

    @Override
    protected void fillFooter() {
    }
}
