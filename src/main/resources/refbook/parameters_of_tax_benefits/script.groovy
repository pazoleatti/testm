package refbook.parameters_of_tax_benefits

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Cкрипт справочника «Параметры налоговых льгот» (id = 7)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordsCountCache = [:]
@Field
def refBookCache = [:]

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

void save() {
    saveRecords.each {
        // 1. проверка обязательности заполнения атрибутов в справочнике "Параметры налоговых льгот"
        def String tax = getRefBookValue(6, it.TAX_BENEFIT_ID.referenceValue)?.CODE?.stringValue
        if (tax in ['20220', '20230']) {
            def String section = it.SECTION?.stringValue
            def String item = it.ITEM?.stringValue
            def String subitem = it.SUBITEM?.stringValue
            def String errorStr = "Для налоговой льготы «${tax}» поле «%s» является обязательным!"
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

        // 2. проверка уникальности записи
        def String query = "TAX_BENEFIT_ID =" + it.TAX_BENEFIT_ID.referenceValue + " AND DICT_REGION_ID = " + it.DICT_REGION_ID.referenceValue
        def int recordsCount = getRecordsCount(7, query, validDateFrom)
        if (recordsCount > (isNewRecords ? 0 : 1)) {
            logger.error("Атрибуты «Код региона» и «Код налоговой льготы» не уникальны!")
        }
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Количество записей
def int getRecordsCount(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return 0
    }
    String dateStr = sdf.format(date)
    if (recordsCountCache.containsKey(refBookId)) {
        Integer recordsCount = recordsCountCache.get(refBookId).get(dateStr + filter)
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

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    recordsCountCache.get(refBookId).put(dateStr + filter, records.size())

    return records.size()
}