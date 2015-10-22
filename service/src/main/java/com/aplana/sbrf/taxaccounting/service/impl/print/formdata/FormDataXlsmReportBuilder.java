package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
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

    private static final Log LOG = LogFactory.getLog(FormDataXlsmReportBuilder.class);

    private int rowNumber = 9;

    private boolean isShowChecked;

    private CellStyleBuilder cellStyleBuilder;
    private static final String TEMPLATE = ClassUtils
			.classPackageAsResourcePath(FormDataXlsmReportBuilder.class)
			+ "/acctax.xlsm";

    private static final int MERGE_REGIONS_NUM_BACK = 10;

	private enum CellType{
		DATE,
		STRING,
		BIGDECIMAL,
        NUMERATION,
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

        /**
         * Получить стриль для ячейки excel'я.
         *
         * @param value тип ячейки (дата, число, строка, ...)
         * @param alias алиас столбца
         */
        public CellStyle createCellStyle(CellType value, String alias) {
            return createCellStyle(value, alias, null);
        }

        /**
         * Получить стриль для ячейки excel'я.
         *
         * @param value тип ячейки (дата, число, строка, ...)
         * @param alias алиас столбца
         * @param subKey дополнительное значение для ключа (что бы получить стили дельт)
         */
        public CellStyle createCellStyle(CellType value, String alias, String subKey){
            String key = alias + (subKey != null && !subKey.isEmpty() ? subKey : "");
            if (cellStyleMap.containsKey(key))
                return cellStyleMap.get(key);
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
                case NUMERATION:
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
                    style.setWrapText(true);
                    style.setAlignment(CellStyle.ALIGN_RIGHT);
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

            cellStyleMap.put(key, style);
            return style;
        }
    }

	private FormData data;
    private RefBookValue refBookValue;
	private List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows;
	private FormTemplate formTemplate;
	private ReportPeriod reportPeriod,rpCompare;
	private Date acceptanceDate;
	private Date creationDate;

    private Map<String, XSSFFont> fontMap = new HashMap<String, XSSFFont>();

    public FormDataXlsmReportBuilder() throws IOException {
        super("report", ".xlsm");
        InputStream templeteInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
        try {
            workBook = WorkbookFactory.create(templeteInputStream);
        } catch (InvalidFormatException e) {
            LOG.error(e.getMessage(), e);
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
		reportPeriod = data.getReportPeriod();
		acceptanceDate = data.getAcceptanceDate();
		creationDate = data.getCreationDate();
        this.refBookValue = refBookValue;
        cellStyleBuilder = new CellStyleBuilder();
        this.rpCompare = data.getRpCompare();
        if (!isShowChecked) {
            Iterator<Column> iterator = data.getFormTemplate().getColumns().iterator();
            while (iterator.hasNext()) {
                Column c = iterator.next();
                if (c.isChecking()) {
                    for(DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow: this.dataRows) {
                        dataRow.removeColumn(c);
                    }
                    for(DataRow<HeaderCell> header: this.data.getHeaders()) {
                        header.removeColumn(c);
                    }
                    iterator.remove();
                }
            }
        }
	}

    @Override
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
        AreaReference arSubdivision = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_SUBDIVISION).getRefersToFormula());
        CellRangeAddress region = new CellRangeAddress(arSubdivision.getFirstCell().getRow(), arSubdivision.getFirstCell().getRow(), notNullColumn, formTemplate.getColumns().size() - 1);
        sheet.addMergedRegion(region);
        if (data.getPerformer() != null) {
            createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION,  data.getPerformer().getReportDepartmentName(), 0, notNullColumn);
        } else {
            createCellByRange(XlsxReportMetadata.RANGE_SUBDIVISION,  "", 0, notNullColumn);
        }

        //Fill date
        StringBuilder sb = new StringBuilder();

        /*1. Если статус налоговой формы "Утверждена", "Принята" - дата присвоения статуса "Утверждена".
        2. Если статус формы "Создана", "Подготовлена" - дата создания налоговой формы.*/
        char[] arr;
        Date printDate;
        printDate = ((data.getState() == WorkflowState.ACCEPTED || data.getState() == WorkflowState.APPROVED) && acceptanceDate != null)
                ? acceptanceDate :
                ((data.getState() == WorkflowState.CREATED || data.getState() == WorkflowState.PREPARED) && creationDate != null)
                        ? creationDate : new Date();

        arr = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase().toCharArray();
        if (XlsxReportMetadata.sdf_m.format(printDate).equalsIgnoreCase("март") ||
                XlsxReportMetadata.sdf_m.format(printDate).equalsIgnoreCase("август")) {
            String month = XlsxReportMetadata.sdf_m.format(printDate).toLowerCase() + "а";
            arr = month.toCharArray();
        } else {
            arr[arr.length - 1] = 'я';
        }
        sb.append(String.format(XlsxReportMetadata.DATE_CREATE, XlsxReportMetadata.sdf_d.format(printDate),
                new String(arr), XlsxReportMetadata.sdf_y.format(printDate)));

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

        String code = (formTemplate.getHeader() != null) ? formTemplate.getHeader().replace(XlsxReportMetadata.REPORT_DELIMITER, '\n') : "";
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_CODE, code, 0, shiftCode);
        for(int i = shiftCode; i <= shiftCode + countColumnsCode; i++) {
            createNotHiddenCell(ar2.getFirstCell().getCol() + i, r2).setCellStyle(cellStyle);
        }
        r2.setHeight((short) -1);

        StringBuilder sbPeriodName = new StringBuilder();
        //Fill period
        if(data.getComparativePeriodId() != null){
            /*String rpName =  !refBookValue.getStringValue().equals("34") ? reportPeriod.getName() : "";
            String rpCompareName =  !refBookValue.getStringValue().equals("34") ? rpCompare.getName() : "";*/
            sbPeriodName.append(String.format(
                    XlsxReportMetadata.REPORT_PERIOD,
                    rpCompare.getName(),
                    rpCompare.getTaxPeriod().getYear(),
                    reportPeriod.getName(),
                    reportPeriod.getTaxPeriod().getYear(),
                    data.isAccruing() ? "(нарастающим итогом)" : ""));
        } else  if (data.getPeriodOrder() != null) {
            sbPeriodName.append(
                    String.format(XlsxReportMetadata.MONTHLY,
                            Months.fromId(data.getPeriodOrder()).getTitle().toLowerCase(new Locale("ru", "RU")),
                            reportPeriod.getTaxPeriod().getYear()
                    )
            );
        } else {
            sbPeriodName.append(
                    String.format(XlsxReportMetadata.MONTHLY,
                            !refBookValue.getStringValue().equals("34") ? reportPeriod.getName() : "",
                            reportPeriod.getTaxPeriod().getYear()
                    )
            );
        }
        sb.append(sbPeriodName.toString());
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_PERIOD, sb.toString(), 0, formTemplate.getColumns().size()/2);
    }

	@Override
    protected void createTableHeaders(){
        //Поскольку имеется шаблон с выставленными алиасами, то чтобы не записать данные в ячейку с алиасом
        //делаем проверку на то, что сумма начала записи таблицы и кол-ва строк не превышает номер строки с алиасом
        //и если превышает,то сдвигаем
        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_POSITION).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow());
        if (rowNumber + data.getHeaders().size() >= r.getRowNum()){
            int rowBreakes = rowNumber + data.getHeaders().size() - r.getRowNum();
            if(0 == rowBreakes)
                sheet.shiftRows(r.getRowNum(), r.getRowNum() + 1, 1);
            else
                sheet.shiftRows(r.getRowNum(), r.getRowNum() + 1, rowBreakes);
        }
        for (DataRow<HeaderCell> headerCellDataRow : data.getHeaders()){
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
            }
            rowNumber++;
        }
    }

    @Override
    protected void createDataForTable() {
        rowNumber = (rowNumber > sheet.getLastRowNum() ? sheet.getLastRowNum() : rowNumber);//if we have empty strings
        sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
        for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow : dataRows) {
            Row row = sheet.getRow(rowNumber) != null ? sheet.getRow(rowNumber++) : sheet.createRow(rowNumber++);

            for (int i = 0; i < formTemplate.getColumns().size(); i++) {
                Column column = formTemplate.getColumns().get(i);
                if (column.isChecking() && !isShowChecked) {
                    continue;
                }
                if (column.getWidth() == 0 && column.getAlias() != null) {
                    if (formTemplate.getColumns().size() == i + 1)
                        continue;
                    Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, i, false);
                    CellStyle cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue((String) dataRow.get(column.getAlias()));
                    if (dataRow.getCell(column.getAlias()).getColSpan() > 1)
                        i = i + dataRow.getCell(column.getAlias()).getColSpan() - 1;
                    continue;
                }
                Object obj = dataRow.get(column.getAlias());
                Cell cell = mergedDataCells(dataRow.getCell(column.getAlias()), row, i, false);
                CellStyle cellStyle;
                if (ColumnType.STRING.equals(column.getColumnType())) {
                    String str = (String) obj;
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(str);
                } else if (ColumnType.DATE.equals(column.getColumnType())) {
                    Date date = (Date) obj;
                    if (date != null)
                        cell.setCellValue(date);
                    else
                        cell.setCellValue("");
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.DATE, column.getAlias());
                    cell.setCellStyle(cellStyle);
                } else if (ColumnType.NUMBER.equals(column.getColumnType())) {
                    BigDecimal bd = (BigDecimal) obj;
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.BIGDECIMAL, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                    if (bd != null){
                        cell.setCellValue(((NumericColumn)column).getPrecision() >0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                    }
                } else if (ColumnType.AUTO.equals(column.getColumnType())) {
                    Long bd = (Long) obj;
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.NUMERATION, column.getAlias());
                    cell.setCellStyle(cellStyle);

                    cell.setCellValue(bd != null ? String.valueOf(bd) : "");
                } else if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.STRING, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataRow.getCell(column.getAlias()).getRefBookDereference());
                } else if (obj == null) {
                    cellStyle = getCellStyle(dataRow.getCell(column.getAlias()), CellType.EMPTY, column.getAlias());
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue("");
                }
                if (dataRow.getCell(column.getAlias()).getColSpan() > 1)
                    i = i + dataRow.getCell(column.getAlias()).getColSpan() - 1;
            }
        }
    }

    /**
     * Получить стиль для ячейки excel'я по типу и стилю ячейки формы.
     *
     * @param dataRowCell ячека формы
     * @param type тип ячейки
     * @param alias стиль ячейки
     * @return сти
     */
    private CellStyle getCellStyle(com.aplana.sbrf.taxaccounting.model.Cell dataRowCell, CellType type, String alias) {
        if (!DiffService.STYLE_NO_CHANGE.equals(dataRowCell.getStyleAlias())
                && !DiffService.STYLE_INSERT.equals(dataRowCell.getStyleAlias())
                && !DiffService.STYLE_DELETE.equals(dataRowCell.getStyleAlias())
                && !DiffService.STYLE_CHANGE.equals(dataRowCell.getStyleAlias())) {
            // если стиль не относится к стилям дельт, то получить обычный стиль
            return cellStyleBuilder.createCellStyle(type, alias);
        }
        XSSFCellStyle cellStyle = (XSSFCellStyle) cellStyleBuilder.createCellStyle(type, alias, dataRowCell.getStyleAlias());

        // фон
        XSSFColor bgColor = getColor(dataRowCell.getStyle().getBackColor());
        if (bgColor != null) {
            cellStyle.setFillForegroundColor(bgColor);
            cellStyle.setFillBackgroundColor(bgColor);
        }

        // шрифт
        Font font = getFont(dataRowCell.getStyleAlias(), dataRowCell);
        cellStyle.setFont(font);

        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        return cellStyle;
    }

    /** Получить цвет по rgb. */
    private XSSFColor getColor(Color color) {
        // TODO (Ramil Timerbaev) если rgb = 0 0 0, то в excel'е цвет почему то задается белый (при 255 255 255 - черный)
        if (!(color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
            return new XSSFColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
        }
        return null;
    }

    /** Получить шрифт по алиасу стиля. */
    private XSSFFont getFont(String alias, com.aplana.sbrf.taxaccounting.model.Cell cell) {
        if (!fontMap.containsKey(alias)) {
            XSSFFont font = (XSSFFont) workBook.createFont();
            // жирность
            font.setBold(cell.getStyle().isBold());

            // курсив
            font.setItalic(cell.getStyle().isItalic());

            // цвет шрифта
            XSSFColor color = getColor(cell.getStyle().getFontColor());
            if (color != null) {
                font.setColor(color);
            }

            fontMap.put(alias, font);
        }
        return fontMap.get(alias);
    }

    @Override
    protected void cellAlignment() {
        for (int i = 0; i < formTemplate.getColumns().size(); i++ ){
            widthCellsMap.put(i, formTemplate.getColumns().get(i).getWidth());
        }
        super.cellAlignment();
    }

    @Override
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
        workBook.setPrintArea(0, 0, (!data.getHeaders().isEmpty()?data.getHeaders().get(0).size() - columnBreaks : 0) , 0,
                (dataRows != null ? dataRows.size() : 0) + data.getSigners().size() + data.getHeaders().size() + 15);
        sheet.setFitToPage(true);
        sheet.setAutobreaks(true);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.getPrintSetup().setFitWidth((short) 1);
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
        if (LOG.isDebugEnabled())
            LOG.debug(workBook.getName(rangeName).getRefersToFormula());
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