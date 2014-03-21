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
 *
 * Графа    fix
 * Графа 1  currencyCode        Код валюты номинала               - зависит от графы 2 - атрибут 810 CODE_CUR «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
 * Графа 2  issuer              Эмитент                           - атрибут 809 ISSUER «Эмитент», справочника 84 «Ценные бумаги»
 * Графа 3  regNumber           Номер государственной регистрации - зависит от графы 2 - атрибут 813 REG_NUM «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
 * Графа 4  amount              Количество облигаций (шт.)
 * Графа 5  cost                Номинальная стоимость лота (руб.коп.)
 * Графа 6  shortPositionOpen   Дата открытия короткой позиции
 * Графа 7  shortPositionClose  Дата закрытия короткой позиции
 * Графа 8  pkdSumOpen          Сумма ПКД полученного при открытии короткой позиции
 * Графа 9  pkdSumClose         Сумма ПКД уплаченного при закрытии короткой позиции
 * Графа 10 maturityDatePrev    Дата погашения предыдущего купона
 * Графа 11 maturityDateCurrent Дата погашения текущего купона
 * Графа 12 currentCouponRate   Ставка текущего купона (% годовых)
 * Графа 13 incomeCurrentCoupon Объявленный доход по текущему купону (руб.коп.)
 * Графа 14 couponIncome        Выплачиваемый купонный доход (руб.коп.)
 * Графа 15 totalPercIncome     Всего процентный доход (руб.коп.)
 */

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
def editableColumns = ['issuer', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']

// Обязательно заполняемые атрибуты (графа 2, 4..7, 10..15 - 7 графа необязательная для раздела А)
// TODO (Ramil Timerbaev) неясности в чтз про графу 8, 9
@Field
def nonEmptyColumns = [/*"issuer",*/ "amount", "cost", "shortPositionOpen",  // TODO Левыкин: Раскомментировать после появления значений в справочнике
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
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows += row
        }
    }
    dataRows.removeAll(deleteRows)
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }

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
    destinationRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
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
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Код валюты номинала', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 15, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): 'Код валюты номинала',
            (xml.row[0].cell[2]): 'Эмитент',
            (xml.row[0].cell[3]): 'Номер государственной регистрации',
            (xml.row[0].cell[4]): 'Количество облигаций (шт.)',
            (xml.row[0].cell[5]): 'Номинальная стоимость лота (руб.коп.)',
            (xml.row[0].cell[6]): 'Дата открытия короткой позиции',
            (xml.row[0].cell[7]): 'Дата закрытия короткой позиции',
            (xml.row[0].cell[8]): 'Сумма ПКД полученного при открытии короткой позиции',
            (xml.row[0].cell[9]): 'Сумма ПКД уплаченного при закрытии короткой позиции',
            (xml.row[0].cell[10]): 'Дата погашения предыдущего купона',
            (xml.row[0].cell[11]): 'Дата погашения текущего купона',
            (xml.row[0].cell[12]): 'Ставка текущего купона (% годовых)',
            (xml.row[0].cell[13]): 'Объявленный доход по текущему купону (руб.коп.)',
            (xml.row[0].cell[14]): 'Выплачиваемый купонный доход (руб.коп.)',
            (xml.row[0].cell[15]): 'Всего процентный доход (руб.коп.)',
            (xml.row[1].cell[1]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
            (xml.row[1].cell[4]): '4',
            (xml.row[1].cell[5]): '5',
            (xml.row[1].cell[6]): '6',
            (xml.row[1].cell[7]): '7',
            (xml.row[1].cell[8]): '8',
            (xml.row[1].cell[9]): '9',
            (xml.row[1].cell[10]): '10',
            (xml.row[1].cell[11]): '11',
            (xml.row[1].cell[12]): '12',
            (xml.row[1].cell[13]): '13',
            (xml.row[1].cell[14]): '14',
            (xml.row[1].cell[15]): '15'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными.
def addData(def xml, int headRowCount) {
    endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

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
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

    def rows = dataRowHelper.allCached
    //удаляем все нефиксированные строки
    def deleteList = []
    rows.each {
        if (it.getAlias() == null){
            deleteList.add(it)
        }
    }
    rows.removeAll(deleteList)

    def int rowIndex = 1  // Строки НФ, от 1

    def group
    def subGroup
    def sectionRowsMap = [:]
    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if (groupsMap.get(row.cell[0].text()) != null) {
            group = groupsMap.get(row.cell[0].text())
            continue
        }

        if (subGroupsMap.get(row.cell[0].text()) != null) {
            subGroup = subGroupsMap.get(row.cell[0].text())
            continue
        }

        //Пропуск пустых и итоговых строк
        if (row.cell[0].text() || !(row.cell[1].text())) {
            continue
        }

        def xmlIndexCol = 0

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.keySet().each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        xmlIndexCol++
        // графа 1 зависима от 2-й
        getRecordIdImport(84, 'CODE_CUR', row.cell[xmlIndexCol].text(),  xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 2
        newRow.issuer = getRecordIdImport(84, 'ISSUER', row.cell[xmlIndexCol].text(),  xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 3 зависима от 2-й
        getRecordIdImport(84, 'REG_NUM', row.cell[xmlIndexCol].text(),  xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 4
        newRow.amount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 5
        newRow.cost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 6
        newRow.shortPositionOpen = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 7
        newRow.shortPositionClose = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 8
        newRow.pkdSumOpen = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 9
        newRow.pkdSumClose = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 10
        newRow.maturityDatePrev = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 11
        newRow.maturityDateCurrent = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 12
        newRow.currentCouponRate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 13
        newRow.incomeCurrentCoupon = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 14
        newRow.couponIncome = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        // графа 15
        newRow.totalPercIncome = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++

        if (group && subGroup) {
            def section = group + subGroup
            if (sectionRowsMap.get(section) != null) {
                sectionRowsMap.get(section).add(newRow)
            } else {
                ArrayList<DataRow> newList = new ArrayList<DataRow>()
                newList.add(newRow)
                sectionRowsMap.put(section, newList)
            }
        }
    }
    sectionRowsMap.keySet().each { sectionKey ->
        rows.addAll(getDataRow(rows, sectionKey).getIndex(), sectionRowsMap.get(sectionKey))
        rows.eachWithIndex { row, i ->
            row.setIndex(i + 1)
        }
    }
    dataRowHelper.save(rows)
}