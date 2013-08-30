package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
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
import java.util.List;
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
		DATE,
		STRING,
		BIGDECIMAL,
		EMPTY ,
		DEFAULT
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
			CellStyle style = workBook.createCellStyle();
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
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
            switch (value){
                case DATE:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    if(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat().equals("")){
                        style.setDataFormat(dataFormat.getFormat(dateFormater));
                    } else{
                        System.out.println("dataFormat: " + Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat());
                        style.setDataFormat(dataFormat.getFormat(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat()));
                    }

                    break;
                case BIGDECIMAL:
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
                    style.setWrapText(true);
                    System.out.println("numericFormat precision: " + ((NumericColumn)currColumn).getPrecision() + ", format: " +
                            XlsxReportMetadata.Presision.getPresision(((NumericColumn)currColumn).getPrecision()));
                    style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.Presision.getPresision(((NumericColumn)currColumn).getPrecision())));
                    break;
                case STRING:
                    style.setAlignment(CellStyle.ALIGN_LEFT);
                    style.setWrapText(true);
                    break;
                case EMPTY:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    break;
                default:
                    break;
            }

			return style;
		}
	}

	private FormData data;
	private List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows;
	private FormTemplate formTemplate;
	private Department department;
	private ReportPeriod reportPeriod;
	private Date acceptanceDate;
	private Date creationDate;

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

	public FormDataXlsxReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows) throws IOException {
		this();
		this.data = data.getData();
        this.dataRows = dataRows;
		formTemplate = data.getFormTemplate();
		this.isShowChecked = isShowChecked;
		department = data.getDepartment();
		reportPeriod = data.getReportPeriod();
		acceptanceDate = data.getAcceptanceDate();
		creationDate = data.getCreationDate();
	}

	protected void createTableHeaders(){

        for (DataRow<HeaderCell> headerCellDataRow : formTemplate.getHeaders()){
            Row row = sheet.createRow(rowNumber);
            for (int i=0; i<formTemplate.getColumns().size(); i++){
                if ((formTemplate.getColumns().get(i).isChecking() && !isShowChecked) || formTemplate.getColumns().get(i).getWidth() == 0){
                    continue;
                }
                HeaderCell headerCell = headerCellDataRow.getCell(formTemplate.getColumns().get(i).getAlias());
                Cell workBookcell = mergedDataCells(headerCellDataRow.getCell(formTemplate.getColumns().get(i).getAlias()), row);
                workBookcell.setCellStyle(cellStyleBuilder.cellStyle);
                workBookcell.setCellValue(headerCell.getValue().toString());
                if(headerCell.getColSpan() > 1){
                    i = i + headerCell.getColSpan() -1;
                }
                cellNumber = row.getLastCellNum();
            }
            rowNumber++;
            cellNumber = 0;
        }
	}

    /*
     * Create new merge region, or if we have intersections return null
     */
	private CellRangeAddress tableBorders(int startCell,int endCell, int startRow, int endRow){
        for (int i = 0; i < sheet.getNumMergedRegions(); i++){
            CellRangeAddress cellRangeAddressTemp = sheet.getMergedRegion(i);
            if (cellRangeAddressTemp.isInRange(startRow, startCell) || cellRangeAddressTemp.isInRange(endCell, endCell))
                return null;
        }
		CellRangeAddress region = new CellRangeAddress(
				startRow,
				endRow,
				startCell,
				endCell);
        RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
        return region;
    }

	protected void createDataForTable(){
        rowNumber = (rowNumber > sheet.getLastRowNum()?sheet.getLastRowNum():rowNumber);//if we have empty strings
        sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
		for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow : dataRows) {
			Row row = sheet.createRow(rowNumber++);
            String zeroColumnAlias = "empty";
			for (Column column : formTemplate.getColumns()) {
                if (column.isChecking() && !isShowChecked){
                    continue;
                }
                if (column.getWidth() == 0){
                    if (dataRow.get(column.getAlias()) != null)
                        zeroColumnAlias = column.getAlias();
                    continue;
                }else if (!zeroColumnAlias.equals("empty")){
                    Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row);
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING,dataRow.getCell(zeroColumnAlias).getStyle(),
                            column);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue((String) dataRow.get(zeroColumnAlias));
                    zeroColumnAlias = "empty";
                    continue;
                }
				Object obj = dataRow.get(column.getAlias());
				Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row);
				if(column instanceof StringColumn){
					String str = (String)obj;
					CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING,dataRow.getCell(column.getAlias()).getStyle(),
                            column);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(str);
					fillWidth(cell.getColumnIndex(),str != null?str.length():0);
				}
				else if(column instanceof DateColumn){
					Date date = (Date)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE,dataRow.getCell(column.getAlias()).getStyle(),
                            column));
                    if (date!=null)
					    cell.setCellValue(date);
                    else
                        cell.setCellValue("");
				}
				else if(column instanceof NumericColumn){
					BigDecimal bd = (BigDecimal)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.BIGDECIMAL,dataRow.getCell(column.getAlias()).getStyle(),
                            column));

                    System.out.println("bd value: " + bd);
					cell.setCellValue(bd!=null? bd.toString() :"");
					fillWidth(cell.getColumnIndex(),String.valueOf(bd!=null?bd.doubleValue():"").length());
				}else if(column instanceof RefBookColumn){
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING,dataRow.getCell(column.getAlias()).getStyle(),
                            dataRow.getCell(column.getAlias()).getColumn());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataRow.getCell(column.getAlias()).getRefBookDereference());
                    fillWidth(cell.getColumnIndex(), String.valueOf(dataRow.getCell(column.getAlias()) != null ?
                            dataRow.getCell(column.getAlias()).getRefBookDereference() :
                            "").length());
                }
				else if(obj == null){
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.EMPTY,dataRow.getCell(column.getAlias()).getStyle(),
                            column));
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

        /*1. Если статус налоговой формы "Утверждена", "Принята" - дата присвоения статуса "Утверждена".
        2. Если статус формы "Создана", "Подготовлена" - дата создания налоговой формы.*/
        printDate = ((data.getState() == WorkflowState.ACCEPTED || data.getState() == WorkflowState.APPROVED) && acceptanceDate!=null)
                ? acceptanceDate :
                ((data.getState() == WorkflowState.CREATED || data.getState() == WorkflowState.PREPARED) && creationDate!=null)
                        ? creationDate : null;

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
			c = r.createCell(0);
            c.setCellValue("Исполнитель:");
            c = r.createCell(1);
			c.setCellValue((data.getPerformer().getName()!=null?data.getPerformer().getName():"") + "/" +
                    (data.getPerformer().getPhone()!=null?data.getPerformer().getPhone():""));
		}

	}

	/*
	 * Merge rows with data. Depend on fields from com.aplana.sbrf.taxaccounting.model.Cell rowSpan and colSpan.
	 */
	private Cell mergedDataCells(AbstractCell cell,Row currRow){
        int currColumn = currRow.getLastCellNum()!=-1?currRow.getLastCellNum():0;
		Cell currCell = currRow.createCell(currColumn);
		if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
			CellRangeAddress region;
			if(currColumn + cell.getColSpan() > formTemplate.getColumns().size()){
                region = tableBorders(currColumn, formTemplate.getColumns().size(), currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1);
			}else if(currColumn + cell.getColSpan() > formTemplate.getColumns().size() - 1){
                region = tableBorders(currColumn, formTemplate.getColumns().size() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1);
			}
			else{
                region = tableBorders(currColumn, currColumn + cell.getColSpan() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1);
			}
            if (region!= null)
			    sheet.addMergedRegion(region);
		}
		return currCell;
	}

}
