package form_template.income.rnu107

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
/**
 * 502 - (РНУ-107) Регистр налогового учёта доходов, возникающих в связи с применением в сделках с Взаимозависимыми лицами и резидентами оффшорных зон тарифов, не соответствующих рыночному уровню
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
//        checkCreation()
        break
    case FormDataEvent.CHECK:
//        logicalCheck()
        break
    case FormDataEvent.CALCULATE:
//        calc()
//        logicalCheck()
        break
    case FormDataEvent.ADD_ROW:
//        addNewRow()
        break
    case FormDataEvent.DELETE_ROW:
//        deleteRow()
        break
// После принятия из Утверждено
    case FormDataEvent.AFTER_MOVE_APPROVED_TO_ACCEPTED:
//        logicalCheck()
        break
// После принятия из Подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
//        logicalCheck()
        break
// Консолидация
    case FormDataEvent.COMPOSE:
//        consolidation()
//        calc()
//        logicalCheck()
        break
}