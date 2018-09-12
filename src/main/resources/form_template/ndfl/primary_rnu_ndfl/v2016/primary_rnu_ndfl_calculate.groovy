package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.IdentityObject
import com.aplana.sbrf.taxaccounting.model.PagingResult
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.Address
import com.aplana.sbrf.taxaccounting.model.identification.AttributeChangeEvent
import com.aplana.sbrf.taxaccounting.model.identification.AttributeChangeEventType
import com.aplana.sbrf.taxaccounting.model.identification.AttributeChangeListener
import com.aplana.sbrf.taxaccounting.model.identification.AttributeCountChangeListener
import com.aplana.sbrf.taxaccounting.model.identification.Country
import com.aplana.sbrf.taxaccounting.model.identification.DocType
import com.aplana.sbrf.taxaccounting.model.identification.IdentificationData
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier
import com.aplana.sbrf.taxaccounting.model.identification.PersonTb
import com.aplana.sbrf.taxaccounting.model.identification.PersonalData
import com.aplana.sbrf.taxaccounting.model.identification.RefBookObject
import com.aplana.sbrf.taxaccounting.model.identification.TaxpayerStatus
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.PersonService
import com.aplana.sbrf.taxaccounting.script.service.RefBookPersonService
import com.aplana.sbrf.taxaccounting.script.service.RefBookService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.lang3.exception.ExceptionUtils

import java.sql.SQLSyntaxErrorException

new Calculate(this).run()

/**
 * Скрипт отвечает за идентификацию физлиц. Результатом работы скрипта является создание или обновление записей
 * в справочнике физлиц на основе данных содержащихся в разделе Реквизиты налоговой формы.
 * Алгоритм имеет несколько этапов. Данные для кждого этапа определяются соответствующей процедурой в БД.
 * 1. На первом этапе ищутся записи в справочнике физлиц, которые совпадают по всем ключевым параметам с записями из
 * реквизитов налоговой формы. Считается что это одни и теже физлица. Если какие-то параметры отличаются, то они
 * обновляются.
 * 2. На втором этапе ищутся физлица, которые совпадают хотя бы по одному ключевому параметру, тогда необходимо
 * провести сравнение физлица из реквизитов налоговй формы с отобранными физлицами из справочника физлиц.
 * Из этих физлиц выбирается одно физлицо с максимальным весом выше порога схожести. Значения в справочнике
 * обновляются свежими даннными.
 * Для физлиц из формы, для которых не были найдены соответствия на шагах 1 и 2 создаются новые записи в справочнике физлиц.
 *
 * По результату работы скрипта каждое физлицо из налоговой формы раздела "Реквизиты" будет иметь ссылку на запись в
 * справочнике "Физические лица"
 */
@TypeChecked
class Calculate extends AbstractScriptClass {

    DeclarationData declarationData
    Map<String, Object> calculateParams
    RefBookPersonService refBookPersonService
    NdflPersonService ndflPersonService
    ReportPeriodService reportPeriodService
    RefBookFactory refBookFactory
    CommonRefBookService commonRefBookService
    RefBookService refBookService
    PersonService personService
    DepartmentService departmentService

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    Integer INCLUDE_TO_REPORT = 1

    Integer NOT_INCLUDE_TO_REPORT = 0

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

    final String T_PERSON = "1" //"Реквизиты"

    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""

    // Справочники
    final String R_ID_DOC_TYPE = "Коды документов"

    List<Country> countryRefBookCache = []
    List<DocType> docTypeRefBookCache = []
    List<TaxpayerStatus> taxpayerStatusRefBookCache = []

    Map<Long, Map<String, String>> refBookAttrCache = new HashMap<Long, Map<String, String>>()
    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]
    HashMap<Long, RefBook> mapRefBookToIdCache = new HashMap<Long, RefBook>()
    //Коды стран из справочника
    Map<Long, String> countryCodeCache = [:]
    //Коды статуса налогоплательщика
    Map<Long, String> taxpayerStatusCodeCache = [:]
    //Коды Асну
    Map<Long, String> asnuCache = [:]
    //Приоритет Асну
    Map<Long, Integer> asnuPriority = [:]
    /**
     * Мапа используется при определении дубликатов между физлицами которые еще не имеют ссылок на справочник физлиц.
     * Ключем здесь выступает физлицо, которе будет оригиналом и для которого будет создана запись в справочнике ФЛ,
     * а значением является список физлиц которые будут ссылаться на запись в справочнике ФЛ созданную для оригинала.
     */
    Map<NaturalPerson, List<NaturalPerson>> primaryPersonOriginalDuplicates = new HashMap<>()

    /**
     * Список физлиц для вставки
     */
    List<NaturalPerson> insertPersonList = []

    /**
     * Список физлиц дубликатов
     */
    List<NaturalPerson> duplicatePersonList = []

    List<NaturalPerson> primaryPersonDataList = []

    Date declarationDataCreationDate

    Map<Integer, Department> departmentCache = [:]

    private Calculate() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Calculate(scriptClass) {
        //noinspection GroovyAssignabilityCheck
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
        }
        if (scriptClass.getBinding().hasVariable("calculateParams")) {
            this.calculateParams = (Map<String, Object>) scriptClass.getProperty("calculateParams")
        }
        if (scriptClass.getBinding().hasVariable("refBookPersonService")) {
            this.refBookPersonService = (RefBookPersonService) scriptClass.getProperty("refBookPersonService")
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getProperty("refBookService")
        }
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
        if (scriptClass.getBinding().hasVariable("commonRefBookService")) {
            this.commonRefBookService = (CommonRefBookService) scriptClass.getProperty("commonRefBookService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
    }

    @Override
    public void run() {
        initConfiguration()
        updateAndCheckException({
            switch (formDataEvent) {
                case FormDataEvent.CALCULATE:
                    long timeFull = System.currentTimeMillis()
                    long time = System.currentTimeMillis()

                    logForDebug("Начало расчета ПНФ")

                    if (declarationData.asnuId == null) {
                        //noinspection GroovyAssignabilityCheck
                        throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!")
                    }

                    //выставляем параметр что скрипт не формирует новый xml-файл
                    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE)

                    refBookPersonService.clearRnuNdflPerson(declarationData.id)

                    //Получаем список всех ФЛ в первичной НФ
                    primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFromNdfl(declarationData.id, createPrimaryRowMapper(false))
                    if (logger.containsLevel(LogLevel.ERROR)) {
                        return
                    }

                    insertPersonList.addAll(primaryPersonDataList)

                    //noinspection GroovyAssignabilityCheck
                    logForDebug("В ПНФ номер " + declarationData.id + " получены записи о физ. лицах (" + primaryPersonDataList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))

                    Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries { NaturalPerson naturalPerson ->
                        [naturalPerson.getPrimaryPersonId(), naturalPerson]
                    }

                    //Заполнени временной таблицы версий
                    time = System.currentTimeMillis()
                    refBookPersonService.fillRecordVersions()
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Заполнение таблицы версий (" + ScriptUtils.calcTimeMillis(time))

                    // Идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
                    time = System.currentTimeMillis()
                    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, createRefbookHandler())
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time))

                    time = System.currentTimeMillis()
                    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap)
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Обновление записей (" + ScriptUtils.calcTimeMillis(time))

                    time = System.currentTimeMillis()
                    Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, createRefbookHandler())
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Основная выборка по всем параметрам (" + checkSimilarityPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time))

                    time = System.currentTimeMillis()
                    updateNaturalPersonRefBookRecords(primaryPersonMap, checkSimilarityPersonMap)
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Обновление записей (" + ScriptUtils.calcTimeMillis(time))

                    time = System.currentTimeMillis()
                    createNaturalPersonRefBookRecords()
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Создание (" + insertPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))

                    countTotalAndUniquePerson()
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Завершение расчета ПНФ (" + ScriptUtils.calcTimeMillis(timeFull))
            }
        })
    }

    // Вывод информации о количестве обработанных физлиц всего и уникальных
    void countTotalAndUniquePerson() {
        int countInDeclarationData = ndflPersonService.getCountNdflPerson(declarationData.id)
        int countOfUniqueEntries = personService.getCountOfUniqueEntries(declarationData.id)
        logger.info("Записей физических лиц обработано: $countInDeclarationData, всего уникальных записей физических лиц: $countOfUniqueEntries")
    }

    NaturalPersonPrimaryRnuRowMapper createPrimaryRowMapper(boolean isLog) {
        final Logger localLogger = logger
        NaturalPersonPrimaryRnuRowMapper naturalPersonRowMapper = new NaturalPersonPrimaryRnuRowMapper() {
            @Override
            public DocType getDocTypeByCode(String code, NaturalPerson ndflPerson) {
                if (docTypeCodeMap == null) {
                    throw new ServiceException("Не проинициализирован кэш справочника 'Коды документов'!") as Throwable
                }
                DocType result = docTypeCodeMap.get(code)
                String fio = buildFio(ndflPerson)
                String inp = ndflPerson.getPersonIdentifier()?.inp ?: ""
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
                //noinspection GroovyAssignabilityCheck
                String pathError = String.format(SECTION_LINE_MSG + ". %s", T_PERSON, ndflPerson.num ?: "",
                        "ДУЛ Код='${code ?: ""}'")
                if (code == null) {
                    result = null
                    if (isLog) {
                        localLogger.warnExp("%s. %s.", "Наличие обязательных реквизитов для формирования отчетности", fioAndInp, pathError,
                                "Не заполнен обязательный параметр")
                    }
                } else if (code == "0") {
                    result = null
                    if (isLog) {
                        localLogger.warnExp("%s. %s.", "Наличие обязательных реквизитов для формирования отчетности", fioAndInp, pathError,
                                "Параметр не может быть равен \"0\"")
                    }
                } else if (result == null) {
                    if (isLog) {
                        localLogger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_ID_DOC_TYPE), fioAndInp, pathError,
                                "\"ДУЛ Код (Графа 10)\" не соответствует справочнику '$R_ID_DOC_TYPE'")
                    }
                }
                return result
            }
        }
        naturalPersonRowMapper.setAsnuId(declarationData.asnuId)

        List<Country> countryList = getCountryRefBookList()
        naturalPersonRowMapper.setCountryCodeMap(countryList.collectEntries {
            [it.code, it]
        })

        List<DocType> docTypeList = getDocTypeRefBookList()
        naturalPersonRowMapper.setDocTypeCodeMap(docTypeList.collectEntries {
            [it.code, it]
        })

        List<TaxpayerStatus> taxpayerStatusCodeList = getTaxpayerStatusRefBookList()
        naturalPersonRowMapper.setTaxpayerStatusCodeMap(taxpayerStatusCodeList.collectEntries {
            [it.code, it]
        })

        return naturalPersonRowMapper
    }

    List<Country> getCountryRefBookList() {
        if (countryRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.COUNTRY.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                Country country = new Country()
                country.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
                country.setCode(refBookValueMap?.get("CODE")?.getStringValue())
                countryRefBookCache.add(country)
            }
        }
        return countryRefBookCache
    }

    List<DocType> getDocTypeRefBookList() {
        if (docTypeRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.DOCUMENT_CODES.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                DocType docType = new DocType()
                docType.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
                docType.setName(refBookValueMap?.get("NAME")?.getStringValue())
                docType.setCode(refBookValueMap?.get("CODE")?.getStringValue())
                docType.setPriority(refBookValueMap?.get("PRIORITY")?.getNumberValue()?.intValue())
                docTypeRefBookCache.add(docType)
            }
        }
        return docTypeRefBookCache
    }

    List<TaxpayerStatus> getTaxpayerStatusRefBookList() {
        if (taxpayerStatusRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                TaxpayerStatus taxpayerStatus = new TaxpayerStatus()
                taxpayerStatus.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
                taxpayerStatus.setName(refBookValueMap?.get("NAME")?.getStringValue())
                taxpayerStatus.setCode(refBookValueMap?.get("CODE")?.getStringValue())
                taxpayerStatusRefBookCache.add(taxpayerStatus)
            }
        }
        return taxpayerStatusRefBookCache
    }

    //---------------- Identification ----------------

    /**
     * Выполняет действия для того чтобы определить имеются ли в налоговой форме записи в разделе реквизиты, которые
     * являются одним и тем же физическим лицом. Для таких физлиц будет создана одна запись в справочнике
     * "Физические лица".
     * Алгоритм следующий:
     * A. Проходим циклом по <code>insertPersonList</code> и для каждого физлица определяем значения:
     * 1. ИНП
     * 2. СНИЛС
     * 3. ИНН
     * 4. ИНН в стране гражданства
     * 5. ДУЛ
     * 6. Персональные данные (ФИО и дата рождения)
     * B. Для каждого значения необходимо проверить встречалось ли оно на предыдущих итерациях и тогда:
     * 1. Если оно встречалось более 1 раза оно будет находится в мапе где где ключ значение из шага A, а значение
     * список физлиц у которых такое же значение из шага A. Добавляем в этот список физлиц текущее физлицо.
     * 2. Если оно встречалось только 1 раз оно будет находится в мапе где ключ значение из шага A, а значение физлицо.
     * Тогда мы создаем запись в мапе из шага B1 с двумя физлицами.
     * 3. Если оно не встречалось добавляем запись в мапу из шага B2 с физлицом.
     * За шаг B отвечает метод {@link #addToReduceMap(Object, Map < ?, NaturalPerson >, Map < ?, List < NaturalPerson > >, NaturalPerson)}
     * C. Далее для каждой entry из мапы описаной на шаге B1, сравниваем между собой физлиц из значения этой entry.
     * Сравнение выполняется с вложенным циклом для того чтобы не делать лишнюю работу заведено поле{@link #primaryDuplicateIds}
     * В это поле добавляется идентификатор физлица определенный раннее как дубликат и таким образом алгоритм будет знать
     * что физлицо не надо делать оригиналом и не надо сравнивать по другим совпадающим параметрам.
     * Для оригинала выбирается первое значение из списка физлиц, для того чтобы обеспечить повторяемость результата список
     * предварительно сортируется по ключу идентификатора из реквизитов налоговой формы.
     * Результат записывается в {@link #primaryPersonOriginalDuplicates}
     * Одновременно удаляем дубликаты из списка физлиц отобранных для вставки в справочник физлиц, чтобы для дубликатов
     * не создалась отдельная запись.
     * За шаг C отвечает {@link #mapDuplicates(List < NaturalPerson >)}
     * D. После создания записей в справочнике физлиц назначаем дубликатам ссылку на запись в справочнике физлиц, такую
     * же какую имеют оригиналы.
     */
    void performPrimaryPersonDuplicates() {

        Map<String, NaturalPerson> inpMatchedMap = new HashMap<>()
        Map<String, List<NaturalPerson>> inpReducedMatchedMap = new HashMap<>()

        Map<String, NaturalPerson> snilsMatchedMap = new HashMap<>()
        Map<String, List<NaturalPerson>> snilsReducedMatchedMap = new HashMap<>()

        Map<String, NaturalPerson> innMatchedMap = new HashMap<>()
        Map<String, List<NaturalPerson>> innReducedMatchedMap = new HashMap<>()

        Map<String, NaturalPerson> innForeignMatchedMap = new HashMap<>()
        Map<String, List<NaturalPerson>> innForeignReducedMatchedMap = new HashMap<>()

        Map<PersonDocument, NaturalPerson> idDocMatchedMap = new HashMap<>()
        Map<PersonDocument, List<NaturalPerson>> idDocReducedMatchedMap = new HashMap<>()

        Map<PersonalData, NaturalPerson> personalDataMatchedMap = new HashMap<>()
        Map<PersonalData, List<NaturalPerson>> personalDataReducedMatchedMap = new HashMap<>()

        for (NaturalPerson person : insertPersonList) {
            String inp = person.personIdentityList.isEmpty() ? "" : person.personIdentityList.get(0)
            String snils = person.snils?.replaceAll("[\\s-]", "")?.toLowerCase()
            String inn = person.inn
            String innForeign = person.innForeign
            PersonDocument personDocument = null
            if (!person?.personDocumentList.isEmpty()) {
                person.personDocumentList.get(0)
            }
            PersonalData personalData = new PersonalData(person.firstName, person.lastName, person.middleName, person.birthDate)
            addToReduceMap(inp, inpMatchedMap, inpReducedMatchedMap, person)
            if (snils != null) {
                addToReduceMap(snils, snilsMatchedMap, snilsReducedMatchedMap, person)
            }
            if (inn != null) {
                addToReduceMap(inn, innMatchedMap, innReducedMatchedMap, person)
            }
            if (innForeign != null) {
                addToReduceMap(innForeign, innForeignMatchedMap, innForeignReducedMatchedMap, person)
            }
            addToReduceMap(personDocument, idDocMatchedMap, idDocReducedMatchedMap, person)
            addToReduceMap(personalData, personalDataMatchedMap, personalDataReducedMatchedMap, person)
        }

        List<NaturalPerson> pickedForWeightComparePersons = []
        sendToMapDuplicates(inpReducedMatchedMap)
        sendToMapDuplicates(snilsReducedMatchedMap)
        sendToMapDuplicates(innReducedMatchedMap)
        sendToMapDuplicates(innForeignReducedMatchedMap)
        sendToMapDuplicates(idDocReducedMatchedMap)
        sendToMapDuplicates(personalDataReducedMatchedMap)
    }

    /**
     * Разделяет <code>processingPersonList</code> на отдельные списки, каждый список передается
     * в {@link #mapDuplicates(List < NaturalPerson >)} для дальнейшей обработки
     * @param processingPersonList списки обрабатываемых Физлиц, сгруппированные по ключевому параметру.
     */
    void sendToMapDuplicates(Map<?, List<NaturalPerson>> processingPersonList) {
        if (!processingPersonList.isEmpty()) {
            for (List<NaturalPerson> personList : processingPersonList.values()) {
                mapDuplicates(personList)
            }
        }
    }

    /**
     * Добавляет физлиц в мапу по ключу.
     * @param key ключ по которому добавляется параметр
     * @param matchMap здесь находятся физлица, которые имеют значение с таким же <code>key</code>
     * @param reduceMap если физлицо уже присутствует в <code>matchMap</code>, тогда возможно это дубликат и необходимо
     * провести сравнение по весам
     * @param person сравниваемое физлицо
     */
    void addToReduceMap(Object key, Map<?, NaturalPerson> matchMap, Map<?, List<NaturalPerson>> reduceMap, NaturalPerson person) {
        List<NaturalPerson> list1 = reduceMap.get(key)
        if (list1 != null) {
            reduceMap.get(key).add(person)
        } else {
            NaturalPerson pastMatchedPerson = matchMap.get(key)
            if (pastMatchedPerson != null) {
                reduceMap.put(key, [person, pastMatchedPerson])
            } else {
                matchMap.put(key, person)
            }
        }
    }

    /**
     * Отвечает за распределение физлиц на оригиналы и дубликаты, на основе расчета по весам
     * @param processingPersonList список обрабатываемых Физлиц.
     */
    void mapDuplicates(List<NaturalPerson> processingPersonList) {
        Collections.sort(processingPersonList, new Comparator<NaturalPerson>() {
            @Override
            int compare(NaturalPerson o1, NaturalPerson o2) {
                return o1.primaryPersonId.compareTo(o2.primaryPersonId)
            }
        })
        Double similarityLine = similarityThreshold.doubleValue() / 1000
        // Список физлиц которые были выбраны в качестве оригинала
        NaturalPerson originalPerson = null
        List<NaturalPerson> originalPrimaryPersonList = new ArrayList<>(primaryPersonOriginalDuplicates.keySet())

        // Выбираем физлицо, которое будет оригиналом.
        // Проверяем было ли физлицо выбрано оригиналом раннее. Если да, то вы выбираем его снова.
        for (NaturalPerson person : processingPersonList) {
            if (originalPrimaryPersonList.contains(person)) {
                originalPerson = person
                processingPersonList.remove(person)
                break
            }
        }

        /* Если физлицо не было раннее выбрано в качестве оригинала, тогда берем первое физлицо, но проверяем чтобы оно
         не содержалось в списке дубликатов и чтобы оно не было дубликатом имеющихся оригиналов*/
        if (originalPerson == null && !processingPersonList.isEmpty()) {
            outer:
            for (NaturalPerson person : processingPersonList) {
                if (duplicatePersonList.contains(person)) {
                    continue
                } else {
                    for (NaturalPerson original : originalPrimaryPersonList) {
                        refBookPersonService.calculateWeight(original, [person], new PersonDataWeightCalculator(refBookPersonService.getBaseCalculateList()))
                        if (person.weight > similarityLine) {
                            duplicatePersonList.add(person)
                            insertPersonList.remove(person)
                            primaryPersonOriginalDuplicates.get(original).add(person)
                            continue outer
                        }
                    }
                    originalPerson = person
                    processingPersonList.remove(person)
                    break
                }
            }
        }

        if (originalPerson != null) {
            for (int i = 0; i < processingPersonList.size(); i++) {
                // Это список физлиц-дубликатов для конкретного оригинала
                List<NaturalPerson> duplicatePersons = primaryPersonOriginalDuplicates.get(originalPerson)
                if (duplicatePersons == null || !duplicatePersonList.contains(processingPersonList.get(i))) {
                    refBookPersonService.calculateWeight(originalPerson, [processingPersonList.get(i)], new PersonDataWeightCalculator(refBookPersonService.getBaseCalculateList()))
                    if (processingPersonList.get(i).weight > similarityLine) {
                        duplicatePersonList.add(processingPersonList.get(i))
                        insertPersonList.remove(processingPersonList.get(i))
                        if (duplicatePersons != null) {
                            duplicatePersons.add(processingPersonList.get(i))
                        } else {
                            primaryPersonOriginalDuplicates.put(originalPerson, [processingPersonList.get(i)])
                        }
                    }
                }
            }
        }
    }

    def createNaturalPersonRefBookRecords() {

        int createCnt = 0
        if (!insertPersonList.isEmpty()) {

            performPrimaryPersonDuplicates()

            List<Address> addressList = new ArrayList<>()
            List<PersonDocument> documentList = new ArrayList<>()
            List<PersonIdentifier> identifierList = new ArrayList<>()
            List<PersonTb> personTbList = new ArrayList<>()

            for (NaturalPerson person : insertPersonList) {

                ScriptUtils.checkInterrupted()

                Address address = person.getAddress()
                if (address != null) {
                    addressList.add(address)
                }

                PersonDocument personDocument = person.getMajorDocument()
                if (personDocument != null && personDocument.docType != null) {
                    personDocument.documentNumber = performDocNumber(personDocument)
                    documentList.add(personDocument)
                }

                PersonIdentifier personIdentifier = person.getPersonIdentifier()
                if (personIdentifier != null) {
                    identifierList.add(personIdentifier)
                }

                Integer parentTbId = departmentService.getParentTBId(declarationData.departmentId)

                PersonTb personTb = new PersonTb()
                personTb.naturalPerson = person
                personTb.tbDepartmentId = parentTbId
                personTb.importDate = getDeclarationDataCreationDate()
                personTbList.add(personTb)
            }

            //insert addresses batch
            insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressList, { Address address ->
                mapAddressAttr(address)
            })

            //insert persons batch
            insertBatchRecords(RefBook.Id.PERSON.getId(), insertPersonList, { NaturalPerson person ->
                mapPersonAttr(person)
            })

            //insert documents batch
            insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentList, { PersonDocument personDocument ->
                mapPersonDocumentAttr(personDocument)
            })

            addReportDocsToPersons(documentList)

            //insert identifiers batch
            insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifierList, { PersonIdentifier personIdentifier ->
                mapPersonIdentifierAttr(personIdentifier)
            })

            insertBatchRecords(RefBook.Id.PERSON_TB.getId(), personTbList, { PersonTb personTb -> mapPersonTbAttr(personTb) })

            //update reference to ref book
            updatePrimaryToRefBookPersonReferences(insertPersonList)

            for (Map.Entry<NaturalPerson, List<NaturalPerson>> entry : primaryPersonOriginalDuplicates.entrySet()) {
                for (NaturalPerson duplicatePerson : entry.getValue()) {
                    duplicatePerson.id = entry.getKey().id
                }
                updatePrimaryToRefBookPersonReferences(entry.getValue())
            }

            for (NaturalPerson person : insertPersonList) {
                Long recordId = refBookService.getNumberValue(RefBook.Id.PERSON.id, person.getId(), "record_id").longValue()
                person.setOldId(recordId)
                Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(person)
                refBookPersonValues.put("OLD_ID", new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, person.getId(), null, null, refBookPersonValues)
                logger.infoExp("Создана новая запись в Реестре физических лиц. Идентификатор ФЛ: %s, ФИО: %s %s %s", "", String.format("%s, ИНП: %s", buildFio(person), person.getPersonIdentifier().getInp()), recordId, person.getLastName() ?: "", person.getFirstName() ?: "", person.getMiddleName() ?: "")
                createCnt++
            }
        }

        logForDebug("Создано записей: " + createCnt)
    }

    NaturalPersonRefbookHandler createRefbookHandler() {

        NaturalPersonRefbookHandler refbookHandler = new NaturalPersonRefbookHandler()

        refbookHandler.setLogger(logger)

        List<Country> countryList = getCountryRefBookList()
        refbookHandler.setCountryMap(countryList.collectEntries {
            [it.id, it]
        })

        List<DocType> docTypeList = getDocTypeRefBookList()
        refbookHandler.setDocTypeMap(docTypeList.collectEntries {
            [it.id, it]
        })

        List<TaxpayerStatus> taxpayerStatusList = getTaxpayerStatusRefBookList()
        refbookHandler.setTaxpayerStatusMap(taxpayerStatusList.collectEntries {
            [it.id, it]
        })

        return refbookHandler
    }

    def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

        long time = System.currentTimeMillis()

        //Проходим по списку и определяем наиболее подходящюю запись, если подходящей записи не найдено то содадим ее
        List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>()

        //список записей для обновления атрибутов справочника физлиц
        Set<Map<String, RefBookValue>> updatePersonList = new LinkedHashSet<>()

        List<Address> insertAddressList = new ArrayList<Address>()
        List<Map<String, RefBookValue>> updateAddressList = new ArrayList<Map<String, RefBookValue>>()

        List<PersonDocument> updateDocumentList = new ArrayList<PersonDocument>()

        List<PersonIdentifier> insertIdentifierList = new ArrayList<PersonIdentifier>()
        List<Map<String, RefBookValue>> updateIdentifierList = new ArrayList<Map<String, RefBookValue>>()

        List<PersonTb> insertPersonTbList = new ArrayList<>()

        //primaryId - RefBookPerson
        HashMap<Long, NaturalPerson> conformityMap = new HashMap<Long, NaturalPerson>()

        int msgCnt = 0
        int maxMsgCnt = 0
        for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {

            Long primaryPersonId = entry.getKey()

            Map<Long, NaturalPerson> similarityPersonValues = entry.getValue()

            List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values())

            NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId)

            long inTime = System.currentTimeMillis()

            IdentificationData identificationData = new IdentificationData()
            identificationData.naturalPerson = primaryPerson
            identificationData.refBookPersonList = similarityPersonList
            identificationData.tresholdValue = similarityThreshold
            identificationData.declarationDataAsnuId = declarationData.asnuId
            if (asnuPriority.isEmpty()) {
                getRefAsnu()
            }
            identificationData.priorityMap = asnuPriority

            NaturalPerson refBookPerson = refBookPersonService.identificatePerson(identificationData, logger)

            conformityMap.put(primaryPersonId, refBookPerson)

            //Адрес нужно создать заранее и получить Id
            if (refBookPerson != null) {
                if (primaryPerson.getAddress() != null && refBookPerson.getAddress() == null) {
                    insertAddressList.add(primaryPerson.getAddress())
                }
            }

            if (msgCnt <= maxMsgCnt) {
                //noinspection GroovyAssignabilityCheck
                logForDebug("Идентификация (" + ScriptUtils.calcTimeMillis(inTime))
            }

            msgCnt++
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Идентификация ФЛ, обновление адресов (" + ScriptUtils.calcTimeMillis(time))

        insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), insertAddressList, { Address address ->
            mapAddressAttr(address)
        })

        time = System.currentTimeMillis()

        int updCnt = 0
        msgCnt = 0
        maxMsgCnt = 0
        for (Map.Entry<Long, NaturalPerson> entry : conformityMap.entrySet()) {

            List<PersonDocument> insertDocumentList = new ArrayList<PersonDocument>()

            long inTime = System.currentTimeMillis()

            ScriptUtils.checkInterrupted()

            Long primaryPersonId = entry.getKey()
            NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId)
            NaturalPerson refBookPerson = entry.getValue()

            AttributeCountChangeListener addressAttrCnt = new AttributeCountChangeListener()
            AttributeCountChangeListener personAttrCnt = new AttributeCountChangeListener()

            if (refBookPerson != null) {


                primaryPerson.setId(refBookPerson.getId())
                StringBuilder infoMsgBuilder = new StringBuilder()
                Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(refBookPerson)
                boolean changed = false
                if (refBookPerson.needUpdate) {
                    //person
                    updatePersonAttr(refBookPersonValues, primaryPerson, personAttrCnt)
                    changed = declarationData.asnuId != refBookPerson.sourceId || personAttrCnt.isUpdate()
                    //address
                    if (primaryPerson.getAddress() != null) {
                        if (refBookPerson.getAddress() != null) {
                            Map<String, RefBookValue> refBookAddressValues = mapAddressAttr(refBookPerson.getAddress())

                            fillSystemAliases(refBookAddressValues, refBookPerson.getAddress())

                            updateAddressAttr(refBookAddressValues, primaryPerson.getAddress(), addressAttrCnt)

                            if (addressAttrCnt.isUpdate()) {
                                changed = true
                                updateAddressList.add(refBookAddressValues)
                            }
                        }
                    }
                }

                //documents
                PersonDocument primaryPersonDocument = primaryPerson.getMajorDocument()
                if (primaryPersonDocument != null) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null
                    PersonDocument personDocument = BaseWeightCalculator.findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber())

                    if (personDocument == null) {
                        if (primaryPersonDocument.docType != null) {
                            primaryPersonDocument.documentNumber = performDocNumber(primaryPersonDocument)
                            insertDocumentList.add(primaryPersonDocument)
                            refBookPerson.getPersonDocumentList().add(primaryPersonDocument)
                            changed = true
                            infoMsgBuilder.append(String.format("[Добавлена запись о новом ДУЛ:  \"Код ДУЛ\" = \"%s\", \"Серия и номер ДУЛ\": \"%s\"]", primaryPersonDocument?.docType?.code, primaryPersonDocument?.getDocumentNumber()))
                        }
                    }
                }

                insertBatchRecords(RefBook.Id.ID_DOC.getId(), insertDocumentList, { PersonDocument personDocument ->
                    mapPersonDocumentAttr(personDocument)
                })

                //check inc report
                if (checkIncReportFlag(refBookPerson, updateDocumentList, infoMsgBuilder)) {
                    changed = true
                }
                refBookPersonValues.put("REPORT_DOC", new RefBookValue(RefBookAttributeType.REFERENCE, refBookPerson?.getMajorDocument()?.getId()))

                //identifiers
                PersonIdentifier primaryPersonIdentifier = primaryPerson.getPersonIdentifier()
                if (primaryPersonIdentifier != null) {
                    if (!containsPersonIdentifier(refBookPerson, primaryPersonIdentifier.getAsnuId(), primaryPersonIdentifier.getInp())) {
                        insertIdentifierList.add(primaryPersonIdentifier)
                        infoMsgBuilder.append(String.format("[Добавлен новый \"ИНП\": \"%s\", \"АСНУ\": \"%s\"]", primaryPersonIdentifier?.inp, asnuCache.get(primaryPersonIdentifier?.getAsnuId())))
                    }
                }

                Integer parentTbId = departmentService.getParentTBId(declarationData.departmentId)
                if (!refBookPerson.personTbList.tbDepartmentId.contains(parentTbId)) {
                    PersonTb personTb = new PersonTb()
                    personTb.naturalPerson = refBookPerson
                    personTb.tbDepartmentId = parentTbId
                    personTb.importDate = getDeclarationDataCreationDate()
                    insertPersonTbList.add(personTb)
                    Department department = getDepartment(parentTbId)
                    infoMsgBuilder.append(String.format("[Добавлен новый Тербанк: \"%s\"]", department.getShortName()))
                }


                if (changed) {
                    updatePersonList.add(refBookPersonValues)
                    fillSystemAliases(refBookPersonValues, refBookPerson)
                }
                updatePersonReferenceList.add(primaryPerson)

                if (addressAttrCnt.isUpdate() || personAttrCnt.isUpdate() || !infoMsgBuilder.toString().isEmpty()) {
                    logger.infoExp("Обновлена запись в Реестре физических лиц. Идентификатор ФЛ: %s, ФИО: %s. Изменено:  %s", "", String.format("%s, ИНП: %s", buildFio(primaryPerson), primaryPerson.getPersonIdentifier().getInp()), refBookPerson.getRecordId(), buildFio(primaryPerson), buildRefreshNotice(addressAttrCnt, personAttrCnt, infoMsgBuilder))
                    updCnt++
                }
            }
            if (msgCnt < maxMsgCnt) {
                logForDebug("Обновление (" + ScriptUtils.calcTimeMillis(inTime))
            }
            msgCnt++
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Обновление ФЛ, документов (" + ScriptUtils.calcTimeMillis(time))
        time = System.currentTimeMillis()

        insertPersonList.removeAll(updatePersonReferenceList)
        //update reference to ref book
        if (!updatePersonReferenceList.isEmpty()) {
            updatePrimaryToRefBookPersonReferences(updatePersonReferenceList)
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Обновление справочников (" + ScriptUtils.calcTimeMillis(time))
        time = System.currentTimeMillis()


        insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), insertIdentifierList, { PersonIdentifier personIdentifier ->
            mapPersonIdentifierAttr(personIdentifier)
        })

        insertBatchRecords(RefBook.Id.PERSON_TB.getId(), insertPersonTbList, { PersonTb personTb -> mapPersonTbAttr(personTb) })

        List<Map<String, RefBookValue>> refBookDocumentList = new ArrayList<Map<String, RefBookValue>>()

        for (PersonDocument personDoc : updateDocumentList) {
            ScriptUtils.checkInterrupted()
            Map<String, RefBookValue> values = mapPersonDocumentAttr(personDoc)
            fillSystemAliases(values, personDoc)
            refBookDocumentList.add(values)
        }

        for (Map<String, RefBookValue> refBookValues : updateAddressList) {
            ScriptUtils.checkInterrupted()
            Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
            getProvider(RefBook.Id.PERSON_ADDRESS.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues)
        }

        for (Map<String, RefBookValue> refBookValues : updatePersonList) {
            ScriptUtils.checkInterrupted()
            Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
            getProvider(RefBook.Id.PERSON.getId()).updateRecordVersionWithoutLock(logger, uniqueId, refBookValues.get("VERSION").getDateValue() as Date ?: null, null, refBookValues)
        }

        for (Map<String, RefBookValue> refBookValues : refBookDocumentList) {
            ScriptUtils.checkInterrupted()
            Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
            getProvider(RefBook.Id.ID_DOC.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues)
        }

        for (Map<String, RefBookValue> refBookValues : updateIdentifierList) {
            ScriptUtils.checkInterrupted()
            Long uniqueId = refBookValues.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
            getProvider(RefBook.Id.ID_TAX_PAYER.getId()).updateRecordVersionWithoutLock(logger, uniqueId, getRefBookPersonVersionFrom(), null, refBookValues)
        }

        logForDebug("Идентификация и обновление (" + ScriptUtils.calcTimeMillis(time))

        logForDebug("Обновлено записей: " + updCnt)

    }

    String performDocNumber(PersonDocument personDocument) {
        List<DocType> docTypes = getDocTypeRefBookList()
        String toReturn = personDocument.documentNumber
        if (docTypes.contains(personDocument.docType)) {
            String docNumber = personDocument.documentNumber
            if (ScriptUtils.checkDulSymbols(personDocument.docType.code, BaseWeightCalculator.prepareStringDul(personDocument.documentNumber).toUpperCase())) {
                toReturn = ScriptUtils.formatDocNumber(personDocument.docType.code, docNumber.replaceAll("[^А-Яа-я\\w]", "").toUpperCase())
            }
        }
        return toReturn
    }

    /**
     *  Собирает сообщение об обновлении атрибутов
     * @param addressAttrCnt слушатель обновления атрибута ФЛ
     * @param personAttrCnt слушатель обновления атрибута адреса
     * @param infoMsg билдер сообщения
     * @return сообщение об обновлении атрибутов
     */
    def buildRefreshNotice(AttributeCountChangeListener addressAttrCnt, AttributeCountChangeListener personAttrCnt, StringBuilder infoMsg) {
        StringBuffer sb = new StringBuffer()
        appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb)
        appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb)
        sb.append(infoMsg)
        return sb.toString()
    }

    /**
     * Проверяет существует ли у ФЛ запись с определенными ИНП и АСНУ
     * @param person объект ФЛ
     * @param asnuId значение АСНУ
     * @param inp значение ИНП
     * @return флаг указывающий имеется ли совпадение
     */
    static Boolean containsPersonIdentifier(NaturalPerson person, Long asnuId, String inp) {
        String primaryInp = BaseWeightCalculator.prepareString(inp)
        for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
            String refbookInp = BaseWeightCalculator.prepareString(personIdentifier.getInp())
            if ((asnuId != null && asnuId.equals(personIdentifier.getAsnuId())) && BaseWeightCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {
                return true
            }
        }
        return false
    }

    int[] updatePrimaryToRefBookPersonReferences(List<NaturalPerson> primaryDataRecords) {
        ScriptUtils.checkInterrupted()
        ndflPersonService.updateRefBookPersonReferences(primaryDataRecords)
    }

    /**
     * Метод устанавливает признак включения в отчетность на основе приоритета
     */
    boolean checkIncReportFlag(NaturalPerson naturalPerson, List<PersonDocument> updateDocumentList, StringBuilder messageBuilder) {
        boolean toReturn = false
        List personDocumentList = naturalPerson.getDocuments()

        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            //индекс документа в списке personDocumentList который выбран главным, всем остальным необходимо выставить статус incRep 0
            int incRepIndex = IdentificationUtils.selectIncludeReportDocumentIndex(naturalPerson, personDocumentList)

            for (int i = 0; i < personDocumentList.size(); i++) {

                //noinspection GroovyUncheckedAssignmentOfMemberOfRawType
                PersonDocument personDocument = personDocumentList.get(i)

                if (i == incRepIndex) {
                    if (naturalPerson?.majorDocument != null && naturalPerson.majorDocument.getId() != personDocument?.getId()) {
                        String oldValue = String.format("%s - (%s) %s", naturalPerson?.majorDocument?.getDocumentNumber(), naturalPerson?.majorDocument?.getDocType()?.getCode(), naturalPerson?.majorDocument?.getDocType()?.getName())
                        String newValue = String.format("%s - (%s) %s", personDocument?.getDocumentNumber(), personDocument?.getDocType()?.getCode(), personDocument?.getDocType()?.getName())
                        toReturn = true
                        personDocument.setIncRep(INCLUDE_TO_REPORT)
                        updateDocumentList.add(personDocument)
                        naturalPerson.setMajorDocument(personDocument)

                        messageBuilder.append(String.format("[ДУЛ, включаемый в отчетность: %s  ->  %s]", oldValue, newValue))
                    }

                } else {
                    personDocument.setIncRep(NOT_INCLUDE_TO_REPORT)
                    updateDocumentList.add(personDocument)
                }
            }

        }
        return toReturn
    }

    def updateAddressAttr(Map<String, RefBookValue> values, Address address, AttributeChangeListener attributeChangeListener) {
        putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType(), null, attributeChangeListener)
        putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId(), { Long val -> findCountryRecordId(val) }, attributeChangeListener)
        putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode(), null, attributeChangeListener)
        putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict(), null, attributeChangeListener)
        putOrUpdate(values, "CITY", RefBookAttributeType.STRING, address.getCity(), null, attributeChangeListener)
        putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality(), null, attributeChangeListener)
        putOrUpdate(values, "STREET", RefBookAttributeType.STRING, address.getStreet(), null, attributeChangeListener)
        putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse(), null, attributeChangeListener)
        putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, address.getBuild(), null, attributeChangeListener)
        putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment(), null, attributeChangeListener)
        putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode(), null, attributeChangeListener)
        putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno(), null, attributeChangeListener)
    }

    def updatePersonAttr(Map<String, RefBookValue> values, NaturalPerson person, AttributeChangeListener attributeChangeListener) {
        putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId, { Long val -> findAsnuCodeById(val) }, attributeChangeListener)
        putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), null, attributeChangeListener)
        putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), null, attributeChangeListener)
        putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), null, attributeChangeListener)
        putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), null, attributeChangeListener)
        putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId(), { Long val -> findCountryRecordId(val) }, attributeChangeListener)
        putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), null, attributeChangeListener)
        putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), null, attributeChangeListener)
        putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), null, attributeChangeListener)
        putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId(), { Long val -> findTaxpayerStatusById(val) }, attributeChangeListener)
    }

    static Map<String, RefBookValue> mapAddressAttr(Address address) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode())
        putValue(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode())
        putValue(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict())
        putValue(values, "CITY", RefBookAttributeType.STRING, address.getCity())
        putValue(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality())
        putValue(values, "STREET", RefBookAttributeType.STRING, address.getStreet())
        putValue(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse())
        putValue(values, "BUILD", RefBookAttributeType.STRING, address.getBuild())
        putValue(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment())
        putValue(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId())
        putValue(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno())
        putValue(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType())

        return values
    }

    Map<String, RefBookValue> mapPersonAttr(NaturalPerson person) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>()
        putValue(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName())
        putValue(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName())
        putValue(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName())
        putValue(values, "INN", RefBookAttributeType.STRING, person.getInn())
        putValue(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign())
        putValue(values, "SNILS", RefBookAttributeType.STRING, person.getSnils())
        putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId())
        putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate())
        putValue(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null)
        putValue(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId())
        putValue(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId())
        putValue(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId())
        putValue(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId)
        putValue(values, "REPORT_DOC", RefBookAttributeType.REFERENCE, person.getMajorDocument()?.getId())
        return values
    }

    /**
     * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
     */

    static putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, Closure getValue, AttributeChangeListener attributeChangedListener) {
        putOrUpdate(valuesMap, attrName, type, value, getValue != null ? getValue : ({ val -> val?.toString() }), attributeChangedListener, { RefBookAttributeType attrType, Object valueA, Object valueB ->
            isAttrEquals(attrType, valueA, valueB)
        })
    }

    static void putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
        values.put(attrName, new RefBookValue(type, value))
    }

    static putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, Closure getValue, AttributeChangeListener attributeChangedListener, Closure attrEquator) {

        AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, getValue(value))

        RefBookValue refBookValue = valuesMap.get(attrName)
        if (refBookValue != null) {
            //обновление записи, если новое значение задано и отличается от существующего
            changeEvent.setCurrentValue(getValue(refBookValue.getValue()))

            if (value != null && !attrEquator(type, refBookValue.getValue(), value)) {
                //значения не равны, обновление
                changeEvent.setType(AttributeChangeEventType.REFRESHED)
                attributeChangedListener.processAttr(changeEvent)
                refBookValue.setValue(value)
            }
        } else {
            //создание новой записи
            valuesMap.put(attrName, new RefBookValue(type, value))
            changeEvent.setType(AttributeChangeEventType.CREATED)
            refBookValue.setValue(value)
        }
    }

    static Map<String, RefBookValue> mapPersonDocumentAttr(PersonDocument personDocument) {
        Map<String, RefBookValue> values = new HashMap<>()
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personDocument.getNaturalPerson().getId())
        putValue(values, "DOC_NUMBER", RefBookAttributeType.STRING, personDocument.getDocumentNumber())
        def incRepVal = personDocument.getIncRep() != null ? personDocument.getIncRep() : 1
        putValue(values, "INC_REP", RefBookAttributeType.NUMBER, incRepVal)//default value is 1
        putValue(values, "DOC_ID", RefBookAttributeType.REFERENCE, personDocument.getDocType()?.getId())
        return values
    }

    static Map<String, RefBookValue> mapPersonIdentifierAttr(PersonIdentifier personIdentifier) {
        Map<String, RefBookValue> values = new HashMap<>()
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personIdentifier.getNaturalPerson().getId())
        putValue(values, "INP", RefBookAttributeType.STRING, personIdentifier.getInp())
        putValue(values, "AS_NU", RefBookAttributeType.REFERENCE, personIdentifier.getAsnuId())
        return values
    }

    static Map<String, RefBookValue> mapPersonTbAttr(PersonTb personTb) {
        Map<String, RefBookValue> values = new HashMap<>()
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personTb.getNaturalPerson().getId())
        putValue(values, "TB_DEPARTMENT_ID", RefBookAttributeType.REFERENCE, (long) personTb.tbDepartmentId)
        putValue(values, "IMPORT_DATE", RefBookAttributeType.DATE, personTb.importDate)
        return values
    }

    /**
     * Получить все записи справочника по его идентификатору
     * @param refBookId - идентификатор справочника
     * @return - список всех версий всех записей справочника
     */
    List<Map<String, RefBookValue>> getRefBookAll(long refBookId) {
        def recordData = getProvider(refBookId).getRecordDataWhere("1 = 1")
        def refBookList = []
        if (recordData != null) {
            recordData.each { key, value ->
                refBookList.add(value)
            }
        }

        if (refBookList.size() == 0) {
            throw new ServiceException("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def insertBatchRecords(Long refBookId, List identityObjectList, Closure refBookMapper) {

        //подготовка записей
        if (identityObjectList != null && !identityObjectList.isEmpty()) {
            String refBookName = getProvider(refBookId).refBook.name
            logForDebug("Добавление записей: cправочник «${refBookName}», количество ${identityObjectList.size()}")

            identityObjectList.collate(1000).each { identityObjectSubList ->
                if (identityObjectSubList != null && !identityObjectSubList.isEmpty()) {

                    List<RefBookRecord> recordList = new ArrayList<RefBookRecord>()
                    for (IdentityObject identityObject : identityObjectSubList) {

                        ScriptUtils.checkInterrupted()

                        Map<String, RefBookValue> values = refBookMapper(identityObject)
                        recordList.add(createRefBookRecord(values))
                    }

                    //создание записей справочника
                    List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, getRefBookPersonVersionFrom(), null, recordList)

                    //установка id
                    for (int i = 0; i < identityObjectSubList.size(); i++) {

                        ScriptUtils.checkInterrupted()

                        Long id = generatedIds.get(i)
                        IdentityObject identityObject = identityObjectSubList.get(i)
                        identityObject.setId(id)
                    }
                }
            }
        }
    }

    /**
     * Создание новой записи справочника адреса физлиц
     * @param person
     * @return
     */
    static RefBookRecord createRefBookRecord(Map<String, RefBookValue> values) {
        RefBookRecord record = new RefBookRecord()
        putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, null)
        record.setValues(values)
        return record
    }

    static void fillSystemAliases(Map<String, RefBookValue> values, RefBookObject refBookObject) {
        values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getId()))
        values.put("RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getRecordId()))
        values.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, refBookObject.getVersion()))
        values.put("STATUS", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getStatus()))
    }

    /**
     * Объединяет информациионную часть сообщения с префиксом-атрибутом
     * @param refBookId идентификатор справочника
     * @param attrCounter слушатель атрибута
     * @param sb билдер сообщения
     */
    def appendAttrInfo(Long refBookId, AttributeCountChangeListener attrCounter, StringBuffer sb) {
        if (attrCounter != null && attrCounter.isUpdate()) {
            for (Map.Entry<String, String> msgEntry : attrCounter.getMessages()) {
                String aliasKey = msgEntry.getKey()
                String msg = msgEntry.getValue()
                sb.append("[")
                        .append(getAttrNameFromRefBook(refBookId, aliasKey))
                        .append(": ")
                        .append(msg)
                        .append("]")
            }
        }
    }

    String getAttrNameFromRefBook(Long id, String alias) {
        Map<String, String> attrMap = refBookAttrCache.get(id)
        if (attrMap != null) {
            return attrMap.get(alias)
        } else {
            attrMap = new HashMap<String, String>()
            RefBook refBook = getRefBookFromCache(id)
            List<RefBookAttribute> refBookAttributeList = refBook.getAttributes()
            for (RefBookAttribute attr : refBookAttributeList) {
                if (id == RefBook.Id.PERSON.id && attr.getAlias() == "INN") {
                    attrMap.put(attr.getAlias(), "ИНН в РФ")
                } else if (id == RefBook.Id.PERSON_ADDRESS.id && attr.getAlias() == "LOCALITY") {
                    attrMap.put(attr.getAlias(), "Нас. пункт")
                } else if (id == RefBook.Id.PERSON_ADDRESS.id && attr.getAlias() == "STREET") {
                    attrMap.put(attr.getAlias(), "Улица")
                } else if (id == RefBook.Id.PERSON_ADDRESS.id && attr.getAlias() == "HOUSE") {
                    attrMap.put(attr.getAlias(), "Дом")
                } else if (id == RefBook.Id.PERSON_ADDRESS.id && attr.getAlias() == "BUILDING") {
                    attrMap.put(attr.getAlias(), "Корпус")
                } else if (id == RefBook.Id.PERSON_ADDRESS.id && attr.getAlias() == "APPARTMENT") {
                    attrMap.put(attr.getAlias(), "Квартира")
                } else {
                    attrMap.put(attr.getAlias(), attr.getName());
                }
            }
            refBookAttrCache.put(id, attrMap)
            return attrMap.get(alias)
        }
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

    Date getRefBookPersonVersionFrom() {
        return getReportPeriodStartDate()
    }

    /**
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
    }

    static Boolean isAttrEquals(RefBookAttributeType type, Object valueA, Object valueB) {
        if (type.equals(RefBookAttributeType.STRING)) {
            return BaseWeightCalculator.isEqualsNullSafeStr((String) valueA, (String) valueB)
        } else {
            return ScriptUtils.equalsNullSafe(valueA, valueB)
        }
    }

    /**
     * Получение провайдера с использованием кеширования.
     * @param providerId
     * @return
     */
    RefBookDataProvider getProvider(long providerId) {
        if (!providerCache.containsKey(providerId)) {
            providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
        }
        return providerCache.get(providerId)
    }

    /**
     * По id страны найти код записи в кэше справочника
     * @param String code
     * @return Long id
     */
    String findCountryRecordId(Long id) {
        Map<Long, String> citizenshipCodeMap = getRefCountryCode()
        return id != null ? (citizenshipCodeMap.get(id) ?: "") : ""
    }

    /**
     * По id статуса налогоплательщика найти код статуса в кэше справочника
     * @param String code
     * @return Long id
     */
    String findTaxpayerStatusById(Long id) {
        Map<Long, String> taxpayerStatusMap = getRefTaxpayerStatusCode()
        return id != null ? (taxpayerStatusMap.get(id) ?: "") : ""
    }

    /**
     * По id АСНУ найти код записи в кэше справочника
     * @param String code
     * @return Long id
     */
    String findAsnuCodeById(Long id) {
        Map<Long, String> asnuCodeMap = getRefAsnu()
        return id != null ? (asnuCodeMap.get(id) ?: "") : ""
    }

    RefBook getRefBookFromCache(Long id) {
        RefBook refBook = mapRefBookToIdCache.get(id)
        if (refBook != null) {
            return refBook
        } else {
            refBook = commonRefBookService.get(id)
            mapRefBookToIdCache.put(id, refBook)
            return refBook
        }
    }

    /**
     * Получить "Страны"
     * @return
     */
    Map<Long, String> getRefCountryCode() {
        if (countryCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.COUNTRY.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                countryCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return countryCodeCache
    }

    /**
     * Получить "Статусы налогоплательщика"
     * @return
     */
    Map<Long, String> getRefTaxpayerStatusCode() {
        if (taxpayerStatusCodeCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                taxpayerStatusCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return taxpayerStatusCodeCache
    }

    /**
     * Получить "АСНУ"
     * @return
     */
    Map<Long, String> getRefAsnu() {
        if (asnuCache.size() == 0) {
            PagingResult<Map<String, RefBookValue>> refBookMap = getRefBook(RefBook.Id.ASNU.id)
            refBookMap.each { Map<String, RefBookValue> refBook ->
                Long asnuId = (Long) refBook?.id?.numberValue
                asnuCache.put(asnuId, refBook?.CODE?.stringValue)
                asnuPriority.put(asnuId, (Integer) refBook?.PRIORITY?.numberValue)
            }
        }
        return asnuCache
    }

    /**
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    PagingResult<Map<String, RefBookValue>> getRefBook(long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        PagingResult<Map<String, RefBookValue>> refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

    void updateAndCheckException(Closure<Object> update) {
        try {
            update()
        } catch (Exception e) {
            int i = ExceptionUtils.indexOfThrowable(e, SQLSyntaxErrorException.class)
            if (i != -1) {
                SQLSyntaxErrorException sqlSyntaxErrorException = (SQLSyntaxErrorException) ExceptionUtils.getThrowableList(e).get(i)
                if (sqlSyntaxErrorException.getLocalizedMessage().contains("ORA-02049") || sqlSyntaxErrorException.getLocalizedMessage().contains("ORA-00060")) {
                    e.printStackTrace()
                    logger.error("Невозможно выполнить обновление записей Реестра физических лиц при выполнении операции \"Идентификация ФЛ\" " +
                            "для налоговой формы №: ${declarationData.id}. Записи Реестра физических лиц используются при выполнении операции \"Идентификация ФЛ\" " +
                            "для другой налоговой формы. Выполните операцию позднее.")
                    return
                }
            }
            throw e
        }
    }

    /**
     * Для документов, включаемых в отчетность, добавляем взаимную ссылку в REF_BOOK_PERSON
     */
    void addReportDocsToPersons(List<PersonDocument> documents) {
        documents.each { PersonDocument document ->
            if (document.isIncludeReport() && document.naturalPerson) {
                addDocToItsPerson(document)
            }
        }
    }

    void addDocToItsPerson(PersonDocument document) {
        Long personId = document.naturalPerson.id
        setPersonReportDoc(personId, document.id)
    }

    void setPersonReportDoc(Long personId, Long documentId) {
        def personProvider = getProvider(RefBook.Id.PERSON.id)

        Map<String, RefBookValue> personFields = personProvider.getRecordData(personId)
        personFields.put('REPORT_DOC', new RefBookValue(RefBookAttributeType.NUMBER, documentId))
        personProvider.updateRecordVersionWithoutLock(logger, personId, null, null, personFields)
    }

    /**
     * Объединяет фамилию имя и отчество физлица
     * @param naturalPerson объект физлица
     * @return строка вида <фамилия> <имя> <отчество>
     */
    static String buildFio(NaturalPerson naturalPerson) {
        return naturalPerson.lastName + " " + naturalPerson.firstName + (naturalPerson.middleName ? " " + naturalPerson.middleName : "")
    }

    /**
     * Получить дату создания налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @return дата создания налоговой формы
     */
    Date getDeclarationDataCreationDate() {
        if (declarationDataCreationDate == null) {
            declarationDataCreationDate = declarationService.getDeclarationDataCreationDate(declarationData.id)
        }
        return declarationDataCreationDate
    }

    /**
     * Получить объект подразделения
     * @param id идентфикатор подразделения
     * @return объект подразделения
     */
    Department getDepartment(Integer id) {
        Department result = departmentCache.get(id)
        if (result == null) {
            result = departmentService.get(id)
            departmentCache.put(id, result)
        }
        return result
    }
}