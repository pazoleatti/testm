package form_template.income.rnu120

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
/**
 * (РНУ-120) Регистр налогового учёта доходов по кредитам, выданным физическим лицам, признаваемым Взаимозависимыми лицами, а также любым физическим независимым лицам, являющимся резидентами оффшорных зон
 * formTemplateId=378
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        prevPeriodCheck()
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
        prevPeriodCheck()
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

// Редактируемые атрибуты (графа 2 .. 4, 5, 6 .. 10, 11, 16)
// TODO - Непонятно что делать с графой 11
@Field
def editableColumns = ['numberAccount', 'debtBalance', 'operationDate',
        'course', 'interestRate', 'baseForCalculation', 'calcPeriodAccountingBeginDate',
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
def arithmeticCheckAlias = ['booAccount', 'taxAccount', 'accrualPrev', 'accrualReportPeriod', 'accrualReportPeriod', 'sumRate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

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
    def dataRows = dataRowHelper.getAllCached()

    def dataRowHelperPrev = getFormDataPrev()
    if (dataRowHelperPrev==null) return
    def dataRowsPrev = dataRowHelperPrev.getAllCached()

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // сортировка
    sortRows(dataRows, groupColums)

    // расчет
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    dataRows.each { row ->
        def rowPrev = getRowPrev(row, dataRowsPrev)

        // графа 1
        row.number = ++rowNumber

        // графа 13
        row.booAccount = calc13(row)

        // графа 12, расчет графы 12 зависит от графы 13
        row.taxAccount = calc12(row, rowPrev)

        // графа 14
        row.accrualPrev = calc14(rowPrev)

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

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        // TODO потом поменять после проверки
        // throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
        logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
    }
}

/** Логические проверки. */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def dataRowHelperPrev = getFormDataPrev()
    if (dataRowHelperPrev==null) return
    def dataRowsPrev = dataRowHelperPrev.getAllCached()

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNumber++
        errorMsg = "Строка $rowNumber: "

        def rowPrev = getRowPrev(row, dataRowsPrev)

        // 0. Проверка уникальности значий в графе «Номер ссудного счета»
        def find = dataRows.find{it->
            (row!=it && row.numberAccount==it.numberAccount)
        }
        if (find!=null) {
            logger.error(errorMsg + "Номер ссудного счета не уникальный!")
        }

        // 1. Проверка на заполнение поля «<Наименование поля>» (1 .. 17)
        checkNonEmptyColumns(row, rowNumber, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Арифметические проверки расчета неитоговых граф
        def calcValues = [
                booAccount: calc13(row),
                taxAccount: calc12(row, rowPrev),
                accrualPrev: calc14(row, rowPrev),
                accrualReportPeriod: calc15(row),
                sumRate: calc17(row, rowPrev)
        ]
        checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)

        // 4. Проверка курса валюты
        if (row.operationDate != null && row.course != null) {
            def name = row.getCell('course').column.name
            def dataProvider = refBookFactory.getDataProvider(22)
            def record = dataProvider.getRecords(row.operationDate, null, "RATE = $row.course", null);
            if (record.size() == 0) {
                RefBook rb = refBookFactory.get(22);
                def rbName = rb.getName()
                def attrName = rb.getAttribute('RATE').getName()
                logger.error("В справочнике «$rbName» не найдено значение «${row.course}», соответствующее атрибуту «$attrName»!")
            }
        }

        // 5. Проверки деления на 0
        if (row.baseForCalculation == 0) {
            def name = row.getCell('baseForCalculation').column.name
            logger.error(errorMsg + "Деление на ноль: «$name» имеет нулевое значение.")
        }

        //для расчета графы 12
        if (row.calcPeriodAccountingEndDate != null && rowPrev.calcPeriodBeginDate != null &&
                (row.calcPeriodAccountingEndDate - rowPrev.calcPeriodBeginDate + 1) == 0) {
            def name1 = row.getCell('calcPeriodAccountingEndDate').column.name+"(${row.getCell('calcPeriodAccountingEndDate').column.groupName})"
            def name2 = row.getCell('calcPeriodBeginDate').column.name+"(${row.getCell('calcPeriodBeginDate').column.groupName})"
            logger.error(errorMsg + "Деление на ноль: количество дней между «$name1» и «$name2»(за предыдущий период) равно 0.")
        }

        //для расчета графы 13
        if (row.calcPeriodAccountingEndDate != null && row.calcPeriodAccountingBeginDate != null &&
                (row.calcPeriodAccountingEndDate - row.calcPeriodAccountingBeginDate + 1)==0) {
            def name1 = row.getCell('calcPeriodAccountingEndDate').column.name+"(${row.getCell('calcPeriodAccountingEndDate').column.groupName})"
            def name2 = row.getCell('calcPeriodAccountingBeginDate').column.name+"(${row.getCell('calcPeriodAccountingBeginDate').column.groupName})"
            logger.error(errorMsg + "Деление на ноль: количество дней между «$name1» и «$name2» равно 0.")
        }

        //для расчета графы 15, 17
        if (row.calcPeriodEndDate != null && row.calcPeriodBeginDate != null &&
                (row.calcPeriodEndDate - row.calcPeriodBeginDate + 1) == 0) {
            def name1 = row.getCell('calcPeriodEndDate').column.name+"(${row.getCell('calcPeriodEndDate').column.groupName})"
            def name2 = row.getCell('calcPeriodBeginDate').column.name+"(${row.getCell('calcPeriodBeginDate').column.groupName})"
            logger.error(errorMsg + "Деление на ноль: количество дней между «$name1» и «$name2» равно 0.")
        }
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
    newRow.numberAccount = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

def calc12(def row, def rowPrev) {
    if (row.booAccount!=null) {
        if (row.debtBalance==null || row.course==null || row.interestRate==null ||
            row.baseForCalculation==null || row.baseForCalculation==0 || row.calcPeriodAccountingEndDate==null ||
            rowPrev.calcPeriodBeginDate==null || (row.calcPeriodAccountingEndDate - rowPrev.calcPeriodBeginDate + 1)==0 ||
            rowPrev.accrualPrev==null) return null
        BigDecimal x = row.debtBalance * row.course * row.interestRate / row.baseForCalculation *
                (row.calcPeriodAccountingEndDate - rowPrev.calcPeriodBeginDate + 1) - rowPrev.accrualPrev
        return roundValue(x, 2)
    }
    return null
}

def calc13(def row) {
    if (true) {
        if (row.debtBalance==null || row.interestRate==null || row.baseForCalculation==null ||
            row.baseForCalculation==0 || row.calcPeriodAccountingEndDate==null || row.calcPeriodAccountingBeginDate==null ||
            (row.calcPeriodAccountingEndDate - row.calcPeriodAccountingBeginDate + 1)==0) return null
        BigDecimal x = row.debtBalance * row.interestRate / row.baseForCalculation *
                (row.calcPeriodAccountingBeginDate - row.calcPeriodAccountingBeginDate + 1)
        return roundValue(x, 2)
    }
    return null
}

def calc14(def rowPrev) {
    return rowPrev.accrualReportPeriod
}

def calc15(def row) {
    if (row.debtBalance==null || row.interestRate==null || row.baseForCalculation==null ||
        row.baseForCalculation==0 || row.calcPeriodEndDate==null || row.calcPeriodBeginDate==null ||
        (row.calcPeriodEndDate - row.calcPeriodBeginDate + 1)==0) return null
    BigDecimal x = row.debtBalance * row.interestRate / row.baseForCalculation *
            (row.calcPeriodEndDate - row.calcPeriodBeginDate + 1)
    return roundValue(x, 2)
}

def calc17(def row, def rowPrev) {
    BigDecimal x
    if (true) {
        if (row.debtBalance==null || row.marketInterestRate==null || row.baseForCalculation==null ||
                row.baseForCalculation==0 || row.calcPeriodAccountingEndDate==null || rowPrev.calcPeriodBeginDate==null ||
                (row.calcPeriodAccountingEndDate - rowPrev.calcPeriodBeginDate + 1)==0 || rowPrev.accrualReportPeriod==null ||
                rowPrev.sumRate==null) return null
        x = row.debtBalance * row.marketInterestRate / row.baseForCalculation *
                (row.calcPeriodAccountingBeginDate - rowPrev.calcPeriodBeginDate + 1) -
                rowPrev.accrualReportPeriod - rowPrev.sumRate
    } else {
        if (row.debtBalance==null || row.marketInterestRate==null || row.baseForCalculation==null ||
                row.baseForCalculation==0 || row.calcPeriodEndDate==null || row.calcPeriodBeginDate==null ||
                (row.calcPeriodEndDate - row.calcPeriodBeginDate + 1)==0 || row.accrualPrev==null) return null
        x = row.debtBalance * row.marketInterestRate / row.baseForCalculation *
                (row.calcPeriodEndDate - rowPrev.calcPeriodBeginDate + 1) - row.accrualPrev
    }
    return roundValue(x, 2)
}

// Получить данные за предыдущий период
def getFormDataPrev() {
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def formPrev = null
    if (reportPeriodPrev != null) {
        formPrev = formDataService.find(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, reportPeriodPrev.id)
        if (formPrev!=null && formPrev.id != null) {
            return formDataService.getDataRowHelper(formPrev)
        }
    }
    return formPrev
}

// Получить данные за предыдущий период
def getRowPrev(def row, def dataRowsPrev) {
    if (dataRowsPrev!=null) {
        for (def rowPrev in dataRowsPrev) {
            if (row.numberAccount == rowPrev.numberAccount) {
                return rowPrev
            }
        }
    }
    return [:]
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}
