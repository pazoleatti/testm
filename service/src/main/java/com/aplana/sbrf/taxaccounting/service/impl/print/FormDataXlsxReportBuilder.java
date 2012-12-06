package com.aplana.sbrf.taxaccounting.service.impl.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
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
	}



	public String createReport() throws IOException{
		
		createTableHeaders();
		createDataForTable();
		return flush();
	}
	
	private String flush() throws IOException {
		File file = File.createTempFile("test", ".xlsx");
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
		Row row = sheet.createRow(rowNumber++);
		CellStyle cellStyle = workBook.createCellStyle();
		cellStyle.setFillBackgroundColor(HSSFColor.GREEN.index);
		cellStyle.setFillPattern(HSSFColor.BRIGHT_GREEN.index);
		for (Column el : data.getFormColumns()) {
			System.out.println("-------" + el.getName() + "-----" +el.getAlias() + "-----" + el.getOrder() + "-----" + el.getGroupName());
			aliasMap.put(cellNumber, el.getAlias());
			fillWidth(cellNumber,el.getName().length());
			Cell cell = row.createCell(cellNumber++);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(el.getName());
		}
		
		cellNumber = 0;
	}
	
	private void createDataForTable(){
		Row row = sheet.createRow(rowNumber++);
		for (DataRow dataRow : data.getDataRows()) {
			//System.out.println("----cell" + dataRow + "-----" + dataRow.getAlias());
			for (Map.Entry<Integer, String> alias : aliasMap.entrySet()) {
				Object obj = dataRow.get(alias.getValue());
				
				if(obj instanceof String){
					String str = (String)obj;
					row.createCell(alias.getKey()).setCellValue(str);
					fillWidth(alias.getKey(),str.length());
				}
				else if(obj instanceof Date){
					Date date = (Date)obj;
					CreationHelper createHelper = workBook.getCreationHelper();
					Cell cell = row.createCell(alias.getKey());
					CellStyle cellStyle = workBook.createCellStyle();
					cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(dateFormater));
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
		Integer l = widthCellsMap.get(cellNumber);
		if(l == null)
			widthCellsMap.put(cellNumber, length);
		else{
			if (l.compareTo(length) < 0 )
				widthCellsMap.put(cellNumber, length);
		}
	}

}
