package form_template.deal.forecast_major_transactions.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * 810 - Прогноз крупных сделок
 *
 * formTemplateId=810
 *
 * @author Lhaziev
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        create()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent)
        break
    case FormDataEvent.CALCULATE:
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['ikksr', 'transactionName', 'sum']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['name']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['ikksr', 'transactionName', 'sum']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Дата окончания отчетного периода
@Field
Calendar calendarStartDate = null

@Field
def refBookCache = [:]

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

Calendar getCalendarStartDate() {
    if (calendarStartDate == null) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId)
    }
    return calendarStartDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы
def getFormDataPrevPeriod(boolean check) {
    if (getCalendarStartDate().get(Calendar.MONTH) != Calendar.JANUARY) {
        def formDataPrev = formDataService.getFormDataPrev(formData)
        if (formDataPrev == null || !formDataPrev.getState().equals(WorkflowState.ACCEPTED)) {
            def prevPeriod = reportPeriodService.getPrevReportPeriod(formData.getReportPeriodId())
            // предыдущего период может не найтись, потому что периода он не существует
            String periodName = "предыдущий период";
            if (prevPeriod != null) {
                periodName = prevPeriod.getName() + " " + prevPeriod.getTaxPeriod().getYear();
            }
            if (check) {
                logger.warn("Прогнозы крупных сделок не были скопированы из предыдущего отчетного периода. " +
                        "В Системе не найдена форма «%s» в статусе «Принята» за «%s», Подразделение: «%s», Тип: «%s».",
                        formData.getFormType().getName(), periodName, formDataDepartment.name, formData.getKind().getTitle())
            }
            return null
        }
        return formDataPrev
    } else {
        return null
    }
}

void create() {
    def formDataPrev = getFormDataPrevPeriod(true);
    if (formDataPrev != null) {
        def prevDataRows = formDataService.getDataRowHelper(formDataPrev)?.allSaved
        if (prevDataRows != null) {
            def dataRows = formDataService.getDataRowHelper(formData).allCached
            dataRows.addAll(prevDataRows)
        }
    }
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    for (row in dataRows) {
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение графы
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка на положительное значение ожидаемого дохода
        if (row.sum && row.sum < 0) {
            def msg1 = row.getCell('sum').column.name
            rowError(logger, row, "Строка $rowNum: Значение графы «$msg1» не может быть меньше «0»!")
        }

        // 3. Проверка на отсутствие в списке не ВЗЛ ОРН
        if (row.ikksr) {
            boolean isVZL = false
            def record = getRefBookValue(520, row.ikksr)
            def start = record?.START_DATE?.value
            def end = record?.END_DATE?.value
            if (record.TYPE?.referenceValue && record.TAX_STATUS?.referenceValue &&
                    start <= getReportPeriodEndDate() && (end == null || getReportPeriodStartDate() <= end && end <= getReportPeriodEndDate())) {
                def recordType = getRefBookValue(525, record.TYPE.referenceValue)
                def recordStatus = getRefBookValue(511, record.TAX_STATUS.referenceValue)
                if (recordType?.CODE.stringValue == "ВЗЛ" && recordStatus?.CODE.numberValue.longValue() == 2)
                    isVZL = true
            }
            if (!isVZL) {
                def msg1 = record.NAME.stringValue
                rowError(logger, row, "Строка $rowNum: Организация «$msg1» не является взаимозависимым лицом с общим режимом налогообложения в данном отчетном периоде!")
            }
        }
    }

    // 4. Проверка наличия формы предыдущего периода
    getFormDataPrevPeriod(true)
}
