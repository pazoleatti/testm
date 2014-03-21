package form_template.income.rnu39_2.v1970

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
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
// графа 1  - currencyCode          - зависит от графы 2 - атрибут 810 CODE_CUR «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»
// графа 2  - issuer                - атрибут 809 ISSUER «Эмитент», справочника 84 «Ценные бумаги»
// графа 3  - regNumber             - зависит от графы 2 - атрибут 813 REG_NUM «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
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
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
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

// Все атрибуты
@Field
def allColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon', 'couponIncome', 'totalPercIncome']

// Редактируемые атрибуты (графа 2, 4..13)
@Field
def editableColumns = ['issuer', 'amount', 'cost', 'shortPositionOpen', 'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev', 'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Обязательно заполняемые атрибуты (графа 2, 4..7, 10..15 - 7 графа необязательная для раздела А)
// TODO (Ramil Timerbaev) неясности в чтз про графу 8, 9
@Field
def nonEmptyColumns = [/*"issuer",*/ "amount", "cost", "shortPositionOpen",  // TODO (Ramil Timerbaev): Раскомментировать после появления значений в справочнике
        "shortPositionClose", /*"pkdSumOpen", "pkdSumClose",*/ "maturityDatePrev", "maturityDateCurrent",
        "currentCouponRate", "incomeCurrentCoupon", "couponIncome", "totalPercIncome"]

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 5, 8, 9, 14, 15)
@Field
def totalColumns = ["amount", "cost", "pkdSumOpen", "pkdSumClose", "couponIncome", "totalPercIncome"]

// список алиасов подразделов
@Field
def sections = ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5']

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

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
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
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // отсортировать/группировать
    sort(dataRows)

    def dateStart = getReportPeriodStartDate()
    def dateEnd =  getReportPeriodEndDate()

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
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }

    dataRowHelper.save(dataRows)
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
            logger.error(errorMsg + 'Неверно указана дата первой части сделки!')
        }

        // 2. Проверка даты второй части сделки
        // Графа 7 (раздел А) = не заполнена;
        // Графа 7 (раздел Б) - принадлежит отчётному периоду
        if ((isA && row.shortPositionClose != null) || (!isA && row.shortPositionClose > reportDay)) {
            logger.error(errorMsg + 'Неверно указана дата второй части сделки!')
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        // поправить индексы, потому что они после изменения не пересчитываются
        updateIndexes(dataRows)
    }


    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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
    def newRow = formData.createDataRow()
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
            row.maturityDateCurrent == null || row.currentCouponRate == null || row.incomeCurrentCoupon == null ) {
        return null
    }
    def tmp = 0
    if ((isA && dateStart <= row.maturityDateCurrent && row.maturityDateCurrent <= dateEnd) ||
            (!isA && row.shortPositionOpen <= row.maturityDateCurrent && row.maturityDateCurrent <= row.shortPositionClose)) {
        // справочник 15 "Общероссийский классификатор валют", атрибут 64 CODE - "Код валюты. Цифровой"
        def currencyCode = getRefBookValue(84, row.issuer)?.CODE_CUR?.stringValue
        if (currencyCode == '810') {
            tmp = row.amount * row.incomeCurrentCoupon
        } else {
            def t = row.maturityDateCurrent - row.maturityDatePrev
            // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
            def recordId = formDataService.getRefBookRecordId(15, recordCache, providerCache, 'CODE', currencyCode,
                    getReportPeriodEndDate(), row.getIndex(), getColumnName(row, 'currencyCode'), logger, true)
            def record22 = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER',
                    "${recordId}", row.maturityDateCurrent, row.getIndex(), getColumnName(row, 'currencyCode'),
                    logger, true)
            if (record22 == null) {
                return null
            } else {
                tmp = roundValue(row.currentCouponRate * row.cost * t / 360, 2) * record22.RATE.value
            }
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
            // графа 2  - issuer (справочник 84)
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

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
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
            (xml.row[1].cell[15]): '15',
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
            '1. Ипотечные облигации, выпущенные до 1 января 2007 года':'1',
            '2. Ипотечные облигации, выпущенные после 1 января 2007 года':'2',
            '3. Корпоративные облигации':'3',
            '4. ОВГВЗ':'4',
            '5. Прочие еврооблигации':'5'
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