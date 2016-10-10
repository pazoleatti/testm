package form_template.land.calc_for_declaration.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field

/**
 * Расчет земельного налога по земельным участкам, подлежащим включению в декларацию.
 *
 * formTemplateId = 918
 * formTypeId = 918
 *
 * TODO:
 *      - тесты
 *
 *      - группировка
 *      - сортировака
 *      - расчеты итогов
 *
 */

// графа    - fix
// графа 1  - rowNumber
// графа 2  - department           - атрибут 161 - NAME - «Наименование подразделения», справочник 30 «Подразделения»
// графа 3  - kno
// графа 4  - kpp
// графа 5  - kbk                  - атрибут 7031 - CODE - «Код», справочник 703 «Коды бюджетной классификации земельного налога»
// графа 6  - oktmo                - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований (ОКТМО)»
// графа 7  - cadastralNumber
// графа 8  - landCategory         - атрибут 7021 - CODE - «Код», справочник 702 «Категории земли»
// графа 9  - constructionPhase    - атрибут 7011 - CODE - «Код», справочник 701 «Периоды строительства»
// графа 10 - cadastralCost
// графа 11 - taxPart
// графа 12 - ownershipDate
// графа 13 - terminationDate
// графа 14 - period
// графа 15 - benefitCode          - атрибут 7053.7041 - TAX_BENEFIT_ID.CODE - «Код налоговой льготы».«Код», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 16 - benefitBase          - зависит от графы 15 - атрибут 7053.7043 - TAX_BENEFIT_ID.BASE - «Код налоговой льготы».«Основание», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 17 - benefitParam         - зависит от графы 15 - атрибут 7061 - REDUCTION_PARAMS - «Параметры льготы», справочник 705 «Параметры налоговых льгот земельного налога»
// графа 18 - startDate
// графа 19 - endDate
// графа 20 - benefitPeriod
// графа 21 - taxRate
// графа 22 - kv
// графа 23 - kl
// графа 24 - sum
// графа 25 - q1
// графа 26 - q2
// графа 27 - q3
// графа 28 - year

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calcItog()
            formDataService.saveCachedDataRows(formData, logger)
        }
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

@Field
def compareStyleName = 'Корректировка-удалено'

@Field
def allColumns = ['fix', 'rowNumber', 'department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber',
                  'landCategory', 'constructionPhase', 'cadastralCost', 'taxPart', 'ownershipDate', 'terminationDate',
                  'period', 'benefitCode', 'benefitBase', 'benefitParam', 'startDate', 'endDate', 'benefitPeriod',
                  'taxRate', 'kv', 'kl', 'sum', 'q1', 'q2', 'q3', 'year']

// Редактируемые атрибуты
@Field
def editableColumns = []

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns - 'fix'

// Проверяемые на пустые значения атрибуты (графа 2..8, 10, 12, 14, 21, 22, 25..28)
@Field
def nonEmptyColumns = ['department', 'kno', 'kpp', 'kbk', 'oktmo', 'cadastralNumber','landCategory', 'cadastralCost',
                       'ownershipDate', 'period', 'taxRate', 'kv', 'q1', 'q2', 'q3', 'year']

// графа 3, 4, 6
@Field
def groupColumns = ['kno', 'kpp', 'oktmo']

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

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

//// Обертки методов

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

// Логические проверки
void logicCheck() {
    // пока не описаны в аналитике
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (row in dataRows) {
        if (row.getAlias()) {
            continue
        }
        def rowIndex = row.getIndex()
        // 1. Проверка обязательности заполнения граф
        checkNonEmptyColumns(row, rowIndex, nonEmptyColumns, logger, true)
    }
}

void calcItog(){
    // TODO
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = groupColumns + (allColumns - groupColumns)
    // массовое разыменование справочных и зависимых значений
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns())
    // TODO
    sortRows(dataRows, columns)
    dataRowHelper.saveSort()
}

def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }
    return newRow
}

@Field
def sourceTypeId_916 = 916 // Расчет земельного налога за отчетные периоды
@Field
def sourceTypeId_917 = 917 // Земельные участки, подлежащие включению в декларацию

void consolidation() {
    // получить источники
    def sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)

    // проверка перед консолидацией
    def has_917 = false
    for (Relation relation : sourcesInfo) {
        if (sourceTypeId_917 == relation.formType.id) {
            has_917 = true
        }
    }
    if (!has_917) {
        logger.error("Не удалось консолидировать данные в форму. В Системе отсутствует форма вида «Земельные участки, подлежащие включению в декларацию» в состоянии «Принята» " +
                "за период: «%s %s» для подразделения «%s»", getReportPeriod()?.name, getReportPeriod()?.taxPeriod?.year?.toString(), formDataDepartment.name)
        return
    }

    // графа 2..28
    def consolidationColumns = allColumns - 'fix' - 'rowNumber'
    // На форме настроек подразделений для подразделения формы найти значения поля «КПП».
    def kppList = getKPPList()

    def dataRows = []
    // собрать данные из источнков
    for (Relation relation : sourcesInfo) {
        if (![sourceTypeId_916, sourceTypeId_917].contains(relation.formType.id)) {
            continue
        }
        FormData sourceFormData = formDataService.get(relation.formDataId, null)
        def sourceDataRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        sourceDataRows.each { sourceRow ->
            if ((relation.formType.id == sourceTypeId_916 && kppList.contains(sourceRow.kpp)) || relation.formType.id == sourceTypeId_917) {
                def newRow = getNewRow()
                consolidationColumns.each { alias ->
                    newRow[alias] = sourceRow[alias]
                }
                dataRows.add(newRow)
            }
        }

    }
    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// Получить параметры подразделения
def getKPPList() {
    def departmentId = formData.departmentId
    def departmentParamList = getProvider(RefBook.DEPARTMENT_CONFIG_LAND).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
    }
    def kppList = []
    for (def record : departmentParamList) {
        def kpp = record.KPP?.stringValue
        if (kpp != null && !kppList.contains(kpp)) {
            kppList.add(kpp)
        }
    }
    return kppList
}