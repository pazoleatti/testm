/**
 * Скрипт для РНУ-49 (rnu49.groovy).
 * Форма "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *      - уникальность инвентарного номера
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
// графа 2  - firstRecordNumber
// графа 3  - operationDate
// графа 4  - reasonNumber
// графа 5  - reasonDate
// графа 6  - invNumber
// графа 7  - name
// графа 8  - price
// графа 9  - amort
// графа 10 - expensesOnSale
// графа 11 - sum
// графа 12 - sumInFact
// графа 13 - costProperty
// графа 14 - marketPrice
// графа 15 - sumIncProfit
// графа 16 - profit
// графа 17 - loss
// графа 18 - usefullLifeEnd
// графа 19 - monthsLoss
// графа 20 - expensesSum
// графа 21 - saledPropertyCode
// графа 22 - saleCode
// графа 23 - propertyType

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    if (currentDataRow == null || getIndex(currentDataRow) == -1) {
        row = formData.getDataRow('totalA')
        formData.dataRows.add(getIndex(row), newRow)
    } else if (currentDataRow.getAlias() == null) {
        formData.dataRows.add(getIndex(currentDataRow) + 1, newRow)
    } else {
        def alias = currentDataRow.getAlias()
        def row = (alias.contains('total') ? formData.getDataRow(alias) : formData.getDataRow('total' + alias))
        formData.dataRows.add(getIndex(row), newRow)
    }

    // графа 2..14, 18, 19, 21..23
    ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
            'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
            'sum', 'sumInFact', 'costProperty', 'marketPrice', 'usefullLifeEnd',
            'monthsLoss', 'saledPropertyCode', 'saleCode', 'propertyType'].each {
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
    /*
     * Проверка объязательных полей.
     */

    // Список проверяемых столбцов (графа 2..14, 18, 19, 21..23)
    def requiredColumns = ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
            'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
            'sum', 'sumInFact', 'costProperty', 'marketPrice', 'usefullLifeEnd',
            'monthsLoss', 'saledPropertyCode', 'saleCode', 'propertyType']

    for (def row : formData.dataRows) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты.
     */

    // графа 1, 15..17, 20
    formData.dataRows.eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            // графа 1
            row.rowNumber = i + 1

            // графа 15
            if (row.sum - row.marketPrice * 0.8 > 0) {
                row.sumIncProfit = 0
            } else {
                row.sumIncProfit = row.marketPrice * 0.8 - row.sum
            }

            // графа 16
            row.profit = row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit

            // графа 17
            row.loss = row.profit

            // графа 20
            if (row.monthsLoss != 0) {
                row.expensesSum = round(row.loss / row.monthsLoss, 2)
            } else {
                row.expensesSum = 0
                def column = getColumnName(row, 'monthsLoss')
                logger.error("Деление на ноль. Возможно неправильное значение в графе \"$column\".")
            }
        }
    }

    // подразделы
    ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
        firstRow = formData.getDataRow(section)
        lastRow = formData.getDataRow('total' + section)
        // графы для которых считать итого (графа 9..13, 15..20)
        ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                'profit', 'loss', /*'usefullLifeEnd',*/ 'monthsLoss', 'expensesSum'].each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
}

/**
 * Логические проверки.
 *
 * useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {
        // Список проверяемых столбцов (графа 1..22)
        def columns = ['rowNumber', 'firstRecordNumber', 'operationDate', 'reasonNumber',
                'reasonDate', 'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
                'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit',
                'profit', 'loss', 'usefullLifeEnd', 'monthsLoss', 'expensesSum',
                'saledPropertyCode', 'saleCode']

        for (def row : formData.dataRows) {
            if (isFixedRow(row)) {
                continue
            }

            // 1. Обязательность заполнения поля графы (графа 1..22)
            if (!checkRequiredColumns(row, columns, useLog)) {
                return false
            }

            // 2. Проверка на уникальность поля «инвентарный номер» (графа 6)
            // TODO (Ramil Timerbaev) Как должна производиться эта проверка?
            if (false) {
                logger.warn('Инвентарный номер не уникальный!')
            }

            // 3. Проверка на нулевые значения (графа 8, 13, 15, 17, 20)
            if (row.price == 0 &&
                    row.costProperty == 0 &&
                    row.sumIncProfit == 0 &&
                    row.loss == 0 &&
                    row.expensesSum == 0) {
                logger.error('Все суммы по операции нулевые!')
                return false
            }
            // 4. Проверка формата номера первой записи	Формат графы 2: ГГ-НННН
            if (!row.firstRecordNumber.matches('\\w{2}-\\w{6}')) {
                logger.error('Неправильно указан номер предыдущей записи!')
                return false
            }

            // 5. Арифметическая проверка графы 15
            def hasError
            if (row.sum - row.marketPrice * 0.8 > 0) {
                hasError = (row.sumIncProfit != 0)
            } else {
                hasError = (row.sumIncProfit != (row.marketPrice * 0.8 - row.sum))
            }
            if (hasError) {
                logger.error('Неверное значение графы «Сумма к увеличению прибыли (уменьшению убытка)»!')
                return false
            }

            // 6. Арифметическая проверка графы 16
            if (row.profit != (row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit)) {
                logger.error('Неверное значение графы «Прибыль от реализации»!')
                return false
            }

            // 7. Арифметическая проверка графы 17
            if (row.loss != row.profit) {
                logger.error('Неверное значение графы «Убыток от реализации»!')
                return false
            }

            // 8. Арифметическая проверка графы 20
            if (row.monthsLoss != 0 && row.expensesSum != round(row.loss / row.monthsLoss, 2)) {
                logger.error('Неверное значение графы «Сумма расходов, приходящаяся на каждый месяц»!')
                return false
            }

            // 9. Проверка итоговых значений формы
            // графы для которых считать итого (графа 9..13, 15..20)
            def totalColumns = ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                    'profit', 'loss', /*'usefullLifeEnd',*/ 'monthsLoss', 'expensesSum']
            // подразделы
            for (def section : ['A', 'B', 'V', 'G', 'D', 'E']) {
                firstRow = formData.getDataRow(section)
                lastRow = formData.getDataRow('total' + section)
                for (def column : totalColumns) {
                    if (lastRow.getCell(column).getValue().equals(getSum(column, firstRow, lastRow))) {
                        logger.error('Итоговые значения рассчитаны неверно!')
                        return false
                    }
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
        for (def row : formData.dataRows) {
            if (isFixedRow(row)) {
                continue
            }

            // 1. Проверка шифра при реализации амортизируемого имущества
            // Графа 21 (группа «А») = 1 или 2, и графа 22 = 1
            if (isSection('A', row) &&
                    ((row.saledPropertyCode != 1 && row.saledPropertyCode != 2) || row.saleCode != 1)) {
                logger.error('Для реализованного амортизируемого имущества (группа «А») указан неверный шифр!')
                return false
            }

            // 2. Проверка шифра при реализации прочего имущества
            // Графа 21 (группа «Б») = 3 или 4, и графа 22 = 1
            if (isSection('B', row) &&
                    ((row.saledPropertyCode != 3 && row.saledPropertyCode != 4) || row.saleCode != 1)) {
                logger.error('Для реализованного прочего имущества (группа «Б») указан неверный шифр!')
                return false
            }

            // 3. Проверка шифра при списании (ликвидации) амортизируемого имущества
            // Графа 21 (группа «В») = 1 или 2, и графа 22 = 2
            if (isSection('V', row) &&
                    ((row.saledPropertyCode != 1 && row.saledPropertyCode != 2) || row.saleCode != 2)) {
                logger.error('Для списанного (ликвидированного) амортизируемого имущества (группа «В») указан неверный шифр!')
                return false
            }

            // 4. Проверка шифра при реализации имущественных прав (кроме прав требования, долей паёв)
            // Графа 21 (группа «Г») = 5, и графа 22 = 1
            if (isSection('G', row) &&
                    (row.saledPropertyCode != 5 || row.saleCode != 1)) {
                logger.error('Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Г») указан неверный шифр!')
                return false
            }

            // 5. Проверка шифра при реализации прав на земельные участки
            // Графа 21 (группа «Д») = 6, и графа 22 = 1
            if (isSection('D', row) &&
                    (row.saledPropertyCode != 6 || row.saleCode != 1)) {
                logger.error('Для реализованных прав на земельные участки (группа «Д») указан неверный шифр!')
                return false
            }

            // 6. Проверка шифра при реализации долей, паёв
            if (isSection('E', row) &&
                    (row.saledPropertyCode != 7 || row.saleCode != 1)) {
                logger.error('Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Е») указан неверный шифр!')
                return false
            }

            // 7. Проверка актуальности поля «Шифр вида реализованного (выбывшего) имущества»
            // Проверка соответствия «графы 21» справочным данным справочника «Шифр вида реализованного (выбывшего) имущества»
            if (false) {
                logger.warn('Шифр вида реализованного (выбывшего) имущества в справочнике отсутствует!')
            }

            // 8. Проверка актуальности поля «Шифр вида реализации (выбытия)»	Проверка соответствия «графы 22» справочным данным справочника «Шифр вида реализации (выбытия)»
            if (false) {
                logger.warn('Шифр вида реализации (выбытия) в справочнике отсутствует!')
            }

            // 9. Проверка актуальности поля «Тип имущества»
            // Проверка соответствия «графы 23» справочным данным справочника «Тип имущества»
            if (false) {
                logger.warn('Тип имущества в справочнике не найден!')
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
                ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
                    copyRows(source, formData, section, 'total' + section)
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
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

def isSection(def section, def row) {
    def sectionRow = formData.getDataRow(section)
    def totalRow = formData.getDataRow('total' + section)
    return getIndex(row) > getIndex(sectionRow) && getIndex(row) < getIndex(totalRow)
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
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceForm форма источник
 * @param destinationForm форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
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