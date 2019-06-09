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
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.formatDate

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
    Date versionFrom = new GregorianCalendar(2017, Calendar.JANUARY, 1).time
    // Количество блоков ".Файл.ИнфЧасть" в xml при импорте
    int infPartCount = 0

    void importData() {
        List<NaturalPerson> parsedPersons = []

        parseXml(parsedPersons)
        def savedPersons = save(parsedPersons)
        checkPersons(savedPersons)
        for (def person : savedPersons) {
            logger.info("Создана новая запись в справочнике Физические лица: " +
                    "$person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}")
        }
        logger.info("Обработано записей из файла ${infPartCount}. В справочнике ФЛ создано ${savedPersons.size()} записей")
    }

    void parseXml(List<NaturalPerson> persons) {
        XMLStreamReader reader = null
        try {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
            reader = xmlFactory.createXMLStreamReader(inputStream)

            NaturalPersonExt person = null

            boolean inInfoPart = false
            while (reader.hasNext()) {
                if (reader.startElement) {
                    String tag = reader.name.localPart
                    if ('ИнфЧасть' == tag) {
                        checkInterrupted()
                        inInfoPart = true
                        person = new NaturalPersonExt()
                    } else if (inInfoPart) {
                        if ('АнкетДаннФЛ' == tag) {
                            parsePersonInfo(reader, person)
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

    void parsePersonInfo(XMLStreamReader reader, NaturalPersonExt person) {
        person.lastName = getAttrValue(reader, "ФамФЛ")
        person.firstName = getAttrValue(reader, "ИмяФЛ")
        person.middleName = getAttrValue(reader, "ОтчФЛ")
        person.birthDateStr = getAttrValue(reader, "ДатаРожд")
        person.birthDate = toDate(person.birthDateStr)
        person.citizenshipCode = getAttrValue(reader, "Гражд")
        person.citizenship = getCountryByCode(person.citizenshipCode)
        person.inn = getAttrValue(reader, "ИННФЛ")
        person.innForeign = getAttrValue(reader, "ИННИно")
        person.taxPayerStatusCode = getAttrValue(reader, "СтатусФЛ")
        person.taxPayerStatus = getTaxpayerStatusByCode(person.taxPayerStatusCode)
        person.address = parseAddress(reader)
    }

    Address parseAddress(XMLStreamReader reader) {
        Address address = new Address()
        address.regionCode = getAttrValue(reader, "КодРегион")
        address.postalCode = getAttrValue(reader, "Индекс")
        address.district = getAttrValue(reader, "Район")
        address.city = getAttrValue(reader, "Город")
        address.locality = getAttrValue(reader, "НаселПункт")
        address.street = getAttrValue(reader, "Улица")
        address.house = getAttrValue(reader, "Дом")
        address.build = getAttrValue(reader, "Корпус")
        address.appartment = getAttrValue(reader, "Кварт")
        address.countryCode = getAttrValue(reader, "КодСтрИно")
        address.country = getCountryByCode(address.countryCode)
        address.addressIno = getAttrValue(reader, "АдресИно")
        // Если присутствует хотя бы один из атрибутов
        if (address.regionCode || address.postalCode || address.district || address.city || address.locality || address.street || address.house ||
                address.build || address.appartment || address.countryCode || address.addressIno) {
            return address
        } else {
            return null
        }
    }

    PersonDocument parseDocumentInfo(XMLStreamReader reader, NaturalPerson person) {
        PersonDocument document = new PersonDocumentExt()
        document.docTypeCode = getAttrValue(reader, "УдЛичнФЛВид")
        document.docType = getDocTypeByCode(document.docTypeCode)
        document.documentNumber = getAttrValue(reader, "УдЛичнФЛНом")
        document.incRepStr = getAttrValue(reader, "УдЛичнФЛГл")
        document.incRep = toInteger(document.incRepStr)
        document.naturalPerson = person
        return document
    }

    PersonIdentifier parsePersonIdentifier(XMLStreamReader reader, NaturalPerson person) {
        PersonIdentifier personIdentifier = new PersonIdentifierExt()
        personIdentifier.asnuName = getAttrValue(reader, "СисИсточНам")
        personIdentifier.asnu = getAsnuByName(personIdentifier.asnuName)
        personIdentifier.inp = getAttrValue(reader, "СисИсточИНП")
        personIdentifier.naturalPerson = person
        return personIdentifier
    }

    /**
     * Определение Системы-источника у ФЛ
     */
    RefBookAsnu getMaxPriorityAsnu(List<PersonIdentifier> identifiers) {
        def maxPriorityAsnu = null
        for (def identifier : identifiers) {
            // 1. Найдена АСНУ в справочнике
            // 2. Из всех найденных записей отбор записей с максимальным значением атрибута "Приоритет".
            // 3. Среди отобранных с максимальным Приоритетом отбор записи с минимальным значением атрибута "Код АСНУ".
            if (identifier.asnu != null &&
                    (maxPriorityAsnu == null || identifier.asnu.priority > maxPriorityAsnu.priority ||
                            identifier.asnu.priority == maxPriorityAsnu.priority && identifier.asnu.code > maxPriorityAsnu.code)
            ) {
                maxPriorityAsnu = identifier.asnu
            }
        }
        return maxPriorityAsnu
    }

    String getAttrValue(XMLStreamReader reader, String attrName) {
        return reader?.getAttributeValue(null, attrName)?.trim()
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

    List<NaturalPerson> save(List<NaturalPerson> persons) {
        if (persons) {
            List<Address> addressesToCreate = []
            List<PersonDocument> documentsToCreate = []
            List<PersonIdentifier> identifiersToCreate = []

            for (def person : persons) {
                person.address != null && addressesToCreate.add(person.address)

                for (def identifier : person.personIdentityList) {
                    if (identifier.asnu != null && identifier.inp) {
                        identifiersToCreate.add(identifier)
                    }
                }

                for (def document : person.personDocumentList) {
                    if (document.docType != null && document.documentNumber) {
                        documentsToCreate.add(document)
                    }
                }
            }

            insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressesToCreate, this.&mapAddressAttr)

            insertBatchRecords(RefBook.Id.PERSON.getId(), persons, this.&mapPersonAttr)

            insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentsToCreate, this.&mapPersonDocumentAttr)

            insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifiersToCreate, this.&mapPersonIdentifierAttr)
        }
        return persons
    }

    /**
     * Обработка ошибочных ситуаций алгоритма
     */
    void checkPersons(List<NaturalPerson> persons) {
        for (def person : persons) {
            // 3.a Ошибки возникшие при создании записей
            for (def identifier : person.personIdentityList) {
                def asnuName = (identifier as PersonIdentifierExt).asnuName
                if (asnuName && identifier.asnu == null) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                            " АСНУ=\"$asnuName\" не найдена запись в справочнике \"АСНУ\".")
                }
            }
            if (person.source == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                        " невозможно определить систему-источник. Атрибут \"Система-источник\" не был заполнен.")
            }
            for (def document : person.personDocumentList) {
                def docTypeCode = (document as PersonDocumentExt).docTypeCode
                if (docTypeCode && document.docType == null) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, \"$document.documentNumber\"" +
                            " указан код ДУЛ ($docTypeCode), отсутствующий в справочнике \"Коды документов\".")
                }
            }
            def taxPayerStatusCode = (person as NaturalPersonExt).taxPayerStatusCode
            if (taxPayerStatusCode && person.taxPayerStatus == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, \"${person.majorDocument?.documentNumber ?: "(документ не определен)"}\"" +
                        " cтатус налогоплательщика ($taxPayerStatusCode) не найден в справочнике \"Статусы налогоплательщика\".")
            }
            def citizenshipCode = (person as NaturalPersonExt).citizenshipCode
            if (citizenshipCode && person.citizenship == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                        " код страны гражданства ($citizenshipCode) не найден в справочнике \"Общероссийский классификатор стран мира\".")
            }
            if (person.address != null) {
                def countryCode = person.address.countryCode
                if (countryCode && person.address.country == null) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                            " код страны адреса за пределами РФ ($countryCode) не найден в справочнике \"Общероссийский классификатор стран мира\".")
                }
            }
            // Проверки заполненности обязательный полей
            checkFilled(person.lastName, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ФамФЛ")
            checkFilled(person.firstName, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ИмяФЛ")
            checkFilled((person as NaturalPersonExt).birthDateStr, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ДатаРожд")
            checkFilled((person as NaturalPersonExt).citizenshipCode, person, "Файл.ИнфЧасть.АнкетДаннФЛ.Гражд")
            checkFilled((person as NaturalPersonExt).taxPayerStatusCode, person, "Файл.ИнфЧасть.АнкетДаннФЛ.СтатусФЛ")
            for (def document : person.personDocumentList) {
                checkFilled((document as PersonDocumentExt).docTypeCode, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛВид")
                checkFilled(document.documentNumber, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛНом")
                checkFilled((document as PersonDocumentExt).incRepStr, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛГл")
            }
            for (def identifier : person.personIdentityList) {
                checkFilled((identifier as PersonIdentifierExt).asnuName, person, "Файл.ИнфЧасть.СисИсточ.СисИсточНам")
            }

            // 3.b Проверки данных ФЛ
            List<String> warnings = []
            // 1. Проверка корректности формата ДУЛ
            for (def document : person.personDocumentList) {
                if (document.docType && document.documentNumber) {
                    warnings << ScriptUtils.checkDul(document.docType.getCode(), document.documentNumber, "ДУЛ Номер")
                }
            }
            if (person.citizenship?.code == "643") {
                // 2. Проверка разрешеннных символов в фамилии для граждан РФ
                warnings << ScriptUtils.checkName(person.lastName, "Фамилия")
                // 3. Проверка разрешеннных символов в имени для граждан РФ
                warnings << ScriptUtils.checkName(person.firstName, "Имя")
            }
            // 4. Проверка корректности ИНН РФ
            def message = ScriptUtils.checkInn(person.inn)
            if (message) {
                warnings << "Указан некорректный ИНН РФ. $message"
            }
            // 5. Проверка наличия ИНП для указанных систем-источников
            for (def identifier : person.personIdentityList) {
                if (!identifier.inp) {
                    warnings << "В файле загрузки для системы-источника (${(identifier as PersonIdentifierExt).asnuName}) не указан элемент ИНП"
                }
            }
            // 6. Проверка, что среди ДУЛ имеется ДУЛ с признаком "главный ДУЛ"
            if (person.majorDocument == null) {
                warnings << "Среди записей о ДУЛ отсутствует запись, для которой признак \"Включается в отчетность\"=1"
            }

            for (def warning : warnings) {
                if (warning) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                            " не пройдена проверка: $warning")
                }
            }
        }
    }

    String checkFilled(String value, NaturalPerson person, String attrName) {
        if (value != null && value.isEmpty()) {
            logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName $person.middleName, ${formatDate(person.birthDate)}, ${person.majorDocument?.documentNumber ?: "(документ не определен)"}" +
                    " было указано пустое значение либо значение, состоящее из одних пробелов, для атрибута ($attrName)")
        }
        return value
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
                logger.info("В справочнике 'Документ, удостоверяющий личность' создано записей: " + docIds.size())
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
                logger.info("В справочнике 'Идентификаторы налогоплательщика' создано записей: " + docIds.size())
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

    String formatDate(Date date) {
        return date ? ScriptUtils.formatDate(date) : ""
    }

    class NaturalPersonExt extends NaturalPerson {
        String birthDateStr
        String citizenshipCode
        String taxPayerStatusCode
    }

    class PersonDocumentExt extends PersonDocument {
        String docTypeCode
        String incRepStr
    }

    class PersonIdentifierExt extends PersonIdentifier {
        String asnuName
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
        }
        return asnu
    }
}