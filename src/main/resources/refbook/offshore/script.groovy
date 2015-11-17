/*
    blob_data.id = 'a396c647-2b0a-4124-9831-5b44e625e69e'
 */
package refbook.offshore

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Скрипт справочника «Оффшорные зоны» (id = 519)
 *
 * @author Lhaziev
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}


@Field
def refBookCache = [:]

def getRecord(def refBookId, def recordId) {
    if (refBookCache[getRefBookCacheKey(refBookId, recordId)] != null) {
        return refBookCache[getRefBookCacheKey(refBookId, recordId)]
    } else {
        def provider = refBookFactory.getDataProvider(refBookId)
        def value = provider.getRecordData(recordId)
        refBookCache.put(getRefBookCacheKey(refBookId, recordId), value)
        return value
    }
}

void save() {
    saveRecords.each {
        def Long code = it.CODE?.referenceValue
        def Long offshoreCode = it.OFFSHORE_CODE?.referenceValue
        it.CODE_2.setValue(code)
        it.CODE_3.setValue(code)
        if (code != null) {
            def record = getRecord(10L, code)
            it.SHORTNAME.setValue(record?.NAME.stringValue)
            it.NAME.setValue(record?.FULLNAME.stringValue)
        }
        it.OFFSHORE_NAME.setValue(offshoreCode)
    }
}