package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataCommon(java.util.ArrayList, java.util.ArrayList)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataIncome(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataDeduction(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)}
 *
 */

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importData()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        // Подготовка для последующего формирования спецотчета
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        // Формирование спецотчета
        createSpecificReport()
        break
    case FormDataEvent.CHECK:
        // Проверки
        checkData()
        break
    case FormDataEvent.CALCULATE:
        calculate()
        // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
}

@Field final FormDataKind FORM_DATA_KIND = FormDataKind.PRIMARY;

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

/**
 * Тип первичной формы данные которой используются для идентификации 100 - РНУ, 200 - 1151111
 */
@Field
int FORM_TYPE = 100;

def calcTimeMillis(long time) {
    long currTime = System.currentTimeMillis();
    return (currTime - time) + " мс)";
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
        List<Map<String, RefBookValue>> refBookRecords = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId());
        refBookRecords.each { refBookValueMap ->
            TaxpayerStatus taxpayerStatus = new TaxpayerStatus();
            taxpayerStatus.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
            taxpayerStatus.setName(refBookValueMap?.get("NAME")?.getStringValue());
            taxpayerStatus.setCode(refBookValueMap?.get("CODE")?.getStringValue());
            taxpayerStatusRefBookCache.add(taxpayerStatus);
        }
    }
    return taxpayerStatusRefBookCache;
}

/**
 * Карта соответствия адреса формы адресу в справочнике ФИАС
 */
@Field Map<Long, Long> fiasAddressIdsCache = [:];

Map<Long, Long> getFiasAddressIdsMap() {
    if (fiasAddressIdsCache.isEmpty()) {
        fiasAddressIdsCache = fiasRefBookService.checkAddressByFias(declarationData.id);
    }
    return fiasAddressIdsCache;
}


NaturalPersonPrimaryRnuRowMapper createPrimaryRowMapper() {

    NaturalPersonPrimaryRnuRowMapper naturalPersonRowMapper = new NaturalPersonPrimaryRnuRowMapper();
    naturalPersonRowMapper.setAsnuId(declarationData.asnuId);

    //naturalPersonRowMapper.setLogger(logger); //TODO отключил из-за предупреждений по справочнику ФИАС

    List<Country> countryList = getCountryRefBookList();
    naturalPersonRowMapper.setCountryCodeMap(countryList.collectEntries {
        [it.code, it]
    });

    List<DocType> docTypeList = getDocTypeRefBookList();
    naturalPersonRowMapper.setDocTypeCodeMap(docTypeList.collectEntries {
        [it.code, it]
    });

    List<TaxpayerStatus> taxpayerStatusCodeList = getTaxpayerStatusRefBookList();
    naturalPersonRowMapper.setTaxpayerStatusCodeMap(taxpayerStatusCodeList.collectEntries {
        [it.code, it]
    });

    Map<Long, Long> fiasAddressIdsMap = getFiasAddressIdsMap();
    naturalPersonRowMapper.setFiasAddressIdsMap(fiasAddressIdsMap);

    return naturalPersonRowMapper;
}

NaturalPersonRefbookHandler createRefbookHandler() {

    NaturalPersonRefbookHandler refbookHandler = new NaturalPersonRefbookScriptHandler();

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

def updatePrimaryToRefBookPersonReferences(primaryDataRecords){

    ScriptUtils.checkInterrupted();

    if (FORM_TYPE == 100){
        ndflPersonService.updateRefBookPersonReferences(primaryDataRecords);
    } else {
        raschsvPersSvStrahLicService.updateRefBookPersonReferences(primaryDataRecords)
    }
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

    logger.info("В ПНФ номер " + declarationData.id + " получены записи о физ.лицах (" + primaryPersonDataList.size() + " записей, " + calcTimeMillis(time));

    //println "Find primary data in " + declarationData.id + " found: " + primaryPersonDataList.size() + " person " + calcTimeMillis(time)

    Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries {
        [it.getPrimaryPersonId(), it]
    }

    //Заполнени временной таблицы версий
    time = System.currentTimeMillis();
    refBookPersonService.fillRecordVersions(getRefBookPersonVersionTo());
    logger.info("Заполнение таблицы версий (" + calcTimeMillis(time));

    //println "Fill version table " + calcTimeMillis(time)

    //Шаг 1. список физлиц первичной формы для создания записей в справочниках
    time = System.currentTimeMillis();
    List<NaturalPerson> insertPersonList = refBookPersonService.findPersonForInsertFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createPrimaryRowMapper());
    logger.info("Предварительная выборка новых данных (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

    //println "Select for insert " + insertPersonList.size() + " person " + calcTimeMillis(time)

    time = System.currentTimeMillis();
    createNaturalPersonRefBookRecords(insertPersonList);
    logger.info("Создание (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

    //println "Insert: " + insertPersonList.size() + calcTimeMillis(time)

    //Шаг 2. идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
    time = System.currentTimeMillis();
    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + calcTimeMillis(time));

    //println "Select for update: " + similarityPersonMap.size() + calcTimeMillis(time)

    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
    logger.info("Обновление записей (" + calcTimeMillis(time));

    //println "Update ref: " + calcTimeMillis(time)

    time = System.currentTimeMillis();
    Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
    logger.info("Основная выборка по всем параметрам (" + checkSimilarityPersonMap.size() + " записей, " + calcTimeMillis(time));

    //println "Select for check: " + calcTimeMillis(time)

    time = System.currentTimeMillis();
    updateNaturalPersonRefBookRecords(primaryPersonMap, checkSimilarityPersonMap);
    logger.info("Обновление записей (" + calcTimeMillis(time));

    //println "Update reference: " + calcTimeMillis(time)
    //println "End: " + calcTimeMillis(timeFull)

    logger.info("Завершение расчета ПНФ (" + calcTimeMillis(timeFull));
}

//---------------- Identification ----------------
// Далее идет код скрипта такой же как и в 1151111 возможно следует вынести его в отдельный сервис

def createNaturalPersonRefBookRecords(List<NaturalPerson> insertRecords) {

    int createCnt = 0;
    if (insertRecords != null && !insertRecords.isEmpty()) {

        List<Address> addressList = new ArrayList<Address>();
        List<PersonDocument> documentList = new ArrayList<PersonDocument>();
        List<PersonIdentifier> identifierList = new ArrayList<PersonIdentifier>();

        for (NaturalPerson person : insertRecords) {

            ScriptUtils.checkInterrupted();

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

        //insert addresses batch
        insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressList, { address ->
            mapAddressAttr(address)
        });

        //insert persons batch
        insertBatchRecords(RefBook.Id.PERSON.getId(), insertRecords, { person ->
            mapPersonAttr(person)
        });

        //insert documents batch
        insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentList, { personDocument ->
            mapPersonDocumentAttr(personDocument)
        });

        //insert identifiers batch
        insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifierList, { personIdentifier ->
            mapPersonIdentifierAttr(personIdentifier)
        });

        //update reference to ref book

        updatePrimaryToRefBookPersonReferences(insertRecords);


        //Выводим информацию о созданных записях
        for (NaturalPerson person : insertRecords) {
            String noticeMsg = String.format("Создана новая запись в справочнике 'Физические лица': %d, %s %s %s", person.getId(), person.getLastName(), person.getFirstName(), (person.getMiddleName() ?: ""));
            logger.info(noticeMsg);
            createCnt++;
        }

    }

    logger.info("Создано записей: " + createCnt)

}

/**
 *
 * @param primaryPersonMap
 * @param similarityPersonMap
 * @return
 */
def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

    long time = System.currentTimeMillis();

    //println "updateNaturalPersonRefBookRecords similarityPersonMap.size=" + similarityPersonMap.size()

    //Проходим по списку и определяем наиболее подходящюю запись, если подходящей записи не найдено то содадим ее
    List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>();

    List<NaturalPerson> insertPersonList = new ArrayList<NaturalPerson>();
    //список записей для обновления атрибутов справочника физлиц
    List<Map<String, RefBookValue>> updatePersonList = new ArrayList<Map<String, RefBookValue>>();

    List<Address> insertAddressList = new ArrayList<Address>();
    List<Map<String, RefBookValue>> updateAddressList = new ArrayList<Map<String, RefBookValue>>();

    List<PersonDocument> insertDocumentList = new ArrayList<PersonDocument>();
    List<PersonDocument> updateDocumentList = new ArrayList<PersonDocument>();

    List<PersonIdentifier> insertIdentifierList = new ArrayList<PersonIdentifier>();
    List<Map<String, RefBookValue>> updateIdentifierList = new ArrayList<Map<String, RefBookValue>>();

    //primaryId - RefBookPerson
    HashMap<Long, NaturalPerson> conformityMap = new HashMap<Long, NaturalPerson>();

    int msgCnt = 0;
    int maxMsgCnt = 0;
    for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {

        long inTime = System.currentTimeMillis();

        ScriptUtils.checkInterrupted();

        Long primaryPersonId = entry.getKey();

        Map<Long, NaturalPerson> similarityPersonValues = entry.getValue();

        List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values());

        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);

        inTime = System.currentTimeMillis();
        NaturalPerson refBookPerson = refBookPersonService.identificatePerson(primaryPerson, similarityPersonList, SIMILARITY_THRESHOLD, logger);
        if (msgCnt <= maxMsgCnt){
            logger.info("identificate (" + calcTimeMillis(inTime));
        }

        conformityMap.put(primaryPersonId, refBookPerson);

        //Адрес нужно создать заранее и получить Id
        if (refBookPerson != null) {
            if (primaryPerson.getAddress() != null && refBookPerson.getAddress() == null) {
                insertAddressList.add(primaryPerson.getAddress());
            }
        }

        if (msgCnt <= maxMsgCnt){
            logger.info("in identificate (" + calcTimeMillis(inTime));
        }

        msgCnt++;
    }

    logger.info("identificate person, update address (" + calcTimeMillis(time));

    insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), insertAddressList, { address ->
        mapAddressAttr(address)
    });

    time = System.currentTimeMillis();

    int updCnt = 0;
    msgCnt = 0;
    maxMsgCnt = 0;
    for (Map.Entry<Long, NaturalPerson> entry : conformityMap.entrySet()) {

        long inTime = System.currentTimeMillis();

        ScriptUtils.checkInterrupted();

        Long primaryPersonId = entry.getKey();
        NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId);
        NaturalPerson refBookPerson = entry.getValue();

        AttributeCountChangeListener addressAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener personAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener documentAttrCnt = new AttributeCountChangeListener();
        AttributeCountChangeListener taxpayerIdentityAttrCnt = new AttributeCountChangeListener();

        if (refBookPerson != null) {

            primaryPerson.setId(refBookPerson.getId());

            //address
            if (primaryPerson.getAddress() != null) {
                if (refBookPerson.getAddress() != null) {
                    Map<String, RefBookValue> refBookAddressValues = mapAddressAttr(refBookPerson.getAddress());

                    fillSystemAliases(refBookAddressValues, refBookPerson.getAddress());

                    updateAddressAttr(refBookAddressValues, primaryPerson.getAddress(), addressAttrCnt);

                    if (addressAttrCnt.isUpdate()) {
                        updateAddressList.add(refBookAddressValues);
                    }
                }
            }


            //person
            Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(refBookPerson);
            fillSystemAliases(refBookPersonValues, refBookPerson);
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
                    refBookPerson.getPersonDocumentList().add(primaryPersonDocument);
                }
            }


            //check inc report
            checkIncReportFlag(refBookPerson, updateDocumentList, documentAttrCnt);

            //identifiers
            PersonIdentifier primaryPersonIdentifier = primaryPerson.getPersonIdentifier();
            if (primaryPersonIdentifier != null) {
                //Ищем совпадение в списке идентификаторов
                PersonIdentifier refBookPersonIdentifier = findIdentifierByAsnu(refBookPerson, primaryPersonIdentifier.getAsnuId());

                if (refBookPersonIdentifier != null) {

                    String primaryInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());
                    String refbookInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());

                    if (!BaseWeigthCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {

                        AttributeChangeEvent changeEvent = new AttributeChangeEvent("INP", primaryInp);
                        changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.STRING, refbookInp));
                        changeEvent.setType(AttributeChangeEventType.REFRESHED);
                        taxpayerIdentityAttrCnt.processAttr(changeEvent);

                        Map<String, RefBookValue> refBookPersonIdentifierValues = mapPersonIdentifierAttr(refBookPersonIdentifier);
                        fillSystemAliases(refBookPersonIdentifierValues, refBookPersonIdentifier);
                        updateIdentifierList.add(refBookPersonIdentifierValues);
                    }

                } else {
                    insertIdentifierList.add(primaryPersonIdentifier);
                }
            }

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

        if (msgCnt < maxMsgCnt){
            logger.info("in identificate update (" + calcTimeMillis(inTime));
        }

        msgCnt++;

    }

    logger.info("update person, documents, id (" + calcTimeMillis(time));
    time = System.currentTimeMillis();
    //println "crete and update reference"

    //crete and update reference
    createNaturalPersonRefBookRecords(insertPersonList);

    //update reference to ref book
    if (!updatePersonReferenceList.isEmpty()) {
        updatePrimaryToRefBookPersonReferences(updatePersonReferenceList);
    }

    logger.info("update reference (" + calcTimeMillis(time));
    time = System.currentTimeMillis();

    insertBatchRecords(RefBook.Id.ID_DOC.getId(), insertDocumentList, { personDocument ->
        mapPersonDocumentAttr(personDocument)
    });

    insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), insertIdentifierList, { personIdentifier ->
        mapPersonIdentifierAttr(personIdentifier)
    });

    List<Map<String, RefBookValue>> refBookDocumentList = new ArrayList<Map<String, RefBookValue>>();

    for (PersonDocument personDoc : updateDocumentList) {
        ScriptUtils.checkInterrupted();
        Map<String, RefBookValue> values = mapPersonDocumentAttr(personDoc);
        fillSystemAliases(values, personDoc);
        refBookDocumentList.add(values);
    }

    for (Map<String, RefBookValue> refBookValues : updateAddressList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.PERSON_ADDRESS.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updatePersonList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : refBookDocumentList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    for (Map<String, RefBookValue> refBookValues : updateIdentifierList) {
        ScriptUtils.checkInterrupted();
        Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();
        getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues);
    }

    logger.info("identification and update end (" + calcTimeMillis(time));

    logger.info("Обновлено записей: " + updCnt);

}

def fillSystemAliases(Map<String, RefBookValue> values, RefBookObject refBookObject) {
    values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getId()));
    values.put("RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getRecordId()));
    values.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, refBookObject.getVersion()));
    values.put("STATUS", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getStatus()));
}


PersonIdentifier findIdentifierByAsnu(NaturalPerson person, Long asnuId) {
    for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
        if (asnuId != null && asnuId.equals(personIdentifier.getAsnuId())) {
            return personIdentifier;
        }
    }
    return null;
}



@Field
def INCLUDE_TO_REPORT = 1;

@Field
def NOT_INCLUDE_TO_REPORT = 0;

/**
 * Метод устанавливает признак включения в отчетность на основе приоритета
 */
def checkIncReportFlag(NaturalPerson naturalPerson, List<PersonDocument> updateDocumentList, AttributeCountChangeListener attrChangeListener) {

    List personDocumentList = naturalPerson.getPersonDocumentList();

    if (!personDocumentList.isEmpty()) {

        //сортировка по приоритету
        personDocumentList.sort { a, b -> (a.getDocType()?.getPriority() <=> b.getDocType()?.getPriority()) ?: (a.id <=> b.id) }

        for (int i = 0; i < personDocumentList.size(); i++) {

            PersonDocument personDocument = personDocumentList.get(i);

            String docInf = new StringBuilder().append(personDocument.getId()).append(", ").append(personDocument.getDocumentNumber()).append(" ").toString();
            if (i == 0) {
                if (!personDocument.getIncRep().equals(INCLUDE_TO_REPORT)) {

                    AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", INCLUDE_TO_REPORT);
                    changeEvent.setType(AttributeChangeEventType.REFRESHED);
                    changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.NUMBER, personDocument.getIncRep()));

                    attrChangeListener.processAttr(docInf, changeEvent);

                    personDocument.setIncRep(INCLUDE_TO_REPORT);

                    if (personDocument.getId() != null) {
                        updateDocumentList.add(personDocument);
                    }
                }
            } else {

                if (!personDocument.getIncRep().equals(NOT_INCLUDE_TO_REPORT)) {

                    AttributeChangeEvent changeEvent = new AttributeChangeEvent("INC_REP", NOT_INCLUDE_TO_REPORT);
                    changeEvent.setType(AttributeChangeEventType.REFRESHED);

                    changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.NUMBER, personDocument.getIncRep()));
                    attrChangeListener.processAttr(docInf, changeEvent);

                    personDocument.setIncRep(NOT_INCLUDE_TO_REPORT);

                    if (personDocument.getId() != null) {
                        updateDocumentList.add(personDocument);
                    }
                }
            }
        }

    }
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
    putOrUpdate(values, "SEX", RefBookAttributeType.NUMBER, person.getSex(), attributeChangeListener);
    putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), attributeChangeListener);
    putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), attributeChangeListener);
    putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), attributeChangeListener);
    putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId(), attributeChangeListener);
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
    putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId());
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
    def incRepVal = personDocument.getIncRep() != null ? personDocument.getIncRep() : 1;
    putValue(values, "INC_REP", RefBookAttributeType.NUMBER, incRepVal); //default value is 1
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
    if (identityObjectList != null && !identityObjectList.isEmpty()) {

        logger.info("insert multiple records: refBookId=" + refBookId + ", size="+identityObjectList.size())

        List<RefBookRecord> recordList = new ArrayList<RefBookRecord>();
        for (IdentityObject identityObject : identityObjectList) {

            ScriptUtils.checkInterrupted();

            def values = refBookMapper(identityObject);
            recordList.add(createRefBookRecord(values));
        }

        //создание записей справочника
        List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, getRefBookPersonVersionFrom(), null, recordList);

        //установка id
        for (int i = 0; i < identityObjectList.size(); i++) {

            ScriptUtils.checkInterrupted();

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
    putOrUpdate(valuesMap, attrName, type, value, attributeChangedListener, { attrType, valueA, valueB ->
        isAttrEquals(attrType, valueA, valueB);
    });
}

def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, AttributeChangeListener attributeChangedListener, attrEquator) {

    AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, value);

    RefBookValue refBookValue = valuesMap.get(attrName);
    if (refBookValue != null) {
        //обновление записи, если новое значение задано и отличается от существующего
        changeEvent.setCurrentValue(refBookValue);

        if (value != null && !attrEquator(type, refBookValue.getValue(), value)) {
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

def isAttrEquals(RefBookAttributeType type, Object valueA, Object valueB) {
    if (type.equals(RefBookAttributeType.STRING)) {
        return BaseWeigthCalculator.isEqualsNullSafeStr(valueA, valueB);
    } else if (type.equals(RefBookAttributeType.DATE)) {
        return ScriptUtils.equalsNullSafe(valueA, valueB);
    } else {
        return ScriptUtils.equalsNullSafe(valueA, valueB);
    }
}

/**
 * Создание новой записи справочника адреса физлиц
 * @param person
 * @return
 */
def createRefBookRecord(Map<String, RefBookValue> values) {
    RefBookRecord record = new RefBookRecord();
    putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
    record.setValues(values);
    return record;
}

def buildRefreshNotice(AttributeCountChangeListener addressAttrCnt, AttributeCountChangeListener personAttrCnt, AttributeCountChangeListener documentAttrCnt, AttributeCountChangeListener taxpayerIdentityAttrCnt) {
    StringBuffer sb = new StringBuffer();
    appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb);
    appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb);
    appendAttrInfo(RefBook.Id.ID_DOC.getId(), documentAttrCnt, sb);
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

//----------------------------------------------------------------------------------------------------------------------
//--------------------------------------IDENTIFICATION END--------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------

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
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId(declarationDataId) {
    String whereClause = """
            JOIN ref_book_person p ON (frb.record_id = p.record_id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
        """
    def refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_PERSON_ID, whereClause, getReportPeriodEndDate() - 1)
    def refBookMapResult = new HashMap<Long, Map<String, RefBookValue>>();
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
    def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias;
    if ('rnu_ndfl_person_db' != reportAlias) {
        throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
    }
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

    // Ограничение числа выводимых записей
    int startIndex = 1
    int pageSize = 100

    PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

    //Если записи не найдены, то система формирует предупреждение:
    //Заголовок: "Предупреждение"
    //Текст: "Физическое лицо: <Данные ФЛ> не найдено в форме", где <Данные ФЛ> - значение полей формы, по которым выполнялся поиск физического лица, через разделитель "; "
    //Кнопки: "Закрыть"

    if (pagingResult.isEmpty()) {
        subreportParamsToString = { it.collect { (it.value != null ? (it.value + ";") : "") } join " " }
        logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме");
        //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме");
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
    switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
        case 'rnu_ndfl_person_db': createSpecificReportPersonDb();
            break;
        case 'rnu_ndfl_person_all_db': createSpecificReportDb();
            break;
        default:
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
    }
}
/**
 * Спец. отчет "РНУ НДФЛ по физическому лицу". Данные макет извлекает непосредственно из бд
 */
def createSpecificReportPersonDb() {
    def row = scriptSpecificReportHolder.getSelectedRecord()
    def ndflPerson = ndflPersonService.get(Long.parseLong(row.id))
    if (ndflPerson != null) {
        def params = [NDFL_PERSON_ID : ndflPerson.id];
        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
        declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
    } else {
        throw new ServiceException("Не найдены данные для формирования отчета!");
    }
}
/**
 * Формирует спец. отчеты, данные для которых макет извлекает непосредственно из бд
 */
def createSpecificReportDb() {
    def params = [declarationId : declarationData.id]
    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
    declarationService.exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.declarationSubreport.name + ".xlsx")
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
//------------------ Import Data ----------------------

void importData() {

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    logger.info("Начало загрузки данных первичной налоговой формы "+declarationData.id+". Дата начала отчетного периода: "+sdf.format(getReportPeriodStartDate())+", дата окончания: "+sdf.format(getReportPeriodEndDate()));

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

    def familia = ndflPerson.lastName != null ? ndflPerson.lastName + " ": ""
    def imya = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
    def otchestvo = ndflPerson.middleName != null ? ndflPerson.middleName : ""
    def fio = familia + imya + otchestvo
    def ndflPersonOperations = infoPart.'СведОпер'

    ndflPersonOperations.each {
        processNdflPersonOperation(ndflPerson, it, fio)
    }

    //Идентификатор декларации для которой загружаются данные
    if (ndflPerson.incomes != null && !ndflPerson.incomes.isEmpty()) {
        ndflPerson.declarationDataId = declarationData.getId()
        ndflPersonService.save(ndflPerson)
    } else {
        logger.warn("ФЛ ФИО = $fio ФЛ ИНП = ${ndflPerson.inp} Не загружен в систему поскольку не имеет операций в отчетном периоде")
    }
}

void processNdflPersonOperation(NdflPerson ndflPerson, NodeChild ndflPersonOperationsNode, String fio) {

    List<NdflPersonIncome> incomes = new ArrayList<NdflPersonIncome>();
    // При создание объекто операций доходов выполняется проверка на соответствие дат отчетному периоду
    incomes.addAll(ndflPersonOperationsNode.'СведДохНал'.collect {
        transformNdflPersonIncome(it, toString(ndflPersonOperationsNode.'@КПП'), toString(ndflPersonOperationsNode.'@ОКТМО'), ndflPerson.inp, fio)
    });
    // Если проверка на даты не прошла, то операция не добавляется.
    // https://jira.aplana.com/browse/SBRFNDFL-581 - временное решение если дата не прошла то загружаем, но выводим сообщение
    if (incomes.contains(null)) {
        //TODO return
    }

    incomes.each {
        if (it != null){
            ndflPerson.incomes.add(it);
        }
    }

    //ndflPerson.incomes.addAll(incomes);


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

NdflPersonIncome transformNdflPersonIncome(NodeChild node, String kpp, String oktmo, String inp, String fio) {
    def operationNode = node.parent();

    Date incomeAccruedDate = toDate(node.'@ДатаДохНач')
    Date incomePayoutDate = toDate(node.'@ДатаДохВыпл')
    Date taxDate = toDate(node.'@ДатаНалог')
    if (operationNotRelateToCurrentPeriod(incomeAccruedDate, incomePayoutDate, taxDate,
            kpp, oktmo, inp, fio)) {
        return null
    }

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

// Проверка на принадлежность операций периоду при загрузке ТФ
boolean operationNotRelateToCurrentPeriod(Date incomeAccruedDate, Date incomePayoutDate, Date taxDate,
                                          String kpp, String oktmo, String inp, String fio) {
    // Доход.Дата.Начисление
    boolean incomeAccruedDateOk = dateRelateToCurrentPeriod(incomeAccruedDate)
    // Доход.Дата.Выплата
    boolean incomePayoutDateOk = dateRelateToCurrentPeriod(incomePayoutDate)
    // НДФЛ.Расчет.Дата
    boolean taxDateOk = dateRelateToCurrentPeriod(taxDate)
    if (!incomeAccruedDateOk) {
        logger.warn("У параметра ТФ \"Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаДохНач\" недопустимое значение: \"${incomeAccruedDate ?: ""}\":дата операции не входит в отчетный период ТФ.\n" +
                "КПП = $kpp.\n" +
                "ОКТМО = $oktmo\n" +
                "ФЛ ИНП = $inp\n" +
                "ФИО = $fio")
    }
    if (!incomePayoutDateOk) {
        logger.warn("У параметра ТФ \"Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаДохВыпл\" недопустимое значение: \"${incomePayoutDate ?: ""}\":дата операции не входит в отчетный период ТФ.\n" +
                "КПП = $kpp.\n" +
                "ОКТМО = $oktmo\n" +
                "ФЛ ИНП = $inp\n" +
                "ФИО = $fio")
    }
    if (!taxDateOk) {
        logger.warn("У параметра ТФ \"Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаНалог\" недопустимое значение: \"${taxDateOk ?: ""}\":дата операции не входит в отчетный период ТФ.\n" +
                "КПП = $kpp.\n" +
                "ОКТМО = $oktmo\n" +
                "ФЛ ИНП = $inp\n" +
                "ФИО = $fio")
    }
    if (incomeAccruedDateOk && incomePayoutDateOk && taxDateOk) {
        return false
    }
    return true
}

boolean dateRelateToCurrentPeriod(Date date) {

    //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
    if (date==null || (date >= getReportPeriodStartDate() && date <= getReportPeriodEndDate())) {
        return true
    }
    return false
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
//если проверки различаются то используется параметр {@link #FORM_DATA_KIND}
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
@Field final TEMPLATE_PERSON_FL = "ФИО клиента: \"%s\", Уникальный код клиента: \"%s\""

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
@Field final long REF_BOOK_INCOME_CODE_ID = RefBook.Id.INCOME_CODE.id

// Виды дохода Мапа <Признак, Идентификатор_кода_вида_дохода>
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
 * Получить календарную дату начала отчетного периода
 * @return
 */
def getReportPeriodCalendarStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getCalendarStartDate(declarationData.reportPeriodId)?.time
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
    // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE.CODE>
    def mapResult = [:]
    def refBookMap = getRefBook(REF_BOOK_INCOME_CODE_ID)
    refBookMap.each { refBook ->
        mapResult.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
    }
    return mapResult;
}

/**
 * Получить "Виды доходов"
 * @return
 */
def getRefIncomeType() {
    // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
    def mapResult = [:]
    def refBookList = getRefBook(REF_BOOK_INCOME_KIND_ID)
    refBookList.each { refBook ->
        String mark = refBook?.MARK?.stringValue
        List<String> incomeTypeIdList = mapResult.get(mark)
        if (incomeTypeIdList == null) {
            incomeTypeIdList = []
        }
        incomeTypeIdList.add(refBook?.INCOME_TYPE_ID?.referenceValue)
        mapResult.put(mark, incomeTypeIdList)
    }
    return mapResult
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
@Field final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
@Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
@Field final String MESSAGE_ERROR_ABSENT = " Отсутсвуют строки:"


@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" (%d записей)."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" (%d записей)."

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
@Field final String R_INCOME_TYPE = "Виды доходов"
@Field final String R_RATE = "Ставки"
@Field final String R_TYPE_CODE = "Коды видов вычетов"
@Field final String R_NOTIF_SOURCE = "Коды налоговых органов"
@Field final String R_ADDRESS = "Адреса"
@Field final String R_INP = "Идентификаторы налогоплательщиков"
@Field final String R_DUL = "Документы, удостоверяющие личность"
@Field final String R_DETAIL = "Настройки подразделений"

// Реквизиты
@Field final String C_ADDRESS = "Адрес регистрации в Российской Федерации "
@Field final String C_CITIZENSHIP = "Гражданство (код страны)"
@Field final String C_ID_DOC = "Документ удостоверяющий личность.Номер"
@Field final String C_ID_DOC_TYPE = "Документ удостоверяющий личность.Код"
@Field final String C_STATUS = "Статус"
@Field final String C_RATE = "Ставка"
@Field final String C_TYPE_CODE = "Код вычета"
@Field final String C_NOTIF_SOURCE = "Документ о праве на налоговый вычет.Код источника"
@Field final String C_LAST_NAME = "Налогоплательщик.Фамилия"
@Field final String C_FIRST_NAME = "Налогоплательщик.Имя"
@Field final String C_MIDDLE_NAME = "Налогоплательщик.Отчество"
@Field final String C_BIRTH_DATE = "Налогоплательщик.Дата рождения"
@Field final String C_INN_NP = "ИНН.В Российской федерации"
@Field final String C_SNILS = "СНИЛС"
@Field final String C_INN_FOREIGN = "ИНН.В стране гражданства"
@Field final String C_REGION_CODE = "Адрес регистрации в Российской Федерации.Код субъекта"
@Field final String C_AREA = "Адрес регистрации в Российской Федерации.Район"
@Field final String C_CITY = "Адрес регистрации в Российской Федерации.Город"
@Field final String C_LOCALITY = "Адрес регистрации в Российской Федерации.Населенный пункт"
@Field final String C_STREET = "Адрес регистрации в Российской Федерации.Улица"
@Field final String C_HOUSE = "Адрес регистрации в Российской Федерации.Дом"
@Field final String C_BUILDING = "Адрес регистрации в Российской Федерации.Корпус"
@Field final String C_FLAT = "Адрес регистрации в Российской Федерации.Квартира"

// Сведения о доходах и НДФЛ
@Field final String C_INCOME_CODE = "Доход.Вид.Код"
@Field final String C_INCOME_TYPE = "Доход.Вид.Признак"
@Field final String C_INCOME_ACCRUED_DATE = "Доход.Дата.Начисление"
@Field final String C_INCOME_PAYOUT_DATE = "Доход.Дата.Выплата"
@Field final String C_INCOME_ACCRUED_SUMM = "Доход.Сумма.Начисление"
@Field final String C_INCOME_PAYOUT_SUMM = "Доход.Сумма.Выплата"
@Field final String C_TOTAL_DEDUCTIONS_SUMM = "Сумма вычета"
@Field final String C_TAX_BASE = "Налоговая база"
@Field final String C_TAX_RATE = "НДФЛ.Процентная ставка"
@Field final String C_TAX_DATE = "НДФЛ.Расчет.Дата"
@Field final String C_CALCULATED_TAX = "НДФЛ.Расчет.Сумма.Исчисленный"
@Field final String C_WITHHOLDING_TAX = "НДФЛ.Расчет.Сумма.Удержанный"
@Field final String C_NOT_HOLDING_TAX = "НДФЛ.Расчет.Сумма.Не удержанный"
@Field final String C_OVERHOLDING_TAX = "НДФЛ.Расчет.Сумма.Излишне удержанный"
@Field final String C_REFOUND_TAX = "НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику"
@Field final String C_TAX_TRANSFER_DATE = "НДФЛ.Перечисление в бюджет.Срок"
@Field final String C_PAYMENT_DATE = "НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
@Field final String C_PAYMENT_NUMBER = "НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
@Field final String C_TAX_SUMM = "НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
@Field final String C_OKTMO = "Доход.Источник выплаты.ОКТМО"
@Field final String C_KPP = "Доход.Источник выплаты.КПП"

// Сведения о вычетах
@Field final String C_NOTIF_DATE = "Документ о праве на налоговый вычет.Дата"
@Field final String C_INCOME_ACCRUED = "Начисленный доход.Дата"
@Field final String C_PERIOD_PREV_DATE = "Применение вычета.Предыдущий период.Дата"
@Field final String C_PERIOD_CURR_DATE = "Применение вычета.Текущий период.Дата"

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

    ScriptUtils.checkInterrupted();

    long time = System.currentTimeMillis();
    // Реквизиты
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    println "Получены записи таблицы '$T_PERSON' (${ndflPersonList.size()} записей)."
    logger.info(SUCCESS_GET_TABLE, T_PERSON, ndflPersonList.size())

    // Сведения о доходах и НДФЛ
    List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    println "Получены записи таблицы '$T_PERSON_INCOME' (${ndflPersonList.size()} записей)."
    logger.info(SUCCESS_GET_TABLE, T_PERSON_INCOME, ndflPersonIncomeList.size())

    // Сведения о вычетах
    List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    println "Получены записи таблицы '$T_PERSON_DEDUCTION' (${ndflPersonList.size()} записей)."
    logger.info(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION, ndflPersonDeductionList.size())

    // Сведения о доходах в виде авансовых платежей
    List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    println "Получены записи таблицы '$T_PERSON_PREPAYMENT' (${ndflPersonList.size()} записей)."
    logger.info(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT, ndflPersonPrepaymentList.size())

    println "Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)");

    ScriptUtils.checkInterrupted();

    // Проверки на соответствие справочникам
    checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    ScriptUtils.checkInterrupted();

    // Общие проверки
    checkDataCommon(ndflPersonList, ndflPersonIncomeList)

    ScriptUtils.checkInterrupted();

    // Проверки сведений о доходах
    checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    ScriptUtils.checkInterrupted();

    // Проверки Сведения о вычетах
    checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList)

    println "Все проверки (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Все проверки (" + (System.currentTimeMillis() - time) + " мс)");
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
    println "Получен справочник '$R_CITIZENSHIP' (${citizenshipCodeMap.size()} записей).";
    logger.info(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

    // Виды документов, удостоверяющих личность
    def documentTypeMap = getRefDocumentTypeCode()
    println "Получен справочник '$R_ID_DOC_TYPE' (${documentTypeMap.size()} записей).";
    logger.info(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

    // Статус налогоплательщика
    def taxpayerStatusMap = getRefTaxpayerStatusCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

    // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE.CODE>
    def incomeCodeMap = getRefIncomeCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

    // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
    def incomeTypeMap = getRefIncomeType()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

    // Ставки
    def rateList = getRefRate()
    logger.info(SUCCESS_GET_REF_BOOK, R_RATE, rateList.size())

    // Коды видов вычетов
    def deductionTypeList = getRefDeductionType()
    logger.info(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

    // Коды налоговых органов
    def taxInspectionList = getRefNotifSource()
    logger.info(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

    println "Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ФЛ Map<person_id, RefBook>
    def personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, R_PERSON, personMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника Физические лица (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника Физические лица (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ИНП Map<person_id, List<RefBook>>
    def inpMap = getActualRefInpMapByDeclarationDataId()
    logger.info(SUCCESS_GET_TABLE, R_INP, inpMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника ИНП (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника ИНП (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ДУЛ Map<person_id, List<RefBook>>
    def dulMap = getActualRefDulByDeclarationDataId()
    logger.info(SUCCESS_GET_TABLE, R_DUL, dulMap.size())
    println "Проверки на соответствие справочникам / Выгрузка справочника ДУЛ (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника ДУЛ (" + (System.currentTimeMillis() - time) + " мс)");

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
    println "Проверки на соответствие справочникам / Выгрузка справочника Адреса (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника Адреса (" + (System.currentTimeMillis() - time) + " мс)");

    //поиск всех адресов формы в справочнике ФИАС
    time = System.currentTimeMillis();
    Map<Long, Long> checkFiasAddressMap = getFiasAddressIdsMap();
    logger.info(SUCCESS_GET_TABLE, R_FIAS, checkFiasAddressMap.size());
    println "Проверки на соответствие справочникам / Выгрузка справочника $R_FIAS (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника $R_FIAS (" + (System.currentTimeMillis() - time) + " мс)");

    long timeIsExistsAddress = 0
    time = System.currentTimeMillis();
    //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
    for (NdflPerson ndflPerson : ndflPersonList) {

        ScriptUtils.checkInterrupted();

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");

        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        // Спр1 ФИАС
        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-448
        long tIsExistsAddress = System.currentTimeMillis();
        if (!isExistsAddress(ndflPerson.id)) {
            List<String> address = []
            if (!ScriptUtils.isEmpty(ndflPerson.regionCode)) {
                address.add("Код субъекта='${ndflPerson.regionCode}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.area)) {
                address.add("Район='${ndflPerson.area}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.city)) {
                address.add("Город='${ndflPerson.city}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.locality)) {
                address.add("Населенный пункт='${ndflPerson.locality}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.street)) {
                address.add(ndflPerson.street)
                address.add("Улица='${ndflPerson.street}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.house)) {
                address.add("Дом='${ndflPerson.house}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.building)) {
                address.add("Корпус='${ndflPerson.building}'")
            }
            if (!ScriptUtils.isEmpty(ndflPerson.flat)) {
                address.add("Квартира='${ndflPerson.flat}'")
            }
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Графы ${address.join(", ")}. $fioAndInp." +
                    " Текст ошибки: 'Адрес регистрации в Российской Федерации' не соответствует справочнику '$R_FIAS'.")
        }
        timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Гражданство (код страны) (Графа 7)='${ndflPerson.citizenship}'. $fioAndInp." +
                    " Текст ошибки: 'Гражданство (код страны) (Графа 7)' не соответствует справочнику '$R_CITIZENSHIP'.")
        }

        // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
        if (!documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}'. $fioAndInp." +
                    " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)' не соответствует справочнику '$R_ID_DOC_TYPE'.")
        }

        // Спр4 Статус (Обязательное поле)
        if (!taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Cтатус (Графа 12)='${ndflPerson.status}'. $fioAndInp." +
                    " Текст ошибки: 'Cтатус (Графа 12)' не соответствует справочнику '$R_STATUS'.")
        }

        // Спр10 Наличие связи с "Физическое лицо"
        if (ndflPerson.personId == null || ndflPerson.personId == 0) {
            //TODO turn_to_error
            logger.warn("Не удалось установить связь со справочником '$R_PERSON' для Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""} $fioAndInp.")
        } else {
            def personRecord = personMap.get(ndflPerson.recordId)

            if (!personRecord) {
                //TODO turn_to_error
                logger.warn("Не найдена актуальная запись в справочнике '$R_PERSON' для Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""} $fioAndInp.")
            } else {
                // Спр11 Фамилия (Обязательное поле)
                if (!ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Фамилия (Графа 3)='${ndflPerson.lastName}'. $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Фамилия (Графа 3)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр11 Имя (Обязательное поле)
                if (!ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Имя (Графа 4)='${ndflPerson.firstName}'. $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Имя (Графа 4)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр11 Отчество (Необязательное поле)
                if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Отчество (Графа 5)='${ndflPerson.middleName}'. $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Отчество (Графа 5)' не соответствует справочнику '$R_PERSON'.")
                }

                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // Спр12 ИНП первичная (Обязательное поле)
                    def inpList = inpMap.get(personRecord.get("id")?.value)
                    if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Уникальный код клиента (Графа 2)='${ndflPerson.inp}'. $fioAndInp." +
                                " Текст ошибки: 'Уникальный код клиента (Графа 2)' не соответствует справочнику '$R_INP'.")
                    }
                } else {
                    //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                    //if (formType == CONSOLIDATE){}
                    String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                    if (!ndflPerson.inp.equals(recordId)) {
                        //TODO turn_to_error
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Уникальный код клиента (Графа 2)='${ndflPerson.inp}'. $fioAndInp." +
                                " Текст ошибки: 'Уникальный код клиента (Графа 2)' не соответствует справочнику '$R_INP'.")
                    }
                }

                // Спр13 Дата рождения (Обязательное поле)
                if (!ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Дата рождения (Графа 6)='${ndflPerson.birthDay}'. $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Дата рождения (Графа 6)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр14 Гражданство (Обязательное поле)
                def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                if (!ndflPerson.citizenship.equals(citizenship)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Гражданство (код страны) (Графа 7)='${ndflPerson.citizenship}'. $fioAndInp." +
                            " Текст ошибки: 'Гражданство (код страны) (Графа 7)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр15 ИНН.В Российской федерации (Необязательное поле)
                if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В Российской федерации (Графа 8)='${ndflPerson.innNp}'. $fioAndInp." +
                            " Текст ошибки: 'ИНН.В Российской федерации (Графа 8)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр16 ИНН.В стране гражданства (Необязательное поле)
                if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В стране гражданства (Графа 9)='${ndflPerson.innForeign}'. $fioAndInp." +
                            " Текст ошибки: 'ИНН.В стране гражданства (Графа 9)' не соответствует справочнику '$R_PERSON'.")
                }


                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
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
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}'. $fioAndInp." +
                                " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)' не соответствует справочнику '$R_DUL'.")
                    }
                    if (!personDocNumberList.contains(ndflPerson.idDocNumber)) {
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}'. $fioAndInp." +
                                " Текст ошибки: 'Документ удостоверяющий личность.Номер (Графа 11)' не соответствует справочнику '$R_DUL'.")
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
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}', Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}'. $fioAndInp." +
                                " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)', 'Документ удостоверяющий личность.Номер (Графа 11)' не соответствует справочнику '$R_DUL'.")
                    } else {
                        int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                        if (incRep != 1) {
                            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}'. $fioAndInp." +
                                    " Текст ошибки: 'Документ удостоверяющий личность.Номер (Графа 11)' не включается в отчетность.")
                        }
                    }
                }

                // Спр18 Статус налогоплательщика (Обязательное поле)
                def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                if (!ndflPerson.status.equals(taxpayerStatus)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Cтатус (Графа 12)='${ndflPerson.status}'. $fioAndInp." +
                            " Текст ошибки: 'Cтатус (Графа 12)' не соответствует справочнику '$R_PERSON'.")
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

                // Адрес регистрации в Российской Федерации.Код субъекта
                if (!ndflPerson.regionCode.equals(regionCode)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)='${ndflPerson.regionCode}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Район
                if (!ndflPerson.area.equals(area)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Район (Графа 15)='${ndflPerson.area}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Район (Графа 15)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Город
                if (!ndflPerson.city.equals(city)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Город (Графа 16)='${ndflPerson.city}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Город (Графа 16)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Населенный пункт
                if (!ndflPerson.locality.equals(locality)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)='${ndflPerson.locality}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Улица
                if (!ndflPerson.street.equals(street)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Улица (Графа 18)='${ndflPerson.street}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Улица (Графа 18)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Дом
                if (!ndflPerson.house.equals(house)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Дом (Графа 19)='${ndflPerson.house}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Дом (Графа 19)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Корпус
                if (!ndflPerson.building.equals(building)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Корпус (Графа 20)='${ndflPerson.building}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Корпус (Графа 20)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Квартира
                if (!ndflPerson.flat.equals(flat)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Квартира (Графа 21)='${ndflPerson.flat}'. $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Квартира (Графа 21)' не соответствует справочнику '$R_PERSON'.")
                }
            }
        }
    }
    println "Проверки на соответствие справочникам / '${T_PERSON}' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / '${T_PERSON}' (" + (System.currentTimeMillis() - time) + " мс)");

    println "Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)";
    logger.info("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find { key, value -> value == ndflPersonIncome.incomeCode }) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Код (Графа 4)='${ndflPersonIncome.incomeCode}'. $fioAndInp." +
                    " Текст ошибки: 'Доход.Вид.Код (Графа 4)' не соответствует справочнику '$R_INCOME_CODE'.")
        }

        /*
        Спр6
        Доход.Вид.Признак (Графа 5) - (Необязательное поле)
        incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>

        Доход.Вид.Код (Графа 4) - (Необязательное поле)
        incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE.CODE>
         */
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeType)) {
            List<Long> incomeTypeIdList = incomeTypeMap.get(ndflPersonIncome.incomeType)
            if (incomeTypeIdList == null || incomeTypeIdList.isEmpty()) {
                logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Признак (Графа 5)='${ndflPersonIncome.incomeType}'. $fioAndInp." +
                        " Текст ошибки: 'Доход.Вид.Признак (Графа 5)' не соответствует справочнику '$R_INCOME_TYPE'.")
            } else {
                if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeCode)) {
                    List<String> incomeCodeList = []
                    incomeTypeIdList.each { incomeTypeId ->
                        String incomeCode = incomeCodeMap.get(incomeTypeId)
                        incomeCodeList.add(incomeCode)
//                        logger.info("Доход.Вид.Признак incomeTypeId=$incomeTypeId incomeCode=$incomeCode ndflPersonIncome.incomeCode=${ndflPersonIncome.incomeCode}")
                    }
                    if (!incomeCodeList.contains(ndflPersonIncome.incomeCode)) {
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Код (Графа 4)='${ndflPersonIncome.incomeCode}', Доход.Вид.Признак (Графа 5)='${ndflPersonIncome.incomeType}'. $fioAndInp." +
                                " Текст ошибки: Не найдено соответствие между 'Доход.Вид.Код (Графа 4)'='${ndflPersonIncome.incomeCode}' и 'Доход.Вид.Признак (Графа 5)'='${ndflPersonIncome.incomeType}' в справочнике '$R_INCOME_CODE'.")
                    }
                }
            }
        }

        // Спр7 НДФЛ.Процентная ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate.toString())) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate}'. $fioAndInp." +
                    " Текст ошибки: 'НДФЛ.Процентная ставка (Графа 14)' не соответствует справочнику '$R_RATE'.")
        }
    }
    println "Проверки на соответствие справочникам / '${T_PERSON_INCOME}' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / '${T_PERSON_INCOME}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_DEDUCTION}'. Строка ${ndflPersonDeduction.rowNum ?: ""}. Код вычета (Графа 3)='${ndflPersonDeduction.typeCode}'. $fioAndInp." +
                    " Текст ошибки: 'Код вычета (Графа 3)' не соответствует справочнику '$R_TYPE_CODE'.")
        }

        // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_DEDUCTION}'. Строка ${ndflPersonDeduction.rowNum ?: ""}. Документ о праве на налоговый вычет.Код источника (Графа 7)='${ndflPersonDeduction.notifSource}'. $fioAndInp." +
                    " Текст ошибки: 'Документ о праве на налоговый вычет.Код источника (Графа 7)' не соответствует справочнику '$R_NOTIF_SOURCE'.")
        }
    }
    println "Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION}' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
        // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_PREPAYMENT}'. Строка ${ndflPersonPrepayment.rowNum ?: ""}. Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)='${ndflPersonPrepayment.notifSource}'. $fioAndInp." +
                    " Текст ошибки: 'Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)' не соответствует справочнику '$R_NOTIF_SOURCE'.")
        }
    }
    println "Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT}' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT}' (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Общие проверки
 */
def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList) {

    // Map<NdflPerson.id, NdflPerson>
    def mapNdflPerson = [:]

    // Параметры подразделения
    def mapRefBookNdfl = getRefBookNdfl()
    def mapRefBookNdflDetail = getRefBookNdflDetail(mapRefBookNdfl.id)

    long time = System.currentTimeMillis();
    for (NdflPerson ndflPerson : ndflPersonList) {

        ScriptUtils.checkInterrupted();

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        mapNdflPerson.put(ndflPerson.id, ndflPerson)

        // Общ1 Корректность ИНН (Необязательное поле)
        if (ndflPerson.innNp != null && !ScriptUtils.checkControlSumInn(ndflPerson.innNp)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В Российской федерации (Графа 8)='${ndflPerson.innNp}'. $fioAndInp." +
                    " Текст ошибки: Некорректное значение 'ИНН.В Российской федерации (Графа 8)'.")
        }

        // Общ11 СНИЛС (Необязательное поле)
        if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. СНИЛС='${ndflPerson.snils}'. $fioAndInp." +
                    " Текст ошибки: Некорректное значение 'СНИЛС'.")
        }
    }
    println "Общие проверки / '${T_PERSON}' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Общие проверки / '${T_PERSON}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        ScriptUtils.checkInterrupted();

        NdflPerson ndflPerson = mapNdflPerson.get(ndflPersonIncome.ndflPersonId)
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])

        // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ
        operationNotRelateToCurrentPeriod(ndflPersonIncome.incomeAccruedDate, ndflPersonIncome.incomePayoutDate, ndflPersonIncome.taxDate,
                ndflPersonIncome.kpp, ndflPersonIncome.oktmo, ndflPerson.inp, fio)

        // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
        List<ColumnFillConditionData> columnFillConditionDataList = []
        //1 Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column4And5Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Вид.Код (Графа 4)='${ndflPersonIncome.incomeCode ?: ""}', Доход.Вид.Признак (Графа 5)='${ndflPersonIncome.incomeType ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."
        )
        //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column6Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Дата.Начисление (Графа 6)='${ndflPersonIncome.incomeAccruedDate ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10."
        )
        //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column7Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Дата.Выплата (Графа 7)='${ndflPersonIncome.incomePayoutDate ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11."
        )
        //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6Fill(),
                new Column10Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Сумма.Начисление (Графа 10)='${ndflPersonIncome.incomeAccruedSumm ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6."
        )
        //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column11Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Сумма.Выплата (Графа 11)='${ndflPersonIncome.incomePayoutSumm ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7."
        )
        //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24Fill(),
                new Column12NotFill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Сумма вычета (Графа 12)='${ndflPersonIncome.totalDeductionsSumm ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графы 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22,23,24."
        )
        //7 Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column13And14And15Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Налоговая база (Графа 13)='${ndflPersonIncome.taxBase ?: ""}', НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}', НДФЛ.Расчет.Дата (Графа 15)='${ndflPersonIncome.taxDate ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графы 13,14,15 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24."
        )
        //8 Раздел 2. Графа 16 должна быть заполнена, если заполнены Раздел 2. Графы 6,10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6And10Fill(),
                new Column16Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)='${ndflPersonIncome.calculatedTax ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 16 должна быть заполнена, если заполнены Раздел 2. Графы 6,10."
        )
        //9 Раздел 2. Графа 18 или 19 должна быть заполнена, если заполнены Раздел 2. Графы 6,10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6And10Fill(),
                new Column18Or19Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)='${ndflPersonIncome.notHoldingTax ?: ""}', НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)='${ndflPersonIncome.overholdingTax ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 18 или 19 должна быть заполнена, если заполнены Раздел 2. Графы 6,10."
        )
        //10 Раздел 2. Графа 17 должна быть заполнена, если заполнены Раздел 2. Графы 7,11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11Fill(),
                new Column17Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Удержанный (Графа 17)='${ndflPersonIncome.withholdingTax ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 17 должна быть заполнена, если заполнены Раздел 2. Графы 7,11."
        )
        //11 Раздел 2. Графа 20 должна быть заполнена, если заполнены Раздел 2. Графы 7,11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11Fill(),
                new Column20Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)='${ndflPersonIncome.refoundTax ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 20 должна быть заполнена, если заполнены Раздел 2. Графы 7,11."
        )
        //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7,11 или 23,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11Or22And23And24Fill(),
                new Column21Fill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Перечисление в бюджет.Срок (Графа 21)='${ndflPersonIncome.taxTransferDate ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7,11 или 23,24,25."
        )
        //12 Раздел 2. Графа 21 должна быть НЕ заполнена, если НЕ заполнены Раздел 2. Графы 7,11 и 23,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11And22And23And24NotFill(),
                new Column21NotFill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Перечисление в бюджет.Срок (Графа 21)='${ndflPersonIncome.taxTransferDate ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Раздел 2. Графа 21 должна быть не заполнена, если не заполнены Раздел 2. Графы 7,11 и 23,24,25."
        )
        //13 Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column22And23And24FillOrColumn22And23And24NotFill(),
                "Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка '${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Перечисление в бюджет.Платежное поручение.Дата (Графа 22)='${ndflPersonIncome.paymentDate ?: ""}', НДФЛ.Перечисление в бюджет.Платежное поручение.Номер (Графа 23)='${ndflPersonIncome.paymentNumber ?: ""}', НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)='${ndflPersonIncome.taxSumm ?: ""}'. $fioAndInp." +
                        " Текст ошибки: Должны быть либо заполнены все 3 Графы 22,23,24, либо ни одна их них."
        )
        columnFillConditionDataList.each { columnFillConditionData ->
            if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                    !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                logger.warn(columnFillConditionData.conditionMessage)
            }
        }

        // Общ10 Соответствие КПП и ОКТМО Тербанку
        if (ndflPersonIncome.oktmo != null) {
            def kppList = mapRefBookNdflDetail.get(ndflPersonIncome.oktmo)
            if (kppList == null || !kppList.contains(ndflPersonIncome.kpp)) {
                Department department = departmentService.get(declarationData.departmentId)
                if (kppList == null) {
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Источник выплаты.ОКТМО (Графа 8)='${ndflPersonIncome.oktmo}'." +
                            " Текст ошибки: значение 'Доход.Источник выплаты.ОКТМО (Графа 8)' не найдено в Справочнике '$R_DETAIL' для подразделения '${department ? department.name : ""}'.")
                } else {
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка '${ndflPersonIncome.rowNum ?: ""}'. Доход.Источник выплаты.КПП (Графа 9)='${ndflPersonIncome.kpp}'." +
                            " Текст ошибки: значение 'Доход.Источник выплаты.КПП (Графа 9)' не найдено в Справочнике '$R_DETAIL' для подразделения '${department ? department.name : ""}'.")
                }
            }
        }
    }

    ScriptUtils.checkInterrupted();

    println "Общие проверки / '$T_PERSON_INCOME' (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Общие проверки / '$T_PERSON_INCOME' (" + (System.currentTimeMillis() - time) + " мс)");

    ScriptUtils.checkInterrupted();

    // Общ8 Отсутствие пропусков и повторений в нумерации строк
    time = System.currentTimeMillis();

    List<Integer> rowNumPersonList = ndflPersonService.findDublRowNum("NDFL_PERSON", declarationData.id)
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

    println "Общие проверки / Проверки на отсутсвие повторений (" + (System.currentTimeMillis() - time) + " мс)";
    logger.info("Общие проверки / Проверки на отсутсвие повторений (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Класс для проверки заполненности полей
 */
class ColumnFillConditionData {
    ColumnFillConditionChecker columnConditionCheckerAsIs
    ColumnFillConditionChecker columnConditionCheckerToBe
    String conditionMessage

    ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionMessage) {
        this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
        this.columnConditionCheckerToBe = columnConditionCheckerToBe
        this.conditionMessage = conditionMessage
    }
}
interface ColumnFillConditionChecker {
    boolean check(NdflPersonIncome ndflPersonIncome)
}
/**
 * Проверка: "Раздел 2. Графа 4,5 заполнены"
 */
class Column4And5Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.incomeCode) && !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)
    }
}
/**
 * Проверка: "Раздел 2. Графа 6 заполнена"
 */
class Column6Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomeAccruedDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 7 заполнена"
 */
class Column7Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomePayoutDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 10 заполнена"
 */
class Column10Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графы 6, 10 заполнены"
 */
class Column6And10Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomeAccruedDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графа 11 заполнена"
 */
class Column11Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 заполнены"
 */
class Column7And11Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графа 12 НЕ заполнена"
 */
class Column12NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графы 13, 14, 15 заполнены"
 */
class Column13And14And15Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.taxBase) && !ScriptUtils.isEmpty(ndflPersonIncome.taxRate) && ndflPersonIncome.taxDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графы 16 заполнена"
 */
class Column16Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 17 заполнена"
 */
class Column17Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)
    }
}
/**
 * Проверка: "Раздел 2. Графа 18 или 19 заполнена"
 */
class Column18Or19Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.notHoldingTax) || !ScriptUtils.isEmpty(ndflPersonIncome.overholdingTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 20 заполнена"
 */
class Column20Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.refoundTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 21 заполнена"
 */
class Column21Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.taxTransferDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графы 21 НЕ заполнена"
 */
class Column21NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.taxTransferDate == null
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 ИЛИ 22, 23, 24 заполнены"
 */
class Column7And11Or22And23And24Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return (ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) ||
                (ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm))
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 И 22, 23, 24 НЕ заполнены"
 */
class Column7And11And22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return (ndflPersonIncome.incomePayoutDate == null && ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) &&
                (ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ScriptUtils.isEmpty(ndflPersonIncome.taxSumm))
    }
}
/**
 * Проверка: "Раздел 2. Графы 22, 23, 24 НЕ заполнены"
 */
class Column22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графы 22, 23, 24 заполнены"
 */
class Column22And23And24Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)
    }
}
/**
 * 	Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна их них
 */
class Column22And23And24FillOrColumn22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return new Column22And23And24NotFill().check(ndflPersonIncome) || new Column22And23And24Fill().check(ndflPersonIncome)
    }
}
/**
 * 	Всегда возвращает true
 */
class ColumnTrueFillOrNotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return true
    }
}

/**
 * Проверки сведений о доходах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 * @param ndflPersonDeductionList
 * @param ndflPersonPrepaymentList
 */
def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                    List<NdflPersonPrepayment> ndflPersonPrepaymentList) {

    def personsCache = [:]
    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
        personsCache.put(ndflPerson.id, ndflPerson)
    }

    def ndflPersonPrepaymentCache = [:]
    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: []
        ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
        ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
    }

    List<DateConditionData> dateConditionDataList = []

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1010", "3020", "1110", "1400", "2001", "2010", "2012",
                                                                 "2300", "2710", "2760", "2762", "2770", "2900", "4800"],
            ["00"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                                 "1548", "1549", "1551", "1552", "1554"],
            ["01", "02"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // Соответствует маске 31.12.20**
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                 "1541", "1542", "1543"],
            ["04"], new MatchMask("31.12.20\\d{2}"), """"Соответствует маске 31.12.20**""")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000"], ["05"], new LastMonthCalendarDay(), """"Последний календарный день месяца""")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["07"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear(), """"«Графа 7 Раздел 2» < 31.12.20**, то «Графа 6 Раздел 2» = «Графа 7 Раздел 2», иначе «Графа 6 Раздел 2» = 31.12.20**""")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["11"], new LastMonthCalendarDay(), """"Последний календарный день месяца""")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear(), """"«Графа 7 Раздел 2» < 31.12.20**, то «Графа 6 Раздел 2» = «Графа 7 Раздел 2», иначе «Графа 6 Раздел 2» = 31.12.20**""")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790"], ["00"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // Последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
    dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthCalendarDayButNotFree(), """Последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день""")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2640", "2641"], ["00"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2800"], ["00"], new Column6EqualsColumn7(), """"«Графа 6 Раздел 2» = «Графе 7 Раздел 2»""")

    // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
    def ndflPersonIncomeCache = [:]
    ndflPersonIncomeList.each { ndflPersonIncome ->
        List<NdflPersonIncome> ndflPersonIncomeByNdflPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
        ndflPersonIncomeByNdflPersonIdList.add(ndflPersonIncome)
        ndflPersonIncomeCache.put(ndflPersonIncome.ndflPersonId, ndflPersonIncomeByNdflPersonIdList)
    }

    ndflPersonIncomeCache.each {

        ScriptUtils.checkInterrupted();

        for (NdflPersonIncome ndflPersonIncome : it.value) {
            def ndflPerson = personsCache.get(ndflPersonIncome.ndflPersonId)
            def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            // СведДох1 Доход.Дата.Начисление (Графа 6)
            if (dateConditionDataList != null) {
                dateConditionDataList.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        if (!dateConditionData.checker.check(ndflPersonIncome)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'.Строка='${ndflPersonIncome.rowNum ?: ""}'. Доход.Дата.Начисление (Графа 6)='${ndflPersonIncome.incomeAccruedDate ?: ""}' $fioAndInp." +
                                    " Не выполнено условие: если «Графа 4 Раздел 2»='${ndflPersonIncome.incomeCode}' и «Графа 5 Раздел 2»='${ndflPersonIncome.incomeType}', то ${dateConditionData.conditionMessage}.")
                        }
                    }
                }
            }

            // СведДох2 Сумма вычета (Графа 12)
            BigDecimal sumNdflDeduction = getDeductionSumForIncome(ndflPersonIncome, ndflPersonDeductionList)
            if (!comparNumbEquals(ndflPersonIncome.totalDeductionsSumm ?: 0, sumNdflDeduction)) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. Сумма вычета (Графа 12)='${ndflPersonIncome.totalDeductionsSumm ?: ""}' $fioAndInp." +
                        " Значение не соответствует правилу: Графа 12 Раздел 2 = сумма значений граф 16 Раздел 3.")
            }
            if (comparNumbGreater(sumNdflDeduction, ndflPersonIncome.incomeAccruedSumm ?: 0)) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. Доход.Сумма.Начисление (Графа 10)='${ndflPersonIncome.incomeAccruedSumm ?: ""}' $fioAndInp." +
                        " Значение не соответствует правилу: сумма значений граф 16 Раздела 3 ≤ графа 10 Раздел 2.")
            }

            // СведДох4 НДФЛ.Процентная ставка (Графа 14)
            if (ndflPersonIncome.taxRate == 13) {
                Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                Boolean conditionB = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status == "1"
                Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010", "2002", "2003"].contains(ndflPersonIncome.incomeCode) && Integer.parseInt(ndflPerson.status ?: 0) >= 3
                if (!(conditionA || conditionB || conditionC)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 13» не выполнено ни одно из условий:\\n" +
                            " «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» ≠ 1010 и «Графа 12 Раздел 1» ≠ 2\\n" +
                            " «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» = 1\\n" +
                            " «Графа 7 Раздел 1» ≠ 643 и («Графа 4 Раздел 2» = 2000 или 2001 или 2010 или 2002 или 2003) и («Графа 12 Раздел 1» ≥ 3).")
                }
            } else if (ndflPersonIncome.taxRate == 15) {
                if (!(ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 15» не выполнено условие: «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» ≠ 1.")
                }
            } else if (ndflPersonIncome.taxRate == 35) {
                if (!(["2740", "3020", "2610"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 35» не выполнено условие: «Графа 4 Раздел 2» = (2740 или 3020 или 2610) и «Графа 12 Раздел 1» ≠ 2.")
                }
            } else if (ndflPersonIncome.taxRate == 30) {
                def conditionA = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && ndflPersonIncome.incomeCode != "1010"
                def conditionB = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                if (!(conditionA || conditionB)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 30» не выполнено ни одно из условий:\\n" +
                            " «Графа 12 Раздел 1» ≥ 2 и «Графа 4 Раздел 2» ≠ 1010\\n" +
                            " («Графа 4 Раздел 2» ≠ 2000 или 2001 или 2010) и «Графа 12 Раздел 1» > 2.")
                }
            } else if (ndflPersonIncome.taxRate == 9) {
                if (!(ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 9» не выполнено условие: «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» = 1110 и «Графа 12 Раздел 1» = 1.")
                }
            } else {
                if (!(ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = ${ndflPersonIncome.taxRate}» не выполнено условие: «Графа 7 Раздел 1» ≠ 643 и «Графа 4 Раздел 2» = 1010 и «Графа 12 Раздел 1» ≠ 1.")
                }
            }

            // СведДох5 НДФЛ.Расчет.Дата (Графа 15)
            if (ndflPersonIncome.taxDate != null) {
                // Должна быть выполнена хотя бы одна проверка
                boolean checkTaxDate = false
                // СведДох5.1
                if (ndflPersonIncome.calculatedTax ?: 0 > 0 && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                    if (ndflPersonIncome.taxDate == ndflPersonIncome.incomeAccruedDate) {
                        checkTaxDate = true
                    }
                }
                // СведДох5.2
                if (!checkTaxDate) {
                    if (ndflPersonIncome.withholdingTax ?: 0 > 0 && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = true
                        }
                    }
                }
                // СведДох5.3
                if (!checkTaxDate) {
                    if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                            ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null &&
                            !["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode)) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = true
                        }
                    }
                }
                // СведДох5.4
                if (!checkTaxDate) {
                    if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                            ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                            ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode) &&
                            ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate()) {
                        // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                        if (ndflPersonIncome.taxDate == ndflPersonIncome.incomeAccruedDate) {
                            checkTaxDate = true
                        }
                    }
                }
                // СведДох5.5
                if (!checkTaxDate) {
                    if (ndflPersonIncome.notHoldingTax ?: 0 > 0 &&
                            ndflPersonIncome.withholdingTax ?: 0 < ndflPersonIncome.calculatedTax ?: 0 &&
                            ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542"].contains(ndflPersonIncome.incomeCode) &&
                            (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
                        // «Графа 15 Раздел 2"» = "31.12.20**"
                        Calendar calendarPayout = Calendar.getInstance()
                        calendarPayout.setTime(ndflPersonIncome.taxDate)
                        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                        int month = calendarPayout.get(Calendar.MONTH)
                        if (dayOfMonth == 31 && month == 12) {
                            checkTaxDate = true
                        }
                    }
                }
                // СведДох5.6
                if (!checkTaxDate) {
                    if (ndflPersonIncome.overholdingTax ?: 0 > 0 &&
                            ndflPersonIncome.withholdingTax ?: 0 > ndflPersonIncome.calculatedTax ?: 0 &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate) {
                            checkTaxDate = true
                        }
                    }
                }
                // СведДох5.7
                if (!checkTaxDate) {
                    if (ndflPersonIncome.refoundTax ?: 0 > 0 &&
                            ndflPersonIncome.withholdingTax ?: 0 > ndflPersonIncome.calculatedTax ?: 0 &&
                            ndflPersonIncome.overholdingTax ?: 0 &&
                            ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                        // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                        if (!(ndflPersonIncome.taxDate == ndflPersonIncome.incomePayoutDate)) {
                            checkTaxDate = true
                        }
                    }
                }
                if (!checkTaxDate) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Дата (Графа 15)='${ndflPersonIncome.taxDate}' $fioAndInp." +
                            " Не выполнено ни одно из условий проверок при «Графа 15 Раздел 2» ≠ '0'.")
                }
            }

            // СведДох6 НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
            if (ndflPersonIncome.calculatedTax != null) {
                // СведДох6.1
                if (ndflPersonIncome.taxRate != 13) {
                    if (ndflPersonIncome.calculatedTax ?: 0 != ScriptUtils.round((ndflPersonIncome.taxBase ?: 0 * ndflPersonIncome.taxRate ?: 0), 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'.Графа НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)='${ndflPersonIncome.calculatedTax}' $fioAndInp." +
                                " Не выполнено условие: если «Графа 14 Раздел 2» ≠ '13', то «Графа 16' = «Графа 13 Раздел 2» × «Графа 14 Раздел 2», с округлением до целого числа по правилам округления.")
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
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    List<NdflPersonIncome> S1List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate
                        it.incomeAccruedSumm != null && it.incomeAccruedSumm != 0 &&
                                ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate() &&
                                it.taxRate == 13 && it.incomeCode != "1010"
                    } ?: []
                    BigDecimal S1 = S1List.sum { it.taxBase ?: 0 } ?: 0
                    /*
                    S2 - сумма значений по "Графе 16" (calculatedTax)
                    Для суммирования строк по "Графе 16" (calculatedTax) должны быть соблюдены ВСЕ следующие условия:
                    1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                    2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) < "Графы 6" для текущей строки (МЕНЬШЕ)
                    2. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                    3. Значение "Графы 14" (taxRate) = 13
                    4. Значение "Графы 4" (incomeCode) != "1010"
                     */
                    List<NdflPersonIncome> S2List = S2List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate() &&
                                it.taxRate == 13 && it.incomeCode != "1010"
                    } ?: []
                    BigDecimal S2 = S2List.sum { it.calculatedTax ?: 0 } ?: 0
                    // Сумма по «Графа 16» текущей операции = S1 x 13% - S2
                    if (ndflPersonIncome.calculatedTax != ScriptUtils.round((S1 * 13 - S2), 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME' .Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)='${ndflPersonIncome.calculatedTax}' $fioAndInp." +
                                " Не выполнено условие: сумма по «Графа 16 Раздел 2» текущей операции = S1 x 13%% - S2.")
                    }
                }
                // СведДох6.3
                if (ndflPersonIncome.taxRate == 13 && ndflPerson.status == "6") {
                    List<NdflPersonPrepayment> ndflPersonPrepaymentListByBersonIdList = ndflPersonPrepaymentCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    if (!ndflPersonPrepaymentListByBersonIdList.isEmpty()) {
                        List<NdflPersonPrepayment> ndflPersonPrepaymentCurrentList = ndflPersonPrepaymentListByBersonIdList.findAll { it.operationId == ndflPersonIncome.operationId } ?: []
                        Long ndflPersonPrepaymentSum = ndflPersonPrepaymentCurrentList.sum { it.summ } ?: 0
                        if (!(ndflPersonIncome.calculatedTax ==
                                ScriptUtils.round((ndflPersonIncome.taxBase ?: 0 * 13 - ndflPersonPrepaymentSum ?: 0), 0))
                        ) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)='${ndflPersonIncome.calculatedTax}' $fioAndInp" +
                                    " Не выполнено условие: «Графа 16 Раздел 2» = «Графа 13 Раздел 2» x 13%% - «Графа 4 Раздел 4» .")
                        }
                    }
                }
            }

            // СведДох7 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
            if (ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                // СведДох7.1
                if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"] && ndflPersonIncome.incomeType == "02")
                        && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                ) {
                    // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                            && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Удержанный (Графа 17)='${ndflPersonIncome.withholdingTax}' $fioAndInp." +
                                " Не выполнено условие: если (((«Графа 4 Раздел 2» = 2520 или 2720 или 2740 или 2750 или 2790 или 4800) и «Графа 5 Раздел 2» = '13')" +
                                " или ((«графа 4» = 1530 или 1531 или 1532 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1543* или 1544 или 1545 или 1546 или 1547" +
                                " или 1548 или 1549 или 1551 или 1552 или 1554) и «Графа 5 Раздел 2» ≠ 02)) и «Графа 19 Раздел 2» = 0, то «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2».")
                    }
                } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                        && ndflPersonIncome.overholdingTax > 0
                ) {
                    // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» ≤ ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    NdflPersonIncome ndflPersonIncomePreview = null
                    if (!ndflPersonIncomeCurrentList.isEmpty()) {
                        ndflPersonIncomePreview = ndflPersonIncomeCurrentList.find {
                            it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                    (ndflPersonIncomePreview == null || ndflPersonIncomePreview.incomeAccruedDate < it.incomeAccruedDate)
                        }
                    }
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax ?: 0 + ndflPersonIncomePreview.calculatedTax ?: 0
                            && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0
                            && ndflPersonIncome.withholdingTax <= (ScriptUtils.round(ndflPersonIncome.taxBase ?: 0, 0) - ndflPersonIncome.calculatedTax) * 50)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Удержанный (Графа 17)='${ndflPersonIncome.withholdingTax}' $fioAndInp." +
                                " Не выполнено условие: если «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» ≤ ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%%).")
                    }
                } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                             "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                ) {
                    if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Удержанный (Графа 17)='${ndflPersonIncome.withholdingTax}' $fioAndInp." +
                                " Не выполнено условие: если ((«Графа 4 Раздел 2» = 2520 или 2720 или 2740 или 2750 или 2790 или 4800) и «Графа 5 Раздел 2» = '14')" +
                                " или ((«Графа 4 Раздел 2» = 1530 или 1531 или 1532 или 1533 или 1535 или 1536 или 1537 или 1539 или 1541 или 1542 или 1544" +
                                " или 1545 или 1546 или 1547 или 1548 или 1549 или 1551 или 1552 или 1554 ) и «Графа 5 Раздел 2» = '02')," +
                                " то «Графа 17 Раздел 2» = 0.")
                    }
                } else if (!(ndflPersonIncome.incomeCode != null)) {
                    if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Расчет.Сумма.Удержанный (Графа 17)='${ndflPersonIncome.withholdingTax}' $fioAndInp." +
                                " Не выполнено условие: если «Графа 4 Раздел 2» ≠ 0, то «Графа 17 Раздел 2» = «Графа 24 Раздел 2».")
                    }
                }
            }

            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdAndOperationIdList = ndflPersonIncomeCurrentByPersonIdList.findAll { it.operationId == ndflPersonIncome.operationId } ?: []
            // "Сумма Граф 16"
            Long calculatedTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.calculatedTax ?: 0 } ?: 0
            // "Сумма Граф 17"
            Long withholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.withholdingTax ?: 0 } ?: 0
            // "Сумма Граф 18"
            Long notHoldingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.notHoldingTax ?: 0 } ?: 0
            // "Сумма Граф 19"
            Long overholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.overholdingTax ?: 0 } ?: 0
            // "Сумма Граф 20"
            Long refoundTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.refoundTax ?: 0 } ?: 0

            // СведДох8 НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
            if (calculatedTaxSum > withholdingTaxSum) {
                if (!(notHoldingTaxSum == calculatedTaxSum - withholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. сумма НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)='${notHoldingTaxSum}' $fioAndInp." +
                            " Не выполнено условие: «Сумма Граф 16 Раздел 2» > «Сумма Граф 17 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2»," +
                            " то «Сумма Граф 18 Раздел 2» = «Сумма Граф 16 Раздел 2» - «Сумма Граф 17 Раздел 2».")
                }
            }

            // СведДох9 НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
            if (calculatedTaxSum < withholdingTaxSum) {
                if (!(overholdingTaxSum == withholdingTaxSum - calculatedTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. сумма НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)='${overholdingTaxSum}' $fioAndInp." +
                            " Не выполнено условие: «Сумма Граф 16 Раздел 2» < «Сумма Граф 17 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2»," +
                            " то «Сумма Граф 19 Раздел 2» = «Сумма Граф 17 Раздел 2» - «Сумма Граф 16 Раздел 2».")
                }
            }

            // СведДох10 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
            if (ndflPersonIncome.refoundTax > 0) {
                if (!(refoundTaxSum <= overholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. сумма НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)='${refoundTaxSum}' $fioAndInp." +
                            " Не выполнено условие: если «Графа 20 Раздел 2» > 0," +
                            " то «Сумма Граф 20 Раздел 2» ≤ «Сумма Граф 19 Раздел 2» для текущей пары «Графа 2 Раздел 2» и «Графа 3 Раздел 2».")
                }
            }

            // СведДох11 НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
            if (ndflPersonIncome.taxSumm != null) {

                dateConditionDataList = []

                // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["1010", "3020", "1110", "1400", "2001", "2010",
                                                                "2710", "2760", "2762", "2770", "2900", "4800"], ["00"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                // 3,4 "Графа 21" ≤ "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" ≤ "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
                dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                                "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                                "1548", "1549", "1551", "1552", "1553", "1554"], ["01", "02", "03", "04"],
                        new Column21EqualsColumn7Plus30WorkingDays(), """«Графа 21 Раздел 2» ≤ «Графа 7 Раздел 2» + "30 календарных дней", если «Графа 7 Раздел 2» + "30 календарных дней" - выходной день, то «Графа 21 Раздел 2» ≤ "Следующий рабочий день" после «Графа 7 Раздел 2» + "30 календарных дней\"""")

                // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["2000"], ["05", "06", "07", "08", "09", "10", "11", "12"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                // 7 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["2002"], ["07", "08", "09", "10"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                // 8 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["2003"], ["13"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                // 9 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
                dateConditionDataList << new DateConditionData(["2012", "2300"], ["00"],
                        new Column21EqualsColumn7LastDayOfMonth(), """«Графа 21 Раздел 2» = Последний календарный день месяца для месяца «Графы 7 Раздел 2», если Последний календарный день месяца - выходной день, то «Графа 21 Раздел 2» = следующий рабочий день""")

                // 10 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["2520", "2740", "2750", "2790", "4800"], ["13"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                // 12,13,14 "Графа 21" = "Графа 7" + "1 рабочий день"
                dateConditionDataList << new DateConditionData(["2610", "2640", "2641", "2800"], ["00"],
                        new Column21EqualsColumn7Plus1WorkingDay(), """«Графа 21 Раздел 2» = «Графа 7 Раздел 2» + "1 рабочий день\"""")

                dateConditionDataList.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        if (!dateConditionData.checker.check(ndflPersonIncome)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. 'НДФЛ.Перечисление в бюджет.Срок (Графа 21)'=${ndflPersonIncome.taxTransferDate} и 'Доход.Дата.Выплата (Графа 7)'=${ndflPersonIncome.incomePayoutDate} $fioAndInp." +
                                    " Не выполнено условие: если «Графа 4 Раздел 2» = ${ndflPersonIncome.incomeCode} и «Графа 5 Раздел 2» = ${ndflPersonIncome.incomeType}, то ${dateConditionData.conditionMessage}.")
                        }
                    } else if (["2520", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14") {
                        // 11 "Графа 21" = "Графа 7" + "1 рабочий день"
                        /*
                        Найти следующую за текущей строкой, удовлетворяющую условиям:
                        "Графа 10" > "0"
                        "Графа 5" ≠ "02"
                        "Графа 5"≠ "14"
                        "Графа 7" является минимальной из "Граф 7", удовлетворяющих условию: ("Графа 7" (следующей строки) ≥ "Графа 7" (текущей строки))
                        "Графа 7" ≤ "31.12.20**" + "1 календарный день", где 31.12.20** - последний день текущего года
                         */

                        // Получим 1-ый рабочий день следующего года
                        Calendar firstWorkingDay = Calendar.getInstance()
                        firstWorkingDay.setTime(getReportPeriodStartDate())
                        firstWorkingDay.set(Calendar.DAY_OF_YEAR, firstWorkingDay.getActualMaximum(Calendar.DAY_OF_YEAR))
                        firstWorkingDay.add(Calendar.DATE, 1)
                        if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            firstWorkingDay.add(Calendar.DATE, 2);
                        }
                        if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            firstWorkingDay.add(Calendar.DATE, 1);
                        }

                        // Найдем следующую за текущей строку в РНУ
                        List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                        NdflPersonIncome ndflPersonIncomeFind = null;
                        ndflPersonIncomeCurrentList.each {
                            if (it.incomeAccruedSumm ?: 0 > 0 && !["02", "14"].contains(it.incomeType)
                                    && (ndflPersonIncomeFind == null || ndflPersonIncomeFind.incomePayoutDate > it.incomePayoutDate)
                                    && ndflPersonIncome.incomePayoutDate <= it.incomePayoutDate
                                    && ndflPersonIncome.operationId < it.operationId) {
                                if (it.incomePayoutDate.before(firstWorkingDay.getTime()) || it.incomePayoutDate.equals(firstWorkingDay.getTime())) {
                                    ndflPersonIncomeFind = it
                                }
                            }
                        }
                        if (ndflPersonIncomeFind != null) {
                            Column21EqualsColumn7Plus1WorkingDay column7Plus1WorkingDay = new Column21EqualsColumn7Plus1WorkingDay()
                            if (!column7Plus1WorkingDay.check(ndflPersonIncomeFind)) {
                                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                                logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncomeFind.rowNum}'. 'НДФЛ.Перечисление в бюджет.Срок (Графа 21)'=${ndflPersonIncomeFind.taxTransferDate} и 'Доход.Дата.Выплата (Графа 7)'=${ndflPersonIncomeFind.incomePayoutDate} $fioAndInp." +
                                        " Не выполнено условие: если «Графа 4 Раздел 2» = ${ndflPersonIncomeFind.incomeCode} и «Графа 5 Раздел 2» = ${ndflPersonIncomeFind.incomeType}, то «Графа 21 Раздел 2» = «Графа 7 Раздел 2» + '1 рабочий день.")
                            }
                        }
                    }
                }
            }
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
                && ndflPersonIncome.incomeAccruedDate?.format("dd.MM.yyyy") == ndflPersonDeduction.incomeAccrued?.format("dd.MM.yyyy")
                && ndflPersonIncome.ndflPersonId == ndflPersonDeduction.ndflPersonId) {
            sumNdflDeduction += ndflPersonDeduction.periodCurrSumm ?: 0
        }
    }
    return sumNdflDeduction
}

/**
 * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
 */
class DateConditionData {
    List<String> incomeCodes
    List<String> incomeTypes
    DateConditionChecker checker
    String conditionMessage

    DateConditionData(List<String> incomeCodes, List<String> incomeTypes, DateConditionChecker checker, String conditionMessage) {
        this.incomeCodes = incomeCodes
        this.incomeTypes = incomeTypes
        this.checker = checker
        this.conditionMessage = conditionMessage
    }
}

interface DateConditionChecker {
    boolean check(NdflPersonIncome ndflPersonIncome)
}

/**
 * Проверка: "Графа 6" = "Графе 7"
 */
class Column6EqualsColumn7 implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        String accrued = ndflPersonIncome.incomeAccruedDate?.format("dd.MM.yyyy")
        String payout = ndflPersonIncome.incomePayoutDate?.format("dd.MM.yyyy")
        return accrued == payout
    }
}

/**
 * Проверка: Соответствия маске
 */
class MatchMask implements DateConditionChecker {
    String maskRegex

    MatchMask(String maskRegex) {
        this.maskRegex = maskRegex
    }

    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return false
        }
        String accrued = ndflPersonIncome.incomeAccruedDate.format("dd.MM.yyyy")
        Pattern pattern = Pattern.compile(maskRegex)
        Matcher matcher = pattern.matcher(accrued)
        if (matcher.matches()) {
            return true
        }
        return false
    }
}

/**
 * Проверка "Последний календарный день месяца"
 */
class LastMonthCalendarDay implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return false
        }
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(ndflPersonIncome.incomeAccruedDate)
        int currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(calendar.DATE, 1)
        int comparedMonth = calendar.get(Calendar.MONTH)
        return currentMonth != comparedMonth
    }
}

/**
 * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
 */
class Column7LastDayOfYear implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendarPayout = Calendar.getInstance()
        calendarPayout.setTime(ndflPersonIncome.incomePayoutDate)
        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
        int month = calendarPayout.get(Calendar.MONTH)
        if (dayOfMonth != 31 || month != 12) {
            return new Column6EqualsColumn7().check(ndflPersonIncome)
        } else {
            return new MatchMask("31.12.20\\d{2}").check(ndflPersonIncome)
        }
    }
}

/**
 * Проверка: Последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
 */
class LastMonthCalendarDayButNotFree implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return false
        }
        boolean lastMonthDay = new LastMonthCalendarDay().check(ndflPersonIncome)
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(ndflPersonIncome.incomeAccruedDate)
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
 * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
 */
class Column21EqualsColumn7Plus1WorkingDay implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(ndflPersonIncome.incomePayoutDate);

        calendar7.add(Calendar.DATE, 1);
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar7.add(Calendar.DATE, 2);
        }
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar7.add(Calendar.DATE, 1);
        }

        return calendar21.equals(calendar7);
    }
}

/**
 * Проверка: "Графа 21" ≤ "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" ≤ "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
 */
class Column21EqualsColumn7Plus30WorkingDays implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(ndflPersonIncome.incomePayoutDate);

        calendar7.add(Calendar.DATE, 30);
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar7.add(Calendar.DATE, 2);
        }
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar7.add(Calendar.DATE, 1);
        }

        return calendar21.before(calendar7) || calendar21.equals(calendar7);
    }
}

/**
 * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
 */
class Column21EqualsColumn7LastDayOfMonth implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(ndflPersonIncome.incomePayoutDate);

        calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar7.add(Calendar.DATE, 2);
        }
        if (calendar7.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar7.add(Calendar.DATE, 1);
        }

        return calendar21.equals(calendar7);
    }
}

/**
 * Проверки Сведения о вычетах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 * @param ndflPersonDeductionList
 */
def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList) {

    for (NdflPerson ndflPerson : ndflPersonList) {
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp ?: ""])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
    }

    def mapNdflPersonIncome = [:]
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
        String operationIdNdflPersonIdDate = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}_${ndflPersonIncome.incomeAccruedDate ? ScriptUtils.formatDate(ndflPersonIncome.incomeAccruedDate, "dd.MM.yyyy") : ""}"
        mapNdflPersonIncome.put(operationIdNdflPersonIdDate, ndflPersonIncome)
    }

    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Выч14 Документ о праве на налоговый вычет.Код источника (Графа 7)
        if (ndflPersonDeduction.typeCode == "1" && ndflPersonDeduction.notifSource != "0000") {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION' .Строка='${ndflPersonDeduction.rowNum ?: ""}'. Документ о праве на налоговый вычет.Код источника (Графа 7)='${ndflPersonDeduction.notifSource}' $fioAndInp." +
                    " Текст ошибки: 'Код вычета'='${ndflPersonDeduction.typeCode}', 'Код источника'='${ndflPersonDeduction.notifSource}'.")
        }

        // Выч15 (Графы 9)
        // Выч16 (Графы 10)
        String operationIdNdflPersonIdDate = "${ndflPersonDeduction.operationId}_${ndflPersonDeduction.ndflPersonId}_${ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued, "dd.MM.yyyy")}"
        NdflPersonIncome ndflPersonIncome = mapNdflPersonIncome.get(operationIdNdflPersonIdDate)
        if (ndflPersonIncome == null) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION'. Строка='${ndflPersonDeduction.rowNum ?: ""}' $fioAndInp." +
                    " Текст ошибки: не была найдена записи в таблице '$T_PERSON_INCOME', где 'ID операции (Графа 9)' = '${ndflPersonDeduction.operationId}'," +
                    " ссылка на таблицу '$T_PERSON' = '${ndflPersonDeduction.ndflPersonId}' и 'Доход.Дата.Начисление (Графа 6)'='${ScriptUtils.formatDate(ndflPersonDeduction.incomeAccrued, 'dd.MM.yyyy')}'.")
        } else {
            // Выч17 Начисленный доход.Код дохода (Графы 11)
            if (ndflPersonDeduction.incomeCode != ndflPersonIncome.incomeCode) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION'. Строка='${ndflPersonDeduction.rowNum ?: ""}'. 'Начисленный доход.Код дохода (Графа 11)'='${ndflPersonDeduction.incomeCode}' $fioAndInp." +
                        " Текст ошибки: 'Начисленный доход.Код дохода (Графа 11)'='${ndflPersonDeduction.incomeCode}' ≠ 'Доход.Вид.Код (Графа 4)' = ${ndflPersonIncome.incomeCode}'.")
            }

            // Выч18 Начисленный доход.Сумма (Графы 12)
            if (!comparNumbEquals(ndflPersonDeduction.incomeSumm, ndflPersonIncome.incomeAccruedSumm)) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION'. Строка='${ndflPersonDeduction.rowNum ?: ""}'. Начисленный доход.Сумма (Графа 12)='${ndflPersonDeduction.incomeSumm}' $fioAndInp." +
                        " Текст ошибки: 'Начисленный доход.Сумма (Графа 12)'= ${ndflPersonDeduction.incomeSumm}' ≠ 'Доход.Сумма.Начисление (Графа 10)'= ${ndflPersonIncome.incomeAccruedSumm}'.")
            }
        }

        // Выч20 Применение вычета.Текущий период.Дата (Графы 15)
        if (ndflPersonDeduction.periodCurrDate != ndflPersonDeduction.incomeAccrued) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION'. Строка='${ndflPersonDeduction.rowNum ?: ''}'. Применение вычета.Текущий период.Дата (Графа 15)='${ndflPersonDeduction.incomeAccrued}' $fioAndInp." +
                    " Текст ошибки: 'Применение вычета.Текущий период.Дата (Графа 15)'='${ndflPersonDeduction.periodCurrDate}' ≠ 'Начисленный доход.Дата (Графа 10)'='${ndflPersonDeduction.incomeAccrued}'.")
        }

        // Выч21 Документ о праве на налоговый вычет.Сумма (Графы 16) (Графы 8)
        if (comparNumbGreater(ndflPersonDeduction.notifSumm ?: 0, ndflPersonDeduction.periodCurrSumm ?: 0)) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            logger.warn("Ошибка в значении: Раздел '$T_PERSON_DEDUCTION'. Строка='${ndflPersonDeduction.rowNum ?: ""}'. Документ о праве на налоговый вычет.Сумма (Графа 8)='${ndflPersonDeduction.notifSumm}' $fioAndInp." +
                    " Текст ошибки: 'Документ о праве на налоговый вычет.Сумма (Графа 8)'='${ndflPersonDeduction.notifSumm}' > 'Применение вычета.Текущий период.Сумма (Графа 16)'='${ndflPersonDeduction.periodCurrSumm}'.")
        }
    }
}

/**
 * Сравнение чисел с плавающей точкой через эпсилон-окрестности
 */
boolean comparNumbEquals(def d1, def d2) {
    return (Math.abs(d1 - d2) < 0.001)
}
boolean comparNumbGreater(double d1, double d2) {
    return (d1 - d2 > 0.001)
}

//>------------------< CHECK DATA UTILS >----------------------<

/**
 * Получить параметры для конкретного тербанка
 * @return
 */
def getRefBookNdfl() {
    def departmentId = declarationData.departmentId
    def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
def getRefBookNdflDetail(def departmentParamId) {
    def mapNdflDetail = [:]
    def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
    def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }
    def kppList = []
    def mapOktmo = getRefOktmoByDepartmentId()
    departmentParamTableList.each { departmentParamTable ->

        String oktmoCode = mapOktmo.get(departmentParamTable?.OKTMO?.referenceValue)?.CODE?.stringValue

        kppList = mapNdflDetail.get(oktmoCode)
        if (kppList == null) {
            kppList = []
        }

        if (!kppList.contains(departmentParamTable?.KPP?.stringValue)) {
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(oktmoCode, kppList)
        }
    }
    return mapNdflDetail
}

/**
 * Получить "ОКТМО"
 */
def getRefOktmoByDepartmentId() {
    String whereClause = """
        JOIN REF_BOOK_NDFL_DETAIL nd ON (frb.id = nd.OKTMO)
        JOIN REF_BOOK_NDFL n ON (n.DEPARTMENT_ID = ${declarationData.departmentId} AND nd.REF_BOOK_NDFL_ID = n.ID)
    """
    return getRefBookByRecordVersionWhere(RefBook.Id.OKTMO.id, whereClause, getReportPeriodEndDate() - 1)
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
boolean isExistsAddress(ndflPersonId) {
    Map<Long, Long> checkFiasAddressMap = getFiasAddressIdsMap();
    return (checkFiasAddressMap.get(ndflPersonId) != null)
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


//TODO вынес handler в скрипт, чтобы не обновлять ядро на нексте

/**
 * @author Andrey Drunk
 */
public class NaturalPersonRefbookScriptHandler extends NaturalPersonRefbookHandler {

    /**
     *
     */
    private Map<Long, NaturalPerson> refbookPersonTempMap;

    /**
     * Карта для создания идкнтификаторов ФЛ
     */
    private Map<Long, Map<Long, PersonIdentifier>> identitiesMap;

    /**
     * Карта для создания документов ФЛ
     */
    private Map<Long, Map<Long, PersonDocument>> documentsMap;

    /**
     * Кэш справочника страны
     */
    private Map<Long, Country> countryMap;

    /**
     * Кэш справочника статусы Налогоплателищика
     */
    private Map<Long, TaxpayerStatus> taxpayerStatusMap;

    /**
     * Кэш справочника типы документов
     */
    private Map<Long, DocType> docTypeMap;

    /**
     *
     */
    public NaturalPersonRefbookScriptHandler() {
        super();
        refbookPersonTempMap = new HashMap<Long, NaturalPerson>();
        identitiesMap = new HashMap<Long, Map<Long, PersonIdentifier>>();
        documentsMap = new HashMap<Long, Map<Long, PersonDocument>>();
    }

    @Override
    public void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException {

        //Идентификатор записи первичной формы
        Long primaryPersonId = SqlUtils.getLong(rs, PRIMARY_PERSON_ID);

        //if (primaryPersonId == null) {throw new ServiceException("Не задано значение PRIMARY_PERSON_ID");}

        //Список сходных записей
        Map<Long, NaturalPerson> similarityPersonMap = map.get(primaryPersonId);

        if (similarityPersonMap == null) {
            similarityPersonMap = new HashMap<Long, NaturalPerson>();
            map.put(primaryPersonId, similarityPersonMap);
        }

        //Идентификатор справочника
        Long refBookPersonId = SqlUtils.getLong(rs, REFBOOK_PERSON_ID);

        NaturalPerson naturalPerson = similarityPersonMap.get(refBookPersonId);

        if (naturalPerson == null) {
            naturalPerson = buildNaturalPerson(rs, refBookPersonId, primaryPersonId);
            similarityPersonMap.put(refBookPersonId, naturalPerson);
        }


        //Добавляем документы физлица
        addPersonDocument(rs, naturalPerson);

        //Добавляем идентификаторы
        addPersonIdentifier(rs, naturalPerson);

        //Адрес
        Address address = buildAddress(rs);
        naturalPerson.setAddress(address);

        //System.out.println(rowNum + ", primaryPersonId=" + primaryPersonId + ", [" + naturalPerson + "][" + Arrays.toString(naturalPerson.getPersonDocumentList().toArray()) + "][" + Arrays.toString(naturalPerson.getPersonIdentityList().toArray()) + "][" + address + "]");

    }

    private void addPersonIdentifier(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {

        Long primaryPersonId = naturalPerson.getPrimaryPersonId();
        Long refBookPersonId = naturalPerson.getId();
        Long personIdentifierId = SqlUtils.getLong(rs, "book_id_tax_payer_id");
        Map<Long, PersonIdentifier> personIdentityMap = identitiesMap.get(refBookPersonId);

        if (personIdentityMap == null) {
            personIdentityMap = new HashMap<Long, PersonIdentifier>();
            identitiesMap.put(refBookPersonId, personIdentityMap);
        }

        if (personIdentifierId != null && !personIdentityMap.containsKey(personIdentifierId)) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setId(personIdentifierId);

            personIdentifier.setRecordId(SqlUtils.getLong(rs, "tax_record_id"));
            personIdentifier.setStatus(SqlUtils.getInteger(rs, "tax_status"));
            personIdentifier.setVersion(rs.getDate("tax_version"));

            personIdentifier.setInp(rs.getString("inp"));
            personIdentifier.setAsnuId(SqlUtils.getLong(rs, "as_nu"));
            personIdentifier.setNaturalPerson(naturalPerson);
            personIdentityMap.put(personIdentifierId, personIdentifier);
            naturalPerson.getPersonIdentityList().add(personIdentifier);
        }
    }

    private void addPersonDocument(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
        Long primaryPersonId = naturalPerson.getPrimaryPersonId();
        Long refBookPersonId = naturalPerson.getId();
        Long docId = SqlUtils.getLong(rs, "ref_book_id_doc_id");
        Map<Long, PersonDocument> pesonDocumentMap = documentsMap.get(refBookPersonId);

        if (pesonDocumentMap == null) {
            pesonDocumentMap = new HashMap<Long, PersonDocument>();
            documentsMap.put(refBookPersonId, pesonDocumentMap);
        }

        if (docId != null && !pesonDocumentMap.containsKey(docId)) {
            Long docTypeId = SqlUtils.getLong(rs, "doc_id");
            DocType docType = getDocTypeById(docTypeId);
            PersonDocument personDocument = new PersonDocument();
            personDocument.setId(docId);

            personDocument.setRecordId(SqlUtils.getLong(rs, "doc_record_id"));
            personDocument.setStatus(SqlUtils.getInteger(rs, "doc_status"));
            personDocument.setVersion(rs.getDate("doc_version"));

            personDocument.setDocType(docType);
            personDocument.setDocumentNumber(rs.getString("doc_number"));
            personDocument.setIncRep(SqlUtils.getInteger(rs, "inc_rep"));
            personDocument.setNaturalPerson(naturalPerson);
            pesonDocumentMap.put(docId, personDocument);
            naturalPerson.getPersonDocumentList().add(personDocument);
        }
    }

    private NaturalPerson buildNaturalPerson(ResultSet rs, Long refBookPersonId, Long primaryPersonId) throws SQLException {
        NaturalPerson naturalPerson = refbookPersonTempMap.get(refBookPersonId);
        if (naturalPerson != null) {
            return naturalPerson;
        } else {

            NaturalPerson person = new NaturalPerson();

            //person
            person.setId(refBookPersonId);

            //TODO Разделить модель на два класса NaturalPerson для представления данных первичной формы и данных справочника
            //person.setPrimaryPersonId(primaryPersonId);

            person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
            person.setStatus(SqlUtils.getInteger(rs, "person_status"));
            person.setVersion(rs.getDate("person_version"));

            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setSex(SqlUtils.getInteger(rs, "sex"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerStatus(getTaxpayerStatusById(SqlUtils.getLong(rs, "taxpayer_state")));
            person.setCitizenship(getCountryById(SqlUtils.getLong(rs, "citizenship")));

            //additional
            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "medical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));
            person.setSourceId(SqlUtils.getLong(rs, "source_id"));
            person.setRecordId(SqlUtils.getLong(rs, "record_id"));

            refbookPersonTempMap.put(refBookPersonId, person);

            return person;
        }


    }

    private Address buildAddress(ResultSet rs) throws SQLException {
        Long addrId = SqlUtils.getLong(rs, "REF_BOOK_ADDRESS_ID");
        if (addrId != null) {
            Address address = new Address();

            address.setId(addrId);
            address.setRecordId(SqlUtils.getLong(rs, "addr_record_id"));
            address.setStatus(SqlUtils.getInteger(rs, "addr_status"));
            address.setVersion(rs.getDate("addr_version"));

            address.setAddressType(SqlUtils.getInteger(rs, "address_type"));
            address.setCountry(getCountryById(SqlUtils.getLong(rs, "country_id")));
            address.setRegionCode(rs.getString("region_code"));
            address.setPostalCode(rs.getString("postal_code"));
            address.setDistrict(rs.getString("district"));
            address.setCity(rs.getString("city"));
            address.setLocality(rs.getString("locality"));
            address.setStreet(rs.getString("street"));
            address.setHouse(rs.getString("house"));
            address.setBuild(rs.getString("build"));
            address.setAppartment(rs.getString("appartment"));
            address.setAddressIno(rs.getString("address"));
            return address;
        } else {
            return null;
        }
    }

    public Map<Long, Country> getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }

    public Map<Long, TaxpayerStatus> getTaxpayerStatusMap() {
        return taxpayerStatusMap;
    }

    public void setTaxpayerStatusMap(Map<Long, TaxpayerStatus> taxpayerStatusMap) {
        this.taxpayerStatusMap = taxpayerStatusMap;
    }

    public Map<Long, DocType> getDocTypeMap() {
        return docTypeMap;
    }

    public void setDocTypeMap(Map<Long, DocType> docTypeMap) {
        this.docTypeMap = docTypeMap;
    }

    private TaxpayerStatus getTaxpayerStatusById(Long taxpayerStatusId) {
        if (taxpayerStatusId != null) {
            return taxpayerStatusMap != null ? taxpayerStatusMap.get(taxpayerStatusId) : new TaxpayerStatus(taxpayerStatusId, null);
        } else {
            return null;
        }
    }

    private Country getCountryById(Long countryId) {
        if (countryId != null) {
            return countryMap != null ? countryMap.get(countryId) : new Country(countryId, null);
        } else {
            return null;
        }
    }

    private DocType getDocTypeById(Long docTypeId) {
        if (docTypeId != null) {
            return docTypeMap != null ? docTypeMap.get(docTypeId) : new DocType(docTypeId, null);
        } else {
            return null;
        }
    }


}
