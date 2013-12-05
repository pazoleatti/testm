package form_template.income.rnu47

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException

import java.math.RoundingMode
import java.text.SimpleDateFormat
import groovy.transform.Field

/**
 * Форма "(РНУ-47) Регистр налогового учёта «ведомость начисленной амортизации по основным средствам,
 * а также расходов в виде капитальных вложений»"
 * formTemplateId=344
 *
 *
 * @author vsergeev
 *
 * Графы:
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
            if (!formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id) && formData.periodOrder != 1) {
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
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
}

//// Кэши и константы

// Все аттрибуты
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
            value[11].sumCurrentPeriodTotal = round((value[11].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO))
            value[11].sumTaxPeriodTotal = round((value[11].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO))
        } else if (group13.contains(row.getAlias())) {
            value[12].sumCurrentPeriodTotal = round((value[12].sumCurrentPeriodTotal?:BigDecimal.ZERO)+(row.sumCurrentPeriodTotal?:BigDecimal.ZERO))
            value[12].sumTaxPeriodTotal = round((value[12].sumTaxPeriodTotal?:BigDecimal.ZERO)+(row.sumTaxPeriodTotal?:BigDecimal.ZERO))
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
            value.sumCurrentPeriodTotal += round(row.cost10perMonth?:BigDecimal.ZERO)
            value.sumTaxPeriodTotal += round(row.cost10perTaxPeriod?:BigDecimal.ZERO)
            value.amortPeriod += round(row.amortMonth?:BigDecimal.ZERO)
            value.amortTaxPeriod += round(row.amortTaxPeriod?:BigDecimal.ZERO)
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
        def formDataOld = formData.periodOrder != 1 ? formDataService.getFormDataPrev(formData, formDataDepartment.id) : null
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
                if (row.sumTaxPeriodTotal != (row.sumCurrentPeriodTotal + getFieldFromPreviousMonth(dataRowsOld, row.getAlias(), "sumTaxPeriodTotal"))) {
                    invalidCapitalForm += " Экземпляр за период ${getDateString(startOld)} - ${getDateString(endOld)} не существует (отсутствуют первичные данные для расчёта)"
                    logger.error(invalidCapitalForm)
                } else
                //2.3	графа 4 = (сумма)графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.sumTaxPeriodTotal != getFieldSumForAllPeriods(row.getAlias(), "sumCurrentPeriodTotal"))  {
                    def periodOrderList = getFieldInvalidPeriods(row.getAlias(), "sumCurrentPeriodTotal")
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
                if (row.amortTaxPeriod != (row.amortPeriod + getFieldFromPreviousMonth(dataRowsOld, row.getAlias(), "amortTaxPeriod"))) {
                    invalidAmortSumms += " Экземпляр за период ${getDateString(startOld)} - ${getDateString(endOld)} не существует (отсутствуют первичные данные для расчёта)"
                    logger.error(invalidAmortSumms)
                } else
                //3.3   графа 6 = (сумма)графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
                if (row.amortTaxPeriod != getFieldSumForAllPeriods(row.getAlias(), "amortPeriod")) {
                    def periodOrderList = getFieldInvalidPeriods(row.getAlias(), "amortPeriod")
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

    def totalValues = getTotalValues(dataRows)
    for (row in dataRows) {
        def index = dataRows.indexOf(row)
        if ((index == 11 || index == 12) &&
                !(row.sumCurrentPeriodTotal == totalValues[index].sumCurrentPeriodTotal &&
                        row.sumTaxPeriodTotal == totalValues[index].sumTaxPeriodTotal)) {
            loggerError('Итоговые значения рассчитаны неверно!')
            break
        }
    }
}

/** Получить данные за определенный месяц */
def FormData getFormDataPeriod(def taxPeriod, def periodOrder) {
    if (taxPeriod != null && periodOrder != null) {
        return formDataService.findMonth(formData.formType.id, formData.kind, formDataDepartment.id, taxPeriod.id, periodOrder)
    }
}

/** Возвращает значение графы 4 или 6 за предыдущий месяц */
def getFieldFromPreviousMonth(def dataRows, def alias, def field) {
    if (dataRows != null) {
        def row = getDataRow(dataRows, alias)
        if(row != null){
            return row[field]
        }
    }
    return BigDecimal.ZERO
}

/** Возвращает сумму значений графы (3 или 5) за все месяцы текущего года, включая текущий отчетный период */
def getFieldSumForAllPeriods(def alias, def field) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def sum = 0
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def DataRow row = dataRows != null ? getDataRow(dataRows, alias) : null
        def value = row?.getCell(field)?.getValue()
        if (value != null){
            sum += value
        }
    }
    return sum
}

/** Возвращает периоды с некорректными данными для расчета графы 4 или 6. field - графа 3 или 5*/
def getFieldInvalidPeriods(def alias, def field) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def periods = []
    for (def periodOrder = 1; periodOrder<=formData.periodOrder; periodOrder++){
        def formDataPeriod = getFormDataPeriod(taxPeriod, periodOrder)
        def dataRows = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod)?.allCached : null
        def DataRow row = dataRows != null ? getDataRow(dataRows, alias) : null
        if (row?.getCell(field)?.getValue() == null){
            periods += periodOrder
        }
    }
    return periods
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
        for (def dataRow : addRows){
            if (row.getAlias() == dataRow.getAlias()){
                addRow = dataRow
                break
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

BigDecimal round(BigDecimal value, int newScale = 2) {
    return value?.setScale(newScale, RoundingMode.HALF_UP)
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalancePeriod == null) {
        if (!reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId) || formData.periodOrder == null) {
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