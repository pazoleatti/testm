package form_template.vat.vat_724_1_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * (724.1.1) Корректировка сумм НДС и налоговых вычетов за прошедшие налоговые периоды
 *
 * formTemplateId=848
 */

// графа 1  - rowNum
// графа    - fix
// графа 2  - period
// графа 3  - number
// графа 4  - sumPlus
// графа 5  - sumMinus
// графа 6  - numberNds
// графа 7  - sumNdsPlus
// графа 8  - sumNdsMinus
// графа 9  - rateNds
// графа 10 - sum

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

// все атрибуты
@Field
def allColumns = ['rowNum', 'fix', 'period', 'number', 'sumPlus', 'sumMinus', 'numberNds', 'sumNdsPlus', 'sumNdsMinus', 'rateNds', 'sum']

// Редактируемые атрибуты (графа 2,3,6-8) не для разделов 1-6
@Field
def editableColumns = ['period', 'number', 'sumPlus', 'sumNdsPlus', 'sumNdsMinus']

// Редактируемые атрибуты (графа 2-8) для разделов 1-6
@Field
def editableColumns_1_6 = ['period', 'number', 'sumPlus', 'sumMinus', 'numberNds', 'sumNdsPlus', 'sumNdsMinus']

// Проверяемые на пустые значения атрибуты (графа 2, 3, 9, 10)
@Field
def nonEmptyColumns = ['period', 'number', 'rateNds', 'sum']

@Field
def emptyColumns_7_9 = ['sumPlus', 'sumMinus']

// Атрибуты итоговых строк для которых вычисляются суммы
// для групп 1 - 6, "ВСЕГО по дополнительному листу книги продаж..." (графа 4, 5, 7, 8, 10)
@Field
def totalColumns1_6_Sale = ['sumPlus', 'sumMinus', 'sumNdsPlus', 'sumNdsMinus', 'sum']

// для группы "ВСЕГО по разделам 1-6" (графа 7, 8)
@Field
def totalColumnsMega = ['sumNdsPlus', 'sumNdsMinus']

// для группы 7, 8, 9, "ВСЕГО по дополнительному листу книги покупок..." (графа 7, 8, 10)
@Field
def totalColumns7_9_Purchase = ['sumNdsPlus', 'sumNdsMinus', 'sum']

@Field
def skipColumns = ['sumMinus', 'sumNdsMinus']

// Группируемые атрибуты (графа 2)
@Field
def groupColumns = ['period']

// Сортируемые атрибуты (графа 2, 6, 3)
@Field
def sortColumns = ['period', 'numberNds', 'number']

// список алиасов подразделов
@Field
def sections = ['1', '2', '3', '4', '5', '6', '7', '8', '9']

@Field
def rateMap = [
        '1' : ['18', '1'],
        '2' : ['10', '2'],
        '3' : ['18/118', '3, 5-9'],
        '4' : ['10/110', '4'],
        '5' : ['18/118', '3, 5-9'],
        '6' : ['18/118', '3, 5-9'],
        '7' : ['18/118', '3, 5-9'],
        '8' : ['18/118', '3, 5-9'],
        '9' : ['18/118', '3, 5-9']
]

@Field
def numberMap = [ '1' : [['6030901'], '1-4'],
                  '2' : [['6030901'], '1-4'],
                  '3' : [['6030901'], '1-4'],
                  '4' : [['6030901'], '1-4'],
                  '5' : [['6030904'], '5'],
                  '6' : [['6030905'], '6'],
                  '7' : [['6030903'], '7, 9'],
                  '8' : [['6030901', '6030904', '6030905'], '8'],
                  '9' : [['6030903'], '7, 9']
]

@Field
def sectionsLabels = ['sale_18' : "ВСЕГО по дополнительному листу книги продаж за %s по ставке 18%%",
                      'sale_10' : "ВСЕГО по дополнительному листу книги продаж за %s по ставке 10%%",
                      'purchase' : "ВСЕГО по дополнительному листу книги покупок за %s по ставке 18%%"]

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

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы
def addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def index
    boolean isGroup_1_6 = false
    if (currentDataRow != null) {
        def alias = currentDataRow.getAlias()
        index = currentDataRow.getIndex()
        if (alias == null || alias.startsWith('head')) {
            index++
        }
        if (alias != null && alias == "mega_total") {
            index += 2
        }
        isGroup_1_6 = getDataRow(dataRows, 'mega_total').getIndex() > currentDataRow.getIndex()
    } else {
        index = dataRows.size() + 1
    }
    dataRowHelper.insert(getNewRow(isGroup_1_6), index)
}

/** Получить новую строку с заданными стилями. */
def getNewRow(boolean isGroup_1_6) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    def columns = isGroup_1_6 ? editableColumns_1_6 : editableColumns
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    (allColumns - columns).each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удаляем рассчитываемые итоги
    dataRows.removeAll { row ->
        row.getAlias() != null && row.getAlias() != ('mega_total') && !row.getAlias().startsWith('head_')
    }

    updateIndexes(dataRows)

    // распределяем строки по группам
    def sectionMap = arrangeRows(dataRows)

    // расчеты
    sectionMap.keySet().each { section ->
        for (row in sectionMap[section]) {
            if (row.getAlias() != null) {
                continue
            }
            row.rateNds = rateMap[section][0]
        }
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        row.sum = calcCheck10(row, null)
    }

    // добавим строки макета
    dataRows.clear()
    dataRows.addAll(formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).rows)
    updateIndexes(dataRows)

    // Сортировка и добавление нефиксированных строк
    sectionMap.each { key, rows ->
        rows.sort { row ->
            getRefBookValue(8, row.period)?.CODE?.stringValue ?: '00'
        }
        dataRows.addAll(getDataRow(dataRows, 'head_' + key).getIndex(), rows)
        updateIndexes(dataRows)
    }

    // создает рассчитываемые итоги
    calcTotals(dataRows)
    updateIndexes(dataRows)
}

def calcCheck10(def row, def sum) {
    def index = row.getIndex()
    // ЕСЛИ «Графа 7» заполнена И «Графа 8» не заполнена, ТО «Графа 10» = «Графа 7»
    if (row.sumNdsPlus != null && row.sumNdsMinus == null) {
        if (sum != null && sum != row.sumNdsPlus) {
            logger.error("Строка $index: Графа «${getColumnName(row,'sum')}» должна быть заполнена значением графы с суммой корректировки (-) НДС!")
        }
        return row.sumNdsPlus
    }
    // ЕСЛИ «Графа 7» не заполнена И «Графа 8» заполнена, ТО «Графа 10» = «Графа 8»
    if (row.sumNdsPlus == null && row.sumNdsMinus != null) {
        if (sum != null && sum != row.sumNdsMinus) {
            logger.error("Строка $index: Графа «${getColumnName(row,'sum')}» должна быть заполнена значением графы с суммой корректировки (+) НДС!")
        }
        return row.sumNdsMinus
    }
    // ЕСЛИ «Графа 7» заполнена И «Графа 8» заполнена, ТО «Графа 10» = «Графа 7» + «Графа 8»
    if (row.sumNdsPlus != null && row.sumNdsMinus != null) {
        if (sum != null && sum != row.sumNdsPlus + row.sumNdsMinus) {
            logger.error("Строка $index: Графа «${getColumnName(row,'sum')}» должна быть заполнена суммой значений графы с суммой корректировки (+) и графы с суммой корректировки (-) НДС!")
        }
        return row.sumNdsPlus + row.sumNdsMinus
    }
    return null
}

def calcTotals(def dataRows) {
    // Добавление подитогов (Итого за налоговый период)
    calcPeriodTotals(dataRows)

    // Итого по разделам 1-6
    def megaTotal = getDataRow(dataRows, "mega_total")
    calcMegaTotal(megaTotal, dataRows)

    // Считаем три группы супер-итогов
    def superRows = calcSuperTotals(dataRows)
    if (!superRows.isEmpty()) {
        dataRows.addAll(dataRows.indexOf(getDataRow(dataRows, 'head_8')), superRows)
    }
}

void calcMegaTotal(def megaTotal, def dataRows) {
    megaTotal.sumNdsPlus = BigDecimal.ZERO
    dataRows.each { row ->
        if (row.getAlias() != null && row.getAlias() ==~ /total_[1-6]_.*/) {
            // суммируем по 4-й графе (5-я пустая)
            megaTotal.sumNdsPlus += (row.sumNdsPlus ?: BigDecimal.ZERO)
        }
    }
}

def calcSuperTotals(def dataRows) {
    def sectionsSuper = ['sale_18' : ['1', '3', '5', '6', '7'], 'sale_10' : ['2', '4'], 'purchase' : ['8', '9']]
    def totalColumnsSuper = ['sale_18' : totalColumns1_6_Sale, 'sale_10' : totalColumns1_6_Sale, 'purchase' : totalColumns7_9_Purchase]

    def superRows = []
    sectionsSuper.keySet().each { key ->
        def subSections = sectionsSuper[key]
        def columns = totalColumnsSuper[key] - skipColumns
        // фиксированные строки (Итого за )
        def rows = dataRows?.findAll { row ->
            row.getAlias() != null && row.getAlias() ==~ /total_[${subSections.join('')}]_.*/
        }
        def sumMap = [:]
        rows.each { row ->
            def periodName = row.fix?.replaceAll('Итого за ', '')
            def periodId = getRefBookRecord(8, 'NAME', periodName, getReportPeriodEndDate(), -1, null, false)?.record_id?.value
            if (sumMap[periodId] == null) {
                sumMap[periodId] = []
            }
            sumMap[periodId].add(row)
        }
        sumMap.keySet().each { periodId ->
            def superRow = getSuperTotalRow(key, periodId as Long)
            columns.each { alias ->
                superRow[alias] = BigDecimal.ZERO
                sumMap[periodId].each { row ->
                    superRow[alias] += (row[alias] ?: BigDecimal.ZERO)
                }
            }
            superRows.add(superRow)
        }
    }
    return superRows
}

/** считает итоги, специфично, т.к. есть объединения по графам 4,5 и 7,8 */
void calcSpecificTotals(def dataRows, def totalRow, def columns) {
    if (columns == null)
        return
    def tempRow = formData.createDataRow()
    calcTotalSum(dataRows, tempRow, columns)
    def specialColumns = ['sumPlus', 'sumMinus', 'sumNdsPlus', 'sumNdsMinus']
    columns.each { alias ->
        if (!specialColumns.contains(alias)) {
            totalRow[alias] = tempRow[alias]
        }
    }
    if (columns.contains('sumPlus')) {
        totalRow.sumPlus = tempRow.sumPlus + tempRow.sumMinus
    }
    if (columns.contains('sumNdsPlus')) {
        totalRow.sumNdsPlus = tempRow.sumNdsPlus + tempRow.sumNdsMinus
    }
}

void calcPeriodTotals(List<DataRow<Cell>> dataRows) {
    def section = null
    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = dataRows.get(i);
        DataRow<Cell> nextRow = null;
        if (i < dataRows.size() - 1) {
            nextRow = dataRows.get(i + 1);
        }
        if (row.getAlias() != null) {
            if (row.getAlias().startsWith("head_")) {
                section = row.getAlias().replaceAll("head_", '')
            }
            continue;
        }
        if (nextRow == null || isDiffRow(row, nextRow, groupColumns)) {
            DataRow<Cell> aliasedRow = calcItog(i, dataRows, section, row.period);
            dataRows.add(++i, aliasedRow);
        }
    }
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows, def section, def periodId) {
    def newRow = getPeriodTotalRow(section, periodId)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    def columns
    if (section != null && Integer.valueOf(section) < 7) {
        columns = totalColumns1_6_Sale
    } else if (section != null && Integer.valueOf(section) > 6) {
        columns = totalColumns7_9_Purchase
    }
    calcSpecificTotals(rows, newRow, columns)
    return newRow
}

/** Получить новую итоговую строку с заданными стилями. */
def getSuperTotalRow(def key, def periodId) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    def refBookValue = getRefBookValue(8, periodId)
    newRow.fix = String.format(sectionsLabels[key], refBookValue?.NAME?.stringValue ?: "графа 2 не задана")
    newRow.getCell('fix').colSpan = 2
    newRow.getCell('sumPlus').colSpan = 2
    newRow.getCell('sumNdsPlus').colSpan = 2
    newRow.setAlias('super_' + key + '_' + refBookValue?.CODE?.stringValue)
    allColumns.each {
        newRow.getCell(it).styleAlias = 'Контрольные суммы'
    }
    return newRow
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def isAfterSection6 = false
    def section = null
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (row.getAlias().startsWith('head_')){
                section = row.getAlias().replaceAll('head_', '')
                if (section == '7') {
                    isAfterSection6 = true
                }
            }
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка незаполненности граф с суммой корректировки
        if (isAfterSection6 && emptyColumns_7_9.find { row[it] != null } != null) {
            logger.error("Строка $index: Графы с суммой корректировки (+, -) налоговой базы (раздел 7-9) не должны быть заполнены!")
        }

        // 3. Проверка на заполнение хотя бы одной из граф «+», «-» с суммой корректировки
        // 4. Проверка положительности суммы корректировки
        // 5. Проверка отрицательности суммы корректировки
        if (!isAfterSection6) {
            if (row.sumPlus == null && row.sumMinus == null) {
                logger.error("Строка $index: Должна быть заполнена хотя бы одна из граф с суммой корректировки (+, -) налоговой базы (раздел 1-6)!")
            }
            if (row.sumPlus != null && !(row.sumPlus > 0)) {
                logger.error("Строка $index: Графа с суммой корректировки (+) налоговой базы (раздел 1-6) должна быть заполнена значением больше «0»!")
            }
            if (row.sumMinus != null && !(row.sumMinus < 0)) {
                logger.error("Строка $index: Графа с суммой корректировки (-) налоговой базы (раздел 1-6) должна быть заполнена значением меньше «0»!")
            }
        }

        if (row.sumNdsPlus == null && row.sumNdsMinus == null) {
            logger.error("Строка $index: Должна быть заполнена хотя бы одна из граф с суммой корректировки (+, -) НДС (раздел 1-9)!")
        }
        if (row.sumNdsPlus != null && !(row.sumNdsPlus > 0)) {
            logger.error("Строка $index: Графа с суммой корректировки (+) НДС (раздел 1-9) должна быть заполнена значением больше «0»!")
        }
        if (row.sumNdsMinus != null && !(row.sumNdsMinus < 0)) {
            logger.error("Строка $index: Графа с суммой корректировки (-) НДС (раздел 1-9) должна быть заполнена значением меньше «0»!")
        }

        // 6. Проверка номера балансового счета
        if (row.numberNds != null) {
            def numberNds = getRefBookValue(101, row.numberNds).ACCOUNT.value
            def validNumbers = numberMap[section][0]
            if (section != null && !validNumbers.contains(numberNds)) {
                logger.error("Строка $index: Графа «${getColumnName(row,'numberNds')}» заполнена неверно! Возможные значения (раздел ${numberMap[section][1]}): пустое значение, «${validNumbers.join('», «')}».")
            }
        }

        // 7. Проверка ставки НДС
        def validRate = rateMap[section][0]
        if (row.rateNds && validRate != row.rateNds) {
            logger.error("Строка $index: Графа «${getColumnName(row,'rateNds')}» заполнена неверно! Возможные значения (раздел ${rateMap[section][1]}): «$validRate».")
        }

        // 8. Проверка суммы НДС по дополнительным листам книги покупок и продаж
        if (row.sum != null) {
            calcCheck10(row, row.sum)
        }
    }
    compareSpecificTotalValues(dataRows, arrangeRows(dataRows))
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).rows
    updateIndexes(dataRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // копирование данных по разделам
                sections.each { section ->
                    copyRows(sourceDataRows, dataRows, 'head_' + section, 'head_' + (Integer.valueOf(section) + 1))
                }
            }
        }
    }
    dataRowHelper.setAllCached(dataRows)
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
    if (toAlias == 'head_7') {
        toAlias = 'mega_total'
    }
    def from = getDataRow(sourceDataRows, fromAlias).getIndex()
    def to = (toAlias != 'head_10') ? (getDataRow(sourceDataRows, toAlias).getIndex() - 1) : sourceDataRows.size()
    if (from >= to) {
        return
    }
    def copyRows = sourceDataRows.subList(from, to)
    if (toAlias != 'head_10') {
        destinationDataRows.addAll(getDataRow(destinationDataRows, toAlias).getIndex() - 1, copyRows)
    } else {
        destinationDataRows.addAll(copyRows)
    }
    // поправить индексы, потому что они после вставки не пересчитываются
    updateIndexes(destinationDataRows)
}

DataRow<Cell> getPeriodTotalRow(def section, def periodId) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    def refBookValue = getRefBookValue(8, periodId)
    newRow.fix = 'Итого за ' + (refBookValue?.NAME?.stringValue ?: "графа 2 не задана")
    newRow.setAlias('total_' + section + '_' + refBookValue?.CODE?.stringValue)
    newRow.getCell('fix').colSpan = 2
    newRow.getCell('sumPlus').colSpan = 2
    newRow.getCell('sumNdsPlus').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

def arrangeRows(dataRows) {
    def sectionMap = [:]
    sections.each { section ->
        def fromAlias = 'head_' + section
        def toAlias = 'head_' + (Integer.valueOf(section) + 1)
        if (toAlias == 'head_7') {
            toAlias = 'mega_total'
        }
        def from = getDataRow(dataRows, fromAlias).getIndex()
        def to = (toAlias != 'head_10') ? (getDataRow(dataRows, toAlias).getIndex() - 1) : dataRows.size()
        if (from < to) {
            if (sectionMap[section] == null) {
                sectionMap[section] = []
            }
            sectionMap[section].addAll(dataRows.subList(from, to))
        }
    }
    return sectionMap
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (внутри групп)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    def newRows = []
    def tempRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (!tempRows.isEmpty()) {
                sortRows(tempRows, columns)
                newRows.addAll(tempRows)
                tempRows = []
            }
            newRows.add(row)
            continue
        }
        tempRows.add(row)
    }
    if (!tempRows.isEmpty()) {
        sortRows(tempRows, columns)
        newRows.addAll(tempRows)
    }
    dataRowHelper.setAllCached(newRows)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

@Field
def megaTotalsPatterns = ['sale_18' : /ВСЕГО по дополнительному листу книги продаж за (.*) по ставке 18%/,
                          'sale_10' : /ВСЕГО по дополнительному листу книги продаж за (.*) по ставке 10%/,
                          'purchase' : /ВСЕГО по дополнительному листу книги покупок за (.*) по ставке 18%/]

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 10
    int HEADER_ROW_COUNT = 4
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNum')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId)
    def templateRows = formTemplate.rows
    def valuesTotal = [ getDataRow(templateRows, 'head_1')?.fix,
                        getDataRow(templateRows, 'head_2')?.fix,
                        getDataRow(templateRows, 'head_3')?.fix,
                        getDataRow(templateRows, 'head_4')?.fix,
                        getDataRow(templateRows, 'head_5')?.fix,
                        getDataRow(templateRows, 'head_6')?.fix,
                        getDataRow(templateRows, 'head_7')?.fix,
                        getDataRow(templateRows, 'head_8')?.fix,
                        getDataRow(templateRows, 'head_9')?.fix
    ]

    def rowIndex = 0
    def allValuesCount = allValues.size()
    def sectionMap = [:]
    def rows = []
    def sectionIndex = null

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
        // если это начало раздела, то запомнить его название и обрабатывать следующую строку
        String firstValue = rowValues[INDEX_FOR_SKIP]
        if (valuesTotal.contains(firstValue)) {
            def prevSectionIndex = sectionIndex
            sectionIndex = firstValue[0]
            if (prevSectionIndex != null && (Integer.valueOf(prevSectionIndex) + 1 != Integer.valueOf(sectionIndex))) {
                throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            sectionMap.put(sectionIndex, [])
            rows.add(getDataRow(templateRows, 'head_' + sectionIndex))
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue.contains('Итого за ')) {
            rowIndex++
            def periodName = firstValue.replaceAll('Итого за ', '')
            def periodId = getRecordIdImport(8, 'NAME', periodName, fileRowIndex, 2 + colOffset)
            def totalPeriod = getPeriodTotalRow(sectionIndex, periodId)
            fillTotalRowFromXls(totalPeriod, rowValues, colOffset, fileRowIndex, rowIndex)
            rows.add(totalPeriod)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue == 'ВСЕГО по разделам 1-6') {
            if (sectionIndex != '6') {
                throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            def totalSections_1_6 = getDataRow(templateRows, 'mega_total')
            fillTotalRowFromXls(totalSections_1_6, rowValues, colOffset, fileRowIndex, rowIndex)
            sectionMap[sectionIndex].add(totalSections_1_6)
            rows.add(totalSections_1_6)
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (firstValue.startsWith("ВСЕГО по дополнительному листу")) {
            if (sectionIndex != '7') {
                throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
            }
            def entry = megaTotalsPatterns.find { key, pattern -> firstValue ==~ pattern}
            if (entry != null) {
                def periodName = firstValue.replaceAll(entry.value, "\$1")
                def periodId = getRecordIdImport(8, 'NAME', periodName, fileRowIndex, colIndex + colOffset)
                def totalPeriod = getSuperTotalRow(entry.key, periodId)
                fillTotalRowFromXls(totalPeriod, rowValues, colOffset, fileRowIndex, rowIndex)
                rows.add(totalPeriod)
            }
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }

        if (sectionIndex == null) {
            throw new ServiceException("Строка %d: Структура файла не соответствует макету налоговой формы", fileRowIndex)
        }

        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, Integer.valueOf(sectionIndex) < 7)
        sectionMap[sectionIndex].add(newRow)
        rows.add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    def newRows = (sectionMap.values().sum { it } ?: [])
    showMessages(rows, logger)
    if (logger.containsLevel(LogLevel.ERROR) || newRows == null || newRows.isEmpty()) {
        return
    }

    updateIndexes(rows)

    // сравнение итогов
    compareSpecificTotalValues(rows, sectionMap)

    if (!logger.containsLevel(LogLevel.ERROR)) {
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
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNum')]),
            ([(headerRows[0][2]): 'Данные бухгалтерского учёта']),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'rateNds')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'sum')]),

            ([(headerRows[1][2]): 'Величина корректировки налоговой базы']),
            ([(headerRows[1][6]): 'Величина корректировки НДС']),

            ([(headerRows[2][2]): 'Налоговый период, за который вносится корректировка']),
            ([(headerRows[2][3]): 'номер балансового счёта']),
            ([(headerRows[2][4]): 'Сумма корректировки (+)']),
            ([(headerRows[2][5]): 'Сумма корректировки (-)']),
            ([(headerRows[2][6]): 'номер балансового счёта']),
            ([(headerRows[2][7]): 'Сумма корректировки (+)']),
            ([(headerRows[2][8]): 'Сумма корректировки (-)']),

            ([(headerRows[3][0]): '1']),
            ([(headerRows[3][2]): '2']),
            ([(headerRows[3][3]): '3']),
            ([(headerRows[3][4]): '4']),
            ([(headerRows[3][6]): '5']),
            ([(headerRows[3][7]): '6']),
            ([(headerRows[3][9]): '7']),
            ([(headerRows[3][10]): '8'])
    ]
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
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, boolean isGroup_1_6) {
    def newRow = getNewRow(isGroup_1_6)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // Графа 2
    def colIndex = 2
    newRow.period = getRecordIdImport(8, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)

    // Графа 3
    colIndex = 3
    newRow.number = getRecordIdImport(101, 'ACCOUNT', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 4
    colIndex = 4
    newRow.sumPlus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5
    colIndex = 5
    newRow.sumMinus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // Графа 6
    colIndex = 6
    newRow.numberNds = getRecordIdImport(101, 'ACCOUNT', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex = 7
    newRow.sumNdsPlus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex = 8
    newRow.sumNdsMinus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9
    colIndex = 9
    newRow.rateNds = values[colIndex]

    // графа 10
    colIndex = 10
    newRow.sum = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    return newRow
}

/**
 * Заполнить новую строку нф по значениям из экселя.
 *
 * @param totalRow строка
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
void fillTotalRowFromXls(def totalRow, def values, def colOffset, def fileRowIndex, def rowIndex) {
    totalRow.setIndex(rowIndex)
    totalRow.setImportIndex(fileRowIndex)

    // графа 4
    colIndex = 4
    totalRow.sumPlus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 5
    colIndex = 5
    totalRow.sumMinus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 7
    colIndex = 7
    totalRow.sumNdsPlus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex = 8
    totalRow.sumNdsMinus = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 10
    colIndex = 10
    totalRow.sum = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
}

void compareSpecificTotalValues(def dataRows, def sectionMap) {
    def isImport = formDataEvent == FormDataEvent.IMPORT
    def logLevel = isImport ? LogLevel.WARNING : LogLevel.ERROR
    // считаем итоги
    def calcRows = []
    calcRows.addAll(formDataService.getFormTemplate(formData.formType.id, formData.reportPeriodId).rows)
    updateIndexes(calcRows)
    sectionMap.each { key, tempRows ->
        calcRows.addAll(getDataRow(calcRows, 'head_' + key).getIndex(), tempRows.findAll { it.getAlias() == null || it.getAlias().startsWith('total_') })
        updateIndexes(calcRows)
    }
    calcPeriodTotals(calcRows)

    updateIndexes(dataRows)

    sections.each { section ->
        def periodTotals = dataRows.findAll { row -> row.getAlias() != null && row.getAlias().startsWith("total_" + section)}
        def calcPeriodTotals = calcRows.findAll { row -> row.getAlias() != null && row.getAlias().startsWith("total_" + section)}
        def fromAlias = 'head_' + section
        def toAlias = 'head_' + (Integer.valueOf(section) + 1)
        def from = getDataRow(dataRows, fromAlias).getIndex()
        def to = (toAlias != 'head_10') ? (getDataRow(dataRows, toAlias).getIndex() - 1) : dataRows.size()
        def sectionRows = dataRows.subList(from, to).findAll{ row -> row.getAlias() == null || row.getAlias().startsWith("total_") }
        checkItogRows(sectionRows, calcPeriodTotals, periodTotals, new GroupString() {
            @Override
            String getString(DataRow<Cell> row) {
                return getRefBookValue(8, row.period)?.NAME?.stringValue
            }
        }, new CheckGroupSum() {
            @Override
            String check(DataRow<Cell> row1, DataRow<Cell> row2) {
                def columns
                if (Integer.valueOf(section) < 7) {
                    columns = totalColumns1_6_Sale - skipColumns
                } else if (Integer.valueOf(section) > 6) {
                    columns = totalColumns7_9_Purchase - skipColumns
                }
                for (def column : columns) {
                    if (row1[column] != row2[column]) {
                        return getColumnName(row1, column)
                    }
                }
                return null
            }
        })
    }
    //
    def megaTotal = getDataRow(dataRows, "mega_total")
    def compareMegaTotal = getDataRow(calcRows, "mega_total")
    // рассчитываем итог по 1-6 на основе исходных данных
    calcMegaTotal(compareMegaTotal, dataRows)
    def columns = totalColumnsMega - skipColumns
    if (isImport) {
        compareTotalValues(megaTotal, compareMegaTotal, columns, logger, false)
    } else {
        columns.each { alias ->
            def value = megaTotal[alias] ?: BigDecimal.ZERO
            if (value != compareMegaTotal[alias]) {
                logger.log(logLevel, WRONG_TOTAL, false, getColumnName(megaTotal, alias))
            }
        }
    }

    def compareSuperRows = calcSuperTotals(dataRows)
    def superRows = dataRows.findAll { row -> row.getAlias() != null && row.getAlias().startsWith("super_") }
    superRows.each { superRow ->
        // рассчитанная строка с таким же алиасом
        def calcRow = compareSuperRows.find { row ->
            row.getAlias() == superRow.getAlias()
        }
        if (calcRow == null) {
            rowLog(logger, superRow, String.format("Строка %d: Строка итога не относится к какой-либо группе!", superRow.getIndex()), logLevel)
        } else {
            compareTotalValues(superRow, calcRow, columns, logger, !isImport)
            compareSuperRows.remove(calcRow)
        }
    }
    if (!compareSuperRows.isEmpty()) {
        compareSuperRows.each { calcRow ->
            def firstValue = calcRow.fix
            def entry = megaTotalsPatterns.find { key, pattern -> firstValue ==~ pattern}
            if (entry != null) {
                def periodName = firstValue.replaceAll(entry.value, "\$1")
                logger.log(logLevel, "Группа строк «%s» не имеет строки «%s»!", false, periodName, firstValue)
            }
        }
    }
}

// вынес метод в скрипт для правки проверок
void checkItogRows(def dataRows, def testItogRows, def itogRows, GroupString groupString, CheckGroupSum checkGroupSum) {
    def logLevel = formDataEvent == FormDataEvent.IMPORT ? LogLevel.WARNING : LogLevel.ERROR
    // считает количество реальных групп данных
    def groupCount = 0
    // Итоговые строки были удалены
    // Неитоговые строки были удалены
    for (int i = 0; i < dataRows.size(); i++) {
        DataRow<Cell> row = dataRows.get(i);
        // строка или итог другой группы после строки без подитога между ними
        if (i > 0) {
            def prevRow = dataRows.get(i - 1)
            if (prevRow.getAlias() == null && compareGroup(prevRow, row)) {
                itogRows.add(groupCount, null)
                groupCount++
                String groupCols = groupString.getString(prevRow);
                if (groupCols != null) {
                    logger.log(logLevel, "Группа «%s» не имеет строки итога!", false, groupCols); // итога (не  подитога)
                }
            }
        }
        if (row.getAlias() != null) {
            // итог после итога (или после строки из другой группы)
            if (i < 1 || dataRows.get(i - 1).getAlias() != null || compareGroup(dataRows.get(i - 1), row)) {
                rowLog(logger, row, String.format("Строка %d: Строка итога не относится к какой-либо группе!", row.getIndex()), logLevel); // итога (не  подитога)
                // удаляем из проверяемых итогов строку без подчиненных строк
                itogRows.remove(row)
            } else {
                groupCount++
            }
        } else {
            // нефиксированная строка и отсутствует последний итог
            if (i == dataRows.size() - 1) {
                itogRows.add(groupCount, null)
                groupCount++
                String groupCols = groupString.getString(row);
                if (groupCols != null) {
                    rowLog(logger, row, String.format("Группа «%s» не имеет строки итога!", groupCols), logLevel); // итога (не  подитога)
                }
            }
        }
    }
    if (testItogRows.size() == itogRows.size()) {
        for (int i = 0; i < testItogRows.size(); i++) {
            DataRow<Cell> testItogRow = testItogRows.get(i);
            DataRow<Cell> realItogRow = itogRows.get(i);
            if (realItogRow == null) {
                continue
            }
            int rowIndex = dataRows.indexOf(realItogRow) - 1
            def row = dataRows.get(rowIndex)
            String groupCols = groupString.getString(row);
            if (groupCols != null) {
                String checkStr = checkGroupSum.check(testItogRow, realItogRow);
                if (checkStr != null) {
                    rowLog(logger, realItogRow, String.format(GROUP_WRONG_ITOG_SUM, realItogRow.getIndex(), groupCols, checkStr), logLevel);
                }
            }
        }
    }
}

boolean compareGroup(def rowA, def rowB) {
    def periodA = (rowA.getAlias() != null) ? rowA.fix?.replace('Итого за ','') : getValuesByGroupColumn(rowA)
    def periodB = (rowB.getAlias() != null) ? rowB.fix?.replace('Итого за ','') : getValuesByGroupColumn(rowB)
    return periodA != periodB
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    def value
    // графа 12
    if (row?.period) {
        value = getRefBookValue(8, row.period)?.NAME?.stringValue
    }
    return value
}
