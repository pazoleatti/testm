/**
 * Скрипт для РНУ-5 (rnu5.groovy).
 * Форма "(РНУ-5) Простой регистр налогового учёта «расходы»".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - консолидация
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
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
    // подготовить
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
    // принять
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        // TODO (Ramil Timerbaev) нужен ли тут пересчет данных
        break
    // вернуть из принята в подготовлена
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED : // TODO (Ramil Timerbaev) поправить на правильное событие
        acceptance()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1 - rowNumber
// графа 2 - code
// графа 3 - balance
// графа 4 - name
// графа 5 - sum

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)

    ['code', 'balance', 'name', 'sum'].each {
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

    // список проверяемых столбцов (графа 2..5)
    def requiredColumns = ['code', 'balance', 'name', 'sum']

    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            hasError = true
        }
    }
    if (hasError) {
        return
    }

    /*
     * Расчеты.
     */

    /** Сумма "Итого". */
    def total = 0

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    formData.dataRows.each {
        if (isTotal(it)) {
            delRow += it
        }
    }
    delRow.each {
        formData.dataRows.remove(getIndex(it))
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.code }

    // нумерация (графа 1) и посчитать "итого"
    formData.dataRows.eachWithIndex { row, i ->
        row.rowNumber = i + 1
        total += row.sum
    }

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sum = 0
    formData.dataRows.eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewRow(tmp, sum))
            sum = 0
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == formData.dataRows.size() - 1) {
            sum += row.sum
            totalRows.put(i + 1, getNewRow(row.code, sum))
            sum = 0
        }

        sum += row.sum
        tmp = row.code
    }
    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }

    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    formData.dataRows.add(totalRow)
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
    totalRow.sum = total
    setTotalStyle(totalRow)
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..5)
        requiredColumns = ['rowNumber', 'code', 'balance', 'name', 'sum']

        def totalSum = 0
        def hasTotal = false
        def sums = [:]

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }
            // 1. Обязательность заполнения полей (графа 1..5)
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            // 2. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1

            // 3. Проверка итогового значения по коду для графы 5
            sums[row.code] = (sums[row.code] != null ? sums[row.code] : 0) + row.sum

            totalSum += row.sum
        }

        if (hasTotal) {
            // 3. Проверка итогового значения по коду для графы 5
            def hindError = false
            sums.each { code, sum ->
                def row = formData.getDataRow('total' + code)
                if (row.sum != sum && !hindError) {
                    hindError = true
                    logger.error("Неверное итоговое значение по коду $code графы «Сумма расходов за отчётный период (руб.)»!")
                }
            }
            if (hindError) {
                return false
            }

            // 4. Проверка итогового значения по всем строкам для графы 5
            def totalRow = formData.getDataRow('total')
            if (totalRow.sum != totalSum) {
                logger.error('Неверное итоговое значение графы «Сумма расходов за отчётный период (руб.)»!')
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
    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // Проверка балансового счёта для кода классификации расхода (графа 2)
            if (false) {
                logger.warn('Балансовый счёт в справочнике отсутствует!')
            }

            // Проверка кода классификации расхода для данного РНУ (графа 2)
            if (false) {
                logger.error('Операция в РНУ не учитывается!')
                return false
            }

            // Проверка балансового счёта для кода классификации расхода (графа 3)
            if (false) {
                logger.warn('Балансовый счёт в справочнике отсутствует!')
                return false
            }
        }
    }
    return true
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getDestinations(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.PRIMARY).each() {
        formDataCompositionService.compose(it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    if (!isBank()) {
        return
    }
    boolean isFirst = true;

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), FormDataKind.PRIMARY).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def child = FormDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (child != null && child.state == WorkflowState.ACCEPTED) {
                if (isFirst) {
                    // Удалить все строки и собрать из источников их строки
                    formData.dataRows.clear()
                    isFirst = false;
                    child.getDataRows().each { row->
                        def newRow = formData.createDataRow()
                        formData.dataRows.add(newRow)
                        newRow.putAll(row)
                        newRow.setAlias(row.getAlias())
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной первичной формы прошло успешно.')
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
def checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getDestinations(formData.getDepartmentId(), formData.getFormType().getId(), FormDataKind.PRIMARY).each { department ->
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
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    if (!isTerBank()) {
        return
    }

    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), FormDataKind.PRIMARY);
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = FormDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
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
 * Получить новую строку.
 */
def getNewRow(def alias, def sum) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    newRow.sum = sum
    newRow.code = 'Итого по коду'
    newRow.balance = alias
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'code', 'balance', 'name', 'sum'].each {
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

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.PRIMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}