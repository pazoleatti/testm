package form_template.income.outcome_complex

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)"
 * formTemplateId=303
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
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
        if (!isBank()) {
            calcTotal()
        }
        break
}

// графа  1 - consumptionTypeId
// графа  2 - consumptionGroup
// графа  3 - consumptionTypeByOperation
// графа  4 - consumptionBuhSumAccountNumber
// графа  5 - consumptionBuhSumRnuSource
// графа  6 - consumptionBuhSumAccepted
// графа  7 - consumptionBuhSumPrevTaxPeriod
// графа  8 - consumptionTaxSumRnuSource
// графа  9 - consumptionTaxSumS
// графа 10 - rnuNo
// графа 11 - logicalCheck
// графа 12 - accountingRecords
// графа 13 - opuSumByEnclosure3
// графа 14 - opuSumByTableP
// графа 15 - opuSumTotal
// графа 16 - difference

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS']

@Field
def rowsCalc = ['R3','R4','R5','R6','R7','R8','R9','R10','R11','R12','R13','R14','R15','R16','R17','R1','R26','R27',
        'R28','R29', 'R30','R31','R32', 'R70','R71']

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataSimple = getFormDataSimple()
    def income102NotFound = []
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (def row : dataRowHelper.getAllCached()) {
        // исключить итоговые строки
        if (row.getAlias() in ['R67', 'R93']) {
            continue
        }
        if (!isEmpty(row.consumptionTaxSumS) && !isEmpty(row.consumptionBuhSumAccepted) &&
                !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ОКРУГЛ( «графа9»-(Сумма 6-Сумма 7);2),
            sum6 = 0
            sum7 = 0
            for (rowSum in dataRowHelper.getAllCached()) {
                String knySum
                String kny
                if (rowSum.getCell('consumptionTypeId').hasValueOwner()) {
                    knySum = rowSum.getCell('consumptionTypeId').valueOwner.value
                } else {
                    knySum = rowSum.getCell('consumptionTypeId').value
                }
                if (row.getCell('consumptionTypeId').hasValueOwner()) {
                    kny = row.getCell('consumptionTypeId').valueOwner.value
                } else {
                    kny = row.getCell('consumptionTypeId').value
                }
                if (kny == knySum) {
                    sum6 += rowSum.consumptionBuhSumAccepted
                    sum7 += rowSum.consumptionBuhSumPrevTaxPeriod
                }
            }
            tmp = round(row.consumptionTaxSumS - (sum6 - sum7), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = (tmp < 0 ? message : value.toString())
        }

        if (!isEmpty(row.consumptionBuhSumAccepted) && !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // графа 13
            if (row.getAlias() in ['R3', 'R11']) {
                tmp = calcColumn6(['R3', 'R11'])
            } else {
                tmp = row.consumptionBuhSumAccepted
            }
            row.opuSumByEnclosure3 = tmp

            // графа 14
            row.opuSumByTableP = getSumFromSimple(formDataSimple, 'consumptionAccountNumber',
                    'rnu5Field5Accepted', row.consumptionBuhSumAccountNumber)

            // графа 15
            def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords)
            if (income102 == null || income102.isEmpty()) {
                income102NotFound += getIndex(row)
                tmp = 0
            } else {
                tmp = (income102[0] != null ? income102[0].getTotalSum() : 0)
            }
            row.opuSumTotal = tmp

            // графа 16
            row.difference = (getValue(row.opuSumByEnclosure3) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
        }
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }


    calcTotal()
    dataRowHelper.save(dataRowHelper.getAllCached())
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        if (rowsCalc.contains(row.getAlias())) {
            // Проверка обязательных полей
            checkRequiredColumns(row, nonEmptyColumns)
        }
    }
}

/**
 * Расчет итоговых строк.
 */
void calcTotal() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def totalRow1 = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), 'R67')
    def totalRow2 = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), 'R93')

    // суммы для графы 9
    ['consumptionTaxSumS'].each { alias ->
        totalRow1.getCell(alias).setValue(getSum(alias, 'R2', 'R66'))
        totalRow2.getCell(alias).setValue(getSum(alias, 'R69', 'R92'))
    }
}

/**
 * Скрипт для консолидации.
 *
 * @author rtimerbaev
 * @since 22.02.2013 15:30
 */
void consolidation() {
    if (!isBank()) {
        return
    }
    // очистить форму
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { alias ->
            if (row.getCell(alias).isEditable()) {
                row.getCell(alias).setValue(0)
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null)
        }
        if (row.getAlias() in ['R67', 'R93']) {
            row.consumptionTaxSumS = 0
        }
    }

    // получить консолидированные формы из источников
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (def row : formDataService.getDataRowHelper(child).getAllCached()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), row.getAlias())
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)))
                    }
                }
            }
        }
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
    dataRowHelper.save(dataRowHelper.allCached)
    dataRowHelper.commit()
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

double summ(String columnName, String fromRowA, String toRowA) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def from = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), fromRowA)
    def to = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), toRowA)
    if (from > to) {
        return 0
    }
    def result = summ(formData, dataRowHelper.getAllCached(), new ColumnRange(columnName, from, to))
    return result ?: 0;
}

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def from = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), rowFromAlias)
    def to = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), rowToAlias)
    if (from > to) {
        return 0
    }
    return summ(formData, dataRowHelper.getAllCached(), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formDataService.getDataRowHelper(formData).getAllCached().indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить значение для графы 13. Сумма значении графы 6 указанных строк
 *
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcColumn6(def aliasRows) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def sum = 0
    aliasRows.each { alias ->
        sum += dataRowHelper.getDataRow(dataRowHelper.getAllCached(), alias).consumptionBuhSumAccepted
    }
    return sum
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple() {
    return formDataService.find(304, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить сумму значений из расходов простых.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromSimple(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : formDataService.getDataRowHelper(data).getAllCached()) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
}

// Проверить заполненость обязательных полей
// Нередактируемые не проверяются
def checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('«' + name + '»')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("Строка ${row.getIndex()}: не заполнены графы : $errorMsg.")
    }
}