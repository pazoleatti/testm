package form_template.vat.vat_724_10.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * (724.10) Расчет суммы доначисления НДС в связи с применением тарифов и цен, не соответствующих рыночному уровню, по сделкам с Взаимозависимыми лицами и резидентами оффшорных зон.
 * formTemplateId=623
 */

// графа 1 - rowNum
// графа   - fix
// графа 2 - name       - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
// графа 3 - iksr       - зависит от графы 2 - атрибут 5218 - IKSR - «IKSR», справочник 520 «Участники ТЦО»
// графа 4 - date
// графа 5 - nameOf
// графа 6 - rnu
// графа 7 - number
// графа 8 - sum
// графа 9 - ndsSum

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
    case FormDataEvent.MOVE_ACCEPTED_TO_APPROVED: // из "Принята" в "Утверждена"
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:  // из "Принята" в "Создана"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED: // из "Принята" в "Подготовлена"
        checkDeclarations()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
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
def allColumns = ['rowNum', 'fix', 'name', 'iksr', 'date', 'nameOf', 'rnu', 'number', 'sum', 'ndsSum']

// Редактируемые атрибуты (графа 2, 4..9)
@Field
def editableColumns = ['name', 'date', 'nameOf', 'rnu', 'number', 'sum', 'ndsSum']

// Проверяемые на пустые значения атрибуты для разделов (графа 2, 4..9)
@Field
def nonEmptyColumns = editableColumns

// Атрибуты итоговых строк для которых вычисляются суммы (графа 8, 9)
@Field
def totalColumns = ['sum', 'ndsSum']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4']

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
    def index = 0
    if (currentDataRow != null) {
        index = currentDataRow.getIndex()
    } else {
        def lastRow = getDataRow(dataRows, 'total4')
        if (lastRow != null) {
            index = lastRow.getIndex()
        }
    }
    dataRowHelper.insert(getNewRow(), index)
}

// Получить новую строку с заданными стилями
def getNewRow() {
    def row = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    (allColumns - editableColumns).each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return row
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    // расчет итогов для каждого раздела
    for (def section : sections) {
        def rows = getSectionRows(dataRows, section)
        def totalRow = getDataRow(rows, 'total' + section)
        if (!rows) {
            totalColumns.each{
                totalRow[it] = BigDecimal.ZERO
            }
            continue
        }
        calcTotalSum(rows, totalRow, totalColumns)
    }
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        if (row.getAlias()) {
            continue
        }
        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка суммы в графе 9
        if (row.ndsSum != calc9(row)) {
            logger.warn("Строка %d: Графа «%s» заполнена неверно! Данная графа должна принимать следующее значение: Значение графы «%s» * 0.18!",
                    row.getIndex(), getColumnName(row, 'ndsSum'), getColumnName(row, 'sum'))
        }
    }

    // 3. Проверка итоговых значений
    for (def section : sections) {
        def rows = getSectionRows(dataRows, section)
        def totalRow = getDataRow(rows, 'total' + section)
        def tmpTotal = formData.createStoreMessagingDataRow()
        calcTotalSum(rows, tmpTotal, totalColumns)
        totalColumns.each { alias ->
			if (tmpTotal[alias] != totalRow[alias]) {
				logger.error("Строка %d: " + WRONG_TOTAL, totalRow.getIndex(), getColumnName(totalRow, alias))
			}
        }
    }
}

def calc9(def row) {
    if (row.sum == null) {
        return null
    }
    return round(row.sum * 0.18, 0)
}

void consolidation() {
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def dataRows = formTemplate.rows
    updateIndexes(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    def sectionRowsMap = [:]
    sections.each { section ->
        sectionRowsMap[section] = []
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    def rows = getSectionRows(sourceDataRows, section, true)
                    sectionRowsMap[section].addAll(rows)
                }
            }
        }
    }

    // заполнить форму по разделам
    for (def section : sections) {
        def sectionRows = sectionRowsMap[section]
        if (!sectionRows) {
            continue
        }
        def index = getDataRow(dataRows, 'total' + section).getIndex() - 1
        dataRows.addAll(index, sectionRows)
        updateIndexes(dataRows)
    }
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

def getFirstRowIndex(def dataRows, def section) {
    if (section == sections[0]) {
        return 0
    }
    def alias = 'total' + sections[sections.indexOf(section) - 1]
    return getDataRow(dataRows, alias)?.getIndex()
}

def getLastRowIndex(def dataRows, def section) {
    return getDataRow(dataRows, 'total' + section).getIndex()
}

def getSectionRows(def dataRows, def section, def onlySimpleRows = false) {
    def from = getFirstRowIndex(dataRows, section)
    def to = getLastRowIndex(dataRows, section) - (onlySimpleRows ? 2 : 1)
    def sectionsRows = (from <= to ? dataRows[from..to] : [])
    return sectionsRows
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def firstRow = (dataRows?.size() ? dataRows.get(0) : null)
    def columnList = firstRow?.keySet()?.collect { firstRow?.getCell(it)?.getColumn() }
    for (def section : sections) {
        def sectionRows = getSectionRows(dataRows, section, true)
        // Массовое разыменование строк НФ
        refBookService.dataRowsDereference(logger, sectionRows, columnList)
        sortRowsSimple(sectionRows)
    }
    dataRowHelper.saveSort()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 10
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = tmpRow.getCell('rowNum').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows
    updateIndexes(templateRows)

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def totalRowFromFileMap = [:]

    def mapRows = [:]
    def sectionAlias = getDataRow(templateRows, 'total1')?.getAlias()
    templateRows.collect { it.getAlias() }.each {
        mapRows[it] = []
    }

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
        def str = rowValues[INDEX_FOR_SKIP]
        if (str?.contains("ИТОГО за ")) {
            rowIndex++
            totalRowFromFileMap[sectionAlias] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            // если строка последняя, не обрабатывать дальше
            if (sectionAlias != templateRows[templateRows.size() - 1].getAlias()) {
                def totalRow = templateRows?.find { it.fix == str }
                // получить индекс следующей итоговой строки
                sectionAlias = templateRows[totalRow.getIndex()]?.getAlias()
            }

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        mapRows[sectionAlias].add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // копирование данных по разделам
    def dataRows = []
    mapRows.each { rowAlias, sectionRows ->
        if (sectionRows != null && !sectionRows.isEmpty()) {
            dataRows.addAll(sectionRows)
        }
        def totalRow = getDataRow(templateRows, rowAlias)
        dataRows.add(totalRow)
    }
    updateIndexes(dataRows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        // подсчитанные итоговые строки для сравнения
        def totalRowTmpMap = [:]
        mapRows.each { rowAlias, sectionRows ->
            def totalRowTmp = formData.createStoreMessagingDataRow()
            calcTotalSum(sectionRows, totalRowTmp, totalColumns)
            totalRowTmpMap[rowAlias] = totalRowTmp
        }

        // задание значении итоговым строкам нф из итоговых строк файла
        totalRowTmpMap.keySet().toArray().each { rowAlias ->
            def totalRow = getDataRow(templateRows, rowAlias)
            def totalRowFromFile = totalRowFromFileMap[rowAlias]
            totalColumns.each { alias ->
                totalRow[alias] = totalRowFromFile[alias]
                totalRow.setImportIndex(totalRowFromFile.getImportIndex())
            }
        }
        // сравнение
        totalRowTmpMap.each { rowAlias, totalRowTmp ->
            def totalRow = getDataRow(templateRows, rowAlias)
            compareTotalValues(totalRow, totalRowTmp, totalColumns, logger, false)
        }
    }

    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = dataRows
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
    checkHeaderSize(headerRows, colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('rowNum').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('name').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('iksr').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('date').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('nameOf').column.name]),
            ([(headerRows[0][6]): 'Данные налогового учета о доходе, доначисленном до рыночного уровня']),
            ([(headerRows[0][9]): tmpRow.getCell('ndsSum').column.name]),
            ([(headerRows[1][6]): 'РНУ']),
            ([(headerRows[1][7]): 'Номер записи в РНУ']),
            ([(headerRows[1][8]): 'Сумма']),
            ([(headerRows[2][0]): '1'])
    ]
    (2..9).each { index ->
        headerMapping.add(([(headerRows[2][index]): index.toString()]))
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
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    def colIndex

    // графа 2 - name       - атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Участники ТЦО»
    // графа 3 - iksr       - зависит от графы 2 - атрибут 5218 - IKSR - «IKSR», справочник 520 «Участники ТЦО»

    // графа 2
    colIndex = 2
    def String iksrName = getColumnName(newRow, 'iksr')
    def nameFromFile = values[2]
    def recordId = getTcoRecordId(nameFromFile, values[3], iksrName, fileRowIndex, colIndex, getReportPeriodEndDate(), false, logger, refBookFactory, recordCache)
    newRow.name = recordId

    // графа 4
    colIndex = 4
    newRow.date = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5..7
    colIndex = 5
    ['nameOf', 'rnu', 'number'].each { alias ->
        newRow[alias] = values[colIndex]
        colIndex++
    }

    // графа 8
    colIndex = 8
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex = 9
    newRow.ndsSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

/**
 * Получить новую итоговую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewTotalRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 8
    colIndex = 8
    newRow.sum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 9
    colIndex = 9
    newRow.ndsSum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    return newRow
}

def endMessage(String iksrName) {
    // сообщение 5
    logger.warn("Для заполнения на форме граф с общей информацией о юридическом лице выполнен поиск значения файла " +
            "по графе «$iksrName» в следующих атрибутах справочника «Участники ТЦО»: " +
            "«ИНН (заполняется для резидентов, некредитных организаций)», " +
            "«Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)», " +
            "«Код налогоплательщика в стране инкорпорации», " +
            "«Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)», " +
            "«КИО (заполняется для нерезидентов)»")
}

/** Проверить принятость всех декларации в этом году. */
void checkDeclarations() {
    // подразделение банка
    def bankDepartmentId = 1

    if (formData.kind != FormDataKind.CONSOLIDATED || formDataDepartment.id != bankDepartmentId ||
            getDepartmentReportPeriod(formData.departmentReportPeriodId).reportPeriod.order != 4) {
        return
    }

    def taxPeriodId = getDepartmentReportPeriod(formData.departmentReportPeriodId).reportPeriod.taxPeriod.id
    // список отчетных периодов
    def periods = reportPeriodService.listByTaxPeriod(taxPeriodId)
    // период за 4ый квартал
    def lastPeriod = periods.find { it.order == 4 }

    def periodIds = periods.collect { it.id }
    // список корректирующих периодов
    def correctionDateMap = departmentReportPeriodService.getCorrectionDateListByReportPeriod(periodIds)
    def correctionPeriodIds = correctionDateMap.keySet().collect { it }
    // если не входит последний 4 период, то добавить его
    def isIncludeLastPeriod = correctionPeriodIds.find { it == lastPeriod.id }
    if (!isIncludeLastPeriod && lastPeriod) {
        if (correctionPeriodIds) {
            correctionPeriodIds.add(lastPeriod?.id)
        } else {
            correctionPeriodIds = [getDepartmentReportPeriod(formData.departmentReportPeriodId).reportPeriod.id]
        }
    }

    // мапа декларации (id -> название)
    def declarationTypeMap = [
            4  : 'Декларация по НДС (раздел 1-7)',
            7  : 'Декларация по НДС (аудит, раздел 1-7)',
            20 : 'Декларация по НДС (короткая, раздел 1-7)'
    ]

    // получение декларации и проверка принятости
    def results = []
    declarationTypeMap.keySet().toArray().each { declarationTypeId ->
        correctionPeriodIds.each { def periodId ->
            def declarationData = declarationService.getLast(declarationTypeId, bankDepartmentId, periodId)
            if (declarationData?.accepted) {
                DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriod(declarationData.departmentReportPeriodId)
                def date = (correctionDateMap[periodId] ? correctionDateMap[periodId][-1] : null)
                def result = [
                        declarationName : declarationTypeMap[declarationTypeId],
                        departmentName : getDepartment(bankDepartmentId)?.name,
                        periodName : departmentReportPeriod.reportPeriod.name,
                        year : departmentReportPeriod.reportPeriod.taxPeriod.year,
                        correctionDate : date?.format('dd.MM.yyyy')
                ]
                results.add(result)
            }
        }
    }

    // вывод результатов проверки
    if (results) {
        def msg = "Существуют принятые экземпляры деклараций-приемников:"
        logger.warn(msg)

        results.each { result ->
            def declarationName = result.declarationName
            def departmentName = result.departmentName
            def periodName = result.periodName
            def year = result.year
            def correctionDate = result.correctionDate
            if (correctionDate) {
                msg = "Вид: «%s», Подразделение: «%s», Период: «%d, %s», Дата сдачи корректировки: «%s»."
                logger.warn(msg, declarationName, departmentName, year, periodName, correctionDate)
            } else {
                msg = "Вид: «%s», Подразделение: «%s», Период: «%d, %s»."
                logger.warn(msg, declarationName, departmentName, year, periodName)
            }
        }
    }
}
@Field
def departmentReportPeriodMap = [:]

DepartmentReportPeriod getDepartmentReportPeriod(def id) {
    if (departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

@Field
def departmentMap = [:]

Department getDepartment(def id) {
    if (departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}