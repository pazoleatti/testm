package form_template.income.rnu48_1

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Форма "(РНУ-48.1) Регистр налогового учёта «ведомость ввода в эксплуатацию инвентаря и принадлежностей до 40 000 руб.".
 * formTemplateId=343
 *
 * @author vsergeev
 * @author rtimerbaev
 */

// 1 - number          - № пп
// 2 - inventoryNumber - Инвентарный номер
// 3 - usefulDate      - Дата ввода в эксплуатацию
// 4 - amount          - Сумма, включаемая в состав материальных расходов

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
}

// Все атрибуты
@Field
def allColumns = ['number', 'inventoryNumber', 'usefulDate', 'amount']

// Редактируемые атрибуты (графа 2..4)
@Field
def editableColumns = ['inventoryNumber', 'usefulDate', 'amount']

// Группируемые атрибуты (графа 3, 2)
@Field
def groupColumns = ['usefulDate', 'inventoryNumber']

// Проверяемые на пустые значения атрибуты (графа 1..4)
@Field
def nonEmptyColumns = ['number', 'inventoryNumber', 'usefulDate', 'amount']

// Атрибуты итоговых строк для которых вычисляются суммы (графа )
@Field
def totalColumns = ['amount']

void checkCreation() {
    //проверка периода ввода остатков
    if (reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }
    formDataService.checkUnique(formData, logger)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // удалить строку "итого"
    deleteAllAliased(dataRows)
    // сортировка
    sortRows(dataRows, groupColumns)

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        row.number = ++rowNumber
    }
    //расчитываем новые итоговые значения
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.getAllCached()

    def reportPeriodRange = getReportPeriodRange()
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // 1. Обязательность заполнения поля графы 1..4
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка даты ввода в эксплуатацию и границ отчетного периода
        if (! (row.usefulDate in reportPeriodRange)){
            logger.error("Строка ${row.getIndex()}: Дата ввода в эксплуатацию вне границ отчетного периода!")
        }
    }

    // 3. Проверка итоговых значений по всей форме
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.inventoryNumber = 'Итого'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

def getReportPeriodRange() {
    def periodStartsDate = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    def periodEndsDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time

    return periodStartsDate..periodEndsDate
}