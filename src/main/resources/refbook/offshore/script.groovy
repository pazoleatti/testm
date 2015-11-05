/*
    blob_data.id = 'a396c647-2b0a-4124-9831-5b44e625e69e'
 */
package refbook.offshore

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

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

void save() {
    saveRecords.each {
        def Long code = it.CODE?.referenceValue
        def Long offshoreCode = it.OFFSHORE_CODE?.referenceValue
        it.CODE_2.setValue(code)
        it.CODE_3.setValue(code)
        it.OFFSHORE_NAME.setValue(offshoreCode)
    }
}