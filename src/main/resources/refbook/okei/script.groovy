package refbook.okei

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «Коды единиц измерения на основании ОКЕИ» (id = 12)
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
        def pattern = /[0-9]{3}/
        if (!(code ==~ pattern)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код", code, pattern)
        }
    }
}
