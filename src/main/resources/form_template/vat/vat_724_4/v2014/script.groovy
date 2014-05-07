package form_template.vat.vat_724_4.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * (724.4) Отчёт по сумме НДС, уплаченного в бюджет, в случае возврата ранее реализованных товаров (отказа от услуг)
 * или возврата соответствующих сумм авансовых платежей.
 *
 * formTemplateId=603
 *
 * TODO:
 *      - графа 2, 3, 5 - непонятно графа строковая или спавочная
 *      - графа 7 пока не справочный, а строковый, потому что нет справочника "ставки ндс"
 *      - логическая провека 5 и 6: не понятно какое название графы выводить: оба или одно
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - name
// графа 3 - number
// графа 4 - sum
// графа 5 - number2
// графа 6 - sum2
// графа 7 - nds        - атрибут 000 - ???? - «Ставка», справочник ?? «Ставки НДС» // TODO (Ramil Timerbaev) пока нет этого справочника

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
    case FormDataEvent.ADD_ROW :
        addRow()
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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

// все атрибуты
@Field
def allColumns = ['rowNum', 'fix', 'name', 'number', 'sum', 'number2', 'sum2', 'nds']

// Редактируемые атрибуты (графа 2, 3..7) // TODO (Ramil Timerbaev) графу 2 потом возможно надо будет убрать
@Field
def editableColumns = ['name', 'number', 'sum', 'number2', 'sum2', 'nds']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..4, 6)
@Field
def nonEmptyColumns = ['rowNum', 'name', 'number', 'sum', 'sum2']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6)
@Field
def totalColumns = ['sum', 'sum2']

// Группируемые атрибуты (графа 3, 5)
@Field
def sortColumns = ['number', 'number2']

// список алиасов подразделов
@Field
def sections = ['1', '2']

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head')) {
            index++
        }
    } else {
        def lastRow = getDataRow(dataRows, 'total2')
        index = lastRow.getIndex()
    }
    dataRowHelper.insert(getNewRow(), index)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'head' + section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // отсортировать/группировать
        sortRows(sectionsRows, sortColumns)

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

    def isSection1 = false
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isSection1 = (row.getAlias() == 'head1')
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // TODO (Ramil Timerbaev) пока исключил эту проверку потому что графа 7 имеет тип строка
        // 2. Проверка суммы НДС
        if (false && row.sum != null && row.nds != null && row.sum2 != null) {
            def tmp = row.sum * row.nds
            def tmp1 = tmp + (tmp * 3) / 100
            def tmp2 = tmp - (tmp * 3) / 100
            if (tmp1 > row.sum2 && row.sum2 > tmp2) {
                logger.warn(errorMsg + 'Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе!')
            }
        }

        // 4..5. Проверка номера балансового счета (графа 5) по разделам
        if (row.number2 != null && row.nds != null) {
            def logicCheck5 = isSection1 &&
                    ((row.number2 == '60309.01' && row.nds in ['10', '18', '10/110', '18/118']) ||
                            (row.number2 in ['60309.04', '60309.05'] && row.nds == '18/118'))
            def logicCheck6 = (!isSection1 && row.number2 in ['60309.02', '60309.03'] && row.nds == '18/118')
            if (isSection1 ? !logicCheck5 : !logicCheck6) {
                def columns = "«${getColumnName(row, 'number2')}», «${getColumnName(row, 'nds')}»"
                logger.error('Строка %d: Графы %s заполнены неверно!', row.getIndex(), columns)
            }
        }
    }

    // 3. Проверка итоговых значений по разделам
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'head' + section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])
        def tmpTotal = getTotalRow(sectionsRows)
        def hasError = false
        totalColumns.each { alias ->
            if (lastRow[alias] != tmpTotal[alias]) {
                logger.error(WRONG_TOTAL, getColumnName(lastRow, alias))
                hasError = true
            }
        }
        if (hasError) {
            break
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
                    copyRows(sourceDataRows, dataRows, 'head' + section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null, 8, 4)

    checkHeaderSize(xml.row[3].cell.size(), xml.row.size(), 8, 4)

    def headerMapping = [
            (xml.row[0].cell[0]): getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[2]): 'Данные бухгалтерского учёта',
            (xml.row[0].cell[7]): getColumnName(tmpRow, 'nds'),

            (xml.row[1].cell[2]): 'Налоговая база',
            (xml.row[1].cell[5]): 'НДС',

            (xml.row[2].cell[2]): 'наименование балансового счёта',
            (xml.row[2].cell[3]): 'номер балансового счёта',
            (xml.row[2].cell[4]): 'сумма',
            (xml.row[2].cell[5]): 'номер балансового счёта',
            (xml.row[2].cell[6]): 'сумма',

            (xml.row[3].cell[0]): '1'
    ]

    (2..7).each { index ->
        headerMapping.put((xml.row[3].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def sectionIndex = null
    def mapRows = [:]

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        def firstValue = row.cell[1].text()
        if (firstValue != null && firstValue != '' && firstValue != 'Итого') {
            sectionIndex = firstValue[0]
            mapRows.put(sectionIndex, [])
            continue
        } else if (firstValue == 'Итого') {
            continue
        }

        def newRow = getNewRow()
        def int xlsIndexRow = xmlIndexRow + rowOffset
        def xmlIndexCol = 1

        // графа 2
        xmlIndexCol++
        newRow.name = row.cell[xmlIndexCol].text()

        // графа 3
        xmlIndexCol++
        newRow.number = row.cell[xmlIndexCol].text()

        // графа 4
        xmlIndexCol++
        newRow.sum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol++
        newRow.number2 = row.cell[xmlIndexCol].text()

        // графа 6
        xmlIndexCol++
        newRow.sum2 = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // TODO (Ramil Timerbaev) это графа пока строковая, должна быть справочной, справочник пока не готов
        // графа 7
        xmlIndexCol++
        newRow.nds = row.cell[xmlIndexCol].text()

        mapRows[sectionIndex].add(newRow)
    }

    deleteNotFixedRows(dataRows)

    // копирование данных по разделам
    sections.each { section ->
        def copyRows = mapRows[section]
        if (copyRows != null && !copyRows.isEmpty()) {
            def insertIndex = getDataRow(dataRows, 'total' + section).getIndex() - 1
            dataRows.addAll(insertIndex, copyRows)
            // поправить индексы, потому что они после вставки не пересчитываются
            updateIndexes(dataRows)
        }
    }

    dataRowHelper.save(dataRows)
}

def getTotalRow(sectionsRows) {
    def newRow = formData.createDataRow()
    totalColumns.each { alias ->
        newRow.getCell(alias).setValue(BigDecimal.ZERO, null)
    }
    for (def row : sectionsRows) {
        totalColumns.each { alias ->
            def value1 = newRow.getCell(alias).value
            def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
            newRow.getCell(alias).setValue(value1 + value2, null)
        }
    }
    return newRow
}