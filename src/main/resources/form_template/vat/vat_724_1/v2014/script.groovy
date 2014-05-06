package form_template.vat.vat_724_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Отчёт о суммах начисленного НДС по операциям Банка
 * formTemplateId=600
 *
 * TODO:
 *      - графа 2, 3, 5, 7 - непонятно графа строковая или спавочная
 *      - графа 2 пока сделана редактируемой потому что непонятности со справочными типами
 *      - не сделано: для графы 5 и графы 7 в чтз в перечне полей есть огрничения значении по разделам
 *      - не сделано: для графы 5 в чтз в перечне полей есть комментарий:
 *              После выбора значения в «Графе 5» значение «Графы 2» и «Графы 3» очищается
 *              это делать в коде?
 *      - логическая проверка 3 - не выполняется потому что графа 7 имеет тип строка
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - baseAccName    - зависит от графы 3 - атрибут 000 - NAME - «Наименование балансового счета», справочник 00 «Балансовые счета»
// графа 3 - baseAccNum     - атрибут 000 - NAME - «Номер балансового счета», справочник 00 «Балансовые счета»
// графа 4 - baseSum
// графа 5 - ndsNum         - атрибут 000 - NAME - «Номер балансового счета», справочник 00 «Балансовые счета»
// графа 6 - ndsSum
// графа 7 - ndsRate        - атрибут 000 - NAME - «Ставка», справочник 00 «Ставки НДС»
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

@Field
def allColumns = ['rowNum', 'baseAccName', 'baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Редактируемые атрибуты (графа 2, 3..8) // TODO (Ramil Timerbaev) графу 2 потом возможно надо будет убрать
@Field
def editableColumns = ['baseAccName', 'baseAccNum', 'baseSum', 'ndsNum', 'ndsSum', 'ndsRate', 'ndsBookSum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (1..4, 6..8)
@Field
def nonEmptyColumns = allColumns - 'ndsNum'

// Сортируемые атрибуты (графа 3, 5)
@Field
def sortColumns = ['baseAccNum', 'ndsNum']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 6, 8)
@Field
def totalColumns = ['baseSum', 'ndsSum', 'ndsBookSum']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7']

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
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
    dataRowHelper.insert(getNewRow(), index)
}

// Получить новую строку с заданными стилями
def getNewRow() {
    def row = formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'head_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])

        // отсортировать/группировать
        sortRows(sectionsRows, sortColumns)

        // расчитать
        def value7 = calc7(section)
        for (def row : sectionsRows) {
            // графа 7
            row.ndsRate = value7
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
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            isSection1or2 = (row.getAlias() == 'head_1' || row.getAlias() == 'head_2')
            isSection5or6 = (row.getAlias() == 'head_5' || row.getAlias() == 'head_6')
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка суммы НДС по данным бухгалтерского учета и книге продаж
        if (row.ndsSum != row.ndsBookSum &&
                (isSection1or2 && row.ndsNum == '60309.01' || isSection5or6)) {
            logger.warn(errorMsg + 'Сумма НДС по данным бухгалтерского учета не соответствует данным книги продаж!')
        }

        // TODO (Ramil Timerbaev) пока исключил эту проверку потому что графа 7 имеет тип строка
        // 3. Проверка суммы НДС
        if (false && row.baseSum != null && row.ndsRate != null) {
            def tmp = row.baseSum * row.ndsRate
            def tmp1 = tmp + (tmp * 3) / 100
            def tmp2 = tmp - (tmp * 3) / 100
            if (tmp1 > tmp2) {
                logger.warn(errorMsg + 'Сумма НДС по данным бухгалтерского учета не соответствует налоговой базе!')
            }
        }
    }

    for (def section : sections) {
        def firstRow = getDataRow(dataRows, 'head_' + section)
        def lastRow = getDataRow(dataRows, 'total_' + section)
        def from = firstRow.getIndex()
        def to = lastRow.getIndex() - 1

        // 4. Проверка итоговых значений по разделам 1-7
        def rows = (from <= to ? dataRows[from..to] : [])
        checkTotalSum(rows, totalColumns, logger, true)

        // 5..8. Проверка номера балансового счета (графа 5) по разделам
        def sectionsRows = (from < to ? dataRows[from..(to - 1)] : [])
        def value5 = calc5(section)
        for (def row : sectionsRows) {
            if (row.ndsNum != null && row.ndsNum != '' && row.ndsNum != value5) {
                logger.error('Строка %d: Графа «%s» заполнена неверно!', row.getIndex(), getColumnName(row, 'ndsNum'))
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
                    copyRows(sourceDataRows, dataRows, 'head_' + section, 'total_' + section)
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

def calc5(def section) {
    def tmp = null
    switch (section) {
        case '1':
        case '2':
        case '3':
        case '4':
            tmp = '60309.01'
            break
        case '5':
            tmp = '60309.04'
            break
        case '6':
            tmp = '60309.05'
            break
        case '7':
            break
    }
    return tmp
}

def calc7(def section) {
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
            tmp = '10/118'
            break
        case '7':
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

    addData(xml, 3)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    def aliasR = [
            '1. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 18%': [getDataRow(dataRows, 'head_1')],
            'head_1': [getDataRow(dataRows, 'total_1')],
            '2. Суммы, полученные от реализации товаров (услуг, имущественных прав) по ставке 10%': [getDataRow(dataRows, 'head_2')],
            'head_2': [getDataRow(dataRows, 'total_2')],
            '3. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_3')],
            'head_3': [getDataRow(dataRows, 'total_3')],
            '4. Суммы, полученные от реализации товаров (услуг, имущественных прав) по расчётной ставке исчисления налога от суммы полученного дохода 10/110': [getDataRow(dataRows, 'head_4')],
            'head_4': [getDataRow(dataRows, 'total_4')],
            '5. Суммы полученной оплаты (частичной оплаты) в счёт предстоящего оказания услуг по расчётной ставке исчисления налога от суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_5')],
            'head_5': [getDataRow(dataRows, 'total_5')],
            '6. Суммы, полученные в виде штрафов, пени, неустоек по расчётной ставке исчисления налога от общей суммы полученного дохода 18/118': [getDataRow(dataRows, 'head_6')],
            'head_6': [getDataRow(dataRows, 'total_6')],
            '7. Суммы, отражённые в бухгалтерском учёте и книге продаж, не вошедшие в разделы с 1 по 6': [getDataRow(dataRows, 'head_7')],
            'head_7': [getDataRow(dataRows, 'total_7')]
    ]

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапок
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == "") {
            title = row.cell[1].text()
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // Графа 2
        newRow.baseAccName = row.cell[2].text()

        // Графа 3
        newRow.baseAccNum = row.cell[3].text()

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

        aliasR[title].add(newRow)
    }
    aliasR.each { k, v ->
        rows.addAll(v)
    }
    dataRowHelper.save(rows)
}