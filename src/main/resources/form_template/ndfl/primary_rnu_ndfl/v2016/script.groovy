package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.model.PersonData
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.model.util.StringUtils

// com.aplana.sbrf.taxaccounting.refbook.* - используется для получения id-справочников
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.util.slurpersupport.NodeChild
import groovy.xml.MarkupBuilder

import javax.script.ScriptException
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.Characters
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

/**
 * Справочник "Коды, определяющие налоговый (отчётный) период"
 */
@Field
def PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importData()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport()
        break
    case FormDataEvent.CHECK:
        checkData()
        break
    case FormDataEvent.CALCULATE:
        calculate()
        //checkData()
        break
}

//------------------ Calculate ----------------------

/**
 * Порог схожести при идентификации физлиц 0..1000, 1000 - совпадение по всем параметрам
 */
@Field
def SIMILARITY_THRESHOLD = 700;

def calculate() {

    def asnuId = declarationData.asnuId;

    if (asnuId == null) {
        throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!");
    }

    //выставляем параметр что скрипт не формирует новый xml-файл
    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)

    logger.info("Рассчет данных. Общее количество записей о физ. лицах: " + ndflPersonList.size());

    //Два списка для создания новых записей и для обновления существующих
    List<NdflPerson> createdPerson = new ArrayList<NdflPerson>();
    List<NdflPerson> updatedPerson = new ArrayList<NdflPerson>();
    def updCnt = 0, createCnt = 0;

    for (NdflPerson ndflPerson : ndflPersonList) {

        PersonData personData = createPersonData(ndflPerson, asnuId);

        Long refBookPersonId = refBookPersonService.identificatePerson(personData, SIMILARITY_THRESHOLD);

        ndflPerson.setPersonId(refBookPersonId)

        if (refBookPersonId != null) {
            //обновление записи
            updatedPerson.add(ndflPerson);
            updCnt++;
        } else {
            //Новые записи помещаем в список для пакетного создания
            createdPerson.add(ndflPerson);
            createCnt++;
        }
    }

    //Создание справочников
    if (createdPerson != null && !createdPerson.isEmpty()) {
        createRefbookPersonData(createdPerson, asnuId);
    }

    //Обновление справочников
    if (updatedPerson != null && !updatedPerson.isEmpty()) {
        updateRefbookPersonData(updatedPerson, asnuId);
    }

    //Обновление данных декларации
    ndflPersonService.updatePersonRefBookReferences(ndflPersonList);

    logger.info("Рассчет завешен. Записей обработано " + ndflPersonList.size() + ".  В справочнике 'Физические лица' записей создано: " + createCnt + ", записей обновлено: " + updCnt);
}

/**
 * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
 * @param personMap
 * @return
 */
def getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    Map<Long, Map<String, RefBookValue>> addressMap = Collections.emptyMap();
    def addressIds = [];
    personMap.each { personId, person ->
        if (person.get(RF_ADDRESS).value != null) {
            addressIds.add(person.get(RF_ADDRESS).value)
        }
    }

    if (addressIds.size() > 0) {
        addressMap = getRefAddress(addressIds)
        logger.info(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
    }
    return addressMap;
}

def updateRefbookPersonData(List<NdflPerson> personList, Long asnuId) {

    logger.info("Подготовка к обновлению данных по физлицам: " + personList.size() + ")");

    Date versionFrom = getVersionFrom();

    List<Long> personIds = getPersonIds(personList);

    //-----<INITIALIZE_CACHE_DATA>-----
    //PersonId : Физлица
    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefPersons(personIds);
    //Id : Адрес
    Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPerson);
    //PersonId : UniqId: ИНП
    Map<Long, List<Map<String, RefBookValue>>> inpMap = getRefInpMap(personIds)
    //PersonId :  UniqId:Документы
    Map<Long, List<Map<String, RefBookValue>>> identityDocMap = getRefDul(personIds)
    //-----<INITIALIZE_CACHE_DATA_END>-----

    for (NdflPerson person : personList) {
        def personId = person.getPersonId();

        Map<String, RefBookValue> refBookPersonValues = refBookPerson.get(personId);
        def addressId = refBookPersonValues.get("ADDRESS")?.getReferenceValue();

        if (addressMap.containsKey(addressId)) {
            Map<String, RefBookValue> addressValues = addressMap.get(addressId);
            fillAddressAttr(addressValues, person);
            getProvider(RefBook.Id.PERSON_ADDRESS.getId()).updateRecordVersionWithoutLock(logger, addressId, versionFrom, null, addressValues);
        } else {
            RefBookRecord refBookRecord = creatAddressRecord(person);
            List<Long> addressIds = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, Arrays.asList(refBookRecord));
            addressId = addressIds.first();
        }

        updatePersonRecord(refBookPersonValues, person, asnuId, addressId);

        getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, personId, versionFrom, null, refBookPersonValues);

        //Обновление списка документов
        //Проверка, если задан номер и тип документа
        if (person.getIdDocNumber() != null && !person.getIdDocNumber().isEmpty() && person.getIdDocType() != null && !person.getIdDocType().isEmpty()) {
            updateIdentityDocRecords(identityDocMap.get(personId), person);
        }

        //Обновление идентификаторов АСНУ - ИНП
        updateTaxpayerIdentity(inpMap.get(personId), person, asnuId);

    }
}

/**
 * По списку
 * @param personList
 * @return
 */
def createRefbookPersonData(List<NdflPerson> personList, Long asnuId) {

    logger.info("Подготовка к созданию данных по физ.лицам: " + personList.size());

    List<RefBookRecord> addressRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        NdflPerson person = personList.get(i)
        RefBookRecord refBookRecord = creatAddressRecord(person);
        addressRecords.add(refBookRecord);
    }

    List<Long> addressIds = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, addressRecords)
    logger.info("В справочнике 'Адреса физических лиц' создано записей: " + addressIds.size());

    //создание записей справочника физлиц
    List<RefBookRecord> personRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        Long addressId = addressIds.get(i);
        NdflPerson person = personList.get(i)
        RefBookRecord refBookRecord = createPersonRecord(person, asnuId, addressId);
        personRecords.add(refBookRecord);
    }

    //сгенерированные идентификаторы справочника физлиц
    List<Long> personIds = getProvider(RefBook.Id.PERSON.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, personRecords)
    logger.info("В справочнике 'Физические лица' создано записей: " + personIds.size());

    //создание записей справочников документы и идентфикаторы физлиц
    List<RefBookRecord> documentsRecords = new ArrayList<RefBookRecord>()
    List<RefBookRecord> taxpayerIdsRecords = new ArrayList<RefBookRecord>()
    for (int i = 0; i < personList.size(); i++) {
        NdflPerson person = personList.get(i)
        person.setPersonId(personIds.get(i)); // выставляем присвоенный Id
        documentsRecords.add(createIdentityDocRecord(person));
        taxpayerIdsRecords.add(createIdentityTaxpayerRecord(person, asnuId));
    }

    List<Long> docIds = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, documentsRecords)


    List<Long> taxIds = getProvider(RefBook.Id.ID_TAX_PAYER.getId()).createRecordVersionWithoutLock(logger, versionFrom, null, taxpayerIdsRecords)

    logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size());
    logger.info("В справочнике 'Идентификаторы физических лиц' создано записей: " + taxIds.size());

}

/**
 * Создание новой записи справочника адреса физлиц
 * @param person
 * @return
 */
RefBookRecord creatAddressRecord(NdflPerson person) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillAddressAttr(values, person)
    record.setValues(values);
    return record;
}

/**
 * Заполнение записи справочника адреса физлиц
 * @param values
 * @param person
 * @return
 */
def fillAddressAttr(Map<String, RefBookValue> values, NdflPerson person) {


    int addressType = person.getAddress() != null && !person.getAddress().isEmpty() ? 1 : 0;
    //Тип адреса. Значения: 0 - в РФ 1 - вне РФ
    Long countryId = findCountryId(person.getCountryCode())  //код страны проживания не РФ

    putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, addressType);
    putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, countryId);
    putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, person.getRegionCode());
    putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, person.getArea());
    putOrUpdate(values, "CITY", RefBookAttributeType.STRING, person.getCity());
    putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, person.getLocality());
    putOrUpdate(values, "STREET", RefBookAttributeType.STRING, person.getStreet());
    putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, person.getHouse());
    putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, person.getBuilding());
    putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, person.getFlat());
    putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, person.getPostIndex());
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, person.getAddress());
}

/**
 * Создание новой записи справочника физлиц
 * @param person класс предоставляющий данные для заполнения справочника
 * @param asnuId идентификатор АСНУ в справочнике АСНУ
 * @return запись справочника
 */
RefBookRecord createPersonRecord(NdflPerson person, Long asnuId, Long addressId) {
    RefBookRecord refBookRecord = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillNdflPersonAttr(values, person, asnuId, addressId);
    refBookRecord.setValues(values);
    return refBookRecord;
}

/**
 * Обновление атрибутов записи справочника физлиц
 * @param values
 * @param person
 * @param asnuId
 * @param addressId
 * @return
 */
def updatePersonRecord(Map<String, RefBookValue> values, NdflPerson person, Long asnuId, Long addressId) {
    fillNdflPersonAttr(values, person, asnuId, addressId);
}

/**
 * Заполнение аттрибутов справочника физлиц
 * @param values карта для хранения значений атрибутов
 * @param person класс предоставляющий данные для заполнения справочника
 * @param asnuId ссылка на справочник АСНУ
 * @param addressId ссылка на справочник адреса физлиц
 * @return
 */
def fillNdflPersonAttr(Map<String, RefBookValue> values, NdflPerson person, Long asnuId, Long addressId) {

    Long countryId = findCountryId(person.getCitizenship());
    Long statusId = findTaxpayerStatusByCode(person.getStatus());

    putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName());
    putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName());
    putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName());
    putOrUpdate(values, "SEX", RefBookAttributeType.STRING, null);
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInnNp());
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign());
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils());
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
    putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDay());
    putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, addressId);
    putOrUpdate(values, "PENSION", RefBookAttributeType.NUMBER, 2);
    putOrUpdate(values, "MEDICAL", RefBookAttributeType.NUMBER, 2);
    putOrUpdate(values, "SOCIAL", RefBookAttributeType.NUMBER, 2);
    putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, 2);
    putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, countryId);
    putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, statusId);
    putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, asnuId);
    putOrUpdate(values, "OLD_ID", RefBookAttributeType.REFERENCE, null);
}

/**
 * Документы, удостоверяющие личность
 */
RefBookRecord createIdentityDocRecord(NdflPerson person) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    fillIdentityDocAttr(values, person);
    record.setValues(values);
    return record;
}


def updateIdentityDocRecords(List<Map<String, RefBookValue>> identityDocRefBook, NdflPerson person) {

    Map<Long, String> docCodes = getRefDocument()

    //Id типа документа - приоритет,
    Map<Long, Integer> docPriorities = getRefDocumentPriority();

    //Идентификатор типа документа
    Long docTypeId = docCodes.find { it.value == person.getIdDocType() }?.key;

    if (docTypeId != null) {

        //Ищем документ с таким же типом
        Map<String, RefBookValue> findedDoc = identityDocRefBook?.find {
            Long docIdRef = it.get("DOC_ID")?.getReferenceValue();
            String docNumber = it.get("DOC_NUMBER")?.getStringValue();
            docTypeId.equals(docIdRef) && person.getIdDocNumber()?.equalsIgnoreCase(docNumber);
        };

        List<Map<String, RefBookValue>> identityDocRecords = new ArrayList<Map<String, RefBookValue>>();

        if (findedDoc != null) {
            //документ с таким типом и номером существует, ничего не делаем
            return;
        } else {
            RefBookRecord refBookRecord = createIdentityDocRecord(person);
            List<Long> ids = getProvider(RefBook.Id.ID_DOC.getId()).createRecordVersionWithoutLock(logger, getVersionFrom(), null, Arrays.asList(refBookRecord));
            Map<String, RefBookValue> values = refBookRecord.getValues();
            values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, ids.first()));
            identityDocRecords.add(values);
        }

        if (identityDocRefBook != null && !identityDocRefBook.isEmpty()) {
            identityDocRecords.addAll(identityDocRecords);
        }

        //Если документов несколько - выбираем по приоритету какой использовать в отчетах
        if (identityDocRecords.size() > 1) {
            //сбрасываем текушие
            identityDocRecords.each {
                it.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "0"));
            }
            Map<String, RefBookValue> minimalPrior = identityDocRecords.min {
                docPriorities.get(it.get("DOC_ID"));
            }

            minimalPrior?.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "1"));
        }

        //Обновление признака включается в отчетность
        for (Map<String, RefBookValue> identityDocsValues : identityDocRecords) {
            Long uniqueId = identityDocsValues.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
            getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, versionFrom, null, identityDocsValues);
        }

    }
}


def fillIdentityDocAttr(Map<String, RefBookValue> values, NdflPerson person) {

    values.put("PERSON_ID", new RefBookValue(RefBookAttributeType.REFERENCE, person.getPersonId()));
    values.put("DOC_NUMBER", new RefBookValue(RefBookAttributeType.STRING, person.getIdDocNumber()));
    values.put("ISSUED_BY", new RefBookValue(RefBookAttributeType.STRING, null));
    values.put("ISSUED_DATE", new RefBookValue(RefBookAttributeType.DATE, null));
    //Признак включения в отчет, при создании ставиться 1, при обновлении надо выбрать с минимальным приоритетом
    values.put("INC_REP", new RefBookValue(RefBookAttributeType.STRING, "1"));
    values.put("DOC_ID", new RefBookValue(RefBookAttributeType.REFERENCE, findDocumentTypeByCode(person.getIdDocType())));
}

/**
 * Создание записи в справочнике Идентификаторы физлиц
 * @param person
 * @param asnuId
 * @return
 */
RefBookRecord createIdentityTaxpayerRecord(NdflPerson person, Long asnuId) {
    RefBookRecord record = new RefBookRecord();
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putOrUpdate(values, "PERSON_ID", RefBookAttributeType.REFERENCE, person.getPersonId());
    putOrUpdate(values, "INP", RefBookAttributeType.STRING, person.getInp());
    putOrUpdate(values, "AS_NU", RefBookAttributeType.REFERENCE, asnuId);
    record.setValues(values);
    return record;
}

/**
 * Обновление записи в справочнике "Идентификаторы налогоплатильщика"
 * @param taxpayerIdentityRefBook список записей справочника для текущего ФЛ
 * @param person ФЛ
 * @param asnuId id записи справочника АСНУ
 * @return
 */
def updateTaxpayerIdentity(List<Map<String, RefBookValue>> taxpayerIdentityRefBook, NdflPerson person, Long asnuId) {

    //Ищем в списке записей запись с такимже АСНУ, по постановке обновляем только ИНП в рамках одной АСНУ (корректировка)
    Long findedAsnuId = taxpayerIdentityRefBook?.find {
        asnuId.equals(it.get("AS_NU")?.getReferenceValue())
    }?.get("AS_NU")?.getReferenceValue();

    if (findedAsnuId != null) {
        for (Map<String, RefBookValue> refBookValues : taxpayerIdentityRefBook) {
            RefBookValue value = refBookValues.get("AS_NU");
            if (asnuId.equals(value?.getReferenceValue())) {
                //нашли запись с нужной АСНУ, обновляем ИНП
                Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
                putOrUpdate(refBookValues, "INP", RefBookAttributeType.STRING, person.getInp());
                getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getVersionFrom(), null, refBookValues);
            }
        }
    } else {
        //Такой АСНУ нету, создаем новую запиь
        RefBookRecord refBookRecord = createIdentityTaxpayerRecord(person, asnuId);
        getProvider(RefBook.Id.ID_TAX_PAYER.getId()).createRecordVersionWithoutLock(logger, getVersionFrom(), null, Arrays.asList(refBookRecord));
    }
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 */
def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value) {
    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи
        if (value != null) {
            refBookValue.setValue(value);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
    }
}

/**
 * Создание объекта по которуму будет идентифицироваться физлицо в справочнике
 * @param person
 * @return
 */
PersonData createPersonData(NdflPerson person, Long asnuId) {
    PersonData personData = new PersonData();

    personData.lastName = person.getLastName();
    personData.firstName = person.getFirstName();
    personData.middleName = person.getMiddleName();

    personData.inn = person.getInnNp();
    personData.innForeign = person.getInnForeign();
    personData.snils = person.getSnils();
    personData.birthDate = person.getBirthDay();

    personData.taxPayerStatusId = findTaxpayerStatusByCode(person.getStatus());
    personData.citizenshipId = findCountryId(person.getCitizenship());

    //Идентификаторы
    personData.inp = person.getInp();
    personData.asnuId = asnuId;

    //Документы
    personData.documentTypeId = findDocumentTypeByCode(person.getIdDocType());
    personData.documentNumber = person.getIdDocNumber();

    //Выставляем тип адреса getAddress
    int addressType = person.getAddress() != null && !person.getAddress().isEmpty() ? 1 : 0;
    //Тип адреса. Значения: 0 - в РФ 1 - вне РФ

    //Устанавливаем тип адреса, проверяется при сравнении
    personData.addressType = addressType;

    //Адрес в РФ
    personData.regionCode = person.getRegionCode();
    personData.postalCode = person.getPostIndex();
    personData.district = person.getArea();
    personData.city = person.getCity();
    personData.locality = person.getLocality();
    personData.street = person.getStreet();
    personData.house = person.getHouse();
    personData.build = person.getBuilding();
    personData.appartment = person.getFlat();

    //Адрес вре РФ, ставим код страны и сам адрес
    personData.countryId = findCountryId(person.getCountryCode())  //код страны проживания не РФ
    personData.addressIno = person.getAddress();

    return personData;
}

/**
 * По коду статуса налогоплатильщика найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findTaxpayerStatusByCode(code) {
    def taxpayerStatusMap = getRefTaxpayerStatus();
    def result = taxpayerStatusMap.find {
        it.value == code
    }?.key

    if (code != null && !code.isEmpty() && result == null) {
        logger.warn("В справочнике 'Статусы налогоплатильщика' не найдена запись, статус с кодом " + code);
    }
    return result;


}

/**
 * Получить дату которая используется в качестве версии записей справочника
 * @return дата используемая в качестве даты версии справочника
 */
def getVersionFrom() {
    return getReportPeriodStartDate();
}

/**
 * По коду документа найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findDocumentTypeByCode(code) {
    Map<Long, String> documentTypeMap = getRefDocument()
    def result = documentTypeMap.find {
        it.value?.equalsIgnoreCase(code)
    }?.key;
    if (code != null && !code.isEmpty() && result == null) {
        logger.warn("В справочнике 'Виды документов' не найдена запись, вид документа с кодом " + code);
    }
    return result;
}

/**
 * По цифровому коду страны найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findCountryId(countryCode) {
    def citizenshipCodeMap = getRefCitizenship();
    def result = countryCode != null && !countryCode.isEmpty() ? citizenshipCodeMap.find {
        it.value == countryCode
    }?.key : null;
    if (countryCode != null && !countryCode.isEmpty() && result == null) {
        logger.warn("В справочнике 'ОК 025-2001 (Общероссийский классификатор стран мира)' не найдена запись, страна с кодом " + countryCode);
    }
    return result;
}

//------------------ PREPARE_SPECIFIC_REPORT ----------------------

def prepareSpecificReport() {
    PrepareSpecificReportResult result = new PrepareSpecificReportResult();
    List<Column> tableColumns = createTableColumns();
    List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    def rowColumns = createRowColumns()

    //Проверка, подготовка данных
    def params = scriptSpecificReportHolder.subreportParamValues
    def reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

    if (reportParameters.isEmpty()) {
        throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
    }

    def resultReportParameters = [:]
    reportParameters.each { key, value ->
        if (value != null) {
            resultReportParameters.put(key, value)
        }
    }
    PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters);

    if (pagingResult.isEmpty()) {
        throw new ServiceException("По заданным параметрам ни одной записи не найдено: " + resultReportParameters);
    }

    pagingResult.getRecords().each() { ndflPerson ->
        DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null));
        row.getCell("id").setStringValue(ndflPerson.id.toString())
        row.lastName = ndflPerson.lastName
        row.firstName = ndflPerson.firstName
        row.middleName = ndflPerson.middleName
        row.snils = ndflPerson.snils
        row.innNp = ndflPerson.innNp
        row.birthDay = ndflPerson.birthDay
        row.idDocNumber = ndflPerson.idDocNumber
        dataRows.add(row)
    }

    result.setTableColumns(tableColumns);
    result.setDataRows(dataRows);
    scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
    scriptSpecificReportHolder.setSubreportParamValues(params)
}

def createTableColumns() {
    List<Column> tableColumns = new ArrayList<Column>()

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("snils")
    column4.setName("СНИЛС")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("innNp")
    column5.setName("ИНН")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6= new DateColumn()
    column6.setAlias("birthDay")
    column6.setName("Дата рождения")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("idDocNumber")
    column7.setName("ДУЛ")
    column7.setWidth(10)
    tableColumns.add(column7)

    return tableColumns;
}

def createRowColumns() {
    List<Column> tableColumns = new ArrayList<Column>();

    Column columnId = new StringColumn()
    columnId.setAlias("id")
    columnId.setName("id")
    columnId.setWidth(10)
    tableColumns.add(columnId)

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("snils")
    column4.setName("СНИЛС")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("innNp")
    column5.setName("ИНН")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6= new DateColumn()
    column6.setAlias("birthDay")
    column6.setName("Дата рождения")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new StringColumn()
    column7.setAlias("idDocNumber")
    column7.setName("ДУЛ")
    column7.setWidth(10)
    tableColumns.add(column7)

    return tableColumns;
}

//------------------ Create Report ----------------------

def createSpecificReport() {

    def params = scriptSpecificReportHolder.subreportParamValues
    def row = scriptSpecificReportHolder.getSelectedRecord()
    def ndflPerson = ndflPersonService.get(Long.parseLong(row.id))

    if (ndflPerson != null) {
        //формирование отчета
        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
            calculateReportData(it, ndflPerson)
        });

        declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".xlsx")

    } else {
        throw new ServiceException("Не найдены данные для формирования отчета!");
    }
}

void calculateReportData(writer, ndflPerson) {

    //отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения
    def departmentId = declarationData.departmentId

    //Подразделение
    def department = departmentService.get(departmentId)

    def reportPeriodCode = findReportPeriodCode(reportPeriod)

    def builder = new MarkupBuilder(writer)
    builder.Файл() {

        СлЧасть('КодПодр': department.sbrfCode) {}
        ИнфЧасть('ПериодОтч': reportPeriodCode, 'ОтчетГод': reportPeriod?.taxPeriod?.year) {
            ПолучДох(ndflPersonAttr(ndflPerson)) {
                def incomes = ndflPerson.incomes.sort { a, b -> (a.rowNum <=> b.rowNum) }
                def deductions = ndflPerson.deductions.sort { a, b -> a.rowNum <=> b.rowNum }
                def prepayments = ndflPerson.prepayments.sort { a, b -> a.rowNum <=> b.rowNum }

                def operationList = incomes.collectEntries { personIncome ->
                    def key = personIncome.operationId
                    def value = ["ИдОпер": personIncome.operationId, "КПП": personIncome.kpp, "ОКТМО": personIncome.oktmo]
                    return [key, value]
                }

                def incomeOperations = mapToOperationId(incomes);
                def deductionOperations = mapToOperationId(deductions);
                def prepaymentOperations = mapToOperationId(prepayments);

                operationList.each { key, value ->
                    СведОпер(value) {
                        //доходы
                        incomeOperations.get(key).each { personIncome ->
                            СведДохНал(incomeAttr(personIncome)) {}
                        }

                        //Вычеты
                        deductionOperations.get(key).each { personDeduction ->
                            СведВыч(deductionAttr(personDeduction)) {}
                        }

                        //Авансовые платежи
                        prepaymentOperations.get(key).each { personPrepayment ->
                            СведАванс(prepaymentAttr(personPrepayment)) {}
                        }
                    }
                }
            }
        }
    }
}

def mapToOperationId(collection) {
    def result = [:]
    collection.collectEntries(result) { personOperation ->
        def key = personOperation.operationId
        def value
        if (result.containsKey(key)) {
            value = result.get(key);
        } else {
            value = [].toList()
        }
        value.add(personOperation)
        return [key, value]
    }
    return result;
}

/**
 * Получить полное название подразделения по id подразделения.
 */
def getDepartmentFullName(def id) {
    return departmentService.getParentsHierarchy(id)
}

/**
 * Находит код периода из справочника "Коды, определяющие налоговый (отчётный) период"
 */
def findReportPeriodCode(reportPeriod) {
    RefBookDataProvider dataProvider = refBookFactory.getDataProvider(PERIOD_CODE_REFBOOK)
    Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());
    if (refBookValueMap.isEmpty()) {
        throw new ServiceException("Некорректные данные в справочнике \"Коды, определяющие налоговый (отчётный) период\"");
    }
    return refBookValueMap.get("CODE").getStringValue();
}

//------------------ Import Data ----------------------

void importData() {

    //валидация по схеме
    declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile)

    InputStream xmlInputStream = ImportInputStream;

    if (xmlInputStream == null) {
        throw new ServiceException("Отсутствует значение параметра ImportInputStream!");
    }

    if (logger.containsLevel(LogLevel.WARNING)) {
        throw new ServiceException("ТФ не соответствует XSD-схеме РНУ НФДЛ. Загрузка не возможна.");
    }

    //Каждый элемент ИнфЧасть содержит данные об одном физ лице, максимальное число элементов в документе 15000
    QName infoPartName = QName.valueOf('ИнфЧасть')

    //Используем StAX парсер для импорта
    XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
    XMLEventReader reader = xmlFactory.createXMLEventReader(xmlInputStream)

    def sb;
    try {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()

            if (event.isCharacters() && ((Characters) event).isWhiteSpace()) {
                continue;
            }

            if (!event.isStartElement() && !event.isEndElement()) {
                continue;
            }

            //Последовательно обрабатываем все элементы ИнфЧасть в документе
            if (event.isStartElement() && event.getName().equals(infoPartName)) {
                sb = new StringBuilder()
            }

            if (event.isStartElement()) {
                sb?.append(processStartElement(event.asStartElement()))
            }

            if (event.isEndElement()) {
                sb?.append(processEndElement(event.asEndElement()))
            }

            if (event.isEndElement() && event.getName().equals(infoPartName)) {
                String personData = sb.toString();
                if (personData != null && !personData.isEmpty()) {
                    def infoPart = new XmlSlurper().parseText(sb.toString())
                    processInfoPart(infoPart)
                }
            }
        }
    } finally {
        reader?.close()
    }
}

String processStartElement(StartElement start) {
    String var1 = "<" + start.getName().getLocalPart();
    Iterator var2;
    Attribute var3;
    if (start.getAttributes() != null) {
        var2 = start.getAttributes();
        for (var3 = null; var2.hasNext(); var1 = var1 + " " + processAttr(var3)) {
            //println processAttr(var3)
            var3 = (Attribute) var2.next();
        }
    }
    var1 = var1 + ">";
    return var1;
}

String processAttr(Attribute attr) {
    if (attr != null) {
        return attr.getName().getLocalPart() + "=\'" + attr.getValue() + "\'"
    } else {
        return "";
    }
}

String processEndElement(EndElement end) {
    StringBuffer var1 = new StringBuffer();
    var1.append("</").append(end.getName().getLocalPart()).append(">");
    return var1.toString();
}

void processInfoPart(infoPart) {

    def ndflPersonNode = infoPart.'ПолучДох'[0]

    NdflPerson ndflPerson = transformNdflPersonNode(ndflPersonNode)

    def ndflPersonOperations = infoPart.'СведОпер'
    ndflPersonOperations.each {
        processNdflPersonOperation(ndflPerson, it)
    }

    //Идентификатор декларации для которой загружаются данные
    ndflPerson.declarationDataId = declarationData.getId()
    ndflPersonService.save(ndflPerson)
}

void processNdflPersonOperation(NdflPerson ndflPerson, NodeChild ndflPersonOperationsNode) {

    List<NdflPersonIncome> incomes = new ArrayList<NdflPersonIncome>();
    incomes.addAll(ndflPersonOperationsNode.'СведДохНал'.collect {
        transformNdflPersonIncome(it)
    });
    ndflPerson.incomes.addAll(incomes);

    List<NdflPersonDeduction> deductions = new ArrayList<NdflPersonDeduction>();
    ndflPersonOperationsNode.'СведВыч'.collect {
        transformNdflPersonDeduction(it)
    }
    ndflPerson.deductions.addAll(deductions)

    List<NdflPersonPrepayment> prepayments = new ArrayList<NdflPersonPrepayment>();
    ndflPerson.prepayments = ndflPersonOperationsNode.'СведАванс'.collect {
        transformNdflPersonPrepayment(it)
    }
    ndflPerson.prepayments.addAll(prepayments);
}

NdflPerson transformNdflPersonNode(NodeChild node) {
    NdflPerson ndflPerson = new NdflPerson()
    ndflPerson.inp = toString(node.'@ИНП')
    ndflPerson.snils = toString(node.'@СНИЛС')
    ndflPerson.lastName = toString(node.'@ФамФЛ')
    ndflPerson.firstName = toString(node.'@ИмяФЛ')
    ndflPerson.middleName = toString(node.'@ОтчФЛ')
    ndflPerson.birthDay = toDate(node.'@ДатаРожд')
    ndflPerson.citizenship = toString(node.'@Гражд')
    ndflPerson.innNp = toString(node.'@ИННФЛ')
    ndflPerson.innForeign = toString(node.'@ИННИно')
    ndflPerson.idDocType = toString(node.'@УдЛичнФЛКод')
    ndflPerson.idDocNumber = toString(node.'@УдЛичнФЛНом')
    ndflPerson.status = toString(node.'@СтатусФЛ')
    ndflPerson.postIndex = toString(node.'@Индекс')
    ndflPerson.regionCode = toString(node.'@КодРегион')
    ndflPerson.area = toString(node.'@Район')
    ndflPerson.city = toString(node.'@Город')
    ndflPerson.locality = toString(node.'@НаселПункт')
    ndflPerson.street = toString(node.'@Улица')
    ndflPerson.house = toString(node.'@Дом')
    ndflPerson.building = toString(node.'@Корпус')
    ndflPerson.flat = toString(node.'@Кварт')
    ndflPerson.countryCode = toString(node.'@КодСтрИно')
    ndflPerson.address = toString(node.'@АдресИно')
    ndflPerson.additionalData = toString(node.'@ДопИнф')
    return ndflPerson
}

NdflPersonIncome transformNdflPersonIncome(NodeChild node) {
    def operationNode = node.parent();

    NdflPersonIncome personIncome = new NdflPersonIncome()
    personIncome.rowNum = toInteger(node.'@НомСтр')
    personIncome.incomeCode = toString(node.'@КодДох')
    personIncome.incomeType = toString(node.'@ТипДох')

    personIncome.operationId = toBigDecimal(operationNode.'@ИдОпер')
    personIncome.oktmo = toString(operationNode.'@ОКТМО')
    personIncome.kpp = toString(operationNode.'@КПП')


    personIncome.incomeAccruedDate = toDate(node.'@ДатаДохНач')
    personIncome.incomePayoutDate = toDate(node.'@ДатаДохВыпл')
    personIncome.incomeAccruedSumm = toBigDecimal(node.'@СуммДохНач')
    personIncome.incomePayoutSumm = toBigDecimal(node.'@СуммДохВыпл')
    personIncome.totalDeductionsSumm = toBigDecimal(node.'@СумВыч')
    personIncome.taxBase = toBigDecimal(node.'@НалБаза')
    personIncome.taxRate = toInteger(node.'@Ставка')
    personIncome.taxDate = toDate(node.'@ДатаНалог')
    personIncome.calculatedTax = toInteger(node.'@НИ')
    personIncome.withholdingTax = toInteger(node.'@НУ')
    personIncome.notHoldingTax = toInteger(node.'@ДолгНП')
    personIncome.overholdingTax = toInteger(node.'@ДолгНА')
    personIncome.refoundTax = toInteger(node.'@ВозврНал')
    personIncome.taxTransferDate = toDate(node.'@СрокПрчслНал')
    personIncome.paymentDate = toDate(node.'@ПлПоручДат')
    personIncome.paymentNumber = toString(node.'@ПлатПоручНом')
    personIncome.taxSumm = toInteger(node.'@НалПерСумм')
    return personIncome
}

NdflPersonDeduction transformNdflPersonDeduction(NodeChild node) {

    NdflPersonDeduction personDeduction = new NdflPersonDeduction()
    personDeduction.rowNum = toInteger(node.'@НомСтр')
    personDeduction.operationId = toBigDecimal(node.parent().'@ИдОпер')
    personDeduction.typeCode = toString(node.'@ВычетКод')
    personDeduction.notifType = toString(node.'@УведТип')
    personDeduction.notifDate = toDate(node.'@УведДата')
    personDeduction.notifNum = toString(node.'@УведНом')
    personDeduction.notifSource = toString(node.'@УведИФНС')
    personDeduction.notifSumm = toBigDecimal(node.'@УведСум')
    personDeduction.incomeAccrued = toDate(node.'@ДатаДохНач')
    personDeduction.incomeCode = toString(node.'@КодДох')
    personDeduction.incomeSumm = toBigDecimal(node.'@СуммДохНач')
    personDeduction.periodPrevDate = toDate(node.'@ДатаПредВыч')
    personDeduction.periodPrevSumm = toBigDecimal(node.'@СумПредВыч')
    personDeduction.periodCurrDate = toDate(node.'@ДатаТекВыч')
    personDeduction.periodCurrSumm = toBigDecimal(node.'@СумТекВыч')
    return personDeduction
}

NdflPersonPrepayment transformNdflPersonPrepayment(NodeChild node) {
    NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
    personPrepayment.rowNum = toInteger(node.'@НомСтр')
    personPrepayment.operationId = toBigDecimal(node.parent().'@ИдОпер')
    personPrepayment.summ = toBigDecimal(node.'@Аванс')
    personPrepayment.notifNum = toString(node.'@УведНом')
    personPrepayment.notifDate = toDate(node.'@УведДата')
    personPrepayment.notifSource = toString(node.'@УведИФНС')
    return personPrepayment;
}

Integer toInteger(xmlNode) {
    if (xmlNode != null && !xmlNode.isEmpty()) {
        return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Integer.valueOf(xmlNode.text()) : null;
    } else {
        return null;
    }
}

BigDecimal toBigDecimal(xmlNode) {
    if (xmlNode != null && !xmlNode.isEmpty()) {
        return xmlNode.text() != null && !xmlNode.text().isEmpty() ? new BigDecimal(xmlNode.text()) : null;
    } else {
        return null;
    }
}

Date toDate(xmlNode) {
    if (xmlNode != null && !xmlNode.isEmpty()) {
        return xmlNode.text() != null && !xmlNode.text().isEmpty() ? new java.text.SimpleDateFormat('dd.MM.yyyy').parse(xmlNode.text()) : null;
    } else {
        return null;
    }
}

String toString(xmlNode) {
    if (xmlNode != null && !xmlNode.isEmpty()) {
        return xmlNode.text() != null && !xmlNode.text().isEmpty() ? StringUtils.cleanString(xmlNode.text()) : null;
    } else {
        return null;
    }
}

def formatDate(date) {
    return ScriptUtils.formatDate(date, "dd.MM.yyyy")
}

def ndflPersonAttr(ndflPerson) {
    [
            'ИНП'        : ndflPerson.inp,
            'СНИЛС'      : ndflPerson.snils,
            'ФамФЛ'      : ndflPerson.lastName,
            'ИмяФЛ'      : ndflPerson.firstName,
            'ОтчФЛ'      : ndflPerson.middleName,
            'ДатаРожд'   : formatDate(ndflPerson.birthDay),
            'Гражд'      : ndflPerson.citizenship,
            'ИННФЛ'      : ndflPerson.innNp,
            'ИННИно'     : ndflPerson.innForeign,
            'УдЛичнФЛКод': ndflPerson.idDocType,
            'УдЛичнФЛНом': ndflPerson.idDocNumber,
            'СтатусФЛ'   : ndflPerson.status,
            'Индекс'     : ndflPerson.postIndex,
            'КодРегион'  : ndflPerson.regionCode,
            'Район'      : ndflPerson.area,
            'Город'      : ndflPerson.city,
            'НаселПункт' : ndflPerson.locality,
            'Улица'      : ndflPerson.street,
            'Дом'        : ndflPerson.house,
            'Корпус'     : ndflPerson.building,
            'Кварт'      : ndflPerson.flat,
            'КодСтрИно'  : ndflPerson.countryCode,
            'АдресИно'   : ndflPerson.address,
            'ДопИнф'     : ndflPerson.additionalData]


}

def incomeAttr(personIncome) {
    [
            'НомСтр'      : personIncome.rowNum,
            'КодДох'      : personIncome.incomeCode,
            'ТипДох'      : personIncome.incomeType,

            'ИдОпер'      : personIncome.operationId,//toBigDecimal()
            'ОКТМО'       : personIncome.oktmo,
            'КПП'         : personIncome.kpp,

            'ДатаДохНач'  : formatDate(personIncome.incomeAccruedDate),
            'ДатаДохВыпл' : formatDate(personIncome.incomePayoutDate),
            'СуммДохНач'  : personIncome.incomeAccruedSumm,//toBigDecimal()
            'СуммДохВыпл' : personIncome.incomePayoutSumm,//toBigDecimal()
            'СумВыч'      : personIncome.totalDeductionsSumm,//toBigDecimal()
            'НалБаза'     : personIncome.taxBase,//toBigDecimal()
            'Ставка'      : personIncome.taxRate,//toInteger()
            'ДатаНалог'   : formatDate(personIncome.taxDate),
            'НИ'          : personIncome.calculatedTax,//toInteger()
            'НУ'          : personIncome.withholdingTax,//toInteger()
            'ДолгНП'      : personIncome.notHoldingTax,//toInteger()
            'ДолгНА'      : personIncome.overholdingTax,//toInteger()
            'ВозврНал'    : personIncome.refoundTax,//toInteger()
            'СрокПрчслНал': formatDate(personIncome.taxTransferDate),
            'ПлПоручДат'  : formatDate(personIncome.paymentDate),
            'ПлатПоручНом': personIncome.paymentNumber,
            'НалПерСумм'  : personIncome.taxSumm,//toInteger()
    ]
}

def deductionAttr(personDeduction) {
    [
            'НомСтр'     : personDeduction.rowNum,//toInteger()
            'ИдОпер'     : personDeduction.operationId,//toBigDecimal()
            'ВычетКод'   : personDeduction.typeCode,
            'УведТип'    : personDeduction.notifType,
            'УведДата'   : formatDate(personDeduction.notifDate),
            'УведНом'    : personDeduction.notifNum,
            'УведИФНС'   : personDeduction.notifSource,
            'УведСум'    : personDeduction.notifSumm,//toBigDecimal()
            'ДатаДохНач' : formatDate(personDeduction.incomeAccrued),
            'КодДох'     : personDeduction.incomeCode,
            'СуммДохНач' : personDeduction.incomeSumm,//toBigDecimal()
            'ДатаПредВыч': formatDate(personDeduction.periodPrevDate),
            'СумПредВыч' : personDeduction.periodPrevSumm,//toBigDecimal()
            'ДатаТекВыч' : formatDate(personDeduction.periodCurrDate),
            'СумТекВыч'  : personDeduction.periodCurrSumm//toBigDecimal()
    ]
}

def prepaymentAttr(personPrepayment) {
    [
            "НомСтр"  : personPrepayment.rowNum,//.toInteger()
            "ИдОпер"  : personPrepayment.operationId,//toBigDecimal()
            "Аванс"   : personPrepayment.summ,//toBigDecimal()
            "УведНом" : personPrepayment.notifNum,
            "УведДата": formatDate(personPrepayment.notifDate),
            "УведИФНС": personPrepayment.notifSource
    ]
}

//------------------Check Data ----------------------
// Кэш провайдеров
@Field def providerCache = [:]

// Физические лица
@Field def personsCache = [:]
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id

// Мапа <ID_Данные о физическом лице - получателе дохода, Физическое лицо: <ФИО> ИНП:<ИНП>>
@Field def ndflPersonFLMap = [:]
@Field final TEMPLATE_PERSON_FL = "Физическое лицо: \"%s\", ИНП: \"%s\""

// Страны Мапа <Идентификатор, Код>
@Field def asnuCache = [:]
@Field final long REF_BOOK_ASNU_ID = RefBook.Id.ASNU.id

// Страны Мапа <Идентификатор, Код>
@Field def citizenshipCache = [:]
@Field final long REF_BOOK_COUNTRY_ID = RefBook.Id.COUNTRY.id

// Виды документов, удостоверяющих личность Мапа <Идентификатор, Код>
@Field def documentCodesCache = [:]
@Field final long REF_BOOK_DOCUMENT_ID = RefBook.Id.DOCUMENT_CODES.id

//Приоритет документов удостоверяющих личность <Идентификатор, Приоритет(int)>
@Field def documentPriorityCache = [:]

// Статус налогоплательщика Мапа <Идентификатор, Код>
@Field def taxpayerStatusCache = [:]
@Field final long REF_BOOK_TAXPAYER_STATUS_ID = RefBook.Id.TAXPAYER_STATUS.id

// Адрес Мапа <Идентификатор, Адрес>
@Field def addressCache = [:]
@Field final long REF_BOOK_ADDRESS_ID = RefBook.Id.PERSON_ADDRESS.id

// Тербанки Мапа <id, наименование>
@Field def terBankCache = [:]
@Field final long REF_DEPARTMENT_ID = RefBook.Id.DEPARTMENT.id

// ИНП Мапа <person_id, массив_инп>
@Field def inpCache = [:]
@Field final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id

@Field def dulCache = [:]
@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// Коды видов доходов Мапа <Идентификатор, Код>
@Field def incomeCodeCache = [:]
@Field final long REF_BOOK_INCOME_CODE_ID = RefBook.Id.INCOME_CODE.id

// Виды дохода Мапа <Признак, Идентификатор_кода_вида_дохода>
@Field def incomeKindCache = [:]
@Field final long REF_BOOK_INCOME_KIND_ID = RefBook.Id.INCOME_KIND.id

// Ставки
@Field def rateCache = []
@Field final long REF_BOOK_RATE_ID = RefBook.Id.NDFL_RATE.id

// Коды видов вычетов
@Field def deductionTypeCache = []
@Field final long REF_BOOK_DEDUCTION_TYPE_ID = RefBook.Id.DEDUCTION_TYPE.id

// Коды налоговых органов
@Field def taxInspectionCache = []
@Field final long REF_TAX_INSPECTION_ID = RefBook.Id.TAX_INSPECTION.id

// Дата начала отчетного периода
@Field def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
@Field final long REF_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_NOT_FOUND_REF = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
@Field final String MESSAGE_ERROR_VALUE = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: %s."
@Field final String MESSAGE_ERROR_INN = "Некорректный ИНН"
@Field final String MESSAGE_ERROR_BIRTHDAY = "Дата рождения налогоплательщика превышает дату отчетного периода"
@Field final String MESSAGE_ERROR_DATE = "Дата не входит в отчетный период формы"
@Field final String MESSAGE_ERROR_NOT_MATCH_RULE = "Значение не соответствует правилу: "
@Field final String MESSAGE_ERROR_MUST_FILL = "Поле \"%s\" должно быть заполнено, если %s"
@Field final String MESSAGE_ERROR_NOT_FILL = "Поле \"%s\" не заполнено. Должны быть заполнены либо все поля %s, либо не заполнено ни одно из полей."
@Field final String MESSAGE_ERROR_NOT_MUST_FILL = "Поле \"%s\" не должно быть заполнено, если %s"
@Field final String MESSAGE_ERROR_NOT_FOUND_PERSON = "Не удалось установить связь со справочником \"%s\" для Раздел: \"%s\". Строка: \"%s\", ИНП: \"%s\"."
@Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
@Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
@Field final String MESSAGE_ERROR_ABSENT = " Отсутсвуют строки:"
@Field final String MESSAGE_ERROR_NOT_FOUND_PARAM = "Указанные значения не найдены в Справочнике \"Настройки подразделений\" для \"%s\""

@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" размером %d."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" в колличестве %d."

// Таблицы
@Field final String T_PERSON = "Реквизиты"
@Field final String T_PERSON_INCOME = "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION = "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT = "Сведения о доходах в виде авансовых платежей"

// Справочники
@Field final String R_FIAS = "ФИАС"
@Field final String R_PERSON = "Физические лица"
@Field final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
@Field final String R_ID_DOC_TYPE = "Коды документов"
@Field final String R_STATUS = "Статусы налогоплательщика"
@Field final String R_INCOME_CODE = "Коды видов доходов"
@Field final String R_INCOME_KIND = "Виды доходов"
@Field final String R_RATE = "Ставки"
@Field final String R_TYPE_CODE = "Коды видов вычетов"
@Field final String R_NOTIF_SOURCE = "Коды налоговых органов"
@Field final String R_ADDRESS = "Адреса"
@Field final String R_INP = "Идентификаторы налогоплательщиков"
@Field final String R_DUL = "Документы, удостоверяющий личность"

// Реквизиты
@Field final String C_ADDRESS = "Адрес регистрации в Российской Федерации "
@Field final String C_CITIZENSHIP = "Гражданство (код страны)"
@Field final String C_ID_DOC_TYPE = "Код вида документа"
@Field final String C_ID_DOC_NUMBER = "Серия и номер документа"
@Field final String C_STATUS = "Статус"
@Field final String C_INCOME_CODE = "Код вида дохода"
@Field final String C_INCOME_KIND = "Вид дохода"
@Field final String C_RATE = "Ставка"
@Field final String C_TYPE_CODE = "Код вычета"
@Field final String C_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление"
@Field final String C_LAST_NAME = "Фамилия"
@Field final String C_FIRST_NAME = "Имя"
@Field final String C_MIDDLE_NAME = "Отчество"
@Field final String C_INP = "Уникальный код клиента"
@Field final String C_BIRTH_DATE = "Дата рождения"
@Field final String C_INN_NP = "ИНН  физического лица"
@Field final String C_INN_FOREIGN = "ИНН  иностранного гражданина"
@Field final String C_REGION_CODE = "Код Региона"
@Field final String C_AREA = "Район"
@Field final String C_CITY = "Город"
@Field final String C_LOCALITY = "Населенный пункт"
@Field final String C_STREET = "Улица"
@Field final String C_HOUSE = "Дом"
@Field final String C_BUILDING = "Корпус"
@Field final String C_FLAT = "Квартира"

// Сведения о доходах и НДФЛ
@Field final String C_INCOME_TYPE = "Признак вида дохода"
@Field final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода"
@Field final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода"
@Field final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода"
@Field final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода"
@Field final String C_TOTAL_DEDUCTIONS_SUMM = "Общая сумма вычетов"
@Field final String C_TAX_BASE = "Налоговая база"
@Field final String C_TAX_RATE = "Ставка налога"
@Field final String C_TAX_DATE = "Дата налога"
@Field final String C_CALCULATED_TAX = "Сумма налога исчисленная"
@Field final String C_WITHHOLDING_TAX = "Сумма налога удержанная"
@Field final String C_NOT_HOLDING_TAX = "Сумма налога, не удержанная налоговым агентом"
@Field final String C_OVERHOLDING_TAX = "Сумма налога, излишне удержанная налоговым агентом"
@Field final String C_REFOUND_TAX = "Сумма возвращенного налога"
@Field final String C_TAX_TRANSFER_DATE = "Срок (дата) перечисления налога"
@Field final String C_PAYMENT_DATE = "Дата платежного поручения"
@Field final String C_PAYMENT_NUMBER = "Номер платежного поручения перечисления налога в бюджет"
@Field final String C_TAX_SUMM = "Сумма налога перечисленная"
@Field final String C_OKTMO = "ОКТМО"
@Field final String C_KPP = "КПП"

// Сведения о вычетах
@Field final String C_NOTIF_DATE = "Дата выдачи уведомления"
@Field final String C_INCOME_ACCRUED = "Дата начисления дохода"
@Field final String C_PERIOD_PREV_DATE = "Дата применения вычета в предыдущем периоде"
@Field final String C_PERIOD_CURR_DATE = "Дата применения вычета в текущем периоде"

// Поля справочника Физические лица
@Field final String RF_LAST_NAME = "LAST_NAME"
@Field final String RF_FIRST_NAME = "FIRST_NAME"
@Field final String RF_MIDDLE_NAME = "MIDDLE_NAME"
@Field final String RF_BIRTH_DATE = "BIRTH_DATE"
@Field final String RF_CITIZENSHIP = "CITIZENSHIP"
@Field final String RF_INN = "INN"
@Field final String RF_INN_FOREIGN = "INN_FOREIGN"
@Field final String RF_SNILS = "SNILS"
@Field final String RF_TAXPAYER_STATE = "TAXPAYER_STATE"
@Field final String RF_ADDRESS = "ADDRESS"
@Field final String RF_RECORD_ID = "RECORD_ID"
@Field final String RF_COUNTRY = "COUNTRY"
@Field final String RF_REGION_CODE = "REGION_CODE"
@Field final String RF_DISTRICT = "DISTRICT"
@Field final String RF_CITY = "CITY"
@Field final String RF_LOCALITY = "LOCALITY"
@Field final String RF_STREET = "STREET"
@Field final String RF_HOUSE = "HOUSE"
@Field final String RF_BUILD = "BUILD"
@Field final String RF_APPARTMENT = "APPARTMENT"
@Field final String RF_DUBLICATES = "DUBLICATES"
@Field final String RF_DOC_ID = "DOC_ID"
@Field final String RF_DOC_NUMBER = "DOC_NUMBER"

/**
 * Проверки НДФЛ
 * @return
 */
def checkData() {
    // Реквизиты
    ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON, ndflPersonList.size())

    // Сведения о доходах и НДФЛ
    ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_INCOME, ndflPersonIncomeList.size())

    // Сведения о вычетах
    ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION, ndflPersonDeductionList.size())

    // Сведения о доходах в виде авансовых платежей
    ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT, ndflPersonPrepaymentList.size())

    // Проверки на соответствие справочникам
    checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Общие проверки
    checkDataCommon(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Проверки сведений о доходах
    checkDataIncome(ndflPersonList, ndflPersonIncomeList)
}

/**
 * Проверки на соответствие справочникам
 * @return
 */
def checkDataReference(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {
    // Страны
    def citizenshipCodeMap = getRefCitizenship()
    logger.info(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

    // Виды документов, удостоверяющих личность
    def documentTypeMap = getRefDocument()
    logger.info(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

    // Статус налогоплательщика
    def taxpayerStatusMap = getRefTaxpayerStatus()
    logger.info(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

    // Коды видов доходов Мапа <Идентификатор, Код>
    def incomeCodeMap = getRefIncomeCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

    // Виды доходов Мапа <Признак, Идентификатор_кода_вида_дохода>
    def incomeKindMap = getRefIncomeKind()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_KIND, incomeKindMap.size())

    // Ставки
    def rateList = getRefRate()
    logger.info(SUCCESS_GET_REF_BOOK, R_RATE, rateList.size())

    // Коды видов вычетов
    def deductionTypeList = getRefDeductionType()
    logger.info(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

    // Коды налоговых органов
    def taxInspectionList = getRefNotifSource()
    logger.info(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

    // Физические лица
    def personIds = getPersonIds(ndflPersonList)
    def personMap = [:]

    // ИНП <person_id, массив_ИНП>
    def inpMap = [:]

    // ДУЛ <person_id, массив_ДУЛ>
    def dulMap = [:]

    // Адреса
    def addressIds = []
    def addressMap = [:]

    if (personIds.size() > 0) {
        // todo Сделать получение записей оригиналов, если текущая запись - дубликат
        personMap = getRefPersons(personIds)
        logger.info(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

        // Получим мапу ИНП
        inpMap = getRefINP(personIds)
        logger.info(SUCCESS_GET_TABLE, R_INP, inpMap.size())

        // Получим мапу ДУЛ
        dulMap = getRefDul(personIds)
        logger.info(SUCCESS_GET_TABLE, R_DUL, dulMap.size())

        // Получим Мапу адресов
        personMap.each { personId, person ->
            // Сохраним идентификаторы адресов в коллекцию
            if (person.get(RF_ADDRESS).value != null) {
                addressIds.add(person.get(RF_ADDRESS).value)
            }
        }
        if (addressIds.size() > 0) {
            addressMap = getRefAddress(addressIds)
            logger.info(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
        }
    }

    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        // Спр1 ФИАС
        // todo oshelepeav Исправить на фатальную ошибку https://jira.aplana.com/browse/SBRFNDFL-448
        if (!isExistsAddress(ndflPerson.regionCode, ndflPerson.area, ndflPerson.city, ndflPerson.locality, ndflPerson.street)) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_ADDRESS, fioAndInp, C_ADDRESS, R_FIAS);
        }

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_CITIZENSHIP);
        }

        // Спр3 Документ удостоверяющий личность (Обязательное поле)
        if (!documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_ID_DOC_TYPE);
        }

        // Спр4 Статусы налогоплательщиков (Обязательное поле)
        if (!taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_STATUS);
        }

        // Спр10 Наличие связи с "Физическое лицо"
        // todo Почему в объект помещается 0 - ndflPerson.personId == 0
        if (ndflPerson.personId == null || ndflPerson.personId == 0) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_PERSON,
                    R_PERSON, T_PERSON, fio, ndflPerson.inp);
        } else {
            def person = personMap.get(ndflPerson.personId)

            // Спр11 Фамилия (Обязательное поле)
            if (!ndflPerson.lastName.equals(person.get(RF_LAST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_LAST_NAME, fioAndInp, C_LAST_NAME, R_PERSON);
            }

            // Спр11 Имя (Обязательное поле)
            if (!ndflPerson.firstName.equals(person.get(RF_FIRST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_FIRST_NAME, fioAndInp, C_FIRST_NAME, R_PERSON);
            }

            // Спр11 Отчество (Необязательное поле)
            if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(person.get(RF_MIDDLE_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_MIDDLE_NAME, fioAndInp, C_MIDDLE_NAME, R_PERSON);
            }

            // Спр12 ИНП первичная (Обязательное поле)
            def inpList = inpMap.get(ndflPerson.personId)
            if (!ndflPerson.inp.equals(person.get(RF_SNILS).value) && !inpList.contains(ndflPerson.inp)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INP, fioAndInp, C_INP, R_INP);
            }

            // todo Спр12.1 ИНП консолидированная - Реализовать позже

            // Спр13 Дата рождения (Обязательное поле)
            if (!ndflPerson.birthDay.equals(person.get(RF_BIRTH_DATE).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, C_BIRTH_DATE, R_PERSON);
            }

            // Спр14 Гражданство (Обязательное поле)
            def citizenship = citizenshipCodeMap.get(person.get(RF_CITIZENSHIP).value)
            if (!ndflPerson.citizenship.equals(citizenship)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_PERSON);
            }

            // Спр15 ИНН (Необязательное поле)
            if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(person.get(RF_INN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, C_INN_NP, R_PERSON);
            }

            // Спр16 ИНН в стране Гражданства (Необязательное поле)
            if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(person.get(RF_INN_FOREIGN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INN_FOREIGN, fioAndInp, C_INN_FOREIGN, R_PERSON);
            }

            // Спр17 Документ удостоверяющий личность (Обязательное поле)
            def dulList = dulMap.get(ndflPerson.personId)
            // Вид документа
            def idDocTypeList = []
            // Серия и номер документа
            def idDocNumberList = []
            dulList.each { dul ->
                idDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                idDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
            }
            if (!idDocTypeList.contains(ndflPerson.idDocType)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_PERSON);
            }
            if (!idDocNumberList.contains(ndflPerson.idDocNumber)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_ID_DOC_NUMBER, fioAndInp, C_ID_DOC_NUMBER, R_PERSON);
            }

            // todo Спр17.1 Документ удостоверяющий личность (консолидированная) (Обязательное поле) - Реализовать позже

            // Спр18 Статус налогоплательщика (Обязательное поле)
            def taxpayerStatus = taxpayerStatusMap.get(person.get(RF_TAXPAYER_STATE).value)
            if (!ndflPerson.status.equals(taxpayerStatus)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_PERSON);
            }

            // Спр19 Адрес (Необязательное поле)
            // Сравнение должны быть проведено даже с учетом пропусков
            def address = addressMap.get(person.get(RF_ADDRESS).value)
            def regionCode
            def area
            def city
            def locality
            def street
            def house
            def building
            def flat
            if (address != null) {
                regionCode = address.get(RF_REGION_CODE).value
                area = address.get(RF_DISTRICT).value
                city = address.get(RF_CITY).value
                locality = address.get(RF_LOCALITY).value
                street = address.get(RF_STREET).value
                house = address.get(RF_HOUSE).value
                building = address.get(RF_BUILD).value
                flat = address.get(RF_APPARTMENT).value
            }

            // Код Региона
            if (!ndflPerson.regionCode.equals(regionCode)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_REGION_CODE, fioAndInp, C_REGION_CODE, R_PERSON);
            }

            // Район
            if (!ndflPerson.area.equals(area)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_AREA, fioAndInp, C_AREA, R_PERSON);
            }

            // Город
            if (!ndflPerson.city.equals(city)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_CITY, fioAndInp, C_CITY, R_PERSON);
            }

            // Населенный пункт
            if (!ndflPerson.locality.equals(locality)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_LOCALITY, fioAndInp, C_LOCALITY, R_PERSON);
            }

            // Улица
            if (!ndflPerson.street.equals(street)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_STREET, fioAndInp, C_STREET, R_PERSON);
            }

            // Дом
            if (!ndflPerson.house.equals(house)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_HOUSE, fioAndInp, C_HOUSE, R_PERSON);
            }

            // Корпус
            if (!ndflPerson.building.equals(building)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_BUILDING, fioAndInp, C_BUILDING, R_PERSON);
            }

            // Квартира
            if (!ndflPerson.flat.equals(flat)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_FLAT, fioAndInp, C_FLAT, R_PERSON);
            }
        }
    }

    ndflPersonIncomeList.each { ndflPersonIncome ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find { key, value -> value == ndflPersonIncome.incomeCode }) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, C_INCOME_CODE, R_INCOME_CODE);
        }

        // Спр6 Вида дохода (Необязательное поле)
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (ndflPersonIncome.incomeType != null) {
            def idIncomeCode = incomeKindMap.get(ndflPersonIncome.incomeType)
            if (!idIncomeCode) {
                logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            } else if (!incomeCodeMap.get(idIncomeCode.value)) {
                logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            }
        }

        // Спр7 Ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_RATE, fioAndInp, C_RATE, R_RATE);
        }
    }

    ndflPersonDeductionList.each { ndflPersonDeduction ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_TYPE_CODE, fioAndInp, C_TYPE_CODE, R_TYPE_CODE);
        }

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }

    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }
}

/**
 * Общие проверки
 */
def checkDataCommon(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {

    // Порядковые номера строк в "Реквизиты"
    def rowNumPersonList = []

    // Порядковые номера строк в "Сведения о доходах и НДФЛ"
    def rowNumPersonIncomeList = []

    // Порядковые номера строк в "Сведения о вычетах"
    def rowNumPersonDeductionList = []

    // Порядковые номера строк в "Сведения о доходах в виде авансовых платежей"
    def rowNumPersonPrepaymentList = []

    // Тербанки
    def mapTerBank = getTerBank()

    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def mapOktmoAndKpp = getOktmoAndKpp(departmentParam.record_id.value)

    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        rowNumPersonList.add(ndflPerson.rowNum)

        // Общ1 ИНН (Необязательное поле)
        if (ndflPerson.innNp != null && !ScriptUtils.checkControlSumInn(ndflPerson.innNp)) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, MESSAGE_ERROR_INN);
        }

        // Общ4 Дата рождения (Обязательное поле)
        if (ndflPerson.birthDay > getReportPeriodEndDate()) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, MESSAGE_ERROR_BIRTHDAY);
        }
    }

    ndflPersonIncomeList.each { ndflPersonIncome ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        rowNumPersonIncomeList.add(ndflPersonIncome.rowNum)

        // Общ5 Даты доходов
        // Дата начисления дохода (Необязательное поле)
        if (ndflPersonIncome.incomeAccruedDate != null &&
                (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate()
                        || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата выплаты дохода (Необязательное поле)
        if (ndflPersonIncome.incomePayoutDate != null &&
                (ndflPersonIncome.incomePayoutDate < getReportPeriodStartDate()
                        || ndflPersonIncome.incomePayoutDate > getReportPeriodEndDate())) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата налога (Необязательное поле)
        if (ndflPersonIncome.taxDate != null &&
                (ndflPersonIncome.taxDate < getReportPeriodStartDate()
                        || ndflPersonIncome.taxDate > getReportPeriodEndDate())) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Срок (дата) перечисления налога (Необязательное поле)
        if (ndflPersonIncome.taxTransferDate != null &&
                (ndflPersonIncome.taxTransferDate < getReportPeriodStartDate()
                        || ndflPersonIncome.taxTransferDate > getReportPeriodEndDate())) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }

        // Общ7 Заполненность полей
        if (ndflPersonIncome.paymentDate == null &&
                ndflPersonIncome.paymentNumber == null &&
                ndflPersonIncome.taxSumm == null) {
            // если не заполнены Раздел 2. Графы 22-24
            def emptyField = "не заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // если не заполнена Раздел 2. Графа 21
            if (ndflPersonIncome.taxTransferDate == null) {
                emptyField += ", \"" + C_TAX_TRANSFER_DATE + "\""

                // Раздел 2. Графа 4 "Код вида дохода" должна быть заполнена
                if (ndflPersonIncome.incomeCode == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_CODE, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
                // Раздел 2. Графа 5 "Признак вида дохода" должна быть заполнена
                if (ndflPersonIncome.incomeType == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_TYPE, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_TYPE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }

            // если не заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null) {
                emptyField += ", \"" + C_INCOME_PAYOUT_DATE + "\", \"" + C_INCOME_PAYOUT_SUMM + "\""

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть НЕ заполнена
                if (ndflPersonIncome.taxTransferDate != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }

            // 	Раздел 2. Графа 13 "Налоговая база" должна быть заполнена
            if (ndflPersonIncome.taxBase == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_BASE, emptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 14 "Ставка налога" должна быть заполнена
            if (ndflPersonIncome.taxRate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_RATE, emptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 15 "Дата налога" должна быть заполнена
            if (ndflPersonIncome.taxDate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_DATE, emptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнены Раздел 2. Графы 22-24
            def notEmptyField = "заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // Раздел 2. Графа 12 "Общая сумма вычетов" должна быть НЕ заполнена
            if (ndflPersonIncome.totalDeductionsSumm != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TOTAL_DEDUCTIONS_SUMM, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 13 "Налоговая база" должна быть НЕ заполнена
            if (ndflPersonIncome.taxBase != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_BASE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 14 "Ставка налога" должна быть НЕ заполнена
            if (ndflPersonIncome.taxRate != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_RATE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 15 "Дата налога" должна быть НЕ заполнена
            if (ndflPersonIncome.taxDate != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_DATE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
            if (ndflPersonIncome.taxTransferDate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        }

        // если заполнена Раздел 2. Графа 6 "Дата начисления дохода"
        if (ndflPersonIncome.incomeAccruedDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_DATE])

            // Раздел 2. Графа 10 "Сумма начисленного дохода" должна быть заполнена
            if (ndflPersonIncome.incomeAccruedSumm != null) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть заполнена
                if (ndflPersonIncome.calculatedTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_CALCULATED_TAX, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.notHoldingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_NOT_HOLDING_TAX, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.overholdingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_OVERHOLDING_TAX, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            } else {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_SUMM, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнена Раздел 2. Графа 10 "Сумма начисленного дохода"
            if (ndflPersonIncome.incomeAccruedSumm != null) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 6 "Дата начисления дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_DATE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                def emptyField = "не заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть НЕ заполнена
                if (ndflPersonIncome.calculatedTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_CALCULATED_TAX, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.notHoldingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_NOT_HOLDING_TAX, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.overholdingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_OVERHOLDING_TAX, emptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 7 "Дата выплаты дохода"
        if (ndflPersonIncome.incomePayoutDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_DATE])
            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода" должна быть заполнена
            if (ndflPersonIncome.incomePayoutSumm != null) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_PAYOUT_DATE, C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 17 "Сумма налога удержанная" должна быть заполнена
                if (ndflPersonIncome.withholdingTax == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_WITHHOLDING_TAX, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_WITHHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 20 "Сумма возвращенного налога" должна быть заполнена
                if (ndflPersonIncome.refoundTax == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_REFOUND_TAX, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_REFOUND_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    logger.error(MESSAGE_ERROR_VALUE,
                            T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            } else {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_SUMM, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
            if (ndflPersonIncome.incomePayoutSumm != null) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 7 "Дата выплаты дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_DATE, notEmptyField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        }

        // Раздел 2. Графа 22, 23, 24 Должны быть либо заполнены все 3 Графы, либо ни одна их них
        if (ndflPersonIncome.paymentDate != null || ndflPersonIncome.paymentNumber != null || ndflPersonIncome.taxSumm != null) {
            def allField = getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])
            // Раздел 2. Графа 22 "Дата платежного поручения"
            if (ndflPersonIncome.paymentDate == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_DATE, allField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 23 "Номер платежного поручения перечисления налога в бюджет"
            if (ndflPersonIncome.paymentNumber == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_NUMBER, allField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_NUMBER, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 24 "Сумма налога перечисленная"
            if (ndflPersonIncome.taxSumm == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_TAX_SUMM, allField])
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
        }

        // Общ10 Соответствие КПП и ОКТМО Тербанку
        def kppList = mapOktmoAndKpp.get(ndflPersonIncome.oktmo)
        def msgErr = sprintf(MESSAGE_ERROR_NOT_FOUND_PARAM, [mapTerBank.get(declarationData.departmentId).value])
        if (kppList == null) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OKTMO, fioAndInp, msgErr);
        } else {
            if (!kppList.contains(ndflPersonIncome.kpp)) {
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_KPP, fioAndInp, msgErr);
            }
        }
    }

    ndflPersonDeductionList.each { ndflPersonDeduction ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        rowNumPersonDeductionList.add(ndflPersonDeduction.rowNum)

        // Общ6 Даты налоговых вычетов
        // Дата выдачи уведомления (Обязательное поле)
        if (ndflPersonDeduction.notifDate < getReportPeriodStartDate() || ndflPersonDeduction.notifDate > getReportPeriodEndDate()) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата начисления дохода (Обязательное поле)
        if (ndflPersonDeduction.incomeAccrued < getReportPeriodStartDate() || ndflPersonDeduction.incomeAccrued > getReportPeriodEndDate()) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_INCOME_ACCRUED, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в предыдущем периоде (Необязательное поле)
        if (ndflPersonDeduction.periodPrevDate != null &&
                (ndflPersonDeduction.periodPrevDate < getReportPeriodStartDate() || ndflPersonDeduction.periodPrevDate > getReportPeriodEndDate())) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_PREV_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в текущем периоде (Обязательное поле)
        if (ndflPersonDeduction.periodCurrDate < getReportPeriodStartDate() || ndflPersonDeduction.periodCurrDate > getReportPeriodEndDate()) {
            logger.error(MESSAGE_ERROR_VALUE,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_CURR_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
    }

    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        rowNumPersonPrepaymentList.add(ndflPersonPrepayment.rowNum)
    }

    // Общ8 Отсутствие пропусков и повторений
    def msgErrDubl = getErrorMsgDubl(rowNumPersonList, T_PERSON)
    msgErrDubl += getErrorMsgDubl(rowNumPersonIncomeList, T_PERSON_INCOME)
    msgErrDubl += getErrorMsgDubl(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    msgErrDubl += getErrorMsgDubl(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrDubl = msgErrDubl == "" ? "" : MESSAGE_ERROR_DUBL + msgErrDubl
    def msgErrAbsent = getErrorMsgAbsent(rowNumPersonList, T_PERSON)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonIncomeList, T_PERSON_INCOME)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrAbsent = msgErrAbsent == "" ? "" : MESSAGE_ERROR_ABSENT + msgErrAbsent
    if (msgErrDubl != "" || msgErrAbsent != "") {
        logger.warn(MESSAGE_ERROR_DUBL_OR_ABSENT + msgErrDubl + msgErrAbsent);
    }
}

/**
 * Проверки сведений о доходах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 */
def checkDataIncome(ndflPersonList, ndflPersonIncomeList) {

    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
    }

    ndflPersonIncomeList.each { ndflPersonIncome ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // СведДох1 Дата начисления дохода
    }
}

/**
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param ndflPersonList
 * @return
 */
def getPersonIds(def ndflPersonList) {
    def personIds = []
    ndflPersonList.each { ndflPerson ->
        // todo Почему ndflPerson.personId != 0
        if (ndflPerson.personId != null && ndflPerson.personId != 0) {
            personIds.add(ndflPerson.personId)
        }
    }
    return personIds;
}

/**
 * Получить "Физические лица"
 * @param personIds
 * @return
 */
// todo добавить обработку дублей
def getRefPersons(def personIds) {
    if (personsCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
//        def dublMap = [:]
//        def dublList = []
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)

            // Добавим дубликат в Мапу
//            if (person.get(RF_DUBLICATES).value != null && !dublMap.get(person.get(RF_DUBLICATES).value)) {
//                dublMap.put(person.get(RF_DUBLICATES).value, personId)
//                dublList.add(person.get(RF_DUBLICATES).value)
//            }
        }
        // Получим оригинальные записи, если имеются дубликаты
//        if (dublMap.size() > 0) {
//            def refBookDublMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, dublList)
//            refBookDublMap.each { originalPersonId, person ->
//                dublPersonId = dublMap.get(originalPersonId).value
//            }
//        }
    }
    return personsCache;
}

/**
 * Получить "АСНУ"
 * @return
 */
def getRefAsnu() {
    if (asnuCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_ASNU_ID)
        refBookMap.each { refBook ->
            asnuCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return asnuCache;
}

/**
 * Получить "Страны"
 * @return
 */
def getRefCitizenship() {
    if (citizenshipCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_COUNTRY_ID)
        refBookMap.each { refBook ->
            citizenshipCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return citizenshipCache;
}

/**
 * Получить "Коды документов, удостоверяющих личность"
 * @return
 */
def getRefDocument() {
    if (documentCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentCodesCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return documentCodesCache;
}

def getRefDocumentPriority() {
    if (documentPriorityCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentPriorityCache.put(refBook?.id?.numberValue, refBook?.PRIORITY?.stringValue)
        }
    }
    return documentPriorityCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefTaxpayerStatus() {
    if (taxpayerStatusCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_TAXPAYER_STATUS_ID)
        refBookMap.each { refBook ->
            taxpayerStatusCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return taxpayerStatusCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefAddress(def addressIds) {
    if (addressCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_ADDRESS_ID, addressIds)
        refBookMap.each { addressId, address ->
            addressCache.put(addressId, address)
        }
    }
    return addressCache;
}

/**
 * Получить набор тербанков
 */
def getTerBank() {
    if (terBankCache.size() == 0) {
        def refBookMap = getRefBookByFilter(REF_DEPARTMENT_ID, "PARENT_ID = 0")
        refBookMap.each { refBook ->
            terBankCache.put(refBook?.id?.numberValue, refBook?.NAME?.stringValue)
        }
    }
    return terBankCache
}

/**
 * Получить "ИНП"
 * todo Получение ИНП реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefINP(def personIds) {
    if (inpCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId.toString())
            def inpList = []
            refBookMap.each { refBook ->
                inpList.add(refBook?.INP?.stringValue)
            }
            inpCache.put(personId, inpList)
        }
    }
    return inpCache;
}

/**
 * В отличии от предыдущего метода складывает в кэш все аттрибуты справочника
 * @param personIds
 * @return
 */
def getRefInpMap(def personIds) {
    if (inpCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId.toString())
            def inpList = []
            refBookMap.each { refBook ->
                inpList.add(refBook)
            }
            inpCache.put(personId, inpList)
        }
    }
    return inpCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 * todo Получение ДУЛ реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefDul(def personIds) {
    if (dulCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_DOC_ID, "PERSON_ID = " + personId.toString())
            def dulList = []
            refBookMap.each { refBook ->
                dulList.add(refBook)
            }
            dulCache.put(personId, dulList)
        }
    }
    return dulCache;
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getRefIncomeCode() {
    if (incomeCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_INCOME_CODE_ID)
        refBookMap.each { refBook ->
            incomeCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return incomeCodeCache;
}

/**
 * Получить "Виды доходов"
 * @return
 */
def getRefIncomeKind() {
    if (incomeKindCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_INCOME_KIND_ID)
        refBookList.each { refBook ->
            def incomeTypeId = refBook.find { key, value -> key == "INCOME_TYPE_ID" }.value
            incomeKindCache.put(refBook?.MARK?.stringValue, incomeTypeId)
        }
    }
    return incomeKindCache;
}

/**
 * Получить "Ставки"
 * @return
 */
def getRefRate() {
    if (rateCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_RATE_ID)
        refBookList.each { refBook ->
            rateCache.add(refBook?.RATE?.stringValue)
        }
    }
    return rateCache;
}

/**
 * Получить "Коды видов вычетов"
 * @return
 */
def getRefDeductionType() {
    if (deductionTypeCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DEDUCTION_TYPE_ID)
        refBookList.each { refBook ->
            deductionTypeCache.add(refBook?.CODE?.stringValue)
        }
    }
    return deductionTypeCache;
}

/**
 * Получить "Коды налоговых органов"
 * @return
 */
def getRefNotifSource() {
    if (taxInspectionCache.size() == 0) {
        def refBookList = getRefBook(REF_TAX_INSPECTION_ID)
        refBookList.each { refBook ->
            taxInspectionCache.add(refBook?.CODE?.stringValue)
        }
    }
    return taxInspectionCache;
}

/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и фильтру (отсутствие значений не является ошибкой)
 * @param refBookId - идентификатор справочника
 * @param filter - фильтр
 * @return - возвращает лист
 */
def getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
 * @param refBookId - идентификатор справочника
 * @param recordIds - коллекция идентификаторов записей справочника
 * @return - возвращает мапу
 */
def getRefBookByRecordIds(def long refBookId, def recordIds) {
    def refBookMap = getProvider(refBookId).getRecordData(recordIds)
    if (refBookMap == null || refBookMap.size() == 0) {
        throw new ScriptException("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookMap
}

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

/**
 * Получить дату начала отчетного периода
 * @return
 */
def getReportPeriodStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

/**
 * Получить дату окончания отчетного периода
 * @return
 */
def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

/**
 * Получить параметры для конкретного тербанка
 * @return
 */
def getDepartmentParam() {
    def departmentId = declarationData.departmentId
    def departmentParamList = getProvider(REF_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }
    return departmentParamList?.get(0)
}

/**
 * Получить параметры подразделения
 * @param departmentParamId
 * @return
 */
def getOktmoAndKpp(def departmentParamId) {
    def mapNdflDetail = [:]
    def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
    def departmentParamTableList = getProvider(REF_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }
    def kppList = []
    departmentParamTableList.each { departmentParamTable ->
        kppList = mapNdflDetail.get(departmentParamTable?.OKTMO?.stringValue)
        if (kppList == null) {
            kppList = []
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(departmentParamTable?.OKTMO?.stringValue, kppList)
        } else if (!kppList.contains(departmentParamTable?.KPP?.stringValue)) {
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(departmentParamTable?.OKTMO?.stringValue, kppList)
        }
    }
    return mapNdflDetail
}

/**
 * Найти адресообразующий объект
 * @param regionCode код региона (обязательный параметр)
 * @param area район
 * @param city город
 * @param locality населенный пункт
 * @param street улица
 */
/**
 * Существует ли адрес в справочнике адресов
 */
@Memoized
boolean isExistsAddress(regionCode, area, city, locality, street) {
    if (!regionCode || !area || !city || !locality || !street) {
        return false
    }

    return fiasRefBookService.findAddress(regionCode, area, city, locality, street).size() > 0
}

/**
 * Преобразование массива имен полей в строку с помещением каждого имени в кавычки
 * @param fieldNameList
 */
def getQuotedFields(def fieldNameList) {
    def result = ""
    fieldNameList.each { fieldName ->
        if (result != "") {
            result += ", "
        }
        result += "\"" + fieldName + "\""
    }
    return result
}

/**
 * Формирование сообщения с повторяющимися номерами
 * @param inputList
 * @param tableName
 * @return
 */
def getErrorMsgDubl(def inputList, def tableName) {
    def resultMsg = ""
    def dublList = inputList.findAll { inputList.count(it) > 1 }.unique()
    if (dublList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + dublList.sort().join(", ") + "."
    }
    return resultMsg
}

/**
 * Формирование сообщения с пропущенными номерами
 * @param inputList
 * @param tableName
 * @return
 */
def getErrorMsgAbsent(def inputList, def tableName) {
    def absentList = []
    def sortList = inputList.unique().sort()
    if (sortList != null && sortList.size() > 0) {
        def i = sortList.get(0) == null ? 0 : sortList.get(0)
        sortList.each { item ->
            if (item != null) {
                if (item != i) {
                    absentList.add(i)
                }
                i++
            }
        }
    }
    def resultMsg = ""
    if (absentList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + absentList.sort().join(", ") + "."
    }
    return resultMsg
}