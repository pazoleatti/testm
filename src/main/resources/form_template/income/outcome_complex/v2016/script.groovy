package form_template.income.outcome_complex.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * formTypeId = 303
 * formTemplateId = 1303
 */

// графа 1  - consumptionTypeId
// графа 2  - consumptionGroup
// графа 3  - consumptionTypeByOperation
// графа 4  - consumptionBuhSumAccountNumber
// графа 5  - consumptionBuhSumRnuSource
// графа 6  - consumptionBuhSumAccepted
// графа 7  - consumptionBuhSumPrevTaxPeriod
// графа 8  - consumptionTaxSumRnuSource
// графа 9  - consumptionTaxSumS
// графа 10 - rnuNo
// графа 11 - logicalCheck
// графа 12 - accountingRecords
// графа 13 - opuSumByEnclosure3
// графа 14 - opuSumByTableP
// графа 15 - opuSumTotal
// графа 16 - table2
// графа 17 - difference

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

// Проверяемые на пустые значения атрибуты (графа 9)
@Field
def nonEmptyColumns = ['consumptionTaxSumS']

//Аттрибуты, очищаемые перед импортом формы (графа 9, 16)
@Field
def resetColumns = ['consumptionTaxSumS', 'table2']

@Field
def totalColumn = 'consumptionTaxSumS'

@Field
def headerAlias1 = 'R1'

@Field
def headerAlias2 = 'R54'

// Алиас для первой строки итогов
@Field
def totalRowAlias1 = 'R53'

// Алиас для второй строки итогов
@Field
def totalRowAlias2 = 'R87'

@Field
def skipAliases = [ headerAlias1, headerAlias2, totalRowAlias1, totalRowAlias2 ]

// Алиасы строк, по которым надо подвести итоги для первой итоговой строки
@Field
def rowsAliasesForSum1 = (2..52).collect { "R$it" as String } as List<String>

// Алиасы строк, по которым надо подвести итоги для второй итоговой строки
@Field
def rowsAliasesForSum2 = (55..86).collect { "R$it" as String } as List<String>

@Field
def formTypeId_RNU25 = 324
@Field
def formTypeId_RNU26 = 325
@Field
def formTypeId_RNU27 = 326
@Field
def formTypeId_TABLE2 = 852

@Field
def rnuFormTypeIds = [ formTypeId_RNU25, formTypeId_RNU26, formTypeId_RNU27 ]

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    BigDecimal tmpValue

    // итоговые строки
    def totalRow1 = getDataRow(dataRows, totalRowAlias1)
    tmpValue = getSum(dataRows, totalColumn, rowsAliasesForSum1)
    totalRow1[totalColumn] = checkOverflow(tmpValue, totalRow1, totalColumn, 15)

    def totalRow2 = getDataRow(dataRows, totalRowAlias2)
    tmpValue = getSum(dataRows, totalColumn, rowsAliasesForSum2)
    totalRow2[totalColumn] = checkOverflow(tmpValue, totalRow2, totalColumn, 15)
}

/**
 * Условия выполнения расчетов.
 * Проверка разрядности итоговых значении.
 *
 * @param value проверяемое значение
 * @param row строка
 * @param alias алиас столбца проверяемого значения
 * @param size размер числа
 * @return вернет null и запишет в лог фатальное сообщение - если разрядность нарушена, иначе вернет проверяемое значение
 */
def checkOverflow(BigDecimal value, def row, def alias, int size) {
    if (value == null) {
        return value
    }
    BigDecimal overpower = new BigDecimal("1E" + size)
    if (value.abs().compareTo(overpower) != -1) {
        String columnName = getColumnName(row, alias)
        def msg = "Строка %d: Значение графы «%s» превышает допустимую разрядность. Должно быть не более %d знакомест и не более 2 знаков после запятой. Устанавливаемое значение: %s"
        logger.error(msg, row.getIndex(), columnName, size, value)
        return null
    }
    return value
}

// Подсчет сумм для столбца colName в строках rowsAliases
BigDecimal getSum(def dataRows, def colName, def rowsAliases) {
    return rowsAliases.sum { rowAlias ->
        return getDataRow(dataRows, rowAlias)[colName] ?: BigDecimal.ZERO
    }
}

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        if (row.getAlias() in skipAliases) {
            continue
        }
        // 1. Проверка заполнения обязательных граф
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }

    // 2. Арифметическая проверка итоговых значений
    checkTotalSum(getDataRow(dataRows, totalRowAlias1), getSum(dataRows, totalColumn, rowsAliasesForSum1))
    checkTotalSum(getDataRow(dataRows, totalRowAlias2), getSum(dataRows, totalColumn, rowsAliasesForSum2))

    // 3. Проверка наличия данных в форме «Таблица 2» для заполнения графы 16
    // выполняется при консолидации
}

// Консолидация формы
def consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // очистить форму
    for (def row : dataRows) {
        if (row.getAlias() in [headerAlias1, headerAlias2]) {
            continue
        }
        if (row.getAlias() in [totalRowAlias1, totalRowAlias2]) {
            row.consumptionTaxSumS = BigDecimal.ZERO
            row.table2 = null
            continue
        }
        resetColumns.each { alias ->
            row[alias] = BigDecimal.ZERO
        }
    }

    // получение источников
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind, getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)

    // консолидация из рну или сводных
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)

    // консолидация из таблицы 2
    consolidationFromTable2(dataRows, formSources)
    addExplanationPrev(dataRows, 'consumptionTypeId')
}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId == formData.formType.id } != null
    def isPrimarySource = formSources.find { it.formTypeId in rnuFormTypeIds } != null
    if (isSummarySource && isPrimarySource) {
        logger.warn("Для текущей формы назначены формы-источники по двум видам консолидации: 1. Формы РНУ; 2. «Сводная форма начисленных расходов (расходы сложные)». Консолидация выполнена из форм-источников «Сводная форма начисленных расходов (расходы сложные)")
        return true
    } else if (isSummarySource || isPrimarySource) {
        return isSummarySource
    } else {
        logger.warn("Для текущей формы не назначены корректные формы-источники.")
        return true
    }
}

/** Консолидация из сводных "расходов сложных". */
void consolidationFromSummary(dataRows, formSources) {
    // отобрать сводные формы
    def summarySources = formSources.findAll { it.formTypeId == formData.formType.id }

    summarySources.each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            for (def sourceRow : formDataService.getDataRowHelper(source).allSaved) {
                if (sourceRow.getAlias() in [headerAlias1, headerAlias2]) {
                    continue
                }
                def row = getDataRow(dataRows, sourceRow.getAlias())

                // графа 9
                row.consumptionTaxSumS += (sourceRow.consumptionTaxSumS ?: BigDecimal.ZERO)
            }
        }
    }
}

/** Консолидация из рну и сводной формы "расходы сложные" предыдущего периода. */
void consolidationFromPrimary(dataRows, formSources) {
    def sumMap = [:]
    rnuFormTypeIds.each { typeId ->
        sumMap[typeId] = BigDecimal.ZERO
    }

    def rowForRnu25 = dataRows.find { it.consumptionTypeId == '26310' }
    def rowForRnu26 = dataRows.find { it.consumptionTypeId == '26320' }
    def rowForRnu27 = dataRows.find { it.consumptionTypeId == '26330' }

    // отобрать формы рну
    def rnuSources = formSources.findAll { it.formTypeId in rnuFormTypeIds }

    // значения из рну
    rnuSources.each {
        def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (source != null && source.state == WorkflowState.ACCEPTED) {
            def sourceRows = formDataService.getDataRowHelper(source).allSaved
            def sourceTotalRow = sourceRows.find { it.getAlias() == 'total' }
            sumMap[it.formTypeId] += (sourceTotalRow?.reserveCreation ?: BigDecimal.ZERO)
        }
    }

    rowForRnu25.consumptionTaxSumS += sumMap[formTypeId_RNU25]
    rowForRnu26.consumptionTaxSumS += sumMap[formTypeId_RNU26]
    rowForRnu27.consumptionTaxSumS += sumMap[formTypeId_RNU27]

    // значения из предыдущего периода
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (reportPeriod.order != 1) {
        def prevFormData = formDataService.getFormDataPrev(formData)
        if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
            def prevDataRows = formDataService.getDataRowHelper(prevFormData)?.allSaved

            def prevRowForRnu25 = prevDataRows.find { it.consumptionTypeId == '26310' }
            def prevRowForRnu26 = prevDataRows.find { it.consumptionTypeId == '26320' }
            def prevRowForRnu27 = prevDataRows.find { it.consumptionTypeId == '26330' }

            rowForRnu25.consumptionTaxSumS += (prevRowForRnu25?.consumptionTaxSumS ?: BigDecimal.ZERO)
            rowForRnu26.consumptionTaxSumS += (prevRowForRnu26?.consumptionTaxSumS ?: BigDecimal.ZERO)
            rowForRnu27.consumptionTaxSumS += (prevRowForRnu27?.consumptionTaxSumS ?: BigDecimal.ZERO)
        }
    }
}

/** консолидация из формы "таблица 2". */
void consolidationFromTable2(def dataRows, def formSources) {
    // отобрать форму "таблица 2"
    def formSource = formSources.find { it.formTypeId == formTypeId_TABLE2 }
    if (formSource == null) {
        return
    }
    def source = formDataService.getLast(formSource.formTypeId, formSource.kind, formSource.departmentId,
            formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)

    if (source != null && source.state == WorkflowState.ACCEPTED) {
        def indexes = dataRows.findAll { !(it.getAlias() in skipAliases) }
        for (def sourceRow : formDataService.getDataRowHelper(source).allSaved) {
            def code = getRefBookValue(27, sourceRow.code)?.CODE?.value
            def row = (code ? dataRows.find { it.consumptionTypeId == code } : null)
            if (!code || !row) {
                continue
            }
            indexes.remove(row)

            // графа 16
            row.table2 += (sourceRow.sum ?: BigDecimal.ZERO)
        }

        // Логическая проверка 3. Проверка наличия данных в форме «Таблица 2» для заполнения графы 16
        if (!indexes.isEmpty()) {
            def subMsg = indexes.collect { it.getIndex() }.join(', ')
            def columnName16 = getColumnName(dataRows[0], 'table2')
            def template = formDataService.getFormTemplate(formTypeId_TABLE2, formData.reportPeriodId)
            def table2Name = (template?.name ?: 'Таблица 2. Пояснение отклонений от ОФР в простом регистре налогового учёта «Расходы»')
            logger.warn("Строки %s: Для заполнения графы «%s» не найдены строки по требуемым КНУ в форме-источнике «%s»!",
                    subMsg, columnName16, table2Name)
        }
    }
}

void checkTotalSum(totalRow, sum){
    if (totalRow[totalColumn] != sum) {
        logger.error('Итоговое значение в строке %d рассчитано неверно в графе «%s»!',
                totalRow.getIndex(), getColumnName(totalRow, totalColumn))
    }
}

void importData() {
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'КНУ'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[i]
        fileRowIndex++
        rowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            break
        }
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def alias = "R" + rowIndex
        def dataRow = getDataRow(dataRows, alias)
        // заполнить строку нф значениями из эксель
        if (alias in [totalRowAlias1, totalRowAlias2]) {
            // итоги
            fillTotalRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        } else {
            // остальные строки
            fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        }
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }

    // сравнение итогов
    def totalRow1Tmp = formData.createStoreMessagingDataRow()
    def totalRow2Tmp = formData.createStoreMessagingDataRow()
    totalRow1Tmp[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSum1)
    totalRow2Tmp[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSum2)

    def totalRow1 = getDataRow(dataRows, totalRowAlias1)
    def totalRow2 = getDataRow(dataRows, totalRowAlias2)
    compareTotalValues(totalRow1, totalRow1Tmp, [totalColumn], logger, false)
    compareTotalValues(totalRow2, totalRow2Tmp, [totalColumn], logger, false)

    showMessages(dataRows, logger)
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows) {
    checkHeaderSize(headerRows, 10, 3)

    def headerMapping = [
            ([(headerRows[0][0]) : 'КНУ']),
            ([(headerRows[0][1]) : 'Группа расхода']),
            ([(headerRows[0][2]) : 'Вид расхода по операции']),
            ([(headerRows[0][3]) : 'Расход по данным бухгалтерского учёта']),
            ([(headerRows[0][7]) : 'Расход по данным налогового учёта']),
            ([(headerRows[1][3]) : 'номер счёта учёта']),
            ([(headerRows[1][4]) : 'источник информации в РНУ']),
            ([(headerRows[1][5]) : 'сумма']),
            ([(headerRows[1][6]) : 'в т.ч. учтено в предыдущих налоговых периодах']),
            ([(headerRows[1][7]) : 'источник информации в РНУ']),
            ([(headerRows[1][8]) : 'сумма']),
            ([(headerRows[1][9]) : 'форма РНУ'])
    ]
    (0..9).each { index ->
        headerMapping.add([(headerRows[2][index]): (index + 1).toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // очищаем столбцы
    resetColumns.each { alias ->
        dataRow[alias] = null
    }

    def knu = normalize(getOwnerValue(dataRow, 'consumptionTypeId'))
    def group = normalize(getOwnerValue(dataRow, 'consumptionGroup'))
    //type = normalize(dataRow.consumptionTypeByOperation)
    def num = normalize(getOwnerValue(dataRow, 'consumptionBuhSumAccountNumber'))


    // графа 1
    def colIndex = 0
    def knuImport = normalize(values[colIndex])

    // графа 2
    colIndex++
    def groupImport = normalize(values[colIndex])

    // графа 3
    colIndex++
    // def typeImport = normalize(values[colIndex])

    // графа 4
    colIndex++
    def numImport = normalize(values[colIndex])

    // если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
    // то продолжаем обработку строки иначе пропускаем строку
    if (!((knu == knuImport && group == groupImport && num == numImport) ||
            ((!knuImport.isEmpty() || !groupImport.isEmpty() || !numImport.isEmpty()) &&
                    knu.contains(knuImport) && group.contains(groupImport) && num.contains(numImport)))) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu.")
        return
    }

    // графа 9
    colIndex = 8
    if (dataRow.getCell('consumptionTaxSumS').isEditable()) {
        dataRow.consumptionTaxSumS = parseNumber(values[colIndex].trim(), fileRowIndex, colIndex + colOffset, logger, true)
    }
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillTotalRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // графа 9
    def colIndex = 8
    dataRow.consumptionTaxSumS = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
}

/**
 * Добавить "Пояснение" из формы пред периода
 * @param dataRows строки формы
 */
void addExplanationPrev(def dataRows, def codeAlias) {
    // Отчётный период.
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    if (reportPeriod.order == 1) {
        return
    }
    // Форма предыдущего периода
    def formDataOld = formDataService.getFormDataPrev(formData)
    if (formDataOld == null || formDataOld.state != WorkflowState.ACCEPTED) {
        return
    }
    def dataRowsPrev = formDataService.getDataRowHelper(formDataOld).allSaved
    for (row in dataRows) {
        if (!(row.getAlias() in skipAliases)) {
            if (dataRowsPrev != null && !(dataRowsPrev.isEmpty())) {
                def prevRow = dataRowsPrev.find { it[codeAlias] == row[codeAlias] }
                row.table2 = (row.table2 ?: BigDecimal.ZERO) + (prevRow?.table2 ?: BigDecimal.ZERO)
            }
        }
    }
}