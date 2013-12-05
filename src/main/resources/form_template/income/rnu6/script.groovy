package form_template.income.rnu6

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * (РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4,
 *                      учёт которых требует применения метода начисления
 * formTemplateId=318
 *
 * графа 1  Число/15/                       number
 * helper   Строка/1000                     helper
 * графа 2  А140/CODE/Строка/15/            kny
 * графа 3  Дата                            date
 * графа 4  A350/NUMBER/Строка/12/          code
 * графа 5  Строка/15                       docNumber
 * графа 6  Дата/ДД.ММ.ГГГГ                 docDate
 * графа 7  A64/CODE/Строка/3/              currencyCode
 * графа 8  Число/19.4/                     rateOfTheBankOfRussia
 * графа 9  Число/17.2/                     taxAccountingCurrency
 * графа 10 Число/17.2/                     taxAccountingRuble
 * графа 11 Число/17.2/                     accountingCurrency
 * графа 12 Число/17.2/                     ruble
 *
 * @author Stanislav Yasinskiy
 */

// Признак периода ввода остатков
@Field
def boolean isBalancePeriod = null

def getBalancePeriod(){
    if (isBalancePeriod == null){
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

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
        def cols = (getBalancePeriod() ?
            ['kny', 'date', 'docNumber', 'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', 'ruble']
            : editableColumns)
        formDataService.addRow(formData, currentDataRow, cols, null)
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
def editableColumns = ['kny', 'date', 'docNumber', 'docDate', 'currencyCode', 'taxAccountingCurrency', 'accountingCurrency']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'kny', 'date', 'code', 'docNumber', 'docDate', 'currencyCode',
        'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'taxAccountingRuble', 'accountingCurrency', 'ruble']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['taxAccountingRuble', 'ruble']

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
    if (!getBalancePeriod() && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
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

        // сортируем по кодам
        dataRowHelper.save(dataRows.sort { getKnu(it.kny) })

        // номер последний строки предыдущей формы
        def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            row.number = ++number
            if (!getBalancePeriod()) {
                row.rateOfTheBankOfRussia = calc8(row)
                row.taxAccountingRuble = calc10(row)
                row.ruble = calc12(row)
            }
            row.code = row.kny
        }

        // посчитать "итого по коду"
        def totalRows = [:]
        def tmp = null
        def sum = 0, sum2 = 0
        dataRows.eachWithIndex { row, i ->
            if (tmp == null) {
                tmp = row.kny
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.kny) {
                def code = getKnu(tmp)
                totalRows.put(i, getNewRow(code, sum, sum2))
                sum = 0
                sum2 = 0
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
            if (i == dataRows.size() - 1) {
                sum += (row.taxAccountingRuble ?: 0)
                sum2 += (row.ruble ?: 0)
                def code = getKnu(row.kny)
                def totalRowCode = getNewRow(code, sum, sum2)
                totalRows.put(i + 1, totalRowCode)
                sum = 0
                sum2 = 0
            }
            sum += (row.taxAccountingRuble ?: 0)
            sum2 += (row.ruble ?: 0)
            tmp = row.kny
        }

        // добавить "итого по коду" в таблицу
        def i = 1
        totalRows.each { index, row ->
            dataRowHelper.insert(row, index + i++)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size + 1)
    dataRowHelper.save(dataRows)
}

def BigDecimal calc8(DataRow row) {
    if(row.currencyCode==null || row.date == null) {
        return null
    }
    if (isRubleCurrency(row.currencyCode)) {
        return 1
    }
    if (row.date == null || row.currencyCode == null) {
        return null
    }
    return getRate(row.date, row.currencyCode)
}

def BigDecimal calc10(DataRow row) {
    if (row.taxAccountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return (row.taxAccountingCurrency * row.rateOfTheBankOfRussia).setScale(2, BigDecimal.ROUND_HALF_UP)
}

def BigDecimal calc12(DataRow row) {
    if (row.accountingCurrency == null || row.rateOfTheBankOfRussia == null) {
        return null
    }
    return (row.accountingCurrency * row.rateOfTheBankOfRussia).setScale(2, BigDecimal.ROUND_HALF_UP)
}

// Получить курс валюты value на дату date
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords(date != null ? date : currentDate, null, "CODE_NUMBER = $value", null);
    return res.getRecords().get(0).RATE.numberValue
}

// Проверка валюты currencyCode на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue == '810') : false
}

def calcTotalRow(def dataRows) {
    def totalRow =  getTotalRow('total','Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Получить новую строку подитога
def getNewRow(def alias, def sum, def sum2) {
    def newRow = getTotalRow('total' + alias,'Итого по КНУ ' + alias)
    newRow.taxAccountingRuble = sum
    newRow.ruble = sum2
    return newRow
}

def getTotalRow(def alias, def title){
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.helper = title
    newRow.getCell('helper').colSpan = 9
    ['number', 'helper', 'taxAccountingRuble', 'accountingCurrency', 'ruble'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    List<Map<Integer, Object>> docs = new ArrayList<>()
    List<Map<Integer, Object>> uniq456 = new ArrayList<>(dataRows.size())
    SimpleDateFormat dateFormat = new SimpleDateFormat('dd.MM.yyyy')

    // алиасы графов для арифметической проверки
    def arithmeticCheckAlias = ['rateOfTheBankOfRussia', 'taxAccountingRuble', 'ruble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

    // Дата начала отчетного периода
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    // Дата окончания отчетного периода
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def index
    def errorMsg

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        index = row.getIndex()
        errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !getBalancePeriod())

        // 2. Проверка на нулевые значения
        if (row.taxAccountingCurrency == 0 &&
                row.taxAccountingRuble == 0 &&
                row.accountingCurrency == 0 &&
                row.ruble == 0) {
            loggerError(errorMsg + 'Все суммы по операции нулевые!')
        }

        // 3. Проверка, что не отображаются данные одновременно по бухгалтерскому и по налоговому учету
        if (row.taxAccountingRuble != null && row.ruble != null && row.taxAccountingRuble != 0 && row.ruble != 0) {
            logger.warn(errorMsg + 'одновременно указаны данные по налоговому  «%s» и бухгалтерскому «%s» учету.',
                    row.getCell('taxAccountingRuble').column.name, row.getCell('ruble').column.name)
        }

        // 4. Проверка даты совершения операции и границ отчётного периода
        if (row.date != null && (row.date.after(endDate) || row.date.before(startDate))) {
            loggerError(errorMsg + 'Дата совершения операции вне границ отчётного периода!')
        }

        // 5. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
        if (!getBalancePeriod()) {
            def Map<Integer, Object> map = new HashMap<>()
            if (row.docDate != null && row.docNumber != null) {
                map.put(5, row.docNumber)
                map.put(6, row.docDate)
                if (!docs.indexOf(map)) {
                    docs.add(map)
                    def c12 = 0
                    def c10 = 0
                    for (rowSum in dataRows) {
                        if (rowSum.docNumber == row.docNumber && rowSum.docDate == row.docDate) {
                            c12 += rowSum.ruble
                            c10 += rowSum.taxAccountingRuble
                        }
                    }
                    if (!(c10 > c12)) {
                        loggerError(errorMsg + 'Сумма данных бухгалтерского учёта превышает сумму начисленных платежей ' +
                                'для документа %s от %s!', row.docNumber as String, rowSum.docDate as String)
                    }
                }
                if (row.code != null) {
                    // 7. Проверка на уникальность записи по налоговому учету
                    map.put(4, row.code);
                    if (uniq456.contains(map)) {
                        loggerError(errorMsg + "Имеется другая запись в налоговом учете с аналогичными значениями балансового " +
                                "счета=%s, документа № %s от %s.", refBookService.getStringValue(28, row.code, 'NUMBER').toString(),
                                row.docNumber.toString(), dateFormat.format(row.docDate))
                    } else {
                        uniq456.add(map)
                    }
                }
            }
        }

        // 6. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            loggerError(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 8. Проверка соответствия балансового счета коду налогового учета
        if (row.kny != row.code) {
            loggerError(errorMsg + "Балансовый счет не соответствует коду налогового учета!")
        }

        // 9. Арифметические проверки расчета неитоговых строк
        if (!getBalancePeriod()) {
            needValue['rateOfTheBankOfRussia'] = calc8(row)
            needValue['taxAccountingRuble'] = calc10(row)
            needValue['ruble'] = calc12(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        }

        // 12. Проверка наличия суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        // 13. Проверка значения суммы дохода в налоговом учете, для первичного документа, указанного для суммы дохода в бухгалтерском учёте
        if (row.docDate != null) {
            date = row.docDate as Date
            from = new GregorianCalendar()
            from.setTime(date)
            from.set(Calendar.YEAR, from.get(Calendar.YEAR) - 3)
            taxPeriods = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, from.getTime(), date)
            isFind = false
            for (taxPeriod in taxPeriods) {
                reportPeriods = reportPeriodService.listByTaxPeriod(taxPeriod.id)
                for (reportPeriod in reportPeriods) {
                    findFormData = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriod.id)
                    if (findFormData != null && findFormData.id != formData.id) {
                        for (findRow in formDataService.getDataRowHelper(findFormData).getAllCached()) {
                            // SBRFACCTAX-3531 исключать строку из той же самой формы не надо
                            if (findRow.code == row.code && findRow.docNumber == row.docNumber
                                    && findRow.docDate == row.docDate) {
                                isFind = true
                                if (!(findRow.ruble > row.ruble)) {
                                    logger.warn(errorMsg + "Операция, указанная в строке ${row.number}, в налоговом учете имеет сумму, меньше чем указано в бухгалтерском учете! См. РНУ-6 за ${reportPeriod.name} ${reportPeriod.getYear()} года.")
                                }
                            }
                        }
                    }
                }
            }
            if (!isFind) {
                logger.warn('Операция, указанная в строке %s, в налоговом учете за последние 3 года не проходила!',
                        row.number.toString())
            }
        }

        // Проверки соответствия НСИ
        checkNSI(28, row, 'kny')
        checkNSI(28, row, 'code')
        checkNSI(15, row, 'currencyCode')
        // TODO (Ramil Timerbaev) 4. курс банка России - rateOfTheBankOfRussia
    }

    // 10. Арифметические проверки расчета итоговых строк «Итого по КНУ»
    checkSubTotalSum(dataRows, totalColumns, logger, !getBalancePeriod())

    // 11. Арифметические проверки расчета строки общих итогов
    checkTotalSum(dataRows, totalColumns, logger, !getBalancePeriod())
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg, Object...args) {
    if (getBalancePeriod()) {
        logger.warn(msg, args)
    } else {
        logger.error(msg, args)
    }
}