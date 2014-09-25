package form_template.vat.vat_724_6.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  6.5	(724.6)  Отчёт о суммах НДС начисленных налоговым агентом с сумм дохода иностранных юридических лиц
 *  (балансовый счёт 60309.02)
 *
 *  formTemplateId=604
 */

// графа 1  - rowNum
// графа    - fix
// графа 2  - operDate
// графа 3  - contragent
// графа 4  - type
// графа 5  - sum
// графа 6  - number
// графа 7  - sum2
// графа 8  - date
// графа 9  - number2

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
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        formDataService.consolidationTotal(formData, logger, ['total'])
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['operDate', 'contragent', 'type', 'sum', 'number', 'sum2', 'date', 'number2']

// Сумируемые колонки в фиксированной строке
@Field
def totalColumns = ['sum', 'sum2']

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
        // 2. Проверка суммы НДС
        if (row.sum != null && row.sum2 != null &&
                !(row.sum2 > row.sum * 0.15 && row.sum2 < row.sum * 0.21)) {
            rowWarning(logger, row, "Строка $index: Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе!")
        }
    }
    // 3. Проверка итоговых значений
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = getDataRow(dataRows, 'total')
    deleteAllAliased(dataRows)
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата операции',
            (xml.row[0].cell[3]): 'Контрагент',
            (xml.row[0].cell[4]): 'Доход контрагента',
            (xml.row[0].cell[6]): 'НДС',
            (xml.row[0].cell[8]): 'Счёт-фактура',
            (xml.row[1].cell[4]): 'Вид',
            (xml.row[1].cell[5]): 'Сумма',
            (xml.row[1].cell[6]): 'Номер мемориального ордера',
            (xml.row[1].cell[7]): 'Сумма',
            (xml.row[1].cell[8]): 'Дата',
            (xml.row[1].cell[9]): 'Номер',
            (xml.row[2].cell[0]): '1'
    ]
    (2..9).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

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

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // графа 2
        newRow.operDate = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, true)

        // графа 3
        newRow.contragent =  row.cell[3].text()

        // графа 4
        newRow.type = row.cell[4].text()

        // графа 5
        newRow.sum = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.number = row.cell[6].text()

        // графа 7
        newRow.sum2 = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.number2 = row.cell[9].text()

        rows.add(newRow)
    }

    // Добавляем итоговые строки
    rows.add(getDataRow(dataRows, 'total'))

    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 9, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []

    def totalTmp = formData.createDataRow()
    totalColumns.each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }
        def newRow = getNewRow()
        newRow.setImportIndex(rnuIndexRow)

        // графа 2
        newRow.operDate = parseDate(row.cell[2].text(), "dd.MM.yyyy", rnuIndexRow, 2 + colOffset, logger, true)

        // графа 3
        newRow.contragent =  row.cell[3].text()

        // графа 4
        newRow.type = row.cell[4].text()

        // графа 5
        newRow.sum = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.number = row.cell[6].text()

        // графа 7
        newRow.sum2 = parseNumber(row.cell[7].text(), rnuIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.date = parseDate(row.cell[8].text(), "dd.MM.yyyy", rnuIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.number2 = row.cell[9].text()

        rows.add(newRow)

        totalColumns.each { alias ->
            def value1 = totalTmp.getCell(alias).value
            def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
            totalTmp.getCell(alias).setValue(value1 + value2, null)
        }
    }

    // сравнение итогов
    if (xml.rowTotal.size() == 1) {
        rnuIndexRow = rnuIndexRow + 2
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 5
        total.sum = parseNumber(row.cell[5].text(), rnuIndexRow, 5 + colOffset, logger, true)

        // графа 7
        total.sum2 = parseNumber(row.cell[7].text(), rnuIndexRow, 7 + colOffset, logger, true)

        def colIndexMap = ['sum' : 5, 'sum2' : 7]

        for (def alias : totalColumns) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }

    // расчет итогов
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    dataRowHelper.save(rows)
}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}