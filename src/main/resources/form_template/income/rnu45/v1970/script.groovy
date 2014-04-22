package form_template.income.rnu45.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-45
 * Форма "(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»"  (341)
 * formTemplateId=341
 *
 * графа 1	- rowNumber
 * графа 2	- inventoryNumber
 * графа 3	- name
 * графа 4	- buyDate
 * графа 5	- usefulLife
 * графа 6	- expirationDate
 * графа 7	- startCost
 * графа 8	- depreciationRate
 * графа 9	- amortizationMonth
 * графа 10	- amortizationSinceYear
 * графа 11	- amortizationSinceUsed
 *
 * @author akadyrgulov
 * @author Stanislav Yasinskiy
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = isMonthBalance() ? balanceEditableColumns : editableColumns
        def autoColumns = isMonthBalance() ? ['rowNumber'] : autoFillColumns
        formDataService.addRow(formData, currentDataRow, columns, autoColumns)
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
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

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost']
@Field
def balanceEditableColumns = ['inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost',
                              'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'inventoryNumber', 'name', 'buyDate', 'usefulLife', 'expirationDate', 'startCost',
        'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['startCost', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

@Field
def isBalance = null
@Field
def startDate = null
@Field
def endDate = null

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

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true)
    }
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        // Отчётный период
        if (!reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId) || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalance
}

//// Кастомные методы

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца

// Получение формы предыдущего месяца
def getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (formDataPrev != null) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalColumns.each {
        totalRow[it] = null
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    def endDate = getMonthEndDate()
    def dateStart = getMonthStartDate()

    def dataOld = null
    if (formData.kind != FormDataKind.PRIMARY) {
        dataOld = getFormDataPrev() != null ? getDataRowHelperPrev() : null
    }

    def index = 0

    for (def row in dataRows) {
        // графа 1
        row.rowNumber = ++index

        if (formData.kind != FormDataKind.PRIMARY) {
            continue;
        }

        // графа 8
        row.depreciationRate = calc8(row)
        // графа 9
        row.amortizationMonth = calc9(row)
        // для граф 10 и 11
        prevValues = getPrev10and11(dataOld, row)
        // графа 10
        row.amortizationSinceYear = calc10(row, dateStart, endDate, prevValues[0])
        // графа 11
        row.amortizationSinceUsed = calc11(row, dateStart, endDate, prevValues[1])
    }
    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

// Ресчет графы 8
def BigDecimal calc8(def row) {
    if (row.usefulLife == null || row.usefulLife == 0) {
        return null
    }
    return ((1 / row.usefulLife) * 100).setScale(4, RoundingMode.HALF_UP)
}

// Ресчет графы 9
def BigDecimal calc9(def row) {
    if (row.startCost == null || row.depreciationRate == null) {
        return null
    }
    return (row.startCost * row.depreciationRate / 100).setScale(2, RoundingMode.HALF_UP)
}

// Ресчет графы 10
def BigDecimal calc10(def row, def dateStart, def dateEnd, def oldRow10) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && dateStart != null && dateEnd != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.get(Calendar.MONTH) == Calendar.JANUARY || (buyDate.after(dateStart) && buyDate.before(dateEnd))) ? 0 : ((oldRow10 == null) ? 0 : oldRow10))
    return null
}

// Ресчет графы 11
def BigDecimal calc11(def row, def dateStart, def dateEnd, def oldRow11) {
    Calendar buyDate = calc10and11(row)
    if (buyDate != null && dateStart != null && dateEnd != null && row.amortizationMonth != null)
        return row.amortizationMonth + ((buyDate.after(dateStart) && buyDate.before(dateEnd)) ? 0 : ((oldRow11 == null) ? 0 : oldRow11))
    return null
}

// Общая часть ресчета граф 10 и 11
Calendar calc10and11(def row) {
    if (row.buyDate == null) {
        return null
    }
    Calendar buyDate = Calendar.getInstance()
    buyDate.setTime(row.buyDate)
    return buyDate
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {
        // Инвентарные номера
        def Set<String> invSet = new HashSet<String>()
        def dataOld = null
        if (formData.kind != FormDataKind.PRIMARY) {
            dataOld = getFormDataPrev() != null ? getDataRowHelperPrev() : null
        }

        def dateEnd = getMonthEndDate()
        def dateStart = getMonthStartDate()

        // алиасы графов для арифметической проверки
        def arithmeticCheckAlias = ['depreciationRate', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']
        // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
        def needValue = [:]

        for (def row in dataRows) {
            if (row.getAlias() != null) {
                continue
            }

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            // 1. Проверка на заполнение поля
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isMonthBalance())

            // 2. Проверка на уникальность поля «инвентарный номер»
            if (invSet.contains(row.inventoryNumber)) {
                loggerError(errorMsg + "Инвентарный номер не уникальный!")
            } else {
                invSet.add(row.inventoryNumber)
            }

            // 3. Проверка на нулевые значения
            if (row.startCost == 0 && row.amortizationMonth == 0 && row.amortizationSinceYear == 0 && row.amortizationSinceUsed == 0) {
                loggerError(errorMsg + "Все суммы по операции нулевые!")
            }

            if (formData.kind == FormDataKind.PRIMARY) {
                // 4. Арифметические проверки расчета неитоговых граф
                needValue['depreciationRate'] = calc8(row)
                needValue['amortizationMonth'] = calc9(row)
                prevValues = getPrev10and11(dataOld, row)
                needValue['amortizationSinceYear'] = calc10(row, dateStart, dateEnd, prevValues[0])
                needValue['amortizationSinceUsed'] = calc11(row, dateStart, dateEnd, prevValues[1])
                checkCalc(row, arithmeticCheckAlias, needValue, logger, !isMonthBalance())
            }
        }
        // 5. Арифметические проверки расчета итоговой строки
        checkTotalSum(dataRows, totalColumns, logger, !isMonthBalance())
    }
}

// Получить значение за предыдущий отчетный период для графы 10 и 11
def getPrev10and11(def dataOld, def row) {
    if (dataOld != null)
        for (def rowOld : dataOld.getAllCached()) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                return [rowOld.amortizationSinceYear, rowOld.amortizationSinceUsed]
            }
        }
    return [null, null]
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'inventoryNumber'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'name'),
            (xml.row[0].cell[4]) : getColumnName(tmpRow, 'buyDate'),
            (xml.row[0].cell[5]) : getColumnName(tmpRow, 'usefulLife'),
            (xml.row[0].cell[6]) : getColumnName(tmpRow, 'expirationDate'),
            (xml.row[0].cell[7]) : getColumnName(tmpRow, 'startCost'),
            (xml.row[0].cell[8]) : getColumnName(tmpRow, 'depreciationRate'),
            (xml.row[0].cell[9]) : getColumnName(tmpRow, 'amortizationMonth'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'amortizationSinceYear'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'amortizationSinceUsed'),
            (xml.row[1].cell[0]) : '1'
    ]
    (2..11).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
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

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
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
        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        def columns = isMonthBalance() ? balanceEditableColumns : editableColumns
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        newRow.rowNumber = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 3
        newRow.name = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.buyDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 5
        newRow.usefulLife = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, 0 + colOffset, logger, false)
        xmlIndexCol++
        // графа 6
        newRow.expirationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 7
        newRow.startCost = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 8
        newRow.depreciationRate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 9
        newRow.amortizationMonth = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 10
        newRow.amortizationSinceYear = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        xmlIndexCol++
        // графа 11
        newRow.amortizationSinceUsed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)

        rows.add(newRow)
    }
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

def getMonthStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getMonthEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def loggerError(def msg) {
    if (isMonthBalance()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}
