package form_template.income.rnu120

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.*
import groovy.transform.Field

/**
 * 378 - Форма "(РНУ-120) Регистр налогового учёта доходов по кредитам, выданным физическим лицам, признаваемым Взаимозависимыми лицами, а также любым физическим независимым лицам, являющимся резидентами оффшорных зон".
 *
 * TODO:
 *      - для графы 11 нет описания расчета
 *
 * @author Lenar Haziev
 */

// графа 1  - number
// графа 2  - numberAccount
// графа 3  - debtBalance
// графа 4  - operationDate
// графа 5  - course
// графа 6  - interestRate
// графа 7  - baseForCalculation
// графа 8  - calcPeriodAccountingBeginDate
// графа 9  - calcPeriodAccountingEndDate
// графа 10 - calcPeriodBeginDate
// графа 11 - calcPeriodEndDate
// графа 12 - taxAccount
// графа 13 - booAccount
// графа 14 - accrualPrev
// графа 15 - accrualReportPeriod
// графа 16 - marketInterestRate
// графа 17 - sumRate


switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        def formPrev = getFormPrev()
        if ((formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        def formPrev = getFormPrev()
        if ((formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        def formPrev = getFormPrev()
        if ((formPrev == null || formPrev.state != WorkflowState.ACCEPTED)) {
            logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
            return
        }
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
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

// все атрибуты
@Field
def allColumns = ['number', 'numberAccount', 'debtBalance', 'operationDate',
        'course', 'interestRate', 'baseForCalculation', 'calcPeriodAccountingBeginDate',
        'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate', 'taxAccount', 'booAccount',
        'accrualPrev', 'accrualReportPeriod', 'marketInterestRate', 'sumRate']

// Редактируемые атрибуты (графа 2 .. 4, 6 .. 10, 16)
// TODO - Непонятно что делать с графой 11
@Field
def editableColumns = ['numberAccount', 'debtBalance', 'operationDate',
        'interestRate', 'baseForCalculation', 'calcPeriodAccountingBeginDate',
        'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate', 'marketInterestRate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1 .. 17)
@Field
def nonEmptyColumns = ['number', 'numberAccount', 'debtBalance', 'operationDate',
                'course', 'interestRate', 'baseForCalculation', 'calcPeriodAccountingBeginDate',
                'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate', 'taxAccount', 'booAccount',
                'accrualPrev', 'accrualReportPeriod', 'marketInterestRate', 'sumRate']

// TODO - нет точного списка графов по которым нужно рассчитать сумму
@Field
def totalSumColumns = ['taxAccount', 'booAccount','accrualPrev', 'accrualReportPeriod', 'sumRate']

// алиасы графов по которым производится сортировка
@Field
def groupColums = ['numberAccount', 'operationDate']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['course', 'booAccount', 'taxAccount', 'accrualPrev', 'accrualReportPeriod', 'accrualReportPeriod', 'sumRate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Расчеты. Алгоритмы заполнения полей формы. */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataRowsPrev = formDataService.getDataRowHelper(getFormPrev()).allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // сортировка
    sortRows(dataRows, groupColums)

    // расчет
    def rowNumber = 0
    dataRows.each { row ->
        def rowPrev = getRowPrev(row, dataRowsPrev)

        // графа 1
        row.number = ++rowNumber

        // графа 5
        row.course = getCourse('RUB', row.operationDate)

        // графа 13
        row.booAccount = calc13(row)

        // графа 12, расчет графы 12 зависит от графы 13
        row.taxAccount = calc12(row, rowPrev)

        // графа 14
        row.accrualPrev = calc14(row, rowPrev)

        // графа 15
        row.accrualReportPeriod = calc15(row)

        // графа 17
        row.sumRate = calc17(row, rowPrev)
    }

    // добавить строку "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    // используется save() т.к. есть сортировка
    dataRowHelper.save(dataRows)
}

/** Логические проверки. */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def dataRowsPrev = formDataService.getDataRowHelper(getFormPrev()).allCached

    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def calcValues = [:]

    def rowNumber = 0
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNumber++

        def rowPrev = getRowPrev(row, dataRowsPrev)

        // 1. Проверка на заполнение поля «<Наименование поля>» (1 .. 17)
        checkNonEmptyColumns(row, rowNumber, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error("Строка $rowNumber: Нарушена уникальность номера по порядку!")
        }

        // 3. Арифметические проверки расчета неитоговых граф
        calcValues = [
                course: getCourse('RUB', row.operationDate),
                booAccount: calc13(row),
                taxAccount: calc12(row, rowPrev),
                accrualPrev: calc14(row, rowPrev),
                accrualReportPeriod: calc15(row),
                sumRate: calc17(row, rowPrev)
        ]
        checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)
    }

    // 4. Арифметические проверки расчета итоговой графы
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

/*
 * Вспомогательные методы.
 */

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.numberAccount = 'Итого:'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

def calc12(def row, def rowPrev) {
    if (row.booAccount!=null) {
        BigDecimal x = row.debtBalance * row.course * row.interestRate / row.baseForCalculation *
                (row.calcPeriodAccountingBeginDate - rowPrev.calcPeriodBeginDate + 1) - rowPrev.accrualPrev
        return roundValue(x, 2)
    }
    return null
}

def calc13(def row) {
    if (true) {
        BigDecimal x = row.debtBalance * row.interestRate / row.baseForCalculation *
                (row.calcPeriodAccountingBeginDate - row.calcPeriodAccountingBeginDate + 1)
        return roundValue(x, 2)
    }
    return null
}

def calc14(def row, def rowPrev) {
    return rowPrev.accrualReportPeriod
}

def calc15(def row) {
    BigDecimal x = row.debtBalance * row.interestRate / row.baseForCalculation *
            (row.calcPeriodEndDate - row.calcPeriodBeginDate + 1)
    return roundValue(x, 2)
}

def calc17(def row, def rowPrev) {
    BigDecimal x
    if (true) {
        x = row.debtBalance * row.marketInterestRate / row.baseForCalculation *
                (row.calcPeriodAccountingBeginDate - rowPrev.calcPeriodBeginDate + 1) -
                rowPrev.accrualReportPeriod - rowPrev.sumRate
    } else {
        x = row.debtBalance * row.marketInterestRate / row.baseForCalculation *
                (row.calcPeriodEndDate - rowPrev.calcPeriodBeginDate + 1) - row.accrualPrev
    }
    return roundValue(x, 2)
}

/**
 * Получить курс валюты
 */
def getCourse(def currencyCode, def date) {
    if (currencyCode!=null) {
        def currency = getRecordId(15, 'CODE_2', currencyCode, -1, null, true)
        def res = getRefBookRecord(22, 'CODE_NUMBER', "$currency", date, -1, null, true)
        if (res==null) {
            logger.info("RUB")
            return 1
        }
        return res.RATE.getNumberValue()
    } else {
        return 1
    }
}

// Получить данные за предыдущий период
FormData getFormPrev() {
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    FormData formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
    }
    return formPrev
}

// Получить данные за предыдущий период
def getRowPrev(def row, def dataRowsPrev) {
    if (formPrev!=null) {
        for (def rowPrev in dataRowsPrev) {
            if (row.numberAccount == rowPrev.numberAccount) {
                return rowPrev
            }
        }
    }
    return null
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}
