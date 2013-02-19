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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.util.ClassUtils;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataReport;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;


/**
 * 
 * @author avanteev
 */
public class FormDataXlsxReportBuilder {

	private static final int cellWidth = 10;
	
	private int rowNumber = 9;
	private int cellNumber = 0;
	private boolean isShowChecked;
	
	private String dateFormater = "dd.MM.yyyy";
	
	private Workbook workBook;
	private Sheet sheet;
	
	private CellStyleBuilder cellStyleBuilder;
	private InputStream templeteInputStream;
	private static String TEMPLATE = ClassUtils
			.classPackageAsResourcePath(FormDataXlsxReportBuilder.class)
			+ "/acctax.xlsx";
	
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
	private Department department;
	private ReportPeriod reportPeriod;
	
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
	
	public FormDataXlsxReportBuilder() throws IOException {
		templeteInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(TEMPLATE);
		try {
			workBook = WorkbookFactory.create(templeteInputStream);
		} catch (InvalidFormatException e) {
			throw new IOException("Wrong file format. Template must be in format of 2007 Excel!!!");
		}
		sheet = workBook.getSheet("Учет налогов");
		cellStyleBuilder = new CellStyleBuilder();
		
	}
	
	public FormDataXlsxReportBuilder(FormDataReport data, boolean isShowChecked) throws IOException {
		this();
		this.data = data.getData();
		this.formTemplate = data.getFormTemplate();
		this.isShowChecked = isShowChecked;
		this.department = data.getDepartment();
		this.reportPeriod = data.getReportPeriod();
	}



	public String createReport() throws IOException{
		fillHeader();
		createTableHeaders();
		createDataForTable();
		fillFooter();
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
			++rowNumber;
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
			Row row3 = sheet.createRow(++rowNumber);
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
		//rowNumber = sheet.getLastRowNum() + 1;
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
		sheet.shiftRows(rowNumber + 1, sheet.getLastRowNum(), data.getDataRows().size() + 2);
		for (DataRow dataRow : data.getDataRows()) {
			Row row = sheet.createRow(++rowNumber);
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
	
	private void fillHeader(){
		System.out.println(workBook.getName(XlsxReportMetadata.RANGE_DATE_CREATE).getRefersToFormula());
		StringBuilder sb;
		AreaReference ar;
		Row r;
		Cell c;
		
		//Fill subdivision
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_SUBDIVISION).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());
		sb.append(" " + department.getName());
		c.setCellValue(sb.toString());
		
		//Fill date
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_DATE_CREATE).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());
		
		if(data.getState() == WorkflowState.ACCEPTED && data.getAcceptanceDate()!=null){
			//Просто склонение
			char[] arr = XlsxReportMetadata.sdf_m.format(data.getAcceptanceDate()).toLowerCase().toCharArray();
			arr[arr.length - 1] = 'я';
			
			sb.append(String.format(XlsxReportMetadata.DATE_CREATE, XlsxReportMetadata.sdf_d.format(data.getAcceptanceDate()),
					new String(arr), 
					XlsxReportMetadata.sdf_y.format(data.getAcceptanceDate())));
		}
		else
			sb.append(String.format(XlsxReportMetadata.DATE_CREATE, "__", "_______", "__"));
		c.setCellValue(sb.toString());
		
		//Fill period
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_PERIOD).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());
		if(data.getFormType().getTaxType() == TaxType.TRANSPORT)
			sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, reportPeriod.getName()));
		else if(data.getFormType().getTaxType() == TaxType.INCOME)
			sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, reportPeriod.getName()));
		c.setCellValue(sb.toString());
	}
	
	private void fillFooter(){
		AreaReference ar;
		Row r;
		Cell c;
		
		//Fill position and FIO
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_POSITION).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		rowNumber = r.getRowNum();
		c = r.getCell(ar.getFirstCell().getCol());
		CellStyle cs = c.getCellStyle();

		for (int i = 0;i < data.getSigners().size(); i++) {
			Row rs = sheet.createRow(rowNumber);
			Cell crs_p = rs.createCell(XlsxReportMetadata.CELL_POS);
			crs_p.setCellValue(data.getSigners().get(i).getPosition());
			Cell crs_s = rs.createCell(XlsxReportMetadata.CELL_SIGN);
			crs_s.setCellValue("_______");
			Cell crs_fio = rs.createCell(XlsxReportMetadata.CELL_FIO);
			crs_fio.setCellValue(data.getSigners().get(i).getName());
			crs_p.setCellStyle(cs);
			crs_s.setCellStyle(cs);
			crs_fio.setCellStyle(cs);
			rowNumber++;
			sheet.shiftRows(rowNumber, sheet.getLastRowNum(), 1);
		}
		
		//Fill performer
		if(data.getPerformer()!=null){
			r = sheet.getRow(sheet.getLastRowNum());
			c = r.getCell(0);
			c.setCellValue(data.getPerformer().getName() + "/" + data.getPerformer().getPhone());
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
