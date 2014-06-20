package form_template.income.rnu72.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * (РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными
 * formTypeId=358
 * TODO:
 *      - перед логическими проверками или расчетами проверка рну-71.1 - опечатка в чтз! надо будет проверять рну-4 для заполнения графы 6, а пока графа 6 заполняется в ручную
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 0  - forLabel - fix
// графа 2  - date
// графа 3  - nominal
// графа 4  - price
// графа 5  - income
// графа 6  - cost279
// графа 7  - costReserve
// графа 8  - loss
// графа 9  - profit

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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
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

// все атрибуты
@Field
def allColumns = ['number', 'forLabel', 'date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Редактируемые атрибуты (графа 2..7)
@Field
def editableColumns = ['date', 'nominal', 'price', 'income', 'cost279', 'costReserve']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['date', 'nominal']

// Проверяемые на пустые значения атрибуты (графа 1..9)
@Field
def nonEmptyColumns = ['number', 'date', 'nominal', 'price', 'income', 'cost279', 'costReserve', 'loss', 'profit']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 5..9)
@Field
def totalSumColumns = ['income', 'cost279', 'costReserve', 'loss', 'profit']

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = ++index
        def tmp = calcFor8or9(row)
        // графа 8
        row.loss = calc8(tmp)
        // графа 9
        row.profit = calc9(tmp)
    }

    // добавить итого
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.forLabel = 'Итого'
    totalRow.getCell('forLabel').setColSpan(2)
    allColumns.each { alias ->
        totalRow.getCell(alias).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalSumColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def arithmeticCheckAlias = ['loss', 'profit']
    def values = [:]
    // номер последний строки предыдущей формы
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 3. Проверка на нулевые значения
        def hasError = true
        for (def alias : ['income', 'cost279', 'costReserve', 'loss', 'profit']) {
            if (row.getCell(alias).value != 0) {
                hasError = false
                break
            }
        }
        if (hasError) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 4. Арифметическая проверка графы 8, 9
        def tmp = calcFor8or9(row)
        values['loss'] = calc8(tmp)
        values['profit'] = calc9(tmp)
        checkCalc(row, arithmeticCheckAlias, values, logger, true)
    }

    // 5. Проверка итогового значений по всей форме (графа 5..9)
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

/*
 * Вспомогательные методы.
 */

/** Получить одинаковую часть расчетов для графы 8 или 9. */
def calcFor8or9(def row) {
    if (row.income == null || row.cost279 == null || row.costReserve == null) {
        return null
    }
    return row.income - (row.cost279 - row.costReserve)
}

def BigDecimal calc8(def tmp) {
    if (tmp == null) {
        return null
    }
    return ((BigDecimal)(tmp < 0 ? -tmp : 0)).setScale(2, BigDecimal.ROUND_HALF_UP)
}

def BigDecimal calc9(def tmp) {
    if (tmp == null) {
        return null
    }
    return ((BigDecimal)(tmp >= 0 ? tmp : 0)).setScale(2, BigDecimal.ROUND_HALF_UP)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата',
            (xml.row[0].cell[3]): 'Номинал права требования, закладной',
            (xml.row[0].cell[4]): 'Цена приобретения права требования, закладной',
            (xml.row[0].cell[5]): 'Доход (выручка) от реализации финансовых услуг',
            (xml.row[0].cell[6]): 'Стоимость права требования, закладной, подлежащая отнесению на расходы в соответствии с п.3 ст. 279 НК РФ',
            (xml.row[0].cell[7]): 'Стоимость права требования, закладной, списанных за счёт резервов',
            (xml.row[0].cell[8]): 'Убыток',
            (xml.row[0].cell[9]): 'Прибыль',
            (xml.row[1].cell[0]): '1'

    ]
    (2..9).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 1)
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

        def int xmlIndexCol = 0

        // графа 1
        xmlIndexCol++

        // графа fix
        xmlIndexCol++

        // графа 2
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 3
        newRow.nominal = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        xmlIndexCol++

        // графа 4
        newRow.price = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 5
        newRow.income = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 6
        newRow.cost279 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 7
        newRow.costReserve = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}