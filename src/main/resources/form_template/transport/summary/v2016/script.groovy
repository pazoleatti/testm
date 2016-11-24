package form_template.transport.summary.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import groovy.transform.Field

/**
 * Расчёт суммы налога по каждому транспортному средству.
 *
 * formTypeId = 200
 * formTemplateId = 1200
 *
 * TODO:
 *      - добавить тесты
 */

// графа    - fix
// графа 1  - rowNumber
// графа 2  - kno               - атрибут 2102 - TAX_ORGAN_CODE - «Код налогового органа (кон.)», справочник 210 «Параметры представления деклараций по транспортному налогу»
// графа 3  - kpp               - зависит от графы 2 - атрибут 2103 - KPP - «КПП», справочник 210 «Параметры представления деклараций по транспортному налогу»
// графа 4  - okato             - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 5  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 6  - tsType            - зависит от графы 5 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 7  - model
// графа 8  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
// графа 9  - vi
// графа 10 - regNumber
// графа 11 - regDate
// графа 12 - regDateEnd
// графа 13 - taxBase
// графа 14 - taxBaseOkeiUnit   - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
// графа 15 - createYear
// графа 16 - years
// графа 17 - ownMonths
// графа 18 - partRight
// графа 19 - coefKv
// графа 20 - taxRate           - атрибут 416 - VALUE - «Ставка (руб.)», справочник 41 «Ставки транспортного налога»
// графа 21 - coefKp            - атрибут 2093 - COEF - «Повышающий коэффициент», справочник 209 «Повышающие коэффициенты транспортного налога»
// графа 22 - calculatedTaxSum
// графа 23 - benefitMonths
// графа 24 - coefKl
// графа 25 - taxBenefitCode    - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7.6 «Параметры налоговых льгот транспортного налога».«Коды налоговых льгот и вычетов транспортного налога»
// графа 26 - taxBenefitBase    - зависит от графы 25 - атрибут 702 - BASE - «Основание», справочник 7 «Параметры налоговых льгот транспортного налога»
// графа 27 - taxBenefitSum
// графа 28 - deductionCode     - атрибут 15 - CODE - «Код», справочник 6 «Коды налоговых льгот и вычетов транспортного налога»
// графа 29 - deductionSum
// графа 30 - taxSumToPay
// графа 31 - q1
// графа 32 - q2
// графа 33 - q3
// графа 34 - q4

// TODO (Ramil Timerbaev) отличия от предыдущей версии
// удалены графы:
//      - графа 17 - stealDateStart
//      - графа 18 - stealDateEnd
//      - графа 19 - periodStartCost
//      - графа 20 - periodEndCost
//      - графа 27 - benefitStartDate
//      - графа 28 - benefitEndDate
//      - графа 31 - benefitSum
//      - графа 32 - taxBenefitCodeDecrease 7 19 TAX_BENEFIT_ID 6 16 NAME
//      - графа 33 - benefitSumDecrease
//      - графа 34 - benefitCodeReduction 7 19 TAX_BENEFIT_ID 6 16 NAME
//      - графа 35 - benefitSumReduction
// переименованы графа:
//      - taxAuthority  -> kno
//      - coef362       -> coefKv
//      - coefKl        -> coefKp
//      - koefKp        -> coefKl
//      - benefitBase   -> taxBenefitBase

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        preConsolidationCheck()
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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

//Все аттрибуты
@Field
def allColumns = ['fix', 'rowNumber', 'kno', 'kpp', 'okato', 'tsTypeCode', 'tsType', 'model', 'ecoClass',
        'vi', 'regNumber', 'regDate', 'regDateEnd', 'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths',
        'partRight', 'coefKv', 'taxRate', 'coefKp', 'calculatedTaxSum', 'benefitMonths', 'coefKl', 'taxBenefitCode',
        'taxBenefitBase', 'taxBenefitSum', 'deductionCode', 'deductionSum', 'taxSumToPay', 'q1', 'q2', 'q3', 'q4']

// графа 30..34
@Field
def totalColumns = null

// графа 2
@Field
def editableColumns = ['kno']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// графа 1..7, 9..11, 13..20, 22, 30, 31 (графа 32..34 обязательны для некоторых периодов) - 30..34 убраны, добавляются в логической проверке 2
@Field
def nonEmptyColumns = ['kno', 'okato', 'tsTypeCode', 'model', 'vi', 'regNumber', 'regDate',
        'taxBase', 'taxBaseOkeiUnit', 'createYear', 'years', 'ownMonths', 'partRight', 'coefKv',
        'taxRate', 'calculatedTaxSum' /*, 'taxSumToPay', 'q1', 'q2', 'q3', 'q4'*/ ]

// графа 2, 3, 4
@Field
def groupColumns = ['kno', 'kpp', 'okato']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = false) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def getProvider(def id) {
    return formDataService.getRefBookProvider(refBookFactory, id, providerCache)
}

@Field
def currentReportPeriod = null

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить фиксированные строки
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    sortRows(dataRows, groupColumns)

    def showMsg = (formDataEvent == FormDataEvent.CALCULATE)

    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowV = getEqualsRowFromVehicles(row, showMsg)
        def rowsB = getEqualsRowsFromBenefit(row, showMsg)
        def region = (row.okato ? getRegion(row.okato) : null)

        // графа 2
        row.kno = calc2(row, region, showMsg)
        // графа 19
        row.coefKv = calc19(row, rowV)
        // графа 20
        row.taxRate = calc20(row, region, showMsg)
        // графа 22
        row.calculatedTaxSum = calc22(row)
        // графа 24
        row.coefKl = calc24(row, rowV, rowsB)
        // графа 27
        row.taxBenefitSum = calc27(row, rowV, rowsB)
        // графа 30
        row.taxSumToPay = calc30(row)
        // графа 31
        row.q1 = calc31(row, rowV, rowsB)
        // графа 32
        row.q2 = calc32(row, rowV, rowsB)
        // графа 33
        row.q3 = calc33(row, rowV, rowsB)
        // графа 34
        row.q4 = calc34(row)
    }

    // заполнить справочными значениями, т.к. после пересчета значения могли измениться
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    // добавить подитоги
    addAllStatic(dataRows)

    // добавить строку "итого"
    dataRows.add(calcTotalRow(dataRows, showMsg))
    updateIndexes(dataRows)

    // сравение данных с предыдущей формой
    comparePrevRows()
}

/**
 * Получить для строки нф соответствующую строку из источников "Сведения о ТС"
 *
 * @param row строка нф
 * @param showMsg признак показывать ли сообщение
 */
def getEqualsRowFromVehicles(def row, showMsg = false) {
    if (getReportPeriod()?.order == 1) {
        return
    }
    def sourcerRowsMap = getSourceRowsMap()

    // графа 9, 10, 11
    def columns = ['vi', 'regNumber', 'regDate']
    def key = getKey(row, columns)
    def rows = sourcerRowsMap[key]
    if (rows?.size() == 1) {
        return rows[0]
    } else if (showMsg) {
        // Логическая проверка 19. Проверка наличия информации в форме-источнике вида «Сведения о ТС»
        def formName = getFormTypeById(formTypeVehicleId)?.name
        def columnName9 = getColumnName(row, 'vi')
        def value9 = row.vi
        def columnName10 = getColumnName(row, 'regNumber')
        def value10 = row.regNumber
        def columnName11 = getColumnName(row, 'regDate')
        def value11 = row.regDate?.format('dd.MM.yyyy')
        logger.warn("Строка %s: Не удалось уточнить суммы авансовых платежей, " +
                "т.к. в форме-источнике вида «%s» отсутствует ТС с «%s» = %s, «%s» = %s, «%s» = %s",
                row.getIndex(), formName, columnName9, value9, columnName10, value10, columnName11, value11)
    }
    return null
}

/**
 * Получить для строки нф соответствующие строки из источников "Льготы ТС".
 *
 * @param row строка нф
 * @param showMsg признак показывать ли сообщение
 */
def getEqualsRowsFromBenefit(def row, def showMsg = false) {
    if (row.taxBenefitCode == null || getReportPeriod()?.order == 1) {
        return null
    }
    def sourcerRowsMap = getSourceRowsMap()

    // графа 4, 5, 9, 10
    def columns = ['okato', 'tsTypeCode', 'vi', 'regNumber']
    def key = getKey(row, columns)
    def rows = sourcerRowsMap[key]
    def findRows = []
    for (def rowB : rows) {
        // проверка пересечения
        if (isCross(row.regDate, row.regDateEnd, rowB.benefitStartDate, rowB.benefitEndDate, true)) {
            findRows.add(rowB)
        }
    }
    if (!findRows && showMsg) {
        // Логическая проверка 20. Проверка наличия информации в форме-источнике вида «Сведения о льготируемых ТС»
        def formName = getFormTypeById(formTypeBenefitId)?.name
        def columnName4 = getColumnName(row, 'okato')
        def value4 = getRefBookValue(96L, row.okato)?.CODE?.value
        def columnName5 = getColumnName(row, 'tsTypeCode')
        def value5 = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
        def columnName9 = getColumnName(row, 'vi')
        def value9 = row.vi
        def columnName10 = getColumnName(row, 'regNumber')
        def value10 = row.regNumber
        logger.warn("Строка %s: Не удалось уточнить суммы авансовых платежей, " +
                "т.к. в форме-источнике вида «%s» отсутствует ТС с «%s» = %s, «%s» = %s, «%s» = %s",
                row.getIndex(), formName, columnName4, value4, columnName5, value5,
                columnName9, value9, columnName10, value10)
    }
    return findRows
}

/**
 * Расчет графы 2.
 *
 * @param row строка нф
 * @param region регион - запись справочника 4 "Коды субъектов Российской Федерации"
 * @param showMsg признак показывать ли сообщения
 */
def calc2(def row, def region, def showMsg = false) {
    if (row.kno) {
        return row.kno
    }
    if (formDataDepartment.regionId == null || row.okato == null || region == null) {
        return
    }
    def regionId = region?.record_id?.value
    def allRecords = getAllRecords(210L).values()
    def records210 = allRecords.findAll { record ->
        record.DECLARATION_REGION_ID.value == formDataDepartment.regionId &&
                record.REGION_ID.value == regionId &&
                record.OKTMO.value == row.okato
    }
    if (records210?.size() == 1) {
        return records210[0]?.record_id?.value
    } else if (!records210 && showMsg) {
        // 3. Проверка наличия параметров представления декларации для кода ОКТМО
        logicCheck3(row, region, false)
    } else if (records210?.size() > 1) {
        // сгруппировать предыдущие строки
        def prevRowsMap = getPrevRowsMap()
        def key = getKey(row, uniqueColumns)
        def findPrevRows = prevRowsMap[key]
        if (findPrevRows?.size() == 1) {
            return findPrevRows[0].kno
        } else if (findPrevRows?.size() > 1) {
            def findPrevRow = findPrevRows.max { it.regDate }
            return findPrevRow?.kno
        } else if (!findPrevRows && showMsg) {
            // 3. Проверка наличия параметров представления декларации для кода ОКТМО
            logicCheck3(row, region, true)
        }
    }
    return null
}

/**
 * Получить рассчитанное значение в графе 18.
 *
 * @return 1 - если value не задано, null - если value имеет неправильное значение, результат деления - если все нормально
 */
def calc18(def value) {
    if (!value) {
        return BigDecimal.ONE
    }
    def result6 = logicCheck10(value)
    if (!result6) {
        return null
    }
    int precision = 20 // точность при делении
    return result6[0].divide(result6[1], precision, BigDecimal.ROUND_HALF_UP)
}

def calc19(def row, def rowV, def periodOrder = null) {
    if (periodOrder == null || getReportPeriod()?.order == periodOrder) {
        return row.ownMonths
    }
    def tmp1 = subCalc19(row.regDate, row.regDateEnd, periodOrder)
    if (tmp1 == null) {
        return null
    }
    def tmp2 = subCalc19(rowV?.stealDateStart, rowV?.stealDateEnd, periodOrder, true)
    if (tmp2 == null) {
        tmp2 = BigDecimal.ZERO
    }
    BigDecimal tmp = (tmp1 - tmp2) / 12
    return round(tmp, 4)
}

/**
 * Для расчета графы 19.
 *
 * @param dateStart даты начала
 * @param dateEnd даты окончания
 * @param periodOrder номер периода в году
 * @param isSteal true - расчет месяцев угона, false - расчет месяцев владения
 */
def subCalc19(def dateStart, def dateEnd, def periodOrder, def isSteal = false) {
    // общая часть
    if (dateStart == null) {
        return null
    }
    def dates = getPeriodDates(periodOrder)
    def periodStart = dates?.get(0)
    def periodEnd = dates?.get(1)

    if (dateEnd && dateEnd < periodStart || dateStart > periodEnd) {
        return round(BigDecimal.ZERO, 0)
    }
    def end = (dateEnd == null || dateEnd > periodEnd ? periodEnd : dateEnd)
    def start = (dateStart < periodStart ? periodStart : dateStart)

    // специфика
    BigDecimal tmp
    if (isSteal) {
        tmp = end.format('M').toInteger() - start.format('M').toInteger() - 1
        if (tmp < 0) {
            tmp = BigDecimal.ZERO
        }
    } else {
        def m1 = start.format('M').toInteger() + (start.format('d').toInteger() > 15 ? 1 : 0)
        def m2 = end.format('M').toInteger() - (end.format('d').toInteger() > 15 ? 0 : 1)
        tmp = m2 - m1 + 1
    }
    return round(tmp, 0)
}

// мапа с датами граничных значении периодов
@Field
def datesMap = [:]

/** Получить даты начала и окончания периода по номеру периода в году. */
def getPeriodDates(def periodOrder = null) {
    def period = (periodOrder ?: getReportPeriod()?.order)
    if (datesMap.isEmpty()) {
        def year = getReportPeriod()?.taxPeriod?.year
        def format = 'dd.MM.yyyy'
        datesMap[1] = [ Date.parse(format, '01.01.' + year), Date.parse(format, '31.03.' + year) ]
        datesMap[2] = [ Date.parse(format, '01.04.' + year), Date.parse(format, '30.06.' + year) ]
        datesMap[3] = [ Date.parse(format, '01.07.' + year), Date.parse(format, '30.09.' + year) ]
        datesMap[4] = [ Date.parse(format, '01.01.' + year), Date.parse(format, '31.12.' + year) ]
    }
    return datesMap[period]
}

def calc20(def row, def region, def showMsg = false) {
    def record41 = getRecord41(row, region, showMsg)
    return record41?.record_id?.value
}

def calc22(def row, def periodOrder = null) {
    if (row.taxBase == null || row.partRight == null || row.taxRate == null) {
        return null
    }
    def period = (periodOrder ?: getReportPeriod()?.order)
    def value19 = (getReportPeriod()?.order != period ? calc19(row, periodOrder) : row.coefKv)
    if (value19 == null) {
        return null
    }

    def value18 = calc18(row.partRight)
    def value20 = getRefBookValue(41L, row.taxRate)?.VALUE?.value
    if (row.taxBase == null || value18 == null || value19 == null || value20 == null) {
        return null
    }
    // графа 22 = ОКРУГЛ (графа 13 * графа 20 * графа 18 * графа 19;0)
    BigDecimal tmp = row.taxBase.multiply(value20).multiply(value18).multiply(value19)
    BigDecimal value21 = getRefBookValue(209L, row.coefKp)?.COEF?.value
    if (value21) {
        tmp = tmp.multiply(value21)
    }
    return round(tmp, 0)
}

def calc24(def row, def rowV, def rowsB, def periodOrder = null) {
    if (row.benefitMonths == null) {
        return null
    }
    def period = (periodOrder ?: getReportPeriod()?.order)
    BigDecimal month
    if (getReportPeriod()?.order == period) {
        month = row.benefitMonths
    } else {
        def dates = getPeriodDates(period)
        def periodStart = dates?.get(0)
        def periodEnd = dates?.get(1)

        month = BigDecimal.ZERO
        for (def rowB : rowsB) {
            def start = [ rowV?.regDate, rowB?.benefitStartDate, periodStart ].max()
            def end = [ rowV?.regDateEnd, rowB?.benefitEndDate, periodEnd ].min()
            BigDecimal tmp = end.format('M').toInteger() - start.format('M').toInteger() + 1
            if (tmp < 0) {
                tmp = BigDecimal.ZERO
            }
            month = month + tmp
        }
    }
    BigDecimal tmp = (month ? month / 12 : null)
    return round(tmp, 4)
}

def calc27(def row, def rowV, def rowsB, def periodOrder = null) {
    def recordId7 = null
    if (row.taxBenefitCode) {
        recordId7 = row.taxBenefitCode
    } else if (rowsB) {
        // взять любую льготу
        recordId7 = rowsB[0].taxBenefitCode
    }
    def record7 = (recordId7 ? getAllRecords(7L)?.get(recordId7) : null)
    if (!record7) {
        return null
    }
    def code = (record7 ? getAllRecords(6L).get(record7?.TAX_BENEFIT_ID?.value)?.CODE?.value : null)

    BigDecimal value18 = calc18(row.partRight)
    BigDecimal value20 = getRefBookValue(41L, row.taxRate)?.VALUE?.value
    BigDecimal value24 = (getReportPeriod()?.order != periodOrder ? calc24(row, rowV, rowsB, periodOrder) : row.coefKl)

    if (row.taxBase == null || value18 == null || value20 == null || value24 == null) {
        return null
    }
    BigDecimal value21 = getRefBookValue(209L, row.coefKp)?.COEF?.value

    BigDecimal tmp = null
    if (code == '20210' || code == '30200') {
        // С = ОКРУГЛ(графа 13 * графа 20 * графа 18 * графа 21 * графа 24; 0)
        tmp = row.taxBase.multiply(value20).multiply(value18).multiply(value24)
    } else if (code == '20220') {
        BigDecimal a = record7?.PERCENT?.value
        if (a == null) {
            return null
        }
        int precision = 10 // точность при делении
        // С = ОКРУГЛ(графа 13 * графа 20 * графа 18 * графа 21 * графа 24 * А / 100; 0)
        tmp = row.taxBase.multiply(value20).multiply(value18).multiply(value24).multiply(a).divide(100, precision, BigDecimal.ROUND_HALF_UP)
    } else if (code == '20230') {
        BigDecimal b = record7?.RATE?.value
        int precision = 10 // точность при делении
        // С = ОКРУГЛ(графа 13 * (графа 20 – В) * графа 18 * графа 21 * графа 24 / 100; 0)
        tmp = row.taxBase.multiply(value20 - b).multiply(value18).multiply(value24).divide(100, precision, BigDecimal.ROUND_HALF_UP)
    }
    if (tmp && value21) {
        tmp = tmp.multiply(value21)
    }
    return round(tmp, 0)
}

def calc30(def row) {
    if (row.calculatedTaxSum == null) {
        return null
    }
    BigDecimal tmp = row.calculatedTaxSum - (row.taxBenefitSum ?: BigDecimal.ZERO) - (row.deductionSum ?: BigDecimal.ZERO)
    if (tmp < 0) {
        tmp = BigDecimal.ZERO
    }
    return round(tmp, 0)
}

def calc31(def row, def rowV, def rowsB) {
    return calc31_33(row, rowV, rowsB, 1)
}

def calc32(def row, def rowV, def rowsB) {
    return calc31_33(row, rowV, rowsB, 2)
}

def calc33(def row, def rowV, def rowsB) {
    return calc31_33(row, rowV, rowsB, 3)
}

def calc31_33(def row, def rowV, def rowsB, def periodOrder) {
    if (getReportPeriod()?.order < periodOrder) {
        return null
    }

    BigDecimal tmp = BigDecimal.ZERO
    if (row.deductionCode != null) {
        return round(tmp, 0)
    }
    def record310 = getRecord310(row)
    def obUpl = (record310?.PREPAYMENT?.value ?: 1)
    if (!obUpl) {
        return round(tmp, 0)
    }
    def value22 = row.calculatedTaxSum
    def value27 = row.taxBenefitSum
    if (getReportPeriod()?.order != periodOrder) {
        value22 = calc22(row, periodOrder)
        value27 = calc27(row, rowV, rowsB, periodOrder)
    }

    tmp = (value22 ?: BigDecimal.ZERO) - (value27 ?: BigDecimal.ZERO)
    return round(tmp, 0)
}

@Field
def record310Map = [:]

def getRecord310(def row) {
    if (row?.kno == null) {
        return null
    }
    if (record310Map[row.kno] != null) {
        return record310Map[row.kno]
    }
    def record210 = getRefBookValue(210L, row.kno)
    def kno = record210?.TAX_ORGAN_CODE?.value
    def kpp = record210?.KPP?.value
    def records310 = getAllRecords2(310L)
    record310Map[row.kno] = records310?.find {
        it?.DEPARTMENT_ID?.value == formDataDepartment.id &&
                it?.TAX_ORGAN_CODE?.value == kno &&
                it?.KPP?.value == kpp
    }
    return record310Map[row.kno]
}

def calc34(def row) {
    if (getReportPeriod()?.order < 4) {
        return null
    }
    BigDecimal tmp = (row.taxSumToPay ?: BigDecimal.ZERO) - (row.q1 ?: BigDecimal.ZERO) -
            (row.q2 ?: BigDecimal.ZERO) - (row.q3 ?: BigDecimal.ZERO)
    return round(tmp, 0)
}

// графа 4, 5, 9, 10
@Field
def uniqueColumns = ['okato', 'tsTypeCode', 'vi', 'regNumber']

@Field
def prevRowsMap = null

/** Cгруппировать предыдущие строки. */
def getPrevRowsMap() {
    if (prevRowsMap == null) {
        prevRowsMap = [:]
        def prevRows = getPrevDataRows()
        fillRowsMap(prevRows, uniqueColumns, prevRowsMap)
    }
    return prevRowsMap
}

/**
 * Сгруппировать строки по графам.
 *
 * @param rows строки
 * @param columns графы
 * @param rowsMap мапа для группировки
 */
void fillRowsMap(def rows, def columns, def rowsMap) {
    if (!rows || !columns || rowsMap == null) {
        return
    }
    for (def row : rows) {
        if (row.getAlias()) {
            continue
        }
        def key = getKey(row, columns)
        if (rowsMap[key] == null) {
            rowsMap[key] = []
        }
        rowsMap[key].add(row)
    }
}

@Field
def sourceRowsMap = null

/** Получить сгруппированные строки исчтоников для расчетов. */
def getSourceRowsMap() {
    if (sourceRowsMap != null) {
        return sourceRowsMap
    }

    // источники не надо доставать для 1 квартала
    if (getReportPeriod()?.order == 1) {
        sourcerRowsMap = [:]
        return sourcerRowsMap
    }

    // сведения о ТС
    def dataRowsVehicles = getAllRowsSources(formTypeVehicleId)
    if (!dataRowsVehicles) {
        logicCheck21or22(formTypeVehicleId)
    }

    // льготы ТС
    def dataRowsBenefit = getAllRowsSources(formTypeBenefitId)
    if (!dataRowsBenefit) {
        logicCheck21or22(formTypeBenefitId)
    }

    // графы соответствия 8, 9, 10
    def keyColumnsVehilces = ['identNumber', 'regNumber', 'regDate']
    // графы соответствия 2, 3, 5, 6
    def keyColumnsBenefit = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber']

    // кэш совпадении
    sourceRowsMap = [:]
    fillRowsMap(dataRowsVehicles, keyColumnsVehilces, sourceRowsMap)
    fillRowsMap(dataRowsBenefit, keyColumnsBenefit, sourceRowsMap)
    return sourceRowsMap
}

/**
 * Логическая проверка 3. Проверка наличия параметров представления декларации для кода ОКТМО.
 *
 * @param row строка нф
 * @param region регион - запись справочника 4 "Коды субъектов Российской Федерации"
 * @param isMany true - найдено много записей, false - не найдено записей
 */
void logicCheck3(def row, def region, def isMany) {
    LogLevel level = (isMany ? LogLevel.WARNING : LogLevel.ERROR)
    def columnName4 = getColumnName(row, 'okato')
    def value4 = getRefBookValue(96L, row.okato)?.CODE?.value
    def subMsg = (isMany ? "более одной записи, актуальной" : "отсутствует запись, актуальная")
    def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
    def declarationRegionCode = getRefBookValue(4L, formDataDepartment.regionId)?.CODE?.value
    def regionCode = region?.CODE?.value ?: ''
    logger.log(level, "Строка %s: Графа «%s» = «%s»: В справочнике " +
            "«Параметры представления деклараций по транспортному налогу» %s на дату %s, в которой " +
            "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
            "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», поле «Код ОКТМО» = «%s»",
            row.getIndex(), columnName4, value4, subMsg, dateInStr,
            declarationRegionCode, formDataDepartment.name, regionCode, value4)
}

// Логическая проверка 21. Проверка наличия формы-источника вида «Сведения о ТС» в состоянии «Принята»
// Логическая проверка 22. Проверка наличия формы-источника вида «Сведения о льготируемых ТС» в состоянии «Принята»
void logicCheck21or22(def sourceFormTypeId) {
    def formName = getFormTypeById(sourceFormTypeId)?.name
    def periodName = getReportPeriod()?.name + ' ' + getReportPeriod()?.taxPeriod?.year
    logger.warn("Не удалось уточнить суммы авансовых платежей, в Системе отсутствует форма " +
            "вида «%s» в состоянии «Принята» за период: «%s» для подразделения «%s»",
            formName, periodName, formDataDepartment.name)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def isCalc = (formDataEvent == FormDataEvent.CALCULATE)

    // для логической проверки 2
    def nonEmptyColumnsTmp = nonEmptyColumns + getTotalColumns()

    // для логической проверки 4
    def needCheck4or5 = true

    // для логической проверки 9
    def needValue = [:]
    // графа 2, 19, 20, 22, 24, 27, 30..34
    def arithmeticCheckAlias = [ /* 'kno', */ 'coefKv', 'taxRate', 'calculatedTaxSum', 'coefKl', 'taxBenefitSum', 'taxSumToPay', 'q1', 'q2', 'q3', 'q4']
    def showMsg = false

    // для логической проверки 15
    def rowsMap15 = [:]

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «%s»", formDataDepartment.name)
        return
    }

    def onlySimpleRows = dataRows?.findAll { !it.getAlias() }

    for (def row : onlySimpleRows) {
       def index = row.getIndex()

        // 2. Проверка заполнения обязательных граф
        checkNonEmptyColumns(row, index, nonEmptyColumnsTmp, logger, true)

        // 3. Проверка наличия параметров представления декларации для кода ОКТМО
        // Выполняется при расчете, в методе calc2()

        // 6. Проверка корректности заполнения даты регистрации ТС
        if (row.regDate && row.regDate > getReportPeriodEndDate()) {
            def columnName11 = getColumnName(row, 'regDate')
            def dateInStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", index, columnName11, dateInStr)

            needCheck4or5 = false
        }

        // 7. Проверка корректности заполнения даты снятия с регистрации ТС
        if (row.regDateEnd && (row.regDateEnd < startYearDate || row.regDate && row.regDateEnd < row.regDate)) {
            def columnName12 = getColumnName(row, 'regDateEnd')
            def dateInStr = startYearDate?.format('dd.MM.yyyy')
            def columnName11 = getColumnName(row, 'regDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                    index, columnName12, dateInStr, columnName11)

            needCheck4or5 = false
        }

        // 8. Проверка корректности заполнения года выпуска ТС
        if (row.createYear && row.createYear?.format('yyyy')?.toInteger() > year) {
            def columnName15 = getColumnName(row, 'createYear')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»", index, columnName15, year)
        }

        // 9. Проверка корректности заполнения расчетных граф 2, 19, 20, 22, 24, 27, 30-34
        if (!isCalc) {
            def rowV = getEqualsRowFromVehicles(row, showMsg)
            def rowsB = getEqualsRowsFromBenefit(row, showMsg)
            def region = (row.okato ? getRegion(row.okato) : null)

            // TODO (Ramil Timerbaev) графа 2 редактируемая и расчитываемая, в 1.2.5.1 сказали убрать из проверки, в 1.3 добавится новая проверка для графы 2
            // needValue.kno = calc2(row, region, showMsg)
            needValue.coefKv = calc19(row, rowV)
            needValue.taxRate = calc20(row, region, showMsg)
            needValue.calculatedTaxSum = calc22(row)
            needValue.coefKl = calc24(row, rowV, rowsB)
            needValue.taxBenefitSum = calc27(row, rowV, rowsB)
            needValue.taxSumToPay = calc30(row)
            needValue.q1 = calc31(row, rowV, rowsB)
            needValue.q2 = calc32(row, rowV, rowsB)
            needValue.q3 = calc33(row, rowV, rowsB)
            needValue.q4 = calc34(row)
            def errorColumns = []
            for (def alias : arithmeticCheckAlias) {
                if (needValue[alias] == null && row[alias] == null) {
                    continue
                }
                if (needValue[alias] == null || row[alias] == null || needValue[alias].compareTo(row[alias]) != 0) {
                    errorColumns.add(getColumnName(row, alias))
                }
            }
            if (!errorColumns.isEmpty()) {
                def columnNames = errorColumns.join('», «')
                logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", index, columnNames)
            }
        }

        // 10. Проверка корректности заполнения доли налогоплательщика в праве на ТС
        def tmp = logicCheck10(row.partRight)
        if (tmp != null && tmp.isEmpty()) {
            def columnName18 = getColumnName(row, 'partRight')
            logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                    "«(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», " +
                    "числитель должен быть меньше либо равен знаменателю, " +
                    "числитель и знаменатель не должны быть равны нулю",
                    index, columnName18)
        }

        // 11. Проверка наличия ставки ТС в справочнике
        // Выполняется при расчете, в методе calc20()

        // 13. Проверка одновременного заполнения данных о налоговой льготе
        if (!isCalc) {
            // графа 23..27
            def columns = ['benefitMonths', 'coefKl', 'taxBenefitCode', /* 'taxBenefitBase', */ 'taxBenefitSum']
            def hasValue = columns.find { row[it] }
            def hasNull = columns.find { !row[it] }
            if (hasValue && hasNull) {
                logger.error("Строка %s: Данные о налоговой льготе указаны не полностью", index)
            }
        }

        // 14. Проверка одновременного заполнения данных о налоговом вычете
        // графа 28, 29
        def columnsForCheck14 = ['deductionCode', 'deductionSum']
        def hasValue = columnsForCheck14.find { row[it] }
        def hasNull = columnsForCheck14.find { !row[it] }
        if (hasValue && hasNull) {
            logger.error("Строка %s: Данные о налоговом вычете указаны не полностью", index)
        }

        def record210 = getRefBookValue(210L, row.kno)

        // 15. Проверка заполнения формы настроек подразделений
        // сбор данных
        if (record210) {
            def key = record210?.TAX_ORGAN_CODE?.value + '#' + record210?.KPP?.value
            if (rowsMap15[key] == null) {
                rowsMap15[key] = []
            }
            rowsMap15[key].add(row)
        }

        // 17. Проверка разрядности значений граф 30-34, рассчитываемых в итоговых строках
        // Выполняется при расчете, в методе checkOverflowLocal()

        // 18. Проверка корректности заполнения кода налогового органа
        if (!isCalc && row.okato && row.kno) {
            if (record210?.OKTMO?.value != row.okato || record210?.REGION_ID?.value != getRegion(row.okato)?.record_id?.value) {
                def columnName4 = getColumnName(row, 'okato')
                def value4 = getRefBookValue(96L, row.okato)?.CODE?.value
                logger.error("Строка %s: значение графы «%s» (%s) должно быть равно значению поля «Код ОКТМО» и " +
                        "соответствовать значению поля «Код региона РФ» записи справочника, выбранной в графе 2",
                        index, columnName4, value4)
            }
        }

        // 19. Проверка наличия информации в форме-источнике вида «Сведения о ТС»
        // Выполняется при расчете, в методе getEqualsRowFromVehicles()

        // 20. Проверка наличия информации в форме-источнике вида «Сведения о льготируемых ТС»
        // Выполняется при расчете, в методе getEqualsRowsFromBenefit()
    }

    if (needCheck4or5) {
        // 4. Проверка на наличие в форме строк с одинаковым значением граф 9, 10 и пересекающимися периодами владения
        // сбор данных
        def map4 = [ vi : [:], regNumber : [:] ]
        for (def row : onlySimpleRows) {
            def aliases = map4.keySet().toList()
            for (def alias : aliases) {
                def key = row[alias]
                if (row.regDate == null || !key) {
                    continue
                }
                if (map4[alias][key] == null) {
                    map4[alias][key] = []
                }
                map4[alias][key].add(row)
            }
        }
        // проверка совпадении
        for (def alias : map4.keySet()) {
            def rowsMap = map4[alias]
            for (def key : rowsMap.keySet()) {
                def rows = rowsMap[key]
                if (rows.size() < 2) {
                    continue
                }
                def hasCross = checkHasCross(rows)
                if (hasCross) {
                    def indexes = rows?.collect { it.getIndex() }
                    def indexesInStr = indexes?.join(', ')
                    def row = rows[0]
                    def columnName = getColumnName(row, alias)
                    def value = row[alias]
                    logger.error("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                            indexesInStr, columnName, value)
                }
            }
        }

        // 5. Проверка на наличие в форме строк с одинаковым значением граф 4, 5, 9, 10, 13, 14 и пересекающимися периодами владения
        // сбор данных
        def columns = ['okato', 'tsTypeCode', 'vi', 'regNumber', 'taxBase', 'taxBaseOkeiUnit']
        def map5 = [:]
        fillRowsMap(onlySimpleRows, columns, map5)
        // проверка совпадении
        for (def key : map5.keySet().toList()) {
            def rows = map5[key]
            if (rows.size() < 2) {
                continue
            }
            def hasCross = checkHasCross(rows)
            def indexes = rows?.collect { it.getIndex() }
            def indexesInStr = indexes?.join(', ')
            if (hasCross) {
                // сообщение a
                def row = rows[0]
                def value4 = getRefBookValue(96, row.okato)?.CODE?.value ?: ''
                def value5 = getRefBookValue(42, row.tsTypeCode)?.CODE?.value ?: ''
                def value9 = row.vi ?: ''
                def value10 = row.regNumber ?: ''
                def value13 = row.taxBase ?: ''
                def value14 = getRefBookValue(12, row.taxBaseOkeiUnit)?.CODE?.value ?: ''
                logger.error("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                        "Регистрационный знак «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                        "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, " +
                        "регистрационным знаком ТС, налоговой базой, единицей измерения налоговой базы по ОКЕИ и пересекающимися периодами владения ТС",
                        indexesInStr, value4, value5, value9, value10, value13, value14)
            } else {
                // сообщение b
                logger.warn("Строки %s: На форме присутствуют несколько строк по одному ТС. Проверьте периоды регистрации ТС", indexesInStr)
            }
        }
    }

    // 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
    if (isCalc && !getPrevDataRows()) {
        def tmpRow = (dataRows ? dataRows[0] : formData.createDataRow())
        def columnName2 = getColumnName(tmpRow, 'kno')
        def columnName3 = getColumnName(tmpRow, 'kpp')
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def tmpYear = prevReportPeriod?.year
        logger.warn("Данные граф «%s», «%s» не были скопированы из формы предыдущего периода. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                columnName2, columnName3, periodName, tmpYear, formDataDepartment.name)
    }

    // 15. Проверка заполнения формы настроек подразделений
    for (def key : rowsMap15.keySet().toList()) {
        def rows = rowsMap15[key]
        def record310 = getRecord310(rows[0])
        if (!record310) {
            def indexes = rows?.collect { it.getIndex() }
            def indexesInStr = indexes?.join(', ')
            def record210 = getRefBookValue(210L, rows[0].kno)
            def kno = record210?.TAX_ORGAN_CODE?.value
            def kpp = record210?.KPP?.value
            logger.error("Строки %s: На форме настроек подразделений для подразделения «%s» " +
                    "отсутствует запись с «Код налогового органа (кон.) = %s» и «КПП = %s»",
                    indexesInStr, formDataDepartment.name, kno, kpp)
        }
    }

    // 16. Проверка корректности значений итоговых строк (строка "итого")
    if (!isCalc) {
        def lastSimpleRow = null
        def subTotalMap = [:]
        for (def row : dataRows) {
            if (!row.getAlias()) {
                lastSimpleRow = row

                // подитог кно/кпп/октмо - проверка отсутствия подитога
                def key2 = row.getCell('kno').refBookDereference + '#' +
                        row.getCell('kpp').refBookDereference + '#' +
                        row.getCell('okato').refBookDereference

                if (subTotalMap[key2] == null) {
                    def findSubTotal = dataRows.find {
                        it.getAlias()?.startsWith('total2') &&
                                it.getCell('kno').refBookDereference == row.getCell('kno').refBookDereference &&
                                it.getCell('kpp').refBookDereference == row.getCell('kpp').refBookDereference &&
                                it.getCell('okato').refBookDereference == row.getCell('okato').refBookDereference
                    }
                    subTotalMap[key2] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.getCell('kno').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.getCell('kpp').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'okato') + '=' + (row.getCell('okato').refBookDereference ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }

                // подитог кно/кпп - проверка отсутствия подитога
                def key1 = row.getCell('kno').refBookDereference + '#' + row.getCell('kpp').refBookDereference
                if (subTotalMap[key1] == null) {
                    def findSubTotal = dataRows.find {
                        it.getAlias()?.startsWith('total1') &&
                                it.getCell('kno').refBookDereference == row.getCell('kno').refBookDereference &&
                                it.getCell('kpp').refBookDereference == row.getCell('kpp').refBookDereference
                    }
                    subTotalMap[key1] = (findSubTotal != null)
                    if (findSubTotal == null) {
                        def subMsg = getColumnName(row, 'kno') + '=' + (row.getCell('kno').refBookDereference ?: 'не задан') + ', ' +
                                getColumnName(row, 'kpp') + '=' + (row.getCell('kpp').refBookDereference ?: 'не задан')
                        logger.error(GROUP_WRONG_ITOG, subMsg)
                    }
                }
                continue
            }

            if (row.getAlias() != null && row.getAlias().indexOf('total2') != -1) {
                // подитог кно/кпп/октмо
                // принадлежность подитога к последей простой строке
                if (row.getCell('kno').refBookDereference != lastSimpleRow.getCell('kno').refBookDereference ||
                        row.getCell('kpp').refBookDereference != lastSimpleRow.getCell('kpp').refBookDereference ||
                        row.getCell('okato').refBookDereference != lastSimpleRow.getCell('okato').refBookDereference) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow2(dataRows.indexOf(row) - 1, dataRows, row.kno, row.okato, showMsg)
                checkTotalRow(row, srow)
            } else if (row.getAlias() != null && row.getAlias().indexOf('total1') != -1) {
                // подитог кно/кпп
                // принадлежность подитога к последей простой строке
                if (row.getCell('kno').refBookDereference != lastSimpleRow.getCell('kno').refBookDereference ||
                        row.getCell('kpp').refBookDereference != lastSimpleRow.getCell('kpp').refBookDereference) {
                    logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                    continue
                }
                // проверка сумм
                def srow = calcSubTotalRow1(dataRows.indexOf(row) - 1, dataRows, row.kno, showMsg)
                checkTotalRow(row, srow)
            }
            lastSubTotalRow = row
        }

        // строка "итого"
        def totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
        if (totalRow != null) {
            def tmpTotalRow = calcTotalRow(dataRows, showMsg)
            checkTotalRow(totalRow, tmpTotalRow)
        } else {
            logger.error("Итоговые значения рассчитаны неверно!")
        }
    }

    // 21. Проверка наличия формы-источника вида «Сведения о ТС» в состоянии «Принята»
    // 22. Проверка наличия формы-источника вида «Сведения о льготируемых ТС» в состоянии «Принята»
    // Выполняются при расчете, в методе getSourceRowsMap()
}

/**
 * Логическая проверка 10. Проверка корректности заполнения доли налогоплательщика в праве на ТС.
 *
 * @param value значение графы 18
 * @return null - если значение value пусто, пустой список - если ошибка, список из двух элементов (числитель и знаменатель) - все нормально
 */
def logicCheck10(def value) {
    if (!value) {
        return null
    }
    def partArray = value?.split('/')
    if (!(value ==~ /\d{1,10}\/\d{1,10}/) ||
            partArray[0].toString().startsWith('0') ||
            partArray[1].toString().startsWith('0') ||
            partArray[0].toBigDecimal() > partArray[1].toBigDecimal() ||
            partArray[0].toBigDecimal() == 0 ||
            partArray[1].toBigDecimal() == 0) {
        return []
    }
    return [partArray[0].toBigDecimal(), partArray[1].toBigDecimal()]
}


/**
 * Проверить итоги/подитоги. Для логической проверки 18.
 *
 * @param row итоговая строка нф
 * @param tmpRow посчитанная итоговая строка
 */
void checkTotalRow(def row, def tmpRow) {
    def errorColumns = []
    for (def column : getTotalColumns()) {
        if (row[column] != tmpRow[column]) {
            errorColumns.add(getColumnName(row, column))
        }
    }
    if (!errorColumns.isEmpty()) {
        def columnNames = errorColumns.join('», «')
        logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", row.getIndex(), columnNames)
    }
}

/** Получение региона по коду ОКТМО. */
def getRegion(def record96Id) {
    def record96 = getRefBookValue(96, record96Id)
    def okato = getOkato(record96?.CODE?.stringValue)
    if (okato) {
        def allRecords = getAllRecords(4L).values()
        return allRecords.find { record ->
            record.OKTMO_DEFINITION.value == okato
        }
    }
    return null
}

def getOkato(def codeOkato) {
    if (!codeOkato || codeOkato.length() < 3) {
        return codeOkato
    }
    codeOkato = codeOkato?.substring(0, 3)
    if (codeOkato && !(codeOkato in ["719", "718", "118"])) {
        codeOkato = codeOkato?.substring(0, 2)
    }
    return codeOkato
}

@Field
def groupColumnsListListB = [
        // для предконсолидационной проверки 5 b 6 (графа 2, 3, 5, 6, 7, 8)
        ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'powerVal', 'baseUnit']
]

@Field
def groupColumnsListList = [
        // для предконсолидационной проверки 1
        ['version', 'pastYear'],
        // для предконсолидационной проверки 2
        ['codeOKATO'],
        // для предконсолидационной проверки 3
        ['codeOKATO', 'tsTypeCode', 'baseUnit'],
        // для предконсолидационной проверки 4 (графа 2, 4, 8, 12, 13, 14)
        ['codeOKATO', 'tsTypeCode', 'identNumber', 'taxBase', 'baseUnit', 'baseUnit']
]

def preConsolidationCheck() {
    // получить все принятые источники и сгруппировать: по проверке, по источнику, по группе столбцов - группа строк
    def sourcesInfo = getSourcesInfo()
    def complexMap = [:]
    for (relation in sourcesInfo) {
        def groupColumnsList
        if (relation.formType.id == formTypeVehicleId) {
            groupColumnsList = groupColumnsListList
        } else if (relation.formType.id == formTypeBenefitId) {
            groupColumnsList = groupColumnsListListB
        } else {
            continue
        }
        def dataRows = getDataRows(relation.formDataId)
        groupColumnsList.each { groupColumns ->
            def columnsKey = groupColumns.toString()
            if (complexMap[columnsKey] == null) {
                complexMap[columnsKey] = [:]
            }
            if (complexMap[columnsKey][relation] == null) {
                complexMap[columnsKey][relation] = [:]
            }
            def rowsMap = complexMap[columnsKey][relation]
            fillRowsMap(dataRows, groupColumns, rowsMap)
        }
    }

    // 1. Проверка наличия повышающего коэффициента для ТС с заполненной графой 22 «Модель (версия) из перечня, утвержденного на налоговый период» в форме-источнике
    int i = 0
    def groupColumns = groupColumnsListList[i]
    def columnsKey = groupColumns.toString()
    def sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.version != null) { // скопировал из первички
                def avgCostId = getRefBookValue(218L, row.version)?.AVG_COST?.value
                // справочник «Повышающие коэффициенты транспортного налога»
                def allRecords = getAllRecords(209L).values()
                def record = allRecords.find { record ->
                    record.AVG_COST.value == avgCostId &&
                            record.YEAR_FROM.value < row.pastYear &&
                            record.YEAR_TO.value >= row.pastYear
                }
                if (record == null) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    def avgCost = getRefBookValue(211L, avgCostId).NAME.value
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.warn("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s». В справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Средняя стоимость» = «%s» и значение «%s» больше значения поля «Количество лет, прошедших с года выпуска ТС (от)» и меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'pastYear'), row.pastYear, getColumnName(row, 'averageCost'), avgCost,
                            getReportPeriodEndDate().format("dd.MM.yyyy"), avgCost, getColumnName(row, 'pastYear'),
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
                    )
                }
            }
        }
    }

    // 2. Проверка наличия параметров представления декларации для кода ОКТМО, заданного в графе 2 «Код ОКТМО» формы-источника
    i = 1
    groupColumns = groupColumnsListList[i]
    columnsKey = groupColumns.toString()
    sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.codeOKATO != null) {
                def region = getRegion(row.codeOKATO)
                def regionId = region?.record_id?.value
                def declarationRegionId = formDataDepartment.regionId
                // справочник «Параметры представления деклараций по транспортному налогу»
                def allRecords = getAllRecords(210L).values()
                def record = allRecords.find { record ->
                    record.DECLARATION_REGION_ID.value == declarationRegionId &&
                            record.REGION_ID.value == regionId &&
                            record.OKTMO.value == row.codeOKATO
                }
                if (record == null) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                    def regionCode = region?.CODE?.value ?: ''
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s»: В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись, " +
                            "актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) справочника «Подразделения» " +
                            "для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код по ОКТМО» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO,
                            getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            formDataDepartment.name, regionCode, codeOKATO,
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
                    )
                }
            }
        }
    }

    // 3. Проверка наличия ставки для ТС формы-источника
    i = 2
    groupColumns = groupColumnsListList[i]
    columnsKey = groupColumns.toString()
    sourceRowsMap = complexMap[columnsKey]
    sourceRowsMap.each { Relation relation, rowsMap ->
        rowsMap.each { key, dataRows ->
            def row = dataRows[0]
            if (row.codeOKATO != null && row.tsTypeCode != null && row.taxBase != null && row.pastYear != null && row.ecoClass != null) {
                def region = getRegion(row.codeOKATO)
                def regionId = region?.record_id?.value
                def declarationRegionId = formDataDepartment.regionId
                def ecoClassCode = getRefBookValue(40L, row.ecoClass)?.CODE?.value
                // справочник «Ставки транспортного налога»
                def allRecords = getAllRecords(41L).values()
                def record = allRecords.find { record ->
                    record.DECLARATION_REGION_ID.value == declarationRegionId &&
                            record.DICT_REGION_ID.value == regionId &&
                            record.CODE.value == row.tsTypeCode &&
                            ((record.MIN_AGE.value == null) || (record.MIN_AGE.value < row.pastYear)) &&
                            ((record.MAX_AGE.value == null) || (record.MAX_AGE.value >= row.pastYear)) &&
                            ((record.MIN_POWER.value == null) || (record.MIN_POWER.value < row.taxBase)) &&
                            ((record.MAX_POWER.value == null) || (record.MAX_POWER.value >= row.taxBase)) &&
                            ((record.MIN_ECOCLASS.value == null) || (getRefBookValue(40L, record.MIN_ECOCLASS.value)?.CODE?.value < ecoClassCode)) &&
                            ((record.MAX_ECOCLASS.value == null) || (getRefBookValue(40L, record.MAX_ECOCLASS.value)?.CODE?.value >= ecoClassCode))
                }
                if (record == null) {
                    def rowIndexes = dataRows.collect { it.getIndex() }
                    def declarationRegionCode = getRefBookValue(4, declarationRegionId).CODE.value
                    def regionCode = region?.CODE?.value ?: ''
                    def tsTypeCode = getRefBookValue(42L, row.tsTypeCode).CODE.value
                    def baseUnit = getRefBookValue(12L, row.baseUnit).CODE.value
                    def codeOKATO = getRefBookValue(96L, row.codeOKATO).CODE.value
                    def periodName = getReportPeriod().name
                    def periodYear = getReportPeriod().taxPeriod.year
                    logger.error("Строки %s формы-источника. Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: В справочнике «Ставки транспортного налога» " +
                            "отсутствует запись, актуальная на дату %s, в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                            "справочника «Подразделения» для подразделения «%s», поле «Код субъекта РФ» = «%s», поле «Код вида ТС» = «%s», поле «Ед. измерения мощности» = «%s». " +
                            "Форма-источник: Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s %s»",
                            rowIndexes.join(', '), getColumnName(row, 'codeOKATO'), codeOKATO,
                            getColumnName(row, 'tsTypeCode'), tsTypeCode, getColumnName(row, 'baseUnit'), baseUnit,
                            getReportPeriodEndDate().format('dd.MM.yyyy'), declarationRegionCode,
                            relation.getDepartment().name, regionCode, tsTypeCode, baseUnit,
                            relation.formDataKind.title, relation.formType.name, relation.department.name, periodName, periodYear
                    )
                }

            }
        }
    }

    // 4. Проверка корректности данных в формах-источниках вида «Сведения о ТС»
    def relationsV = sourcesInfo?.findAll { it.formType.id == formTypeVehicleId }
    if (relationsV?.size() > 1) {
        i = 3
        groupColumns = groupColumnsListList[i]
        columnsKey = groupColumns.toString()
        sourceRowsMap = complexMap[columnsKey]
        preConsolidationCheck4or5or6(relationsV, sourceRowsMap, 'regDate', 'regDateEnd')
    }

    def relationsB = sourcesInfo?.findAll { it.formType.id == formTypeBenefitId }
    if (relationsB?.size() > 1) {
        groupColumns = groupColumnsListListB[0]
        columnsKey = groupColumns.toString()
        sourceRowsMap = complexMap[columnsKey]

        // 5. Проверка корректности данных в формах-источниках вида «Сведения о льготируемых ТС»
        preConsolidationCheck4or5or6(relationsB, sourceRowsMap, 'benefitStartDate', 'benefitEndDate')

        // 6. Проверка использования для ТС одного вида льготы
        preConsolidationCheck4or5or6(relationsB, sourceRowsMap, null, null, true)
    }

    // 7. Проверка правильности назначения форм-источников
    // получить все источники: созданные и несозданные
    def light = false
    def excludeIfNotExist = false
    def workflowState = null
    def allRelations = formDataService.getSourcesInfo(formData, light, excludeIfNotExist, workflowState, userInfo, logger)
    relationsB = allRelations?.findAll { it.formType.id == formTypeBenefitId }
    if (relationsB) {
        // если есть льготы, то проверить чтобы у каждой льготы была в подразделении форма "сведения о ТС"
        relationsV = allRelations?.findAll { it.formType.id == formTypeVehicleId }
        for (def relation : relationsB) {
            def find = relationsV.find { it.department.id == relation.department.id }
            if (!find) {
                def formNameV = relationsV[0].formType.name
                def formNameB = relation.formType.name
                def departmentName = relationsV[0].department.name
                logger.error("Подразделения назначенных форм-источников вида «%s» и «%s» должны совпадать. " +
                        "В назначениях отсутствует форма вида «%s» для подразделения «%s»",
                        formNameV, formNameB, formNameV, departmentName)
            }
        }
    }
}

// Ключ для строк формы-источника, по графам columns
String getKey(def row, def columns) {
    return columns.collect { row[it] }.join('#')
}

/**
 * Предконсолидационная проверка 4, 5 или 6.
 *
 * @param relations список назначении
 * @param sourceRowsMap мапа со сгруппированными данными по всем источника
 * @param aliasStart алиас даты начала
 * @param aliasEnd алиас даты окончания
 * @param isPreConsolidationCheck6 признак того что это предконсолидационная проверка 6 (только для льгот)
 */
void preConsolidationCheck4or5or6(def relations, def sourceRowsMap, def aliasStart, aliasEnd, def isPreConsolidationCheck6 = false) {
    def usedMap = [:]
    def findCrossMap = [:]
    for (def relation : relations) {
        def otherRelations = relations?.findAll { it != relation }
        def rowsMap = sourceRowsMap[relation]
        for (def key : rowsMap.keySet().toList()) {
            if (usedMap[key]) {
                continue
            }
            usedMap[key] = true
            def rows = rowsMap[key]
            for (def otherRelation : otherRelations) {
                if (usedMap[otherRelation]) {
                    continue
                }
                def otherRowsMap = sourceRowsMap[otherRelation]
                def otherRows = otherRowsMap[key]
                for (def row : rows) {
                    for (def otherRow : otherRows) {
                        def hasError
                        // проверка двух строк источников
                        if (isPreConsolidationCheck6) {
                            hasError = (row.taxBenefitCode != otherRow.taxBenefitCode)
                        } else {
                            hasError = isCross(row[aliasStart], row[aliasEnd], otherRow[aliasStart], otherRow[aliasEnd], true)
                        }
                        if (hasError) {
                            // запомнить номера строк источников
                            if (findCrossMap[relation] == null) {
                                findCrossMap[relation] = []
                            }
                            findCrossMap[relation].add(row.getIndex())

                            if (findCrossMap[otherRelation] == null) {
                                findCrossMap[otherRelation] = []
                            }
                            findCrossMap[otherRelation].add(otherRow.getIndex())
                        }
                    }
                }
            }
        }
        usedMap[relation] = true
    }
    if (findCrossMap) {
        def messages = []
        findCrossMap.each { relation, rowIndexes ->
            def rowIndexesInStr = rowIndexes?.unique()?.join(', ')
            def formNameV = relation.formType.name
            def departmentName = relation.department.name
            def subMsg = String.format("Строки %s формы-источника вида «%s» для подразделения «%s»",
                    rowIndexesInStr, formNameV, departmentName)
            messages.add(subMsg)
        }
        def subMsg = messages.join(', ')
        if (isPreConsolidationCheck6) {
            logger.error("%s, содержат более одного вида льготы для одного ТС. Проверьте данные форм-источников", subMsg)
        } else {
            logger.error("%s, содержат ТС с пересекающимися периодами владения. Проверьте данные форм-источников", subMsg)
        }
    }
}

@Field
def formTypeVehicleId = 201

@Field
def formTypeBenefitId = 202

def consolidation() {
    // мапа для опредения источника по строке (строка -> источник)
    def relationMap = [:]

    // сведения о ТС
    def dataRowsVehicles = getAllRowsSources(formTypeVehicleId, relationMap)
    // льготы ТС
    def dataRowsBenefit = getAllRowsSources(formTypeBenefitId, relationMap)

    // графы соответствия 2, 4, 8, 9, 13, 14
    def keyColumnsVehilces = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'taxBase', 'baseUnit']
    // графы соответствия 2, 3, 5, 6, 7, 8
    def keyColumnsBenefit = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'powerVal', 'baseUnit']

    // кэш совпадении
    def equalsRowsMap = [:]
    if (dataRowsVehicles && dataRowsBenefit) {
        fillRowsMap(dataRowsBenefit, keyColumnsBenefit, equalsRowsMap)
    }
    def rowIndex = 0
    def dataRows = []
    for (def rowV : dataRowsVehicles) {
        def key = getKey(rowV, keyColumnsVehilces)
        def rowsB = equalsRowsMap[key]
        def findRowsB = []
        for (def rowB : rowsB) {
            // проверка пересечения
            if (isCross(rowV.regDate, rowV.regDateEnd, rowB.benefitStartDate, rowB.benefitEndDate)) {
                findRowsB.add(rowB)
            }
        }
        // шаг a и b
        def newRow = getNewRow(rowV, findRowsB, relationMap)
        newRow.setIndex(rowIndex++)
        dataRows.add(newRow)

        if (findRowsB) {
            rowsB.removeAll(findRowsB)
            if (rowsB?.isEmpty()) {
                equalsRowsMap.remove(key)
            }
        }
    }

    // шаг c
    for (def key : equalsRowsMap.keySet().toList()) {
        def rowsB = equalsRowsMap[key]
        for (def rowB : rowsB) {
            // 1. Проверка наличия данных о льготируемом ТС в форме «Сведения о ТС»
            def relation = relationMap[rowB]
            def formName = relation?.formType?.name
            def formType = relation?.formDataKind?.title
            def periodName = getReportPeriod()?.name + ' ' + getReportPeriod()?.taxPeriod?.year
            def departmentName = relation?.department?.name
            def formNameV = getFormTypeById(formTypeVehicleId)?.name
            logger.warn("Строка %s формы-источника вида «%s», типа «%s» за период «%s» для подразделения «%s»: " +
                    "содержит данные по ТС, которое отсутствует в формах-источниках вида «%s». Проверьте данные форм-источников",
                    rowB.getIndex(), formName, formType, periodName, departmentName, formNameV)

            def newRow = getNewRow(rowB)
            newRow.setIndex(rowIndex++)
            dataRows.add(newRow)
        }
    }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

/**
 * Получить все строки всех источников. Также заполнить мапу relationMap для опредения к какому источнику принадлежит строка.
 *
 * @param sourceFormTypeId идентификатор типа источника
 * @param relationMap мапа для опредения источника по строке (строка -> источник)
 */
def getAllRowsSources(def sourceFormTypeId, def relationMap = null) {
    def relations = getSourcesInfo()?.findAll { it.formType.id == sourceFormTypeId }
    def dataRows = []
    for (def relation : relations) {
        def rows = getDataRows(relation.formDataId)
        dataRows.addAll(rows)
        if (relationMap != null) {
            rows.each { row ->
                relationMap[row] = relation
            }
        }
    }
    return dataRows
}

/**
 * Сформировать новую строку по строке сведении о ТС и по строке льготы.
 *
 * @param rowV строка сведении о ТС
 * @param rowsB строки льгот (может быть null)
 * @param relationMap мапа для опредения источника по строке (строка -> источник)
 */
def getNewRow(def rowV, def rowsB, def relationMap) {
    def newRow = getNewRow()
    // графа 4 = графа 2 источника
    newRow.okato = rowV.codeOKATO
    // графа 5 = графа 4 источника
    newRow.tsTypeCode = rowV.tsTypeCode
    // графа 7 = графа 6 источника
    newRow.model = rowV.model
    // графа 8 = графа 7 источника
    newRow.ecoClass = rowV.ecoClass
    // графа 9 = графа 8 источника
    newRow.vi = rowV.identNumber
    // графа 10 = графа 9 источника
    newRow.regNumber = rowV.regNumber
    // графа 11 = графа 10 источника
    newRow.regDate = rowV.regDate
    // графа 12 = графа 11 источника
    newRow.regDateEnd = rowV.regDateEnd
    // графа 13 = графа 13 источника
    newRow.taxBase = rowV.taxBase
    // графа 14 = графа 14 источника
    newRow.taxBaseOkeiUnit = rowV.baseUnit
    // графа 15 = графа 15 источника
    newRow.createYear = rowV.year
    // графа 16 = графа 16 источника
    newRow.years = rowV.pastYear
    // графа 17 = графа 12 источника
    newRow.ownMonths = rowV.month
    // графа 18 = графа 19 источника
    newRow.partRight = rowV.share

    // графа 21 = графа 2 источника
    if (rowV.version) {
        def record218 = getAllRecords(218L)?.get(rowV.version)
        def record209 = getAllRecords(209L)?.values()?.find {
            it?.AVG_COST?.value == record218?.AVG_COST?.value &&
                    it?.YEAR_FROM?.value < rowV.pastYear && rowV.pastYear <= it?.YEAR_TO?.value
        }
        if (record209 == null) {
            // 2. Проверка наличия повышающего коэффициента для ТС с заполненной графой 22 формы-источника «Сведения о ТС»
            def relation = relationMap[rowV]
            def formName = relation?.formType?.name
            def formType = relation?.formDataKind?.title
            def periodName = getReportPeriod()?.name + ' ' + getReportPeriod()?.taxPeriod?.year
            def departmentName = relation?.department?.name
            def valuas23 = getRefBookValue(211L, record218?.AVG_COST?.value)?.NAME?.value
            def value16 = rowV.pastYear
            logger.warn("Строка %s формы-источника вида «%s», типа «%s» за период «%s» для подразделения «%s»: " +
                    "в справочнике «Повышающие коэффициенты транспортного налога» отсутствует запись, " +
                    "в которой поле «Средняя стоимость» = «%s» и " +
                    "значение «%s» больше значения поля «Количество лет, прошедших с года выпуска ТС (от)» и " +
                    "меньше или равно значения поля «Количество лет, прошедших с года выпуска ТС (до)»",
                    rowV.getIndex(), formName, formType, periodName, departmentName, valuas23, value16)
        }
        newRow.coefKp = record209?.record_id?.value
    }

    if (rowsB) {
        // графа 23
        def value23 = BigDecimal.ZERO
        for (def rowB : rowsB) {
            def start = [ rowV.regDate, rowB.benefitStartDate, getReportPeriodStartDate() ].max()
            def end = [ (rowV.regDateEnd ?: getReportPeriodEndDate()),
                    (rowB.benefitEndDate ?: getReportPeriodEndDate()), getReportPeriodEndDate() ].min()
            def tmp = end.format('M').toInteger() - start.format('M').toInteger() + 1
            if (tmp < 0) {
                tmp = BigDecimal.ZERO
            }
            value23 = value23 + tmp
        }
        newRow.benefitMonths = round(value23, 0)

        // графа 25 = графа 9 источника
        newRow.taxBenefitCode = rowsB[0].taxBenefitCode
    }

    // графа 28 = графа 24 источника
    newRow.deductionCode = rowV.deductionCode
    // графа 29 = графа 25 источника
    newRow.deductionSum = rowV.deduction

    return newRow
}

/** Сформировать новую строку по строке льготы. */
def getNewRow(def rowB) {
    def newRow = getNewRow()

    // графа 4 = графа 2 источника
    newRow.okato = rowB.codeOKATO
    // графа 5 = графа 3 источника
    newRow.tsTypeCode = rowB.tsTypeCode
    // графа 9 = графа 5 источника
    newRow.vi = rowB.identNumber
    // графа 10 = графа 6 источника
    newRow.regNumber = rowB.regNumber
    // графа 13 = графа 7 источника
    newRow.taxBase = rowB.powerVal
    // графа 14 = графа 8 источника
    newRow.taxBaseOkeiUnit = rowB.baseUnit
    // графа 25 = графа 9 источника
    newRow.taxBenefitCode = rowB.taxBenefitCode

    return newRow
}

def getRecord41(def row, def region, def showMsg = false) {
    if (region == null || row.tsTypeCode == null || row.taxBase == null ||
            row.taxBaseOkeiUnit == null || row.years == null) {
        return null
    }

    def regionId = region?.record_id?.value
    def declarationRegionId = formDataDepartment.regionId
    def ecoClassCode = getRefBookValue(40L, row.ecoClass)?.CODE?.value
    def allRecords = getAllRecords(41L).values()
    def records = allRecords.findAll { record ->
        record.DECLARATION_REGION_ID.value == declarationRegionId &&
                record.DICT_REGION_ID.value == regionId &&
                record.CODE.value == row.tsTypeCode &&
                record.UNIT_OF_POWER.value == row.taxBaseOkeiUnit &&
                ((record.MIN_AGE.value == null) || (record.MIN_AGE.value < row.years)) &&
                ((record.MAX_AGE.value == null) || (record.MAX_AGE.value >= row.years)) &&
                ((record.MIN_POWER.value == null) || (record.MIN_POWER.value < row.taxBase)) &&
                ((record.MAX_POWER.value == null) || (record.MAX_POWER.value >= row.taxBase)) &&
                ((record.MIN_ECOCLASS.value == null) || (getRefBookValue(40L, record.MIN_ECOCLASS.value)?.CODE?.value < ecoClassCode)) &&
                ((record.MAX_ECOCLASS.value == null) || (getRefBookValue(40L, record.MAX_ECOCLASS.value)?.CODE?.value >= ecoClassCode))
    }

    if (records != null && records.size() == 1) {
        return records[0]
    } else if (records) {
        def max = 0
        def maxRecord = null
        def checkAttributes = ['MIN_AGE', 'MAX_AGE', 'MIN_POWER', 'MAX_POWER', 'MIN_ECOCLASS', 'MAX_ECOCLASS']
        for (def record : records) {
            def fill = checkAttributes.sum { record[it] ? 1 : 0 }
            if (max < fill) {
                max = fill
                maxRecord = record
            }
        }
        return maxRecord
    }

    if (showMsg) {
        // Логическая проверка 11. Проверка наличия ставки ТС в справочнике
        def columnName4 = getColumnName(row, 'okato')
        def value4 = getRefBookValue(96L, row.okato)?.CODE?.value
        def columnName5 = getColumnName(row, 'tsTypeCode')
        def value5 = getRefBookValue(42L, row.tsTypeCode)?.CODE?.value
        def columnName14 = getColumnName(row, 'taxBaseOkeiUnit')
        def value14 = getRefBookValue(12L, row.taxBaseOkeiUnit)?.CODE?.value
        def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
        def regionCode = getRefBookValue(4L, declarationRegionId)?.CODE?.value
        def departmentName = formDataDepartment.name
        def regionCodeOktmo = region?.CODE?.value
        logger.error("Строка %s: Графа «%s» = «%s», графа «%s» = «%s», графа «%s» = «%s»: " +
                "В справочнике «Ставки транспортного налога» отсутствует запись, актуальная, на дату %s, " +
                "в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», " +
                "поле «Код вида ТС» = «%s», поле «Ед. измерения мощности» = «%s»",
                row.getIndex(), columnName4, value4, columnName5, value5, columnName14, value14,
                dateInStr, regionCode, departmentName, regionCodeOktmo, value5, value14)

    }
    return null
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def subTotalRow1Map = [:] // подитог 1ого уровня -> мапа с подитогами 2ого уровня
    def subTotalRow2Map = [:] // подитог 2ого уровня -> строки подгруппы
    def simpleRows = []
    def total = null

    // разложить группы по мапам
    dataRows.each{ row ->
        if (row.getAlias() == null) {
            simpleRows.add(row)
        } else if (row.getAlias().contains('total2')) {
            subTotalRow2Map.put(row, simpleRows)
            simpleRows = []
        } else if (row.getAlias().contains('total1')) {
            subTotalRow1Map.put(row, subTotalRow2Map)
            subTotalRow2Map = [:]
        } else {
            total = row
        }
    }
    dataRows.clear()

    // отсортировать и добавить все строки
    def tmpSorted1Rows = subTotalRow1Map.keySet().toList()?.sort { getSortValue(it) }
    tmpSorted1Rows.each { keyRow1 ->
        def subMap = subTotalRow1Map[keyRow1]
        def tmpSorted2Rows = subMap.keySet().toList()?.sort { getSortValue(it) }
        tmpSorted2Rows.each { keyRow2 ->
            def dataRowsList = subMap[keyRow2]
            sortAddRows(dataRowsList, dataRows)
            dataRows.add(keyRow2)
        }
        dataRows.add(keyRow1)
    }
    // если остались данные вне иерархии, то добавить их перед итогом
    sortAddRows(simpleRows, dataRows)
    dataRows.add(total)

    dataRowHelper.saveSort()
}

// значение группируемых столбцов для сортировки подитоговых строк
def getSortValue(def row) {
    return row.getCell('kno').refBookDereference + '#' + row.getCell('kpp').refBookDereference + '#' + row.getCell('okato').refBookDereference
}

void sortAddRows(def addRows, def dataRows) {
    if (!addRows.isEmpty()) {
        def firstRow = addRows[0]
        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, addRows, columnNameList)
        sortRowsSimple(addRows)
        dataRows.addAll(addRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 35
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFileMap = [:]           // мапа для хранения строк итогов/подитогов со значениями из файла (стили простых строк)
    def totalRowMap = [:]                   // мапа для хранения строк итогов/подитогов нф с посчитанными значениями и со стилями

    // графа 2, 3, 20, 25, 26
    def columns = ['kno', 'kpp', 'taxRate', 'taxBenefitCode', 'taxBenefitBase']
    def columnNames = columns.collect { getColumnName(tmpRow, it) }
    def subMsg = columnNames.join('», «')

    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    def departmentRegionId = formDataDepartment.regionId
    if (!departmentRegionId) {
        logger.warn("Не удалось заполнить графу «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»", subMsg)
    }

    // заполнить кэш данными из справочника ОКТМО
    def limitRows = 10
    if (allValuesCount > limitRows) {
        fillRefBookCache(96L)
        fillRecordCache(96L, 'CODE', getReportPeriodEndDate())
    }

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск итоговой строки
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("итого")) {
            // получить значения итоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск подитоговых строк
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого по ")) {
            def subTotalRowFromFile = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            def knoId = subTotalRowFromFile.kno
            def subTotalRow
            // сформировать и подсчитать подитоги
            if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("октмо")) {
                def oktmo = (rowValues[4] ? getRecordIdImport(96L, 'CODE', rowValues[4], fileRowIndex, 4 + colOffset) : null)
                subTotalRow = calcSubTotalRow2(rowIndex - 1, rows, knoId, oktmo)
            } else {
                subTotalRow = calcSubTotalRow1(rowIndex - 1, rows, knoId)
            }
            rows.add(subTotalRow)
            // получить значения подитоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = subTotalRowFromFile
            totalRowMap[rowIndex] = subTotalRow

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, departmentRegionId, subMsg)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    totalRowMap[rowIndex] = totalRow
    updateIndexes(rows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        // сравнение
        totalRowFromFileMap.keySet().toArray().each { index ->
            def totalFromFile = totalRowFromFileMap[index]
            def total = totalRowMap[index]
            compareTotalValues(totalFromFile, total, getTotalColumns(), logger, 0, false)
            // задание значении итоговой строке нф из итоговой строки файла (потому что в строках из файла стили для простых строк)
            total.setImportIndex(totalFromFile.getImportIndex())
            (getTotalColumns() + 'fix').each { alias ->
                total[alias] = totalFromFile[alias]
            }
        }
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            // название первого столбца хранится в нулевой скрытой графе
            [(headerRows[0][0]) : headers[0].fix],
            [(headerRows[0][2]) : headers[0].kno],
            [(headerRows[0][3]) : headers[0].kpp],
            [(headerRows[0][4]) : headers[0].okato],
            [(headerRows[0][5]) : headers[0].tsTypeCode],
            [(headerRows[0][6]) : headers[0].tsType],
            [(headerRows[0][7]) : headers[0].model],
            [(headerRows[0][8]) : headers[0].ecoClass],
            [(headerRows[0][9]) : headers[0].vi],
            [(headerRows[0][10]): headers[0].regNumber],
            [(headerRows[0][11]): headers[0].regDate],
            [(headerRows[0][12]): headers[0].regDateEnd],
            [(headerRows[0][13]): headers[0].taxBase],
            [(headerRows[0][14]): headers[0].taxBaseOkeiUnit],
            [(headerRows[0][15]): headers[0].createYear],
            [(headerRows[0][16]): headers[0].years],
            [(headerRows[0][17]): headers[0].ownMonths],
            [(headerRows[0][18]): headers[0].partRight],
            [(headerRows[0][19]): headers[0].coefKv],
            [(headerRows[0][20]): headers[0].taxRate],
            [(headerRows[0][21]): headers[0].coefKp],
            [(headerRows[0][22]): headers[0].calculatedTaxSum],
            [(headerRows[0][23]): headers[0].benefitMonths],
            [(headerRows[0][24]): headers[0].coefKl],
            [(headerRows[0][25]): headers[0].taxBenefitCode],
            [(headerRows[1][25]): headers[1].taxBenefitCode],
            [(headerRows[1][26]): headers[1].taxBenefitBase],
            [(headerRows[1][27]): headers[1].taxBenefitSum],
            [(headerRows[0][28]): headers[0].deductionCode],
            [(headerRows[1][28]): headers[1].deductionCode],
            [(headerRows[1][29]): headers[1].deductionSum],
            [(headerRows[0][30]): headers[0].taxSumToPay],
            [(headerRows[0][31]): headers[0].q1],
            [(headerRows[0][32]): headers[0].q2],
            [(headerRows[0][33]): headers[0].q3],
            [(headerRows[0][34]): headers[0].q4],
            [(headerRows[2][0]) : '1']
    ]
    (2..34).each {
        headerMapping.add([(headerRows[2][it]) : it.toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param departmentRegionId регион подразделения
 * @param subMsg список названии граф зависящих от регина формы
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def departmentRegionId, def subMsg) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def colIndex

    // графа 4 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex = 4
    def record96 = null
    def region = null
    if (values[colIndex]) {
        record96 = getRecordImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
        newRow.okato = record96?.record_id?.value

        lastImportOktmo = values[colIndex]
        region = getRegion(newRow.okato)
    }

    // графа 2 - атрибут 2102 - TAX_ORGAN_CODE - «Код налогового органа (кон.)», справочник 210 «Параметры представления деклараций по транспортному налогу»
    colIndex = 2
    if (!values[colIndex] || !values[colIndex + 1]) {
        // 3.с В справочнике «Параметры представления декларации по транспортному налогу» не заполнены графы 2, 3 в файле
        def columnName2 = getColumnName(newRow, 'kno')
        def columnName3 = getColumnName(newRow, 'kpp')
        def columnNames = []
        if (!values[colIndex]) {
            columnNames.add(columnName2)
        }
        if (!values[colIndex + 1]) {
            columnNames.add(columnName3)
        }
        def subMsg2 = columnNames.join('», «')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», «%s», т.к. не заполнены графы «%s»",
                fileRowIndex, getXLSColumnName(colIndex + 1), columnName2, columnName3, subMsg2)
    } else if (departmentRegionId && region) {
        def records210 = getRecords210(values[colIndex], values[3], values[4], fileRowIndex, colIndex, colOffset)
        if (records210?.size() == 1) {
            newRow.kno = records210[0]?.record_id?.value
        } else if (records210 == null || records210?.isEmpty()) {
            // 3.a В справочнике «Параметры представления декларации по транспортному налогу» не найдено ни одной записи
            // 3.b В справочнике «Параметры представления декларации по транспортному налогу» найдено несколько записей
            def columnName2 = getColumnName(newRow, 'kno')
            def columnName3 = getColumnName(newRow, 'kpp')
            def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            def subMsg2 = (records210?.size() > 1 ?
                "найдено несколько записей, актуальных на дату %s, в которых" :
                "отсутствует запись, актуальная на дату %s, в которой")
            sunMsg2 = String.format(subMsg2, dateInStr)
            def regionCode = getRefBookValue(4L, departmentRegionId)?.CODE?.value
            def departmentName = formDataDepartment.name
            def regionCodeOktmo = region?.CODE?.value
            def value4 = record96?.CODE?.value
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графы: «%s», «%s», т.к. в справочнике " +
                    "«Параметры представления деклараций по транспортному налогу» %s " +
                    "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                    "справочника «Подразделения» для подразделения «%s», " +
                    "поле «Код региона РФ» = «%s», поле «Код ОКТМО» = «%s»",
                    fileRowIndex, getXLSColumnName(colIndex + 1), columnName2, columnName3, sunMsg2,
                    regionCode, departmentName, regionCodeOktmo, value4)
        }
    }

    // графа 3 - зависит от графы 2 - не проверяется, потому что используется для нахождения записи для графы 2

    // 2. Проверка заполнения кода ОКТМО - перенес сюда что б не нарушалась последовательность вывода сообщений
    if (!values[4]) {
        def columnName4 = getColumnName(newRow, 'okato')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графы «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, getXLSColumnName(5), subMsg, columnName4)
    }

    // графа 5 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    colIndex = 5
    def record42 = getRecordImport(42, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    newRow.tsTypeCode = record42?.record_id?.value

    // графа 6 - зависит от графы 5 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    colIndex++
    if (record42) {
        def expectedValues = [record42?.NAME?.value]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'tsType'), record42?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 7
    colIndex++
    newRow.model = values[colIndex]

    // графа 8 - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
    colIndex++
    newRow.ecoClass = getRecordIdImport(40, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9
    colIndex++
    newRow.vi = values[colIndex]

    // графа 10
    colIndex++
    newRow.regNumber = values[colIndex]

    // графа 11
    colIndex++
    newRow.regDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex++
    newRow.regDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13
    colIndex++
    newRow.taxBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    colIndex++
    newRow.taxBaseOkeiUnit = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 15
    colIndex++
    newRow.createYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 16
    colIndex++
    newRow.years = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.ownMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.partRight = values[colIndex]

    // графа 19
    colIndex++
    newRow.coefKv = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20 - атрибут 416 - VALUE - «Ставка (руб.)», справочник 41 «Ставки транспортного налога»
    colIndex++
    if (departmentRegionId) {
        // граф 4, 5, 13, 14, 16
        def columns = ['okato', 'tsTypeCode', 'taxBase', 'taxBaseOkeiUnit', 'years']
        def emptyColumns = columns.findAll { !newRow[it] }
        if (emptyColumns) {
            // 4.a Проверка заполнения обязательных граф для выбора налоговой ставки
            def columnName20 = getColumnName(newRow, 'taxRate')
            def columnNames = emptyColumns?.collect { getColumnName(newRow, it) }
            def subMsg2 = columnNames?.join('», «')
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнены графы «%s»",
                    fileRowIndex, getXLSColumnName(colIndex + 1), columnName20, subMsg2)
        } else {
            def record41 = getRecord41(newRow, region)
            newRow.taxRate = record41?.record_id?.value

            if (!newRow.taxRate) {
                // 4.b Нет записи в справочнике
                def columnName20 = getColumnName(newRow, 'taxRate')
                def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
                def regionCode = getRefBookValue(4L, departmentRegionId)?.CODE?.value
                def departmentName = formDataDepartment.name
                def regionCodeOktmo = region?.CODE?.value
                def value5 = getRefBookValue(42L, newRow.tsTypeCode)?.CODE?.value
                def value14 = getRefBookValue(12L, newRow.taxBaseOkeiUnit)?.CODE?.value
                logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                        "«Ставки транспортного налога» отсутствует запись, актуальная, на дату %s, в которой " +
                        "поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                        "справочника «Подразделения» для подразделения «%s», " +
                        "поле «Код региона РФ» = «%s», поле «Код вида ТС» = «%s», " +
                        "поле «Ед. измерения мощности» = «%s»",
                        fileRowIndex, getXLSColumnName(colIndex + 1), columnName20, dateInStr,
                        regionCode, departmentName, regionCodeOktmo, value5, value14)
            }
        }
    }

    // TODO (Ramil Timerbaev) неуникальный атрибут
    // графа 21 - атрибут 2093 - COEF - «Повышающий коэффициент», справочник 209 «Повышающие коэффициенты транспортного налога»
    colIndex++
    newRow.coefKp = getRecordIdImport(209L, 'COEF', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 22
    colIndex++
    newRow.calculatedTaxSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 23
    colIndex++
    newRow.benefitMonths = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 24
    colIndex++
    newRow.coefKl = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 25 - атрибут 19.15 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 7.6 «Параметры налоговых льгот транспортного налога».«Коды налоговых льгот и вычетов транспортного налога»
    colIndex++
    if (departmentRegionId && region) {
        def record6 = getAllRecords(6L)?.values()?.find { it?.CODE?.value == values[colIndex] }
        def regionId = region?.record_id?.value
        def record7 = null
        if (record6) {
            record7 = getAllRecords(7L)?.values()?.find {
                it?.DECLARATION_REGION_ID?.value == departmentRegionId &&
                        it?.DICT_REGION_ID?.value == regionId &&
                        it?.TAX_BENEFIT_ID?.value == record6?.record_id?.value &&
                        it?.BASE?.value == values[colIndex + 1]
            }
        }
        if (record7) {
            newRow.taxBenefitCode = record7?.record_id?.value
        } else {
            // 5. Проверка наличия информации о налоговой льготе в справочнике «Параметры налоговых льгот транспортного налога»
            def columnName25 = getColumnName(newRow, 'taxBenefitCode')
            def columnName26 = getColumnName(newRow, 'taxBenefitBase')
            def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            def regionCode = getRefBookValue(4L, departmentRegionId)?.CODE?.value
            def departmentName = formDataDepartment.name
            def regionCodeOktmo = region?.CODE?.value
            def value25 = values[colIndex]
            def value26 = values[colIndex + 1]
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графы «%s», «%s», т.к. в справочнике " +
                    "«Параметры налоговых льгот транспортного налога» отсутствует запись, актуальная на дату %s, " +
                    "в которой поле «Код субъекта РФ представителя декларации» равно значению поля «Регион» (%s) " +
                    "справочника «Подразделения» для подразделения «%s», поле «Код региона РФ» = «%s», " +
                    "поле «Код налоговой льготы» = «%s», поле «Основание» = «%s»",
                    fileRowIndex, getXLSColumnName(colIndex + 1), columnName25, columnName26,
                    dateInStr, regionCode, departmentName, regionCodeOktmo, value25, value26)
        }
    }

    // графа 26 - зависит от графы 25 - не проверяется, потому что используется для нахождения записи для графы 9
    colIndex++

    // графа 27
    colIndex++
    newRow.taxBenefitSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 28 - атрибут 15 - CODE - «Код», справочник 6 «Коды налоговых льгот и вычетов транспортного налога»
    colIndex++
    newRow.deductionCode = getRecordIdImport(6L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 29..34
    colIndex = 28
    ['deductionSum', 'taxSumToPay', 'q1', 'q2', 'q3', 'q4'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

// для подитога первого уровня (Итого по КНО/КПП) не указывается октмо, необходимый для заполнения графы 2
// поэтому используется октмо из последней предыдущией строки
@Field
def lastImportOktmo = null

/**
 * Получить новую итоговую строку нф по значениям из экселя. Строка используется только для получения значении,
 * для вставки в бд сформируются другие строки с нормальными стилями и алиасами.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа fix
    def int colIndex = 0
    newRow.fix = values[colIndex]

    // графа 2
    colIndex = 2
    if (values[4]) {
        lastImportOktmo = values[4]
    }
    def oktmo = lastImportOktmo

    def records210 = getRecords210(values[colIndex], values[3], oktmo, fileRowIndex, colIndex, colOffset)
    newRow.kno = (records210?.size() == 1 ? records210[0]?.record_id?.value : null)

    // графа 4 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex = 4
    newRow.okato = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 30..34
    colIndex = 29
    getTotalColumns().each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

def getRecords210(def kno, def kpp, def oktmo, def fileRowIndex, def colIndex, def colOffset) {
    def records210 = null
    def record96 = getRecordImport(96, 'CODE', oktmo, fileRowIndex, colIndex + colOffset, false)
    if (record96) {
        def oktmoId = record96?.record_id?.value
        def declarationRegionId = formDataDepartment.regionId
        def regionId = getRegion(oktmoId)?.record_id?.value
        records210 = getAllRecords(210L)?.values()?.findAll {
            it?.DECLARATION_REGION_ID?.value == declarationRegionId &&
                    it?.REGION_ID?.value == regionId &&
                    it?.TAX_ORGAN_CODE?.value == kno &&
                    it?.KPP?.value == kpp &&
                    it?.OKTMO?.value == oktmoId
        }
    }
    return records210
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = (refBookId == 310 ? getReportPeriodEndDate() - 1 : getReportPeriodEndDate())
        def provider = getProvider(refBookId)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

// Сравнения данных с данными формы предыдущего периода
void comparePrevRows() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def prevDataRows = getPrevDataRows()
    if (!prevDataRows) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        // Проверка при сравнении 1. Проверка наличия формы за предыдущий период в состоянии «Принята»
        logger.warn("Не удалось сравнить данные формы с данными формы за предыдущий период. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: %s %s для подразделения «%s»",
                periodName, year, formDataDepartment.name)
    }

    // сброс окрашивания если предыдущих данных нет
    if (!prevDataRows) {
        def rows = dataRows.findAll { !it.getAlias() }
        for (def row : rows) {
            setDefaultStyles(row)
        }
        return
    }

    def currentActualRowsMap = getActualRowsMap(dataRows)
    def prevActualRowsMap = getActualRowsMap(prevDataRows)

    // графа 11, 12, 15, 16, 18..29 (31..33)
    def compareColumns = ['regDate', 'regDateEnd', 'createYear', 'years', 'partRight', 'coefKv', 'taxRate',
            'coefKp', 'calculatedTaxSum', 'benefitMonths', 'coefKl', 'taxBenefitCode', /* 'taxBenefitBase', */
            'taxBenefitSum', 'deductionCode', 'deductionSum']
    def reportPeriod = getReportPeriod()
    switch (reportPeriod.order) {
        case 4: compareColumns.add('q3')
        case 3: compareColumns.add('q2')
        case 2: compareColumns.add('q1')
    }

    def compareStyleName = 'Сравнение'

    // сравнение
    currentActualRowsMap.each { groupKey, row ->
        def prevRow = prevActualRowsMap[groupKey]
        if (prevRow == null) {
            // окрасить всю строку
            allColumns.each { alias ->
                row.getCell(alias).setStyleAlias(compareStyleName)
            }
        } else {
            // окрасить ячейки с расхождениями
            for (def alias : compareColumns) {
                if (row[alias] != prevRow[alias]) {
                    row.getCell(alias).setStyleAlias(compareStyleName)
                }
            }

            // сравнение зависимых граф
            // графа 26
            def taxBenefitBase1 = getRefBookValue(7L, row.taxBenefitBase)?.BASE?.value
            def taxBenefitBase2 = getRefBookValue(7L, prevRow.taxBenefitBase)?.BASE?.value
            if (taxBenefitBase1 != taxBenefitBase2) {
                row.getCell('benefitBase').setStyleAlias(compareStyleName)
            }
        }
    }
}

@Field
def prevDataRows = null

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (prevDataRows != null) {
        return prevDataRows
    }
    if (getPrevReportPeriod()?.period == null) {
        prevDataRows = []
        return prevDataRows
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    prevDataRows = (prevFormData?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(prevFormData)?.allSaved : [])
    return prevDataRows
}

@Field
def prevReportPeriodMap = null

/**
 * Получить предыдущий отчетный период
 *
 * @return мапа с данными предыдущего периода:
 *      period - период (может быть null, если предыдущего периода нет);
 *      periodName - название;
 *      year - год;
 */
def getPrevReportPeriod() {
    if (prevReportPeriodMap != null) {
        return prevReportPeriodMap
    }
    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def find = false
    // предыдущий период в том же году, что и текущий, и номера периодов отличаются на единицу
    if (prevReportPeriod && reportPeriod.order > 1 && reportPeriod.order - 1 == prevReportPeriod.order &&
            reportPeriod.taxPeriod.year == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    // если текущий период первый в налоговом периоде, то предыдущий период должен быть последним, и года налоговых периодов должны отличаться на единицу
    if (!find && prevReportPeriod && reportPeriod.order == 1 && prevReportPeriod.order == 4 &&
            reportPeriod.taxPeriod.year - 1 == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    prevReportPeriodMap = [:]
    if (find) {
        prevReportPeriodMap.period = prevReportPeriod
        prevReportPeriodMap.periodName = prevReportPeriod.name
        prevReportPeriodMap.year = prevReportPeriod.taxPeriod.year
    } else {
        // получение названии периодов
        def filter = 'T = 1'
        def provider = formDataService.getRefBookProvider(refBookFactory, 8L, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        records?.sort { it?.END_DATE?.value }

        prevReportPeriodMap.period = null
        prevReportPeriodMap.periodName = records[reportPeriod.order - 2].NAME?.value
        prevReportPeriodMap.year = (reportPeriod.order == 1 ? reportPeriod.taxPeriod.year - 1 : reportPeriod.taxPeriod.year)
    }
    return prevReportPeriodMap
}

// Получить ключ группировки 1ого уровня (по графе 3, 4)
def getGroupL1Key(def row) {
    return row?.getCell('kno')?.refBookDereference + '#' + row?.getCell('kpp')?.refBookDereference
}

/**
 * Получить мапу с актуальными строками. Ключ - список значении граф, значение - актуальная строка.
 * Если строк несколько, то берется строка с большим значением в графе 11.
 */
def getActualRowsMap(def rows) {
    // строки группировки (графа 4, 5, 9, 10)
    def groupColumns = ['okato', 'tsTypeCode', 'vi', 'regNumber']
    def map = [:]
    for (def row : rows) {
        if (row.getAlias()) {
            continue
        }
        def key = getKey(row, groupColumns)
        map[key] = (map[key] != null && map[key].regDate > row.regDate) ? map[key] : row
    }
    return map
}

/** Получить итоговую строку. */
def creatTotalRow() {
    def newRow = createRow()
    newRow.getCell("fix").colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    for (def alias : getTotalColumns()) {
        newRow[alias] = BigDecimal.ZERO
    }
    return newRow
}

/** Получить итоговую строку с суммами. */
def calcTotalRow(def dataRows, def showMsg = false) {
    def newRow = creatTotalRow()
    newRow.setAlias('total')
    newRow.fix = 'Итого'

    for (def alias : getTotalColumns()) {
        BigDecimal sum = BigDecimal.ZERO
        for (def row : dataRows) {
            if (!row.getAlias()) {
                sum = sum + (row[alias] ?: BigDecimal.ZERO)
            }
        }
        if (checkOverflowLocal(sum, newRow, alias, 15, dataRows.size() + 1, showMsg)) {
            newRow[alias] = sum
        } else {
            newRow[alias] = null
        }
    }
    return newRow
}

/**
 * Добавить промежуточные итоги.
 * По графе 2, 3 (КНО/КПП) - 1 уровень группировки, а внутри этой группы по графе 4 (октмо) - 2 уровнь группировки.
 */
void addAllStatic(def dataRows) {
    for (int i = 0; i < dataRows.size(); i++) {
        def row = getRow(dataRows, i)
        def nextRow = getRow(dataRows, i + 1)
        int j = 0

        // 2 уровнь группировки
        def value2 = row?.getCell('okato')?.refBookDereference
        def nextValue2 = nextRow?.getCell('okato')?.refBookDereference
        if (row.getAlias() == null && nextRow == null || value2 != nextValue2) {
            def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.okato, true)
            j++
            dataRows.add(i + j, subTotalRow2)
        }

        // 1 уровнь группировки
        def value1 = getGroupL1Key(row)
        def nextValue1 = getGroupL1Key(nextRow)
        if (row.getAlias() == null && nextRow == null || value1 != nextValue1) {
            // если все значения пустые, то подитог по 2 уровню группировки не добавится,
            // поэтому перед добавлением подитога по 1 уровню группировки, нужно добавить подитог с пустыми значениями по 2 уровню
            if (j == 0) {
                def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.okato, true)
                j++
                dataRows.add(i + j, subTotalRow2)
            }
            def subTotalRow1 = calcSubTotalRow1(i, dataRows, row.kno, true)
            j++
            dataRows.add(i + j, subTotalRow1)
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога 1 уровня группировки - по графе 2, 3 КНО/КПП. */
def calcSubTotalRow1(int i, def dataRows, def kno, def showMsg = false) {
    def newRow = creatTotalRow()
    newRow.setAlias('total1#' + i)
    newRow.fix = 'Итого по КНО/КПП'

    // значения группы
    newRow.kno = kno

    def sums = [:]
    for (def alias : getTotalColumns()) {
        sums[alias] = BigDecimal.ZERO
    }

    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias()) {
            continue
        }
        if (newRow.kno != srow.kno || newRow.kpp != srow.kpp) {
            break
        }
        for (def alias : getTotalColumns()) {
            if (srow[alias] != null) {
                sums[alias] = sums[alias] + srow[alias]
            }
        }
    }

    for (def alias : getTotalColumns()) {
        if (checkOverflowLocal(sums[alias], newRow, alias, 15, i + 3, showMsg)) {
            newRow[alias] = sums[alias]
        } else {
            newRow[alias] = null
        }
    }

    return newRow
}

/** Расчет итога 2 уровня группировки - по графе 4 ОКТМО (группировка внутри группы по КНО/КПП). */
def calcSubTotalRow2(int i, def dataRows, def kno, def okato, def showMsg = false) {
    def newRow = creatTotalRow()
    newRow.setAlias("total2#" + i)
    newRow.fix = 'ИТОГО по КНО/КПП/ОКТМО'

    // значения группы
    newRow.kno = kno
    newRow.okato = okato

    def sums = [:]
    for (def alias : getTotalColumns()) {
        sums[alias] = BigDecimal.ZERO
    }

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias() != null || srow.okato != newRow.okato) {
            break
        }
        for (def alias : getTotalColumns()) {
            if (srow[alias] != null) {
                sums[alias] = sums[alias] + srow[alias]
            }
        }
    }

    for (def alias : getTotalColumns()) {
        if (checkOverflowLocal(sums[alias], newRow, alias, 15, i + 2, showMsg)) {
            newRow[alias] = sums[alias]
        } else {
            newRow[alias] = null
        }
    }

    return newRow
}

/** Получение строки по номеру. */
def getRow(def dataRows, int i) {
    if (i < dataRows.size() && i >= 0) {
        return dataRows.get(i)
    } else {
        return null
    }
}

def createRow() {
    return (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
}

def getNewRow() {
    def newRow = createRow()
    setDefaultStyles(newRow)
    return newRow
}

void setDefaultStyles(def row) {
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
}

/** Заполнить refBookCache всеми записями справочника refBookId. */
void fillRefBookCache(def refBookId) {
    def records = getAllRecords2(refBookId)
    for (def record : records) {
        def recordId = record?.record_id?.value
        def key = getRefBookCacheKey(refBookId, recordId)
        if (refBookCache[key] == null) {
            refBookCache.put(key, record)
        }
    }
}

/**
 * Заполнить recordCache всеми записями справочника refBookId из refBookCache.
 *
 * @param refBookId идентификатор справочника
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param date дата по которой будет осуществляться поиск
 */
void fillRecordCache(def refBookId, def alias, def date) {
    def keys = refBookCache.keySet().toList()
    def needKeys = keys.findAll { it.contains(refBookId + SEPARATOR) }
    def dateSts = date.format('dd.MM.yyyy')
    def rb = refBookFactory.get(refBookId)
    for (def needKey : needKeys) {
        def recordId = refBookCache[needKey]?.record_id?.value
        def value = refBookCache[needKey][alias]?.value
        def filter = getFilter(alias, value, rb)
        def key = dateSts + filter
        if (recordCache[refBookId] == null) {
            recordCache[refBookId] = [:]
        }
        recordCache[refBookId][key] = recordId
    }
}

/**
 * Формирование фильтра. Взято из FormDataServiceImpl.getRefBookRecord(...)
 *
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param value значение атрибута справочника
 * @param rb справочник
 */
def getFilter(def alias, def value, def rb) {
    def filter
    if (value == null || value.isEmpty()) {
        filter = alias + " is null"
    } else {
        RefBookAttributeType type = rb.getAttribute(alias).getAttributeType()
        String template
        // TODO: поиск по выражениям с датами не реализован
        if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
            if (!isNumeric(value)) {
                // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                return null
            }
            template = "%s = %s"
        } else {
            template = "LOWER(%s) = LOWER('%s')"
        }
        filter = String.format(template, alias, value)
    }
    return filter
}

boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?")
}

@Field
def allRecordsMap2 = [:]

def getAllRecords2(def refbookId) {
    if (allRecordsMap2[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        def date = (refbookId == 310 ? getReportPeriodEndDate() - 1 : getReportPeriodEndDate())
        allRecordsMap2[refbookId] = provider.getRecords(date, null, null, null)
    }
    return allRecordsMap2[refbookId]
}

@Field
def sourcesInfo = null

def getSourcesInfo() {
    if (sourcesInfo == null) {
        sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
        if (!sourcesInfo) {
            sourcesInfo = []
        }
    }
    return sourcesInfo
}

@Field
def dataRowsSourceMap = [:]

def getDataRows(def formDataId) {
    if (dataRowsSourceMap[formDataId] == null) {
        def source = formDataService.get(formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
        dataRowsSourceMap[formDataId] = sourceDataRows
        if (dataRowsSourceMap[formDataId] == null) {
            dataRowsSourceMap[formDataId] = []
        }
    }
    return dataRowsSourceMap[formDataId]
}

/**
 * Проверка пересечения диапозона дат.
 *
 * @param start1 дата начала 1
 * @param end1 дата окончания 1
 * @param start2 дата начала 2
 * @param end2 дата окончания 2
 * @param useEndPeriodDate признак использования даты окончания периода формы, если даты окончания не заданы
 */
def isCross(def start1, def end1, def start2, def end2, def useEndPeriodDate = false) {
    if (start1 == null || start2 == null) {
        return null
    }
    def tmpEnd1 = end1 ?: (useEndPeriodDate ? getReportPeriodEndDate() : null)
    def tmpEnd2 = end2 ?: (useEndPeriodDate ? getReportPeriodEndDate() : null)
    if (start1 <= start2 && (tmpEnd1 == null || start2 <= tmpEnd1) || start2 <= start1 && (tmpEnd2 == null || start1 <= tmpEnd2)) {
        return true
    }
    return false
}

// проверка на наличие пересечений
boolean checkHasCross(def rows) {
    boolean hasCross = false
    for (int i = 0; i < rows.size(); i++) {
        def row1 = rows[i]
        for (int j = i + 1; j < rows.size(); j++) {
            def row2 = rows[j]
            if (isCross(row1.regDate, row1.regDateEnd, row2.regDate, row2.regDateEnd, true))  {
                hasCross = true
                break
            }
        }
        if (hasCross) {
            break
        }
    }
    return hasCross
}

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}

// графа 30..34
def getTotalColumns() {
    if (totalColumns == null) {
        def order = getReportPeriod().order
        if (order == 1) {
            totalColumns = ['taxSumToPay', 'q1']
        } else if (order == 2) {
            totalColumns = ['taxSumToPay', 'q1', 'q2']
        } else if (order == 3) {
            totalColumns = ['taxSumToPay', 'q1', 'q2', 'q3']
        } else if (order == 4) {
            totalColumns = ['taxSumToPay', 'q1', 'q2', 'q3', 'q4']
        }
    }
    return totalColumns
}

def checkOverflowLocal(BigDecimal value, def row, def alias, def size, def index, def showMsg) {
    if (value == null) {
        return true
    }
    BigDecimal overpower = new BigDecimal("1E" + size)

    if (value.abs().compareTo(overpower) != -1) {
        if (showMsg) {
            // Логическая проверка 17. Проверка разрядности значений граф 30-34, рассчитываемых в итоговых строках
            String columnName = getColumnName(row, alias)
            logger.error("Строка %s: Значение графы «%s» превышает допустимую разрядность (%s знаков)",
                    index, columnName, size)
        }
        return false
    }
    return true
}