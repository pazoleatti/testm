package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author avanteev
 */
public class FormDataXlsmReportBuilder extends AbstractReportBuilder {

    private final Log logger = LogFactory.getLog(getClass());

    private int rowNumber = 9;
    /*private int cellNumber = 0;*/
    private boolean isShowChecked;

    private CellStyleBuilder cellStyleBuilder;
    private static final String TEMPLATE = ClassUtils
			.classPackageAsResourcePath(FormDataXlsmReportBuilder.class)
			+ "/acctax.xlsm";

    private static final String FILE_NAME = "Налоговый_отчет_";
    private static final String POSTFIX = ".xlsm";
    private static final int MERGE_REGIONS_NUM_BACK = 10;

	private enum CellType{
		DATE,
		STRING,
		BIGDECIMAL,
		EMPTY ,
		DEFAULT
	}

    private final class CellStyleBuilder{

        private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

        private CellStyleBuilder() {
            for (Column column : formTemplate.getColumns()){
                this.createCellStyle(CellType.DEFAULT, column.getAlias() + "_header");
            }
        }

        public CellStyle createCellStyle(CellType value, String alias){
            if (cellStyleMap.containsKey(alias))
                return cellStyleMap.get(alias);
            DataFormat dataFormat = workBook.createDataFormat();
            Column currColumn;
            CellStyle style = workBook.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);

            switch (value){
                case DATE:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    currColumn = formTemplate.getColumn(alias);
                    if(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat().equals("")){
                        style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.sdf.toPattern()));
                    } else{
                        style.setDataFormat(dataFormat.getFormat(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat()));
                    }
                    break;
                case BIGDECIMAL:
                    currColumn = formTemplate.getColumn(alias);
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
                    style.setWrapText(true);
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
                    style.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.Presision.getPresision(((NumericColumn)currColumn).getPrecision())));
                    break;
                case STRING:
                    style.setAlignment(CellStyle.ALIGN_LEFT);
                    style.setWrapText(true);
                    break;
                case EMPTY:
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    break;
                case DEFAULT:
                    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                    style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
                    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    style.setAlignment(CellStyle.ALIGN_CENTER);
                    style.setWrapText(true);
                    style.setBorderBottom(CellStyle.BORDER_THICK);
                    style.setBorderTop(CellStyle.BORDER_THICK);
                    style.setBorderRight(CellStyle.BORDER_THICK);
                    style.setBorderLeft(CellStyle.BORDER_THICK);
                    break;
            }

            cellStyleMap.put(alias, style);
            return style;
        }
    }

	private FormData data;
    private RefBookValue refBookValue;
	private List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows;
	private FormTemplate formTemplate;
	private String departmentName;
	private ReportPeriod reportPeriod;
	private Date acceptanceDate;
	private Date creationDate;

    public FormDataXlsmReportBuilder() throws IOException {
        super(FILE_NAME, POSTFIX);
        InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
        try {
            workBook = WorkbookFactory.create(templeteInputStream);
        } catch (InvalidFormatException e) {
            logger.error(e.getMessage(), e);
            throw new IOException("Wrong file format. Template must be in format of 2007 Excel!!!");
        }
        sheet = workBook.getSheetAt(0);
	}

	public FormDataXlsmReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows, RefBookValue refBookValue)
            throws IOException {
		this();
		this.data = data.getData();
        this.dataRows = dataRows;
		formTemplate = data.getFormTemplate();
		this.isShowChecked = isShowChecked;
        departmentName = data.getDepartmentName();
		reportPeriod = data.getReportPeriod();
		acceptanceDate = data.getAcceptanceDate();
		creationDate = data.getCreationDate();
        this.refBookValue = refBookValue;
        cellStyleBuilder = new CellStyleBuilder();
	}

    protected void fillHeader(){

        int nullColumnCount = 0;
        int notNullColumn = formTemplate.getColumns().size() - 1;
        //Необходимо чтобы определять нулевые столбцы для Excel, по идее в конце должно делаться
        for (int i = formTemplate.getColumns().size() - 1, j = 0; i >= 0 && j <= 2; i-- ){
            if (formTemplate.getColumns().get(i).getWidth() == 0){
                sheet.setColumnWidth(i, 0);
                nullColumnCount++;
            } else {
                j++;
            }
        }

        //Опеределяет первый не нулевой столбец
        for (int i = 0; i <= formTemplate.getColumns().size() - 1 ; i++){
            if (formTemplate.getColumns().get(i).getWidth() != 0) {
                notNullColumn = i;
                break;
            }
        }

        //Fill subdivision
        createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION, departmentName, 0, 0);
        if (notNullColumn != 0) {
            AreaReference arDN = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_SUBDIVISION).getRefersToFormula());
            Row rDN = sheet.getRow(arDN.getFirstCell().getRow()) != null ? sheet.getRow(arDN.getFirstCell().getRow())
                    : sheet.createRow(arDN.getFirstCell().getRow());
            createNotHiddenCell(notNullColumn, rDN).setCellValue(rDN.getCell(0).getRichStringCellValue());
        }

        //Fill subdivision signature
        createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION_SIGN, null, 0, 0);

        //Fill date
        StringBuilder sb = new StringBuilder();

        /*1. Если статус налоговой формы "Утверждена", "Принята" - дата присвоения статуса "Утверждена".
        2. Если статус формы "Создана", "Подготовлена" - дата создания налоговой формы.*/
        char[] arr;
        Date printDate;
        boolean isTaxTypeDeal = data.getFormType().getTaxType().equals(TaxType.DEAL);
        if (isTaxTypeDeal) {
            printDate = ((data.getState() == WorkflowState.ACCEPTED || data.getState() == WorkflowState.APPROVED) && acceptanceDate!=null)
                    ? acceptanceDate :
                    ((data.getState() == WorkflowState.CREATED || data.getState() == WorkflowState.PREPARED) && creationDate!=null)
                            ? creationDate : new Date();

            arr = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase().toCharArray();
            if(XlsxReportMetadata.sdf_m.format(printDate).equalsIgnoreCase("март") ||
                    XlsxReportMetadata.sdf_m.format(printDate).equalsIgnoreCase("август"))
            {
                String month = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase() + "а";
                arr = month.toCharArray();
            } else {
                arr[arr.length - 1] = 'я';
            }
            sb.append(String.format(XlsxReportMetadata.DATE_CREATE, XlsxReportMetadata.sdf_d.format(printDate),
                    new String(arr), XlsxReportMetadata.sdf_y.format(printDate)));
        } else {
            sb.append(String.format(XlsxReportMetadata.DATE_CREATE, "__",
                    "______", "__"));
        }

        createCellByRange(XlsxReportMetadata.RANGE_DATE_CREATE, sb.toString(), 0, 0);
        sb.delete(0, sb.length());
        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_NAME).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow()) != null ? sheet.getRow(ar.getFirstCell().getRow())
                : sheet.createRow(ar.getFirstCell().getRow());
        CellStyle cellStyle = r.getCell(0).getCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER_SELECTION);
        cellStyle.setWrapText(true);
        for(int i = 1; i < formTemplate.getColumns().size(); i++) {
            r.createCell(i).setCellStyle(cellStyle);
        }
        r.setHeight((short) -1);

        //Fill report name
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_NAME, formTemplate.getFullName(), 0, notNullColumn);

        //Fill code
        AreaReference ar2 = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_CODE).getRefersToFormula());
        Row r2 = sheet.getRow(ar2.getFirstCell().getRow()) != null ? sheet.getRow(ar2.getFirstCell().getRow())
                : sheet.createRow(ar2.getFirstCell().getRow());
        int shiftCode = formTemplate.getColumns().size() - ar2.getFirstCell().getCol() - 2 - nullColumnCount;
        int countColumnsCode = 1;
        if (shiftCode < 0) {
            countColumnsCode = shiftCode >= -1 ? -shiftCode : 0;
            shiftCode = 0;
        }
        countColumnsCode += nullColumnCount;

        String code = formTemplate.getCode().replace(XlsxReportMetadata.REPORT_DELIMITER, '\n');
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_CODE, code, 0, shiftCode);
        for(int i = shiftCode; i <= shiftCode + countColumnsCode; i++) {
            createNotHiddenCell(ar2.getFirstCell().getCol() + i, r2).setCellStyle(cellStyle);
        }
        r2.setHeight((short) -1);

        //Fill period
        if (!refBookValue.getStringValue().equals("34"))
            sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, reportPeriod.getName(), String.valueOf(reportPeriod.getTaxPeriod().getYear())));
        else
            sb.append(String.format(XlsxReportMetadata.REPORT_PERIOD, "", String.valueOf(reportPeriod.getTaxPeriod().getYear())));
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_PERIOD, sb.toString(), 0, formTemplate.getColumns().size()/2);
    }

	protected void createTableHeaders(){
        //Поскольку имеется шаблон с выставленными алиасами, то чтобы не записать данные в ячейку с алиасом
        //делаем проверку на то, что сумма начала записи таблицы и кол-ва строк не превышает номер строки с алиасом
        //и если превышает,то сдвигаем
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
                Column column = formTemplate.getColumns().get(i);
                if ((column.isChecking() && !isShowChecked)){
                    continue;
                }
                HeaderCell headerCell = headerCellDataRow.getCell(column.getAlias());
                Cell workBookcell = mergedDataCells(headerCellDataRow.getCell(column.getAlias()), row, i, true);
                workBookcell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DEFAULT, column.getAlias() + "_header"));
                workBookcell.setCellValue(String.valueOf(headerCell.getValue()));
                if(headerCell.getColSpan() > 1){
                    i = i + headerCell.getColSpan() - 1;
                }
                /*cellNumber = row.getLastCellNum();*/
            }
            rowNumber++;
            /*cellNumber = 0;*/
        }
    }

	protected void createDataForTable(){
        rowNumber = (rowNumber > sheet.getLastRowNum()?sheet.getLastRowNum():rowNumber);//if we have empty strings
        sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
        for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow : dataRows) {
            Row row = sheet.getRow(rowNumber) != null?sheet.getRow(rowNumber++) : sheet.createRow(rowNumber++);

            for (int i = 0; i < formTemplate.getColumns().size(); i++) {
                Column column = formTemplate.getColumns().get(i);
                if (column.isChecking() && !isShowChecked) {
                    continue;
                }
                if (column.getWidth() == 0 && column.getAlias() != null) {
                    if (formTemplate.getColumns().size() == i + 1)
                        continue;
                    Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, i, false);
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue((String) dataRow.get(column.getAlias()));
                    if (dataRow.getCell(column.getAlias()).getColSpan() > 1)
                        i = i + dataRow.getCell(column.getAlias()).getColSpan() - 1;
                    continue;
                }
                Object obj = dataRow.get(column.getAlias());
                Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, i, false);
                if (column instanceof StringColumn) {
                    String str = (String) obj;
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(str);
                } else if (column instanceof DateColumn) {
                    Date date = (Date) obj;
                    if (date != null)
                        cell.setCellValue(date);
                    else
                        cell.setCellValue("");
                    cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.DATE, column.getAlias()));
                } else if (column instanceof NumericColumn) {
                    BigDecimal bd = (BigDecimal) obj;
                    cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.BIGDECIMAL, column.getAlias()));

                    cell.setCellValue(bd != null ? String.valueOf(bd) : "");
                } else if (column instanceof RefBookColumn || column instanceof ReferenceColumn) {
                    CellStyle cellStyle = cellStyleBuilder.createCellStyle(CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataRow.getCell(column.getAlias()).getRefBookDereference());
                } else if (obj == null) {
                    cell.setCellStyle(cellStyleBuilder.createCellStyle(CellType.EMPTY, column.getAlias()));
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
            widthCellsMap.put(i, formTemplate.getColumns().get(i).getWidth());
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

        int cellSignPosition = formTemplate.getColumns().size() / 2;
        for (int i = 0;i < data.getSigners().size(); i++) {
            Row rs = sheet.createRow(rowNumber);
            Cell crsP = createNotHiddenCell(XlsxReportMetadata.CELL_POS, rs);
            crsP.setCellValue(data.getSigners().get(i).getPosition());
            Cell crsS = createNotHiddenCell(cellSignPosition, rs);
            crsS.setCellValue("_______");
            Cell crsFio = createNotHiddenCell(cellSignPosition + 2, rs);
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
            String performer = "Исполнитель: " + (data.getPerformer().getName() != null ? data.getPerformer().getName() : "") + "/" +
                    (data.getPerformer().getPhone() != null ? data.getPerformer().getPhone() : "");
            c.setCellValue(performer);
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
        workBook.setPrintArea(0, 0, (!formTemplate.getHeaders().isEmpty()?formTemplate.getHeaders().get(0).size() - columnBreaks : 0) , 0,
                (dataRows != null ? dataRows.size() : 0) + data.getSigners().size() + formTemplate.getHeaders().size() + 15);
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
    private Cell mergedDataCells(AbstractCell cell,Row currRow, int currColumn, boolean isHeader){
        Cell currCell;
        if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
            if(currColumn + cell.getColSpan() > formTemplate.getColumns().size()){
                tableBorders(currColumn, formTemplate.getColumns().size(), currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }else if(currColumn + cell.getColSpan() > formTemplate.getColumns().size() - 1){
                tableBorders(currColumn, formTemplate.getColumns().size() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }
            else{
                tableBorders(currColumn, currColumn + cell.getColSpan() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }
            currCell = currRow.getCell(currColumn) != null ? currRow.getCell(currColumn) : currRow.createCell(currColumn);
        } else {
            currCell = currRow.createCell(currColumn);
        }
        return currCell;
    }

    /*
    * Create new merge region, or if we haven't intersections
    */
    private void tableBorders(int startCell,int endCell, int startRow, int endRow, boolean isHeader){
        if (sheet.getNumMergedRegions() > 0 && sheet.getNumMergedRegions() <= MERGE_REGIONS_NUM_BACK){
            for (int i = 0; i < sheet.getNumMergedRegions(); i++){
                CellRangeAddress cellRangeAddressTemp = sheet.getMergedRegion(i);
                if (cellRangeAddressTemp.isInRange(startRow, startCell) || cellRangeAddressTemp.isInRange(endRow, endCell))
                    return;
            }
        } else {
            for (int i = sheet.getNumMergedRegions() - MERGE_REGIONS_NUM_BACK; i < sheet.getNumMergedRegions() && i > 0; i++){
                CellRangeAddress cellRangeAddressTemp = sheet.getMergedRegion(i);
                if (cellRangeAddressTemp.isInRange(startRow, startCell) || cellRangeAddressTemp.isInRange(endRow, endCell))
                    return;
            }
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

    private void createCellByRange(String rangeName, String cellValue, int shiftRows, int shiftColumns){
        if (logger.isDebugEnabled())
            logger.debug(workBook.getName(rangeName).getRefersToFormula());
        XSSFRichTextString richTextString = new XSSFRichTextString();
        AreaReference ar = new AreaReference(workBook.getName(rangeName).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow() + shiftRows) != null ? sheet.getRow(ar.getFirstCell().getRow() + shiftRows)
                : sheet.createRow(ar.getFirstCell().getRow() + shiftRows);
        if (r.getCell(ar.getFirstCell().getCol()) != null &&
                r.getCell(ar.getFirstCell().getCol()).getStringCellValue()!= null &&
                !r.getCell(ar.getFirstCell().getCol()).getStringCellValue().isEmpty()){
            richTextString = (XSSFRichTextString) r.getCell(ar.getFirstCell().getCol()).getRichStringCellValue();
            r.getCell(ar.getFirstCell().getCol()).setCellValue("");//чтобы при печати не залипала перенесенная запись
        }
        Cell c = createNotHiddenCell(ar.getFirstCell().getCol() + shiftColumns, r);
        if (richTextString.numFormattingRuns() > 1){
            int richTextStart = richTextString.length() - 1;
            XSSFFont richTextIndex = richTextString.getFontAtIndex(richTextStart);
            richTextString.append(cellValue != null?cellValue:"");
            richTextString.applyFont(richTextStart,
                    richTextString.length(), richTextIndex);
        } else {
            richTextString.append(cellValue != null?cellValue:"");
            c.setCellStyle(r.getCell(ar.getFirstCell().getCol())!=null?r.getCell(ar.getFirstCell().getCol()).getCellStyle()
                : r.createCell(ar.getFirstCell().getCol()).getCellStyle());
        }
        c.setCellValue(richTextString);
    }


}
