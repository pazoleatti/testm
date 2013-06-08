package com.aplana.sbrf.taxaccounting.service.impl.print.logsystem;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractXlsxReportBuilder;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemReportBuilder extends AbstractXlsxReportBuilder {

    static {
        fileName = "Журнал_аудита_";
    }

    private int rowNumber = 3;
    private int cellNumber = 0;

    private static final String DATE_DATA_FORMAT = "dd.MM.yyyy HH:mm";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private List<LogSystemSearchResultItem> items;

    private static final String dateColumnHeader = "Дата-время";
    private static final String eventColumnHeader = "Событие";
    private static final String noteColumnHeader = "Текст события";
    private static final String reportPeriodColumnHeader = "Период";
    private static final String departmentColumnHeader = "Подразделение";
    private static final String formDataKindtColumnHeader = "Тип налоговой формы";
    private static final String formTypetColumnHeader = "Вид налоговой формы/декларации";
    private static final String userLoginColumnHeader = "Пользователь";
    private static final String userRolesColumnHeader = "Роль пользователя";
    private static final String userIpColumnHeader = "IP пользователя";

    public LogSystemReportBuilder(List<LogSystemSearchResultItem> items) {
        super();
        this.workBook = new SXSSFWorkbook();
        this.sheet = workBook.createSheet("Журнал аудита");
        this.sheet.setColumnWidth(2, cellWidth * 256);
        sheet.getLastRowNum();
        this.items = items;
    }

    @Override
    protected void createTableHeaders() {
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setBorderBottom(CellStyle.BORDER_THICK);
        cs.setBorderTop(CellStyle.BORDER_THICK);
        cs.setBorderRight(CellStyle.BORDER_THICK);
        cs.setBorderLeft(CellStyle.BORDER_THICK);
        cs.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
        cs.setFillBackgroundColor(HSSFColor.BRIGHT_GREEN.index);
        cs.setFillPattern(CellStyle.SOLID_FOREGROUND);

        Row row = sheet.createRow(rowNumber);
        Cell cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(dateColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(eventColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(noteColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(reportPeriodColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(departmentColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(formDataKindtColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(formTypetColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(userLoginColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(userRolesColumnHeader);

        cell = row.createCell(cellNumber++);
        cell.setCellStyle(cs);
        cell.setCellValue(userIpColumnHeader);

        cellNumber = 0;
        rowNumber = rowNumber + 1;
    }

    @Override
    protected void fillHeader() {
        CellStyle cs = workBook.createCellStyle();
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cs.setFont(font);

        Row row = sheet.createRow(rowNumber++);
        Cell cell = row.createCell(cellNumber++);
        cell.setCellValue("Журнал аудита");
        cell.setCellStyle(cs);
        cellNumber = 0;
        Row reportRow = sheet.createRow(rowNumber);
        Cell reportDate = reportRow.createCell(cellNumber);
        reportDate.setCellValue(new SimpleDateFormat(DATE_FORMAT).format(new Date()));

        cellNumber = 0;
        rowNumber = rowNumber + 2;
    }

    @Override
    protected void createDataForTable() {
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setWrapText(true);
        cs.setBorderBottom(CellStyle.BORDER_DOUBLE);
        cs.setBorderTop(CellStyle.BORDER_DOUBLE);
        cs.setBorderRight(CellStyle.BORDER_DOUBLE);
        cs.setBorderLeft(CellStyle.BORDER_DOUBLE);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DATA_FORMAT);

        for(LogSystemSearchResultItem item : items){
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Cell cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(sdf.format(item.getLogDate()));
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getEvent().getTitle());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getNote());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getReportPeriod().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getDepartment().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getFormKind().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getFormType().getName());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getUser().getLogin());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getRoles());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getIp());
            fillWidth(cellNumber, cell.getStringCellValue().length());
            cellNumber++;

            cellNumber = 0;
            rowNumber = rowNumber + 1;
        }
    }

    @Override
    protected void fillFooter() {

    }
}
