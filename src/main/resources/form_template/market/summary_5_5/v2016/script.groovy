package form_template.market.summary_5_5.v2016

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.ColumnType
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataReport
import com.aplana.sbrf.taxaccounting.model.FormStyle
import com.aplana.sbrf.taxaccounting.model.NumericColumn
import com.aplana.sbrf.taxaccounting.model.RefBookColumn
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.FormDataXlsmReportBuilder.CellType // нужный импорт
import com.aplana.sbrf.taxaccounting.service.impl.print.formdata.XlsxReportMetadata
import groovy.transform.Field
import org.apache.commons.io.IOUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.AreaReference
import org.apache.poi.ss.util.CellRangeAddress

/**
 * 5.5 Внутренние интервалы плат по гарантиям и непокрытым аккредитивам, ИТФ.
 *
 * В форме нет расчетов, логических проверок, загрузки эксель, есть только консолидация из формы 5.2 (б).
 * При консолидации данные источника группируются.
 *
 * formTemplateId = 914
 * formType = 914
 */

// графа    - fix
// графа 1  - rowNum                - № пп
// графа 2  - creditRating          - Международный кредитный рейтинг
// графа 3  - category              - Наличие обеспечения обязательства
// графа 4  - creditPeriod          - Срок обязательства / Объём обязательства в рублях / Границы процентного интервала

// графа 5  - count05year100        - Количество сопоставимых обязательств - скрытый столбец
// графа 6  - min05year100          - Интервал процентных ставок (% годовых) / min
// графа 7  - max05year100          - Интервал процентных ставок (% годовых) / max
// графа 8  - count05_1year100      - скрытый столбец
// графа 9  - min05_1year100
// графа 10 - max05_1year100
// графа 11 - count1_3year100       - скрытый столбец
// графа 12 - min1_3year100
// графа 13 - max1_3year100
// графа 14 - count3year100         - скрытый столбец
// графа 15 - min3year100
// графа 16 - max3year100

// графа 17 - count05year101        - скрытый столбец
// графа 18 - min05year101
// графа 19 - max05year101
// графа 20 - count05_1year101      - скрытый столбец
// графа 21 - min05_1year101
// графа 22 - max05_1year101
// графа 23 - count1_3year101       - скрытый столбец
// графа 24 - min1_3year101
// графа 25 - max1_3year101
// графа 26 - count3year101         - скрытый столбец
// графа 27 - min3year101
// графа 28 - max3year101

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CALCULATE_TASK_COMPLEXITY:
        calcTaskComplexity()
        break
    case FormDataEvent.GET_SPECIFIC_REPORT_TYPES:
        specificReportType.add("Таблица внутренних рыночных интервалов")
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport()
        break
}

@Field
def refBookCache = [:]

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void consolidation() {
    // мапа со списоком строк НФ (ключ по критериям сопоставимости -> строки НФ)
    def groupRowsMap = [:]

    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    sourcesInfo.each { Relation relation ->
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        // сгруппировать строки в группы
        for (def row : dataRows) {
            if (row.groupExclude != null && getRefBookValue(38L, row.groupExclude)?.CODE?.value == 0) {
                def key = getKey(row)
                if (groupRowsMap[key] == null) {
                    groupRowsMap[key] = []
                }
                groupRowsMap[key].add(row)
            }
        }
    }
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    // заполнить значения: количество, min, max
    def templateRows = formDataService.getFormTemplate(formData.formTemplateId)?.rows
    def groupKeys = groupRowsMap.keySet().toList()
    for (def key : groupKeys) {
        def rows = groupRowsMap[key]
        def criteria = key.split('#')

        // определить alias колонок
        def columnSubAlias = criteria[2] + criteria[3] // 05year/05_1year/1_3year/3year + 100/101
        if (columnSubAlias.contains('null')) {
            continue
        }
        def columnCount = 'count' + columnSubAlias
        def columnMin = 'min' + columnSubAlias
        def columnMax = 'max' + columnSubAlias

        // определить alias строк
        def rowSubAlias = criteria[1] + '_' + criteria[0] + '_' + criteria[4] // 1/2 + AAA/AA/A/BBB/BB/B/CCC/CC/C/D/other + yesNo/yes/no
        def aliasForCount = rowSubAlias + '_count'
        def aliasForMinMax = rowSubAlias + '_minmax'

        // количество
        def row = templateRows.find { it.getAlias() == aliasForCount }
        if (row) {
            row[columnCount] = rows.size()
        }
        // min, max
        row = templateRows.find { it.getAlias() == aliasForMinMax }
        if (row) {
            row[columnMin] = calcMin(rows)
            row[columnMax] = calcMax(rows)
        }
    }

    updateIndexes(templateRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = templateRows
}

/**
 * Получить ключ для определения группы.
 *
 * <pre>
 * Перечень критериев сопоставимости:
 * 1. Международный кредитный рейтинг   - по графе 9 источника  - AAA, AA, A, BBB, BB, B, CCC, CC, C, D, null
 * 2. Вид обязательства                 - по графе 17 источника - 1, 2, null
 * 3. Срок обязательства                - по графе 16 источника - 05year, 05_1year, 1_3year, 3year, null
 * 4. Объем обязательства               - по графе 23 источника - 100, 101, null
 * 5. Наличие обеспечения обязательства - по графе 21 источника - yesNo, yes, no, null
 *
 * Пример: AAA#1#1_3year#101#yes
 *
 * Критерии 2 используется для группировки в отдельный блок (начальные строки из макета)
 * Критерии 1 и 5 используется для определения строки в блоке:
 *      'AAA' + '_' + 'yes' + '_' + 'count'  = AAA_yes_count
 *      'AAA' + '_' + 'yes' + '_' + 'minmax' = AAA_yes_minmax
 * Критерии 3 и 4 используется для определения столбца:
 *      'count' + '1_3year' + '100' = count1_5year100
 *      'min' + '1_3year' + '100'   = min1_5year100
 *      'max' + '1_3year' + '100'   = max1_5year100
 * </pre>
 *
 * @param row строка источника
 */
String getKey(def row) {
    // 1. Международный кредитный рейтинг
    def internationalRating = row.internationalRating
    def rating603 = getRefBookValue(603L, internationalRating)?.INTERNATIONAL_CREDIT_RATING?.value
    def rating602 = (rating603 ? getRefBookValue(602L, rating603)?.INTERNATIONAL_CREDIT_RATING?.value : null)
    def rating = null
    if (rating602 == 'AAA') {
        rating = 'AAA'
    } else if (rating602 in ['AA', 'AA+', 'AA-']) {
        rating = 'AA'
    } else if (rating602 in ['A', 'A+', 'A-']) {
        rating = 'A'
    } else if (rating602 in ['BBB', 'BBB+', 'BBB-']) {
        rating = 'BBB'
    } else if (rating602 in ['BB', 'BB+', 'BB-']) {
        rating = 'BB'
    } else if (rating602 in ['B', 'B+', 'B-']) {
        rating = 'B'
    } else if (rating602 in ['CCC', 'CCC+', 'CCC-']) {
        rating = 'CCC'
    } else if (rating602 in ['CC', 'CC+', 'CC-']) {
        rating = 'CC'
    } else if (rating602 in ['C', 'C+', 'C-']) {
        rating = 'C'
    } else if (rating602 == 'D') {
        rating = 'D'
    }

    // 2. Вид обязательства
    def obligationType = getRefBookValue(608L, row.obligationType)?.CODE?.value?.toString()

    // 3. Срок обязательства
    def period
    if (row.period == null) {
        period = null
    } else if (row.period <= 0.5) {
        period = '05year'
    } else if (row.period <= 1) {
        period = '05_1year'
    } else if (row.period <= 3) {
        period = '1_3year'
    } else {
        period = '3year'
    }

    // 4. Объем обязательства
    def creditSum
    if (row.endSum == null) {
        creditSum = null
    } else if (row.endSum <= 100_000_000) {
        creditSum = '100'
    } else {
        creditSum = '101'
    }

    // 5. Наличие обеспечения обязательства
    def provisionPresence = null
    if (internationalRating && rating in ['B', 'CCC', 'CC', 'C', 'D']) {
        def isNo = (row.provisionPresence ? getRefBookValue(38L, row.provisionPresence)?.CODE?.value == 0 : null)
        if (isNo != null) {
            provisionPresence = (isNo ? 'no' : 'yes')
        }
    } else if (internationalRating) {
        provisionPresence = 'yesNo'
    }

    def tmp = (rating           + "#" +     // 1. Международный кредитный рейтинг
            obligationType      + "#" +     // 2. Вид обязательства
            period              + "#" +     // 3. Срок обязательства
            creditSum           + "#" +     // 4. Объем обязательства
            provisionPresence               // 5. Наличие обеспечения обязательства
    )
    return tmp
}

def calcMin(def rows) {
    calcMinOrMax(rows, true)
}

def calcMax(def rows) {
    calcMinOrMax(rows, false)
}

/**
 * Посчитать min или max по группе строк источника.
 *
 * @param rows строки источни
 * @param isMin признак того что расчет для min
 */
def calcMinOrMax(def rows, def isMin) {
    def result
    def n = rows.size()
    if (n < 4) {
        // если строк меньше четырех, то найти минимум или максимум
        def row
        if (isMin) {
            row = rows.min { it.rate }
        } else {
            row = rows.max { it.rate }
        }
        result = row?.rate
    } else {
        def values = rows.collect { it.rate }.sort()
        double k = (isMin ? n / 4 : n * 0.75)
        def precision = k % 1
        if (precision == 0) {
            int index = k.intValue()
            result = (values[index - 1] + values[index]) / 2
        } else {
            int index = (k - precision).intValue()
            result = values[index]
        }
    }
    return round(result, 2)
}

@Field
def REPORT_COLUMN_COUNT = 20

void calcTaskComplexity() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allSaved
    def rowCount = dataRows.size() + 8 // + 8 строк
    taskComplexityHolder.setValue(REPORT_COLUMN_COUNT * rowCount)
}

/** Специфический отчет "Контролируемые лица" XLSM*/
void createSpecificReport() {
    FormDataReport data = new FormDataReport()
    data.setData(formData)
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    data.setFormTemplate(formTemplate)
    formData.setHeaders(formTemplate.getHeaders())
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    data.setReportPeriod(reportPeriod)
    def dataRows = formDataService.getDataRowHelper(formData).allSaved
    def periodCode = refBookFactory.getDataProvider(8L).getRecordData(reportPeriod.getDictTaxPeriodId()).get('CODE')
    FormDataXlsmReportBuilder builder = new FormDataXlsmReportBuilder(data, false, dataRows, periodCode, true) {
        {
            ROW_NUMBER = 4;
            rowNumber = ROW_NUMBER;
        }

        @Override
        protected void fillHeader() {
            clearSheet(workBook, sheet)
            fillHeaderCells(workBook, sheet)
        }
        @Override
        protected void fillFooter() {
            // footer пустой
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
                    org.apache.poi.ss.usermodel.Cell workBookcell = mergedDataCells(headerCellDataRow.getCell(column.getAlias()), row, i, true);
                    CellStyle cellStyle = cellStyleBuilder.getCellStyle(CellType.HEADER, column.getAlias() + "_header");
                    cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
                    workBookcell.setCellStyle(cellStyle);
                    workBookcell.setCellValue(String.valueOf(headerCell.getValue()));
                    if(headerCell.getColSpan() > 1){
                        i = i + headerCell.getColSpan() - 1;
                    }
                }
                rowNumber++;
            }
            autoSizeHeaderRowsHeight();
        }

        // почти скопировал метод ради выравнивания объединенных ячеек (по центру)
        @Override
        protected void createDataForTable() {
            rowNumber = (rowNumber > sheet.getLastRowNum() ? sheet.getLastRowNum() : rowNumber);//if we have empty strings
            sheet.shiftRows(rowNumber, sheet.getLastRowNum(), dataRows.size() + 2);
            // перебираем строки
            for (DataRow<Cell> dataRow : dataRows) {
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
                    org.apache.poi.ss.usermodel.Cell cell = mergedDataCells(dataRow.getCell(columnAlias), row, i, false);
                    CellStyle cellStyle;
                    if (!dataRow.getCell(columnAlias).isForceValue()) {
                        if (ColumnType.STRING.equals(column.getColumnType())) {
                            String str = (String) obj;
                            cellStyle = getCellStyle(formStyle, CellType.STRING, columnAlias);
                            setCellStyle(cell, cellStyle)
                            cell.setCellValue(str);
                        } else if (ColumnType.DATE.equals(column.getColumnType())) {
                            Date date = (Date) obj;
                            if (date != null)
                                cell.setCellValue(date);
                            else
                                cell.setCellValue("");
                            cellStyle = getCellStyle(formStyle, CellType.DATE, columnAlias);
                            setCellStyle(cell, cellStyle)
                        } else if (ColumnType.NUMBER.equals(column.getColumnType())) {
                            BigDecimal bd = (BigDecimal) obj;
                            cellStyle = getCellStyle(formStyle, CellType.BIGDECIMAL, columnAlias);
                            setCellStyle(cell, cellStyle)
                            cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);

                            if (bd != null){
                                cell.setCellValue(((NumericColumn)column).getPrecision() >0 ? Double.parseDouble(bd.toString()) : bd.longValue());
                            }
                        } else if (ColumnType.AUTO.equals(column.getColumnType())) {
                            Long bd = (Long) obj;
                            cellStyle = getCellStyle(formStyle, CellType.NUMERATION, columnAlias);
                            setCellStyle(cell, cellStyle)

                            cell.setCellValue(bd != null ? String.valueOf(bd) : "");
                        } else if (ColumnType.REFBOOK.equals(column.getColumnType()) || ColumnType.REFERENCE.equals(column.getColumnType())) {
                            RefBookValue refBookValue = dataRow.getCell(columnAlias).getRefBookValue();
                            cellStyle = getCellStyle(formStyle, CellType.REFBOOK, columnAlias);
                            setCellStyle(cell, cellStyle)
                            if (refBookValue != null) {
                                switch (refBookValue.getAttributeType()) {
                                    case RefBookAttributeType.DATE:
                                        Date date = refBookValue.getDateValue();
                                        if (date != null)
                                            cell.setCellValue(date);
                                        else
                                            cell.setCellValue("");
                                        break;
                                    case RefBookAttributeType.NUMBER:
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
                            setCellStyle(cell, cellStyle)
                            cell.setCellValue("");
                        }
                    } else {
                        String str = (String) obj;
                        cellStyle = getCellStyle(formStyle, CellType.STRING, columnAlias);
                        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue(str);
                    }
                    if (dataRow.getCell(columnAlias).getColSpan() > 1)
                        i = i + dataRow.getCell(columnAlias).getColSpan() - 1;
                }
            }
        }

        private setCellStyle(org.apache.poi.ss.usermodel.Cell cell, CellStyle cellStyle) {
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            cell.setCellStyle(cellStyle);
        }
    };
    String filePath = builder.createReport()
    def file = new File(filePath)
    FileInputStream fileInputStream = null;
    try {
        fileInputStream = new FileInputStream(file);
        IOUtils.copy(fileInputStream, scriptSpecificReportHolder.getFileOutputStream())
    } finally {
        fileInputStream.close()
        file.delete()
    }

    // название файла
    scriptSpecificReportHolder.setFileName("Таблица внутренних рыночных интервалов.xlsm")
}

enum StyleType {
    TITLE_CENTER,
    TITLE_RIGHT,
}

void clearSheet(Workbook workBook, Sheet sheet) {
    // убрать объединения
    def count = sheet.getNumMergedRegions()
    for (int i = count - 1; i >= 0; i--) {
        sheet.removeMergedRegion(i)
    }
    // очистим пустые строки в начале
    sheet.removeRow(sheet.getRow(0))
    sheet.removeRow(sheet.getRow(1))
    sheet.removeRow(sheet.getRow(2))
    sheet.removeRow(sheet.getRow(3))
    workBook.removeName(XlsxReportMetadata.RANGE_DATE_CREATE);
    workBook.removeName(XlsxReportMetadata.RANGE_REPORT_CODE);
    workBook.removeName(XlsxReportMetadata.RANGE_REPORT_PERIOD);
    workBook.removeName(XlsxReportMetadata.RANGE_REPORT_NAME);
    workBook.removeName(XlsxReportMetadata.RANGE_SUBDIVISION);
    workBook.removeName(XlsxReportMetadata.RANGE_SUBDIVISION_SIGN);
    workBook.removeName(XlsxReportMetadata.RANGE_FIO);
}

void fillHeaderCells(Workbook workBook, Sheet sheet) {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def order = reportPeriod.order
    def isFirstHalfYear = order == 1
    def yyyy = reportPeriod.taxPeriod.year
    def zzzz = reportPeriod.taxPeriod.year + 1
    String dateStart = isFirstHalfYear ? "01.08.$yyyy" : "01.02.$zzzz"
    String dateEnd = isFirstHalfYear ? "01.02.$zzzz" : "31.07.$zzzz"
    String date1 = isFirstHalfYear ? "01.08.$yyyy" : "01.02.$zzzz"
    String date2 = isFirstHalfYear ? "01.08.$yyyy" : "01.02.$zzzz"

    CellRangeAddress region

    def rowIndex = 0
    tmpRow = sheet.createRow(rowIndex)
    tmpRow.setHeightInPoints(33.75)

    cell = tmpRow.createCell(0)
    cell.setCellValue("Внутренние интервалы плат по выданным гарантиям (поручительствам) для проверки рыночности плат по договорам о предоставлении гарантий, непокрытых аккредитивов и инструментам торгового финансирования, заключённым Банком с его взаимозависимыми лицами и резидентами оффшорных зон, с использованием внутренних источников информации")
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_CENTER))
    // объединение 28-и ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 0, 27)
    sheet.addMergedRegion(region)

    rowIndex++
    tmpRow = sheet.createRow(rowIndex)

    cell = tmpRow.createCell(3)
    cell.setCellValue("в период с")
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_RIGHT))

    cell = tmpRow.createCell(4)
    cell.setCellValue(dateStart)
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_CENTER))
    // объединение 3-х ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 4, 6)
    sheet.addMergedRegion(region)

    cell = tmpRow.createCell(7)
    cell.setCellValue("по")
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_RIGHT))

    cell = tmpRow.createCell(8)
    cell.setCellValue(dateEnd)
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_CENTER))
    // объединение 3-х ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 8, 10)
    sheet.addMergedRegion(region)

    rowIndex++
    tmpRow = sheet.createRow(rowIndex)

    cell = tmpRow.createCell(3)
    cell.setCellValue("включая заключённые до")
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_RIGHT))

    cell = tmpRow.createCell(4)
    cell.setCellValue(date1)
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_CENTER))
    // объединение 3-х ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 4, 6)
    sheet.addMergedRegion(region)

    cell = tmpRow.createCell(7)
    cell.setCellValue("существенные условия по которым были изменены после")
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_RIGHT))
    // объединение 6-и ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 7, 12)
    sheet.addMergedRegion(region)

    cell = tmpRow.createCell(13)
    cell.setCellValue(date2)
    cell.setCellStyle(getCellStyle(workBook, StyleType.TITLE_CENTER))
    // объединение 3-х ячеек
    region = new CellRangeAddress(rowIndex, rowIndex, 13, 15)
    sheet.addMergedRegion(region)
}

@Field
def cellStyleMap = [:]

CellStyle getCellStyle(def workBook, StyleType styleType) {
    def alias = styleType.name()
    if (cellStyleMap.containsKey(alias)) {
        return cellStyleMap.get(alias)
    }
    CellStyle style = workBook.createCellStyle()
    switch (styleType) {
        case StyleType.TITLE_CENTER :
            style.setWrapText(true)
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            style.setAlignment(CellStyle.ALIGN_CENTER)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(12 as short)
            font.setFontName('Calibri')
            style.setFont(font)
            break
        case StyleType.TITLE_RIGHT :
            style.setWrapText(true)
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER)
            style.setAlignment(CellStyle.ALIGN_RIGHT)

            Font font = workBook.createFont()
            font.setBoldweight(Font.BOLDWEIGHT_BOLD)
            font.setFontHeightInPoints(12 as short)
            font.setFontName('Calibri')
            style.setFont(font)
            break
    }
    cellStyleMap.put(alias, style)
    return style
}