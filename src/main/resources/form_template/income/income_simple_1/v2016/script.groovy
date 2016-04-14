package form_template.income.income_simple_1.v2016

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
 * Сводная форма "Доходы, учитываемые в простых РНУ (доходы простые) Сбербанка"
 * formTypeId=1305
 *
 *
 * @author bkinzyabulatov
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
// графа 14 - explanation               - Пояснение Таблица 1 (графа 4)
// графа 15 - difference                - Расхождение

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
@Field
def formDataCache = [:]

//Все аттрибуты
@Field
def allColumns = ['incomeTypeId', 'incomeGroup', 'incomeTypeByOperation', 'accountNo',
        'rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'explanation', 'difference']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def calcColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'explanation', 'difference']

@Field
def controlColumns = ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'explanation', 'difference']

@Field
def totalColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def head1Alias = 'R1'
@Field
def first1Alias = 'R2'
@Field
last1Alias = 'R112'
@Field
total1Alias = 'R113'
@Field
head2Alias = 'R114'
@Field
first2Alias = 'R115'
@Field
last2Alias = 'R394'
@Field
total2Alias = 'R395'

@Field
def rowsNotCalc = [head1Alias, total1Alias, head2Alias, total2Alias]

@Field
def formTypeId_RNU6 = 318

@Field
def formTypeId_RNU4 = 316

@Field
def formTypeId_Tab1 = 851

@Field // Set важен
def rows5 = ([18] + (25..30) + [35, 78, 79, 89, 98, 111, 112] + (133..136) + [138, 139, 231, 232] + (375..377) + (387..390) + (392..394)) as Set

@Field // Set важен
def rows679 = ([18] + (25..30) + [35, 78, 79, 89, 98, 111, 112] + (133..136) + [138, 139] + (387..390) + (392..394)) as Set

@Field // Set важен
def rows8 = ((2..77) + (80..88) + (90..112) + (115..230) + (233..374) + (378..393)) as Set

@Field // Set важен
def rowsKnu = ((2..112) + (115..394)) as Set

@Field
def chRows = ['R231', 'R232', 'R375', 'R376', 'R377']

@Field
def editableStyle = 'Редактируемая'

@Field
def startDate = null

@Field
def endDate = null

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

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

    def rows679Aliases = rows679.collect { "R$it" as String }
    // для графы 12
    def map12 = [:]
    dataRows.each { row ->
        if (!(row.getAlias() in rowsNotCalc)) {
            if (map12[row.accountNo] == null) {
                map12[row.accountNo] = BigDecimal.ZERO
            }
            map12[row.accountNo] += (row.rnu4Field5Accepted ?: 0)
        }
    }
    // Лог. проверка
    dataRows.each { row ->
        if (rows679Aliases.contains(row.getAlias())) {
            def BigDecimal summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        // Графа 11
        if (!rowsNotCalc.contains(row.getAlias())) {
            row.opuSumByEnclosure2 = BigDecimal.ZERO
        }

        // Графа 12
        if (!(row.getAlias() in rowsNotCalc)) {
            row.opuSumByTableD = map12[row.accountNo]
        }

        // Графа 13
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            if (row.accountNo && row.accountingRecords) {
                row.opuSumTotal = BigDecimal.ZERO
                def income102Records = getIncome102Data(row)
                for (income102 in income102Records) {
                    row.opuSumTotal += income102.TOTAL_SUM.numberValue
                }
            } else {
                row.opuSumTotal = null
            }
        }

        if (row.getAlias() in chRows) {
            row.opuSumTotal = BigDecimal.ZERO
            def income101Records = getIncome101Data(row)
            for (income101 in income101Records) {
                row.opuSumTotal += income101.DEBET_RATE.numberValue
            }
        }

        // Графа 15
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            if (row.opuSumByTableD != null && row.opuSumTotal != null) {
                row.difference = (row.opuSumByEnclosure2 ?: 0) + (row.opuSumByTableD ?: 0) - (row.opuSumTotal ?: 0) - (row.explanation ?: 0)
            }
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
            if (row.accountNo && row.accountingRecords) {
                def income102Records = getIncome102Data(row)
                if (!income102Records || income102Records.isEmpty()) {
                    rowIndexes102 += row.getIndex()
                }
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
    calcExplanation(dataRows, formSources, isFromSummary)
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
        calcColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row[alias] = 0
            }
        }
        controlColumns.each { alias ->
            row[alias] = null
        }
    }
    // получить данные из источников
    formSources.each { departmentFormType ->
        def child = getFormData(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def childData = formDataService.getDataRowHelper(child)

            for (def row : childData.allSaved) {
                if (row.getAlias() == null || row.getAlias().contains('total')) {
                    continue
                }
                def rowResult = dataRows.find { row.getAlias() == it.getAlias() }
                if (rowResult != null) {
                    for (alias in calcColumns) {
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
        calcColumns.each { alias ->
            if (row.getCell(alias)?.style?.alias == editableStyle || row.getAlias() in [total1Alias, total2Alias]) {
                row[alias] = 0
            }
        }
        controlColumns.each { alias ->
            row[alias] = null
        }
    }

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    // Предыдущий отчётный период
    def formDataOld = formDataService.getFormDataPrev(formData)
    if (formDataOld == null) {
        def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId());
        if (prevReportPeriod != null) {
            // Последний экземпляр
            formDataOld = getFormData(301, formData.getKind(), formData.getDepartmentId(), prevReportPeriod.getId(), null, formData.comparativePeriodId, formData.accruing);
        }
    }
    if (formDataOld != null && reportPeriod.order != 1) {
        def dataRowsOld = formDataService.getDataRowHelper(formDataOld)?.allSaved
        (rows5 + rows679).each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 5» +=«графа 5» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowOld = dataRowsOld.find { row.incomeTypeId == it.incomeTypeId && row.accountNo == it.accountNo }
            if (rowOld) {
                if (rows5.contains(rowNum)) {
                    row.rnu6Field10Sum = rowOld.rnu6Field10Sum
                }
                if (rows679.contains(rowNum)) {
                    //«графа 6» +=«графа 6» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
                    row.rnu6Field12Accepted = rowOld.rnu6Field12Accepted
                    //«графа 7» больше не берется из источников
                }
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
    def knuList = rowsKnu.collect {
        def row = getDataRow(dataRows, 'R' + it)
        return row.incomeTypeId
    }
    fillRecordsMap(knuList, getReportPeriodEndDate())

    // получить формы-источники в текущем налоговом периоде
    def strangeCodesRnu4 = [] as SortedSet<String>
    def strangeCodesRnu6 = [] as SortedSet<String>
    formSources.each {
        def child = getFormData(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
        if (child != null && child.state == WorkflowState.ACCEPTED) {
            def dataRowsChild = formDataService.getDataRowHelper(child).allSaved
            switch (child.formType.id) {
                // рну 6
                case formTypeId_RNU6:
                    fillFromRnu6(dataRows, dataRowsChild, strangeCodesRnu6)
                    break
                // рну 4
                case formTypeId_RNU4:
                    fillFromRnu4(dataRows, dataRowsChild, strangeCodesRnu4)
                    break
            }
        }
    }
    if (!strangeCodesRnu6.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodesRnu6.join(', '), formTypeService.get(formTypeId_RNU6).name)
    }
    if (!strangeCodesRnu4.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodesRnu4.join(', '), formTypeService.get(formTypeId_RNU4).name)
    }
}

void fillFromRnu6(def dataRows, def dataRowsChild, def strangeCodes) {
    def codeBalanceMap = [:]
    def codeMap = [:]
    dataRowsChild.each { rowRNU6 ->
        if (rowRNU6.getAlias() == null) {
            // для проверки кодов
            def map = getRefBookValue(28, rowRNU6.code)
            def codeKey = map.CODE.value
            def balanceKey = map.NUMBER.value?.replace('.', '')
            if (codeMap[codeKey] == null) {
                codeMap[codeKey] = []
            }
            if (codeMap[codeKey][balanceKey] == null) {
                codeMap[codeKey][balanceKey] = []
            }
            codeMap[codeKey].add(balanceKey)

            // ключ состоит из id записи кну и балансового счета
            String key = String.valueOf(rowRNU6.code)
            if (codeBalanceMap[key] == null) {
                codeBalanceMap[key] = ["sum5" : 0, "sum6" : 0]
            }
            //«графа 5» =  сумма значений по «графе 10» (столбец «Сумма дохода в налоговом учёте. Рубли») всех форм источников вида «(РНУ-6)
            codeBalanceMap[key].sum5 += (rowRNU6.taxAccountingRuble ?: 0)
            //«графа 6» =  сумма значений по «графе 12» (столбец «Сумма дохода в бухгалтерском учёте. Рубли») всех форм источников вида «(РНУ-6)
            codeBalanceMap[key].sum6 += (rowRNU6.ruble ?: 0)
            // графа 7 больше не берется из источников
        }
    }
    (rows5 + rows679).each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        def knu = row.incomeTypeId
        def balanceKey = row.accountNo.replace('.', "")
        // удаляем использованные коды
        codeMap.get(knu)?.remove(balanceKey)
        // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
        if (codeMap.get(knu) != null && !(codeMap.get(knu).isEmpty())) {
            strangeCodes.add(knu)
        }
        if (row.incomeTypeId != null && row.accountNo != null) {
            def recordId = getRecordId(knu, balanceKey, getReportPeriodEndDate())

            if (recordId != null) {
                def sums = codeBalanceMap[String.valueOf(recordId)]
                if (sums != null) {
                    if (rows5.contains(rowNum)) {
                        row.rnu6Field10Sum = (row.rnu6Field10Sum ?: 0) + (sums.sum5 ?: 0)
                    }
                    if (rows679.contains(rowNum)) {
                        row.rnu6Field12Accepted = (row.rnu6Field12Accepted ?: 0) + (sums.sum6 ?: 0)
                        // графа 7 больше не берется из источников
                    }
                }
            }
        }
    }
    codeMap.clear()
    codeBalanceMap.clear()
}

void fillFromRnu4(def dataRows, def dataRowsChild, def strangeCodes) {
    def codeMap = [:]
    dataRowsChild.each { rowRNU4 ->
        def map = getRefBookValue(28, rowRNU4.balance)
        def codeKey = map.CODE.value
        def balanceKey = map.NUMBER.value?.replace('.', '')
        if (codeMap[codeKey] == null) {
            codeMap[codeKey] = [:]
        }
        if (codeMap[codeKey][balanceKey] == null) {
            codeMap[codeKey][balanceKey] = []
        }
        codeMap[codeKey][balanceKey].add(rowRNU4)
    }
    rows8.each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        String knu = row.incomeTypeId
        String balanceKey = row.accountNo.replace('.', "")
        // получаем строки источника по КНУ и Балансовому счету
        Map balanceMap = codeMap.get(knu)
        // удаляем использованные строки источника
        balanceMap?.remove(balanceKey)
        // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
        if (balanceMap != null && !(balanceMap.isEmpty())) {
            strangeCodes.add(knu)
        }
        // считаем
        def rowsChild = balanceMap?.get(balanceKey) ?: []
        rowsChild.each{ rowChild ->
            row.rnu4Field5Accepted = (row.rnu4Field5Accepted ?: BigDecimal.ZERO) + rowChild.sum
        }
    }
}

void calcExplanation(def dataRows, def formSources, def isFromSummary) {
    def sourcesTab1 = formSources.findAll { it.formTypeId == formTypeId_Tab1 }
    def rowNumbers = []
    def formDataTab1
    if (sourcesTab1 != null && !sourcesTab1.isEmpty()) {
        for (sourceTab1 in sourcesTab1) {
            def tempFormData = formDataService.getLast(sourceTab1.formTypeId, sourceTab1.kind, sourceTab1.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            // один принятый(для ТБ) источник
            if (tempFormData != null && (isFromSummary || tempFormData.state == WorkflowState.ACCEPTED)) {
                formDataTab1 = tempFormData
                break
            }
        }
    }
    def codeMap = [:]
    if (formDataTab1 != null) {
        def dataRowHelper = formDataService.getDataRowHelper(formDataTab1)
        def sourceRows = dataRowHelper.allSaved
        sourceRows.each { row ->
            def map = getRefBookValue(28, row.code)
            def code = map.CODE?.value ?: ''
            def opu = map.OPU?.value ?: ''
            // одна строка с КНУ
            if (codeMap[code] == null) {
                codeMap[code] = [:]
            }
            if (codeMap[code][opu] == null) {
                codeMap[code][opu] = []
            }
            codeMap[code][opu].add(row)
        }
    }
    def strangeCodes = []
    for (row in dataRows) {
        if (!(row.getAlias() in rowsNotCalc)) {
            def knu = row.incomeTypeId
            def opuMap = codeMap.get(knu)
            def opuKey = row.accountingRecords
            def sourceRows = opuMap?.get(opuKey)
            row.explanation = BigDecimal.ZERO
            if (sourceRows != null && !(sourceRows.isEmpty())) {
                sourceRows.each { sourceRow ->
                    row.explanation += sourceRow.sum
                }
            } else {
                rowNumbers.add(row.getIndex())
            }
            // удаляем использованные строки источника
            opuMap?.remove(opuKey)
            // если остались строки источника с тем же кну, но с другим балансовым счетом, то выводим сообщение
            if (opuMap != null && !(opuMap.isEmpty())) {
                strangeCodes.add(knu)
            }
        }
    }
    if (!rowNumbers.isEmpty()) {
        logger.warn("Строка %s: Графа «%s» заполнена значением «0», т.к. не найдены строки по требуемым КНУ в форме-источнике «%s»!",
                rowNumbers.join(', '), getColumnName(dataRows[0], 'explanation'), formTypeService.get(formTypeId_Tab1).name)
    }
    if (!strangeCodes.isEmpty()) {
        logger.warn("По строкам с КНУ %s не совпадает номер балансового счета текущей формы и формы-источника «%s»!",
                strangeCodes.join(', '), formTypeService.get(formTypeId_Tab1).name)
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

    // формирование строк нф
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
            ([(headerRows[0][5]): 'РНУ-6 (графа 12) сумма']),
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
    calcColumns.each { alias ->
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
    totalColumns.each { alias ->
        colIndex++
        dataRow[alias] = parseNumber(normalize(values[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }
}