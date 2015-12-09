package form_template.income.income_simple_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormData
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
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]
@Field
def recordCache = [:]
@Field
def formDataCache = [:]

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

// Получение Id записи из справочника 28 с использованием кэширования
def getRecordId(String knu, String accountNo, Date date) {
    def ref_id = 28
    String filter = getFilterForRefbook28(knu, accountNo)
    if (knu == '') {
        filter = "CODE is null"
    }
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

// Метод заполняющий в кэш все записи для разыменывавания из справочника 28
void fillRecordsMap(List<String> values, Date date) {
    def ref_id = 28
    def filterList = values.collect {
        "LOWER(CODE) = LOWER('$it')"
    }
    def filter = filterList.join(" OR ")
    def records = refBookFactory.getDataProvider(ref_id).getRecords(date, null, filter, null)
    records.each { record ->
        filter = getFilterForRefbook28(record['CODE'].value, record['NUMBER'].value)
        if (recordCache[ref_id] == null) {
            recordCache[ref_id] = [:]
        }
        recordCache[ref_id][filter] = record.get(RefBook.RECORD_ID_ALIAS).numberValue
    }
}

/**
 * Получить фильтр для поиска в справочнике 28.
 *
 * @param knu код налогового учета (графа 1 сводной)
 * @param number балансовый счёт по учёту дохода (графа 4 сводной)
 */
def getFilterForRefbook28(def knu, def number) {
    def tmpNumber = number.replace('.', '')
    return "LOWER(CODE) = LOWER('$knu') and LOWER(NUMBER) = LOWER('$tmpNumber')"
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def row40001 = getDataRow(dataRows, total1Alias)
    def row40002 = getDataRow(dataRows, total2Alias)
    totalColumns.each { alias ->
        row40001[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        row40002[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }

    // Лог. проверка
    dataRows.each { row ->
        if (([2, 3] + (5..11) + (17..20) + [22, 24, 28, 29, 30] + [48, 49] + (54..56) + (70..75) + [145] + (148..158) + (160..162))
                .collect { "R$it" as String }.contains(row.getAlias())) {
            def BigDecimal summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0)
                    + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        // Графа 11
        if (!rowsNotCalc.contains(row.getAlias())) {
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = getFormData(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

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

    for (def row : dataRows) {
        def columns = []
        nonEmptyColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle) {
                columns.add(alias)
            }
        }
        checkNonEmptyColumns(row, row.getIndex(), columns, logger, true)
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
        def child = getFormData(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            def childData = formDataService.getDataRowHelper(child)

            for (def row : childData.allSaved) {
                if (row.getAlias() == null || row.getAlias().contains('total')) {
                    continue
                }
                def rowResult = dataRows.find { row.getAlias() == it.getAlias() }
                if (rowResult != null) {
                    for (alias in ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']) {
                        if (rowResult.getCell(alias)?.editable && row.getCell(alias).getValue() != null) {
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
            formDataOld = getFormData(301, formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId(), null, formData.comparativePeriodId, formData.accruing);
        }
    }
    if (formDataOld != null && reportPeriod.order != 1) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allSaved
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
    fillRecordsMap(knuList, getReportPeriodEndDate())

    // карта "id формы" : ("номер/дата документа" : "суммы")
    def sum7map = [:]

    // получить формы-источники в текущем налоговом периоде
    formSources.each {
        def child = getFormData(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataRowsChild = formDataService.getDataRowHelper(child).allSaved
            switch (child.formType.id) {
            // рну 6
                case formTypeId_RNU6:
                    def codeBalanceMap = [:]
                    dataRowsChild.each { rowRNU6 ->
                        if (rowRNU6.getAlias() == null) {
                            // ключ состоит из id записи кну и балансового счета
                            String key = String.valueOf(rowRNU6.code)
                            if (codeBalanceMap[key] == null) {
                                codeBalanceMap[key] = ["sum5" : 0, "sum6" : 0, "sum7" : 0]
                            }
                            //«графа 5» =  сумма значений по «графе 10» (столбец «Сумма дохода в налоговом учёте. Рубли») всех форм источников вида «(РНУ-6)
                            codeBalanceMap[key].sum5 += (rowRNU6.taxAccountingRuble ?: 0)
                            //«графа 6» =  сумма значений по «графе 12» (столбец «Сумма дохода в бухгалтерском учёте. Рубли») всех форм источников вида «(РНУ-6)
                            codeBalanceMap[key].sum6 += (rowRNU6.ruble ?: 0)
                            //графа 7
                            if (rowRNU6.ruble != null && rowRNU6.ruble != 0) {
                                def dateFrom = Date.parse('dd.MM.yyyy', '01.01.' + (Integer.valueOf(rowRNU6.docDate?.format('yyyy')) - 3))
                                def reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, rowRNU6.docDate)
                                reportPeriodList.each { period ->
                                    // ищем формы, в процессе заполняем карту
                                    def primaryRNU6 = getFormData(child.formType.id, child.kind, child.departmentId, period.getId(), null, child.comparativePeriodId, child.accruing)
                                    if (primaryRNU6 != null) {
                                        // для формы достаточно id в качестве ключа
                                        String keyForm = String.valueOf(primaryRNU6.id)
                                        String keyDoc = "${rowRNU6.code}#${rowRNU6.docNumber}#${rowRNU6.docDate}"
                                        if (sum7map[keyForm] == null) { // если карта пустая, то еще не заполняли
                                            sum7map[keyForm] = [:]
                                            def dataPrimary = formDataService.getDataRowHelper(primaryRNU6)
                                            dataPrimary.allSaved.each { rowPrimary ->
                                                if (rowPrimary.getAlias() == null &&
                                                        rowPrimary.code == rowRNU6.code &&
                                                        rowPrimary.docNumber == rowRNU6.docNumber &&
                                                        rowPrimary.docDate == rowRNU6.docDate) {
                                                    String localKeyDoc = "${rowPrimary.code}#${rowPrimary.docNumber}#${rowPrimary.docDate}"
                                                    if (sum7map[keyForm][localKeyDoc] == null) {
                                                        sum7map[keyForm][localKeyDoc] = 0
                                                    }
                                                    sum7map[keyForm][localKeyDoc] += (rowPrimary.taxAccountingRuble ?: 0)
                                                }
                                            }
                                        }
                                        codeBalanceMap[key].sum7 += (sum7map[keyForm][keyDoc] ?: 0)
                                    }
                                }
                            }
                        }
                    }
                    rows567.each { rowNum ->
                        def row = getDataRow(dataRows, "R$rowNum")
                        if (row.incomeTypeId != null && row.accountNo != null) {
                            def recordId = getRecordId(row.incomeTypeId, row.accountNo, getReportPeriodEndDate())

                            if (isEqualNum(row.accountNo, recordId)) {
                                def sums = codeBalanceMap[String.valueOf(recordId)]
                                if (sums != null) {
                                    row.rnu6Field10Sum = (row.rnu6Field10Sum ?: 0) + (sums.sum5 ?: 0)
                                    row.rnu6Field12Accepted = (row.rnu6Field12Accepted ?: 0) + (sums.sum6 ?: 0)
                                    row.rnu6Field12PrevTaxPeriod = (row.rnu6Field12PrevTaxPeriod ?: 0) + (sums.sum7 ?: 0)
                                }
                            }
                        }
                    }
                    codeBalanceMap.clear()
                    break
            // рну 4
                case formTypeId_RNU4:
                    rows8.each { rowNum ->
                        def row = getDataRow(dataRows, "R$rowNum")

                        def recordId = getRecordId(row.incomeTypeId, row.accountNo, getReportPeriodEndDate())

                        def sum8 = 0
                        dataRowsChild.each { rowRNU4 ->
                            if (rowRNU4.getAlias() == null) {
                                if (row.incomeTypeId != null && row.accountNo != null && recordId == rowRNU4.balance && isEqualNum(row.accountNo, rowRNU4.balance)) {
                                    //«графа 8» =  сумма значений по «графе 5» (столбец «Сумма дохода за отчётный квартал») всех форм источников вида «(РНУ-4)
                                    sum8 += (rowRNU4.sum ?: 0)
                                }
                            }
                        }
                        row.rnu4Field5Accepted = (row.rnu4Field5Accepted ?: 0) + (sum8 ?: 0)
                    }
                    break
            }
        }
    }
}

FormData getFormData(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId, Integer periodOrder, Integer comparativePeriodId, boolean accruing) {
    String key = "$formTypeId#${kind.id}#$departmentId#$reportPeriodId#$periodOrder"
    if (formDataCache[key] != -1 && formDataCache[key] == null){ // чтобы повторно не искал несуществующие формы
        formDataCache[key] = formDataService.getLast(formTypeId, kind, departmentId, reportPeriodId, periodOrder, comparativePeriodId, accruing) ?: -1
    }
    return (formDataCache[key] != -1) ? formDataCache[key] : null
}

def getBalanceValue(def value) {
    formDataService.getRefBookValue(28, value, refBookCache)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    def a = accNum?.replace('.', '')
    def b = (balance ? getBalanceValue(balance)?.replace('.', '') : null)
    return a == b
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
        def dataRow = dataRows.find{ it.getAlias() == alias }
        if (dataRow == null) {
            continue
        }
        // заполнить строку нф значениями из эксель
        if (alias in [total1Alias, total2Alias]) {
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
    def row40001Tmp = formData.createStoreMessagingDataRow()
    def row40002Tmp = formData.createStoreMessagingDataRow()
    totalColumns.each { alias ->
        row40001Tmp[alias] = getSum(dataRows, alias, first1Alias, last1Alias)
        row40002Tmp[alias] = getSum(dataRows, alias, first2Alias, last2Alias)
    }
    def row40001 = getDataRow(dataRows, total1Alias)
    def row40002 = getDataRow(dataRows, total2Alias)
    compareTotalValues(row40001, row40001Tmp, totalColumns, logger, false)
    compareTotalValues(row40002, row40002Tmp, totalColumns, logger, false)

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
    checkHeaderSize(headerRows[0].size(), headerRows.size(), 8, 3)
    def headerMapping = [
            ([(headerRows[0][0]): 'КНУ']),
            ([(headerRows[0][1]): 'Группа дохода']),
            ([(headerRows[0][2]): 'Вид дохода по операциям']),
            ([(headerRows[0][3]): 'Балансовый счёт по учёту дохода']),
            ([(headerRows[0][4]): 'РНУ-6 (графа 10) сумма']),
            ([(headerRows[0][5]): 'РНУ-6 (графа 12)']),
            ([(headerRows[0][7]): 'РНУ-4 (графа 5) сумма']),
            ([(headerRows[1][5]): 'сумма']),
            ([(headerRows[1][6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10'])
    ]
    (0..7).each { index ->
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

    // графа 5..8 (графа 8 всегда редактируемая, добавил для однообразности)
    colIndex = 3
    ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
        colIndex++
        if (dataRow.getCell(alias).isEditable()) {
            dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
        }
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

    // графа 5..8
    def colIndex = 3
    ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
}