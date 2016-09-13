package form_template.market.summary_7_129.v2016

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 7.129 (Сводный) Кредитные договоры в CRM
 * formTemplateId = 906
 *
 * @author Bulat Kinzyabulatov
 *
 * графа 1  - rowNum             - № п/п
 * графа 2  - depName            - Наименование подразделения
 * графа 3  - inn                - ИНН заемщика
 * графа 4  - debtorName         - Наименование заемщика
 * графа 5  - docNum             - № кредитного договора
 * графа 6  - docDate            - Дата кредитного договора
 * графа 7  - creditDate         - Дата выдачи кредита
 * графа 8  - productId          - ID продукта CRM
 * графа 9  - clientId           - ID клиента CRM
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
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
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['rowNum', 'depName', 'inn', 'debtorName', 'docNum', 'docDate', 'creditDate', 'productId', 'clientId']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['depName', 'inn', 'debtorName', 'docNum', 'docDate']

@Field
def startDate = null

@Field
def endDate = null

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

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
}

String getKey(def row) {
    return (row.inn?.trim() + "#" + row.docNum?.trim() + "#" + row.docDate?.format('dd.MM.yyyy')).toLowerCase()
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void consolidation() {
    def rowsMap = [:]
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    // сортируем по месяцам
    sourcesInfo.sort { it.month }.each { Relation relation ->
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def dataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        dataRows.each { row ->
            // перезаписываем если совпадает по ключу
            def key = getKey(row)
            rowsMap[key]= row
        }
    }
    List<DataRow> dataRows = []
    rowsMap.values().each { row ->
        def newRow = formData.createDataRow()
        row.each { alias, value ->
            newRow[alias] = value
        }
        dataRows.add(newRow)
    }
    sortRows(refBookService, logger, dataRows, null, null, null)
    updateIndexes(dataRows)
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.allCached = dataRows
}