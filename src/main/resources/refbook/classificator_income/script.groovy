package refbook.classificator_income

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue

/**
 * Cкрипт справочника «Классификатор доходов Сбербанка России для целей налогового учёта» (id = 28)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

void save() {
    saveRecords.each {
        def String balanceAccount = it.BALANCE_ACCOUNT?.stringValue
        def String opu = it.OPU?.stringValue
        it.NUMBER = new RefBookValue(RefBookAttributeType.STRING, (balanceAccount ?: '') + (opu ?: ''))
    }
}