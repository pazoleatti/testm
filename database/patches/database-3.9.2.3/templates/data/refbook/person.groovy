package refbook // person_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentType
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.PersonService
import com.aplana.sbrf.taxaccounting.script.service.TAUserService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService
import com.google.common.base.Strings
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.IOUtils

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import java.text.DateFormat
import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.formatDate

/**
 * Cкрипт Реестра ФЛ.
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
    TAUserService taUserServiceScript
    InputStream inputStream
    String fileName
    CommonRefBookService commonRefBookService
    DepartmentService departmentService
    PersonService personService

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
        if (scriptClass.getBinding().hasVariable("inputStream")) {
            this.inputStream = (InputStream) scriptClass.getProperty("inputStream")
        }
        if (scriptClass.getBinding().hasVariable("refBookAsnuService")) {
            this.refBookAsnuService = (RefBookAsnuService) scriptClass.getProperty("refBookAsnuService")
        }
        if (scriptClass.getBinding().hasVariable("fileName")) {
            this.fileName = (String) scriptClass.getProperty("fileName")
        }
        if (scriptClass.getBinding().hasVariable("commonRefBookService")) {
            this.commonRefBookService = (CommonRefBookService) scriptClass.getProperty("commonRefBookService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("taUserServiceScript")) {
            this.taUserServiceScript = (TAUserService) scriptClass.getProperty("taUserServiceScript")
        }
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.IMPORT:
                importData()
                break
        }
    }

    // Кэш провайдеров
    Map<Long, RefBookDataProvider> providerCache = [:]
    // Кэш код страны - страна
    Map<String, RefBookCountry> countriesCache = new HashMap<>()
    // Кэш код статуса налогоплательщика - сам статус
    Map<String, RefBookTaxpayerState> taxpayerStatusessCache = new HashMap<>()
    // Кэш код типа документа - сам тип
    Map<String, RefBookDocType> docTypesCache = new HashMap<>()
    // Кэш АСНУ
    Map<String, RefBookAsnu> asnuCache = new HashMap<>()

    // Дата новых версии при импорте
    Date versionFrom = new GregorianCalendar(2017, Calendar.JANUARY, 1).time
    // Количество блоков ".Файл.ИнфЧасть" в xml при импорте
    int infPartCount = 0
    // Идентификатор тербанка из списка ТБ для ФЛ
    Integer tbPersonDepartmentId = null
    // Время выгрузки
    Date importDate = null
    // Маппа неопределенных АСНУ, где ключ ФЛ, значение - неопределнные АСНУ для ФЛ
    Map<RegistryPerson, List<PersonIdentifier>> undefinedAsnu = [:]
    // Маппа неопределенных Видов документов, где ключ ФЛ, значение - ДУЛы у которых не определен вид
    Map<RegistryPerson, List<IdDoc>> undefinedIdDocTypes = [:]
    /**
     * Мапа для сопоставления сохраненных и пропарсенных ФЛ
     */
    Map<RegistryPerson, RegistryPerson> savedAndParsedPersons = [:]

    void importData() {
        defineTB()
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }

        fillAsnuCache()
        // Из потока данных входного xml файла создадим массив байтов для возможности повторного чтения xml-файла
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        IOUtils.copy(inputStream, baos)
        IOUtils.closeQuietly(inputStream)
        byte[] bytes = baos.toByteArray()

        /* После процедуры создания ФЛ они изменяются, а при проверках нужно иметь связь данных по ФЛ из xml файла (т.к.
        * проверки проводятся по атрибутам xml-файла) и созданных ФЛ (т.к. в сообщениях используются идентификаторы из
        * Реестра ФЛ). Это обуславливает иметь набор объектов ФЛ соответствующий xml файлу и созданный в Реестре список объектов ФЛ.
        */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)
        List<RegistryPerson> parsedPersons = parseXml(inputStream)
        IOUtils.closeQuietly(inputStream)

        inputStream = new ByteArrayInputStream(bytes)
        List<RegistryPerson> parsedPersonsToCheck = parseXml(inputStream)
        IOUtils.closeQuietly(inputStream)

        List<RegistryPerson> savedPersons = save(parsedPersons)
        parsedPersons.eachWithIndex { RegistryPerson registryPerson, int index ->
            savedAndParsedPersons.put(savedPersons[index], parsedPersonsToCheck[index])
        }

        checkPersons(savedPersons)
        for (RegistryPerson person : savedPersons) {
            logger.info("Создана новая запись в Реестре физических лиц: " +
                    "$person.recordId, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, " +
                    "${person.reportDoc?.documentNumber ?: "(документ не определен)"}")
        }
        logger.info("Обработано записей из файла ${infPartCount}. В Реестре ФЛ создано ${savedPersons.size()} записей")
    }

    /****************************************************************************************************************
     * Определения ТерБанка для физлица
     ****************************************************************************************************************/

    void defineTB() {
        String guid = fileName.substring(23, fileName.indexOf(".", 23))
        RefBookDataProvider provider = getProvider(RefBook.Id.TB_PERSON.id)
        Map<String, String> filterParams = new HashMap<>()
        filterParams.put("GUID", guid)
        String filter = commonRefBookService.getSearchQueryStatementWithAdditionalStringParameters(filterParams, null, RefBook.Id.TB_PERSON.id, true)
        PagingResult<Map<String, RefBookValue>> records = provider.getRecordsWithVersionInfo(null, null, filter, null, "ASC")
        if (!records.isEmpty()) {
            Map<String, RefBookValue> record = records.get(0)
            Map<String, RefBookValue> tbPersonDepartment = record.get("TB_DEPARTMENT_ID").getValue() as Map<String, RefBookValue>
            tbPersonDepartmentId = tbPersonDepartment.get("id").numberValue.intValue()
            Department department = departmentService.get(tbPersonDepartmentId)
            if (department.type != DepartmentType.TERR_BANK) {
                logger.error("Загрузка файла %s не может быть выполнена. Подразделение \"%s\" определенное для файла, не является территориальным банком.", fileName, department.name)
            }
        } else {
            logger.error("Загрузка файла %s не может быть выполнена. Имя файла содержит GUID, для которого отсутствует запись в справочнике \"Тербанки для ФЛ при первичной загрузке\"", fileName)
        }
    }

    List<RegistryPerson> parseXml(InputStream inputStream) {
        List<RegistryPerson> persons = []
        XMLStreamReader reader = null
        try {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
            reader = xmlFactory.createXMLStreamReader(inputStream)

            RegistryPersonExt person = null

            boolean inInfoPart = false
            while (reader.hasNext()) {
                if (reader.startElement) {
                    String tag = reader.name.localPart
                    if ('СлЧасть' == tag) {
                        defineImportDate(reader)
                    }
                    if ('ИнфЧасть' == tag) {
                        checkInterrupted()
                        inInfoPart = true
                        person = new RegistryPersonExt()
                    } else if (inInfoPart) {
                        if ('АнкетДаннФЛ' == tag) {
                            parsePersonInfo(reader, person)
                        } else if ('УдЛичнФЛ' == tag) {
                            IdDoc document = parseDocumentInfo(reader, person)
                            if (document) {
                                person.getDocuments().add(document)
                            }
                        } else if ('СисИсточ' == tag) {
                            PersonIdentifier identifier = parsePersonIdentifier(reader, person)
                            if (identifier) {
                                person.personIdentityList.add(identifier)
                            }
                        }
                    }
                } else if (reader.endElement) {
                    String tag = reader.name.localPart
                    if ('ИнфЧасть' == tag) {
                        inInfoPart = false
                        infPartCount++
                        person.setSource(getMaxPriorityAsnu(person.personIdentityList))
                        if (!person.getSource()) {
                            person.setSource(new RefBookAsnu())
                        }
                        persons.add(person)
                    }
                }
                reader.next()
            }
        } finally {
            reader?.close()
        }
        return persons
    }

    /**
     * Определить дату выгрузки
     * @param reader парсер XML
     */
    void defineImportDate(XMLStreamReader reader) {
        String value = getAttrValue(reader, "ВрВыгр")
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        importDate = formatter.parse(value)
    }

    void parsePersonInfo(XMLStreamReader reader, RegistryPersonExt person) {
        person.lastName = getAttrValue(reader, "ФамФЛ")?.toUpperCase()
        person.firstName = getAttrValue(reader, "ИмяФЛ")?.toUpperCase()
        person.middleName = getAttrValue(reader, "ОтчФЛ")?.toUpperCase()
        person.birthDateStr = getAttrValue(reader, "ДатаРожд")
        person.birthDate = toDate(person.birthDateStr)
        person.citizenshipCode = getAttrValue(reader, "Гражд")
        person.citizenship = getCountryByCode(person.citizenshipCode) ?: new RefBookCountry()
        person.inn = getAttrValue(reader, "ИННФЛ")?.toUpperCase()
        person.innForeign = getAttrValue(reader, "ИННИно")?.toUpperCase()
        person.taxPayerStatusCode = getAttrValue(reader, "СтатусФЛ")
        person.taxPayerState = getTaxpayerStatusByCode(person.taxPayerStatusCode) ?: new RefBookTaxpayerState()
        person.address = parseAddress(reader)
        person.reportDoc = new IdDoc()
    }

    AddressExt parseAddress(XMLStreamReader reader) {
        AddressExt address = new AddressExt()
        address.regionCode = getAttrValue(reader, "КодРегион")?.toUpperCase()
        address.postalCode = getAttrValue(reader, "Индекс")?.toUpperCase()
        address.district = getAttrValue(reader, "Район")?.toUpperCase()
        address.city = getAttrValue(reader, "Город")?.toUpperCase()
        address.locality = getAttrValue(reader, "НаселПункт")?.toUpperCase()
        address.street = getAttrValue(reader, "Улица")?.toUpperCase()
        address.house = getAttrValue(reader, "Дом")?.toUpperCase()
        address.build = getAttrValue(reader, "Корпус")?.toUpperCase()
        address.appartment = getAttrValue(reader, "Кварт")?.toUpperCase()
        address.countryCode = getAttrValue(reader, "КодСтрИно")?.toUpperCase()
        address.country = getCountryByCode(address.countryCode) ?: new RefBookCountry()
        address.addressIno = getAttrValue(reader, "АдресИно")?.toUpperCase()
        return address
    }

    IdDoc parseDocumentInfo(XMLStreamReader reader, RegistryPerson person) {
        IdDoc document = new PersonDocumentExt()
        document.docTypeCode = getAttrValue(reader, "УдЛичнФЛВид")?.toUpperCase()
        RefBookDocType docType = getDocTypeByCode(document.docTypeCode)
        document.docType = docType ?: new RefBookDocType()
        document.documentNumber = getAttrValue(reader, "УдЛичнФЛНом")?.toUpperCase()
        document.incRepStr = getAttrValue(reader, "УдЛичнФЛГл")?.toUpperCase()
        if (document.incRepStr == "1") {
            person.reportDoc = document
        }
        document.person = person
        if (!docType) {
            if (undefinedIdDocTypes.get(person)) {
                undefinedIdDocTypes.get(person).add(document)
            } else {
                undefinedIdDocTypes.put(person, [document])
            }
            return null
        }
        return document
    }

    PersonIdentifier parsePersonIdentifier(XMLStreamReader reader, RegistryPerson person) {
        PersonIdentifier personIdentifier = new PersonIdentifierExt()
        personIdentifier.asnuName = getAttrValue(reader, "СисИсточНам")
        RefBookAsnu refBookAsnu = getAsnuByName(personIdentifier.asnuName)
        personIdentifier.asnu = refBookAsnu ?: new RefBookAsnu()
        personIdentifier.inp = getAttrValue(reader, "СисИсточИНП")?.toUpperCase()
        personIdentifier.person = person
        if (!refBookAsnu) {
            if (undefinedAsnu.get(person)) {
                undefinedAsnu.get(person).add(personIdentifier)
            } else {
                undefinedAsnu.put(person, [personIdentifier])
            }
            return null
        }
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

    Map<RegistryPerson, List<IdDoc>> removedDocuments = [:]

    List<RegistryPerson> save(List<RegistryPerson> persons) {
        if (persons) {
            for (def person : persons) {
                PersonTb personTb = new PersonTb()
                personTb.setPerson(person)
                Department department = new Department()
                department.setId(tbPersonDepartmentId)
                personTb.setTbDepartment(department)
                personTb.setImportDate(importDate)
                person.personTbList.add(personTb)
                person.startDate = versionFrom
                Iterator<IdDoc> idDocIterator = person.documents.iterator()
                while (idDocIterator.hasNext()) {
                    IdDoc idDoc = idDocIterator.next()
                    if (!getDocTypeByCode(idDoc.docType?.getCode()) || Strings.isNullOrEmpty(idDoc.documentNumber)) {
                        idDocIterator.remove()
                    }
                }
            }

            personService.savePersons(persons)
        }
        return persons
    }

    /**
     * Обработка ошибочных ситуаций алгоритма
     */
    void checkPersons(List<RegistryPerson> persons) {
        for (def person : persons) {
            // 4.a Ошибки возникшие при создании записей
            List<PersonIdentifier> identityPersonList = undefinedAsnu.get(person)
            if (identityPersonList != null) {
                for (def identifier : identityPersonList) {
                    def asnuName = (identifier as PersonIdentifierExt).asnuName
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                            " АСНУ=\"$asnuName\" не найдена запись в справочнике \"АСНУ\".")

                }
            }
            if (person.source?.id == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                        " невозможно определить систему-источник. Атрибут \"Система-источник\" не был заполнен.")
            }
            List<IdDoc> idDocTypes = this.undefinedIdDocTypes.get(person)
            if (idDocTypes != null) {
                for (IdDoc document : idDocTypes) {
                    def docTypeCode = (document as PersonDocumentExt).docTypeCode
                    if (docTypeCode && document.docType.id == null) {
                        logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, \"$document.documentNumber\"" +
                                " указан код ДУЛ ($docTypeCode), отсутствующий в справочнике \"Коды документов\".")
                    }
                }
            }

            def taxPayerStatusCode = (person as RegistryPersonExt).taxPayerStatusCode
            if (taxPayerStatusCode && person.taxPayerState.id == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, \"${person.reportDoc?.documentNumber ?: "(документ не определен)"}\"" +
                        " cтатус налогоплательщика ($taxPayerStatusCode) не найден в справочнике \"Статусы налогоплательщика\".")
            }
            def citizenshipCode = (person as RegistryPersonExt).citizenship.code
            if (citizenshipCode == null) {
                logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                        " код страны гражданства (${(person as RegistryPersonExt).citizenshipCode}) не найден в справочнике \"Общероссийский классификатор стран мира\".")
            }
            if (person.address.country != null) {
                def countryCode = (person.address as AddressExt).countryCode
                if (countryCode && person.address.country.id == null) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                            " код страны адреса за пределами РФ (${(person.address as AddressExt).countryCode}) не найден в справочнике \"Общероссийский классификатор стран мира\".")
                }
            }
            // Проверки заполненности обязательный полей
            checkFilled(person.lastName, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ФамФЛ")
            checkFilled(person.firstName, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ИмяФЛ")
            checkFilled((person as RegistryPersonExt).birthDateStr, person, "Файл.ИнфЧасть.АнкетДаннФЛ.ДатаРожд")
            checkFilled((person as RegistryPersonExt).citizenshipCode, person, "Файл.ИнфЧасть.АнкетДаннФЛ.Гражд")
            checkFilled((person as RegistryPersonExt).taxPayerStatusCode, person, "Файл.ИнфЧасть.АнкетДаннФЛ.СтатусФЛ")
            for (def document : savedAndParsedPersons.get(person).documents) {
                checkFilled((document as PersonDocumentExt).docTypeCode, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛВид")
                checkFilled(document.documentNumber, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛНом")
                checkFilled((document as PersonDocumentExt).incRepStr, person, "Файл.ИнфЧасть.УдЛичнФЛ.УдЛичнФЛГл")
            }
            for (def identifier : person.personIdentityList) {
                checkFilled((identifier as PersonIdentifierExt).asnuName, person, "Файл.ИнфЧасть.СисИсточ.СисИсточНам")
            }

            // 4.b Проверки данных ФЛ
            List<String> warnings = []
            // 1. Проверка корректности формата ДУЛ
            for (def document : savedAndParsedPersons.get(person).documents) {
                if (document.docType != null && document.documentNumber != null) {
                    warnings.add(ScriptUtils.checkDul(document.docType.getCode(), document.documentNumber, "ДУЛ Номер"))
                }
            }

            if (person.citizenship.code != null) {
                // 2. Проверка разрешеннных символов в фамилии
                warnings.addAll(ScriptUtils.checkLastName(person.lastName, person.citizenship.code))
                // 3. Проверка разрешеннных символов в имени
                warnings.addAll(ScriptUtils.checkFirstName(person.firstName, person.citizenship.code))
            }
            // 4. Проверка корректности ИНН РФ
            def message = ScriptUtils.checkInn(person.inn)
            if (message) {
                warnings.add("Указан некорректный ИНН РФ. $message")
            }
            // 5. Проверка наличия ИНП для указанных систем-источников
            for (def identifier : person.personIdentityList) {
                if (!identifier.inp) {
                    warnings.add("В файле загрузки для системы-источника (${(identifier as PersonIdentifierExt).asnuName}) не указан элемент ИНП")
                }
            }
            // 6. Проверка, что среди ДУЛ имеется ДУЛ с признаком "главный ДУЛ"
            if (savedAndParsedPersons.get(person)?.reportDoc?.incRepStr != "1") {
                warnings << "Среди записей о ДУЛ отсутствует запись, для которой признак \"Включается в отчетность\"=1"
            }

            for (def warning : warnings) {
                if (warning) {
                    logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                            " не пройдена проверка: $warning")
                }
            }
        }
    }

    String checkFilled(String value, RegistryPerson person, String attrName) {
        if (value != null && value.isEmpty()) {
            logger.warn("Для ФЛ $person.id, $person.lastName $person.firstName ${person.middleName ?: ""}, ${formatDate(person.birthDate)}, ${person.reportDoc?.documentNumber ?: "(документ не определен)"}" +
                    " было указано пустое значение либо значение, состоящее из одних пробелов, для атрибута ($attrName)")
        }
        return value
    }

    String formatDate(Date date) {
        return date ? ScriptUtils.formatDate(date) : ""
    }

    class RegistryPersonExt extends RegistryPerson {
        String birthDateStr
        String citizenshipCode
        String taxPayerStatusCode
    }

    class AddressExt extends Address {
        String countryCode
    }

    class PersonDocumentExt extends IdDoc {
        String docTypeCode
        String incRepStr
    }

    class PersonIdentifierExt extends PersonIdentifier {
        String asnuName
    }

    /************************************* ОБЩИЕ МЕТОДЫ** *****************************************************************/

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
    RefBookCountry getCountryByCode(String code) {
        RefBookCountry country = null
        if (code) {
            country = countriesCache.get(code)
            if (!country) {
                def recordData = getProvider(RefBook.Id.COUNTRY.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    country = new RefBookCountry()
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
    RefBookTaxpayerState getTaxpayerStatusByCode(String code) {
        RefBookTaxpayerState taxpayerStatus = null
        if (code) {
            taxpayerStatus = taxpayerStatusessCache.get(code)
            if (!taxpayerStatus) {
                def recordData = getProvider(RefBook.Id.TAXPAYER_STATUS.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    taxpayerStatus = new RefBookTaxpayerState()
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
    RefBookDocType getDocTypeByCode(String code) {
        RefBookDocType docType = null
        if (code) {
            docType = docTypesCache.get(code)
            if (!docType) {
                def recordData = getProvider(RefBook.Id.DOCUMENT_CODES.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    docType = new RefBookDocType()
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
            asnu = asnuCache.get(StringUtils.cleanString(name))
            if (!asnu) {
                asnu = refBookAsnuService.fetchByName(name)
                asnuCache.put(name, asnu)
            }
        }
        return asnu
    }

    /**
     * Заполняет кэш АСНУ значениями из справочника. в качестве ключа используется обработаное имя АСНУ
     */
    void fillAsnuCache() {
        List<RefBookAsnu> availableAsnu = refBookAsnuService.fetchAvailableAsnu(taUserServiceScript.getCurrentUserInfo())
        for (RefBookAsnu asnu : availableAsnu) {
            asnuCache.put(StringUtils.cleanString(asnu.name), asnu)
        }
    }
}