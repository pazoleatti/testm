package form_template.income.rnu22.v1970

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования"
 * formTemplateId=322
 *
 * @version 59
 *
 * TODO:
 * 	- консолидация http://jira.aplana.com/browse/SBRFACCTAX-4455
 * 	- http://conf.aplana.com/pages/viewpage.action?pageId=8790975 период ввода остатков
 *
 * @author rtimerbaev
 *
 * графа 1  - rowNumber
 * графа 2  - contractNumber
 * графа 3  - contractData
 * графа 4  - base
 * графа 5  - transactionDate
 * графа 6  - course
 * графа 7  - interestRate
 * графа 8  - basisForCalc
 * графа 9  - calcPeriodAccountingBeginDate
 * графа 10 - calcPeriodAccountingEndDate
 * графа 11 - calcPeriodBeginDate
 * графа 12 - calcPeriodEndDate
 * графа 13 - accruedCommisCurrency
 * графа 14 - accruedCommisRub
 * графа 15 - commisInAccountingCurrency
 * графа 16 - commisInAccountingRub
 * графа 17 - accrualPrevCurrency
 * графа 18 - accrualPrevRub
 * графа 19 - reportPeriodCurrency
 * графа 20 - reportPeriodRub
 */

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod
isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        if (!prevPeriodCheck()) {
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def cols = isBalancePeriod ? (allColumns - 'rowNumber') : editableColumns
        formDataService.addRow(formData, currentDataRow, cols, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
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

// Все аттрибуты
@Field
def allColumns = ['rowNumber', 'contractNumber', 'contractData', 'base', 'transactionDate',
        'course', 'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate', 'calcPeriodAccountingEndDate',
        'calcPeriodBeginDate', 'calcPeriodEndDate', 'accruedCommisCurrency', 'accruedCommisRub',
        'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
        'reportPeriodCurrency', 'reportPeriodRub']

// Поля, для которых подсчитываются итоговые значения
@Field
def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
        'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
        'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']

// Редактируемые атрибуты
@Field
def editableColumns = ['contractNumber', 'contractData', 'base', 'transactionDate', 'course',
        'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
        'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate']

// Обязательно заполняемые атрибуты
@Field
def nonEmptyColumns = ['contractNumber', 'contractData', 'base',
        'transactionDate', 'course', 'interestRate', 'basisForCalc']

@Field
def sortColumns = ["transactionDate", "contractData", "contractNumber"]

@Field
def arithmeticCheckAlias = ['accruedCommisCurrency', 'accruedCommisRub',
        'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
        'reportPeriodCurrency', 'reportPeriodRub']

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataRowsOld = null
    if (formData.kind == FormDataKind.PRIMARY) {
        dataRowsOld = getPrevDataRows()
    }
    def tmp
    def dFrom = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def DataRow row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod)

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (!(dFrom != null && dTo != null && ((row.transactionDate != null && row.transactionDate <= dFrom) ||
                (row.calcPeriodAccountingEndDate != null && row.calcPeriodAccountingEndDate <= dTo) ||
                (row.calcPeriodEndDate != null && row.calcPeriodEndDate <= dTo)))) {
            loggerError(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 2. Проверка на нулевые значения (графа 13..20)
        def allNull = true
        def allNullCheck = ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                'reportPeriodCurrency', 'reportPeriodRub']
        for (alias in allNullCheck) {
            tmp = row[alias]
            if (tmp != null && tmp != 0) {
                allNull = false
                break
            }
        }
        if (allNull) {
            loggerError(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 3. Проверка на сумму платы (графа 4)
        if (row.base != null && !(row.base > 0)) {
            logger.warn(errorMsg + 'Суммы платы равны 0!')
        }

        // 4. Проверка задания расчётного периода
        if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate &&
                row.calcPeriodBeginDate > row.calcPeriodEndDate) {
            logger.warn(errorMsg + 'Неправильно задан расчётный период!')
        }

        // 5. Проверка на корректность даты договора
        if (row.contractData > dTo) {
            loggerError(errorMsg + 'Дата договора неверная!')
        }

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 14, 16)
        if (row.accruedCommisRub < row.commisInAccountingRub) {
            logger.warn(errorMsg + "Сумма данных бухгалтерского учёта превышает сумму начисленных платежей для документа ${row.contractNumber}")
        }

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = (row.calcPeriodAccountingBeginDate != null || row.calcPeriodAccountingEndDate != null) &&
                row.calcPeriodBeginDate != null && row.calcPeriodEndDate != null
        if (checkColumn9and10 || checkColumn11and12) {
            loggerError(errorMsg + 'Поля в графах 9, 10, 11, 12 заполены неверно!')
        }

        def date1 = row.calcPeriodBeginDate
        def date2 = row.calcPeriodEndDate
        if (date1 != null && date2 != null && row.basisForCalc != null && row.basisForCalc * (date2 - date1 + 1) == 0) {
            loggerError(errorMsg + "Деление на ноль. Возможно неправильно выбраны даты.")
        }

        // 9. Проверка на уникальность поля «№ пп» (графа 1)
        if (++i != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        if (formData.kind == FormDataKind.PRIMARY) {
            def rowPrev
            for (def rowOld in dataRowsOld) {
                if (rowOld.contractNumber == row.contractNumber) {
                    rowPrev = rowOld
                    break
                }
            }
            def values = [:]

            tmp = getGraph13_15(row)
            values.accruedCommisCurrency = tmp
            values.commisInAccountingCurrency = tmp
            values.accruedCommisRub = getGraph14(row)
            values.commisInAccountingRub = getGraph16(row)
            values.accrualPrevCurrency = rowPrev?.reportPeriodCurrency
            values.accrualPrevRub = rowPrev?.reportPeriodRub
            values.reportPeriodCurrency = getGraph19(row)
            values.reportPeriodRub = getGraph20(row)
            checkCalc(row, arithmeticCheckAlias, values, logger, !isBalancePeriod)
        }
    }
    checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataRowsOld = null
    if (formData.kind == FormDataKind.PRIMARY) {
        dataRowsOld = getPrevDataRows()
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    sortRows(dataRows, sortColumns)
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }

    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // графа 1, 13..20
    dataRows.each { DataRow row ->

        // графа 1
        row.rowNumber = ++i

        if (formData.kind == FormDataKind.PRIMARY) {
            def rowPrev = null
            for (def rowOld in dataRowsOld) {
                if (rowOld.contractNumber == row.contractNumber) {
                    rowPrev = rowOld
                    break
                }
            }

            // графа 13, 15
            def temp = getGraph13_15(row)
            row.accruedCommisCurrency = temp
            row.commisInAccountingCurrency = temp
            // графа 14
            row.accruedCommisRub = getGraph14(row)
            // графа 16
            row.commisInAccountingRub = getGraph16(row)
            // графа 17
            row.accrualPrevCurrency = rowPrev?.reportPeriodCurrency
            // графа 18
            row.accrualPrevRub = rowPrev?.reportPeriodRub
            // графа 19
            row.reportPeriodCurrency = getGraph19(row)
            // графа 20
            row.reportPeriodRub = getGraph20(row)
        }
    }

    // добавить строку "итого"
    def DataRow totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contractNumber = 'Итого'
    totalRow.getCell('contractNumber').colSpan = 11
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }

    calcTotalSum(dataRows, totalRow, totalColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allCached.each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        dataRows.add(row)
                    }
                }
            }
        }
    }
    //TODO http://jira.aplana.com/browse/SBRFACCTAX-4455
    def ignoredRows = []
    for (def row : dataRows) {
        if (!ignoredRows.contains(row)) {
            for (def rowB : dataRows) {
                if (row != rowB && isEqualRows(row, rowB) && !ignoredRows.contains(rowB)) {
                    addRowToRow(row, rowB)
                    ignoredRows.add(rowB)
                }
            }
        }
    }
    dataRows.removeAll(ignoredRows)
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Определение идентичности строк(графа 2,3,5)
 * @param rowA
 * @param rowB
 * @return
 */
def boolean isEqualRows(def DataRow rowA, def DataRow rowB) {
    return rowA.contractNumber == rowB.contractNumber &&
            rowA.contractData == rowB.contractData &&
            rowA.transactionDate == rowB.transactionDate
}

/**
 * Увеличиваем значения строки А на значения строки B
 * @param rowA
 * @param rowB
 */
void addRowToRow(def DataRow rowA, def DataRow rowB) {
    def columns = ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
            'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']
    //суммируем графы 13-20
    columns.each { col ->
        def a = rowA[col]
        def b = rowB[col]
        rowA[col] = ((a != null) ? (a + (b ?: 0)) : ((b ?: 0)))
    }
}

BigDecimal getGraph13_15(def DataRow row) {
    def date1, date2
    if (row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null) {
        date1 = row.calcPeriodAccountingBeginDate
        date2 = row.calcPeriodAccountingEndDate
    } else {
        date1 = row.calcPeriodBeginDate
        date2 = row.calcPeriodEndDate
    }
    return calcFor13or15or19(row, date1, date2)
}

BigDecimal getGraph14(def DataRow row) {
    if (row.accruedCommisCurrency != null && row.course != null) {
        return (row.accruedCommisCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
}

BigDecimal getGraph16(def DataRow row) {
    if (row.commisInAccountingCurrency != null && row.course != null) {
        return (row.commisInAccountingCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
}

BigDecimal getGraph19(def DataRow row) {
    return calcFor13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
}

BigDecimal calcFor13or15or19(def row, def date1, def date2) {
    def tmp = BigDecimal.ZERO
    if (date1 == null || date2 == null || row.basisForCalc == null ||
            row.base == null || row.interestRate == null) {
        return tmp.setScale(2, RoundingMode.HALF_UP)
    }
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division != 0) {
        tmp = (row.base * row.interestRate) / division
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph20(def DataRow row) {
    if (row.reportPeriodCurrency != null && row.course != null) {
        return (row.reportPeriodCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
boolean prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
        return false
    }
    return true
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (isBalancePeriod || isConsolidated) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
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

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        def cols = isBalancePeriod ? (allColumns - 'rowNumber') : editableColumns
        cols.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 2
        newRow.contractNumber = row.cell[1].text()

        // графа 3
        newRow.contractData = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 4
        newRow.base = parseNumber(row.cell[3].text(),xlsIndexRow, 0 + colOffset, logger, false)

        // графа 5
        newRow.transactionDate = parseDate(row.cell[4].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 6
        newRow.course = parseNumber(row.cell[5].text(),xlsIndexRow, 0 + colOffset, logger, false)

        // графа 7
        newRow.interestRate = parseNumber(row.cell[6].text(),xlsIndexRow, 0 + colOffset, logger, false)

        // графа 8
        newRow.basisForCalc = parseNumber(row.cell[7].text(),xlsIndexRow, 0 + colOffset, logger, false)

        // графа 9
        newRow.calcPeriodAccountingBeginDate = parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 10
        newRow.calcPeriodAccountingEndDate = parseDate(row.cell[9].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 11
        newRow.calcPeriodBeginDate = parseDate(row.cell[10].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 12
        newRow.calcPeriodEndDate = parseDate(row.cell[11].text(), "dd.MM.yyyy", xlsIndexRow, 0 + colOffset, logger, false)

        // графа 13
        newRow.accruedCommisCurrency = parseNumber(row.cell[12].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 14
        newRow.accruedCommisRub = parseNumber(row.cell[13].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 15
        newRow.commisInAccountingCurrency = parseNumber(row.cell[14].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 16
        newRow.commisInAccountingRub = parseNumber(row.cell[15].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 17
        newRow.accrualPrevCurrency = parseNumber(row.cell[16].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 18
        newRow.accrualPrevRub = parseNumber(row.cell[17].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 19
        newRow.reportPeriodCurrency = parseNumber(row.cell[18].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 20
        newRow.reportPeriodRub = parseNumber(row.cell[19].text(), xlsIndexRow, 0 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null, 20, 4)
    // проверка шапки таблицы
    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 20, 4)
    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Номер договора',
            (xml.row[0].cell[2]): 'Дата договора',
            (xml.row[0].cell[3]): 'База для расчёта комиссии',
            (xml.row[0].cell[4]): 'Дата совершения операции',
            (xml.row[0].cell[5]): 'Курс Банка России',
            (xml.row[0].cell[6]): 'Процентная ставка',
            (xml.row[0].cell[7]): 'База для расчёта (дни)',

            (xml.row[0].cell[8]): 'Расчётный период',

            (xml.row[1].cell[8]): 'начисление',
            (xml.row[1].cell[10]): 'доначисление',

            (xml.row[2].cell[8]): 'дата начала',
            (xml.row[2].cell[9]): 'дата окончания',
            (xml.row[2].cell[10]): 'дата начала',
            (xml.row[2].cell[11]): 'дата окончания',

            (xml.row[0].cell[12]): 'Сумма в налоговом учёте',
            (xml.row[1].cell[12]): 'валюта',
            (xml.row[1].cell[13]): 'рубли',

            (xml.row[0].cell[14]): 'Сумма в бухгалтерском учёте',
            (xml.row[1].cell[14]): 'валюта',
            (xml.row[1].cell[15]): 'рубли',

            (xml.row[0].cell[16]): 'Сумма доначисления',
            (xml.row[1].cell[16]): 'предыдущий квартал\t',
            (xml.row[1].cell[18]): 'отчётный квартал\t',
            (xml.row[2].cell[16]): 'валюта',
            (xml.row[2].cell[17]): 'рубли',
            (xml.row[2].cell[18]): 'валюта',
            (xml.row[2].cell[19]): 'рубли',
    ]
    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg, Object...args) {
    if (isBalancePeriod) {
        logger.warn(msg, args)
    } else {
        logger.error(msg, args)
    }
}
