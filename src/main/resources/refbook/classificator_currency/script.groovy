package refbook.classificator_currency

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника «Общероссийский классификатор валют» (id = 15)
 *
 * @author Ellina Mamedova
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def code = it.CODE?.stringValue
        if (code!=null && code!="" && !code.isNumber()) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Значение должно содержать только цифры!", "Код", code)
        }
    }
}
