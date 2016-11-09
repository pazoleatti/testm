package refbook.tax_benefits_transport

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Скрипт справочника «Параметры налоговых льгот транспортного налога» (id = 7)
 *
 */
switch (formDataEvent) {
    case FormDataEvent.ADD_ROW:
        record.put("DECLARATION_REGION_ID", new RefBookValue(RefBookAttributeType.REFERENCE, departmentService.get(userInfo.getUser().getDepartmentId()).getRegionId()));
        break
    case FormDataEvent.SAVE:
        save()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

void save() {
    saveRecords.each {
        def String tax = getRefBookValue(6, it.TAX_BENEFIT_ID.referenceValue)?.CODE?.stringValue
        def percent = it.PERCENT?.numberValue
        def rate = it.RATE?.numberValue
        def String section = it.SECTION?.stringValue
        def String item = it.ITEM?.stringValue
        def String subitem = it.SUBITEM?.stringValue

        // 1. Проверка корректности заполнения уменьшающего процента
        if (percent != null && !(percent > 0 && percent < 100)) {
            logger.error("Значение поля «%s» должно быть больше 0 и меньше 100", 'Уменьшающий процент, %')
        }
        // 2. Проверка корректности заполнения пониженной ставки
        if (rate != null && !(rate > 0)) {
            logger.error("Значение поля «%s» должно быть больше 0", 'Пониженная ставка')
        }
        // 3. Проверка корректности заполнения кода налоговой льготы
        if (tax && tax.length() > 1 && tax.startsWith('40')) {
            logger.error("Значение поля «%s» не должно содержать кода налогового вычета", 'Код налоговой льготы')
        }
        // 4. Проверка корректности заполнения основания
        def errorStr = []
        if (!['20200', '20210', '20220', '20230'].contains(tax)) {
            if (section) {
                errorStr.add('«Основание - статья»')
            }
            if (item) {
                errorStr.add('«Основание - пункт»')
            }
            if (subitem) {
                errorStr.add('«Основание - подпункт»')
            }
            if (!errorStr.isEmpty()) {
                logger.error("Значения полей: %s для выбранного кода льготы не заполняются", errorStr.join(', '))
            }
        } else{
            if (!section) {
                errorStr.add('«Основание - статья»')
            }
            if (!item) {
                errorStr.add('«Основание - пункт»')
            }
            if (!subitem) {
                errorStr.add('«Основание - подпункт»')
            }
            if (!errorStr.isEmpty()) {
                logger.error("Значения полей: %s для выбранного кода льготы должны быть заполнены", errorStr.join(', '))
            }
        }

        // Заполнение поля "Основание"
        if (!logger.containsLevel(LogLevel.ERROR) && ['20200', '20210', '20220', '20230'].contains(tax)) {
            it.BASE.value = fill(section) + fill(item) + fill(subitem)
        }
    }
}

def fill(def str) {
    return "0000".substring(str.length()) + str
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}