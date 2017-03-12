package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.util.slurpersupport.NodeChild
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.tools.DocGenerator
import org.springframework.jdbc.core.RowMapper

import javax.script.ScriptException
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.*
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Вид формы "Консолидированная", используется при определении проверок в частях
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataReference(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataCommon(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataIncome(java.lang.Object, java.lang.Object)}
 *
 */

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importData()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
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
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
}

@Field final FormDataKind FORM_DATA_KIND_PRIMARY = FormDataKind.PRIMARY;
@Field final FormDataKind FORM_DATA_KIND_CONSOLIDATED = FormDataKind.CONSOLIDATED;

/**
 * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
 */
@Field final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
@Field final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

/**
 * Справочник "Коды, определяющие налоговый (отчётный) период"
 */
@Field
def PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();

//------------------ Calculate ----------------------
/**
 * Порог схожести при идентификации физлиц 0..1000, 1000 - совпадение по всем параметрам
 */
@Field
int SIMILARITY_THRESHOLD = 700;

def calcTimeMillis(long time) {
    long currTime = System.currentTimeMillis();
    return " (" + (currTime - time) + " ms)";
}

@Field List<Country> countryRefBookCache = [];

List<Country> getCountryRefBookList() {

    if (countryRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.COUNTRY.getId());

        refBookRecords.each { refBookValueMap ->
            Country country = new Country();
            country.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue());
            country.setCode(refBookValueMap?.get("CODE")?.getStringValue());

            countryRefBookCache.add(country);
        }
    }
    return countryRefBookCache;
}

@Field List<DocType> docTypeRefBookCache = [];

List<DocType> getDocTypeRefBookList() {
    if (docTypeRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.DOCUMENT_CODES.getId());
        refBookRecords.each { refBookValueMap ->
            DocType docType = new DocType();
            docType.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue());
            docType.setName(refBookValueMap?.get("NAME")?.getStringValue());
            docType.setCode(refBookValueMap?.get("CODE")?.getStringValue());
            docType.setPriority(refBookValueMap?.get("PRIORITY")?.getNumberValue()?.intValue());
            docTypeRefBookCache.add(docType);
        }
    }
    return docTypeRefBookCache;
}

@Field List<TaxpayerStatus> taxpayerStatusRefBookCache = [];

List<TaxpayerStatus> getTaxpayerStatusRefBookList() {
    if (taxpayerStatusRefBookCache.isEmpty()) {
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.DOCUMENT_CODES.getId());
        refBookRecords.each { refBookValueMap ->
            TaxpayerStatus taxpayerStatus = new TaxpayerStatus();
            taxpayerStatus.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
            taxpayerStatus.setName(refBookValueMap?.get("NAME")?.getStringValue());
            taxpayerStatus.setCode(refBookValueMap?.get("CODE")?.getStringValue());
        }
    }
    return taxpayerStatusRefBookCache;
}

NaturalPersonPrimaryRnuRowMapper createPrimaryRowMapper() {

    NaturalPersonPrimaryRnuRowMapper naturalPersonRowMapper = new NaturalPersonPrimaryRnuRowMapper();
    naturalPersonRowMapper.setAsnuId(declarationData.asnuId);
    naturalPersonRowMapper.setLogger(logger);

    List<Country> countryList = getCountryRefBookList();
    naturalPersonRowMapper.setCountryCodeMap(countryList.collectEntries {
        [it.code, it]
    });

    List<DocGenerator.DocType> docTypeList = getDocTypeRefBookList();
    naturalPersonRowMapper.setDocTypeCodeMap(docTypeList.collectEntries {
        [it.code, it]
    });


    List<TaxpayerStatus> taxpayerStatusList = getTaxpayerStatusRefBookList();
    naturalPersonRowMapper.setTaxpayerStatusCodeMap(taxpayerStatusList.collectEntries {
        [it.code, it]
    });

    return naturalPersonRowMapper;
}

NaturalPersonRefbookHandler createRefbookHandler() {

    NaturalPersonRefbookHandler refbookHandler = new NaturalPersonRefbookHandler();

    refbookHandler.setLogger(logger);

    List<Country> countryList = getCountryRefBookList();
    refbookHandler.setCountryMap(countryList.collectEntries {
        [it.id, it]
    })


    List<DocType> docTypeList = getDocTypeRefBookList();
    refbookHandler.setDocTypeMap(docTypeList.collectEntries {
        [it.id, it]
    });

    List<TaxpayerStatus> taxpayerStatusList = getTaxpayerStatusRefBookList();
    refbookHandler.setTaxpayerStatusMap(taxpayerStatusList.collectEntries {
        [it.id, it]
    });

    return refbookHandler;
}

/**
 * Получить версию используемую для поиска записей в справочнике ФЛ
 */


@Field Date refBookPersonVersionTo = null;

def getRefBookPersonVersionTo() {
    if (refBookPersonVersionTo == null) {
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.MONTH, 0);
        localCalendar.set(Calendar.DATE, 1);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);
        localCalendar.add(Calendar.YEAR, 10);
        refBookPersonVersionTo = localCalendar.getTime();
    }
    return refBookPersonVersionTo;
}

def getRefBookPersonVersionFrom() {
    return getReportPeriodStartDate();
}


def calculate() {

    long timeFull = System.currentTimeMillis();
    long time = System.currentTimeMillis();

    logger.info("Начало расчета ПНФ");

    if (declarationData.asnuId == null) {
        throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!");
    }

    //выставляем параметр что скрипт не формирует новый xml-файл
    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

    //Получаем список всех ФЛ в первичной НФ
    List<NaturalPerson> primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFromNdfl(declarationData.id, createPrimaryRowMapper());

    logger.info("В ПНФ номер " + declarationData.id + " найдено записей о физ.лицах: " + primaryPersonDataList.size() + calcTimeMillis(time));

    time = System.currentTimeMillis();
    Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries {
        [it.getPrimaryPersonId(), it]
    }
    logger.info("map to id: " + calcTimeMillis(time));

    //Заполнени временной таблицы версий
    time = System.currentTimeMillis();
    refBookPersonService.fillRecordVersions(getRefBookPersonVersionTo());
    logger.info("fillRecordVersions: " + calcTimeMillis(time));

    //Шаг 1. список физлиц первичной формы для создания записей в справочниках
    time = System.currentTimeMillis();
    List<NaturalPerson> insertPersonList = refBookPersonService.findPersonForInsertFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createPrimaryRowMapper());
    logger.info("step1 find insertRecords: " + insertPersonList.size() + calcTimeMillis(time));

    time = System.currentTimeMillis();
    createNaturalPersonRefBookRecords(insertPersonList);
    logger.info("createNaturalPersonRefBookRecords: " + calcTimeMillis(time));


    time = System.currentTimeMillis();
    //Шаг 2. идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("step2 similarityPersonMap: " + similarityPersonMap.size() + calcTimeMillis(time));

    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
    //updateNaturalPersonRefBookReferenceRecords(primaryPersonMap, similarityPersonMap);
    logger.info("updateNaturalPersonRefBookRecords: " + calcTimeMillis(time));

    time = System.currentTimeMillis();
    Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("step3 checkSimilarityPersonMap: " + checkSimilarityPersonMap.size() + calcTimeMillis(time));

    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
    //updateNaturalPersonRefBookReferenceRecords(primaryPersonMap, similarityPersonMap);
    logger.info("updateNaturalPersonRefBookRecords: " + calcTimeMillis(time));

    logger.info("end find data: " + checkSimilarityPersonMap.size() + calcTimeMillis(timeFull));

    logger.info("Завершение расчета ПНФ " + " " + calcTimeMillis(timeFull));
}

//---------------- identification ----------------

def createNaturalPersonRefBookRecords(List<NaturalPerson> insertRecords) {

    println "start create insertRecords=" + insertRecords

    if (insertRecords != null && !insertRecords.isEmpty()) {

        List<Address> addressList = new ArrayList<Address>();
        List<PersonDocument> documentList = new ArrayList<PersonDocument>();
        List<PersonIdentifier> identifierList = new ArrayList<PersonIdentifier>();

        for (NaturalPerson person : insertRecords) {

            Address address = person.getAddress();
            if (address != null) {
                addressList.add(address);
            }

            PersonDocument personDocument = person.getPersonDocument();
            if (personDocument != null) {
                documentList.add(personDocument);
            }

            PersonIdentifier personIdentifier = person.getPersonIdentifier();
            if (personIdentifier != null) {
                identifierList.add(personIdentifier);
            }

        }

        println "insert address"

        //insert addresses batch
        insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressList, { address ->
            mapAddressAttr(address)
        });

        println "insert person"
        //insert persons batch
        insertBatchRecords(RefBook.Id.PERSON.getId(), insertRecords, { person ->
            mapPersonAttr(person)
        });

        println "insert personDocument"

        //insert documents batch
        insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentList, { personDocument ->
            mapPersonDocumentAttr(personDocument)
        });


        println "insert personIdentifier"

        //insert identifiers batch
        insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifierList, { personIdentifier ->
            mapPersonIdentifierAttr(personIdentifier)
        });

        println "insert updateRefBookPersonReferences"
        //update reference to ref book7uuiuji
        ndflPersonService.updateRefBookPersonReferences(insertRecords);

        //Выводим информацию о созданных записях
        for (NaturalPerson person : insertRecords) {
            String noticeMsg = String.format("Создана новая запись в справочнике 'Физические лица': %d, %s %s %s", person.getId(), person.getLastName(), person.getFirstName(), (person.getMiddleName() ?: ""));
            logger.info(noticeMsg);
        }

    }
    println "end create"
}

/**
 *
 * @param primaryPersonMap
 * @param similarityPersonMap
 * @return
 */
def updateNaturalPersonRefBookReferenceRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {
    List<NaturalPerson> insertPersonList = new ArrayList<NaturalPerson>();
    List<NaturalPerson> updatePersonList = new ArrayList<NaturalPerson>();
    for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {
        Long primaryPersonId = entry.getKey();
        Map<Long, NaturalPerson> similarityPersonValues= entry.getValue();
        List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values());
        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);
        NaturalPerson refBookPerson = refBookPersonService.identificatePerson(primaryPerson, similarityPersonList, SIMILARITY_THRESHOLD, logger);
        if (refBookPerson != null) {
            primaryPerson.setId(refBookPerson.getId());
            updatePersonList.add(primaryPerson);
        } else {
            //Если метод identificatePerson вернул null, то это означает что в списке сходных записей отсутствуют записи перевыщающие порог схожести
            insertPersonList.add(primaryPerson);
        }
    }
    //crete and update reference
    createNaturalPersonRefBookRecords(insertPersonList);
    //update reference to ref book
    ndflPersonService.updateRefBookPersonReferences(updatePersonList);
}


/**
 *
 * @param primaryPersonMap
 * @param similarityPersonMap
 * @return
 */
def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

    //Проходим по списку и определяем наиболее подходящюю запись, если подходящей записи не найдено то содадим ее
    List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>();
    List<NaturalPerson> insertPersonList = new ArrayList<NaturalPerson>();
    List<Map<String, RefBookValue>> updatePersonList = new ArrayList<Map<String, RefBookValue>>();

    List<Address> insertAddressList = new ArrayList<Address>();
    List<Map<String, RefBookValue>> updateAddressList = new ArrayList<Map<String, RefBookValue>>();

    List<PersonDocument> insertDocumentList = new ArrayList<PersonDocument>();
    List<PersonDocument> updateDocumentList = new ArrayList<PersonDocument>();

    List<PersonIdentifier> insertIdentifierList = new ArrayList<PersonIdentifier>();
    List<Map<String, RefBookValue>> updateIdentifierList = new ArrayList<Map<String, RefBookValue>>();

    int updCnt = 0;
    for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {
        Long primaryPersonId = entry.getKey();

        Map<Long, NaturalPerson> similarityPersonValues = entry.getValue();

        List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values());

        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);
        NaturalPerson refBookPerson = refBookPersonService.identificatePerson(primaryPerson, similarityPersonList, SIMILARITY_THRESHOLD, logger);

        AttributeCountChangeListener addressAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener personAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener documentAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener taxpayerIdentityAttrCnt = new AttributeCountChangeListener();

        if (refBookPerson != null) {
            //address
            if (primaryPerson.getAddress() != null) {
                if (refBookPerson.getAddress() != null) {
                    Map<String, RefBookValue> refBookAddressValues = mapAddressAttr(refBookPerson.getAddress());
                    refBookAddressValues.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookPerson.getAddress().getId()));
                    updateAddressAttr(refBookAddressValues, primaryPerson.getAddress(), addressAttrCnt);
                    if (addressAttrCnt.isUpdate()) {
                        updateAddressList.add(refBookAddressValues);
                    }
                } else {
                    insertAddressList.add(primaryPerson.getAddress());
                }
            }

            //person
            Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(refBookPerson);
            refBookPersonValues.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookPerson.getId()));
            updatePersonAttr(refBookPersonValues, primaryPerson, personAttrCnt);
            if (personAttrCnt.isUpdate()) {
                updatePersonList.add(refBookPersonValues);
            }

            //documents
            PersonDocument primaryPersonDocument = primaryPerson.getPersonDocument();
            if (primaryPersonDocument != null) {
                Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                PersonDocument personDocument = BaseWeigthCalculator.findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());
                if (personDocument == null) {

                    insertDocumentList.add(primaryPersonDocument);

                    List<PersonDocument> personDocumentList = new ArrayList<PersonDocument>();
                    personDocumentList.add(primaryPersonDocument)
                    personDocumentList.addAll(refBookPerson.getPersonDocumentList());

                    updatePriority(personDocumentList, documentAttrCnt);

                    updateDocumentList.addAll(personDocumentList);

                }
            }

            //identifiers
            PersonIdentifier primaryPersonIdentifier = primaryPerson.getPersonIdentifier();
            if (primaryPersonIdentifier != null) {
                //Ищем совпадение в списке идентификаторов
                PersonIdentifier refBookPersonIdentifier = findIdentifierByAsnu(refBookPerson, primaryPersonIdentifier.getAsnuId());
                if (refBookPersonIdentifier != null) {

                    String primaryInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());
                    String refbookInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());

                    if (!BaseWeigthCalculator.equalsNullSafe(primaryInp, refbookInp)) {
                        AttributeChangeEvent changeEvent = new AttributeChangeEvent("INP", primaryInp);
                        changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.STRING, refbookInp));
                        changeEvent.setType(AttributeChangeEventType.REFRESHED);
                        taxpayerIdentityAttrCnt.processAttr(changeEvent);

                        Map<String, RefBookValue> refBookPersonIdentifierValues = mapPersonIdentifierAttr(refBookPersonIdentifier);
                        refBookPersonIdentifierValues.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookPersonIdentifier.getId()));
                        updateIdentifierList.add(refBookPersonIdentifierValues);
                    }

                } else {
                    insertIdentifierList.add(refBookPersonIdentifier);
                }
            }

            primaryPerson.setId(refBookPerson.getId());
            updatePersonReferenceList.add(primaryPerson);

            if (addressAttrCnt.isUpdate() || personAttrCnt.isUpdate() || documentAttrCnt.isUpdate() || taxpayerIdentityAttrCnt.isUpdate()) {

                def recordId = refBookPerson.getRecordId();

                logger.info(String.format("Обновлена запись в справочнике 'Физические лица': %d, %s %s %s", recordId,
                        refBookPerson.getLastName(),
                        refBookPerson.getFirstName(),
                        refBookPerson.getMiddleName()) + " " + buildRefreshNotice(addressAttrCnt, personAttrCnt, documentAttrCnt, taxpayerIdentityAttrCnt));
                updCnt++;
            }
        } else {
            //Если метод identificatePerson вернул null, то это означает что в списке сходных записей отсутствуют записи перевыщающие порог схожести
            insertPersonList.add(primaryPerson);
        }
    }


    //crete and update reference
    createNaturalPersonRefBookRecords(insertPersonList);

    //update reference to ref book
    if (!updatePersonReferenceList.isEmpty()) {
        ndflPersonService.updateRefBookPersonReferences(updatePersonReferenceList);
    }

    insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), insertAddressList, { address ->
        mapAddressAttr(address)
    });

    insertBatchRecords(RefBook.Id.ID_DOC.getId(), insertDocumentList, { personDocument ->
        mapPersonDocumentAttr(personDocument)
    });

    insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), insertIdentifierList, { personIdentifier ->
        mapPersonIdentifierAttr(personIdentifier)
    });


    List<Map<String, RefBookValue>> refBookDocumentList = new ArrayList<Map<String, RefBookValue>>();


    for (PersonDocument personDoc: updateDocumentList){
        Map<String, RefBookValue> values = mapPersonDocumentAttr(personDoc);
        values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, personDoc.getId()));
        refBookDocumentList.addAll();
    }

    for (Map<String, RefBookValue> refBookValues : updateAddressList) {
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getReferenceValue()?.longValue();
        getProvider(RefBook.Id.PERSON_ADDRESS.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updatePersonList) {
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getReferenceValue()?.longValue();
        getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : refBookDocumentList) {
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getReferenceValue()?.longValue();
        getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updateIdentifierList) {
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getReferenceValue()?.longValue();
        getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }


}

PersonIdentifier findIdentifierByAsnu(NaturalPerson person, Long asnuId) {
    for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
        if (asnuId != null && asnuId.equals(personIdentifier.getAsnuId())) {
            return personIdentifier;
        }
    }
    return null;
}

/**
 * Метод установленный признак включения в отчетность на основе приоритета
 */
def updatePriority(List<PersonDocument> personDocumentList, AttributeChangeListener attributeChangeListener) {
    for (PersonDocument personDocument : personDocumentList) {
        personDocument.setIncRep(0);
    }

    PersonDocument minimalPriorDoc = personDocumentList.min { it.getDocType().getPriority() }

    AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", minimalPriorDoc.getIncRep());
    changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.NUMBER, 1));
    attributeChangeListener.processAttr(changeEvent);

    minimalPriorDoc.setIncRep(1);
}

def updateAddressAttr(Map<String, RefBookValue> values, Address address, AttributeChangeListener attributeChangeListener) {
    putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType(), attributeChangeListener);
    putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId(), attributeChangeListener);
    putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode(), attributeChangeListener);
    putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict(), attributeChangeListener);
    putOrUpdate(values, "CITY", RefBookAttributeType.STRING, address.getCity(), attributeChangeListener);
    putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality(), attributeChangeListener);
    putOrUpdate(values, "STREET", RefBookAttributeType.STRING, address.getStreet(), attributeChangeListener);
    putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse(), attributeChangeListener);
    putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, address.getBuild(), attributeChangeListener);
    putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment(), attributeChangeListener);
    putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode(), attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno(), attributeChangeListener);
}

def mapAddressAttr(Address address) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType());
    putValue(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId());
    putValue(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode());
    putValue(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict());
    putValue(values, "CITY", RefBookAttributeType.STRING, address.getCity());
    putValue(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality());
    putValue(values, "STREET", RefBookAttributeType.STRING, address.getStreet());
    putValue(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse());
    putValue(values, "BUILD", RefBookAttributeType.STRING, address.getBuild());
    putValue(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment());
    putValue(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode());
    putValue(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno());
    return values;
}


def updatePersonAttr(Map<String, RefBookValue> values, NaturalPerson person, AttributeChangeListener attributeChangeListener) {
    putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), attributeChangeListener);
    putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), attributeChangeListener);
    putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), attributeChangeListener);
    putOrUpdate(values, "SEX", RefBookAttributeType.STRING, person.getSex(), attributeChangeListener);
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), attributeChangeListener);
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), attributeChangeListener);
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), attributeChangeListener);
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, null, attributeChangeListener);
    putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), attributeChangeListener);
    putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null, attributeChangeListener);
    putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId(), attributeChangeListener);
    putOrUpdate(values, "PENSION", RefBookAttributeType.NUMBER, person.getPension(), attributeChangeListener);
    putOrUpdate(values, "MEDICAL", RefBookAttributeType.NUMBER, person.getMedical(), attributeChangeListener);
    putOrUpdate(values, "SOCIAL", RefBookAttributeType.NUMBER, person.getSocial(), attributeChangeListener);
    putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee(), attributeChangeListener);
    putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId(), attributeChangeListener);
    putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId(), attributeChangeListener);
    putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId, attributeChangeListener);
    putOrUpdate(values, "OLD_ID", RefBookAttributeType.REFERENCE, null, attributeChangeListener);
}

def mapPersonAttr(NaturalPerson person) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName());
    putValue(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName());
    putValue(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName());
    putValue(values, "SEX", RefBookAttributeType.NUMBER, person.getSex());
    putValue(values, "INN", RefBookAttributeType.STRING, person.getInn());
    putValue(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign());
    putValue(values, "SNILS", RefBookAttributeType.STRING, person.getSnils());
    putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
    putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate());
    putValue(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null);
    putValue(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId());
    putValue(values, "PENSION", RefBookAttributeType.NUMBER, person.getPension() ?: 2);
    putValue(values, "MEDICAL", RefBookAttributeType.NUMBER, person.getMedical() ?: 2);
    putValue(values, "SOCIAL", RefBookAttributeType.NUMBER, person.getSocial() ?: 2);
    putValue(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee() ?: 2);
    putValue(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId());
    putValue(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId());
    putValue(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId);
    putValue(values, "OLD_ID", RefBookAttributeType.REFERENCE, null);
    return values;
}

def mapPersonDocumentAttr(PersonDocument personDocument) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personDocument.getNaturalPerson().getId());
    putValue(values, "DOC_NUMBER", RefBookAttributeType.STRING, personDocument.getDocumentNumber());
    putValue(values, "ISSUED_BY", RefBookAttributeType.STRING, null);
    putValue(values, "ISSUED_DATE", RefBookAttributeType.DATE, null);
    putValue(values, "INC_REP", RefBookAttributeType.NUMBER, personDocument.getIncRep() ?: 1); //default value is 1
    putValue(values, "DOC_ID", RefBookAttributeType.REFERENCE, personDocument.getDocType()?.getId());
    return values;
}

def mapPersonIdentifierAttr(PersonIdentifier personIdentifier) {
    Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
    putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personIdentifier.getNaturalPerson().getId());
    putValue(values, "INP", RefBookAttributeType.STRING, personIdentifier.getInp());
    putValue(values, "AS_NU", RefBookAttributeType.REFERENCE, personIdentifier.getAsnuId());
    return values;
}

def insertBatchRecords(refBookId, identityObjectList, refBookMapper) {
    //подготовка записей

    println "insertBatchRecords refBookId=" + refBookId + ", identityObjectList=" + identityObjectList.size + ", refBookMapper=" + refBookMapper

    if (identityObjectList != null && !identityObjectList.isEmpty()) {
        List<RefBookRecord> recordList = new ArrayList<RefBookRecord>();
        for (IdentityObject identityObject : identityObjectList) {
            def values = refBookMapper(identityObject);
            recordList.add(createRefBookRecord(values));
        }

        //создание записей справочника
        List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, getRefBookPersonVersionFrom(), null, recordList);

        //установка id
        for (int i = 0; i < identityObjectList.size(); i++) {
            Long id = generatedIds.get(i);
            IdentityObject identityObject = identityObjectList.get(i);
            identityObject.setId(id);
        }
    }

}

def putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
    values.put(attrName, new RefBookValue(type, value));
}

/**
 * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
 */
def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, AttributeChangeListener attributeChangedListener) {

    AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, value);

    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи, если новое значение задано и отличается от существующего
        changeEvent.setCurrentValue(refBookValue);
        if (value != null && !ScriptUtils.equalsNullSafe(refBookValue.getValue(), value)) {
            //значения не равны, обновление
            refBookValue.setValue(value);
            changeEvent.setType(AttributeChangeEventType.REFRESHED);
        }
    } else {
        //создание новой записи
        valuesMap.put(attrName, new RefBookValue(type, value));
        changeEvent.setType(AttributeChangeEventType.CREATED);
    }

    attributeChangedListener.processAttr(changeEvent);
}

/**
 * Создание новой записи справочника адреса физлиц
 * @param person
 * @return
 */
def createRefBookRecord(Map<String, RefBookValue> values) {
    RefBookRecord record = new RefBookRecord();
    record.setValues(values);
    return record;
}

def buildRefreshNotice(AttributeCountChangeListener addressAttrCnt, AttributeCountChangeListener personAttrCnt, AttributeCountChangeListener documentAttrCnt, AttributeCountChangeListener taxpayerIdentityAttrCnt) {
    StringBuffer sb = new StringBuffer();
    appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb);
    appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb);
    //appendAttrInfo(RefBook.Id.ID_DOC.getId(), documentAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_TAX_PAYER.getId(), taxpayerIdentityAttrCnt, sb);
    return sb.toString();
}


@Field HashMap<Long, RefBook> mapRefBookToIdCache = new HashMap<Long, RefBook>();

def getRefBookFromCache(Long id) {
    RefBook refBook = mapRefBookToIdCache.get(id);
    if (refBook != null) {
        return refBook;
    } else {
        refBook = refBookFactory.get(id);
        mapRefBookToIdCache.put(id, refBook);
        return refBook;
    }
}

@Field Map<Long, Map<String, String>> refBookAttrCache = new HashMap<Long, Map<String, String>>();

def getAttrNameFromRefBook(Long id, String alias) {
    Map<String, String> attrMap = refBookAttrCache.get(id);
    if (attrMap != null) {
        return attrMap.get(alias);
    } else {
        attrMap = new HashMap<String, String>();
        RefBook refBook = getRefBookFromCache(id);
        List<RefBookAttribute> refBookAttributeList = refBook.getAttributes();
        for (RefBookAttribute attr : refBookAttributeList) {
            attrMap.put(attr.getAlias(), attr.getName());
        }
        refBookAttrCache.put(id, attrMap);
        return attrMap.get(alias);
    }
}

def appendAttrInfo(Long refBookId, AttributeCountChangeListener attrCounter, StringBuffer sb) {
    if (attrCounter != null && attrCounter.isUpdate()) {
        List<String> msgList = new ArrayList<String>();
        for (Map.Entry<String, String> msgEntry : attrCounter.getMessages()) {
            String aliasKey = msgEntry.getKey();
            String msg = msgEntry.getValue();
            msgList.add(new StringBuffer(getAttrNameFromRefBook(refBookId, aliasKey)).append(": ").append(msg).toString())
        }

        if (!msgList.isEmpty()) {
            sb.append(Arrays.toString(msgList.toArray()));
        }
    }
}


//------------------ IDENTIFICATION END --------------------------
/**
 * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
 * @param personMap
 * @return
 */
def getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    def addressIds = [];
    personMap.each { personId, person ->
        if (person.get(RF_ADDRESS).value != null) {
            addressIds.add(person.get(RF_ADDRESS).value)
        }
    }

    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
    if (refBookMap != null && !addressIds.isEmpty()) {
        return refBookMap;
    } else {
        return Collections.emptyMap();
    }
}

/**
 * Получить "Физические лица"
 * NDFL_PERSON.PERSON_ID будет ссылаться на актуальную записи справочника ФЛ только после проведения расчета
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersonsByDeclarationDataId() {
    Long declarationDataId = declarationData.id;
    String whereClause = String.format("id in (select person_id from ndfl_person where declaration_data_id = %s)", declarationDataId)
    return getRefBookByRecordWhere(REF_BOOK_PERSON_ID, whereClause)
}

/**
 * Получить актуальные на отчетную дату записи справочника "Физические лица"
 * @return Map < person_id , Map < имя_поля , значение_поля > >
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId() {
    String whereClause = """
            JOIN ref_book_person p ON (frb.record_id = p.record_id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationData.id} AND p.id = np.person_id)
        """
    def refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_PERSON_ID, whereClause, getReportPeriodEndDate() - 1)
    def refBookMapResult = [:]
    refBookMap.each { personId, refBookValue ->
        Long refBookRecordId = refBookValue.get(RF_RECORD_ID).value
        refBookMapResult.put(refBookRecordId, refBookValue)
    }
    return refBookMapResult
}

//Приоритет документов удостоверяющих личность <Идентификатор, Приоритет(int)>
@Field def documentPriorityCache = [:]

def getRefDocumentPriority() {
    if (documentPriorityCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentPriorityCache.put(refBook?.id?.numberValue, refBook?.PRIORITY?.numberValue?.intValue())
        }
    }
    return documentPriorityCache;
}

/**
 * Получить "ИНП"
 */
def getRefInpMapByDeclarationDataId() {
    if (inpCache.isEmpty()) {
        Long declarationDataId = declarationData.id;
        String whereClause = String.format("person_id in(select person_id from ndfl_person where declaration_data_id = %s)", declarationDataId)
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_TAX_PAYER.id, whereClause)

        refBookMap.each { personId, refBook ->
            def inpList = inpCache.get(refBook?.PERSON_ID?.referenceValue);
            if (inpList == null) {
                inpList = [];
            }
            inpList.add(refBook?.INP?.stringValue)
            inpCache.put(refBook?.PERSON_ID?.referenceValue, inpList)
        }
    }
    return inpCache;
}

/**
 * Получить "ИНП"
 */
Map<Long, Map<String, RefBookValue>> getActualRefInpMapByDeclarationDataId() {
    if (inpActualCache.isEmpty()) {
        String whereClause = """
            JOIN ref_book_person p ON (frb.person_id = p.id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationData.id} AND p.id = np.person_id)
        """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_ID_TAX_PAYER_ID, whereClause, getReportPeriodEndDate() - 1)

        refBookMap.each { id, refBook ->
            List<String> inpList = inpActualCache.get(refBook?.PERSON_ID?.referenceValue)
            if (inpList == null) {
                inpList = []
            }
            inpList.add(refBook?.INP?.stringValue)
            inpActualCache.put(refBook?.PERSON_ID?.referenceValue, inpList)
        }
    }
    return inpActualCache
}

/**
 * По коду статуса налогоплательщика найти id записи в кэше справочника
 * @param String code
 * @return Long id
 */
def findTaxpayerStatusByCode(code) {
    def taxpayerStatusMap = getRefTaxpayerStatusCode();
    def result = taxpayerStatusMap.find {
        it.value == code
    }?.key

    if (code != null && !code.isEmpty() && result == null) {
        logger.warn("В справочнике 'Статусы налогоплательщика' не найдена запись, статус с кодом " + code);
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
    Map<Long, String> documentTypeMap = getRefDocumentTypeCode()
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
    def citizenshipCodeMap = getRefCountryCode();
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

    Column column6 = new DateColumn()
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

    Column column6 = new DateColumn()
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

        scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")

    } else {
        throw new ServiceException("Не найдены данные для формирования отчета!");
    }
}

/**
 * Формат имени файла: РНУ_НДФЛ_<ИД формы>_<ФамилияИО>_<ДУЛ>_<ДатаВремя выгрузки>, где
 * <ИД формы> - ID формы из БД
 * <ФамилияИО> - Фамилия ФЛ полностью + первая буква имени + первая буква отчества (при наличии). Пример: ИвановаИИ
 * <ДУЛ> - Серия и номер документа, удостоверяющего личность в формате "Серия№Номер", Серия и Номер ДУЛ не должны содержать разделителей. Пример: 8888№123321
 * <ДатаВремя выгрузки> - дата и время выгрузки в формате ГГГГММДД_ЧЧММ. Пример: 20160216_1842
 * @return
 */
def createFileName(NdflPerson ndflPerson) {
    StringBuilder sb = new StringBuilder();
    sb.append("РНУ_НДФЛ_");
    sb.append(declarationData.id).append("_");
    sb.append(capitalize(ndflPerson.lastName));
    sb.append(firstChar(ndflPerson.firstName));
    sb.append(firstChar(ndflPerson.middleName)).append("_");
    sb.append(ndflPerson.idDocNumber?.replaceAll("\\s", "")?.toLowerCase()).append("_");
    sb.append(new SimpleDateFormat("yyyy.MM.dd_HHmm").format(new Date()));
    return sb.toString();
}


String firstChar(String str) {
    if (str != null && !str.isEmpty()) {
        return String.valueOf(Character.toUpperCase(str.charAt(0)));
    } else {
        return "";
    }
}

String capitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
        return str;
    }
    return new StringBuilder(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1).toLowerCase())
            .toString();
}

void calculateReportData(writer, ndflPerson) {

    //отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения
    def departmentId = declarationData.departmentId

    //Подразделение
    def department = departmentService.get(departmentId)

    def reportPeriodCode = findReportPeriodCode(reportPeriod)

    // Подготовка данных для СведОпер
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

    def builder = new MarkupBuilder(writer)
    builder.Файл() {
        СлЧасть('КодПодр': department.sbrfCode) {}
        ИнфЧасть('ПериодОтч': reportPeriodCode, 'ОтчетГод': reportPeriod?.taxPeriod?.year) {
            ПолучДох(ndflPersonAttr(ndflPerson)) {}
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
 * Находит код периода из справочника "Коды, определяющие налоговый (отчётный) период"
 */
def findReportPeriodCode(reportPeriod) {
    RefBookDataProvider dataProvider = refBookFactory.getDataProvider(RefBook.Id.PERIOD_CODE.getId())
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
        throw new ServiceException("ТФ не соответствует XSD-схеме РНУ НФДЛ. Загрузка невозможна.");
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
    deductions.addAll(ndflPersonOperationsNode.'СведВыч'.collect {
        transformNdflPersonDeduction(it)
    });
    ndflPerson.deductions.addAll(deductions)

    List<NdflPersonPrepayment> prepayments = new ArrayList<NdflPersonPrepayment>();
    prepayments.addAll(ndflPersonOperationsNode.'СведАванс'.collect {
        transformNdflPersonPrepayment(it)
    });
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
            'ДопИнф'     : ndflPerson.additionalData
    ]
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

//Далее и до конца файла идет часть проверок общая для первичной и консолидированно,
//если проверки различаются то используется параметр {@link #FORM_DATA_KIND_CONSOLIDATED}
//При внесении изменений учитывается что эта чать скрипта используется(копируется) и в первичной и в консолидированной

//>------------------< REF BOOK >----------------------<

// Дата начала отчетного периода
@Field def periodStartDate = null

// Дата окончания отчетного периода
@Field def periodEndDate = null

// Кэш провайдеров cправочников
@Field Map<Long, RefBookDataProvider> providerCache = [:]
//Физлица
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id
@Field Map<Long, Map<String, RefBookValue>> personsActualCache = [:]

//Коды Асну
@Field Map<Long, String> asnuCache = [:]

//Коды стран из справочника
@Field Map<Long, String> countryCodeCache = [:]

//Виды документов, удостоверяющих личность
@Field Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
@Field Map<Long, String> documentTypeCodeCache = [:]

//Коды статуса налогоплательщика
@Field Map<Long, String> taxpayerStatusCodeCache = [:]

//Адреса физлиц
@Field Map<Long, Map<String, RefBookValue>> addressCache = [:]

//<person_id:  list<id: <record>>>
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulCache = [:]
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulActualCache = [:]

@Field def sourceReportPeriod = null

@Field Map<Long, DepartmentReportPeriod> departmentReportPeriodMap = [:]

@Field Map<Long, DeclarationTemplate> declarationTemplateMap = [:]

@Field Map<Long, String> departmentFullNameMap = [:]

// Мапа <ID_Данные о физическом лице - получателе дохода, Физическое лицо: <ФИО> ИНП:<ИНП>>
@Field def ndflPersonFLMap = [:]
@Field final TEMPLATE_PERSON_FL = "Физическое лицо: \"%s\", ИНП: \"%s\""

// Виды документов, удостоверяющих личность Мапа <Идентификатор, Код>
@Field def documentCodesCache = [:]
@Field final long REF_BOOK_DOCUMENT_ID = RefBook.Id.DOCUMENT_CODES.id

// Статус налогоплательщика Мапа <Идентификатор, Код>
@Field def taxpayerStatusCache = [:]
@Field final long REF_BOOK_TAXPAYER_STATUS_ID = RefBook.Id.TAXPAYER_STATUS.id

@Field final long REF_BOOK_ADDRESS_ID = RefBook.Id.PERSON_ADDRESS.id

// Тербанки Мапа <id, наименование>
@Field def terBankCache = [:]
@Field final long REF_DEPARTMENT_ID = RefBook.Id.DEPARTMENT.id

// ИНП <person_id:  list<id: <record>>>
@Field Map<Long, List<Map<String, RefBookValue>>> inpActualCache = [:]
@Field Map<Long, List<Map<String, RefBookValue>>> inpCache = [:]
@Field final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id

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

// Кэш для справочников
@Field def refBookCache = [:]

/**
 * Получить "АСНУ"
 * @return
 */
def getRefAsnu() {
    if (asnuCache.size() == 0) {
        def refBookMap = getRefBook(RefBook.Id.ASNU.id)
        refBookMap.each { refBook ->
            asnuCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return asnuCache;
}


def getDeclarationTemplateById(def id) {
    if (id != null && declarationTemplateMap[id] == null) {
        declarationTemplateMap[id] = declarationService.getTemplate(id)
    }
    return declarationTemplateMap[id]
}

def getReportPeriod() {
    if (sourceReportPeriod == null) {
        sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return sourceReportPeriod
}


def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
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
Date getReportPeriodEndDate() {
    if (periodEndDate == null) {
        periodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return periodEndDate
}

/**
 * Выгрузка из справочников по условию
 * @param refBookId
 * @param whereClause
 * @return
 */
def getRefBookByRecordWhere(def long refBookId, def whereClause) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
    if (refBookMap == null || refBookMap.size() == 0) {
        //throw new ScriptException("Не найдены записи справочника " + refBookId)
        return Collections.emptyMap();
    }
    return refBookMap
}

/**
 * Выгрузка из справочников по условию и версии
 * @param refBookId
 * @param whereClause
 * @return
 * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
 */
def getRefBookByRecordVersionWhere(def long refBookId, def whereClause, def version) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
    if (refBookMap == null || refBookMap.size() == 0) {
        //throw new ScriptException("Не найдены записи справочника " + refBookId)
        return Collections.emptyMap();
    }
    return refBookMap
}

/**
 * Получить "Страны"
 * @return
 */
def getRefCountryCode() {
    if (countryCodeCache.size() == 0) {
        def refBookMap = getRefBook(RefBook.Id.COUNTRY.getId())
        refBookMap.each { refBook ->
            countryCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return countryCodeCache;
}

/**
 * Получить "Виды документов"
 */
def getRefDocumentType() {
    if (documentTypeCache.size() == 0) {
        def refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.getId())
        refBookList.each { refBook ->
            documentTypeCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return documentTypeCache;
}

def getRefDocumentTypeCode() {
    if (documentTypeCodeCache.size() == 0) {
        def refBookList = getRefDocumentType()
        refBookList.each { id, refBookValueMap ->
            documentTypeCodeCache.put(id, refBookValueMap?.get("CODE")?.getStringValue())
        }
    }
    return documentTypeCodeCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefTaxpayerStatusCode() {
    if (taxpayerStatusCodeCache.size() == 0) {
        def refBookMap = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId())
        refBookMap.each { refBook ->
            taxpayerStatusCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return taxpayerStatusCodeCache;
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
 * Получить "Адреса налогоплательщика"
 * @return
 */
def getRefAddress(def addressIds) {
    if (addressCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(RefBook.Id.PERSON_ADDRESS.getId(), addressIds)
        refBookMap.each { addressId, address ->
            addressCache.put(addressId, address)
        }
    }
    return addressCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
def getRefDulByDeclarationDataId() {

    if (dulCache.isEmpty()) {
        Long declarationDataId = declarationData.id;
        String whereClause = String.format("person_id in (select person_id from ndfl_person where declaration_data_id = %s)", declarationDataId)
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(REF_BOOK_ID_DOC_ID, whereClause)

        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            def dulList = dulCache.get(refBookPersonId);
            if (dulList == null) {
                dulList = [];
                dulCache.put(refBookPersonId, dulList)
            }
            dulList.add(refBookValues);
        }
    }
    return dulCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataId() {
    if (dulActualCache.isEmpty()) {
        String whereClause = """
            JOIN ref_book_person p ON (frb.person_id = p.id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationData.id} AND p.id = np.person_id)
        """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_ID_DOC_ID, whereClause, getReportPeriodEndDate() - 1)

        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            def dulList = dulActualCache.get(refBookPersonId);
            if (dulList == null) {
                dulList = [];
            }
            dulList.add(refBookValues);
            dulActualCache.put(refBookPersonId, dulList)
        }
    }
    return dulActualCache
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
List<Map<String, RefBookValue>> getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    List<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
 * @param refBookId - идентификатор справочника
 * @param recordIds - коллекция идентификаторов записей справочника
 * @return - возвращает мапу
 */
def getRefBookByRecordIds(def long refBookId, def recordIds) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordData(recordIds)
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
 * Разыменование записи справочника
 */
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//>------------------< UTILS >----------------------<

// Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
@Field final long REF_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_NOT_FOUND_REF = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
@Field final String MESSAGE_ERROR_VALUE = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: %s."
@Field final String MESSAGE_ERROR_INN = "Некорректный ИНН"
@Field final String MESSAGE_ERROR_SNILS = "Некорректный СНИЛС"
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
@Field final String MESSAGE_ERROR_DUL = "Указанный документ не включается в отчетность"


@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" размером %d."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" в колличестве %d."

// Таблицы
@Field final String T_PERSON = "Реквизиты"
@Field final String T_PERSON_INCOME = "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION = "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT = "Сведения о доходах в виде авансовых платежей"

// Справочники
@Field final String R_FIAS = "КЛАДР" //TODO замена
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
@Field final String C_ID_DOC = "Документ, удостоверяющий личность"
@Field final String C_ID_DOC_TYPE = "Код вида документа"
@Field final String C_INC_REP = "Включается в отчетность"
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
@Field final String C_SNILS = "СНИЛС"
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

//Адрес
@Field final String RF_COUNTRY = "COUNTRY"
@Field final String RF_REGION_CODE = "REGION_CODE"
@Field final String RF_DISTRICT = "DISTRICT"
@Field final String RF_CITY = "CITY"
@Field final String RF_LOCALITY = "LOCALITY"
@Field final String RF_STREET = "STREET"
@Field final String RF_HOUSE = "HOUSE"
@Field final String RF_BUILD = "BUILD"
@Field final String RF_APPARTMENT = "APPARTMENT"

//ДУЛ
@Field final String RF_DOC_ID = "DOC_ID"
@Field final String RF_DOC_NUMBER = "DOC_NUMBER"
@Field final String RF_INC_REP = "INC_REP"

//>------------------< CHECK DATA >----------------------<

/**
 * Проверки НДФЛ (первичная и консолидированная)
 * @return
 */
def checkData() {
    long time = System.currentTimeMillis();
    // Реквизиты
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON, ndflPersonList.size())

    // Сведения о доходах и НДФЛ
    List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_INCOME, ndflPersonIncomeList.size())

    // Сведения о вычетах
    List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION, ndflPersonDeductionList.size())

    // Сведения о доходах в виде авансовых платежей
    List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT, ndflPersonPrepaymentList.size())

    println "Получение записей из таблиц НФДЛ: " + (System.currentTimeMillis() - time);
    logger.info("Получение записей из таблиц НФДЛ: (" + (System.currentTimeMillis() - time) + " ms)");

    // Проверки на соответствие справочникам
    checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Общие проверки
    checkDataCommon(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Проверки сведений о доходах
//    checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList)

    // Проверки Сведения о вычетах
//    checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList)

    println "Все проверки " + (System.currentTimeMillis() - time);
    logger.info("Все проверки: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Проверки на соответствие справочникам
 * @return
 */
def checkDataReference(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {

    long time = System.currentTimeMillis();
    // Страны
    def citizenshipCodeMap = getRefCountryCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

    // Виды документов, удостоверяющих личность
    def documentTypeMap = getRefDocumentTypeCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

    // Статус налогоплательщика
    def taxpayerStatusMap = getRefTaxpayerStatusCode()
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

    println "Проверки на соответствие справочникам / Выгрузка справочников: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочников: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    // ФЛ Map<person_id, RefBook>
    def personMap = getActualRefPersonsByDeclarationDataId()
    logger.info(SUCCESS_GET_TABLE, R_PERSON, personMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника Физические лица: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника Физические лица: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    // ИНП Map<person_id, List<RefBook>>
    def inpMap = getActualRefInpMapByDeclarationDataId()
    logger.info(SUCCESS_GET_TABLE, R_INP, inpMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника ИНП: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника ИНП: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    // ДУЛ Map<person_id, List<RefBook>>
    def dulMap = getActualRefDulByDeclarationDataId()
    logger.info(SUCCESS_GET_TABLE, R_DUL, dulMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника ДУЛ: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника ДУЛ: (" + (System.currentTimeMillis() - time) + " ms)");

    // Получим Мапу адресов
    // Адреса
    def addressIds = []
    def addressMap = [:]
    time = System.currentTimeMillis();
    personMap.each { recordId, person ->
        // Сохраним идентификаторы адресов в коллекцию
        if (person.get(RF_ADDRESS).value != null) {
            addressIds.add(person.get(RF_ADDRESS).value)
        }
    }
    if (addressIds.size() > 0) {
        addressMap = getRefAddress(addressIds)
        logger.info(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
    }
    println "Проверки на соответствие справочникам / Выгрузка справочника Адреса: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника Адреса: (" + (System.currentTimeMillis() - time) + " ms)");

    //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}

    long timeIsExistsAddress = 0
    time = System.currentTimeMillis();
    for (NdflPerson ndflPerson : ndflPersonList) {

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");

        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        // Спр1 ФИАС
        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-448
        long tIsExistsAddress = System.currentTimeMillis();
        if (!isExistsAddress(ndflPerson.regionCode, ndflPerson.area, ndflPerson.city, ndflPerson.locality, ndflPerson.street)) {
            logger.warn("""Ошибка в значении: Раздел "Реквизиты". Строка "${ndflPerson.rowNum}". Графа "Адрес регистрации в Российской Федерации ". $fioAndInp. Текст ошибки: "Адрес регистрации в Российской Федерации " не соответствует справочнику "$R_FIAS".""")
        }
        timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_CITIZENSHIP);
        }

        // Спр3 Документ удостоверяющий личность (Обязательное поле)
        if (!documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {

            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_ID_DOC_TYPE);
        }

        // Спр4 Статусы налогоплательщиков (Обязательное поле)
        if (!taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_STATUS);
        }

        // Спр10 Наличие связи с "Физическое лицо"
        if (ndflPerson.personId == null || ndflPerson.personId == 0) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_PERSON, R_PERSON, T_PERSON, fio, ndflPerson.inp);
        } else {
            def personRecord = personMap.get(ndflPerson.recordId)

            if (!personRecord) {
                logger.error("Не найдена актуальная запись в справочнике \"Физические лица\" для ФЛ " + fioAndInp)
            } else {
                // Спр11 Фамилия (Обязательное поле)
                if (!ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_LAST_NAME, fioAndInp, C_LAST_NAME, R_PERSON);
                }

                // Спр11 Имя (Обязательное поле)
                if (!ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_FIRST_NAME, fioAndInp, C_FIRST_NAME, R_PERSON);
                }

                // Спр11 Отчество (Необязательное поле)
                if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_MIDDLE_NAME, fioAndInp, C_MIDDLE_NAME, R_PERSON);
                }

                if (FORM_DATA_KIND_PRIMARY.equals(FormDataKind.PRIMARY)) {
                    // Спр12 ИНП первичная (Обязательное поле)
                    def inpList = inpMap.get(personRecord.get("id")?.value)
                    if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                        logger.warn("""Ошибка в значении: Раздел "Реквизиты". Строка "${ndflPerson.rowNum}". Графа "Уникальный код клиента". $fioAndInp. Текст ошибки: "Уникальный код клиента" не соответствует справочнику "Идентификаторы налогоплательщиков".""")
                    }
                } else {
                    //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                    //if (formType == CONSOLIDATE){}
                    String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                    if (!ndflPerson.inp.equals(recordId)) {
                        //TODO turn_to_error
                        logger.warn("""Ошибка в значении: Раздел "Реквизиты". Строка "${ndflPerson.rowNum}". Графа "Уникальный код клиента". $fioAndInp. Текст ошибки: "Уникальный код клиента" не соответствует справочнику "Идентификаторы налогоплательщиков".""")
                    }
                }

                // Спр13 Дата рождения (Обязательное поле)
                if (!ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, C_BIRTH_DATE, R_PERSON);
                }

                // Спр14 Гражданство (Обязательное поле)
                def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                if (!ndflPerson.citizenship.equals(citizenship)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_PERSON);
                }

                // Спр15 ИНН (Необязательное поле)
                if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, C_INN_NP, R_PERSON);
                }

                // Спр16 ИНН в стране Гражданства (Необязательное поле)
                if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_INN_FOREIGN, fioAndInp, C_INN_FOREIGN, R_PERSON);
                }


                if (FORM_DATA_KIND_PRIMARY.equals(FormDataKind.PRIMARY)) {
                    // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                    def allDocList = dulMap.get(personRecord.get("id")?.value)
                    // Вид документа
                    def personDocTypeList = []
                    // Серия и номер документа
                    def personDocNumberList = []
                    allDocList.each { dul ->
                        personDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                        personDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
                    }
                    if (!personDocTypeList.contains(ndflPerson.idDocType)) {
                        logger.warn("""Ошибка в значении: Раздел "Реквизиты". Строка "${ndflPerson.rowNum}". Графа "Код вида документа". $fioAndInp. Текст ошибки: "Код вида документа" не соответствует справочнику "Физические лица".""")
                    }
                    if (!personDocNumberList.contains(ndflPerson.idDocNumber)) {
                        logger.warn("""Ошибка в значении: Раздел "Реквизиты". Строка "${ndflPerson.rowNum}". Графа "Серия и номер документа". $fioAndInp. Текст ошибки: "Серия и номер документа" не соответствует справочнику "Физические лица".""")
                    }
                } else {
                    def allDocList = dulMap.get(ndflPerson.personId)
                    //Ищем в справочнике запись по параметрам код документа и номер
                    Map<String, RefBookValue> dulRecordValues = allDocList.find { recordValues ->
                        String docTypeCode = documentTypeMap.get(recordValues.get(RF_DOC_ID).getReferenceValue())
                        String docNumber = recordValues.get(RF_DOC_NUMBER).getStringValue()
                        return ndflPerson.idDocType.equals(docTypeCode) && ndflPerson.idDocNumber.equals(docNumber)
                    }

                    if (dulRecordValues == null) {
                        //"Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
                        logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_ID_DOC, fioAndInp, C_ID_DOC, R_DUL);
                    } else {
                        int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                        if (incRep != 1) {
                            //MESSAGE_ERROR_VALUE = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: %s."
                            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_ID_DOC, fioAndInp, C_ID_DOC, MESSAGE_ERROR_DUL);

                        }
                    }
                }

                // Спр18 Статус налогоплательщика (Обязательное поле)
                def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                if (!ndflPerson.status.equals(taxpayerStatus)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_PERSON);
                }

                // Спр19 Адрес (Необязательное поле)
                // Сравнение должно быть проведено даже с учетом пропусков
                def address = addressMap.get(personRecord.get(RF_ADDRESS).value)
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
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_REGION_CODE, fioAndInp, C_REGION_CODE, R_PERSON);
                }

                // Район
                if (!ndflPerson.area.equals(area)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_AREA, fioAndInp, C_AREA, R_PERSON);
                }

                // Город
                if (!ndflPerson.city.equals(city)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITY, fioAndInp, C_CITY, R_PERSON);
                }

                // Населенный пункт
                if (!ndflPerson.locality.equals(locality)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_LOCALITY, fioAndInp, C_LOCALITY, R_PERSON);
                }

                // Улица
                if (!ndflPerson.street.equals(street)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STREET, fioAndInp, C_STREET, R_PERSON);
                }

                // Дом
                if (!ndflPerson.house.equals(house)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_HOUSE, fioAndInp, C_HOUSE, R_PERSON);
                }

                // Корпус
                if (!ndflPerson.building.equals(building)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_BUILDING, fioAndInp, C_BUILDING, R_PERSON);
                }

                // Квартира
                if (!ndflPerson.flat.equals(flat)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_FLAT, fioAndInp, C_FLAT, R_PERSON);
                }
            }
        }
    }
    println "Проверки на соответствие справочникам / NdflPerson: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPerson: (" + (System.currentTimeMillis() - time) + " ms)");

    println "Проверки на соответствие справочникам / Проверка существования адреса: " + timeIsExistsAddress;
    logger.info("Проверки на соответствие справочникам / Проверка существования адреса: (" + timeIsExistsAddress + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find { key, value -> value == ndflPersonIncome.incomeCode }) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, C_INCOME_CODE, R_INCOME_CODE);
        }

        // Спр6 Вида дохода (Необязательное поле)
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (ndflPersonIncome.incomeType != null) {
            def idIncomeCode = incomeKindMap.get(ndflPersonIncome.incomeType)
            if (!idIncomeCode) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            } else if (!incomeCodeMap.get(idIncomeCode.value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            }
        }

        // Спр7 Ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate.toString())) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_RATE, fioAndInp, C_RATE, R_RATE);
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonIncome: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonIncome: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_TYPE_CODE, fioAndInp, C_TYPE_CODE, R_TYPE_CODE);
        }

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonDeduction: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonDeduction: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonPrepayment: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonPrepayment: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Общие проверки
 */
def checkDataCommon(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {

    // Тербанки
    //def mapTerBank = getTerBank()

    // Параметры подразделения
    // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-263
//    def departmentParam = getDepartmentParam()
//    def mapOktmoAndKpp = getOktmoAndKpp(departmentParam.record_id.value)

    long time = System.currentTimeMillis();
    for (NdflPerson ndflPerson : ndflPersonList) {

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        println "ndflPerson.rowNum=" + ndflPerson.rowNum

        // Общ1 Корректность ИНН (Необязательное поле)
        if (ndflPerson.innNp != null && !ScriptUtils.checkControlSumInn(ndflPerson.innNp)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, MESSAGE_ERROR_INN);
        }

        // Общ2 Корректность КПП, Пока необходимость данной проверки под вопросом
        // Общ3 Корректность ОКТМО, Пока необходимость данной проверки под вопросом

        // Общ4 Дата рождения (Обязательное поле)
        //if (ndflPerson.birthDay > getReportPeriodEndDate()) {
        //    logger.error(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, MESSAGE_ERROR_BIRTHDAY);
        //}

        // Общ11 СНИЛС (Необязательное поле)
        if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_SNILS, fioAndInp, MESSAGE_ERROR_SNILS);
        }
    }
    println "Общие проверки / NdflPerson: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / NdflPerson: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Общ5 Принадлежность дат операций к отчетному периоду
        // Дата начисления дохода (Необязательное поле)
        if (ndflPersonIncome.incomeAccruedDate != null && (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }

        // Дата выплаты дохода (Необязательное поле)
        if (ndflPersonIncome.incomePayoutDate != null && (ndflPersonIncome.incomePayoutDate < getReportPeriodStartDate() || ndflPersonIncome.incomePayoutDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата налога (Необязательное поле)
        if (ndflPersonIncome.taxDate != null && (ndflPersonIncome.taxDate < getReportPeriodStartDate() || ndflPersonIncome.taxDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Срок (дата) перечисления налога (Необязательное поле)
        if (ndflPersonIncome.taxTransferDate != null && (ndflPersonIncome.taxTransferDate < getReportPeriodStartDate() || ndflPersonIncome.taxTransferDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }

        // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
        if (ndflPersonIncome.paymentDate == null && ndflPersonIncome.paymentNumber == null && ndflPersonIncome.taxSumm == null) {
            // если не заполнены Раздел 2. Графы 22-24
            def emptyField = "не заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // если не заполнена Раздел 2. Графа 21
            if (ndflPersonIncome.taxTransferDate == null) {
                emptyField += ", \"" + C_TAX_TRANSFER_DATE + "\""

                // Раздел 2. Графа 4 "Код вида дохода" должна быть заполнена
                if (ndflPersonIncome.incomeCode == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_CODE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
                // Раздел 2. Графа 5 "Признак вида дохода" должна быть заполнена
                if (ndflPersonIncome.incomeType == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_TYPE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_TYPE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }

            // если НЕ заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate == null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                emptyField += ", \"" + C_INCOME_PAYOUT_DATE + "\", \"" + C_INCOME_PAYOUT_SUMM + "\""

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть НЕ заполнена
                if (ndflPersonIncome.taxTransferDate != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }

            // 	Раздел 2. Графа 13 "Налоговая база" должна быть заполнена
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxBase)) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_BASE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 14 "Ставка налога" должна быть заполнена
            if (ndflPersonIncome.taxRate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_RATE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 15 "Дата налога" должна быть заполнена
            if (ndflPersonIncome.taxDate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_DATE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнены Раздел 2. Графы 22-24
            def notEmptyField = "заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // Раздел 2. Графа 12 "Общая сумма вычетов" должна быть НЕ заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TOTAL_DEDUCTIONS_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }

            // если заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 6 "Дата начисления дохода"
        if (ndflPersonIncome.incomeAccruedDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_DATE])

            // Раздел 2. Графа 10 "Сумма начисленного дохода" должна быть заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_CALCULATED_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.notHoldingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_NOT_HOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.overholdingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_OVERHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            } else {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнена Раздел 2. Графа 10 "Сумма начисленного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 6 "Дата начисления дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                def emptyField = "не заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть НЕ заполнена
                if (!ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_CALCULATED_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.notHoldingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_NOT_HOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.overholdingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_OVERHOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 7 "Дата выплаты дохода"
        if (ndflPersonIncome.incomePayoutDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_DATE])

            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_PAYOUT_DATE, C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 17 "Сумма налога удержанная" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_WITHHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_WITHHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 20 "Сумма возвращенного налога" должна быть заполнена
                if (ndflPersonIncome.refoundTax == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_REFOUND_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_REFOUND_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            } else {
                // Раздел 2. Графа 11 "Сумма выплаченного дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если не заполнена Раздел 2. Графа 7 "Дата выплаты дохода"

            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 7 "Дата выплаты дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                // если НЕ заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
                if (ndflPersonIncome.paymentDate == null &&
                        ndflPersonIncome.paymentNumber == null &&
                        ndflPersonIncome.taxSumm == null) {

                    // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть НЕ заполнена
                    if (ndflPersonIncome.taxTransferDate != null) {
                        def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                        //TODO turn_to_error
                        logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                    }
                }
            }
        }

        // Раздел 2. Графа 22, 23, 24 Должны быть либо заполнены все 3 Графы, либо ни одна их них
        if (ndflPersonIncome.paymentDate != null || ndflPersonIncome.paymentNumber != null || !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
            def allField = getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])
            // Раздел 2. Графа 22 "Дата платежного поручения"
            if (ndflPersonIncome.paymentDate == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_DATE, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 23 "Номер платежного поручения перечисления налога в бюджет"
            if (ndflPersonIncome.paymentNumber == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_NUMBER, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_NUMBER, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 24 "Сумма налога перечисленная"
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_TAX_SUMM, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
        }

        // Общ10 Соответствие КПП и ОКТМО Тербанку
        // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-263
//        def kppList = mapOktmoAndKpp.get(ndflPersonIncome.oktmo)
//        def msgErr = sprintf(MESSAGE_ERROR_NOT_FOUND_PARAM, [mapTerBank.get(declarationData.departmentId).value])
//        if (kppList == null) {
//            logger.error(MESSAGE_ERROR_VALUE,
//                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OKTMO, fioAndInp, msgErr);
//        } else {
//            if (!kppList.contains(ndflPersonIncome.kpp)) {
//                logger.error(MESSAGE_ERROR_VALUE,
//                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_KPP, fioAndInp, msgErr);
//            }
//        }
    }

    // Общ12
    if (FORM_DATA_KIND_CONSOLIDATED.equals(FormDataKind.CONSOLIDATED)) {
        // Map<DEPARTMENT.CODE, DEPARTMENT.NAME>
        def mapDepartmentNotExistRnu = [
                4  : 'Байкальский банк',
                8  : 'Волго-Вятский банк',
                20 : 'Дальневосточный банк',
                27 : 'Западно-Сибирский банк',
                32 : 'Западно-Уральский банк',
                37 : 'Московский банк',
                44 : 'Поволжский банк',
                52 : 'Северный банк',
                64 : 'Северо-Западный банк',
                82 : 'Сибирский банк',
                88 : 'Среднерусский банк',
                97 : 'Уральский банк',
                113: 'Центральный аппарат ПАО Сбербанк',
                102: 'Центрально-Чернозёмный банк',
                109: 'Юго-Западный банк'
        ]
        def listDepartmentNotAcceptedRnu = []
        List<DeclarationData> declarationDataList = declarationService.find(CONSOLIDATED_RNU_NDFL_TEMPLATE_ID, declarationData.departmentReportPeriodId)
        for (DeclarationData dd : declarationDataList) {
            // Подразделение
            Long departmentCode = departmentService.get(dd.departmentId)?.code
            mapDepartmentNotExistRnu.remove(departmentCode)

            // Если налоговая форма не принята
            if (!dd.state.equals(State.ACCEPTED)) {
                listDepartmentNotAcceptedRnu.add(mapDepartmentNotExistRnu.get(departmentCode))
            }
        }

        // Период
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        def period = getRefBookValue(RefBook.Id.PERIOD_CODE.id, reportPeriod?.dictTaxPeriodId)
        def periodCode = period?.CODE?.stringValue
        def periodName = period?.NAME?.stringValue
        def calendarStartDate = reportPeriod?.calendarStartDate

        if (!mapDepartmentNotExistRnu.isEmpty()) {
            def listDepartmentNotExistRnu = []
            mapDepartmentNotExistRnu.each {
                listDepartmentNotExistRnu.add(it.value)
            }
            logger.warn("""За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")} года
                        не созданы экземпляры консолидированных налоговых форм для следующих ТБ: "${
                listDepartmentNotExistRnu.join("\", \"")
            }".
                        Данные этих форм не включены в отчетность!""")
        }
        if (!listDepartmentNotAcceptedRnu.isEmpty()) {
            logger.warn("""За период $periodCode ($periodName) ${ScriptUtils.formatDate(calendarStartDate, "yyyy")} года
                        имеются не принятые экземпляры консолидированных налоговых форм для следующих ТБ: "${
                listDepartmentNotAcceptedRnu.join("\", \"")
            }".
                        , для которых в системе существуют КНФ в текущем периоде, состояние которых <> "Принята">. Данные этих форм не включены в отчетность!""")
        }
    }

    println "Общие проверки / NdflPersonIncome: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / NdflPersonIncome: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Общ6 Принадлежность дат налоговых вычетов к отчетному периоду
        // Дата выдачи уведомления (Обязательное поле)
        if (ndflPersonDeduction.notifDate < getReportPeriodStartDate() || ndflPersonDeduction.notifDate > getReportPeriodEndDate()) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата начисления дохода (Обязательное поле)
        if (ndflPersonDeduction.incomeAccrued < getReportPeriodStartDate() || ndflPersonDeduction.incomeAccrued > getReportPeriodEndDate()) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_INCOME_ACCRUED, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в предыдущем периоде (Необязательное поле)
        if (ndflPersonDeduction.periodPrevDate != null &&
                (ndflPersonDeduction.periodPrevDate < getReportPeriodStartDate() || ndflPersonDeduction.periodPrevDate > getReportPeriodEndDate())) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_PREV_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в текущем периоде (Обязательное поле)
        if (ndflPersonDeduction.periodCurrDate < getReportPeriodStartDate() || ndflPersonDeduction.periodCurrDate > getReportPeriodEndDate()) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_CURR_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
    }
    println "Общие проверки / NdflPersonDeduction: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / NdflPersonDeduction: (" + (System.currentTimeMillis() - time) + " ms)");

    // Общ8 Отсутствие пропусков и повторений в нумерации строк
    time = System.currentTimeMillis();

    List<Integer> rowNumPersonList = ndflPersonService.findDublRowNum("NDFL_PERSON", declarationData.id)
    logger.info("rowNumPersonList.size() = " + rowNumPersonList.size())
    def msgErrDubl = getErrorMsgDubl(rowNumPersonList, T_PERSON)
    List<Integer> rowNumPersonIncomeList = ndflPersonService.findDublRowNum("NDFL_PERSON_INCOME", declarationData.id)
    msgErrDubl += getErrorMsgDubl(rowNumPersonIncomeList, T_PERSON_INCOME)
    List<Integer> rowNumPersonDeductionList = ndflPersonService.findDublRowNum("NDFL_PERSON_DEDUCTION", declarationData.id)
    msgErrDubl += getErrorMsgDubl(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    List<Integer> rowNumPersonPrepaymentList = ndflPersonService.findDublRowNum("NDFL_PERSON_PREPAYMENT", declarationData.id)
    msgErrDubl += getErrorMsgDubl(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrDubl = msgErrDubl == "" ? "" : MESSAGE_ERROR_DUBL + msgErrDubl

    rowNumPersonList = ndflPersonService.findMissingRowNum("NDFL_PERSON", declarationData.id)
    def msgErrAbsent = getErrorMsgAbsent(rowNumPersonList, T_PERSON)
    rowNumPersonIncomeList = ndflPersonService.findMissingRowNum("NDFL_PERSON_INCOME", declarationData.id)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonIncomeList, T_PERSON_INCOME)
    rowNumPersonDeductionList = ndflPersonService.findMissingRowNum("NDFL_PERSON_DEDUCTION", declarationData.id)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    rowNumPersonPrepaymentList = ndflPersonService.findMissingRowNum("NDFL_PERSON_PREPAYMENT", declarationData.id)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrAbsent = msgErrAbsent == "" ? "" : MESSAGE_ERROR_ABSENT + msgErrAbsent
    if (msgErrDubl != "" || msgErrAbsent != "") {
        //В ТФ имеются пропуски или повторы в нумерации строк.
        logger.warn(MESSAGE_ERROR_DUBL_OR_ABSENT + msgErrDubl + msgErrAbsent);
    }

    println "Общие проверки / Проверки на отсутсвие повторений: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / Проверки на отсутсвие повторений: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Проверки сведений о доходах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 */
def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList) {

    def personsCache = [:]
    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
        personsCache.put(ndflPerson.id, ndflPerson)
    }
    def incomeAccruedDateConditionDataList = []

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["1010", "3020", "1110", "1400", "2001", "2010", "2012",
                                                                              "2300", "2710", "2760", "2762", "2770", "2900", "4800"], ["00"], new Column6EqualsColumn7())

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                              "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                                              "1548", "1549", "1551", "1552", "1554"], ["01", "02"], new Column6EqualsColumn7())

    // Соответствует маске 31.12.20**
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                              "1541", "1542", "1543"], ["04"], new MatchMask("31.12.20\\d{2}"))

    // Последний календарный день месяца
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2000"], ["05"], new LastMonthCalendarDay())

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2000", "2002"], ["07"], new Column6EqualsColumn7())

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear())

    // Последний календарный день месяца
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2000", "2002"], ["11"], new LastMonthCalendarDay())

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear())

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2520", "2720", "2740", "2750", "2790"], ["00"], new Column6EqualsColumn7())

    // Последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2610"], ["00"], new LastMonthCalendarDayButNotFree())

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2640", "2641"], ["00"], new Column6EqualsColumn7())

    // "Графа 6" = "Графе 7"
    incomeAccruedDateConditionDataList << new IncomeAccruedDateConditionData(["2800"], ["00"], new Column6EqualsColumn7())

    // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
    def ndflPersonIncomeCache = [:]
    ndflPersonIncomeList.each { ndflPersonIncome ->
        List<NdflPersonIncome> ndflPersonIncomeByNdflPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId)
        ndflPersonIncomeByNdflPersonIdList.add(ndflPersonIncome)
        ndflPersonIncomeCache.put(ndflPersonIncome.ndflPersonId, ndflPersonIncomeByNdflPersonIdList)
    }

    ndflPersonIncomeCache.each { ndflPersonId, ndflPersonIncomeByNdflPersonIdList ->
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeByNdflPersonIdList) {

            def ndflPerson = personsCache.get(ndflPersonIncome.ndflPersonId)
            def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

            // СведДох1 Дата начисления дохода (Графа 6)
            incomeAccruedDateConditionDataList.each { incomeData ->
                if (incomeData.incomeCodes.contains(ndflPersonIncome.incomeCode) && incomeData.incomeTypes.contains(ndflPersonIncome.incomeCode)) {
                    if (incomeData.checker.check(ndflPersonIncome)) {
                        def messageErrorIncomeAccruedDate = MESSAGE_ERROR_NOT_MATCH_RULE + " \"Если Графа 4 = %s и Графа 5 = %s, то Графа 6 = %s\""
                        def textError = sprintf(messageErrorIncomeAccruedDate, ndflPersonIncome.incomeCode, ndflPersonIncome.incomeType, ndflPersonIncome.incomeAccruedDate.format("dd.MM.yyyy"))
                        logger.error(MESSAGE_ERROR_VALUE,
                                T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, textError);
                    }
                }
            }

            // СведДох2 Сумма вычета (Графа 12)
            BigDecimal sumNdflDeduction = getDeductionSumForIncome(ndflPersonIncome, ndflPersonDeductionList)
            if (ndflPersonIncome.totalDeductionsSumm ?: 0 != sumNdflDeduction) {
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + "\"Графа 12 Раздел 2 = сумма значений граф 16 Раздел 3\"");
            }
            if (ndflPersonIncome.incomeAccruedSumm ?: 0 < sumNdflDeduction) {
                logger.error(MESSAGE_ERROR_VALUE,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + "\"сумма значений граф 16 Раздела 3 ≤ графа 10 Раздел 2\"");
            }

            // СведДох4 Процентная ставка (Графа 14)
            if (ndflPersonIncome.taxRate == 13) {
                Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                Boolean conditionB = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status == "1"
                Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010", "2002", "2003"].contains(ndflPersonIncome.incomeCode) && Integer.parseInt(ndflPerson.status ?: 0) >= 3
                if (!(conditionA || conditionB || conditionC)) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = 13» не выполнено ни одно из условий:\\n
                                «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» ≠ 1010 и «Графа 12 Раздел 1» ≠ 2\\n
                                «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» = 1\\n
                                «Графа 7 Раздел 1» ≠ 643 и («Графа 4 Раздел 2» = 2000 или 2001 или 2010 или 2002 или 2003) и («Графа 12 Раздел 1» ≥ 3).""")
                }
            } else if (ndflPersonIncome.taxRate == 15) {
                if (!(ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = 15» не выполнено условие: «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» ≠ 1.""")
                }
            } else if (ndflPersonIncome.taxRate == 35) {
                if (!(["2740", "3020", "2610"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = 35» не выполнено условие: «Графа 4 Раздел 2» = (2740 или 3020 или 2610) и «Графа 12 Раздел 1» ≠ 2.""")
                }
            } else if (ndflPersonIncome.taxRate == 30) {
                def conditionA = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && ndflPersonIncome.incomeCode != "1010"
                def conditionB = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                if (!(conditionA || conditionB)) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = 30» не выполнено ни одно из условий:\\n
                                «Графа 12 Раздел 1» ≥ 2 и «Графа 4 Раздел 2» ≠ 1010\\n
                                («Графа 4 Раздел 2» ≠ 2000 или 2001 или 2010) и «Графа 12 Раздел 1» > 2.""")
                }
            } else if (ndflPersonIncome.taxRate == 9) {
                if (!(ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = 9» не выполнено условие: «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» = 1110 и «Графа 12 Раздел 1» = 1.""")
                }
            } else {
                if (!(ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Ставка" $fioAndInp.
                                Текст ошибки: для «Графа 14 Раздел 2 = ${
                        ndflPersonIncome.taxRate
                    }» не выполнено условие: «Графа 7 Раздел 1» ≠ 643 и «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» ≠ 1.""")
                }
            }

            // СведДох5 Дата налога (Графа 15)
            if (ndflPersonIncome.taxDate != null) {
                // СведДох5.1
                if (ndflPersonIncome.calculatedTax ?: 0 > 0 && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» ≠ «Графа 6 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomeAccruedDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата исчисления" $fioAndInp.
                                Не выполнено условие: если «Графа 16 Раздел 2» > "0" и «Графа 4 Раздел 2» ≠ 0, то «Графа 15 Раздел 2» = «Графа 6 Раздел 2».""")
                    }
                }
                // СведДох5.2
                if (ndflPersonIncome.withholdingTax ?: 0 > 0 && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» ≠ «Графа 7 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 17 Раздел 2» > "0" и «Графа 4 Раздел 2» ≠ 0, то «Графа 15 Раздел 2» = «Графа 7 Раздел 2».""")
                    }
                }
                // СведДох5.3
                if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                        ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null &&
                        !["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode)) {
                    // «Графа 15 Раздел 2» ≠ «Графа 7 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 18 Раздел 2» > "0" и «Графа 17 Раздел 2» < «Графа 16 Раздел 2» и «Графа 4 Раздел 2» ≠ 0 и («Графа 4 Раздел 2» ≠ 1530 или 1531 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1543*)"
                                , то «Графа 15 Раздел 2» = «Графа 7 Раздел 2».""")
                    }
                }
                // СведДох5.4
                if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                        ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode) &&
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate()) {
                    // «Графа 15 Раздел 2» ≠ «Графа 6 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomeAccruedDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 18 Раздел 2» > 0 и «Графа 17 Раздел 2» < «Графа 16 Раздел 2» и («Графа 4 Раздел 2» = 1530 или 1531 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1543*) и «Графа 7 Раздел 2» = "текущий отчётный период"
                                , то «Графа 15 Раздел 2» = «Графа 6 Раздел 2».""")
                    }
                }
                // СведДох5.5
                if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                        ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542"].contains(ndflPersonIncome.incomeCode) &&
                        (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
                    // «Графа 15 Раздел 2"» = "31.12.20**"
                    Calendar calendarPayout = Calendar.getInstance()
                    calendarPayout.setTime(ndflPersonIncome.taxDate)
                    int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                    int month = calendarPayout.get(Calendar.MONTH)
                    if (!(dayOfMonth == 31 && month == 12)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 18 Раздел 2» > "0" и «Графа 17 Раздел 2» < «Графа 16 Раздел 2» и («Графа 4 Раздел 2» = 1530 или 1531 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542) и «Графа 6 Раздел 2» ≠ "текущий отчётный период"
                                , то «Графа 15 Раздел 2"» = "31.12.20**".""")
                    }
                }
                // СведДох5.6
                if (ndflPersonIncome.overholdingTax ?: 0 > 0 &&
                        ndflPersonIncome.withholdingTax ?: 0 > ndflPersonIncome.calculatedTax ?: 0 &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» ≠ «Графа 7 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 19 Раздел 2» > "0" и «Графа 17 Раздел 2» > «Графа 16 Раздел 2» и «Графа 4 Раздел 2» ≠ 0
                                , то «Графа 15 Раздел 2» = «Графа 7 Раздел 2».""")
                    }
                }
                // СведДох5.7
                if (ndflPersonIncome.refoundTax ?: 0 > 0 &&
                        ndflPersonIncome.withholdingTax ?: 0 > ndflPersonIncome.calculatedTax ?: 0 &&
                        ndflPersonIncome.overholdingTax ?: 0 &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» ≠ «Графа 7 Раздел 2»
                    if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Дата удержания" $fioAndInp.
                                Не выполнено условие: если «Графа 20 Раздел 2» > "0" и «Графа 17 Раздел 2» > «Графа 16 Раздел 2» и «Графа 19 Раздел 2» > "0" и «Графа 4 Раздел 2» = ≠ 0
                                , то «Графа 15 Раздел 2» = «Графа 7 Раздел 2».""")
                    }
                }
            }

            // СведДох6 Сумма налога исчисленная (Графа 16)
            if (ndflPersonIncome.calculatedTax != null) {
                // СведДох6.1
                if (ndflPersonIncome.taxRate != 13) {
                    if (ndflPersonIncome.calculatedTax ?: 0 != ScriptUtils.round((ndflPersonIncome.taxBase ?: 0 * ndflPersonIncome.taxRate ?: 0), 0)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога исчисленная" $fioAndInp.
                                Не выполнено условие: если «Графа 14 Раздел 2» ≠ "13", то «Графа 16" = «Графа 13 Раздел 2» × «Графа 14 Раздел 2», с округлением до целого числа по правилам округления.""")
                    }
                }
                // СведДох6.2
                if (ndflPersonIncome.taxRate == 13 && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "6") {
                    /*
                    S1 - сумма значений по "Графе 13" (taxBase)
                    Для суммирования строк по "Графе 13" (taxBase) должны быть соблюдены ВСЕ следующие условия:
                    1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                    2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) <= "Графы 6" для текущей строки (МЕНЬШЕ ИЛИ РАВНО)
                    3. Значение "Графы 10" (incomeAccruedSumm) != 0
                    4. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                    5. Значение "Графы 14" (taxRate) = 13
                    6. Значение "Графы 4" (incomeCode) != "1010"
                     */
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId)
                    List<NdflPersonIncome> S1List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate
                        it.incomeAccruedSumm != null && it.incomeAccruedSumm != 0 &&
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate() &&
                        it.taxRate == 13 &&
                        it.incomeCode != "1010"
                    }
                    BigDecimal S1 = S1List.sum {S1List.taxBase ?: 0}
                    /*
                    S2 - сумма значений по "Графе 16" (calculatedTax)
                    Для суммирования строк по "Графе 16" (calculatedTax) должны быть соблюдены ВСЕ следующие условия:
                    1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                    2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) < "Графы 6" для текущей строки (МЕНЬШЕ)
                    2. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                    3. Значение "Графы 14" (taxRate) = 13
                    4. Значение "Графы 4" (incomeCode) != "1010"
                     */
                    List<NdflPersonIncome> S2List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate() &&
                        it.taxRate == 13 &&
                        it.incomeCode != "1010"
                    }
                    BigDecimal S2 = S2List.sum {S1List.calculatedTax ?: 0}
                    // Сумма по «Графа 16» текущей операции = S1 x 13% - S2
                    if (ndflPersonIncome.calculatedTax != S1 * 13 - S2) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога исчисленная" $fioAndInp.
                                Не выполнено условие: сумма по «Графа 16 Раздел 2» текущей операции = S1 x 13% - S2.""")
                    }
                }
                // СведДох6.3
                if (ndflPersonIncome.taxRate == 13 && ndflPerson.status == "6") {
                    // todo Написал Павлу Астахову вопрос: нет ли в постановке к этой проверке ошибки?
                }
            }

            // СведДох7 Сумма налога удержанная (Графа 17)
            if (ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                // СведДох7.1
                if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                    || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                         "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"] && ndflPersonIncome.incomeType == "02")
                    && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                ) {
                    // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога удержанная" $fioAndInp.
                                Не выполнено условие: если (((«Графа 4 Раздел 2» = 2520 или 2720 или 2740 или 2750 или 2790 или 4800) и «Графа 5 Раздел 2» = "13")
                                или ((«графа 4» = 1530 или 1531 или 1532 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1543* или 1544 или 1545 или 1546 или 1547
                                или 1548 или 1549 или 1551 или 1552 или 1554) и «Графа 5 Раздел 2» ≠ 02)) и «Графа 19 Раздел 2» = 0, то «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2».""")
                    }
                } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                    || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                         "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                    && ndflPersonIncome.overholdingTax > 0
                ) {
                    // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» ≤ ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId)
                    List<NdflPersonIncome> ndflPersonIncomePreviewList = ndflPersonIncomeCurrentList.findAll { it.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate }
                    BigDecimal previewCalculatedTax = 0
                    ndflPersonIncomePreviewList.each {
                    }
                    // todo Написал Павлу Астахову вопрос: как определять предыдущую запись?
                } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                    || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                         "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                ) {
                    if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога удержанная" $fioAndInp.
                                Не выполнено условие: если ((«Графа 4 Раздел 2» = 2520 или 2720 или 2740 или 2750 или 2790 или 4800) и «Графа 5 Раздел 2» = "14")
                                или ((«Графа 4 Раздел 2» = 1530 или 1531 или 1532 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1544
                                или 1545 или 1546 или 1547 или 1548 или 1549 или 1551 или 1552 или 1554 ) и «Графа 5 Раздел 2» = "02"),
                                то «Графа 17 Раздел 2» = 0.""")
                    }
                } else if (!(ndflPersonIncome.incomeCode != null)) {
                    if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm)) {
                        logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога удержанная" $fioAndInp.
                                Не выполнено условие: если «Графа 4 Раздел 2» ≠ 0,
                                то «Графа 17 Раздел 2» = «Графа 24 Раздел 2».""")
                    }
                }
            }

            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId)
            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdAndOperationIdList = ndflPersonIncomeCurrentByPersonIdList.findAll { it.operationId == ndflPersonIncome.operationId }
            // "Сумма Граф 16"
            Long calculatedTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.calculatedTax ?: 0 }
            // "Сумма Граф 17"
            Long withholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.withholdingTax ?: 0 }
            // "Сумма Граф 18"
            Long notHoldingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.notHoldingTax ?: 0 }
            // "Сумма Граф 19"
            Long overholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.overholdingTax ?: 0 }
            // "Сумма Граф 20"
            Long refoundTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.refoundTax ?: 0 }

            // СведДох8 Сумма налога, не удержанная налоговым агентом (Графа 18)
            if (calculatedTaxSum > withholdingTaxSum) {
                if (!(notHoldingTaxSum == calculatedTaxSum - withholdingTaxSum)) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога, не удержанная налоговым агентом" $fioAndInp.
                                Не выполнено условие: «Сумма Граф 16 Раздел 2» > «Сумма Граф 17 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2»,
                                то «Сумма Граф 18 Раздел 2» = «Сумма Граф 16 Раздел 2» - «Сумма Граф 17 Раздел 2».""")
                }
            }

            // СведДох9 Сумма налога, излишне удержанная налоговым агентом (Графа 19)
            if (calculatedTaxSum < withholdingTaxSum) {
                if (!(overholdingTaxSum == withholdingTaxSum - calculatedTaxSum)) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма налога, излишне удержанная налоговым агентом" $fioAndInp.
                                Не выполнено условие: «Сумма Граф 16 Раздел 2» < «Сумма Граф 17 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2»,
                                то «Сумма Граф 19 Раздел 2» = «Сумма Граф 17 Раздел 2» - «Сумма Граф 16 Раздел 2».""")
                }
            }

            // СведДох10 Сумма возвращенного налога (Графа 20)
            if (ndflPersonIncome.refoundTax > 0) {
                if (!(refoundTaxSum <= overholdingTaxSum)) {
                    logger.error("""Ошибка в значении: Раздел "Сведения о доходах".Строка="${ndflPersonIncome.rowNum}".Графа "Сумма возвращенного налога" $fioAndInp.
                                Не выполнено условие: если «Графа 20 Раздел 2» > 0,
                                то «Сумма Граф 20 Раздел 2» ≤ «Сумма Граф 19 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2».""")
                }
            }

            // СведДох11 Сумма налога перечисленная (Графа 24)
        }
    }
}

/**
 * Возвращает "Сумму применения вычета в текущем периоде"
 * @param ndflPersonIncome
 * @param ndflPersonDeductionList
 * @return
 */
BigDecimal getDeductionSumForIncome(NdflPersonIncome ndflPersonIncome, List<NdflPersonDeduction> ndflPersonDeductionList) {
    BigDecimal sumNdflDeduction = new BigDecimal(0)
    for (ndflPersonDeduction in ndflPersonDeductionList) {
        if (ndflPersonIncome.operationId == ndflPersonDeduction.operationId
                && ndflPersonIncome.incomeAccruedDate.format("dd.MM.yyyy") == ndflPersonDeduction.incomeAccrued.format("dd.MM.yyyy")
                && ndflPersonIncome.ndflPersonId == ndflPersonDeduction.ndflPersonId) {
            sumNdflDeduction += ndflPersonDeduction.periodCurrSumm ?: 0
        }
    }
    return sumNdflDeduction
}

class IncomeAccruedDateConditionData {

    List<String> incomeCodes
    List<String> incomeTypes
    IncomeAccruedDateConditionChecker checker

    IncomeAccruedDateConditionData(List<String> incomeCodes, List<String> incomeTypes, IncomeAccruedDateConditionChecker checker) {
        this.incomeCodes = incomeCodes
        this.incomeTypes = incomeTypes
        this.checker = checker
    }
}

interface IncomeAccruedDateConditionChecker {
    boolean check(NdflPersonIncome income)
}

/**
 * "Графа 6" = "Графе 7"
 */
class Column6EqualsColumn7 implements IncomeAccruedDateConditionChecker {
    @Override
    boolean check(NdflPersonIncome income) {
        String accrued = income.incomeAccruedDate.format("dd.MM.yyyy")
        String payout = income.incomePayoutDate.format("dd.MM.yyyy")
        return accrued == payout
    }
}

/**
 * Проверка соответствия маске
 */
class MatchMask implements IncomeAccruedDateConditionChecker {
    String maskRegex

    MatchMask(String maskRegex) {
        this.maskRegex = maskRegex
    }

    @Override
    boolean check(NdflPersonIncome income) {
        String accrued = income.incomeAccruedDate.format("dd.MM.yyyy")
        Pattern pattern = Pattern.compile(maskRegex)
        Matcher matcher = pattern.matcher(accrued)
        if (matcher.matches()) {
            return true
        }
        return false
    }
}

/**
 * Последний календарный день месяца
 */
class LastMonthCalendarDay implements IncomeAccruedDateConditionChecker {
    @Override
    boolean check(NdflPersonIncome income) {
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(income.incomeAccruedDate)
        int currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(calendar.DATE, 1)
        int comparedMonth = calendar.get(Calendar.MONTH)
        return currentMonth != comparedMonth
    }
}

/**
 * Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
 */
class Column7LastDayOfYear implements IncomeAccruedDateConditionChecker {
    @Override
    boolean check(NdflPersonIncome income) {
        // Дата выплаты дохода (Графа 7)
        Calendar calendarPayout = Calendar.getInstance()
        calendarPayout.setTime(income.incomePayoutDate)
        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
        int month = calendarPayout.get(Calendar.MONTH)
        if (dayOfMonth != 31 || month != 12) {
            return new Column6EqualsColumn7().check(income)
        } else {
            return new MatchMask("31.12.20\\d{2}").check(income)
        }
    }
}

/**
 * Последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
 */
class LastMonthCalendarDayButNotFree implements IncomeAccruedDateConditionChecker {
    @Override
    boolean check(NdflPersonIncome income) {
        boolean lastMonthDay = new LastMonthCalendarDay().check(income)
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(income.incomeAccruedDate)
        // Получим номер дня в неделе
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (lastMonthDay && dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
            return true
        } else if (!lastMonthDay && dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return true
        }
        return false
    }
}

/**
 * Проверки Сведения о вычетах
 */
def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList) {

    for (NdflPerson ndflPerson : ndflPersonList) {
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
    }

    def mapNdflPersonIncome = [:]
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
        String operationIdNdflPersonIdDate = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}_${ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate, "dd.MM.yyyy")}"
        mapNdflPersonIncome.put(operationIdNdflPersonIdDate, ndflPersonIncome)
    }

    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {
        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Выч14 Документ о праве на налоговый вычет.Код источника (Графа 7)
        if (ndflPersonDeduction.typeCode == "1" && ndflPersonDeduction.notifSource != "0000") {
            logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}".Графа "Код источника" $fioAndInp.
                            Текст ошибки: "Код вычета" = "${ndflPersonDeduction.typeCode}", "Код источника" = "${ndflPersonDeduction.notifSource}".""")
        }

        // Выч15 (Графы 9)
        // Выч16 (Графы 10)
        String operationIdNdflPersonIdDate = "${ndflPersonDeduction.operationId}_${ndflPersonDeduction.ndflPersonId}_${ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued, "dd.MM.yyyy")}"
        NdflPersonIncome ndflPersonIncome = mapNdflPersonIncome.get(operationIdNdflPersonIdDate)
        if (ndflPersonIncome == null) {
            logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}" $fioAndInp.
                            Текст ошибки: не была найдена записи в таблице "Начисленный доход", где "ИД операции" = "${ndflPersonDeduction.operationId}",
                            ссылка на таблицу "Реквизиты" = "${ndflPersonDeduction.ndflPersonId}" и "Дата начисления дохода" = "${ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued, "dd.MM.yyyy")}".""")
        } else {
            // Выч17 Начисленный доход.Код дохода (Графы 11)
            if (ndflPersonDeduction.incomeCode != ndflPersonIncome.incomeCode) {
                logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}".Графа "Код дохода" $fioAndInp.
                            Текст ошибки: "Сведения о вычетах"."Код дохода" = ${ndflPersonDeduction.incomeCode}" ≠ "Сведения о доходах"."Код дохода" = ${ndflPersonIncome.incomeCode}".""")
            }

            // Выч18 Начисленный доход.Сумма (Графы 12)
            if (ndflPersonDeduction.incomeSumm != ndflPersonIncome.incomeAccruedSumm) {
                logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}".Графа "Сумма начисленного дохода" $fioAndInp.
                            Текст ошибки: "Сведения о вычетах"."Сумма начисленного дохода" = ${ndflPersonDeduction.incomeSumm}" ≠ "Сведения о доходах"."Сумма начисленного дохода" = ${ndflPersonIncome.incomeAccruedSumm}".""")
            }
        }

        // Выч20 Применение вычета.Текущий период.Дата (Графы 15)
        if (ndflPersonDeduction.periodCurrDate != ndflPersonDeduction.incomeAccrued) {
            logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}".Графа "Дата применения вычета в текущем периоде" $fioAndInp.
                            Текст ошибки: "Дата применения вычета в текущем периоде" = "${ndflPersonDeduction.periodCurrDate}" ≠ "Дата начисления дохода" = "${ndflPersonDeduction.incomeAccrued}".""")
        }

        // Выч21 Применение вычета.Текущий период.Сумма (Графы 16) (Графы 8)
        if (ndflPersonDeduction.notifSumm ?: 0 > ndflPersonDeduction.periodCurrSumm ?: 0) {
            logger.error("""Ошибка в значении: Раздел "Сведения о вычетах".Строка="${ndflPersonDeduction.rowNum}".Графа "Сумма вычета согласно документу" $fioAndInp.
                            Текст ошибки: "Сумма вычета согласно документу" = "${ndflPersonDeduction.notifSumm}" > "Сумма применения вычета в текущем периоде" = "${ndflPersonDeduction.periodCurrSumm}".""")
        }
    }
}

//>------------------< CHECK DATA UTILS >----------------------<

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
    if (inputList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + inputList.join(", ") + "."
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
    def resultMsg = ""
    if (inputList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + inputList.join(", ") + "."
    }
    return resultMsg
}


//-----------------

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimaryRnuRowMapper extends NaturalPersonPrimaryRowMapper {

    private Long asnuId;

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        person.setPrimaryPersonId(getLong(rs, "id"));
        person.setId(getLong(rs, "person_id"));

        person.setSnils(rs.getString("snils"));
        person.setLastName(rs.getString("last_name"));
        person.setFirstName(rs.getString("first_name"));
        person.setMiddleName(rs.getString("middle_name"));
        person.setBirthDate(rs.getDate("birth_day"));

        person.setCitizenship(getCountryByCode(rs.getString("citizenship")));
        person.setInn(rs.getString("inn_np"));
        person.setInnForeign(rs.getString("inn_foreign"));

        String inp = rs.getString("inp");
        if (inp != null && asnuId != null) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setNaturalPerson(person);
            personIdentifier.setInp(inp);
            personIdentifier.setAsnuId(asnuId);
            person.getPersonIdentityList().add(personIdentifier);
        }

        String documentTypeCode = rs.getString("id_doc_type");
        String documentNumber = rs.getString("id_doc_number");

        if (documentNumber != null && documentTypeCode != null) {
            PersonDocument personDocument = new PersonDocument();
            personDocument.setNaturalPerson(person);
            personDocument.setDocumentNumber(documentNumber);
            personDocument.setDocType(getDocTypeByCode(documentTypeCode));
            person.getPersonDocumentList().add(personDocument);
        }


        person.setTaxPayerStatus(getTaxpayerStatusByCode(rs.getString("status")));
        person.setAddress(buildAddress(rs));

        //rs.getString("additional_data")
        return person;
    }


    private Address buildAddress(ResultSet rs) throws SQLException {

        if (getFiasAddres() != null) {
            Address address = new Address();

            address.setCountry(getCountryByCode(rs.getString("country_code")));
            address.setRegionCode(rs.getString("region_code"));
            address.setPostalCode(rs.getString("post_index"));
            address.setDistrict(rs.getString("area"));
            address.setCity(rs.getString("city"));
            address.setLocality(rs.getString("locality"));
            address.setStreet(rs.getString("street"));
            address.setHouse(rs.getString("house"));
            address.setBuild(rs.getString("building"));
            address.setAppartment(rs.getString("flat"));
            address.setAddressIno(rs.getString("address"));
            //Тип адреса. Значения: 0 - в РФ 1 - вне РФ
            int addressType = (address.getAddressIno() != null && !address.getAddressIno().isEmpty()) ? 1 : 0;
            address.setAddressType(addressType);

            return address;
        } else {
            return null;
        }
    }

    public AddressObject getFiasAddres() {
        //TODO Получить адрес в справонике фиас
        return new AddressObject();
    }

}

/**
 * Обработчик запроса данных из ПНФ
 *
 * @author Andrey Drunk
 */
public abstract class NaturalPersonPrimaryRowMapper implements RowMapper<NaturalPerson> {

    private Map<String, Country> countryCodeMap;

    private Map<String, TaxpayerStatus> taxpayerStatusCodeMap;

    private Map<String, DocType> docTypeCodeMap;

    protected Logger logger;

    /**
     * Возвращает значение целочисленного столбца. Если значения нет, то вернет null
     * @param resultSet набор данных
     * @param columnLabel название столбца
     * @return целое число, либо null
     * @throws SQLException
     */
    public static Integer getInteger(ResultSet resultSet, String columnLabel) throws SQLException {
        Integer ret = resultSet.getInt(columnLabel);
        return resultSet.wasNull()?null:ret;
    }

    public static Long getLong(ResultSet resultSet, String columnLabel) throws SQLException {
        Long ret = resultSet.getLong(columnLabel);
        return resultSet.wasNull()?null:ret;
    }

    public Map<String, Country> getCountryCodeMap() {
        return countryCodeMap;
    }

    public void setCountryCodeMap(Map<String, Country> countryCodeMap) {
        this.countryCodeMap = countryCodeMap;
    }

    public Map<String, TaxpayerStatus> getTaxpayerStatusCodeMap() {
        return taxpayerStatusCodeMap;
    }

    public void setTaxpayerStatusCodeMap(Map<String, TaxpayerStatus> taxpayerStatusCodeMap) {
        this.taxpayerStatusCodeMap = taxpayerStatusCodeMap;
    }

    public Map<String, DocType> getDocTypeCodeMap() {
        return docTypeCodeMap;
    }

    public void setDocTypeCodeMap(Map<String, DocType> docTypeCodeMap) {
        this.docTypeCodeMap = docTypeCodeMap;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Country getCountryByCode(String code) {
        if (code != null) {
            return countryCodeMap != null ? countryCodeMap.get(code) : new Country(null, code);
        } else {
            return null;
        }
    }

    public TaxpayerStatus getTaxpayerStatusByCode(String code) {
        if (code != null) {
            return taxpayerStatusCodeMap != null ? taxpayerStatusCodeMap.get(code) : new TaxpayerStatus(null, code);
        } else {
            return null;
        }
    }

    public DocType getDocTypeByCode(String code) {
        if (code != null) {
            return docTypeCodeMap != null ? docTypeCodeMap.get(code) : new DocType(null, code);
        } else {
            return null;
        }
    }

}
