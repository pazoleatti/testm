package form_template.income.income_simple.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Сводная форма "Доходы, учитываемые в простых РНУ (доходы простые)"
 * formTypeId=301
 *
 * TODO:
 *      - не сделан подсчет графы 13 (контрольные графы) потому что справочники "Отчет о прибылях и убытках" и "Оборотная ведомость" еще не реализованы
 *      - указанные справочники реализованы, но не заполнены данными
 *
 * @since 6.06.2013
 * @author auldanov
 */

// графа 1  - incomeTypeId              - КНУ
// графа 2  - incomeGroup               - Группа дохода
// графа 3  - incomeTypeByOperation     - Вид дохода по операциям
// графа 4  - accountNo                 - Балансовый счёт по учёту дохода
// графа 5  - rnu6Field10Sum            - РНУ-6 (графа 10) cумма
// графа 6  - rnu6Field12Accepted       - сумма
// графа 7  - rnu6Field12PrevTaxPeriod  - в т.ч. учтено в предыдущих налоговых периодах по графе 10
// графа 8  - rnu4Field5Accepted        - РНУ-4 (графа 5) сумма
// графа 9  - logicalCheck              - Логическая проверка
// графа 10 - accountingRecords         - Счёт бухгалтерского учёта
// графа 11 - opuSumByEnclosure2        - в Приложении №5
// графа 12 - opuSumByTableD            - в Таблице "Д"
// графа 13 - opuSumTotal               - в бухгалтерской отчётности
// графа 14 - difference                - Расхождение

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
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
}

//// Кэши и константы
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

//Все аттрибуты
@Field
def allColumns = ['incomeTypeId', 'incomeGroup', 'incomeTypeByOperation', 'accountNo',
        'rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

@Field
def rowsNotCalc = ['R1', 'R53', 'R54', 'R156']

@Field
def totalColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def formTypeId_RNU6 = 318

@Field
def formTypeId_RNU4 = 316

@Field
def rows567 = ([2, 3] + (5..11) + (17..20) + [22, 24] + (28..30) + [48, 49, 51, 52] + (65..70) + [139] + (142..151) + (153..155))

@Field
def rows8 = ((2..52) + (55..155))

@Field
def chRows = ['R118', 'R119', 'R140', 'R141', 'R141_1']

@Field
def editableStyle = 'Редактируемая'

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

// Метод заполняющий в кэш все записи для разыменывавания
void fillRecordsMap(def ref_id, String alias, List<String> values, Date date) {
    def filterList = values.collect {
        "LOWER($alias) = LOWER('$it')"
    }
    def filter = filterList.join(" OR ")
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    records.each { record ->
        filter = "LOWER($alias) = LOWER('${record[alias]}')"
        if (recordCache[ref_id] == null) {
            recordCache[ref_id] = [:]
        }
        recordCache[ref_id][filter] = record.get(RefBook.RECORD_ID_ALIAS).numberValue
    }
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    def row40001 = getDataRow(dataRows, 'R53')
    def row40002 = getDataRow(dataRows, 'R156')
    totalColumns.each { alias ->
        row40001[alias] = getSum(dataRows, alias, 'R2', 'R52')
        row40002[alias] = getSum(dataRows, alias, 'R55', 'R155')
    }

    // Лог. проверка
    dataRows.each { row ->
        if (['R2', 'R3', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R17', 'R18', 'R19', 'R20', 'R22', 'R24', 'R28',
                'R29', 'R30', 'R48', 'R49', 'R51', 'R52', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R139', 'R142',
                'R143', 'R144', 'R145', 'R146', 'R147', 'R148', 'R149', 'R150', 'R151', 'R153', 'R154', 'R155'
        ].contains(row.getAlias())) {
            def BigDecimal summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0)
                    + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        // Графа 11
        if (!rowsNotCalc.contains(row.getAlias())) {
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = formDataService.getLast(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (formData302 != null) {
                data302 = formDataService.getDataRowHelper(formData302)
                for (def rowOfForm302 in data302.allSaved) {
                    if (rowOfForm302.incomeBuhSumAccountNumber == row.accountNo) {
                        sum6ColumnOfForm302 += rowOfForm302.incomeBuhSumAccepted ?: 0
                    }
                }
            }
            row.opuSumByEnclosure2 = sum6ColumnOfForm302
        }

        // Графа 12
        if (!(row.getAlias() in rowsNotCalc)) {
            def sum8Column = 0
            dataRows.each {
                if (it.accountNo == row.accountNo) {
                    sum8Column += it.rnu4Field5Accepted ?: 0
                }
            }
            row.opuSumByTableD = sum8Column
        }

        // Графа 13
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            row.opuSumTotal = 0
            def income102Records = getIncome102Data(row)
            for (income102 in income102Records) {
                row.opuSumTotal += income102.TOTAL_SUM.numberValue
            }
        }

        if (row.getAlias() in chRows) {
            row.opuSumTotal = 0
            def income101Records = getIncome101Data(row)
            for (income101 in income101Records) {
                row.opuSumTotal += income101.DEBET_RATE.numberValue
            }
        }

        // Графа 14
        if (!rowsNotCalc.contains(row.getAlias()) && !(row.getAlias() in chRows)) {
            row.difference = (row.opuSumByEnclosure2 ?: 0) + (row.opuSumByTableD ?: 0) - (row.opuSumTotal ?: 0)
        }

        if (row.getAlias() in ['R118', 'R119']) {
            row.difference = (row.opuSumTotal ?: 0) - (row.rnu4Field5Accepted ?: 0)
        }

        if (row.getAlias() in ['R140', 'R141', 'R141_1']) {
            row.difference = (row.opuSumTotal ?: 0) -
                    ((getDataRow(dataRows, 'R140').rnu4Field5Accepted ?: 0) +
                            (getDataRow(dataRows, 'R141').rnu4Field5Accepted ?: 0) +
                            ((dataRows.find { 'R141_1'.equals(it.getAlias()) }?.rnu4Field5Accepted) ?: 0))
        }
    }
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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    def rowIndexes101 = []
    def rowIndexes102 = []
    for (row in dataRows) {
        //пропускаем строки где нет 10-й графы
        if (!row.accountingRecords) {
            continue
        }
        if (row.getAlias() in chRows) {
            def income101Records = getIncome101Data(row)
            if (!income101Records || income101Records.isEmpty()) {
                rowIndexes101 += row.getIndex()
            }
        }
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            def income102Records = getIncome102Data(row)
            if (!income102Records || income102Records.isEmpty()) {
                rowIndexes102 += row.getIndex()
            }
        }
    }
    if (!rowIndexes101.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes101.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
    }
    if (!rowIndexes102.isEmpty()) {
        logger.warn("Cтроки ${rowIndexes102.join(', ')}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
    }

    dataRows.each { row ->
        if (!rowsNotCalc.contains(row.getAlias())) {
            // Проверка обязательных полей
            checkRequiredColumns(row, nonEmptyColumns)
        }
    }

    def row40001 = getDataRow(dataRows, 'R53')
    def row40002 = getDataRow(dataRows, 'R156')
    def need40001 = [:]
    def need40002 = [:]
    totalColumns.each { alias ->
        need40001[alias] = getSum(dataRows, alias, 'R2', 'R52')
        need40002[alias] = getSum(dataRows, alias, 'R55', 'R155')
    }
    checkTotalSum(row40001, need40001)
    checkTotalSum(row40002, need40002)
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
    def isPrimarySource = formSources.find { it.formTypeId in [formTypeId_RNU6, formTypeId_RNU4] } != null
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

def consolidationFromSummary(def dataRows, def formSources) {
    // очистить форму
    dataRows.each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in ['R53', 'R156']) {
                row[alias] = 0
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }
    // получить данные из источников
    formSources.each { departmentFormType ->
        def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            def childData = formDataService.getDataRowHelper(child)

            for (def row : childData.allSaved) {
                if (row.getAlias() == null || row.getAlias().contains('total')) {
                    continue
                }
                def rowResult = dataRows.find { row.getAlias() == it.getAlias() }
                if (rowResult != null) {
                    for (alias in ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']) {
                        if (row.getCell(alias).getValue() != null) {
                            rowResult.getCell(alias).setValue(summ(rowResult.getCell(alias), row.getCell(alias)), null)
                        }
                    }
                }
            }
        }
    }
}

/** Консолидация данных из рну-6 и рну-4 в сводные доходы простые уровня ОП. */
def consolidationFromPrimary(def dataRows, def formSources) {

    // Очистить форму
    dataRows.each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in ['R53', 'R156']) {
                row[alias] = 0
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def formDataOld = formDataService.getFormDataPrev(formData)
    if (formDataOld != null && reportPeriod.order != 1) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allSaved
        rows567.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 5» +=«графа 5» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowOld = getDataRow(dataRowsOld, "R$rowNum")
            row.rnu6Field10Sum = rowOld.rnu6Field10Sum
            //«графа 6» +=«графа 6» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12Accepted = rowOld.rnu6Field12Accepted
            //«графа 7» +=«графа 7» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12PrevTaxPeriod = rowOld.rnu6Field12PrevTaxPeriod
        }
        rows8.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 8» +=«графа 8» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu4Field5Accepted = getDataRow(dataRowsOld, "R$rowNum").rnu4Field5Accepted
        }
        def row = dataRows.find { 'R141_1'.equals(it.getAlias()) }
        def rowPrev = dataRowsOld.find { 'R141_1'.equals(it.getAlias()) }
        if (row != null) {
            row.rnu4Field5Accepted = rowPrev?.rnu4Field5Accepted ?: 0
        }
    }

    // Прошел по строкам и получил список кну
    def knuList = ((2..52) + (55..155)).collect {// 141_1 пропускаем для совместимости с предыдущими формами
        def row = getDataRow(dataRows, 'R' + it)
        return row.incomeTypeId
    }
    fillRecordsMap(28, 'CODE', knuList, getReportPeriodEndDate())

    // получить формы-источники в текущем налоговом периоде
    formSources.each {
        def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataChild = formDataService.getDataRowHelper(child)
            switch (child.formType.id) {
            // рну 6
                case formTypeId_RNU6:
                    rows567.each { rowNum ->
                        def row = getDataRow(dataRows, "R$rowNum")

                        def recordId = getRecordId(28, 'CODE', row.incomeTypeId, getReportPeriodEndDate())

                        def sum5 = 0
                        def sum6 = 0
                        def sum7 = 0
                        dataChild.allSaved.each { rowRNU6 ->
                            if (rowRNU6.getAlias() == null) {
                                // если «графа 2» (столбец «Код налогового учета») формы источника = «графе 1» (столбец «КНУ») текущей строки и
                                //«графа 4» (столбец «Балансовый счёт (номер)») формы источника = «графе 4» (столбец «Балансовый счёт по учёту дохода»)
                                if (row.incomeTypeId != null && row.accountNo != null && recordId == rowRNU6.code && isEqualNum(row.accountNo, rowRNU6.code)) {
                                    //«графа 5» =  сумма значений по «графе 10» (столбец «Сумма дохода в налоговом учёте. Рубли») всех форм источников вида «(РНУ-6)
                                    sum5 += rowRNU6.taxAccountingRuble ?: 0
                                    //«графа 6» =  сумма значений по «графе 12» (столбец «Сумма дохода в бухгалтерском учёте. Рубли») всех форм источников вида «(РНУ-6)
                                    sum6 += rowRNU6.ruble ?: 0
                                    //графа 7
                                    if (rowRNU6.ruble != null && rowRNU6.ruble != 0) {
                                        def dateFrom = Date.parse('dd.MM.yyyy', '01.01.' + (Integer.valueOf(rowRNU6.date?.format('yyyy')) - 3))
                                        def reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, rowRNU6.date)
                                        reportPeriodList.each { period ->
                                            def primaryRNU6 = formDataService.getLast(child.formType.id, child.kind, child.departmentId, period.getId(), null, child.comparativePeriodId, child.accruing) // TODO не реализовано получение по всем подразделениям.
                                            if (primaryRNU6 != null) {
                                                def dataPrimary = formDataService.getDataRowHelper(primaryRNU6)
                                                dataPrimary.allSaved.each { rowPrimary ->
                                                    if (rowPrimary.code != null && rowPrimary.code == rowRNU6.code &&
                                                            rowPrimary.docNumber != null && rowPrimary.docNumber == rowRNU6.docNumber &&
                                                            rowPrimary.docDate != null && rowPrimary.docDate == rowRNU6.docDate) {
                                                        sum7 += rowPrimary.taxAccountingRuble ?: 0
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        row.rnu6Field10Sum += sum5
                        row.rnu6Field12Accepted += sum6
                        row.rnu6Field12PrevTaxPeriod += sum7
                    }
                    break
            // рну 4
                case formTypeId_RNU4:
                    rows8.each { rowNum ->
                        def row = getDataRow(dataRows, "R$rowNum")

                        def recordId = getRecordId(28, 'CODE', row.incomeTypeId, getReportPeriodEndDate())

                        def sum8 = 0
                        dataChild.getAll().each { rowRNU4 ->
                            if (rowRNU4.getAlias() == null) {
                                if (row.incomeTypeId != null && row.accountNo != null && recordId == rowRNU4.balance && isEqualNum(row.accountNo, rowRNU4.balance)) {
                                    //«графа 8» =  сумма значений по «графе 5» (столбец «Сумма дохода за отчётный квартал») всех форм источников вида «(РНУ-4)
                                    sum8 += rowRNU4.sum
                                }
                            }
                        }
                        row.rnu4Field5Accepted += sum8
                    }
                    def row = dataRows.find { 'R141_1'.equals(it.getAlias()) }
                    if (row != null) {
                        def recordId = getRecordId(28, 'CODE', row.incomeTypeId, getReportPeriodEndDate())
                        def sum8 = 0
                        dataChild.getAll().each { rowRNU4 ->
                            if (rowRNU4.getAlias() == null) {
                                if (row.incomeTypeId != null && row.accountNo != null && recordId == rowRNU4.balance && isEqualNum(row.accountNo, rowRNU4.balance)) {
                                    //«графа 8» =  сумма значений по «графе 5» (столбец «Сумма дохода за отчётный квартал») всех форм источников вида «(РНУ-4)
                                    sum8 += rowRNU4.sum
                                }
                            }
                        }
                        row.rnu4Field5Accepted += sum8
                    }
                    break
            }
        }
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
    return accNum.replace('.', '') == getBalanceValue(balance).replace('.', '')
}

// Проверить заполненость обязательных полей
// Нередактируемые не проверяются
def checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell?.style?.alias == editableStyle && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            colNames.add('«' + getColumnName(row, it) + '»')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("Строка ${row.getIndex()}: не заполнены графы : $errorMsg.")
    }
}

/** Получить сумму диапазона строк определенного столбца. */
def getSum(def dataRows, String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = getDataRow(dataRows, rowFromAlias).getIndex() - 1
    def to = getDataRow(dataRows, rowToAlias).getIndex() - 1
    if (from > to) {
        return 0
    }
    return ((BigDecimal) summ(formData, dataRows, new ColumnRange(columnAlias, from, to))).setScale(2, BigDecimal.ROUND_HALF_UP)
}

void checkTotalSum(totalRow, needRow) {
    def errorColumns = []
    totalColumns.each { totalColumn ->
        if (totalRow[totalColumn] != needRow[totalColumn]) {
            errorColumns += "\"" + getColumnName(totalRow, totalColumn) + "\""
        }
    }
    if (!errorColumns.isEmpty()) {
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графах ${errorColumns.join(", ")}!")
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
        def alias
        if (rowIndex < 142){
            alias = "R" + rowIndex
        } else if (rowIndex == 142) {
            alias = "R141_1"
        } else {
            alias = "R" + (rowIndex - 1)
        }
        def dataRow = dataRows.find{ it.getAlias() == alias }
        if (dataRow == null) {
            continue
        }
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, rowIndex, colOffset)
    }
    if (rowIndex < dataRows.size()) {
        logger.error("Структура файла не соответствует макету налоговой формы.")
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(dataRows)
        formDataService.getDataRowHelper(formData).allCached = dataRows
    }
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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), 8, 3)
    def headerMapping = [
            (headerRows[0][0]): 'КНУ',
            (headerRows[0][1]): 'Группа дохода',
            (headerRows[0][2]): 'Вид дохода по операциям',
            (headerRows[0][3]): 'Балансовый счёт по учёту дохода',
            (headerRows[0][4]): 'РНУ-6 (графа 10) сумма',
            (headerRows[0][5]): 'РНУ-6 (графа 12)',
            (headerRows[0][7]): 'РНУ-4 (графа 5) сумма',
            (headerRows[1][5]): 'сумма',
            (headerRows[1][6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10'
    ]
    (0..7).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
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
    // def group = normalize(dataRow.incomeGroup)
    def type = normalize(getOwnerValue(dataRow, 'incomeTypeByOperation'))
    def num = normalize(getOwnerValue(dataRow, 'accountNo'))


    def colIndex = 0
    def knuImport = normalize(values[colIndex])

    colIndex++
    // def groupImport = normalize(values[colIndex])

    colIndex++
    def typeImport = normalize(values[colIndex])

    colIndex++
    def numImport = normalize(values[colIndex]).replace(",", ".")

    // если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
    // то продолжаем обработку строки иначе пропускаем строку
    if (!((knu == knuImport && type == typeImport && num == numImport) ||
            ((!knuImport.isEmpty() || !typeImport.isEmpty() || !numImport.isEmpty()) &&
                    knu.contains(knuImport) && type.contains(typeImport) && num.contains(numImport)))) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu.")
        return
    }

    // графа 5..7
    colIndex = 3
    ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod'].each { alias ->
        colIndex++
        if (dataRow.getCell(alias).isEditable()) {
            dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
        }
    }

    // графа 8
    colIndex++
    dataRow.rnu4Field5Accepted = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
}