package form_template.land.calc_for_declaration.v2016

import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.DaoException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import groovy.transform.Field

/**
 * Расчет земельного налога по земельным участкам, подлежащим включению в декларацию.
 *
 * formTemplateId = 918
 * formTypeId = 918
 *
 * TODO:
 *      - дополнить тесты: добавлена загрузка эксель, переопределение источников, изменена консолидация
 */

// графа    - fix
// графа 1  - rowNumber
// графа 2  - department           - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 3  - kno
// графа 4  - kpp
// графа 5  - kbk                  - атрибут 7031 - CODE - «Код», справочник 703 «Коды бюджетной классификации земельного налога»
// графа 6  - oktmo                - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
// графа 7  - cadastralNumber
// графа 8  - landCategory         - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
// графа 9  - constructionPhase    - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
// графа 10 - cadastralCost
// графа 11 - taxPart
// графа 12 - ownershipDate
// графа 13 - terminationDate
// графа 14 - period
// графа 15 - benefitCode          - атрибут 7053.7041 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 16 - benefitBase          - зависит от графы 15 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 17 - benefitParam         - зависит от графы 15 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 18 - startDate
// графа 19 - endDate
// графа 20 - benefitPeriod
// графа 21 - taxRate
// графа 22 - kv
// графа 23 - kl
// графа 24 - sum
// графа 25 - q1
// графа 26 - q2
// графа 27 - q3
// графа 28 - year
// графа 29 - name

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        if (formData.manual) {
            formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        }
        break
    case FormDataEvent.DELETE_ROW:
        if (formData.manual && currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.GET_SOURCES:
        getSources()
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
def allColumns = ['fix', 'rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
                  'landCategory', 'constructionPhase', 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
                  'period', 'benefitCode', 'benefitBase', 'benefitParam', 'startDate', 'endDate', 'benefitPeriod',
                  'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year', 'name']

// Редактируемые атрибуты
@Field
def editableColumns = []

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// Проверяемые на пустые значения атрибуты (графа 2..8, 10, 12, 14, 21, 22, 25..28)
@Field
def nonEmptyColumns = ['department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber','landCategory', 'cadastralCost',
                       'ownershipDate', 'period', 'taxRate', 'kv', 'q1', 'q2', 'q3', 'year']

// графа 3, 4, 6
@Field
def groupColumns = ['kno', 'kpp', 'oktmo']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 25..28)
@Field
def totalColumns = ['q1', 'q2', 'q3', 'year']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

//// Обертки методов

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    // 1. Проверка заполнения поля «Регион» справочника «Подразделения» для подразделения формы
    if (formDataDepartment.regionId == null) {
        logger.error("В справочнике «Подразделения» не заполнено поле «Регион» для подразделения «%s»", formDataDepartment.name)
    }

    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()

        // 2. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)
    }

    // 3. Проверка корректности значений итоговых строк
    def lastSimpleRow = null
    def subTotalMap = [:]
    for (def row : dataRows) {
        if (!row.getAlias()) {
            lastSimpleRow = row

            // подитог кно/кпп/октмо - проверка отсутствия подитога
            def key2 = row.kno + '#' + row.kpp + '#' + row.oktmo
            if (subTotalMap[key2] == null) {
                def findSubTotal = dataRows.find { it.getAlias()?.startsWith('total2') && it.kno == row.kno && it.kpp == row.kpp && it.oktmo == row.oktmo }
                subTotalMap[key2] = (findSubTotal != null)
                if (findSubTotal == null) {
                    def subMsg = getColumnName(row, 'kno') + '=' + (row.kno ?: 'не задан') + ', ' +
                            getColumnName(row, 'kpp') + '=' + (row.kpp ?: 'не задан') + ', ' +
                            getColumnName(row, 'oktmo') + '=' + (getRefBookValue(96L, row.oktmo)?.CODE?.value ?: 'не задан')
                    logger.error(GROUP_WRONG_ITOG, subMsg)
                }
            }

            // подитог кно/кпп - проверка отсутствия подитога
            def key1 = row.kno + '#' + row.kpp
            if (subTotalMap[key1] == null) {
                def findSubTotal = dataRows.find { it.getAlias()?.startsWith('total1') && it.kno == row.kno && it.kpp == row.kpp }
                subTotalMap[key1] = (findSubTotal != null)
                if (findSubTotal == null) {
                    def subMsg = getColumnName(row, 'kno') + '=' + (row.kno ?: 'не задан') + ', ' +
                            getColumnName(row, 'kpp') + '=' + (row.kpp ?: 'не задан')
                    logger.error(GROUP_WRONG_ITOG, subMsg)
                }
            }
            continue
        }

        if (row.getAlias() != null && row.getAlias().indexOf('total2') != -1) {
            // подитог кно/кпп/октмо
            // принадлежность подитога к последей простой строке
            if (row.kno != lastSimpleRow.kno || row.kpp != lastSimpleRow.kpp || row.oktmo != lastSimpleRow.oktmo) {
                logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                continue
            }
            // проверка сумм
            def srow = calcSubTotalRow2(dataRows.indexOf(row) - 1, dataRows, row.kno, row.kpp, row.oktmo)
            checkTotalRow(row, srow)
        } else if (row.getAlias() != null && row.getAlias().indexOf('total1') != -1) {
            // подитог кно/кпп
            // принадлежность подитога к последей простой строке
            if (row.kno != lastSimpleRow.kno || row.kpp != lastSimpleRow.kpp) {
                logger.error(GROUP_WRONG_ITOG_ROW, row.getIndex())
                continue
            }
            // проверка сумм
            def srow = calcSubTotalRow1(dataRows.indexOf(row) - 1, dataRows, row.kno, row.kpp)
            checkTotalRow(row, srow)
        }
        lastSubTotalRow = row
    }

    // строка "ВСЕГО"
    def totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
    if (totalRow != null) {
        def tmpTotalRow = calcTotalRow(dataRows)
        checkTotalRow(totalRow, tmpTotalRow)
    } else {
        logger.error("Итоговые значения рассчитаны неверно!")
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def subTotalRow1Map = [:] // подитог 1ого уровня -> мапа с подитогами 2ого уровня
    def subTotalRow2Map = [:] // подитог 2ого уровня -> строки подгруппы
    def simpleRows = []
    def total = null

    // разложить группы по мапам
    dataRows.each{ row ->
        if (row.getAlias() == null) {
            simpleRows.add(row)
        } else if (row.getAlias().contains('total2')) {
            subTotalRow2Map.put(row, simpleRows)
            simpleRows = []
        } else if (row.getAlias().contains('total1')) {
            subTotalRow1Map.put(row, subTotalRow2Map)
            subTotalRow2Map = [:]
        } else {
            total = row
        }
    }
    dataRows.clear()

    // отсортировать и добавить все строки
    def tmpSorted1Rows = subTotalRow1Map.keySet().toList()?.sort { getSortValue(it) }
    tmpSorted1Rows.each { keyRow1 ->
        def subMap = subTotalRow1Map[keyRow1]
        def tmpSorted2Rows = subMap.keySet().toList()?.sort { getSortValue(it) }
        tmpSorted2Rows.each { keyRow2 ->
            def dataRowsList = subMap[keyRow2]
            sortAddRows(dataRowsList, dataRows)
            dataRows.add(keyRow2)
        }
        dataRows.add(keyRow1)
    }
    // если остались данные вне иерархии, то добавить их перед итогом
    sortAddRows(simpleRows, dataRows)
    dataRows.add(total)

    dataRowHelper.saveSort()
}

// значение группируемых столбцов для сортировки подитоговых строк
def getSortValue(def row) {
    return row.kno + '#' + row.kpp + '#' + (row.oktmo ? getRefBookValue(96L, row.oktmo) : '')
}

void sortAddRows(def addRows, def dataRows) {
    if (!addRows.isEmpty()) {
        def firstRow = addRows[0]
        // Массовое разыменовывание граф НФ
        def columnNameList = firstRow.keySet().collect { firstRow.getCell(it).getColumn() }
        refBookService.dataRowsDereference(logger, addRows, columnNameList)
        sortRowsSimple(addRows)
        dataRows.addAll(addRows)
    }
}

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

@Field
def sourceTypeId = 916 // Расчет земельного налога за отчетные периоды

void consolidation() {
    // графа 3..28
    def consolidationColumns = allColumns - 'fix' - 'rowNumber' - 'department'
    def dataRows = []
    def departmentNames = []

    // получить источники
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)

    // собрать данные из источнков
    for (Relation relation : sourcesInfo) {
        if (relation.formType.id != sourceTypeId) {
            continue
        }
        def useDepartment = false
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        for (def sourceRow : sourceDataRows) {
            if (!sourceRow.getAlias() && sourceRow.department == formData.departmentId) {
                def newRow = getNewRow()
                consolidationColumns.each { alias ->
                    newRow[alias] = sourceRow[alias]
                }
                newRow.department = relation?.department?.id
                dataRows.add(newRow)
                useDepartment = true
            }
        }
        if (useDepartment) {
            departmentNames.add(relation?.department?.name)
        }
    }

    if (departmentNames) {
        def subMsg = departmentNames.sort().join(', ')
        logger.warn("Выполнена консолидация данных в форму из форм-источников вида «Расчет земельного налога за отчетные периоды» подразделений: «%s»", subMsg)
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { groupColumns.contains(it.getAlias())})
    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    // добавить подитоги
    addAllStatic(dataRows)

    // добавить строку "всего"
    dataRows.add(calcTotalRow(dataRows))
    updateIndexes(dataRows)
}

/** Получить итоговую строку. */
def getTotalRow() {
    def newRow = formData.createDataRow()
    newRow.getCell("fix").colSpan = 3
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    for (def alias : totalColumns) {
        newRow[alias] = BigDecimal.ZERO
    }
    return newRow
}

/** Получить итоговую строку с суммами. */
def calcTotalRow(def dataRows) {
    def newRow = getTotalRow()
    newRow.setAlias('total')
    newRow.fix = 'ВСЕГО'
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

/**
 * Добавить промежуточные итоги.
 * По графе 3, 4 (КНО/КПП) - 1 уровень группировки, а внутри этой группы по графе 6 (октмо) - 2 уровнь группировки.
 */
void addAllStatic(def dataRows) {
    for (int i = 0; i < dataRows.size(); i++) {
        def row = getRow(dataRows, i)
        def nextRow = getRow(dataRows, i + 1)
        int j = 0

        // 2 уровнь группировки
        def value2 = row?.oktmo
        def nextValue2 = nextRow?.oktmo
        if (row.getAlias() == null && nextRow == null || value2 != nextValue2) {
            def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.kpp, row.oktmo)
            j++
            dataRows.add(i + j, subTotalRow2)
        }

        // 1 уровнь группировки
        def value1 = getGroupL1Key(row)
        def nextValue1 = getGroupL1Key(nextRow)
        if (row.getAlias() == null && nextRow == null || value1 != nextValue1) {
            // если все значения пустые, то подитог по 2 уровню группировки не добавится,
            // поэтому перед добавлением подитога по 1 уровню группировки, нужно добавить подитог с пустыми значениями по 2 уровню
            if (j == 0) {
                def subTotalRow2 = calcSubTotalRow2(i, dataRows, row.kno, row.kpp, row.oktmo)
                j++
                dataRows.add(i + j, subTotalRow2)
            }
            def subTotalRow1 = calcSubTotalRow1(i, dataRows, row.kno, row.kpp)
            j++
            dataRows.add(i + j, subTotalRow1)
        }
        i += j  // Обязательно чтобы избежать зацикливания в простановке
    }
}

/** Расчет итога 1 уровня группировки - по графе 3, 4 КНО/КПП. */
def calcSubTotalRow1(int i, def dataRows, def kno, def kpp) {
    def newRow = getTotalRow()
    newRow.setAlias('total1#' + i)
    newRow.fix = 'ИТОГО ПО КНО/КПП'

    // значения группы
    newRow.kno = kno
    newRow.kpp = kpp

    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias()) {
            continue
        }
        if (newRow.kno != srow.kno || newRow.kpp != srow.kpp) {
            break
        }
        for (def alias : totalColumns) {
            if (srow[alias] != null) {
                newRow[alias] = newRow[alias] + srow[alias]
            }
        }
    }
    return newRow
}

/** Расчет итога 2 уровня группировки - по графе 6 ОКТМО (группировка внутри группы по КНО/КПП). */
def calcSubTotalRow2(int i, def dataRows, def kno, def kpp, def oktmo) {
    def newRow = getTotalRow()
    newRow.setAlias("total2#" + i)
    newRow.fix = 'ИТОГО'

    // значения группы
    newRow.kno = kno
    newRow.kpp = kpp
    newRow.oktmo = oktmo

    // идем от текущей позиции вверх и ищем нужные строки
    for (int j = i; j >= 0; j--) {
        def srow = getRow(dataRows, j)
        if (srow.getAlias() != null || srow.oktmo != newRow.oktmo) {
            break
        }
        for (def alias : totalColumns) {
            if (srow[alias] != null) {
                newRow[alias] = newRow[alias] + srow[alias]
            }
        }
    }
    return newRow
}

/** Получение строки по номеру. */
def getRow(def dataRows, int i) {
    if (i < dataRows.size() && i >= 0) {
        return dataRows.get(i)
    } else {
        return null
    }
}

// Получить ключ группировки 1ого уровня (по графе 3, 4)
def getGroupL1Key(def row) {
    return row?.kno + '#' + row?.kpp
}

/**
 * Проверить итоги/подитоги. Для логической проверки N.
 *
 * @param row итоговая строка нф
 * @param tmpRow посчитанная итоговая строка
 */
void checkTotalRow(def row, def tmpRow) {
    def errorColumns = []
    for (def column : totalColumns) {
        if (row[column] != tmpRow[column]) {
            errorColumns.add(getColumnName(row, column))
        }
    }
    if (!errorColumns.isEmpty()) {
        def columnNames = errorColumns.join('», «')
        logger.error("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", row.getIndex(), columnNames)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 30
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 0

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
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

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()
    def totalRowFromFileMap = [:]           // мапа для хранения строк итогов/подитогов со значениями из файла (стили простых строк)
    def totalRowMap = [:]                   // мапа для хранения строк итогов/подитогов нф с посчитанными значениями и со стилями

    def hasRegion = (formDataDepartment.regionId != null)
    if (!hasRegion) {
        def columnName15 = getColumnName(tmpRow, 'benefitCode')
        logger.warn("Не удалось заполнить графу «%s», т.к. для подразделения формы не заполнено поле «Регион» справочника «Подразделения»",
                columnName15)
    }

    // заполнить кэш данными из справочника ОКТМО
    def limitRows = 10
    if (allValuesCount > limitRows) {
        fillRefBookCache(96L)
        fillRecordCache(96L, 'CODE', getReportPeriodEndDate())
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
        // пропуск итоговой строки
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("всего")) {
            // получить значения итоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // пропуск подитоговых строк
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого")) {
            // сформировать и подсчитать подитоги
            def subTotalRow
            if (rowValues[INDEX_FOR_SKIP]?.trim()?.toLowerCase()?.contains("итого по ")) {
                subTotalRow = calcSubTotalRow1(rowIndex - 1, rows, rowValues[3], rowValues[4])
            } else {
                def oktmo = (rowValues[6] ? getRecordIdImport(96L, 'CODE', rowValues[6], fileRowIndex, 6 + colOffset) : null)
                subTotalRow = calcSubTotalRow2(rowIndex - 1, rows, rowValues[3], rowValues[4], oktmo)
            }
            rows.add(subTotalRow)
            // получить значения подитоговой строки из файла
            rowIndex++
            totalRowFromFileMap[rowIndex] = getNewTotalRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
            totalRowMap[rowIndex] = subTotalRow

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, hasRegion)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    // итоговая строка
    def totalRow = calcTotalRow(rows)
    rows.add(totalRow)
    totalRowMap[rowIndex] = totalRow
    updateIndexes(rows)

    // сравнение итогов
    if (!totalRowFromFileMap.isEmpty()) {
        // сравнение
        totalRowFromFileMap.keySet().toArray().each { index ->
            def totalFromFile = totalRowFromFileMap[index]
            def total = totalRowMap[index]
            compareTotalValues(totalFromFile, total, totalColumns, logger, 0, false)
            // задание значении итоговой строке нф из итоговой строки файла (потому что в строках из файла стили для простых строк)
            total.setImportIndex(totalFromFile.getImportIndex())
            (totalColumns + 'fix').each { alias ->
                total[alias] = totalFromFile[alias]
            }
        }
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
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][0]) : headers[0].fix],
            [(headerRows[0][2]) : headers[0].department],
            [(headerRows[0][3]) : headers[0].kno],
            [(headerRows[0][4]) : headers[0].kpp],
            [(headerRows[0][5]) : headers[0].kbk],
            [(headerRows[0][6]) : headers[0].oktmo],
            [(headerRows[0][7]) : headers[0].cadastralNumber],
            [(headerRows[0][8]) : headers[0].landCategory],
            [(headerRows[0][9]) : headers[0].constructionPhase],
            [(headerRows[0][10]): headers[0].cadastralCost],
            [(headerRows[0][11]): headers[0].taxPart],
            [(headerRows[0][12]): headers[0].ownershipDate],
            [(headerRows[0][13]): headers[0].terminationDate],
            [(headerRows[0][14]): headers[0].period],
            [(headerRows[0][15]): headers[0].benefitCode],
            [(headerRows[1][15]): headers[1].benefitCode],
            [(headerRows[1][16]): headers[1].benefitBase],
            [(headerRows[1][17]): headers[1].benefitParam],
            [(headerRows[1][18]): headers[1].startDate],
            [(headerRows[1][19]): headers[1].endDate],
            [(headerRows[1][20]): headers[1].benefitPeriod],
            [(headerRows[0][21]): headers[0].taxRate],
            [(headerRows[0][22]): headers[0].kv],
            [(headerRows[0][23]): headers[0].kl],
            [(headerRows[0][24]): headers[0].sum],
            [(headerRows[0][25]): headers[0].q1],
            [(headerRows[1][25]): headers[1].q1],
            [(headerRows[1][26]): headers[1].q2],
            [(headerRows[1][27]): headers[1].q3],
            [(headerRows[1][28]): headers[1].year],
            [(headerRows[0][29]): headers[0].name],
            [(headerRows[2][0]) : '1']
    ]
    (2..28).each {
        headerMapping.add([(headerRows[2][it]) : it.toString()])
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
 * @param hasRegion признак необходимости заполнения графы 15, 16, 17
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def hasRegion) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 2 - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
    def int colIndex = 2
    newRow.department = getRecordIdImport(30L, 'NAME', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 3
    colIndex++
    newRow.kno = values[colIndex]

    // графа 4
    colIndex++
    newRow.kpp = values[colIndex]

    // графа 5 - атрибут 7031 - CODE - «Код», справочник 703 «Коды бюджетной классификации земельного налога»
    colIndex++
    newRow.kbk = getRecordIdImport(703L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 6 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex++
    if (values[colIndex]) {
        newRow.oktmo = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    } else {
        // проверка графы 6
        def xlsColumnName6 = getXLSColumnName(colIndex + colOffset)
        def columnName15 = getColumnName(newRow, 'benefitCode')
        def columnName6 = getColumnName(newRow, 'oktmo')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, xlsColumnName6, columnName15, columnName6)
    }

    // проверка графы 2
    if (values[colIndex] && !values[2]) {
        def xlsColumnName2 = getXLSColumnName(2 + colOffset)
        def columnName15 = getColumnName(newRow, 'benefitCode')
        def columnName2 = getColumnName(newRow, 'department')
        logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. не заполнена графа «%s»",
                fileRowIndex, xlsColumnName2, columnName15, columnName2)
    }

    // графа 7
    colIndex++
    newRow.cadastralNumber = values[colIndex]

    // графа 8 - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
    colIndex++
    newRow.landCategory = getRecordIdImport(702L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9 - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
    colIndex++
    newRow.constructionPhase = getRecordIdImport(701L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 10
    colIndex++
    newRow.cadastralCost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.taxPart = values[colIndex]

    // графа 12
    colIndex++
    newRow.ownershipDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13
    colIndex++
    newRow.terminationDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14
    colIndex++
    newRow.period = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 15, 16, 17
    if (hasRegion) {
        // графа 15 - атрибут 7053 - TAX_BENEFIT_ID - «Код налоговой льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        colIndex = 15
        def record704 = (values[6] ? getRecordImport(704, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false) : null)
        def code = record704?.record_id?.value
        def oktmo = newRow.oktmo
        def param = values[17] ?: null
        def region = getRefBookValue(30L, newRow.department)?.REGION_ID?.value
        def record705 = getRecord705Import(code, region, oktmo, param)
        if (record705 == null) {
            // повторный поиск с регионом текущей формы
            region = formDataDepartment?.regionId
            record705 = getRecord705Import(code, region, oktmo, param)
        }
        newRow.benefitCode = record705?.record_id?.value
        if (values[6] && record705 == null) {
            def xlsColumnName15 = getXLSColumnName(colIndex + colOffset)
            def columnName15 = getColumnName(newRow, 'benefitCode')
            def columnName16 = getColumnName(newRow, 'benefitBase')
            def columnName17 = getColumnName(newRow, 'benefitParam')
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графы: «%s», «%s», «%s», т.к. в справочнике " +
                    "«Параметры налоговых льгот земельного налога» не найдена соответствующая запись",
                    fileRowIndex, xlsColumnName15, columnName15, columnName16, columnName17)
        }

        // графа 16 - зависит от графы 15 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
        if (record704 && record705) {
            colIndex++
            def expectedValues = [record704?.BASE?.value]
            formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'benefitBase'), record704?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
        }

        // графа 17 - зависит от графы 15 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
        // проверять не надо, т.к. участвует в поиске родительской записи
    }

    // графа 18
    colIndex = 18
    newRow.startDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 19
    colIndex++
    newRow.endDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 20..28
    ['benefitPeriod', 'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

/**
 * Получить новую итоговую строку нф по значениям из экселя. Строка используется только для получения значении,
 * для вставки в бд сформируются другие строки с нормальными стилями и алиасами.
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

    // графа fix
    def int colIndex = 0
    newRow.fix = values[colIndex]

    // графа 3
    colIndex = 3
    newRow.kno = values[colIndex]

    // графа 4
    colIndex = 4
    newRow.kpp = values[colIndex]

    // графа 6 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
    colIndex = 6
    newRow.oktmo = getRecordIdImport(96L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 25..28
    colIndex = 24
    [ 'q1', 'q2', 'q3', 'year'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}


/** Заполнить refBookCache всеми записями справочника refBookId. */
void fillRefBookCache(def refBookId) {
    def records = getAllRecords2(refBookId)
    for (def record : records) {
        def recordId = record?.record_id?.value
        def key = getRefBookCacheKey(refBookId, recordId)
        if (refBookCache[key] == null) {
            refBookCache.put(key, record)
        }
    }
}

/**
 * Заполнить recordCache всеми записями справочника refBookId из refBookCache.
 *
 * @param refBookId идентификатор справочника
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param date дата по которой будет осуществляться поиск
 */
void fillRecordCache(def refBookId, def alias, def date) {
    def keys = refBookCache.keySet().toList()
    def needKeys = keys.findAll { it.contains(refBookId + SEPARATOR) }
    def dateSts = date.format('dd.MM.yyyy')
    def rb = refBookFactory.get(refBookId)
    for (def needKey : needKeys) {
        def recordId = refBookCache[needKey]?.record_id?.value
        def value = refBookCache[needKey][alias]?.value
        def filter = getFilter(alias, value, rb)
        def key = dateSts + filter
        if (recordCache[refBookId] == null) {
            recordCache[refBookId] = [:]
        }
        recordCache[refBookId][key] = recordId
    }
}

/**
 * Формирование фильтра. Взято из FormDataServiceImpl.getRefBookRecord(...)
 *
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param value значение атрибута справочника
 * @param rb справочник
 */
def getFilter(def alias, def value, def rb) {
    def filter
    if (value == null || value.isEmpty()) {
        filter = alias + " is null"
    } else {
        RefBookAttributeType type = rb.getAttribute(alias).getAttributeType()
        String template
        // TODO: поиск по выражениям с датами не реализован
        if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
            if (!isNumeric(value)) {
                // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                return null
            }
            template = "%s = %s"
        } else {
            template = "LOWER(%s) = LOWER('%s')"
        }
        filter = String.format(template, alias, value)
    }
    return filter
}

boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?")
}

@Field
def allRecordsMap2 = [:]

def getAllRecords2(def refbookId) {
    if (allRecordsMap2[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        allRecordsMap2[refbookId] = provider.getRecords(getReportPeriodEndDate(), null, null, null)
    }
    return allRecordsMap2[refbookId]
}

/**
 * Получить запись справочника 705 "Параметры налоговых льгот земельного налога".
 *
 * @param code графа 15 - код налоговой льготы (id справочника 704)
 * @param region графа 2 - регион подразделения графы 2 (id справочника 4)
 * @param oktmo графа 6 - код ОКТМО (id справочника 96)
 * @param param графа 17 - параметры льготы (строка, может быть null)
 */
def getRecord705Import(def code, def region, def oktmo, def param) {
    if (code == null || oktmo == null) {
        return null
    }
    def allRecords = getAllRecords2(705L)
    for (def record : allRecords) {
        if (code == record?.TAX_BENEFIT_ID?.value &&
                region == record?.DECLARATION_REGION_ID?.value &&
                oktmo == record?.OKTMO?.value &&
                ((param ?: null) == (record?.REDUCTION_PARAMS?.value ?: null) || param?.equalsIgnoreCase(record?.REDUCTION_PARAMS?.value))) {
            return record
        }
    }
    return null
}

/** Получить результат для события FormDataEvent.GET_SOURCES. */
void getSources() {
    // нестандратны только формы-источники - "Расчет земельного налога за отчетные периоды"
    if (!(form && needSources)) {
        // формы-приемники, декларации-истчоники, декларации-приемники не переопределять
        return
    }

    def reportPeriod = getReportPeriod()
    def reportPeriodId = reportPeriod?.id
    def periodOrder = null
    def comparativePeriodId = null
    def accruing = false
    def SBId = 0

    def departments = departmentService.getAllChildren(SBId)
    for (def department : departments) {
        def departmentId = department.id
        def tmpFormData = formDataService.getLast(sourceTypeId, FormDataKind.SUMMARY, departmentId,
                reportPeriodId, periodOrder, comparativePeriodId, accruing)
        if (tmpFormData == null) {
            continue
        }
        def relation = getRelation(tmpFormData, department, reportPeriod)
        if (relation) {
            sources.sourceList.add(relation)
        }
    }
    sources.sourcesProcessedByScript = true
}

/**
 * Получить запись для источника-приемника.
 *
 * @param tmpFormData нф
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(FormData tmpFormData, Department department, ReportPeriod period, Integer monthOrder = null) {
    // boolean excludeIfNotExist - исключить несозданные источники
    if (excludeIfNotExist && tmpFormData == null) {
        return null
    }
    // WorkflowState stateRestriction - ограничение по состоянию для созданных экземпляров
    if (stateRestriction && tmpFormData != null && stateRestriction != tmpFormData.state) {
        return null
    }
    Relation relation = new Relation()
    def isSource = true

    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(tmpFormData?.departmentReportPeriodId) as DepartmentReportPeriod
    DepartmentReportPeriod comparativePeriod = getDepartmentReportPeriodById(tmpFormData?.comparativePeriodId) as DepartmentReportPeriod
    FormType formType = getFormTypeById(tmpFormData.formType.id) as FormType
    def departmentFormTypes = departmentFormTypeService.getByTaxType(tmpFormData.departmentId, TaxType.LAND, getReportPeriodStartDate(), getReportPeriodEndDate())
    def departmentFormType = departmentFormTypes.find { it.formTypeId == tmpFormData.formType.id && it.kind == tmpFormData.kind }
    def performers = departmentFormType?.performers?.collect { getDepartmentById(it) as Department }

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        /** Идентификатор подразделения */
        relation.departmentId = tmpFormData.departmentId
        /** полное название подразделения */
        relation.fullDepartmentName = getDepartmentFullName(tmpFormData.departmentId)
        /** Дата корректировки */
        relation.correctionDate = departmentReportPeriod?.correctionDate
        /** Вид нф */
        relation.formTypeName = formType?.name
        /** Год налогового периода */
        relation.year = period.taxPeriod.year
        /** Название периода */
        relation.periodName = period.name
        /** Название периода сравнения */
        relation.comparativePeriodName = comparativePeriod?.reportPeriod?.name
        /** Дата начала периода сравнения */
        relation.comparativePeriodStartDate = comparativePeriod?.reportPeriod?.startDate
        /** Год периода сравнения */
        relation.comparativePeriodYear = comparativePeriod?.reportPeriod?.taxPeriod?.year
        /** название подразделения-исполнителя */
        relation.performerNames = performers?.collect { it?.name }?.findAll { it }
    }
    /**************  Общие параметры ***************/
    /** подразделение */
    relation.department = department
    /** Период */
    relation.departmentReportPeriod = departmentReportPeriod
    /** Статус ЖЦ */
    relation.state = tmpFormData?.state
    /** форма/декларация создана/не создана */
    relation.created = (tmpFormData != null)
    /** является ли форма источников, в противном случае приемник*/
    relation.source = isSource
    /** Введена/выведена в/из действие(-ия) */
    try {
        relation.status = getFormTemplateById(tmpFormData.formTemplateId) != null
    } catch (DaoException e) {
        relation.status = false
    }
    /** Налог */
    relation.taxType = TaxType.LAND

    /**************  Параметры НФ ***************/
    /** Идентификатор созданной формы */
    relation.formDataId = tmpFormData?.id
    /** Вид НФ */
    relation.formType = formType
    /** Тип НФ */
    relation.formDataKind = tmpFormData.kind
    /** подразделение-исполнитель*/
    relation.performers = performers
    /** Период сравнения. Может быть null */
    relation.comparativePeriod = comparativePeriod
    /** Номер месяца */
    relation.month = monthOrder
    if (tmpFormData) {
        /** Признак расчета значений нарастающим итогом (false - не нарастающим итогом, true - нарастающим итогом, пустое - форма без периода сравнения) */
        relation.accruing = tmpFormData.accruing
        /** Признак ручного ввода */
        relation.manual = tmpFormData.manual
    }
    return relation
}

@Field
def departmentReportPeriodMap = [:]

def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

// Мапа для хранения типов форм (id типа формы -> тип формы)
@Field
def formTypeMap = [:]

/** Получить тип фомры по id. */
def getFormTypeById(def id) {
    if (formTypeMap[id] == null) {
        formTypeMap[id] = formTypeService.get(id)
    }
    return formTypeMap[id]
}


// Мапа для хранения подразделений (id подразделения -> подразделение)
@Field
def departmentMap = [:]

/** Получить подразделение по id. */
def getDepartmentById(def id) {
    if (id != null && departmentMap[id] == null) {
        departmentMap[id] = departmentService.get(id)
    }
    return departmentMap[id]
}

@Field
def formTemplateMap = [:]

def getFormTemplateById(def formTemplateId) {
    if (formTemplateId == null) {
        return null
    }

    if (formTemplateMap[formTemplateId] == null) {
        formTemplateMap[formTemplateId] = formDataService.getFormTemplate(formTemplateId)
    }
    return formTemplateMap[formTemplateId]
}

// Мапа для хранения полного названия подразделения (id подразделения  -> полное название)
@Field
def departmentFullNameMap = [:]

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}