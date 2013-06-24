/**
 * Скрипт для РНУ-22 (rnu22.groovy).
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- графа 17 и графа 18 уточняют
 *
 * @author rtimerbaev
 */

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

// графа 1  - rowNumber
// графа 2  - contractNumber
// графа 3  - contraclData
// графа 4  - base
// графа 5  - transactionDate
// графа 6  - course
// графа 7  - interestRate
// графа 8  - basisForCalc
// графа 9  - calcPeriodAccountingBeginDate
// графа 10 - calcPeriodAccountingEndDate
// графа 11 - calcPeriodBeginDate
// графа 12 - calcPeriodEndDate
// графа 13 - accruedCommisCurrency
// графа 14 - accruedCommisRub
// графа 15 - commisInAccountingCurrency
// графа 16 - commisInAccountingRub
// графа 17 - accrualPrevCurrency
// графа 18 - accrualPrevRub
// графа 19 - reportPeriodCurrency
// графа 20 - reportPeriodRub

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    // если данных еще нет или строка не выбрана
    if (formData.dataRows.isEmpty() || currentDataRow == null ||
            getIndex(currentDataRow) == -1) {
        formData.dataRows.add(newRow)
    } else {
        formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)
    }

    // графа 2..12
    ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
            'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate',
            'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate'].each {
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
    // РНУ-22 за предыдущий отчетный период
    def formDataOld = getFormDataOld()

    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 2..12)
    def requiredColumns = ['contractNumber', 'contraclData', 'base',
            'transactionDate', 'course', 'interestRate', 'basisForCalc']
    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // удалить строку "итого"
    def total = null
    if (formData != null && !formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (row.getAlias() == 'total') {
                total = row
                break
            }
        }
    }
    if (total != null) {
        formData.dataRows.remove(total)
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    def tmp

    // графа 1, 13..20
    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 13
        row.accruedCommisCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

        // графа 14
        row.accruedCommisRub = round(row.accruedCommisCurrency * row.course, 2)

        // графа 15
        // TODO (Ramil Timerbaev) совпадает с 13ой графой (ответ: это нормально)
        row.commisInAccountingCurrency = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)

        // графа 16
        row.commisInAccountingRub = round(row.commisInAccountingCurrency * row.course, 2)

        // графа 17
        // TODO (Ramil Timerbaev) уточнят
        tmp = getSum(formDataOld, 'reportPeriodCurrency')
        row.accrualPrevCurrency = round(tmp, 2)

        // графа 18
        // TODO (Ramil Timerbaev) уточнят
        tmp = getSum(formDataOld, 'reportPeriodRub')
        row.accrualPrevRub = round(tmp, 2)

        // графа 19
        row.reportPeriodCurrency = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)

        // графа 20
        row.reportPeriodRub = round(row.reportPeriodCurrency * row.course, 2)
    }

    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.contractNumber = 'Итого'
    setTotalStyle(totalRow)

    // графы для которых надо вычислять итого (графа 13..20)
    def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
            'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
            'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(formData, alias))
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    // РНУ-22 за предыдущий отчетный период
    def formDataOld = getFormDataOld()

    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..8, 13..20)
        def requiredColumns = ['rowNumber', 'contractNumber', 'contraclData', 'base',
                'transactionDate', 'course', 'interestRate', 'basisForCalc',
                'accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                'reportPeriodCurrency', 'reportPeriodRub']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода (графа 13..20)
        def totalColumns = ['accruedCommisCurrency', 'accruedCommisRub',
                'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
                'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub']

        // признак наличия итоговых строк
        def hasTotal = false

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 7. Обязательность заполнения поля графы 1..8, 13..20
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
            if (a != null && b != null && (
            (row.transactionDate != null && (row.transactionDate < a || b < row.transactionDate)) ||
                    (row.calcPeriodAccountingEndDate != null && (row.calcPeriodAccountingEndDate < a && b < row.calcPeriodAccountingEndDate)) ||
                    (row.calcPeriodEndDate != null && (row.calcPeriodEndDate < a || b < row.calcPeriodEndDate)))) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                return false
            }

            // 2. Проверка на нулевые значения (графа 13..20)
            def allNull = true
            ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
                    'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
                    'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
                tmp = row.getCell(alias).getValue()
                if (tmp != null && tmp != 0) {
                    allNull = false
                }
            }
            if (allNull) {
                logger.error('Все суммы по операции нулевые!')
                return false
            }

            // 3. Проверка на сумму платы (графа 4)
            if (row.base != null && row.base == 0) {
                logger.warn('Суммы платы равны 0!')
            }

            // 4. Проверка задания расчётного периода
            if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate ||
                    row.calcPeriodBeginDate > row.calcPeriodEndDate) {
                logger.warn('Неправильно задан расчётный период!')
            }

            // 5. Проверка на корректность даты договора
            if (row.contraclData > b) {
                logger.error('Дата договора неверная!')
                return false
            }

            // 8. Проверка на заполнение поля «<Наименование поля>»
            // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
            def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                    (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
            // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
            def checkColumn11and12 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                    (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
            if (checkColumn9and10 || checkColumn11and12) {
                logger.error('Поля в графе 9, 10, 11, 12 заполены неверно!')
                return false
            }

            // 9. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1

            // 10. Арифметическая проверка графы 13
            tmp = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)
            if (row.accruedCommisCurrency != tmp) {
                logger.warn('Неверно рассчитана графа «Сумма начисленной комиссии. Валюта»!')
            }

            // 11. Арифметическая проверка графы 14
            if (row.accruedCommisRub != round(row.accruedCommisCurrency * row.course, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма начисленной комиссии. Рубли»!')
            }

            // 12. Арифметическая проверка графы 15
            tmp = getColumn13or15or19(row, row.calcPeriodAccountingBeginDate, row.calcPeriodAccountingEndDate)
            if (row.commisInAccountingCurrency != tmp) {
                logger.warn('Неверно рассчитана графа «Сумма комиссии, отражённая в бухгалтерском учёте. Валюта»!')
            }

            // 13. Арифметическая проверка графы 16
            if (row.commisInAccountingRub != round(row.commisInAccountingCurrency * row.course, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма комиссии, отражённая в бухгалтерском учёте. Рубли»!')
            }

            // 14. Арифметическая проверка графы 17
            // TODO (Ramil Timerbaev) уточнят
            tmp = getSum(formDataOld, 'reportPeriodCurrency')
            if (row.accrualPrevCurrency != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма доначисления. Предыдущий период. Валюта»!')
            }

            // 15. Арифметическая проверка графы 18
            // TODO (Ramil Timerbaev) уточнят
            tmp = getSum(formDataOld, 'reportPeriodRub')
            if (row.accrualPrevRub != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма доначисления. Предыдущий период. Рубли»!')
            }

            // 16. Арифметическая проверка графы 19
            tmp = getColumn13or15or19(row, row.calcPeriodBeginDate, row.calcPeriodEndDate)
            if (row.reportPeriodCurrency != tmp) {
                logger.warn('Неверно рассчитана графа «Сумма доначисления. Отчётный период. Валюта»!')
            }

            // 17. Арифметическая проверка графы 20
            if (row.reportPeriodRub != round(row.reportPeriodCurrency * row.course, 2)) {
                logger.warn('Неверно рассчитана графа «Сумма доначисления. Отчётный период. Рубли»!')
            }

            // 18. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 14, 16)
            if (totalRow.accruedCommisRub < totalRow.commisInAccountingRub) {
                logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
            }

            // 18. Проверка итогового значений по всей форме (графа 13..20)
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

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
    if (false) {
        logger.warn('Неверный курс валюты!')
        return false
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
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

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
 * Получить значение графа 13 (аналогично для графа 15 и графа 19)
 *
 * @param row строка нф
 * @param date1 дата начала
 * @param date2 дата окончания
 */
def getColumn13or15or19(def row, def date1, def date2) {
    if (date1 == null || date2 == null) {
        return 0
    }
    def division = row.basisForCalc * (date2 - date1 + 1)
    if (division == 0) {
        logger.error('Деление на ноль. Возможно неправильно выбраны даты.')
        return 0
    }
    return round((row.base * row.interestRate) / (division), 2)
}

/**
 * Получить сумму столбца.
 */
def getSum(def form, def columnAlias) {
    if (form == null) {
        return 0
    }
    def to = 0
    def from = form.dataRows.size() - 2
    if (to > from) {
        return 0
    }
    return summ(form, new ColumnRange(columnAlias, to, from))
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'contractNumber', 'contraclData', 'base', 'transactionDate',
            'course', 'interestRate', 'basisForCalc', 'calcPeriodAccountingBeginDate', 'calcPeriodAccountingEndDate', 'calcPeriodBeginDate', 'calcPeriodEndDate',
            'accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency',
            'commisInAccountingRub', 'accrualPrevCurrency', 'accrualPrevRub',
            'reportPeriodCurrency', 'reportPeriodRub'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // (РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования (За предыдущий отчетный период)
    def formDataOld = (prevReportPeriod != null ?
        FormDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, prevReportPeriod.id) : null)

    return formDataOld
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
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
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}