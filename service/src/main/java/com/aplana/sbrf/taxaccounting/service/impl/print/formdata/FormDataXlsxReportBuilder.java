package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractXlsxReportBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author avanteev
 */
public class FormDataXlsxReportBuilder extends AbstractXlsxReportBuilder {

    static {
        fileName = "Налоговый_отчет_";
    }

	private static final Log logger = LogFactory.getLog(FormDataXlsxReportBuilder.class);
	
	private int rowNumber = 9;
	private int cellNumber = 0;
	private boolean isShowChecked;
	
	private static String dateFormater = "dd.MM.yyyy";
	
	private CellStyleBuilder cellStyleBuilder;
    private static final String TEMPLATE = ClassUtils
			.classPackageAsResourcePath(FormDataXlsxReportBuilder.class)
			+ "/acctax.xlsx";
	
	private enum CellType{
		DATE {
			@Override
			public CellStyle createCellStyle(CellStyle style) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderTop(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);
				
				return style;
			}
		},
		STRING {
			@Override
			public CellStyle createCellStyle(CellStyle style) {
				style.setAlignment(CellStyle.ALIGN_LEFT);
				style.setWrapText(true);
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderTop(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);
				
				return style;
			}
		},
		BIGDECIMAL {
			@Override
			public CellStyle createCellStyle(CellStyle style) {
				style.setAlignment(CellStyle.ALIGN_RIGHT);
				style.setWrapText(true);
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderTop(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);
				
				return style;
			}
		},
		EMPTY {
			@Override
			public CellStyle createCellStyle(CellStyle style) {
				style.setAlignment(CellStyle.ALIGN_CENTER);
				style.setBorderBottom(CellStyle.BORDER_THIN);
				style.setBorderTop(CellStyle.BORDER_THIN);
				style.setBorderRight(CellStyle.BORDER_THIN);
				style.setBorderLeft(CellStyle.BORDER_THIN);
				
				return style;
			}
		},
		DEFAULT {
			@Override
			public CellStyle createCellStyle(CellStyle style) {
				return null;
			}
		};
		
		public abstract CellStyle createCellStyle(CellStyle style);
	}
	
	private final class CellStyleBuilder{
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
		
		
		public CellStyle createCellStyle(CellType value, FormStyle formStyle,Column currColumn){
			DataFormat dataFormat = workBook.createDataFormat();
			CellStyle style = value.createCellStyle(workBook.createCellStyle());
			if(formStyle != null){
				((XSSFCellStyle)style).setFillForegroundColor(new XSSFColor(new java.awt.Color(
						formStyle.getBackColor().getRed(),
						formStyle.getBackColor().getGreen(),
						formStyle.getBackColor().getBlue()))
				);
				
				((XSSFCellStyle)style).setFillBackgroundColor(
						new XSSFColor(new java.awt.Color(
								formStyle.getFontColor().getRed(),
								formStyle.getFontColor().getGreen(),
								formStyle.getFontColor().getBlue()))
						);
				style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			}
			if(currColumn instanceof DateColumn){
				DateColumn dateCurrColumn = (DateColumn)currColumn;
				if(Formats.getById(dateCurrColumn.getFormatId()).getFormat().equals(""))
					style.setDataFormat(dataFormat.getFormat(dateFormater));
				else
					style.setDataFormat(dataFormat.getFormat(Formats.getById(dateCurrColumn.getFormatId()).getFormat()));
			}else if(currColumn instanceof NumericColumn){
				NumericColumn nc = (NumericColumn)currColumn;
				style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.Presision.getPresision(nc.getPrecision())));
			}
			
			return style;
		}
	}
	
	private FormData data;
	private FormTemplate formTemplate;
	private Department department;
	private ReportPeriod reportPeriod;
	private Date acceptanceDate;
	private Date creationDate;

	private Map<Integer, String> aliasMap  = new HashMap<Integer, String>();

	private int skip = 0;


	public FormDataXlsxReportBuilder() throws IOException {
        super();
        InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
		try {
			workBook = WorkbookFactory.create(templeteInputStream);
		} catch (InvalidFormatException e) {
			logger.error(e.getMessage(), e);
			throw new IOException("Wrong file format. Template must be in format of 2007 Excel!!!");
		}
		sheet = workBook.getSheet("Учет налогов");
		cellStyleBuilder = new CellStyleBuilder();

	}

	public FormDataXlsxReportBuilder(FormDataReport data, boolean isShowChecked) throws IOException {
		this();
		this.data = data.getData();
		formTemplate = data.getFormTemplate();
		this.isShowChecked = isShowChecked;
		department = data.getDepartment();
		reportPeriod = data.getReportPeriod();
		acceptanceDate = data.getAcceptanceDate();
		creationDate = data.getCreationDate();
	}

	protected void createTableHeaders(){
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
				fillWidth(cellNumber, el.getWidth());
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
						//fillWidth(startCell,data.getFormColumns().get(realCell).getWidth());
					}
					aliasMap.put(cellNumber, data.getFormColumns().get((i - startCell) + realCell).getAlias());
					fillWidth(i,data.getFormColumns().get(i).getName().length() / (endCell - startCell));
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
		//fillWidth(startCell,data.getFormColumns().get(startCell).getWidth());
		for(int i = startCell;i<=endCell;i++){
			aliasMap.put(cellNumber, data.getFormColumns().get(i).getAlias());
			fillWidth(i,data.getFormColumns().get(i).getName().length());
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

	protected void createDataForTable(){
		sheet.shiftRows(rowNumber + 1, sheet.getLastRowNum(), data.getDataRows().size() + 2);
		for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow : data.getDataRows()) {
			Row row = sheet.createRow(++rowNumber);
			//System.out.println("----cell" + dataRow + "-----" + dataRow.getAlias());
			for (Map.Entry<Integer, String> alias : aliasMap.entrySet()) {
				Object obj = dataRow.get(alias.getValue());
				Cell cell = mergedDataCells(dataRow.getCell(alias.getValue()), row, alias.getKey());

				if(obj instanceof String){
					String str = (String)obj;
					CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING,dataRow.getCell(alias.getValue()).getStyle(),
							dataRow.getCell(alias.getValue()).getColumn());
					cell.setCellStyle(cellStyle);
					cell.setCellValue(str);
					//fillWidth(alias.getKey(),str.length());
				}
				else if(obj instanceof Date){
					Date date = (Date)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE,dataRow.getCell(alias.getValue()).getStyle(),
							dataRow.getCell(alias.getValue()).getColumn()));
					cell.setCellValue(date);
					//fillWidth(alias.getKey(),String.valueOf(date).length());
				}
				else if(obj instanceof BigDecimal){
					BigDecimal bd = (BigDecimal)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.BIGDECIMAL,dataRow.getCell(alias.getValue()).getStyle(),
							dataRow.getCell(alias.getValue()).getColumn()));
					cell.setCellValue(bd.doubleValue());
					//fillWidth(alias.getKey(),String.valueOf(bd.doubleValue()).length());
				}
				else if(obj == null){
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.EMPTY,dataRow.getCell(alias.getValue()).getStyle(),
							dataRow.getCell(alias.getValue()).getColumn()));
					cell.setCellValue("");
				}
			}

		}

	}

	protected void fillHeader(){
		logger.debug(workBook.getName(XlsxReportMetadata.RANGE_DATE_CREATE).getRefersToFormula());
		StringBuilder sb;
		AreaReference ar;
		char[] arr;
		Date printDate;
		Row r;
		Cell c;

		//Fill subdivision
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_SUBDIVISION).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());
		sb.append(" ").append(department.getName());
		c.setCellValue(sb.toString());

		//Fill date
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_DATE_CREATE).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());

		if(data.getState() == WorkflowState.ACCEPTED && acceptanceDate!=null){
			printDate = acceptanceDate;
		} else {
			printDate = creationDate;
		}
		arr = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase().toCharArray();
		if(XlsxReportMetadata.sdf_m.format(printDate).toLowerCase().equals("март") ||
				XlsxReportMetadata.sdf_m.format(printDate).toLowerCase().equals("август"))
		{
			String month = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase() + "а";
			arr = month.toCharArray();
		} else {
			arr[arr.length - 1] = 'я';
		}
		sb.append(String.format(XlsxReportMetadata.DATE_CREATE, XlsxReportMetadata.sdf_d.format(printDate),
				new String(arr), XlsxReportMetadata.sdf_y.format(printDate)));
		c.setCellValue(sb.toString());

		//Fill report name
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_NAME).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		c.setCellValue(formTemplate.getFullName());

		//Fill code
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_CODE).getRefersToFormula());

		StringTokenizer sToK = new StringTokenizer(formTemplate.getCode(), XlsxReportMetadata.REPORT_DELIMITER);//This needed because we can have not only one delimiter
		int j = 0;
		while(sToK.hasMoreTokens()){
			r = sheet.getRow(ar.getFirstCell().getRow() + j);
			if(r == null)
				r = sheet.createRow(ar.getFirstCell().getRow() + j);
			c = r.getCell(ar.getFirstCell().getCol());
			if(c == null)
				c = r.createCell(ar.getFirstCell().getCol());

			if(j != 0){
				CellStyle style = workBook.createCellStyle();
				Font font = workBook.createFont();
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				style.setFont(font);
				c.setCellStyle(style);
			}
			c.setCellValue(sToK.nextToken());

			j++;
		}


		//Fill period
		ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_PERIOD).getRefersToFormula());
		r = sheet.getRow(ar.getFirstCell().getRow());
		c = r.getCell(ar.getFirstCell().getCol());
		sb = new StringBuilder(c.getStringCellValue());
		if(data.getFormType().getTaxType() == TaxType.TRANSPORT || data.getFormType().getTaxType() == TaxType.INCOME)
			sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, reportPeriod.getName()));
		c.setCellValue(sb.toString());
	}

	protected void fillFooter(){
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
			Cell crsP = rs.createCell(XlsxReportMetadata.CELL_POS);
			crsP.setCellValue(data.getSigners().get(i).getPosition());
			Cell crsS = rs.createCell(XlsxReportMetadata.CELL_SIGN);
			crsS.setCellValue("_______");
			Cell crsFio = rs.createCell(XlsxReportMetadata.CELL_FIO);
			crsFio.setCellValue("(" + data.getSigners().get(i).getName() + ")");
			crsP.setCellStyle(cs);
			crsS.setCellStyle(cs);
			crsFio.setCellStyle(cs);
			rowNumber++;
			sheet.shiftRows(rowNumber, sheet.getLastRowNum(), 1);
		}

		//Fill performer
		if(data.getPerformer()!=null){
			r = sheet.getRow(sheet.getLastRowNum());
			c = r.getCell(0);
			c.setCellValue(data.getPerformer().getName() + "/" + data.getPerformer().getPhone());
		}

		;

	}
	
	/*
	 * Merge rows with data. Depend on fields from com.aplana.sbrf.taxaccounting.model.Cell rowSpan and colSpan.
	 */
	private Cell mergedDataCells(com.aplana.sbrf.taxaccounting.model.Cell cell,Row currRow,int currColumn){
		Cell currCell = currRow.createCell(currColumn);
		if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
			CellRangeAddress region;
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
