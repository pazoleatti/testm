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

@Field
def innPattern = /([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})[0-9]{8}/;
@Field
def kppPattern = /([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})/;

void save() {
    saveRecords.each {
        def String inn = it.INN_KIO?.stringValue
        def String kpp = it.KPP?.stringValue
        def Long organization = getRefBookValue(70, it.ORGANIZATION?.referenceValue)?.CODE?.numberValue
        if (organization == 1 && (inn == null || inn == '')) {
            logger.error('Для организаций РФ атрибут «ИНН» является обязательным')
        }
        if (checkPattern(logger, null, null, inn, innPattern, true)) {
            checkControlSumInn(logger, null, null, inn, true)
        }
        checkPattern(logger, null, null, kpp, kppPattern, true)
    }
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}