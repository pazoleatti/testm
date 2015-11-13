package refbook.tax_benefits_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Скрипт справочника «Параметры налоговых льгот транспортного налога» (id = 7)
 *
 * @author LHaziev
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

@Field
def String errorStr = "Для налоговой льготы «%s» поле «%s» является обязательным!"

void save() {
    saveRecords.each {
        def String tax = getRefBookValue(6, it.TAX_BENEFIT_ID.referenceValue)?.CODE?.stringValue
        def String percent = it.PERCENT?.numberValue
        def String rate = it.RATE?.numberValue
        // 1. проверка обязательности заполнения атрибутов в справочнике "Параметры налоговых льгот"
        if (tax in ['20200', '20210', '20220', '20230']) {
            def String section = it.SECTION?.stringValue
            def String item = it.ITEM?.stringValue
            def String subitem = it.SUBITEM?.stringValue
            if (section == null || section == '') {
                logger.error(errorStr, tax, 'Основание - статья')
            }
            if (item == null || item == '') {
                logger.error(errorStr, tax, 'Основание - пункт')
            }
            if (subitem == null || subitem == '') {
                logger.error(errorStr, tax, 'Основание - подпункт')
            }
        }
        if (tax == '20220' && percent == null) {
            logger.error(errorStr, tax, 'Уменьшающий процент, %')
        }
        if (tax == '20230' && rate == null) {
            logger.error(errorStr, tax, 'Пониженная ставка')
        }
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}