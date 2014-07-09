package form_template.income.rnu12.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 * formTemplateId=364
 *
 * графа 1  - rowNumber
 * графа 2  - code
 * графа 3  - numberFirstRecord
 * графа 4  - opy
 * графа 5  - operationDate
 * графа 6  - name
 * графа 7  - documentNumber
 * графа 8  - date
 * графа 9  - periodCounts
 * графа 10 - advancePayment
 * графа 11 - outcomeInNalog
 * графа 12 - outcomeInBuh
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
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
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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

// Редактируемые атрибуты
@Field
def editableColumns = ['numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber', 'date',
        'periodCounts', 'advancePayment', 'outcomeInBuh']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'code']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['numberFirstRecord', 'opy', 'operationDate', 'name', 'documentNumber',
        'date', 'periodCounts', 'advancePayment', 'outcomeInBuh']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

@Field
def start = null

@Field
def endDate = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRows.sort { getKnu(it.opy) }

        for (row in dataRows) {
            // графа 11
            row.outcomeInNalog = calc11(row)
        }

        calcSubTotal(dataRows)
    }

    dataRows.add(calcTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}

void calcSubTotal(def dataRows) {
    // посчитать "итого по коду"
    def totalRows = [:]
    def code = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    dataRows.eachWithIndex { row, i ->
        def knu = getKnu(row.opy)
        if (code == null) {
            code = knu
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (code != knu) {
            totalRows.put(i, getNewRow(code, sums))
            totalColumns.each {
                sums[it] = 0
            }
            code = knu
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == dataRows.size() - 1) {
            totalColumns.each {
                def val = row.getCell(it).getValue()
                if (val != null)
                    sums[it] += val
            }
            totalRows.put(i + 1, getNewRow(knu, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            def val = row.getCell(it).getValue()
            if (val != null)
                sums[it] += val
        }
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i++, row)
    }
}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Получить новую строку.
def getNewRow(def alias, def sums) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it], null)
    }
    return newRow
}


def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 9
    ['rowNumber', 'fix', 'code', 'numberFirstRecord', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def BigDecimal calc11(def row) {
    if (row.advancePayment != null && row.advancePayment > 0 && row.periodCounts != null && row.periodCounts != 0) {
        return (row.advancePayment / row.periodCounts).setScale(2, RoundingMode.HALF_UP)
    }
    return null
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    // календарная дата начала отчетного периода
    def startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    // дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['outcomeInNalog']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка даты совершения операции и границ отчетного периода (графа 5)
        if (row.operationDate != null && (row.operationDate.after(endDate) || row.operationDate.before(startDate))) {
            rowError(logger, row, errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 4. Проверка количества отчетных периодов при авансовых платежах (графа 9)
        if (row.periodCounts != null && (row.periodCounts < 1 || 999 < row.periodCounts)) {
            rowError(logger, row, errorMsg + 'Неверное количество отчетных периодов при авансовых платежах!')
        }

        // 5. Проверка на нулевые значения (графа 11, 12)
        if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
            rowError(logger, row, errorMsg + 'Все суммы по операции нулевые!')
        }

        // 6. Проверка формата номера первой записи
        if (row.numberFirstRecord != null && !row.numberFirstRecord.matches('\\d{2}-\\w{6}')) {
            rowError(logger, row, errorMsg + 'Неправильно указан номер первой записи (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!')
        }

        needValue['outcomeInNalog'] = calc11(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // Арифметическая проверка итоговых значений строк «Итого по КНУ»
    checkSubTotalSum(dataRows, totalColumns, logger, true)

    // Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(27, code)?.CODE?.stringValue
}

def getStartDate() {
    if (!start) {
        start = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Код налогового учёта',
            (xml.row[0].cell[3]): 'Номер первой записи',
            (xml.row[0].cell[4]): 'Символ ОПУ (номер)',
            (xml.row[0].cell[5]): 'Дата совершения операции',
            (xml.row[0].cell[6]): 'Наименование операции',
            (xml.row[0].cell[7]): 'Первичный документ',
            (xml.row[0].cell[9]): 'Количество кварталов при авансовых платежах',
            (xml.row[0].cell[10]): 'Сумма авансового платежа',
            (xml.row[0].cell[11]): 'Сумма расхода в налоговом учёте',
            (xml.row[0].cell[12]): 'Сумма расхода в бухгалтерском учёте',
            (xml.row[1].cell[7]): 'Номер',
            (xml.row[1].cell[8]): 'Дата',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[2]): '2',
            (xml.row[2].cell[3]): '3',
            (xml.row[2].cell[4]): '4',
            (xml.row[2].cell[5]): '5',
            (xml.row[2].cell[6]): '6',
            (xml.row[2].cell[7]): '7',
            (xml.row[2].cell[8]): '8',
            (xml.row[2].cell[9]): '9',
            (xml.row[2].cell[10]): '10',
            (xml.row[2].cell[11]): '11',
            (xml.row[2].cell[12]): '12'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1

        // графа 2

        // графа 3
        newRow.numberFirstRecord = row.cell[3].text()

        // графа 4
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(OPU) = LOWER('" + row.cell[4].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        if (records.size() == 1) {
            newRow.opy = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        } else {
            logger.error("Проверка файла: Строка ${xlsIndexRow} содержит значение, отсутствующее в справочнике " +
                    "«" + refBookFactory.get(27).getName() + "»!")
        }

        // графа 5
        newRow.operationDate = parseDate(row.cell[5].text(), "dd.MM.yyyy", xlsIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.name = row.cell[6].text()

        // графа 7
        newRow.documentNumber = row.cell[7].text()

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.periodCounts = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, true)

        // графа 10
        newRow.advancePayment = parseNumber(row.cell[10].text(), xlsIndexRow, 10 + colOffset, logger, true)

        // графа 11
        newRow.outcomeInNalog = parseNumber(row.cell[11].text(), xlsIndexRow, 11 + colOffset, logger, true)

        // графа 12
        newRow.outcomeInBuh = parseNumber(row.cell[12].text(), xlsIndexRow, 12 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 3
        newRow.numberFirstRecord = row.cell[3].text()

        // графа 4
        String filter = "LOWER(CODE) = LOWER('" + row.cell[2].text() + "') and LOWER(OPU) = LOWER('" + row.cell[4].text() + "')"
        def records = refBookFactory.getDataProvider(27).getRecords(reportPeriodEndDate, null, filter, null)
        if (records.size() == 1) {
            newRow.opy = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        } else {
            logger.error("Проверка файла: Строка ${rnuIndexRow} содержит значение, отсутствующее в справочнике " +
                    "«" + refBookFactory.get(27).getName() + "»!")
        }

        // графа 5
        newRow.operationDate = parseDate(row.cell[5].text(), "dd.MM.yyyy", rnuIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.name = row.cell[6].text()

        // графа 7
        newRow.documentNumber = row.cell[7].text()

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", rnuIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.periodCounts = parseNumber(row.cell[9].text(), rnuIndexRow, 9 + colOffset, logger, true)

        // графа 10
        newRow.advancePayment = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)

        // графа 11
        newRow.outcomeInNalog = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)

        // графа 12
        newRow.outcomeInBuh = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        rows.add(newRow)
    }
    calcSubTotal(rows)
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 10
        total.advancePayment = parseNumber(row.cell[10].text(), rnuIndexRow, 10 + colOffset, logger, true)

        // графа 11
        total.outcomeInNalog = parseNumber(row.cell[11].text(), rnuIndexRow, 11 + colOffset, logger, true)

        // графа 12
        total.outcomeInBuh = parseNumber(row.cell[12].text(), rnuIndexRow, 12 + colOffset, logger, true)

        def colIndexMap = ['advancePayment' : 10, 'outcomeInNalog' : 11, 'outcomeInBuh' : 12]
        for (def alias : totalColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }
    dataRowHelper.save(rows)
}