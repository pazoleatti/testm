package form_template.income.rnu33.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО"
 * formTemplateId=332
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - code                              атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
// графа 3  - valuablePaper                     атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
// графа 4  - issue                             абсолютное значение - атрибут 812 - SHORTNAME - «Выпуск», из справочника 84 «Ценные бумаги» текст
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = (isMonthBalance() ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, null)
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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, logger, ['month', 'total'])
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
def allColumns = ['rowNumber', 'fix', 'code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
                  'bondsCount', 'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc',
                  'marketPriceOnDateAcquisitionInRub', 'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble',
                  'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost',
                  'parPaper', 'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
                  'profitLoss', 'excessOfTheSellingPrice']

// Редактируемые атрибуты (графа 2..11, 13..17, 19, 21..23)
@Field
def editableColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount',
                       'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub',
                       'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent', 'marketPriceRuble',
                       'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..27)
@Field
def nonEmptyColumns = ['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate', 'bondsCount',
                       'purchaseCost', 'costs', 'marketPriceOnDateAcquisitionInPerc', 'marketPriceOnDateAcquisitionInRub',
                       'taxPrice', 'redemptionVal', 'exercisePrice', 'exerciseRuble', 'marketPricePercent',
                       'marketPriceRuble', 'exercisePriceRetirement', 'costsRetirement', 'allCost', 'parPaper',
                       'averageWeightedPricePaper', 'issueDays', 'tenureSkvitovannymiBonds', 'interestEarned',
                       'profitLoss', 'excessOfTheSellingPrice']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7..9, 13, 15, 17..20, 25..27)
@Field
def totalSumColumns = ['bondsCount', 'purchaseCost', 'costs', 'redemptionVal', 'exerciseRuble', 'marketPriceRuble',
                       'exercisePriceRetirement', 'costsRetirement', 'allCost', 'interestEarned', 'profitLoss',
                       'excessOfTheSellingPrice']

// Признак периода ввода остатков
@Field
def isBalance

// Налоговый период
@Field
def taxPeriod = null

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
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

    // отсортировать/группировать
    sort(dataRows)

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        if (isMonthBalance()) {
            continue
        }
        def code = getRefBookValue(61, row.code)?.CODE?.value

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

    def monthRow = getDataRow(dataRows, 'month')
    def totalRow = getDataRow(dataRows, 'total')

    // посчитать "Итого за текущий месяц"
    calcTotalSum(dataRows - totalRow, monthRow, totalSumColumns)

    // посчитать строку "Итого за текущий отчётный (налоговый) период"
    calcTotalRow(monthRow, totalRow)

    dataRowHelper.save(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def dataProvider = refBookFactory.getDataProvider(84)
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

        // 2. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isMonthBalance())

        // 3. Проверка рыночной цены в процентах к номиналу (графа 10, 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.redemptionVal != 100) {
            loggerError(row, errorMsg + 'Неверно указана цена в процентах при погашении!')
        }

        def code = getRefBookValue(61, row.code)?.CODE?.value

        // 4. Проверка Номера сделки
        if (code != null && code.toString() in codesFromRnu54) {
            loggerError(row, "Строка $index учитывается в РНУ-64!")
        }

        // 5. Проверка даты приобретения и даты реализации (графа 2, 5, 6)
        if (code == 5 && row.purchaseDate <= row.implementationDate) {
            loggerError(row, errorMsg + 'Неверно указаны даты приобретения и реализации')
        }

        // 5. Проверка рыночной цены в рублях к номиналу (графа 14)
        if (row.marketPriceOnDateAcquisitionInPerc > 0 && row.marketPriceOnDateAcquisitionInPerc != row.exercisePrice) {
            loggerError(row, errorMsg + 'Неверно указана цена в рублях при погашении!')
        }

        // 6. Проверка определения срока короткой позиции (графа 2, 21)
        if (code == 5 && row.parPaper >= 0) {
            loggerError(row, errorMsg + 'Неверно определен срок короткой позиции!')
        }

        // 7. Проверка определения процентного дохода по короткой позиции (графа 2, 22)
        if (code == 5 && row.averageWeightedPricePaper >= 0) {
            loggerError(row, errorMsg + 'Неверно определен процентный доход по короткой позиции!')
        }

        // 8. Арифметическая проверка графы 12, 16, 17, 18, 20, 24, 25, 26, 27
        if (!isMonthBalance()) {
            // алиасы графов для арифметической проверки
            def arithmeticCheckAlias = ['taxPrice', 'marketPricePercent', 'marketPriceRuble', 'exercisePriceRetirement',
                    'allCost', 'tenureSkvitovannymiBonds', 'interestEarned', 'profitLoss', 'excessOfTheSellingPrice']
            def needValue = [:]
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
        def record = dataProvider.getRecords(reportPeriodEndDate, null, "SHORTNAME = '${row.issue}'", null)
        if (record.size() == 0) {
            loggerError(row, errorMsg + "Значение графы «Выпуск» отсутствует в справочнике «Ценные бумаги»")
        }
    }

    // 10. Проверка итоговых значений за текущий месяц
    def monthRow = getDataRow(dataRows, 'month')
    def tmpMonthRow = getTotalMonthRow(dataRows)
    if (isDiffRow(monthRow, tmpMonthRow, totalSumColumns)) {
        loggerError(null, "Итоговые значения за текущий месяц рассчитаны неверно!")
    }

    // 10. Проверка итоговых значений за текущий отчётный (налоговый) период - подсчет сумм для общих итогов
    def totalRow = getDataRow(dataRows, 'total')
    def tmpTotalRow = formData.createDataRow()
    calcTotalRow(monthRow, tmpTotalRow)
    if (isDiffRow(totalRow, tmpTotalRow, totalSumColumns)) {
        loggerError(null, "Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
    }
}

// Отсорировать данные (по графе 3, 4, 2)
void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 3  - valuablePaper (справочник)
        // графа 4  - issue (справочник)
        // графа 2  - code (справочник)
        if (a.getAlias() != null || b.getAlias() != null) {
            return a.getAlias() <=> b.getAlias()
        }
        if (a.valuablePaper == b.valuablePaper && a.issue == b.issue) {
            def codeA = getRefBookValue(61, a.code)?.CODE?.value
            def codeB = getRefBookValue(61, b.code)?.CODE?.value
            return codeA <=> codeB
        }
        if (a.valuablePaper == b.valuablePaper) {
            def codeA = a.issue
            def codeB = b.issue
            return codeA <=> codeB
        }
        def codeA = getRefBookValue(62, a.valuablePaper)?.CODE?.value
        def codeB = getRefBookValue(62, b.valuablePaper)?.CODE?.value
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
void calcTotalRow(def currentMonthRow, def currentTotalRow) {
    // сложить текущие суммы и за предыдущие месяцы
    if (formData.kind == FormDataKind.PRIMARY) {
        // получить итоги за предыдущие месяцы текущего налогового периода
        def prevTotal = getPrevTotalRow()
        totalSumColumns.each { alias ->
            def tmp1 = (currentMonthRow.getCell(alias).value ?: 0)
            def tmp2 = (prevTotal?.getCell(alias)?.value ?: 0)
            currentTotalRow.getCell(alias).setValue(tmp1 + tmp2, null)
        }
    } else if (formData.kind == FormDataKind.CONSOLIDATED) {
        totalSumColumns.each { alias ->
            currentTotalRow.getCell(alias).setValue(0, null)
        }

        departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
                getReportPeriodStartDate(), getReportPeriodEndDate()).each {
            if (it.formTypeId == formData.formType.id) {
                def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
                if (source != null && source.state == WorkflowState.ACCEPTED) {
                    formDataService.getDataRowHelper(source).allCached.each { row ->
                        if (row.getAlias() == 'total') {
                            totalSumColumns.each { alias ->
                                // суммировать только итоговые значения за налоговый период (alias == total)
                                def tmp1 = currentTotalRow.getCell(alias).value
                                def tmp2 = (row.getCell(alias).value ?: 0)
                                currentTotalRow.getCell(alias).setValue(tmp1 + tmp2, null)
                            }
                        }
                    }
                }
            }
        }
    }
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
    def formDataOld = formDataService.getFormDataPrev(formData)
    if (formDataOld != null) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allCached
        return (dataRowsOld ? getDataRow(dataRowsOld, 'total') : null)
    }
    return null
}

// Получить строки из нф по заданному идентификатору нф
def getRnuRowsById(def id) {
    def formDataRNU = formDataService.getLast(id, formData.kind, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder)
    if (formDataRNU != null) {
        return formDataService.getDataRowHelper(formDataRNU)?.allCached
    }
    return null
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        if (!departmentReportPeriod.isBalance() || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = formData.periodOrder - 1 % 3 == 0
        }
    }
    return isBalance
}

void prevPeriodCheck() {
    // Проверка наличия данных предыдущих месяцев
    if (formData.periodOrder > 1 && formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true)
    }
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), 'Итого за текущий месяц')

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 27, 3)

    def headerMapping = [
            (xml.row[0].cell[0])  : getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2])  : getColumnName(tmpRow, 'code'),
            (xml.row[0].cell[3])  : getColumnName(tmpRow, 'valuablePaper'),
            (xml.row[0].cell[4])  : getColumnName(tmpRow, 'issue'),
            (xml.row[0].cell[5])  : getColumnName(tmpRow, 'purchaseDate'),
            (xml.row[0].cell[6])  : getColumnName(tmpRow, 'implementationDate'),
            (xml.row[0].cell[7])  : getColumnName(tmpRow, 'bondsCount'),
            (xml.row[0].cell[8])  : getColumnName(tmpRow, 'purchaseCost'),
            (xml.row[0].cell[9])  : getColumnName(tmpRow, 'costs'),
            (xml.row[0].cell[10]) : 'Рыночная цена на дату приобретения',
            (xml.row[0].cell[12]) : getColumnName(tmpRow, 'taxPrice'),
            (xml.row[0].cell[13]) : getColumnName(tmpRow, 'redemptionVal'),
            (xml.row[0].cell[14]) : 'Цена реализации',
            (xml.row[0].cell[16]) : 'Рыночная цена',
            (xml.row[0].cell[18]) : getColumnName(tmpRow, 'exercisePriceRetirement'),
            (xml.row[0].cell[19]) : getColumnName(tmpRow, 'costsRetirement'),
            (xml.row[0].cell[20]) : getColumnName(tmpRow, 'allCost'),
            (xml.row[0].cell[21]) : 'Показатели для расчёта поцентного дохода за время владения сквитованными облигациями',
            (xml.row[0].cell[25]) : getColumnName(tmpRow, 'interestEarned'),
            (xml.row[0].cell[26]) : getColumnName(tmpRow, 'profitLoss'),
            (xml.row[0].cell[27]) : getColumnName(tmpRow, 'excessOfTheSellingPrice'),

            (xml.row[1].cell[10]) : '% к номиналу',
            (xml.row[1].cell[11]) : 'руб.коп.',
            (xml.row[1].cell[14]) : '% к номиналу',
            (xml.row[1].cell[15]) : 'руб.коп.',
            (xml.row[1].cell[16]) : '% к номиналу',
            (xml.row[1].cell[17]) : 'руб.коп.',
            (xml.row[1].cell[21]) : 'Номинал одной бумаги, руб.коп.',
            (xml.row[1].cell[22]) : 'Средневзвешенная цена одной бумаги на дату размещения, руб.коп.',
            (xml.row[1].cell[23]) : 'Срок обращения согласно условиям выпуска, дней',
            (xml.row[1].cell[24]) : 'Срок владения сквитованными облигациями, дней',

            (xml.row[2].cell[0])  : '1'
    ]

    (2..27).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        def columns = (isMonthBalance() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 2 - атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
        def xmlIndexCol = 2
        newRow.code = getRecordIdImport(61, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 3 - атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
        xmlIndexCol = 3
        newRow.valuablePaper = getRecordIdImport(62, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 4 - атрибут 814 - SHORTNAME - «Выпуск», из справочника 84 «Ценные бумаги» текстовое значение
        xmlIndexCol = 4
        newRow.issue = row.cell[xmlIndexCol].text()

        // графа 5
        xmlIndexCol = 5
        newRow.purchaseDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        newRow.implementationDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 7
        newRow.bondsCount = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        newRow.purchaseCost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        newRow.costs = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 10
        newRow.marketPriceOnDateAcquisitionInPerc = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 11
        newRow.marketPriceOnDateAcquisitionInRub = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 13
        newRow.redemptionVal = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 14
        xmlIndexCol = 14
        newRow.exercisePrice = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 15
        xmlIndexCol = 15
        newRow.exerciseRuble = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 16
        xmlIndexCol = 16
        newRow.marketPricePercent = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 17
        xmlIndexCol = 17
        newRow.marketPriceRuble = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 19
        xmlIndexCol = 19
        newRow.costsRetirement = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 21
        xmlIndexCol = 21
        newRow.parPaper = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 22
        xmlIndexCol = 22
        newRow.averageWeightedPricePaper = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 23
        xmlIndexCol = 23
        newRow.issueDays = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

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

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 27, 1)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1
    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        rnuIndexRow++

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
        def columns = (isMonthBalance() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 2 - атрибут 611 - CODE - "Код сделки", справочник 61 "Коды сделок"
        def xmlIndexCol = 2
        newRow.code = getRecordIdImport(61, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 3 - атрибут 621 - CODE - "Код признака", справочник 62 "Признаки ценных бумаг"
        xmlIndexCol = 3
        newRow.valuablePaper = getRecordIdImport(62, 'CODE', row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 4 - атрибут 814 - SHORTNAME - «Выпуск», из справочника 84 «Ценные бумаги» текстовое значение
        xmlIndexCol = 4
        newRow.issue = row.cell[xmlIndexCol].text()

        // графа 5
        xmlIndexCol = 5
        newRow.purchaseDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        newRow.implementationDate = getDate(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 7
        newRow.bondsCount = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        newRow.purchaseCost = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        newRow.costs = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 10
        newRow.marketPriceOnDateAcquisitionInPerc = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 11
        newRow.marketPriceOnDateAcquisitionInRub = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 12
        newRow.taxPrice = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 13
        newRow.redemptionVal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 14
        xmlIndexCol = 14
        newRow.exercisePrice = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 15
        xmlIndexCol = 15
        newRow.exerciseRuble = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 16
        xmlIndexCol = 16
        newRow.marketPricePercent = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 17
        xmlIndexCol = 17
        newRow.marketPriceRuble = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 18
        xmlIndexCol = 18
        newRow.exercisePriceRetirement = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 19
        xmlIndexCol = 19
        newRow.costsRetirement = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 20
        xmlIndexCol = 20
        newRow.allCost = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 21
        xmlIndexCol = 21
        newRow.parPaper = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 22
        xmlIndexCol = 22
        newRow.averageWeightedPricePaper = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 23
        xmlIndexCol = 23
        newRow.issueDays = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 24
        xmlIndexCol = 24
        newRow.tenureSkvitovannymiBonds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 25
        xmlIndexCol = 25
        newRow.interestEarned = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 26
        xmlIndexCol = 26
        newRow.profitLoss = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 27
        xmlIndexCol = 27
        newRow.excessOfTheSellingPrice = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }

    def totalRow = getTotalMonthRow(rows)
    rows.add(totalRow)

    if (xml.rowTotal.size() == 1) {
        rnuIndexRow += 2

        def row = xml.rowTotal[0]

        def total = formData.createDataRow()

        // графа 7
        xmlIndexCol = 7
        total.bondsCount = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        total.purchaseCost = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        total.costs = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 13
        total.redemptionVal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 15
        xmlIndexCol = 15
        total.exerciseRuble = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 17
        xmlIndexCol = 17
        total.marketPriceRuble = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 18
        xmlIndexCol = 18
        total.exercisePriceRetirement = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 19
        xmlIndexCol = 19
        total.costsRetirement = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 20
        xmlIndexCol = 20
        total.allCost = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 25
        xmlIndexCol = 25
        total.interestEarned = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 26
        xmlIndexCol = 26
        total.profitLoss = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 27
        xmlIndexCol = 27
        total.excessOfTheSellingPrice = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        def colIndexMap = ['bondsCount' : 7, 'purchaseCost' : 8, 'costs' : 9, 'redemptionVal' : 13,
                        'exerciseRuble' : 15, 'marketPriceRuble' : 17, 'exercisePriceRetirement' : 18,
                        'costsRetirement' : 19, 'allCost' : 20, 'interestEarned' : 25, 'profitLoss' : 26,
                        'excessOfTheSellingPrice' : 27]
        for (def alias : totalSumColumns) {
            def v1 = total[alias]
            def v2 = totalRow[alias]
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.error(TRANSPORT_FILE_SUM_ERROR, colIndexMap[alias] + colOffset, rnuIndexRow)
                break
            }
        }
    }

    dataRowHelper.save(rows)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

void loggerError(def row, def msg) {
    if (isMonthBalance()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, [getDataRow(dataRows, 'month')], getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}