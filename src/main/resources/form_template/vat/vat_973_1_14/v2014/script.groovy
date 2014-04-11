package form_template.vat.vat_973_1_14.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  (937.1.14) Расшифровка графы 14 «Расхождение» формы 937.1
 *
 *  formTemplateId=607
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['differences', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'differences', 'sum']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def dateFormat = 'dd.MM.yyyy'

// Добавление строки
void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // По-умолчанию перед «Итого»
    def index = getDataRow(dataRows, 'total').getIndex()
    // Если выделена нефиксированная строка
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    }
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    newRow.index = index
    dataRowHelper.insert(newRow, index)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row in dataRows) {
        def index = row.getIndex()
        if (row.getAlias() != 'total') {
            // Проверка заполнения граф
            checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)
        }
    }
    // Проверка суммы в строке 13
    def other = dataRowHelper.getDataRow(dataRows, 'R13')
    if (other.sum != calcOther(dataRows)) {
        logger.error("Сумма в строке 13 «Прочие (расшифровать):» не совпадает с расшифровкой!")
    }
    // Проверка итоговых значений
    def itog = dataRowHelper.getDataRow(dataRows, 'total')
    if (itog.sum != calcItog(dataRows)) {
        logger.error(WRONG_TOTAL, getColumnName(itog, 'sum'))
    }
    // Проверка наличия экземпляра налоговой формы 937.1 по соответствующему подразделению за соответствующий налоговый период; проверка итоговой суммы
    def formData937_1 = formDataService.find(606, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (formData937_1 == null) {
        logger.warn("Экземпляр налоговой формы 937.1 «Итоговые данные книги покупок» за период %s — %s не существует (отсутствуют первичные данные для проверки)!",
                getReportPeriodStartDate().format(dateFormat), getReportPeriodEndDate().format(dateFormat))
    } else {
        def dataRows937_1 = formDataService.getDataRowHelper(formData937_1).allCached
        def totalARow = null
        if (dataRows937_1 != null) {
            totalARow = getDataRow(dataRows937_1, 'totalA')
        }
        if (calcItog(dataRows) - calcOther(dataRows) != totalARow?.diff) {
            logger.warn('Сумма расхождения не соответствует расшифровке!')
        }
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def rowNum = 0
    for (def row in dataRows) {
        rowNum++
        if (row.getAlias() == null) {
            row.rowNum = rowNum
        }
    }
    def itog = dataRowHelper.getDataRow(dataRows, 'total')
    itog?.sum = calcItog(dataRows)
    def other = dataRowHelper.getDataRow(dataRows, 'R13')
    other?.sum = calcOther(dataRows)
    dataRowHelper.update(dataRows)
}

// Расчет итога
def calcItog(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() != 'total' && row.getAlias() != 'R13') {
            sum += row.sum == null ? 0 : row.sum
        }
    }
    return sum
}

// Расчет прочих
def calcOther(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() == null) {
            sum += row.sum == null ? 0 : row.sum
        }
    }
    return sum
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def tmp = []
    // Суммы по фиксированным строкам
    def staticSum = [:]
    // Строка «Итого»
    def totalRow = null
    // Инициализация сумм и разбор строк
    for (def row in dataRows) {
        if (row.getAlias() != null && row.getAlias() != 'total') {
            tmp.add(row)
            staticSum.put(row.getAlias(), 0 as BigDecimal)
        } else if (row.getAlias() == 'total') {
            totalRow = row
        }
    }
    dataRows = tmp
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.VAT) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                def srcAlias = srcRow.getAlias()
                if (srcAlias == null) {
                    dataRows.add(srcRow)
                } else if (srcAlias != 'total') {
                    staticSum.put(srcAlias, staticSum.get(srcAlias) + srcRow.sum)
                }
            }
        }
    }
    // Установка сумм для фиксированных строк
    for (def row in dataRows) {
        if (row.getAlias() != null) {
            row.sum = staticSum.get(row.getAlias())
        }
    }
    // Добавление строки «Итого»
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
    logger.info("Формирование консолидированной формы прошло успешно.")
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}