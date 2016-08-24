package form_template.market.summary_5_3_2.v2016

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.ColumnType
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataReport
import com.aplana.sbrf.taxaccounting.model.FormStyle
import com.aplana.sbrf.taxaccounting.model.FormTemplate
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
 * 5.3.2 Внутренние интервалы процентных ставок по Кредитным продуктам и Субординированным кредитам.
 *
 * В форме нет расчетов, логических проверок, загрузки эксель, есть только консолидация из формы 5.2.
 * При консолидации данные источника группируются. По каждой валюте и типу процентной ставки формируется группа из 20 строк.
 * Что бы не формировать эти строки кодом в ручную, а также для наглядности при доработках, они хранятся в начальных данных макета.
 * Поэтому после создании формы, начальные строки удаляются.
 *
 * formTemplateId = 908
 * formType = 908
 */

// графа    - fix
// графа 1  - rowNum                - № пп
// графа 2  - creditRating          - Кредитный рейтинг заёмщика
// графа 3  - category              - Категория обеспечения кредита
// графа 4  - creditPeriod          - Срок кредита / Объем кредита в рублёвом эквиваленте / Границы процентного интервала
// графа 5  - count1year100         - Количество сопоставимых кредитов - скрытый столбец
// графа 6  - min1year100           - Интервал процентных ставок (% годовых) / min
// графа 7  - max1year100           - Интервал процентных ставок (% годовых) / max
// графа 8  - count1year100_1000    - скрытый столбец
// графа 9  - min1year100_1000
// графа 10 - max1year100_1000
// графа 11 - count1year1000        - скрытый столбец
// графа 12 - min1year1000
// графа 13 - max1year1000
// графа 14 - count1_5year100       - скрытый столбец
// графа 15 - min1_5year100
// графа 16 - max1_5year100
// графа 17 - count1_5year100_1000  - скрытый столбец
// графа 18 - min1_5year100_1000
// графа 19 - max1_5year100_1000
// графа 20 - count1_5year1000      - скрытый столбец
// графа 21 - min1_5year1000
// графа 22 - max1_5year1000
// графа 23 - count5_10year100      - скрытый столбец
// графа 24 - min5_10year100
// графа 25 - max5_10year100
// графа 26 - count5_10year100_1000 - скрытый столбец
// графа 27 - min5_10year100_1000
// графа 28 - max5_10year100_1000
// графа 29 - count5_10year1000     - скрытый столбец
// графа 30 - min5_10year1000
// графа 31 - max5_10year1000
// графа 32 - count10year100        - скрытый столбец
// графа 33 - min10year100
// графа 34 - max10year100
// графа 35 - count10year100_1000   - скрытый столбец
// графа 36 - min10year100_1000
// графа 37 - max10year100_1000
// графа 38 - count10year1000       - скрытый столбец
// графа 39 - min10year1000
// графа 40 - max10year1000

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        deleteAllRows()
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
@Field
def recordCache = [:]
@Field
def providerCache = [:]

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
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
                def key = getKey(row, relation)
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

    // мапа с валютами (буквенные код валюты -> список из двух списков строк: фиксированная процентная ставка и плавающая процентная ставка)
    def currencyMap = [:]
    groupRowsMap.keySet().toList().each { key ->
        def rows = groupRowsMap[key]
        def row = rows[0]
        // валюта
        def code = getCurrencyCode(row)
        if (currencyMap[code] == null) {
            // список из двух списков строк: фиксированная процентная ставка и плавающая процентная ставка
            currencyMap[code] = [[], []]
        }
        // тип процентной ставки
        def index = (isFix(row) ? 0 : 1)
        currencyMap[code][index].add(key)
    }

    // сортировка по валюте
    def sortedCurrency = currencyMap.keySet().toList().sort { a, b ->
        def a1 = getSortIndex(a)
        def b1 = getSortIndex(b)
        if (a1 == 0 && b1 == 0) {
            return a <=> b
        }
        return (b1 <=> a1)
    }

    // добавить группу фиксированных строк для каждой валюты + расчеты
    def rowNum = 0
    def dataRows = []
    sortedCurrency.each { currencyKey ->
        def rateTypes = currencyMap[currencyKey].grep()
        rateTypes.each { groupKeys ->
            def groupRows = getGroupRows(groupRowsMap, groupKeys, rowNum)
            dataRows.addAll(groupRows)
            // нумерация
            rowNum = groupRows[-1].rowNum
        }
    }

    updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
}

/**
 * Получить ключ для определения группы.
 *
 * <pre>
 * Перечень критериев сопоставимости:
 * 1. Кредитный рейтинг заёмщика    - по графе 8 источника  - 1-11, 12-22, 23-26, other
 * 2. Валюта кредита                - по графе 16 источника - id записи справочника 15
 * 3. Средневзвешенный срок кредита - по графе 15 источника - 1year, 1_5year, 5_10year, 10year
 * 4. Объем кредита                 - по графе 17 источника - 100, 100_1000, 1000
 * 5. Категория обеспечения кредита - по графе 23 источника - 1, 2, 3, other
 * 6. Тип процентной ставки         - по графе 18 источника - id записи справочника 72
 *
 * Пример: 1-11#23#1_5year#100#2#181049600
 *
 * Критерии 2 и 6 используется для группировки в отдельный блок (начальные строки из макета)
 * Критерии 1 и 5 используется для определения строки в блоке:
 *      '1-11' + '_' + '1' + 'count'  = 1-11_1count
 *      '1-11' + '_' + '1' + 'minmax' = 1-11_1minmax
 * Критерии 3 и 4 используется для определения столбца:
 *      'count' + '1_5year' + '100' = count1_5year100
 *      'min' + '1_5year' + '100'   = min1_5year100
 *      'max' + '1_5year' + '100'   = max1_5year100
 * </pre>
 *
 * @param row строка источника
 * @param source источник (необходим для формирования сообщения при проверках)
 */
String getKey(def row, Relation source) {
    // 1. Кредитный рейтинг заёмщика
    def creditRating = getRefBookValue(604L, row.creditRating)?.NAME?.value
    if (!(creditRating in ['1 КЛАСС', '2 КЛАСС', '3 КЛАСС'])) {
        def columnName = getColumnName(row, 'creditRating')
        def record603 = getRefBookRecord(603L, 'CREDIT_RATING', creditRating, getReportPeriodEndDate(), row.getIndex(), columnName, false)
        if (record603) {
            def className = record603?.CREDIT_QUALITY_CLASS?.value
            creditRating = getRefBookValue(601L, className)?.CREDIT_QUALITY_CLASS?.value
        }
    }
    if (creditRating == null) {
        creditRating = null
    } else if ('1 КЛАСС' == creditRating) {
        creditRating = '1-11'
    } else if ('2 КЛАСС' == creditRating) {
        creditRating = '12-22'
    } else if ('3 КЛАСС' == creditRating) {
        creditRating = '23-26'
    } else {
        creditRating = 'other'
    }

    // 3. Средневзвешенный срок кредита
    def avgPeriod
    if (row.avgPeriod == null) {
        avgPeriod = null
    } else if (row.avgPeriod <= 1) {
        avgPeriod = '1year'
    } else if (row.avgPeriod <= 5) {
        avgPeriod = '1_5year'
    } else if (row.avgPeriod <= 10) {
        avgPeriod = '5_10year'
    } else {
        avgPeriod = '10year'
    }

    // 4. Объем кредита
    def credit = row.creditSum
    def code = getCurrencyCode(row)
    // если валюта не рубль, то перевести объем кредита в рубли
    if (row.currencyCode != null && code != null && code != 'RUR') {
        columnName = getColumnName(row, 'currencyCode')
        def record22 = getRefBookRecord(22L, 'CODE_LETTER', row.currencyCode.toString(), getReportPeriodEndDate(), row.getIndex(), columnName, false)
        if (record22) {
            credit = (record22?.RATE?.value ? round(credit * record22?.RATE?.value, 2) : null)
        } else {
            // 1. В справочнике «Курсы валют» не найден курс для заданной валюты на заданную дату
            def msg = "Форма-источника: «%s», Подразделение: «%s», Период: «%s», строка %d: Не найден курс валюты для «%s» на дату %s!"
            def formName = source?.formType?.name
            def departmentName = source?.department?.name
            def periodName = source?.departmentReportPeriod?.reportPeriod?.name + ' ' + source?.departmentReportPeriod?.reportPeriod?.taxPeriod?.year
            def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            logger.error(msg, formName, departmentName, periodName, row.getIndex(), code, dateInStr)
        }
    }
    def creditSum
    if (credit == null) {
        creditSum = null
    } else if (credit <= 100_000_000) {
        creditSum = '100'
    } else if (credit <= 1_000_000_000) {
        creditSum = '100_1000'
    } else {
        creditSum = '1000'
    }

    // 5. Категория обеспечения кредита
    def provideCategory = getRefBookValue(606L, row.provideCategory)?.NAME?.value
    if (provideCategory == null) {
        provideCategory = null
    } else if ('Полностью обеспеченный' == provideCategory) {
        provideCategory = '1'
    } else if ('Частично обеспеченный' == provideCategory) {
        provideCategory = '2'
    } else if ('Необеспеченный' == provideCategory) {
        provideCategory = '3'
    } else {
        provideCategory = 'other'
    }

    def tmp = (creditRating     + "#" +     // 1. Кредитный рейтинг заёмщика
            row.currencyCode    + "#" +     // 2. Валюта кредита
            avgPeriod           + "#" +     // 3. Средневзвешенный срок кредита
            creditSum           + "#" +     // 4. Объем кредита
            provideCategory     + "#" +     // 5. Категория обеспечения кредита
            row.rateType                    // 6. Тип процентной ставки
    )

    return tmp.toLowerCase()
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
            row = rows.min { it.economyRate }
        } else {
            row = rows.max { it.economyRate }
        }
        result = row?.economyRate
    } else {
        def values = rows.collect { it.economyRate }.sort()
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

/**
 * Для сортировки валют. Порядок: USD, RUR, EUR, KZT, остальные валюты.
 *
 * @param value буквенный код валюты
 */
def getSortIndex(def value) {
    switch (value) {
        case 'USD': return 4
        case 'RUR': return 3
        case 'EUR': return 2
        case 'KZT': return 1
        default: return 0
    }
}

/**
 * Сформировать и заполнить группу строк текущей формы по данным из источника (группа строк источника для одной валюты).
 *
 * @param groupRowsMap мапа со группированными данными источника
 * @param groupKeys список ключей групп источника для одной валюты
 * @param rowNum нумерация
 *
 * @return группа строк текущей формы
 */
def getGroupRows(def groupRowsMap, def groupKeys, def rowNum) {
    def tmpKey = groupKeys[0]
    def tmpRows = groupRowsMap[tmpKey]
    def tmpRow = tmpRows[0]
    def isFix = isFix(tmpRow)
    def templateRows = getTemplateRows()

    // изменить надписи
    def header = templateRows.find { it.getAlias() == 'header' }
    header.fix = (isFix ? 'Фиксированная процентная ставка' : '"Плавающая" процентная ставка')
    def subHeader = templateRows.find { it.getAlias() == 'subHeader' }
    subHeader.fix = (tmpRow?.currencyCode ? getRefBookValue(15L, tmpRow.currencyCode)?.NAME?.value : null)

    // заполнить значения: количество, min, max
    groupKeys.each { key ->
        def rows = groupRowsMap[key]
        def criteria = key.split('#')

        // определить alias колонок
        def columnSubAlias = criteria[2] + criteria[3] // 1year / 1_5year / 5_10year / 10year + 100 / 100_1000 / 1000
        def columnCount = 'count' + columnSubAlias
        def columnMin = 'min' + columnSubAlias
        def columnMax = 'max' + columnSubAlias

        // определить alias строк
        def rowSubAlias = criteria[0] + '_' + criteria[4] // 1-11 / 12-22 / 23-26 + 1/2/3
        def aliasForCount = rowSubAlias + 'count'
        def aliasForMinMax = rowSubAlias + 'minmax'

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

    // графа 1
    def  numberedRows = templateRows.findAll { it.getAlias() != 'header' && it.getAlias() != 'subHeader' }
    numberedRows.each{ row ->
        row.rowNum = ++rowNum
    }

    // удалить алиасы строк
    templateRows.each { row ->
        row.setAlias(null)
    }

    return templateRows
}

@Field
FormTemplate formTemplate = null

/** Получить начальные строки макета. */
def getTemplateRows() {
    if (formTemplate == null) {
        formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    }
    return formTemplate?.clone()?.rows
}

def isFix(def row) {
    def rateType = (row.rateType ? getRefBookValue(72L, row.rateType)?.CODE?.value : null)
    return rateType == 'fix'
}

def getCurrencyCode(def row) {
    return (row.currencyCode ? getRefBookValue(15L, row.currencyCode)?.CODE_2?.value : null)
}

void deleteAllRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = []
    formDataService.saveCachedDataRows(formData, logger)
}

@Field
def REPORT_COLUMN_COUNT = 28

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
    getTemplateRows() // заполняет formTemplate
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
        IOUtils.copy(new FileInputStream(file), scriptSpecificReportHolder.getFileOutputStream())
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
    cell.setCellValue("Внутренние интерквартильные интервалы ставок / Внутренние интервалы ставок / Внутренние рыночные ставки (в % годовых) для проверки рыночности совокупных процентных ставок по договорам о предоставлении Кредитных продуктов и Субординированных кредитов, заключенным Банком с его Взаимозависимыми лицами и Резидентами Оффшорных зон, с использованием внутренних источников информации")
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