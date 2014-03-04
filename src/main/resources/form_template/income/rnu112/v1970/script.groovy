package form_template.income.rnu112.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-112) Регистр налогового учета сделок РЕПО и сделок займа ценными бумагами
 * formTemplateId=374
 *
 * TODO:
 *      - расчет графы 11 не доделан, потому что не доописан в чтз
 *      - логические проверки не доописаны
 *
 * @author Stanislav Yasinskiy
 */

// графа    - fix
// графа 1  - number
// графа 2  - check
// графа 3  - transactionNumber
// графа 4  - contractor            атрибут 32 NAME «Наименование организации», справочник 9 «Организации - участники контролируемых сделок»
// графа 5  - country               зависит от графы 4, атрибут 33 COUNTRY «Страна регистрации», справочника 9 «Организации - участники контролируемых сделок»
// графа 6  - dateFirstPart
// графа 7  - dateSecondPart
// графа 8  - incomeDate
// графа 9  - outcomeDate
// графа 10 - rateREPO
// графа 11 - maxRate
// графа 12 - outcome
// графа 13 - marketRate
// графа 14 - income
// графа 15 - sum

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
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'check', 'transactionNumber', 'contractor', 'country', 'dateFirstPart',
        'dateSecondPart', 'incomeDate','outcomeDate', 'rateREPO', 'maxRate', 'outcome', 'marketRate', 'income', 'sum']

// Редактируемые атрибуты (графа 2..4, 6..10, 13)
@Field
def editableColumns = ['check', 'transactionNumber', 'contractor', 'dateFirstPart', 'dateSecondPart', 'incomeDate',
        'outcomeDate', 'rateREPO', 'marketRate']

// Проверяемые на пустые значения атрибуты (графа 1..7, 15)
@Field
def nonEmptyColumns = ['number', 'check', 'transactionNumber', 'contractor', 'dateFirstPart',
        'dateSecondPart', 'sum']

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['check', 'transactionNumber']

// Сумируемые колонки в фиксированной с троке (графа 9, 10, 13, 14, 15)
@Field
def totalColumns = ['outcomeDate', 'rateREPO', 'marketRate', 'income', 'sum']

// Текущая дата
@Field
def currentDate = new Date()

// Отчетная дата
@Field
def reportDay = null

// Дата начала отчетного периода
@Field
def startDate = null

@Field
def date01_01_2010 = new Date(1262286000000)
@Field
def date01_01_2011 = new Date(1293822000000)
@Field
def date01_01_2013 = new Date(1356980400000)
@Field
def date31_12_2012 = new Date(1356894000000)
@Field
def date31_12_2013 = new Date(1388430000000)

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod = null

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период
void prevPeriodCheck() {
    // Проверка только для первичных
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        return
    }
    if (!isBalancePeriod() && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.formType.name
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

        // отсортировать/группировать
        sortRows(dataRows, groupColumns)

        // обновить индексы строк
        dataRows.eachWithIndex { row, i ->
            row.setIndex(i + 1)
        }

        // номер последний строки предыдущей формы
        def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            // графа 1
            row.number = ++index
            // графа 11
            row.maxRate = calcMaxRate(row)
            // графа 12
            row.outcome = calcOutcome(row)
            // графа 14
            row.income = calcIncome(row)
            // графа 15
            row.sum = calcSum(row)
        }
    }

    // промежуточные итоги для "+"
    def rows = getRowsByCheck(dataRows, '+')
    def checkTotalRow = getTotalRow(rows, 'checkTotal', 'Итого по сделкам, подлежащим проверке')
    def index = (rows.isEmpty() ? 0 : rows[rows.size() - 1].getIndex())
    dataRows.add(index, checkTotalRow)

    // промежуточные итоги для "-"
    def otherTotalRow = getTotalRow(getRowsByCheck(dataRows, '-'), 'otherTotal', 'Итого по другим сделкам')
    dataRows.add(otherTotalRow)

    // итоги
    def totalRow = getTotalRow(dataRows, 'total', 'Итого за текущий отчетный (налоговый) период')
    dataRows.add(totalRow)

    dataRowHelper.save(dataRows)
}

// TODO вопросы к заказчику
// Расчет графы 11
def BigDecimal calcMaxRate(def row) {
    if (row.outcomeDate == null || row.dateFirstPart == null) {
        return null
    }
    def date = row.dateFirstPart
    def currencyCode = '' // TODO (Ramil Timerbaev) не понятно как получать
    def rate = getRate(date)
    def tmp = null
    if (currencyCode == '810') {
        if ((date01_01_2010 <= date && date <= date31_12_2012)) {
            tmp = rate * 1.8
        } else if (date01_01_2013 <= date && date < date31_12_2013) {
            tmp = rate * 1.8
        }
    } else {
        if ((date01_01_2011 <= date && date <= date31_12_2013)) {
            tmp = rate * 0.8
        } else if (date01_01_2013 <= date && date < date31_12_2013) {
            tmp = rate * 0.8
        }
    }
    return roundValue(tmp, 2)
}

// Расчет графы 12
def BigDecimal calcOutcome(def row) {
    if (row.outcomeDate == null || row.rateREPO == null || row.maxRate == null) {
        return null
    }
    def tmp = null
    if (row.outcomeDate > 0) {
        if (row.rateREPO > row.maxRate) {
            tmp = (row.rateREPO * row.maxRate != 0 ? row.outcomeDate / (row.rateREPO * row.maxRate) : null)
        } else {
            tmp = row.outcomeDate
        }
    }
    return roundValue(tmp, 2)
}

// Расчет графы 14
def BigDecimal calcIncome(def row) {
    if (row.incomeDate == null || row.rateREPO == null || row.marketRate == null) {
        return null
    }
    def tmp = null
    if (row.incomeDate > 0) {
        if (row.rateREPO >= row.marketRate) {
            tmp = row.incomeDate
        } else {
            tmp = (row.rateREPO * row.marketRate != 0 ? row.incomeDate / (row.rateREPO * row.marketRate) : null)
        }
    }
    return roundValue(tmp, 2)
}

// Расчет графы 15
def BigDecimal calcSum(def row) {
    if (row.incomeDate == null || row.income == null) {
        return null
    }
    return roundValue(row.income - row.incomeDate, 2)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    // алиасы графов для арифметической проверки (графа )
    def arithmeticCheckAlias = ['maxRate', 'outcome', 'income', 'sum']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def reportDate = getReportDate()
    def startDate = getReportPeriodStartDate()
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей (графа 1..7, 15)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // . Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 2. Проверка даты первой части РЕПО (графа 6)
        if (row.dateFirstPart > reportDate) {
            logger.error(errorMsg + 'Неверно указана дата первой части сделки!')
        }

        // 3. Проверка даты второй части РЕПО	«Графа7» в рамках отчётного квартала 	1
        if (row.dateSecondPart < startDate || reportDate < row.dateSecondPart) {
            logger.error(errorMsg + 'Неверно указана дата второй части сделки!')
        }

        // 5. Арифметические проверки граф 11, 12, 14, 15
        needValue['maxRate'] = calcMaxRate(row)
        needValue['outcome'] = calcOutcome(row)
        needValue['income'] = calcIncome(row)
        needValue['sum'] = calcSum(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // TODO (Ramil Timerbaev) логические проверки не доописаны
        // 7?. Проверка ввода графы 2 (только + или -)
        if (row.check != null && !row.check.matches('[+||-]{1}')) {
            logger.error(errorMsg + "Графа «${getColumnName(row, 'check')}» может принимать значение «+» или «-»!")
        }
    }

    // 6. Проверка итоговых значений по всей форме (промежуточные и итоговые)
    // промежуточные итоги для "+"
    def checkTotalRow = getDataRow(dataRows, 'checkTotal')
    def tmpCheckTotalRow = getTotalRow(getRowsByCheck(dataRows, '+'), '', '')
    if (isDiffRow(checkTotalRow, tmpCheckTotalRow, totalColumns)) {
        logger.error('Значения «Итого по сделкам, подлежащим проверке»  рассчитаны неверно!')
    }

    // промежуточные итоги для "-"
    def otherTotalRow = getDataRow(dataRows, 'otherTotal')
    def tmpOtherTotalRow = getTotalRow(getRowsByCheck(dataRows, '-'), '', '')
    if (isDiffRow(otherTotalRow, tmpOtherTotalRow, totalColumns)) {
        logger.error('Значения «Итого по другим сделкам»  рассчитаны неверно!')
    }

    def totalRow = getDataRow(dataRows, 'total')
    def tmpTotalRow = getTotalRow(dataRows, '', '')
    if (isDiffRow(totalRow, tmpTotalRow, totalColumns)) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }
}

/** Получить итоговую строку по отчетному периоду. */
def getTotalRow(def dataRows, def alias, def name) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = name
    newRow.getCell('fix').colSpan = 5
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Получить строки для подсчета промежуточных итогов.
 *
 * @param dataRows строки для поиска
 * @param check значение графы 2 («+» или «-»)
 */
def getRowsByCheck(def dataRows, def check) {
    def resultRows = []
    def rows = (check == '+' ? dataRows : dataRows.reverse())
    for (def row : rows) {
        if (row.getAlias() != null) {
            continue
        }
        if (row.check == check) {
            resultRows.add(row)
        } else {
            break
        }
    }
    return resultRows
}

def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId).time
    }
    return reportDay
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

/** Получить ставку рефинансирования ЦБ РФ. */
def getRate(def date) {
    if (date != null) {
        def res = refBookFactory.getDataProvider(23).getRecords(date, null, null, null);
        if (res.getRecords() != null && res.getRecords().size() > 0)
            return res.getRecords().get(0).RATE?.getNumberValue()
    }
    return null
}