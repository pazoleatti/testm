package form_template.income.rnu48_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Форма "(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.".
 * formTypeId=343
 *
 * @author vsergeev
 * @author rtimerbaev
 */

// 1 - number          - № пп
// fix
// 2 - inventoryNumber - Инвентарный номер
// 3 - usefulDate      - Дата ввода в эксплуатацию
// 4 - amount          - Сумма, включаемая в состав материальных расходов

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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
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
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['total'])
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

// Все атрибуты
@Field
def allColumns = ['number', 'inventoryNumber', 'usefulDate', 'amount']

// Редактируемые атрибуты (графа 2..4)
@Field
def editableColumns = ['inventoryNumber', 'usefulDate', 'amount']

// Группируемые атрибуты (графа 3, 2)
@Field
def groupColumns = ['usefulDate', 'inventoryNumber']

// Проверяемые на пустые значения атрибуты (графа 1..4)
@Field
def nonEmptyColumns = ['inventoryNumber', 'usefulDate', 'amount']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalColumns = ['amount']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalColumns.each {
        totalRow[it] = null
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)
    // сортировка
    sortRows(dataRows, groupColumns)

    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.update(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.getAllCached()

    def periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    def periodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    def reportPeriodRange = periodStartDate..periodEndDate

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // 1. Обязательность заполнения поля графы 1..4
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка даты ввода в эксплуатацию и границ отчетного периода
        if (! (row.usefulDate in reportPeriodRange)){
            rowError(logger, row, "Строка ${row.getIndex()}: Дата ввода в эксплуатацию вне границ отчетного периода!")
        }
    }

    // 3. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 4, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Инвентарный номер',
            (xml.row[0].cell[3]): 'Дата ввода в эксплуатацию',
            (xml.row[0].cell[4]): 'Сумма, включаемая в состав материальных расходов',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
            (xml.row[1].cell[4]): '4'
    ]

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

    def dataRows = dataRowHelper.allCached

    // Итоговая строка
    def totalRow = getDataRow(dataRows, 'total')
    // Очистка итогов
    totalColumns.each { alias ->
        totalRow[alias] = null
    }

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
        if (row.cell[1].text() != null && row.cell[1].text() != '') {
            continue
        }

        def xmlIndexCol = 0

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
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.usefulDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 4
        newRow.amount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
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

        // графа 2
        def xmlIndexCol = 2
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        // графа 3
        xmlIndexCol = 3
        newRow.usefulDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", rnuIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 4
        xmlIndexCol = 4
        newRow.amount = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    def totalRow = getDataRow(dataRows, 'total')
    calcTotalSum(rows, totalRow, totalColumns)
    rows.add(totalRow)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 4
        xmlIndexCol = 4
        total.amount = parseNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset, logger, true)

        def colIndexMap = ['amount' : 4]
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
