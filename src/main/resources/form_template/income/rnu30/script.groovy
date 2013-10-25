package form_template.income.rnu30

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.".
 *
 * @version 59
 *
 * TODO:
 *      - поменять тип графы 3 и 4 на справочник, после того как будет готов справочник для графы 3 - задача http://jira.aplana.com/browse/SBRFACCTAX-4738
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
        consolidation()
        calc() && logicalCheck() && checkNSI()
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
// графа 3  - provision                             атрибут ?? - ?? - "Код обеспечения", справочник ?? "Обеспечение"
// графа 4  - nameBalanceAccount                    атрибут 152 - BALANCE_ACCOUNT - "Номер балансового счёта", справочник 29 "Классификатор соответствия счетов бухгалтерского учёта кодам налогового учёта"
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
    def index = 0

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
    data.insert(newRow, index + 1)
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

    // TODO (Ramil Timerbaev) проверка наличия рну-6 перед расчетом - аналитик сказала пока убрать
//    def rowsRnu6 = getRnuRowsById(318)
//    if (rowsRnu6 == null) {
//        def name = '"(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления"'
//        logger.info("Не найдены экземпляры $name за текущий отчетный период!")
//        return false
//    }

    /*
     * Проверка обязательных полей.
     */

    // Список проверяемых столбцов для первых строк (графа 2..5, 8, 12, 16)
    requiredColumns1 = ['debtor', 'provision', 'nameBalanceAccount', 'debt45_90DaysSum',
            'debtOver90DaysSum', 'reservePrev', 'useReserve']

    // Список проверяемых столбцов для раздела А и Б (графа 2, 4, 12, 16)
    requiredColumnsAB = ['debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

    for (def row : getRows(data)) {
        if (!isFixedRow(row)) {
            def requiredColumns = (isFirstSection(data, row) ? requiredColumns1 : requiredColumnsAB)
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }
    }

    // отсортировать/группировать
    sort(data)

    /*
     * Расчеты
     */

    def isFirst
    def tmp
    def index = 1
    getRows(data).each { row ->
        if (!isFixedRow(row)) {
            isFirst = isFirstSection(data, row)

            // графа 1
            row.number = index++

            if (isFirst) {
                // графа 6
                row.debt45_90DaysNormAllocation50per = calc6()

                // графа 7
                row.debt45_90DaysReserve = calc7(row)

                // графа 9
                row.debtOver90DaysNormAllocation100per = calc9()

                // графа 10
                row.debtOver90DaysReserve = calc10(row)

                // графа 11
                row.totalReserve = calc11(row)

                // графа 14
                row.calcReserve = calc14(row)

                // графа 15
                row.reserveRecovery = calc15(row)

                // графа 13 - стоит поле остальных потому что в расчетах используются графа 14, 15
                row.reserveCurrent = calc13(row)
            } else {
                // графа 13
                row.reserveCurrent = calc13AB(row)
            }
        }
    }
    save(data)

    // Первые строки (графа 5, 7, 8, 10..16)
    def totalColumns1 = getTotalColumns1()
    def totalRow = getRowByAlias(data, 'total')
//    totalColumns1.each { alias ->
//        totalRow.getCell(alias).setValue(0)
//    }
    totalColumns1.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias, totalRow))
    }

    def aRow =getRowByAlias(data, 'A')
    def totalARow = getRowByAlias(data, 'totalA')

    def bRow = getRowByAlias(data, 'B')
    def totalBRow = getRowByAlias(data, 'totalB')

    // раздел А и Б (графа 12, 13, 16)
    def totalColumnsAB = getTotalColumnsAB()
//    totalColumnsAB.each { alias ->
//        totalARow.getCell(alias).setValue(0)
//        totalBRow.getCell(alias).setValue(0)
//    }
    totalColumnsAB.each { alias ->
        totalARow.getCell(alias).setValue(getSum(data, alias, aRow, totalARow))
        totalBRow.getCell(alias).setValue(getSum(data, alias, bRow, totalBRow))
    }

    // Всего (графа 5, 7, 8, 10..16)
    def totalAllRow = getRowByAlias(data, 'totalAll')
    ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        tmp = getValue(totalRow.getCell(alias).getValue()) +
                getValue(totalARow.getCell(alias).getValue()) +
                getValue(totalBRow.getCell(alias).getValue())
        totalAllRow.getCell(alias).setValue(tmp)
    }
    save(data)

    return true
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

    // для раздера А и Б - графы 1, 2, 4, 12, 16
    requiredColumnsAB = ['number', 'debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

    // алиасы графов для арифметической проверки (6, 7, 9..11, 13..15)
    def arithmeticCheckAlias = ['debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve',
            'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve',
            'reserveCurrent', 'calcReserve', 'reserveRecovery']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    def colNames = []

    def isFirst
    def index
    def errorMsg
    for (def row : getRows(data)) {
        if (isFixedRow(row)) {
            continue
        }

        isFirst = isFirstSection(data, row)

        // 1. Обязательность заполнения полей
        def requiredColumns = (isFirst ? requiredColumns1 :  requiredColumnsAB)
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        if (isFirst) {
            // 3. Арифметическая проверка графы 6, 7, 9..11, 13..15
            needValue['debt45_90DaysNormAllocation50per'] = calc6()
            needValue['debt45_90DaysReserve'] = calc7(row)
            needValue['debtOver90DaysNormAllocation100per'] = calc9()
            needValue['debtOver90DaysReserve'] = calc10(row)
            needValue['totalReserve'] = calc11(row)
            needValue['reserveCurrent'] = calc13(row)
            needValue['calcReserve'] = calc14(row)
            needValue['reserveRecovery'] = calc15(row)

            arithmeticCheckAlias.each { alias ->
                if (needValue[alias] != row.getCell(alias).getValue()) {
                    def name = getColumnName(row, alias)
                    colNames.add('"' + name + '"')
                }
            }
            if (!colNames.isEmpty()) {
                def msg = colNames.join(', ')
                logger.error(errorMsg + "неверно рассчитано значение графы: $msg.")
                return false
            }
        } else {
            // 3. Арифметическая проверка графы 13
            if (row.reserveCurrent != calc13AB(row)) {
                def msg = colNames.join(', ')
                logger.error(errorMsg + "неверно рассчитано значение графы: $msg.")
            }
        }

        // 13. Проверка на уникальность поля "№ пп" (графа 1)
        if (i != row.number) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
            return false
        }
        i++
    }

    def columns

    // 10. Проверка итоговых значений по строкам, не входящим в состав раздел А и Б (графа 5, 7, 8, 10..16)
    def totalRow = getRowByAlias(data, 'total')
    columns = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    for (def alias : columns) {
        if (totalRow.getCell(alias).getValue() != getSum(data, alias, totalRow)) {
            def name = getColumnName(totalRow, alias)
            logger.error("Итоговые значения для \"$name\" рассчитаны неверно!")
            return false
        }
    }

    // 11 + 12. Проверка итоговых значений по строкам из раздела А и B
    def aRow = getRowByAlias(data, 'A')
    def totalARow = getRowByAlias(data, 'totalA')

    def bRow = getRowByAlias(data, 'B')
    def totalBRow = getRowByAlias(data, 'totalB')

    //  раздел А и Б (графа 12, 13, 16)
    columns = ['reservePrev', 'reserveCurrent', 'useReserve']
    for (def alias : columns) {
        if (totalARow.getCell(alias).getValue() != getSum(data, alias, aRow, totalARow)) {
            def name = getColumnName(totalARow, alias)
            logger.error("Итоговые значения для \"$name\" раздела А рассчитаны неверно!")
            return false
        }
        if (totalBRow.getCell(alias).getValue() != getSum(data, alias, bRow, totalBRow)) {
            def name = getColumnName(totalBRow, alias)
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
    def index
    def errorMsg
    def cache = [:]
    for (def row : getRows(data)) {
        if (isTotal(row)) {
            continue
        }

        index = getIndex(row) + 1
        errorMsg = "В строке $index "

        // 1. Проверка актуальности поля «Обеспечение» (графа 3)
        // TODO (Ramil Timerbaev) getRecordById(??, ??, cache) == null
        if (false) {
            logger.warn(errorMsg + 'обеспечение в справочнике отсутствует!')
        }

        // 2. Проверка счёта бухгалтерского учёта для данного РНУ (графа 4)
        // TODO (Ramil Timerbaev) getRecordById(29, ??, cache) == null
        if (false) {
            logger.error(errorMsg + 'операция в РНУ не учитывается!')
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
                def sourceData = getData(source)
                copyRows(sourceData, data, null, 'total')
                copyRows(sourceData, data, 'A', 'totalA')
                copyRows(sourceData, data, 'B', 'totalB')
            }
        }
    }
    save(data)
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
 * Получить номер строки в таблице (0..n).
 *
 * @param row строка
 */
def getIndex(def row) {
    row.getIndex() - 1
}

/**
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def data, String rowAlias) {
    def row = getRowByAlias(data, rowAlias)
    return (row != null ? getIndex(row) : -1)
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias, def rowEnd) {
    def from = 0
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, getRows(data), new ColumnRange(columnAlias, from, to))
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
            getIndex(row) > getIndexByAlias(data, section) &&
            getIndex(row) < getIndexByAlias(data, 'total' + section)
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
 * @param sourceData даныне источника
 * @param destinationData данные приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceData, def destinationData, def fromAlias, def toAlias) {
    def from = (fromAlias != null ? getIndexByAlias(sourceData, fromAlias) + 1 : 0)
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }
    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    save(destinationData)
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

    def date = new Date()
    def cache = [:]
    def newRows = []
    def index = 0

    // TODO (Ramil Timerbaev) поправить получение строк если загружать из *.rnu или *.xml
    for (def row : xml.row) {
        index++

        def newRow = formData.createDataRow()
        setEdit(newRow, null) // TODO (Ramil Timerbaev) задать раздел
        def indexCell = 0

        // графа 1
        newRow.number = getNumber(row.cell[indexCell].text())
        index++

        // графа 2
        newRow.debtor = row.cell[indexCell].text()
        index++

        // графа 3
        // TODO (Ramil Timerbaev) получить значение из справочника
        newRow.provision = getRecord(0, '', row.cell[indexCell].text(), date, cache)
        index++

        // графа 4
        // TODO (Ramil Timerbaev) получить значение из справочника
        newRow.nameBalanceAccount = getRecord(29, 'BALANCE_ACCOUNT', row.cell[indexCell].text(), date, cache)
        index++

        // графа 5
        newRow.debt45_90DaysSum = getNumber(row.cell[indexCell].text())
        index++

        // графа 6
        newRow.debt45_90DaysNormAllocation50per = getNumber(row.cell[indexCell].text())
        index++

        // графа 7
        newRow.debt45_90DaysReserve = getNumber(row.cell[indexCell].text())
        index++

        // графа 8
        newRow.debtOver90DaysSum = getNumber(row.cell[indexCell].text())
        index++

        // графа 9
        newRow.debtOver90DaysNormAllocation100per = getNumber(row.cell[indexCell].text())
        index++

        // графа 10
        newRow.debtOver90DaysReserve = getNumber(row.cell[indexCell].text())
        index++

        // графа 11
        newRow.totalReserve = getNumber(row.cell[indexCell].text())
        index++

        // графа 12
        newRow.reservePrev = getNumber(row.cell[indexCell].text())
        index++

        // графа 13
        newRow.reserveCurrent = getNumber(row.cell[indexCell].text())
        index++

        // графа 14
        newRow.calcReserve = getNumber(row.cell[indexCell].text())
        index++

        // графа 15
        newRow.reserveRecovery = getNumber(row.cell[indexCell].text())
        index++

        // графа 16
        newRow.useReserve = getNumber(row.cell[indexCell].text())
        index++

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    if (xml.rowTotal.size() > 0) {
        //def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        //index = 12

        // TODO (Ramil Timerbaev) поправить/уточнить
        // графа
        // total. = getNumber(row.cell[index++].text())

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
    try {
        return new BigDecimal(tmp)
    } catch (Exception e) {
        throw new Exception("Значение \"$value\" не может быть преобразовано в число. " + e.message)
    }
}

/**
 * Получить запись из справочника по идентифкатору записи.
 *
 * @param refBookId идентификатор справончика
 * @param recordId идентификатор записи
 * @param cache кеш
 * @return
 */
def getRecordById(def refBookId, def recordId, def cache) {
    if (cache[refBookId] != null) {
        if (cache[refBookId][recordId] != null) {
            return cache[refBookId][recordId]
        }
    } else {
        cache[refBookId] = [:]
    }
    def record = refBookService.getRecordData(refBookId, recordId)
    if (record != null) {
        cache[refBookId][recordId] = record
        return cache[refBookId][recordId]
    }
    // def refBook = refBookFactory.get(refBookId)
    // def refBookName = refBook.name
    // logger.error("Не удалось найти запись (id = $recordId) в справочнике $refBookName (id = $refBookId)")
    return null
}

/**
 * Получить запись из справочника по фильту на дату.
 *
 * @param refBookId идентификатор справончика
 * @param code атрибут справочника по которому искать данные
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return запись справочника
 */
def getRecord(def refBookId, String code, def value, Date date, def cache) {
    String filter = code + " = '" + value + "'"
    if (cache[refBookId] != null) {
        if (cache[refBookId][filter] != null) {
            return cache[refBookId][filter]
        }
    } else {
        cache[refBookId] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(refBookId)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[refBookId][filter] = records.get(0)
        return cache[refBookId][filter]
    }
    def refBook = refBookFactory.get(refBookId)
    def refBookName = refBook.name
    logger.error("Не удалось найти запись в справочнике $refBookName (id = $refBookId) с атрибутом $code равным $value!")
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
 * Получить список графов для которых надо вычислять итого (первые строки: графа 5, 7, 8, 10..16).
 */
def getTotalColumns1() {
    return ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
}

/**
 * Получить список графов для которых надо вычислять итого (раздел А и Б: графа 12, 13, 16).
 */
def getTotalColumnsAB() {
    return ['reservePrev', 'reserveCurrent', 'useReserve']
}

/**
 * Отсортировать / группировать строки
 */
void sort(def data) {
    def rows = getRows(data)
    def sortRows = []
    def from
    def to

    // первые строки
    from = 0
    to = getIndexByAlias(data, 'total') - 1
    sortRows.add(rows[from..to])

    // раздел А
    from = getIndexByAlias(data, 'A') + 1
    to = getIndexByAlias(data, 'totalA') - 1
    sortRows.add(rows[from..to])

    // раздела Б
    from = getIndexByAlias(data, 'B') + 1
    to = getIndexByAlias(data, 'totalB') - 1
    sortRows.add(rows[from..to])

    sortRows.each {
        it.sort {
            // графа 2  - debtor
            // графа 3  - provision
            def a, def b ->
                if (a.provision == b.provision) {
                    return a.debtor <=> b.debtor
                }
                // TODO (Ramil Timerbaev)
                // def recordA = getRecordById(??, a.valuablePaper, cache)
                // def recordB = getRecordById(??, b.valuablePaper, cache)
                // def a = (recordA != null ? recordA.???.value : null)
                // def b = (recordB != null ? recordB.???.value : null)
                // return a <=> b
                return a.provision <=> b.provision
        }
    }
}

/**
 * Получить строки из нф по заданному идентификатору нф.
 */
def getRnuRowsById(def id) {
    def formDataRNU = formDataService.find(id, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    def data = getData(formDataRNU)
    return (data ? getRows(data) : null)
}

def calc6() {
    return 50
}

def calc7(def row) {
    def tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
    return roundValue(tmp, 2)
}

def calc9() {
    return 100
}

def calc10(def row) {
    def tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
    return roundValue(tmp, 2)
}

def calc11(def row) {
    return row.debt45_90DaysReserve + row.debtOver90DaysReserve
}

def calc14(def row) {
    return (row.totalReserve + row.useReserve > row.reservePrev ?
        row.totalReserve + row.useReserve - row.reservePrev : 0)
}

def calc15(def row) {
    return (row.totalReserve + row.useReserve < row.reservePrev ?
        row.reservePrev - (row.totalReserve + row.useReserve) : 0)
}

def calc13(def row) {
    return row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve
}

def calc13AB(def row) {
    return row.reservePrev - row.useReserve
}