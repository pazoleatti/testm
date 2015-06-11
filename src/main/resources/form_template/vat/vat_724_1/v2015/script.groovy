package form_template.vat.vat_724_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Отчёт о суммах начисленного НДС по операциям Банка (v.2015)
 * formTemplateId=1600
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - baseAccName    - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
// графа 3 - baseAccNum     - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
// графа 4 - baseSum
// графа 5 - ndsNum
// графа 6 - ndsSum
// графа 7 - ndsRate
// графа 8 - ndsBookSum
// графа 9 - ndsDealSum

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNum', 'baseAccName', 'baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum', 'ndsDealSum']

// Редактируемые атрибуты (графа 3..6, 8)
@Field
def editableColumns = ['baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsBookSum']

// Проверяемые на пустые значения атрибуты для разделов 1, 2, 3, 4 (1-4, 6-8)
@Field
def nonEmptyColumns1 = ['baseAccNum', 'baseSum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Проверяемые на пустые значения атрибуты для разделов 1*, 5, 6 (1-8)
@Field
def nonEmptyColumns2 = ['baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Проверяемые на пустые значения атрибуты для разделов 7 (1-4, 6, 8)
@Field
def nonEmptyColumns3 = ['baseAccNum', 'baseSum', 'ndsSum', 'ndsBookSum', 'ndsDealSum']

// Сортируемые атрибуты (графа 3, 5, 2, 4, 6, 7, 8, 9)
@Field
def sortColumns = ['baseAccNum', 'ndsNum', 'baseAccName', 'baseSum', 'ndsSum', 'ndsRate', 'ndsBookSum', 'ndsDealSum']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6, 8)
@Field
def totalColumns = ['baseSum', 'ndsSum', 'ndsBookSum']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Добавить новую строку (строки между заглавными строками и строками итогов)
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index = 1
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head_')) {
            index++
        } else if (alias == 'total') {// строка "Всего" возвращает в шестой раздел
            index--
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total_7')
        if (lastRow != null) {
            index = lastRow.getIndex()
        }
    }
    def isSection7 = (index > getDataRow(dataRows, 'head_7').getIndex())
    dataRowHelper.insert(getNewRow(isSection7), index)
}

// Получить новую строку с заданными стилями
def getNewRow(def isSection7) {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    setRowStyles(row, isSection7)
    return row
}

void setRowStyles(def row, def isSection7) {
    def columns = (isSection7 ? editableColumns + ['ndsRate', 'ndsDealSum'] : editableColumns)
    columns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - columns).each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def superTotalRow = getDataRow(dataRows, 'total')
    totalColumns.each{
        superTotalRow[it] = BigDecimal.ZERO
    }

    for (def section : sections) {
        def isSection7 = section == '7'
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // расчитать
        if(formDataEvent != FormDataEvent.IMPORT) {
            for (def row : sectionsRows) {
                if (row.getAlias() == null) {
                    // графа 7
                    row.ndsRate = calc7(row, section)
                }
            }
        }

        // посчитать итоги по разделам
        def rows = (from <= to ? dataRows[from..to].findAll{ it.getAlias() == null } : [])

        calcTotalSum(rows, lastRow, isSection7 ? (totalColumns + 'ndsDealSum') : totalColumns)
        if (!isSection7) {
            totalColumns.each{
                superTotalRow[it] = superTotalRow[it] + (lastRow[it]?:0)
            }
        }
    }

    sortFormDataRows(false)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def isSection1or2or3 = false
    def isSection5or6 = false
    def isSection7 = false
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (row.getAlias().matches("(head_.+|total(_.+)?)")) {
                isSection1or2or3 = (row.getAlias() in ['head_1', 'head_2', 'head_3'])
                isSection5or6 = (row.getAlias() == 'head_5' || row.getAlias() == 'head_6')
                isSection7 = row.getAlias() == 'head_7'
            }
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        def columns = (isSection5or6 ? nonEmptyColumns2 : (isSection7 ? nonEmptyColumns3 : nonEmptyColumns1))
        checkNonEmptyColumns(row, index, columns, logger, true)

        // 2. Проверка суммы НДС по данным бухгалтерского учета и книге продаж
        if (row.ndsSum != row.ndsBookSum && (isSection1or2or3 && row.ndsNum == '60309.01' || isSection5or6)) {
            rowWarning(logger, row, errorMsg + 'Сумма НДС по данным бухгалтерского учета не соответствует данным книги продаж!' +
                    (isSection1or2or3 ?
                            "Ожидаемое значение (разделы 1-3): «Графа 6» = «Графа 8» в строках, в которых «Графа 5» = «60309.01»." :
                            "Ожидаемое значение (раздел 5 и 6): «Графа 6» = «Графа 8»."))
        }
    }

    def hasError = false
    Map<String, BigDecimal> superSums = [:]
    totalColumns.each{
        superSums[it] = BigDecimal.ZERO
    }

    for (def section : sections) {
        isSection7 = section == '7'
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // 3. Проверка итоговых значений по разделам 1-7
        def tmpTotal = formData.createDataRow()
        def columns = (isSection7 ? (totalColumns + 'ndsDealSum') : totalColumns)
        calcTotalSum(sectionsRows, tmpTotal, columns)

        if (!hasError) {
            columns.each { alias ->
                if (lastRow[alias] != tmpTotal[alias]) {
                    logger.error('Строка ' + lastRow.getIndex() + ': ' + WRONG_TOTAL, getColumnName(lastRow, alias))
                    hasError = true
                }
            }
        }

        // 4..6. Проверка номера балансового счета (графа 5) по разделам
        // 8. Проверка номера балансового счета (графа 5) по разделу между фиксированной строкой 2 и 4
        if (isSection7) {
            continue
        }
        totalColumns.each{
            superSums[it] = (superSums[it]?:0) + (lastRow[it]?:0)
        }
        def values5 = calc5(section)
        def endString = ''
        switch (section) {
            case '1':
            case '2':
            case '3':
            case '4': endString = "(разделы 1-4): пустое значение или «60309.01»."
                break
            case '5': endString = "(раздел 5): «60309.04»."
                break
            case '6': endString = "(раздел 6): «60309.05»."
        }
        for (def row : sectionsRows) {
            if (row.getAlias() == null && !(row.ndsNum in values5)) {
                rowError(logger, row, 'Строка ' + row.getIndex() + ': Графа «' + getColumnName(row, 'ndsNum') + '» заполнена неверно! Ожидаемое значение ' + endString)
            }
        }
    }
    def superTotalRow = getDataRow(dataRows, 'total')
    totalColumns.each { alias ->
        if (superTotalRow[alias] != superSums[alias]) {
            logger.error('Строка ' + superTotalRow.getIndex() + ': ' + WRONG_TOTAL, getColumnName(superTotalRow, alias))
        }
    }

}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить нефиксированные строки
    deleteExtraRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    def formSources = departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate())
    // сортируем по наименованию подразделения
    formSources.sort { departmentService.get(it.departmentId).name }
    formSources.each {
        if (it.formTypeId == formData.formType.id) {
            def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (child != null && child.state == WorkflowState.ACCEPTED) {
                def childDataRows = formDataService.getDataRowHelper(child).allCached
                def final department = departmentService.get(child.departmentId)
                // копирование данных по разделам
                sections.each { section ->
                    def isSection7 = section == '7'
                    def firstRowAlias = getFirstRowAlias(section)
                    def lastRowAlias = getLastRowAlias(section)
                    copyRows(childDataRows, dataRows, firstRowAlias, lastRowAlias, isSection7, department)
                }
            }
        }
    }
}

// Удалить нефиксированные строки
void deleteExtraRows(def dataRows) {
    def deleteRows = []
    dataRows.each { DataRow row ->
        if (!(row.getAlias() != null && (row.getAlias().matches("(head_.+|total(_.+)?)")))) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Получить произвольную фиксированную строку со стилями. */
def getFixedRow(String title, String alias, boolean isLongName) {
    def total = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    total.setAlias(alias)
    total.fix = title
    total.getCell('fix').colSpan = isLongName ? 9 : 2
    (allColumns + 'fix').each {
        total.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return total
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 * @param isSection7 седьмая ли секция
 * @param department подразделение источника
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias, def isSection7, def department) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)

    def headRow = getFixedRow(department.name, "sub_head_${department.id}", true)
    destinationDataRows.add(getDataRow(destinationDataRows, toAlias).getIndex() - 1, headRow)
    updateIndexes(destinationDataRows)

    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    updateIndexes(destinationDataRows)

    def subTotalRow = getFixedRow("Итого по ${department.name}", "sub_total_${department.id}", false)
    def columns = (isSection7 ? (totalColumns + 'ndsDealSum') : totalColumns)
    calcTotalSum(copyRows, subTotalRow, columns)
    destinationDataRows.add(getDataRow(destinationDataRows, toAlias).getIndex() - 1, subTotalRow)
    updateIndexes(destinationDataRows)
}

def calc5(def section) {
    def tmp = null
    switch (section) {
        case '1':
        case '2':
        case '3':
        case '4':
            tmp = [null, '', '60309.01']
            break
        case '5':
            tmp = ['60309.04']
            break
        case '6':
            tmp = ['60309.05']
            break
    }
    return tmp
}

def calc7(def row, def section) {
    def tmp = null
    switch (section) {
        case '1':
            tmp = '18'
            break
        case '2':
            tmp = '10'
            break
        case '3':
        case '5':
        case '6':
            tmp = '18/118'
            break
        case '4':
            tmp = '10/110'
            break
        case '7':
            tmp = row.ndsRate
            break
    }
    return tmp
}

def getFirstRowAlias(def section) {
    return 'head_' + section
}

def getLastRowAlias(def section) {
    return 'total_' + section
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (dataRows.find { it.getAlias() != null && it.getAlias().startsWith("sub_head_") } == null) {
        for (def section : sections) {
            def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
            def lastRow = getDataRow(dataRows, getLastRowAlias(section))
            def from = firstRow.getIndex()
            def to = lastRow.getIndex() - 1
            def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

            // Массовое разыменование строк НФ
            def columnList = firstRow.keySet().collect{firstRow.getCell(it).getColumn()}
            refBookService.dataRowsDereference(logger, sectionsRows, columnList)

            sortRows(sectionsRows, sortColumns)
        }

        if (saveInDB) {
            dataRowHelper.saveSort()
        } else {
            updateIndexes(dataRows);
        }
    }
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
    int COLUMN_COUNT = 9
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def mapRows = [:]

    try {
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null && !isEmptyCells(rowCells)) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }

            def newRow = getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex)
            if (rowCells.length < 10 || newRow == null) {
                continue
            }
            // определить раздел по техническому полю и добавить строку в нужный раздел
            def sectionIndex = pure(rowCells[10])
            if (mapRows[sectionIndex] == null) {
                mapRows[sectionIndex] = []
            }

            mapRows[sectionIndex].add(newRow)
        }
    } finally {
        reader.close()
    }

    def newRows = (mapRows.values().sum { it } ?: [])
    showMessages(newRows, logger)
    if (logger.containsLevel(LogLevel.ERROR) || newRows == null || newRows.isEmpty()) {
        return
    }

    // сравнение итогов
    if (totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [ 'baseSum' : 4, 'ndsSum' : 6, 'ndsBookSum' : 8, 'ndsDealSum' : 9 ]
        // итоговая строка для сверки сумм
        def totalTmp = formData.createStoreMessagingDataRow()
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
        }
        // подсчет итогов
        for (def row : newRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalTmp.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalTmp.getCell(alias).setValue(value1 + value2, null)
            }
        }
        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
    }

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows

    def rows = []
    sections.each { section ->
        def firstRow = getDataRow(templateRows, getFirstRowAlias(section))
        def lastRow = getDataRow(templateRows, getLastRowAlias(section))
        def copyRows = mapRows[section]

        rows.add(firstRow)
        if (copyRows != null && !copyRows.isEmpty()) {
            rows.addAll(copyRows)

            // расчет итогов
            calcTotalSum(copyRows, lastRow, totalColumns)
        }
        rows.add(lastRow)

        if ('6' == section) {
            rows.add(getDataRow(templateRows, 'total'))
        }
    }

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex
    def cell

    // графа 3 - атрибут 900 - ACCOUNT - «Номер счета», справочник 101 «План счетов бухгалтерского учета»
    colIndex = 3
    def record101 = getRecordImport(101, 'ACCOUNT', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.baseAccNum = record101?.record_id?.value

    // графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование счета», справочник 101 «План счетов бухгалтерского учета»
    if (record101 != null) {
        colIndex = 2
        def value1 = pure(rowCells[colIndex])
        def value2 = record101?.ACCOUNT_NAME?.value?.toString()
        formDataService.checkReferenceValue(101, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4
    colIndex = 4
    cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
    newRow.baseSum = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5
    colIndex++
    newRow.ndsNum = pure(rowCells[colIndex])

    // графа 6
    colIndex++
    cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
    newRow.ndsSum = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7
    colIndex++
    newRow.ndsRate = pure(rowCells[colIndex])

    // графа 8
    colIndex++
    cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
    newRow.ndsBookSum = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex++
    cell = pure(rowCells[colIndex])?.replaceAll(",", ".")
    newRow.ndsDealSum = parseNumber(cell, fileRowIndex, colIndex + colOffset, logger, true)

    // Техническое поле(группа)
    colIndex++
    def sectionIndex = pure(rowCells[colIndex])
    setRowStyles(newRow, sectionIndex == '7')

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 9
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def title = null
    def isFirstSection = true
    def isSection7 = false

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def rowHead7 = getDataRow(templateRows, 'head_7')

    def aliasR = [
            '1. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 18%': [getDataRow(templateRows, 'head_1')],
            'total_1': [getDataRow(templateRows, 'total_1')],
            '2. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 10%': [getDataRow(templateRows, 'head_2')],
            'total_2': [getDataRow(templateRows, 'total_2')],
            '3. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(templateRows, 'head_3')],
            'total_3': [getDataRow(templateRows, 'total_3')],
            '4. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 10/110': [getDataRow(templateRows, 'head_4')],
            'total_4': [getDataRow(templateRows, 'total_4')],
            '5. Суммы полученной оплаты (частичной оплаты) в счёт предстоящего оказания услуг по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(templateRows, 'head_5')],
            'total_5': [getDataRow(templateRows, 'total_5')],
            '6. Суммы, полученные в виде штрафов, пени, неустоек по расчётной ставке исчисления налога от общей суммы полученного дохода 18/118': [getDataRow(templateRows, 'head_6')],
            'total_6': [getDataRow(templateRows, 'total_6')],
            'total': [getDataRow(templateRows, 'total')],
            '7. Сумма налога, подлежащая вычету у продавца, по которой отгрузка соответствующих товаров осуществлена в текущем отчетном периоде': [rowHead7],
            'total_7': [getDataRow(templateRows, 'total_7')]
    ]

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++

        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // Пропуск итоговых строк
        if (!rowValues[INDEX_FOR_SKIP]) {
            title = rowValues[1]
            if (isFirstSection && title == 'Итого') {
                isFirstSection = false
            }
            isSection7 = (title == rowHead7.fix)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        if (aliasR[title] == null) {
            logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
        } else {
            // простая строка
            rowIndex++
            def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, isSection7)
            aliasR[title].add(newRow)
        }
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    aliasR.each { k, v ->
        rows.addAll(v)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[1][2]): 'Налоговая база',
            (headerRows[1][5]): 'НДС',
            (headerRows[1][7]): 'Ставка НДС',
            (headerRows[1][8]): 'Сумма НДС по книге продаж',
            (headerRows[2][2]): 'Наименование балансового счета',
            (headerRows[2][3]): 'Номер балансового счета',
            (headerRows[2][4]): 'Сумма',
            (headerRows[2][5]): 'Номер балансового счета',
            (headerRows[2][6]): 'Сумма',
            (headerRows[2][7]): 'Ставка НДС',
            (headerRows[2][8]): 'Сумма НДС по книге продаж',
            (headerRows[2][9]): 'Сумма НДС по книге покупок'
    ]
    (2..9).each { index ->
        headerMapping.put((headerRows[3][index]), index.toString())
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 * @param isSection7 признак что строка для раздела 7
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isSection7) {
    def newRow = getNewRow(isSection7)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // Графа 3 - атрибут 900 - ACCOUNT - «Номер балансового счета», справочник 101 «План счетов бухгалтерского учета»
    def colIndex = 3
    record = getRecordImport(101, 'ACCOUNT', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.baseAccNum = record?.record_id?.value

    // Графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
    if (record != null) {
        colIndex = 2
        def value1 = values[colIndex]
        def value2 = record?.ACCOUNT_NAME?.value?.toString()
        formDataService.checkReferenceValue(101, value1, value2, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // Графа 4
    colIndex = 4
    newRow.baseSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // Графа 5
    colIndex = 5
    newRow.ndsNum = values[colIndex]

    // Графа 6
    colIndex = 6
    newRow.ndsSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // Графа 7
    colIndex = 7
    newRow.ndsRate = values[colIndex]

    // Графа 8
    colIndex = 8
    newRow.ndsBookSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // Графа 9
    colIndex = 9
    newRow.ndsDealSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}