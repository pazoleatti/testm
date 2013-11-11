package form_template.income.rnu55

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * Скрипт для РНУ-55 (rnu55.groovy).
 * Форма "(РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов".
 * formTemplateId=348
 *
 * @author rtimerbaev
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
def editableColumns = ['bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate', 'percentInCurrency']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate',
        'percentInCurrency', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']

// Атрибуты для итогов
@Field
def totalColumns = ['percentInRuble', 'sumIncomeinRuble']

// Все атрибуты
@Field
def allColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate',
        'percentInCurrency', 'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']

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
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
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

    if (!dataRows.isEmpty()) {
        def tmp
        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())
        // Отчетная дата
        def reportDate = getReportDate()
        //Начальная дата отчетного периода
        def reportDateStart = reportPeriodService.getStartDate(formData.reportPeriodId).time
        def index = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

        for (def row in dataRows) {
            // графа 1
            row.number = ++index
            // графа 9
            row.percentInRuble = calc9(row)
            // графа 10
            row.sumIncomeinCurrency = calc10(row, reportDateStart, reportDate, daysInYear)
            // графа 11
            row.sumIncomeinRuble = calc11(row, reportDate)
        }
    }

    // Добавление итогов
    dataRows.add(getTotalRow(dataRows))

    dataRowHelper.save(dataRows)
}

// Расчет итоговой строки
def getTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    totalRow.getCell('bill').colSpan = 7
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

// Ресчет графы 9
def BigDecimal calc9(def row) {
    if (row.percentInCurrency != null && row.currency != null && row.implementationDate != null) {
        rate = 1
        if (!isRubleCurrency(row.currency)) {
            rate = getRate(row.implementationDate, row.currency)
        }
        return row.percentInCurrency * rate
    } else {
        return null
    }
}

// Ресчет графы 10
def BigDecimal calc10(def row, def startDate, def endDate, def daysInYear) {
    if (row.buyDate == null || startDate == null || endDate == null || row.nominal == null
            || row.percent == null || daysInYear == null || daysInYear == 0 || row.bill == null) {
        return null
    }

    def tmp = 0
    if (row.percentInCurrency == null) {
        countsDays = (row.buyDate >= startDate ?
            endDate - row.buyDate - 1 : endDate - startDate)
        if (countsDays != 0) {
            tmp = row.nominal * (row.percent / 100) * (countsDays / daysInYear)
        }
    } else {
        tmp = row.percentInCurrency - getCalcPrevColumn10(row.bill, 'sumIncomeinCurrency')
    }
    return tmp.setScale(2, RoundingMode.HALF_UP)
}

// Ресчет графы 11
def BigDecimal calc11(def row, def endDate) {
    if (row.currency == null || endDate == null || row.implementationDate == null
            || row.sumIncomeinCurrency == null || row.bill == null) {
        return null
    }
    def tmp = 0
    if (row.percentInCurrency == null) {
        if (row.implementationDate != null) {
            rate = 1
            if (!isRubleCurrency(row.currency)) {
                rate = getRate(row.implementationDate, row.currency)
            }
            tmp = row.sumIncomeinCurrency * rate
        } else {
            tmp = row.sumIncomeinCurrency * getRate(endDate, row.currency)
        }
    } else {
        tmp = row.percentInRuble - getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
    }
    return tmp.setScale(2, RoundingMode.HALF_UP)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return
    }

    def i = formDataService.getFormDataPrevRowCount(formData, formDataDepartment.id)

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['percentInCurrency', 'sumIncomeinCurrency', 'sumIncomeinRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Количество дней в году
    def daysInYear = getCountDaysInYaer(new Date())
    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    /** Отчетная дата. */
    def reportDate = getReportDate()

    // Векселя
    def List<String> billsList = new ArrayList<String>()

    def cell
    def hasError

    for (def row in dataRowHelper.getAllCached()) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Проверка на заполнение поля 1..11
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка даты приобретения и границ отчетного периода (графа 3)
        if (row.buyDate > endDate) {
            logger.error('Дата приобретения вне границ отчетного периода!')
        }

        // 3. Проверка даты реализации (погашения)  и границ отчетного периода (графа 7)
        if (row.implementationDate < startDate || endDate < row.implementationDate) {
            logger.error('Дата реализации (погашения) вне границ отчетного периода!')
        }

        // 4. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
        if (++i != row.number) {
            logger.error('Нарушена уникальность номера по порядку!')
        }

        // 5. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            logger.error("Повторяющееся значения в графе «Вексель»")
        } else {
            billsList.add(row.bill)
        }

        // 6. Проверка корректности значения в «Графе 3»
        // TODO

        // 7. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 10 и графы 11
        // TODO (Ramil Timerbaev)
        if (false) {
            logger.error("Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)!")
        }

        // 8. Проверка на неотрицательные значения
        ['percentInCurrency', 'percentInRuble'].each {
            cell = row.getCell(it)
            if (cell.getValue() != null && cell.getValue() < 0) {
                def name = cell.getColumn().getName()
                logger.error("Значение графы \"$name\"  отрицательное!")
            }
        }

        // 9. Арифметическая проверка графы 9-11
        needValue['percentInCurrency'] = calc9(row)
        needValue['sumIncomeinCurrency'] = calc10(row, startDate, reportDate, daysInYear)
        needValue['sumIncomeinRuble'] = calc11(row, reportDate)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        checkNSI(15, row, 'currency')
    }

    //10. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
    checkTotalSum(dataRows, totalColumns, logger, true)
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

// TODO (Ramil Timerbaev) учесть графу 3 при суммировании
/**
 * Cумма ранее начисленного процентного дохода по векселю до отчётного периода
 * (сумма граф 10 из РНУ-55 предыдущих отчётных (налоговых) периодов)
 * выбирается по графе 2 с даты приобретения (графа3) по дату начала отчетного периода.
 *
 * @param bill вексель
 * @param sumColumnName название графы, по которой суммировать данные
 */
def getCalcPrevColumn10(def bill, def sumColumnName) {
    def formDataOld = getFormDataOld()
    def sum = 0
    if (formDataOld == null) {
        return 0
    }
    formDataOld.dataRows.each {
        if (bill == row.bill) {
            sum += row.getCell(sumColumnName).getValue() != null ? row.getCell(sumColumnName).getValue() : 0
        }
    }
    return sum
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-55 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

// Получить курс банка России на указанную дату.
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords(date != null ? date : new Date(), null, "CODE_NUMBER = $value", null);
    return res.getRecords().get(0).RATE.numberValue
}

/**
 * Получить количество дней в году по указанной дате.
 */
def getCountDaysInYaer(def date) {
    if (date == null) {
        return 0
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def year = date.format('yyyy')
    def end = format.parse("31.12.$year")
    def begin = format.parse("01.01.$year")
    return end - begin + 1
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}
