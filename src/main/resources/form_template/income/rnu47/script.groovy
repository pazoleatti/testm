package form_template.income.rnu47

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

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
        checkCreation()
        break
    case FormDataEvent.CHECK :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
            return
        }
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        def rnu46FormData = getRnu46FormData()
        if (rnu46FormData==null) {
            logger.error("Не найдены экземпляры РНУ-46 за текущий отчетный период!")
            return
        }
        def formDataOld = getFormDataOld()
        if (formDataOld == null && !isFirstPeriod()) {
            logger.error("Не найдены экземпляры РНУ-47 за прошлый отчетный период!!")
            return
        }
        calc()
        !hasError() && logicalCheck()
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
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        !hasError() && logicalCheck()
        break
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/**
 * Получить данные за формы "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»"
 */
def getRnu46FormData(){
    def formData46 = formDataService.find(342, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData46!=null) {
        return formDataService.getDataRowHelper(formData46)
    }
    return null
}

/**
 * расчет значений ячеек, заполняющихся автоматически
 */
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

/**
 * расчет строк 1-11
 */
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

/**
 * расчет строк 12-13
 */
def getTotalValues(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def group12 = ['R1', 'R2', 'R8', 'R9', 'R10']
    def group13 = ['R3', 'R4', 'R5', 'R6', 'R7']
    def value = [11: [:], 12: [:]]

    // расчет для строк 12-13
    dataRows.eachWithIndex{row, index->
        if (group12.contains(row.getAlias())) {
            value[11].sumCurrentPeriodTotal = (value[11].sumCurrentPeriodTotal?:new BigDecimal(0))+(row.sumCurrentPeriodTotal?:new BigDecimal(0))
            value[11].sumTaxPeriodTotal = (value[11].sumCurrentPeriodTotal?:new BigDecimal(0))+(row.sumCurrentPeriodTotal?:new BigDecimal(0))
        } else if (group13.contains(row.getAlias())) {
            value[12].sumCurrentPeriodTotal = (value[12].sumCurrentPeriodTotal?:new BigDecimal(0))+(row.sumCurrentPeriodTotal?:new BigDecimal(0))
            value[12].sumTaxPeriodTotal = (value[12].sumTaxPeriodTotal?:new BigDecimal(0))+(row.sumTaxPeriodTotal?:new BigDecimal(0))
        }
    }
    return value
}
/**
 * расчет столбцов 3-6 для строк 1-11
 */
def calc3_6(def rows, def group) {
    def value = [
                    sumCurrentPeriodTotal: 0,
                    sumTaxPeriodTotal: 0,
                    amortPeriod: 0,
                    amortTaxPeriod: 0
                ]
    rows.each{row ->
        if (refBookService.getNumberValue(71,row.amortGroup,'GROUP')==group) {
            value.sumCurrentPeriodTotal += row.cost10perMonth?:0
            value.sumTaxPeriodTotal += row.cost10perTaxPeriod?:0
            value.amortPeriod += row.amortMonth?:0
            value.amortTaxPeriod += row.amortTaxPeriod?:0
        }
    }
    return value
}

/**
 * логические проверки (таблица 149)
 */
void logicalCheck(){
    rnu46check()
    groupRowsCheck()
    totalRowCheck()
}

/**
 * Проверка итоговых значений по амортизационным группам
 */
boolean totalRowCheck() {
    boolean isValid = true

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalValues = getTotalValues()
    dataRows.eachWithIndex{row, index->
        if ( (index==11 || index==12) &&
             !(row.sumCurrentPeriodTotal==totalValues[index].sumCurrentPeriodTotal &&
              row.sumTaxPeriodTotal==totalValues[index].sumTaxPeriodTotal) ) isValid = false
    }
    if (! isValid) {
        logger.error('Итоговые значения рассчитаны неверно!')
    }

    return isValid
}

boolean rnu46check(){
    def hasData = false
    def groupList = 0..10
    for ( def row :rnu46FormData.allCached) {
        if (refBookService.getNumberValue(71,row.amortGroup,'GROUP') in groupList) {
            hasData = true
        }
    }
    if (!hasData) {
        logger.error("Отсутствуют данные РНУ-46!")
    }
    return hasData

}

/**
 *  Проверка суммы расходов в виде капитальных вложений с начала года
 *  Проверка суммы начисленной амортизации с начала года
 */
boolean groupRowsCheck() {
    boolean isValid = true
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def groupRowsAliases = ['R0', 'R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10']
    def formDataOld = getFormDataOld()
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null
    // значения для первых 11 строк
    def row1_11 = calcRows1_11()

    for (def row : dataRows) {
        if (row.getAlias() in groupRowsAliases) {
            if (checkRequiredColumns(row, allColumns)){
                return false
            }
        } else {
            continue
        }
        //2.		Проверка суммы расходов в виде капитальных вложений с начала года

        //2.1	графа 4 ? графа 3;
        final invalidCapitalForm = 'Неверная сумма расходов в виде капитальных вложений с начала года!'
        if (! (row.sumTaxPeriodTotal >= row.sumCurrentPeriodTotal)) {
            isValid = false
            logger.error(invalidCapitalForm)
        } else
        //2.2	графа 4 = графа 3 + графа 4 за предыдущий месяц;
        // TODO (если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
        if (row.sumCurrentPeriodTotal != null && !(row.sumTaxPeriodTotal == (row.sumCurrentPeriodTotal + getSumTaxPeriodTotalFromPreviousMonth(dataOld, row.getAlias())))) {
            isValid = false
            logger.error(invalidCapitalForm)
        } else
        //2.3	графа 4 = ?графа 3 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! (row.sumTaxPeriodTotal == getSumCurrentQuarterTotalForAllPeriods(row.getAlias())) )  {
            isValid = false
            logger.error(invalidCapitalForm)
        }

        //3.    Проверка суммы начисленной амортизации с начала года
        final invalidAmortSumms = 'Неверная сумма начисленной амортизации с начала года!'
        //3.1.	графа 6 ? графа 5
        if (! (row.amortTaxPeriod >= row.amortPeriod)) {
            isValid = false
            logger.error(invalidAmortSumms)
        } else
        //3.2   графа 6 = графа 5 + графа 6 за предыдущий месяц;
        //  (если текущий отчетный период – январь, то слагаемое «по графе 6 за предыдущий месяц» в формуле считается равным «0.00»)
        if (row.amortPeriod != null && !(row.amortTaxPeriod == (row.amortPeriod + getAmortTaxPeriodFromPreviousMonth(dataOld, row.getAlias()))) ) {
            isValid = false;
            logger.error(invalidAmortSumms)
        }
        //3.3   графа 6 = ?графа 5 за все месяцы текущего года, начиная с января и включая текущий отчетный период.
        if (! (row.amortTaxPeriod == getAmortQuarterForAllPeriods(row.getAlias())) ) {
            isValid = false
            logger.error(invalidAmortSumms)
        }

        def index = row.getIndex() - 1
        if (index<11) {
            row1_11[index].each{k,v->
                row[k] = v
            }
        }

    }

    return isValid
}

/**
 * Получить данные за предыдущий месяц
 *
 */
def FormData getFormDataOld() {
    if (isFirstPeriod()) {//TODO нужно брать за предыдущий месяц(если текущий отчетный период – январь, то слагаемое «по графе 4 за предыдущий месяц» в формуле считается равным «0.00»)
        return null
    }
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-47 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }
    return formDataOld
}

/**
 * Получить данные за определенный период
 *
 */
def FormData getFormDataPeriod(def reportPeriod) {
    def formDataPeriod = null
    if (reportPeriod != null) {
        formDataPeriod = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id)
    }
    return formDataPeriod
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
boolean checkRequiredColumns(def DataRow row, def ArrayList<String> columns) {
    def colNames = []
    columns.each {
        if (isBlankOrNull(row.getCell(it).getValue())) {
            def name = row.getCell(it).column.name
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("В строке ${row.getIndex()} не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * возвращает значение графы 4 за предыдущий месяц
 * @return
 */
def getSumTaxPeriodTotalFromPreviousMonth(def DataRowHelper dataRowHelper, def alias) {
    if (dataRowHelper != null) {
        def row = dataRowHelper.getDataRow(dataRowHelper.allCached, alias)
        if(row != null){
            return row.sumTaxPeriodTotal
        }
    }
    return 0
}


/**
 * возвращает значение графы 6 за предыдущий месяц
 * @return
 */
def getAmortTaxPeriodFromPreviousMonth(def DataRowHelper dataRowHelper, def alias) {
    if (dataRowHelper != null) {
        def row = dataRowHelper.getDataRow(dataRowHelper.allCached, alias)
        if(row != null){
            return row.amortTaxPeriod
        }
    }
    return 0
}

/**
 * возвращает значение графы 3 за все месяцы текущего года, включая текущий отчетный период
 * TODO актуализировать / добавить выдачу ошибок при отсутствии значений
 * @return
 */
def getSumCurrentQuarterTotalForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.sum{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? dataRowHelper.getDataRow(dataRowHelper.allCached, alias) : null
            return row!=null ? row.sumCurrentPeriodTotal : 0
        }
        return 0
    }
}

/**
 * возвращает значение графы 6 за все месяцы текущего года, включая текущий отчетный период
 * TODO актуализировать / добавить выдачу ошибок при отсутствии значений
 * @return
 */
def getAmortQuarterForAllPeriods(def alias) {
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def TaxPeriod taxPeriod = reportPeriod.taxPeriod
    def List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(taxPeriod.id)
    return periodList.sum{ period ->
        if (period.order < reportPeriod.order) {
            def formDataPeriod = getFormDataPeriod(period)
            def DataRowHelper dataRowHelper = formDataPeriod != null ? formDataService.getDataRowHelper(formDataPeriod) : null
            def row = dataRowHelper != null ? dataRowHelper.getDataRow(dataRowHelper.allCached, alias) : null
            return row!=null ? row.amortTaxPeriod : 0
        }
        return 0
    }
}
boolean isBlankOrNull(value) {
    value == null || value.equals('')
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

// TODO Проверка на первый месяц
boolean isFirstPeriod(){
    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    return reportPeriod.order == 1
}

def getAllColumns(){
    return ["amortGroup", "sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]
}

def getCalcColumns(){
    return ["sumCurrentPeriodTotal", "sumTaxPeriodTotal", "amortPeriod", "amortTaxPeriod"]
}

/**
 * Консолидация.
 */
void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // удалить все строки и собрать из источников их строки
    def rows = dataRowHelper.allCached
    rows.each{ row ->
        calcColumns.each{ column ->
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
        def addRow = getRowByAlias(addRows, row.getAlias())
        calcColumns.each{ column ->
            def value = row[column]
            row[column] = (value == null) ? addRow[column] : (value + (addRow[column]?:0))
        }
    }
}

def DataRow getRowByAlias(def rows, def alias){
    if (isBlankOrNull(alias)) {
        for (def row : rows){
            if (alias == row.getAlias()){
                return row
            }
        }
    }
    return null
}
