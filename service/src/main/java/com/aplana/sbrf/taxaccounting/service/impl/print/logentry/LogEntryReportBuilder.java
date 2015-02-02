package com.aplana.sbrf.taxaccounting.service.impl.print.logentry;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
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

	
	public LogEntryReportBuilder(List<LogEntry> list){
        this.list = list;
		this.workBook = new XSSFWorkbook();
		this.sheet = workBook.createSheet("Учет налогов");
        this.sheet.setColumnWidth(2, cellWidthMin * 256 * 4);
	}
	
	@Override
    protected void createTableHeaders(){
		CellStyle cs = workBook.createCellStyle();
		cs.setAlignment(CellStyle.ALIGN_LEFT);
		cs.setBorderBottom(CellStyle.BORDER_THICK);
		cs.setBorderTop(CellStyle.BORDER_THICK);
		cs.setBorderRight(CellStyle.BORDER_THICK);
		cs.setBorderLeft(CellStyle.BORDER_THICK);
		cs.setFillForegroundColor(IndexedColors.GREEN.index);
		cs.setFillBackgroundColor(IndexedColors.GREEN.index);
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

    @Override
    protected void createDataForTable(){
        CellStyle csType = workBook.createCellStyle();
        csType.setAlignment(CellStyle.ALIGN_CENTER);
        csType.setBorderBottom(CellStyle.BORDER_DOUBLE);
        csType.setBorderTop(CellStyle.BORDER_DOUBLE);
        csType.setBorderRight(CellStyle.BORDER_DOUBLE);
        csType.setBorderLeft(CellStyle.BORDER_DOUBLE);

		CellStyle cs = workBook.createCellStyle();
		cs.setAlignment(CellStyle.ALIGN_LEFT);
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
				cell.setCellStyle(csType);
				cell.setCellValue("ошибка");
                cellNumber++;
				break;
			case WARNING:
				cell = row.createCell(cellNumber);
				cell.setCellStyle(csType);
				cell.setCellValue("предупреждение");
                cellNumber++;
				break;
			case INFO:
				cell = row.createCell(cellNumber);
				cell.setCellStyle(csType);
				cell.setCellValue("");
                fillWidth(cellNumber, cell.getStringCellValue().length());
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
