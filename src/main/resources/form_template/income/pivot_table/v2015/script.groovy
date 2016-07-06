package form_template.income.pivot_table.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * 850 - Сводная таблица - Лист 08 декларации по прибыли
 * TODO доработать
 * formTemplateId=850
 *
 * @author Bulat Kinzyabulatov
 */

// 1  number       - № п/п
// 2  rnu          - Номер РНУ
// 3  code         - Код КНУ
// 4  corrType     - Вид корректировки (1, 2, 3)
// 5  base         - Основание отнесения сделки к контролируемой в соответствии со ст. 105.14 НК РФ
// 6  countryCode  - Код страны регистрации (инкорпорации) контрагента
// 7  innKio       - ИНН контрагента
// 8  rsk          - Регистрационный номер контрагента в стране регистрации (инкорпорации)
// 9  name         - Наименование организации (ФИО) контрагента
// 10 sign010      - Доходы от реализации. Код строки 010. Признак (0-уменьшение, 1- увеличение)
// 11 sum010       - Доходы от реализации. Код строки 010. Сумма в рублях
// 12 sign020      - Внереализационные доходы. Код строки 020. Признак (0-уменьшение, 1- увеличение)
// 13 sum020       - Внереализационные доходы. Код строки 020. Сумма в рублях
// 14 sign030      - Расходы, уменьшающие сумму доходов от реализации. Код строки 030. Признак (0-уменьшение, 1- увеличение)
// 15 sum030       - Расходы, уменьшающие сумму доходов от реализации. Код строки 030. Сумма в рублях
// 16 sign040      - Внереализационные расходы. Код строки 040. Признак (0-уменьшение, 1- увеличение)
// 17 sum040       - Внереализационные расходы. Код строки 040. Сумма в рублях
// 18 sign060      - Доходы от выбытия, в том числе доход от погашения. Код строки 060. Признак (0-уменьшение, 1- увеличение)
// 19 sum060       - Доходы от выбытия, в том числе доход от погашения. Код строки 060. Сумма в рублях
// 20 sign070      - Расходы, связанные с приобретением и реализацией или иным выбытием (в том числе, погашением). Код строки 070. Признак (0-уменьшение, 1- увеличение)
// 21 sum070       - Расходы, связанные с приобретением и реализацией или иным выбытием (в том числе, погашением). Код строки 070. Сумма в рублях
// 22 sign080      - Итого сумма корректировки (сумма строк 060, 070) по модулю. Код строки 080. Признак (0-уменьшение, 1- увеличение)
// 23 sum080       - Итого сумма корректировки (сумма строк 060, 070) по модулю. Код строки 080. Сумма в рублях
// 24 sign100      - Сумма налога, подлежащая исчислению, исходя из итоговой суммы корректировки по строкам 050, 080 и соответствующей налоговой ставки. Код строки 100. Признак (0-уменьшение, 1- увеличение)
// 25 sum100       - Сумма налога, подлежащая исчислению, исходя из итоговой суммы корректировки по строкам 050, 080 и соответствующей налоговой ставки. Код строки 100. Сумма в рублях

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_LOAD:
        afterLoad()
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW: // TODO
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// TODO обновить графы
@Field
def allColumns = ['number', 'fix', 'name', 'iksr', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate',
                  'concessionsDate', 'income', 'finResult', 'code', 'marketPrice', 'finResultTax', 'incomeCorrection']

// Редактируемые атрибуты
@Field
def editableColumns = ['name', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate', 'concessionsDate',
                       'income', 'code', 'marketPrice']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'iksr', 'finResult', 'finResultTax', 'incomeCorrection']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'dealNum', 'dealDate', 'cost', 'costReserve', 'repaymentDate', 'concessionsDate', 'income',
                       'finResult', 'code', 'marketPrice', 'finResultTax', 'incomeCorrection']

// Группируемые атрибуты
@Field
def groupColumns = ['code']

@Field
def sortColumns = ['code', 'name', 'dealNum', 'dealDate']

@Field
def totalColumns = ['incomeCorrection']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
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

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
        // TODO
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    // TODO

    updateIndexes(dataRows)
}

@Field
def formTypeIds = [] //TODO

void consolidation() {
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def templateRows = formTemplate.rows
    // TODO
}

void afterLoad() {
    // прибыль сводная
    if (binding.variables.containsKey("specialPeriod") && formData.kind == FormDataKind.SUMMARY) {
        // для справочников начало от 01.01.year (для прибыли start_date)
        specialPeriod.calendarStartDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
}