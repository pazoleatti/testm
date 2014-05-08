package form_template.income.rnu40_1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * (РНУ-40.1) Регистр налогового учёта начисленного процентного дохода по прочим дисконтным облигациям. Отчёт 1
 * formTemplateId=338
 *
 * @author auldanov
 */

// графа    - fix
// графа 1  - number                - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
// графа 2  - name                  - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
// графа 3  - issuer                - атрибут 809 - ISSUER - «Эмитент», справочника 84 «Ценные бумаги»
// графа 4  - registrationNumber    - зависит от графы 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
// графа 5  - buyDate
// графа 6  - cost
// графа 7  - bondsCount
// графа 8  - upCost
// графа 9  - circulationTerm
// графа 10 - percent
// графа 11 - currencyCode          - зависит от графы 3 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочника 84 «Ценные бумаги»

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
    case FormDataEvent.COMPOSE :
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

// все атрибуты
@Field
def allColumns = ['fix', 'number', 'name', 'issuer', 'registrationNumber', 'buyDate', 'cost',
        'bondsCount', 'upCost', 'circulationTerm', 'percent', 'currencyCode']

// Редактируемые атрибуты (графа 2, 3, 5..9)
@Field
def editableColumns = ['name', 'issuer', 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 2, 3, 5..10)
@Field
def nonEmptyColumns = ['name', 'issuer', 'buyDate', 'cost', 'bondsCount', 'upCost', 'circulationTerm', 'percent']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 7, 10)
@Field
def totalSumColumns = ['bondsCount', 'percent']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8']

// Дата окончания отчетного периода
@Field
def endDate = null

// Текущая дата
@Field
def currentDate = new Date()

@Field
def taxPeriod = null

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
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

// Поиск записи в справочнике по значению
def getRecord(def refBookId, def String alias, def String value) {
    return getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, getReportPeriodEndDate())
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

/** Добавить новую строку. */
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index

    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        index = getDataRow(dataRows, 'total1').getIndex()
    } else if (currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    } else {
        def alias = currentDataRow.getAlias()
        if (alias.contains('total')) {
            index = getDataRow(dataRows, alias).getIndex()
        } else {
            index = getDataRow(dataRows, 'total' + alias).getIndex()
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // отсортировать/группировать
    sort(dataRows)

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // графа 10
        row.percent = calc10(row, lastDay)
    }

    // посчитать итоги по разделам
    sections.each { section ->
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        totalSumColumns.each { alias ->
            lastRow.getCell(alias).setValue(getSum(dataRows, alias, firstRow, lastRow), null)
        }
    }
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def lastDay = getReportPeriodEndDate()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Арифметическая проверка графы 10
        def needValue = [:]
        needValue['percent'] = calc10(row, lastDay)
        checkCalc(row, ['percent'], needValue, logger, true)
    }

    // 3. Арифметическая проверка строк промежуточных итогов (графа 7, 10)
    for (def section : sections) {
        def firstRow = getDataRow(dataRows, section)
        def lastRow = getDataRow(dataRows, 'total' + section)
        for (def col : totalSumColumns) {
            def value = roundValue(lastRow.getCell(col).value ?: 0, 6)
            def sum = roundValue(getSum(dataRows, col, firstRow, lastRow), 6)
            if (sum != value) {
                def name = lastRow.getCell(col).column.name
                logger.error("Неверно рассчитаны итоговые значения для раздела $section в графе \"$name\"!")
            }
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    deleteNotFixedRows(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.findMonth(it.formTypeId, it.kind, it.departmentId, getTaxPeriod()?.id, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, section, 'total' + section)
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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

/** Получить новую стролу с заданными стилями. */
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

/** Отсорировать данные (по графе 1, 2). */
void sort(def dataRows) {
    // список со списками строк каждого раздела для сортировки
    def sortRows = []

    // подразделы, собрать список списков строк каждого раздела
    sections.each { section ->
        def from = getDataRow(dataRows, section).getIndex()
        def to = getDataRow(dataRows, 'total' + section).getIndex() - 2
        if (from <= to) {
            sortRows.add(dataRows[from..to])
        }
    }

    // отсортировать строки каждого раздела
    sortRows.each { sectionRows ->
        sectionRows.sort { def a, def b ->
            // графа 1  - number (справочник)
            // графа 2  - name (справочник)
            def recordA = getRefBookValue(30, a.name)
            def recordB = getRefBookValue(30, b.name)
            def numberA = recordA?.SBRF_CODE?.value
            def numberB = recordB?.SBRF_CODE?.value
            if (numberA == numberB) {
                def nameA = recordA?.NAME?.value
                def nameB = recordB?.NAME?.value
                return nameA <=> nameB
            }
            return numberA <=> numberB
        }
    }
}

/** Получить сумму столбца. */
def getSum(def dataRows, def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getIndex()
    def to = rowEnd.getIndex() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, dataRows, new ColumnRange(columnAlias, from, to))
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
 * Получить значение для графы 10.
 *
 * @param row строка нф
 * @param lastDay последний день отчетного месяца
 */
def calc10(def row, def lastDay) {
    if (row.buyDate == null || row.cost == null || row.bondsCount == null || row.upCost == null ||
            row.circulationTerm == null || row.upCost == null || row.bondsCount == 0 || row.circulationTerm == 0) {
        return null
    }
    def tmp
    tmp = ((row.cost / row.bondsCount) - row.upCost) * ((lastDay - row.buyDate) / row.circulationTerm) * row.bondsCount
    tmp = roundValue(tmp, 2)

    def rate = getRate(row, lastDay)
    if (rate == null) {
        return null
    }

    tmp = tmp * rate
    return roundValue(tmp, 2)
}

/** Поправить индексы. */
void updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def getTaxPeriod() {
    if (taxPeriod == null) {
        taxPeriod = reportPeriodService.get(formData.reportPeriodId).taxPeriod
    }
    return taxPeriod
}

// Получение импортируемых данных
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    def xml = getXML(ImportInputStream, importService, fileName, 'Номер территориального банка', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 11, 2)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Номер территориального банка ',
            (xml.row[0].cell[2]) : 'Наименование территориального банка / подразделения Центрального аппарата',
            (xml.row[0].cell[3]) : 'Эмитент',
            (xml.row[0].cell[4]) : 'Номер государственной регистрации',
            (xml.row[0].cell[5]) : 'Дата приобретения',
            (xml.row[0].cell[6]) : 'Номинальная стоимость, ед. вал.',
            (xml.row[0].cell[7]) : 'Количество облигаций, шт.',
            (xml.row[0].cell[8]) : 'Средневзвешенная цена одной бумаги на дату размещения, ед.вал.',
            (xml.row[0].cell[9]) : 'Срок обращения условиям выпуска, дни',
            (xml.row[0].cell[10]): 'Процентный доход, руб.коп.',
            (xml.row[0].cell[11]): 'Код валюты номинала'
    ]

    (1..11).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 0 // Смещение для индекса колонок в ошибках импорта

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
        def firstValue = row.cell[0].text()
        if (firstValue != null && firstValue != '' && firstValue != 'Всего') {
            sectionIndex = firstValue[0]
            mapRows.put(sectionIndex, [])
            continue
        } else if (firstValue == 'Всего') {
            continue
        }

        def newRow = getNewRow()
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // графа 2 - атрибут 161 - NAME - "Наименование подразделения", справочник 30 "Подразделения"
        def xmlIndexCol = 2
        def record30 = getRecordImport(30, 'NAME', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        newRow.name = record30?.record_id?.value

        // графа 1 - зависит от графы 2 - атрибут 166 - SBRF_CODE - "Код подразделения в нотации Сбербанка", справочник 30 "Подразделения"
        if (record30 != null) {
            xmlIndexCol = 1
            formDataService.checkReferenceValue(30, row.cell[xmlIndexCol].text(), record30?.SBRF_CODE?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        // графа 3 - атрибут 809 - ISSUER - «Эмитент», справочника 84 «Ценные бумаги»
        xmlIndexCol = 3
        def record84 = getRecordImport(84, 'ISSUER', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        newRow.issuer = record84?.record_id?.value

        // графа 4 - зависит от графы 3 - атрибут 813 - REG_NUM - «Государственный регистрационный номер», справочника 84 «Ценные бумаги»
        if (record84 != null) {
            xmlIndexCol = 4
            formDataService.checkReferenceValue(84, row.cell[xmlIndexCol].text(), record84?.REG_NUM?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }

        // графа 5
        xmlIndexCol = 5
        newRow.buyDate = getDate(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        newRow.cost = getNumber(row.cell[6].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 7
        newRow.bondsCount = getNumber(row.cell[7].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        newRow.upCost = getNumber(row.cell[8].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        newRow.circulationTerm = getNumber(row.cell[9].text(), xlsIndexRow, xmlIndexCol + colOffset)

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

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def lastDay) {
    if (row.issuer == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def code = getRefBookValue(84, row.issuer)?.CODE_CUR?.value
    // получить id записи из справочника валют (15) по цифровому коду валюты
    def recordId = getRecordId(15, 'CODE', code?.toString(), row.getIndex(), getColumnName(row, 'issuer'), lastDay)
    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = getRefBookRecord(22, 'CODE_NUMBER', recordId?.toString(), lastDay, row.getIndex(), null, true)
    return record22?.RATE?.value
}