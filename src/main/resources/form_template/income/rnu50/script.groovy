package form_template.income.rnu50

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-50) Регистр налогового учёта «ведомость понесённых убытков от реализации амортизируемого имущества»"
 * formTemplateId=365
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа 2  - rnu49rowNumber
// графа 3  - invNumber
// графа 4  - lossReportPeriod
// графа 5  - lossTaxPeriod

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        if (formData.kind == FormDataKind.CONSOLIDATED) {
            // если форма консолидированная то не надо брать данные из рну 49
            consolidationFrom49()
        }
        calc()
        logicCheck()
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
        break
}

// Все атрибуты
@Field
def allColumns = ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['rnu49rowNumber', 'invNumber']

// Проверяемые на пустые значения атрибуты (графа 1..5)
@Field
def nonEmptyColumns = ['rowNumber', 'rnu49rowNumber', 'invNumber', 'lossReportPeriod', 'lossTaxPeriod']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4, 5)
@Field
def totalColumns = ['lossReportPeriod', 'lossTaxPeriod']

// Дата начала отчетного периода
@Field
def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    // сортировка
    sortRows(dataRows, groupColumns)

    // итоги
    def totalRow = getTotalRow(dataRows)
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }

    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    // 4. Проверки существования необходимых экземпляров форм
    if (getDataRowsRNU49() == null){
        logger.error('Отсутствуют данные РНУ-49!')
        return
    }
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    if (dataRows.isEmpty()) {
        return
    }
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        // 1. Обязательность заполнения полей (графа 1..5)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на нулевые значения
        if (row.lossReportPeriod == 0 && row.lossTaxPeriod == 0) {
            logger.error("Строка $index: Все суммы по операции нулевые!")
        }

        // 3. Проверка формата номера записи в РНУ-49 (графа 2)
        if (!row.rnu49rowNumber.matches('\\w{2}-\\w{6}')) {
            logger.error("Строка $index: Неправильно указан номер записи в РНУ-49 (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!")
        }
    }

    // 5. Проверка итоговых значений формы	Заполняется автоматически
    checkTotalSum(dataRows, totalColumns, logger, true)
}

void consolidation () {
    if (formData.kind == FormDataKind.CONSOLIDATED) {
        // если форма консолидированная то брать данные из рну 50 (обычная консолидация)
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
    } else {
        // если форма не консолидированная (первичная) то брать данные из рну 49
        consolidationFrom49()
        logger.info('Формирование первичной формы РНУ-50 прошло успешно.')
    }
    calc()
    logicCheck()
}

void consolidationFrom49() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить все строки
    dataRows.clear()
    def dataRows49 = getDataRowsRNU49()
    if (dataRows49 == null) {
        dataRowHelper.save(dataRows)
        return
    }
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')?.intValue()
    def start = getStartDate()
    def end = getEndDate()
    for (def row49 : dataRows49) {
        if (row49.getAlias() == null && row49.usefullLifeEnd != null &&
                row49.monthsLoss != null && row49.expensesSum != null) {
            def newRow = formData.createDataRow()
            // графа 1
            newRow.rowNumber = ++rowNumber
            // графа 3
            newRow.invNumber = calc3(row49)
            // графа 2
            newRow.rnu49rowNumber = calc2(row49, newRow)
            // графа 4
            newRow.lossReportPeriod = calc4(row49, start, end)
            // графа 5
            newRow.lossTaxPeriod = calc5(row49, start, end)
            dataRows.add(newRow)
        }
    }
    dataRowHelper.save(dataRows)
}

void checkCreation() {
    if (formData.kind != FormDataKind.CONSOLIDATED) {
        // проверка наличия формы рну 49 в статусе подготовлена или выше (но не создана)
        def formData49 = getFormDataRNU49()
        if (formData49 == null || formData49.state != WorkflowState.ACCEPTED) {
            logger.error("Отсутствует или не находится в статусе «Принята» форма «${formTemplateService.get(312).fullName}» за текущий отчетный период!")
            return
        }
    }
    //проверка периода ввода остатков
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }
    formDataService.checkUnique(formData, logger)
}

def getFormDataRNU49() {
    return formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

def getDataRowsRNU49() {
    def formDataRNU = getFormDataRNU49()
    if (formDataRNU != null) {
        return formDataService.getDataRowHelper(formDataRNU)?.allCached
    }
    return null
}

def getStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

def calc3(def row49) {
    return row49.invNumber
}

def calc2(def row49, def row) {
    return (row.invNumber ? row49.firstRecordNumber : null)
}

def calc4(def row49, def startDate, def endDate) {
    def result = null
    def date = row49.operationDate
    if (date >= startDate && date <= endDate) {
        if (row49.usefullLifeEnd > row49.operationDate) {
            result = row49.expensesSum * (endDate[Calendar.MONTH] - date[Calendar.MONTH])
        } else {
            result = row49.expensesSum
        }
    }
    return round(result, 2)
}

def calc5(def row49, def startDate, def endDate) {
    def result = null
    def date = row49.usefullLifeEnd // 18 графа РНУ-49
    if (date < startDate) {
        result = row49.expensesSum * 3
    } else if (date >= startDate && date <= endDate) {
        result = row49.expensesSum * (endDate[Calendar.MONTH] - row49.usefullLifeEnd[Calendar.MONTH])
    }
    return round(result, 2)
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.rnu49rowNumber = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}