package refbook.person

import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import groovy.transform.Field
import groovy.transform.TypeChecked

/**
 * Cкрипт справочника "Документы, удостоверяющие личность" (id = 902).
 */

println "run script formDataEvent=" + formDataEvent

switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
    case FormDataEvent.DELETE:
        println "delete"

}

@Field
final Long uniqueRecordId = getProperty("uniqueRecordId")

def getProperty(String name) {
    try{
        return super.getProperty(name)
    } catch (MissingPropertyException e) {
        return null
    }
}

/**
 * Создадим дубли ДУЛ и ИНП с привязкой к новой записи
 * sourceUniqueRecordId - идентификатор старой записи, null при редактировании
 * uniqueRecordId - идентификатор новой записи
 * isNewRecords - признак новой записи
 */
void save() {
    RefBookDataProvider dataProvider = refBookFactory.getDataProvider(RefBook.Id.ID_DOC.getId());

    //Проверяем если
    for (Map<String, RefBookValue> attrValues : saveRecords) {
        Long personId = attrValues.get("PERSON_ID")?.getReferenceValue();

        if (personId == null) {
            throw new ServiceException("Ошибка заполнения атрибутов справочника 'Документы, удостоверяющие личность': отсутствует значение атрибута 'Физическое лицо'")
        }

        Integer includeReportAttrValue = attrValues.get("INC_REP")?.getNumberValue()?.intValue();

        List<Map<String, RefBookValue>> personDocuments = getPersonDocuments(personId);

        if (includeReportAttrValue != null) {
            if (includeReportAttrValue == 1) {
                //Если у редактируемого или создаваемого документа установлен признак включения в отчетность 1, то у остальных документов должен быть выставлен признак 0

                for (Map<String, RefBookValue> documentsAttrValues : personDocuments) {
                    Long documentUniqueRecordId = documentsAttrValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
                    //Если задан uniqueRecordId, то надо исключить этот id из списка
                    if (!documentUniqueRecordId.equals(uniqueRecordId)) {
                        Integer documentIncRepValue = documentsAttrValues.get("INC_REP")?.getNumberValue()?.intValue();
                        if (documentIncRepValue == null || documentIncRepValue == 1) {
                            documentsAttrValues.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 0));
                            documentsAttrValues.remove(RefBook.RECORD_VERSION_FROM_ALIAS);
                            getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, documentUniqueRecordId, validDateFrom, null, documentsAttrValues);
                        }
                    }
                }
            }
        } else {
            //значение не задано ставим 0 по умолчанию
            attrValues.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        }

        //проверка
        checkIncludeReportDocument(personDocuments, includeReportAttrValue == 1);
    }
}

@TypeChecked
int checkIncludeReportDocument(List<Map<String, RefBookValue>> personDocuments, boolean includeReportAttrValue1) {
    int incToRepCnt = includeReportAttrValue1?1:0

    for (Map<String, RefBookValue> documentsAttrValues : personDocuments) {
        Long documentUniqueRecordId = documentsAttrValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        if (documentUniqueRecordId == uniqueRecordId) {
            continue
        }
        Integer documentIncRepValue = documentsAttrValues.get("INC_REP")?.getNumberValue()?.intValue();
        if (documentIncRepValue != null && documentIncRepValue == 1) {
            incToRepCnt++;
        }
    }

    if (incToRepCnt > 1) {
        throw new ServiceException("Ошибка заполнения справочника 'Документы, удостоверяющие личность': для физического лица должен быть определен только один документ с признаком 'включается в отчетность' = 1")
    }
    return incToRepCnt
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 * @return
 */
@TypeChecked
PagingResult<Map<String, RefBookValue>> getPersonDocuments(long personId) {
    return getRefBookValuesByFilter(RefBook.Id.ID_DOC.getId(), "PERSON_ID = " + personId)
}

@TypeChecked
PagingResult<Map<String, RefBookValue>> getRefBookValuesByFilter(long refBookId, String filter) {
    return getProvider(refBookId).getRecords(null, null, filter, null)
}

@Field def providerCache = [:]

/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
RefBookDataProvider getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}