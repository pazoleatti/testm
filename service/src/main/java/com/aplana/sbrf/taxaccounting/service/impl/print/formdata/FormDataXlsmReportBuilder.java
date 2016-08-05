package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

    protected int ROW_NUMBER = 9; // для переопределения в скриптах

    protected int rowNumber = ROW_NUMBER; // для переопределения в скриптах

    protected boolean isShowChecked;

    protected CellStyleBuilder cellStyleBuilder;
    private static final String TEMPLATE = ClassUtils
            .classPackageAsResourcePath(FormDataXlsmReportBuilder.class)
            + "/acctax.xlsm";

    protected static final int MERGE_REGIONS_NUM_BACK = 10;

    public enum CellType{
        DATE,
        STRING,
        BIGDECIMAL,
        NUMERATION,
        EMPTY ,
		HEADER,
        REFBOOK
    }

    private final class CellStyleBuilder{

        private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

        private CellStyleBuilder() {
            for (Column column : columns){
                this.getCellStyle(CellType.HEADER, column.getAlias() + "_header");
            }
        }

        /**
         * Получить стиль для ячейки excel'я.
         *
         * @param cellType тип ячейки (дата, число, строка, ...)
         * @param columnAlias алиас столбца
         */
        public CellStyle getCellStyle(CellType cellType, String columnAlias) {
            return getCellStyle(cellType, columnAlias, null);
        }

        /**
         * Получить стиль для ячейки excel'я.
         *
         * @param cellType тип ячейки (дата, число, строка, ...)
         * @param columnAlias алиас столбца
         * @param formStyle стиль ячейки
         */
        public CellStyle getCellStyle(CellType cellType, String columnAlias, FormStyle formStyle){
            String cacheKey = columnAlias + "_" + (formStyle != null ? formStyle.toString() : "");
            if (cellStyleMap.containsKey(cacheKey)) {
				return cellStyleMap.get(cacheKey);
			}
            DataFormat dataFormat = workBook.createDataFormat();
            Column currColumn;
            CellStyle cellStyle = workBook.createCellStyle();
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);

            switch (cellType){
                case DATE:
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    currColumn = formTemplate.getColumn(columnAlias);
                    if(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat().isEmpty()){
                        cellStyle.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.sdf.get().toPattern()));
                    } else{
                        cellStyle.setDataFormat(dataFormat.getFormat(Formats.getById(((DateColumn)currColumn).getFormatId()).getFormat()));
                    }
                    break;
                case BIGDECIMAL:
                    currColumn = formTemplate.getColumn(columnAlias);
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    cellStyle.setWrapText(true);
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    cellStyle.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.getPrecision(((NumericColumn) currColumn).getPrecision())));
                    break;
                case NUMERATION:
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    cellStyle.setWrapText(true);
                    cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                    break;
                case STRING:
                    cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
                    cellStyle.setWrapText(true);
                    break;
                case EMPTY:
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    break;
                case HEADER: // для заголовков
                    cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
                    cellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
                    cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                    cellStyle.setWrapText(true);
                    cellStyle.setBorderBottom(CellStyle.BORDER_THICK);
                    cellStyle.setBorderTop(CellStyle.BORDER_THICK);
                    cellStyle.setBorderRight(CellStyle.BORDER_THICK);
                    cellStyle.setBorderLeft(CellStyle.BORDER_THICK);
                    break;
                case REFBOOK:
                    currColumn = formTemplate.getColumn(columnAlias);
                    RefBookAttribute refBookAttribute;
                    if (currColumn instanceof RefBookColumn) {
                        refBookAttribute = ((RefBookColumn)currColumn).getRefBookAttribute();
                    } else {
                        refBookAttribute = ((ReferenceColumn)currColumn).getRefBookAttribute();
                    }
                    switch (refBookAttribute.getAttributeType()) {
                        case STRING:
                            cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
                            cellStyle.setWrapText(true);
                            break;
                        case DATE:
                            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                            if(refBookAttribute.getFormat() == null){
                                cellStyle.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.sdf.get().toPattern()));
                            } else{
                                cellStyle.setDataFormat(dataFormat.getFormat(refBookAttribute.getFormat().getFormat()));
                            }
                            break;
                        case NUMBER:
                            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                            cellStyle.setWrapText(true);
                            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
                            cellStyle.setDataFormat(dataFormat.getFormat(XlsxReportMetadata.getPrecision(refBookAttribute.getPrecision())));
                            break;

                    }
                    break;
            }
			if (formStyle != null) {
				// фон
				XSSFColor bgColor = getColor(formStyle.getBackColor());
				if (bgColor != null) {
					((XSSFCellStyle) cellStyle).setFillForegroundColor(bgColor);
					((XSSFCellStyle) cellStyle).setFillBackgroundColor(bgColor);
					// сплошная заливка фона
					cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				}
				// шрифт
				Font font = getFont(formStyle);
				cellStyle.setFont(font);
			}
            cellStyleMap.put(cacheKey, cellStyle);
            return cellStyle;
        }
    }

    protected FormData data;
    protected List<DataRow<HeaderCell>> headers = new ArrayList<DataRow<HeaderCell>>();
    protected List<Column> columns = new ArrayList<Column>();
    protected RefBookValue periodCode;
    protected List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows;
    protected FormTemplate formTemplate;
    protected ReportPeriod reportPeriod,rpCompare;
    protected Date acceptanceDate;
    protected Date creationDate;

    private Map<String, XSSFFont> fontMap = new HashMap<String, XSSFFont>();

    private FormDataXlsmReportBuilder() throws IOException {
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

    /**
     *
     * @param data данные нф
     * @param isShowChecked отображать проверочные столбцы?
     * @param dataRows табличные данные
     * @param periodCode код периода из справочника
     * @param deleteHiddenColumns признак того, что в печатном представлении надо убрать скрытые столбцы
     * @throws IOException
     */
    public FormDataXlsmReportBuilder(FormDataReport data, boolean isShowChecked, List<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>> dataRows, RefBookValue periodCode, boolean deleteHiddenColumns)
            throws IOException {
        this();
        this.data = data.getData();
        this.dataRows = dataRows;
        this.formTemplate = data.getFormTemplate();
        this.isShowChecked = isShowChecked;
        this.reportPeriod = data.getReportPeriod();
        this.acceptanceDate = data.getAcceptanceDate();
        this.creationDate = data.getCreationDate();
        this.periodCode = periodCode;
        this.cellStyleBuilder = new CellStyleBuilder();
        this.rpCompare = data.getRpCompare();
        this.headers = this.data.cloneHeaders();
        this.columns = this.formTemplate.cloneColumns();

        if (!isShowChecked) {
            Iterator<Column> iterator = this.columns.iterator();
            while (iterator.hasNext()) {
                Column c = iterator.next();
                if (c.isChecking()) {
                    for(DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow: this.dataRows) {
                        dataRow.removeColumn(c);
                    }
                    for(DataRow<HeaderCell> header: this.headers) {
                        header.removeColumn(c);
                    }
                    iterator.remove();
                }
            }
        }

        if (deleteHiddenColumns) {
            Set<Integer> hiddenOrders = new HashSet<Integer>();
            Iterator<Column> iterator = this.columns.iterator();
            int i = 1;
            while (iterator.hasNext()) {
                Column c = iterator.next();
                if (c.getWidth() == 0) {
                    hiddenOrders.add(c.getOrder());
                    Column nextColumn = null;
                    if (i < columns.size()) {
                        nextColumn = columns.get(i);
                    }

                    //Удаляем скрытый столбец таблицы
                    for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow: this.dataRows) {
                        com.aplana.sbrf.taxaccounting.model.Cell cell = dataRow.getCell(c.getAlias());
                        if (nextColumn != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)) {
                            String value = (dataRow.get(c.getAlias()) != null) ? String.valueOf(dataRow.get(c.getAlias())) : null;
                            if (value != null && !value.isEmpty()) {
                                //Если в скрытом столбце есть какие то значения и он объединяется с соседними столбцами/ячейками (т.е значение для объединенной ячейки хранится в скрытой), то перед удалением переносим значения в соседний
                                dataRow.putForce(nextColumn.getAlias(), value);
                            }
                            //Переносим стиль с удаленного ячейке в столбце на ячейку в следующем столбце
                            com.aplana.sbrf.taxaccounting.model.Cell nextCell = dataRow.getCell(nextColumn.getAlias());
                            nextCell.setStyleAlias(cell.getStyleAlias());
                            //Переносим объединение ячеек
                            if (cell.getColSpan() > 1) {
                                //Уменьшаем объединение столбцов на 1, т.к скрытый столбец будет удален
                                nextCell.setColSpan(cell.getColSpan() - 1);
                                if (cell.getRowSpan() > 1) {
                                    //Строки объединяем только если одновременно объединяются столбцы
                                    nextCell.setRowSpan(cell.getRowSpan());
                                }
                            }
                        } else if (nextColumn == null || cell.getColSpan() == 1) {
                            //Если объединение ячеек было прописано не для скрытого столбца, то просматриваем предыдущие не должны ли они были объединяться со скрытым
                            for (Column prevCol : columns) {
                                if (prevCol.getOrder() < c.getOrder()) {
                                    com.aplana.sbrf.taxaccounting.model.Cell prevCell = dataRow.getCell(prevCol.getAlias());
                                    if (prevCell.getColSpan() > 1) {
                                        // считаем количество уже удаленных скрытых столбцов
                                        int hiddenCount = 0;
                                        for (Integer hiddenOrder : hiddenOrders) {
                                            if (hiddenOrder < c.getOrder() && hiddenOrder > prevCol.getOrder()) {
                                                hiddenCount++;
                                            }
                                        }
                                        if (c.getOrder() - (prevCol.getOrder() + prevCell.getColSpan() + hiddenCount) < 0) {
                                            //Найденная ячейка объединяет скрытую
                                            //Уменьшаем объединение столбцов на 1, т.к скрытый столбец будет удален
                                            prevCell.setColSpan(prevCell.getColSpan() - 1);
                                        }
                                    }
                                }
                            }
                        }
                        dataRow.removeColumn(c);
                    }

                    //Удаляем скрытый столбец из шапки таблицы
                    for (DataRow<HeaderCell> header: this.headers) {
                        HeaderCell headerCell = header.getCell(c.getAlias());
                        if (nextColumn != null && (headerCell.getColSpan() > 1 || headerCell.getRowSpan() > 1)) {
                            //Если скрытый столбец объединяется с соседним, то перед удалением назначаем объединение соседу
                            HeaderCell nextHeaderCell = header.getCell(nextColumn.getAlias());
                            if (headerCell.getColSpan() > 1) {
                                //Уменьшаем объединение столбцов на 1, т.к скрытый столбец будет удален
                                nextHeaderCell.setColSpan(headerCell.getColSpan() - 1);
                                if (headerCell.getRowSpan() > 1) {
                                    //Строки объединяем только если одновременно объединяются столбцы
                                    nextHeaderCell.setRowSpan(headerCell.getRowSpan());
                                }
                            }
                        } else if (nextColumn == null || headerCell.getColSpan() == 1) {
                            //Если столбец удаляется, но объединение прописано в предыдущих столбах, но надо его найти и уменьшить на 1
                            for (Column prevCol : columns) {
                                if (prevCol.getOrder() < c.getOrder()) {
                                    HeaderCell prevHeaderCell = header.getCell(prevCol.getAlias());
                                    if (prevHeaderCell.getColSpan() > 1) {
                                        // считаем количество уже удаленных скрытых столбцов
                                        int hiddenCount = 0;
                                        for (Integer hiddenOrder : hiddenOrders) {
                                            if (hiddenOrder < c.getOrder() && hiddenOrder > prevCol.getOrder()) {
                                                hiddenCount++;
                                            }
                                        }
                                        if (c.getOrder() - (prevCol.getOrder() + prevHeaderCell.getColSpan() + hiddenCount) < 0) {
                                            //Найденная ячейка объединяет скрытую
                                            prevHeaderCell.setColSpan(prevHeaderCell.getColSpan() - 1);
                                        }
                                    }
                                }
                            }
                        }
                        header.removeColumn(c);
                    }
                    iterator.remove();
                    //Уменьшаем счетчик т.к один столбец удалили
                    i--;
                }
                i++;
            }
        }
    }

    @Override
    protected void fillHeader(){

        int nullColumnCount = 0;
        int notNullColumn = columns.size() - 1;
        //Необходимо чтобы определять нулевые столбцы для Excel, по идее в конце должно делаться
        for (int i = columns.size() - 1, j = 0; i >= 0 && j <= 2; i-- ){
            if (columns.get(i).getWidth() == 0){
                sheet.setColumnWidth(i, 0);
                nullColumnCount++;
            } else {
                j++;
            }
        }

        //Опеределяет первый не нулевой столбец
        for (int i = 0; i <= columns.size() - 1 ; i++){
            if (columns.get(i).getWidth() != 0) {
                notNullColumn = i;
                break;
            }
        }

        //Fill subdivision
        AreaReference arSubdivision = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_SUBDIVISION).getRefersToFormula());
        CellRangeAddress region = new CellRangeAddress(arSubdivision.getFirstCell().getRow(), arSubdivision.getFirstCell().getRow(), notNullColumn, columns.size() - 1);
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

        arr = XlsxReportMetadata.sdf_m.get().format(printDate).toLowerCase().toCharArray();
        if (XlsxReportMetadata.sdf_m.get().format(printDate).equalsIgnoreCase("март") ||
                XlsxReportMetadata.sdf_m.get().format(printDate).equalsIgnoreCase("август")) {
            String month = XlsxReportMetadata.sdf_m.get().format(printDate).toLowerCase() + "а";
            arr = month.toCharArray();
        } else {
            arr[arr.length - 1] = 'я';
        }
        sb.append(String.format(XlsxReportMetadata.DATE_CREATE, XlsxReportMetadata.sdf_d.get().format(printDate),
                new String(arr), XlsxReportMetadata.sdf_y.get().format(printDate)));

        createCellByRange(XlsxReportMetadata.RANGE_DATE_CREATE, sb.toString(), 0, 0);
        sb.delete(0, sb.length());
        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_NAME).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow()) != null ? sheet.getRow(ar.getFirstCell().getRow())
                : sheet.createRow(ar.getFirstCell().getRow());
        CellStyle cellStyle = r.getCell(0).getCellStyle();
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER_SELECTION);
        cellStyle.setWrapText(true);
        for(int i = 1; i < columns.size(); i++) {
            r.createCell(i).setCellStyle(cellStyle);
        }
        r.setHeight((short) -1);

        //Fill report name
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_NAME, formTemplate.getFullName(), 0, notNullColumn);

        //Fill code
        AreaReference ar2 = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_REPORT_CODE).getRefersToFormula());
        Row r2 = sheet.getRow(ar2.getFirstCell().getRow()) != null ? sheet.getRow(ar2.getFirstCell().getRow())
                : sheet.createRow(ar2.getFirstCell().getRow());
        int shiftCode = columns.size() - ar2.getFirstCell().getCol() - 2 - nullColumnCount;
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
            /*String rpName =  !periodCode.getStringValue().equals("34") ? reportPeriod.getName() : "";
            String rpCompareName =  !periodCode.getStringValue().equals("34") ? rpCompare.getName() : "";*/
            sbPeriodName.append(String.format(
                    XlsxReportMetadata.REPORT_PERIOD,
                    rpCompare.getName(),
                    rpCompare.getTaxPeriod().getYear(),
                    reportPeriod.getName(),
                    reportPeriod.getTaxPeriod().getYear()));
        } else if (data.getPeriodOrder() != null) {
            sbPeriodName.append(
                    String.format(XlsxReportMetadata.MONTHLY,
                            Months.fromId(data.getPeriodOrder()).getTitle().toLowerCase(new Locale("ru", "RU")),
                            reportPeriod.getTaxPeriod().getYear()
                    )
            );
        } else {
            sbPeriodName.append(
                    String.format(XlsxReportMetadata.MONTHLY,
                            !periodCode.getStringValue().equals("34") ? reportPeriod.getName() : "",
                            reportPeriod.getTaxPeriod().getYear()
                    )
            );
        }
        if (data.isAccruing())
            sbPeriodName.append("(нарастающим итогом)");
        sb.append(sbPeriodName.toString());
        createCellByRange(XlsxReportMetadata.RANGE_REPORT_PERIOD, sb.toString(), 0, columns.size()/2);
    }

    @Override
    protected void createTableHeaders(){
        // Поскольку имеется шаблон с выставленными алиасами, то чтобы не записать данные в ячейку с алиасом
        // делаем проверку на то, что сумма начала записи таблицы и кол-ва строк не превышает номер строки с алиасом
        // и если превышает, то сдвигаем
        AreaReference ar = new AreaReference(workBook.getName(XlsxReportMetadata.RANGE_POSITION).getRefersToFormula());
        Row r = sheet.getRow(ar.getFirstCell().getRow());
        if (rowNumber + headers.size() >= r.getRowNum()){
            int rowBreakes = rowNumber + headers.size() - r.getRowNum();
            if(0 == rowBreakes)
                sheet.shiftRows(r.getRowNum(), r.getRowNum() + 1, 1);
            else
                sheet.shiftRows(r.getRowNum(), r.getRowNum() + 1, rowBreakes);
        }
        for (DataRow<HeaderCell> headerCellDataRow : headers){
            Row row = sheet.createRow(rowNumber);
            for (int i=0; i<columns.size(); i++){
                Column column = columns.get(i);
                if (column.isChecking() && !isShowChecked){
                    continue;
                }
                HeaderCell headerCell = headerCellDataRow.getCell(column.getAlias());
                Cell workBookcell = mergedDataCells(headerCellDataRow.getCell(column.getAlias()), row, i, true);
                workBookcell.setCellStyle(cellStyleBuilder.getCellStyle(CellType.HEADER, column.getAlias() + "_header"));
                workBookcell.setCellValue(String.valueOf(headerCell.getValue()));
                if(headerCell.getColSpan() > 1){
                    i = i + headerCell.getColSpan() - 1;
                }
            }
            rowNumber++;
        }
        autoSizeHeaderRowsHeight();
    }

    @Override
    protected void createDataForTable() {
        rowNumber = (rowNumber > sheet.getLastRowNum() ? sheet.getLastRowNum() : rowNumber);//if we have empty strings
        sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
        sheet.createFreezePane(0, rowNumber);
		// перебираем строки
        for (DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow : dataRows) {
            Row row = sheet.getRow(rowNumber) != null ? sheet.getRow(rowNumber++) : sheet.createRow(rowNumber++);
			// перебираем столбцы
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                if ((column.isChecking() && !isShowChecked)) {
                    continue;
                }
				String columnAlias = column.getAlias();
				FormStyle formStyle = dataRow.getCell(columnAlias).getStyle();
                if (column.getWidth() == 0 && columnAlias != null) {
                    if (columns.size() == i + 1)
                        continue;
                }
                Object obj = dataRow.get(columnAlias);
                Cell cell = mergedDataCells(dataRow.getCell(columnAlias), row, i, false);
                CellStyle cellStyle;
                if (!dataRow.getCell(columnAlias).isForceValue()) {
                    if (ColumnType.STRING.equals(column.getColumnType())) {
                        String str = (String) obj;
                        cellStyle = getCellStyle(formStyle, CellType.STRING, columnAlias);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue(str);
                    } else if (ColumnType.DATE.equals(column.getColumnType())) {
                        Date date = (Date) obj;
                        if (date != null)
                            cell.setCellValue(date);
                        else
                            cell.setCellValue("");
                        cellStyle = getCellStyle(formStyle, CellType.DATE, columnAlias);
                        cell.setCellStyle(cellStyle);
                    } else if (ColumnType.NUMBER.equals(column.getColumnType())) {
                        BigDecimal bd = (BigDecimal) obj;
                        cellStyle = getCellStyle(formStyle, CellType.BIGDECIMAL, columnAlias);
                        cell.setCellStyle(cellStyle);
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                        if (bd != null){
                            cell.setCellValue(((NumericColumn)column).getPrecision() >0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                        }
                    } else if (ColumnType.AUTO.equals(column.getColumnType())) {
                        Long bd = (Long) obj;
                        cellStyle = getCellStyle(formStyle, CellType.NUMERATION, columnAlias);
                        cell.setCellStyle(cellStyle);

                        cell.setCellValue(bd != null ? String.valueOf(bd) : "");
                    } else if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                        RefBookValue refBookValue = dataRow.getCell(columnAlias).getRefBookValue();
                        cellStyle = getCellStyle(formStyle, CellType.REFBOOK, columnAlias);
                        cell.setCellStyle(cellStyle);
                        if (refBookValue != null) {
                            switch (refBookValue.getAttributeType()) {
                                case DATE:
                                    Date date = refBookValue.getDateValue();
                                    if (date != null)
                                        cell.setCellValue(date);
                                    else
                                        cell.setCellValue("");
                                    break;
                                case NUMBER:
                                    RefBookAttribute refBookAttribute;
                                    if (ColumnType.REFBOOK.equals(column.getColumnType())) {
                                        refBookAttribute = ((RefBookColumn)column).getRefBookAttribute();
                                    } else {
                                        refBookAttribute = ((ReferenceColumn)column).getRefBookAttribute();
                                    }
                                    Number bd = refBookValue.getNumberValue();
                                    if (bd != null){
                                        cell.setCellValue(refBookAttribute.getPrecision() >0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                                    }
                                    break;
                                default:
                                    cell.setCellValue(dataRow.getCell(columnAlias).getRefBookDereference());
                                    break;
                            }
                        }
                    } else if (obj == null) {
                        cellStyle = getCellStyle(formStyle, CellType.EMPTY, columnAlias);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue("");
                    }
                } else {
                    String str = (String) obj;
                    cellStyle = getCellStyle(formStyle, CellType.STRING, columnAlias);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(str);
                }
                if (dataRow.getCell(columnAlias).getColSpan() > 1)
                    i = i + dataRow.getCell(columnAlias).getColSpan() - 1;
            }
        }
    }

    /**
     * Получить стиль для ячейки excel'я по типу и стилю ячейки формы.
     *
     * @param formStyle стиль ячейки формы
     * @param cellType тип ячейки
     * @param columnAlias алиас столбца
     * @return
     */
    protected CellStyle getCellStyle(FormStyle formStyle, CellType cellType, String columnAlias) {
        return cellStyleBuilder.getCellStyle(cellType, columnAlias, formStyle);
    }

    /** Получить цвет по rgb. */
    private XSSFColor getColor(Color color) {
        // TODO (Ramil Timerbaev) если rgb = 0 0 0, то в excel'е цвет почему то задается белый (при 255 255 255 - черный)
        if (color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0) {
            XSSFColor xssfColor = new XSSFColor();
            xssfColor.setIndexed(IndexedColors.BLACK.getIndex());
            return xssfColor;
        } else if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255) {
            XSSFColor xssfColor = new XSSFColor();
            xssfColor.setIndexed(IndexedColors.WHITE.getIndex());
            return xssfColor;
        } else {
            return new XSSFColor(new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()));
        }
    }

    /** Получить шрифт по алиасу стиля. */
    private XSSFFont getFont(FormStyle formStyle) {
		String cacheKey = formStyle.toString();
        if (!fontMap.containsKey(cacheKey)) {
            XSSFFont font = (XSSFFont) workBook.createFont();
            // жирность
            font.setBold(formStyle.isBold());
            // курсив
            font.setItalic(formStyle.isItalic());
            // цвет шрифта
            XSSFColor color = getColor(formStyle.getFontColor());
            if (color != null) {
                font.setColor(color);
            }
            fontMap.put(cacheKey, font);
        }
        return fontMap.get(cacheKey);
    }

    @Override
    protected void cellAlignment() {
        for (int i = 0; i < columns.size(); i++ ){
            widthCellsMap.put(i, columns.get(i).getWidth());
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
        cs.setWrapText(true);
        CellStyle csFio = c.getCellStyle();
        csFio.setWrapText(false);

        int cellSignPosition = columns.size() / 2;

        int columnWidth = 0;
        for (int i = 1; i < cellSignPosition; i++) {
            columnWidth += sheet.getColumnWidth(i) / 256;
        }

        for (int i = 0; i < data.getSigners().size(); i++) {
            Row rs = sheet.createRow(rowNumber);
            // Объединяем ячейки должности
            CellRangeAddress region = new CellRangeAddress(rowNumber, rowNumber, XlsxReportMetadata.CELL_POS + 1, cellSignPosition - 1);
            sheet.addMergedRegion(region);

            Cell crsP = createNotHiddenCell(XlsxReportMetadata.CELL_POS, rs);

            if (crsP != null) {
                String position = data.getSigners().get(i).getPosition();
                crsP.setCellValue(position);
                // Вычисляем количество строк
                int linesCount = getLinesCount(position, columnWidth);
                rs.setHeight((short) (sheet.getDefaultRowHeight() * linesCount));

                Cell crsS = createNotHiddenCell(cellSignPosition, rs);
                crsS.setCellValue("_______");
                Cell crsFio = createNotHiddenCell(cellSignPosition + 2, rs);
                crsFio.setCellValue("(" + data.getSigners().get(i).getName() + ")");
                crsP.setCellStyle(cs);
                crsS.setCellStyle(cs);
                crsFio.setCellStyle(csFio);
                rowNumber++;
            }
        }

        //Fill performer
        if(data.getPerformer()!=null){
            r = sheet.createRow(rowNumber);
            c = createNotHiddenCell(0, r);
            if (c != null) {
                String performer = "Исполнитель: " + (data.getPerformer().getName() != null ? data.getPerformer().getName() : "") + "/" +
                        (data.getPerformer().getPhone() != null ? data.getPerformer().getPhone() : "");
                c.setCellValue(performer);
                sheet.shiftRows(sheet.getLastRowNum(), sheet.getLastRowNum(), 1);
            }
        }

    }

    @Override
    protected void setPrintSetup() {
        int columnBreaks = 0;//необходимо чтобы определить область печати
        for (int i=0; i<columns.size(); i++){
            if (columns.get(i).isChecking() && !isShowChecked){
                columnBreaks++;
            }
        }
        workBook.setPrintArea(0, 0, (!headers.isEmpty() ? headers.get(0).size() - columnBreaks : 0) , 0,
                (dataRows != null ? dataRows.size() : 0) + data.getSigners().size() + headers.size() + 15);
        sheet.setFitToPage(true);
        sheet.setAutobreaks(true);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.getPrintSetup().setFitWidth((short) 1);
    }

    /*
    * Merge rows with data. Depend on fields from com.aplana.sbrf.taxaccounting.model.Cell rowSpan and colSpan.
    */
    protected Cell mergedDataCells(AbstractCell cell, Row currRow, int currColumn, boolean isHeader){
        Cell currCell;
        if(cell != null && (cell.getColSpan() > 1 || cell.getRowSpan() > 1)){
            if(currColumn + cell.getColSpan() > columns.size()){
                tableBorders(currColumn, columns.size(), currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
            }else if(currColumn + cell.getColSpan() > columns.size() - 1){
                tableBorders(currColumn, columns.size() - 1, currRow.getRowNum(), currRow.getRowNum() + cell.getRowSpan() - 1, isHeader);
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
        } else {
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
        if (c != null) {
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

    protected void autoSizeHeaderRowsHeight() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() >= ROW_NUMBER && mergedRegion.getLastRow() - mergedRegion.getFirstRow() == 1) {
                Cell firstRowCell = sheet.getRow(mergedRegion.getFirstRow()).getCell(mergedRegion.getFirstColumn());

                int columnWidth = (int) (columns.get(firstRowCell.getColumnIndex()).getWidth() * 1.5);
                int firstRowCellLinesCount = getLinesCount(firstRowCell.getStringCellValue(), columnWidth) - 1;
                int firstRowIndex = firstRowCell.getRowIndex();
                if (map.get(firstRowIndex) == null) {
                    map.put(firstRowIndex, firstRowCellLinesCount);
                } else if (map.get(firstRowIndex) < firstRowCellLinesCount) {
                    map.put(firstRowIndex, firstRowCellLinesCount);
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            sheet.getRow(entry.getKey()).setHeight((short) (sheet.getDefaultRowHeight() * entry.getValue()));
        }
    }

    private int getLinesCount(String string, int width) {
        if (string.length() > 0 && width > 0) {
            char[] chars = string.toCharArray();
            int linesCount = 0;
            for (int i = 0; i < chars.length; ) {
                linesCount++;
                i += width;
            }
            return linesCount;
        } else if (string.length() > 0 && width == 0) {
            return 1;
        }
        return 0;
    }
}