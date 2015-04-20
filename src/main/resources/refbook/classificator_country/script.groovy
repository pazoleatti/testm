package refbook.classificator_country

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «ОК 025-2001 (Общероссийский классификатор стран мира)» (id = 10)
 *
 * @author Bulat Kinzyabulatov
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def code2 = it.CODE_2?.stringValue
        def code3 = it.CODE_3?.stringValue
        if (code2?.length() != 2 || code2?.trim()?.length() != 2) {
            logger.error("Поле «Код (2-х букв.)» должно содержать 2 символа.")
        }
        if (code3?.length() != 3 || code3?.trim()?.length() != 3) {
            logger.error("Поле «Код (3-х букв.)» должно содержать 3 символа.")
        }
    }
}
