package form_template.income.rnu30

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Форма "(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов".
 *
 * @author rtimerbaev
 */

// графа 0  - fix - для вывода надписей
// графа 1  - number
// графа 2  - debtor
// графа 3  - provision                             атрибут 822 - CODE - "Код обеспечения", справочник 86 "Обеспечение"
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
        // !hasError() && calc() && logicalCheck() && checkNSI()
        break
    case FormDataEvent.MIGRATION :
        migration()
        break
}

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def newRow = formData.createDataRow()
    def index = 0

    if (currentDataRow == null) {

        // в первые строки
        setEdit(newRow, null)
    } else if (currentDataRow.getIndex() == -1 ||
            'total'.equals(currentDataRow.getAlias()) ||
            isFirstSection(dataRows, currentDataRow)) {

        // в первые строки
        if ('total'.equals(currentDataRow.getAlias())) {
            index = getRowByAlias(dataRows, 'total').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, null)
    } else if (isSection(dataRows, currentDataRow, 'A') ||
            'totalA'.equals(currentDataRow.getAlias()) ||
            'A'.equals(currentDataRow.getAlias())) {

        // в раздел А
        if ('totalA'.equals(currentDataRow.getAlias()) || 'A'.equals(currentDataRow.getAlias())) {
            index = getRowByAlias(dataRows, 'totalA').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, 'A')
    } else if (isSection(dataRows, currentDataRow, 'B') ||
            'totalAll'.equals(currentDataRow.getAlias()) ||
            'totalB'.equals(currentDataRow.getAlias()) ||
            'B'.equals(currentDataRow.getAlias())) {

        // в раздел Б
        if ('totalAll'.equals(currentDataRow.getAlias()) || 'totalB'.equals(currentDataRow.getAlias()) ||
                'B'.equals(currentDataRow.getAlias())) {
            index = getRowByAlias(dataRows, 'totalB').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, 'B')
    }
    dataRowHelper.insert(newRow, index)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (currentDataRow.getAlias() == null) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.delete(currentDataRow)
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

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

    for (def row : dataRows) {
        if (row.getAlias() == null) {
            def requiredColumns = (isFirstSection(dataRows, row) ? requiredColumns1 : requiredColumnsAB)
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }
        }
    }

    // отсортировать/группировать
    sort(dataRows)

    /*
     * Расчеты
     */

    def isFirst
    def tmp
    def index = 1
    dataRows.each { row ->
        if (row.getAlias() == null) {
            isFirst = isFirstSection(dataRows, row)

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

    // Первые строки (графа 5, 7, 8, 10..16)
    def totalColumns1 = getTotalColumns1()
    def totalRow = getRowByAlias(dataRows, 'total')
//    totalColumns1.each { alias ->
//        totalRow.getCell(alias).setValue(0)
//    }
    totalColumns1.each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias, totalRow))
    }

    def aRow = getRowByAlias(dataRows, 'A')
    def totalARow = getRowByAlias(dataRows, 'totalA')

    def bRow = getRowByAlias(dataRows, 'B')
    def totalBRow = getRowByAlias(dataRows, 'totalB')

    // раздел А и Б (графа 12, 13, 16)
    def totalColumnsAB = getTotalColumnsAB()
//    totalColumnsAB.each { alias ->
//        totalARow.getCell(alias).setValue(0)
//        totalBRow.getCell(alias).setValue(0)
//    }
    totalColumnsAB.each { alias ->
        totalARow.getCell(alias).setValue(getSum(dataRows, alias, aRow, totalARow))
        totalBRow.getCell(alias).setValue(getSum(dataRows, alias, bRow, totalBRow))
    }

    // Всего (графа 5, 7, 8, 10..16)
    def totalAllRow = getRowByAlias(dataRows, 'totalAll')
    ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve'].each { alias ->
        tmp = getValue(totalRow.getCell(alias).getValue()) +
                getValue(totalARow.getCell(alias).getValue()) +
                getValue(totalBRow.getCell(alias).getValue())
        totalAllRow.getCell(alias).setValue(tmp)
    }
    dataRowHelper.save(dataRows)
    return true
}

/**
 * Логические проверки.
 */
def logicalCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

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
    def i = 1

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        isFirst = isFirstSection(dataRows, row)

        // 1. Обязательность заполнения полей
        def requiredColumns = (isFirst ? requiredColumns1 :  requiredColumnsAB)
        if (!checkRequiredColumns(row, requiredColumns)) {
            return false
        }

        index = row.getIndex()
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
    def totalRow = getRowByAlias(dataRows, 'total')
    columns = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum',
            'debtOver90DaysReserve', 'totalReserve', 'reservePrev',
            'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    for (def alias : columns) {
        if (totalRow.getCell(alias).getValue() != getSum(dataRows, alias, totalRow)) {
            def name = getColumnName(totalRow, alias)
            logger.error("Итоговые значения для \"$name\" рассчитаны неверно!")
            return false
        }
    }

    // 11 + 12. Проверка итоговых значений по строкам из раздела А и B
    def aRow = getRowByAlias(dataRows, 'A')
    def totalARow = getRowByAlias(dataRows, 'totalA')

    def bRow = getRowByAlias(dataRows, 'B')
    def totalBRow = getRowByAlias(dataRows, 'totalB')

    //  раздел А и Б (графа 12, 13, 16)
    columns = ['reservePrev', 'reserveCurrent', 'useReserve']
    for (def alias : columns) {
        if (totalARow.getCell(alias).getValue() != getSum(dataRows, alias, aRow, totalARow)) {
            def name = getColumnName(totalARow, alias)
            logger.error("Итоговые значения для \"$name\" раздела А рассчитаны неверно!")
            return false
        }
        if (totalBRow.getCell(alias).getValue() != getSum(dataRows, alias, bRow, totalBRow)) {
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def index
    def errorMsg
    def cache = [:]
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        index = row.getIndex()
        errorMsg = "В строке $index "

        // 1. Проверка актуальности поля «Обеспечение» (графа 3)
        if (row.provision != null && getRecordById(86, row.provision, cache) == null) {
            logger.warn(errorMsg + 'обеспечение в справочнике отсутствует!')
        }

        // 2. Проверка счёта бухгалтерского учёта для данного РНУ (графа 4)
        if (row.nameBalanceAccount != null && getRecordById(29, row.nameBalanceAccount, cache) == null) {
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def sourceFormData = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(sourceFormData)
                def sourceDataRows = sourceDataRowHelper.getAllCached()
                copyRows(sourceDataRows, dataRows, null, 'total')
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
    dataRowHelper.save(dataRows)
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
void migration() {
    importData()
    if (!hasError()) {
        def total = getCalcTotalRow()
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.getAllCached()
        dataRowHelper.insert(total, dataRows.size + 1)
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Получить номер строки в таблице по псевдонимиу (1..n).
 */
def getIndexByAlias(def dataRows, String rowAlias) {
    def row = getRowByAlias(dataRows, rowAlias)
    return (row != null ? row.getIndex() : -1)
}

/**
 * Получить сумму графы в указанном диапозоне строк.
 */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить сумму графы c первой строки и до указанной.
 */
def getSum(def dataRows, def columnAlias, def rowEnd) {
    def from = 0
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/**
 * Проверить принадлежит ли указанная строка к первому разделу (до строки "итого").
 */
def isFirstSection(def dataRows, def row) {
    return row != null && row.getIndex() < getRowByAlias(dataRows, 'total').getIndex()
}

/**
 * Проверить принадлежит ли указанная строка к разделу (A или B).
 */
def isSection(def dataRows, def row, def section) {
    return row != null &&
            row.getIndex() > getIndexByAlias(dataRows, section) &&
            row.getIndex() < getIndexByAlias(dataRows, 'total' + section)
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
        def errorMsg = colNames.join(', ')
        index = row.getIndex()
        logger.error("В строке $index не заполнены колонки : $errorMsg.")
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
        return row.getCell(alias).column.name.replace('%', '%%')
    }
    return ''
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = (fromAlias != null ? getIndexByAlias(sourceDataRows, fromAlias) : 0)
    def to = getIndexByAlias(sourceDataRows, toAlias) - 1
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getIndexByAlias(destinationDataRows, toAlias) - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    destinationDataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

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
        newRow.provision = getRecord(86, '', row.cell[indexCell].text(), date, cache)
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
    dataRowHelper.insert(newRows, 1)

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
 * @param dataRows строки нф
 * @param alias алиас
 */
def getRowByAlias(def dataRows, def alias) {
    if (alias == null || alias == '' || dataRows == null) {
        return null
    }
    for (def row : dataRows) {
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

    def totalColumns = getTotalColumns()
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // просуммировать значения неитоговых строк
    for (def row : dataRows) {
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
void sort(def dataRows) {
    def sortRows = []
    def from
    def to

    // первые строки
    from = 0
    to = getIndexByAlias(dataRows, 'total') - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    // раздел А
    from = getIndexByAlias(dataRows, 'A')
    to = getIndexByAlias(dataRows, 'totalA') - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    // раздела Б
    from = getIndexByAlias(dataRows, 'B')
    to = getIndexByAlias(dataRows, 'totalB') - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    def cache = [:]
    sortRows.each {
        it.sort {
            // графа 2  - debtor
            // графа 3  - provision
            def a, def b ->
                if (a.provision == b.provision) {
                    return a.debtor <=> b.debtor
                }
                def recordA = (a.provision ? getRecordById(86, a.provision, cache) : null)
                def recordB = (b.provision ? getRecordById(86, b.provision, cache) : null)
                def codeA = (recordA != null ? recordA.CODE.value : null)
                def codeB = (recordB != null ? recordB.CODE.value : null)
                return codeA <=> codeB
        }
    }
}

/**
 * Получить строки из нф по заданному идентификатору нф.
 */
def getRnuRowsById(def id) {
    def formDataRNU = formDataService.find(id, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formDataRNU != null) {
        def dataRowHelper = formDataService.getDataRowHelper(formDataRNU)
        def dataRows = dataRowHelper.getAllCached()
        return dataRows
    }
    return null
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

def getTotalColumns() {
    return []
}