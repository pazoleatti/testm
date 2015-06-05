package form_template.vat.vat_operBank.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Разнарядка на безакцептное списание/зачисление по суммам НДС с территориальных банков, Московского банка и подразделений ЦА (по операциям банка).
 *
 * formTemplateId=620
 *
 * TODO:
 *      - общий метод для итогов/проверки итогов не походит, потому что у всех строк есть alias
 *      - консолидация не описана
 *      - загрузку из экселя сказали пока не делать
 */

// графа 1  - code
// графа 2  - name
// графа 3  - totalVAT
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
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        // TODO (Ramil Timerbaev) загрузку из экселя сказали пока не делать
        break
    case FormDataEvent.SORT_ROWS:
        // сортировки нет, строки формы фиксированные
        break
}

// Проверяемые на пустые значения атрибуты (графа 2..6)
@Field
def nonEmptyColumns = ['name', 'totalVAT', 'month1', 'month2', 'month3']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 3..6)
@Field
def totalColumns = ['totalVAT', 'month1', 'month2', 'month3']

// алиас строки "ВСЕГО ПО ТБ"
@Field
def totalTBAlias = 'R17'

// алиас строки "Всего по Сбербанку"
@Field
def totalSBAlias = 'R20'

// строки для строки "ВСЕГО ПО ТБ" (строки 1..16)
@Field
def totalTBRows = ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15', 'R16']

// строки для строки "Всего по Сбербанку" (строки 17..19)
@Field
def totalSBRows = ['R17', 'R18', 'R19']

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
    checkTotal(dataRows, totalTBAlias, totalTBRows)
    // итоги по СБ
    checkTotal(dataRows, totalSBAlias, totalSBRows)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (def row : dataRows) {
        // пропустить итоговые строки
        if (row.getAlias() in [totalTBAlias, totalSBAlias]) {
            continue
        }
        // графа 3
        row.totalVAT = calc3(row)
    }

    // итоги по ТБ
    calcTotal(dataRows, totalTBAlias, totalTBRows)
    // итоги по СБ
    calcTotal(dataRows, totalSBAlias, totalSBRows)
}

def calc3(def row) {
    if (row.month1 == null || row.month2 == null || row.month3 == null) {
        return null
    }
    return roundValue(row.month1 + row.month2 + row.month3)
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
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
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