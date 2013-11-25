package form_template.income.rnu54

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-54) Регистр налогового учёта открытых сделок РЕПО с обязательством покупки по 2-й части".
 * formTemplateId=347
 *
 * @version 65
 *
 * @author rtimerbaev
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE:
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW:
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck() && checkNSI()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        !hasError() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!hasError()) {
            calc()
        }
        break
    case FormDataEvent.MIGRATION:
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
        }
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
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while (row.getAlias() != null && index > 0) {
            row = getRows(data).get(--index)
        }
        if (index != currentDataRow.getIndex() && getRows(data).get(index).getAlias() == null) {
            index++
        }
    } else if (getRows(data).size() > 0) {
        for (int i = getRows(data).size() - 1; i >= 0; i--) {
            def row = getRows(data).get(i)
            if (row.getAlias() == null) {
                index = getRows(data).indexOf(row) + 1
                break
            }
        }
    }
    data.insert(newRow, index + 1)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        getData(formData).delete(currentDataRow)
    }
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

    // отсортировать/группировать - при импорте не сортировать
    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(data)
    }

    /** Отчетная дата. */
    def reportDate = getReportDate()

    /** Дата нужная при подсчете графы 12. */
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def someDate = getDate('01.11.2009', format)

    /** Количество дней в году. */
    def daysInYear = getCountDaysInYaer(new Date())

    /** Курс ЦБ РФ на отчётную дату. */
    def course = 1

    def tmp
    def a, b, c

    getRows(data).each { row ->

        // графа 9, 10 - при импорте не рассчитывать эти графы
        if (formDataEvent != FormDataEvent.IMPORT) {
            //def currency = getCurrency(row.currencyCode)
            course = getCourse(row.currencyCode, reportDate)

            a = calcAForColumn9or10(row, reportDate, course)
            b = 0; c = 0
            if (a < 0) {
                c = abs(a)
            } else if (a > 0) {
                b = a
            }
            row.income = b
            row.outcome = c
        }

        // графа 11
        row.rateBR = roundTo2(calc11Value(row, reportDate))
        // графа 12
        row.outcome269st = calc12(row)
        // графа 13
        row.outcomeTax = calc13(row)
    }
    save(data)

    // строка итого
    def totalRow = getCalcTotalRow()
    insert(data, totalRow)
}

def BigDecimal calc12(def row) {
    def tmp = 0
    if (row.outcome > 0 && row.currency == '810') {
        if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
            tmp = calc12Value(row, 1.5, reportDate, daysInYear)
        } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
            tmp = calc12Value(row, 2, reportDate, daysInYear)
        } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
            tmp = calc12Value(row, 1.8, reportDate, daysInYear)
        } else {
            tmp = calc12Value(row, 1.1, reportDate, daysInYear)
        }
    } else if (row.outcome > 0 && row.currency != '810') {
        if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
            tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
        } else {
            tmp = calc12Value(row, 1, reportDate, daysInYear) * course
        }
    }
    return tmp
}

def BigDecimal calc13(def row) {
    tmp = 0
    if (row.outcome > 0 && row.outcome <= row.outcome269st) {
        tmp = row.outcome
    } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
        tmp = row.outcome269st
    }
    return tmp
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)
    if (data == null) {
        return true
    }
    if (!getRows(data).isEmpty()) {

        // список проверяемых столбцов (графа 12, 13)
        def requiredColumns = ['outcome269st', 'outcomeTax']

        /** Отчетная дата. */
        def reportDate = getReportDate()

        /** Дата нужная при подсчете графы 12. */
        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        def someDate = getDate('01.11.2009', format)

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** Курс ЦБ РФ на отчётную дату. */
        def course

        def hasTotalRow = false
        def hasError
        def tmp
        def a, b, c

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            def index = row.tadeNumber
            def errorMsg
            if (index != null && index != '') {
                errorMsg = "В строке \"Номер сделки\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1. Обязательность заполнения поля графы 12 и 13
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            //def currency = getCurrency(row.currencyCode)
            course = getCourse(row.currencyCode, reportDate)

            // 2. Проверка даты первой части РЕПО (графа 7)
            if (row.part1REPODate > reportDate) {
                logger.error(errorMsg + 'неверно указана дата первой части сделки!')
                return false
            }
            // 3. Проверка даты второй части РЕПО (графа 8)
            if (row.part2REPODate <= reportDate) {
                logger.error(errorMsg + 'неверно указана дата второй части сделки!')
                return false
            }

            // 4. Проверка финансового результата (графа 9, 10, 12, 13)
            if (row.income != 0 && row.outcome != 0) {
                logger.error(errorMsg + 'задвоение финансового результата!')
                return false
            }

            // 5. Проверка финансого результата
            if (row.outcome == 0 && (row.outcome269st != 0 || row.outcomeTax != 0)) {
                logger.error(errorMsg + 'задвоение финансового результата!')
                return false
            }

            // 6. Проверка финансового результата
            if (row.acquisitionPrice != null && row.salePrice != null && reportDate != null
                    && row.part1REPODate != null && course != null) {
                tmp = ((row.acquisitionPrice - row.salePrice)
                        * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
                if (tmp < 0 && row.income != roundTo2(abs(tmp))) {
                    logger.warn(errorMsg + 'неверно определены доходы')
                }
            }

            // 7. Проверка финансового результата
            if (tmp > 0 && row.outcome != roundTo2(abs(tmp))) {
                logger.warn(errorMsg + 'неверно определены расходы')
            }

            // 8. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Начало
            // графа 9, 10
            a = calcAForColumn9or10(row, reportDate, course)
            b = 0; c = 0
            if (a < 0) {
                c = abs(a)
            } else if (a > 0) {
                b = a
            }
            // графа 9
            if (row.income != b) {
                name = getColumnName(row, 'income')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }
            // графа 10
            if (row.outcome != c) {
                name = getColumnName(row, 'outcome')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 11
            def col11 = roundTo2(calc11Value(row, row.part2REPODate))
            if (col11 != null && col11 != row.rateBR) {
                name = getColumnName(row, 'rateBR')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 12
            if (row.outcome269st != calc12(row)) {
                name = getColumnName(row, 'outcome269st')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 13
            if (row.outcomeTax != calc13(row)) {
                name = getColumnName(row, 'outcomeTax')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
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
        for (def row : getRows(data)) {
            if (isTotal(row)) {
                continue
            }
            def index = row.tadeNumber
            def errorMsg
            if (index != null && index != '') {
                errorMsg = "В строке \"Номер сделки\" равной $index "
            } else {
                index = row.getIndex()
                errorMsg = "В строке $index "
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (row.currencyCode != null && getCurrency(row.currencyCode) == null) {
                logger.warn(errorMsg + 'неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            def col11 = roundTo2(calc11Value(row, row.part2REPODate))
            if (col11 != null && col11 != row.rateBR) {
                logger.error(errorMsg + 'неверно указана ставка Банка России!')
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
    def newRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row ->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        newRows.add(row)
                    }
                }
            }
        }
    }
    if (!newRows.isEmpty()) {
        data.insert(newRows, 1)
        sort(data)
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
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
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    String charset = ""
    // TODO в дальнейшем убрать возможность загружать RNU для импорта!
    if (formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml') ||
            formDataEvent == FormDataEvent.MIGRATION && fileName.contains('.xml')) {
        if (!fileName.contains('.xml')) {
            logger.error('Формат файла должен быть *.xml')
            return
        }
    } else {
        if (!fileName.contains('.r')) {
            logger.error('Формат файла должен быть *.r??')
            return
        }
        charset = 'cp866'
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    def xmlString = importService.getData(is, fileName, charset)
    if (xmlString == null || xmlString == '') {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml, fileName)

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
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
def getDate(def value, format) {
    if (isEmpty(value)) {
        return null
    }
    try {
        return format.parse(value)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в дату. " + e.message)
    }
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11Value(DataRow row, def rateDate) {
    def currency = getCurrency(row.currencyCode)
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotal(row) && row.outcome != 0 && row.currencyCode != null) {
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «графа 6»,
        if (currency == '810') {
            return rate
        } else { // Если «графа 3» ≠ 810), то
            // Если «графа 6» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')) {
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')) {
                // Если «графа 6» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «графа 6»;
                return rate
            } else {
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
    return roundTo2(summ(formData, rows, new ColumnRange(columnAlias, from, to)))
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
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
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
    if (row.acquisitionPrice != null && row.salePrice != null && reportDate != null && row.part1REPODate != null && row.part2REPODate != null && course != null) {
        // ((«графа 6» - «графа 5») х (отчетная дата – «графа 7») / («графа 8» - «графа 7»)) х курс ЦБ РФ
        def tmp = ((row.acquisitionPrice - row.salePrice) *
                (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
        return roundTo2(tmp)
    } else {
        return null
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
    if (date != null) {
        def res = refBookFactory.getDataProvider(23).getRecords(date, null, null, null);
        if (res.getRecords() != null && res.getRecords().size() > 0)
            return res.getRecords().get(0).RATE.getNumberValue()
    }
    return null
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE')
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return refBookService.getStringValue(15, currencyCode, 'CODE') == '810'
}

/**
 * Получить курс валюты.
 *
 * @param currency атрибут "Цифровой код валюты"
 * @param date дата
 */
def getCourse(def currency, def date) {
    if (currency != null && date != null)
        if (isRubleCurrency(currency)) {
            return 1
        } else {
            def res = refBookFactory.getDataProvider(22).getRecords(date, null, 'CODE_NUMBER=' + currency, null);
            if (res.getRecords() != null && res.getRecords().size() > 0)
                return res.getRecords().get(0).RATE.getNumberValue()
        }
    return null
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, def fileName) {
    def tmp
    def index
    def date = new Date()
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def data = getData(formData)
    data.clear()
    def cache = [:]
    def newRows = []

    def records
    def totalRecords
    def type
    if (formDataEvent == FormDataEvent.MIGRATION ||
            formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml')) {
        records = xml.exemplar.table.detail.record
        totalRecords = xml.exemplar.table.total.record
        type = 1 // XML
    } else {
        records = xml.row
        totalRecords = xml.rowTotal
        type = 2 // RNU
    }

    for (def row : records) {
        index = 0
        def newRow = getNewRow()

        // графа 1
        newRow.tadeNumber =  getCellValue(row, index, type, true)
        index++

        // графа 2
        newRow.securityName =  getCellValue(row, index, type, true)
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        tmp = null
        if (getCellValue(row, index, type, true) != null &&  getCellValue(row, index, type, true).trim() != '') {
            tmp = getRecordId(15, 'CODE',  getCellValue(row, index, type, true), date, cache)
        }
        newRow.currencyCode = tmp
        index++

        // графа 4
        newRow.nominalPriceSecurities = getNumber(getCellValue(row, index, type))
        index++

        // графа 5 - 6 поменяты местами, потому что в тф(XML) и в настройках у них места перепутаны
        if (type==1) {
            // графа 6
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 5
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++
        } else {
            // графа 5
            newRow.salePrice = getNumber(getCellValue(row, index, type))
            index++

            // графа 6
            newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
            index++
        }
        // графа 7
        newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
        index++

        // графа 8
        newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
        index++

        // графа 9
        newRow.income = getNumber(getCellValue(row, index, type))
        index++

        // графа 10
        newRow.outcome = getNumber(getCellValue(row, index, type))
        index++

        // графа 11
        newRow.rateBR = getNumber(getCellValue(row, index, type))
        index++

        // графа 12
        newRow.outcome269st = getNumber(getCellValue(row, index, type))
        index++

        // графа 13
        newRow.outcomeTax = getNumber(getCellValue(row, index, type))

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (totalRecords.size() >= 1) {
        def row = totalRecords[0]
        def totalRow = formData.createDataRow()

        // графа 4
        totalRow.nominalPriceSecurities = getNumber(getCellValue(row, 3, type))

        // графа 5 - 6 поменяты местами, потому что в тф и в настройках у них места перепутаны
        if (type==1) {
            // графа 6
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 4, type))

            // графа 5
            totalRow.salePrice = getNumber(getCellValue(row, 5, type))
        } else {
            // графа 6
            totalRow.acquisitionPrice = getNumber(getCellValue(row, 5, type))

            // графа 5
            totalRow.salePrice = getNumber(getCellValue(row, 4, type))
        }

        // графа 9
        totalRow.income = getNumber(getCellValue(row, 8, type))

        // графа 10
        totalRow.outcome = getNumber(getCellValue(row, 9, type))

        // графа 12
        totalRow.outcome269st = getNumber(getCellValue(row, 11, type))

        // графа 13
        totalRow.outcomeTax = getNumber(getCellValue(row, 12, type))

        return totalRow
    } else {
        return null
    }
}

// для получения данных из RNU или XML
String getCellValue(def row, int index, def type, boolean isTextXml = false){
    if (type==1) {
        if (isTextXml) {
            return row.field[index].text()
        } else {
            return row.field[index].@value.text()
        }
    }
    return row.cell[index+1].text()
}

/**
 * Получить числовое значение.
 *
 * @param value строка
 */
def getNumber(def value) {
    def tmp = value.trim()
    if ("".equals(tmp)) {
        return null
    }
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить модуль числа.
 */
def abs(def value) {
    return (value != null && value < 0) ? -value : value
}

/**
 * Рассчитать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [5: 'salePrice', 6: 'acquisitionPrice', 9: 'income', 10: 'outcome', 12: 'outcome269st', 13: 'outcomeTax']
    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.tadeNumber = 'Итого'
    totalRow.getCell('tadeNumber').colSpan = 2
    setTotalStyle(totalRow)
    // графа  5, 6, 9, 10, 12, 13
    ['salePrice', 'acquisitionPrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
    return totalRow
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Отсорировать данные (по графе 7, 1).
 *
 * @param data данные нф (хелпер)
 */
void sort(def data) {
    getRows(data).sort { def a, def b ->
        // графа 1  - tadeNumber
        // графа 7  - part1REPODate
        if (a.part1REPODate == b.part1REPODate) {
            return a.tadeNumber <=> b.tadeNumber
        }
        return a.part1REPODate <=> b.part1REPODate
    }
}