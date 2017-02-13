package refbook.classificator_okved

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «Общероссийский классификатор видов экономической деятельности» (id = 925)
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
        if (code && patterns.find { code ==~ it } == null) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, patterns.join("\" / \""))
        }
    }
}
