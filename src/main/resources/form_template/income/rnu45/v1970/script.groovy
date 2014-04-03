package form_template.income.rnu45.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
        if (formData.kind == FormDataKind.PRIMARY) {
            prevPeriodCheck()
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
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
    if (!isMonthBalance() && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
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

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        def endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
        def dateStart = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time

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
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size() + 1)
    dataRowHelper.save(dataRows)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 6
    ['rowNumber', 'fix', 'startCost', 'depreciationRate', 'amortizationMonth', 'amortizationSinceYear',
            'amortizationSinceUsed'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
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
    return (row.startCost * row.depreciationRate).setScale(2, RoundingMode.HALF_UP)
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

        def dateEnd = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
        def dateStart = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time

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

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Инвентарный номер',
            (xml.row[0].cell[3]): 'Наименование',
            (xml.row[0].cell[4]): 'Дата приобретения',
            (xml.row[0].cell[5]): 'Срок полезного использования (мес.)',
            (xml.row[0].cell[6]): 'Дата истечения срока полезного использования',
            (xml.row[0].cell[7]): 'Первоначальная стоимость (руб.)',
            (xml.row[0].cell[8]): 'Норма амортизации (% в мес.)',
            (xml.row[0].cell[9]): 'Сумма начисленной амортизации за месяц (руб.)',
            (xml.row[0].cell[10]): 'Сумма начисленной амортизации с начала года (руб.)',
            (xml.row[0].cell[11]): 'Сумма начисленной амортизации с даты ввода в эксплуатацию (руб.)',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
            (xml.row[1].cell[4]): '4',
            (xml.row[1].cell[5]): '5',
            (xml.row[1].cell[6]): '6',
            (xml.row[1].cell[7]): '7',
            (xml.row[1].cell[8]): '8',
            (xml.row[1].cell[9]): '9',
            (xml.row[1].cell[10]): '10',
            (xml.row[1].cell[11]): '11'
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
    dataRowHelper.save(rows)
}

def loggerError(def msg) {
    if (isMonthBalance()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}
