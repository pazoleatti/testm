package form_template.income.rnu36_1.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 * formTemplateId=333
 *
 * @author rtimerbaev
 */

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2              атрибут 350 - NUMBER - "Номер", справочник 28 "Классификатор доходов Сбербанка России для целей налогового учёта"
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

// все атрибуты
@Field
def allColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Редактируемые атрибуты (графа 1..7)
@Field
def editableColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..8)
@Field
def nonEmptyColumns = ['series', 'amount', 'nominal', 'shortPositionDate', 'balance2', 'averageWeightedPrice', 'termBondsIssued', 'percIncome']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 2, 8)
@Field
def totalColumns = ['amount', 'percIncome']

// Дата окончания отчетного периода
@Field
def endDate = null

// Текущая дата
@Field
def currentDate = new Date()

@Field
def providerCache = [:]

@Field
def recordCache = [:]

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'totalA').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex()
    } else {
        index = getDataRow(dataRows, 'totalA').getIndex()
        switch (currentDataRow.getAlias()) {
            case 'A':
            case 'totalA':
                index = getDataRow(dataRows, 'totalA').getIndex()
                break
            case 'B':
            case 'totalB':
            case 'total':
                index = getDataRow(dataRows, 'totalB').getIndex()
                break
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // последний день отчетного месяца
    def lastDay = getReportPeriodEndDate()

    dataRows.each { row ->
        if (row.getAlias() == null) {
            // графа 8
            row.percIncome = calcPercIncome(row, lastDay)
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def rowA = getDataRow(dataRows, 'A')
    def rowB = getDataRow(dataRows, 'B')
    def totalRowA = getDataRow(dataRows, 'totalA')
    def totalRowB = getDataRow(dataRows, 'totalB')
    totalColumns.each { alias ->
        totalRowA.getCell(alias).setValue(getSum(dataRows, rowA, totalRowA, alias), null)
        totalRowB.getCell(alias).setValue(getSum(dataRows, rowB, totalRowB, alias), null)
    }
    // посчитать Итого
    getDataRow(dataRows, 'total').percIncome = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // последний день отчетного месяца
    def lastDay = getReportPeriodEndDate()
    // отчетная дата
    def reportDay = reportPeriodService.getMonthReportDate(formData.reportPeriodId, formData.periodOrder)?.time

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // . Проверка обязательных полей (графа 2..8)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        if (row.shortPositionDate > reportDay) {
            logger.error(errorMsg + 'Неверно указана дата приобретения (открытия короткой позиции)!')
        }

        // 2. Арифметическая проверка графы 8
        def needValue = [:]
        needValue.percIncome = calcPercIncome(row, lastDay)
        checkCalc(row, ['percIncome'], needValue, logger, false)
    }

    // 3. Проверка итоговых значений по разделу А
    def rowA = getDataRow(dataRows, 'A')
    def totalRowA = getDataRow(dataRows, 'totalA')
    for (def alias : totalColumns) {
        def tmpA = getSum(dataRows, rowA, totalRowA, alias)
        if (totalRowA.getCell(alias).value != tmpA) {
            logger.error("Итоговые значений для раздела A рассчитаны неверно!")
            break
        }
    }

    // 4. Проверка итоговых значений по разделу Б
    def totalRowB = getDataRow(dataRows, 'totalB')
    def rowB = getDataRow(dataRows, 'B')
    for (def alias : totalColumns) {
        def tmpB = getSum(dataRows, rowB, totalRowB, alias)
        if (totalRowB.getCell(alias).value != tmpB) {
            logger.error("Итоговые значений для раздела Б рассчитаны неверно!")
            break
        }
    }

    // 5. Проверка итоговых значений по всей форме
    def total = (totalRowA.percIncome ?: 0) - (totalRowB.percIncome ?: 0)
    if (getDataRow(dataRows, 'total').percIncome != total) {
        logger.error('Итоговые значения рассчитаны неверно!')
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
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def taxPeriodId = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.id
            def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, taxPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Посчитать значение графы 8
def calcPercIncome(def row, def lastDay) {
    if (row.termBondsIssued == null || row.termBondsIssued == 0 ||
            lastDay == null || row.nominal == null || row.averageWeightedPrice == null ||
            row.shortPositionDate == null || row.amount == null) {
        return null
    }
    def tmp = ((row.nominal - row.averageWeightedPrice) *
            (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
    return roundValue(tmp, 2)
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
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Получить сумму для указанной графы.
 *
 * @param dataRows строки
 * @param labelRow строка начала раздела
 * @param totalRow строка итогов раздела
 * @param alias алиас графы для которой суммируются данные
 */
def getSum(def dataRows, def labelRow, def totalRow, def alias) {
    def from = labelRow.getIndex()
    def to = totalRow.getIndex() - 2
    // если нет строк в разделе то в итоги 0
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(alias, from, to))
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr, null, 2)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта
    // получить все строки
    def rows = dataRowHelper.allCached
    rows = rows.grep { it.getAlias() != null }

    int rowIndex = 0  // Строки НФ, от 1
    // индекс для перебора, с учетом пропуска шапки + отсутпа
    def i = headRowCount

    // проверим что первая строка соответствует шаблону формы
    if (xml.row[++i].cell[0].text() != "А. Облигации в портфеле банка") {
        logger.error('Не верный шаблон налоговой формы. Первая строка должна соответствовать строке "А. Облигации в портфеле банка" ')
        return;
    } else {
        // переход к следующей строке
        rowIndex++
    }

    // добавить спроки для группы А
    while (i < xml.row.size() && xml.row[++i].cell[0].text() != 'Итого "А"') {
        def newRow = createNewRow(rowIndex)
        rows.add(rowIndex, newRow)
        filForm(newRow, xml.row[i], i, colOffset)
        // переход к следующей строке
        rowIndex++
    }

    // Итого «А»
    if (i >= xml.row.size()) {
        logger.error('Не верный шаблон налоговой формы. Не найдены итоги для части А')
        return;
    } else {
        // переход к следующей строке формы
        rowIndex++
        // переход к следующей строке xml'ки
        i++
    }

    // проверим что первая строка соответствует шаблону формы
    if (xml.row[i].cell[0].text() != "Б. Облигации, по которым открыта короткая позиция") {
        logger.error('Не верный шаблон налоговой формы. Не найдена часть Б. Облигации, по которым открыта короткая позиция')
        return;
    } else {
        // переход к следующей строке формы
        rowIndex++
    }

    // добавим строки в часть Б
    while (i < xml.row.size() && xml.row[++i].cell[0].text() != 'Итого "Б"') {
        def newRow = createNewRow(rowIndex)
        rows.add(rowIndex, newRow)
        filForm(newRow, xml.row[i], i, colOffset)
        // переход к следующей строке формы
        rowIndex++
    }

    dataRowHelper.save(rows)
}

/**
 * Создание новой строки с проставленными стилями
 * @param rowIndex
 * @return
 */
def createNewRow(def rowIndex) {
    def newRow = formData.createDataRow()
    newRow.setIndex(rowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    newRow
}

/**
 * Считать данные в форму
 */
def filForm(def newRow, def row, int i, int colOffset) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    // графа 1
    newRow.series = row.cell[0].text()
    // графа 2
    newRow.amount = parseNumber(row.cell[1].text(), i, colOffset, logger, false)
    // графа 3
    newRow.nominal = parseNumber(row.cell[2].text(), i, colOffset, logger, false)
    // графа 4
    newRow.shortPositionDate = parseDate(row.cell[3].text(), "dd.MM.yyyy", i, colOffset, logger, false)
    //  графа 5
    newRow.balance2 = formDataService.getRefBookRecordIdImport(28L, recordCache, providerCache, 'NUMBER', row.cell[4].text(), reportPeriodEndDate, newRow.getIndex(), 5, logger, true)
    // графа 6
    newRow.averageWeightedPrice = parseNumber(row.cell[5].text(), i, colOffset, logger, false)
    // графа 7
    newRow.termBondsIssued = parseNumber(row.cell[6].text(), i, colOffset, logger, false)
    // графа 8
    newRow.percIncome = parseNumber(row.cell[7].text(), i, colOffset, logger, false)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('Серия', null)
    // проверка шапки таблицы
    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 8, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): 'Серия',
            (xml.row[0].cell[1]): 'Количество, шт.',
            (xml.row[0].cell[2]): 'Номинал, руб.',
            (xml.row[0].cell[3]): 'Дата приобретения (открытия короткой позиции)',
            (xml.row[0].cell[4]): 'Балансовый счёт второго порядка',
            (xml.row[0].cell[5]): 'Средневзвешенная цена одной облигации на дату, когда выпуск признан размещенным, руб.коп.',
            (xml.row[0].cell[6]): 'Срок обращения согласно условиям выпуска, дней',
            (xml.row[0].cell[7]): 'Процентный доход с даты приобретения, руб.коп.',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[1].cell[3]): '4',
            (xml.row[1].cell[4]): '5',
            (xml.row[1].cell[5]): '6',
            (xml.row[1].cell[6]): '7',
            (xml.row[1].cell[7]): '8'
    ]
    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}