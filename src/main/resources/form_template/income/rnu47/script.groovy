package form_template.income.rnu47

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
 * @author vsergeev
 *
 * Графы:  *
 * 2    amortGroup               -   Амортизационные группы
 * 3    sumCurrentPeriodTotal    -   За отчётный месяц
 * 4    sumTaxPeriodTotal        -   С начала налогового периода
 * 5    amortPeriod              -   За отчётный месяц
 * 6    amortTaxPeriod           -   С начала налогового периода
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE :
        if (!isMonthBalance()) {
            def rnu46FormData = getRnu46DataRowHelper()
            if (rnu46FormData==null) {
                logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
                return
            }
            if (!formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id) && !isFirstPeriod()) {
                logger.error("Не найдены экземпляры РНУ-47 за прошлый отчетный период!!")
                return
            }
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK :
        if (!isMonthBalance()) {
            def rnu46FormData = getRnu46DataRowHelper()
            if (rnu46FormData==null) {
                logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
                return
            }
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

/** Признак периода ввода остатков. */
@Field
def isBalancePeriod

/* Получить данные за формы "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»" */
def getRnu46DataRowHelper(){
    def taxPeriodId = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.id
    def formData46 = formDataService.findMonth(342, formData.kind, formDataDepartment.id, taxPeriodId, formData.periodOrder)
    if (formData46!=null) {
        return formDataService.getDataRowHelper(formData46)
    }
    return null
}

/** Расчет значений ячеек, заполняющихся автоматически */
void calc(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (!isMonthBalance()) {
        // расчет для первых 11 строк
        def row1_11 = calcRows1_11()

        dataRows.eachWithIndex{row, index->
            if (index<11) {
                row1_11[index].each{k,v->
                    row[k] = v
                }
            }
        }
    }
    // расчет для строк 12-13
    def totalValues = getTotalValues(dataRows)
    dataRows.eachWithIndex{row, index->
        if (index==11 || index==12) {
            row.sumCurrentPeriodTotal = totalValues[index].sumCurrentPeriodTotal
            row.sumTaxPeriodTotal = totalValues[index].sumTaxPeriodTotal
        }
    }
    dataRowHelper.save(dataRows)
}

/** Расчет строк 1-11 */
def calcRows1_11(){
    def rnu46Rows = getRnu46DataRowHelper()?.allCached
    def groupList = 0..10
    def value = [:]
    groupList.each{group ->
        value[group] = calc3_6(rnu46Rows, group)
    }
    return value
}

/** Расчет строк 12-13 */
def getTotalValues(def dataRows){
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
        def amortGroup = refBookService.getNumberValue(71, row.amortGroup, 'GROUP')
        if (amortGroup != null && amortGroup==group) {
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
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (!isMonthBalance()) {
        def hasData = false
        def groupList = 0..10
        for (def row :rnu46DataRowHelper.allCached) {
            if (refBookService.getNumberValue(71,row.amortGroup,'GROUP').intValue() in groupList) {
                hasData = true
                break
            }
        }
        if (!hasData) {
            logger.error("Отсутствуют данные РНУ-46!")
        }

        def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
        //вынес сюда проверку на первый месяц
        def formDataOld = !isFirstPeriod() ? formDataService.getFormDataPrev(formData, formDataDepartment.id) : null
        def dataRowsOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null
        // значения для первых 11 строк
        def row1_11 = calcRows1_11()

        def startOld
        def endOld
        if (formDataOld?.periodOrder != null) {
            startOld = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formDataOld.periodOrder).time
            endOld = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formDataOld.periodOrder).time
        }
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
            def invalidCapitalForm = "Строка ${row.getIndex()}: Неверная сумма расходов в виде капитальных вложений с начала года!"
            if (row.sumTaxPeriodTotal != null && row.sumCurrentPeriodTotal != null) {
                if (row.sumTaxPeriodTotal < row.sumCurrentPeriodTotal) {
                    logger.error(invalidCapitalForm)
                } else
                //2.2	графа 4 = графа 3 + графа 4 за предыдущий месяц;
                // (если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
                if (row.sumTaxPeriodTotal != (row.sumCurrentPeriodTotal + getSumTaxPeriodTotalFromPreviousMonth(dataRowsOld, row.getAlias()))) {
                    invalidCapitalForm += " Экземпляр за период ${getDateString(startOld)} - ${getDateString(endOld)} не существует (отсутствуют первичные данные для расчёта)"
                    logger.error(invalidCapitalForm)
                } else
                //2.3	графа 4 = (сумма)графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.sumTaxPeriodTotal != getSumCurrentQuarterTotalForAllPeriods(row.getAlias()))  {
                    def periodOrderList = getSumCurrentQuarterInvalidPeriods(row.getAlias())
                    if (!periodOrderList.isEmpty()) {
                        invalidCapitalForm += " Экземпляр за периоды "
                        periodOrderList.eachWithIndex{ periodOrder, index ->
                            if(index != 0){
                                invalidCapitalForm += ", "
                            }
                            def start = reportPeriodService.getMonthStartDate(formData.reportPeriodId, periodOrder).time
                            def end = reportPeriodService.getMonthEndDate(formData.reportPeriodId, periodOrder).time
                            invalidCapitalForm += "${getDateString(start)} - ${getDateString(end)}"
                        }
                        invalidCapitalForm += " не существует (отсутствуют первичные данные для расчёта)"
                        logger.error(invalidCapitalForm)
                    }
                }
            }

            //3.    Проверка суммы начисленной амортизации с начала года
            def invalidAmortSumms = "Строка ${row.getIndex()}: Неверная сумма начисленной амортизации с начала года!"
            //3.1.	графа 6 ? графа 5
            if (row.amortTaxPeriod != null && row.amortPeriod != null) {
                if (row.amortTaxPeriod < row.amortPeriod) {
                    logger.error(invalidAmortSumms)
                } else
                //3.2   графа 6 = графа 5 + графа 6 за предыдущий месяц;
                //  (если текущий отчетный период – январь, то слагаемое «по графе 6 за предыдущий месяц» в формуле считается равным «0.00»)
                if (row.amortTaxPeriod != (row.amortPeriod + getAmortTaxPeriodFromPreviousMonth(dataRowsOld, row.getAlias()))) {
                    invalidAmortSumms += " Экземпляр за период ${getDateString(startOld)} - ${getDateString(endOld)} не существует (отсутствуют первичные данные для расчёта)"
                    logger.error(invalidAmortSumms)
                } else
                //3.3   графа 6 = (сумма)графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.amortTaxPeriod != getAmortQuarterForAllPeriods(row.getAlias())) {
                    def periodOrderList = getAmortQuarterInvalidPeriods(row.getAlias())
                    if (!periodOrderList.isEmpty()) {
                        invalidAmortSumms += " Экземпляр за периоды "
                        periodOrderList.eachWithIndex{ periodOrder, index ->
                            if(index != 0){
                                invalidAmortSumms += ", "
                            }
                            def start = reportPeriodService.getMonthStartDate(formData.reportPeriodId, periodOrder).time
                            def end = reportPeriodService.getMonthEndDate(formData.reportPeriodId, periodOrder).time
                            invalidAmortSumms += "${getDateString(start)} - ${getDateString(end)}"
                        }
                        invalidAmortSumms += " не существует (отсутствуют первичные данные для расчёта)"
                        logger.error(invalidAmortSumms)
                    }
                }
            }

            def index = row.getIndex() - 1
            if (index<11) {
                row1_11[index].each{k,v->
                    row[k] = v
                }
            }

        }
    }

    boolean isValid = true

    def totalValues = getTotalValues(dataRows)
    dataRows.eachWithIndex{row, index->
        if ( (index==11 || index==12) &&
                !(row.sumCurrentPeriodTotal==totalValues[index].sumCurrentPeriodTotal &&
                        row.sumTaxPeriodTotal==totalValues[index].sumTaxPeriodTotal) ) {
            isValid = false
        }
    }
    if (!isValid) {
        loggerError('Итоговые значения рассчитаны неверно!')
    }
}

/** Получить данные за определенный месяц */
def FormData getFormDataPeriod(def taxPeriod, def periodOrder) {
    if (taxPeriod != null) {
        return formDataService.findMonth(formData.formType.id, formData.kind, formDataDepartment.id, taxPeriod.id, periodOrder)
    }
}

/** Возвращает значение графы 4 за предыдущий месяц */
def getSumTaxPeriodTotalFromPreviousMonth(def dataRows, def alias) {
    if (dataRows != null) {
        def row = getDataRow(dataRows, alias)
        if(row != null){
            return row.sumTaxPeriodTotal
        }
    }
    return BigDecimal.ZERO
}

/** Возвращает значение графы 6 за предыдущий месяц */
def getAmortTaxPeriodFromPreviousMonth(def dataRows, def alias) {
    if (dataRows != null) {
        def row = getDataRow(dataRows, alias)
        if(row != null){
            return row.amortTaxPeriod
        }
    }
    return BigDecimal.ZERO
}

/** Возвращает значение графы 3 за все месяцы текущего года, включая текущий отчетный период */
def getSumCurrentQuarterTotalForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def sum = 0
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.sumCurrentPeriodTotal != null){
            sum += row?.sumCurrentPeriodTotal
        }
    }
    return sum
}

/** Возвращает периоды с некорректными данными для расчета графы 3 */
def getSumCurrentQuarterInvalidPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def periods = []
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.sumCurrentPeriodTotal == null){
            periods += periodOrder
        }
    }
    return periods
}

/** Возвращает значение графы 6 за все месяцы текущего года, включая текущий отчетный период */
def getAmortQuarterForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def sum = 0
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.amortTaxPeriod != null){
            sum += row?.amortTaxPeriod
        }
    }
    return sum
}

/** Возвращает периоды с некорректными данными для расчета графы 6 */
def getAmortQuarterInvalidPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def periods = []
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.amortTaxPeriod == null){
            periods += periodOrder
        }
    }
    return periods
}

boolean isFirstPeriod(){
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    return reportPeriod.order == 1 && formData.periodOrder == 1
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // удалить все строки и собрать из источников их строки
    def dataRows = dataRowHelper.allCached
    dataRows.each{ row ->
        arithmeticCheckAlias.each{ column ->
            row[column] = null
        }
    }
    def taxPeriodId = reportPeriodService.get(formData.reportPeriodId)?.taxPeriod?.id
    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.findMonth(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, taxPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                addRowsToRows(dataRows, sourceForm.allCached)
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalancePeriod == null) {
        // Отчётный период
        def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
        if (!reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId) || formData.periodOrder == null) {
            isBalancePeriod = false
        } else {
            isBalancePeriod = formData.periodOrder - 1 % 3 == 0
        }
    }
    return isBalancePeriod
}

def loggerError(def msg) {
    if (isBalancePeriod) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}