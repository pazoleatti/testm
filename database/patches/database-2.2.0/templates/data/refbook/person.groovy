package refbook.person // person_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import com.aplana.sbrf.taxaccounting.model.IdentityObject
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType.NUMBER
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType.REFERENCE
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType.STRING
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted

/**
 * Cкрипт справочника "Физические лица" (id = 904).
 * ref_book_id = 904
 */

(new Person(this)).run()


@SuppressWarnings("GrMethodMayBeStatic")
class Person extends AbstractScriptClass {

    Long sourceUniqueRecordId
    Long uniqueRecordId
    Boolean isNewRecords
    Date validDateFrom
    RefBookFactory refBookFactory
    RefBookAsnuService refBookAsnuService
    InputStream inputStream

    @TypeChecked(TypeCheckingMode.SKIP)
    Person(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("sourceUniqueRecordId")) {
            this.sourceUniqueRecordId = (Long) scriptClass.getBinding().getProperty("sourceUniqueRecordId")
        }
        if (scriptClass.getBinding().hasVariable("uniqueRecordId")) {
            this.uniqueRecordId = (Long) scriptClass.getBinding().getProperty("uniqueRecordId")
        }
        if (scriptClass.getBinding().hasVariable("isNewRecords")) {
            this.isNewRecords = (Boolean) scriptClass.getBinding().getProperty("isNewRecords")
        }
        if (scriptClass.getBinding().hasVariable("validDateFrom")) {
            this.validDateFrom = (Date) scriptClass.getBinding().getProperty("validDateFrom")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("inputStream")) {
            this.inputStream = (InputStream) scriptClass.getProperty("inputStream")
        }
        if (scriptClass.getBinding().hasVariable("refBookAsnuService")) {
            this.refBookAsnuService = (RefBookAsnuService) scriptClass.getProperty("refBookAsnuService")
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.SAVE:
                save()
                break
            case FormDataEvent.IMPORT:
                importData()
                break
        }
    }

    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]
    // Кэш код страны - страна
    Map<String, Country> countriesCache = new HashMap<>()
    // Кэш код статуса налогоплательщика - сам статус
    Map<String, TaxpayerStatus> taxpayerStatusessCache = new HashMap<>()
    // Кэш код типа документа - сам тип
    Map<String, DocType> docTypesCache = new HashMap<>()
    // Кэш АСНУ
    Map<String, RefBookAsnu> asnuCache = new HashMap<>()
    // Документ, удостоверяющий личность (ДУЛ)
    final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id
    // ИНП
    final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id
    // Дата новых версии при импорте
    Date versionFrom = new GregorianCalendar(1990, Calendar.JANUARY, 1).time
    String versionFromString = versionFrom.format("dd.MM.yyyy")
    // Количество блоков ".Файл.ИнфЧасть" в xml при импорте
    int infPartCount = 0

    void importData() {
        List<NaturalPerson> parsedPersons = []

        parseXml(parsedPersons)
        def validPersons = validatePersons(parsedPersons)

        if (!logger.containsLevel(LogLevel.ERROR)) {
            save(validPersons)
            for (def person : validPersons) {
                logger.info("Создана новая запись в справочнике Физические лица: \"${person.id}\", " +
                        "\"${person.lastName}\" \"${person.firstName}\" \"${person.middleName}\", \"${versionFromString}\"")
            }
            logger.info("Обработано записей из файла ${infPartCount}. Удалось загрузить ${validPersons.size()} записей")
        }
    }

    void parseXml(List<NaturalPerson> persons) {
        XMLStreamReader reader = null
        try {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
            reader = xmlFactory.createXMLStreamReader(inputStream)

            NaturalPerson person = null

            boolean inInfoPart = false
            String idFL
            while (reader.hasNext()) {
                if (reader.startElement) {
                    String tag = reader.name.localPart
                    if ('ИнфЧасть' == tag) {
                        checkInterrupted()
                        inInfoPart = true
                        idFL = getAttrValue(reader, "ИдФЛ")
                        person = new NaturalPerson()
                    } else if (inInfoPart) {
                        if ('АнкетДаннФЛ' == tag) {
                            parsePersonInfo(reader, idFL, person)
                        } else if ('УдЛичнФЛ' == tag) {
                            person.personDocumentList.add(parseDocumentInfo(reader, person))
                        } else if ('СисИсточ' == tag) {
                            person.personIdentityList.add(parsePersonIdentifier(reader, person))
                        }
                    }
                } else if (reader.endElement) {
                    String tag = reader.name.localPart
                    if ('ИнфЧасть' == tag) {
                        inInfoPart = false
                        infPartCount++
                        person.setSource(getMaxPriorityAsnu(person.personIdentityList))
                        persons.add(person)
                    }
                }
                reader.next()
            }
        } finally {
            reader?.close()
        }
    }

    void parsePersonInfo(XMLStreamReader reader, String idFL, NaturalPerson person) {
        person.lastName = getAttrValue(reader, "ФамФЛ")
        person.firstName = getAttrValue(reader, "ИмяФЛ")
        person.middleName = getAttrValue(reader, "ОтчФЛ")
        person.birthDate = toDate(getAttrValue(reader, "ДатаРожд"))
        person.citizenship = getCountryByCode(getAttrValue(reader, "Гражд"))
        person.inn = getAttrValue(reader, "ИННФЛ")
        person.innForeign = getAttrValue(reader, "ИННИно")
        person.taxPayerStatus = getTaxpayerStatusByCode(getAttrValue(reader, "СтатусФЛ"))
        person.address = parseAddress(reader, idFL, person)
    }

    Address parseAddress(XMLStreamReader reader, String idFL, NaturalPerson person) {
        Address address = new Address()
        address.regionCode = getAttrValue(reader, "КодРегион")
        address.postalCode = getAttrValue(reader, "Индекс")
        address.district = truncateIfNeeded(getAttrValue(reader, "Район"), person, idFL, 100, "Район")
        address.city = truncateIfNeeded(getAttrValue(reader, "Город"), person, idFL, 100, "Город")
        address.locality = truncateIfNeeded(getAttrValue(reader, "НаселПункт"), person, idFL, 100, "НаселПункт")
        address.street = truncateIfNeeded(getAttrValue(reader, "Улица"), person, idFL, 100, "Улица")
        address.house = getAttrValue(reader, "Дом")
        address.build = getAttrValue(reader, "Корпус")
        address.appartment = getAttrValue(reader, "Кварт")
        address.country = getCountryByCode(getAttrValue(reader, "КодСтрИно"))
        address.addressIno = getAttrValue(reader, "АдресИно")
        return address
    }

    PersonDocument parseDocumentInfo(XMLStreamReader reader, NaturalPerson person) {
        PersonDocument document = new PersonDocument()
        document.docType = getDocTypeByCode(getAttrValue(reader, "УдЛичнФЛВид"))
        document.documentNumber = getAttrValue(reader, "УдЛичнФЛНом")
        document.incRep = toInteger(getAttrValue(reader, "УдЛичнФЛГл"))
        document.naturalPerson = person
        return document
    }

    PersonIdentifier parsePersonIdentifier(XMLStreamReader reader, NaturalPerson person) {
        PersonIdentifier personIdentifier = new PersonIdentifier()
        personIdentifier.asnu = getAsnuByName(getAttrValue(reader, "СисИсточНам"))
        personIdentifier.inp = getAttrValue(reader, "СисИсточИНП")
        personIdentifier.naturalPerson = person
        return personIdentifier
    }

    RefBookAsnu getMaxPriorityAsnu(List<PersonIdentifier> personIdentifiers) {
        def maxPriorityAsnu = null
        for (def personIdentifier : personIdentifiers) {
            if (!maxPriorityAsnu || personIdentifier.asnu?.priority > maxPriorityAsnu.priority) {
                maxPriorityAsnu = personIdentifier.asnu
            }
        }
        return maxPriorityAsnu
    }

    String getAttrValue(XMLStreamReader reader, String attrName) {
        return reader?.getAttributeValue(null, attrName)
    }

    Date toDate(String value) {
        if (value) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy")
            return formatter.parse(value)
        }
        return null
    }

    Integer toInteger(String value) {
        if (value) {
            return Integer.valueOf(value)
        }
        return null
    }

    void save(List<NaturalPerson> persons) {
        if (persons) {
            List<Address> addresses = []
            List<PersonDocument> documents = []
            List<PersonIdentifier> identifiers = []

            for (def person : persons) {
                Address address = person.getAddress()
                if (address != null) {
                    addresses.add(person.address)
                }

                PersonDocument document = person.getPersonDocument()
                if (document != null) {
                    documents.addAll(person.personDocumentList)
                }

                PersonIdentifier identifier = person.getPersonIdentifier()
                if (identifier != null) {
                    identifiers.addAll(person.personIdentityList)
                }
            }

            insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addresses, this.&mapAddressAttr)

            insertBatchRecords(RefBook.Id.PERSON.getId(), persons, this.&mapPersonAttr)

            insertBatchRecords(RefBook.Id.ID_DOC.getId(), documents, this.&mapPersonDocumentAttr)

            def validIdentifiers = checkPersonIdentifiers(identifiers)
            insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), validIdentifiers, this.&mapPersonIdentifierAttr)
        }
    }

    List<NaturalPerson> validatePersons(List<NaturalPerson> persons) {
        List<NaturalPerson> validPersons = []
        for (def person : persons) {
            List<String> errors = [] // фатальные (локальные), т.е. не будут созданы записи
            addIfNotNull(errors, ScriptUtils.checkName(person.lastName, "Фамилия"))
            addIfNotNull(errors, ScriptUtils.checkName(person.firstName, "Имя"))
            warnIfNotNull(ScriptUtils.checkInn(person.inn))
            for (def document : person.personDocumentList) {
                if (document.docType) {
                    addIfNotNull(errors, ScriptUtils.checkDul(document.docType.getCode(), document.documentNumber, "ДУЛ Номер"))
                }
            }
            if (!errors) {
                validPersons.add(person)
            } else {
                if (1 == errors.size()) {
                    logger.warn("Не удалось создать запись \"${person.lastName}\" \"${person.firstName}\" \"${person.middleName}\"" +
                            ", \"${person.majorDocument?.documentNumber}\", \"${person.source?.name}\". " +
                            "Не пройдены проверки: " + errors[0])
                } else {
                    logger.warn("Не удалось создать запись \"${person.lastName}\" \"${person.firstName}\" \"${person.middleName}\"" +
                            ", \"${person.majorDocument?.documentNumber}\", \"${person.source?.name}\". " +
                            "Не пройдены проверки:")
                    for (int i = 0; i < errors.size(); i++) {
                        logger.warn("${i + 1}. ${errors[i]}")
                    }
                }
            }
        }
        return validPersons
    }

    void addIfNotNull(Collection collection, Object object) {
        if (object) {
            collection.add(object)
        }
    }

    void warnIfNotNull(String message) {
        if (message) {
            logger.warn(message)
        }
    }

    List<PersonIdentifier> checkPersonIdentifiers(List<PersonIdentifier> identifiers) {
        List<PersonIdentifier> validIdentifiers = []
        for (def identifier : identifiers) {
            if (identifier.inp) {
                validIdentifiers.add(identifier)
            } else {
                def person = identifier.naturalPerson
                logger.warn("Не удалось создать запись об идентификаторе налогоплательщика у записи  \"${person.id}\", " +
                        "\"${person.lastName}\" \"${person.firstName}\" \"${person.middleName}\", \"${versionFromString}\". Причина: отсутствует \"ИНП\".")
                identifier.naturalPerson.personIdentityList.remove(identifier)
            }
        }
        return validIdentifiers
    }

    void insertBatchRecords(Long refBookId, List<? extends IdentityObject> identityObjects, Closure refBookMapper) {
        if (identityObjects) {
            String refBookName = getProvider(refBookId).refBook.name
            logForDebug("Добавление записей: cправочник «${refBookName}», количество ${identityObjects.size()}")

            identityObjects.collate(1000).each { identityObjectSubList ->
                checkInterrupted()
                if (identityObjectSubList != null && !identityObjectSubList.isEmpty()) {

                    List<RefBookRecord> recordList = []
                    for (IdentityObject identityObject : identityObjectSubList) {
                        checkInterrupted()

                        Map<String, RefBookValue> values = refBookMapper(identityObject)
                        recordList.add(createRefBookRecord(values))
                    }

                    //создание записей справочника
                    List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, versionFrom, null, recordList)

                    //установка id
                    for (int i = 0; i < identityObjectSubList.size(); i++) {
                        checkInterrupted()

                        Long id = generatedIds.get(i)
                        IdentityObject identityObject = identityObjectSubList.get(i)
                        identityObject.setId(id)
                    }
                }
            }
        }
    }

    Map<String, RefBookValue> mapAddressAttr(Address address) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "ADDRESS_TYPE", NUMBER, address.getAddressType() ?: 0)
        putValue(values, "COUNTRY_ID", REFERENCE, address.getCountry()?.getId())
        putValue(values, "REGION_CODE", STRING, address.getRegionCode())
        putValue(values, "DISTRICT", STRING, address.getDistrict())
        putValue(values, "CITY", STRING, address.getCity())
        putValue(values, "LOCALITY", STRING, address.getLocality())
        putValue(values, "STREET", STRING, address.getStreet())
        putValue(values, "HOUSE", STRING, address.getHouse())
        putValue(values, "BUILD", STRING, address.getBuild())
        putValue(values, "APPARTMENT", STRING, address.getAppartment())
        putValue(values, "POSTAL_CODE", STRING, address.getPostalCode())
        putValue(values, "ADDRESS", STRING, address.getAddressIno())
        return values
    }

    Map<String, RefBookValue> mapPersonAttr(NaturalPerson person) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "LAST_NAME", STRING, person.getLastName())
        putValue(values, "FIRST_NAME", STRING, person.getFirstName())
        putValue(values, "MIDDLE_NAME", STRING, person.getMiddleName())
        putValue(values, "INN", STRING, person.getInn())
        putValue(values, "INN_FOREIGN", STRING, person.getInnForeign())
        putValue(values, "SNILS", STRING, person.getSnils())
        putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate())
        putValue(values, "BIRTH_PLACE", STRING, null)
        putValue(values, "ADDRESS", REFERENCE, person.getAddress()?.getId())
        putValue(values, "EMPLOYEE", NUMBER, person.getEmployee() ?: 2)
        putValue(values, "CITIZENSHIP", REFERENCE, person.getCitizenship()?.getId())
        putValue(values, "TAXPAYER_STATE", REFERENCE, person.getTaxPayerStatus()?.getId())
        putValue(values, "SOURCE_ID", REFERENCE, person.source?.id)
        return values
    }

    Map<String, RefBookValue> mapPersonDocumentAttr(PersonDocument personDocument) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "PERSON_ID", REFERENCE, personDocument.getNaturalPerson().getId())
        putValue(values, "DOC_NUMBER", STRING, personDocument.getDocumentNumber())
        def incRepVal = personDocument.getIncRep() != null ? personDocument.getIncRep() : 1
        putValue(values, "INC_REP", NUMBER, incRepVal)
        putValue(values, "DOC_ID", REFERENCE, personDocument.getDocType()?.getId())
        return values
    }

    Map<String, RefBookValue> mapPersonIdentifierAttr(PersonIdentifier personIdentifier) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "PERSON_ID", REFERENCE, personIdentifier.getNaturalPerson().getId())
        putValue(values, "INP", STRING, personIdentifier.getInp())
        putValue(values, "AS_NU", REFERENCE, personIdentifier.asnu.id)
        return values
    }

    void putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
        values.put(attrName, new RefBookValue(type, value))
    }

    String truncateIfNeeded(String stringToTruncate, NaturalPerson personRecord, String idFL, int maxLength, String fieldName) {
        if (stringToTruncate && stringToTruncate.length() > maxLength) {
            logger.warn("\"${fieldName}\" превышает допустимые размеры поля. Значение обрезано. Запись ${idFL}, ${personRecord.lastName} " +
                    "${personRecord.firstName} ${personRecord.middleName}, ${personRecord.birthDate.format("dd.MM.yyyy")}. Причина: \"Превышено допустимое значение в поле\"")
            return stringToTruncate.substring(0, maxLength)
        }
        return stringToTruncate
    }

    /**
     * Создание новой записи справочника
     */
    RefBookRecord createRefBookRecord(Map<String, RefBookValue> values) {
        RefBookRecord record = new RefBookRecord()
        putValue(values, "RECORD_ID", NUMBER, null)
        record.setValues(values)
        return record
    }

    /**
     * Создадим дубли ДУЛ и ИНП с привязкой к новой записи
     */
    void save() {

        /*
        sourceUniqueRecordId - идентификатор старой записи
        uniqueRecordId - идентификатор новой записи
        isNewRecords - признак новой записи
         */
        if (sourceUniqueRecordId && uniqueRecordId && isNewRecords) {

            // Перенесем ДУЛ из старой версии в новую
            PagingResult<Map<String, RefBookValue>> oldRefDulList = getRefDul(sourceUniqueRecordId)
            List<RefBookRecord> newRefDulList = new ArrayList<RefBookRecord>()
            if (oldRefDulList && !oldRefDulList.isEmpty()) {
                oldRefDulList.each { Map<String, RefBookValue> oldRefDul ->
                    newRefDulList.add(createIdentityDocRecord(oldRefDul))
                }
            }
            if (!newRefDulList.isEmpty()) {
                List<Long> docIds = getProvider(REF_BOOK_ID_DOC_ID).createRecordVersionWithoutLock(logger, validDateFrom, null, newRefDulList)
                logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size())
            }

            // Перенесем ИНП из старой версии в новую
            PagingResult<Map<String, RefBookValue>> oldRefInpList = getRefInp(sourceUniqueRecordId)
            List<RefBookRecord> newRefInpList = new ArrayList<RefBookRecord>()
            if (oldRefInpList && !oldRefInpList.isEmpty()) {
                oldRefInpList.each { Map<String, RefBookValue> oldRefInp ->
                    newRefInpList.add(createIdentityTaxpayerRecord(oldRefInp))
                }
            }
            if (!newRefInpList.isEmpty()) {
                List<Long> docIds = getProvider(REF_BOOK_ID_TAX_PAYER_ID).createRecordVersionWithoutLock(logger, validDateFrom, null, newRefInpList)
                logger.info("В справочнике 'Документы физических лиц' создано записей: " + docIds.size())
            }
        }
    }

    /**
     * Документы, удостоверяющие личность
     */
    RefBookRecord createIdentityDocRecord(Map<String, RefBookValue> oldRefDul) {
        RefBookRecord record = new RefBookRecord()
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        fillIdentityDocAttr(values, oldRefDul)
        record.setValues(values)
        return record
    }

    /**
     * Идентификаторы физлиц
     */
    RefBookRecord createIdentityTaxpayerRecord(Map<String, RefBookValue> oldRefInp) {
        RefBookRecord record = new RefBookRecord()
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        fillIdentityTaxpayerRecord(values, oldRefInp)
        record.setValues(values)
        return record
    }

    /**
     * Заполнение аттрибутов справочника документов
     * @param values карта для хранения значений атрибутов
     * @param person класс предоставляющий данные для заполнения справочника
     * @return
     */
    def fillIdentityDocAttr(Map<String, RefBookValue> values, Map<String, RefBookValue> oldRefDul) {
        putOrUpdate(values, "PERSON_ID", REFERENCE, uniqueRecordId)
        putOrUpdate(values, "DOC_NUMBER", STRING, oldRefDul?.DOC_NUMBER?.stringValue)
        putOrUpdate(values, "ISSUED_BY", STRING, oldRefDul?.ISSUED_BY?.stringValue)
        putOrUpdate(values, "ISSUED_DATE", RefBookAttributeType.DATE, oldRefDul?.ISSUED_DATE?.dateValue)
        //Признак включения в отчет, при создании ставиться 1, при обновлении надо выбрать с минимальным приоритетом
        putOrUpdate(values, "INC_REP", NUMBER, 1)
        putOrUpdate(values, "DOC_ID", REFERENCE, oldRefDul?.DOC_ID?.referenceValue)
    }

    /**
     * Заполнение аттрибутов справочника Идентификаторы физлиц
     * @param person
     * @param asnuId
     * @return
     */
    def fillIdentityTaxpayerRecord(Map<String, RefBookValue> values, Map<String, RefBookValue> oldRefInp) {
        putOrUpdate(values, "PERSON_ID", REFERENCE, uniqueRecordId)
        putOrUpdate(values, "INP", STRING, oldRefInp?.INP?.stringValue)
        putOrUpdate(values, "AS_NU", REFERENCE, oldRefInp?.AS_NU?.referenceValue)
    }

    /**
     * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
     * @return 0 - изменений нет, 1-создание записи, 2 - обновление
     */
    void putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value) {
        RefBookValue refBookValue = valuesMap.get(attrName)
        if (refBookValue != null) {
            //обновление записи, если новое значение задано и отличается от существующего
            Object currentValue = refBookValue.getValue()
            if (value != null && !ScriptUtils.equalsNullSafe(currentValue, value)) {
                //значения не равны, обновление
                refBookValue.setValue(value)
            }
        } else {
            //создание новой записи
            valuesMap.put(attrName, new RefBookValue(type, value))
        }
    }

    /************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

    /**
     * Получить все записи справочника по его идентификатору и фильтру (отсутствие значений не является ошибкой)
     * @param refBookId - идентификатор справочника
     * @param filter - фильтр
     * @return - возвращает лист
     */
    PagingResult<Map<String, RefBookValue>> getRefBookByFilter(long refBookId, String filter) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecords(null, null, filter, null)
        return refBookList
    }

    /**
     * Получить "Документ, удостоверяющий личность (ДУЛ)"
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRefDul(long personId) {
        return getRefBookByFilter(REF_BOOK_ID_DOC_ID, "PERSON_ID = " + personId)
    }

    /**
     * Получить "ИНП"
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRefInp(long personId) {
        return getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId)
    }

    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(Long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    /**
     * Возвращяет страну по коду
     */
    Country getCountryByCode(String code) {
        Country country = null
        if (code) {
            country = countriesCache.get(code)
            if (!country) {
                def recordData = getProvider(RefBook.Id.COUNTRY.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    country = new Country()
                    country.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    country.setCode(record.get("CODE").getStringValue())
                    countriesCache.put(code, country)
                }
            }
            if (!country) {
                logger.warn("Не найдена страна по коду \"${code}\"")
            }
        }
        return country
    }

    /**
     * Возвращяет статус налогоплательщика по коду
     */
    TaxpayerStatus getTaxpayerStatusByCode(String code) {
        TaxpayerStatus taxpayerStatus = null
        if (code) {
            taxpayerStatus = taxpayerStatusessCache.get(code)
            if (!taxpayerStatus) {
                def recordData = getProvider(RefBook.Id.TAXPAYER_STATUS.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    taxpayerStatus = new TaxpayerStatus()
                    taxpayerStatus.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    taxpayerStatus.setCode(record.get("CODE").getStringValue())
                    taxpayerStatusessCache.put(code, taxpayerStatus)
                }
            }
            if (!taxpayerStatus) {
                logger.warn("Не найден статус налогоплательщика по коду \"${code}\"")
            }
        }
        return taxpayerStatus
    }

    /**
     * Возвращяет вид документа по коду
     */
    DocType getDocTypeByCode(String code) {
        DocType docType = null
        if (code) {
            docType = docTypesCache.get(code)
            if (!docType) {
                def recordData = getProvider(RefBook.Id.DOCUMENT_CODES.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    docType = new DocType()
                    docType.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    docType.setCode(record.get("CODE").getStringValue())
                    docTypesCache.put(code, docType)
                }
            }
            if (!docType) {
                logger.warn("Не найден вид документа по коду \"${code}\"")
            }
        }
        return docType
    }

    /**
     * Возвращяет АСНУ по наименованию
     */
    RefBookAsnu getAsnuByName(String name) {
        RefBookAsnu asnu = null
        if (name) {
            asnu = asnuCache.get(name)
            if (!asnu) {
                asnu = refBookAsnuService.fetchByName(name)
                asnuCache.put(name, asnu)
            }
            if (!asnu) {
                logger.error("Не найден АСНУ по наименованию \"${name}\"")
            }
        }
        return asnu
    }
}