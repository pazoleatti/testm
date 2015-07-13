package refbook.declaration_params_property

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

/**
 * Cкрипт справочника "Параметры представления деклараций по налогу на имущество" (id = 200)
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
        if (taxOrganCode && !(taxOrganCode ==~ TAX_ORGAN_PATTERN)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "Код налогового органа", kpp, TAX_ORGAN_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", TAX_ORGAN_PATTERN, TAX_ORGAN_MEANING)
        }
        // Проверка поля «КПП» на корректность формата введенных данных
        if (kpp && !(kpp ==~ KPP_PATTERN)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", "КПП", kpp, KPP_PATTERN)
            logger.error("Расшифровка паттерна «%s»: %s.", KPP_PATTERN, KPP_MEANING)
        }
    }
}


