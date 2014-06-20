package form_template.income.rnu75.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * (РНУ-75) Регистр налогового учёта доходов по операциям депозитария
 * formTypeId=366
 *
 * 1  - number
 * 2  - date
 * 3  - depo
 * 4  - reasonNumber
 * 5  - reasonDate
 * 6  - taxSum
 * 7  - factSum
 *
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
def editableColumns = ['date', 'depo', 'reasonNumber', 'reasonDate', 'taxSum', 'factSum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'date', 'depo', 'reasonNumber', 'reasonDate', 'taxSum', 'factSum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['taxSum', 'factSum']

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

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

        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            // графа 1
            row.number = ++index
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size() + 1)

    dataRowHelper.save(dataRows)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 3. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты совершения операции и границ отчётного периода
        if (row.date != null && (row.date.after(endDate) || row.date.before(startDate))) {
            logger.error(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 2. Проверка на нулевые значения
        if ((row.taxSum == null || row.taxSum == 0) && (row.factSum == null || row.factSum == 0)) {
            logger.error(errorMsg + 'Суммы по операции нулевые!!')
        }

        // 4. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }
    }

    // 5. Арифметическая проверка итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}


def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 5
    ['number', 'fix', 'taxSum', 'factSum'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата совершения операции',
            (xml.row[0].cell[3]): 'Депонент',
            (xml.row[0].cell[4]): 'Основание для совершения операции',
            (xml.row[0].cell[6]): 'Начисленная в налоговом учете сумма дохода',
            (xml.row[0].cell[7]): 'Фактически отраженная в бухгалтерском учете сумма дохода',
            (xml.row[1].cell[4]): 'номер',
            (xml.row[1].cell[5]): 'дата',
            (xml.row[2].cell[0]): '1'
    ]
    (2..7).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }
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

        // графа 2
        newRow.date = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, true)

        // графа 3
        newRow.depo = row.cell[3].text()

        // графа 4
        newRow.reasonNumber = row.cell[4].text()

        // графа 5
        newRow.reasonDate = parseDate(row.cell[5].text(), "dd.MM.yyyy", xlsIndexRow, 5 + colOffset, logger, true)

        // графа 6
        newRow.taxSum = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, true)

        // графа 7
        newRow.factSum = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}