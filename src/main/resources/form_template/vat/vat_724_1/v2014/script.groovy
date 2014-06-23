package form_template.vat.vat_724_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Отчёт о суммах начисленного НДС по операциям Банка
 * formTemplateId=600
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
// графа 8 - ndsBookSum

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
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
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
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
def allColumns = ['rowNum', 'baseAccName', 'baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Редактируемые атрибуты (графа 3..6, 8)
@Field
def editableColumns = ['baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsBookSum']

// Проверяемые на пустые значения атрибуты для разделов 1, 2, 3, 4 (1-4, 6-8)
@Field
def nonEmptyColumns1 = allColumns - 'baseAccName' - 'ndsNum'

// Проверяемые на пустые значения атрибуты для разделов 1*, 5, 6 (1-8)
@Field
def nonEmptyColumns2 = allColumns - 'baseAccName'

// Проверяемые на пустые значения атрибуты для разделов 7 (1-4, 6, 8)
@Field
def nonEmptyColumns3 = allColumns - 'baseAccName' - 'ndsNum' - 'ndsRate'

// Сортируемые атрибуты (графа 3, 5)
@Field
def sortColumns = ['baseAccNum', 'ndsNum']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6, 8)
@Field
def totalColumns = ['baseSum', 'ndsSum', 'ndsBookSum']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7']

// Дата окончания отчетного периода
@Field
def endDate = null

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
    def row = formData.createDataRow()
    def columns = (isSection7 ? editableColumns + 'ndsRate' : editableColumns)
    columns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - columns).each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // отсортировать/группировать
        sortRows(sectionsRows, sortColumns)

        // расчитать
        for (def row : sectionsRows) {
            // графа 7
            row.ndsRate = calc7(row, section)
        }

        // посчитать итоги по разделам
        def rows = (from <= to ? dataRows[from..to] : [])
        calcTotalSum(rows, lastRow, totalColumns)
    }
    updateIndexes(dataRows)

    // подсчет номера по порядку
    def number = 0
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 1
        row.rowNum = ++number
    }

    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def isSection1or2 = false
    def isSection5or6 = false
    def isSection7 = false
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isSection1or2 = (row.getAlias() == 'head_1' || row.getAlias() == 'head_2')
            isSection5or6 = (row.getAlias() == 'head_5' || row.getAlias() == 'head_6')
            isSection7 = row.getAlias() == 'head_7'
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        def columns = (isSection5or6 ? nonEmptyColumns2 : (isSection7 ? nonEmptyColumns3 : nonEmptyColumns1))
        checkNonEmptyColumns(row, index, columns, logger, true)

        // 2. Проверка суммы НДС по данным бухгалтерского учета и книге продаж
        if (row.ndsSum != row.ndsBookSum &&
                (isSection1or2 && row.ndsNum == '60309.01' || isSection5or6)) {
            rowWarning(logger, row, errorMsg + 'Сумма НДС по данным бухгалтерского учета не соответствует данным книги продаж!')
        }
    }

    def hasError = false
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, getFirstRowAlias(section))
        def lastRow = getDataRow(dataRows, getLastRowAlias(section))
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // 3. Проверка итоговых значений по разделам 1-7
        def tmpTotal = getTotalRow(sectionsRows, lastRow.getIndex())

        if (!hasError) {
            totalColumns.each { alias ->
                if (lastRow[alias] != tmpTotal[alias]) {
                    logger.error(WRONG_TOTAL, getColumnName(lastRow, alias))
                    hasError = true
                }
            }
        }

        // 4..6. Проверка номера балансового счета (графа 5) по разделам
        // 8. Проверка номера балансового счета (графа 5) по разделу между фиксированной строкой 2 и 4
        if (section == '7') {
            continue
        }
        def values5 = calc5(section)
        for (def row : sectionsRows) {
            if (!(row.ndsNum in values5)) {
                rowError(logger, row, 'Строка ' + row.getIndex() + ': Графа «' + getColumnName(row, 'ndsNum') + '» заполнена неверно!')
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить нефиксированные строки
    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    def firstRowAlias = getFirstRowAlias(section)
                    def lastRowAlias = getLastRowAlias(section)
                    copyRows(sourceDataRows, dataRows, firstRowAlias, lastRowAlias)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

// Удалить нефиксированные строки
void deleteNotFixedRows(def dataRows) {
    def deleteRows = []
    dataRows.each { row ->
        if (row.getAlias() == null) {
            deleteRows.add(row)
        }
    }
    if (!deleteRows.isEmpty()) {
        dataRows.removeAll(deleteRows)
        updateIndexes(dataRows)
    }
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceDataRows строки источника
 * @param destinationDataRows строки приемника
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно)
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def sourceDataRows, def destinationDataRows, def fromAlias, def toAlias) {
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = getDataRow(sourceDataRows, toAlias).getIndex() - 1
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null, 9, 4)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 9, 4)

    def headerMapping = [
            (xml.row[1].cell[2]): 'Налоговая база',
            (xml.row[1].cell[5]): 'НДС',
            (xml.row[1].cell[7]): 'Ставка НДС',
            (xml.row[1].cell[8]): 'Сумма НДС по книге продаж',
            (xml.row[2].cell[2]): 'Наименование балансового счета',
            (xml.row[2].cell[3]): 'Номер балансового счета',
            (xml.row[2].cell[4]): 'Сумма',
            (xml.row[2].cell[5]): 'Номер балансового счета',
            (xml.row[2].cell[6]): 'Сумма',
            (xml.row[2].cell[7]): 'Ставка НДС',
            (xml.row[2].cell[8]): 'Сумма НДС по книге продаж'
    ]
    (2..8).each { index ->
        headerMapping.put((xml.row[3].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def title = null
    def isFirstSection = true
    def isSection7 = false

    def rowHead7 = getDataRow(dataRows, 'head_7')

    def aliasR = [
            '1. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 18%': [getDataRow(dataRows, 'head_1')],
            'total_1': [getDataRow(dataRows, 'total_1')],
            '2. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 10%': [getDataRow(dataRows, 'head_2')],
            'total_2': [getDataRow(dataRows, 'total_2')],
            '3. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_3')],
            'total_3': [getDataRow(dataRows, 'total_3')],
            '4. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 10/110': [getDataRow(dataRows, 'head_4')],
            'total_4': [getDataRow(dataRows, 'total_4')],
            '5. Суммы полученной оплаты (частичной оплаты) в счёт предстоящего оказания услуг по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_5')],
            'total_5': [getDataRow(dataRows, 'total_5')],
            '6. Суммы, полученные в виде штрафов, пени, неустоек по расчётной ставке исчисления налога от общей суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_6')],
            'total_6': [getDataRow(dataRows, 'total_6')],
            '7. Суммы, отражённые в бухгалтерском учёте и книге продаж, не вошедшие в разделы с 1 по 6': [rowHead7],
            'total_7': [getDataRow(dataRows, 'total_7')]
    ]

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапок
        if (xmlIndexRow < headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == "") {
            title = row.cell[1].text()
            if (isFirstSection && title == 'Итого') {
                isFirstSection = false
            }
            isSection7 = (title == rowHead7.fix)
            continue
        }

        def newRow = getNewRow(isSection7)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 3 - атрибут 900 - ACCOUNT - «Номер балансового счета», справочник 101 «План счетов бухгалтерского учета»
        record = getRecordImport(101, 'ACCOUNT', row.cell[3].text(), xlsIndexRow, 3 + colOffset)
        newRow.baseAccNum = record?.record_id?.value

        // Графа 2 - зависит от графы 3 - атрибут 901 - ACCOUNT_NAME - «Наименование балансового счета», справочник 101 «План счетов бухгалтерского учета»
        if (record != null) {
            def value1 = record?.ACCOUNT_NAME?.value?.toString()
            def value2 = row.cell[2].text()
            formDataService.checkReferenceValue(101, value1, value2, xlsIndexRow, 2 + colOffset, logger, true)
        }

        // Графа 4
        newRow.baseSum = parseNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset, logger, true)

        // Графа 5
        newRow.ndsNum = row.cell[5].text()

        // Графа 6
        newRow.ndsSum = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, true)

        // Графа 7
        newRow.ndsRate = row.cell[7].text()

        // Графа 8
        newRow.ndsBookSum = parseNumber(row.cell[8].text(), xlsIndexRow, 8 + colOffset, logger, true)

        if (aliasR[title] == null) {
            logger.error("Строка %d: Структура файла не соответствует макету налоговой формы", xlsIndexRow)
        } else {
            aliasR[title].add(newRow)
        }
    }

    aliasR.each { k, v ->
        rows.addAll(v)
    }
    dataRowHelper.save(rows)
}

def getTotalRow(sectionsRows, def index) {
    def newRow = formData.createDataRow()
    totalColumns.each { alias ->
        newRow.getCell(alias).setValue(BigDecimal.ZERO, null)
    }
    for (def row : sectionsRows) {
        totalColumns.each { alias ->
            def value1 = newRow.getCell(alias).value
            def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
            newRow[alias] = value1 + value2
        }
    }
    return newRow
}

def getFirstRowAlias(def section) {
    return 'head_' + section
}

def getLastRowAlias(def section) {
    return 'total_' + section
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}