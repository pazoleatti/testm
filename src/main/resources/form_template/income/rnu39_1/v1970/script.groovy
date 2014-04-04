package form_template.income.rnu39_1.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-39.1) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 1(месячный)
 * formTemplateId=336
 *
 * Очень похожа с формой РНУ-39.2 - чтз одинаковое, различие в составе подразделов
 *
 * @author bkinzyabulatov
 * @author Dmitriy Levykin
 */

// графа    - fix
// графа 1  - currencyCode        Код валюты номинала               - зависит от графы 2 - атрибут 810 CODE_CUR «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
// графа 2  - issuer              Эмитент                           - атрибут 809 ISSUER «Эмитент», справочника 84 «Ценные бумаги»
// графа 3  - regNumber           Номер государственной регистрации - зависит от графы 2 - атрибут 813 REG_NUM «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
// графа 4  - amount              Количество облигаций (шт.)
// графа 5  - cost                Номинальная стоимость лота (руб.коп.)
// графа 6  - shortPositionOpen   Дата открытия короткой позиции
// графа 7  - shortPositionClose  Дата закрытия короткой позиции
// графа 8  - pkdSumOpen          Сумма ПКД полученного при открытии короткой позиции
// графа 9  - pkdSumClose         Сумма ПКД уплаченного при закрытии короткой позиции
// графа 10 - maturityDatePrev    Дата погашения предыдущего купона
// графа 11 - maturityDateCurrent Дата погашения текущего купона
// графа 12 - currentCouponRate   Ставка текущего купона (% годовых)
// графа 13 - incomeCurrentCoupon Объявленный доход по текущему купону (руб.коп.)
// графа 14 - couponIncome        Выплачиваемый купонный доход (руб.коп.)
// графа 15 - totalPercIncome     Всего процентный доход (руб.коп.)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Поля, для которых подсчитываются итоговые значения (графа 4, 5, 8, 9, 14, 15)
@Field
def totalColumns = ["amount", "cost", "pkdSumOpen", "pkdSumClose", "couponIncome", "totalPercIncome"]

// Редактируемые атрибуты (графа 2, 4..13)
@Field
def editableColumns = ['issuer', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose', 'pkdSumOpen',
        'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']

// Обязательно заполняемые атрибуты (графа 2, 4..7, 10..15 - 7 графа необязательная для раздела А)
// TODO (Ramil Timerbaev) неясности в чтз про графу 8, 9
@Field
def nonEmptyColumns = ["issuer", "amount", "cost", "shortPositionOpen",
        "shortPositionClose", /*"pkdSumOpen", "pkdSumClose",*/ "maturityDatePrev", "maturityDateCurrent",
        "currentCouponRate", "incomeCurrentCoupon", "couponIncome", "totalPercIncome"]

@Field
def autoFillColumns = ["couponIncome", "totalPercIncome"]

@Field
def groups = ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, false)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def reportDateNextDay = dTo + 1
    def nonEmptyColumnsA = nonEmptyColumns - 'shortPositionClose' // 7 графа необязательная для раздела А

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        def isInASector = isInASector(dataRows, row)

        // 1. Обязательность заполнения полей (графа 2, 4..7, 10..15)
        checkNonEmptyColumns(row, index, (isInASector ? nonEmptyColumnsA : nonEmptyColumns), logger, true)

        // 2. Проверка даты первой части сделки
        if (row.shortPositionOpen > reportDateNextDay) {
            logger.error(errorMsg + "Неверно указана дата первой части сделки!")
        }
        // 3. Проверка даты второй части сделки
        if ((isInASector && row.shortPositionClose != null) || (!isInASector && row.shortPositionClose > reportDateNextDay)) {
            logger.error(errorMsg + "Неверно указана дата второй части сделки!")
        }

        // 4. Арифметическая проверка вычислимых граф (графа  14, 15)
        def values = [:]
        values.couponIncome = calc14(dataRows, row, (dFrom..dTo))
        values.totalPercIncome = calc15(dataRows, row)
        checkCalc(row, autoFillColumns, values, logger, true)
    }

    // 5. Арифметическая проверка строк промежуточных итогов (Проверка итоговых значений формы)
    for (def section : groups) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        def columnNames = []
        for (def column : totalColumns) {
            if (lastRow[column] != getSum(dataRows, column, firstRow, lastRow)) {
                columnNames.add(getColumnName(lastRow, column))
            }
        }
        if (!columnNames.isEmpty()) {
            def index = lastRow.getIndex()
            def errorMsg = "Строка $index: "
            logger.error(errorMsg + "Неверно рассчитано итоговое значение граф: ${columnNames.join(', ')}!")
        }
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // отсортировать/группировать
    sort(dataRows)

    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()

    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
        if (row.getAlias() == null) {
            row.couponIncome = calc14(dataRows, row, (dFrom..dTo))
            row.totalPercIncome = calc15(dataRows, row)
        }
    }

    // расчет итогов
    groups.each { section ->
        firstRow = getDataRow(dataRows, section)
        lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each {
            lastRow[it] = getSum(dataRows, it, firstRow, lastRow)
        }
    }

    dataRowHelper.save(dataRows)
}

void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    DataRow<Cell> newRow = formData.createDataRow()
    newRow.keySet().each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = getDataRow(dataRows, 'totalA1')
        index = dataRows.indexOf(row)
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        def alias = currentDataRow.getAlias()
        def totalAlias = alias.contains('total') ? alias : 'total' + ((alias in ['A', 'B']) ? (alias + '1') : alias)
        def row = getDataRow(dataRows, totalAlias)
        index = dataRows.indexOf(row)
    }

    dataRowHelper.insert(newRow, index + 1)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    deleteNotFixedRows(dataRows)

    // Налоговый период
    def taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, taxPeriod.id, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceRows = formDataService.getDataRowHelper(source).getAll()
                // подразделы
                groups.each { section ->
                    copyRows(sourceRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Копировать заданный диапозон строк из источника в приемник
void copyRows(def sourceRows, def destinationRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceRows, fromAlias).getIndex()
    def to = getDataRow(sourceRows, toAlias).getIndex() - 1
    if (from > to) {
        return
    }

    def copyRows = sourceRows.subList(from, to)
    destinationRows.addAll(getDataRow(destinationRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationRows)
}

BigDecimal calc14(def dataRows, def row, def period) {
    def boolean condition = false
    def isInASector = isInASector(dataRows, row)
    if (isInASector && row.maturityDateCurrent != null && row.maturityDateCurrent in period) {
        condition = true
    } else if (!isInASector && row.maturityDateCurrent != null && row.shortPositionOpen != null && row.shortPositionClose != null &&
            row.maturityDateCurrent in (row.shortPositionOpen..row.shortPositionClose)) {
        condition = true
    }
    if (!condition) {
        return round(0)
    }

    def currencyCode = getRefBookValue(84, row.issuer)?.CODE_CUR?.stringValue
    if (currencyCode == '810') {
        if (row.amount == null || row.incomeCurrentCoupon == null) {
            return null
        }
        return round(row.amount * row.incomeCurrentCoupon)
    } else {
        if (row.currentCouponRate == null || row.cost == null || row.maturityDateCurrent == null ||
                row.maturityDatePrev == null || currencyCode == null) {
            return null
        }
        def String cellName = getColumnName(row, 'currencyCode')
        def recordId = formDataService.getRefBookRecordId(15, recordCache, providerCache, 'CODE', currencyCode,
                row.maturityDateCurrent, row.getIndex(), cellName, logger, true)
        def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', 
                "${recordId}", row.maturityDateCurrent, row.getIndex(), cellName, logger, true)
        def rate = record?.RATE?.numberValue
        if (rate == null) {
            return null
        }
        return round(row.currentCouponRate * row.cost * (row.maturityDateCurrent - row.maturityDatePrev) / 360 * rate)
    }
}

BigDecimal calc15(def dataRows, def row) {
    def tmp = null
    if (isInASector(dataRows, row)) {
        tmp = row.couponIncome
    } else if (row.pkdSumClose != null && row.couponIncome != null && row.pkdSumOpen != null) {
        tmp =  row.pkdSumClose + row.couponIncome - row.pkdSumOpen
    }
    return round(tmp)
}

boolean isInASector(def dataRows, def row) {
    def rowB = getDataRow(dataRows, 'B')
    return row.getIndex() < rowB.getIndex()
}

// Округление
BigDecimal round(BigDecimal value, int precision = 2) {
    return value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

// Получить сумму столбца
def getSum(def rows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

void sort(def dataRows) {
    def sortRows = []
    groups.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }
    sortRows.each {
        it.sort { DataRow a, DataRow b ->
            def recordA = getRefBookValue(84, a.issuer)
            def recordB = getRefBookValue(84, b.issuer)
            def valueA = recordA?.CODE_CUR?.value
            def valueB = recordB?.CODE_CUR?.value
            if (valueA != valueB) {
                return valueA <=> valueB
            } else {
                valueA = recordA?.ISSUER?.value
                valueB = recordB?.ISSUER?.value
                return valueA <=> valueB
            }
        }
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'currencyCode'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'currencyCode'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'issuer'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'regNumber'),
            (xml.row[0].cell[4]) : getColumnName(tmpRow, 'amount'),
            (xml.row[0].cell[5]) : getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'shortPositionOpen'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'shortPositionClose'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'pkdSumOpen'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'pkdSumClose'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'maturityDatePrev'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'maturityDateCurrent'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'currentCouponRate'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'incomeCurrentCoupon'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'couponIncome'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'totalPercIncome')
    ]

    (1..15).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными.
def addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def groupsMap = [
            'А. Открытые короткие позиции':'A',
            'Б. Закрытые короткие позиции':'B'
    ]
    def subGroupsMap = [
            '1. ОФЗ':'1',
            '2. Субфедеральные и муниципальные облигации, за исключением муниципальных облигаций, выпущенных до 1 января 2007 года на срок не менее 3 лет':'2',
            '3. Муниципальные облигации, выпущенные до 1 января 2007 года на срок не менее 3 лет':'3',
            '4. Государственные облигации Республики Беларусь':'4'
    ]

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def group = null
    def sectionIndex = null
    def sectionRowsMap = [:]
    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if (groupsMap.get(row.cell[0].text()) != null) {
            group = groupsMap.get(row.cell[0].text())
            continue
        }

        if (subGroupsMap.get(row.cell[0].text()) != null) {
            sectionIndex = group + subGroupsMap.get(row.cell[0].text())
            sectionRowsMap.put(sectionIndex, [])
            continue
        }

        //Пропуск пустых и итоговых строк
        if (row.cell[0].text() || !(row.cell[1].text())) {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.keySet().each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        // графа 2 - атрибут 809 ISSUER «Эмитент», справочника 84 «Ценные бумаги»
        def xmlIndexCol = 2
        def record84 = getRecordImport(84, 'ISSUER', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)
        newRow.issuer = record84?.record_id?.value

        if (record84 != null) {
            // графа 1 - зависит от графы 2 - атрибут 810 CODE_CUR «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
            xmlIndexCol = 1
            formDataService.checkReferenceValue(84, row.cell[xmlIndexCol].text(), record84?.CODE_CUR?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, false)

            // графа 3 - зависит от графы 2 - атрибут 813 REG_NUM «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
            xmlIndexCol = 3
            formDataService.checkReferenceValue(84, row.cell[xmlIndexCol].text(), record84?.REG_NUM?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 4
        xmlIndexCol = 4
        newRow.amount = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 5
        newRow.cost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        newRow.shortPositionOpen = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 7
        newRow.shortPositionClose = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        newRow.pkdSumOpen = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        newRow.pkdSumClose = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 10
        newRow.maturityDatePrev = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 11
        newRow.maturityDateCurrent = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 12
        newRow.currentCouponRate = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 13
        newRow.incomeCurrentCoupon = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        sectionRowsMap.get(sectionIndex).add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    groups.each { section ->
        def copyRows = sectionRowsMap[section]
        if (copyRows) {
            def insertIndex = getDataRow(dataRows, 'total' + section).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            updateIndexes(dataRows)
        }
    }
    dataRowHelper.save(dataRows)
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}