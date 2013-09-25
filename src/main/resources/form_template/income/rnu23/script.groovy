package form_template.income.rnu23

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-23) Регистр налогового учёта доходов по выданным гарантиям".
 *
 * @version 59
 *
 * TODO:
 *      - неясности с консолидацией. Пока убрал расчеты и проверки после консолидации http://jira.aplana.com/browse/SBRFACCTAX-4455.
 *      - поправить загрузку (возможно загрузка не нужна)
 *      - проверки нси: курс валют http://jira.aplana.com/browse/SBRFACCTAX-4446
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.COMPOSE : // обобщить
        consolidation()
        // calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.IMPORT :
        importData()
        // TODO (Ramil Timerbaev)
        // !hasError() && calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.MIGRATION :
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
        break
}

// графа 1  - number
// графа 2  - contract
// графа 3  - contractDate
// графа 4  - amountOfTheGuarantee
// графа 5  - dateOfTransaction
// графа 6  - rateOfTheBankOfRussia
// графа 7  - interestRate
// графа 8  - baseForCalculation
// графа 9  - accrualAccountingStartDate
// графа 10 - accrualAccountingEndDate
// графа 11 - preAccrualsStartDate
// графа 12 - preAccrualsEndDate
// графа 13 - incomeCurrency
// графа 14 - incomeRuble
// графа 15 - accountingCurrency
// графа 16 - accountingRuble
// графа 17 - preChargeCurrency
// графа 18 - preChargeRuble
// графа 19 - taxPeriodCurrency
// графа 20 - taxPeriodRuble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size()-1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (!isFixedRow(row)) {
                index = getIndex(row) + 1
                break
            }
        }
    }
    data.insert(getNewRow(),index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def data = getData(formData)
    data.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)

    /*
     * Проверка обязательных полей.
     */

    // список проверяемых столбцов (графа 2..8)
    def requiredColumns = ['contract', 'contractDate', 'amountOfTheGuarantee',
            'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation']

    for (def row : getRows(data)) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // РНУ-22 предыдущего периода
    def formDataOld = getFormDataOld()
    def totalRowOld = getRowByAlias(getData(formDataOld), 'total')

    /*
     * Расчеты.
     */

    // удалить строку "итого"
    def deleteRows = []
    getRows(data).each { row ->
        if (isFixedRow(row)) {
            deleteRows.add(row)
        }
    }
    getRows(data).removeAll(deleteRows)
    if (getRows(data).isEmpty()) {
        return true
    }

    // отсортировать/группировать
    getRows(data).sort { def a, def b ->
        // графа 2  - contract
        // графа 3  - contractDate
        // графа 5  - dateOfTransaction
        if (a.dateOfTransaction == b.dateOfTransaction && a.contractDate == b.contractDate) {
            return a.contract <=> b.contract
        }
        if (a.dateOfTransaction == b.dateOfTransaction) {
            return a.contractDate <=> b.contractDate
        }
        return a.dateOfTransaction <=> b.dateOfTransaction
    }

    // графа 1, 13..20
    getRows(data).eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 13
        row.incomeCurrency = getColumn13or15(row)

        // графа 14
        row.incomeRuble = roundValue(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 15
        row.accountingCurrency = getColumn13or15(row)

        // графа 16
        row.accountingRuble = roundValue(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 17
        row.preChargeCurrency = roundValue((totalRowOld != null ? totalRowOld.taxPeriodCurrency : 0), 2)

        // графа 18
        row.preChargeRuble = roundValue((totalRowOld != null ? totalRowOld.taxPeriodRuble : 0), 2)

        // графа 19 (дата графа 11 и 12)
        row.taxPeriodCurrency = getColumn13or15or19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)

        // графа 20
        row.taxPeriodRuble = roundValue(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)
    }
    save(data)

    // добавить строки "итого"
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    def i = 1

    // список проверяемых столбцов (графа 1..8, 13..20)
    def requiredColumns = ['number', 'contract', 'contractDate', 'amountOfTheGuarantee',
            'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
            'incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
            'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
    for (def row : getRows(data)) {
        if (!isFixedRow(row)) {
            // 7. Обязательность заполнения поля графы 1..8, 13..20
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }
    }

    // суммы строки общих итогов
    def totalSums = [:]

    // графы для которых надо вычислять итого (графа 13..20)
    def totalColumns = getTotalColumns()

    // признак наличия итоговых строк
    def hasTotal = false

    def index
    def errorMsg

    for (def row : getRows(data)) {
        if (isTotal(row)) {
            hasTotal = true
            continue
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (a != null && b != null &&
                ((row.dateOfTransaction != null && (row.dateOfTransaction < a || b < row.dateOfTransaction)) ||
                        (row.accrualAccountingEndDate != null && (row.accrualAccountingEndDate < a || b < row.accrualAccountingEndDate)) ||
                        (row.preAccrualsEndDate != null && (row.preAccrualsEndDate < a || b < row.preAccrualsEndDate)))
        ) {
            logger.error(errorMsg + 'дата совершения операции вне границ отчётного периода!')
            return false
        }

        // 2. Проверка на нулевые значения (графа 13..20)
        def hasNull = true
        ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
                'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble'].each { alias ->
            tmp = row.getCell(alias).getValue()
            if (tmp != null && tmp != 0) {
                hasNull = false
            }
        }
        if (hasNull) {
            logger.error(errorMsg + 'все суммы по операции нулевые!')
            return false
        }

        // 3. Проверка на сумму гарантии (графа 4)
        if (row.amountOfTheGuarantee != null && row.amountOfTheGuarantee == 0) {
            logger.warn(errorMsg + 'суммы гарантии равны нулю!')
        }

        // 4. Проверка задания расчётного периода (графа 9, 10, 11, 12)
        if (row.accrualAccountingStartDate > row.accrualAccountingEndDate ||
                row.preAccrualsStartDate > row.preAccrualsEndDate) {
            logger.warn(errorMsg + 'неправильно задан расчётный период!')
        }

        // 5. Проверка на корректность даты договора (графа 3)
        if (row.contractDate > b) {
            logger.error(errorMsg + 'дата договора неверная!')
            return false
        }

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null &&
                (row.preAccrualsStartDate != null || row.preAccrualsEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = (row.accrualAccountingStartDate != null || row.accrualAccountingEndDate != null) &&
                row.preAccrualsStartDate != null && row.preAccrualsEndDate != null
        if (checkColumn9and10 || checkColumn11and12) {
            logger.error(errorMsg + 'поля в графе 9, 10, 11, 12 заполены неверно!')
            return  false
        }

        // 9. Проверка на уникальность поля «№ пп» (графа 1)
        if (i != row.number) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
            return  false
        }
        i++

        // 10. Арифметическая проверка графы 13
        tmp = getColumn13or15(row)
        if (row.incomeCurrency != tmp) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма начисленного дохода. Валюта»!')
        }

        // 11. Арифметическая проверка графы 14
        if (row.incomeRuble != roundValue(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма начисленного дохода. Рубли»!')
        }

        // 12. Арифметическая проверка графы 15
        tmp = getColumn13or15(row)
        if (row.accountingCurrency != tmp) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма дохода, отражённая в бухгалтерском учёте. Валюта»!')
        }

        // 13. Арифметическая проверка графы 16
        if (row.accountingRuble != roundValue(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма дохода, отражённая в бухгалтерском учёте. Рубли»!')
        }

        // 14. Арифметическая проверка графы 17
        tmp = getSum(formDataOld, 'taxPeriodCurrency')
        if (row.preChargeCurrency != tmp) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма доначисления. Предыдущий период. Валюта»!')
        }

        // 15. Арифметическая проверка графы 18
        tmp = getSum(formDataOld, 'taxPeriodRuble')
        if (row.preChargeRuble != tmp) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма доначисления. Предыдущий период. Рубли»!')
        }

        // 16. Арифметическая проверка графы 19
        tmp = getColumn13or15or19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
        if (row.taxPeriodCurrency != tmp) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма доначисления. Отчётный период. Валюта»!')
        }

        // 17. Арифметическая проверка графы 20
        if (row.taxPeriodRuble != roundValue(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)) {
            logger.warn(errorMsg + 'неверно рассчитана графа «Сумма доначисления. Отчётный период. Рубли»!')
        }

        // 18. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += row.getCell(alias).getValue()
        }
    }

    if (hasTotal) {
        def totalRow = getRowByAlias(data, 'total')

        // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 16, 14, 18)
        if (totalRow.incomeRuble + totalRow.preChargeRuble < totalRow.accountingRuble) {
            logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
        }

        // 18. Проверка итогового значений по всей форме (графа 13..20)
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.error('Итоговые значения рассчитаны неверно!')
                return false
            }
        }
    } else {
        logger.error('Итоговые значения не рассчитаны')
        return false
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // TODO (Ramil Timerbaev) курсы валют
//    def data = getData(formData)
//    def tmp
//    for (def row : getRows(data)) {
//        // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
//        tmp = row.rateOfTheBankOfRussia
//        // справочник 22 "Курс валют"
//        getRecordId(22, String code, def value, Date date, def cache)
//        if (false) {
//            logger.warn('Неверный курс валюты!')
//            return false
//        }
//    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)

    // удалить все строки и собрать из источников их строки
    data.clear()

    def newRows = []
    def sumColumns = getTotalColumns()
    def isFind
    def tmp
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceData = getData(source)
                // строки источника
                getRows(sourceData).each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        isFind = false

                        // строки приемника - искать совпадения, если совпадения есть, то суммировать графы 13..20
                        for (def dRow : newRows) {
                            if (sRow.contract == dRow.contract && sRow.contractDate == dRow.contractDate &&
                                    sRow.dateOfTransaction == dRow.dateOfTransaction) {
                                isFind = true
                                sumColumns.each { alias ->
                                    tmp = (dRow.getCell(alias).getValue() ?: 0) + (sRow.getCell(alias).getValue() ?: 0)
                                    dRow.getCell(alias).setValue(tmp)
                                }
                                break
                            }
                        }
                        // если совпадений нет, то просто добавить строку
                        if (!isFind) {
                            newRows.add(sRow)
                        }
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
    }
    save(data)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    /** Признак периода ввода остатков. */
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

    //проверка периода ввода остатков
    if (isBalancePeriod) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    // TODO (Ramil Timerbaev) поправить формат на правильный
    if (!fileName.contains('.r')) {
        logger.error('Формат файла должен быть *.r??')
        return
    }

    // TODO (Ramil Timerbaev) поправить параметры
    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml)

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить значение графы 13 и 15 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 */
def getColumn13or15(def row) {
    def date1
    def date2
    if (row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null) {
        // графа 9 и 10
        date1 = row.accrualAccountingStartDate
        date2 = row.accrualAccountingEndDate
    } else if (row.preAccrualsStartDate != null && row.preAccrualsEndDate != null) {
        // графа 11 и 12
        date1 = row.preAccrualsStartDate
        date2 = row.preAccrualsEndDate
    } else {
        return null
    }
    return getColumn13or15or19(row, date1, date2)
}

/**
 * Получить значение графы 13 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def getColumn13or15or19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return 0
    }
    def division = row.baseForCalculation * (date2 - date1 + 1)
    if (division == 0) {
        def index = row.getIndex()
        throw new ServiceLoggerException('Деление на ноль в строке ' + index + '. Возможно неправильно выбраны даты.', logger.getEntries())
    }
    return roundValue((row.amountOfTheGuarantee * row.interestRate) / (division), 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    def to = 0
    def from = getRows(form).size() - 2
    if (to > from) {
        return 0
    }
    return summ(form, new ColumnRange(columnAlias, to, from))
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период)
    def formDataOld = null
    if (prevReportPeriod != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, prevReportPeriod.id)
    }
    return formDataOld
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'contract', 'contractDate', 'amountOfTheGuarantee',
            'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate',
            'baseForCalculation', 'accrualAccountingStartDate', 'accrualAccountingEndDate',
            'preAccrualsStartDate', 'preAccrualsEndDate', 'incomeCurrency', 'incomeRuble',
            'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble',
            'taxPeriodCurrency', 'taxPeriodRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить номер строки в таблице (1..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex() - 1
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // графа 2..12
    ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction',
            'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation',
            'accrualAccountingStartDate', 'accrualAccountingEndDate',
            'preAccrualsStartDate', 'preAccrualsEndDate'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Получить список строк формы.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 *
 * return итоговая строка
 */
def addData(def xml) {
    def data = getData(formData)
    data.clear()

    // def date = new Date()
    def cache = [:]
    def newRows = []
    def index = 0

    // TODO (Ramil Timerbaev) поправить получение строк если загружать из *.rnu или *.xml
    for (def row : xml.row) {
        index++

        def newRow = getNewRow()
        def indexCell = 0

        // графа 1
        newRow.number = getNumber(row.cell[indexCell].text())
        index++

        // графа 2
        newRow.contract = getNumber(row.cell[indexCell].text())
        index++

        // графа 3
        newRow.contractDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 4
        newRow.amountOfTheGuarantee = getNumber(row.cell[indexCell].text())
        index++

        // графа 5
        newRow.dateOfTransaction = getNumber(row.cell[indexCell].text())
        index++

        // графа 6
        newRow.rateOfTheBankOfRussia = getNumber(row.cell[indexCell].text())
        index++

        // графа 7
        newRow.interestRate = getNumber(row.cell[indexCell].text())
        index++

        // графа 8
        newRow.baseForCalculation = getNumber(row.cell[indexCell].text())
        index++

        // графа 9
        newRow.accrualAccountingStartDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 10
        newRow.accrualAccountingEndDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 11
        newRow.preAccrualsStartDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 12
        newRow.preAccrualsEndDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 13
        newRow.incomeCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 14
        newRow.incomeRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 15
        newRow.accountingCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 16
        newRow.accountingRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 17
        newRow.preChargeCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 18
        newRow.preChargeRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 19
        newRow.taxPeriodCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 20
        newRow.taxPeriodRuble = getNumber(row.cell[indexCell].text())
        index++

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() > 0) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        index = 12

        // TODO (Ramil Timerbaev) поправить/уточнить
        // графа 13
        total.incomeCurrency = getNumber(row.cell[index++].text())
        // графа 14
        total.incomeRuble = getNumber(row.cell[index++].text())
        // графа 15
        total.accountingCurrency = getNumber(row.cell[index++].text())
        // графа 16
        total.accountingRuble = getNumber(row.cell[index++].text())
        // графа 17
        total.preChargeCurrency = getNumber(row.cell[index++].text())
        // графа 18
        total.preChargeRuble = getNumber(row.cell[index++].text())
        // графа 19
        total.taxPeriodCurrency = getNumber(row.cell[index++].text())
        // графа 20
        total.taxPeriodRuble = getNumber(row.cell[index++].text())

        return total
    } else {
        return null
    }
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = getTotalColumns()
    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(totalCalc.getCell(columnAlias).column.order)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contract = 'Итого'
    setTotalStyle(totalRow)

    def totalColumns = getTotalColumns()
    def data = getData(formData)
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    // просуммировать значения неитоговых строк
    for (def row : getRows(data)) {
        if (row.getAlias() != null) {
            continue
        }
        totalColumns.each { alias ->
            tmp = totalRow.getCell(alias).getValue() + (row.getCell(alias).getValue() ?: 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }
    return totalRow
}

/**
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить список графов для которых надо вычислять итого (графа 13..20).
 */
def getTotalColumns() {
    return ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
            'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
}