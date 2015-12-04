package form_template.vat.vat_operBank.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.FormType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям банка, справочно)
 *
 * formTemplateId=620
 *
 */

// графа 1  - code
// графа 2  - name
// графа    - fix1
// графа 3  - totalVAT
// графа    - fix2
// графа 4  - month1
// графа 5  - month2
// графа 6  - month3

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.IMPORT:
        // TODO (Ramil Timerbaev) загрузку из экселя сказали пока не делать
        break
}

// Проверяемые на пустые значения атрибуты (графа 2..6)
@Field
def nonEmptyColumns = ['name', 'totalVAT', 'month1', 'month2', 'month3']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 3..6)
@Field
def totalColumns = ['totalVAT', 'month1', 'month2', 'month3']
@Field
def totalNDSColumns = ['totalVAT', 'month1']

// алиас строки "Всего по Сбербанку"
@Field
def totalAlias = 'R16'

// алиас строки "Всего по Сбербанку (сумма НДС)"
@Field
def totalNDSAlias = 'R21'

// строки для строки "Всего по Сбербанку" (строки 1..16)
@Field
def totalRows = ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15']

// строки для строки "Всего по Сбербанку (сумма НДС)" (строки 18..20)
@Field
def totalNDSRows = ['R18', 'R19', 'R20']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
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
// Мапа для хранения номеров строк и id подразделений (номер строки -> идентификатор подразделения)
@Field
def departmentRowMap = [
        R1  : 4,
        R2  : 8,
        R3  : 20,
        R4  : 27,
        R5  : 32,
        R6  : 44,
        R7  : 52,
        R8  : 64,
        R9  : 82,
        R10 : 88,
        R11 : 97,
        R12 : 102,
        R13 : 109,
        R14 : 37,

        18 : 212,
        19 : 130,
        20 : -1 // TODO
]


void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row in dataRows) {
        if (row.getAlias() in [totalTBAlias, totalSBAlias]) {
            continue
        }
        def index = row.getIndex()

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 3. Арифметическая проверка «Графы 3»
        def needValue = [:]
        needValue['totalVAT'] = calc3(row)
        def arithmeticCheckAlias = needValue.keySet().asList()
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }

    // 2. Проверка итоговых значений
    // итоги по ТБ
    checkTotal(dataRows, totalTBAlias, totalRows)
    // итоги по СБ
    checkTotal(dataRows, totalSBAlias, totalNDSRows)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        if (row.getAlias() in ['R16', 'R17', 'R21']) {
            continue
        }
        if (row.getAlias() in totalRows) {
            row.totalVAT = calc3(row)
            row.month1 = calc4(row)
            row.month2 = calc56(row)
            row.month3 = calc56(row)
        }
        if (row.getAlias() in totalNDSRows) {
            row.totalVAT = calc3NDS(row)
            row.month1 = calc4NDS(row)
        }
    }

    // итоги
    calcTotal(dataRows, totalTBAlias, totalRows)
    // итоги с НДС
    calcTotal(dataRows, totalSBAlias, totalNDSRows)
}

def calc3(def row) {
    def departmentId = departmentRowMap.get(row.getAlias())
    def dataRows724_1 = getDataRows(600, FormDataKind.PRIMARY, departmentId) ?: []
    def dataRows724_4 = getDataRows(603, FormDataKind.PRIMARY, departmentId) ?: []
    // TODO id фрмы 724_1_1
    def dataRows724_1_1 = getDataRows(-1, FormDataKind.PRIMARY, departmentId) ?: []

    def a = (getDataRow(dataRows724_1, 'total')?.ndsSum ?: 0) + (getDataRow(dataRows724_1, 'total_7')?.ndsBookSum ?: 0) - (getDataRow(dataRows724_1, 'total_7')?.ndsDealSum ?: 0)
    def b = getDataRow(dataRows724_4, 'sum2')?.ndsSum ?: 0
    // TODO алиасы строк и граф
    def c = (getDataRow(dataRows724_1_1, 'todo')?.todo ?: 0) + (getDataRow(dataRows724_1_1, 'todo')?.todo ?: 0)

    return a - b + c
}

def calc4(def row) {
    return (row.totalVAT ?: 0) - (row.month2 ?: 0) - (row.month3 ?: 0)
}

def calc56(def row) {
    return (row.totalVAT ?: 0)/3
}
def calc3NDS(def row) {
    def departmentId = departmentRowMap.get(row.getAlias())
    def dataRows724_1 = getDataRows(600, FormDataKind.PRIMARY, departmentId) ?: []
    def dataRows724_4 = getDataRows(603, FormDataKind.PRIMARY, departmentId) ?: []
    // TODO id фрмы 724_1_1
    def dataRows724_1_1 = getDataRows(-1, FormDataKind.PRIMARY, departmentId) ?: []

    def a = (getDataRow(dataRows724_1, 'total_1')?.ndsSum ?: 0) +
            (getDataRow(dataRows724_1, 'total_7')?.ndsBookSum ?: 0) -
            (getDataRow(dataRows724_1, 'total_7')?.ndsDealSum ?: 0)
    def b = getDataRow(dataRows724_4, 'sum2')?.ndsSum ?: 0
    // TODO алиасы строк и граф
    def c = (getDataRow(dataRows724_1_1, 'todo')?.todo ?: 0) + (getDataRow(dataRows724_1_1, 'todo')?.todo ?: 0)

    return a - b + c
}

def calc4NDS(def row) {
    return (row.totalVAT ?: 0) - (row.month2 ?: 0) - (row.month3 ?: 0)
}

/**
 * Посчитать итоги.
 *
 * @param dataRows строки формы
 * @param totalAlias алиас итоговой строки
 * @param totalRows алиасы строк для подсчета итогов
 */
void calcTotal(def dataRows, def totalAlias, def totalRows) {
    def totalRow = getDataRow(dataRows, totalAlias)
    def tmpRows = dataRows.findAll { row -> row.getAlias() in totalRows }
    // TODO (Ramil Timerbaev) общий метод для итогов не походит, потому что у всех строк есть alias
    calcTotalSum(tmpRows, totalRow, totalColumns)
}

/**
 * Проверить итоги.
 *
 * @param dataRows строки формы
 * @param totalAlias алиас итоговой строки
 * @param totalRows алиасы строк для подсчета итогов
 */
void checkTotal(def dataRows, def totalAlias, def totalRows) {
    def totalRow = getDataRow(dataRows, totalAlias)
    def tmpRows = dataRows.findAll { row -> row.getAlias() in totalRows }
    tmpRows.add(totalRow)
    // TODO (Ramil Timerbaev) общий метод для проверки итогов не походит, потому что у всех строк есть alias
    checkTotalSum(tmpRows, totalColumns, logger, true)
}

void consolidation() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                // TODO (Ramil Timerbaev) консолидация не описана
            }
        }
    }
}

// Округляет число до требуемой точности
def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

// Получить строки формы
def getDataRows(def formId, def kind, def departmentId) {
    def reportPeriodId = formData.reportPeriodId
    def periodOrder = formData.periodOrder
    def sourceFormData = formDataService.getLast(formId, kind, departmentId, reportPeriodId, periodOrder, formData.comparativePeriodId, formData.accruing)
    if (sourceFormData != null && sourceFormData.id != null)
        return formDataService.getDataRowHelper(sourceFormData)?.allSaved
    return null
}