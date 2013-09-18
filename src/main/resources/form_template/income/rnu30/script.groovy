package form_template.income.rnu30

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Форма "(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.".
 *
 * @version 59
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheck() && checkNSI()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED : // проверка при "вернуть из принята в подготовлена"
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        break
    case FormDataEvent.COMPOSE :
        // TODO (Ramil Timerbaev)
        consolidation()
        // calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.IMPORT :
        importData()
        // TODO (Ramil Timerbaev)
        if (!hasError() /*&& calc() && logicalCheck() && checkNSI()*/) {
            logger.info('Закончена загрузка файла ' + UploadFileName)
        }
        break
    case FormDataEvent.MIGRATION :
        importData()
        if (!hasError()) {
            def total = getCalcTotalRow()
            def data = getData(formData)
            insert(data, total)
            logger.info('Закончена загрузка файла ' + UploadFileName)
        }
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
    def data = getData(formData)
    def newRow = formData.createDataRow()
    def index

    if (currentDataRow == null ||
            getIndex(currentDataRow) == -1 ||
            'total'.equals(currentDataRow.getAlias()) ||
            isFirstSection(data, currentDataRow)) {

        // в первые строки
        if ('total'.equals(currentDataRow.getAlias())) {
            index = getIndex(getRowByAlias(data, 'total'))
        } else {
            index = getIndex(currentDataRow) + 1
        }
        setEdit(newRow, null)
    } else if (isSection(data, currentDataRow, 'A') ||
            'totalA'.equals(currentDataRow.getAlias()) ||
            'A'.equals(currentDataRow.getAlias())) {

        // в раздел А
        if ('totalA'.equals(currentDataRow.getAlias()) || 'A'.equals(currentDataRow.getAlias())) {
            index = getIndex(getRowByAlias(data, 'totalA'))
        } else {
            index = getIndex(currentDataRow) + 1
        }
        setEdit(newRow, 'A')
    } else if (isSection(data, currentDataRow, 'B') ||
            'totalAll'.equals(currentDataRow.getAlias()) ||
            'totalB'.equals(currentDataRow.getAlias()) ||
            'B'.equals(currentDataRow.getAlias())) {

        // в раздел Б
        if ('totalAll'.equals(currentDataRow.getAlias()) || 'totalB'.equals(currentDataRow.getAlias()) ||
                'B'.equals(currentDataRow.getAlias())) {
            index = getIndex(getRowByAlias(data, 'totalB'))
        } else {
            index = getIndex(currentDataRow) + 1
        }
        setEdit(newRow, 'B')
    }
    data.insert(newRow, index)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
        def data = getData(formData)
        data.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)
    /*
     * Проверка объязательных полей.
     */

    // Список проверяемых столбцов для первых строк (графа 2..5, 8, 12, 16)
    requiredColumns1 = ['debtor', 'provision', 'nameBalanceAccount', 'debt45_90DaysSum',
            'debtOver90DaysSum', 'reservePrev', 'useReserve']

    // Список проверяемых столбцов для раздела А и Б (графа 2, 4, 12, 16)
    requiredColumnsAB = ['debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

    for (def row : getRows(data)) {
        if (!isFixedRow(row)) {
            def requiredColumns = (isFirstSection(row) ? requiredColumns1 : requiredColumnsAB)
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }
    }

    /*
     * Расчеты
     */

    def isFirst
    def tmp
    def index = 1
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            isFirst = isFirstSection(row)

            // графа 1
            row.number = index++

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

                // графа 13 - стоит поле остальных потому что в расчетах используются графа 14, 15
                row.reserveCurrent = row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
            } else {
                // графа 13
                row.reserveCurrent = row.reservePrev - row.useReserve
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
 */
def logicalCheck() {
    def data = getData(formData)
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
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        isFirst = isFirstSection(row)

        // 1. Обязательность заполнения полей
        def requiredColumns = (isFirst ? requiredColumns1 :  requiredColumnsAB)

        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        if (isFirst) {
            // 2. Арифметическая проверка графы 7
            tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
            if (row.debt45_90DaysReserve != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Задолженность от 45 до 90 дней. Расчётный резерв»!')
            }

            // 3. Арифметическая проверка графы 10
            tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
            if (row.debtOver90DaysReserve != round(tmp, 2)) {
                logger.warn('Неверно рассчитана графа «Задолженность более 90 дней. Расчётный резерв»!')
            }

            // 4. Арифметическая проверка графы 11
            if (row.totalReserve != row.debt45_90DaysReserve + row.debtOver90DaysReserve) {
                logger.warn('Наверное значение графы «Итого расчётный резерв»')
            }

            // 5. Арифметическая проверка графы 13
            // TODO (Ramil Timerbaev) уточнить: 13 графу считать только для первых строк или для всех?
            tmp = row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
            if (row.reserveCurrent != tmp) {
                logger.warn('Неверно рассчитана графа «Резерв на отчётную дату. Текущую»!')
            }

            // 6. Арифметическая проверка графы 14
            tmp = (row.totalReserve + row.useReserve > row.reservePrev ?
                row.totalReserve + row.useReserve - row.reservePrev : 0)
            if (row.calcReserve != tmp) {
                logger.warn('Неверно рассчитана графа «Изменение фактического резерва. Доначисление резерва с отнесением на расходы код 22670»!')
            }

            // 7. Арифметическая проверка графы 15
            tmp = (row.totalReserve + row.useReserve < row.reservePrev ?
                row.reservePrev - (row.totalReserve + row.useReserve) : 0)
            if (row.reserveRecovery != tmp) {
                logger.warn('Неверно рассчитана графа «Изменение фактического резерва. Восстановление резерва на доходах код 13091»!')
            }

            // 8. Арифметическая проверка графы 6
            if (row.debt45_90DaysNormAllocation50per != 50) {
                logger.warn('Неверно рассчитана графа «Задолженность от 45 до 90 дней. Норматив отчислений 50%»!')
            }

            // 9. Арифметическая проверка графы 9
            if (row.debtOver90DaysNormAllocation100per != 100) {
                logger.warn('Неверно рассчитана графа «Задолженность более 90 дней. Норматив отчислений 100%»!')
            }
        }

        // 13. Проверка на уникальность поля "№ пп" (графа 1)
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
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    for (def row : getRows(data)) {
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
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = getData(formData)
    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            deleteRows.add(row)
        }
    }
    getRows(data).removeAll(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
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
 * Проверка при создании формы.
 */
void checkCreation() {
    /** Признак периода ввода остатков. */
    def isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)

    //проверка периода ввода остатков
    if (isBalancePeriod) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}


/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    // TODO (Ramil Timerbaev) поправить формат на правильный
    if (!fileName.contains('.r')) {
        logger.error('Формат файла должен быть *.r??')
        return
    }

    // TODO (Ramil Timerbaev) поправить параметры
    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
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
        def totalLoad = addData(xml)

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
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
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex()
}

/**
 * Получить номер строки в таблице по псевдонимиу.
 */
def getIndex(def form, String rowAlias) {
    def row = getRowByAlias(getData(form), rowAlias)
    if (row != null) {
        return (getRows(getData(form))).indexOf(row)
    }
    return -1
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
def isFirstSection(def data, def row) {
    return row != null && getIndex(row) < getIndex(getRowByAlias(data, 'total'))
}

/**
 * Проверить принадлежит ли указанная строка к разделу (A или B).
 */
def isSection(def data, def row, def section) {
    return row != null &&
            getIndex(row) > getIndex(getRowByAlias(data, section)) &&
            getIndex(row) < getIndex(getRowByAlias(data, 'total' + section))
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
        def index = row.number
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
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    // TODO (Ramil Timerbaev)
    // графа
    ['', ''].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Получить список строк формы.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
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
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
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
    def dataSource = getData(sourceForm)
    getRows(dataSource).subList(from, to).each { row ->
        def dataDestination = getData(destinationForm)
        getRows(dataDestination).add(getIndex(destinationForm, toAlias), row)
    }
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 *
 * return итоговая строка
 */
def addData(def xml) {
    def data = getData(formData)
    data.clear()

    // def date = new Date()
    def cache = [:]
    def newRows = []
    def indexRow = 0

    // TODO (Ramil Timerbaev) поправить получение строк если загружать из *.rnu или *.xml
    for (def row : xml.row) {
        indexRow++

        def newRow = getNewRow()
        def indexCell = 0

        // графа 1
        newRow.number = getNumber(row.cell[indexCell].text())
        index++

        // графа 2
        newRow.contract = getNumber(row.cell[indexCell].text())
        index++

        // графа 3
        newRow.contractDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 4
        newRow.amountOfTheGuarantee = getNumber(row.cell[indexCell].text())
        index++

        // графа 5
        newRow.dateOfTransaction = getNumber(row.cell[indexCell].text())
        index++

        // графа 6
        newRow.rateOfTheBankOfRussia = getNumber(row.cell[indexCell].text())
        index++

        // графа 7
        newRow.interestRate = getNumber(row.cell[indexCell].text())
        index++

        // графа 8
        newRow.baseForCalculation = getNumber(row.cell[indexCell].text())
        index++

        // графа 9
        newRow.accrualAccountingStartDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 10
        newRow.accrualAccountingEndDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 11
        newRow.preAccrualsStartDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 12
        newRow.preAccrualsEndDate = getNumber(row.cell[indexCell].text())
        index++

        // графа 13
        newRow.incomeCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 14
        newRow.incomeRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 15
        newRow.accountingCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 16
        newRow.accountingRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 17
        newRow.preChargeCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 18
        newRow.preChargeRuble = getNumber(row.cell[indexCell].text())
        index++

        // графа 19
        newRow.taxPeriodCurrency = getNumber(row.cell[indexCell].text())
        index++

        // графа 20
        newRow.taxPeriodRuble = getNumber(row.cell[indexCell].text())
        index++

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() > 0) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        def index = 12

        // TODO (Ramil Timerbaev) поправить/уточнить
        // графа 13
        total.incomeCurrency = getNumber(row.cell[index++].text())
        // графа 14
        total.incomeRuble = getNumber(row.cell[index++].text())
        // графа 15
        total.accountingCurrency = getNumber(row.cell[index++].text())
        // графа 16
        total.accountingRuble = getNumber(row.cell[index++].text())
        // графа 17
        total.preChargeCurrency = getNumber(row.cell[index++].text())
        // графа 18
        total.preChargeRuble = getNumber(row.cell[index++].text())
        // графа 19
        total.taxPeriodCurrency = getNumber(row.cell[index++].text())
        // графа 20
        total.taxPeriodRuble = getNumber(row.cell[index++].text())

        return total
    } else {
        return null
    }
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
    return new BigDecimal(tmp)
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
def getRecordId(def ref_id, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[ref_id]!=null) {
        if (cache[ref_id][filter] != null) {
            return cache[ref_id][filter]
        }
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    logger.error("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
    return null
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    for (def row : getRows(data)) {
        if (alias.equals(row.getAlias())) {
            return row
        }
    }
    return null
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumns = getTotalColumns()
    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumns.each { columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(totalCalc.getCell(columnAlias).column.order)
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
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contract = 'Итого'
    setTotalStyle(totalRow)

    def totalColumns = getTotalColumns()
    def data = getData(formData)
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    // просуммировать значения неитоговых строк
    for (def row : getRows(data)) {
        if (row.getAlias() != null) {
            continue
        }
        totalColumns.each { alias ->
            tmp = totalRow.getCell(alias).getValue() + (row.getCell(alias).getValue() ?: 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }
    return totalRow
}

/**
 * Получить список графов для которых надо вычислять итого (графа 13..20).
 */
def getTotalColumns() {
    return ['incomeCurrency', 'incomeRuble', 'accountingCurrency', 'accountingRuble',
            'preChargeCurrency', 'preChargeRuble', 'taxPeriodCurrency', 'taxPeriodRuble']
}

// TODO (Ramil Timerbaev)
/**
 * Для отладки. Потом убрать
 */
void log(def message) {
    def s = '===== ' + message
    logger.info(s)
    // System.out.println(s)
}