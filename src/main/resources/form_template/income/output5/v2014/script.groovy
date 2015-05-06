package form_template.income.output5.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Сведения о уплаченных суммах налога по операциям с ГЦБ.
 * formTemplateId=420
 *
 * TODO:
 *      - колонтитул
 *      - код формы
 *
 * @author Ramil Timerbaev
 */

// графа 1 - rowNum
// графа 2 - name
// графа 3 - date
// графа 4 - sum

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
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        break
}

// Проверяемые на пустые значения атрибуты (графа 1..4)
@Field
def nonEmptyColumns = ['date', 'sum']

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def row : dataRows) {
        def index = row.getIndex()

        // 1. Проверка на заполнение
        if (row.getAlias() != 'R2') {
            if(row.getAlias() == 'R1'){
                checkNonEmptyColumns(row, index, nonEmptyColumns - 'date', logger, true)
            } else{
                checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)
            }
        }
    }
}

void calc() {
    // расчетов нет
    // сортировки нет, все строки фиксированные
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 4, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[1]): getColumnName(tmpRow, 'name'),

            (xml.row[0].cell[2]): 'Отчётный квартал',
            (xml.row[1].cell[2]): 'дата',
            (xml.row[1].cell[3]): 'сумма',
    ]
    (0..3).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    def int rowIndex = 0

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow < headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        def dataRow = dataRows.get(rowIndex)
        dataRow.setImportIndex(xlsIndexRow)
        rowIndex++

        def xmlIndexCol = -1

        def values = [:]
        // графа 1
        xmlIndexCol++
        values.rowNum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        // графа 2
        xmlIndexCol++
        values.name = row.cell[xmlIndexCol].text()

        // Проверить фиксированные значения (графа 1..2)
        ['rowNum', 'name'].each { alias ->
            def value = values[alias]?.toString()
            def valueExpected = dataRow.getCell(alias).value?.toString()
            checkFixedValue(dataRow, value, valueExpected, rowIndex, alias, logger, true)
        }

        // графа 3
        xmlIndexCol++
        if(rowIndex != 1 && rowIndex != 2){
            dataRow.getCell('date').setCheckMode(true)
            dataRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }else{
            dataRow.date = null
        }

        // графа 4
        xmlIndexCol++
        if(rowIndex != 2) {
            dataRow.getCell('sum').setCheckMode(true)
            dataRow.sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        } else {
            dataRow.sum = null
        }
    }

    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        dataRowHelper.save(dataRows)
    }
}