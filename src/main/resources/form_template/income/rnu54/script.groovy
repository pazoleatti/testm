package form_template.income.rnu54

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-54) Регистр налогового учёта открытых сделок РЕПО с обязательством покупки по 2-й части".
 *
 * @version 65
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - откуда брать курс ЦБ РФ на отчётную дату для подсчета графы 12 и для 5ой и 6ой логической проверки
 *      - проверки корректности данных при загрузке данных из транспортного файла
 *
 * @author rtimerbaev
 */

import java.text.SimpleDateFormat

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        checkNSI()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        // для сохранения изменений приемников
        getData(formData).commit()
        break
    case FormDataEvent.IMPORT :
        importData()
        break
}

// графа 1  - tadeNumber
// графа 2  - securityName
// графа 3  - currencyCode
// графа 4  - nominalPriceSecurities
// графа 5  - salePrice
// графа 6  - acquisitionPrice
// графа 7  - part1REPODate
// графа 8  - part2REPODate
// графа 9  - income
// графа 10 - outcome
// графа 11 - rateBR
// графа 12 - outcome269st
// графа 13 - outcomeTax

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = getNewRow()
    def index = 0
    if(currentDataRow!=null){
        if(currentDataRow.getAlias()==null){
            index = getIndex(currentDataRow)+1
        }
    }
    data.insert(newRow,index+1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    getData(formData).delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    if (data == null) {
        return
    }
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 1..10)
    def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
            'nominalPriceSecurities', 'salePrice', 'acquisitionPrice',
            'part1REPODate', 'part2REPODate']

    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты
     */

    // удалить строку "итого"
    def delRow = []
    getRows(data).each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        data.delete(row)
    }

    /** Отчетная дата. */
    def reportDate = getReportDate()

    /** Дата нужная при подсчете графы 12. */
    def someDate = getDate('01.11.2009')

    /** Количество дней в году. */
    def daysInYear = getCountDaysInYaer(new Date())

    /** Курс ЦБ РФ на отчётную дату. */
    def course = 1

    def tmp
    def a, b ,c

    getRows(data).each { row ->

        def currency = getCurrency(row.currencyCode)
        course = getCourse(row.currencyCode,reportDate)

        // графа 9, 10
        a = calcAForColumn9or10(row, reportDate, course)
        b = 0
        c = 0
        if (a < 0) {
            c = roundTo2(Math.abs(a))
        } else if (a > 0) {
            b = roundTo2(a)
        }
        row.income = b
        row.outcome = c

        // графа 11
        row.rateBR = roundTo2(calc11Value(row, reportDate))

        // графа 12
        if (row.outcome == 0) {
            tmp = 0
        } else if (row.outcome > 0 && currency == '810') {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                tmp = calc12Value(row, 1.5, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                tmp = calc12Value(row, 2, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                tmp = calc12Value(row, 1.8, reportDate, daysInYear)
            } else {
                tmp = calc12Value(row, 1.1, reportDate, daysInYear)
            }
        } else if (row.outcome > 0 && currency != '810') {
            if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
            } else {
                tmp = calc12Value(row, 1, reportDate, daysInYear) * course
            }
        }
        row.outcome269st = tmp

        // графа 13
        if (row.outcome == 0) {
            tmp = 0
        } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
            tmp = row.outcome
        } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
            tmp = row.outcome269st
        }
        row.outcomeTax = tmp
    }
    save(data)

    // строка итого
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.tadeNumber = 'Итого'
    totalRow.getCell('tadeNumber').colSpan = 2
    setTotalStyle(totalRow)
    ['salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
    insert(data, totalRow)
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = getData(formData)
    if (data == null) {
        return true
    }
    if (!getRows(data).isEmpty()) {

        // список проверяемых столбцов (графа 12, 13)
        // + добавил проверку редактируемых графов (1..10 ) потому что без них невозможен расчет
        def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
                'nominalPriceSecurities', 'salePrice', 'acquisitionPrice',
                'part1REPODate', 'part2REPODate',
                // графа 12, 13
                'outcome269st', 'outcomeTax']

        /** Отчетная дата. */
        def reportDate = getReportDate()

        /** Дата нужная при подсчете графы 12. */
        def someDate = getDate('01.11.2009')

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** Курс ЦБ РФ на отчётную дату. */
        def course = 1

        def hasTotalRow = false
        def hasError
        def tmp
        def a, b, c

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }
            // 1. Обязательность заполнения поля графы 12 и 13
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            def currency = getCurrency(row.currencyCode)
            course= getCourse(row.currencyCode,reportDate)

            // 2. Проверка даты первой части РЕПО (графа 7)
            if (row.part1REPODate > reportDate) {
                logger.error('Неверно указана дата первой части сделки!')
                return false
            }
            // 3. Проверка даты второй части РЕПО (графа 8)
            if (row.part2REPODate <= reportDate) {
                logger.error('Неверно указана дата второй части сделки!')
                return false
            }

            // 4. Проверка финансового результата (графа 9, 10, 12, 13)
            if (row.income != 0 && row.outcome != 0) {
                logger.error('Задвоение финансового результата!')
                return false
            }

            // 5. Проверка финансого результата
            if (row.outcome == 0 && (row.outcome269st != 0 || row.outcomeTax != 0)) {
                logger.error('Задвоение финансового результата!')
                return false
            }

            // 6. Проверка финансового результата
            tmp = ((row.acquisitionPrice - row.salePrice) * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
            if (tmp < 0 && row.income != roundTo2(Math.abs(tmp))) {
                logger.warn('Неверно определены доходы')
            }

            // 7. Проверка финансового результата
            if (tmp > 0 && row.outcome != roundTo2(Math.abs(tmp))) {
                logger.warn('Неверно определены расходы')
            }

            // 8. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Начало
            // графа 9, 10
            a = calcAForColumn9or10(row, reportDate, course)
            b = 0
            c = 0
            if (a < 0) {
                c = roundTo2(Math.abs(a))
            } else if (a > 0) {
                b = roundTo2(a)
            }
            // графа 9
            if (row.income != b) {
                name = getColumnName(row, 'income')
                logger.warn("Неверно рассчитана графа «$name»!")
            }
            // графа 10
            if (row.outcome != c) {
                name = getColumnName(row, 'outcome')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 11
            def col11 = roundTo2(calc11Value(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                name = getColumnName(row, 'rateBR')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 12
            if (row.outcome == 0) {
                tmp = 0
            } else if (row.outcome > 0 && currency == '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                    tmp = calc12Value(row, 1.5, reportDate, daysInYear)
                } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                    tmp = calc12Value(row, 2, reportDate, daysInYear)
                } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                    tmp = calc12Value(row, 1.8, reportDate, daysInYear)
                } else {
                    tmp = calc12Value(row, 1.1, reportDate, daysInYear)
                }
            } else if (row.outcome > 0 && currency != '810') {
                if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                    tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
                } else {
                    tmp = calc12Value(row, 1, reportDate, daysInYear) * course
                }
            }
            if (row.outcome269st != tmp) {
                name = getColumnName(row, 'outcome269st')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 13
            if (row.outcome == 0) {
                tmp = 0
            } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
                tmp = row.outcome
            } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
                tmp = row.outcome269st
            }
            if (row.outcomeTax != tmp) {
                name = getColumnName(row, 'outcomeTax')
                logger.warn("Неверно рассчитана графа «$name»!")
            }
            // 8. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Конец
        }

        // 9. Проверка итоговых значений формы  Заполняется автоматически (графа 5, 6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = getRowByAlias(data, 'total')
            def totalSumColumns = ['salePrice', 'acquisitionPrice', 'income',
                    'outcome', 'outcome269st', 'outcomeTax']
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != getSum(alias)) {
                    logger.error('Итоговые значения формы рассчитаны неверно!')
                    return false
                }
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    if (data == null) {
        return true
    }
    if (!getRows(data).isEmpty()) {
        /** Отчетная дата. */
        def reportDate = getReportDate()

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            def col11 = roundTo2(calc11Value(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                logger.error('Неверно указана ставка Банка России!')
                return false
            }
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    if (data == null) {
        return
    }
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        insert(data, row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    //проверка периода ввода остатков
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Получение импортируемых данных.
 * Транспортный файл формата xml.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '' || !fileName.contains('.xml')) {
        return
    }

    def is = ImportInputStream
    if (is == null) {
        return
    }

    def xmlString = importService.getData(is, fileName)
    if (xmlString == null || xmlString == '') {
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        return
    }

    // сохранить начальное состояние формы
    def data = getData(formData)
    def rowsOld = getRows(data)
    try {
        // добавить данные в форму
        addData(xml)

        // расчитать и проверить
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicalCheck(false)
            checkNSI()
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.toString())
    }
    // откатить загрузку если есть ошибки
    if (logger.containsLevel(LogLevel.ERROR)) {
        data.clear()
        data.insert(rowsOld, 1)
    } else {
        logger.info('Данные загружены')
    }
    data.commit()
}



/*
 * Вспомогательные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Проверить попадает ли указанная дата в период
 */
def inPeriod(def date, def from, to) {
    if (date == null) {
        return false
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def dateFrom = format.parse(from)
    def dateTo = format.parse(to)
    return (dateFrom < date && date <= dateTo)
}

/**
 * Получить дату по строковому представлению (формата дд.ММ.гггг)
 */
def getDate(def value) {
    if (isEmpty(value)) {
        return null
    }
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    return format.parse(value)
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11Value(DataRow row, def rateDate){
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotal(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (currency == '810')    {
            return rate
        } else{ // Если «графа 3» ≠ 810), то
            // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')){
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')){
                // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
                return rate
            } else{
                //Если  «графа 6» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
                //то  «графа 11» = 15.
                return 15
            }
        }
    }
}
/**
 * Посчитать значение для графы 12.
 *
 * @paam row строка нф
 * @paam coef коэфициент
 * @paam reportDate отчетная дата
 * @paam days количество дней в году
 */
def calc12Value(def row, def coef, def reportDate, def days) {
    def tmp = (row.salePrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
    return roundTo2(tmp)
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def rows = getRows(data)
    if (rows.isEmpty()) {
        return 0
    }
    def lastRow = rows.get(rows.size() - 1)
    def to = (lastRow.getAlias() == null ? rows.size() - 1 : rows.size() - 2)
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate',
            'income', 'outcome', 'rateBR', 'outcome269st', 'outcomeTax'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def row = formData.createDataRow()

    // графа 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate'].each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(getData(formData)).indexOf(row)
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
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = row.tadeNumber
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
            logger.error("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить отчетную дату.
 */
def getReportDate() {
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Получить значение для графы 9 и графы 10
 *
 * @param row строка
 * @param reportDate отчетная дата
 * @param course курс
 */
def calcAForColumn9or10(def row, def reportDate, def course) {
    // ((«графа 6» - «графа 5») х (отчетная дата – «графа 7») / («графа 8» - «графа 7»)) х курс ЦБ РФ
    def tmp = ((row.acquisitionPrice - row.salePrice) *
            (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
    return tmp
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def getRows(def data) {
    return data.getAllCached()
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}

/**
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo2(BigDecimal value) {
    if (value != null) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Получить ставку рефинансирования ЦБ РФ
 * @param date
 */
def getRate(def date) {
    if (date!=null) {
        def refDataProvider = refBookFactory.getDataProvider(23)
        def res = refDataProvider.getRecords(date, null, null, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else {
        return null
    }

}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15,currencyCode,'CODE')
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE_2')=='810'
}

/**
 * Получить курс валюты.
 *
 * @param currency атрибут "Цифровой код валюты"
 * @param date дата
 */
def getCourse(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else if (isRubleCurrency(currency)){
        return 1;
    } else {
        return null
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
void addData(def xml) {
    def tmp
    def newRows = []
    def index
    def refDataProvider = refBookFactory.getDataProvider(15)

    // TODO (Ramil Timerbaev) Проверка корректности данных
    for (def row : xml.exemplar.table.detail.record) {
        index = 0

        def newRow = getNewRow()

        // графа 1
        newRow.tadeNumber = row.field[index].@value.text()
        index++

        // графа 2
        newRow.securityName = row.field[index].@value.text()
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        tmp = null
        if (row.field[index].@value.text() != null &&
                row.field[index].@value.text().trim() != '') {
            def records = refDataProvider.getRecords(new Date(), null, "CODE = '" + row.field[index].@value.text() + "'", null);
            if (records != null && !records.getRecords().isEmpty()) {
                tmp = records.getRecords().get(0).get('record_id').getNumberValue()
            }
        }
        newRow.currencyCode = tmp
        index++

        // графа 4
        newRow.nominalPriceSecurities = getNumber(row.field[index].@value.text())
        index++

        // графа 5
        newRow.salePrice = getNumber(row.field[index].@value.text())
        index++

        // графа 6
        newRow.acquisitionPrice = getNumber(row.field[index].@value.text())
        index++

        // графа 7
        newRow.part1REPODate = getDate(row.field[index].@value.text())
        index++

        // графа 8
        newRow.part2REPODate = getDate(row.field[index].@value.text())

        newRows.add(newRow)
    }
    // проверка итоговых данных
    if (xml.exemplar.table.total.record.field.size() > 0 && !newRows.isEmpty()) {
        def totalRow = formData.createDataRow()

        totalRow.salePrice = 0
        totalRow.acquisitionPrice = 0

        newRows.each { row ->
            totalRow.salePrice += (row.salePrice != null ? row.salePrice : 0)
            totalRow.acquisitionPrice += (row.acquisitionPrice != null ? row.acquisitionPrice : 0)
        }

        for (def row : xml.exemplar.table.total.record) {
            // графа 5 и 6
            if (totalRow.salePrice != getNumber(row.field[4].@value.text()) ||
                    totalRow.acquisitionPrice != getNumber(row.field[5].@value.text())) {
                logger.error('Итоговые значения неправильные.')
                return
            }
        }
    }
    def data = getData(formData)
    data.clear()
    data.insert(newRows, 1)
    data.commit()
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    if (value == null) {
        return null
    }
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    // поменять запятую на точку и убрать пробелы
    tmp = tmp.replaceAll(',', '.').replaceAll('[^\\d.,-]+', '')
    return new BigDecimal(tmp)
}