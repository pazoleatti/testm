package form_template.market.summary_5_3_2.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormTemplate
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 5.3.2 Внутренние интервалы процентных ставок по Кредитным продуктам и Субординированным кредитам.
 *
 * В форме нет расчетов, логических проверок, загрузки эксель, есть только консолидация из формы 5.2.
 * При консолидации данные источника группируются. По каждой валюте и типу процентной ставки формируется группа из 20 строк.
 * Что бы не формировать эти строки кодом в ручную, а также для наглядности при доработках, они хранятся в начальных данных макета.
 * Поэтому после создании формы, начальные строки удаляются.
 *
 * formTemplateId = 908
 * formType = 908
 */

// графа    - fix
// графа 1  - rowNum                - № пп
// графа 2  - creditRating          - Кредитный рейтинг заемщика
// графа 3  - category              - Категория обеспечения кредита
// графа 4  - creditPeriod          - Срок кредита / Объем кредита в рублёвом эквиваленте / Границы процентного интервала
// графа 5  - count1year100         - Количество сопоставимых кредитов - скрытый столбец
// графа 6  - min1year100           - Интервал процентных ставок (% годовых) / min
// графа 7  - max1year100           - Интервал процентных ставок (% годовых) / max
// графа 8  - count1year100_1000    - скрытый столбец
// графа 9  - min1year100_1000
// графа 10 - max1year100_1000
// графа 11 - count1year1000        - скрытый столбец
// графа 12 - min1year1000
// графа 13 - max1year1000
// графа 14 - count1_5year100       - скрытый столбец
// графа 15 - min1_5year100
// графа 16 - max1_5year100
// графа 17 - count1_5year100_1000  - скрытый столбец
// графа 18 - min1_5year100_1000
// графа 19 - max1_5year100_1000
// графа 20 - count1_5year1000      - скрытый столбец
// графа 21 - min1_5year1000
// графа 22 - max1_5year1000
// графа 23 - count5_10year100      - скрытый столбец
// графа 24 - min5_10year100
// графа 25 - max5_10year100
// графа 26 - count5_10year100_1000 - скрытый столбец
// графа 27 - min5_10year100_1000
// графа 28 - max5_10year100_1000
// графа 29 - count5_10year1000     - скрытый столбец
// графа 30 - min5_10year1000
// графа 31 - max5_10year1000
// графа 32 - count10year100        - скрытый столбец
// графа 33 - min10year100
// графа 34 - max10year100
// графа 35 - count10year100_1000   - скрытый столбец
// графа 36 - min10year100_1000
// графа 37 - max10year100_1000
// графа 38 - count10year1000       - скрытый столбец
// графа 39 - min10year1000
// графа 40 - max10year1000

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        deleteAllRows()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

@Field
def refBookCache = [:]
@Field
def recordCache = [:]
@Field
def providerCache = [:]

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

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

void consolidation() {
    // мапа со списоком строк НФ (ключ по критериям сопоставимости -> строки НФ)
    def groupRowsMap = [:]

    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    sourcesInfo.each { Relation relation ->
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        // сгруппировать строки в группы
        for (def row : dataRows) {
            if (row.groupExclude != null && getRefBookValue(38L, row.groupExclude)?.CODE?.value == 0) {
                def key = getKey(row, relation)
                if (groupRowsMap[key] == null) {
                    groupRowsMap[key] = []
                }
                groupRowsMap[key].add(row)
            }
        }
    }

    // мапа с валютами (буквенные код валюты -> список из двух списков строк: фиксированная процентная ставка и плавающая процентная ставка)
    def currencyMap = [:]
    groupRowsMap.keySet().toList().each { key ->
        def rows = groupRowsMap[key]
        def row = rows[0]
        // валюта
        def code = getCurrencyCode(row)
        if (currencyMap[code] == null) {
            // список из двух списков строк: фиксированная процентная ставка и плавающая процентная ставка
            currencyMap[code] = [[], []]
        }
        // тип процентной ставки
        def index = (isFix(row) ? 0 : 1)
        currencyMap[code][index].add(key)
    }

    // сортировка по валюте
    def sortedCurrency = currencyMap.keySet().toList().sort { a, b ->
        def a1 = getSortIndex(a)
        def b1 = getSortIndex(b)
        if (a1 == 0 && b1 == 0) {
            return a <=> b
        }
        return (b1 <=> a1)
    }

    // добавить группу фиксированных строк для каждой валюты + расчеты
    def rowNum = 0
    def dataRows = []
    sortedCurrency.each { currencyKey ->
        def rateTypes = currencyMap[currencyKey].grep()
        rateTypes.each { groupKeys ->
            def groupRows = getGroupRows(groupRowsMap, groupKeys, rowNum)
            dataRows.addAll(groupRows)
            // нумерации
            rowNum = groupRows[-1].rowNum
        }
    }

    updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
}

/**
 * Получить ключ для определения группы.
 *
 * <pre>
 * Перечень критериев сопоставимости:
 * 1. Кредитный рейтинг заёмщика    - по графе 8 источника  - 1-11, 12-22, 23-26, other
 * 2. Валюта кредита                - по графе 16 источника - id записи справочника 15
 * 3. Средневзвешенный срок кредита - по графе 15 источника - 1year, 1_5year, 5_10year, 10year
 * 4. Объем кредита                 - по графе 17 источника - 100, 100_1000, 1000
 * 5. Категория обеспечения кредита - по графе 23 источника - 1, 2, 3, other
 * 6. Тип процентной ставки         - по графе 18 источника - id записи справочника 72
 *
 * Пример: 1-11#23#1_5year#100#2#181049600
 *
 * Критерии 2 и 6 используется для группировки в отдельный блок (начальные строки из макета)
 * Критерии 1 и 5 используется для определения строки в блоке:
 *      '1-11' + '_' + '1' + 'count'  = 1-11_1count
 *      '1-11' + '_' + '1' + 'minmax' = 1-11_1minmax
 * Критерии 3 и 4 используется для определения столбца:
 *      'count' + '1_5year' + '100' = count1_5year100
 *      'min' + '1_5year' + '100'   = min1_5year100
 *      'max' + '1_5year' + '100'   = max1_5year100
 * </pre>
 *
 * @param row строка источника
 * @param source источник (необходим для формирования сообщения при проверках)
 */
String getKey(def row, Relation source) {
    // 1. Кредитный рейтинг заёмщика
    def creditRating = null
    def ratingName = getRefBookValue(604L, row.creditRating)?.NAME?.value
    def columnName = getColumnName(row, 'creditRating')
    def record603 = getRefBookRecord(603L, 'CREDIT_RATING', ratingName, getReportPeriodEndDate(), row.getIndex(), columnName, false)
    if (record603) {
        def className = record603?.CREDIT_QUALITY_CLASS?.value
        creditRating = getRefBookValue(601L, className)?.CREDIT_QUALITY_CLASS?.value
    }
    if (creditRating == null) {
        creditRating = null
    } else if ('1 КЛАСС' == creditRating) {
        creditRating = '1-11'
    } else if ('2 КЛАСС' == creditRating) {
        creditRating = '12-22'
    } else if ('3 КЛАСС' == creditRating) {
        creditRating = '23-26'
    } else {
        creditRating = 'other'
    }

    // 3. Средневзвешенный срок кредита
    def avgPeriod
    if (row.avgPeriod == null) {
        avgPeriod = null
    } else if (row.avgPeriod <= 1) {
        avgPeriod = '1year'
    } else if (row.avgPeriod <= 5) {
        avgPeriod = '1_5year'
    } else if (row.avgPeriod <= 10) {
        avgPeriod = '5_10year'
    } else {
        avgPeriod = '10year'
    }

    // 4. Объем кредита
    def credit = row.creditSum
    def code = getCurrencyCode(row)
    // если валюта не рубль, то перевести объем кредита в рубли
    if (row.currencyCode != null && code != null && code != 'RUR') {
        columnName = getColumnName(row, 'currencyCode')
        def record22 = getRefBookRecord(22L, 'CODE_LETTER', row.currencyCode.toString(), getReportPeriodEndDate(), row.getIndex(), columnName, false)
        if (record22) {
            credit = (record22?.RATE?.value ? credit * record22?.RATE?.value : null)
        } else {
            // 1. В справочнике «Курсы валют» не найден курс для заданной валюты на заданную дату
            def msg = "Форма-источника: «%s», Подразделение: «%s», Период: «%s», строка %d: Не найден курс валюты для «%s» на дату %s!"
            def formName = source.formType.name
            def departmentName = source.department.name
            def periodName = source.departmentReportPeriod.reportPeriod.name + ' ' + source.departmentReportPeriod.reportPeriod.taxPeriod.year
            def dateInStr = getReportPeriodEndDate()?.format('dd.MM.yyyy')
            logger.error(msg, formName, departmentName, periodName, row.getIndex(), code, dateInStr)
        }
    }
    def creditSum
    if (credit == null) {
        creditSum = null
    } else if (credit <= 100_000_000) {
        creditSum = '100'
    } else if (credit <= 1_000_000_000) {
        creditSum = '100_1000'
    } else {
        creditSum = '1000'
    }

    // 5. Категория обеспечения кредита
    def provideCategory = getRefBookValue(606L, row.provideCategory)?.NAME?.value
    if (provideCategory == null) {
        provideCategory = null
    } else if ('Полностью обеспеченный' == provideCategory) {
        provideCategory = '1'
    } else if ('Частично обеспеченный' == provideCategory) {
        provideCategory = '2'
    } else if ('Необеспеченный' == provideCategory) {
        provideCategory = '3'
    } else {
        provideCategory = 'other'
    }

    def tmp = (creditRating     + "#" +     // 1. Кредитный рейтинг заёмщика
            row.currencyCode    + "#" +     // 2. Валюта кредита
            avgPeriod           + "#" +     // 3. Средневзвешенный срок кредита
            creditSum           + "#" +     // 4. Объем кредита
            provideCategory     + "#" +     // 5. Категория обеспечения кредита
            row.rateType                    // 6. Тип процентной ставки
    )

    return tmp.toLowerCase()
}

def calcMin(def rows) {
    calcMinOrMax(rows, true)
}

def calcMax(def rows) {
    calcMinOrMax(rows, false)
}

/**
 * Посчитать min или max по группе строк источника.
 *
 * @param rows строки источни
 * @param isMin признак того что расчет для min
 */
def calcMinOrMax(def rows, def isMin) {
    def result
    def n = rows.size()
    if (n < 4) {
        // если строк меньше четырех, то найти минимум или максимум
        def row
        if (isMin) {
            row = rows.min { it.economyRate }
        } else {
            row = rows.max { it.economyRate }
        }
        result = row?.economyRate
    } else {
        def values = rows.collect { it.economyRate }.sort()
        double k = (isMin ? n / 4 : n * 0.75)
        def precision = k % 1
        if (precision == 0) {
            result = (values[k - 1] + values[k]) / 2
        } else {
            int index = (k - precision).longValue()
            result = values[index]
        }
    }
    return round(result, 2)
}

/**
 * Для сортировки валют. Порядок: USD, RUR, EUR, KZT, остальные валюты.
 *
 * @param value буквенный код валюты
 */
def getSortIndex(def value) {
    switch (value) {
        case 'USD': return 4
        case 'RUR': return 3
        case 'EUR': return 2
        case 'KZT': return 1
        default: return 0
    }
}

/**
 * Сформировать и заполнить группу строк текущей формы по данным из источника (группа строк источника для одной валюты).
 *
 * @param groupRowsMap мапа со группированными данными источника
 * @param groupKeys список ключей групп источника для одной валюты
 * @param rowNum нумерация
 *
 * @return группа строк текущей формы
 */
def getGroupRows(def groupRowsMap, def groupKeys, def rowNum) {
    def tmpKey = groupKeys[0]
    def tmpRows = groupRowsMap[tmpKey]
    def tmpRow = tmpRows[0]
    def isFix = isFix(tmpRow)
    def templateRows = getTemplateRows()

    // изменить надписи
    def header = templateRows.find { it.getAlias() == 'header' }
    header.fix = (isFix ? 'Фиксированная процентная ставка' : '"Плавающая" процентная ставка')
    def subHeader = templateRows.find { it.getAlias() == 'subHeader' }
    subHeader.fix = (tmpRow?.currencyCode ? getRefBookValue(15L, tmpRow.currencyCode)?.NAME?.value : null)

    // заполнить значения: количество, min, max
    groupKeys.each { key ->
        def rows = groupRowsMap[key]
        def criteria = key.split('#')

        // определить alias колонок
        def columnSubAlias = criteria[2] + criteria[3] // 1year / 1_5year / 5_10year / 10year + 100 / 100_1000 / 1000
        def columnCoumn = 'count' + columnSubAlias
        def columnMin = 'min' + columnSubAlias
        def columnMax = 'max' + columnSubAlias

        // определить alias строк
        def rowSubAlias = criteria[0] + '_' + criteria[4] // 1-11 / 12-22 / 23-26 + 1/2/3
        def aliasForCount = rowSubAlias + 'count'
        def aliasForMinMax = rowSubAlias + 'minmax'

        // количество
        def row = templateRows.find { it.getAlias() == aliasForCount }
        if (row) {
            row[columnCoumn] = rows.size()
        }
        // min, max
        row = templateRows.find { it.getAlias() == aliasForMinMax }
        if (row) {
            row[columnMin] = calcMin(rows)
            row[columnMax] = calcMax(rows)
        }
    }

    // графа 1
    def  numberedRows = templateRows.findAll { it.getAlias() != 'header' && it.getAlias() != 'subHeader' }
    numberedRows.each{ row ->
        row.rowNum = ++rowNum
    }

    // удалить алиасы строк
    templateRows.each { row ->
        row.setAlias(null)
    }

    return templateRows
}

@Field
FormTemplate formTemplate = null

/** Получить начальные строки макета. */
def getTemplateRows() {
    if (formTemplate == null) {
        formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    }
    return formTemplate?.clone()?.rows
}

def isFix(def row) {
    def rateType = (row.rateType ? getRefBookValue(72L, row.rateType)?.CODE?.value : null)
    return rateType == 'fix'
}

def getCurrencyCode(def row) {
    return (row.currencyCode ? getRefBookValue(15L, row.currencyCode)?.CODE_2?.value : null)
}

void deleteAllRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = []
    formDataService.saveCachedDataRows(formData, logger)
}