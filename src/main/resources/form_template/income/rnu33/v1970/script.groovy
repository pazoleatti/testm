package form_template.income.rnu33.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО"
 * formTemplateId=332
 *
 * @author rtimerbaev
 */

// графа    - fix
// графа 1  - rowNumber
// графа 2  - code                              атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
// графа 3  - valuablePaper                     атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
// графа 4  - issue                             атрибут 814 - ISSUE - «Выпуск», из справочника 84 «Ценные бумаги»
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
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        if (!prevPeriodCheck()) {
            return
        }
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        if (!prevPeriodCheck()) {
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
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
        if (!prevPeriodCheck()) {
            return
        }
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
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

// Все атрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

// Редактируемые атрибуты (графа 2..11, 13..17, 19, 21..23)
@Field
def editableColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// TODO (Ramil Timerbaev) справочник "Ценные бумаги" пуст, поэтому убрал обязательность графы 4 "Выпуск"
// Проверяемые на пустые значения атрибуты (графа 1..27)
@Field
def nonEmptyColumns = ['rowNumber', 'code', 'valuablePaper', /*'issue',*/ 'purchaseDate', 'implementationDate', 'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub', 'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'parPaper', 'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7..9, 13, 15, 17..20, 25..27)
@Field
def totalSumColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal', 'exerciseRuble', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']

// Признак периода ввода остатков
@Field
def isBalancePeriod

// Дата окончания отчетного периода
@Field
def endDate = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого" и "Итого за текущий месяц"
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sort(dataRows)

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    dataRows.each { row ->
        def record61 = (row.code != null ? getRefBookValue(61, row.code) : null)
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
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    // алиасы графов для арифметической проверки (12, 16, 17, 18, 20, 24, 25, 26, 27)
    def arithmeticCheckAlias = ['taxPrice', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
            'allCost', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
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

        // 1. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 2. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Проверка рыночной цены в процентах к номиналу (графа 10, 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.exercisePrice != 100) {
            logger.error(errorMsg + 'Неверно указана цена в процентах при погашении!')
        }

        def record61 = (row.code ? getRefBookValue(61, row.code) : null)
        def code = record61?.CODE?.value

        // 4. Проверка Номера сделки
        if (code != null && code.toString() in codesFromRnu54) {
            logger.error("Строка $index учитывается в РНУ-64!")
        }

        // 5. Проверка даты приобретения и даты реализации (графа 2, 5, 6)
        if (code == 5 && row.purchaseDate <= row.implementationDate) {
            logger.error(errorMsg + 'Неверно указаны даты приобретения и реализации')
        }

        // 6. Проверка рыночной цены в рублях к номиналу (графа 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.marketPriceOnDateAcquisitionInPerc != row.exercisePrice) {
            logger.error(errorMsg + 'Неверно указана цена в рублях при погашении!')
        }

        // 7. Проверка определения срока короткой позиции (графа 2, 21)
        if (code == 5 && row.parPaper >= 0) {
            logger.error(errorMsg + 'Неверно определен срок короткой позиции!')
        }

        // 8. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
        if (code == 5 && row.averageWeightedPricePaper >= 0) {
            logger.error(errorMsg + 'Неверно определен процентный доход по короткой позиции!')
        }

        // 9. Арифметическая проверка графы 12, 16, 17, 18, 20, 24, 25, 26, 27
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
    }

    // 10. Проверка итоговых значений за текущий месяц
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

// Отсорировать данные (по графе 3, 4, 2)
void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 3  - valuablePaper (справочник)
        // графа 4  - issue (справочник)
        // графа 2  - code (справочник)
        if (a.valuablePaper == b.valuablePaper && a.issue == b.issue) {
            def codeA = (a.code ? getRefBookValue(61, a.code)?.CODE?.value : null)
            def codeB = (b.code ? getRefBookValue(61, b.code)?.CODE?.value : null)
            return codeA <=> codeB
        }
        if (a.valuablePaper == b.valuablePaper) {
            def codeA = (a.issue ? getRefBookValue(84, a.issue)?.ISSUE?.value : null)
            def codeB = (b.issue ? getRefBookValue(84, b.issue)?.ISSUE?.value : null)
            return codeA <=> codeB
        }
        def codeA = (a.valuablePaper ? getRefBookValue(62, a.valuablePaper)?.CODE?.value : null)
        def codeB = (b.valuablePaper ? getRefBookValue(62, b.valuablePaper)?.CODE?.value : null)
        return codeA <=> codeB
    }
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
    // сложить текущие суммы и за предыдущие месяцы
    if (formData.kind == FormDataKind.PRIMARY) {
        // получить итоги за предыдущие месяцы текущего налогового периода
        def prevTotal = getPrevTotalRow()
        totalSumColumns.each { alias ->
            def tmp1 = (currentMonthRow.getCell(alias).value ?: 0)
            def tmp2 = (prevTotal?.getCell(alias)?.value ?: 0)
            newRow.getCell(alias).setValue(tmp1 + tmp2, null)
        }
    } else if (formData.kind == FormDataKind.CONSOLIDATED) {
        departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
            if (it.formTypeId == formData.formType.id) {
                def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
                if (source != null && source.state == WorkflowState.ACCEPTED) {
                    formDataService.getDataRowHelper(source).allCached.each { row ->
                        if (row.getAlias() == 'total') {
                            totalSumColumns.each { alias ->
                                def tmp1 = (currentMonthRow.getCell(alias).value ?: 0)
                                def tmp2 = (row.getCell(alias).value ?: 0)
                                newRow.getCell(alias).setValue(tmp1 + tmp2, null)
                            }
                        }
                    }
                }
            }
        }
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
    def tmp = null
    if (code == 1 ||
            ((code == 2 || code == 5) && row.marketPriceOnDateAcquisitionInRub > row.redemptionVal && row.taxPrice > row.exercisePrice) ||
            ((code == 2 || code == 5) && row.marketPricePercent == 0 && row.marketPriceRuble == 0)) {
        tmp = row.exerciseRuble
    } else if (code == 4) {
        tmp = row.redemptionVal
    } else if ((code == 2 || code == 5) && row.exercisePrice < row.marketPricePercent && row.exerciseRuble < row.marketPriceRuble) {
        tmp = row.marketPriceRuble
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
    def tmp = (row.exercisePriceRetirement ? row.exercisePriceRetirement - row.allCost - row.interestEarned.abs() : 0)
    return roundValue(tmp, 2)
}

def calc27(def row, def code) {
    if (row.exercisePriceRetirement == null || row.exerciseRuble == null) {
        return null
    }
    def tmp = (code != 4 ? row.exercisePriceRetirement - row.exerciseRuble : 0)
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

// Признак периода ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def prevPeriodCheck() {
    // Проверка наличия данных предыдущих месяцев
    if (formData.periodOrder > 1 && formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
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
            return false
        }
    }
    return true
}

// Получение импортируемых данных
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    def xml = getXML(ImportInputStream, importService, fileName, '№ пп', 'Итого за текущий месяц')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 27, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ пп',
            (xml.row[0].cell[1]) : 'Код сделки',
            (xml.row[0].cell[2]) : 'Признак ценной бумаги',
            (xml.row[0].cell[3]) : 'Выпуск',
            (xml.row[0].cell[4]) : 'Дата приобретения, закрытия короткой позиции',
            (xml.row[0].cell[5]) : 'Дата реализации, погашения, прочего выбытия, открытия короткой позиции',

            (xml.row[0].cell[6]) : 'Количество сквитованных облигаций, шт.',
            (xml.row[0].cell[7]) : 'Цена приобретения, руб.коп.',
            (xml.row[0].cell[8]) : 'Расходы, по приобретению, руб.коп.',
            (xml.row[0].cell[9]) : 'Рыночная цена на дату приобретения',
            (xml.row[0].cell[11]): 'Цена приобретения для целей налогообложения, руб. коп.',
            (xml.row[0].cell[12]): 'Стоимость погашения, руб.коп.',
            (xml.row[0].cell[13]): 'Цена реализации',
            (xml.row[0].cell[15]): 'Рыночная цена',
            (xml.row[0].cell[17]): 'Цена реализации (выбытия) для целей налогообложения, руб.коп.',
            (xml.row[0].cell[18]): 'Расходы по реализации (выбытию), руб.коп.',
            (xml.row[0].cell[19]): 'Всего расходы, руб.коп.',
            (xml.row[0].cell[20]): 'Показатели для расчёта поцентного дохода за время владения сквитованными облигациями',
            (xml.row[0].cell[24]): 'Процентный доход, полученный за время владения сквитованными облигациями, руб.коп.',
            (xml.row[0].cell[25]): 'Прибыль (+), убыток (-) от реализации (погашения) за вычетом процентного дохода, руб.коп.',
            (xml.row[0].cell[26]): 'Превышение цены реализации для целей налогообложения над ценой реализации, руб.коп.',

            (xml.row[1].cell[9]) : '% к номиналу',
            (xml.row[1].cell[10]): 'руб.коп.',
            (xml.row[1].cell[13]): '% к номиналу',
            (xml.row[1].cell[14]) : 'руб.коп.',
            (xml.row[1].cell[15]) : '% к номиналу',
            (xml.row[1].cell[16]) : 'руб.коп.',
            (xml.row[1].cell[20]) : 'Номинал одной бумаги, руб.коп.',
            (xml.row[1].cell[21]) : 'Средневзвешенная цена одной бумаги на дату размещения, руб.коп.',
            (xml.row[1].cell[22]) : 'Срок обращения согласно условиям выпуска, дней',
            (xml.row[1].cell[23]) : 'Срок владения сквитованными облигациями, дней',
    ]

    (1..27).each { index ->
        headerMapping.put((xml.row[2].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 0 // Смещение для индекса колонок в ошибках импорта

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++

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

        def int xlsIndexRow = xmlIndexRow + rowOffset

        // графа 2 - атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
        newRow.code = getRecordIdImport(61, 'CODE', row.cell[1].text(), xlsIndexRow, 1 + colOffset, true)
        // графа 3 - атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
        newRow.valuablePaper = getRecordIdImport(62, 'CODE', row.cell[2].text(), xlsIndexRow, 1 + colOffset, true)
        // графа 4 - атрибут 814 - ISSUE - «Выпуск», из справочника 84 «Ценные бумаги»
        newRow.issue = getRecordIdImport(84, 'ISSUE', row.cell[3].text(), xlsIndexRow, 1 + colOffset, true)
        // графа 5
        newRow.purchaseDate = getDate(row.cell[4].text(), xlsIndexRow, 1 + colOffset)
        // графа 6
        newRow.implementationDate = getDate(row.cell[5].text(), xlsIndexRow, 1 + colOffset)
        // графа 7
        newRow.bondsCount = getNumber(row.cell[6].text(), xlsIndexRow, 1 + colOffset)
        // графа 8
        newRow.purchaseCost = getNumber(row.cell[7].text(), xlsIndexRow, 1 + colOffset)
        // графа 9
        newRow.costs = getNumber(row.cell[8].text(), xlsIndexRow, 1 + colOffset)
        // графа 10
        newRow.marketPriceOnDateAcquisitionInPerc = getNumber(row.cell[9].text(), xlsIndexRow, 1 + colOffset)
        // графа 11
        newRow.marketPriceOnDateAcquisitionInRub = getNumber(row.cell[10].text(), xlsIndexRow, 1 + colOffset)

        // графа 13
        newRow.redemptionVal = getNumber(row.cell[12].text(), xlsIndexRow, 1 + colOffset)
        // графа 14
        newRow.exercisePrice = getNumber(row.cell[13].text(), xlsIndexRow, 1 + colOffset)
        // графа 15
        newRow.exerciseRuble = getNumber(row.cell[14].text(), xlsIndexRow, 1 + colOffset)
        // графа 16
        newRow.marketPricePercent = getNumber(row.cell[15].text(), xlsIndexRow, 1 + colOffset)
        // графа 17
        newRow.marketPriceRuble = getNumber(row.cell[16].text(), xlsIndexRow, 1 + colOffset)

        // графа 19
        newRow.costsRetirement = getNumber(row.cell[18].text(), xlsIndexRow, 1 + colOffset)

        // графа 21
        newRow.parPaper = getNumber(row.cell[20].text(), xlsIndexRow, 1 + colOffset)
        // графа 22
        newRow.averageWeightedPricePaper = getNumber(row.cell[21].text(), xlsIndexRow, 1 + colOffset)
        // графа 23
        newRow.issueDays = getNumber(row.cell[22].text(), xlsIndexRow, 1 + colOffset)

        rows.add(newRow)
    }

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        // поправить индексы, потому что они после изменения не пересчитываются
        updateIndexes(dataRows)
    }

    // добавить итоговые строки
    dataRows.each { row ->
        rows.add(row)
    }

    dataRowHelper.save(rows)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}