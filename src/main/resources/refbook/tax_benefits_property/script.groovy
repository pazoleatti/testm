package refbook.tax_benefits_property

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Cкрипт справочника «Параметры налоговых льгот налога на имущество» (id = 203)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            validDateFrom, -1, null, logger, true)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

@Field
def providerCache = [:]
@Field
def recordsCountCache = [:]
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

@Field
def refBookId = 203
@Field
def refBookTaxId = 202
@Field
def currentYear


void save() {
    currentYear = Integer.valueOf(formatDate(validDateFrom, "yyyy"))
    def tax000 = getRecordId(refBookTaxId, 'CODE', '2012000')
    def tax400 = getRecordId(refBookTaxId, 'CODE', '2012400')
    def tax500 = getRecordId(refBookTaxId, 'CODE', '2012500')
    saveRecords.each {
        // Проверка обязательности заполнения атрибутов в справочнике "Параметры налоговых льгот"
        def String tax = getRefBookValue(refBookTaxId, it.TAX_BENEFIT_ID.referenceValue)?.CODE?.stringValue
        def String errorStr = "Для налоговой льготы «${tax}» поле «%s» является обязательным!"
        if (tax in ['20220', '20230']) {
            def String section = it.SECTION?.stringValue
            def String item = it.ITEM?.stringValue
            def String subitem = it.SUBITEM?.stringValue
            if (section == null || section == '') {
                logger.error(errorStr, 'Основание - статья')
            }
            if (item == null || item == '') {
                logger.error(errorStr, 'Основание - пункт')
            }
            if (subitem == null || subitem == '') {
                logger.error(errorStr, 'Основание - подпункт')
            }
        }
        if (tax == '2012400') {
            def Number rate = it.RATE?.numberValue
            if (rate == null) {
                logger.error(errorStr, 'Льготная ставка, %')
            }
        }
        if (tax == '2012500') {
            def Number reductionSum = it.REDUCTION_SUM?.numberValue
            def Number reductionPct = it.REDUCTION_PCT?.numberValue
            if (reductionSum == null && reductionPct == null) {
                logger.error("Для налоговой льготы «${tax}» обязателен к заполнению один из атрибутов " +
                        "«Уменьшение суммы исчисленного налога, руб.» или «Уменьшение суммы исчисленного налога, %%»!")
            }
        }

        // 1. Проверка на заполнение поля «Категория имущества»
        def Number paramDestination = it.PARAM_DESTINATION?.numberValue
        def String assetsCategory = it.ASSETS_CATEGORY?.stringValue
        def boolean categoryIsEmpty = (assetsCategory == null || assetsCategory == '')
        if ((paramDestination == 1 && categoryIsEmpty) || (paramDestination != 1 && !categoryIsEmpty)) {
            logger.error("Категория имущества: атрибут должен быть заполнен только в том случае, если атрибут «Назначение параметра (0 – по средней, 1 – категория, 2 – по кадастровой)» равен значению «1»!")
        }

        // 2. проверка уникальности записи
        def String filter = "DECLARATION_REGION_ID = ${it.DECLARATION_REGION_ID.referenceValue} AND REGION_ID = ${it.REGION_ID.referenceValue} AND PARAM_DESTINATION = ${paramDestination}"
        if (tax == '2012000' || paramDestination == 2) {
            filter += " AND (TAX_BENEFIT_ID =${tax000} or TAX_BENEFIT_ID =${tax400} or TAX_BENEFIT_ID =${tax500})"
        } else if (tax == '2012400') {
            filter += " AND (TAX_BENEFIT_ID =${tax000} or TAX_BENEFIT_ID =${tax400})"
        } else if (tax == '2012500') {
            filter += " AND (TAX_BENEFIT_ID =${tax000} or TAX_BENEFIT_ID =${tax500})"
        }
        def int recordsCount = getRecordsCount(filter)
        if (recordsCount > (isNewRecords ? 0 : 1)) {
            if (paramDestination != 2) {
                logger.error("В течение одного года по одному и тому же субъекту для параметра " +
                        "«по средней/ по категории» в справочнике может быть только либо одна запись с льготой " +
                        "«2012000»/«2012400»/«2012500» либо две записи с льготой «2012400» и «2012500»!")
            } else {
                logger.error("В течение одного года по одному и тому же субъекту для параметра «по кадастровой» " +
                        "в справочнике может быть только одна запись с льготой «2012000»/«2012400»/«2012500»!")
            }
        }
    }
}

// Количество записей
def int getRecordsCount(def filter) {
    if (recordsCountCache.containsKey(refBookId)) {
        Integer recordsCount = recordsCountCache.get(refBookId).get(filter)
        if (recordsCount != null) {
            return recordsCount
        }
    } else {
        recordsCountCache.put(refBookId, [:])
    }

    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    def provider = providerCache.get(refBookId)

    def records = provider.checkRecordExistence(null, filter)
    def count = 0
    for (def record : records) {
        def versionInfo = provider.getRecordVersionInfo(records.get(0).getFirst())
        def int startYear = Integer.valueOf(formatDate(versionInfo.versionStart, "yyyy"))
        def int endYear = Integer.valueOf(formatDate(versionInfo.versionEnd, "yyyy")?:currentYear)
        if (startYear <= currentYear && (endYear == null || endYear >= currentYear)) {
            count++
        }
    }
    recordsCountCache.get(refBookId).put(filter, count)

    return records.size()
}