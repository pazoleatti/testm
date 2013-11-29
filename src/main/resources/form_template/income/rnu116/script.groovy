package form_template.income.rnu115

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * ((РНУ-116) Регистр налогового учёта доходов и расходов, возникающих в связи с применением в конверсионных сделках
 * с Взаимозависимыми лицами и резидентами оффшорных зон курса, не соответствующих рыночному уровню (368)
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
        'marketPrice']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'transactionKind', 'transactionNumber', 'transactionDate', 'transactionDateEnd', 'inn',
        'country', 'contractor', 'transactionType', 'currencyCode', 'currencyVolume', 'currencyCodeLiabilities',
        'currencyVolumeSale', 'price', 'priceRequest', 'priceLiability', 'request', 'liability']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['currencyVolume', 'currencyVolumeSale', 'request', 'outcome']

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
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
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
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            // графа 1
            row.number = ++index

            // Расчет полей зависимых от справочников
            def map = getRefBookValue(9, row.inn)
            row.contractor = map?.NAME?.stringValue
            row.country = map?.COUNTRY?.referenceValue

            // TODO пока нет справочника
            transactionKind = 'премия по опциону' //getRefBookValue(-1, row.transactionKind).ATTRIBUTE.stringValue

            row.priceRequest = calcPrice(row, 'currencyCode')
            row.priceLiability = calcPrice(row, 'currencyCodeLiabilities')

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
def calcPrice(def row, def currency) {
    if (row.getCell(currency).value == null || row.transactionDateEnd == null) {
        return null
    }
    return formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
            'CODE_NUMBER', '' + row.getCell(currency).value, row.transactionDateEnd,
            row.getIndex(), getColumnName(row, currency), logger, true).RATE.numberValue
}

// Расчет графы 17
def calcRequest(def row, def transactionKind) {
    if (transactionKind == null || row.currencyVolume == null || row.priceRequest == null
            || transactionKind != 'премия по опциону') {
        return null
    }
    return row.currencyVolume * row.priceRequest
}

// Расчет графы 18
def calcLiability(def row, def transactionKind) {
    if (row.transactionKind == null || row.currencyVolumeSale == null || row.priceLiability == null
            || transactionKind != 'премия по опциону') {
        return null
    }
    return -1 * row.currencyVolumeSale * row.priceLiability
}

// Расчет графы 19, 20
def calcIncomeOutcome(def row, def income) {
    if (row.request == null || row.liability == null) {
        return null
    }
    def sum = row.request + row.liability
    return sum * income > 0 ? sum : 0
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

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['request', 'liability', 'income', 'outcome', 'incomeDeviation', 'outcomeDeviation'
            , 'priceRequest', 'priceLiability']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def index
    def errorMsg

    for (def row : dataRows) {
        if (row.getAlias() != null) {
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
        BigDecimal
        // 3 Арифметические проверки расчета неитоговых граф
        // TODO пока нет справочника
        transactionKind = 'премия по опциону' //getRefBookValue(-1, row.transactionKind).ATTRIBUTE.stringValue
        needValue['priceRequest'] = calcPrice(row, 'currencyCode')
        needValue['priceLiability'] = calcPrice(row, 'currencyCodeLiabilities')
        needValue['request'] = calcRequest(row, transactionKind)
        needValue['liability'] = calcLiability(row, transactionKind)
        needValue['income'] = calcIncomeOutcome(row, 1)
        needValue['outcome'] = calcIncomeOutcome(row, -1)
        needValue['incomeDeviation'] = calcIncomeOutcomeDeviation(row, 1, transactionKind)
        needValue['outcomeDeviation'] = calcIncomeOutcomeDeviation(row, -1, transactionKind)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        // TODO пока нет справочника
        // checkNSI(-1, row, 'transactionKind')
        checkNSI(10, row, 'country')
        checkNSI(9, row, 'contractor')
        checkNSI(15, row, 'currencyCode')
        checkNSI(15, row, 'currencyCodeLiabilities')
    }

    // 4 Арифметические проверки расчета итоговой графы
    checkTotalSum(dataRows, totalColumns, logger, true)
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
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}