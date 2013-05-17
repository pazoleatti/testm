package com.aplana.sbrf.taxaccounting.service.impl.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

public class LogEntryReportBuilder {
	
	private static String FIRST_COLUMN = "№ п/п";
	private static String SECOND_COLUMN = "Тип сообщения";
	private static String THIRD_COLUMN = "Текст сообщения";
	
	private List<LogEntry> list;
	
	private Workbook workBook;
	private Sheet sheet;
	private int rowNumber = 5;
	private int cellNumber = 0;
	private static final int cellWidth = 30;
	
	public LogEntryReportBuilder(List<LogEntry> list){
		this.list = list;
		this.workBook = new XSSFWorkbook();
		this.sheet = workBook.createSheet("Учет налогов");
	}
	
	public String createReport() throws IOException{
		createTableHeaders();
		createDataForTable();
		sheet.setColumnWidth(2, cellWidth * 256);
		return flush();
	}
	
	
	private String flush() throws IOException{
		File file = File.createTempFile("Список_ошибок_", ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workBook.write(out);

		return file.getAbsolutePath();
	}
	
	private void createTableHeaders(){
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
	
	private void createDataForTable(){
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
				cell = row.createCell(cellNumber++);
				cell.setCellStyle(cs);
				cell.setCellValue("ошибка");
				break;
			case WARNING:
				cell = row.createCell(cellNumber++);
				cell.setCellStyle(cs);
				cell.setCellValue("предупреждение");
				break;
			case INFO:
				cell = row.createCell(cellNumber++);
				cell.setCellStyle(cs);
				cell.setCellValue("");
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
