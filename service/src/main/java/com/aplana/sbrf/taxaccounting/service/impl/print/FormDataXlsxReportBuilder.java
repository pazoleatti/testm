package com.aplana.sbrf.taxaccounting.service.impl.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ClassUtils;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataPrintingService;

public class FormDataXlsxReportBuilder {
	
	private static String TEMPLATE = ClassUtils
			.classPackageAsResourcePath(FormDataPrintingService.class)
			+ "/acctax.xlsx";
	private static final int cellWidth = 10;
	
	private int rowNumber = 6;
	private int cellNumber = 0;
	
	private String dateFormater = "dd.MM.yyyy";
	private InputStream templeteInputStream;
	
	private Workbook workBook;
	private Sheet sheet;
	
	private FormData data;
	
	private Map<Integer, Integer> widthCellsMap = new HashMap<Integer, Integer>();
	private Map<Integer, String> aliasMap  = new HashMap<Integer, String>();
	
	
	public FormDataXlsxReportBuilder() throws IOException {
		InputStream templeteInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(TEMPLATE);
		workBook = new SXSSFWorkbook(new XSSFWorkbook(templeteInputStream));
		sheet = workBook.getSheet("List1");
	}
	
	public FormDataXlsxReportBuilder(FormData data) throws IOException {
		this.data = data;
		
		templeteInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(TEMPLATE);
		workBook = new SXSSFWorkbook(new XSSFWorkbook(templeteInputStream));
		sheet = workBook.getSheet("List1");
		//System.out.println("----sheet " + sheet + "------------- строка" + sheet.createRow(0).createCell(0));
		sheet.createRow(0).createCell(0).setCellValue(data.getFormType().getName());
		
		sheet.createRow(2).createCell(1).setCellValue(data.getKind().getName());
		sheet.createRow(3).createCell(1).setCellValue(data.getDepartmentId());
		
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setDataFormat(workBook.createDataFormat().getFormat(dateFormater));
		Cell cell = sheet.createRow(4).createCell(1);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(new Date(System.currentTimeMillis()));
	}



	public String createReport() throws IOException{
		
		createTableHeaders();
		createDataForTable();
		return flush();
	}
	
	private String flush() throws IOException {
		File file = File.createTempFile("Налоговый отчет_", ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workBook.setPrintArea(0, 0, aliasMap.size(), 0, data.getDataRows().size());
		workBook.write(out);
		try {
			if (templeteInputStream != null) {
				templeteInputStream.close();
			}
		} catch (Exception e) {
			// nothing
		}
		System.out.println("----" + file.getAbsolutePath());
		return file.getAbsolutePath();
	}
	
	private void createTableHeaders(){
		Row row = sheet.createRow(rowNumber);
		
		CellStyle cellStyle = workBook.createCellStyle();
		boolean isSecondTable = false;
		
		cellStyle.setFillBackgroundColor(HSSFColor.BRIGHT_GREEN.index);
		cellStyle.setFillPattern(HSSFColor.BRIGHT_GREEN.index);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setWrapText(true);

		for (Column el : data.getFormColumns()) {
			if(el.getGroupName()!=null){
				isSecondTable = true;
				break;
			}
		}
		//System.out.println("Размер таблицы " + data.getFormColumns().size());
		/*for (Column cell : data.getFormColumns()) {
			System.out.println(cell.getName());
		}*/
		if(isSecondTable){
			Row row2 = sheet.createRow(rowNumber + 1);
			for(int i = 0;i<data.getFormColumns().size();i++){
				if(data.getFormColumns().get(i).getGroupName()==null){
					
					aliasMap.put(i, data.getFormColumns().get(i).getAlias());
					//fillWidth(i,data.getFormColumns().get(i).getName().length());
					Cell cell = row.createCell(i);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(data.getFormColumns().get(i).getName());
					sheet.addMergedRegion(new CellRangeAddress(
							rowNumber, 
							rowNumber + 1, 
							i, 
							i));
					
				}
				else{
					int j;
					for(j = i + 1;j<data.getFormColumns().size();j++){
						/*System.out.println("-------" + data.getFormColumns().get(j).getName() + "-----" +data.getFormColumns().get(j).getAlias() + "-----" + 
								data.getFormColumns().get(j).getOrder() + "-----" + data.getFormColumns().get(j).getGroupName());*/
						if(data.getFormColumns().get(j).getGroupName() == null || !data.getFormColumns().get(j).getGroupName().
								equals(data.getFormColumns().get(i).getGroupName())){
							break;
						}
					}
					groupCells(i,j - 1,row,row2,cellStyle);
					i+=(j-i)-1;
				}
				
			}
		}
		else{
			for (Column el : data.getFormColumns()) {
				//System.out.println("-------" + el.getName() + "-----" +el.getAlias() + "-----" + el.getOrder() + "-----" + el.getGroupName());
				aliasMap.put(cellNumber, el.getAlias());
				Cell cell = row.createCell(cellNumber++);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(el.getName());
			}
		}
		
		cellNumber = 0;
		rowNumber = sheet.getLastRowNum() + 1;
	}
	
	private void groupCells(int startCell,int endCell,Row row1,Row row2,CellStyle style){
		
		Cell cell = row1.createCell(startCell);
		cell.setCellStyle(style);
		cell.setCellValue(data.getFormColumns().get(startCell).getGroupName());
		
		for(int i = startCell;i<=endCell;i++){
			aliasMap.put(i, data.getFormColumns().get(i).getAlias());
			//fillWidth(i,data.getFormColumns().get(i).getName().length());
			cell = row2.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(data.getFormColumns().get(i).getName());
			//System.out.println(cell.getStringCellValue());
		}
		sheet.addMergedRegion(new CellRangeAddress(
				rowNumber, 
				rowNumber, 
				startCell, 
				endCell));
	}
	
	private void createDataForTable(){
		
		for (DataRow dataRow : data.getDataRows()) {
			Row row = sheet.createRow(rowNumber++);
			System.out.println("----cell" + dataRow + "-----" + dataRow.getAlias());
			for (Map.Entry<Integer, String> alias : aliasMap.entrySet()) {
				Object obj = dataRow.get(alias.getValue());
				
				if(obj instanceof String){
					String str = (String)obj;
					row.createCell(alias.getKey()).setCellValue(str);
					fillWidth(alias.getKey(),str.length());
				}
				else if(obj instanceof Date){
					Date date = (Date)obj;
					Cell cell = row.createCell(alias.getKey());
					CellStyle cellStyle = workBook.createCellStyle();
					cellStyle.setDataFormat(workBook.createDataFormat().getFormat(dateFormater));
					cell.setCellStyle(cellStyle);
					cell.setCellValue(date);
					fillWidth(alias.getKey(),String.valueOf(date).length());
				}
				else if(obj instanceof BigDecimal){
					BigDecimal bd = (BigDecimal)obj;
					//System.out.println("BigDecimal" + bd.doubleValue());
					row.createCell(alias.getKey()).setCellValue(bd.doubleValue());
					fillWidth(alias.getKey(),String.valueOf(bd.doubleValue()).length());
				}
			}
			
		}
		for (Map.Entry<Integer, Integer> cellWidth : widthCellsMap.entrySet()) {
			System.out.println("----n" + cellWidth.getKey() + ":" + cellWidth.getValue());
			sheet.setColumnWidth(cellWidth.getKey(), cellWidth.getValue().intValue()*256);
		}
	}
	
	/*
	 * Необходимо чтобы знать какой конечный размер ячеек установить. Делается только в самом конце.
	 */
	private void fillWidth(Integer cellNumber,Integer length){
		if(widthCellsMap.get(cellNumber) == null && length >= cellWidth)
			widthCellsMap.put(cellNumber, length);
		else if(widthCellsMap.get(cellNumber) != null){
			if (widthCellsMap.get(cellNumber).compareTo(length) < 0 )
				widthCellsMap.put(cellNumber, length);
		}
	}

}
