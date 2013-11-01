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
 *      - http://jira.aplana.com/browse/SBRFACCTAX-4849 - РНУ-56. Графа 13 при значении null.
 *      - http://jira.aplana.com/browse/SBRFACCTAX-4853 - РНУ-56. Обязательные поля.
 *      - проверить проверку предыдущих форм после того как в 0.3.5 поправят получение предыдущего периода (после мержа с 0.3.2)
 *      - после SBRFACCTAX-4845 сделать логическую проверку 6 и 7
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 2  - bill
// графа 3  - buyDate
// графа 4  - currency - атрибут 64 - NAME - "Код валюты. Цифровой", справочник 15 "Общероссийский классификатор валют"
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

    if (dataRows.isEmpty()) {
        return
    }

    // Удаление подитогов
    deleteAllAliased(dataRows)

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // номер последний строки предыдущей формы
    def index = getPrevRowNumber()

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

    // TODO Levykin Переписать
    // добавить строку "итого" (графа 13, 15)
    // def totalRow = calcTotalRow(dataRows)
    // dataRowHelper.insert(totalRow, index + 1)

    dataRowHelper.save(dataRows)
}

// Расчет графы 8
def calcTermDealBill(def row) {
    if (row.buyDate == null || row.maturity == null) {
        return null
    }
    return (row.maturity - row.buyDate + 1)
}

// Расчет графы 9
def calcPercIncome(def row) {
    if (row.nominal == null || row.price == null) {
        return null
    }
    return row.nominal - row.price
}

// Расчет графы 12
def calcDiscountInCurrency(def row) {
    if (row.sum == null || row.price == null) {
        return null
    }
    return row.sum - row.price
}

// Расчет графы 13
def calcDiscountInRub(def row) {
    if (row.discountInCurrency != null) {
        if (row.currency != null && !isRubleCurrency(row.currency)) {
            def record = null
            if (row.implementationDate != null) {
                // значение поля «Курс валюты» справочника «Курсы валют» на дату из «Графы 10»
                record = getRecordId(22, 'CODE_NUMBER', "${row.currency}", row.number?.intValue(), getColumnName(row, 'currency'),
                        row.implementationDate)
            }
            if (record != null) {
                return (row.discountInCurrency * getRefBookValue(22, record)?.RATE?.numberValue)?.setScale(2, RoundingMode.HALF_UP)
            }
        } else {
            return row.discountInCurrency
        }
    }
    return null
}

// Расчет графы 14
def calcSumIncomeinCurrency(def row, def startDate, def endDate) {
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
def calcSumIncomeinRuble(def row, def endDate) {
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
        // TODO (Ramil Timerbaev) http://jira.aplana.com/browse/SBRFACCTAX-4849 - РНУ-56. Графа 13 при значении null.
        if (row.discountInRub == null) {
            return null
        }
        tmp = row.discountInRub - getCalcPrevColumn(row.bill, 'sumIncomeinRuble', formData.reportPeriodId)
    }

    return tmp
}

/**
 * Логические проверки
 */
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    def i = getPrevRowNumber()

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['termDealBill', 'percIncome', 'discountInCurrency', 'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    //def colNames = []

    // суммы строки общих итогов
    def totalSums = [:]

    // графы для которых надо вычислять итого (графа 13, 15)
    def totalColumns = ['discountInRub', 'sumIncomeinRuble']

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Векселя
    def List<String> billsList = new ArrayList<String>()

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
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 7. Проверка корректности значения в «Графе 3»
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 8. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
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

        // 11. Проверка итоговых значений по всей форме (графа 13, 15)
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }

        // Проверки соответствия НСИ
        checkNSI(15, row, 'currency') // Проверка кода валюты
    }

    if (totalRow != null) {
        // 11. Проверка итогового значений по всей форме (графа 13, 15)
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.info('======= t = ' + totalSums[alias]) // TODO (Ramil Timerbaev)
                logger.info('======= c = ' + totalRow.getCell(alias).getValue()) // TODO (Ramil Timerbaev)
                def name = getColumnName(totalRow, 'alias')
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
            sum += row.getCell(columnAlias).numberValue ?: 0
        }
    }
    return sum
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    def record = getRefBookValue(15, currencyCode)
    return record != null && record.CODE?.stringValue == '810'
}

// Получить курс банка России на указанную дату
def getRate(def Date date, def value) {
    return getRecordId(22, 'CODE_NUMBER', "$value", -1, null, date ?: new Date(), true)?.RATE?.numberValue
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
                sum += (row.getCell(sumColumnName).numberValue ?: 0)
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

def calcTotalRow(def dataRows) {
    // итого (графа 13, 15)
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    totalRow.getCell('bill').colSpan = 11
    ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'percIncome',
            'implementationDate', 'sum', 'discountInCurrency',
            'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    ['discountInRub', 'sumIncomeinRuble'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias))
    }
    return totalRow
}

/** Получить значение "Номер по порядку" из формы предыдущего периода. */
def getPrevRowNumber() {
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // получить номер последний строки предыдущей формы если текущая форма не первая в этом году
    if (reportPeriod != null && reportPeriod.order == 1) {
        return 0
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def prevDataRows = (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
    if (prevDataRows != null && !prevDataRows.isEmpty()) {
        // пропустить последнюю итоговую строку
        return prevDataRows[prevDataRows.size - 2].number
    }
    return 0
}