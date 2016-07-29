package form_template.income.rnu32_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-32.1) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 1".
 * formTemplateId=330
 *
 * @author rtimerbaev
 */

// графа    - fix
// графа 1  - number                - зависит от графы 2 - атрибут 166 - SBRF_CODE - «Код подразделения в нотации Сбербанка», справочник 30 «Подразделения»
// графа 2  - name                  - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 3  - code                  - зависит от графы 5 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 4  - issuer                - зависит от графы 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 5  - regNumber             - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
// графа 6  - shortPositionData
// графа 7  - faceValue
// графа 8  - countsBonds
// графа 9  - averageWeightedPrice
// графа 10 - termBondsIssued
// графа 11 - maturityDate
// графа 12 - currentPeriod
// графа 13 - currentCouponRate
// графа 14 - incomeCurrentCoupon
// графа 15 - incomePrev
// графа 16 - incomeShortPosition
// графа 17 - percIncome
// графа 18 - totalPercIncome

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

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'name', 'code', 'issuer', 'regNumber', 'shortPositionData', 'faceValue',
                  'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate', 'currentPeriod', 'currentCouponRate',
                  'incomeCurrentCoupon', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']

// Редактируемые атрибуты (графа 2, 5..14)
@Field
def editableColumns = ['name', 'regNumber', 'shortPositionData', 'faceValue',
                       'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate',
                       'currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 2..8, 10..14, 17, 18)
@Field
def nonEmptyColumns = ['name', /*'issuer',*/ 'regNumber', 'shortPositionData',
                       'faceValue', 'countsBonds', 'termBondsIssued', 'maturityDate', 'currentPeriod',
                       'currentCouponRate', 'incomeCurrentCoupon', 'percIncome', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 8, 15..18)
@Field
def totalSumColumns = ['countsBonds', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']

// список алиасов подразделов
@Field
        sections = ['1', '2', '3', '4', '5', '6', '7', '8']

@Field
def taxPeriod = null

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

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(
        def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
        boolean required) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

def addRow() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def index
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'total1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
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

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 15
        row.incomePrev = calc15(row, lastDay, dataRows)
        // графа 16
        row.incomeShortPosition = calc16(row, lastDay, dataRows)
        // графа 17
        row.percIncome = calc17(row, lastDay)
        // графа 18
        row.totalPercIncome = calc18(row)
    }

    sort(dataRows)
    updateIndexes(dataRows)

    calcTotal(dataRows)

    sortFormDataRows(false)
}

void calcTotal(def dataRows) {
    // посчитать итоги по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalSumColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // алиасы графов для арифметической проверки (графа 15..18)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()

        // 2. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Арифметическая проверка графы 15..18
        needValue['incomePrev'] = calc15(row, lastDay, dataRows)
        needValue['incomeShortPosition'] = calc16(row, lastDay, dataRows)
        needValue['percIncome'] = calc17(row, lastDay)
        needValue['totalPercIncome'] = calc18(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 4. Арифметическая проверка итоговых значений по разделам (графа 8, 15..18)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def alias : totalSumColumns) {
            def value = roundValue(lastRow.getCell(alias).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, alias, firstRow, lastRow), 6)
            if (sum != value) {
                def name = getColumnName(lastRow, alias)
                rowError(logger, lastRow, "Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
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
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    calcTotal(dataRows)

    updateIndexes(dataRows)
}

// Отсорировать данные (по графе 1, 2).
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 1
        if (from < to) {
            sortRows.add(dataRows.subList(from, to))
        }
    }

    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)

            def recordA = getRefBookValue(30, a.name)
            def recordB = getRefBookValue(30, b.name)

            def numberA = (recordA != null ? recordA.SBRF_CODE.value : null)
            def numberB = (recordB != null ? recordB.SBRF_CODE.value : null)

            if (numberA == numberB) {
                def nameA = (recordA != null ? recordA.NAME.value : null)
                def nameB = (recordB != null ? recordB.NAME.value : null)
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
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
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

// Получить новую строку с заданными стилями.
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
 * Получить значение для графы 15.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param dataRows строки нф
 */
def calc15(def row, def lastDay, def dataRows) {
    if (row.shortPositionData == null || row.maturityDate == null) {
        return null
    }
    def tmp = null
    if (row.shortPositionData < row.maturityDate) {
        def t = lastDay - row.maturityDate
        tmp = calc15or16(row, lastDay, t, dataRows)
    }
    return roundValue(tmp, 2)
}

/**
 * Получить значение для графы 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param dataRows строки нф
 */
def calc16(def row, def lastDay, def dataRows) {
    if (row.shortPositionData == null || row.maturityDate == null) {
        return null
    }
    def tmp = null
    if (row.shortPositionData >= row.maturityDate) {
        def t = lastDay - row.shortPositionData
        tmp = calc15or16(row, lastDay, t, dataRows)
    }
    return roundValue(tmp, 2)
}

/**
 * Общая часть вычислений при расчете значения для графы 15 или 16.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 * @param t количество дней между последним днем месяца и графой 6 или графой 11
 * @param dataRows строки нф
 */
def calc15or16(def row, def lastDay, def t, def dataRows) {
    if (row.getIndex() < getDataRow(dataRows, '7').getIndex()) {
        if (row.currentPeriod == null || row.currentPeriod == 0 ||
                row.countsBonds == null || row.incomeCurrentCoupon == null) {
            return null
        }
        // Для ценных бумаг, учтенных в подразделах 1, 2, 3, 4, 5, 6
        return row.countsBonds * roundValue(row.incomeCurrentCoupon * t / row.currentPeriod, 2)
    } else {
        // Для ценных бумаг, учтенных в подразделах 7 и 8
        if (row.regNumber == null || row.currentCouponRate == null || row.faceValue == null) {
            return null
        }

        def rate = getRate(row, lastDay)
        if (rate == null) {
            return null
        }
        return roundValue(row.currentCouponRate * row.faceValue * t / 360, 2) * rate
    }
}

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def lastDay) {
    if (row.regNumber == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def record15Id = getRefBookValue(84, row.regNumber)?.CODE_CUR?.value

    def code = getRefBookValue(15, record15Id)?.CODE?.value
    if (code in ['810', '643']) {
        return 1
    }
    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = getRefBookRecord(22, 'CODE_NUMBER', record15Id?.toString(), lastDay, row.getIndex(), null, true)
    return record22?.RATE?.value
}

/**
 * Получить значение для графы 17.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 */
def calc17(def row, def lastDay) {
    if (row.countsBonds == null || row.termBondsIssued == null || row.faceValue == null ||
            row.averageWeightedPrice == null || row.shortPositionData == null || row.regNumber == null ||
            row.termBondsIssued == 0 || row.countsBonds == 0) {
        return null
    }
    def tmp = ((row.faceValue / row.countsBonds - row.averageWeightedPrice) *
            ((lastDay - row.shortPositionData) / row.termBondsIssued) * row.countsBonds)
    tmp = roundValue(tmp, 2)

    def rate = getRate(row, lastDay)
    if (rate == null) {
        return null
    }
    tmp = tmp * rate
    return roundValue(tmp, 2)
}

def calc18(def row) {
    if (row.percIncome == null) {
        return null
    }
    def tmp = (row.incomePrev ?: 0) + (row.incomeShortPosition ?: 0) + row.percIncome
    return roundValue(tmp, 2)
}

// Получить сумму столбца.
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

// Округляет число до требуемой точности
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
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

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 18, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    def mapRows = [:]

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = getNewRow()

        // графа 2 - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
        xmlIndexCol = 2
        def record30 = getRecordImport(30, 'NAME', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        newRow.name = record30?.record_id?.value

        // графа 1 - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
        if (record30 != null) {
            xmlIndexCol = 1
            formDataService.checkReferenceValue(30, row.cell[xmlIndexCol].text(), record30?.SBRF_CODE?.value, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 5 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
        // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
        // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
        xmlIndexCol = 5
        def record84 = getRecordImport(84, 'REG_NUM', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        newRow.regNumber = record84?.record_id?.value

        // графа 3 - зависит от графы 5 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
        xmlIndexCol = 3
        def record15 = getRecordImport(15, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        if (record84 != null && record15 != null) {
            def value1 = record15?.record_id?.value?.toString()
            def value2 = record84?.CODE_CUR?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // графа 4 - зависит от графы 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        xmlIndexCol = 4
        def record100 = getRecordImport(100, 'FULL_NAME', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, false)
        if (record84 != null && record100 != null) {
            def value1 = record100?.record_id?.value?.toString()
            def value2 = record84?.ISSUER?.value?.toString()
            formDataService.checkReferenceValue(84, value1, value2, rnuIndexRow, xmlIndexCol + colOffset, logger, false)
        }

        // Графа 6
        xmlIndexCol = 6
        newRow.shortPositionData = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // графа 7..10
        ['faceValue', 'countsBonds', 'averageWeightedPrice', 'termBondsIssued'].each { alias ->
            xmlIndexCol++
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        // Графа 11
        xmlIndexCol = 11
        newRow.maturityDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        // графа 12..18
        ['currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon', 'incomePrev', 'incomeShortPosition',
         'percIncome', 'totalPercIncome'].each { alias ->
            xmlIndexCol++
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        // Техническое поле(группа)
        xmlIndexCol = 19
        sectionIndex = row.cell[xmlIndexCol].text()

        if (mapRows[sectionIndex] == null) {
            mapRows[sectionIndex] = []
        }
        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)
    dataRows.each { row ->
        if (row.getAlias()?.contains('total')) {
            totalSumColumns.each {
                row[it] = null
            }
        }
    }

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, 'total' + section).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    calcTotal(dataRows)
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, "total$section")
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1
        def sectionRows = (from < to ? dataRows.subList(from, to) : [])

        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, sectionRows, columnNameList)

        sortRowsSimple(sectionRows)
    }

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('number').column.name
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

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def sectionIndex = null
    def mapRows = [:]
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
        if (firstValue != null && firstValue != '' && firstValue != 'Итого:') {
            sectionIndex = firstValue[0]
            if (!(sectionIndex in sections) || getDataRow(templateRows, sectionIndex)?.fix != firstValue) {
                logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            mapRows.put(sectionIndex, [])
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue == 'Итого:') {
            rowIndex++
            totalRowFromFileMap[sectionIndex] = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            // Пропуск итоговых строк
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionIndex].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def rows = []
    sections.each { section ->
        def headRow = getDataRow(templateRows, section)
        def totalRow = getDataRow(templateRows, 'total' + section)
        rows.add(headRow)
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)
        }
        rows.add(totalRow)

        // сравнение итогов
        updateIndexes(rows)
        def totalRowFromFile = totalRowFromFileMap[section]
        compareSimpleTotalValues(totalRow, totalRowFromFile, copyRows, totalSumColumns, formData, logger, false)
    }
    updateIndexes(rows)

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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            // название первого столбца хранится в нулевой скрытой графе
            ([(headerRows[0][0]) : tmpRow.getCell('number').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('name').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('code').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('issuer').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('regNumber').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('shortPositionData').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('faceValue').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('countsBonds').column.name]),
            ([(headerRows[0][9]) : tmpRow.getCell('averageWeightedPrice').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('termBondsIssued').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('maturityDate').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('currentPeriod').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('currentCouponRate').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('incomeCurrentCoupon').column.name]),
            ([(headerRows[0][15]): tmpRow.getCell('incomePrev').column.name]),
            ([(headerRows[0][16]): tmpRow.getCell('incomeShortPosition').column.name]),
            ([(headerRows[0][17]): tmpRow.getCell('percIncome').column.name]),
            ([(headerRows[0][18]): tmpRow.getCell('totalPercIncome').column.name])
    ]
    (1..18).each {
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

    // графа 2 - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
    def colIndex = 2
    def record30 = getRecordImport(30, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.name = record30?.record_id?.value

    // графа 1 - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
    if (record30 != null) {
        colIndex = 1
        formDataService.checkReferenceValue(30, values[colIndex], record30?.SBRF_CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 5 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочник 84 «Ценные бумаги»
    // TODO (Ramil Timerbaev) могут быть проблемы с нахождением записи,
    // если в справочнике 84 есть несколько записей с одинаковыми значениями в поле REG_NUM
    colIndex = 5
    def record84 = getRecordImport(84, 'REG_NUM', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.regNumber = record84?.record_id?.value

    // графа 3 - зависит от графы 5 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
    colIndex = 3
    def record15 = getRecordImport(15, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record15 != null) {
        def value1 = record15?.record_id?.value?.toString()
        def value2 = record84?.CODE_CUR?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4 - зависит от графы 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
    colIndex = 4
    def record100 = getRecordImport(100, 'FULL_NAME', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    if (record84 != null && record100 != null) {
        def value1 = record100?.record_id?.value?.toString()
        def value2 = record84?.ISSUER?.value?.toString()
        formDataService.checkReferenceValue(84, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // Графа 6
    colIndex = 6
    newRow.shortPositionData = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7..10
    ['faceValue', 'countsBonds', 'averageWeightedPrice', 'termBondsIssued'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // Графа 11
    colIndex++
    newRow.maturityDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12..18
    ['currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon', 'incomePrev', 'incomeShortPosition',
     'percIncome', 'totalPercIncome'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}