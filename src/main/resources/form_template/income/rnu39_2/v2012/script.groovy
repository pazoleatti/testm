package form_template.income.rnu39_2.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-39.2) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 2(квартальный)"
 * formTemplateId=337
 *
 * Очень похожа с формой РНУ-39.1 - чтз одинаковое, различие в составе подразделов
 *
 * @author rtimerbaev
 */

// графа    - fix
// графа 1  - currencyCode          - зависит от графы 3 - атрибут 810 CODE_CUR «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
// графа 2  - issuer                - зависит от графы 3 - атрибут 809 ISSUER «Эмитент», справочника 84 «Ценные бумаги»
// графа 3  - regNumber             - атрибут 813 REG_NUM «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
// графа 4  - amount
// графа 5  - cost
// графа 6  - shortPositionOpen
// графа 7  - shortPositionClose
// графа 8  - pkdSumOpen
// графа 9  - pkdSumClose
// графа 10 - maturityDatePrev
// графа 11 - maturityDateCurrent
// графа 12 - currentCouponRate
// графа 13 - incomeCurrentCoupon
// графа 14 - couponIncome
// графа 15 - totalPercIncome

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose',
                  'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate',
                  'incomeCurrentCoupon', 'couponIncome', 'totalPercIncome']

// Редактируемые атрибуты (графа 3..13)
@Field
def editableColumns = ['regNumber', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose', 'pkdSumOpen',
                       'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Обязательно заполняемые атрибуты (графа 3..13 - 7 графа необязательная для раздела А)
@Field
def nonEmptyColumns = ["regNumber", "amount", "cost", "shortPositionOpen",
                       "shortPositionClose", "pkdSumOpen", "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
                       "currentCouponRate", "incomeCurrentCoupon"]

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 5, 8, 9, 14, 15)
@Field
def totalColumns = ["amount", "cost", "pkdSumOpen", "pkdSumClose", "couponIncome", "totalPercIncome"]

// список алиасов подразделов
@Field
def sections = ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5']

@Field
def startDate = null

@Field
def endDate = null

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

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol, logger, true)
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        def alias = currentDataRow.getAlias()
        if (alias == 'A') {
            index = getDataRow(dataRows, 'totalA1').getIndex()
        } else if (alias == 'B') {
            index = getDataRow(dataRows, 'totalB1').getIndex()
        } else if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            index = getDataRow(dataRows, 'total' + alias).getIndex()
        }
    }
    dataRows.add(index - 1, getNewRow())
    formDataService.saveCachedDataRows(formData, logger)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // отсортировать/группировать
    sort(dataRows)

    def dateStart = getReportPeriodStartDate()
    def dateEnd = getReportPeriodEndDate()

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // для раздела А графа 7 необязательна
        def isA = isSectionA(dataRows, row)
        // графа 14
        row.couponIncome = calc14(row, dateStart, dateEnd, isA)
        // графа 15
        row.totalPercIncome = calc15(row, isA)
    }

    // посчитать итоги по разделам
    calcTotalRows(dataRows)

    // Сортировка групп и строк
    sortFormDataRows(false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time
    def dateStart = getReportPeriodStartDate()
    def dateEnd = getReportPeriodEndDate()

    // алиасы графов для арифметической проверки (графа 14, 15)
    def arithmeticCheckAlias = ['couponIncome', 'totalPercIncome']
    // 7 графа необязательная для раздела А
    def nonEmptyColumnsA = nonEmptyColumns - 'shortPositionClose'

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        def isA = isSectionA(dataRows, row)

        // 1. Проверка даты первой части сделки
        if (row.shortPositionOpen > reportDay) {
            rowError(logger, row, errorMsg + 'Неверно указана дата первой части сделки!')
        }

        // 2. Проверка даты второй части сделки
        // Графа 7 (раздел А) = не заполнена;
        // Графа 7 (раздел Б) - принадлежит отчётному периоду
        if ((isA && row.shortPositionClose != null) || (!isA && row.shortPositionClose > reportDay)) {
            rowError(logger, row, errorMsg + 'Неверно указана дата второй части сделки!')
        }

        // для раздела А графа 7 необязательна
        // 3. Обязательность заполнения поля графа 1..13
        checkNonEmptyColumns(row, index, (isA ? nonEmptyColumnsA : nonEmptyColumns), logger, true)

        // 4. Арифметическая проверка графы 14 и 15
        // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
        def needValue = [:]
        needValue.couponIncome = calc14(row, dateStart, dateEnd, isA)
        needValue.totalPercIncome = calc15(row, isA)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 5. Проверка итоговых значений для подраздела 1..5 раздела А и Б
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def col : totalColumns) {
            def value = lastRow.getCell(col).value ?: 0
            if (value != getSum(dataRows, col, firstRow, lastRow)) {
                def name = getColumnName(lastRow, col)
                def number = section[1]
                def sectionName = (section.contains('A') ? 'А' : 'Б')
                logger.error("Неверно рассчитаны итоговые значения для подраздела $number раздела $sectionName в графе \"$name\"!")
            }
        }
    }
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
}

/** Проверка принадлежит ли строка разделу A. */
def isSectionA(def dataRows, def row) {
    return row != null && row.getIndex() < getDataRow(dataRows, 'B').getIndex()
}

/** Получить сумму столбца. */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после изменения не пересчитываются
    updateIndexes(destinationDataRows)
}

/** Получить новую стролу с заданными стилями. */
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

/**
 * Получить значение для графы 14.
 *
 * @param row строка нф
 * @param dateStart дата начала отчетного периода
 * @param dateEnd дата окончания отчетного периода
 * @param isA принадлежит ли строка разделу A (для раздела А графа 7 необязательна)
 */
def calc14(def row, def dateStart, def dateEnd, def isA) {
    if (row.amount == null || row.cost == null || row.shortPositionOpen == null ||
            (!isA && row.shortPositionClose == null) || row.maturityDatePrev == null ||
            row.maturityDateCurrent == null || row.currentCouponRate == null || row.incomeCurrentCoupon == null) {
        return null
    }
    def tmp = 0
    if ((isA && dateStart <= row.maturityDateCurrent && row.maturityDateCurrent <= dateEnd) ||
            (!isA && row.shortPositionOpen <= row.maturityDateCurrent && row.maturityDateCurrent <= row.shortPositionClose)) {
        // справочник 15 "Общероссийский классификатор валют", атрибут 64 CODE - "Код валюты. Цифровой"
        def currencyCode = getCurrencyCode(row.regNumber)
        if (currencyCode in ['810', '643']) {
            tmp = row.amount * row.incomeCurrentCoupon
        } else {
            def rate = getRate(row, row.maturityDateCurrent)
            if (rate == null) {
                return null
            }
            def t = row.maturityDateCurrent - row.maturityDatePrev
            tmp = roundValue(row.currentCouponRate * row.cost * t / 360, 2) * rate
        }
    }
    return roundValue(tmp, 2)
}

/**
 * Получить значение для графы 15.
 *
 * @param row строка
 * @param isA принадлежит ли строка разделу A (для раздела А графа 7 необязательна)
 */
def calc15(def row, def isA) {
    if (row.couponIncome == null || row.pkdSumClose == null || row.pkdSumOpen == null) {
        return null
    }
    return (isA ? row.couponIncome : row.pkdSumClose + row.couponIncome - row.pkdSumOpen)
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Отсортировать данные (по графе 1, 2). */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }
    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - currencyCode (зависимое поле)
            // графа 2  - issuer (зависимое поле)
            def valueA = getCurrencyCode(a.regNumber)
            def valueB = getCurrencyCode(b.regNumber)
            if (valueA != valueB) {
                return valueA <=> valueB
            } else {
                valueA = getIssuerName(a.regNumber)
                valueB = getIssuerName(b.regNumber)
                return valueA <=> valueB
            }
        }
    }
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

/** Получить название эмитента. */
def getIssuerName(def record84Id) {
    def issuerId = getRefBookValue(84, record84Id?.toLong())?.ISSUER?.value
    return getRefBookValue(100, issuerId)?.FULL_NAME?.value
}

/** Получить буквенный код валюты. */
def getCurrencyCode(def record84Id) {
    def record15Id = getRefBookValue(84, record84Id?.toLong())?.CODE_CUR?.value
    return getRefBookValue(15, record15Id)?.CODE?.value
}

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def Date date) {
    if (row.regNumber == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def code = getRefBookValue(84, row.regNumber)?.CODE_CUR?.value

    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER',
            code?.toString(), date, row.getIndex(), getColumnName(row, 'currencyCode'), logger, true)
    return record22?.RATE?.value
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 15 + 1, 1)

    // загрузить данные
    addTransportData(xml)
}

void addTransportData(def xml) {
    def int rnuIndexRow = 2
    def int colOffset = 1

    // мапа для хранения строк по разделам, доступ к списку строк по алиасу раздела
    def sectionRowsMap = [:]
    sections.each {
        sectionRowsMap[it] = []
    }

    def totalTmp = formData.createDataRow()
    totalColumns.each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }
    for (def row : xml.row) {
        rnuIndexRow++

        def rnuIndexCol
        def newRow = getNewRow()

        // графа 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
        // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
        // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
        rnuIndexCol = 3
        def record84 = getRecordImport(84, 'REG_NUM', row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, false)
        newRow.regNumber = record84?.record_id?.value

        // графа 1 - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
        rnuIndexCol = 1
        def record15 = getRecordImport(15, 'CODE', row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, false)
        if (record84 != null && record15 != null) {
            def value1 = record15?.record_id?.value?.toString()
            def value2 = record84?.CODE_CUR?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, rnuIndexCol + colOffset, logger, false)
        }

        // графа 2 - зависит от графы 3 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        rnuIndexCol = 2
        def record100 = getRecordImport(100, 'FULL_NAME', row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset, false)
        if (record84 != null && record100 != null) {
            def value1 = record100?.record_id?.value?.toString()
            def value2 = record84?.ISSUER?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, rnuIndexCol + colOffset, logger, false)
        }

        // графа 4
        rnuIndexCol = 4
        newRow.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 5
        rnuIndexCol = 5
        newRow.cost = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 6
        rnuIndexCol = 6
        newRow.shortPositionOpen = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 7
        rnuIndexCol = 7
        newRow.shortPositionClose = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 8
        rnuIndexCol = 8
        newRow.pkdSumOpen = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 9
        rnuIndexCol = 9
        newRow.pkdSumClose = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 10
        rnuIndexCol = 10
        newRow.maturityDatePrev = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 11
        rnuIndexCol = 11
        newRow.maturityDateCurrent = getDate(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 12
        rnuIndexCol = 12
        newRow.currentCouponRate = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 13
        rnuIndexCol = 13
        newRow.incomeCurrentCoupon = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 14
        rnuIndexCol = 14
        newRow.couponIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 15
        rnuIndexCol = 15
        newRow.totalPercIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        totalColumns.each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }

        // раздел
        def section = (row.cell[16].text() == 'Б' ? 'B' : 'A') + row.cell[17].text()
        if (section in sections) {
            sectionRowsMap[section].add(newRow)
        }
    }

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    deleteNotFixedRows(dataRows)

    sections.each { section ->
        index = getDataRow(dataRows, section).getIndex()
        def rows = sectionRowsMap[section]
        if (rows) {
            dataRows.addAll(index, rows)
            updateIndexes(dataRows)
        }
    }

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 4
        rnuIndexCol = 4
        total.amount = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 5
        rnuIndexCol = 5
        total.cost = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 8
        rnuIndexCol = 8
        total.pkdSumOpen = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 9
        rnuIndexCol = 9
        total.pkdSumClose = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 14
        rnuIndexCol = 14
        total.couponIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        // графа 15
        rnuIndexCol = 15
        total.totalPercIncome = getNumber(row.cell[rnuIndexCol].text(), rnuIndexRow, rnuIndexCol + colOffset)

        def colIndexMap = ["amount": 4, "cost": 5, "pkdSumOpen": 8, "pkdSumClose": 9, "couponIncome": 14, "totalPercIncome": 15]

        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
            }
        }

    }
    calcTotalRows(dataRows)
}

// расчет итогов
void calcTotalRows(def dataRows) {
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // Массовое разыменование строк НФ
        def columnList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionsRows, columnList)

        sortRowsSimple(sectionsRows)
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 16
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('currencyCode').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def headerARow = getDataRow(templateRows, 'A')
    def headerBRow = getDataRow(templateRows, 'B')
    // мапа для хранения алиаса раздела (получить алиас раздела по заголовку раздела)
    def groupsMap = [
            (headerARow.fix): 'A',
            (headerBRow.fix): 'B'
    ]
    // мапа для хранения алиаса подраздела (получить алиас подраздела по заголовку подраздела)
    def subGroupsMap = [:]
    (1..5).each { index ->
        def row = getDataRow(templateRows, 'A' + index)
        subGroupsMap[row.fix] = index
    }

    def group = null
    def sectionIndex = null
    def sectionRowsMap = [:]
    def totalRowFromFileMap = [:]

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++

        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = rowValues[INDEX_FOR_SKIP]
        // определение группы
        if (groupsMap[firstValue] != null) {
            group = groupsMap[firstValue]

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // определение подгруппы
        if (subGroupsMap.get(firstValue) != null) {
            sectionIndex = group + subGroupsMap[firstValue]
            sectionRowsMap[sectionIndex] = []

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP] == "Итого") {
            totalRowFromFileMap[sectionIndex] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        sectionRowsMap[sectionIndex].add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    updateIndexes(templateRows)
    def rows = []
    // заголовок первого раздела
    rows.add(headerARow)
    sections.each { section ->
        if ('B1'.equals(section)) {
            // заголовок второго раздела
            rows.add(headerBRow)
        }
        def headRow = getDataRow(templateRows, section)
        def totalRow = getDataRow(templateRows, 'total' + section)
        rows.add(headRow)
        def copyRows = sectionRowsMap[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[section]
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : tmpRow.getCell('currencyCode').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('issuer').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('regNumber').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('amount').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('cost').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('shortPositionOpen').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('shortPositionClose').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('pkdSumOpen').column.name]),
            ([(headerRows[0][9]) : tmpRow.getCell('pkdSumClose').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('maturityDatePrev').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('maturityDateCurrent').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('currentCouponRate').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('incomeCurrentCoupon').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('couponIncome').column.name]),
            ([(headerRows[0][15]): tmpRow.getCell('totalPercIncome').column.name])
    ]
    (1..15).each {
        headerMapping.add(([(headerRows[1][it]): it.toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
    // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
    // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
    def colIndex = 3
    def record84 = getRecordImport(84, 'REG_NUM', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.regNumber = record84?.record_id?.value

    // графа 1 - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
    colIndex = 1
    def record15 = getRecordImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record15 != null) {
        def value1 = record15?.record_id?.value?.toString()
        def value2 = record84?.CODE_CUR?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 2 - зависит от графы 3 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
    colIndex = 2
    def record100 = getRecordImport(100, 'FULL_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record100 != null) {
        def value1 = record100?.record_id?.value?.toString()
        def value2 = record84?.ISSUER?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    newRow.amount = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5
    colIndex = 5
    newRow.cost = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6
    colIndex = 6
    newRow.shortPositionOpen = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex = 7
    newRow.shortPositionClose = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex = 8
    newRow.pkdSumOpen = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9
    colIndex = 9
    newRow.pkdSumClose = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 10
    colIndex = 10
    newRow.maturityDatePrev = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 11
    colIndex = 11
    newRow.maturityDateCurrent = getDate(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 12..15
    ['currentCouponRate', 'incomeCurrentCoupon', 'couponIncome', 'totalPercIncome'].each { alias ->
        colIndex++
        newRow[alias] = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    }

    return newRow
}