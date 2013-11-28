package form_template.income.rnu47

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper
import groovy.transform.Field

import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils.*

/**
 * Скрипт для РНУ-47 (rnu47.groovy).
 * Форма "(РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам,
 * а также расходов в виде капитальных вложений»".
 *
 * Версия ЧТЗ: 57
 * Вопросы аналитикам по ЧТЗ: http://jira.aplana.com/browse/SBRFACCTAX-2383
 *
 * TODO:
 *      -   не доделаны проверки 2, 3
 *
 * @author vsergeev
 *
 * Графы:  *
 * 2    amortGroup               -   Амортизационные группы
 * 3    sumCurrentPeriodTotal    -   За отчётный месяц
 * 4    sumTaxPeriodTotal        -   С начала налогового периода
 * 5    amortPeriod              -   За отчётный месяц
 * 6    amortTaxPeriod           -   С начала налогового периода
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
            return
        }
        if (!formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id) && !isFirstPeriod()) {
            logger.error("Не найдены экземпляры РНУ-47 за прошлый отчетный период!!")
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
            return
        }
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        break
    case FormDataEvent.DELETE_ROW :
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        logicCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

//Все аттрибуты
@Field
def allColumns = ["amortGroup", "sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]

// Автозаполняемые атрибуты
@Field
def arithmeticCheckAlias = ["sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]

@Field
def dateFormat = new SimpleDateFormat("dd.MM.yyyy")

/* Получить данные за формы "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»" */
def getRnu46FormData(){
    def formData46 = formDataService.find(342, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData46!=null) {
        return formDataService.getDataRowHelper(formData46)
    }
    return null
}

/** расчет значений ячеек, заполняющихся автоматически */
void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // расчет для первых 11 строк
    def row1_11 = calcRows1_11()

    dataRows.eachWithIndex{row, index->
        if (index<11) {
            row1_11[index].each{k,v->
                row[k] = v
            }
        }
    }
    // расчет для строк 12-13
    def totalValues = getTotalValues()
    dataRows.eachWithIndex{row, index->
        if (index==11 || index==12) {
            row.sumCurrentPeriodTotal = totalValues[index].sumCurrentPeriodTotal
            row.sumTaxPeriodTotal = totalValues[index].sumTaxPeriodTotal
        }
    }

    dataRowHelper.save(dataRows)
}

/** расчет строк 1-11 */
def calcRows1_11(){
    def rnu46FormData = getRnu46FormData()
    def rnu46Rows = rnu46FormData.allCached
    def groupList = 0..10
    def value = [:]
    groupList.each{group ->
        value[group] = calc3_6(rnu46Rows, group)
    }
    return value
}

/** расчет строк 12-13 */
def getTotalValues(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def group12 = ['R1', 'R2', 'R8', 'R9', 'R10']
    def group13 = ['R3', 'R4', 'R5', 'R6', 'R7']
    def value = [11: [:], 12: [:]]

    // расчет для строк 12-13
    dataRows.each{row->
        if (group12.contains(row.getAlias())) {
            value[11].sumCurrentPeriodTotal = roundTo((value[11].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO), 2)
            value[11].sumTaxPeriodTotal = roundTo((value[11].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO), 2)
        } else if (group13.contains(row.getAlias())) {
            value[12].sumCurrentPeriodTotal = roundTo((value[12].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO), 2)
            value[12].sumTaxPeriodTotal = roundTo((value[12].sumTaxPeriodTotal?:BigDecimal.ZERO)+(row.sumTaxPeriodTotal?:BigDecimal.ZERO), 2)
        }
    }
    return value
}
/** Расчет столбцов 3-6 для строк 1-11 */
def calc3_6(def rows, def group) {
    def value = [
                    sumCurrentPeriodTotal: BigDecimal.ZERO,
                    sumTaxPeriodTotal: BigDecimal.ZERO,
                    amortPeriod: BigDecimal.ZERO,
                    amortTaxPeriod: BigDecimal.ZERO
                ]
    rows.each{row ->
        if (refBookService.getNumberValue(71,row.amortGroup,'GROUP').intValue()==group) {
            value.sumCurrentPeriodTotal += roundTo(row.cost10perMonth?:BigDecimal.ZERO, 2)
            value.sumTaxPeriodTotal += roundTo(row.cost10perTaxPeriod?:BigDecimal.ZERO, 2)
            value.amortPeriod += roundTo(row.amortMonth?:BigDecimal.ZERO, 2)
            value.amortTaxPeriod += roundTo(row.amortTaxPeriod?:BigDecimal.ZERO, 2)
        }
    }
    return value
}

/** Логические проверки (таблица 149) */
void logicCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def hasData = false
    def groupList = 0..10
    for (def row :rnu46FormData.allCached) {
        if (refBookService.getNumberValue(71,row.amortGroup,'GROUP').intValue() in groupList) {
            hasData = true
            break
        }
    }
    if (!hasData) {
        logger.error("Отсутствуют данные РНУ-46!")
    }

    def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
    def formDataOld = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null
    // значения для первых 11 строк
    def row1_11 = calcRows1_11()

    for (def row : dataRows) {
        if (row.getAlias() in groupRowsAliases) {
            // Проверка на заполнение поля
            def index = row.getIndex()
            checkNonEmptyColumns(row, index, allColumns, logger, true)
        } else {
            continue
        }
        //2.		Проверка суммы расходов в виде капитальных вложений с начала года

        //2.1	графа 4 ? графа 3;
        def invalidCapitalForm = 'Неверная сумма расходов в виде капитальных вложений с начала года!'
        if (! (row.sumTaxPeriodTotal >= row.sumCurrentPeriodTotal)) {
            logger.error(invalidCapitalForm)
        } else
        //2.2	графа 4 = графа 3 + графа 4 за предыдущий месяц;
        // TODO (если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
        if (row.sumCurrentPeriodTotal != null && !(row.sumTaxPeriodTotal == (row.sumCurrentPeriodTotal + getSumTaxPeriodTotalFromPreviousMonth(dataOld, row.getAlias())))) {
            def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
            def start = reportPeriodService.getStartDate(reportPeriodOld.id).getTime()
            def end = reportPeriodService.getEndDate(reportPeriodOld.id).getTime()
            invalidCapitalForm += " Экземпляр за период ${getDateString(start)} - ${getDateString(end)} не существует (отсутствуют первичные данные для расчёта)"
            logger.error(invalidCapitalForm)
        } else
        //2.3	графа 4 = ?графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! (row.sumTaxPeriodTotal == getSumCurrentQuarterTotalForAllPeriods(row.getAlias())) )  {
            def reportPeriodList = getSumCurrentQuarterInvalidPeriods(row.getAlias())
            if (!reportPeriodList.isEmpty()) {
                invalidCapitalForm += " Экземпляр за периоды "
                reportPeriodList.eachWithIndex{ reportPeriodOld, index ->
                    if(index != 0){
                        invalidCapitalForm += ", "
                    }
                    def start = reportPeriodService.getStartDate(reportPeriodOld.id).getTime()
                    def end = reportPeriodService.getEndDate(reportPeriodOld.id).getTime()
                    invalidCapitalForm += "${getDateString(start)} - ${getDateString(end)}"
                }
                invalidCapitalForm += " не существует (отсутствуют первичные данные для расчёта)"
                logger.error(invalidCapitalForm)
            }
        }

        //3.    Проверка суммы начисленной амортизации с начала года
        final invalidAmortSumms = 'Неверная сумма начисленной амортизации с начала года!'
        //3.1.	графа 6 ? графа 5
        if (! (row.amortTaxPeriod >= row.amortPeriod)) {
            logger.error(invalidAmortSumms)
        } else
        //3.2   графа 6 = графа 5 + графа 6 за предыдущий месяц;
        //  (если текущий отчетный период – январь, то слагаемое «по графе 6 за предыдущий месяц» в формуле считается равным «0.00»)
        if (row.amortPeriod != null && !(row.amortTaxPeriod == (row.amortPeriod + getAmortTaxPeriodFromPreviousMonth(dataOld, row.getAlias()))) ) {
            def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
            def start = reportPeriodService.getStartDate(reportPeriodOld.id).getTime()
            def end = reportPeriodService.getEndDate(reportPeriodOld.id).getTime()
            invalidAmortSumms += " Экземпляр за период ${getDateString(start)} - ${getDateString(end)} не существует (отсутствуют первичные данные для расчёта)"
            logger.error(invalidAmortSumms)
        }
        //3.3   графа 6 = ?графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! (row.amortTaxPeriod == getAmortQuarterForAllPeriods(row.getAlias())) ) {
            def reportPeriodList = getAmortQuarterInvalidPeriods(row.getAlias())
            if (!reportPeriodList.isEmpty()) {
                invalidAmortSumms += " Экземпляр за периоды "
                reportPeriodList.eachWithIndex{ reportPeriodOld, index ->
                    if(index != 0){
                        invalidAmortSumms += ", "
                    }
                    def start = reportPeriodService.getStartDate(reportPeriodOld.id).getTime()
                    def end = reportPeriodService.getEndDate(reportPeriodOld.id).getTime()
                    invalidAmortSumms += "${getDateString(start)} - ${getDateString(end)}"
                }
                invalidAmortSumms += " не существует (отсутствуют первичные данные для расчёта)"
                logger.error(invalidAmortSumms)
            }
        }

        def index = row.getIndex() - 1
        if (index<11) {
            row1_11[index].each{k,v->
                row[k] = v
            }
        }

    }

    boolean isValid = true

    def totalValues = getTotalValues()
    dataRows.eachWithIndex{row, index->
        if ( (index==11 || index==12) &&
                !(row.sumCurrentPeriodTotal==totalValues[index].sumCurrentPeriodTotal &&
                        row.sumTaxPeriodTotal==totalValues[index].sumTaxPeriodTotal) ) isValid = false
    }
    if (!isValid) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }
}

/** Получить данные за определенный период */
def FormData getFormDataPeriod(def reportPeriod) {
    def formDataPeriod = null
    if (reportPeriod != null) {
        formDataPeriod = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id)
    }
    return formDataPeriod
}

/** Возвращает значение графы 4 за предыдущий месяц */
def getSumTaxPeriodTotalFromPreviousMonth(def DataRowHelper dataRowHelper, def alias) {
    if (dataRowHelper != null) {
        def row = getDataRow(dataRowHelper.allCached, alias)
        if(row != null){
            return row.sumTaxPeriodTotal
        }
    }
    return BigDecimal.ZERO
}

/** Возвращает значение графы 6 за предыдущий месяц */
def getAmortTaxPeriodFromPreviousMonth(def DataRowHelper dataRowHelper, def alias) {
    if (dataRowHelper != null) {
        def row = getDataRow(dataRowHelper.allCached, alias)
        if(row != null){
            return row.amortTaxPeriod
        }
    }
    return BigDecimal.ZERO
}

/**
 * возвращает значение графы 3 за все месяцы текущего года, включая текущий отчетный период
 * TODO актуализировать / добавить выдачу ошибок при отсутствии значений
 */
def getSumCurrentQuarterTotalForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.sum{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? getDataRow(dataRowHelper.allCached, alias) : null
            return row!=null ? row.sumCurrentPeriodTotal : BigDecimal.ZERO
        }
        return BigDecimal.ZERO
    }
}

/** Возвращает периоды с некорректными данными для расчета графы 3 */
def getSumCurrentQuarterInvalidPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.findAll{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? getDataRow(dataRowHelper.allCached, alias) : null
            return row==null
        }
        return false
    }
}

/**
 * возвращает значение графы 6 за все месяцы текущего года, включая текущий отчетный период
 * TODO актуализировать / добавить выдачу ошибок при отсутствии значений
 */
def getAmortQuarterForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.sum{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? getDataRow(dataRowHelper.allCached, alias) : null
            return row!=null ? row.amortTaxPeriod : BigDecimal.ZERO
        }
        return BigDecimal.ZERO
    }
}

/** Возвращает периоды с некорректными данными для расчета графы 6 */
def getAmortQuarterInvalidPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.findAll{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? getDataRow(dataRowHelper.allCached, alias) : null
            return row==null
        }
        return false
    }
}

// TODO Проверка на первый месяц
boolean isFirstPeriod(){
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    return reportPeriod.order == 1
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // удалить все строки и собрать из источников их строки
    def rows = dataRowHelper.allCached
    rows.each{ row ->
        arithmeticCheckAlias.each{ column ->
            row[column] = null
        }
    }

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                addRowsToRows(rows, sourceForm.allCached)
            }
        }
    }
    dataRowHelper.save(rows)
}

void addRowsToRows(def rows, def addRows){
    rows.each{ row ->
        def addRow = null
        if (row.getAlias() == null || row.getAlias().equals('')) {
            for (def dataRow : addRows){
                if (row.getAlias() == dataRow.getAlias()){
                    addRow = dataRow
                    break
                }
            }
        }
        arithmeticCheckAlias.each{ column ->
            def value = row[column]
            row[column] = (value == null) ? addRow[column] : (value + (addRow[column]?:BigDecimal.ZERO))
        }
    }
}

def String getDateString(Date date){
    return dateFormat.format(date)
}

BigDecimal roundTo(BigDecimal value, int newScale) {
    return value?.setScale(newScale, BigDecimal.ROUND_HALF_UP)
}
