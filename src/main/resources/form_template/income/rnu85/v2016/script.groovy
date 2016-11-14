package form_template.income.rnu85.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * РНУ-85
 *
 * formTypeId = 585
 * formTemplateId = 585
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
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
        // TODO
        //formDataService.consolidationSimple(formData, logger, userInfo)
        //formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        // TODO
        //importData()
        //formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        // TODO
        //importTransportData()
        //formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['rowNumber', 'dealNumber', 'name', 'nameBond', 'series', 'bondKind', 'bondSign', 'bondNominal',
                  'bondCount', 'dealType', 'date1', 'date2', 'date3', 'dealPrice', 'dealRate', 'dealCostMin', 'dealCostMax',
                  'bondDate', 'bondCost', 'bondOutcome1', 'bondOutcome2', 'bondType', 'bondPrice1', 'bondPrice2', 'sumMin',
                  'sumMax', 'income', 'outcome', 'delta']

@Field
def editableColumns = allColumns - ['rowNumber']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns

@Field
def nonEmptyColumns = ['dealNumber', 'name', 'nameBond', 'series', 'bondKind', 'bondSign', 'bondNominal', 'bondCount',
                       'dealType', 'date1', 'date2', 'date3', 'dealPrice', 'dealRate', 'dealCostMin', 'dealCostMax']

// дата окончания отчетного периода
@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                      boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                    boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Кастомные методы

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        def index = row.getIndex()
        // Проверка заполнения обязательных граф
        // checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // TODO
    }
}
def calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // TODO
    }
}