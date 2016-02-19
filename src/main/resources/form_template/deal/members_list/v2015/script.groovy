package form_template.deal.members_list.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import groovy.transform.Field

/**
 * Список участников для заполнения приложения 6.
 *
 * formTemplateId=846
 */

// графа 1  (1)   - rowNumber
// графа 2  (2)   - name			- атрибут 5201 - NAME - «Полное наименование юридического лица с указанием ОПФ», справочник 520 «Юридические лица»
// графа 3  (3)   - address		    - зависит от графы 2 - атрибут 5202 - ADDRESS - «Место нахождения (юридический адрес) юридического лица (из устава)», справочник 520 «Юридические лица»
// графа 4  (4)   - orgCode		    - зависит от графы 2 - атрибут 5203 - ORG_CODE - «Код организации», справочник 520 «Юридические лица»
// графа 5  (5)   - countryCode	    - зависит от графы 2 - атрибут 5204 - COUNTRY_CODE - «Код страны по ОКСМ», справочник 520 «Юридические лица»
// графа 6  (6,1) - inn			    - зависит от графы 2 - атрибут 5205 - INN - «ИНН (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 7  (6,2) - kpp			    - зависит от графы 2 - атрибут 5206 - KPP - «КПП (заполняется для резидентов, некредитных организаций)», справочник 520 «Юридические лица»
// графа 8  (6,3) - swift			- зависит от графы 2 - атрибут 5208 - SWIFT - «Код SWIFT (заполняется для кредитных организаций, резидентов и нерезидентов)», справочник 520 «Юридические лица»
// графа 9  (6,4) - regNum		    - зависит от графы 2 - атрибут 5209 - REG_NUM - «Регистрационный номер в стране инкорпорации (заполняется для нерезидентов)», справочник 520 «Юридические лица»
// графа 10 (7)   - startData		- зависит от графы 2 - атрибут 5210 - START_DATE - «Дата наступления основания для включения в список», справочник 520 «Юридические лица»
// графа 11 (8)   - endData		    - зависит от графы 2 - атрибут 5211 - END_DATE - «Дата наступления основания для исключения из списка», справочник 520 «Юридические лица»
// графа 12 (9)   - category		- атрибут 5061 - CODE - «Код категории», справочник 506 «Категории юридического лица по системе «светофор»»
// графа 13 (10)  - vatStatus		- зависит от графы 2 - атрибут 5212 - VAT_STATUS - «Статус по НДС», справочник 520 «Юридические лица»
// графа 14 (11)  - taxStatus		- зависит от графы 2 - атрибут 5213 - TAX_STATUS - «Специальный налоговый статус», справочник 520 «Юридические лица»
// графа 15 (12)  - depCriterion	- зависит от графы 2 - атрибут 5214 - DEP_CRITERION - «Критерий взаимозависимости», справочник 520 «Юридические лица»

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
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

@Field
def allColumns = ['rowNumber', 'name', 'address', 'orgCode', 'countryCode', 'inn', 'kpp', 'swift', 'regNum', 'startData', 'endData', 'category', 'vatStatus', 'taxStatus', 'depCriterion']

// Проверяемые на пустые значения атрибуты (графа 2..5, 10, 12, 13, 15)
@Field
def nonEmptyColumns = ['name', /*'address', 'orgCode', 'countryCode', 'startData',*/ 'category', /*'vatStatus', 'depCriterion'*/]

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

// Порядок отчетного периода
@Field
def periodOrder = null

@Field
def reportPeriod = null

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

def getPeriodOrder() {
    if (periodOrder == null) {
        periodOrder = reportPeriodService.get(formData.reportPeriodId).getOrder()
    }
    return periodOrder
}

ReportPeriod getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return reportPeriod
}

def getPeriod() {
    def period = reportPeriodService.get(formData.reportPeriodId)
    return period.name + " " + period.taxPeriod.year
}

//// Обертки методов
// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), -1, null, logger, true)
}

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

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    // Взаимозависимые лица
    def sourceFormTypeId1 = 800
    // Участники группы ПАО Сбербанк
    def sourceFormTypeId2 = 845

    def source1 = null
    def source2 = null
    def sourceRows1 = [:]
    def sourceRows2 = [:]

    source1 = formDataService.getLast(sourceFormTypeId1, FormDataKind.PRIMARY, formData.departmentId, getReportPeriod()?.id, null, null, false)
    def formName = formDataService.getFormTemplate(sourceFormTypeId1, getReportPeriod()?.id).name
    if (source1 == null) {
        logger.error("Не найдена форма «%s»: Тип: %s, Период: %s, Подразделение: %s!",
                formName, FormDataKind.PRIMARY.title, getPeriod(), departmentService.get(formData.departmentId)?.name)
        return
    }
    sourceRows1 = formDataService.getDataRowHelper(source1)?.allSaved
    if (sourceRows1.isEmpty()) {
        logger.error("Данные на форме «%s» за отчетный период %s отсутствуют!", formName, getPeriod())
        return
    }
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == sourceFormTypeId2) {
            source2 = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
            sourceRows2 = formDataService.getDataRowHelper(source2)?.allSaved
        }
    }

    def samples = []
    def tmpDataRows = []
    def dataRows = []

    if (getPeriodOrder() == 4) {
        for (row in sourceRows1) {
            def record = getRefBookValue(520L, row.name)
            def end = record?.END_DATE?.value
            if (end == null || end > getReportPeriodEndDate()) {
                samples.add(row)
            }
        }
    }

    if (getPeriodOrder() == 3) {
        def orgCode = getRecordId(513, 'CODE', '1')
        def taxStatus = getRecordId(511, 'CODE', '2')
        for (row in sourceRows1) {
            def record = getRefBookValue(520L, row.name)
            def RefOrgCode = record?.ORG_CODE?.value
            def RefTaxStatus = record?.TAX_STATUS?.value
            def end = record?.END_DATE?.value
            if (RefOrgCode == orgCode && RefTaxStatus == taxStatus && (end == null || end > getReportPeriodEndDate())) {
                samples.add(row)
            }
        }
    }

    def useTcoIds2 = getNamesFromSources(sourceRows2)
    for (sample in samples) {
        boolean flag = sample.name in useTcoIds2
        if (!flag) {
            tmpDataRows.add(sample)
        } else {
            for (row in sourceRows2) {
                if (sample.name == row.name && row.sign == 0) {
                    tmpDataRows.add(sample)
                }
            }
        }
    }

    for (row in tmpDataRows) {
        def newRow = formData.createDataRow()
        allColumns.each { column ->
            newRow[column] = row[column]
        }
        dataRows.add(newRow)
    }

    // 5.1
    def useTcoIds1 = getNamesFromSources(sourceRows1)
    for (row in sourceRows2) {
        if (!(row.name in useTcoIds1)) {
            def name = getRefBookValue(520, row.name)?.NAME?.stringValue
            // лп6
            logger.warn("Строка %s: Организация «%s» не найдена на форме «Взаимозависимые лица» за период %s!", row.getIndex(), name, getPeriod())
        }
    }

    sortRows(refBookService, logger, dataRows, null, null, null)
    updateIndexes(dataRows)
    dataRowHelper.allCached = dataRows
}

/**
 * Получить список идентификаторов с формы Участники группы ПАО Сбербанк или ВЗЛ.
 *
 */
def getNamesFromSources(def sourceRows) {
    def list = []
    for (def row : sourceRows) {
        list.add(row.name)
    }
    return list.unique()
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

        // 2. Проверка ВЗЛ ОРН
        if (getPeriodOrder() == 3 || getPeriodOrder() == 4) {
            def useCode = getPeriodOrder() == 3
            def records520 = getRecords520(useCode)
            def isVZL = records520?.find { it?.record_id?.value == row.name }
            if (records520 && !isVZL) {
                def value2 = getRefBookValue(520L, row.name)?.NAME?.value
                logger.error("Строка %s: Организация «%s» не является взаимозависимым лицом %sв данном отчетном периоде!",
                        rowNum, value2, useCode ? "с общим режимом налогообложения " : "")
            }
        }
    }
}

@Field
def records520 = null

/**
 * Получить значения из справочника "Участники ТЦО".
 * @param useCode true для 9 месяцев, false для года
 * @return
 */
def getRecords520(boolean useCode) {
    if (records520 != null) {
        return records520
    }
    // получить id записи с кодом "2" из справочника "Специальный налоговый статус"
    def provider
    def records
    def filter = null
    if (useCode) {
        provider = formDataService.getRefBookProvider(refBookFactory, 511L, providerCache)
        filter = "CODE = 2"
        records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        def taxStatusId
        if (records && records.size() == 1) {
            taxStatusId = records.get(0)?.record_id?.value
        } else {
            records520 = []
            return records520
        }
        filter = "TAX_STATUS = $taxStatusId"
    }
    // получить записи из справочника "Участники ТЦО"
    provider = formDataService.getRefBookProvider(refBookFactory, 520L, providerCache)
    records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
    records520 = []
    records.each { record ->
        def start = record?.START_DATE?.value
        def end = record?.END_DATE?.value
        def typeId = record?.TYPE?.value
        if (isVZL(start, end, typeId)) {
            records520.add(record)
        }
    }
    return records520
}

// проверка принадлежности организации к ВЗЛ в отчетном периоде
def isVZL(def start, def end, def typeId) {
    if (start <= getReportPeriodEndDate() &&
            (end == null || end > getReportPeriodEndDate()) &&
            getRefBookValue(525L, typeId)?.CODE?.value == "ВЗЛ") {
        return true
    }
    return false
}
