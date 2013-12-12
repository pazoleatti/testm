package form_template.income.rnu23

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Форма "(РНУ-23) Регистр налогового учёта доходов по выданным гарантиям"
 * formTemplateId=323
 *
 * @version 59
 *
 * TODO:
 *      - неясности с консолидацией. Пока убрал расчеты и проверки после консолидации http://jira.aplana.com/browse/SBRFACCTAX-4455.
 *      - поправить загрузку (возможно загрузка не нужна)
 *
 * @author rtimerbaev
 */

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

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4455
        // calc()
        // logicCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        // TODO (Ramil Timerbaev)
        // calc()
        // logicCheck()
        break
    case FormDataEvent.MIGRATION :
        migration()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Все атрибуты
@Field
def allColumns = ['number', 'contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation', 'accrualAccountingStartDate', 'accrualAccountingEndDate', 'preAccrualsStartDate', 'preAccrualsEndDate', 'incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

// Редактируемые атрибуты (графа 2..12)
@Field
def editableColumns = ['contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation', 'accrualAccountingStartDate', 'accrualAccountingEndDate', 'preAccrualsStartDate', 'preAccrualsEndDate']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 5, 3, 2)
@Field
def groupColumns = ['dateOfTransaction', 'contractDate', 'contract']

// Проверяемые на пустые значения атрибуты (графа 1..8, 13..20)
@Field
def nonEmptyColumns = ['number', 'contract', 'contractDate', 'amountOfTheGuarantee', 'dateOfTransaction', 'rateOfTheBankOfRussia', 'interestRate', 'baseForCalculation', 'incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 13..20)
@Field
def totalSumColumns = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']

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

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // РНУ-23 предыдущего периода
    def prevDataRows = getPrevDataRows()
    def totalRowOld = (prevDataRows ? getDataRow(prevDataRows, 'total') : null)

    // удалить строку "итого" и "итого по ГРН: ..."
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // графа 1, 13..20
    dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1
        // графа 13
        row.incomeCurrency = calc13or15(row)
        // графа 14
        row.incomeRuble = calc14(row)
        // графа 15
        row.accountingCurrency = calc13or15(row)
        // графа 16
        row.accountingRuble = calc16(row)
        // графа 17
        row.preChargeCurrency = calc17(totalRowOld)
        // графа 18
        row.preChargeRuble = calc18(totalRowOld)
        // графа 19 (дата графа 11 и 12)
        row.taxPeriodCurrency = calc19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
        // графа 20
        row.taxPeriodRuble = calc20(row)
    }
    // добавить строки "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // алиасы графов для арифметической проверки (графа 13..20)
    def arithmeticCheckAlias = ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble', 'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // РНУ-23 предыдущего периода
    def prevDataRows = getPrevDataRows()
    def prevTotalRow = (prevDataRows == null || prevDataRows.isEmpty() ? null : getDataRow(prevDataRows, 'total'))

    /** Дата начала отчетного периода. */
    def a = reportPeriodService.getStartDate(formData.reportPeriodId).time

    /** Дата окончания отчетного периода. */
    def b = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def rowNumber = 0
    def tmp

    for (def row : dataRows) {
        if (row.getAlias() == 'total') {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (a != null && b != null &&
                ((row.dateOfTransaction != null && (row.dateOfTransaction < a || b < row.dateOfTransaction)) ||
                        (row.accrualAccountingEndDate != null && (row.accrualAccountingEndDate < a || b < row.accrualAccountingEndDate)) ||
                        (row.preAccrualsEndDate != null && (row.preAccrualsEndDate < a || b < row.preAccrualsEndDate)))
        ) {
            logger.error(errorMsg + 'дата совершения операции вне границ отчётного периода!')
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
        }

        // 7. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null &&
                (row.preAccrualsStartDate != null || row.preAccrualsEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = (row.accrualAccountingStartDate != null || row.accrualAccountingEndDate != null) &&
                row.preAccrualsStartDate != null && row.preAccrualsEndDate != null
        if (checkColumn9and10 || checkColumn11and12) {
            logger.error(errorMsg + 'поля в графе 9, 10, 11, 12 заполены неверно!')
        }

        // 9. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.number) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
        }

        // 10. Арифметическая проверка графы 13..20
        needValue['incomeCurrency'] = calc13or15(row)
        needValue['incomeRuble'] = calc14(row)
        needValue['accountingCurrency'] = calc13or15(row)
        needValue['accountingRuble'] = calc16(row)
        needValue['preChargeCurrency'] = calc17(prevTotalRow)
        needValue['preChargeRuble'] = calc18(prevTotalRow)
        needValue['taxPeriodCurrency'] = calc19(row, row.preAccrualsStartDate, row.preAccrualsEndDate)
        needValue['taxPeriodRuble'] = calc20(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, false)

        // TODO (Ramil Timerbaev)
        // проверки деления на ноль
        if (row.baseForCalculation == 0) {
            def name = row.getCell('baseForCalculation').column.name
            logger.error(errorMsg + "деление на ноль: \"$name\" имеет нулевое значение.")
        }
        if (row.accrualAccountingStartDate != null && row.accrualAccountingEndDate != null &&
                (row.accrualAccountingStartDate - row.accrualAccountingEndDate + 1) == 0) {
            def name1 = row.getCell('accrualAccountingStartDate').column.name
            def name2 = row.getCell('accrualAccountingEndDate').column.name
            logger.error(errorMsg + "деление на ноль: количество дней между \"$name1\" и \"$name2\" равно 0.")
        }
        if (row.preAccrualsStartDate != null && row.preAccrualsEndDate != null &&
                (row.preAccrualsStartDate - row.preAccrualsEndDate + 1) == 0) {
            def name1 = row.getCell('preAccrualsStartDate').column.name
            def name2 = row.getCell('preAccrualsEndDate').column.name
            logger.error(errorMsg + "деление на ноль: количество дней между \"$name1\" и \"$name2\" равно 0.")
        }
    }

    def totalRow = getDataRow(dataRows, 'total')
    // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 16, 14, 18)
    if (totalRow.incomeRuble != null && totalRow.preChargeRuble != null && totalRow.accountingRuble != null &&
            totalRow.incomeRuble + totalRow.preChargeRuble < totalRow.accountingRuble) {
        logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
    }

    // 18. Проверка итогового значений по всей форме (графа 13..20)
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить все строки и собрать из источников их строки
    dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(source)
                def sourceDataRows = sourceDataRowHelper.allCached
                // строки источника
                for (def sRow : sourceDataRows) {
                    if (sRow.getAlias() != null) {
                        continue
                    }
                    def isFind = false
                    // строки приемника - искать совпадения, если совпадения есть, то суммировать графы 13..20
                    for (def row : dataRows) {
                        if (sRow.contract == row.contract && sRow.contractDate == row.contractDate &&
                                sRow.dateOfTransaction == row.dateOfTransaction) {
                            isFind = true
                            totalSumColumns.each { alias ->
                                def tmp = (row.getCell(alias).value ?: 0) + (sRow.getCell(alias).value ?: 0)
                                row.getCell(alias).setValue(tmp)
                            }
                            break
                        }
                    }
                    // если совпадений нет, то просто добавить строку
                    if (!isFind) {
                        dataRows.add(sRow)
                    }
                }
            }
        }
    }

    // TODO (Ramil Timerbaev)
    // добавить строки "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)

    if (!dataRows.isEmpty()) {
        dataRowHelper.save(dataRows)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/** Получение импортируемых данных. */
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

void migration() {
    importData()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def total = getTotalRow(dataRows)
        dataRowHelper.insert(total, dataRows.size() + 1)
    }
}

/*
 * Вспомогательные методы.
 */

/** Получить значение графы 13 и 15. */
def calc13or15(def row) {
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
    return calc19(row, date1, date2)
}

/**
 * Получить значение графы 19.
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def calc19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return roundValue(0, 2)
    }
    if (row.baseForCalculation == null || row.amountOfTheGuarantee == null || row.interestRate == null) {
        return null
    }
    def division = row.baseForCalculation * (date2 - date1 + 1)
    if (division == 0) {
        return null
    }
    return roundValue((row.amountOfTheGuarantee * row.interestRate) / (division), 2)
}

def calc14(def row) {
    if (row.incomeCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return roundValue(row.incomeCurrency * row.rateOfTheBankOfRussia, 2)
}

def calc16(def row) {
    if (row.accountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return roundValue(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)
}

/**
 * Получить значение графы 17.
 *
 * @param totalRowOld итоговая строка рну 23 предыдущего отчетного периода
 */
def calc17(def totalRowOld) {
    def tmp = (totalRowOld != null && totalRowOld.taxPeriodCurrency != null ? totalRowOld.taxPeriodCurrency : 0)
    return roundValue(tmp, 2)
}

/**
 * Получить значение графы 18.
 *
 * @param totalRowOld итоговая строка рну 23 предыдущего отчетного периода
 */
def calc18(def totalRowOld) {
    return roundValue((totalRowOld != null && totalRowOld.taxPeriodRuble != null ? totalRowOld.taxPeriodRuble : 0), 2)
}

def calc20(def row) {
    if (row.rateOfTheBankOfRussia == null || row.taxPeriodCurrency == null) {
        return null
    }
    return roundValue(row.taxPeriodCurrency * row.rateOfTheBankOfRussia, 2)
}

/** Получить данные за предыдущий отчетный период. */
def getPrevDataRows() {
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    if (prevFormData != null) {
        return formDataService.getDataRowHelper(prevFormData)?.allCached
    }
    return null
}

/** Получить новую стролу с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 *
 * return итоговая строка
 */
def addData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    // def date = new Date()
    def cache = [:]
    def newRows = []
    def index = 0
    def int rowIndex = 1

    // TODO (Ramil Timerbaev) поправить получение строк если загружать из *.rnu или *.xml
    for (def row : xml.row) {
        index++

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)

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
    dataRowHelper.insert(newRows, 1)

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalCalc = getTotalRow(dataRows)
    def errorColums = []
    if (totalCalc != null) {
        totalSumColumns.each { columnAlias ->
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

/** Получить итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.contract = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}