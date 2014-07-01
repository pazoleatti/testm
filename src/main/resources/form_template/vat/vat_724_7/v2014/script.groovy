package form_template.vat.vat_724_7.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  (724.7)  Отчёт о суммах НДС начисленных налоговым агентом по договорам аренды имущества (балансовый счёт 60309.03)
 *
 *  formTemplateId=605
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
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['total'])
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['operDate', 'name', 'inn', 'kpp', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['operDate', 'name', 'inn', 'kpp', 'balanceNumber', 'sum', 'orderNumber', 'ndsSum', 'sfDate', 'sfNumber']

// Сумируемые колонки в фиксированной строке
@Field
def totalColumns = ['sum', 'ndsSum']

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
        if (row.sum != null && row.ndsSum != null &&
                !(row.ndsSum > row.sum * 0.15 && row.ndsSum < row.sum * 0.21)) {
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
            (xml.row[0].cell[3]): 'Арендодатель',
            (xml.row[0].cell[6]): 'Номер балансового счёта учёта суммы арендной платы',
            (xml.row[0].cell[7]): 'Сумма арендной платы, уплаченная арендодателю',
            (xml.row[0].cell[8]): 'НДС',
            (xml.row[0].cell[10]): 'Счёт-фактура',
            (xml.row[1].cell[3]): 'наименование',
            (xml.row[1].cell[4]): 'ИНН',
            (xml.row[1].cell[5]): 'КПП',
            (xml.row[1].cell[8]): 'номер мемориального ордера',
            (xml.row[1].cell[9]): 'сумма',
            (xml.row[1].cell[10]): 'дата',
            (xml.row[1].cell[11]): 'номер',
            (xml.row[2].cell[0]): '1'
    ]
    (2..11).each { index ->
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

        // графа 2
        newRow.operDate = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, true)

        // графа 3
        newRow.name =  row.cell[3].text()

        // графа 4
        newRow.inn = row.cell[4].text()

        // графа 5
        newRow.kpp = row.cell[5].text()

        // графа 6
        newRow.balanceNumber = row.cell[6].text()

        // графа 7
        newRow.sum = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)

        // графа 8
        newRow.orderNumber = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, true)

        // графа 9
        newRow.ndsSum = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, true)

        // графа 10
        newRow.sfDate = parseDate(row.cell[10].text(), "dd.MM.yyyy", xlsIndexRow, 10 + colOffset, logger, true)

        // графа 11
        newRow.sfNumber = row.cell[11].text()

        rows.add(newRow)
    }

    // Добавляем итоговые строки
    rows.add(getDataRow(dataRows, 'total'))

    dataRowHelper.save(rows)
}
