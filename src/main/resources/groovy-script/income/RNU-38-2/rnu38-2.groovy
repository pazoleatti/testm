/**
 * Скрипт для РНУ-38.2 (rnu38-2.groovy).
 * Форма "РНУ-38.2) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 2".
 *
 * @version 59
 *
 * TODO:
 *      - сколько строк в рну?
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
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
        logicalCheck()
        break
}

// графа 1  - amount
// графа 2  - incomePrev
// графа 3  - incomeShortPosition
// графа 4  - totalPercIncome

/**
 * Добавить новую строку.
 */
def addNewRow() {
    if (formData.dataRows.size == 0) {
        formData.dataRows.add(formData.createDataRow())
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
    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        // уточнить нужно ли убрать данные если нет данных в рну-38.1
        formData.dataRows.each { row ->
            // графа 1
            row.amount = null

            // графа 2
            row.incomePrev = null

            // графа 3
            row.incomeShortPosition = null

            // графа 4
            row.totalPercIncome = null
        }
        return
    }

    /*
     * Расчеты.
     */

    formData.dataRows.each { row ->
        // графа 1
        row.amount = totalRow.amount

        // графа 2
        row.incomePrev = totalRow.incomePrev

        // графа 3
        row.incomeShortPosition = totalRow.incomeShortPosition

        // графа 4
        row.totalPercIncome = row.incomePrev + row.incomeShortPosition
    }
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def totalRow = getTotalRowFromRNU38_1()
    if (totalRow == null) {
        logger.error('Отсутствует РНУ-38.1.')
        return false
    }
    if (formData.dataRows.isEmpty()) {
        logger.error('Отсутствуют данные')
        return false
    }
    def row = formData.dataRows.get(0)

    // TODO (Ramil Timerbaev) для отладки консолидации, потом убрать
//    ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each {
//        row.getCell(it).setValue(1)
//    }
//    return true

    // 1. Обязательность заполнения поля графы 1..4
    def colNames = []
    // Список проверяемых столбцов (графа 1..4)
    ['amount', 'incomePrev', 'incomeShortPosition', 'totalPercIncome'].each {
        if (row.getCell(it).getValue() == null) {
            colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        def index = formData.dataRows.indexOf(row) + 1
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
        return false
    }

    // 2. Арифметическая проверка графы 1
    if (row.amount != totalRow.amount) {
        logger.error('Неверно рассчитана графа «Количество (шт.)»!')
        return false
    }

    // 3. Арифметическая проверка графы 2
    if (row.incomePrev != totalRow.incomePrev) {
        logger.error('Неверно рассчитана графа «Доход с даты погашения предыдущего купона (руб.коп.)»!')
        return false
    }

    // 4. Арифметическая проверка графы 3
    if (row.incomeShortPosition != totalRow.incomeShortPosition) {
        logger.error('Неверно рассчитана графа «Доход с даты открытия короткой позиции, (руб.коп.)»!')
        return false
    }

    // 5. Арифметическая проверка графы 4
    if (row.totalPercIncome != row.incomePrev + row.incomeShortPosition) {
        logger.error('Неверно рассчитана графа «Всего процентный доход (руб.коп.)»!')
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
    if (!logicalCheck()) {
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
 * Получить итоговую строку из нф (РНУ-38.1) Регистр налогового учёта начисленного процентного дохода по ОФЗ, по которым открыта короткая позиция. Отчёт 1.
 */
def getTotalRowFromRNU38_1() {
    def formDataRNU_38_1 = FormDataService.find(334, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU_38_1 != null) {
        for (def row : formDataRNU_38_1.dataRows) {
            if (row.getAlias() == 'total') {
                return row
            }
        }
    }
    return null
}