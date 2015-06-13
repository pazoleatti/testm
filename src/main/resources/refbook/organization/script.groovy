/*
    blob_data.id = 'ba9bb7ca-697c-b0c2-9999-e262617A9784'
 */
package refbook.organization

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Cкрипт справочника «Организации - участники контролируемых сделок» (id = 9)
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
        def String inn = it.INN_KIO?.stringValue
        def String kpp = it.KPP?.stringValue
        def Long organization = getRefBookValue(70, it.ORGANIZATION?.referenceValue)?.CODE?.numberValue
        if (organization == 1 && (inn == null || inn == '')) {
            logger.error('Для организаций РФ атрибут «ИНН» является обязательным')
        }
        if (checkPattern(logger, null, null, inn, INN_JUR_PATTERN, INN_JUR_MEANING,true)) {
            checkControlSumInn(logger, null, null, inn, true)
        }
        checkPattern(logger, null, null, kpp, KPP_PATTERN, KPP_MEANING, true)
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}