package form_template.income.rnu55

import java.math.RoundingMode
import java.text.SimpleDateFormat
import com.aplana.sbrf.taxaccounting.model.DataRow

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
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED:
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED:
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicalCheck()
        break
}

def getAtributes() {
    [
            number: ['number', 'гр. 1', '№ п/п'],
            bill: ['bill', 'гр. 2', 'Вексель'],
            buyDate: ['buyDate', 'гр. 3', 'Дата приобретения'],
            currency: ['currency', 'гр. 4', 'Код валюты'],
            nominal: ['nominal', 'гр. 5', 'Номинал, ед. валюты'],
            percent: ['percent', 'гр. 6', 'Процентная ставка'],
            implementationDate: ['implementationDate', 'гр. 7', 'Дата реализации (погашения)'],
            percentInCurrency: ['percentInCurrency', 'гр. 8', 'Фактически поступившая сумма процентов в валюте'],
            percentInRuble: ['percentInRuble', 'гр. 9', 'Фактически поступившая сумма процентов в рублях'],
            sumIncomeinCurrency: ['sumIncomeinCurrency', 'гр. 10', 'Сумма начисленного процентного дохода за отчётный период в валюте'],
            sumIncomeinRuble: ['sumIncomeinRuble', 'гр. 11', 'Сумма начисленного процентного дохода за отчётный период в рублях']
    ]
}

// графы 2..8
def getEditColumns() {
    ['bill', 'buyDate', 'currency', 'nominal', 'percent', 'implementationDate',
            'percentInCurrency']
}

//Добавить новую строку
void addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def row = formData.createDataRow()
    def dataRows = dataRowHelper.getAllCached()
    def size = dataRows.size()
    def index = currentDataRow != null ? (currentDataRow.getIndex() + 1) : (size == 0 ? 1 : (size + 1))
    getEditColumns().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowHelper.insert(row, index)
}

// Удалить строку
void deleteRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.delete(currentDataRow)
}

// Ресчет графы 9
def calc9(def row) {
    if (row.percentInCurrency != null && row.currency != null && row.implementationDate!=null) {
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
def calc10(def row, def startDate, def endDate, def daysInYear) {
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
    return round(tmp, 2)
}

// Ресчет графы 11
def calc11(def row, def endDate) {
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
    return round(tmp, 2)
}

// Расчеты. Алгоритмы заполнения полей формы.
void calc() {
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

    def tmp
    /** Количество дней в году. */
    def daysInYear = getCountDaysInYaer(new Date())
    // Отчетная дата
    def reportDate = getReportDate()
    //Начальная дата отчетного периода
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def reportDateStart = (tmp ? tmp.getTime() : null)
    // графы для последней строки "итого"
    def sumPercent = 0
    def sumIncome = 0
    def index = 0

    for (def row in dataRows) {

        // графа 1
        row.number = ++index

        // графа 9
        row.percentInRuble = calc9(row)

        // графа 10
        row.sumIncomeinCurrency = calc10(row, reportDateStart, reportDate, daysInYear)

        // графа 11
        row.sumIncomeinRuble = calc11(row, reportDate)

        // графы для последней строки "итого"
        if (row.percentInRuble != null)
            sumPercent += row.percentInRuble
        if (row.sumIncomeinRuble != null)
            sumIncome += row.sumIncomeinRuble

    }
    dataRowHelper.update(dataRows);

    // итого
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.bill = 'Итого'
    totalRow.getCell('bill').colSpan = 7
    setTotalStyle(totalRow)
    totalRow.percentInRuble = sumPercent
    totalRow.sumIncomeinRuble = sumIncome

    dataRowHelper.insert(totalRow, index + 1)
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    if (!dataRowHelper.getAllCached().isEmpty()) {
        def i = 1

        // 1. Проверка на заполнение полей 1..11
        def requiredColumns = ['number', 'bill', 'buyDate', 'currency', 'nominal',
                'percent', 'implementationDate', 'percentInCurrency',
                'percentInRuble', 'sumIncomeinCurrency', 'sumIncomeinRuble']
        for (def row in dataRowHelper.getAllCached()) {
            if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }

        // суммы строки общих итогов
        def totalSums = [:]

        // графы для которых надо вычислять итого (графа 9, 11)
        def totalColumns = ['percentInRuble', 'sumIncomeinRuble']

        // итоговая строка
        def totalRow = null

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** Дата начала отчетного периода. */
        def tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
        def a = (tmp ? tmp.getTime() : null)

        /** Дата окончания отчетного периода. */
        tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
        def b = (tmp ? tmp.getTime() : null)

        /** Отчетная дата. */
        def reportDate = getReportDate()

        // Векселя
        def List<String> billsList = new ArrayList<String>()

        def cell
        def hasError

        for (def row in dataRowHelper.getAllCached()) {
            if (isTotal(row)) {
                totalRow = row
                continue
            }

            // 2. Проверка даты приобретения и границ отчетного периода (графа 3)
            if (row.buyDate > b) {
                logger.error('Дата приобретения вне границ отчетного периода!')
                return false
            }

            // 3. Проверка даты реализации (погашения)  и границ отчетного периода (графа 7)
            if (row.implementationDate < a || b < row.implementationDate) {
                logger.error('Дата реализации (погашения) вне границ отчетного периода!')
                return false
            }

            // 4. Проверка на уникальность поля «№ пп» (графа 1) (в рамках текущего года)
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i = i + 1

            // 5. Проверка на уникальность векселя
            if (billsList.contains(row.bill)) {
                logger.error("Повторяющееся значения в графе «Вексель»")
                return false
            } else {
                billsList.add(row.bill)
            }

            // 6. Проверка корректности значения в «Графе 3»
            // TODO

            // 7. Проверка на наличие данных предыдущих отчетных периодов для заполнения графы 10 и графы 11
            // TODO (Ramil Timerbaev)
            if (false) {
                logger.error("Экземпляр за период(ы) <Дата начала отчетного периода1> - <Дата окончания отчетного периода1>, <Дата начала отчетного периода N> - <Дата окончания отчетного периода N> не существует (отсутствуют первичные данные для расчёта)!")
                return false
            }

            // 8. Проверка на неотрицательные значения
            hasError = false
            ['percentInCurrency', 'percentInRuble'].each {
                cell = row.getCell(it)
                if (cell.getValue() != null && cell.getValue() < 0) {
                    def name = cell.getColumn().getName()
                    logger.error("Значение графы \"$name\"  отрицательное!")
                    hasError = true
                }
            }
            if (hasError) {
                return false
            }
            hasError = false

            // 9. Арифметическая проверка графы 9-11
            if (row.percentInCurrency != calc9(row)) {
                logger.warn('Неверно рассчитана графа «Фактически поступившая сумма процентов в рублях»!')
                return false
            }
            if (row.sumIncomeinCurrency != calc10(row, a, reportDate, daysInYear)) {
                logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в валюте»!')
                return false
            }
            if (row.sumIncomeinRuble != calc11(row, reportDate)) {
                logger.warn('Неверно рассчитана графа «Сумма начисленного процентного дохода за отчётный период в рублях по курсу Банка России»!')
                return false
            }

            // 10. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }

            // Проверки соответствия НСИ.
            // Проверка кода валюты со справочным (графа 4)
            if (!checkNSI(row, "currency", "Код валюты", 15)) {
                return false
            }
        }

        if (totalRow != null) {
            // 10. Проверка итогового значений по всей форме (графа 9, 11)
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return false
                }
            }
        }
    }
    return true
}

// Проверка валюты на рубли
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

/**
 * Проверка соответствия НСИ
 */
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

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

// Проверка является ли строка итоговой
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

// Установить стиль для итоговых строк
void setTotalStyle(def row) {
    ['number', 'bill', 'buyDate', 'currency', 'nominal', 'percent',
            'implementationDate', 'percentInCurrency', 'percentInRuble',
            'sumIncomeinCurrency', 'sumIncomeinRuble'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

//Получить номер строки в таблице
def getIndex(def row) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().indexOf(row)
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
            sum += getValue(row.getCell(sumColumnName).getValue())
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

/**
 * Получить значение или ноль, если значения нет.
 */
def getValue(def value) {
    return (value != null ? value : 0)
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
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

//Получить отчетную дату
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

def round(def value, def int precision = 2) {
    if (value == null) {
        return null
    }
    return value.setScale(precision, RoundingMode.HALF_UP)
}