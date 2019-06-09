package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
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
import com.aplana.sbrf.taxaccounting.model.identification.PersonalData
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier
import com.aplana.sbrf.taxaccounting.model.identification.RefBookObject
import com.aplana.sbrf.taxaccounting.model.identification.TaxpayerStatus
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils

import org.apache.commons.lang3.exception.ExceptionUtils;
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import java.sql.SQLSyntaxErrorException

new Calculate(this).run();

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
    RefBookService refBookService
    PersonService personService

    /**
     * Получить версию используемую для поиска записей в справочнике ФЛ
     */
    Date refBookPersonVersionTo = null;

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    Integer INCLUDE_TO_REPORT = 1;

    Integer NOT_INCLUDE_TO_REPORT = 0;

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

    final String T_PERSON = "1" //"Реквизиты"

    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""

    // Справочники
    final String R_ID_DOC_TYPE = "Коды документов"
    final String R_STATUS = "Статусы налогоплательщика"

    List<Country> countryRefBookCache = []
    List<DocType> docTypeRefBookCache = [];
    List<TaxpayerStatus> taxpayerStatusRefBookCache = [];

    Map<Long, Map<String, String>> refBookAttrCache = new HashMap<Long, Map<String, String>>();
    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]
    HashMap<Long, RefBook> mapRefBookToIdCache = new HashMap<Long, RefBook>();
    //Коды стран из справочника
    Map<Long, String> countryCodeCache = [:]
    //Коды статуса налогоплательщика
    Map<Long, String> taxpayerStatusCodeCache = [:]
    //Коды Асну
    Map<Long, String> asnuCache = [:]
    //Приоритет Асну
    Map<Long, Integer> asnuPriority = [:];
    /**
     * Мапа используется при определении дубликатов между физлицами которые еще не имеют ссылок на справочник физлиц.
     * Ключем здесь выступает физлицо, которе будет оригиналом и для которого будет создана запись в справочнике ФЛ,
     * а значением является список физлиц которые будут ссылаться на запись в справочнике ФЛ созданную для оригинала.
     */
    Map<NaturalPerson, List<NaturalPerson>> primaryPersonOriginalDuplicates = new HashMap<>();

    /**
     * Список физлиц для вставки
     */
    List<NaturalPerson> insertPersonList = []

    /**
     * Список физлиц дубликатов
     */
    List<NaturalPerson> duplicatePersonList = []

    List<NaturalPerson> primaryPersonDataList = []

    private Calculate() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Calculate(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("calculateParams")) {
            this.calculateParams = (Map<String, Object>) scriptClass.getProperty("calculateParams");
        }
        if (scriptClass.getBinding().hasVariable("refBookPersonService")) {
            this.refBookPersonService = (RefBookPersonService) scriptClass.getProperty("refBookPersonService");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getProperty("refBookService")
        }
        if (scriptClass.getBinding().hasVariable("personService")) {
            this.personService = (PersonService) scriptClass.getProperty("personService")
        }
    }

    @Override
    public void run() {
        initConfiguration();
        updateAndCheckException({
            switch (formDataEvent) {
                case FormDataEvent.CALCULATE:
                    long timeFull = System.currentTimeMillis();
                    long time = System.currentTimeMillis();

                    logForDebug("Начало расчета ПНФ");

                    if (declarationData.asnuId == null) {
                        throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!");
                    }

                    //выставляем параметр что скрипт не формирует новый xml-файл
                    calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

                    refBookPersonService.clearRnuNdflPerson(declarationData.id)

                    //Получаем список всех ФЛ в первичной НФ
                    primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFromNdfl(declarationData.id, createPrimaryRowMapper(false));
                    if (logger.containsLevel(LogLevel.ERROR)) {
                        return
                    }

                    insertPersonList.addAll(primaryPersonDataList)

                    logForDebug("В ПНФ номер " + declarationData.id + " получены записи о физ. лицах (" + primaryPersonDataList.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

                    Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries {
                        [it.getPrimaryPersonId(), it]
                    }

                    //Заполнени временной таблицы версий
                    time = System.currentTimeMillis();
                    refBookPersonService.fillRecordVersions(getRefBookPersonVersionTo());
                    logForDebug("Заполнение таблицы версий (" + ScriptUtils.calcTimeMillis(time));

                    // Идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
                    time = System.currentTimeMillis();
                    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
                    logForDebug("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

                    time = System.currentTimeMillis();
                    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
                    logForDebug("Обновление записей (" + ScriptUtils.calcTimeMillis(time));

                    time = System.currentTimeMillis();
                    Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
                    logForDebug("Основная выборка по всем параметрам (" + checkSimilarityPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

                    time = System.currentTimeMillis();
                    updateNaturalPersonRefBookRecords(primaryPersonMap, checkSimilarityPersonMap);
                    logForDebug("Обновление записей (" + ScriptUtils.calcTimeMillis(time));

                    time = System.currentTimeMillis();
                    createNaturalPersonRefBookRecords();
                    logForDebug("Создание (" + insertPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

                    countTotalAndUniquePerson()
                    logForDebug("Завершение расчета ПНФ (" + ScriptUtils.calcTimeMillis(timeFull));
            }
        })
        // Формирование pdf-отчета формы
        // declarationService.createPdfReport(logger, declarationData, userInfo)
    }

    Date getRefBookPersonVersionTo() {
        if (refBookPersonVersionTo == null) {
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(Calendar.MONTH, 0);
            localCalendar.set(Calendar.DATE, 1);
            localCalendar.set(Calendar.HOUR_OF_DAY, 0);
            localCalendar.set(Calendar.MINUTE, 0);
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);
            localCalendar.add(Calendar.YEAR, 100);
            refBookPersonVersionTo = localCalendar.getTime();
        }
        return refBookPersonVersionTo;
    }

    // Вывод информации о количестве обработынных физлиц всего и уникальных
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
                    throw new ServiceException("Не проинициализирован кэш справочника 'Коды документов'!") as Throwable;
                }
                DocType result = docTypeCodeMap.get(code);
                String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                String inp = ndflPerson.getPersonIdentifier()?.inp ?: ""
                String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
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
                return result;
            }
        }
        naturalPersonRowMapper.setAsnuId(declarationData.asnuId);

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

        return naturalPersonRowMapper;
    }

    List<Country> getCountryRefBookList() {
        if (countryRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.COUNTRY.getId());
            refBookRecords.each { refBookValueMap ->
                Country country = new Country();
                country.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue());
                country.setCode(refBookValueMap?.get("CODE")?.getStringValue());
                countryRefBookCache.add(country);
            }
        }
        return countryRefBookCache;
    }

    List<DocType> getDocTypeRefBookList() {
        if (docTypeRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.DOCUMENT_CODES.getId());
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

    List<TaxpayerStatus> getTaxpayerStatusRefBookList() {
        if (taxpayerStatusRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.TAXPAYER_STATUS.getId());
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
            String inp = person.personIdentityList.get(0).getInp()
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
            outer: for (NaturalPerson person : processingPersonList) {
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

        int createCnt = 0;
        if (!insertPersonList.isEmpty()) {

            performPrimaryPersonDuplicates()

            List<Address> addressList = new ArrayList<Address>();
            List<PersonDocument> documentList = new ArrayList<PersonDocument>();
            List<PersonIdentifier> identifierList = new ArrayList<PersonIdentifier>();

            for (NaturalPerson person : insertPersonList) {

                ScriptUtils.checkInterrupted();

                Address address = person.getAddress();
                if (address != null) {
                    addressList.add(address);
                }

                PersonDocument personDocument = person.getPersonDocument();
                if (personDocument != null && personDocument.docType != null) {
                    personDocument.documentNumber = performDocNumber(personDocument)
                    documentList.add(personDocument);
                }

                PersonIdentifier personIdentifier = person.getPersonIdentifier();
                if (personIdentifier != null) {
                    identifierList.add(personIdentifier);
                }

            }

            //insert addresses batch
            insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), addressList, { Address address ->
                mapAddressAttr(address)
            });

            //insert persons batch
            insertBatchRecords(RefBook.Id.PERSON.getId(), insertPersonList, { NaturalPerson person ->
                mapPersonAttr(person)
            });

            //insert documents batch
            insertBatchRecords(RefBook.Id.ID_DOC.getId(), documentList, { PersonDocument personDocument ->
                mapPersonDocumentAttr(personDocument)
            });

            //insert identifiers batch
            insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), identifierList, { PersonIdentifier personIdentifier ->
                mapPersonIdentifierAttr(personIdentifier)
            });

            //update reference to ref book

            updatePrimaryToRefBookPersonReferences(insertPersonList);

            for (Map.Entry<NaturalPerson, List<NaturalPerson>> entry : primaryPersonOriginalDuplicates.entrySet()) {
                for (NaturalPerson duplicatePerson : entry.getValue()) {
                    duplicatePerson.id = entry.getKey().id
                }
                updatePrimaryToRefBookPersonReferences(entry.getValue());
            }

            //Выводим информацию о созданных записях
            for (NaturalPerson person : insertPersonList) {
                Long recordId = refBookService.getNumberValue(RefBook.Id.PERSON.id, person.getId(), "record_id").longValue()
                String noticeMsg = String.format("Создана новая запись в справочнике 'Физические лица': %d, %s %s %s", recordId, person.getLastName(), person.getFirstName(), (person.getMiddleName() ?: ""));
                logger.info(noticeMsg);
                createCnt++;
            }
        }

        logForDebug("Создано записей: " + createCnt)
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

    def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

        long time = System.currentTimeMillis();

        //println "updateNaturalPersonRefBookRecords similarityPersonMap.size=" + similarityPersonMap.size()

        //Проходим по списку и определяем наиболее подходящюю запись, если подходящей записи не найдено то содадим ее
        List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>();

        //список записей для обновления атрибутов справочника физлиц
        Set<Map<String, RefBookValue>> updatePersonList = new LinkedHashSet<>();

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

            IdentificationData identificationData = new IdentificationData()
            identificationData.naturalPerson = primaryPerson
            identificationData.refBookPersonList = similarityPersonList
            identificationData.tresholdValue = similarityThreshold
            identificationData.declarationDataAsnuId = declarationData.asnuId
            if (asnuPriority.isEmpty()) {
                getRefAsnu()
            }
            identificationData.priorityMap = asnuPriority

            NaturalPerson refBookPerson = refBookPersonService.identificatePerson(identificationData, logger);

            conformityMap.put(primaryPersonId, refBookPerson);

            //Адрес нужно создать заранее и получить Id
            if (refBookPerson != null) {
                if (primaryPerson.getAddress() != null && refBookPerson.getAddress() == null) {
                    insertAddressList.add(primaryPerson.getAddress());
                }
            }

            if (msgCnt <= maxMsgCnt) {
                logForDebug("Идентификация (" + ScriptUtils.calcTimeMillis(inTime));
            }

            msgCnt++;
        }

        logForDebug("Идентификация ФЛ, обновление адресов (" + ScriptUtils.calcTimeMillis(time));

        insertBatchRecords(RefBook.Id.PERSON_ADDRESS.getId(), insertAddressList, { Address address ->
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

                if (!refBookPerson.needUpdate) {
                    updatePersonReferenceList.add(primaryPerson);
                    continue;
                }
                /*
               Если загружаемая НФ находится в периоде который заканчивается раньше чем версия записи в справочнике,
               тогда версия записи в справочнике меняется на более раннюю дату, без изменения атрибутов. Такая ситуация
               вряд ли может возникнуть на практике и проверка создана по заданию тестировщиков.
              */
                if (refBookPerson.getVersion() > getReportPeriodEndDate()) {
                    Map<String, RefBookValue> downgradePerson = mapPersonAttr(refBookPerson)
                    downGradeRefBookVersion(downgradePerson, refBookPerson.getId(), RefBook.Id.PERSON.getId())
                }

                //person
                Map<String, RefBookValue> refBookPersonValues = mapPersonAttr(refBookPerson);
                fillSystemAliases(refBookPersonValues, refBookPerson);
                updatePersonAttr(refBookPersonValues, primaryPerson, personAttrCnt);
                if (declarationData.asnuId != refBookPerson.sourceId || personAttrCnt.isUpdate()) {
                    updatePersonList.add(refBookPersonValues);
                }

                //address
                if (primaryPerson.getAddress() != null) {
                    if (refBookPerson.getAddress() != null) {
                        Map<String, RefBookValue> refBookAddressValues = mapAddressAttr(refBookPerson.getAddress());

                        fillSystemAliases(refBookAddressValues, refBookPerson.getAddress());

                        updateAddressAttr(refBookAddressValues, primaryPerson.getAddress(), addressAttrCnt);

                        if (addressAttrCnt.isUpdate()) {
                            updateAddressList.add(refBookAddressValues);
                            updatePersonList.add(refBookPersonValues);
                        }
                    }
                }

                //documents
                PersonDocument primaryPersonDocument = primaryPerson.getPersonDocument();
                if (primaryPersonDocument != null) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null;
                    PersonDocument personDocument = BaseWeightCalculator.findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber());

                    if (personDocument == null) {
                        if (primaryPersonDocument.docType != null) {
                            primaryPersonDocument.documentNumber = performDocNumber(primaryPersonDocument)
                            insertDocumentList.add(primaryPersonDocument);
                            refBookPerson.getPersonDocumentList().add(primaryPersonDocument);
                            updatePersonList.add(refBookPersonValues);
                        }
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

                        String primaryInp = BaseWeightCalculator.prepareString(primaryPersonIdentifier.getInp());
                        String refbookInp = BaseWeightCalculator.prepareString(refBookPersonIdentifier.getInp());

                        if (!BaseWeightCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {

                            AttributeChangeEvent changeEvent = new AttributeChangeEvent("INP", primaryInp);
                            changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.STRING, refbookInp));
                            changeEvent.setType(AttributeChangeEventType.REFRESHED);
                            taxpayerIdentityAttrCnt.processAttr(changeEvent);

                            Map<String, RefBookValue> refBookPersonIdentifierValues = mapPersonIdentifierAttr(primaryPersonIdentifier);
                            fillSystemAliases(refBookPersonIdentifierValues, refBookPersonIdentifier);
                            updateIdentifierList.add(refBookPersonIdentifierValues);
                            updatePersonList.add(refBookPersonValues);
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
            }

            if (msgCnt < maxMsgCnt) {
                logForDebug("Обновление (" + ScriptUtils.calcTimeMillis(inTime));
            }

            msgCnt++;

        }

        logForDebug("Обновление ФЛ, документов (" + ScriptUtils.calcTimeMillis(time));
        time = System.currentTimeMillis();
        //println "crete and update reference"

        insertPersonList.removeAll(updatePersonReferenceList)
        //update reference to ref book
        if (!updatePersonReferenceList.isEmpty()) {
            updatePrimaryToRefBookPersonReferences(updatePersonReferenceList);
        }

        logForDebug("Обновление справочников (" + ScriptUtils.calcTimeMillis(time));
        time = System.currentTimeMillis();

        insertBatchRecords(RefBook.Id.ID_DOC.getId(), insertDocumentList, { PersonDocument personDocument ->
            mapPersonDocumentAttr(personDocument)
        });

        insertBatchRecords(RefBook.Id.ID_TAX_PAYER.getId(), insertIdentifierList, { PersonIdentifier personIdentifier ->
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

        logForDebug("Идентификация и обновление (" + ScriptUtils.calcTimeMillis(time));

        logForDebug("Обновлено записей: " + updCnt);

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
        return toReturn;
    }

    def buildRefreshNotice(AttributeCountChangeListener addressAttrCnt, AttributeCountChangeListener personAttrCnt, AttributeCountChangeListener documentAttrCnt, AttributeCountChangeListener taxpayerIdentityAttrCnt) {
        StringBuffer sb = new StringBuffer();
        appendAttrInfo(RefBook.Id.PERSON_ADDRESS.getId(), addressAttrCnt, sb);
        appendAttrInfo(RefBook.Id.PERSON.getId(), personAttrCnt, sb);
        appendAttrInfo(RefBook.Id.ID_DOC.getId(), documentAttrCnt, sb);
        appendAttrInfo(RefBook.Id.ID_TAX_PAYER.getId(), taxpayerIdentityAttrCnt, sb);
        return sb.toString();
    }

    PersonIdentifier findIdentifierByAsnu(NaturalPerson person, Long asnuId) {
        for (PersonIdentifier personIdentifier : person.getPersonIdentityList()) {
            if (asnuId != null && asnuId.equals(personIdentifier.getAsnuId())) {
                return personIdentifier;
            }
        }
        return null;
    }

    def downGradeRefBookVersion(Map<String, RefBookValue> refBookValue, Long uniqueRecordId, Long refBookId) {
        Date newVersion = getReportPeriodStartDate()
        refBookValue.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, newVersion))
        getProvider(refBookId).updateRecordVersionWithoutLock(logger, uniqueRecordId, newVersion, null, refBookValue)
    }

    int[] updatePrimaryToRefBookPersonReferences(List<NaturalPerson> primaryDataRecords) {
        ScriptUtils.checkInterrupted();
        ndflPersonService.updateRefBookPersonReferences(primaryDataRecords);
    }

    /**
     * Метод устанавливает признак включения в отчетность на основе приоритета
     */
    def checkIncReportFlag(NaturalPerson naturalPerson, List<PersonDocument> updateDocumentList, AttributeCountChangeListener attrChangeListener) {

        List personDocumentList = naturalPerson.getPersonDocumentList();

        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            //индекс документа в списке personDocumentList который выбран главным, всем остальным необходимо выставить статус incRep 0
            int incRepIndex = IdentificationUtils.selectIncludeReportDocumentIndex(naturalPerson, personDocumentList);

            for (int i = 0; i < personDocumentList.size(); i++) {

                PersonDocument personDocument = personDocumentList.get(i);
                String docInf = new StringBuilder().append(personDocument.getId() ?: "0").append(", ").append(personDocument.getDocumentNumber()).append(" ").toString();

                if (i == incRepIndex) {
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
        putOrUpdate(values, "ADDRESS_TYPE", RefBookAttributeType.NUMBER, address.getAddressType(), null, attributeChangeListener);
        putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId(), { Long val -> findCountryRecordId(val) }, attributeChangeListener);
        putOrUpdate(values, "REGION_CODE", RefBookAttributeType.STRING, address.getRegionCode(), null, attributeChangeListener);
        putOrUpdate(values, "DISTRICT", RefBookAttributeType.STRING, address.getDistrict(), null, attributeChangeListener);
        putOrUpdate(values, "CITY", RefBookAttributeType.STRING, address.getCity(), null, attributeChangeListener);
        putOrUpdate(values, "LOCALITY", RefBookAttributeType.STRING, address.getLocality(), null, attributeChangeListener);
        putOrUpdate(values, "STREET", RefBookAttributeType.STRING, address.getStreet(), null, attributeChangeListener);
        putOrUpdate(values, "HOUSE", RefBookAttributeType.STRING, address.getHouse(), null, attributeChangeListener);
        putOrUpdate(values, "BUILD", RefBookAttributeType.STRING, address.getBuild(), null, attributeChangeListener);
        putOrUpdate(values, "APPARTMENT", RefBookAttributeType.STRING, address.getAppartment(), null, attributeChangeListener);
        putOrUpdate(values, "POSTAL_CODE", RefBookAttributeType.STRING, address.getPostalCode(), null, attributeChangeListener);
        putOrUpdate(values, "ADDRESS", RefBookAttributeType.STRING, address.getAddressIno(), null, attributeChangeListener);
    }

    def updatePersonAttr(Map<String, RefBookValue> values, NaturalPerson person, AttributeChangeListener attributeChangeListener) {
        putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), null, attributeChangeListener);
        putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), null, attributeChangeListener);
        putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), null, attributeChangeListener);
        putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), null, attributeChangeListener);
        putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), null, attributeChangeListener);
        putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(), null, attributeChangeListener);
        putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId(), null, attributeChangeListener);
        putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), null, attributeChangeListener);
        putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null, null, attributeChangeListener);
        putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId(), null, attributeChangeListener);
        putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee(), null, attributeChangeListener);
        putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId(), { Long val -> findCountryRecordId(val) }, attributeChangeListener);
        putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId(), { Long val -> findTaxpayerStatusById(val) }, attributeChangeListener);
        putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId, { Long val -> findAsnuCodeById(val) }, attributeChangeListener);
    }

    Map<String, RefBookValue> mapAddressAttr(Address address) {
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

    Map<String, RefBookValue> mapPersonAttr(NaturalPerson person) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        putValue(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName());
        putValue(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName());
        putValue(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName());
        putValue(values, "INN", RefBookAttributeType.STRING, person.getInn());
        putValue(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign());
        putValue(values, "SNILS", RefBookAttributeType.STRING, person.getSnils());
        putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId());
        putValue(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate());
        putValue(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null);
        putValue(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId());
        putValue(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee() ?: 2);
        putValue(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId());
        putValue(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId());
        putValue(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId);
        return values;
    }

    /**
     * Если не заполнен входной параметр, то никаких изменений в соответствующий атрибут записи справочника не вносится
     */

    def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, Closure getValue, AttributeChangeListener attributeChangedListener) {
        putOrUpdate(valuesMap, attrName, type, value, getValue != null ? getValue : ({ val -> val?.toString() }), attributeChangedListener, { RefBookAttributeType attrType, Object valueA, Object valueB ->
            isAttrEquals(attrType, valueA, valueB);
        });
    }

    void putValue(Map<String, RefBookValue> values, String attrName, RefBookAttributeType type, Object value) {
        values.put(attrName, new RefBookValue(type, value));
    }

    def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, Closure getValue, AttributeChangeListener attributeChangedListener, Closure attrEquator) {

        AttributeChangeEvent changeEvent = new AttributeChangeEvent(attrName, getValue(value));

        RefBookValue refBookValue = valuesMap.get(attrName);
        if (refBookValue != null) {
            //обновление записи, если новое значение задано и отличается от существующего
            changeEvent.setCurrentValue(getValue(refBookValue.getValue()));

            if (value != null && !attrEquator(type, refBookValue.getValue(), value)) {
                //значения не равны, обновление
                changeEvent.setType(AttributeChangeEventType.REFRESHED);
                attributeChangedListener.processAttr(changeEvent);
                refBookValue.setValue(value);
            }
        } else {
            //создание новой записи
            valuesMap.put(attrName, new RefBookValue(type, value));
            changeEvent.setType(AttributeChangeEventType.CREATED);
            refBookValue.setValue(value);
        }
    }

    Map<String, RefBookValue> mapPersonDocumentAttr(PersonDocument personDocument) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personDocument.getNaturalPerson().getId());
        putValue(values, "DOC_NUMBER", RefBookAttributeType.STRING, personDocument.getDocumentNumber());
        def incRepVal = personDocument.getIncRep() != null ? personDocument.getIncRep() : 1;
        putValue(values, "INC_REP", RefBookAttributeType.NUMBER, incRepVal); //default value is 1
        putValue(values, "DOC_ID", RefBookAttributeType.REFERENCE, personDocument.getDocType()?.getId());
        return values;
    }

    Map<String, RefBookValue> mapPersonIdentifierAttr(PersonIdentifier personIdentifier) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personIdentifier.getNaturalPerson().getId());
        putValue(values, "INP", RefBookAttributeType.STRING, personIdentifier.getInp());
        putValue(values, "AS_NU", RefBookAttributeType.REFERENCE, personIdentifier.getAsnuId());
        return values;
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

                    List<RefBookRecord> recordList = new ArrayList<RefBookRecord>();
                    for (IdentityObject identityObject : identityObjectSubList) {

                        ScriptUtils.checkInterrupted();

                        Map<String, RefBookValue> values = refBookMapper(identityObject);
                        recordList.add(createRefBookRecord(values));
                    }

                    //создание записей справочника
                    List<Long> generatedIds = getProvider(refBookId).createRecordVersionWithoutLock(logger, getRefBookPersonVersionFrom(), null, recordList);

                    //установка id
                    for (int i = 0; i < identityObjectSubList.size(); i++) {

                        ScriptUtils.checkInterrupted();

                        Long id = generatedIds.get(i);
                        IdentityObject identityObject = identityObjectSubList.get(i);
                        identityObject.setId(id);
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
    RefBookRecord createRefBookRecord(Map<String, RefBookValue> values) {
        RefBookRecord record = new RefBookRecord();
        putValue(values, "RECORD_ID", RefBookAttributeType.NUMBER, null);
        record.setValues(values);
        return record;
    }

    void fillSystemAliases(Map<String, RefBookValue> values, RefBookObject refBookObject) {
        values.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getId()));
        values.put("RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getRecordId()));
        values.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, refBookObject.getVersion()));
        values.put("STATUS", new RefBookValue(RefBookAttributeType.NUMBER, refBookObject.getStatus()));
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

    String getAttrNameFromRefBook(Long id, String alias) {
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
        return getReportPeriodStartDate();
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

    Boolean isAttrEquals(RefBookAttributeType type, Object valueA, Object valueB) {
        if (type.equals(RefBookAttributeType.STRING)) {
            return BaseWeightCalculator.isEqualsNullSafeStr((String) valueA, (String) valueB);
        } else if (type.equals(RefBookAttributeType.DATE)) {
            return ScriptUtils.equalsNullSafe(valueA, valueB);
        } else {
            return ScriptUtils.equalsNullSafe(valueA, valueB);
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
        RefBook refBook = mapRefBookToIdCache.get(id);
        if (refBook != null) {
            return refBook;
        } else {
            refBook = refBookFactory.get(id);
            mapRefBookToIdCache.put(id, refBook);
            return refBook;
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
        return countryCodeCache;
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
        return taxpayerStatusCodeCache;
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
        return asnuCache;
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
            int i = ExceptionUtils.indexOfThrowable(e, SQLSyntaxErrorException.class);
            if (i != -1) {
                SQLSyntaxErrorException sqlSyntaxErrorException = (SQLSyntaxErrorException) ExceptionUtils.getThrowableList(e).get(i)
                if (sqlSyntaxErrorException.getLocalizedMessage().contains("ORA-02049") || sqlSyntaxErrorException.getLocalizedMessage().contains("ORA-00060")) {
                    e.printStackTrace()
                    logger.error("Невозможно выполнить обновление записей справочника \"Физические лица\" при выполнении расчета налоговой формы номер: ${declarationData.id}. Записи справочника \"Физические лица\" используются при идентификации физических лиц в расчете другой налоговой формы. Выполните операцию позднее.")
                    return
                }
            }
            throw e;
        }
    }
}