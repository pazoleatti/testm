/**
 * Скрипт для РНУ-53 (rnu53.groovy).
 * Форма "(РНУ-53) Регистр налогового учёта открытых сделок РЕПО с обязательством продажи по 2-й части".
 *
 * @version 1
 *
 * TODO:
 *      - поведение в основном совпадает с формой 54, отличия в ЧТЗ в 4 сравнениях: при заполениии формы для графов 9-10, в логических проверках 5-6
 *                                                    так же есть отличия при реализации из-за разных псевдонимов у графов 5-6
 *
 *
 * @author lhaziev
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
}

// графа 1  - tadeNumber
// графа 2  - securityName
// графа 3  - currencyCode
// графа 4  - nominalPriceSecurities
// графа 5  - acquisitionPrice
// графа 6  - salePrice
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
    def newRow = formData.createDataRow()
    def data = getData(formData)

    // графа 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'acquisitionPrice', 'salePrice', 'part1REPODate', 'part2REPODate'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    data.insert(newRow,getLastInsertIndex())
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def data = getData(formData)
    data.delete(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def data = getData(formData)
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 1..10)
    def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
            'nominalPriceSecurities', 'acquisitionPrice', 'salePrice',
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
    def daysInYear = getCountDaysInYear(new Date())

    /** Курс ЦБ РФ на отчётную дату. */
    def course = 1

    def tmp
    def a, b ,c

    getRows(data).eachWithIndex { row, i ->

        course = getCourse(row.currencyCode, reportDate)

        // графа 9, 10
        a = calcAForColumn9or10(row, reportDate, course)
        b = 0
        c = 0
        if (a > 0) {
            c = roundTo2(Math.abs(a))
        } else if (a < 0) {
            b = roundTo2(a)
        }
        row.income = b
        row.outcome = c

        def currency = getCurrency(row.currencyCode)
        // графа 11
        row.rateBR = roundTo2(calculateColumn11(row, reportDate))

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
        row.outcome269st = roundTo2(tmp)

        // графа 13
        if (row.outcome == 0) {
            tmp = 0
        } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
            tmp = row.outcome
        } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
            tmp = row.outcome269st
        }
        row.outcomeTax = roundTo2(tmp)
    }
    data.save(getRows(data))

    // строка итого
    if (getRows(data).size()>0) {
        def totalRow = formData.createDataRow()
        totalRow.setAlias('total')
        totalRow.tadeNumber = 'Итого'
        totalRow.getCell('tadeNumber').colSpan = 2
        setTotalStyle(totalRow)
        ['acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
            totalRow.getCell(alias).setValue(getSum(alias))
        }
        data.insert(totalRow,getRows(data).size()+1)
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {

        // список проверяемых столбцов (графа 12, 13)
        def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
                'nominalPriceSecurities', 'acquisitionPrice', 'salePrice',
                'part1REPODate', 'part2REPODate', 'income', 'outcome', 'outcome269st', 'outcomeTax']

        /** Отчетная дата. */
        def reportDate = getReportDate()

        /** Дата нужная при подсчете графы 12. */
        def someDate = getDate('01.11.2009')

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYear(new Date())

        /** Курс ЦБ РФ на отчётную дату. */
        def course = 1

        def hasTotalRow = false
        def BigDecimal tmp
        def a, b, c

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            if (row.currencyCode!=null) {
                course = getCourse(row.currencyCode,reportDate)
            }

            // 1. Обязательность заполнения поля графы 12 и 13
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return false
            }

            // 2. Проверка даты первой части РЕПО (графа 7)
            if (row.part1REPODate > reportDate) {
                logger.error('Неверно указана дата первой части сделки в строке '+ (getRows(data).indexOf(row)+1)+'!')
                return false
            }
            // 3. Проверка даты второй части РЕПО (графа 8)
            if (row.part2REPODate <= reportDate) {
                logger.error('Неверно указана дата второй части сделки в строке '+ (getRows(data).indexOf(row)+1)+'!')
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

            // 5. Проверка финансового результата
            tmp = ((row.salePrice - row.acquisitionPrice) * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
            if (tmp > 0 && row.income != roundTo2(Math.abs(tmp))) {
                logger.warn('Неверно определены доходы')
            }

            // 6. Проверка финансового результата
            if (tmp < 0 && row.outcome != roundTo2(Math.abs(tmp))) {
                logger.warn('Неверно определены расходы')
            }

            // 7. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Начало
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
            def col11 = roundTo2(calculateColumn11(row, row.part2REPODate))
            if (col11!=null && col11!=row.rateBR) {
                name = getColumnName(row, 'rateBR')
                logger.warn("Неверно рассчитана графа «$name»!")
            }

            // графа 12
            def currency = getCurrency(row.currencyCode)
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
            // 7. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Конец
        }

        // 8. Проверка итоговых значений формы  Заполняется автоматически (графа 5, 6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = data.getDataRow(getRows(data),'total')
            def totalSumColumns = ['acquisitionPrice', 'salePrice', 'income',
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
            def col11 = roundTo2(calculateColumn11(row, row.part2REPODate))
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
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row,getRows(data).size()+1)
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
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
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
 * Посчитать значение для графы 12.
 *
 * @paam row строка нф
 * @paam coef коэфициент
 * @paam reportDate отчетная дата
 * @paam days количество дней в году
 */
def calc12Value(def row, def coef, def reportDate, def days) {
    def tmp = (row.acquisitionPrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
    return roundTo2(tmp)
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def to = getLastInsertIndex()-2
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
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
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def data = getData(formData)
    getRows(data).indexOf(row)
}

/**
 * Получить индекс строки "итого" или (если ее нет) то индекс за последней строкой
 * @return
 */
def getLastInsertIndex(){
    def data = getData(formData)
    def size = getRows(data).size()
    if(size >0 && getRows(data).get(size-1).getAlias()!=null){
        return size;
    } else{
        return size+1;
    }
}

/**
 * Получить количество дней в году по указанной дате.
 */
def getCountDaysInYear(def date) {
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
        def index = getIndex(row) + 1
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
    return ((row.salePrice - row.acquisitionPrice) *
            (reportDate - row.part1REPODate) /
            (row.part2REPODate - row.part1REPODate)) * course
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calculateColumn11(DataRow row, def rateDate){
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotal(row) && row.outcome != 0 && row.currencyCode != null){
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «отчетная дата»,
        if (currency == '810')    {
            return rate
        } else{ // Если «графа 3» ≠ 810), то
            // Если «отчетная дата» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')){
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')){
                // Если «отчетная дата» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «отчетная дата»;
                return rate
            } else{
                //Если  «отчетная дата» не принадлежит отчётным периодам с 01.09.2008 по 31.12.2009 (включительно), с 01.01.2011 по 31.12.2012 (включительно)),
                //то  «графа 11» = 15.
                return 15
            }
        }
    }
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
    def cached = data.getAllCached()
    return cached
}

/**
 * Хелпер для округления чисел
 * @param value
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
 */
def getRate(def date) {
    if (date!=null) {
        def refDataProvider = refBookFactory.getDataProvider(23)
        def res = refDataProvider.getRecords(date, null, null, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    }else{
        return null;
    }
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE_2')=='810'
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else if ( isRubleCurrency(currency)){
        return 1;
    } else {
        return null
    }
}
