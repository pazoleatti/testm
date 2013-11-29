package form_template.income.rnu22

import com.aplana.sbrf.taxaccounting.model.*

import java.math.RoundingMode

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*
import groovy.transform.Field

/**
 * Скрипт для РНУ-22.
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * @version 59
 *
 * - нет условии в проверках соответствия НСИ (потому что действительно нет справочников)
 * TODO:
 * 	- графа 19 опущена в регламенте
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
        formDataService.addRow(formData, currentDataRow, editableColumns, arithmeticCheckAlias)
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
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

//Все аттрибуты
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
    // РНУ-22 за предыдущий отчетный период
    def formDataOld = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    formDataOld = formDataOld?.state == WorkflowState.ACCEPTED ? formDataOld : null
    if(formDataOld==null){
        logger.error("Не найдены экземпляры РНУ-22 за прошлый отчетный период!")
    }
    def dataRowsOld = formDataOld ? formDataService.getDataRowHelper(formDataOld)?.allCached : null

    def tmp

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    // Номер последний строки предыдущей формы
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    for (def DataRow row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (!(dFrom != null && dTo != null && ((row.transactionDate != null && row.transactionDate <= dFrom) ||
                (row.calcPeriodAccountingEndDate != null && row.calcPeriodAccountingEndDate <= dTo) ||
                (row.calcPeriodEndDate != null && row.calcPeriodEndDate <= dTo)))) {
            logger.error(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
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
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
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
            logger.error(errorMsg + 'Дата договора неверная!')
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
            logger.error(errorMsg + 'Поля в графах 9, 10, 11, 12 заполены неверно!')
        }

        def date1 = row.calcPeriodBeginDate
        def date2 = row.calcPeriodEndDate
        if (date1 != null && date2 != null && row.basisForCalc != null && row.basisForCalc * (date2 - date1 + 1) == 0) {
            logger.error(errorMsg + "Деление на ноль. Возможно неправильно выбраны даты.")
        }

        // 9. Проверка на уникальность поля «№ пп» (графа 1)
        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

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
        checkCalc(row, arithmeticCheckAlias, values, logger, false)
    }
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // РНУ-22 за предыдущий отчетный период
    def formDataOld = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    formDataOld = formDataOld?.state == WorkflowState.ACCEPTED ? formDataOld : null
    if(formDataOld==null){
        //Прерываем расчет, при проверке сообщение выведется
        //logger.error("Не найдены экземпляры РНУ-22 за прошлый отчетный период!")
        return
    }
    def dataRowsOld = formDataOld ? formDataService.getDataRowHelper(formDataOld)?.allCached : null

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

        def rowPrev
        for (def rowOld in dataRowsOld) {
            if (rowOld.contractNumber == row.contractNumber) {
                rowPrev = rowOld
                break
            }
        }
        // графа 1
        row.rowNumber = ++i

        // графа 13, 15
        def temp = getGraph13_15(row)

        row.accruedCommisCurrency = temp
        row.commisInAccountingCurrency = temp
        row.accruedCommisRub = getGraph14(row)
        row.commisInAccountingRub = getGraph16(row)
        row.accrualPrevCurrency = rowPrev?.reportPeriodCurrency
        row.accrualPrevRub = rowPrev?.reportPeriodRub
        //TODO (Bulat Kinzyabulatov|Sariya Mustafina) описание опущено в регламенте
        row.reportPeriodCurrency = getGraph19(row)
        row.reportPeriodRub = getGraph20(row)
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
    def tmp
    if (date1 == null || date2 == null) {
        tmp = BigDecimal.ZERO
    } else {
        def division = row.basisForCalc * (date2 - date1 + 1)
        if (division == 0) {
            tmp = BigDecimal.ZERO
        } else if (row.base != null && row.interestRate != null) {
            tmp = ((row.base * row.interestRate) / (division))
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
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
    def date1 = row.calcPeriodBeginDate
    def date2 = row.calcPeriodEndDate
    def tmp
    if (date1 == null || date2 == null) {
        tmp = BigDecimal.ZERO
    } else {
        def division = (row.basisForCalc != null) ? row.basisForCalc * (date2 - date1 + 1) : null
        if (division == 0) {
            tmp = BigDecimal.ZERO
        } else if (row.base != null && row.interestRate != null) {
            tmp = ((row.base * row.interestRate) / (division))
        }
    }
    return tmp?.setScale(2, RoundingMode.HALF_UP)
}

BigDecimal getGraph20(def DataRow row) {
    if (row.reportPeriodCurrency != null && row.course != null) {
        return (row.reportPeriodCurrency * row.course).setScale(2, RoundingMode.HALF_UP)
    }
}