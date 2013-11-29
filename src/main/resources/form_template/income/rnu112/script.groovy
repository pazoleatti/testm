package form_template.income.rnu112

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-112) Регистр налогового учета сделок РЕПО и сделок займа ценными бумагами (374)
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
def editableColumns = ['check', 'transactionNumber', 'contractor', 'dateFirstPart', 'dateSecondPart',
        'incomeDate', 'outcomeDate', 'rateREPO']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'check', 'transactionNumber', 'contractor', 'country', 'dateFirstPart', 'dateSecondPart',
        'incomeDate', 'outcomeDate', 'rateREPO', 'maxRate', 'outcome', 'marketRate', 'income', 'sum']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['outcomeDate', 'rateREPO', 'marketRate', 'income']

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

        // "Да"
        def recYesId = getRecordId(38, 'CODE', '1', -1, null, true)

        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            // графа 1
            row.number = ++index

            // Расчет полей зависимых от справочников
            def map = getRefBookValue(9, row.contractor)
            row.country = map?.COUNTRY?.referenceValue

            row.maxRate = calcMaxRate(row)
            row.outcome = calcOutcome(row)
            row.marketRate = calcMarketRate(row, recYesId)
            row.income = calcIncome(row, recYesId)
            row.sum = calcSum(row)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)

    dataRowHelper.save(dataRows)
}

// Расчет графы 11
def BigDecimal calcMaxRate(def row) {
    // TODO вопросы к заказчику
    if (row.outcomeDate == 0 || row.dateFirstPart == null) {
        return null
    }
    currencyCode = ''
    rate = 0
    if (currencyCode == '810') {
        return rate * 1.8
    } else {
        return rate * 1.8
    }
}

// Расчет графы 12
def BigDecimal calcOutcome(def row) {
    if (row.outcomeDate == null || row.rateREPO == null || row.maxRate == null) {
        return null
    }
    if (row.outcomeDate > 0) {
        return (row.rateREPO > row.maxRate) ? (row.outcomeDate / (row.rateREPO * row.maxRate)) : row.outcomeDate
    } else if (row.outcomeDate == 0) {
        return 0
    }
    return null
}

// Расчет графы 13
def BigDecimal calcMarketRate(def row, def recYesId) {
    if (row.incomeDate != null && row.check == recYesId) {
        // TODO вопрос к заказчику
        return row.marketRate
    }
    return null
}

// Расчет графы 14
def BigDecimal calcIncome(def row, def recYesId) {
    if (row.marketRate == null || row.rateREPO == null || row.incomeDate == null || row.check != recYesId) {
        return null
    }
    if (row.incomeDate > 0) {
        return (row.marketRate > row.rateREPO) ? (row.incomeDate / (row.rateREPO * row.marketRate)) : row.incomeDate
    }
    return 0
}

// Расчет графы 15
def BigDecimal calcSum(def row) {
    if (row.incomeDate != null && row.check == recYesId && row.income != null) {
        return row.income - row.incomeDate
    }
    return null
}

// Логические проверки
// TODO проверки логические не описаны вовсе
void logicCheck() {

}


def calcTotalRow(def dataRows) {

}