package com.aplana.sbrf.taxaccounting.service.impl.print.logentry;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class LogEntryReportBuilder extends AbstractReportBuilder {
	
	private static final String FIRST_COLUMN = "№ п/п";
	private static final String SECOND_COLUMN = "Тип сообщения";
	private static final String THIRD_COLUMN = "Текст сообщения";
	
	private List<LogEntry> list;

	private int rowNumber = 0;
	private int cellNumber = 0;

    private static final String fileName = "Список_ошибок_";
    private static final String postfix = ".xlsx";

	
	public LogEntryReportBuilder(List<LogEntry> list){
        super(fileName, postfix);
        this.list = list;
		this.workBook = new XSSFWorkbook();
		this.sheet = workBook.createSheet("Учет налогов");
        this.sheet.setColumnWidth(2, cellWidthMin * 256 * 4);
	}
	
	protected void createTableHeaders(){
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
		cell.setCellValue(FIRST_COLUMN);
		
		cell = row.createCell(cellNumber++);
		cell.setCellStyle(cs);
		cell.setCellValue(SECOND_COLUMN);
		
		cell = row.createCell(cellNumber++);
		cell.setCellStyle(cs);
		cell.setCellValue(THIRD_COLUMN);
		
		cellNumber = 0;
		rowNumber = sheet.getLastRowNum();
		
	}

    @Override
    protected void fillHeader() {
        //No need to implement
    }

    @Override
    protected void fillFooter() {
        //No need to implement
    }

    protected void createDataForTable(){
		CellStyle cs = workBook.createCellStyle();
		cs.setAlignment(CellStyle.ALIGN_CENTER);
		cs.setWrapText(true);
		cs.setBorderBottom(CellStyle.BORDER_DOUBLE);
		cs.setBorderTop(CellStyle.BORDER_DOUBLE);
		cs.setBorderRight(CellStyle.BORDER_DOUBLE);
		cs.setBorderLeft(CellStyle.BORDER_DOUBLE);
		
		for (int i = 0; i < list.size(); i++) {
			Row row = sheet.createRow(sheet.getLastRowNum() + 1);
			Cell cell = row.createCell(cellNumber++);
			
			cell.setCellStyle(cs);
			cell.setCellValue(i + 1);
			switch (list.get(i).getLevel()) {
			case ERROR:
				cell = row.createCell(cellNumber);
				cell.setCellStyle(cs);
				cell.setCellValue("ошибка");
                cellNumber++;
				break;
			case WARNING:
				cell = row.createCell(cellNumber);
				cell.setCellStyle(cs);
				cell.setCellValue("предупреждение");
                cellNumber++;
				break;
			case INFO:
				cell = row.createCell(cellNumber);
				cell.setCellStyle(cs);
				cell.setCellValue("");
                cellNumber++;
				break;
			default:
				break;
			}
			cell = row.createCell(cellNumber++);
			cell.setCellStyle(cs);
			cell.setCellValue(list.get(i).getMessage());
			
			cellNumber = 0;
		}
	}



}
