package form_template.income.rnu56

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState

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

/** Признак периода ввода остатков. */
def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        if (!isBalancePeriod && !checkPrevPeriod()) {
            logger.error('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
            return
        }
        logicalCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        logicalCheck()
        break
}

// Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def row = getNewRow()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    dataRowHelper.insert(row, index)
}

/**
 * Удалить строку
 */
def deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (row.getAlias() != null) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }

    if (dataRows.isEmpty()) {
        return
    }

    /** Дата начала отчетного периода. */
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    /** Дата окончания отчетного периода. */
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    def index = 0
    // получить номер последний строки предыдущей формы
    def formDataOld = getFormDataOld(formData.reportPeriodId)
    if (formDataOld != null) {
        def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
        def dataRowsOld = dataRowHelperOld.getAllCached()
        if (!dataRowsOld.isEmpty()) {
            index = dataRowsOld[dataRowsOld.size - 2].number
        }
    }

    def cache = [:]

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
        row.discountInRub = calcDiscountInRub(row, cache)

        // графа 14
        row.sumIncomeinCurrency = calcSumIncomeinCurrency(row, startDate, endDate)

        // графа 15
        row.sumIncomeinRuble = calcSumIncomeinRuble(row, endDate, cache)
    }
    dataRowHelper.update(dataRows)

    // добавить строку "итого" (графа 13, 15)
    def totalRow = calcTotalRow(dataRows)
    dataRowHelper.insert(totalRow, index + 1)
}

// Расчет графы 8
def calcTermDealBill(def row) {
    if (row.buyDate == null || row.maturity == null) {
        return null
    } else {
        return round(row.maturity - row.buyDate + 1, 0)
    }
}

// Расчет графы 9
def calcPercIncome(def row) {
    if (row.nominal == null || row.price == null) {
        return null
    } else {
        return row.nominal - row.price
    }
}

// Расчет графы 12
def calcDiscountInCurrency(def row) {
    if (row.sum == null || row.price == null) {
        return null
    } else {
        return row.sum - row.price
    }
}

// Расчет графы 13
def calcDiscountInRub(def row, def cache) {
    if (row.discountInCurrency != null) {
        if (row.currency != null && !isRubleCurrency(row.currency, cache)) {
            def record = null
            if (row.implementationDate != null) {
                // значение поля «Курс валюты» справочника «Курсы валют» на дату из «Графы 10»
                record = getRecord(22, 'CODE_NUMBER=' + row.currency, row.implementationDate, cache)
            }
            if (record != null) {
                return round(row.discountInCurrency * record.RATE.value)
            }
        } else {
            return row.discountInCurrency
        }
    }
    return null
}

// Расчет графы 14
def calcSumIncomeinCurrency(def row, def startDate, def endDate) {
    if (startDate == null || endDate == null) {
        return null
    }
    if (row.implementationDate == null) {
        return null
    }
    def tmp
    if ((row.implementationDate < startDate || row.implementationDate > endDate) && row.sum == null) {
        if (row.percIncome == null || row.termDealBill == null) {
            return null
        }
        /** Количество дней владения векселем в отчетном периоде. */
        def countsDays = !row.buyDate.before(startDate) ? endDate - row.buyDate : endDate - startDate
        if (countsDays == 0) {
            return null
        }
        tmp = row.percIncome / row.termDealBill * countsDays
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
    return (tmp != null ? round(tmp, 2) : null)
}

// Расчет графы 15
def calcSumIncomeinRuble(def row, def endDate, cache) {
    def tmp
    if (row.sum != null) {
        if (!isRubleCurrency(row.currency, cache)) {
            if (row.sumIncomeinCurrency == null) {
                return null
            }
            tmp = row.sumIncomeinCurrency * getRate(endDate, row.currency, cache)
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

    return round(tmp)
}

/**
 * Логические проверки
 */
def logicalCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return true
    }

    def i = 0
    // получить номер последний строки предыдущей формы
    def formDataOld = getFormDataOld(formData.reportPeriodId)
    if (formDataOld != null) {
        def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
        def dataRowsOld = dataRowHelperOld.getAllCached()
        if (!dataRowsOld.isEmpty()) {
            i = dataRowsOld[dataRowsOld.size - 2].number
        }
    }

    // http://jira.aplana.com/browse/SBRFACCTAX-4853 - РНУ-56. Обязательные поля.
    // Обязательность заполнения поля графы (1..8, 14, 15)
    def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'sumIncomeinCurrency', 'sumIncomeinRuble']
    // 1. Проверка на заполнение поля
    for (def row : dataRows) {
        if (row.getAlias() == null && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    // алиасы графов для арифметической проверки (графа 8, 9, 12-15)
    def arithmeticCheckAlias = ['termDealBill', 'percIncome', 'discountInCurrency', 'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []

    // суммы строки общих итогов
    def totalSums = [:]

    // графы для которых надо вычислять итого (графа 13, 15)
    def totalColumns = ['discountInRub', 'sumIncomeinRuble']

    /** Дата начала отчетного периода. */
    def startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time

    /** Дата окончания отчетного периода. */
    def endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Векселя
    def List<String> billsList = new ArrayList<String>()

    def totalRow = null

    def cache = [:]
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
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        // 2. Проверка даты приобретения и границ отчетного периода
        if (endDate != null && row.buyDate.after(endDate)) {
            logger.error(errorMsg + 'Дата приобретения вне границ отчетного периода!')
            return false
        }

        // 3. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
        if (++i != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
            return false
        }

        // 4. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            logger.error(errorMsg + 'Повторяющееся значения в графе «Вексель»')
            return false
        } else {
            billsList.add(row.bill)
        }

        // 5. Проверка на нулевые значения (графа 12..15)
        if (row.discountInCurrency == 0 &&
                row.discountInRub == 0 &&
                row.sumIncomeinCurrency == 0 &&
                row.sumIncomeinRuble == 0) {
            logger.error(errorMsg + 'Все суммы по операции нулевые!')
            return false
        }

        // 6. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 14 и графы 15
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 7. Проверка корректности значения в «Графе 3»
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 8. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
            logger.error(errorMsg + 'Расчёт дисконта некорректен!')
            return false
        }

        // 9. Проверка на неотрицательные значения
        if (row.discountInCurrency == null || row.discountInCurrency < 0) {
            logger.error(errorMsg + "Значение графы «${row.getCell('discountInCurrency').column.name}» отрицательное!")
            return false
        }
        if (row.discountInRub == null || row.discountInRub < 0) {
            logger.error(errorMsg + "Значение графы «${row.getCell('discountInRub').column.name}» отрицательное!")
            return false
        }

        // 10. Арифметические проверки граф 8, 9, 12-15
        needValue['termDealBill'] = calcTermDealBill(row)
        needValue['percIncome'] = calcPercIncome(row)
        needValue['discountInCurrency'] = calcDiscountInCurrency(row)
        needValue['discountInRub'] = calcDiscountInRub(row, cache)
        needValue['sumIncomeinCurrency'] = calcSumIncomeinCurrency(row, startDate, endDate)
        needValue['sumIncomeinRuble'] = calcSumIncomeinRuble(row, endDate, cache)
        arithmeticCheckAlias.each { alias ->
            if (needValue[alias] != row.getCell(alias).getValue()) {
                def name = getColumnName(row, alias)
                colNames.add('"' + name + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def msg = colNames.join(', ')
            logger.error(errorMsg + "Неверно рассчитано значение графы: $msg.")
            return false
        }

        // 11. Проверка итоговых значений по всей форме (графа 13, 15)
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }

        // Проверки соответствия НСИ
        // 1. Проверка кода валюты со справочным
        if (!checkNSI(row, 'currency', 15L, cache)) {
            return false
        }
    }

    if (totalRow != null) {
        // 11. Проверка итогового значений по всей форме (графа 13, 15)
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.info('======= t = ' + totalSums[alias]) // TODO (Ramil Timerbaev)
                logger.info('======= c = ' + totalRow.getCell(alias).getValue()) // TODO (Ramil Timerbaev)
                def name = totalRow.getCell(alias).column.name
                logger.error("Итоговые значения рассчитаны неверно в графе \"$name!\"")
                return false
            }
        }
    }

    return true
}

/**
 * Проверка соответствия НСИ.
 *
 * @param row строка
 * @param alias алиас справочной графы
 * @param refbookId идентификатор справочника
 * @param cache кеш
 */
boolean checkNSI(DataRow<Cell> row, String alias, Long refbookId, def cache) {
    def cell = row.getCell(alias)
    if (cell.value != null && getRecordById(refbookId, cell.value, cache) == null) {
        def refBook = refBookFactory.get(refbookId)
        def refBookName = refBook.name
        def colName = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$refBookName» не найден элемент «$colName»!")
        return false
    }
    return true
}

// Проверка при создании формы.
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (findForm != null) {
        logger.error('Формирование нового отчета невозможно, т.к. отчет с указанными параметрами уже сформирован.')
    }
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        rows.add(row)
                    }
                }
            }
        }
    }
    formDataService.getDataRowHelper(formData).save(rows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Получить данные за предыдущий отчетный период по идентификатору отчетного периода
def getFormDataOld(def reportPeriodId) {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(reportPeriodId)
    if (reportPeriodOld != null) {
        return formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    return null
}

// Получить сумму столбца
def getSum(def dataRows, def columnAlias) {
    def sum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) {
            sum += (row.getCell(columnAlias).value ?: 0)
        }
    }
    return sum
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).column.name.replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.getIndex()
        def errorMsg = colNames.join(', ')
        logger.error("В строке $index не заполнены колонки: $errorMsg.")
        return false
    }
    return true
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode, def cache) {
    def record = getRecordById(15, currencyCode, cache)
    return (record != null && record.CODE.value == '810')
}

// Получить курс банка России на указанную дату
def getRate(def Date date, def value, def cache) {
    def record = getRecord(22, "CODE_NUMBER = $value", (date ?: new Date()), cache)
    return record.RATE.value
}

// Округление вещественного числа
def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
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
    def formDataOld = getFormDataOld(reportPeriodId)
    def sum = 0
    if (formDataOld != null && formDataOld.state == WorkflowState.ACCEPTED) {
        def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
        def dataRowsOld = dataRowHelperOld.getAllCached()
        def startDate = reportPeriodService.getStartDate(formDataOld.reportPeriodId).time
        def endDate = reportPeriodService.getEndDate(formDataOld.reportPeriodId).time
        def find = false
        for (def row : dataRowsOld) {
            if (row.bill == bill && row.buyDate >= startDate && row.buyDate <= endDate) {
                sum += (row.getCell(sumColumnName).value ?: 0)
                find = true
            }
        }
        if (find) {
            return sum
        } else {
            return getCalcPrevColumn(bill, sumColumnName, formDataOld.reportPeriodId)
        }
    }
    return 0
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()
//    row.keySet().each{
//        row.getCell(it).setStyleAlias('Автозаполняемая')
//    }
    ['bill', 'buyDate', 'currency', 'nominal', 'price',
            'maturity', 'implementationDate', 'sum'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
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

/**
 * Получить запись из справочника по идентифкатору записи.
 *
 * @param refBookId идентификатор справончика
 * @param recordId идентификатор записи
 * @param cache кеш
 * @return
 */
def getRecordById(def refBookId, def recordId, def cache) {
    if (cache[refBookId] != null) {
        if (cache[refBookId][recordId] != null) {
            return cache[refBookId][recordId]
        }
    } else {
        cache[refBookId] = [:]
    }
    def record = refBookService.getRecordData(refBookId, recordId)
    if (record != null) {
        cache[refBookId][recordId] = record
        return cache[refBookId][recordId]
    }
    // def refBook = refBookFactory.get(refBookId)
    // def refBookName = refBook.name
    // logger.error("Не удалось найти запись (id = $recordId) в справочнике $refBookName (id = $refBookId)!")
    return null
}

/**
 * Получить запись из справочника по фильту на дату.
 *
 * @param refBookId идентификатор справончика
 * @param filter фильтр для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return запись справочника
 */
def getRecord(def refBookId, String filter, Date date, def cache) {
    if (cache[refBookId] != null) {
        if (cache[refBookId][filter] != null) {
            return cache[refBookId][filter]
        }
    } else {
        cache[refBookId] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(refBookId)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[refBookId][filter] = records.get(0)
        return cache[refBookId][filter]
    }
    def refBook = refBookFactory.get(refBookId)
    def refBookName = refBook.name
    logger.error("Не удалось найти запись в справочнике $refBookName (id = $refBookId) с атрибутом $code равным $value!")
    return null
}

/**
 * Проверить данные за предыдущий отчетный период.
 */
def checkPrevPeriod() {
    def formDataOld = getFormDataOld(formData.reportPeriodId)
    if (formDataOld != null && formDataOld.state == WorkflowState.ACCEPTED) {
        def dataRowHelperOld = formDataService.getDataRowHelper(formDataOld)
        def dataRowsOld = dataRowHelperOld.getAllCached()
        if (!dataRowsOld.isEmpty()) {
            return true
        }
    }
    return false
}