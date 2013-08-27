/**
 * Скрипт для РНУ-36.1 (rnu36-1.groovy).
 * Форма "(РНУ-36.1) Регистр налогового учёта начисленного процентного дохода по ГКО. Отчёт 1".
 *
 * @version 68
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- откуда брать последний отчетный день месяца?
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

// графа 1  - series
// графа 2  - amount
// графа 3  - nominal
// графа 4  - shortPositionDate
// графа 5  - balance2
// графа 6  - averageWeightedPrice
// графа 7  - termBondsIssued
// графа 8  - percIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        row = formData.getDataRow('totalA')
        formData.dataRows.add(getIndex(row), newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(getIndex(currentDataRow), newRow)
    } else {
        def row = formData.getDataRow('totalA')
        switch (currentDataRow.getAlias()) {
            case 'A' :
            case 'totalA' :
                row = formData.getDataRow('totalA')
                break
            case 'B' :
            case 'totalB' :
            case 'total' :
                row = formData.getDataRow('totalB')
                break
        }
        formData.dataRows.add(getIndex(row), newRow)
    }

    // графа 1..7
    ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    newRow.getCell('percIncome').styleAlias = 'Вычисляемая'
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
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

    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued']

    for (def row : formData.dataRows) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // последний отчетный день месяца
    // TODO (Ramil Timerbaev) откуда брать
    def lastDay = new Date()
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            // графа 8
            row.percIncome = getColumn8(row, lastDay)
        }
    }

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']
    def tmp
    def sum = 0
    ['A', 'B'].each {
        def row = formData.getDataRow(it)
        def totalRow = formData.getDataRow('total' + it)
        def from = getIndex(row) + 1
        def to = getIndex(totalRow) - 1
        if (from <= to) {
            totalColumns.each { alias ->
                tmp = summ(formData, new ColumnRange(alias, from, to))
                totalRow.getCell(alias).setValue(tmp)
            }
        }
        sum += totalRow.percIncome
    }

    // посчитать Итого
    formData.getDataRow('total').percIncome = sum
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['series', 'amount', 'nominal', 'shortPositionDate',
            'balance2', 'averageWeightedPrice', 'termBondsIssued']

    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def totalColumns = ['amount', 'percIncome']

    // последний день отчетного месяца
    // TODO (Ramil Timerbaev) откуда брать?
    def lastDay = new Date()

    // отчетная дата
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def reportDay = (tmp ? tmp.getTime() + 1 : null)

    def tmp
    for (def row : formData.dataRows) {
        if (isFixedRow(row)) {
            continue
        }

        // TODO (Ramil Timerbaev) в чтз нет проверки объязательных полей
        // . Проверка объязательных полей (графа 2..7)
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns, useLog)) {
            return false
        }

        // 1. Проверка даты приобретения (открытия короткой позиции) (графа 4)
        if (row.shortPositionDate > reportDay) {
            logger.error('Неверно указана дата приобретения (открытия короткой позиции)!')
            return false
        }

        // 2. Арифметическая проверка графы 8
        if (row.termBondsIssued != null || row.termBondsIssued != 0) {
            if (row.percIncome > getColumn8(row, lastDay)) {
                logger.warn('Неверно рассчитана графа «Процентный доход с даты приобретения»!')
            }
        }
    }

    def hasError = false

    // 3, 4. Проверка итоговых значений по разделу А и B
    // графы для которых надо вычислять итого А и Б (графа 2, 8)
    def sum = 0
    ['A', 'B'].each { section ->
        def row = formData.getDataRow(section)
        def totalRow = formData.getDataRow('total' + section)
        totalColumns.each { alias ->
            tmp = summ(formData, new ColumnRange(alias, getIndex(row) + 1, getIndex(totalRow) - 1))
            if (totalRow.getCell(alias).getValue() != tmp) {
                hasError = true
                logger.error("Итоговые значений для раздела $section рассчитаны неверно!")
            }
        }
        sum += totalRow.percIncome
    }
    if (hasError) {
        return false
    }

    // 5. Проверка итоговых значений по всей форме
    def totalRow = formData.getDataRow('total')
    if (totalRow.percIncome != sum) {
        logger.error('Итоговые значений рассчитаны неверно!')
        return false
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // графа 5
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует! ')
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
    // удалить нефиксированные строки
    def deleteRows = []
    formData.dataRows.each { row ->
        if (!isFixedRow(row)) {
            deleteRows += row
        }
    }
    deleteRows.each { row ->
        formData.dataRows.remove(getIndex(row))
    }

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                copyRows(source, formData, 'A', 'totalA')
                copyRows(source, formData, 'B', 'totalB')
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
    if (reportPeriod != null && reportPeriod.reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 *	Посчитать значение графы 8.
 *
 * @param row строка
 * @param lastDay последний день отчетного месяца
 */
def getColumn8(def row, def lastDay) {
    def tmp = ((row.nominal - row.averageWeightedPrice) *
            (lastDay - row.shortPositionDate) / row.termBondsIssued) * row.amount
    return round(tmp, 2)
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formData.dataRows.indexOf(row)
}

/**
 * Получить номер строки в таблице по псевдонимиу.
 */
def getIndex(def form, def rowAlias) {
    return form.dataRows.indexOf(form.getDataRow(rowAlias))
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
            logger.error("В строке \"Серия\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceForm форма источник
 * @param destinationForm форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceForm, def destinationForm, def fromAlias, def toAlias) {
    def from = getIndex(sourceForm, fromAlias) + 1
    def to = getIndex(sourceForm, toAlias)
    if (from > to) {
        return
    }
    sourceForm.dataRows.subList(from, to).each { row ->
        destinationForm.dataRows.add(getIndex(destinationForm, toAlias), row)
    }
}