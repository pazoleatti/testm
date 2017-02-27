package refbook.person

import com.aplana.sbrf.taxaccounting.model.PersonData
import groovy.transform.Field

/**
 * Cкрипт справочника "Физические лица" (id = 904).
 * ref_book_id = 904
 */

switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

// Кэш провайдеров
@Field def providerCache = [:]

// Документ, удостоверяющий личность (ДУЛ)
@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// ИНП
@Field final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id

/**
 * Создадим дубли ДУЛ и ИНП с привязкой к новой записи
 */
void save() {

    // Если работаем с версиями записей
    if (sourceUniqueRecordId && uniqueRecordId && isNewRecords) {

        // Перенесем ДУЛ из старой версии в новую
        def oldRefDulList = getRefDul(sourceUniqueRecordId)
        List<RefBookRecord> newRefDulList = new ArrayList<RefBookRecord>()
        if (oldRefDulList && !oldRefDulList.isEmpty()) {
            oldRefDulList.each { oldRefDul ->
                newRefDulList.add(createIdentityDocRecord(oldRefDul))
            }
        }
        if (!newRefDulList.isEmpty()) {
            List<Long> docIds = getProvider(REF_BOOK_ID_DOC_ID).createRecordVersionWithoutLock(logger, validDateFrom, null, newRefDulList)
            logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size());
        }

        // Перенесем ИНП из старой версии в новую
        def oldRefInpList = getRefInp(sourceUniqueRecordId)
        List<RefBookRecord> newRefInpList = new ArrayList<RefBookRecord>()
        if (oldRefInpList && !oldRefInpList.isEmpty()) {
            oldRefInpList.each { oldRefInp ->
                newRefInpList.add(createIdentityTaxpayerRecord(oldRefInp))
            }
        }
        if (!newRefInpList.isEmpty()) {
            List<Long> docIds = getProvider(REF_BOOK_ID_TAX_PAYER_ID).createRecordVersionWithoutLock(logger, validDateFrom, null, newRefInpList)
            logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size());
        }
    }
}

/**
 * Документы, удостоверяющие личность
 */
RefBookRecord createIdentityDocRecord(def oldRefDul) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillIdentityDocAttr(values, oldRefDul);
    record.setValues(values);
    return record;
}

/**
 * Идентификаторы физлиц
 */
RefBookRecord createIdentityTaxpayerRecord(def oldRefInp) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillIdentityTaxpayerRecord(values, oldRefInp);
    record.setValues(values);
    return record;
}

/**
 * Заполнение аттрибутов справочника документов
 * @param values карта для хранения значений атрибутов
 * @param person класс предоставляющий данные для заполнения справочника
 * @return
 */
def fillIdentityDocAttr(Map<String, RefBookValue> values, def oldRefDul) {
    putOrUpdate(values, "PERSON_ID", RefBookAttributeType.REFERENCE, uniqueRecordId);
    putOrUpdate(values, "DOC_NUMBER", RefBookAttributeType.STRING, oldRefDul?.DOC_NUMBER?.stringValue);
    putOrUpdate(values, "ISSUED_BY", RefBookAttributeType.STRING, oldRefDul?.ISSUED_BY?.stringValue);
    putOrUpdate(values, "ISSUED_DATE", RefBookAttributeType.DATE, oldRefDul?.ISSUED_DATE?.dateValue);
    //Признак включения в отчет, при создании ставиться 1, при обновлении надо выбрать с минимальным приоритетом
    putOrUpdate(values, "INC_REP", RefBookAttributeType.NUMBER, 1);
    putOrUpdate(values, "DOC_ID", RefBookAttributeType.REFERENCE, oldRefDul?.DOC_ID?.referenceValue);
}

/**
 * Заполнение аттрибутов справочника Идентификаторы физлиц
 * @param person
 * @param asnuId
 * @return
 */
def fillIdentityTaxpayerRecord(Map<String, RefBookValue> values, def oldRefInp) {
    putOrUpdate(values, "PERSON_ID", RefBookAttributeType.REFERENCE, uniqueRecordId);
    putOrUpdate(values, "INP", RefBookAttributeType.STRING, oldRefInp?.INP?.stringValue);
    putOrUpdate(values, "AS_NU", RefBookAttributeType.REFERENCE, oldRefInp?.AS_NU?.referenceValue);
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 * @return 0 - изменений нет, 1-создание записи, 2 - обновление
 */
def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value) {
    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи, если новое значение задано и отличается от существующего
        Object currentValue = refBookValue.getValue();
        if (value != null && !ScriptUtils.equalsNullSafe(currentValue, value)) {
            //значения не равны, обновление
            refBookValue.setValue(value);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
    }
}

/************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

/**
 * Получить все записи справочника по его идентификатору и фильтру (отсутствие значений не является ошибкой)
 * @param refBookId - идентификатор справочника
 * @param filter - фильтр
 * @return - возвращает лист
 */
def getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(null, null, filter, null)
    return refBookList
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 * @return
 */
def getRefDul(def long personId) {
    return getRefBookByFilter(REF_BOOK_ID_DOC_ID, "PERSON_ID = " + personId)
}

/**
 * Получить "ИНП"
 * @return
 */
def getRefInp(def long personId) {
    return getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId)
}

/**
 * Получить "ИНП"
 * @return
 */

/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}