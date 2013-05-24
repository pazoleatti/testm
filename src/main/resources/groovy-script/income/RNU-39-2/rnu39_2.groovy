/**
 * Скрипт для РНУ-39.2 (rnu39_2.groovy).
 * Форма "(РНУ-39.2) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 2(квартальный)".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - консолидация (объединение строк в рамках выделенных разделов)
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
        // TODO (Ramil Timerbaev) нужен ли тут пересчет данных
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - currencyCode
// графа 2  - issuer
// графа 3  - regNumber
// графа 4  - amount
// графа 5  - cost
// графа 6  - shortPositionOpen
// графа 7  - shortPositionClose
// графа 8  - pkdSumOpen
// графа 9  - pkdSumClose
// графа 10 - maturityDatePrev
// графа 11 - maturityDateCurrent
// графа 12 - currentCouponRate
// графа 13 - incomeCurrentCoupon
// графа 14 - couponIncome
// графа 15 - totalPercIncome
// графа 16 - positionType
// графа 17 - securitiesGroup

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        row = formData.getDataRow('totalA1')
        formData.dataRows.add(getIndex(row), newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)
    } else {
        def alias = currentDataRow.getAlias()
        def row
        if (alias == 'A') {
            row = formData.getDataRow('totalA1')
        } else if (alias == 'B') {
            row = formData.getDataRow('totalB1')
        } else if (alias.contains('total')) {
            row = formData.getDataRow(alias)
        } else {
            row = formData.getDataRow('total' + alias)
        }
        formData.dataRows.add(getIndex(row), newRow)
    }

    // графа 1..17
    ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost', 'shortPositionOpen',
            'shortPositionClose', 'pkdSumOpen', 'pkdSumClose', 'maturityDatePrev',
            'maturityDateCurrent', 'currentCouponRate', 'incomeCurrentCoupon',
            'couponIncome', 'totalPercIncome', 'positionType', 'securitiesGroup'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
        formData.dataRows.remove(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 1..6, 10..13, 16, 17)
    def requiredColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost',
            'shortPositionOpen', 'maturityDatePrev', 'maturityDateCurrent',
            'currentCouponRate', 'incomeCurrentCoupon',	'positionType', 'securitiesGroup']
    for (def row : formData.dataRows) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // подразделы
    ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5'].each { section ->
        firstRow = formData.getDataRow(section)
        lastRow = formData.getDataRow('total' + section)
        // графы для которых считать итого (графа 4, 5, 8, 9, 14, 15)
        ['amount', 'cost', 'pkdSumOpen', 'pkdSumClose', 'couponIncome', 'totalPercIncome'].each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def tmp

    /** Отчетная дата. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def reportDateEnd = (tmp ? tmp.getTime() : null) // TODO (Ramil Timerbaev) Уточнить

    /** Начальная дата отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def reportDateStart = (tmp ? tmp.getTime() : null)

    // список проверяемых столбцов (графа 1..6, 10..13, 16, 17)
    def requiredColumns = ['currencyCode', 'issuer', 'regNumber', 'amount', 'cost',
            'shortPositionOpen', 'maturityDatePrev', 'maturityDateCurrent',
            'currentCouponRate', 'incomeCurrentCoupon',	'positionType', 'securitiesGroup']

    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }

        // 3. Обязательность заполнения поля графы 1-6, 10-13 , 16, 17
        if (!checkRequiredColumns(row, requiredColumns, useLog)) {
            return false
        }

        // 1. Проверка даты первой части сделки
        if (row.shortPositionOpen > reportDateEnd) {
            logger.error('Неверно указана дата первой части сделки!')
            return false
        }
    }

    // 2. Проверка даты второй части сделки
    def hasError = false
    def needCheck = true
    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }
        if (isSectionA(row) && row.shortPositionClose != null) {
            needCheck = false
            break
        }
        // TODO (Ramil Timerbaev) уточнить последнее условие
        if (!isSectionA(row) &&
                (row.shortPositionClose > reportDateEnd || row.shortPositionClose < reportDateStart)) {
            hasError = true
            break
        }
    }
    if (needCheck && hasError) {
        logger.error('Неверно указана дата второй части сделки!')
        return false
    }

    // 4..13. Проверка итоговых значений для подраздела 1..5 раздела А и Б
    // графа 4, 5, 8, 9, 14, 15
    sumColumns = ['amount', 'cost', 'pkdSumOpen', 'pkdSumClose', 'couponIncome', 'totalPercIncome']
    // 10 подразделов
    ['A1', 'A2', 'A3', 'A4', 'A5', 'B1', 'B2', 'B3', 'B4', 'B5'].each { section ->
        firstRow = formData.getDataRow(section)
        lastRow = formData.getDataRow('total' + section)
        // графы для которых считать итого (графа 4, 5, 8, 9, 14, 15)
        for (def col : sumColumns) {
            if (lastRow.getCell(col).getValue() != getSum(col, firstRow, lastRow)) {
                def number = section[1]
                def sectionName = (section.contains('A') ? 'А' : 'Б')
                logger.error("Итоговые значения для подраздела $number раздела $sectionName рассчитаны неверно!")
                return false
            }
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка кода валюты со справочным (графа 1)
    if (false) {
        logger.warn('Неверный код валюты!')
    }

    // 2. Проверка на наличие данных в справочнике купонов ценных бумаг
    if (false) {
        // TODO (Ramil Timerbaev)
        logger.warn('Для ценной бумаги <Номер государственной регистрации  из справочника ценных бумаг> отсутствует купон с датой погашения в отчётном периоде либо позднее!')
    }

    // 3. Проверка актуальности поля «Тип позиции»
    if (false) {
        logger.warn('Тип позиции в справочнике отсутствует!')
    }

    // 4. Проверка актуальности поля «Группа ценных бумаг»
    if (false) {
        logger.warn('Группа ценных бумаг с справочнике отсутствует!')
    }

    // 5. Проверка номера государственной регистрации
    if (false) {
        logger.warn('Неверный номер государственной регистрации!')
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
    // TODO (Ramil Timerbaev) поменять

    // удалить все строки и собрать из источников их строки
    formData.dataRows.clear()

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
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
    logger.info('Формирование консолидированной первичной формы прошло успешно.')
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Проверка принадлежит ли строка разделу A.
 */
def isSectionA(def row) {
    return row != null && getIndex(row) < getIndex(formData.getDataRow('B'))
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
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
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}