package form_template.income.rnu32_1.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-32.1) Регистр налогового учёта начисленного процентного дохода по облигациям, по которым открыта короткая позиция. Отчёт 1".
 * formTemplateId=330
 *
 * TODO:
 *      - миграция еще не сделаны
 *      - невозможно проверить форму пока не будет заполнен справочник 84 "Ценные бумаги"
 *
 * @author rtimerbaev
 */

// графа 0  - fix
// графа 1  - number                - Номер территориального банка          - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name                  - Наименование территориального банка   - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - code                  - Код валюты номинала                   - атрибут 810 - CODE_CUR - "Цифровой код валюты выпуска", справочник 84 "Ценные бумаги"
// графа 4  - issuer                - Эмитент                               - атрибут 809 - ISSUER - "Эмитент", справочник 84 "Ценные бумаги"
// графа 5  - regNumber             - Номер государственной регистрации     - атрибут 813 - REG_NUM - "Государственный регистрационный номер", справочник 84 "Ценные бумаги"
// графа 6  - shortPositionData     - Дата открытия короткой позиции
// графа 7  - faceValue             - Номинальная стоимость (ед. валюты)
// графа 8  - countsBonds           - Количество облигаций (шт.)
// графа 9  - averageWeightedPrice  - Средневзвешенная цена одной облигации на дату, когда выпуск признан размещенным (ед. вал.)
// графа 10 - termBondsIssued       - Срок обращения выпуска (дни)
// графа 11 - maturityDate          - Дата погашения предыдущего купона
// графа 12 - currentPeriod         - Текущий купонный период (дни)
// графа 13 - currentCouponRate     - Ставка текущего купона, (% год.)
// графа 14 - incomeCurrentCoupon   - Объявленный доход по текущему купону (руб.коп.)
// графа 15 - incomePrev            - Доход с даты погашения предыдущего купона (руб.коп.)
// графа 16 - incomeShortPosition   - Доход с даты открытия короткой позиции (руб.коп.)
// графа 17 - percIncome            - Процентный доход по дисконтным облигациям (руб.коп.)
// графа 18 - totalPercIncome       - Всего процентный доход (руб.коп.)

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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.MIGRATION :
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

// Редактируемые атрибуты (графа 2..14)
@Field
def editableColumns = ['name', 'issuer', 'shortPositionData', 'faceValue',
            'countsBonds', 'averageWeightedPrice', 'termBondsIssued', 'maturityDate',
            'currentPeriod', 'currentCouponRate', 'incomeCurrentCoupon']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 2..8, 10..14, 17, 18)
@Field
def nonEmptyColumns = ['name', 'issuer', 'shortPositionData',
        'faceValue', 'countsBonds', 'termBondsIssued', 'maturityDate', 'currentPeriod',
        'currentCouponRate', 'incomeCurrentCoupon', 'percIncome', 'totalPercIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 8, 15..18)
@Field
def totalSumColumns = ['countsBonds', 'incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']

// список алиасов подразделов
@Field
sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//// Обертки методов

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
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def lastDay = getMonthEndDate()
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

    // посчитать итоги по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalSumColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // алиасы графов для арифметической проверки (графа 15..18)
    def arithmeticCheckAlias = ['incomePrev', 'incomeShortPosition', 'percIncome', 'totalPercIncome']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def lastDay = getMonthEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

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
                logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
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
    dataRows.removeAll(deleteRows)
    // поправить индексы, потому что они после изменения не пересчитываются
    updateIndexes(dataRows)


    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
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

/** Отсорировать данные (по графе 1, 2). */
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
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)

            def recordA = getRefBookValue(30, a.name)
            def recordB = getRefBookValue(30, b.name)

            def numberA = (recordA != null ? recordA.SBRF_CODE.value : null)
            def numberB = (recordB != null ? recordB.SBRF_CODE.value : null)

            def nameA = (recordA != null ? recordA.NAME.value : null)
            def nameB = (recordB != null ? recordB.NAME.value : null)

            if (numberA == numberB) {
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

/** Получить новую строку с заданными стилями. */
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
    if (row.shortPositionData < row.maturityDate) {
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
    def code = getRefBookValue(84L, row.issuer)?.CODE_CUR
    def recordId = getRecordId(15, 'CODE', code.toString(), 1, "1", lastDay)
    if (row.getIndex() < getDataRow(dataRows, '7').getIndex()) {
        if (row.currentPeriod == null || row.currentPeriod == 0 ||
                row.countsBonds == null || row.incomeCurrentCoupon == null) {
            return null
        }
        // Для ценных бумаг, учтенных в подразделах 1, 2, 3, 4, 5, 6
        return row.countsBonds * roundValue(row.incomeCurrentCoupon * t / row.currentPeriod, 2)
    } else {
        if (code == null || row.currentCouponRate == null || row.faceValue == null) {
            return null
        }
        // Для ценных бумаг, учтенных в подразделах 7 и 8
        // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
        def record22 = getRefBookRecord(22, 'CODE_NUMBER', recordId.toString(), lastDay, row.getIndex(), getColumnName(row, 'code'), true)
        return roundValue(row.currentCouponRate * row.faceValue * t / 360, 2) * record22.RATE.value
    }
}

/**
 * Получить значение для графы 17.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 */
def calc17(def row, def lastDay) {
    def code = getRefBookValue(84L, row.issuer)?.CODE_CUR
    def recordId = getRecordId(15, 'CODE', code.toString(), 1, "1", lastDay)
    if (row.countsBonds == null || row.termBondsIssued == null || row.faceValue == null ||
            row.averageWeightedPrice == null || row.shortPositionData == null || code == null ||
            row.termBondsIssued == 0 || row.countsBonds == 0) {
        return null
    }
    def tmp = ((row.faceValue / row.countsBonds - row.averageWeightedPrice) *
            ((lastDay - row.shortPositionData) / row.termBondsIssued) * row.countsBonds)
    tmp = roundValue(tmp, 2)

    // справочник 22 "Курс валют", атрибут 81 RATE - "Курс валют", атрибут 80 CODE_NUMBER - Цифровой код валюты
    def record22 = getRefBookRecord(22, 'CODE_NUMBER', recordId.toString(), lastDay, row.getIndex(), getColumnName(row, 'code'), true)
    tmp = tmp * record22.RATE.value
    return roundValue(tmp, 2)
}

def calc18(def row) {
    if (row.incomePrev == null || row.incomeShortPosition == null || row.percIncome == null) {
        return null
    }
    return roundValue(row.incomePrev + row.incomeShortPosition + row.percIncome, 2)
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

// Округляет число до требуемой точности
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getMonthEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder)?.time
    }
    return reportPeriodEndDate
}

/**
 * Импорт
 */

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Номер территориального банка', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): 'Номер территориального банка',
            (xml.row[0].cell[1]): 'Наименование территориального банка',
            (xml.row[0].cell[2]): 'Код валюты номинала',
            (xml.row[0].cell[3]): 'Эмитент',
            (xml.row[0].cell[4]): 'Номер государственной регистрации',
            (xml.row[0].cell[5]): 'Дата открытия короткой позиции',
            (xml.row[0].cell[6]): 'Номинальная стоимость (ед. валюты)',
            (xml.row[0].cell[7]): 'Количество облигаций (шт.)',
            (xml.row[0].cell[8]): 'Средневзвешенная цена одной облигации на дату, когда выпуск признан размещенным (ед. вал.)',
            (xml.row[0].cell[9]): 'Срок обращения выпуска (дни)',
            (xml.row[0].cell[10]): 'Дата погашения предыдущего купона',
            (xml.row[0].cell[11]): 'Текущий купонный период (дни)',
            (xml.row[0].cell[12]): 'Ставка текущего купона, (% год.)',
            (xml.row[0].cell[13]): 'Объявленный доход по текущему купону (руб.коп.)',
            (xml.row[0].cell[14]): 'Доход с даты погашения предыдущего купона (руб.коп.)',
            (xml.row[0].cell[15]): 'Доход с даты открытия короткой позиции (руб.коп.)',
            (xml.row[0].cell[16]): 'Процентный доход по дисконтным облигациям (руб.коп.)',
            (xml.row[0].cell[17]): 'Всего процентный доход (руб.коп.)',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[1].cell[3]): '4',
            (xml.row[1].cell[4]): '5',
            (xml.row[1].cell[5]): '6',
            (xml.row[1].cell[6]): '7',
            (xml.row[1].cell[7]): '8',
            (xml.row[1].cell[8]): '9',
            (xml.row[1].cell[9]): '10',
            (xml.row[1].cell[10]): '11',
            (xml.row[1].cell[11]): '12',
            (xml.row[1].cell[12]): '13',
            (xml.row[1].cell[13]): '14',
            (xml.row[1].cell[14]): '15',
            (xml.row[1].cell[15]): '16',
            (xml.row[1].cell[16]): '17',
            (xml.row[1].cell[17]): '18',
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

/* Заполнить форму данными */

void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1
    def int rowOffset = 10
    def int colOffset = 1

    def int rowIndex = 1
    def rows = []

    // Индекс для вставки данных по соответствующим фиксированным строкам
    def index = -1

    // Очищаем форму от старых данных
    for (def row : dataRows) {
        if (row.fix != null) {
            rows.add(row)
        }
    }

    for (def row : xml.row) {

        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset


        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        index++

        /* Пропуск итоговых строк */
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        /* Графа 2 */
        def nameId = getRecordIdImport(30, 'NAME', row.cell[1].text(), xlsIndexRow, 1 + colOffset)
        newRow.name = nameId

        /* Проверка зависимой графы 1 */
        checkImportReferenceColumn(30, nameId, refBookCache, 'SBRF_CODE', row.cell[0].text(), xlsIndexRow, colOffset, true)

        /* Графа 4 */
        issuerId = getRecordIdImport(84, 'ISSUER', row.cell[3].text(), xlsIndexRow, 3 + colOffset)
        newRow.issuer = issuerId

        /* Проверка зависимой графы 3 */
        checkImportReferenceColumn(84, issuerId, refBookCache, 'CODE_CUR', row.cell[2].text(), xlsIndexRow, 2 + colOffset, true)

        /* Проверка зависимой графы 5 */
        checkImportReferenceColumn(84, issuerId, refBookCache, 'REG_NUM', row.cell[4].text(), xlsIndexRow, 4 + colOffset, true)

        /* Графа 6 */
        newRow.shortPositionData = parseDate(row.cell[5].text(), "dd.MM.yyyy", xlsIndexRow, 5 + colOffset, logger, false)

        /* Графа 7 */
        newRow.faceValue = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, false)

        /* Графа 8 */
        newRow.countsBonds = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, false)

        /* Графа 9*/
        newRow.averageWeightedPrice = parseNumber(row.cell[8].text(), xlsIndexRow, 8 + colOffset, logger, false)

        /* Графа 10 */
        newRow.termBondsIssued = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, false)

        /* Графа 11 */
        newRow.maturityDate = parseDate(row.cell[10].text(), "dd.MM.yyyy", xlsIndexRow, 10 + colOffset, logger, false)

        /* Графа 12 */
        newRow.currentPeriod = parseNumber(row.cell[11].text(), xlsIndexRow, 11 + colOffset, logger, false)

        /* Графа 13 */
        newRow.currentCouponRate = parseNumber(row.cell[12].text(), xlsIndexRow, 12 + colOffset, logger, false)

        /* Графа 14 */
        newRow.incomeCurrentCoupon = parseNumber(row.cell[13].text(), xlsIndexRow, 13 + colOffset, logger, false)

        rows.add(index, newRow)
    }

    dataRowHelper.save(rows)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

/**
 * Проверка зависимых граф
 *
 * @param refBookId идентификатор справочника
 * @param recordId идентификатор записи
 * @param refBookCache кэш
 * @param recordAttributeName (GString) название атрибута записи
 * @param value значение, полученное из excel
 * @param rowNumber номер строки для логгера
 * @param colNumber номер столбца для логгера
 * @param isFatal управление фатальностью
 * @return
 */
def checkImportReferenceColumn(def refBookId, def recordId, def refBookCache, def recordAttributeName, def value, def rowNumber, def colNumber, def isFatal) {

    def record = formDataService.getRefBookValue(refBookId, recordId, refBookCache)
    if (record != null) {
        if ((value != null && !value.isEmpty() && !value.equals(record[recordAttributeName]?.stringValue))
                || ((value == null || value.isEmpty()) && record[recordAttributeName]?.stringValue != null)) {
            def message = "Проверка файла: Строка ${rowNumber}, столбец ${colNumber} " +
                    "содержит значение, отсутствующее в справочнике «" + refBookFactory.get(refBookId).getName() + "»!"

            if (isFatal) {
                logger.error(message)
            } else {
                logger.warn(message)
            }
        }
    }
}
