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
        def code = it.CODE?.stringValue
        def code2 = it.CODE_2?.stringValue
        def code3 = it.CODE_3?.stringValue
        def pattern = /[0-9]{3}/
        if (code && !(code ==~ pattern)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, pattern)
        }
        if (code2?.length() != 2 || code2?.trim()?.length() != 2) {
            logger.error("Поле «Код (2-х букв.)» должно содержать 2 символа.")
        }
        if (code3?.length() != 3 || code3?.trim()?.length() != 3) {
            logger.error("Поле «Код (3-х букв.)» должно содержать 3 символа.")
        }
    }
}
