package form_template.income.rnu33

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.text.SimpleDateFormat


/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа 2  - code                              атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
// графа 3  - valuablePaper                     атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
// графа 4  - issue
// графа 5  - purchaseDate
// графа 6  - implementationDate
// графа 7  - bondsCount
// графа 8  - purchaseCost
// графа 9  - costs
// графа 10 - marketPriceOnDateAcquisitionInPerc
// графа 11 - marketPriceOnDateAcquisitionInRub
// графа 12 - taxPrice
// графа 13 - redemptionVal
// графа 14 - exercisePrice
// графа 15 - exerciseRuble
// графа 16 - marketPricePercent
// графа 17 - marketPriceRuble
// графа 18 - exercisePriceRetirement
// графа 19 - costsRetirement
// графа 20 - allCost
// графа 21 - parPaper
// графа 22 - averageWeightedPricePaper
// графа 23 - issueDays
// графа 24 - tenureSkvitovannymiBonds
// графа 25 - interestEarned
// графа 26 - profitLoss
// графа 27 - excessOfTheSellingPrice

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        // TODO убрать когда появится механизм назначения periodOrder при создании формы
        if (formData.periodOrder == null)
            return
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
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

// Все атрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

// Редактируемые атрибуты (графа 2..17, 19, 21..23)
@Field
def editableColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..17, 19..26)
@Field
def nonEmptyColumns = ['rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7..9, 13, 15, 17..20, 25..27)
@Field
def totalSumColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal', 'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

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

    // удалить строку "итого" и "Итого за текущий месяц"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sort(dataRows)

    def rowNumber = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)
    dataRows.each { row ->
        def record61 = (row.code != null? getRefBookValue(61, row.code) : null)
        def code = record61?.CODE?.value
        // графа 1
        row.rowNumber = ++rowNumber
        // графа 12
        row.taxPrice = calc12(row)
        // графа 16
        row.marketPricePercent = calc16(row)
        // графа 17
        row.marketPriceRuble = calc17(row)
        // графа 18
        row.exercisePriceRetirement = calc18(row, code)
        // графа 20
        row.allCost = calc20(row)
        // графа 24
        row.tenureSkvitovannymiBonds = calc24(row)
        // графа 25
        row.interestEarned = calc25(row)
        // графа 26
        row.profitLoss = calc26(row)
        // графа 27
        row.excessOfTheSellingPrice = calc27(row, code)
    }
    // посчитать "Итого за текущий месяц"
    def monthRow = getTotalMonthRow(dataRows)
    dataRows.add(monthRow)

    // добавить строку "Итого за текущий отчётный (налоговый) период"
    def totalRow = getTotalRow(monthRow)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // 7. Проверка наличия данных предыдущих месяцев
    if (formData.periodOrder > 1) {
        def taxPeriodId = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.id
        def monthDates = []
        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        Calendar monthDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder)

        (1..formData.periodOrder - 1).each { monthNumber ->
            def form = formDataService.findMonth(formData.formType.id, formData.kind, formDataDepartment.id, taxPeriodId, monthNumber)
            // если нет формы за какой то месяц, то получить даты начала и окончания месяца
            if (form == null) {
                // дата начала месяца
                monthDate.set(Calendar.MONTH, monthNumber - 1)
                monthDate.set(Calendar.DAY_OF_MONTH, 1)
                def from = format.format(monthDate.time)

                // дата окончания месяца
                monthDate.set(Calendar.MONTH, monthNumber)
                monthDate.set(Calendar.DAY_OF_MONTH, monthDate.get(Calendar.DAY_OF_MONTH) - 1)
                def to = format.format(monthDate.time)

                monthDates.add("$from - $to")
            }
        }
        if (!monthDates.isEmpty()) {
            def periods = monthDates.join(', ')
            logger.error("Экземпляр за период(ы) $periods не существует (отсутствуют первичные данные для расчёта)")
        }
    }

    // алиасы графов для арифметической проверки (12, 16, 17, 18, 20, 24, 25, 26, 27)
    def arithmeticCheckAlias = ['taxPrice', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
            'allCost', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def rowNumber = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)
    def rowsRnu64 = getRnuRowsById(355)
    def codesFromRnu54 = []
    rowsRnu64.each { row ->
        codesFromRnu54.add(row.dealingNumber)
    }

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        def record61 = (row.code ? getRefBookValue(61, row.code) : null)
        def code = record61?.CODE?.value

        // 1. Проверка рыночной цены в процентах к номиналу (графа 10, 13)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.redemptionVal != 100) {
            logger.error(errorMsg + 'неверно указана цена в процентах при погашении!')
        }

        // 2. Проверка Номера сделки
        if (code != null && code.toString() in codesFromRnu54) {
            logger.error("Строка $index учитывается в РНУ-64!")
        }

        // 3. Проверка даты приобретения и даты реализации (графа 2, 5, 6)
        if (code == 5 && row.purchaseDate <= row.implementationDate) {
            logger.error(errorMsg + 'неверно указаны даты приобретения и реализации')
        }

        // 4. Проверка рыночной цены в рублях к номиналу (графа 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.marketPriceOnDateAcquisitionInPerc != row.exercisePrice) {
            logger.error(errorMsg + 'неверно указана цена в рублях при погашении!')
        }

        // 5. Проверка определения срока короткой позиции (графа 2, 21)
        if (code == 5 && row.parPaper >= 0) {
            logger.error(errorMsg + 'неверно определен срок короткой позиции!')
        }

        // 6. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
        if (code == 5 && row.averageWeightedPricePaper >= 0) {
            logger.error(errorMsg + 'неверно определен процентный доход по короткой позиции!')
        }

        // 8. Арифметическая проверка графы 12, 16, 17, 18, 20, 24, 25, 26, 27
        needValue['taxPrice'] = calc12(row)
        needValue['marketPricePercent'] = calc16(row)
        needValue['marketPriceRuble'] = calc17(row)
        needValue['exercisePriceRetirement'] = calc18(row, code)
        needValue['allCost'] = calc20(row)
        needValue['tenureSkvitovannymiBonds'] = calc24(row)
        needValue['interestEarned'] = calc25(row)
        needValue['profitLoss'] = calc26(row)
        needValue['excessOfTheSellingPrice'] = calc27(row, code)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 11. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
        }

        // Проверки соответствия НСИ
        // 1. Проверка актуальности поля «Код сделки» (графа 2)
        checkNSI(61, row, 'code')
        // 2. Проверка актуальности поля «Признак ценной бумаги» (графа 3)
        checkNSI(62, row, 'valuablePaper')
    }

    // 9. Проверка итоговых значений за текущий месяц
    def monthRow = getDataRow(dataRows, 'month')
    def tmpMonthRow = getTotalMonthRow(dataRows)
    if (isDiffRow(monthRow, tmpMonthRow, totalSumColumns)) {
        logger.error("Итоговые значения за текущий месяц рассчитаны неверно!")
    }

    // 10. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
    def totalRow = getDataRow(dataRows, 'total')
    def tmpTotalRow = getTotalRow(monthRow)
    if (isDiffRow(totalRow, tmpTotalRow, totalSumColumns)) {
        logger.error("Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
    }
}

// Получить новую строку с заданными стилями
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

// Отсорировать данные (по графе 3, 4, 2)
void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 3  - valuablePaper (справочник)
        // графа 4  - issue
        // графа 2  - code (справочник)
        if (a.valuablePaper == b.valuablePaper && a.issue == b.issue) {
            def codeA = (a.code ? getRefBookValue(61, a.code)?.CODE?.value : null)
            def codeB = (b.code ? getRefBookValue(61, b.code)?.CODE?.value : null)
            return codeA <=> codeB
        }
        if (a.valuablePaper == b.valuablePaper) {
            return a.issue <=> b.issue
        }
        def codeA = (a.valuablePaper ? getRefBookValue(62, a.valuablePaper)?.CODE?.value : null)
        def codeB = (b.valuablePaper ? getRefBookValue(62, b.valuablePaper)?.CODE?.value : null)
        return codeA <=> codeB
    }
}

// Получить модуль числа. Вместо Math.abs() потому что возможна потеря точности.
def abs(def value) {
    return value < 0 ? -value : value
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

// Получить итоговую строку с суммами за месяц
def getTotalMonthRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('month')
    newRow.getCell('fix').setColSpan(4)
    newRow.fix = 'Итого за текущий месяц'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

// Получить строку с итогами за текущий отчетный период
def getTotalRow(def currentMonthRow) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.getCell('fix').setColSpan(4)
    newRow.fix = 'Итого за текущий отчётный (налоговый) период'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    // получить итоги за предыдущие месяцы текущего налогового периода
    def prevTotal = getPrevTotalRow()
    // сложить текущие суммы и за предыдущие месяцы
    totalSumColumns.each { alias ->
        def tmp1 = (currentMonthRow.getCell(alias).value ?: 0)
        def tmp2 = (prevTotal != null ? (prevTotal.getCell(alias).value ?: 0) : 0)
        newRow.getCell(alias).setValue(tmp1 + tmp2)
    }
    return newRow
}

def calc12(def row) {
    if (row.purchaseCost == null || row.marketPriceOnDateAcquisitionInRub == null) {
        return null
    }
    def tmp = (row.purchaseCost > row.marketPriceOnDateAcquisitionInRub ? row.marketPriceOnDateAcquisitionInRub : row.purchaseCost)
    return roundValue(tmp, 2)
}

def calc16(def row) {
    if (row.redemptionVal == null) {
        return null
    }
    return roundValue((row.redemptionVal > 0 ? 100 : row.marketPricePercent), 4)
}

def calc17(def row) {
    if (row.redemptionVal == null) {
        return null
    }
    return roundValue((row.redemptionVal > 0 ? row.redemptionVal : null), 2)
}

def calc18(def row, def code) {
    if (row.marketPriceOnDateAcquisitionInRub == null || row.redemptionVal == null || row.taxPrice == null ||
            row.exercisePrice == null || row.marketPricePercent == null || row.exerciseRuble == null ||
            row.marketPriceRuble == null) {
        return null
    }
    def tmp
    if (code == 1 ||
            ((code == 2 || code == 5) && row.marketPriceOnDateAcquisitionInRub > row.redemptionVal && row.taxPrice > row.exercisePrice) ||
            ((code == 2 || code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
        tmp = row.exerciseRuble
    } else if (code == 4) {
        tmp = row.redemptionVal
    } else if ((code == 2 || code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
        tmp = row.marketPriceRuble
    } else {
        tmp = null
    }
    return roundValue(tmp, 2)
}

def calc20(def row) {
    if (row.taxPrice == null || row.costs == null || row.costsRetirement == null) {
        return null
    }
    return roundValue(row.taxPrice + row.costs + row.costsRetirement, 2)
}

def calc24(def row) {
    if (row.implementationDate == null || row.purchaseDate == null) {
        return null
    }
    return roundValue(row.implementationDate - row.purchaseDate, 0)
}

def calc25(def row) {
    if (row.parPaper == null || row.averageWeightedPricePaper == null ||
            row.tenureSkvitovannymiBonds == null || row.bondsCount == null || row.issueDays == null ||
            row.issueDays == 0) {
        return null
    }
    def tmp = (row.parPaper - row.averageWeightedPricePaper) * row.tenureSkvitovannymiBonds * row.bondsCount / row.issueDays
    return roundValue(tmp, 2)
}

def calc26(def row) {
    if (row.exercisePriceRetirement == null || row.allCost == null || row.interestEarned == null) {
        return null
    }
    def tmp = (row.exercisePriceRetirement ? row.exercisePriceRetirement - row.allCost - abs(row.interestEarned) : 0)
    return roundValue(tmp, 2)
}

def calc27(def row, def code) {
    if (row.exercisePriceRetirement == null || row.exerciseRuble == null) {
        return null
    }
    def tmp = (code != 4 && row.exercisePriceRetirement != null ? row.exercisePriceRetirement - row.exerciseRuble : 0)
    return roundValue(tmp, 2)
}

// Получить нф за предыдущий месяц
def getPrevTotalRow() {
    if (formData.periodOrder == 1) {
        return null
    }
    def formDataOld = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    if (formDataOld != null) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allCached
        return (dataRowsOld ? getDataRow(dataRowsOld, 'total') : null)
    }
    return null
}

// Получить строки из нф по заданному идентификатору нф
def getRnuRowsById(def id) {
    def formDataRNU = formDataService.find(id, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU != null) {
        return formDataService.getDataRowHelper(formDataRNU)?.allCached
    }
    return null
}