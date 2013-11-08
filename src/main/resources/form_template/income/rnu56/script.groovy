package form_template.income.rnu56

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode
/**
 * Форма "(РНУ-56) Регистр налогового учёта процентного дохода по дисконтным векселям сторонних эмитентов"
 *
 * TODO:
 *      - http://jira.aplana.com/browse/SBRFACCTAX-4845 - РНУ-56. Алгоритм заполнения графы 14 и 15.
 *      - http://jira.aplana.com/browse/SBRFACCTAX-4853 - РНУ-56. Обязательные поля.
 *      - после SBRFACCTAX-4845 сделать логическую проверку 6 и 7
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency
// графа 5  - nominal
// графа 6  - price
// графа 7  - maturity
// графа 8  - termDealBill
// графа 9  - percIncome
// графа 10 - implementationDate
// графа 11 - sum
// графа 12 - discountInCurrency
// графа 13 - discountInRub
// графа 14 - sumIncomeinCurrency
// графа 15 - sumIncomeinRuble

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
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
def editableColumns = ['bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'implementationDate', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'termDealBill',
        'sumIncomeinCurrency', 'sumIncomeinRuble']

// Атрибуты для итогов
@Field
def totalColumns = ['discountInRub', 'sumIncomeinRuble']

// Все атрибуты
@Field
def allColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'price', 'maturity', 'termDealBill', 'percIncome',
        'implementationDate', 'sum', 'discountInCurrency', 'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
//def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
//                boolean required = true) {
//    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
//            currentDate, rowIndex, cellName, logger, required)
//}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    if (!isBalancePeriod && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        // TODO потом поменять после проверки
        // throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
        def formName = formData.getFormType().getName()
        logger.error("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Удаление итогов
    deleteAllAliased(dataRows)

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Номер последний строки предыдущей формы
    def index = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

    for (row in dataRows) {
        // графа 1
        row.number = ++index
        // графа 8
        row.termDealBill = calcTermDealBill(row)
        // графа 9
        row.percIncome = calcPercIncome(row)
        // графа 12
        row.discountInCurrency = calcDiscountInCurrency(row)
        // графа 13
        row.discountInRub = calcDiscountInRub(row)
        // графа 14
        row.sumIncomeinCurrency = calcSumIncomeinCurrency(row, startDate, endDate)
        // графа 15
        row.sumIncomeinRuble = calcSumIncomeinRuble(row, endDate)
    }

    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))
    dataRowHelper.save(dataRows)
}

// Расчет графы 8
BigDecimal calcTermDealBill(def row) {
    if (row.buyDate == null || row.maturity == null) {
        return null
    }
    return row.maturity - row.buyDate + 1
}

// Расчет графы 9
BigDecimal calcPercIncome(def row) {
    if (row.nominal == null || row.price == null) {
        return null
    }
    return row.nominal - row.price
}

// Расчет графы 12
BigDecimal calcDiscountInCurrency(def row) {
    if (row.sum == null || row.price == null) {
        return null
    }
    return row.sum - row.price
}

// Расчет графы 13
BigDecimal calcDiscountInRub(def row) {
    if (row.discountInCurrency != null) {
        if (row.currency != null && !isRubleCurrency(row.currency)) {
            def map = null
            if (row.implementationDate != null) {
                // значение поля «Курс валюты» справочника «Курсы валют» на дату из «Графы 10»
                map = getRecord(22, 'CODE_NUMBER', "${row.currency}", row.number?.intValue(),
                        getColumnName(row, 'currency'), row.implementationDate)
            }
            if (map != null) {
                return (row.discountInCurrency * map?.RATE?.numberValue)?.setScale(2,
                        RoundingMode.HALF_UP)
            }
        } else {
            return row.discountInCurrency
        }
    }
    return null
}

// Расчет графы 14
BigDecimal calcSumIncomeinCurrency(def row, def startDate, def endDate) {
    if (startDate == null || endDate == null || row.implementationDate == null) {
        return null
    }
    def tmp
    if ((row.implementationDate < startDate || row.implementationDate > endDate) && row.sum == null) {
        if (row.percIncome == null || row.termDealBill == null) {
            return null
        }
        // Количество дней владения векселем в отчетном периоде
        def countsDays = !row.buyDate.before(startDate) ? endDate - row.buyDate : endDate - startDate
        if (countsDays == 0) {
            return null
        }
        tmp = (row.percIncome / row.termDealBill * countsDays).setScale(2, RoundingMode.HALF_UP)
    } else {
        def sum = getCalcPrevColumn(row.bill, 'sumIncomeinCurrency', formData.reportPeriodId)
        if (row.sum != null) {
            if (row.discountInCurrency == null) {
                return null
            }
            tmp = row.discountInCurrency - sum
        } else {
            if (row.percIncome == null) {
                return null
            }
            tmp = row.percIncome - sum
        }
    }
    return tmp
}

// Расчет графы 15
BigDecimal calcSumIncomeinRuble(def row, def endDate) {
    def tmp
    if (row.sum != null) {
        if (!isRubleCurrency(row.currency)) {
            if (row.sumIncomeinCurrency == null) {
                return null
            }
            tmp = (row.sumIncomeinCurrency * getRate(endDate, row.currency)).setScale(2, RoundingMode.HALF_UP)
        } else {
            tmp = row.sumIncomeinCurrency
        }
    } else {
        if (row.discountInRub == null) {
            return null
        }
        tmp = row.discountInRub - getCalcPrevColumn(row.bill, 'sumIncomeinRuble', formData.reportPeriodId)
    }

    return tmp
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['termDealBill', 'percIncome', 'discountInCurrency', 'discountInRub',
            'sumIncomeinCurrency', 'sumIncomeinRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Векселя
    def List<String> billsList = new ArrayList<String>()

   for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка даты приобретения и границ отчетного периода
        if (endDate != null && row.buyDate != null && row.buyDate.after(endDate)) {
            logger.error(errorMsg + 'Дата приобретения вне границ отчетного периода!')
        }

        // 3. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
        if (++i != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 4. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            logger.error(errorMsg + 'Повторяющееся значения в графе «Вексель»')
        } else {
            billsList.add(row.bill)
        }

        // 5. Проверка на нулевые значения (графа 12..15)
        if (row.discountInCurrency == 0 &&
                row.discountInRub == 0 &&
                row.sumIncomeinCurrency == 0 &&
                row.sumIncomeinRuble == 0) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 6. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 14 и графы 15
        // TODO Получить РНУ-56 за прошлые отчетные периоды (вопрос к аналитикам)

        // 7. Проверка корректности значения в «Графе 3»
        // TODO Получить РНУ-56 за прошлые отчетные периоды (вопрос к аналитикам)

        // 8. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0
                || row.discountInRub != 0)) {
            logger.error(errorMsg + 'Расчёт дисконта некорректен!')
        }

        // 9. Проверка на неотрицательные значения
        if (row.discountInCurrency != null && row.discountInCurrency < 0) {
            logger.error(errorMsg + "Значение графы «${row.getCell('discountInCurrency').column.name}» отрицательное!")
        }
        if (row.discountInRub != null && row.discountInRub < 0) {
            logger.error(errorMsg + "Значение графы «${row.getCell('discountInRub').column.name}» отрицательное!")
        }

        // 10. Арифметические проверки граф 8, 9, 12-15
        needValue['termDealBill'] = calcTermDealBill(row)
        needValue['percIncome'] = calcPercIncome(row)
        needValue['discountInCurrency'] = calcDiscountInCurrency(row)
        needValue['discountInRub'] = calcDiscountInRub(row)
        needValue['sumIncomeinCurrency'] = calcSumIncomeinCurrency(row, startDate, endDate)
        needValue['sumIncomeinRuble'] = calcSumIncomeinRuble(row, endDate)

        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, 'currency') // Проверка кода валюты
    }

    // 11. Арифметические проверки итогов
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    def record = getRefBookValue(15, currencyCode)
    return record != null && record.CODE?.stringValue == '810'
}

// Получить курс банка России на указанную дату
def getRate(def Date date, def value) {
    return getRecord(22, 'CODE_NUMBER', "$value", -1, null, date ?: new Date(), true)?.RATE?.numberValue
}

// TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4845 - РНУ-56. Алгоритм заполнения графы 14 и 15.
/**
 * Получить сумму граф 14 или 15 из РНУ-56 предыдущих отчетных периодов...
 * <ol>
 * <li> получить рну предыдущего отчетного периода
 * <li> отобрать строки, для которых значение графы 2 (вексель) равно <b>bill</b> и графа 3 (дата приобретения) входит в [предыдущий] отчетный период
 * <li> просуммировать значения графы <b>sumColumnName</b> отобранных строк по п.2, потом выход
 * <li> если строки не найдены, то получить рну предпредыдущего отчетного периода и (перейти в п.1) и т.д. пока есть предыдущие периоды.
 * </ol>
 *
 * @param bill значение векселя
 * @param sumColumnName алиас графы для суммирования
 */
def getCalcPrevColumn(def bill, def sumColumnName, def reportPeriodId) {
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def sum = 0
    if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
        def prevDataRowHelper = formDataService.getDataRowHelper(prevFormData)
        def prevDataRows = prevDataRowHelper.getAllCached()
        def startDate = reportPeriodService.getStartDate(prevFormData.reportPeriodId).time
        def endDate = reportPeriodService.getEndDate(prevFormData.reportPeriodId).time
        def find = false
        for (def row : prevDataRows) {
            if (row.bill == bill && row.buyDate >= startDate && row.buyDate <= endDate) {
                sum += row.getCell(sumColumnName).value ?: 0
                find = true
            }
        }
        if (find) {
            return sum
        } else {
            return getCalcPrevColumn(bill, sumColumnName, prevFormData.reportPeriodId)
        }
    }
    return 0
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}
