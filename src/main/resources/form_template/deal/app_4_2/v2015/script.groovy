package form_template.deal.app_4_2.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * 803 - Приложение 4.2. Отчет в отношении доходов и расходов Банка по сделкам с ВЗЛ, РОЗ, НЛ по итогам окончания Налогового периода
 *
 * formTemplateId=803
 *
 * TODO
 */

// rowNumber        - № п/п
// name             - Полное наименование и ОПФ юридического лица
// ikksr            - ИНН и КПП или его аналог (при наличии)
// group            - Группа юридического лица
// sum51            - Сделки с ценными бумагами
// sum52            - Сделки купли-продажи иностранной валюты
// sum53            - Сделки купли-продажи драгоценных металлов
// sum54            - Сделки, отраженные в Журнале взаиморасчетов
// sum55            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// sum56            - Сумма дополнительно начисленных налогооблагаемых расходов
// sum61            - Сделки с ценными бумагами
// sum62            - Сделки купли-продажи иностранной валюты
// sum63            - Сделки купли-продажи драгоценных металлов
// sum64            - Сделки, отраженные в Журнале взаиморасчетов
// sum65            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// sum66            - Сумма дополнительно начисленных налогооблагаемых расходов
// sum7             - Итого объем доходов и расходов, руб.
// thresholdValue   - Применимое Пороговое значение, руб.
// sign             - Признак наличия Контролируемых сделок (Да / Нет)
// categoryRevised  - Пересмотренная Категория юридического лица по состоянию на 1 апреля следующего Налогового периода

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
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
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
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def editableColumns = []

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['name', 'group', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63',
                       'sum64', 'sum65', 'sum66', 'sum7', 'thresholdValue', 'sign', 'categoryRevised']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'group', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63',
                       'sum64', 'sum65', 'sum66', 'sum7', 'thresholdValue', 'sign', 'categoryRevised']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

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

// "Да"
@Field
def Long recYesId

def Long getRecYesId() {
    if (recYesId == null)
        recYesId = getRecordId(38, 'CODE', '1')
    return recYesId
}

// "Нет"
@Field
def Long recNoId

def Long getRecNoId() {
    if (recNoId == null)
        recNoId = getRecordId(38, 'CODE', '0')
    return recNoId
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

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        row.group = calc4(row)
        row.sum7 = calc17(row)
        row.sum8 = calc18(row)
        row.sign = calc19(row)
        row.categoryRevised = calc20(row)
    }
}

def Long calc4(def row) {
    // TODO
    return 0
}

def BigDecimal calc17(def row) {
    // Графа 17 = сумма значений в графах 5-16
    def value = 0
    ['group', 'sum51', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56', 'sum61', 'sum62', 'sum63', 'sum64', 'sum65', 'sum66',
     'sum7', 'thresholdValue', 'sign', 'categoryRevised'].each {
        value += row[it] ?: 0
    }
    return value
}

def BigDecimal calc18(def row) {
    // TODO
    return 0
}

def Long calc19(def row) {
    if (row.sum7 >= row.sum8) {
        return getRecYesId()
    }
    return getRecNoId()
}

def Long calc20(def row) {
    // TODO
    return 0
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка заполнения обязательных полей
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Отсутствие нулевых значений
        if (calc17(row) == 0) {
            logger.error("Строка $rowNum: Объем доходов и расходов по всем сделкам не может быть нулевым!")
        }

        // TODO
        // 3. Наличие порогового значения
        // 4. Наличие правила назначения категории
        // 5. Проверка соответствия категории пороговым значениям
    }
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

def consolidation() {
    // TODO
}