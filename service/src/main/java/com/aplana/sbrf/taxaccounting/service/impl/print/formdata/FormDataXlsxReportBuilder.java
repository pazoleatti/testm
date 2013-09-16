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
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
			cellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			cellStyle.setWrapText(true);
			cellStyle.setBorderBottom(CellStyle.BORDER_THICK);
			cellStyle.setBorderTop(CellStyle.BORDER_THICK);
			cellStyle.setBorderRight(CellStyle.BORDER_THICK);
			cellStyle.setBorderLeft(CellStyle.BORDER_THICK);
		}


		public CellStyle createCellStyle(CellType value, int columnNumber, int rowNumber){
			DataFormat dataFormat = workBook.createDataFormat();
            Column currColumn = formTemplate.getColumns().get(columnNumber);
			CellStyle style = workBook.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);
            /*if (columnNumber == 0 && rowNumber < dataRows.size() - 1){
                style.setBorderRight(CellStyle.BORDER_THIN);
                style.setBorderLeft(CellStyle.BORDER_THICK);
                style.setBorderBottom(CellStyle.BORDER_THIN);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }else if(columnNumber == 0 && rowNumber == dataRows.size() - 1){
                style.setBorderRight(CellStyle.BORDER_THIN);
                style.setBorderLeft(CellStyle.BORDER_THICK);
                style.setBorderBottom(CellStyle.BORDER_THICK);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }else if(columnNumber == formTemplate.getColumns().size() - 1 && rowNumber < dataRows.size() - 1){
                style.setBorderRight(CellStyle.BORDER_THICK);
                style.setBorderLeft(CellStyle.BORDER_THIN);
                style.setBorderBottom(CellStyle.BORDER_THIN);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }else if(columnNumber == formTemplate.getColumns().size() - 1 && rowNumber == dataRows.size() - 1){
                style.setBorderRight(CellStyle.BORDER_THICK);
                style.setBorderLeft(CellStyle.BORDER_THIN);
                style.setBorderBottom(CellStyle.BORDER_THICK);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }else if (columnNumber < formTemplate.getColumns().size() - 1 && rowNumber == dataRows.size() - 1){
                style.setBorderRight(CellStyle.BORDER_THIN);
                style.setBorderLeft(CellStyle.BORDER_THIN);
                style.setBorderBottom(CellStyle.BORDER_THICK);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }else {
                style.setBorderRight(CellStyle.BORDER_THIN);
                style.setBorderLeft(CellStyle.BORDER_THIN);
                style.setBorderBottom(CellStyle.BORDER_THIN);
                style.setBorderTop(CellStyle.BORDER_THIN);
            }*/
			/*if(formStyle != null){
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
			}*/
            switch (value){
                case DATE:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    if(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat().equals("")){
                        style.setDataFormat(dataFormat.getFormat(dateFormater));
                    } else{
                        style.setDataFormat(dataFormat.getFormat(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat()));
                    }

                    break;
                case BIGDECIMAL:
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
                    style.setWrapText(true);
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
        InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
		try {
			workBook = WorkbookFactory.create(templeteInputStream);
		} catch (InvalidFormatException e) {
			logger.error(e.getMessage(), e);
			throw new IOException("Wrong file format. Template must be in format of 2007 Excel!!!");
		}
		sheet = workBook.getSheetAt(0);
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

    protected void fillHeader(){
        //Fill subdivision
        createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION, department.getName(), 0);

        //Fill subdivision signature
        createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION_SIGN, null, 0);

        //Fill date
        StringBuilder sb = new StringBuilder();

        /*1. Если статус налоговой формы "Утверждена", "Принята" - дата присвоения статуса "Утверждена".
        2. Если статус формы "Создана", "Подготовлена" - дата создания налоговой формы.*/
        char[] arr;
        Date printDate;
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
        createCellByRange(XlsxReportMetadata.RANGE_DATE_CREATE, sb.toString(), 0);
        sb.delete(0, sb.length());

        //Fill report name
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_NAME, formTemplate.getFullName(), 0);

        //Fill code
        StringTokenizer sToK = new StringTokenizer(formTemplate.getCode(), XlsxReportMetadata.REPORT_DELIMITER);//This needed because we can have not only one delimiter
        int j = 0;
        while(sToK.hasMoreTokens()){
            createCellByRange(XlsxReportMetadata.RANGE_REPORT_CODE, sToK.nextToken(), j);
            j++;
        }

        //Fill period
        if(data.getFormType().getTaxType() == TaxType.TRANSPORT || data.getFormType().getTaxType() == TaxType.INCOME)
            sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, reportPeriod.getName(), String.valueOf(reportPeriod.getYear())));
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_PERIOD, sb.toString(), 0);
    }

	protected void createTableHeaders(){
        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_POSITION).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow());
        if (rowNumber + formTemplate.getHeaders().size() >= r.getRowNum()){
            int rowBreakes = rowNumber + formTemplate.getHeaders().size() - r.getRowNum();
            if(0 == rowBreakes)
                sheet.shiftRows(r.getRowNum(), r.getRowNum() + 1, 1);
            else
                sheet.shiftRows(r.getRowNum(), r.getRowNum(), rowBreakes);
        }
        for (DataRow<HeaderCell> headerCellDataRow : formTemplate.getHeaders()){
            Row row = sheet.createRow(rowNumber);
            for (int i=0; i<formTemplate.getColumns().size(); i++){
                if ((formTemplate.getColumns().get(i).isChecking() && !isShowChecked)){
                    continue;
                }
                if (formTemplate.getColumns().get(i).getWidth() == 0){
                    mergedDataCells(headerCellDataRow.getCell(formTemplate.getColumns().get(i).getAlias()), row, true);//иначе следующая колонка тоже скрывается, т.к. нет lastCell
                    /*sheet.setColumnHidden(i, true);*/
                    continue;
                }
                HeaderCell headerCell = headerCellDataRow.getCell(formTemplate.getColumns().get(i).getAlias());
                Cell workBookcell = mergedDataCells(headerCellDataRow.getCell(formTemplate.getColumns().get(i).getAlias()), row, true);
                workBookcell.setCellStyle(cellStyleBuilder.cellStyle);
                workBookcell.setCellValue(String.valueOf(headerCell.getValue()));
                if(headerCell.getColSpan() > 1){
                    i = i + headerCell.getColSpan() - 1;
                }
                cellNumber = row.getLastCellNum();
            }
            rowNumber++;
            cellNumber = 0;
        }
	}

	protected void createDataForTable(){
        rowNumber = (rowNumber > sheet.getLastRowNum()?sheet.getLastRowNum():rowNumber);//if we have empty strings
        sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
		for (int j = 0; j < dataRows.size(); j++) {
            DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow = dataRows.get(j);
			Row row = sheet.createRow(rowNumber++);

			for (int i = 0; i < formTemplate.getColumns().size(); i++) {
                Column column = formTemplate.getColumns().get(i);
                if (column.isChecking() && !isShowChecked){
                    continue;
                }
                if (column.getWidth() == 0 && column.getAlias() != null){
                    Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, false);
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, i + 1, j);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue((String) dataRow.get(column.getAlias()));
                    if (dataRow.getCell(column.getAlias()).getColSpan() > 1)
                        i = i + dataRow.getCell(column.getAlias()).getColSpan() - 1;
                    continue;
                }
				Object obj = dataRow.get(column.getAlias());
				Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, false);
				if(column instanceof StringColumn){
					String str = (String)obj;
					CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, i , j);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(str);
					fillWidth(cell.getColumnIndex(),str != null?str.length():0);
				}
				else if(column instanceof DateColumn){
					Date date = (Date)obj;
                    if (date!=null)
                        cell.setCellValue(date);
                    else
                        cell.setCellValue("");
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE, i , j));
				}
				else if(column instanceof NumericColumn){
					BigDecimal bd = (BigDecimal)obj;
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.BIGDECIMAL, i , j));

					cell.setCellValue(bd!=null ? String.valueOf(bd) : "");
					fillWidth(cell.getColumnIndex(),String.valueOf(bd!=null?bd.doubleValue():"").length());
				}else if(column instanceof RefBookColumn){
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, i , j);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataRow.getCell(column.getAlias()).getRefBookDereference());
                    fillWidth(cell.getColumnIndex(), String.valueOf(dataRow.getCell(column.getAlias()) != null ?
                            dataRow.getCell(column.getAlias()).getRefBookDereference() :
                            "").length());
                }
				else if(obj == null){
					cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.EMPTY, i , j));
					cell.setCellValue("");
				}
                if (dataRow.getCell(column.getAlias()).getColSpan() > 1)
                    i = i + dataRow.getCell(column.getAlias()).getColSpan() - 1;
			}

		}

	}

    @Override
    protected void cellAlignment() {
        for (int i = 0; i < formTemplate.getColumns().size(); i++ ){
            if (formTemplate.getColumns().get(i).getWidth() == 0)
                widthCellsMap.put(i, 0);
        }
        super.cellAlignment();
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
			Cell crsP = createNotHiddenCell(XlsxReportMetadata.CELL_POS, rs);
			crsP.setCellValue(data.getSigners().get(i).getPosition());
            Cell crsS = createNotHiddenCell(XlsxReportMetadata.CELL_SIGN, rs);
			crsS.setCellValue("_______");
			Cell crsFio = createNotHiddenCell(XlsxReportMetadata.CELL_FIO, rs);
			crsFio.setCellValue("(" + data.getSigners().get(i).getName() + ")");
			crsP.setCellStyle(cs);
			crsS.setCellStyle(cs);
			crsFio.setCellStyle(cs);
			/*sheet.shiftRows(rowNumber, sheet.getLastRowNum(), 1);*/
            rowNumber++;
		}

		//Fill performer
		if(data.getPerformer()!=null){
			r = sheet.createRow(rowNumber);
			c = createNotHiddenCell(0, r);
            c.setCellValue("Исполнитель:");
            c = createNotHiddenCell(1, r);
			c.setCellValue((data.getPerformer().getName()!=null?data.getPerformer().getName():"") + "/" +
                    (data.getPerformer().getPhone()!=null?data.getPerformer().getPhone():""));
            sheet.shiftRows(sheet.getLastRowNum(), sheet.getLastRowNum(), 1);
		}

	}

    @Override
    protected void setPrintSetup() {
        int columnBreaks = 0;//необходимо чтобы определить область печати
        for (int i=0; i<formTemplate.getColumns().size(); i++){
            if (formTemplate.getColumns().get(i).isChecking() && !isShowChecked){
                columnBreaks++;
            }
        }
        workBook.setPrintArea(0, 0, formTemplate.getHeaders().get(0).size() - columnBreaks, 0,
                dataRows.size() + data.getSigners().size() + formTemplate.getHeaders().size() + 15);
        sheet.setFitToPage(true);
        sheet.setAutobreaks(true);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.getPrintSetup().setFitWidth((short) 1);
        /*sheet.getPrintSetup().setScale((short) 400);*/
        /*sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.A4_PAPERSIZE);*/
        /*((XSSFSheet)sheet).setRowBreak(2);*/
    }

    /*
    * Merge rows with data. Depend on fields from com.aplana.sbrf.taxaccounting.model.Cell rowSpan and colSpan.
    */
	private Cell mergedDataCells(AbstractCell cell,Row currRow, boolean isHeader){
        int currColumn = currRow.getLastCellNum()!=-1?currRow.getLastCellNum():0;
		Cell currCell = currRow.createCell(currColumn);
		if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
            if(currColumn + cell.getColSpan() > formTemplate.getColumns().size()){
                tableBorders(currColumn, formTemplate.getColumns().size(), currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }else if(currColumn + cell.getColSpan() > formTemplate.getColumns().size() - 1){
                tableBorders(currColumn, formTemplate.getColumns().size() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }
            else{
                tableBorders(currColumn, currColumn + cell.getColSpan() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }
		}
		return currCell;
	}

    /*
    * Create new merge region, or if we haven't intersections
    */
    private void tableBorders(int startCell,int endCell, int startRow, int endRow, boolean isHeader){
        for (int i = 0; i < sheet.getNumMergedRegions(); i++){
            CellRangeAddress cellRangeAddressTemp = sheet.getMergedRegion(i);
            if (cellRangeAddressTemp.isInRange(startRow, startCell) || cellRangeAddressTemp.isInRange(endCell, endRow))
                return;
        }
        CellRangeAddress region = new CellRangeAddress(
                startRow,
                endRow,
                startCell,
                endCell);
        if (isHeader){
            RegionUtil.setBorderBottom(CellStyle.BORDER_THICK, region, sheet, workBook);
            RegionUtil.setBorderTop(CellStyle.BORDER_THICK, region, sheet, workBook);
            RegionUtil.setBorderRight(CellStyle.BORDER_THICK, region, sheet, workBook);
            RegionUtil.setBorderLeft(CellStyle.BORDER_THICK, region, sheet, workBook);
        }else {
            RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
            RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
            RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
            RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
        }
        sheet.addMergedRegion(region);
    }

    private Cell createNotHiddenCell(int columnIndex, Row row){
        if (sheet.getColumnWidth(columnIndex) == 0)
            return createNotHiddenCell(columnIndex + 1, row);
        return row.getCell(columnIndex) != null ? row.getCell(columnIndex) :
                row.createCell(columnIndex);
    }

    private void createCellByRange(String rangeName, String cellValue, int shiftRows){
        logger.debug(workBook.getName(rangeName).getRefersToFormula());
        StringBuilder sb = new StringBuilder();
        AreaReference ar = new AreaReference(workBook.getName(rangeName).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow() + shiftRows) != null ? sheet.getRow(ar.getFirstCell().getRow() + shiftRows)
                : sheet.createRow(ar.getFirstCell().getRow() + shiftRows);
        if (r.getCell(ar.getFirstCell().getCol()) != null &&
                r.getCell(ar.getFirstCell().getCol()).getStringCellValue()!= null &&
                !r.getCell(ar.getFirstCell().getCol()).getStringCellValue().isEmpty()){
            sb.append(r.getCell(ar.getFirstCell().getCol()).getStringCellValue());
            r.getCell(ar.getFirstCell().getCol()).setCellValue("");//чтобы при печати не залипала перенесенная запись
        }
        Cell c = createNotHiddenCell(ar.getFirstCell().getCol(), r);
        sb.append(cellValue != null?cellValue:"");
        c.setCellValue(String.valueOf(sb));
        c.setCellStyle(r.getCell(ar.getFirstCell().getCol()).getCellStyle());
    }


}
