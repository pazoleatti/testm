package refbook.classificator_outcome

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Cкрипт справочника «Классификатор расходов Сбербанка России для целей налогового учёта» (id = 27)
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

@Field
def REF_BOOK_ID = 27

@Field
def provider

void save() {
    def refbook = refBookFactory.get(REF_BOOK_ID)
    provider = refBookFactory.getDataProvider(REF_BOOK_ID)
    def Date start = Date.parse('dd.MM.yyyy', '01.01.2016')

    saveRecords.each {
        def String balanceAccount = it.BALANCE_ACCOUNT?.stringValue
        def String opu = it.OPU?.stringValue
        def String number = ((balanceAccount ?: '') + (opu ?: '')).replaceAll('\\.', '')
        it.NUMBER = new RefBookValue(RefBookAttributeType.STRING, number)

        // Уникальность «КНУ» (с 01.01.2016)
        if (validDateFrom >= start && !checkUnique('CODE', it.CODE?.value, start)) {
            logger.error('В справочнике уже существует запись с заданным значением поля «%s»!', refbook.getAttribute('CODE').getName())
        }
    }
}

def checkUnique(String alias, String value, Date date, def defaultFilter = null) {
    if (value != null) {
        String filter = (defaultFilter ?: "LOWER($alias) = LOWER('$value')")
        def pairs = provider.getRecordIdPairs(REF_BOOK_ID, date, false, filter)
        for(def pair: pairs) {
            if (recordCommonId && pair.second == recordCommonId) {
                // проверка при создании новой версии, пропускаем элементы версии
                continue
            } else if (!recordCommonId && pair.first == uniqueRecordId) {
                // проверка при создания нового/сохранении существующего элемента
                continue
            }
            def record = provider.getRecordVersionInfo(pair.first)
            Date fromDate = record.versionStart
            Date toDate = record.versionEnd
            if ((validDateTo == null || fromDate.compareTo(validDateTo) <= 0) &&
                    (toDate == null || toDate.compareTo(validDateFrom) >= 0 )) {
                return false
            }
        }
    }
    return true
}