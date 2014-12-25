package refbook.declaration_params_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника "Параметры представления деклараций по транспортному налогу" (id = 210)
 *
 * @author Alexey Afanasyev
 */

switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def String taxOrganCode = it.TAX_ORGAN_CODE?.stringValue
        def String kpp = it.KPP?.stringValue
        // Проверка поля «Код налогового органа» на корректность формата введенных данных
        if (!checkFormat(taxOrganCode, "[0-9]{4}")) {
            logger.error("Поле «Код налогового органа» должно быть заполнено согласно формату «[0-9]{4}»")
        }
        // Проверка поля «КПП» на корректность формата введенных данных
        if (!checkFormat(kpp, "([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})")) {
            logger.error("Поле «КПП» должно быть заполнено согласно формату «([0-9]{1}[1-9]{1}|[1-9]{1}[0-9]{1})([0-9]{2})([0-9A-Z]{2})([0-9]{3})»")
        }
    }
}
