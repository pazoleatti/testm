package refbook.tax_benefits_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Cкрипт справочника «Параметры налоговых льгот транспортного налога» (id = 7)
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
def refBookCache = [:]

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
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}