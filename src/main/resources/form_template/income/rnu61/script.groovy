package form_template.income.rnu61

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-61
 * Форма "(РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России», учёт которых требует применения метода начисления"
 * formTemplateId=352
 *
 * графа 1  - rowNumber
 * графа 2  - billNumber
 * графа 3  - creationDate
 * графа 4  - nominal
 * графа 5  - currencyCode
 * графа 6  - rateBRBill
 * графа 7  - rateBROperation
 * графа 8  - paymentStart
 * графа 9  - paymentEnd
 * графа 10 - interestRate
 * графа 11 - operationDate
 * графа 12 - sum70606
 * графа 13 - sumLimit
 * графа 14 - percAdjustment
 *
 * @author akadyrgulov
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
def editableColumns = ['billNumber', 'creationDate', 'nominal', 'currencyCode', 'paymentStart', 'paymentEnd',
        'interestRate', 'operationDate', 'sum70606']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'billNumber', 'creationDate', 'nominal', 'currencyCode', 'rateBRBill',
        'rateBROperation', 'paymentStart', 'paymentEnd', 'interestRate', 'operationDate', 'sum70606', 'sumLimit',
        'percAdjustment']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['percAdjustment']

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
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        def daysOfYear = getCountDays(reportPeriodService.getStartDate(formData.reportPeriodId).time)

        // номер последний строки предыдущей формы
        def index = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

        for (def row in dataRows) {
            // графа 1
            row.rowNumber = ++index
            // графа 6
            row.rateBRBill = calc6and7(row.currencyCode, row.creationDate)
            // графа 7
            row.rateBROperation = calc6and7(row.currencyCode, row.operationDate)
            // графа 12
            row.sum70606 = calc12(row)
            // графа 13
            row.sumLimit = calc13(row, daysOfYear)
            // графа 14
            row.percAdjustment = calc14(row)
        }
    }
    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}

def calcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.billNumber = 'Итого'
    totalRow.getCell('billNumber').colSpan = 12
    ['rowNumber', 'billNumber', 'percAdjustment'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Ресчет графы 6 и 7
def BigDecimal calc6and7(def currencyCode, def date) {
    if (currencyCode != null && date != null) {
        def rate = 1
        if (!isRubleCurrency(currencyCode)) {
            rate = getRate(date, currencyCode)
        }
        return rate
    } else {
        return null
    }
}

// Ресчет графы 12
def BigDecimal calc12(def row) {
    // TODO вопрос к заказчику
    def val = row.sum70606
    if (row.currencyCode != null && isRubleCurrency(row.currencyCode)) {
    } else {
    }
    return val
}

// Ресчет графы 13
def BigDecimal calc13(def DataRow<Cell> row, def daysOfYear) {
    row.getCell('sumLimit').setEditable(false);
    row.getCell('sumLimit').setStyleAlias(null);
    if (row.paymentEnd == null || row.creationDate == null) {
        return null
    }
    if (getCountDays(row.creationDate) != getCountDays(row.paymentEnd)) {
        row.getCell('sumLimit').setStyleAlias("Редактируемая");
        row.getCell('sumLimit').setEditable(true);
        return row.sumLimit
    }
    if (row.sum70606 == null && isRubleCurrency(row.currencyCode)) {
        if (row.operationDate != null && row.nominal != null
                && row.interestRate != null && row.rateBROperation != null) {
            def date = (row.operationDate < row.paymentEnd) ? row.operationDate : row.paymentEnd
            return ((row.nominal * row.interestRate / 100 * (date - row.creationDate)) / daysOfYear)
                    .setScale(2, RoundingMode.HALF_UP) * row.rateBROperation
        }
    }
    return null
}

// Ресчет графы 14
def BigDecimal calc14(def row) {
    if (row.sum70606 != null) {
        if (row.sumLimit != null && row.sum70606 > row.sumLimit) {
            return row.sum70606 - row.sumLimit
        } else {
            return null
        }
    } else if (row.nominal != null && row.rateBRBill != null && row.rateBROperation != null) {
        return row.nominal * (row.rateBRBill - row.rateBROperation)
        // TODO вопрос к заказчику "второй строкой записать..."
    }
    return null
}
// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['rateBRBill', 'rateBROperation', 'sum70606', 'sumLimit', 'percAdjustment']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // Инвентарные номера
    def List<String> invList = new ArrayList<String>()
    // Отчетная дата
    def reportDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    //Начальная дата отчетного периода
    def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def daysOfYear = getCountDays(reportDateStart)

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

        // Проверка на уникальность поля «№ пп»
        if (++i != row.rowNumber) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // Проверка на уникальность поля «инвентарный номер»
        if (invList.contains(row.billNumber)) {
            logger.error(errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invList.add(row.billNumber)
        }

        // 2. Проверка даты совершения операции и границ отчетного периода
        if (row.operationDate < reportDateStart || row.operationDate > reportDate) {
            logger.error(errorMsg + "Дата совершения операции вне границ отчетного периода!")
        }

        // 4. Проверка на нулевые значения
        if (row.sum70606 == 0 && row.sumLimit == 0 && row.percAdjustment == 0) {
            logger.error(errorMsg + "Все суммы по операции нулевые!")
        }

        // 5. Арифметические проверки
        needValue['rateBRBill'] = calc6and7(row.currencyCode, row.creationDate)
        needValue['rateBROperation'] = calc6and7(row.currencyCode, row.operationDate)
        needValue['sum70606'] = calc12(row)
        needValue['sumLimit'] = calc13(row, daysOfYear)
        needValue['percAdjustment'] = calc14(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, 'currencyCode')
    }

    // 5. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

// Получить курс банка России на указанную дату.
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords(date != null ? date : new Date(), null, "CODE_NUMBER = $value", null);
    return res.getRecords().get(0).RATE.numberValue
}

def getCountDays(def Date date) {
    def Calendar calendar = Calendar.getInstance()
    calendar.setTime(date)
    return (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
}