/**
 * Скрипт для РНУ-30 (rnu30.groovy).
 * Форма "(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.".
 *
 * @version 59
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *		- логическая проверка 13 уникальность поля ПП (графа 1)
 *		- 13 графу считать только для первых строк или для всех?
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

// графа 0  - forLabel - для вывода надписей
// графа 1  - number
// графа 2  - debtor
// графа 3  - provision
// графа 4  - nameBalanceAccount
// графа 5  - debt45_90DaysSum
// графа 6  - debt45_90DaysNormAllocation50per
// графа 7  - debt45_90DaysReserve
// графа 8  - debtOver90DaysSum
// графа 9  - debtOver90DaysNormAllocation100per
// графа 10 - debtOver90DaysReserve
// графа 11 - totalReserve
// графа 12 - reservePrev
// графа 13 - reserveCurrent
// графа 14 - calcReserve
// графа 15 - reserveRecovery
// графа 16 - useReserve

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.createDataRow()
    def row

    if (currentDataRow == null ||
            getIndex(currentDataRow) == -1 ||
            'total'.equals(currentDataRow.getAlias()) ||
            isFirstSection(currentDataRow)) {

        // в первые строки
        row = formData.getDataRow('total')
        setEdit(newRow, null)
    } else if ('totalA'.equals(currentDataRow.getAlias()) ||
            'A'.equals(currentDataRow.getAlias()) ||
            isSection(currentDataRow, 'A')) {

        // в раздел А
        row = formData.getDataRow('totalA')
        setEdit(newRow, 'A')
    } else if ('totalAll'.equals(currentDataRow.getAlias()) ||
            'totalB'.equals(currentDataRow.getAlias()) ||
            'B'.equals(currentDataRow.getAlias()) ||
            isSection(currentDataRow, 'B')) {

        // в раздел Б
        row = formData.getDataRow('totalB')
        setEdit(newRow, 'B')
    }
    formData.dataRows.add(getIndex(row), newRow)
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

    // Список проверяемых столбцов для первых строк (графа 2..5, 8, 12, 16)
    requiredColumns1 = ['debtor', 'provision', 'nameBalanceAccount', 'debt45_90DaysSum',
            'debtOver90DaysSum', 'reservePrev', 'useReserve']

    // Список проверяемых столбцов для раздела А и Б (графа 2, 4, 12, 16)
    requiredColumnsAB = ['debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

    for (def row : formData.dataRows) {
        if (!isFixedRow(row)) {
            def requiredColumns = (isFirstSection(row) ? requiredColumns1 : requiredColumnsAB)
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return
            }
        }
    }

    /*
     * Расчеты
     */

    def isFirst
    def tmp
    def index = 1
    formData.dataRows.eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            isFirst = isFirstSection(row)

            // графа 1
            row.number = index
            index += 1

            if (isFirst) {
                // графа 6
                row.debt45_90DaysNormAllocation50per = 50

                // графа 7
                tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
                row.debt45_90DaysReserve = round(tmp, 2)

                // графа 9
                row.debtOver90DaysNormAllocation100per = 100

                // графа 10
                tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
                row.debtOver90DaysReserve = round(tmp, 2)

                // графа 11
                row.totalReserve = row.debt45_90DaysReserve + row.debtOver90DaysReserve

                // графа 14
                row.calcReserve = (row.totalReserve + row.useReserve > row.reservePrev ?
                    row.totalReserve + row.useReserve - row.reservePrev : 0)

                // графа 15
                row.reserveRecovery = (row.totalReserve + row.useReserve < row.reservePrev ?
                    row.reservePrev - (row.totalReserve + row.useReserve) : 0)

                // TODO (Ramil Timerbaev) уточнить: 13 графу считать только для первых строк или для всех?
                // графа 13 - стоит поле остальных потому что в расчетах используются графа 14, 15
                row.reserveCurrent = row.reservePrev + row.calcReserve -
                        row.reserveRecovery - row.useReserve
            }
        }
    }

    // Первые строки (графа 5, 7, 8, 10..16)
    def totalRow = formData.getDataRow('total')
    ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias, totalRow))
    }

    def aRow = formData.getDataRow('A')
    def totalARow = formData.getDataRow('totalA')

    def bRow = formData.getDataRow('B')
    def totalBRow = formData.getDataRow('totalB')

    //  раздел А и Б (графа 12, 13, 16)
    ['reservePrev', 'reserveCurrent', 'useReserve'].each { alias ->
        totalARow.getCell(alias).setValue(getSum(alias, aRow, totalARow))
        totalBRow.getCell(alias).setValue(getSum(alias, bRow, totalBRow))
    }

    // Всего (графа 5, 7, 8, 10..16)
    def totalAllRow = formData.getDataRow('totalAll')
    ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        tmp = getValue(totalRow.getCell(alias).getValue()) +
                getValue(totalARow.getCell(alias).getValue()) +
                getValue(totalBRow.getCell(alias).getValue())
        totalAllRow.getCell(alias).setValue(tmp)
    }
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    if (!formData.dataRows.isEmpty()) {
        def i = 1

        // для первых строк - графы 1..16
        requiredColumns1 = ['number', 'debtor', 'provision', 'nameBalanceAccount',
                'debt45_90DaysSum', 'debt45_90DaysNormAllocation50per',
                'debt45_90DaysReserve', 'debtOver90DaysSum',
                'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve',
                'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve',
                'reserveRecovery', 'useReserve']

        //  для раздера А и Б - графы 1, 2, 4, 12, 16
        requiredColumnsAB = ['number', 'debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

        def tmp
        def isFirst
        for (def row : formData.dataRows) {
            if (isFixedRow(row)) {
                continue
            }

            isFirst = isFirstSection(row)

            // 1. Обязательность заполнения полей
            def requiredColumns = (isFirst ? requiredColumns1 :  requiredColumnsAB)

            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            if (isFirst) {
                // 2. Арифметическая проверка графы 7
                tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
                if (row.debt45_90DaysReserve != round(tmp, 2)) {
                    logger.error('Неверно рассчитана графа «Задолженность от 45 до 90 дней. Расчётный резерв»!')
                    return false
                }

                // 3. Арифметическая проверка графы 10
                tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
                if (row.debtOver90DaysReserve != round(tmp, 2)) {
                    logger.error('Неверно рассчитана графа «Задолженность более 90 дней. Расчётный резерв»!')
                    return false
                }

                // 4. Арифметическая проверка графы 11
                if (row.totalReserve != row.debt45_90DaysReserve + row.debtOver90DaysReserve) {
                    logger.error('Наверное значение графы «Итого расчётный резерв»')
                    return false
                }

                // 5. Арифметическая проверка графы 13
                // TODO (Ramil Timerbaev) уточнить: 13 графу считать только для первых строк или для всех?
                tmp = row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
                if (row.reserveCurrent != tmp) {
                    logger.error('Неверно рассчитана графа «Резерв на отчётную дату. Текущую»!')
                    return false
                }

                // 6. Арифметическая проверка графы 14
                tmp = (row.totalReserve + row.useReserve > row.reservePrev ?
                    row.totalReserve + row.useReserve - row.reservePrev : 0)
                if (row.calcReserve != tmp) {
                    logger.error('Неверно рассчитана графа «Изменение фактического резерва. Доначисление резерва с отнесением на расходы код 22670»!')
                    return false
                }

                // 7. Арифметическая проверка графы 15
                tmp = (row.totalReserve + row.useReserve < row.reservePrev ?
                    row.reservePrev - (row.totalReserve + row.useReserve) : 0)
                if (row.reserveRecovery != tmp) {
                    logger.error('Неверно рассчитана графа «Изменение фактического резерва. Восстановление резерва на доходах код 13091»!')
                    return false
                }

                // 8. Арифметическая проверка графы 6
                if (row.debt45_90DaysNormAllocation50per != 50) {
                    logger.error('Неверно рассчитана графа «Задолженность от 45 до 90 дней. Норматив отчислений 50%»!')
                    return false
                }

                // 9. Арифметическая проверка графы 9
                if (row.debtOver90DaysNormAllocation100per != 100) {
                    logger.error('Неверно рассчитана графа «Задолженность более 90 дней. Норматив отчислений 100%»!')
                    return false
                }
            }

            // 13. Проверка на уникальность поля "№ пп" (графа 1)
            // TODO (Ramil Timerbaev) ПОД ВОПРОСОМ
            if (i != row.number) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1
        }

        def columns
        def cell

        // 10. Проверка итоговых значений по строкам, не входящим в состав раздело А и Б (графа 5, 7, 8, 10..16)
        def totalRow = formData.getDataRow('total')
        columns = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
                'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
                'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
        for (def alias : columns) {
            cell = totalRow.getCell(alias)
            if (cell.getValue() != getSum(alias, totalRow)) {
                def name = cell.getColumn().getName()
                logger.error("Итоговые значения для \"$name\" рассчитаны неверно!")
                return false
            }
        }

        // 11 + 12. Проверка итоговых значений по строкам из раздела А и B
        def aRow = formData.getDataRow('A')
        def totalARow = formData.getDataRow('totalA')

        def bRow = formData.getDataRow('B')
        def totalBRow = formData.getDataRow('totalB')

        //  раздел А и Б (графа 12, 13, 16)
        columns = ['reservePrev', 'reserveCurrent', 'useReserve']
        for (def alias : columns) {
            cell = totalARow.getCell(alias)
            if (cell.getValue() != getSum(alias, aRow, totalARow)) {
                def name = cell.getColumn().getName()
                logger.error("Итоговые значения для \"$name\" раздела А рассчитаны неверно!")
                return false
            }
            cell = totalBRow.getCell(alias)
            if (cell.getValue() != getSum(alias, bRow, totalBRow)) {
                def name = cell.getColumn().getName()
                logger.error("Итоговые значения для \"$name\" раздела Б рассчитаны неверно!")
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

            // 1. Проверка актуальности поля «Обеспечение» (графа 3)
            if (false) {
                logger.warn('Обеспечение в справочнике отсутствует!')
            }

            // 2. Проверка счёта бухгалтерского учёта для данного РНУ (графа 4)
            if (false) {
                logger.error('Операция в РНУ не учитывается!')
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
                copyRows(source, formData, null, 'total')
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
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
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowEnd) {
    def from = 0
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Проверить принадлежит ли указанная строка к первому разделу (до строки "итого").
 */
def isFirstSection(def row) {
    return row != null && getIndex(row) < getIndex(formData.getDataRow('total'))
}

/**
 * Проверить принадлежит ли указанная строка к разделу (A или B).
 */
def isSection(def row, def section) {
    return row != null &&
            getIndex(row) > getIndex(formData.getDataRow(section)) &&
            getIndex(row) < getIndex(formData.getDataRow('total' + section))
}

/**
 * Задать редактируемые графы в зависимости от раздела.
 *
 * @param row строка
 * @param section раздел: A, B или пустая строка (первые строки)
 */
def setEdit(def row, def section) {
    if (row == null) {
        return
    }
    def editColumns
    if (section == '' || section == null) {
        // первые строки (графа 2..5, 8, 12, 16)
        editColumns = ['debtor', 'provision', 'nameBalanceAccount', 'debt45_90DaysSum',
                'debtOver90DaysSum', 'reservePrev', 'useReserve']
    } else {
        // раздел А или Б (графа 2, 4, 12, 16)
        editColumns = ['debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']
    }

    editColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * Проверить значение на пустоту и вернуть его.
 */
def getValue(def value) {
    return value ?: 0
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
    def from = (fromAlias != null ? getIndex(sourceForm, fromAlias) + 1 : 0)
    def to = getIndex(sourceForm, toAlias)
    if (from > to) {
        return
    }
    sourceForm.dataRows.subList(from, to).each { row ->
        destinationForm.dataRows.add(getIndex(destinationForm, toAlias), row)
    }
}