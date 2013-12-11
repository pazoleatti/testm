package form_template.income.rnu30

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов".
 * formTemplateId=329
 * TODO:
 *      - загрузка и импорт не доделаны
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
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED : // проверка при "вернуть из принята в подготовлена"
        break
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        // !logger.containsLevel(LogLevel.ERROR) && calc() && logicCheck() && checkNSI()
        break
    case FormDataEvent.MIGRATION :
        migration()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Атрибуты итоговых строк для которых вычисляются суммы (всего графа 5, 7, 8, 10..16)
@Field
def totalColumnsAll = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']

// Атрибуты итоговых строк для которых вычисляются суммы (раздел А и Б: графа 12, 13, 16)
@Field
def totalColumnsAB = ['reservePrev', 'reserveCurrent', 'useReserve']

// Атрибуты итоговых строк для которых вычисляются суммы (первые строки: графа 5, 7, 8, 10..16)
@Field
def totalColumns1 = totalColumnsAll

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов
// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
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
            index = getDataRow(dataRows, 'total').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, null)
    } else if (isSection(dataRows, currentDataRow, 'A') ||
            'totalA'.equals(currentDataRow.getAlias()) ||
            'A'.equals(currentDataRow.getAlias())) {

        // в раздел А
        if ('totalA'.equals(currentDataRow.getAlias()) || 'A'.equals(currentDataRow.getAlias())) {
            index = getDataRow(dataRows, 'totalA').getIndex()
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
            index = getDataRow(dataRows, 'totalB').getIndex()
        } else {
            index = currentDataRow.getIndex() + 1
        }
        setEdit(newRow, 'B')
    }
    dataRowHelper.insert(newRow, index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // отсортировать/группировать
    sort(dataRows)

    def index = 1
    dataRows.each { row ->
        if (row.getAlias() == null) {
            // графа 1
            row.number = index++
            
            if (isFirstSection(dataRows, row)) {
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

    def totalRow = getDataRow(dataRows, 'total')
    totalColumns1.each { alias ->
        totalRow.getCell(alias).setValue(getSum(dataRows, alias, totalRow))
    }

    def aRow = getDataRow(dataRows, 'A')
    def totalARow = getDataRow(dataRows, 'totalA')
    def bRow = getDataRow(dataRows, 'B')
    def totalBRow = getDataRow(dataRows, 'totalB')
    totalColumnsAB.each { alias ->
        totalARow.getCell(alias).setValue(getSum(dataRows, alias, aRow, totalARow))
        totalBRow.getCell(alias).setValue(getSum(dataRows, alias, bRow, totalBRow))
    }

    def totalAllRow = getDataRow(dataRows, 'totalAll')
    totalColumnsAll.each { alias ->
        def tmp = (totalRow.getCell(alias).value ?: 0) +
                (totalARow.getCell(alias).value ?: 0) +
                (totalBRow.getCell(alias).value ?: 0)
        totalAllRow.getCell(alias).setValue(tmp)
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // для первых строк - графы 1..16
    requiredColumns1 = ['number', 'debtor', 'provision', 'nameBalanceAccount', 'debt45_90DaysSum', 'debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    // для раздера А и Б - графы 1, 2, 4, 12, 16
    requiredColumnsAB = ['number', 'debtor', 'nameBalanceAccount', 'reservePrev', 'useReserve']

    // алиасы графов для арифметической проверки (6, 7, 9..11, 13..15)
    def arithmeticCheckAlias = ['debt45_90DaysNormAllocation50per', 'debt45_90DaysReserve', 'debtOver90DaysNormAllocation100per', 'debtOver90DaysReserve', 'totalReserve', 'reserveCurrent', 'calcReserve', 'reserveRecovery']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def rowNumber = 0
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def isFirst = isFirstSection(dataRows, row)
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        
        // 1. Обязательность заполнения полей
        def nonEmptyColumns = (isFirst ? requiredColumns1 :  requiredColumnsAB)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        if (isFirst) {
            // 3. Арифметическая проверка первых строк (графа 6, 7, 9..11, 13..15)
            needValue['debt45_90DaysNormAllocation50per'] = calc6()
            needValue['debt45_90DaysReserve'] = calc7(row)
            needValue['debtOver90DaysNormAllocation100per'] = calc9()
            needValue['debtOver90DaysReserve'] = calc10(row)
            needValue['totalReserve'] = calc11(row)
            needValue['reserveCurrent'] = calc13(row)
            needValue['calcReserve'] = calc14(row)
            needValue['reserveRecovery'] = calc15(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        } else {
            // 3. Арифметическая проверка раздела А и Б (графа 13)
            needValue['reserveCurrent'] = calc13AB(row)
            checkCalc(row, ['reserveCurrent'], needValue, logger, true)
        }

        // 13. Проверка на уникальность поля "№ пп" (графа 1)
        if (++rowNumber != row.number) {
            logger.error(errorMsg + 'нарушена уникальность номера по порядку!')
        }

        // Проверки соответствия НСИ
        // 1. Проверка актуальности поля «Обеспечение» (графа 3)
        checkNSI(86, row, 'provision')
        // 2. Проверка счёта бухгалтерского учёта для данного РНУ (графа 4)
        checkNSI(29, row, 'nameBalanceAccount')
    }

    // 10. Проверка итоговых значений по строкам, не входящим в состав раздел А и Б (графа 5, 7, 8, 10..16)
    def totalRow = getDataRow(dataRows, 'total')
    def columns = ['debt45_90DaysSum', 'debt45_90DaysReserve', 'debtOver90DaysSum', 'debtOver90DaysReserve', 'totalReserve', 'reservePrev', 'reserveCurrent', 'calcReserve', 'reserveRecovery', 'useReserve']
    for (def alias : columns) {
        if (totalRow.getCell(alias).value != getSum(dataRows, alias, totalRow)) {
            def name = getColumnName(totalRow, alias)
            logger.error("Итоговые значения для \"$name\" рассчитаны неверно!")
        }
    }

    // 11 + 12. Проверка итоговых значений по строкам из раздела А и B
    def aRow = getDataRow(dataRows, 'A')
    def totalARow = getDataRow(dataRows, 'totalA')

    def bRow = getDataRow(dataRows, 'B')
    def totalBRow = getDataRow(dataRows, 'totalB')

    //  раздел А и Б (графа 12, 13, 16)
    columns = ['reservePrev', 'reserveCurrent', 'useReserve']
    for (def alias : columns) {
        if (totalARow.getCell(alias).value != getSum(dataRows, alias, aRow, totalARow)) {
            def name = getColumnName(totalARow, alias)
            logger.error("Итоговые значения для \"$name\" раздела А рассчитаны неверно!")
        }
        if (totalBRow.getCell(alias).value != getSum(dataRows, alias, bRow, totalBRow)) {
            def name = getColumnName(totalBRow, alias)
            logger.error("Итоговые значения для \"$name\" раздела Б рассчитаны неверно!")
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    dataRows.removeAll(deleteRows)
    // поправить индексы, потому что они после изменения не пересчитываются
    updateIndexes(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def sourceFormData = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceDataRowHelper = formDataService.getDataRowHelper(sourceFormData)
                def sourceDataRows = sourceDataRowHelper.allCached
                copyRows(sourceDataRows, dataRows, null, 'total')
                copyRows(sourceDataRows, dataRows, 'A', 'totalA')
                copyRows(sourceDataRows, dataRows, 'B', 'totalB')
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/** Получение импортируемых данных. */
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
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def total = getCalcTotalRow()
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        dataRowHelper.insert(total, dataRows.size + 1)
    }
}

/*
 * Вспомогательные методы.
 */

/** Получить сумму графы в указанном диапозоне строк. */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/** Получить сумму графы c первой строки и до указанной. */
def getSum(def dataRows, def columnAlias, def rowEnd) {
    def from = 0
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
}

/** Проверить принадлежит ли указанная строка к первому разделу (до строки "итого"). */
def isFirstSection(def dataRows, def row) {
    return row != null && row.getIndex() < getDataRow(dataRows, 'total').getIndex()
}

/** Проверить принадлежит ли указанная строка к разделу (A или B). */
def isSection(def dataRows, def row, def section) {
    return row != null &&
            row.getIndex() > getDataRow(dataRows, section).getIndex() &&
            row.getIndex() < getDataRow(dataRows, 'total' + section).getIndex()
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
    def from = (fromAlias != null ? getDataRow(sourceDataRows, fromAlias).getIndex() : 0)
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from > to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
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
        newRow.setIndex(index)
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
        newRow.provision = getRecordIdImport(86, '', row.cell[indexCell].text(), indexCell + 1, index, true)
        index++

        // графа 4
        // TODO (Ramil Timerbaev) получить значение из справочника
        newRow.nameBalanceAccount = getRecordIdImport(29, 'BALANCE_ACCOUNT', row.cell[indexCell].text(), indexCell + 1, index, true)
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
    def totalColumns = [] // TODO (Ramil Timerbaev)
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

/** Получить итоговую строку с суммами. */
def getCalcTotalRow() {
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.contract = 'Итого'

    def totalColumns = [] // TODO (Ramil Timerbaev)
    def tmp
    // задать нули
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(0)
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // просуммировать значения неитоговых строк
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        totalColumns.each { alias ->
            tmp = totalRow.getCell(alias).value + (row.getCell(alias).value ?: 0)
            totalRow.getCell(alias).setValue(tmp)
        }
    }
    return totalRow
}

/** Отсортировать / группировать строки (графа 2, 3). */
void sort(def dataRows) {
    def sortRows = []

    // первые строки
    def from = 0
    def to = getDataRow(dataRows, 'total').getIndex() - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    // раздел А
    from = getDataRow(dataRows, 'A').getIndex()
    to = getDataRow(dataRows, 'totalA').getIndex() - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    // раздела Б
    from = getDataRow(dataRows, 'B').getIndex()
    to = getDataRow(dataRows, 'totalB').getIndex() - 2
    if (from <= to) {
        sortRows.add(dataRows[from..to])
    }

    sortRows.each {
        it.sort {
            // графа 2  - debtor
            // графа 3  - provision
            def a, def b ->
                if (a.provision == b.provision) {
                    return a.debtor <=> b.debtor
                }
                def codeA = (a.provision ? getRefBookValue(86, a.provision)?.CODE?.value : null)
                def codeB = (b.provision ? getRefBookValue(86, b.provision)?.CODE?.value : null)
                return codeA <=> codeB
        }
    }
}

def calc6() {
    return roundValue(50, 0)
}

def calc7(def row) {
    if (row.debt45_90DaysSum == null || row.debt45_90DaysNormAllocation50per == null) {
        return null
    }
    def tmp = row.debt45_90DaysSum * row.debt45_90DaysNormAllocation50per / 100
    return roundValue(tmp, 2)
}

def calc9() {
    return roundValue(100, 0)
}

def calc10(def row) {
    if (row.debtOver90DaysSum == null || row.debtOver90DaysNormAllocation100per == null) {
        return null
    }
    def tmp = row.debtOver90DaysSum * row.debtOver90DaysNormAllocation100per / 100
    return roundValue(tmp, 2)
}

def calc11(def row) {
    if (row.debt45_90DaysReserve == null || row.debtOver90DaysReserve == null) {
        return null
    }
    return roundValue(row.debt45_90DaysReserve + row.debtOver90DaysReserve, 2)
}

def calc14(def row) {
    if (row.totalReserve == null || row.useReserve == null || row.reservePrev == null) {
        return null
    }
    return roundValue((row.totalReserve + row.useReserve > row.reservePrev ?
        row.totalReserve + row.useReserve - row.reservePrev : 0), 2)
}

def calc15(def row) {
    if (row.totalReserve == null || row.useReserve == null || row.reservePrev == null) {
        return null
    }
    return roundValue((row.totalReserve + row.useReserve < row.reservePrev ?
        row.reservePrev - (row.totalReserve + row.useReserve) : 0), 2)
}

def calc13(def row) {
    if (row.reservePrev == null || row.calcReserve == null || row.reserveRecovery == null || row.useReserve == null) {
        return null
    }
    return roundValue(row.reservePrev + row.calcReserve - row.reserveRecovery - row.useReserve, 2)
}

def calc13AB(def row) {
    if (row.reservePrev == null || row.useReserve == null) {
        return null
    }
    return roundValue(row.reservePrev - row.useReserve, 2)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}