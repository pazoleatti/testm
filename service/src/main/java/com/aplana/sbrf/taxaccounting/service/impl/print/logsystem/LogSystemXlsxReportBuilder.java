package com.aplana.sbrf.taxaccounting.service.impl.print.logsystem;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 * Класс для построения отчета по "Журналу аудита"
 */
public class LogSystemXlsxReportBuilder extends AbstractReportBuilder {

	private static final Log LOG = LogFactory.getLog(LogSystemXlsxReportBuilder.class);

	private static final String DATE_DATA_FORMAT = "dd.MM.yyyy HH:mm:ss";
	private static final String DATE_FORMAT = "dd.MM.yyyy";

	private int rowNumber = 3;
    private int cellNumber = 0;

    private List<LogSearchResultItem> items;

    private static String[] headers = new String[]{"Дата-время", "Событие", "Текст события", "Период", "Подразделение",
            "Тип формы", "Тип налоговой формы", "Вид налоговой формы/декларации",
            "Пользователь", "Роль пользователя", "IP пользователя", "Сервер"};

    public LogSystemXlsxReportBuilder(List<LogSearchResultItem> items) {
        super("audit",".xlsx");
        this.workBook = new SXSSFWorkbook(500);
        this.sheet = workBook.createSheet("Журнал аудита");
        sheet.getLastRowNum();
        this.items = items;
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

        Row row = sheet.createRow(rowNumber);
        for (String s : headers){
            fillWidth(cellNumber, cellWidthMin);
            Cell cell = row.createCell(cellNumber++);
            cell.setCellStyle(cs);
            cell.setCellValue(s);
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
        cs.setAlignment(CellStyle.ALIGN_CENTER);

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(cellNumber++);
        CellRangeAddress regionTitle = new CellRangeAddress(
                0,
                0,
                0,
                10);
        cell.setCellValue("Журнал аудита");
        cell.setCellStyle(cs);
        cellNumber = 0;
        Row reportRow = sheet.createRow(1);
        Cell reportDate = reportRow.createCell(cellNumber);
        reportDate.setCellValue(new SimpleDateFormat(DATE_FORMAT).format(new Date()));
        reportDate.setCellStyle(cs);
        CellRangeAddress regionDate = new CellRangeAddress(
                1,
                1,
                0,
                10);
        sheet.addMergedRegion(regionTitle);
        sheet.addMergedRegion(regionDate);

        cellNumber = 0;
        rowNumber = rowNumber + 2;
    }

    @Override
    protected void createDataForTable() {
        if (LOG.isDebugEnabled())
            LOG.debug("Fill data for table. " + getClass() + "Data size: " + items.size());
        CellStyle cs = workBook.createCellStyle();
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setWrapText(true);
        cs.setBorderBottom(CellStyle.BORDER_DOUBLE);
        cs.setBorderTop(CellStyle.BORDER_DOUBLE);
        cs.setBorderRight(CellStyle.BORDER_DOUBLE);
        cs.setBorderLeft(CellStyle.BORDER_DOUBLE);

        //Стиль для "Вида налоговой формы/декларации"
        CellStyle csFormType = workBook.createCellStyle();
        csFormType.setAlignment(CellStyle.ALIGN_LEFT);
        csFormType.setBorderBottom(CellStyle.BORDER_DOUBLE);
        csFormType.setBorderTop(CellStyle.BORDER_DOUBLE);
        csFormType.setBorderRight(CellStyle.BORDER_DOUBLE);
        csFormType.setBorderLeft(CellStyle.BORDER_DOUBLE);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DATA_FORMAT);

        for(LogSearchResultItem item : items){
            if (LOG.isDebugEnabled())
                LOG.debug("Data table " + item);
            Row row = sheet.createRow(rowNumber++);
            Cell cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(sdf.format(item.getLogDate()));
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getEvent().getTitle());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getNote() == null?"":item.getNote());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getReportPeriodName() != null ? item.getReportPeriodName() : "");
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getDepartmentName());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getAuditFormType() != null ? item.getAuditFormType().getName() : "");
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getFormKind() != null?item.getFormKind().getTitle():"");
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(csFormType);
            cell.setCellValue(item.getFormTypeName() != null? item.getFormTypeName() :
                    item.getDeclarationTypeName() != null? item.getDeclarationTypeName() : "");
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getUser());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getRoles());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getIp());
            cellNumber++;

            cell = row.createCell(cellNumber);
            cell.setCellStyle(cs);
            cell.setCellValue(item.getServer());
            cellNumber++;

            cellNumber = 0;
        }
        sheet.groupRow(10, 15);
    }

    @Override
    protected void fillFooter() {

    }
}