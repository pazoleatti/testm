/**
 * Скрипт для РНУ-40.2 (rnu40_2.groovy).
 * Форма "(РНУ-40.2) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 2".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
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
        logicalCheck()
        checkNSI()
        break
}

// графа 1  - number
// графа 2  - name
// графа 3  - code
// графа 4  - cost
// графа 5  - bondsCount
// графа 6  - percent

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        row = formData.getDataRow('2')
        formData.dataRows.add(getIndex(row), newRow)
    } else {
        formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)
    }

    // графа 1..6
    ['number', 'name', 'code', 'cost', 'bondsCount', 'percent'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
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

}

/**
 * Логические проверки.
 */
def logicalCheck() {
    // 1. Обязательность заполнения поля графы 1-6
    // список проверяемых столбцов (графа 1..6)
    def requiredColumns = ['number', 'name', 'code', 'cost', 'bondsCount', 'percent']

    for (def row : formData.dataRows) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    // 1. Проверка актуальности поля «Номер территориального банка»	(графа 1)
    if (false) {
        logger.warn('Неверный номер территориального банка!')
    }

    // 2. Проверка актуальности поля «Наименование территориального банка / подразделения Центрального аппарата» (графа 2)
    if (false) {
        logger.warn('Неверное наименование территориального банка/ подразделения Центрального аппарата!')
    }

    // 3. Проверка актуальности поля «Код валюты номинала» (графа 3)
    if (false) {
        logger.error('Неверный код валюты!')
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
                // подразделы
                (1..5).each { section ->
                    copyRows(source, formData, section.toString(), (section + 1).toString())
                }
                copyRows(source, formData, '6', null)
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
    if (!logicalCheck() || !checkNSI()) {
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
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"Номер территориального банка\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В $index строке не заполнены колонки : $errorMsg.")
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
    def to = (toAlias != null ? getIndex(sourceForm, toAlias) : sourceForm.dataRows.size())
    if (from > to) {
        return
    }
    sourceForm.dataRows.subList(from, to).each { row ->
        if (toAlias != null) {
            destinationForm.dataRows.add(getIndex(destinationForm, toAlias), row)
        } else {
            destinationForm.dataRows.add(row)
        }
    }
}