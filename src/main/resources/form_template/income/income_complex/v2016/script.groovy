package form_template.income.income_complex.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "6.1.1	Сводная форма начисленных доходов (доходы сложные)".
 * formTypeId=302
 *
 * TODO заполнение графы 16
 *
 * @author bkinzyabulatov
 *
 * Графы:
 *
 * ********** 6.1.2 Сводная форма "Доходы, учитываемые в простых РНУ" уровня обособленного подразделения **********
 *
 * 1    incomeTypeId                КНУ
 * 2    incomeGroup                 Группа дохода
 * 3    incomeTypeByOperation       Вид дохода по операции
 * 4    accountNo                   Балансовый счёт по учёту дохода
 * 5    rnu6Field10Sum              РНУ-6 (графа 10) cумма
 * 6    rnu6Field12Accepted         сумма
 * 7    rnu6Field12PrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах по графе 10
 * 8    rnu4Field5Accepted          РНУ-4 (графа 5) сумма
 * 9    logicalCheck                Логическая проверка
 * 10   accountingRecords           Счёт бухгалтерского учёта
 * 11   opuSumByEnclosure2          в Приложении №5
 * 12   opuSumByTableD              в Таблице "Д"
 * 13   opuSumTotal                 в бухгалтерской отчётности
 * 14   difference                  Расхождение
 *
 * ********** 6.1.1	Сводная форма начисленных доходов уровня обособленного подразделения **********
 *
 * 1   incomeTypeId                 КНУ
 * 2   incomeGroup                  Группа дохода
 * 3   incomeTypeByOperation        Вид дохода по операциям
 * 4   incomeBuhSumAccountNumber    балансовый счёт по учёту дохода
 * 5   incomeBuhSumRnuSource        источник информации в РНУ
 * 6   incomeBuhSumAccepted         сумма
 * 7   incomeBuhSumPrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах
 * 8   incomeTaxSumRnuSource        источник информации в РНУ
 * 9   incomeTaxSumS                сумма
 * 10  rnuNo                        форма РНУ
 * 11  logicalCheck                 Логическая проверка
 * 12  accountingRecords            Счёт бухгалтерского учёта
 * 13  opuSumByEnclosure2           в Приложении №5
 * 14  opuSumByTableD               в Таблице "Д"
 * 15  opuSumTotal                  в бухгалтерской отчётности
 * 16  explanation                  Расхождение
 * 17  difference                   Расхождение
 */

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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
// подготовить/утвердить/принять
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // принять из утверждена
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // принять из создана
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

// Редактируемые атрибуты (графа 9)
@Field
def editableColumns = ['incomeTaxSumS']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS', 'logicalCheck',
                    'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

// Атрибут итоговой строки для которой вычисляются суммы (графа 9)
@Field
def totalColumn = 'incomeTaxSumS'

@Field
def formTypeId_RNU8 = 320
@Field
def formTypeId_RNU25 = 324
@Field
def formTypeId_RNU26 = 325
@Field
def formTypeId_RNU27 = 326
@Field
def formTypeId_RNU31 = 328

// Алиас для первой строки итогов
@Field
def firstTotalRowAlias = 'R23'

// Алиас для второй строки итогов
@Field
def secondTotalRowAlias = 'R95'

// Алиасы строк ~РНУ-8 для расчета контрольных граф Сводной формы начисленных доходов
@Field
def rowsAliasesRnu8 = (26..31).collect { "R$it" as String } as List<String>

// Алиасы строк, по которым надо подвести итоги для первой итоговой строки
@Field
def rowsAliasesForFirstControlSum = (2..22).collect { "R$it" as String } as List<String>

// Алиасы строк, по которым надо подвести итоги для второй итоговой строки
@Field
def rowsAliasesForSecondControlSum = (25..94).collect { "R$it" as String } as List<String>

@Field
def editableStyle = 'Числовое значение'

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

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // контрольные графы
    calcControlRnu8(dataRows)    // расчет строк ~РНУ-8

    // итоговые строки
    getDataRow(dataRows, firstTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum)
    getDataRow(dataRows, secondTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // 1. Проверка заполнения обязательных граф
    for (def row : dataRows) {
        def columns = []
        editableColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                columns.add(alias)
            }
        }
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    // 2. Арифметическая проверка итоговых значений
    checkTotalSum(getDataRow(dataRows, firstTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum))
    checkTotalSum(getDataRow(dataRows, secondTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum))

    // 3. Проверка наличия формы «Оборотная ведомость»
    def rowIndexes = []
    rowsAliasesRnu8.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        if (!income101Data || income101Data.isEmpty()) { // Нет данных об оборотной ведомости
            rowIndexes += row.getIndex()
        }
    }
    if (!rowIndexes.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
    }

    // 4. Проверка наличия данных в форме-источнике «Таблица 1. Пояснение отклонений от ОФР в простом регистре налогового учёта «Доходы»» для заполнения графы 16
    // перенесена в консолидацию
}

// Консолидация формы
def consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(), getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)
    // TODO консолидация из "Таблица 1" + сообщение при отсутствии

}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId == formData.formType.id } != null
    def isPrimarySource = formSources.find { it.formTypeId in [formTypeId_RNU8, formTypeId_RNU25, formTypeId_RNU26, formTypeId_RNU27, formTypeId_RNU31] } != null
    if (isSummarySource && isPrimarySource) {
        logger.warn("Для текущей формы назначены формы-источники по двум видам консолидации: 1. Формы РНУ; 2. «Сводная форма начисленных доходов (доходы сложные)». Консолидация выполнена из форм-источников «Сводная форма начисленных доходов (доходы сложные)")
        return true
    } else if (isSummarySource || isPrimarySource) {
        return isSummarySource
    } else {
        logger.warn("Для текущей формы не назначены корректные формы-источники.")
        return true
    }
}

void consolidationFromSummary(def dataRows, def formSources) {
    if (dataRows == null || dataRows.isEmpty()) {
        return
    }
    // очистить форму
    dataRows.each { row ->
        editableColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                row[alias] = 0
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
        if (row.getAlias() in [firstTotalRowAlias, secondTotalRowAlias]) {
            row.incomeTaxSumS = 0
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    formSources.each {
        def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (def row : formDataService.getDataRowHelper(child).allSaved) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = getDataRow(dataRows, row.getAlias())
                editableColumns.each { alias ->
                    if (rowResult.getCell(alias)?.editable && row.getCell(alias).getValue() != null) {
                        rowResult[alias] = summ(rowResult.getCell(alias), row.getCell(alias))
                    }
                }
            }
        }
    }
}

// Консолидация из первичек
void consolidationFromPrimary(def dataRows, def formSources) {
    // очистить форму
    dataRows.each { row ->
        editableColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                row[alias] = 0
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
        if (row.getAlias() in [firstTotalRowAlias, secondTotalRowAlias]) {
            row.incomeTaxSumS = 0
        }
    }

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def formDataOld = formDataService.getFormDataPrev(formData)
    if (formDataOld != null && reportPeriod.order != 1 && formDataOld.state == WorkflowState.ACCEPTED) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allSaved
        //графа 9
        def prevCodeList = [
                '15170', '15180', '15190', '15200', '15210', '15220', // РНУ-8
                '16110', // РНУ-25
                '16120', // РНУ-26
                '16130', // РНУ-27
                '16430', '16440', '16450', '16460' // РНУ-31
        ]
        addPrevValue(prevCodeList, dataRows, 'incomeTaxSumS', dataRowsOld, 'incomeTaxSumS')
    }

    // получить формы-источники в текущем налоговом периоде
    formSources.each {
        def isMonth = it.formTypeId in [formTypeId_RNU31] //ежемесячная
        def children = []
        if (isMonth) {
            for (def periodOrder = 3 * reportPeriod.order - 2; periodOrder < 3 * reportPeriod.order + 1; periodOrder++) {
                def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, reportPeriod.id, periodOrder, formData.comparativePeriodId, formData.accruing)
                if (child.state == WorkflowState.ACCEPTED) {
                    children.add(child)
                }
            }
        } else {
            def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (child.state == WorkflowState.ACCEPTED) {
                children.add(child)
            }
        }
        for (def child in children) {
            if (child != null) {
                def dataRowsChild = formDataService.getDataRowHelper(child)?.allSaved
                switch (child.formType.id) {
                    case formTypeId_RNU8: //(РНУ-8) Простой регистр налогового учёта «Требования»
                        def provider = formDataService.getRefBookProvider(refBookFactory, 28L, providerCache)
                        def rows = dataRowsChild.findAll { it.getAlias() == null }
                        ['15170', '15180', '15190', '15200', '15210', '15220'].each { knu ->
                            // графа 9 = разность сумм граф 6 и 5 строк источника где КНУ и балансовый счет совпадают с текущей строкой
                            def row = getRow(dataRows, knu)
                            String filter = "LOWER(CODE) = LOWER('" + row.incomeTypeId + "') and LOWER(NUMBER) = LOWER('" + row.incomeBuhSumAccountNumber.replaceAll(/\./, "") + "')"
                            def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
                            records.each { record ->
                                def recordId = record?.get(RefBook.RECORD_ID_ALIAS)?.value
                                rows.each { rowChild ->
                                    if (recordId == rowChild.balance && isEqualNum(row.incomeBuhSumAccountNumber, rowChild.balance)) {
                                        (row.incomeTaxSumS) ? (row.incomeTaxSumS += (rowChild.outcome - rowChild.income)) : (row.incomeTaxSumS = (rowChild.outcome - rowChild.income))
                                    }
                                }
                            }
                        }
                        break
                    case formTypeId_RNU25: //(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения
                        // графа 9 = сумма граф 13 итогов форм
                        addChildTotalData(dataRows, '16110', 'incomeTaxSumS', dataRowsChild, 'total', ['reserveRecovery'])
                        break
                    case formTypeId_RNU26: //(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения
                        // графа 9 = сумма граф 17 итогов форм
                        addChildTotalData(dataRows, '16120', 'incomeTaxSumS', dataRowsChild, 'total', ['reserveRecovery'])
                        break
                    case formTypeId_RNU27: //(РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
                        // графа 9 = сумма граф 17 итогов форм
                        addChildTotalData(dataRows, '16130', 'incomeTaxSumS', dataRowsChild, 'total', ['recovery'])
                        break
                    case formTypeId_RNU31: //(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям
                        // графа 9 = сумма граф 10, 11 и 12 итогов форм
                        addChildTotalData(dataRows, '16430', 'incomeTaxSumS', dataRowsChild, 'total', ['eurobondsRF', 'itherEurobonds', 'corporateBonds'])
                        // графа 9 = сумма граф 3, 4, 5, 6 итогов форм
                        addChildTotalData(dataRows, '16440', 'incomeTaxSumS', dataRowsChild, 'total', ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds'])
                        // графа 9 = сумма граф 7, 8 итогов форм
                        addChildTotalData(dataRows, '16450', 'incomeTaxSumS', dataRowsChild, 'total', ['municipalBondsBefore', 'rtgageBondsBefore'])
                        // графа 9 = сумма граф 9 итогов форм
                        addChildTotalData(dataRows, '16460', 'incomeTaxSumS', dataRowsChild, 'total', ['ovgvz'])
                        break
                }
            }
        }
    }
}

/**
 * Добавить значение из формы предыдущего периода
 * @param codes - коды строк
 * @param dataRows - строки текущей сводной
 * @param column - столбец текущей сводной
 * @param dataRowsOld - строки прошлой сводной
 * @param columnOld - столбец прошлой сводной
 */
void addPrevValue(Collection<String> codes, def dataRows, String column, def dataRowsOld, String columnOld) {
    if (!(dataRows && dataRowsOld && column && columnOld)) {
        return
    }
    codes.each { code ->
        def row = dataRows.find { row.incomeTypeId == code }
        def rowOld = dataRowsOld.find { it.incomeTypeId == code && it.incomeBuhSumAccountNumber == row.incomeBuhSumAccountNumber }
        row[column] ? (row[column] += rowOld[columnOld]) : (row[column] = rowOld[columnOld])
    }
}

// Расчет контрольных граф Сводной формы начисленных доходов (строки ~РНУ-8)
void calcControlRnu8(def dataRows) {
    rowsAliasesRnu8.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        // графа 14
        row.opuSumByTableD = getOpuSumByTableDControlRnu8(row, income101Data)
        // графа 15
        row.opuSumTotal = getOpuSumTotalControlRnu8(row, income101Data)
        // графа 17 = «Графа 14» - «Графа 15» - «Графа 16»
        row.difference = row.opuSumByTableD - row.opuSumTotal - (row.explanation ?: BigDecimal.ZERO)
    }
}

// Графа 15. Расчет контрольных граф Сводной формы начисленных доходов (строки ~РНУ-8)
def getOpuSumTotalControlRnu8(def row, def income101Data) {
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.OUTCOME_DEBET_REMAINS.numberValue ?: BigDecimal.ZERO)
            } else {
                return BigDecimal.ZERO
            }
        }
    }
    return 0
}

// Графа 14. Расчет контрольных граф Сводной формы начисленных доходов (строки ~РНУ-8)
def getOpuSumByTableDControlRnu8(def row, def income101Data) {
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.INCOME_DEBET_REMAINS.numberValue ?: BigDecimal.ZERO)
            } else {
                return BigDecimal.ZERO
            }
        }
    }
    return 0
}

// Возвращает данные из Оборотной Ведомости за период, для которого сформирована текущая форма
def getIncome101Data(def row) {
    // Справочник 50 - "Оборотная ведомость (Форма 0409101-СБ)"
    return bookerStatementService.getRecords(50L, formData.departmentId, getReportPeriodEndDate(), "ACCOUNT = '${row.accountingRecords}'")
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Подсчет сумм для столбца colName в строках rowsAliases
def getSum(def dataRows, def colName, def rowsAliases) {
    return rowsAliases.sum { rowAlias ->
        return getDataRow(dataRows, rowAlias)[colName] ?: BigDecimal.ZERO
    }
}

def getBalanceValue(def value) {
    formDataService.getRefBookValue(28, value, refBookCache)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    return accNum.replace('.', '') == getBalanceValue(balance)?.replace('.', '')
}

/**
 * Метод для консолидации - расчет ячейки из итоговой строки источника
 * @param dataRows - строки сводной
 * @param knu - КНУ рассчитываемой строки сводной
 * @param column - рассчитываемый столбец сводной
 * @param dataRowsChild - строки источника
 * @param totalAlias - псевдоним итоговой строки
 * @param columnsChild - графы для сложения из источников
 */
void addChildTotalData(def dataRows, String knu, def column, def dataRowsChild, String totalAlias, def columnsChild) {
    if (!(dataRows && knu && column && dataRowsChild && columnsChild)) {
        logger.info("Ошибка при консолидации")// не должен сюда заходить
        return
    }
    def addValue = BigDecimal.ZERO
    for (def rowChild : dataRowsChild) {
        //ищем итоговую строку
        if (rowChild.getAlias() == totalAlias) {
            addValue += columnsChild.sum { columnChild ->
                return (rowChild[columnChild] ?:  BigDecimal.ZERO)
            }
        }
    }
    def row = getRow(dataRows, knu)
    if (row[column] == null) {
        logger.info("Пустая ячейка при КНУ = ${knu}")// не должен сюда заходить
    }
    row[column] ? (row[column] += addValue) : (row[column] = addValue)
}

def getRow(def dataRows, def knu) {
    for (def row : dataRows) {
        // ищем по кну строку в сводной
        if (row.incomeTypeId == knu) {
            return row
        }
    }
}

void checkTotalSum(totalRow, sum) {
    if (totalRow[totalColumn] != sum) {
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графе \"" + getColumnName(totalRow, totalColumn) + "\"")
    }
}

void importData() {
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'КНУ'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def allValuesCount = allValues.size()

    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[i]
        fileRowIndex++
        rowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            break
        }
        // прервать по загрузке нужных строк
        if (rowIndex > dataRows.size()) {
            break
        }

        // найти нужную строку нф
        def alias = "R" + rowIndex
        def dataRow = getDataRow(dataRows, alias)
        // заполнить строку нф значениями из эксель
        if (alias in [firstTotalRowAlias, secondTotalRowAlias]) {
            // итоги
            fillTotalRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        } else {
            // остальные строки
            fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
        }
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }

    // сравнение итогов
    def totalRow1Tmp = formData.createStoreMessagingDataRow()
    def totalRow2Tmp = formData.createStoreMessagingDataRow()
    totalRow1Tmp[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum)
    totalRow2Tmp[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum)

    def totalRow1 = getDataRow(dataRows, firstTotalRowAlias)
    def totalRow2 = getDataRow(dataRows, secondTotalRowAlias)
    compareTotalValues(totalRow1, totalRow1Tmp, [totalColumn], logger, false)
    compareTotalValues(totalRow2, totalRow2Tmp, [totalColumn], logger, false)

    showMessages(dataRows, logger)
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 */
void checkHeaderXls(def headerRows) {
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), 10, 3)
    def headerMapping = [
            ([(headerRows[0][0]): 'КНУ']),
            ([(headerRows[0][1]): 'Группа дохода']),
            ([(headerRows[0][2]): 'Вид дохода по операциям']),
            ([(headerRows[0][3]): 'Доход по данным бухгалтерского учёта']),
            ([(headerRows[0][7]): 'Доход по данным налогового учёта']),
            ([(headerRows[1][3]): 'балансовый счёт по учёту дохода']),
            ([(headerRows[1][4]): 'источник информации в РНУ']),
            ([(headerRows[1][5]): 'сумма']),
            ([(headerRows[1][6]): 'в т.ч. учтено в предыдущих налоговых периодах']),
            ([(headerRows[1][7]): 'источник информации в РНУ']),
            ([(headerRows[1][8]): 'сумма']),
            ([(headerRows[1][9]): 'форма РНУ'])
    ]
    (0..9).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    //очищаем столбцы
    resetColumns.each { alias ->
        dataRow[alias] = null
    }

    def knu = normalize(getOwnerValue(dataRow, 'incomeTypeId'))
    def group = normalize(getOwnerValue(dataRow, 'incomeGroup'))
    def type = normalize(getOwnerValue(dataRow, 'incomeTypeByOperation'))

    def colIndex = 0
    def knuImport = normalize(values[colIndex])

    colIndex++
    def groupImport = normalize(values[colIndex])

    colIndex++
    def typeImport = normalize(values[colIndex])

    //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
    //то продолжаем обработку строки иначе пропускаем строку
    if ((!knu.isEmpty() && knuImport.isEmpty()) || !((knu == knuImport && group == groupImport && type == typeImport) ||
            ((!knuImport.isEmpty() || !groupImport.isEmpty() || !typeImport.isEmpty()) &&
                    knu.contains(knuImport) && group.contains(groupImport) && type.contains(typeImport)))) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu.")
        return
    }

    // графа 6
    colIndex = 5
    if (dataRow.getCell('incomeBuhSumAccepted').isEditable()) {
        dataRow.incomeBuhSumAccepted = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 7
    colIndex++
    if (dataRow.getCell('incomeBuhSumPrevTaxPeriod').isEditable()) {
        dataRow.incomeBuhSumPrevTaxPeriod = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 8
    colIndex++

    // графа 9
    colIndex++
    if (dataRow.getCell('incomeTaxSumS').isEditable()) {
        dataRow.incomeTaxSumS = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}

/**
 * Заполняет итоговую строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param rowIndex номер строки в нф
 * @param colOffset отступ по столбцам
 */
def fillTotalRowFromXls(def dataRow, def values, int fileRowIndex, int rowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)
    dataRow.setIndex(rowIndex)

    // графа 9
    def colIndex = 8
    dataRow.incomeTaxSumS = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
}