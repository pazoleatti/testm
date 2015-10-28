package form_template.income.income_complex.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
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
 * TODO:
 *      - непонятно что делать при отсутствии простых доходов в getIncomeSimpleDataRows().
 *              В расчетах контрольных графов пока будут нули при отсутствии данных в доходах простых.
 *      - неясно что с проверками статуса декларации банка при принятии и отмене принятия формы
 *      - консолидация из первичек недоописана(вопросы к заказчику)
 *
 * @author vsergeev
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
 * 16  difference                   Расхождение
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
    // подготовить/утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    // принятия
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

// Редактируемые атрибуты (графа 6, 7, 9)
@Field
def editableColumns = ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS']

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
def formTypeId_RNU30 = 329
@Field
def formTypeId_RNU31 = 328
@Field
def formTypeId_RNU33 = 332
@Field
def formTypeId_RNU44 = 340
@Field
def formTypeId_RNU49 = 312
@Field
def formTypeId_RNU51 = 345
@Field
def formTypeId_RNU55 = 348
@Field
def formTypeId_RNU56 = 349
@Field
def formTypeId_RNU57 = 353
@Field
def formTypeId_RNU72 = 358
@Field
def formTypeId_RNU75 = 366

// Алиас для первой строки итогов
@Field
def firstTotalRowAlias = 'R30'

// Алиас для второй строки итогов
@Field
def secondTotalRowAlias = 'R88'

// Алиасы строк 4-5 для расчета контрольных граф Сводной формы начисленных доходов
@Field
def rowsAliasesFor4to5 = ['R4', 'R5']

// Алиасы строк 35-30 для расчета контрольных граф Сводной формы начисленных доходов
@Field
def rowsAliasesFor35to40 = ['R35', 'R36', 'R37', 'R38', 'R39', 'R40']

// Алиасы строк, по которым надо подвести итоги для первой итоговой строки
@Field
def rowsAliasesForFirstControlSum = ['R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14',
        'R15', 'R16', 'R17', 'R18', 'R19', 'R20', 'R21', 'R22', 'R23', 'R24', 'R25', 'R26', 'R27', 'R28', 'R29']

// Алиасы строк, по которым надо подвести итоги для второй итоговой строки
@Field
def rowsAliasesForSecondControlSum = ['R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42',
        'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'R54', 'R55', 'R56', 'R57',
        'R58', 'R59', 'R60', 'R61', 'R62', 'R63', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72',
        'R73', 'R74', 'R75', 'R76', 'R77', 'R78', 'R79', 'R80', 'R81', 'R82', 'R83', 'R84', 'R85', 'R86', 'R87']

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

// Получение Id записи с использованием кэширования
def getRecordId(def ref_id, String alias, String value, Date date) {
    String filter = "LOWER($alias) = LOWER('$value')"
    if (value == '') filter = "$alias is null"
    if (recordCache[ref_id] != null) {
        if (recordCache[ref_id][filter] != null) {
            return recordCache[ref_id][filter]
        }
    } else {
        recordCache[ref_id] = [:]
    }
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    if (records.size() == 1) {
        recordCache[ref_id][filter] = records.get(0).get(RefBook.RECORD_ID_ALIAS).numberValue
        return recordCache[ref_id][filter]
    }
    return null
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // итоговые строки
    getDataRow(dataRows, firstTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum)
    getDataRow(dataRows, secondTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def rowIndexes = []
    rowsAliasesFor35to40.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        if (!income101Data || income101Data.isEmpty()) { // Нет данных об оборотной ведомости
            rowIndexes += row.getIndex()
        }
    }
    if (!rowIndexes.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
    }
    rowIndexes.clear()
    rowsAliasesFor4to5.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income102Data = getIncome102Data(row)
        if (!income102Data || income102Data.isEmpty()) { // Нет данных об оборотной ведомости
            rowIndexes += row.getIndex()
        }
    }
    if (!rowIndexes.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
    }

    for (def row : dataRows) {
        def columns = []
        editableColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                columns.add(alias)
            }
        }
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
    }

    checkTotalSum(getDataRow(dataRows, firstTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum))
    checkTotalSum(getDataRow(dataRows, secondTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum))

    if (!logger.containsLevel(LogLevel.ERROR)) {
        // контрольные графы
        calc4to5(dataRows)      // расчет строк 4 и 5
        calc35to40(dataRows)    // расчет строк 35-40
    }
}

// Консолидация формы
def consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(), getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)

}

boolean isFromSummary(def formSources) {
    def isSummarySource = formSources.find { it.formTypeId == formData.formType.id } != null
    def isPrimarySource = formSources.find { it.formTypeId in [formTypeId_RNU8, formTypeId_RNU25, formTypeId_RNU26, formTypeId_RNU27, formTypeId_RNU30, formTypeId_RNU31, formTypeId_RNU33, formTypeId_RNU44, formTypeId_RNU49, formTypeId_RNU51, formTypeId_RNU55, formTypeId_RNU56, formTypeId_RNU57, formTypeId_RNU72, formTypeId_RNU75] } != null
    if (isSummarySource && isPrimarySource) {
        logger.warn("Неверно настроены источники формы \"%s\" для подразделения \"%s\"! Одновременно указаны в качестве источников сводные и первичные налоговые формы. Консолидация произведена из сводных налоговых форм.",
                formData.formType.name, formDataDepartment.name)
        return true
    } else if (isSummarySource || isPrimarySource) {
        return isSummarySource
    } else {
        logger.warn("Неверно настроены источники формы \"%s\" для подразделения \"%s\"! Не указаны в качестве источников корректные сводные или первичные налоговые формы.",
                formData.formType.name, formDataDepartment.name)
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

    // получить формы-источники в текущем налоговом периоде
    formSources.each {
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
        def isMonth = it.formTypeId in [formTypeId_RNU33, formTypeId_RNU31] //ежемесячная
        if (!isMonth && prevReportPeriod != null) {
            def childOld = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, prevReportPeriod.id, null, it.comparativePeriodId, it.accruing)
            //(РНУ-75) Регистр налогового учета доходов по операциям депозитария
            if (childOld != null && childOld.formType.id == formTypeId_RNU75) {
                def dataRowsChildOld = formDataService.getDataRowHelper(childOld)?.allSaved
                // графа 7 = сумма граф 7 итогов форм за предыдущий период
                addChildTotalData(dataRows, '10650', 'incomeBuhSumPrevTaxPeriod', dataRowsChildOld, 'total', ['factSum'])
                addChildTotalData(dataRows, '10670', 'incomeBuhSumPrevTaxPeriod', dataRowsChildOld, 'total', ['factSum'])
            }
        }
        def children = []
        if (isMonth) {
            for (def periodOrder = 3 * reportPeriod.order - 2; periodOrder < 3 * reportPeriod.order + 1; periodOrder++) {
                def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, reportPeriod.id, periodOrder, it.comparativePeriodId, it.accruing)
                children.add(child)
            }
        } else {
            children.add(formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing))
        }
        for (def child in children) {
            if (child != null) {
                def dataRowsChild = formDataService.getDataRowHelper(child)?.allSaved
                switch (child.formType.id) {
                    case formTypeId_RNU75: //(РНУ-75) Регистр налогового учета доходов по операциям депозитария
                        // графа 6 = сумма граф 7 итогов форм
                        addChildTotalData(dataRows, '10650', 'incomeBuhSumAccepted', dataRowsChild, 'total', ['factSum'])
                        addChildTotalData(dataRows, '10670', 'incomeBuhSumAccepted', dataRowsChild, 'total', ['factSum'])
                        // графа 9 = сумма граф 6 итогов форм
                        addChildTotalData(dataRows, '10650', 'incomeTaxSumS', dataRowsChild, 'total', ['taxSum'])
                        addChildTotalData(dataRows, '10670', 'incomeTaxSumS', dataRowsChild, 'total', ['taxSum'])
                        break
                    case formTypeId_RNU49: //(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»
                        // графа 9 = сумма граф 11 и 15 итогов раздела А форм
                        addChildTotalData(dataRows, '10840', 'incomeTaxSumS', dataRowsChild, 'totalA', ['sum', 'sumIncProfit'])
                        // графа 9 = сумма граф 16 итогов раздела А форм
                        addChildTotalData(dataRows, '10845', 'incomeTaxSumS', dataRowsChild, 'totalA', ['profit'])
                        // графа 9 = сумма граф 11 и 15 итогов раздела Б форм
                        addChildTotalData(dataRows, '10850', 'incomeTaxSumS', dataRowsChild, 'totalB', ['sum', 'sumIncProfit'])
                        // графа 9 = сумма граф 11 и 15 итогов раздела Г форм
                        addChildTotalData(dataRows, '10880', 'incomeTaxSumS', dataRowsChild, 'totalG', ['sum', 'sumIncProfit'])
                        // графа 9 = сумма граф 11 и 15 итогов раздела Д форм
                        addChildTotalData(dataRows, '10890', 'incomeTaxSumS', dataRowsChild, 'totalD', ['sum', 'sumIncProfit'])
                        // графа 9 = сумма граф 11 и 15 итогов раздела Е форм
                        addChildTotalData(dataRows, '10900', 'incomeTaxSumS', dataRowsChild, 'totalE', ['sum', 'sumIncProfit'])
                        // графа 9 = сумма граф 13 итогов раздела В форм
                        addChildTotalData(dataRows, '13250', 'incomeTaxSumS', dataRowsChild, 'totalV', ['costProperty'])
                        break
                    case formTypeId_RNU72: //(РНУ-72) Регистр налогового учёта уступки права требования как реализации финансовых услуг и операций с закладными
                        // графа 9 = сумма граф 5 итогов форм
                        addChildTotalData(dataRows, '10855', 'incomeTaxSumS', dataRowsChild, 'total', ['income'])
                        break
                    case formTypeId_RNU44: //(РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее, чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств введённых в эксплуатацию после 01.01.2013
                        // графа 9 = сумма граф 7 итогов форм
                        addChildTotalData(dataRows, '10910', 'incomeTaxSumS', dataRowsChild, 'total', ['summ'])
                        break
                    case formTypeId_RNU51: //(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
                        // графа 9 = сумма граф 18 итогов форм
                        addChildTotalData(dataRows, '11190', 'incomeTaxSumS', dataRowsChild, 'itogo', ['salePriceTax'])
                        // графа 9 = сумма граф 22 итогов форм
                        addChildTotalData(dataRows, '11280', 'incomeTaxSumS', dataRowsChild, 'itogo', ['excessSalePriceTax'])
                        break
                    case formTypeId_RNU57: //(РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
                        // графа 9 = сумма граф 11 итогов форм
                        addChildTotalData(dataRows, '11260', 'incomeTaxSumS', dataRowsChild, 'total', ['implementationpPriceTax'])
                        // графа 9 = сумма граф 13 итогов форм
                        addChildTotalData(dataRows, '11300', 'incomeTaxSumS', dataRowsChild, 'total', ['implementationPriceUp'])
                        break
                    case formTypeId_RNU33: //(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО
                        // графа 9 = сумма граф 27 итогов форм
                        addChildTotalData(dataRows, '11270', 'incomeTaxSumS', dataRowsChild, 'total', ['excessOfTheSellingPrice'])
                        break
                    case formTypeId_RNU8: //(РНУ-8) Простой регистр налогового учёта «Требования»
                        def provider = formDataService.getRefBookProvider(refBookFactory, 28L, providerCache)
                        def rows = dataRowsChild.findAll { it.getAlias() == null }
                        ['13040', '13045', '13050', '13055', '13060', '13065'].each { knu ->
                            // графа 9 = разность сумм граф 6 и 5 строк источника где КНУ и балансовый счет совпадают с текущей строкой
                            def row = getRow(dataRows, knu)
                            String filter = "LOWER(CODE) = LOWER('" + row.incomeTypeId + "') and LOWER(NUMBER) = LOWER('" + row.incomeBuhSumAccountNumber.replaceAll(/\./, "") + "')"
                            def recordId = provider.getRecords(getReportPeriodEndDate(), null, filter, null)?.get(0)?.get(RefBook.RECORD_ID_ALIAS)?.value
                            rows.each { rowChild ->
                                if (recordId == rowChild.balance && isEqualNum(row.incomeBuhSumAccountNumber, rowChild.balance)) {
                                    (row.incomeTaxSumS) ? (row.incomeTaxSumS += (rowChild.outcome - rowChild.income)) : (row.incomeTaxSumS = (rowChild.outcome - rowChild.income))
                                }
                            }
                        }
                        break
                    case formTypeId_RNU25: //(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения
                        // графа 9 = сумма граф 13 итогов форм
                        addChildTotalData(dataRows, '13090', 'incomeTaxSumS', dataRowsChild, 'total', ['reserveRecovery'])
                        break
                    case formTypeId_RNU26: //(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения
                        // графа 9 = сумма граф 17 итогов форм
                        addChildTotalData(dataRows, '13100', 'incomeTaxSumS', dataRowsChild, 'total', ['reserveRecovery'])
                        break
                    case formTypeId_RNU27: //(РНУ-27) Регистр налогового учёта расчёта резерва под возможное обеспечение субфедеральных и муниципальных облигаций, ОВГВЗ, Еврооблигаций РФ и прочих облигаций в целях налогообложения
                        // графа 9 = сумма граф 17 итогов форм
                        addChildTotalData(dataRows, '13110', 'incomeTaxSumS', dataRowsChild, 'total', ['recovery'])
                        break
                    case formTypeId_RNU30: //(РНУ-30) Расчёт резерва по сомнительным долгам на основании результатов инвентаризации сомнительной задолженности и безнадежных долгов.
                        // графа 9 = сумма граф 15 итогов форм
                        addChildTotalData(dataRows, '13120', 'incomeTaxSumS', dataRowsChild, 'total', ['reserveRecovery'])
                        break
                    case formTypeId_RNU31: //(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям
                        // графа 9 = сумма граф 10, 11 и 12 итогов форм
                        addChildTotalData(dataRows, '13650', 'incomeTaxSumS', dataRowsChild, 'total', ['eurobondsRF', 'itherEurobonds', 'corporateBonds'])
                        // графа 9 = сумма граф 3, 4, 5, 6 итогов форм
                        addChildTotalData(dataRows, '13655', 'incomeTaxSumS', dataRowsChild, 'total', ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds'])
                        // графа 9 = сумма граф 7, 8 итогов форм
                        addChildTotalData(dataRows, '13660', 'incomeTaxSumS', dataRowsChild, 'total', ['municipalBondsBefore', 'rtgageBondsBefore'])
                        // графа 9 = сумма граф 9 итогов форм
                        addChildTotalData(dataRows, '13665', 'incomeTaxSumS', dataRowsChild, 'total', ['ovgvz'])
                        break
                    case formTypeId_RNU55: //(РНУ-55) Регистр налогового учёта процентного дохода по процентным векселям сторонних эмитентов
                        // графа 9 = сумма граф 11 итогов форм
                        addChildTotalData(dataRows, '13715', 'incomeTaxSumS', dataRowsChild, 'total', ['sumIncomeinRuble'])
                        break
                    case formTypeId_RNU56: //(РНУ-56) Регистр налогового учёта процентного дохода по дисконтным векселям сторонних эмитентов
                        // графа 9 = сумма граф 15 итогов форм
                        addChildTotalData(dataRows, '13720', 'incomeTaxSumS', dataRowsChild, 'total', ['sumIncomeinRuble'])
                        break
                    case 374: //(РНУ-112) Регистр налогового учета сделок РЕПО и сделок займа ценными бумагами
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 8 итогов форм
                        //addChildTotalData(dataRows, '13070', 'incomeTaxSumS', dataRowsChild, 'total', ['incomeDate'])
                        // графа 9 = сумма граф 15 итогов форм
                        //addChildTotalData(dataRows, '14280', 'incomeTaxSumS', dataRowsChild, 'total', ['sum'])
                        break
                    case 370: //(РНУ-117) Регистр налогового учёта доходов и расходов, по операциям со сделками форвард, квалифицированным в качестве операций с ФИСС для целей налогообложения
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 10.1 итогов форм
                        //addChildTotalData(dataRows, '13940', 'incomeTaxSumS', dataRowsChild, 'itg', ['income'])
                        // графа 9 = сумма граф 11.1 итогов форм
                        //addChildTotalData(dataRows, '13970', 'incomeTaxSumS', dataRowsChild, 'itg', ['deviationMinPrice'])
                        break
                    case 373: //(РНУ-118) Регистр налогового учёта доходов и расходов, по операциям со сделками опцион, квалифицированным в качестве операций с ФИСС для целей налогообложения.
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 13.1 итогов форм
                        //addChildTotalData(dataRows, '13950', 'incomeTaxSumS', dataRowsChild, 'itg', ['income'])
                        // графа 9 = сумма граф 11.1 итогов форм
                        //addChildTotalData(dataRows, '13980', 'incomeTaxSumS', dataRowsChild, 'itg', ['deviationMinPrice'])
                        break
                    case 371: //(РНУ-119) Регистр налогового учёта доходов и расходов, по сделкам своп, квалифицированным в качестве операций с ФИСС для целей налогообложения
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 14.1 итогов форм
                        //addChildTotalData(dataRows, '13960', 'incomeTaxSumS', dataRowsChild, 'itg', ['income'])
                        // графа 9 = сумма граф 15.1 итогов форм
                        //addChildTotalData(dataRows, '13990', 'incomeTaxSumS', dataRowsChild, 'itg', ['deviationMinPrice'])
                        break
                    case 502: //(РНУ-107) Регистр налогового учёта доходов, возникающих в связи с применением в сделках с Взаимозависимыми лицами и резидентами оффшорных зон тарифов, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 12 итогов форм
                        //addChildTotalData(dataRows, '14170', 'incomeTaxSumS', dataRowsChild, 'total', ['incomeSumBefore'])
                        break
                    case 396: //(РНУ-110) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению имущества в аренду Взаимозависимым лицам и резидентам оффшорных зон цен, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        //TODO актуализировать, общих итогов нет
                        // графа 9 = сумма граф 11 итогов форм
                        break
                    case 367: //(РНУ-111) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению Межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон Процентных ставок, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 17 итогов форм
                        //addChildTotalData(dataRows, '14190', 'incomeTaxSumS', dataRowsChild, 'total', ['incomeAddSum'])
                        break
                    case 369: //(РНУ-115) Регистр налогового учёта доходов, возникающих в связи с применением в сделках по предоставлению Межбанковских кредитов Взаимозависимым лицам и резидентам оффшорных зон Процентных ставок, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 19 итогов форм
                        //addChildTotalData(dataRows, '14200', 'incomeTaxSumS', dataRowsChild, 'total', ['liability'])
                        break
                    case 368: //(РНУ-116) Регистр налогового учёта доходов и расходов, возникающих в связи с применением в конверсионных сделках со Взаимозависимыми  лицами и резидентами оффшорных зон курса, не соответствующих рыночному уровню
                        //TODO пропускаю пока не определились с РНУ-100+
                        // графа 9 = сумма граф 19 итогов форм
                        //addChildTotalData(dataRows, '14210', 'incomeTaxSumS', dataRowsChild, 'total', ['liability'])
                        break
                }
            }
        }
    }
}

// Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
void calc35to40(def dataRows) {
    rowsAliasesFor35to40.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        // графа 14
        row.opuSumByTableD = getOpuSumByTableDFor35to40(row, income101Data)
        // графа 15
        row.opuSumTotal = getOpuSumTotalFor35to40(row, income101Data)
        // графа 16
        row.difference = getDifferenceFor35to40(row)
    }
}

// Графа 16. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getDifferenceFor35to40(def row) {
    // «графа 16» = «графа 9» - ( «графа 15» – «графа 14»)
    return row.incomeTaxSumS - (row.opuSumTotal - row.opuSumByTableD)
}

// Графа 15. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getOpuSumTotalFor35to40(def row, def income101Data) {
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.OUTCOME_DEBET_REMAINS.numberValue?:0)
            } else {
                return 0
            }
        }
    }
    return 0
}

// Графа 14. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getOpuSumByTableDFor35to40(def row, def income101Data){
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.INCOME_DEBET_REMAINS.numberValue ?: 0)
            } else {
                return 0
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

// Возвращает данные из Отчета о прибылях и убытках за период, для которого сформирована текущая форма
def getIncome102Data(def row) {
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    return bookerStatementService.getRecords(52L, formData.departmentId, getReportPeriodEndDate(), "OPU_CODE = '${row.accountingRecords}'")
}

// Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
void calc4to5(def dataRows) {
    def incomeSimpleDataRows = getIncomeSimpleDataRows(305)
    if (incomeSimpleDataRows == null) {
        incomeSimpleDataRows = getIncomeSimpleDataRows(301)
    } else if (getIncomeSimpleDataRows(301) != null) {
        logger.warn("Неверно настроены источники сводной! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Расчет произведен из «%s».",
                formTypeService.get(305).name, formTypeService.get(301)?.name, formTypeService.get(305)?.name)
    }

    rowsAliasesFor4to5.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)

        // графа 11
        row.logicalCheck = getLogicalCheckFor4to5(row)
        // графа 13
        row.opuSumByEnclosure2 = getOpuSumByEnclosure2For4to5(row)
        // графа 14
        row.opuSumByTableD = getOpuSumByTableDFor4to5(row, incomeSimpleDataRows)
        // графа 15
        row.opuSumTotal = getOpuSumTotalFor4to5(row)
        // графа 16
        row.difference = getDifferenceFor4to5(row)
    }
}

// Графа 16. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getDifferenceFor4to5(def row) {
    // «графа 16» = («графа 13» + «графа 14») – «графа 15»
    return (row.opuSumByEnclosure2 + row.opuSumByTableD) - row.opuSumTotal
}

// Графа 15. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumTotalFor4to5(def row) {
    // получить отчет о прибылях и убытках
    def income102Records = getIncome102Data(row)
    def tmp = 0
    for (income102 in income102Records) {
        tmp += income102.TOTAL_SUM.numberValue
    }
    return tmp
}

// Графа 14. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumByTableDFor4to5(def row, def incomeSimpleDataRows) {
    if (incomeSimpleDataRows) {
        def sum = 0
        for (def simpleRow : incomeSimpleDataRows) {
            if (simpleRow.accountNo == row.incomeBuhSumAccountNumber && simpleRow.rnu4Field5Accepted != null) {
                sum += simpleRow.rnu4Field5Accepted
            }
        }
        return sum
    }
    return 0
}

// Получить строки формы «Сводная форма "Доходы, учитываемые в простых РНУ" уровня обособленного подразделения»
def getIncomeSimpleDataRows(def formId) {
    def formDataKind = FormDataKind.SUMMARY
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId
    def periodOrder = formData.periodOrder
    // TODO (Aydar Kadyrgulov) Проверить, существует ли за этот же период простая форма. если нет, то вывести сообщение.
    // TODO (Ramil Timerbaev) Аналитик сказала, что ничего нового от банка пока нет, оставь так как есть
    def incomeSimpleFormData = formDataService.getLast(formId, formDataKind, departmentId, reportPeriodId, periodOrder, formData.comparativePeriodId, formData.accruing)
    if (incomeSimpleFormData != null && incomeSimpleFormData.id != null)
        return formDataService.getDataRowHelper(incomeSimpleFormData)?.allSaved
    return null
}

// Графа 13. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumByEnclosure2For4to5(def row) {
    return row.incomeBuhSumAccepted
}

// Графа 11. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getLogicalCheckFor4to5(def row) {
    def result = row.incomeTaxSumS - (row.incomeBuhSumAccepted - row.incomeBuhSumPrevTaxPeriod)
    return  (result < 0 ? 'Требуется уточнение' : round(result).toString())
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Подсчет сумм для столбца colName в строках rowsAliases
def getSum(def dataRows, def colName, def rowsAliases) {
    return rowsAliases.sum { rowAlias ->
        def tmp = getDataRow(dataRows, rowAlias)[colName]
        return (tmp ?: 0)
    }
}

// Проверка на банк
def isBank() {
    boolean isBank = true
    // получаем список приемников
    def list = departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formType.id, FormDataKind.SUMMARY, getReportPeriodStartDate(), getReportPeriodEndDate())
    // если есть приемники в других подразделениях, то это не банк, а ОП
    list.each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
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
    if (!(dataRows && knu && column && dataRowsChild && columnsChild)){
        logger.info("Ошибка при консолидации")//TODO заменить на что-то более адекватное
        return
    }
    def addValue = 0
    for (def rowChild : dataRowsChild) {
        //ищем итоговую строку
        if (rowChild.getAlias() == totalAlias) {
            addValue += columnsChild.sum { columnChild ->
                rowChild[columnChild] ?: 0
            }
        }
    }
    def row = getRow(dataRows, knu)
    if (row[column] == null) {
        logger.info("Пустая ячейка при КНУ = ${knu}")//TODO убрать
    }
    row[column] ? (row[column] += addValue) : (row[column] = addValue)
}

def getRow(def dataRows, def knu) {
    for (def row : dataRows) {
        // ищем по кну строку в сводной
        if (row.incomeTypeId == knu){
            return row
        }
    }
}

void checkTotalSum(totalRow, sum){
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
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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