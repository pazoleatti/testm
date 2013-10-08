import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import com.aplana.sbrf.taxaccounting.model.FormData

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
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
        checkOnPrepareOrAcceptance('Подготовка')
        break
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
        checkOnPrepareOrAcceptance('Принятие')
        break
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
        checkOnCancelAcceptance()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        acceptance()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck(false)
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

    /** Отчетная дата. */
    // def reportDate = getReportDate()

    /** Дата начала отчетного периода. */
    def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def startDate = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def endDate = (tmp ? tmp.getTime() : null)

    /** Количество дней владения векселем в отчетном периоде. */
    def countsDays = 1

    /** Курс банка России. */
    def rate

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
        return round(row.buyDate - row.maturity + 1, 0)
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
            rate = getRate(reportDate, row.currency)
            tmp = row.sumIncomeinCurrency * rate
        } else {
            tmp = row.sumIncomeinCurrency
        }
    } else {
        if (row.discountInRub == null) {
            return null
        }
        tmp = row.discountInRub - getCalcPrevColumn10(row.bill, 'sumIncomeinRuble')
    }

    return row.sumIncomeinRuble = round(tmp)
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
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

    // признак наличия итоговых строк
    def hasTotal = false

    /** Дата начала отчетного периода. */
    def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def startDate = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def endDate = (tmp ? tmp.getTime() : null)

    /** Отчетная дата. */
    def reportDate = getReportDate()

    def totalRow = null

    for (def row : dataRows) {
        if (isTotal(row)) {
            totalRow = row
            continue
        }

        // Проверка на заполнение поля
        if (!checkRequiredColumns(row, requiredColumns, useLog)) {
            return false
        }

        // 2. Проверка даты приобретения и границ отчетного периода
        if (endDate != null && row.buyDate.after(endDate)) {
            logger.error('Дата приобретения вне границ отчетного периода!')
            return false
        }

        // 3. Проверка даты реализации (погашения) и границ отчетного периода
        if (startDate != null && row.implementationDate.before(startDate)
                || endDate != null && row.implementationDate.after(endDate)) {
            logger.error('Дата реализации (погашения) вне границ отчетного периода!')
            return false
        }

        // 4. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
        if (i++ != row.number) {
            logger.error('Нарушена уникальность номера по порядку!')
            return false
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
        // TODO Пока не ясно как проверить
        // def SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy")
        // logger.warn('Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>,
        // <Дата начала отчетного периода N> - <Дата окончания отчетного периода N>
        // не существует (отсутствуют первичные данные для расчёта)!')

        // 7. Проверка корректности расчёта дисконта
        if (row.sum != null && row.price != null && row.sum - row.price <= 0 && (row.discountInCurrency != 0 || row.discountInRub != 0)) {
            logger.error('Расчёт дисконта некорректен!')
            return false
        }

        // 8. Проверка на неотрицательные значения
        if (row.discountInCurrency == null || row.discountInCurrency < 0) {
            logger.error("Значение графы «${row.getCell('discountInCurrency').column.name}» отрицательное!")
        }
        if (row.discountInRub == null || row.discountInRub < 0) {
            logger.error("Значение графы «${row.getCell('discountInRub').column.name}» отрицательное!")
        }

        // 9. Арифметические проверки граф 8, 9, 12-15
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

        // 10. Проверка итоговых значений по всей форме (графа 13, 15)
        totalColumns.each { alias ->
            if (totalSums[alias] == null) {
                totalSums[alias] = 0
            }
            totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
        }

        // Проверки соответствия НСИ.
        // Проверка кода валюты со справочным
        if (!checkNSI(row, "currency", "Код валюты", 15)) {
            return false
        }
    }

    if (totalRow != null) {
        // 16. Проверка итогового значений по всей форме (графа 13, 15)
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

// Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята"
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
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
                source.getDataRows().each { row ->
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

// Проверки при переходе "Отменить принятие"
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

// Принять
void acceptance() {
    if (!logicalCheck(true)) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
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

// TODO (Ramil Timerbaev) учесть графу 3 при суммировании
/**
 * Cумма ранее начисленного процентного дохода по векселю до отчётного периода
 * (сумма граф 14 из РНУ-56 предыдущих отчётных (налоговых) периодов)
 * выбирается по графе 2 с даты приобретения (графа 3) по дату начала отчетного периода.
 *
 * @param bill вексель
 * @param sumColumnName название графы, по которой суммировать данные
 */
def getCalcPrevColumn(def bill, def sumColumnName) {
    def formDataOld = getFormDataOld()
    def sum = 0
    if (formDataOld == null) {
        return 0
    }
    def dataRowHelper = formDataService.getDataRowHelper(formDataOld)
    dataRowHelper.getAllCached().each {
        if (bill == row.bill) {
            sum += getValue(row.getCell(sumColumnName).getValue())
        }
    }
    return sum
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
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
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

// Cумма ранее начисленного процентного дохода по векселю до отчётного периода
// (сумма граф 10 из РНУ-55 предыдущих отчётных (налоговых) периодов)
// выбирается по графе 2 с даты приобретения (графа3) по дату начала отчетного периода
def getCalcPrevColumn10(def bill, def sumColumnName) {
//    def formDataOld = getFormDataOld()
//    def sum = 0
//    if (formDataOld == null) {
//        return 0
//    }
//    formDataOld.dataRows.each {
//        if (bill == row.bill) {
//            sum += getValue(row.getCell(sumColumnName).getValue())
//        }
//    }
//    return sum
    // TODO
    return 0
}