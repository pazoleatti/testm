package form_template.income.rnu115

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.math.RoundingMode

/**
 * (РНУ-115) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению
 * Межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон Процентных ставок,
 * не соответствующих рыночному уровню (369)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
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
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
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

// Редактируемые атрибуты
@Field
def editableColumns = ['transactionKind', 'transactionNumber', 'transactionDate', 'transactionDateEnd', 'inn',
        'transactionType', 'currencyCode', 'currencyVolume', 'currencyCodeLiabilities', 'currencyVolumeSale', 'price',
        'marketPrice'
        // TODO временно ручной ввод (в аналитике нет алгоритма расчета)
        , 'priceRequest', 'priceLiability'
]

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'transactionKind', 'transactionNumber', 'transactionDate', 'transactionDateEnd', 'inn',
        'country', 'contractor', 'transactionType', 'currencyCode', 'currencyVolume', 'currencyCodeLiabilities',
        'currencyVolumeSale', 'price', 'priceRequest', 'priceLiability', 'request', 'liability']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        // TODO вернуть после проверки
        // throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // номер последний строки предыдущей формы
        def index = getPrevRowNumber()

        for (row in dataRows) {
            // графа 1
            row.number = ++index

            // Расчет полей зависимых от справочников
            def map = getRefBookValue(9, row.inn)
            row.contractor = map?.NAME?.stringValue
            row.country = map?.COUNTRY?.referenceValue

            // TODO пока нет справочника
            transactionKind = 'премия по опциону' //getRefBookValue(-1, row.transactionKind).ATTRIBUTE.stringValue

            // TODO временно ручной ввод (в аналитике нет алгоритма расчета)
            //row.priceRequest = calcPrice(row, row.currencyCode, transactionKind)
            //row.priceLiability = calcPrice(row, row.currencyCodeLiabilities, transactionKind)

            row.request = calcRequest(row, transactionKind)
            row.liability = calcLiability(row, transactionKind)
            row.income = calcIncomeOutcome(row, 1)
            row.outcome = calcIncomeOutcome(row, -1)
            row.incomeDeviation = calcIncomeOutcomeDeviation(row, 1, transactionKind)
            row.outcomeDeviation = calcIncomeOutcomeDeviation(row, -1, transactionKind)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)

    dataRowHelper.save(dataRows)
}

// Расчет графы 15, 16
def calcPrice(def row, def code, def transactionKind) {
    if (transactionKind == null || row.transactionDateEnd == null || code == null) {
        return null
    }
    Date date = new Date()
    if (transactionKind != 'премия по опциону') {
        date = row.transactionDateEnd
    }
    // TODO временно ручной ввод (в аналитике нет алгоритма расчета)
    return "курс валюты code на дату date"
}

// Расчет графы 17
def calcRequest(def row, def transactionKind) {
    if (transactionKind == null || row.currencyVolume == null || row.priceRequest == null
            || transactionKind != 'премия по опциону') {
        return null
    }
    return (row.currencyVolume * row.priceRequest).setScale(2, RoundingMode.HALF_UP)
}

// Расчет графы 18
def calcLiability(def row, def transactionKind) {
    if (row.transactionKind == null || row.currencyVolumeSale == null || row.priceLiability == null
            || transactionKind != 'премия по опциону') {
        return null
    }
    return (-1 * row.currencyVolumeSale * row.priceLiability).setScale(2, RoundingMode.HALF_UP)
}

// Расчет графы 19, 20
def calcIncomeOutcome(def row, def income) {
    if (row.request == null || row.liability == null) {
        return null
    }
    def sum = row.request + row.liability
    return sum * income > 0 ? sum.setScale(2, RoundingMode.HALF_UP) : 0.00
}

// Расчет графы 22, 23
def calcIncomeOutcomeDeviation(def row, def incomeMode, def transactionKind) {
    def inoutcome = incomeMode == 1 ? row.income : row.outcome

    if (inoutcome == null || row.transactionKind == null || row.transactionType == null || row.price == null
            || row.marketPrice == null || row.currencyVolumeSale == null || row.priceRequest == null ||
            row.priceLiability == null || row.currencyVolume == null) {
        return null
    }

    if (inoutcome == 0) {
        return 0
    } else {
        if (transactionKind == 'кассовая сделка' || transactionKind == 'кассовая сделка') {
            def transactionType = getRefBookValue(16, row.transactionType).TYPE.stringValue
            if (transactionType == 'продажа' && row.price >= row.marketPrice) {
                return row.price >= row.marketPrice ? 0 : row.currencyVolumeSale * (row.marketPrice - row.price) * row.priceRequest
            }
            if (transactionType == 'покупка' && row.price <= row.marketPrice) {
                return row.price <= row.marketPrice ? 0 : row.currencyVolume * (row.price - row.marketPrice) * row.priceLiability
            }
        } else if (transactionKind != 'премия по опциону') {
            if (inoutcome > 0 && row.price * incomeMode > row.marketPrice) {
                return 0
            }
            if (row.price * incomeMode < row.marketPrice) {
                return (row.marketPrice - row.price) * (incomeMode == 1 ? row.priceRequest : row.priceLiability)
            }
        }
    }
    return null
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = getPrevRowNumber()

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    // TODO priceRequest и priceLiability пока заполняются в ручную (вопрос по аналитике)
    def arithmeticCheckAlias = ['request', 'liability', 'income', 'outcome', 'incomeDeviation', 'outcomeDeviation']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // суммы строки общих итогов
    def totalSums = [:]
    // графы для которых надо вычислять итого
    def totalColumns = ['currencyVolume', 'currencyVolumeSale', 'request', 'outcome']
    def totalRow = null

    def index
    def errorMsg

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            totalRow = row
            continue
        }
        index = row.getIndex()
        errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 3 Арифметические проверки
        // TODO пока нет справочника
        transactionKind = 'премия по опциону' //getRefBookValue(-1, row.transactionKind).ATTRIBUTE.stringValue
        needValue['request'] = calcRequest(row, transactionKind)
        needValue['liability'] = calcLiability(row, transactionKind)
        needValue['income'] = calcIncomeOutcome(row, 1)
        needValue['outcome'] = calcIncomeOutcome(row, -1)
        needValue['incomeDeviation'] = calcIncomeOutcomeDeviation(row, 1, transactionKind)
        needValue['outcomeDeviation'] = calcIncomeOutcomeDeviation(row, -1, transactionKind)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 4. Арифметическая проверка итоговой строки
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }
    }

    if (totalRow != null) {
        // 5. Арифметическая проверка итоговой строки
        for (def alias : totalColumns) {
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                def name = getColumnName(totalRow, alias)
                logger.error("Итоговые значения рассчитаны неверно в графе «$name»!")
            }
        }
    }
}

// Получить сумму столбца
def getSum(def dataRows, def columnAlias) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            sum += row.getCell(columnAlias).getValue() ?: 0
        }
    }
    return sum
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.transactionNumber = 'Итого'
    totalRow.getCell('transactionNumber').colSpan = 9
    totalRow.getCell('fix').colSpan = 2
    totalRow.getCell('price').colSpan = 3
    totalRow.getCell('liability').colSpan = 2
    totalRow.getCell('marketPrice').colSpan = 3

    ['number', 'transactionNumber', 'fix', 'price', 'currencyVolume', 'currencyVolumeSale', 'request', 'outcome',
            'liability', 'marketPrice'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    ['currencyVolume', 'currencyVolumeSale', 'request', 'outcome'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias))
    }
    return totalRow
}

// Получить значение "Номер по порядку" из формы предыдущего периода
def int getPrevRowNumber() {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // получить номер последний строки предыдущей формы если текущая форма не первая в этом году
    if (reportPeriod != null && reportPeriod.order == 1) {
        return 0
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
    if (prevDataRows != null && !prevDataRows.isEmpty()) {
        // пропустить последнюю итоговую строку
        return prevDataRows.get(prevDataRows.size - 2).number
    }
    return 0
}