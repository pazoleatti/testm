package form_template.income.rnu48_1.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.".
 * formTemplateId=343
 *
 * @author vsergeev
 * @author rtimerbaev
 */

// 1 - number          - № пп
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
def nonEmptyColumns = ['number', 'inventoryNumber', 'usefulDate', 'amount']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalColumns = ['amount']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    deleteAllAliased(dataRows)
    // сортировка
    sortRows(dataRows, groupColumns)

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        row.number = ++rowNumber
    }
    //расчитываем новые итоговые значения
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.getAllCached()

    def periodStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)?.time
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
            logger.error("Строка ${row.getIndex()}: Дата ввода в эксплуатацию вне границ отчетного периода!")
        }
    }

    // 3. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.inventoryNumber = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
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
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 4, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Инвентарный номер',
            (xml.row[0].cell[2]): 'Дата ввода в эксплуатацию',
            (xml.row[0].cell[3]): 'Сумма, включаемая в состав материальных расходов',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[1].cell[3]): '4'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

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
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        def xmlIndexCol = 0

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        newRow.number = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 2
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.usefulDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 4
        newRow.amount = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}