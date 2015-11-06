package form_template.deal.app_4_1_9.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * 802 - Приложение 4.1. (9 месяцев) Отчет в отношении доходов и расходов Банка по сделкам с российскими ВЗЛ, применяющими Общий режим налогообложения
 * отличается от 801 только источниками данных
 *
 * formTemplateId=802
 *
 */

// rowNumber        - № п/п
// name             - Полное наименование и ОПФ юридического лица
// ikksr            - ИНН и КПП или его аналог (при наличии)
// sum4             - Сделки с ценными бумагами
// sum42            - Сделки купли-продажи иностранной валюты
// sum43            - Сделки купли-продажи драгоценных металлов
// sum44            - Сделки, отраженные в Журнале взаиморасчетов
// sum45            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// sum46            - Сумма дополнительно начисленных налогооблагаемых расходов
// sum51            - Сделки с ценными бумагами
// sum52            - Сделки купли-продажи иностранной валюты
// sum53            - Сделки купли-продажи драгоценных металлов
// sum54            - Сделки, отраженные в Журнале взаиморасчетов
// sum55            - Сделки с лицами, информация о которых не  отражена в Журнале взаиморасчетов
// sum56            - Сумма дополнительно начисленных налогооблагаемых расходов
// sum6             - Итого объем доходов и расходов, руб.
// category         - Категория Взаимозависимого лица на начало Отчетного периода
// sum8             - Ожидаемый объем доходов и расходов за отчетный Налоговый период, руб.
// sum9             - Скорректированный ожидаемый объем доходов и расходов, руб.
// categoryRevised  - Пересмотренная Категория по итогам Отчетного периода
// categoryPrimary  - Первичная Категория юридического лица на следующий Налоговый период

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
def autoFillColumns = ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum5', 'sum52', 'sum53', 'sum54', 'sum55',
                       'sum56', 'sum6', 'category', 'sum8', 'sum9', 'categoryRevised', 'categoryPrimary']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['name', 'sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum5', 'sum52', 'sum53', 'sum54',
                       'sum55', 'sum56', 'sum6', 'category', 'sum8', 'sum9', 'categoryRevised', 'categoryPrimary']

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
        row.sum56 = calc16(row)
        row.sum8 = calc18(row)
        row.sum9 = calc19(row)
        row.categoryRevised = calc20(row)
        row.categoryPrimary = row.categoryRevised
    }
}

def BigDecimal calc16(def row) {
    // Графа 16 = сумма значений в графах 4-15
    value = 0
    ['sum4', 'sum42', 'sum43', 'sum44', 'sum45', 'sum46', 'sum5', 'sum52', 'sum53', 'sum54', 'sum55', 'sum56'].each {
        value += row[it] ?: 0
    }
    return value
}

def BigDecimal calc18(def row) {
    // Графа 18 = Графа 16 * 4/3
    return (row.sum6 ?: 0) * (4/3)
}

def BigDecimal calc19(def row) {
    // Графа 19 = Графа 18 + Ожидаемый объем доходов и расходов» их формы «Прогноз крупных сделок» для организации, указанной в графе 2
    // TODO Правило извлечения ожидаемого объема доходов и расходов будет добавлено после описания формы «Прогноз крупных сделок»
    value = 0
    return row.sum8 + value
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
        if (calc16(row) == 0) {
            logger.error("Строка $rowNum: Объем доходов и расходов по всем сделкам не может быть нулевым!")
        }

        // TODO
        // 3. Проверка на отсутствие в списке не ВЗЛ ОРН
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