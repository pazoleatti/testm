package form_template.land.calc_for_declaration.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Расчет земельного налога по земельным участкам, подлежащим включению в декларацию.
 *
 * formTemplateId = 918
 * formTypeId = 918
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

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            formDataService.saveCachedDataRows(formData, logger)
        }
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
                  'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

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
    // пока не описаны в аналитике
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()

        // 1. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)
    }

    // 2. Проверка корректности значений итоговых строк
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
    def newRow = formData.createDataRow()
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
def sourceTypeId_916 = 916 // Расчет земельного налога за отчетные периоды
@Field
def sourceTypeId_917 = 917 // Земельные участки, подлежащие включению в декларацию

void consolidation() {
    // получить источники
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)

    // проверка перед консолидацией
    def has_917 = false
    for (Relation relation : sourcesInfo) {
        if (sourceTypeId_917 == relation.formType.id) {
            has_917 = true
        }
    }
    if (!has_917) {
        logger.error("Не удалось консолидировать данные в форму. В Системе отсутствует форма вида «Земельные участки, подлежащие включению в декларацию» в состоянии «Принята» " +
                "за период: «%s %s» для подразделения «%s»", getReportPeriod()?.name, getReportPeriod()?.taxPeriod?.year?.toString(), formDataDepartment.name)
        return
    }

    // графа 2..28
    def consolidationColumns = allColumns - 'fix' - 'rowNumber'
    // На форме настроек подразделений для подразделения формы найти значения поля «КПП».
    def kppList = getKPPList()

    def dataRows = []
    // собрать данные из источнков
    for (Relation relation : sourcesInfo) {
        if (![sourceTypeId_916, sourceTypeId_917].contains(relation.formType.id)) {
            continue
        }
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        for (def sourceRow : sourceDataRows) {
            if (sourceRow.getAlias()) {
                continue
            }
            if ((relation.formType.id == sourceTypeId_916 && kppList.contains(sourceRow.kpp)) || relation.formType.id == sourceTypeId_917) {
                def newRow = getNewRow()
                consolidationColumns.each { alias ->
                    newRow[alias] = sourceRow[alias]
                }
                dataRows.add(newRow)
            }
        }

    }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// Получить параметры подразделения
def getKPPList() {
    def departmentId = formData.departmentId
    def provider = formDataService.getRefBookProvider(refBookFactory, RefBook.WithTable.LAND.tableRefBookId, providerCache)
    def departmentParamList = provider.getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
    }
    def kppList = []
    for (def record : departmentParamList) {
        def kpp = record.KPP?.stringValue
        if (kpp != null && !kppList.contains(kpp)) {
            kppList.add(kpp)
        }
    }
    return kppList
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