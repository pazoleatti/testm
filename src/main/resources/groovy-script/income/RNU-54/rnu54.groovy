/**
 * Скрипт для РНУ-54 (rnu54.groovy).
 * Форма "(РНУ-54) Регистр налогового учёта открытых сделок РЕПО с обязательством покупки по 2-й части".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- откуда брать курс ЦБ РФ на отчётную дату для подсчета графы 12 и для 5ой и 6ой логической проверки
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
    // проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
    // проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
    // проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        acceptance()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
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
    def newRow = formData.createDataRow()
    formData.dataRows.add(newRow)

    // графа 1..10
    ['tadeNumber', 'securityName', 'currencyCode', 'nominalPriceSecurities',
            'salePrice', 'acquisitionPrice', 'part1REPODate', 'part2REPODate',
            'income', 'outcome'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    formData.dataRows.remove(currentDataRow)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 1..10)
    def requiredColumns = ['tadeNumber', 'securityName', 'currencyCode',
            'nominalPriceSecurities', 'salePrice', 'acquisitionPrice',
            'part1REPODate', 'part2REPODate', 'income', 'outcome']

    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты
     */

    // удалить строку "итого"
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(getIndex(row))
    }

    /** Отчетная дата. */
    def reportDate = getReportDate()

    /** Дата нужная при подсчете графы 12. */
    def someDate = getDate('01.11.2009')

    /** Количество дней в году. */
    def daysInYear = getCountDaysInYaer(new Date())

    /** Курс ЦБ РФ на отчётную дату. */
    def course = 1 // TODO (Ramil Timerbaev) откуда брать курс ЦБ РФ на отчётную дату

    formData.dataRows.eachWithIndex { row, i ->

        // графа 11
        if (row.outcome == 0 || isEmpty(row.currencyCode)) {
            row.rateBR = null
        } else if (row.currencyCode == '810') {
            // TODO (Ramil Timerbaev) «графа 11» = ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на «отчетную дату»
            row.rateBR = 0
        } else {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                row.rateBR = 22
            } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012')) {
                // TODO (Ramil Timerbaev) ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на «отчетную дату»
                row.rateBR = 0
            } else {
                row.rateBR = 15
            }
        }

        // графа 12
        if (row.outcome == 0) {
            row.outcome269st = 0
        } else if (row.outcome > 0 && row.currencyCode == '810') {
            if (inPeriod(reportDate, '01.09.2008', '31.12.2009')) {
                row.outcome269st = calc12Value(row, 1.5, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') && row.part1REPODate < someDate) {
                row.outcome269st = calc12Value(row, 2, reportDate, daysInYear)
            } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012')) {
                row.outcome269st = calc12Value(row, 1.8, reportDate, daysInYear)
            } else {
                row.outcome269st = calc12Value(row, 1.1, reportDate, daysInYear)
            }
        } else if (row.outcome > 0 && row.currencyCode != '810') {
            if (inPeriod(reportDate, '01.01.20011', '31.12.2012')) {
                row.outcome269st = calc12Value(row, 0.8, reportDate, daysInYear) * course
            } else {
                row.outcome269st = calc12Value(row, 1, reportDate, daysInYear) * course
            }
        }

        // графа 13
        if (row.outcome == 0) {
            row.outcomeTax = 0
        } else if (row.outcome > 0 && row.outcome <= row.outcome269st) {
            row.outcomeTax = row.outcome
        } else if (row.outcome > 0 && row.outcome > row.outcome269st) {
            row.outcomeTax = row.outcome269st
        }
    }

    // строка итого
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.tadeNumber = 'Итого'
    totalRow.getCell('tadeNumber').colSpan = 2
    setTotalStyle(totalRow)
    ['nominalPriceSecurities', 'salePrice', 'acquisitionPrice', 'income',
            'outcome', 'outcome269st', 'outcomeTax'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {

        // список проверяемых столбцов (графа 12, 13)
        def requiredColumns = ['outcome269st', 'outcomeTax']

        /** Отчетная дата. */
        def reportDate = getReportDate()

        /** Дата нужная при подсчете графы 12. */
        def someDate = getDate('01.11.2009')

        /** Количество дней в году. */
        def daysInYear = getCountDaysInYaer(new Date())

        /** Курс ЦБ РФ на отчётную дату. */
        def course = 1 // TODO (Ramil Timerbaev) откуда брать курс ЦБ РФ на отчётную дату

        def hasTotalRow = false
        def hasError
        def tmp

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotalRow = true
                continue
            }

            // 1. Обязательность заполнения поля графы 12 и 13
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return false
            }

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
            if ((row.income > 0 && row.outcome != 0) ||
                    (row.outcome > 0 && row.income != 0) ||
                    (row.outcome == 0 && (row.outcome269st != 0 || row.outcomeTax != 0))) {
                logger.error('Задвоение финансового результата!')
                return false
            }

            // 5. Проверка финансового результата
            tmp = ((row.acquisitionPrice - row.salePrice) * (reportDate - row.part1REPODate) / (row.part2REPODate - row.part1REPODate)) * course
            if (tmp < 0 && row.income != round(Math.abs(tmp), 2)) {
                logger.warn('Неверно определены доходы')
            }

            // 6. Проверка финансового результата
            if (tmp > 0 && row.outcome != round(Math.abs(tmp), 2)) {
                logger.warn('Неверно определены расходы')
            }

            // 7. Арифметическая проверка графы 12
            hasError = false
            if (row.outcome > 0 && row.currencyCode == '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009') &&
                        row.outcome269st != calc12Value(row, 1.5, reportDate, daysInYear)) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2010', '30.06.2010') &&
                        row.part1REPODate < someDate &&
                        row.outcome269st != calc12Value(row, 2, reportDate, daysInYear)) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2010', '31.12.2012') &&
                        row.outcome269st != calc12Value(row, 1.8, reportDate, daysInYear)) {
                    hasError = true
                } else if (row.outcome269st != calc12Value(row, 1.1, reportDate, daysInYear)) {
                    hasError = true
                }
            } else if (row.outcome > 0 && row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.01.20011', '31.12.2012') &&
                        row.outcome269st != calc12Value(row, 0.8, reportDate, daysInYear) * course) {
                    hasError = true
                } else if (row.outcome269st != calc12Value(row, 1, reportDate, daysInYear) * course) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, рассчитанные с учётом ст. 269 НК РФ (руб.коп.)»!')
                return false
            }

            // 8. Арифметическая проверка графы 13
            hasError = false
            if (row.outcome == 0 && row.outcomeTax != 0) {
                hasError = true
            } else if (row.outcome > 0 && row.outcome <= row.outcome269st &&
                    row.outcomeTax != row.outcome) {
                hasError = true
            } else if (row.outcome > 0 && row.outcome > row.outcome269st &&
                    row.outcomeTax != row.outcome269st) {
                hasError = true
            }
            if (hasError) {
                logger.error('Неверно рассчитана графа «Расходы по сделке РЕПО, учитываемые для целей налогообложения (руб.коп.)»!')
                return false
            }
        }

        // 9. Проверка итоговых значений формы	Заполняется автоматически (графа 4..6, 9, 10, 12, 13).
        if (hasTotalRow) {
            def totalRow = formData.getDataRow('total')
            def totalSumColumns = ['nominalPriceSecurities', 'salePrice', 'acquisitionPrice', 'income',
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
    if (!formData.dataRows.isEmpty()) {
        /** Отчетная дата. */
        def reportDate = getReportDate()

        def hasError
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 1. Проверка кода валюты со справочным (графа 3)
            if (false) {
                logger.warn('Неверный код валюты!')
            }

            // 2. Проверка соответствия ставки рефинансирования ЦБ (графа 11) коду валюты (графа 3)
            hasError = false
            if (((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR == null)) {
                hasError = false
            } else if ((row.outcome == 0 || isEmpty(row.currencyCode)) && row.rateBR != null) {
                hasError = true
            } else if (row.currencyCode == '810' && true) {
                // TODO (Ramil Timerbaev) условие: «графа 11» != ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ» на «отчетную дату»
                // row.rateBR != значнеие из справочника
                hasError = true
            } else if (row.currencyCode != '810') {
                if (inPeriod(reportDate, '01.09.2008', '31.12.2009') && row.rateBR != 22) {
                    hasError = true
                } else if (inPeriod(reportDate, '01.01.2011', '31.12.2012') && true) {
                    // TODO (Ramil Timerbaev) условие: графа 11 != ставка рефинансирования Банка России из справочника «Ставки рефинансирования ЦБ РФ»  на «отчетную дату»
                    // row.rateBR != значнеие из справочника
                    hasError = true
                } else if (row.rateBR != 15) {
                    hasError = true
                }
            }
            if (hasError) {
                logger.error('Неверно указана ставка Банка России!')
                return false
            }
        }
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        formData.dataRows.add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = FormDataService.find(formData.formType.id,
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
    def tmp = (row.salePrice * row.rateBR * coef) * ((reportDate - row.part1REPODate) / days) / 100
    return round(tmp, 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
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
    formData.dataRows.indexOf(row)
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