package form_template.income.rnu53.v2008

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-53) Регистр налогового учёта открытых сделок РЕПО с обязательством продажи по 2-й части".
 * formTemplateId=346
 *
 * @version 1
 *
 * @author lhaziev
 *
 * TODO убрать loggerError и заменить на logger.error
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
        !hasError() && logicalCheck()
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
        logicalCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        consolidation()
        !hasError() && logicalCheck()
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
// графа 5  - acquisitionPrice
// графа 6  - salePrice
// графа 7  - part1REPODate
// графа 8  - part2REPODate
// графа 9  - income
// графа 10 - outcome
// графа 11 - rateBR
// графа 12 - outcome269st
// графа 13 - outcomeTax


@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def endDate = null

@Field
def reportDay = null

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    DataRow<Cell> newRow = getNewRow()
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
        def data = getData(formData)
        data.delete(currentDataRow)
    }
}

void checkBeforeCalc(DataRowHelper form) {
    for (row in form.allCached) {
        // 2. Проверка даты первой части РЕПО (графа 7)
        if (!(row.part1REPODate < getReportDate())) {
            loggerError('Неверно указана дата первой части сделки в строке ' + (form.allCached.indexOf(row) + 1) + '!')
            return
        }
        // 3. Проверка даты второй части РЕПО (графа 8)
        if (!(row.part2REPODate >= getReportDate())) {
            loggerError('Неверно указана дата второй части сделки в строке ' + (form.allCached.indexOf(row) + 1) + '!')
            return
        }
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    def dataRowHelper = getData(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    def delRow = []
    dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        dataRowHelper.delete(row)
    }

    // отсортировать/группировать
    if (formDataEvent != FormDataEvent.IMPORT) {
        sort(dataRowHelper)
    }

    if (formData.kind == FormDataKind.PRIMARY) {
        /** Отчетная дата. */
        def reportDate = getReportDate()
        /** Последний день отчетного периода */
        def lastDayReportPeriod = getReportPeriodEndDate()

        checkBeforeCalc(dataRowHelper)
        if (logger.containsLevel(LogLevel.ERROR)) {
            return  // Расчитывать не можем
        }

        /** Дата нужная при подсчете графы 12. */
        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        def someDate = getDate('01.11.2009', format)

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYear(getReportPeriodEndDate())

        /** Курс ЦБ РФ на Последний день отчетного периода. */
        def course = 1

        def tmp = 0
        def a, b, c

        for (def row : dataRows) {

            // графа 9, 10 - при импорте не рассчитывать эти графы
            if (formDataEvent != FormDataEvent.IMPORT) {
                course = getCourse(row.currencyCode, reportDate)

                a = calcAForColumn9or10(row, reportDate, course)
                b = 0
                c = 0
                if (a != null && a > 0) {
                    c = roundTo2(a)
                } else if (a != null && a < 0) {
                    b = roundTo2(-a)
                }
                row.income = c
                row.outcome = b
            }

            // графа 11
            row.rateBR = roundTo2(calc11(row, lastDayReportPeriod))

            // графа 12
            row.outcome269st = roundTo2(calc12(row, daysInYear, course, someDate, reportDate))

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
        dataRowHelper.save(dataRows)
    }

    // строка итого
    if (dataRows.size() > 0) {
        def totalRow = getCalcTotalRow()
        insert(dataRowHelper, totalRow)
    }
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def data = getData(formData)

    if (!getRows(data).isEmpty()) {

        // список проверяемых столбцов (графа 12, 13)
        def requiredColumns = ['outcome269st', 'outcomeTax']

        /** Отчетная дата. */
        def reportDate = getReportDate()
        /** Последний день отчетного периода */
        def lastDayReportPeriod = getReportPeriodEndDate()

        /** Дата нужная при подсчете графы 12. */
        SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
        def someDate = getDate('01.11.2009', format)

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYear(getReportPeriodEndDate())

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

            if (row.currencyCode != null) {
                course = getCourse(row.currencyCode, lastDayReportPeriod)
            }

            // 2. Проверка даты первой части РЕПО (графа 7)
            if (!(row.part1REPODate < reportDate)) {
                loggerError(errorMsg + 'неверно указана дата первой части сделки в строке ' + (getRows(data).indexOf(row) + 1) + '!')//TODO вернуть error
                return false
            }
            // 3. Проверка даты второй части РЕПО (графа 8)
            if (!(row.part2REPODate >= reportDate)) {
                loggerError(errorMsg + 'неверно указана дата второй части сделки в строке ' + (getRows(data).indexOf(row) + 1) + '!')//TODO вернуть error
                return false
            }

            // 4. Проверка финансового результата (графа 9, 10)
            if (row.income != 0 && row.outcome != 0) {
                loggerError(errorMsg + 'задвоение финансового результата!')//TODO вернуть error
                return false
            }

            // 5. Проверка финансого результата

            if (row.outcome == 0 && (row.outcome269st != 0 || row.outcomeTax != 0)) {
                loggerError(errorMsg + 'задвоение финансового результата!')//TODO вернуть error
                return false
            }

            // 6. Проверка финансового результата
            tmp = calcAForColumn9or10(row, reportDate, course)
            if (tmp != null && tmp > 0 && row.income != roundTo2(tmp)) {
                logger.warn(errorMsg + 'неверно определены доходы')
            }

            // 7. Проверка финансового результата
            if (tmp != null && tmp < 0 && row.outcome != roundTo2(-tmp)) {
                logger.warn(errorMsg + 'неверно определены расходы')
            }

            // 7. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Начало
            // графа 9, 10
            a = calcAForColumn9or10(row, reportDate, course)
            b = 0
            c = 0
            if (a != null && a > 0) {
                c = roundTo2(a)
            } else if (a != null && a < 0) {
                b = roundTo2(-a)
            }

            // графа 9
            if (row.income != c) {
                name = getColumnName(row, 'income')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }
            // графа 10
            if (row.outcome != b) {
                name = getColumnName(row, 'outcome')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 11
            def col11 = roundTo2(calc11(row, lastDayReportPeriod))
            if (col11 != null && col11 != row.rateBR) {
                name = getColumnName(row, 'rateBR')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 12
            if (row.outcome269st != roundTo2(calc12(row, daysInYear, course, someDate, reportDate))) {
                name = getColumnName(row, 'outcome269st')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }

            // графа 13
            if (row.outcome == 0) {
                tmp = 0
            } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
                tmp = row.outcome
            } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
                tmp = row.outcome269st
            }
            if (row.outcomeTax !=  roundTo2(tmp)) {
                name = getColumnName(row, 'outcomeTax')
                logger.warn(errorMsg + "неверно рассчитана графа «$name»!")
            }
            // 7. Арифметическая проверка графы 9, 10, 11, 12, 13 ===============================Конец
        }

        // 8. Проверка итоговых значений формы  Заполняется автоматически (графа 4, 5, 6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = data.getDataRow(getRows(data), 'total')
            def totalSumColumns = ['nominalPriceSecurities', 'acquisitionPrice', 'salePrice', 'income',
                    'outcome', 'outcome269st', 'outcomeTax']
            for (def alias : totalSumColumns) {
                if (totalRow.getCell(alias).getValue() != getSum(alias)) {
                    loggerError('Итоговые значения формы рассчитаны неверно!')//TODO вернуть error
                    return false
                }
            }
        }
    }

    // Проверока наличия итоговой строки
    if (!checkAlias(getRows(data), 'total')) {
        loggerError('Итоговые значения не рассчитаны')//TODO вернуть error
        return false
    }

    return true
}

def BigDecimal calc12(def row, def daysInYear, def course, def someDate, def reportDate) {
    if (row.outcome == null || row.currencyCode == null || row.part1REPODate == null
            || daysInYear == null || course == null || someDate == null) {
        return 0
    }
    def currency = getCurrency(row.currencyCode)
    def tmp = 0
    def isRuble = currency in ['810', '643']
    if (row.outcome > 0 && isRuble) {
        if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
            tmp = calc12Value(row, 1.5, reportDate, daysInYear)
        } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
            tmp = calc12Value(row, 2, reportDate, daysInYear)
        } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
            tmp = calc12Value(row, 1.8, reportDate, daysInYear)
        } else {
            tmp = calc12Value(row, 1.1, reportDate, daysInYear)
        }
    } else if (row.outcome > 0 && !isRuble) {
        if (inPeriod(reportDate, '01.01.2011', '31.12.2012')) {
            tmp = calc12Value(row, 0.8, reportDate, daysInYear) * course
        } else {
            tmp = calc12Value(row, 1, reportDate, daysInYear) * course
        }
    }
    return roundTo2(tmp)
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить все строки и собрать из источников их строки
    data.clear()
    def newRows = []

    // TODO (Ramil Timerbaev) в метод departmentFormTypeService.getFormSources добавить периоды
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
    def total = getCalcTotalRow()
    insert(data, total)
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
            logger.error('Формат файла должен быть *.rnu')
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

        // расчетать, проверить и сравнить итоги
        if (formDataEvent == FormDataEvent.IMPORT) {
            if (totalLoad != null) {
                checkTotalRow(totalLoad)
            } else {
                logger.error("Нет итоговой строки.")
            }
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
def getDate(def value, def format) {
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
 * Посчитать значение для графы 12.
 *
 * @paam row строка нф
 * @paam coef коэфициент
 * @paam reportDate отчетная дата
 * @paam days количество дней в году
 */
def calc12Value(def row, def coef, def reportDate, def days) {
    return (row.acquisitionPrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def to = getLastInsertIndex() - 2
    if (from > to) {
        return 0
    }
    return roundTo2(summ(formData, getRows(data), new ColumnRange(columnAlias, from, to)))
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
def getLastInsertIndex() {
    def data = getData(formData)
    def size = getRows(data).size()
    if (size > 0 && getRows(data).get(size - 1).getAlias() != null) {
        return size;
    } else {
        return size + 1;
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
            loggerError("В строке \"Номер сделки\" равной $index не заполнены колонки : $errorMsg.")//TODO вернуть error
        } else {
            index = getIndex(row) + 1
            loggerError("В строке $index не заполнены колонки : $errorMsg.")//TODO вернуть error
        }
        return false
    }
    return true
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
        return ((row.salePrice - row.acquisitionPrice) *
                (reportDate - row.part1REPODate) /
                (row.part2REPODate - row.part1REPODate)) * course
    } else {
        return null
    }
}

/**
 * Метод возвращает значение для графы 11
 * Логика выделена в отдельный метод так как
 * логика используется при расчетах и при логических проверкат
 * @param row
 * @param rateDate
 */
def calc11(DataRow row, def rateDate) {
    def rate = getRate(rateDate)
    // Если «графа 10» = 0, то « графа 11» не заполняется; && Если «графа 3» не заполнена, то « графа 11» не заполняется
    if (!isTotal(row) && row.outcome != 0 && row.currencyCode != null) {
        // Если «графа 3» = 810, то «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на дату «отчетная дата»,
        if (isRubleCurrency(row.currencyCode)) {
            return rate
        } else { // Если «графа 3» ≠ 810), то
            // Если «отчетная дата» принадлежит периоду с 01.09.2008 по 31.12.2009 (включительно), то «графа 11» = 22;
            if (inPeriod(rateDate, '01.09.2008', '31.12.2009')) {
                return 22
            } else if (inPeriod(rateDate, '01.01.2011', '31.12.2012')) {
                // Если «отчетная дата» принадлежит периоду с 01.01.2011 по 31.12.2012 (включительно), то
                // графа 11 = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на дату «отчетная дата»;
                return rate
            } else {
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()
    // графа 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'acquisitionPrice', 'salePrice', 'part1REPODate', 'part2REPODate'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
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
    return refBookService.getStringValue(15, currencyCode, 'CODE') in ['810', '643']
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency != null && date != null)
        if (isRubleCurrency(currency)) {
            return 1
        } else {
            def record = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache, 'CODE_NUMBER', "$currency",
                    date?:getReportPeriodEndDate(), -1, null, logger, true)

            return record?.RATE?.numberValue
        }
    return null
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml, def fileName) {
    def date = getReportPeriodEndDate()
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def cache = [:]
    def data = getData(formData)
    data.clear()
    def index
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
        newRow.tadeNumber = getCellValue(row, index, type, true)
        index++

        // графа 2
        newRow.securityName = getCellValue(row, index, type, true)
        index++

        // графа 3 - справочник 15 "Общероссийский классификатор валют"
        newRow.currencyCode = getRecords(15, 'CODE', getCellValue(row, index, type, true), date, cache)
        index++

        // графа 4
        newRow.nominalPriceSecurities = getNumber(getCellValue(row, index, type))
        index++

        // графа 5
        newRow.acquisitionPrice = getNumber(getCellValue(row, index, type))
        index++

        // графа 6
        newRow.salePrice = getNumber(getCellValue(row, index, type))
        index++

        // графа 7
        newRow.part1REPODate = getDate(getCellValue(row, index, type), format)
        index++

        // графа 8
        newRow.part2REPODate = getDate(getCellValue(row, index, type), format)
        index++

        if (formDataEvent == FormDataEvent.MIGRATION){
            //При миграции нужно поменять порядок зполнения этих полей
            // графа 10
            newRow.outcome = getNumber(getCellValue(row, index, type))
            index++

            // графа 9
            newRow.income = getNumber(getCellValue(row, index, type))
            index++
        } else {
            // графа 9
            newRow.income = getNumber(getCellValue(row, index, type))
            index++

            // графа 10
            newRow.outcome = getNumber(getCellValue(row, index, type))
            index++
        }

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

        // графа 5
        totalRow.acquisitionPrice = getNumber(getCellValue(row, 4, type))

        // графа 6
        totalRow.salePrice = getNumber(getCellValue(row, 5, type))

        if (formDataEvent == FormDataEvent.MIGRATION) {
            // графа 9
            totalRow.income = getNumber(getCellValue(row, 9, type))

            // графа 10
            totalRow.outcome = getNumber(getCellValue(row, 8, type))
        } else {
            // графа 9
            totalRow.income = getNumber(getCellValue(row, 8, type))

            // графа 10
            totalRow.outcome = getNumber(getCellValue(row, 9, type))
        }

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
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + " like '" + value.replaceAll(' ', '') + "%'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике «" + refBookFactory.get(ref_id).getName() + "» с атрибутом $code равным $value!")
    return null;
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = [4: 'nominalPriceSecurities', 5: 'acquisitionPrice', 6: 'salePrice', 9: 'income', 10: 'outcome', 12: 'outcome269st', 13: 'outcomeTax']

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
 * Вставить новую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
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
    ['nominalPriceSecurities', 'acquisitionPrice', 'salePrice', 'income', 'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias), null)
    }
    return totalRow
}

/**
 * Проверить существования строки по алиасу.
 *
 * @param list строки нф
 * @param rowAlias алиас
 * @return <b>true</b> - строка с указанным алиасом есть, иначе <b>false</b>
 */
def checkAlias(def list, def rowAlias) {
    if (rowAlias == null || rowAlias == "" || list == null || list.isEmpty()) {
        return false
    }
    for (def row : list) {
        if (row.getAlias() == rowAlias) {
            return true
        }
    }
    return false
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

void loggerError(def msg) {
    //TODO вернуть error
    //logger.error(msg)
    logger.warn(msg)
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

/** Получить отчетную дату. */
def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId)?.time
    }
    return reportDay
}