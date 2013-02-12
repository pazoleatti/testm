package com.aplana.sbrf.taxaccounting.service.impl.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;


/**
 * 
 * @author avanteev
 */
public class FormDataXlsxReportBuilder {

	private static final int cellWidth = 10;
	
	private int rowNumber = 6;
	private int cellNumber = 0;
	private boolean isShowChecked;
	
	private String dateFormater = "dd.MM.yyyy";
	
	private Workbook workBook;
	private Sheet sheet;
	
	private CellStyleBuilder cellStyleBuilder;
	
	private enum CellType{
		DATE,
		DATE_TABLE,
		STRING,
		BIGDECIMAL,
		EMPTY,
		DEFAULT
	}
	
	private FormData data;
	private FormTemplate formTemplate;
	
	private Map<Integer, Integer> widthCellsMap = new HashMap<Integer, Integer>();
	private Map<Integer, String> aliasMap  = new HashMap<Integer, String>();

	private int skip = 0;
	
	private class CellStyleBuilder{
		public CellStyle cellStyle;
		
		private CellStyleBuilder(){
			cellStyle = workBook.createCellStyle();
			cellStyle.setFillForegroundColor(IndexedColors.GREEN.index);
			cellStyle.setFillBackgroundColor(IndexedColors.GREEN.index);
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			cellStyle.setWrapText(true);
			cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
			cellStyle.setBorderTop(CellStyle.BORDER_THIN);
			cellStyle.setBorderRight(CellStyle.BORDER_THIN);
			cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		}
		
		public CellStyle createCellStyle(CellType value){
			CellStyle cellStyle = workBook.createCellStyle();
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			
			switch (value) {
			case STRING:
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				cellStyle.setWrapText(true);
				cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cellStyle.setBorderTop(CellStyle.BORDER_THIN);
				cellStyle.setBorderRight(CellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
				
				break;
			case DATE: 
				cellStyle.setDataFormat(workBook.createDataFormat().getFormat(dateFormater));
				cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cellStyle.setBorderTop(CellStyle.BORDER_THIN);
				cellStyle.setBorderRight(CellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
				
			case DATE_TABLE:
				cellStyle.setDataFormat(workBook.createDataFormat().getFormat(dateFormater));
				
				
				break;
			case BIGDECIMAL:
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				cellStyle.setWrapText(true);
				cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cellStyle.setBorderTop(CellStyle.BORDER_THIN);
				cellStyle.setBorderRight(CellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
				
				break;
				
			case EMPTY:
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
				cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cellStyle.setBorderTop(CellStyle.BORDER_THIN);
				cellStyle.setBorderRight(CellStyle.BORDER_THIN);
				cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
				
				break;

			default:
			
				break;
			}
			
			return cellStyle;
		}
		
		public CellStyle createCellStyle(CellType value,FormStyle style){
			CellStyle cellStyle = createCellStyle(value);
			if(style != null){
				((XSSFCellStyle)cellStyle).setFillForegroundColor(new XSSFColor(new java.awt.Color(
						style.getBackColor().getRed(),
						style.getBackColor().getGreen(),
						style.getBackColor().getBlue()))
				);
				
				((XSSFCellStyle)cellStyle).setFillBackgroundColor(
						new XSSFColor(new java.awt.Color(
								style.getFontColor().getRed(),
								style.getFontColor().getGreen(),
								style.getFontColor().getBlue()))
						);
				cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}
			
			return cellStyle;
		}
	}
	
	public FormDataXlsxReportBuilder() {
		/*templeteInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(TEMPLATE);
		workBook = new SXSSFWorkbook(new XSSFWorkbook(templeteInputStream));*/
		
		workBook = new SXSSFWorkbook();
		sheet = workBook.createSheet("Учет налогов");
		
		cellStyleBuilder = new CellStyleBuilder();
		
		sheet.addMergedRegion(new CellRangeAddress(	0,0,0,5));
		
		sheet.setColumnWidth(0, 20 * 256);
		for(int i = 2;i < 5;i++){
			sheet.createRow(i);
		}
		sheet.getRow(2).createCell(0).setCellValue("Тип формы");
		sheet.getRow(3).createCell(0).setCellValue("Подразделение");
		sheet.getRow(4).createCell(0).setCellValue("Дата формирования");
		
		Cell cell = sheet.getRow(4).createCell(1);
		cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE_TABLE));
		cell.setCellValue(new Date(System.currentTimeMillis()));
		
	}
	
	public FormDataXlsxReportBuilder(FormData data,FormTemplate formTemplate, boolean isShowChecked) {
		this();
		this.data = data;
		this.formTemplate = formTemplate;
		this.isShowChecked = isShowChecked;
		
		Cell cell = sheet.createRow(0).createCell(0);
		cell.setCellValue(data.getFormType().getName());
		cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DEFAULT));
		sheet.getRow(2).createCell(1).setCellValue(data.getKind().getName());
		sheet.getRow(3).createCell(1).setCellValue(data.getDepartmentId());
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

		return file.getAbsolutePath();
	}
	
	private void createTableHeaders(){
		Row row = sheet.createRow(rowNumber);
		boolean isSecondTable = false;

		for (Column el : data.getFormColumns()) {
			if(el.getGroupName()!=null){
				isSecondTable = true;
				break;
			}
		}

		/*
		 * If we have two line for headers
		 */
		if(isSecondTable){
			Row row2 = sheet.createRow(rowNumber + 1);
			for(int i = 0;i<data.getFormColumns().size();i++){
				if(data.getFormColumns().get(i).getGroupName()==null){
					if(!isShowChecked && data.getFormColumns().get(i).isChecking()){
						skip++;
						continue;
					}
					aliasMap.put(cellNumber, data.getFormColumns().get(i).getAlias());
					fillWidth(cellNumber,data.getFormColumns().get(i).getWidth());
					Cell cell = row.createCell(cellNumber);
					cell.setCellStyle(cellStyleBuilder.cellStyle);
					cell.setCellValue(data.getFormColumns().get(i).getName());
					tableBorders(i - skip,i - skip, rowNumber, rowNumber + 1);
					cellNumber++;
				}
				else{
					int j;
					for(j = i + 1;j<data.getFormColumns().size();j++){
						if(data.getFormColumns().get(j).getGroupName() == null || !data.getFormColumns().get(j).getGroupName().
								equals(data.getFormColumns().get(i).getGroupName())){
							break;
						}
					}
					groupCells(i - skip,j - skip - 1,row,row2,i);
					i+=(j-i)-1;
				}
				
			}
		}
		else{
			for (Column el : data.getFormColumns()) {
				if(!isShowChecked && el.isChecking()){
					skip++;
					continue;
				}
				aliasMap.put(cellNumber, el.getAlias());
				Cell cell = row.createCell(cellNumber++);
				cell.setCellStyle(cellStyleBuilder.cellStyle);
				cell.setCellValue(el.getName());
			}
		}
		
		/*
		 * If we want to display number of rows
		 */
		if(formTemplate.isNumberedColumns()){
			Row row3 = sheet.createRow(rowNumber + 3);
			int k = 0;
			for(int i = 1;i<data.getFormColumns().size() + 1;i++){
				if(!isShowChecked && data.getFormColumns().get(i - 1).isChecking()){
					++k;
					continue;
				}
					
				Cell cell = row3.createCell(i - k - 1);
				cell.setCellValue(i - k);
				cell.setCellStyle(cellStyleBuilder.cellStyle);
			}
		}
		
		
		cellNumber = 0;
		rowNumber = sheet.getLastRowNum() + 1;
	}
	
	/*
	 * realNumber - the real number that we iterate over list of columns
	 * startCell - the cell in workbook to be filled
	 */
	private void groupCells(int startCell,int endCell,Row row1,Row row2, int realCell){
		if(!isShowChecked){
			int k = 0;
			for(int i = startCell;i<=endCell;i++){
				if(!data.getFormColumns().get((i - startCell) + realCell).isChecking()){
					//Because first checking is first cell for group name
					if(i == startCell){
						Cell cell = row1.createCell(startCell);
						cell.setCellStyle(cellStyleBuilder.cellStyle);
						cell.setCellValue(data.getFormColumns().get(realCell).getGroupName());
						fillWidth(startCell,data.getFormColumns().get(realCell).getWidth());
					}
					aliasMap.put(cellNumber, data.getFormColumns().get((i - startCell) + realCell).getAlias());
					//fillWidth(i,data.getFormColumns().get(i).getName().length());
					Cell cell1 = row2.createCell(cellNumber);
					cell1.setCellStyle(cellStyleBuilder.cellStyle);
					cell1.setCellValue(data.getFormColumns().get((i - startCell) + realCell).getName());
					cellNumber++;
				}
				else
					++k; // again to check whether the second part of the header field to be skipped 
				
			}
			if(startCell < endCell -k)
				tableBorders(startCell,endCell - k,rowNumber, rowNumber);
			//headerCellNumber = endCell - skip;
			skip  += k;
		}else{
			groupCells(startCell, endCell, row1, row2);
		}
		
		
	}
	
	private void groupCells(int startCell,int endCell,Row row1,Row row2){
		Cell cell = row1.createCell(startCell);
		cell.setCellStyle(cellStyleBuilder.cellStyle);
		cell.setCellValue(data.getFormColumns().get(startCell).getGroupName());
		fillWidth(startCell,data.getFormColumns().get(startCell).getWidth());
		for(int i = startCell;i<=endCell;i++){
			aliasMap.put(cellNumber, data.getFormColumns().get(i).getAlias());
			//fillWidth(i,data.getFormColumns().get(i).getName().length());
			Cell cell1 = row2.createCell(cellNumber);
			cell1.setCellStyle(cellStyleBuilder.cellStyle);
			cell1.setCellValue(data.getFormColumns().get(i).getName());
			cellNumber++;
		}
		tableBorders(startCell,endCell,rowNumber, rowNumber);
	}
	
	private void tableBorders(int startCell,int endCell, int startRow, int endRow){
		if(startCell == endCell && startRow == endRow)
			return;
		CellRangeAddress region = new CellRangeAddress(
				startRow, 
				endRow, 
				startCell, 
				endCell);
		
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
		sheet.addMergedRegion(region);
		
	}
	
	private void createDataForTable(){
		
		for (DataRow dataRow : data.getDataRows()) {
			Row row = sheet.createRow(rowNumber++);
			//System.out.println("----cell" + dataRow + "-----" + dataRow.getAlias());
			for (Map.Entry<Integer, String> alias : aliasMap.entrySet()) {
				Object obj = dataRow.get(alias.getValue());
				Cell cell = mergedDataCells(dataRow.getCell(alias.getValue()), row, alias.getKey());
				
				if(obj instanceof String){
					String str = (String)obj;
					
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.STRING,dataRow.getCell(alias.getValue()).getStyle()));
					cell.setCellValue(str);
					//fillWidth(alias.getKey(),str.length());
				}
				else if(obj instanceof Date){
					Date date = (Date)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE,dataRow.getCell(alias.getValue()).getStyle()));
					cell.setCellValue(date);
					//fillWidth(alias.getKey(),String.valueOf(date).length());
				}
				else if(obj instanceof BigDecimal){
					BigDecimal bd = (BigDecimal)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.BIGDECIMAL,dataRow.getCell(alias.getValue()).getStyle()));
					cell.setCellValue(bd.doubleValue());
					//fillWidth(alias.getKey(),String.valueOf(bd.doubleValue()).length());
				}
				else if(obj == null){
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.EMPTY,dataRow.getCell(alias.getValue()).getStyle()));
					cell.setCellValue("");
				}
			}
			
		}
		for (Map.Entry<Integer, Integer> cellWidth : widthCellsMap.entrySet()) {
			//System.out.println("----n" + cellWidth.getKey() + ":" + cellWidth.getValue());
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
	
	/*
	 * Merge rows with data. Depend on fields from com.aplana.sbrf.taxaccounting.model.Cell rowSpan and colSpan.
	 */
	private Cell mergedDataCells(com.aplana.sbrf.taxaccounting.model.Cell cell,Row currRow,int currColumn){
		Cell currCell = currRow.createCell(currColumn);
		if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
			CellRangeAddress region = null;
			if(currColumn + cell.getColSpan() > data.getFormColumns().size()){
				region = new CellRangeAddress(
						currRow.getRowNum(), 
						currRow.getRowNum() + cell.getRowSpan() - 1, 
						currColumn, 
						data.getFormColumns().size());
			}else if(currColumn + cell.getColSpan() > data.getFormColumns().size() - skip - 1){
				region = new CellRangeAddress(
						currRow.getRowNum(), 
						currRow.getRowNum() + cell.getRowSpan() - 1, 
						currColumn, 
						data.getFormColumns().size() - skip - 1);
			}
			else{
				region = new CellRangeAddress(
						currRow.getRowNum(), 
						currRow.getRowNum() + cell.getRowSpan() - 1, 
						currColumn, 
						currColumn + cell.getColSpan() - 1);
			}
			
			sheet.addMergedRegion(region);
		}
		
		
		return currCell;
	}

}
