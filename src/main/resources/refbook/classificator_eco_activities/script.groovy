package refbook.classificator_eco_activities

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «Общероссийский классификатор видов экономической деятельности» (id = 34)
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
        def patterns = [/[0-9]{2}\.[0-9]{2}\.[0-9]{2}/, /[0-9]{2}/, /[0-9]{2}\.[0-9]{1}/, /[0-9]{2}\.[0-9]{2}/, /[0-9]{2}\.[0-9]{2}\.[0-9]{1}/]
        if (patterns.find { code ==~ it } == null) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, patterns.join("\" / \""))
        }
    }
}
