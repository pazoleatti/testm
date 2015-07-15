package form_template.income.income_simple_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Сводная форма "Доходы, учитываемые в простых РНУ (доходы простые) (с полугодия 2015)"
 * formTypeId=305
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
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
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
        break
    case FormDataEvent.IMPORT:
        importData()
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
head1Alias = 'R1'
@Field
first1Alias = 'R2'
@Field
last1Alias = 'R56'
@Field
total1Alias = 'R57'
@Field
head2Alias = 'R58'
@Field
first2Alias = 'R59'
@Field
last2Alias = 'R162'
@Field
total2Alias = 'R163'

@Field
def rowsNotCalc = [head1Alias, total1Alias, head2Alias, total2Alias]

@Field
def totalColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def formTypeId_RNU6 = 318

@Field
def formTypeId_RNU4 = 316

@Field
def rows567 = ([2, 3] + (5..11) + (17..20) + [22, 24] + (28..30) + [48, 49] + (54..56) + (70..75) + [145] + (149..158) + (160..162))

@Field
def rows8 = ((2..56) + (59..162))

@Field
def chRows = ['R123', 'R124', 'R146', 'R147', 'R148']

@Field
def formatY = new SimpleDateFormat('yyyy')
@Field
def format = new SimpleDateFormat('dd.MM.yyyy')

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def row40001 = getDataRow(dataRows, total1Alias)
    def row40002 = getDataRow(dataRows, total2Alias)
    totalColumns.each { alias ->
        row40001[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        row40002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }

    // Лог. проверка
    dataRows.each { row ->
        if (([2, 3] + (5..11) + (17..20) + [22, 24, 28, 29, 30] + [48, 49] + (54..56) + (70..75) + [145] + (148..158) + (160..162))
                .collect { "R$it" }.contains(row.getAlias())) {
            def BigDecimal summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0)
                    + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        // Графа 11
        if (!rowsNotCalc.contains(row.getAlias())) {
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = formDataService.getLast(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (formData302 != null) {
                data302 = formDataService.getDataRowHelper(formData302)
                for (def rowOfForm302 in data302.allCached) {
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

        if (row.getAlias() in ['R123', 'R124']) {
            row.difference = (row.opuSumTotal ?: 0) - (row.rnu4Field5Accepted ?: 0)
        }

        if (row.getAlias() in ['R146', 'R147', 'R148']) {
            row.difference = (row.opuSumTotal ?: 0) -
                    ((getDataRow(dataRows, 'R146').rnu4Field5Accepted ?: 0) +
                            (getDataRow(dataRows, 'R147').rnu4Field5Accepted ?: 0) +
                            ((dataRows.find { 'R148'.equals(it.getAlias()) }?.rnu4Field5Accepted) ?: 0))
        }
    }

    dataRowHelper.update(dataRows)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

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

    def row40001 = getDataRow(dataRows, total1Alias)
    def row40002 = getDataRow(dataRows, total2Alias)
    def need40001 = [:]
    def need40002 = [:]
    totalColumns.each { alias ->
        need40001[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        need40002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
    checkTotalSum(row40001, need40001)
    checkTotalSum(row40002, need40002)
}

// Консолидация формы
def consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def formSources = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(), getReportPeriodStartDate(), getReportPeriodEndDate())
    def isFromSummary = isFromSummary(formSources)
    isFromSummary ? consolidationFromSummary(dataRows, formSources) : consolidationFromPrimary(dataRows, formSources)
    dataRowHelper.update(dataRows)
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
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row[alias] = 0
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row[alias] = null
        }
    }
    // получить данные из источников
    formSources.each { departmentFormType ->
        def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            def childData = formDataService.getDataRowHelper(child)

            for (def row : childData.getAll()) {
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
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
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
    if (formDataOld == null) {
        ReportPeriod prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId());
        if (prevReportPeriod != null) {
            // Последний экземпляр
            formDataOld = formDataService.getLast(301, formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId(), null);
        }
    }
    if (formDataOld != null && reportPeriod.order != 1) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.getAll()
        rows567.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 5» +=«графа 5» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowOld = dataRowsOld.find { row.incomeTypeId == it.incomeTypeId && row.accountNo == it.accountNo }
            if (rowOld) {
                row.rnu6Field10Sum = rowOld.rnu6Field10Sum
                //«графа 6» +=«графа 6» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
                row.rnu6Field12Accepted = rowOld.rnu6Field12Accepted
                //«графа 7» +=«графа 7» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
                row.rnu6Field12PrevTaxPeriod = rowOld.rnu6Field12PrevTaxPeriod
            }
        }
        rows8.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 8» +=«графа 8» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowOld = dataRowsOld.find { row.incomeTypeId == it.incomeTypeId && row.accountNo == it.accountNo }
            if (rowOld) {
                row.rnu4Field5Accepted = rowOld.rnu4Field5Accepted
            }
        }
    }

    // Прошел по строкам и получил список кну
    def knuList = ((2..56) + (59..162)).collect {
        def row = getDataRow(dataRows, 'R' + it)
        return row.incomeTypeId
    }
    fillRecordsMap(28, 'CODE', knuList, getReportPeriodEndDate())

    // получить формы-источники в текущем налоговом периоде
    formSources.each {
        def child = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
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
                        dataChild.getAll().each { rowRNU6 ->
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
                                        def dateFrom = format.parse('01.01.' + (Integer.valueOf(formatY.format(rowRNU6.date)) - 3))
                                        def reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, rowRNU6.date)
                                        reportPeriodList.each { period ->
                                            def primaryRNU6 = formDataService.getLast(child.formType.id, FormDataKind.PRIMARY, child.departmentId, period.getId(), null)
                                            if (primaryRNU6 != null) {
                                                def dataPrimary = formDataService.getDataRowHelper(primaryRNU6)
                                                dataPrimary.getAll().each { rowPrimary ->
                                                    if (rowPrimary.code != null && rowPrimary.code == rowRNU6.code &&
                                                            rowPrimary.docNumber != null && rowPrimary.docNumber == rowRNU6.docNumber &&
                                                            rowPrimary.docDate != null && rowPrimary.docDate == rowRNU6.docDate) {
                                                        sum7 += rowPrimary.taxAccountingRuble
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
                    break
            }
        }
    }
}

void checkCreation() {
    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
    formDataService.checkUnique(formData, logger)
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'КНУ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 8, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'КНУ',
            (xml.row[0].cell[1]): 'Группа дохода',
            (xml.row[0].cell[2]): 'Вид дохода по операциям',
            (xml.row[0].cell[3]): 'Балансовый счёт по учёту дохода',
            (xml.row[0].cell[4]): 'РНУ-6 (графа 10) сумма',
            (xml.row[0].cell[5]): 'РНУ-6 (графа 12)',
            (xml.row[0].cell[7]): 'РНУ-4 (графа 5) сумма',
            (xml.row[1].cell[5]): 'сумма',
            (xml.row[1].cell[6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10'
    ]
    (0..7).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
    def int maxRow = 163

    def rows = dataRowHelper.allCached
    def int rowIndex = 1
    def knu
    def type
    def num
    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }
        // прервать по загрузке нужных строк
        if (rowIndex > maxRow) {
            break
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def alias = "R" + rowIndex
        def curRow = rows.find{ it.getAlias() == alias}
        if (curRow == null) {
            continue
        }
        curRow.setImportIndex(xlsIndexRow)

        //очищаем столбцы
        resetColumns.each {
            curRow[it] = null
        }

        knu = normalize(getOwnerValue(curRow, 'incomeTypeId'))
        //def group = normalize(curRow.incomeGroup)
        type = normalize(getOwnerValue(curRow, 'incomeTypeByOperation'))
        num = normalize(getOwnerValue(curRow, 'accountNo'))

        def xmlIndexCol = 0

        def knuImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        //def groupImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def typeImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def numImport = normalize(row.cell[xmlIndexCol].text()).replace(",", ".")

        //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
        //то продолжаем обработку строки иначе пропускаем строку
        if (!((knu == knuImport && type == typeImport && num == numImport) ||
                ((!knuImport.isEmpty() || !typeImport.isEmpty() || !numImport.isEmpty()) &&
                        knu.contains(knuImport) && type.contains(typeImport) && num.contains(numImport)))) {
            continue
        }
        rowIndex++

        xmlIndexCol = 4

        // графа 5
        if (curRow.getCell('rnu6Field10Sum').isEditable()) {
            curRow.rnu6Field10Sum = parseNumber(normalize(row.cell[xmlIndexCol].text()), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 6
        if (curRow.getCell('rnu6Field12Accepted').isEditable()) {
            curRow.rnu6Field12Accepted = parseNumber(normalize(row.cell[xmlIndexCol].text()), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 7
        if (curRow.getCell('rnu6Field12PrevTaxPeriod').isEditable()) {
            curRow.rnu6Field12PrevTaxPeriod = parseNumber(normalize(row.cell[xmlIndexCol].text()), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 8
        curRow.rnu4Field5Accepted = parseNumber(normalize(row.cell[xmlIndexCol].text()), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
    }
    if (rowIndex < maxRow) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu. ")
    }
    dataRowHelper.update(rows)
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
