package form_template.income.rnu23.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
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
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
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
    case FormDataEvent.COMPOSE:
        consolidation()
        // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4455
        // calc()
        // logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.MIGRATION:
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
def endDate = null

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
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
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
    def totalRowOld
    if (formData.kind == FormDataKind.PRIMARY) {
        def prevDataRows = getPrevDataRows()
        totalRowOld = (prevDataRows ? getDataRow(prevDataRows, 'total') : null)
    }

    // удалить строку "итого" и "итого по ГРН: ..."
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // графа 1, 13..20
    dataRows.eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1
        if (formData.kind == FormDataKind.PRIMARY) {
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
    def prevTotalRow = null
    if (formData.kind == FormDataKind.PRIMARY) {
        def prevDataRows = getPrevDataRows()
        prevTotalRow = (prevDataRows == null || prevDataRows.isEmpty() ? null : getDataRow(prevDataRows, 'total'))
    }

    /** Дата начала отчетного периода. */
    def a = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time

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

        if (formData.kind == FormDataKind.PRIMARY) {
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
                                row.getCell(alias).setValue(tmp, null)
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


/** Получение импортируемых данных. */
void importData() {
    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Номер договора',
            (xml.row[0].cell[2]): 'Дата договора',
            (xml.row[0].cell[3]): 'Сумма гарантии',
            (xml.row[0].cell[4]): 'Дата совершения операции',
            (xml.row[0].cell[5]): 'Курс Банка России',
            (xml.row[0].cell[6]): 'Процентная ставка',
            (xml.row[0].cell[7]): 'База для расчёта (дни)',
            (xml.row[0].cell[8]): 'Расчётный период',
            (xml.row[0].cell[12]): 'Сумма в налоговом учёте',
            (xml.row[0].cell[14]): 'Сумма в бухгалтерском учёте',
            (xml.row[0].cell[16]): 'Сумма доначисления',
            (xml.row[1].cell[8]): 'начисление/факт',
            (xml.row[1].cell[10]): 'доначисление',
            (xml.row[1].cell[12]): 'валюта',
            (xml.row[1].cell[13]): 'рубли',
            (xml.row[1].cell[14]): 'валюта',
            (xml.row[1].cell[15]): 'рубли',
            (xml.row[1].cell[16]): 'предыдущий период',
            (xml.row[1].cell[18]): 'отчётный период',
            (xml.row[2].cell[8]): 'дата начала',
            (xml.row[2].cell[9]): 'дата окончания',
            (xml.row[2].cell[10]): 'дата начала',
            (xml.row[2].cell[11]): 'дата окончания',
            (xml.row[2].cell[16]): 'валюта',
            (xml.row[2].cell[17]): 'рубли',
            (xml.row[2].cell[18]): 'валюта',
            (xml.row[2].cell[19]): 'рубли',
            (xml.row[3].cell[0]): '1',
            (xml.row[3].cell[1]): '2',
            (xml.row[3].cell[2]): '3',
            (xml.row[3].cell[3]): '4',
            (xml.row[3].cell[4]): '5',
            (xml.row[3].cell[5]): '6',
            (xml.row[3].cell[6]): '7',
            (xml.row[3].cell[7]): '8',
            (xml.row[3].cell[8]): '9',
            (xml.row[3].cell[9]): '10',
            (xml.row[3].cell[10]): '11',
            (xml.row[3].cell[11]): '12',
            (xml.row[3].cell[12]): '13',
            (xml.row[3].cell[13]): '14',
            (xml.row[3].cell[14]): '15',
            (xml.row[3].cell[15]): '16',
            (xml.row[3].cell[16]): '17',
            (xml.row[3].cell[17]): '18',
            (xml.row[3].cell[18]): '19',
            (xml.row[3].cell[19]): '20'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 3)



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

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        newRow.number = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 2
        newRow.contract =    row.cell[1].text()

        // графа 3
        newRow.contractDate    = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, false)

        // графа 4
        newRow.amountOfTheGuarantee = parseNumber(row.cell[3].text(), xlsIndexRow, 3 + colOffset, logger, false)

        // графа 5
        newRow.dateOfTransaction = parseDate(row.cell[4].text(), "dd.MM.yyyy", xlsIndexRow, 4 + colOffset, logger, false)

        // графа 6
        newRow.rateOfTheBankOfRussia  = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, false)

        // графа 7
        newRow.interestRate = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, false)

        // графа 8
        newRow.baseForCalculation = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, false)

        // графа 9
        newRow.accrualAccountingStartDate =  parseDate(row.cell[8].text(), "dd.MM.yyyy", xlsIndexRow, 8 + colOffset, logger, false)

        // графа 10
        newRow.accrualAccountingEndDate = parseDate(row.cell[9].text(), "dd.MM.yyyy", xlsIndexRow, 9 + colOffset, logger, false)

        // графа 11
        newRow.preAccrualsStartDate =   parseDate(row.cell[10].text(), "dd.MM.yyyy", xlsIndexRow, 10 + colOffset, logger, false)

        // графа 12
        newRow.preAccrualsEndDate =    parseDate(row.cell[11].text(), "dd.MM.yyyy", xlsIndexRow, 11 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
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
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}