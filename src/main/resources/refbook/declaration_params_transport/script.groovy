package refbook.declaration_params_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

/**
 * Cкрипт справочника "Параметры представления деклараций по транспортному налогу" (id = 210)
 *
 * @author Alexey Afanasyev
 */

switch (formDataEvent) {
    case FormDataEvent.ADD_ROW:
        record.put("DECLARATION_REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentService.get(userInfo.getUser().getDepartmentId()).getRegionId()));
        break
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
