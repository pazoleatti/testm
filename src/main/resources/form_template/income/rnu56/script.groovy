package form_template.income.rnu56

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import java.math.RoundingMode
/**
 * Скрипт для РНУ-56 (rnu56.groovy)
 * Форма "(РНУ-56) Регистр налогового учёта процентного дохода по дисконтным векселям сторонних эмитентов"
 *
 * @version 59
 *
 * @author rtimerbaev
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
        logicalCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

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

// Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
//    row.keySet().each{
//        row.getCell(it).setStyleAlias('Автозаполняемая')
//    }
    ['bill', 'buyDate', 'currency', 'nominal', 'price',
            'maturity', 'implementationDate', 'sum'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
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
    // удалить строку "итого"
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    for (Iterator<DataRow> iter = dataRows.iterator() as Iterator<DataRow>; iter.hasNext();) {
        row = (DataRow) iter.next()
        if (isTotal(row)) {
            iter.remove()
            dataRowHelper.delete(row)
        }
    }

    if (dataRows.isEmpty()) {
        return
    }

    /** Дата начала отчетного периода. */
    def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def startDate = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def endDate = (tmp ? tmp.getTime() : null)

    def index = 0

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
        row.sumIncomeinRuble = calcSumIncomeinRuble(row)
    }
    dataRowHelper.update(dataRows);

    // итого (графа 13, 15)
    def totalRow = formData.createDataRow()
    dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    totalRow.getCell('bill').colSpan = 11
    setTotalStyle(totalRow)
    ['discountInRub', 'sumIncomeinRuble'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
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
def calcDiscountInRub(def row) {
    if (row.currency != null && row.implementationDate != null && !isRubleCurrency(row.currency)) {
        def res = refBookFactory.getDataProvider(22).getRecords(row.implementationDate, null, 'CODE_NUMBER='
                + row.currency, null)
        if (res != null && res.size() == 1) {
            return round(res.get(0).RATE.getNumberValue())
        }
    } else {
        return row.discountInCurrency
    }
    return null
}

// Расчет графы 14
def calcSumIncomeinCurrency(def row, def startDate, def endDate) {
    if (startDate == null || endDate == null) {
        return null
    }

    if (row.implementationDate == null) {
        if (row.percIncome == null || row.termDealBill == null) {
            return null
        }
        /** Количество дней владения векселем в отчетном периоде. */
        def countsDays = !row.buyDate.before(startDate) ? endDate - row.buyDate + 1 : endDate - startDate
        if (countsDays == 0) {
            return null
        }
        return round(row.percIncome / row.termDealBill * countsDays)
    } else {
        def sum = getCalcPrevColumn(row.bill, 'sumIncomeinCurrency')

        if (row.sum != null) {
            if (row.discountInCurrency == null) {
                return null
            }
            return round(row.discountInCurrency - sum)
        } else {
            if (row.percIncome == null) {
                return null
            }
            return round(row.percIncome - sum)
        }
    }
}

// Расчет графы 15
def calcSumIncomeinRuble(def row) {
    def tmp
    if (row.sum != null) {
        if (!isRubleCurrency(row.currency)) {
            if (row.sumIncomeinCurrency == null) {
                return null
            }
            tmp = row.sumIncomeinCurrency * getRate(getReportDate(), row.currency)
        } else {
            tmp = row.sumIncomeinCurrency
        }
    } else {
        if (row.discountInRub == null) {
            return null
        }
        tmp = row.discountInRub - getCalcPrevColumn(row.bill, 'sumIncomeinRuble')
    }

    return row.sumIncomeinRuble = round(tmp)
}

/**
 * Логические проверки
 */
def logicalCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    if (dataRows.isEmpty()) {
        return true
    }

    def i = 1

    // Обязательность заполнения поля графы (с 1 по 15)
    def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'percIncome',
            'implementationDate', 'sum', 'discountInCurrency',
            'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble']

    // суммы строки общих итогов
    def totalSums = [:]

    // графы для которых надо вычислять итого (графа 13, 15)
    def totalColumns = ['discountInRub', 'sumIncomeinRuble']

    /** Дата начала отчетного периода. */
    def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def startDate = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def endDate = (tmp ? tmp.getTime() : null)

    // Векселя
    def List<String> billsList = new ArrayList<String>()

    def totalRow = null

    for (def row : dataRows) {
        if (isTotal(row)) {
            totalRow = row
            continue
        }

        // 1. Проверка на заполнение поля
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        // 2. Проверка даты приобретения и границ отчетного периода
        if (endDate != null && row.buyDate.after(endDate)) {
            logger.error('Дата приобретения вне границ отчетного периода!')
            return false
        }

        // 3. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
        if (i++ != row.number) {
            logger.error('Нарушена уникальность номера по порядку!')
            return false
        }

        // 4. Проверка на уникальность векселя
        if (billsList.contains(row.bill)) {
            logger.error("Повторяющееся значения в графе «Вексель»")
            return false
        } else {
            billsList.add(row.bill)
        }

        // 5. Проверка на нулевые значения (графа 12..15)
        if (row.discountInCurrency == 0 &&
                row.discountInRub == 0 &&
                row.sumIncomeinCurrency == 0 &&
                row.sumIncomeinRuble == 0) {
            logger.error('Все суммы по операции нулевые!')
            return false
        }

        // 6. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 10 и графы 11
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 7. Проверка корректности значения в «Графе 3»
        // TODO Получить РНУ-56 за прошлые отчетные периоды

        // 8. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
            logger.error('Расчёт дисконта некорректен!')
            return false
        }

        // 9. Проверка на неотрицательные значения
        if (row.discountInCurrency == null || row.discountInCurrency < 0) {
            logger.error("Значение графы «${row.getCell('discountInCurrency').column.name}» отрицательное!")
        }
        if (row.discountInRub == null || row.discountInRub < 0) {
            logger.error("Значение графы «${row.getCell('discountInRub').column.name}» отрицательное!")
        }

        // 10. Арифметические проверки граф 8, 9, 12-15
        // Графа 8
        if (row.termDealBill != calcTermDealBill(row)) {
            logger.warn("Неверно рассчитана графа «${row.getCell('termDealBill').column.name}»!")
        }
        // Графа 9
        if (row.percIncome != calcPercIncome(row)) {
            logger.warn("Неверно рассчитана графа «${row.getCell('percIncome').column.name}»!")
        }
        // Графа 12
        if (row.discountInCurrency != calcDiscountInCurrency(row)) {
            logger.warn("Неверно рассчитана графа «${row.getCell('discountInCurrency').column.name}»!")
        }
        // Графа 13
        if (row.discountInRub != calcDiscountInRub(row)) {
            logger.warn("Неверно рассчитана графа «${row.getCell('discountInRub').column.name}»!")
        }
        // Графа 14
        if (row.sumIncomeinCurrency != calcSumIncomeinCurrency(row, startDate, endDate)) {
            logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в валюте»!')
        }
        // Графа 15
        if (row.sumIncomeinRuble != calcSumIncomeinRuble(row)) {
            logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в рублях по курсу Банка России»!')
        }

        // 11. Проверка итоговых значений по всей форме (графа 13, 15)
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }

        // Проверки соответствия НСИ
        // Проверка кода валюты со справочным
        if (!checkNSI(row, "currency", "Код валюты", 15)) {
            return false
        }
    }

    if (totalRow != null) {
        // 11. Проверка итогового значений по всей форме (графа 13, 15)
        for (def alias : totalColumns) {
            if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                logger.error('Итоговые значения рассчитаны неверно!')
                return false
            }
        }
    }
}

// Проверка соответствия НСИ
boolean checkNSI(DataRow<Cell> row, String alias, String msg, Long id) {
    def cell = row.getCell(alias)
    if (cell.value != null && refBookService.getRecordData(id, cell.value) == null) {
        def msg2 = cell.column.name
        def rowNum = row.getIndex()
        logger.warn("Строка $rowNum: В справочнике «$msg» не найден элемент «$msg2»!")
        return false
    }
    return true
}

// Проверка при создании формы.
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id, formData.kind, formData.departmentId,
            formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

// Консолидация
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def rows = new LinkedList<DataRow<Cell>>()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
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

// Проверка является ли строка итоговой
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

// Проверка пустое ли значение
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

// Стиль для итоговых строк
void setTotalStyle(def row) {
    ['number', 'bill', 'buyDate', 'currency', 'nominal',
            'price', 'maturity', 'termDealBill', 'percIncome',
            'implementationDate', 'sum', 'discountInCurrency',
            'discountInRub', 'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

// Получить номер строки в таблице
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    dataRows.indexOf(row)
}

// Получить данные за предыдущий отчетный период
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

// Получить значение или ноль, если значения нет
def getValue(def value) {
    return (value != null ? value : 0)
}

// Получить сумму столбца
def getSum(def columnAlias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def from = 0
    def to = dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки: $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки: $errorMsg.")
        }
        return false
    }
    return true
}

// Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

// Получить курс банка России на указанную дату
def getRate(def Date date, def value) {
    def res = refBookFactory.getDataProvider(22).getRecords(date != null ? date : new Date(), null, "CODE_NUMBER = $value", null);
    return res.getRecords().get(0).RATE.numberValue
}

// Округление вещественного числа
def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}

// TODO Сумма граф <sumColumnName> из РНУ-56 предыдущих отчетных периодов, начиная с РНУ,
// где «Графа 3» принадлежит отчетному периоду
def getCalcPrevColumn(def bill, def sumColumnName) {
    return 0
}