package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
import com.aplana.sbrf.taxaccounting.dao.identification.RefDataHolder
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.NaturalPersonMapper
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.IdentificationData
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson
import com.aplana.sbrf.taxaccounting.model.identification.PersonalData
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.util.BaseWeightCalculator
import com.aplana.sbrf.taxaccounting.model.util.impl.PersonDataWeightCalculator
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LogBusinessService
import com.aplana.sbrf.taxaccounting.service.TAUserService
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.dao.DeadlockLoserDataAccessException
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

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
    LogBusinessService logBusinessService
    TAUserService taUserService
    Long taskDataId
    NamedParameterJdbcTemplate namedParameterJdbcTemplate

    // Дата окончания отчетного периода
    Date periodEndDate = null

    // Дата начала отчетного периода
    Date reportPeriodStartDate = null

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
    final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

    final String C_CITIZENSHIP = "Гражданство (код страны)"
    final String C_STATUS = "Статус (код)"
    final String C_ID_DOC_TYPE = "ДУЛ Код"

    final String T_PERSON = "1" //"Реквизиты"

    final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    final String CLAUSE_END_CREATE = "Физическое лицо будет создано в Реестре физических лиц без указания этого параметра"
    final String CLAUSE_END_UPDATE = "Это значение не будет сохраняться в Реестр физических лиц"

    // Справочники
    final String R_CITIZENSHIP = "Общероссийский классификатор стран мира"
    final String R_ID_DOC_TYPE = "Коды документов"
    final String R_STATUS = "Статусы налогоплательщика"

    List<RefBookCountry> countryRefBookCache = []
    List<RefBookDocType> docTypeRefBookCache = []
    List<RefBookTaxpayerState> taxpayerStatusRefBookCache = []

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]

    //Коды Асну
    Map<Long, RefBookAsnu> asnuCache = [:]

    /**
     * Мапа используется при определении дубликатов между физлицами которые еще не имеют ссылок на справочник физлиц.
     * Ключем здесь выступает физлицо, которе будет оригиналом и для которого будет создана запись в справочнике ФЛ,
     * а значением является список физлиц которые будут ссылаться на запись в справочнике ФЛ созданную для оригинала.
     */
    Map<Long, List<NaturalPerson>> primaryPersonOriginalDuplicates = new HashMap<>()

    /**
     * Список физлиц для вставки
     */
    List<NaturalPerson> insertPersonList = []

    /**
     * Список идентификаторов физлиц дубликатов
     */
    List<Long> duplicatePersonList = []

    List<NaturalPerson> primaryPersonDataList = []

    Map<Integer, Department> departmentCache = [:]

    /**
     *  ИНП дубликатов в разделе 1 сгруппированные по идентификатору версии ФЛ из Реестра ФЛ
     */
    Map<Long, List<PersonIdentifier>> duplicatedPersonIdentifiers = [:]
    /**
     *  Тербанки дубликатов в разделе 1 сгруппированные по идентификатору версии ФЛ из Реестра ФЛ
     */
    Map<Long, List<PersonTb>> duplicatedPersonTbs = [:]

    /**
     * Максимальный идентификатор в реестре ФЛ
     */
    Long maxRegistryPersonId

    /**
     * Флаг снимающийся после первой попытки создания ФЛ в Реестре
     */
    Boolean firstAttemptToCreate = true

    /**
     * Инкапсулирует данные справочников для передачи в мапперы
     */
    RefDataHolder refDataHolder

    /**,
     * Счетчик того сколько раз поток отправился в сон ожидая снятия блокировки с реестра ФЛ
     */
    int sleepCounter

    /**
     * Максимальное количество попыток создать Физлиц в Реестре ФЛ
     */
    int maxLockAttempts = 300

    /**
     * Время (мс) между попытками создать записи в реестре ФЛ
     */
    long sleepBetweenTryLock = 5000L

    boolean runParallel

    String snilsQuery = "with t as (select np.id, np.search_snils from ndfl_person np where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person3.id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from  t join ref_book_person person on (t.search_snils is not null and person.search_snils = t.search_snils and person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) ) where  person.record_id=person.old_id) person2 join ref_book_person person3 on (person3.id=person2.ref_book_person_id)"
    String inpQuery = "with t as (select * from ndfl_person where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person2.ref_book_person_id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from t join ref_book_person person on (person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) )) where person.record_id=person.old_id and t.inp in (select inp from ref_book_id_tax_payer where person_id = person.id and as_nu = :asnu)) person2"
    String fioQuery = "with t as (select np.id, np.search_last_name, np.search_first_name, np.search_middle_name, np.birth_day from ndfl_person np where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person3.id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from t join ref_book_person person on (person.search_last_name = t.search_last_name and person.search_first_name= t.search_first_name and person.search_middle_name= t.search_middle_name and person.birth_date=t.birth_day and person.start_date <= sysdate and ((person.end_date is null) or (person.end_date >= sysdate) ) ) where person.record_id=person.old_id) person2 join ref_book_person person3 on (person3.id=person2.ref_book_person_id)"
    String innQuery = "with t as (select np.id, np.search_inn from ndfl_person np where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person3.id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from  t join ref_book_person person on (t.search_inn is not null and person.search_inn = t.search_inn and person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) ) where person.record_id=person.old_id) person2 join ref_book_person person3 on (person3.id=person2.ref_book_person_id)"
    String innForeignQuery = "with t as (select np.id, np.search_inn_foreign from ndfl_person np where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person3.id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from t join ref_book_person person on (person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) and (t.search_inn_foreign is not null and person.search_inn_foreign = t.search_inn_foreign)) where person.record_id=person.old_id) person2 join ref_book_person person3 on (person3.id=person2.ref_book_person_id)"
    String dulQuery = "with t as (select np.id, np.id_doc_type, np.search_doc_number from ndfl_person np where declaration_data_id=:p_declaration and person_id is null) select person2.person_id, person3.id as ref_book_person_id from (select t.id as person_id, person.id as ref_book_person_id from t join ref_book_doc_type dt on (dt.code=t.id_doc_type) join ref_book_id_doc doc on (doc.doc_id=dt.id and doc.search_doc_number = t.search_doc_number) join (select distinct r1.id,r2.id id1 from ref_book_person r1 join ref_book_person r2 on r1.record_id=r2.record_id) a on (a.id1 = doc.person_id) join ref_book_person person on (person.id = a.id and person.start_date <= sysdate and ( (person.end_date is null) or (person.end_date >= sysdate) ) ) where person.record_id=person.old_id) person2 join ref_book_person person3 on (person3.id=person2.ref_book_person_id)"

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
        if (scriptClass.getBinding().hasVariable("logBusinessService")) {
            this.logBusinessService = (LogBusinessService) scriptClass.getProperty("logBusinessService")
        }
        if (scriptClass.getBinding().hasVariable("taUserService")) {
            this.taUserService = (TAUserService) scriptClass.getProperty("taUserService")
        }
        if (scriptClass.getBinding().hasVariable("taskDataId")) {
            this.taskDataId = (Long) scriptClass.getProperty("taskDataId")
        }
        this.namedParameterJdbcTemplate = (NamedParameterJdbcTemplate) scriptClass.getProperty("namedParameterJdbcTemplate")
    }

    @Override
    public void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CALCULATE:
                long timeFull = System.currentTimeMillis()
                long time = System.currentTimeMillis()

                logForDebug("Начало расчета ПНФ")
                println "Identification #=${declarationData.id} Начало расчета ПНФ"

                ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
                runParallel = configurationParamModel?.get(ConfigurationParam.ASYNC_SERIAL_MODE)?.get(0)?.get(0) == "0"

                maxRegistryPersonId = refBookPersonService.findMaxRegistryPersonId(new Date())

                if (declarationData.asnuId == null) {
                    //noinspection GroovyAssignabilityCheck
                    throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!")
                }

                refBookPersonService.clearRnuNdflPerson(declarationData.id)

                //Получаем список всех ФЛ в первичной НФ
                primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFromNdfl(declarationData.id, createPrimaryRowMapper())
                if (logger.containsLevel(LogLevel.ERROR)) {
                    return
                }

                insertPersonList.addAll(primaryPersonDataList)

                //noinspection GroovyAssignabilityCheck
                logForDebug("В ПНФ номер " + declarationData.id + " получены записи о физ. лицах (" + primaryPersonDataList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                println "Identification #=${declarationData.id} получены записи о физ. лицах (\" ${ primaryPersonDataList.size()} \" записей, \" + ${ScriptUtils.calcTimeMillis(time)}"

                Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries { NaturalPerson naturalPerson ->
                    [naturalPerson.getPrimaryPersonId(), naturalPerson]
                }

                if (!getPrimaryPersonDataList().isEmpty()) {
                    //Заполнени временной таблицы версий
                    time = System.currentTimeMillis()
                    refBookPersonService.fillRecordVersions()
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Заполнение таблицы версий (" + ScriptUtils.calcTimeMillis(time))
                    println "Identification #=${declarationData.id} Заполнение таблицы версий ( ${ScriptUtils.calcTimeMillis(time)}"

                    // Идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
                    time = System.currentTimeMillis()
                    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, createRefbookHandler())
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                    println "Identification #=${declarationData.id} Предварительная выборка по значимым параметрам (\" ${similarityPersonMap.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                    time = System.currentTimeMillis()
                    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap)
                    //noinspection GroovyAssignabilityCheck
                    logForDebug("Обновление записей 1-я процедура (" + ScriptUtils.calcTimeMillis(time))
                    println "Identification #=${declarationData.id} Обновление записей ( ${ScriptUtils.calcTimeMillis(time)}"

                    if (!insertPersonList.isEmpty()) {
                        time = System.currentTimeMillis()
                        MapSqlParameterSource params = new MapSqlParameterSource("p_declaration", declarationData.id)
                        SearchMapper searchMapper = new SearchMapper()
                        namedParameterJdbcTemplate.query(snilsQuery, params, searchMapper)
                        Map<Long, Long> snilsSearchResult = searchMapper.getResult()

                        logForDebug("Выборка по СНИЛС (" + snilsSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                        println "Identification #=${declarationData.id} Выборка по СНИЛС (\" ${snilsSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                        if (snilsSearchResult) {
                            List<NaturalPerson> snilsNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(snilsSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())
                            time = System.currentTimeMillis()
                            logForDebug("Получение ФЛ по СНИЛС (" + snilsNaturalPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                            println "Identification #=${declarationData.id} Получение ФЛ по СНИЛС (\" ${snilsNaturalPersonList.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                            Map<Long, Map<Long, NaturalPerson>> snilsSimilarityPersonMap = createSimilarityPersonMap(snilsSearchResult, snilsNaturalPersonList)

                            updateNaturalPersonRefBookRecords(primaryPersonMap, snilsSimilarityPersonMap)
                        }

                        if (!insertPersonList.isEmpty()) {
                            searchMapper = new SearchMapper()
                            params.addValue("asnu", declarationData.asnuId)
                            time = System.currentTimeMillis()
                            namedParameterJdbcTemplate.query(inpQuery, params, searchMapper)
                            Map<Long, Long> inpSearchResult = searchMapper.getResult()
                            //noinspection GroovyAssignabilityCheck
                            logForDebug("Выборка по ИНП (" + inpSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                            println "Identification #=${declarationData.id} Выборка по ИНП (\" ${inpSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"
                            if (inpSearchResult) {
                                time = System.currentTimeMillis()
                                List<NaturalPerson> inpNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(inpSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())
                                //noinspection GroovyAssignabilityCheck
                                logForDebug("Получение ФЛ по ИНП (" + inpNaturalPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                println "Identification #=${declarationData.id} Получение ФЛ по ИНП (\" ${inpNaturalPersonList.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"
                                Map<Long, Map<Long, NaturalPerson>> inpSimilarityPersonMap = createSimilarityPersonMap(inpSearchResult, inpNaturalPersonList)

                                updateNaturalPersonRefBookRecords(primaryPersonMap, inpSimilarityPersonMap)
                            }

                            if (!insertPersonList.isEmpty()) {
                                searchMapper = new SearchMapper()
                                time = System.currentTimeMillis()
                                namedParameterJdbcTemplate.query(fioQuery, params, searchMapper)
                                Map<Long, Long> fioSearchResult = searchMapper.getResult()
                                //noinspection GroovyAssignabilityCheck
                                logForDebug("Выборка по ФИО (" + fioSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                println "Identification #=${declarationData.id} Выборка по ФИО (\" ${fioSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                if (fioSearchResult) {
                                    List<NaturalPerson> fioNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(fioSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())

                                    time = System.currentTimeMillis()
                                    Map<Long, Map<Long, NaturalPerson>> fioSimilarityPersonMap = createSimilarityPersonMap(fioSearchResult, fioNaturalPersonList)
                                    logForDebug("Получение ФЛ по ФИО (" + fioSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                    println "Identification #=${declarationData.id} Получение ФЛ по ФИО (\" ${fioSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                    updateNaturalPersonRefBookRecords(primaryPersonMap, fioSimilarityPersonMap)
                                }

                                if (!insertPersonList.isEmpty()) {
                                    searchMapper = new SearchMapper()
                                    time = System.currentTimeMillis()
                                    namedParameterJdbcTemplate.query(innQuery, params, searchMapper)
                                    Map<Long, Long> innSearchResult = searchMapper.getResult()
                                    logForDebug("Выборка по по ИНН (" + innSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                    println "Identification #=${declarationData.id} Выборка по ИНН (\" ${innSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                    if (innSearchResult) {
                                        time = System.currentTimeMillis()
                                        List<NaturalPerson> innNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(innSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())
                                        Map<Long, Map<Long, NaturalPerson>> innSimilarityPersonMap = createSimilarityPersonMap(innSearchResult, innNaturalPersonList)
                                        logForDebug("Получение ФЛ по ИНН (" + innNaturalPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                        println "Identification #=${declarationData.id} Получение ФЛ по ИНН (\" ${innNaturalPersonList.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                        updateNaturalPersonRefBookRecords(primaryPersonMap, innSimilarityPersonMap)
                                    }

                                    if (!insertPersonList.isEmpty()) {
                                        /*searchMapper = new SearchMapper()
                                        time = System.currentTimeMillis()
                                        namedParameterJdbcTemplate.query(innForeignQuery, params, searchMapper)
                                        Map<Long, Long> innForeignSearchResult = searchMapper.getResult()
                                        logForDebug("Выборка по ИНН иностр (" + innForeignSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                        println "Identification #=${declarationData.id} Выборка по ИНН иностр(\" ${innForeignSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                        if (innForeignSearchResult) {
                                            time = System.currentTimeMillis()
                                            List<NaturalPerson> innForeignNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(innForeignSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())
                                            Map<Long, Map<Long, NaturalPerson>> innForeignSimilarityPersonMap = createSimilarityPersonMap(innForeignSearchResult, innForeignNaturalPersonList)
                                            logForDebug("Получение ФЛ по ИНН иностр (" + innForeignNaturalPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                            println "Identification #=${declarationData.id} Получение ФЛ по ИНН иностр(\" ${innForeignNaturalPersonList.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                            updateNaturalPersonRefBookRecords(primaryPersonMap, innForeignSimilarityPersonMap)
                                        }*/

                                        if (!insertPersonList.isEmpty()) {
                                            searchMapper = new SearchMapper()
                                            time = System.currentTimeMillis()
                                            namedParameterJdbcTemplate.query(dulQuery, params, searchMapper)
                                            Map<Long, Long> dulSearchResult = searchMapper.getResult()
                                            logForDebug("Выборка по ДУЛ (" + dulSearchResult.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                            println "Identification #=${declarationData.id} Выборка по ДУЛ(\" ${dulSearchResult.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"

                                            if (dulSearchResult) {
                                                time = System.currentTimeMillis()
                                                List<NaturalPerson> dulNaturalPersonList = namedParameterJdbcTemplate.query(getPersonSqlQuery(dulSearchResult.values() as List), new MapSqlParameterSource(), new NaturalPersonScriptRowMapper())
                                                Map<Long, Map<Long, NaturalPerson>> dulSimilarityPersonMap = createSimilarityPersonMap(dulSearchResult, dulNaturalPersonList)
                                                logForDebug("Получение ФЛ по ДУЛ (" + dulNaturalPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                                                println "Identification #=${declarationData.id} Получение ФЛ по ДУЛ(\" ${dulNaturalPersonList.size()}\" записей,  ${ScriptUtils.calcTimeMillis(time)}"
                                                updateNaturalPersonRefBookRecords(primaryPersonMap, dulSimilarityPersonMap)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!insertPersonList.isEmpty()) {
                        time = System.currentTimeMillis()
                        preCreateNaturalPersonRefBookRecords()
                        //noinspection GroovyAssignabilityCheck
                        logForDebug("Создание (" + insertPersonList.size() + " записей, " + ScriptUtils.calcTimeMillis(time))
                        println "Identification #=${declarationData.id} Создание (\" ${insertPersonList.size()}\" записей,   ${ScriptUtils.calcTimeMillis(time)}"
                    }

                }

                countTotalAndUniquePerson()
                //noinspection GroovyAssignabilityCheck
                logForDebug("Завершение расчета ПНФ (" + ScriptUtils.calcTimeMillis(timeFull))
                println "Identification #=${declarationData.id} Завершение расчета ПНФ (${ScriptUtils.calcTimeMillis(time)}"
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Map<Long, Map<Long, NaturalPerson>> createSimilarityPersonMap(Map<Long, Long> searchResult, List<NaturalPerson> naturalPersonList) {
        Map<Long, Map<Long, NaturalPerson>> result = [:]
        searchResult.each {Long k, Long v ->
            if (naturalPersonList.id.contains(v)) {
                Map<Long, NaturalPerson> matchedMap = result.get(k)
                if (matchedMap == null) {
                    matchedMap = [:]
                }
                naturalPersonList.each {
                    if (it.id == v) {
                        if (matchedMap.containsKey(v)) {
                            NaturalPerson addedPerson = matchedMap.get(v)
                            if (it.documents && !addedPerson.getDocuments()?.id?.contains(it.getDocuments().get(0)?.id)) {
                                addedPerson.getDocuments().add(it.getDocuments()?.get(0))
                            }
                            if (it.personIdentityList && !addedPerson.getPersonIdentityList()?.id?.contains(it.getPersonIdentityList().get(0)?.id)) {
                                addedPerson.getPersonIdentityList().add(it.getPersonIdentityList()?.get(0))
                            }
                            if (it.personTbList && !addedPerson.getPersonTbList()?.id?.contains(it.getPersonTbList().get(0)?.id)) {
                                addedPerson.getPersonTbList().add(it.getPersonTbList()?.get(0))
                            }
                        } else {
                            it.primaryPersonId = k
                            matchedMap.put(v, it)
                        }
                    }
                }
                result.put(k, matchedMap)
            }
        }
        return result
    }

    static String getPersonSqlQuery(List<Long> values) {
        return "select distinct person3.id as ref_book_person_id,\n" +
                "             person3.record_id as person_record_id,\n" +
                "             person3.last_name,\n" +
                "             person3.first_name,\n" +
                "             person3.middle_name,\n" +
                "             person3.inn,\n" +
                "             person3.inn_foreign,\n" +
                "             person3.snils,\n" +
                "             person3.taxpayer_state,\n" +
                "             person3.birth_date,\n" +
                "             person3.birth_place,\n" +
                "             person3.citizenship,\n" +
                "             person3.record_id,\n" +
                "             person3.source_id,\n" +
                "             person3.old_id,\n" +
                "             person3.report_doc,\n" +
                "             person3.vip,\n" +
                "             person3.country_id,\n" +
                "             person3.region_code,\n" +
                "             person3.postal_code,\n" +
                "             person3.district,\n" +
                "             person3.city,\n" +
                "             person3.locality,\n" +
                "             person3.street,\n" +
                "             person3.house,\n" +
                "             person3.build,\n" +
                "             person3.appartment,\n" +
                "             person3.address_foreign,\n" +
                "             person3.start_date,\n" +
                "             person3.end_date,\n" +
                "             tax.id as book_id_tax_payer_id,\n" +
                "             tax.inp,\n" +
                "             tax.as_nu,\n" +
                "             doc.id as ref_book_id_doc_id,\n" +
                "             doc.doc_id,\n" +
                "             doc.doc_number,\n" +
                "             tb.id as ref_book_person_tb_id,\n" +
                "             tb.tb_department_id,\n" +
                "             tb.import_date from ref_book_person person3 \n" +
                "             left join ref_book_id_doc doc on (doc.person_id in (select id from ref_book_person where record_id=person3.record_id))\n" +
                "             left join ref_book_id_tax_payer tax on (tax.person_id in (select id from ref_book_person where record_id=person3.record_id))\n" +
                "             left join ref_book_person_tb tb on (tb.person_id in (select id from ref_book_person where record_id=person3.record_id))\n" +
                "where ${SqlUtils.transformToSqlInStatementViaTmpTable("person3.id", values)}"
    }

    // Вывод информации о количестве обработанных физлиц всего и уникальных
    void countTotalAndUniquePerson() {
        int countInDeclarationData = ndflPersonService.getCountNdflPerson(declarationData.id)
        int countOfUniqueEntries = personService.getCountOfUniqueEntries(declarationData.id)
        logger.info("Записей физических лиц обработано: $countInDeclarationData, всего уникальных записей физических лиц: $countOfUniqueEntries")
    }

    NaturalPersonPrimaryRnuRowMapper createPrimaryRowMapper() {
        NaturalPersonPrimaryRnuRowMapper naturalPersonRowMapper = new NaturalPersonPrimaryRnuRowMapper()
        naturalPersonRowMapper.setAsnu(refBookService.getAsnu(declarationData.getAsnuId()))

        List<RefBookCountry> countryList = getCountryRefBookList()
        naturalPersonRowMapper.setCountryCodeMap(countryList.collectEntries {
            [it.code, it]
        })

        List<RefBookDocType> docTypeList = getDocTypeRefBookList()
        naturalPersonRowMapper.setDocTypeCodeMap(docTypeList.collectEntries {
            [it.code, it]
        })

        List<RefBookTaxpayerState> taxpayerStatusCodeList = getTaxpayerStatusRefBookList()
        naturalPersonRowMapper.setTaxpayerStatusCodeMap(taxpayerStatusCodeList.collectEntries {
            [it.code, it]
        })

        return naturalPersonRowMapper
    }

    List<RefBookCountry> getCountryRefBookList() {
        if (countryRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.COUNTRY.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                RefBookCountry country = new RefBookCountry()
                country.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
                country.setCode(refBookValueMap?.get("CODE")?.getStringValue())
                countryRefBookCache.add(country)
            }
        }
        return countryRefBookCache
    }

    List<RefBookDocType> getDocTypeRefBookList() {
        if (docTypeRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.DOCUMENT_CODES.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                RefBookDocType docType = new RefBookDocType()
                docType.setId(refBookValueMap?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue())
                docType.setName(refBookValueMap?.get("NAME")?.getStringValue())
                docType.setCode(refBookValueMap?.get("CODE")?.getStringValue())
                docType.setPriority(refBookValueMap?.get("PRIORITY")?.getNumberValue()?.intValue())
                docTypeRefBookCache.add(docType)
            }
        }
        return docTypeRefBookCache
    }

    List<RefBookTaxpayerState> getTaxpayerStatusRefBookList() {
        if (taxpayerStatusRefBookCache.isEmpty()) {
            List<Map<String, RefBookValue>> refBookRecords = getRefBookAll(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookRecords.each { Map<String, RefBookValue> refBookValueMap ->
                RefBookTaxpayerState taxpayerStatus = new RefBookTaxpayerState()
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
     * За шаг B отвечает метод {@link #addToReduceMap(java.lang.Object, java.util.Map, java.util.Map, com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson)}
     * C. Далее для каждой entry из мапы описаной на шаге B1, сравниваем между собой физлиц из значения этой entry.
     * Сравнение выполняется с вложенным циклом для того чтобы не делать лишнюю работу заведено поле #primaryDuplicateIds
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

        Map<IdDoc, NaturalPerson> idDocMatchedMap = new HashMap<>()
        Map<IdDoc, List<NaturalPerson>> idDocReducedMatchedMap = new HashMap<>()

        Map<PersonalData, NaturalPerson> personalDataMatchedMap = new HashMap<>()
        Map<PersonalData, List<NaturalPerson>> personalDataReducedMatchedMap = new HashMap<>()

        for (NaturalPerson person : insertPersonList) {
            String inp = person.personIdentityList.isEmpty() ? "" : person.personIdentityList.get(0).inp
            String snils = person.snils?.replaceAll("[\\s-]", "")?.toLowerCase()
            String inn = person.inn
            String innForeign = person.innForeign
            IdDoc personDocument = null
            if (!person?.documents.isEmpty()) {
                person.documents.get(0)
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

        sendToMapDuplicates(inpReducedMatchedMap)
        sendToMapDuplicates(snilsReducedMatchedMap)
        sendToMapDuplicates(innReducedMatchedMap)
        sendToMapDuplicates(innForeignReducedMatchedMap)
        sendToMapDuplicates(idDocReducedMatchedMap)
        sendToMapDuplicates(personalDataReducedMatchedMap)
        Iterator<NaturalPerson> iterator = insertPersonList.iterator()

        while (iterator.hasNext()) {
            NaturalPerson person = iterator.next()
            if (duplicatePersonList.contains(person.primaryPersonId)) {
                iterator.remove()
            }
        }

        for (Map.Entry<Long, List<NaturalPerson>> entry : primaryPersonOriginalDuplicates.entrySet()) {
            NaturalPerson original = insertPersonList.find { NaturalPerson insertPerson ->
                entry.getKey() == insertPerson?.primaryPersonId
            }
            if (entry.getKey() != null) {
                for (NaturalPerson duplicate : entry.getValue()) {
                    if (!containsPersonIdentifier(original, duplicate.getPersonIdentifier().getAsnu().getId(), duplicate.getPersonIdentifier().getInp())) {
                        duplicate.getPersonIdentifier().setPerson(original)
                        original.getPersonIdentityList().add(duplicate.getPersonIdentifier())
                    }
                    IdDoc newDocument = BaseWeightCalculator.findDocument(original, duplicate.getDocuments().get(0).getDocType().getId(), duplicate.getDocuments().get(0).getDocumentNumber())
                    if (newDocument == null) {
                        if (duplicate.getDocuments().get(0).docType != null) {
                            duplicate.getDocuments().get(0).setPerson(original)
                            duplicate.getDocuments().get(0).documentNumber = performDocNumber(duplicate.getDocuments().get(0))
                            original.getDocuments().add(duplicate.getDocuments().get(0))
                        }
                    }
                }
            }
        }
    }

    /**
     * Разделяет <code>similarAttributePersonList</code> на отдельные списки, каждый список передается
     * в {@link #mapDuplicates(List < NaturalPerson >)} для дальнейшей обработки
     * @param similarAttributePersonList списки обрабатываемых Физлиц, сгруппированные по ключевому параметру.
     */
    void sendToMapDuplicates(Map<?, List<NaturalPerson>> similarAttributePersonList) {
        if (!similarAttributePersonList.isEmpty()) {
            for (List<NaturalPerson> personList : similarAttributePersonList.values()) {
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
     * @param similarAttributePersonList список обрабатываемых Физлиц.
     */
    void mapDuplicates(List<NaturalPerson> similarAttributePersonList) {
        Collections.sort(similarAttributePersonList, new Comparator<NaturalPerson>() {
            @Override
            int compare(NaturalPerson o1, NaturalPerson o2) {
                return o1.primaryPersonId.compareTo(o2.primaryPersonId)
            }
        })
        Double similarityLine = similarityThreshold.doubleValue() / 1000
        // Список физлиц которые были выбраны в качестве оригинала
        NaturalPerson originalPerson = null
        List<Long> originalPrimaryPersonList = new ArrayList<>(primaryPersonOriginalDuplicates.keySet())

        // Выбираем физлицо, которое будет оригиналом.
        // Проверяем было ли физлицо выбрано оригиналом раннее. Если да, то вы выбираем его снова.
        for (NaturalPerson person : similarAttributePersonList) {
            if (originalPrimaryPersonList.contains(person.primaryPersonId)) {
                originalPerson = insertPersonList.find { NaturalPerson insertPerson ->
                    person.primaryPersonId == insertPerson?.primaryPersonId
                }
                similarAttributePersonList.removeAll { NaturalPerson processingPerson ->
                    person.primaryPersonId == processingPerson.primaryPersonId
                }
                break
            }
        }

        /* Если физлицо не было раннее выбрано в качестве оригинала, тогда берем первое физлицо, но проверяем чтобы оно
         не содержалось в списке дубликатов и чтобы оно не было дубликатом имеющихся оригиналов*/
        if (originalPerson == null) {
            outer:
            for (NaturalPerson person : similarAttributePersonList) {
                if (!duplicatePersonList.contains(person.primaryPersonId)) {
                    for (Long original : originalPrimaryPersonList) {
                        NaturalPerson originalItem = insertPersonList.find { NaturalPerson insertPerson ->
                            insertPerson.primaryPersonId == original
                        }
                        refBookPersonService.calculateWeight(originalItem, [person], new PersonDataWeightCalculator(refBookPersonService.getBaseCalculateList()))
                        if (person.weight > similarityLine) {
                            duplicatePersonList.add(person.primaryPersonId)
                            primaryPersonOriginalDuplicates.get(original).add(person)
                            continue outer
                        }
                    }
                    originalPerson = person
                    similarAttributePersonList.removeAll { NaturalPerson processingPerson ->
                        originalPerson.primaryPersonId == processingPerson.primaryPersonId
                    }
                    break
                }
            }
        }
        if (originalPerson != null) {
            for (int i = 0; i < similarAttributePersonList.size(); i++) {
                if (!duplicatePersonList?.contains(similarAttributePersonList.get(i).primaryPersonId)) {
                    refBookPersonService.calculateWeight(originalPerson, [similarAttributePersonList.get(i)], new PersonDataWeightCalculator(refBookPersonService.getBaseCalculateList()))
                    if (similarAttributePersonList.get(i).weight > similarityLine && !originalPrimaryPersonList.contains(similarAttributePersonList.get(i).primaryPersonId)) {
                        duplicatePersonList.add(similarAttributePersonList.get(i).primaryPersonId)
                        if (primaryPersonOriginalDuplicates.get(originalPerson.primaryPersonId) == null) {
                            primaryPersonOriginalDuplicates.put(originalPerson.primaryPersonId, [similarAttributePersonList.get(i)])
                        } else {
                            primaryPersonOriginalDuplicates.get(originalPerson.primaryPersonId).add(similarAttributePersonList.get(i))
                        }
                    }
                }
            }
        }
    }

    def preCreateNaturalPersonRefBookRecords() {
        if (!insertPersonList.isEmpty()) {
            if (runParallel && firstAttemptToCreate) {
                boolean personsRegistryLocked = refBookPersonService.lockPersonsRegistry(userInfo, taskDataId)

                while (!personsRegistryLocked) {
                    ScriptUtils.checkInterrupted()
                    logForDebug("Ожидание снятия блокировки с реестра ФЛ")
                    Thread.sleep(sleepBetweenTryLock)
                    personsRegistryLocked = refBookPersonService.lockPersonsRegistry(userInfo, taskDataId)
                    if (maxLockAttempts < ++sleepCounter) {
                        logger.error("Не удалось установить блокировку на Реестр Физических лиц")
                        return
                    }
                }
                ScriptUtils.checkInterrupted()
                logForDebug("Блокировка на реестр ФЛ установлена")

                long newMaxPersonId = refBookPersonService.findMaxRegistryPersonId(new Date())
                ScriptUtils.checkInterrupted()
                if (newMaxPersonId > maxRegistryPersonId) {
                    Map<Long, NaturalPerson> primaryPersonMap = insertPersonList.collectEntries { NaturalPerson naturalPerson ->
                        [naturalPerson.getPrimaryPersonId(), naturalPerson]
                    }
                    List<NaturalPerson> newRegistryPersonList = refBookPersonService.findNewRegistryPersons(maxRegistryPersonId, new Date(), new NaturalPersonMapper(getRefData()))
                    Map<Long, NaturalPerson> newRegistryPersonMap = newRegistryPersonList.collectEntries { NaturalPerson naturalPerson ->
                        [naturalPerson.getId(), naturalPerson]
                    }
                    Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = insertPersonList.collectEntries { NaturalPerson naturalPerson ->
                        [naturalPerson.getPrimaryPersonId(), newRegistryPersonMap]
                    }
                    maxRegistryPersonId = newMaxPersonId
                    updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap)
                }
                firstAttemptToCreate = false
            }
            performPrimaryPersonDuplicates()
            createNaturalPersonRefBookRecords()
        }
    }

    def createNaturalPersonRefBookRecords() {

        int createCnt = 0

        Map<Long, String> infoData = [:]

        for (NaturalPerson person : insertPersonList) {

            ScriptUtils.checkInterrupted()

            StringBuilder infoMsgBuilder = new StringBuilder()

            person.personIdentityList.eachWithIndex { PersonIdentifier personIdentifier, int index ->
                if (index > 0) {
                    infoMsgBuilder.append(String.format("[Добавлен новый \"ИНП\": \"%s\", \"АСНУ\": \"%s\"]", personIdentifier?.inp, personIdentifier?.getAsnu()?.getCode()))
                }
            }

            person.setStartDate(getReportPeriodStartDate())

            if (CollectionUtils.isNotEmpty(person.documents)) {
                IdDoc personDocument = person.documents.get(0)
                if (!personDocument.docType?.code || !getDocTypeRefBookList().code.contains(personDocument.docType?.code)) {
                    createMsg(personDocument.docType?.code ?: "_", C_ID_DOC_TYPE, R_ID_DOC_TYPE, CLAUSE_END_CREATE, person)
                    personDocument = new IdDoc()
                } else {
                    personDocument.documentNumber = performDocNumber(personDocument)
                }
                person.reportDoc = personDocument
            } else {
                person.reportDoc = new IdDoc()
            }

            person.documents.eachWithIndex { IdDoc document, int index ->
                if (index > 0) {
                    infoMsgBuilder.append(String.format("[Добавлена запись о новом ДУЛ: \"Код ДУЛ\" = \"%s\", \"Серия и номер ДУЛ\": \"%s\"]", document?.docType?.code, document?.getDocumentNumber()))
                }
            }

            if (person.documents.size() > 1) {
                checkIncReportFlag(person, infoMsgBuilder)
            }

            Department parentTb = new Department()
            parentTb.setId(departmentService.getParentTBId(declarationData.departmentId))

            PersonTb personTb = new PersonTb()
            personTb.person = person
            personTb.tbDepartment = parentTb
            personTb.importDate = declarationData.createdDate
            person.getPersonTbList().add(personTb)

            String fio = (person.lastName ?: "") + " " + (person.firstName ?: "") + " " + (person.middleName ?: "")
            String inp = person.personIdentifier && person.personIdentifier.inp ? person.personIdentifier.inp : ""
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
            if (person.inn == null) {
                if (person.citizenship?.code == "643") {
                    logger.warnExp("Для физического лица ФИО: \"%s\", ИНП: %s в Разделе 1 не указано значение ИНН в РФ. Физическое лицо будет создано в Реестре физических лиц без указания ИНН в РФ",
                            "\"ИНН\" не указан",
                            fioAndInp,
                            fio,
                            person.personIdentifier && person.personIdentifier.inp ? person.personIdentifier.inp : "")
                }
            } else {
                String checkInn = ScriptUtils.checkInn(person.inn)
                if (checkInn != null) {
                    logger.warnExp("Для физического лица ФИО: \"%s\", ИНП: %s в Разделе 1 указано некорректное значение ИНН в РФ  (%s). Причина: %s. Физическое лицо будет создано в Реестре физических лиц без указания ИНН в РФ",
                            "\"ИНН\" не соответствует формату",
                            fioAndInp,
                            fio,
                            person.personIdentifier && person.personIdentifier.inp ? person.personIdentifier.inp : "",
                            person.inn,
                            checkInn)
                    person.setInn(null)
                }
            }
            if (!person.getCitizenship()?.code || !getCountryRefBookList().code.contains(person.getCitizenship()?.code)) {
                createMsg(person.getCitizenship()?.code ?: "_", C_CITIZENSHIP, R_CITIZENSHIP, CLAUSE_END_CREATE, person)
                person.setCitizenship(new RefBookCountry())
            }
            if (!person.getTaxPayerState()?.code || !getTaxpayerStatusRefBookList().code.contains(person.getTaxPayerState()?.code)) {
                createMsg(person.getTaxPayerState()?.code ?: "_", C_STATUS, R_STATUS, CLAUSE_END_CREATE, person)
                person.setTaxPayerState(new RefBookTaxpayerState())
            }
            if (!ScriptUtils.checkSnils(person.snils)) {
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату. %s",
                        "СНИЛС", person.snils ?: "_", CLAUSE_END_CREATE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, person.num ?: "")
                logger.warnExp("%s. %s.", "\"СНИЛС\" не соответствует формату", fioAndInp, pathError,
                        errMsg)
                person.setSnils(null)
            }

            if (infoMsgBuilder.length() != 0) {
                infoData.put(person.primaryPersonId, infoMsgBuilder.toString())
            }
        }

        List<RegistryPerson> savedPersons = personService.saveNewIdentificatedPersons(insertPersonList)

        //update reference to ref book
        updatePrimaryToRefBookPersonReferences(insertPersonList)

        for (Map.Entry<Long, List<NaturalPerson>> entry : primaryPersonOriginalDuplicates.entrySet()) {
            for (NaturalPerson duplicatePerson : entry.getValue()) {
                NaturalPerson original = insertPersonList.find { NaturalPerson insertPerson ->
                    entry.getKey() == insertPerson?.primaryPersonId
                }
                duplicatePerson.id = original.id
            }
            updatePrimaryToRefBookPersonReferences(entry.getValue())
        }

        for (NaturalPerson person : insertPersonList) {
            ScriptUtils.checkInterrupted()
            String info = infoData.get(person.primaryPersonId)
            if (info) {
                logger.infoExp("Обновлена запись в Реестре физических лиц. Идентификатор ФЛ: %s, ФИО: %s. Изменено:  %s", "", String.format("%s, ИНП: %s", buildFio(person), person.getPersonIdentifier().getInp()), person.getRecordId(), buildFio(person), info)
                logBusinessService.logPersonEvent(person.id, FormDataEvent.UPDATE_PERSON,
                        "В ходе идентификации ФЛ первичной формы № $declarationData.id изменено: " + info,
                        taUserService.getSystemUserInfo())
            }
        }


        for (RegistryPerson person : savedPersons) {
            ScriptUtils.checkInterrupted()
            logger.infoExp("Создана новая запись в Реестре физических лиц. Идентификатор ФЛ: %s, ФИО: %s %s %s", "", String.format("%s, ИНП: %s", buildFio(person), person.getPersonIdentityList().get(0).inp), person.recordId, person.getLastName() ?: "", person.getFirstName() ?: "", person.getMiddleName() ?: "")
            logBusinessService.logPersonEvent(person.id, FormDataEvent.CREATE_PERSON,
                    "ФЛ создано в ходе операции \"Идентификация\" первичной формы № $declarationData.id.",
                    taUserService.getSystemUserInfo())
            createCnt++
        }

        logForDebug("Создано записей: " + createCnt)
        println "Identification #=${declarationData.id} Создано записей:  (${createCnt})"

    }

    void createMsg(String value, String propertyName, String refBookName, String endClause, NaturalPerson ndflPerson) {
        String fio = buildFio(ndflPerson)
        String inp = ndflPerson.getPersonIdentifier()?.inp ?: ""
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\". %s",
                propertyName, value ?: "",
                refBookName,
                endClause
        )
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.num ?: "")
        logger.warnExp("%s. %s.",
                String.format(LOG_TYPE_REFERENCES, refBookName), fioAndInp, pathError, errMsg)
    }

    NaturalPersonRefbookHandler createRefbookHandler() {

        NaturalPersonRefbookHandler refbookHandler = new NaturalPersonRefbookHandler()

        refbookHandler.setLogger(logger)

        refbookHandler.setRefDataHolder(getRefData())

        return refbookHandler
    }

    RefDataHolder getRefData() {
        if (refDataHolder == null) {
            refDataHolder = new RefDataHolder()
            List<RefBookAsnu> asnuList = refBookService.findAllAsnu();
            refDataHolder.setAsnuMap(asnuList.collectEntries {
                [it.id, it]
            })
            List<RefBookCountry> countryList = getCountryRefBookList()
            refDataHolder.setCountryMap(countryList.collectEntries {
                [it.id, it]
            })

            List<RefBookDocType> docTypeList = getDocTypeRefBookList()
            refDataHolder.setDocTypeMap(docTypeList.collectEntries {
                [it.id, it]
            })

            List<RefBookTaxpayerState> taxpayerStatusCodeList = getTaxpayerStatusRefBookList()
            refDataHolder.setTaxpayerStatusMap(taxpayerStatusCodeList.collectEntries {
                [it.id, it]
            })
        }
        return refDataHolder
    }

    def updateNaturalPersonRefBookRecords(Map<Long, NaturalPerson> primaryPersonMap, Map<Long, Map<Long, NaturalPerson>> similarityPersonMap) {

        long time = System.currentTimeMillis()

        Set<NaturalPerson> updatePersonList = new LinkedHashSet()
        List<NaturalPerson> updatePersonReferenceList = new ArrayList<NaturalPerson>()

        //primaryId - RefBookPerson
        HashMap<Long, NaturalPerson> conformityMap = new HashMap<Long, NaturalPerson>()

        for (Map.Entry<Long, Map<Long, NaturalPerson>> entry : similarityPersonMap.entrySet()) {

            Long primaryPersonId = entry.getKey()

            Map<Long, NaturalPerson> similarityPersonValues = entry.getValue()

            List<NaturalPerson> similarityPersonList = new ArrayList<NaturalPerson>(similarityPersonValues.values())

            NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId)

            IdentificationData identificationData = new IdentificationData()
            identificationData.naturalPerson = primaryPerson
            identificationData.refBookPersonList = similarityPersonList
            identificationData.tresholdValue = similarityThreshold
            identificationData.declarationDataAsnuId = declarationData.asnuId

            identificationData.setPriorityMap(getRefAsnu())

            NaturalPerson refBookPerson = refBookPersonService.identificatePerson(identificationData, logger)

            conformityMap.put(primaryPersonId, refBookPerson)
        }

        logForDebug("Идентификация (" + ScriptUtils.calcTimeMillis(time))
        println "Identification #=${declarationData.id} Идентификация (  (${ScriptUtils.calcTimeMillis(time)})"
        time = System.currentTimeMillis()

        int updCnt = 0

        for (Map.Entry<Long, NaturalPerson> entry : conformityMap.entrySet()) {

            ScriptUtils.checkInterrupted()

            Long primaryPersonId = entry.getKey()
            NaturalPerson primaryPerson = primaryPersonMap.get(primaryPersonId)
            NaturalPerson refBookPerson = entry.getValue()

            if (refBookPerson != null) {
                primaryPerson.setId(refBookPerson.getId())
                StringBuilder infoMsgBuilder = new StringBuilder()
                boolean changed = false

                if (refBookPerson.needUpdate) {
                    changed = updatePerson(refBookPerson, primaryPerson, infoMsgBuilder)
                }

                //documents
                IdDoc primaryPersonDocument = null
                if (!primaryPerson.getDocuments().isEmpty()) {
                    primaryPersonDocument = primaryPerson.getDocuments().get(0);
                }
                if (primaryPersonDocument && primaryPersonDocument.docType?.code && getDocTypeRefBookList().code.contains(primaryPersonDocument.docType?.code)) {
                    Long docTypeId = primaryPersonDocument.getDocType() != null ? primaryPersonDocument.getDocType().getId() : null
                    IdDoc personDocument = BaseWeightCalculator.findDocument(refBookPerson, docTypeId, primaryPersonDocument.getDocumentNumber())

                    if (personDocument == null) {
                        if (primaryPersonDocument.docType != null) {
                            primaryPersonDocument.documentNumber = performDocNumber(primaryPersonDocument)
                            refBookPerson.getDocuments().add(primaryPersonDocument)
                            changed = true
                            infoMsgBuilder.append(String.format("[Добавлена запись о новом ДУЛ: \"Код ДУЛ\" = \"%s\", \"Серия и номер ДУЛ\": \"%s\"]", primaryPersonDocument?.docType?.code, primaryPersonDocument?.getDocumentNumber()))
                        }
                    }
                } else if (primaryPersonDocument && (!primaryPersonDocument.docType?.code || !getDocTypeRefBookList().code.contains(primaryPersonDocument.docType?.code))) {
                    createMsg(primaryPersonDocument.docType?.code ?: "_", C_ID_DOC_TYPE, R_ID_DOC_TYPE, CLAUSE_END_UPDATE, primaryPerson)
                    primaryPersonDocument = new IdDoc()
                }
                if (checkIncReportFlag(refBookPerson, infoMsgBuilder)) {
                    changed = true
                }

                //identifiers
                PersonIdentifier primaryPersonIdentifier = primaryPerson.getPersonIdentifier()
                if (primaryPersonIdentifier != null) {
                    if (!containsPersonIdentifier(refBookPerson, primaryPersonIdentifier.getAsnu().getId(), primaryPersonIdentifier.getInp())) {
                        refBookPerson.getPersonIdentityList().add(primaryPersonIdentifier)
                        infoMsgBuilder.append(String.format("[Добавлен новый \"ИНП\": \"%s\", \"АСНУ\": \"%s\"]", primaryPersonIdentifier?.inp, primaryPersonIdentifier?.getAsnu()?.getCode()))
                        changed = true
                    } else {
                        if (duplicatedPersonIdentifiers.get(refBookPerson.id) == null) {
                            duplicatedPersonIdentifiers.put(refBookPerson.id, [primaryPersonIdentifier])
                        } else {
                            duplicatedPersonIdentifiers.get(refBookPerson.id).add(primaryPersonIdentifier)
                        }
                    }
                }

                Integer parentTbId = departmentService.getParentTBId(declarationData.departmentId)
                PersonTb personTb = new PersonTb()
                if (!refBookPerson.personTbList.tbDepartment.id.contains(parentTbId)) {
                    personTb.person = refBookPerson
                    Department department = getDepartment(parentTbId)
                    personTb.tbDepartment = department
                    personTb.importDate = declarationData.createdDate
                    refBookPerson.getPersonTbList().add(personTb)
                    infoMsgBuilder.append(String.format("[Добавлен новый Тербанк: \"%s\"]", department.getShortName()))
                    changed = true
                } else {
                    Department department = getDepartment(parentTbId)
                    personTb.tbDepartment = department
                    if (duplicatedPersonTbs.get(refBookPerson.id) == null) {
                        duplicatedPersonTbs.put(refBookPerson.id, [personTb])
                    } else {
                        duplicatedPersonTbs.get(refBookPerson.id).add(personTb)
                    }
                }

                if (changed) {
                    updatePersonList.add(refBookPerson)
                }
                updatePersonReferenceList.add(primaryPerson)

                if (changed) {
                    logger.infoExp("Обновлена запись в Реестре физических лиц. Идентификатор ФЛ: %s, ФИО: %s. Изменено:  %s", "", String.format("%s, ИНП: %s", buildFio(primaryPerson), primaryPerson.getPersonIdentifier().getInp()), refBookPerson.getRecordId(), buildFio(primaryPerson), infoMsgBuilder.toString())
                    logBusinessService.logPersonEvent(primaryPerson.id, FormDataEvent.UPDATE_PERSON,
                            "В ходе идентификации ФЛ первичной формы № $declarationData.id изменено: " + infoMsgBuilder.toString(),
                            taUserService.getSystemUserInfo())
                    updCnt++
                }
            }
        }

        for (NaturalPerson updatePerson : updatePersonList) {
            for (PersonIdentifier duplicatePersonIdentidfier : duplicatedPersonIdentifiers?.get(updatePerson?.id)) {
                updatePerson.getPersonIdentityList().removeAll { PersonIdentifier inp ->
                    inp?.asnu?.getId() == duplicatePersonIdentidfier?.asnu?.getId() && inp?.getInp()?.equalsIgnoreCase(duplicatePersonIdentidfier?.getInp())
                }
            }
            for (Integer duplicatePersonTb : duplicatedPersonTbs.get(updatePerson?.id)?.tbDepartment?.id) {
                updatePerson.getPersonTbList().removeAll { PersonTb existingTb ->
                    duplicatedPersonTbs.get(updatePerson?.id)?.tbDepartment?.id?.contains(existingTb?.tbDepartment?.id)
                }
            }
        }

        //noinspection GroovyAssignabilityCheck
        logForDebug("Обновление атрибутов Физлиц (" + ScriptUtils.calcTimeMillis(time))
        println "Identification #=${declarationData.id} Обновление атрибутов Физлиц (${ScriptUtils.calcTimeMillis(time)})"
        time = System.currentTimeMillis()

        insertPersonList.removeAll(updatePersonReferenceList)
        //update reference to ref book
        if (!updatePersonReferenceList.isEmpty()) {
            updatePrimaryToRefBookPersonReferences(updatePersonReferenceList)
        }

        if (!updatePersonList.isEmpty()) {
            try {
                personService.updatePersons(new ArrayList<NaturalPerson>(updatePersonList))
            } catch (Exception e) {
                e.printStackTrace()
                if (ExceptionUtils.indexOfThrowable(e, DeadlockLoserDataAccessException.class) != -1) {
                    e.printStackTrace()
                    logger.error("Не выполнена операция \"Идентификация\" для налоговой формы: " +
                            "${declarationService.getFullDeclarationDescription(declarationData.id)}. Причина: " +
                            "Параллельно запущены операции идентификации ФЛ, в ходе которых произошла попытка обновить " +
                            "данные одного и того же ФЛ. Необходимо запустить идентификацию повторно.")
                }
                throw e
            }

        }
        logForDebug("Идентификация и обновление (" + ScriptUtils.calcTimeMillis(time))
        println "Identification #=${declarationData.id} Идентификация и обновление (${ScriptUtils.calcTimeMillis(time)})"


        logForDebug("Обновлено записей: " + updCnt)
        println "Identification #=${declarationData.id} Обновлено записей: ${updCnt})"

        if (!firstAttemptToCreate) {
            preCreateNaturalPersonRefBookRecords()
        }

    }

    String performDocNumber(IdDoc personDocument) {
        List<RefBookDocType> docTypes = getDocTypeRefBookList()
        String toReturn = personDocument.documentNumber
        if (docTypes.contains(personDocument.docType)) {
            String docNumber = personDocument.documentNumber
            if (ScriptUtils.checkDul(personDocument.docType.code, BaseWeightCalculator.prepareStringDul(personDocument.documentNumber).toUpperCase(), "") == null) {
                toReturn = ScriptUtils.formatDocNumber(personDocument.docType.code, docNumber.replaceAll("[^А-Яа-я\\w]", "").toUpperCase())
            }
        }
        return toReturn
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
            if ((asnuId != null && asnuId.equals(personIdentifier.getAsnu().getId())) && BaseWeightCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {
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
    static boolean checkIncReportFlag(NaturalPerson naturalPerson, StringBuilder messageBuilder) {
        boolean toReturn = false
        List personDocumentList = naturalPerson.getDocuments()

        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            //индекс документа в списке personDocumentList который включается в отчетность
            int incRepIndex = IdentificationUtils.selectIncludeReportDocumentIndex(naturalPerson, personDocumentList)

            for (int i = 0; i < personDocumentList.size(); i++) {

                //noinspection GroovyUncheckedAssignmentOfMemberOfRawType
                IdDoc personDocument = personDocumentList.get(i)

                if (i == incRepIndex) {
                    if (naturalPerson.reportDoc?.getDocType()?.code != personDocument?.getDocType()?.code || BaseWeightCalculator.prepareStringDul(naturalPerson.reportDoc?.documentNumber).toUpperCase() != BaseWeightCalculator.prepareStringDul(personDocument?.documentNumber).toUpperCase()) {
                        String oldValue = null
                        if (naturalPerson?.reportDoc?.getDocumentNumber()) {
                            oldValue = String.format("%s - (%s) %s", naturalPerson?.reportDoc?.getDocumentNumber(), naturalPerson?.reportDoc?.getDocType()?.getCode(), naturalPerson?.reportDoc?.getDocType()?.getName())
                        }
                        String newValue = null
                        if (personDocument?.getDocumentNumber()) {
                            newValue = String.format("%s - (%s) %s", personDocument?.getDocumentNumber(), personDocument?.getDocType()?.getCode(), personDocument?.getDocType()?.getName())
                        }
                        toReturn = true
                        naturalPerson.setReportDoc(personDocument)

                        messageBuilder.append(makeUpdateMessage("ДУЛ, включаемый в отчетность", oldValue, newValue))
                    }
                    if (naturalPerson.reportDoc?.getId() == null) {
                        toReturn = true
                        naturalPerson.setReportDoc(personDocument)
                    }
                }
            }

        }
        return toReturn
    }

    boolean updatePerson(NaturalPerson refBookPerson, NaturalPerson primaryPerson, StringBuilder infoBuilder) {
        boolean updated = false
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getLastName(), primaryPerson.getLastName())) {
            infoBuilder.append(makeUpdateMessage("Фамилия", refBookPerson.getLastName(), primaryPerson.getLastName()))
            refBookPerson.setLastName(primaryPerson.getLastName())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getFirstName(), primaryPerson.getFirstName())) {
            infoBuilder.append(makeUpdateMessage("Имя", refBookPerson.getFirstName(), primaryPerson.getFirstName()))
            refBookPerson.setFirstName(primaryPerson.getFirstName())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getMiddleName(), primaryPerson.getMiddleName())) {
            infoBuilder.append(makeUpdateMessage("Отчество", refBookPerson.getMiddleName(), primaryPerson.getMiddleName()))
            refBookPerson.setMiddleName(primaryPerson.getMiddleName())
            updated = true
        }
        if (!ScriptUtils.equalsNullSafe(refBookPerson.getBirthDate(), primaryPerson.getBirthDate())) {
            infoBuilder.append(makeUpdateMessage("Дата рождения", refBookPerson.getBirthDate(), primaryPerson.getBirthDate()))
            refBookPerson.setBirthDate(primaryPerson.getBirthDate())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getCitizenship()?.getCode(), primaryPerson.getCitizenship()?.getCode())) {
            if (!primaryPerson.getCitizenship()?.code || !getCountryRefBookList().code.contains(primaryPerson.getCitizenship()?.code)) {
                createMsg(primaryPerson.getCitizenship()?.code ?: "_", C_CITIZENSHIP, R_CITIZENSHIP, CLAUSE_END_UPDATE, primaryPerson)
            } else {
                infoBuilder.append(makeUpdateMessage("Гражданство", refBookPerson.getCitizenship()?.getCode(), primaryPerson.getCitizenship()?.getCode()))
                refBookPerson.setCitizenship(primaryPerson.getCitizenship())
                updated = true
            }
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getInn(), primaryPerson.getInn())) {
            String fio = (primaryPerson.lastName ?: "") + " " + (primaryPerson.firstName ?: "") + " " + (primaryPerson.middleName ?: "")
            String inp = primaryPerson.personIdentifier && primaryPerson.personIdentifier.inp ? primaryPerson.personIdentifier.inp : ""
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
            if (primaryPerson.inn == null) {
                if (primaryPerson.citizenship?.code == "643") {
                    logger.warnExp("Для физического лица ФИО: \"%s\", ИНП: %s в Разделе 1 не указано значение ИНН в РФ. ИНН в РФ не будет сохраняться в Реестр физических лиц",
                            "\"ИНН\" не указан",
                            fioAndInp,
                            fio,
                            primaryPerson.personIdentifier && primaryPerson.personIdentifier.inp ? primaryPerson.personIdentifier.inp : "")
                }
            } else {
                String checkInn = ScriptUtils.checkInn(primaryPerson.inn)
                if (checkInn != null) {
                    logger.warnExp("Для физического лица ФИО: \"%s\", ИНП: %s в Разделе 1 указано некорректное значение ИНН в РФ  (%s). Причина: %s. Это значение ИНН в РФ не будет сохраняться в Реестр физических лиц",
                            "\"ИНН\" не соответствует формату",
                            fioAndInp,
                            fio,
                            primaryPerson.personIdentifier && primaryPerson.personIdentifier.inp ? primaryPerson.personIdentifier.inp : "",
                            primaryPerson.inn,
                            checkInn)
                } else {
                    infoBuilder.append(makeUpdateMessage("ИНН в РФ", refBookPerson.getInn(), primaryPerson.getInn()))
                    refBookPerson.setInn(primaryPerson.getInn())
                    updated = true
                }
            }
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getInnForeign(), primaryPerson.getInnForeign())) {
            infoBuilder.append(makeUpdateMessage("ИНН в стране гражданства", refBookPerson.getInnForeign(), primaryPerson.getInnForeign()))
            refBookPerson.setInnForeign(primaryPerson.getInnForeign())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getSnils(), primaryPerson.getSnils())) {
            String fio = (primaryPerson.lastName ?: "") + " " + (primaryPerson.firstName ?: "") + " " + (primaryPerson.middleName ?: "")
            String inp = primaryPerson.personIdentifier && primaryPerson.personIdentifier.inp ? primaryPerson.personIdentifier.inp : ""
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, inp])
            if (!ScriptUtils.checkSnils(primaryPerson.snils)) {
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату. %s",
                        "СНИЛС", primaryPerson.snils ?: "_", CLAUSE_END_UPDATE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, primaryPerson.num ?: "")
                logger.warnExp("%s. %s.", "\"СНИЛС\" не соответствует формату", fioAndInp, pathError,
                        errMsg)
            } else {
                infoBuilder.append(makeUpdateMessage("СНИЛС", refBookPerson.getSnils(), primaryPerson.getSnils()))
                refBookPerson.setSnils(primaryPerson.getSnils())
                updated = true
            }
        }
        if (!ScriptUtils.equalsNullSafe(refBookPerson.getTaxPayerState()?.getCode(), primaryPerson.getTaxPayerState()?.getCode())) {
            if (!primaryPerson.getTaxPayerState()?.code || !getTaxpayerStatusRefBookList().code.contains(primaryPerson.getTaxPayerState()?.code)) {
                createMsg(primaryPerson.getTaxPayerState()?.code ?: "_", C_STATUS, R_STATUS, CLAUSE_END_UPDATE, primaryPerson)
            } else {
                infoBuilder.append(makeUpdateMessage("Статус налогоплательщика", refBookPerson.getTaxPayerState()?.getCode(), primaryPerson.getTaxPayerState()?.getCode()))
                refBookPerson.setTaxPayerState(primaryPerson.getTaxPayerState())
                updated = true
            }
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getSource()?.getCode(), primaryPerson.getSource()?.getCode())) {
            refBookPerson.setSource(primaryPerson.getSource())
            updated = true
        }

        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getPostalCode(), primaryPerson.getAddress().getPostalCode())) {
            infoBuilder.append(makeUpdateMessage("Индекс", refBookPerson.getAddress().getPostalCode(), primaryPerson.getAddress().getPostalCode()))
            refBookPerson.getAddress().setPostalCode(primaryPerson.getAddress().getPostalCode())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getRegionCode(), primaryPerson.getAddress().getRegionCode())) {
            infoBuilder.append(makeUpdateMessage("Регион", refBookPerson.getAddress().getRegionCode(), primaryPerson.getAddress().getRegionCode()))
            refBookPerson.getAddress().setRegionCode(primaryPerson.getAddress().getRegionCode())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getDistrict(), primaryPerson.getAddress().getDistrict())) {
            infoBuilder.append(makeUpdateMessage("Район", refBookPerson.getAddress().getDistrict(), primaryPerson.getAddress().getDistrict()))
            refBookPerson.getAddress().setDistrict(primaryPerson.getAddress().getDistrict())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getCity(), primaryPerson.getAddress().getCity())) {
            infoBuilder.append(makeUpdateMessage("Город", refBookPerson.getAddress().getCity(), primaryPerson.getAddress().getCity()))
            refBookPerson.getAddress().setCity(primaryPerson.getAddress().getCity())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getLocality(), primaryPerson.getAddress().getLocality())) {
            infoBuilder.append(makeUpdateMessage("Населенный пункт", refBookPerson.getAddress().getLocality(), primaryPerson.getAddress().getLocality()))
            refBookPerson.getAddress().setLocality(primaryPerson.getAddress().getLocality())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getStreet(), primaryPerson.getAddress().getStreet())) {
            infoBuilder.append(makeUpdateMessage("Улица", refBookPerson.getAddress().getStreet(), primaryPerson.getAddress().getStreet()))
            refBookPerson.getAddress().setStreet(primaryPerson.getAddress().getStreet())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getHouse(), primaryPerson.getAddress().getHouse())) {
            infoBuilder.append(makeUpdateMessage("Дом", refBookPerson.getAddress().getHouse(), primaryPerson.getAddress().getHouse()))
            refBookPerson.getAddress().setHouse(primaryPerson.getAddress().getHouse())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getBuild(), primaryPerson.getAddress().getBuild())) {
            infoBuilder.append(makeUpdateMessage("Корпус", refBookPerson.getAddress().getBuild(), primaryPerson.getAddress().getBuild()))
            refBookPerson.getAddress().setBuild(primaryPerson.getAddress().getBuild())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getAppartment(), primaryPerson.getAddress().getAppartment())) {
            infoBuilder.append(makeUpdateMessage("Квартира", refBookPerson.getAddress().getAppartment(), primaryPerson.getAddress().getAppartment()))
            refBookPerson.getAddress().setAppartment(primaryPerson.getAddress().getAppartment())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getCountry()?.getCode(), primaryPerson.getAddress().getCountry()?.getCode())) {
            infoBuilder.append(makeUpdateMessage("Код страны проживания", refBookPerson.getAddress().getCountry().getCode(), primaryPerson.getAddress().getCountry().getCode()))
            refBookPerson.getAddress().setCountry(primaryPerson.getAddress().getCountry())
            updated = true
        }
        if (!BaseWeightCalculator.isEqualsNullSafeStr(refBookPerson.getAddress().getAddressIno(), primaryPerson.getAddress().getAddressIno())) {
            infoBuilder.append(makeUpdateMessage("Адрес за пределами РФ", refBookPerson.getAddress().getAddressIno(), primaryPerson.getAddress().getAddressIno()))
            refBookPerson.getAddress().setAddressIno(primaryPerson.getAddress().getAddressIno())
            updated = true
        }
        return updated
    }

    public static <T> String makeUpdateMessage(String attrName, T oldValue, T newValue) {
        return new StringBuilder("[")
                .append(attrName)
                .append(": ")
                .append(oldValue != null ? oldValue : "__")
                .append(" -> ")
                .append(newValue != null ? newValue : "__")
                .append("]").toString()
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
     * Получить дату начала отчетного периода
     * @return
     */
    Date getReportPeriodStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
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
     * Получить "АСНУ"
     * @return
     */
    Map<Long, RefBookAsnu> getRefAsnu() {
        if (asnuCache.size() == 0) {
            List<RefBookAsnu> asnuList = refBookService.findAllAsnu();
            asnuList.each { RefBookAsnu asnu ->
                Long asnuId = (Long) asnu?.id
                asnuCache.put(asnuId, asnu)
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

    /**
     * Объединяет фамилию имя и отчество физлица
     * @param naturalPerson объект физлица
     * @return строка вида <фамилия> <имя> <отчество>
     */
    static String buildFio(RegistryPerson naturalPerson) {
        return naturalPerson.lastName + " " + naturalPerson.firstName + (naturalPerson.middleName ? " " + naturalPerson.middleName : "")
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

    class SearchMapper implements RowCallbackHandler {
        Map<Long, Long> result = new HashMap<>()
        @Override
        void processRow(ResultSet rs) throws SQLException {
            result.put(SqlUtils.getLong(rs, "person_id"), SqlUtils.getLong(rs, "ref_book_person_id"))
        }
    }

    class NaturalPersonScriptRowMapper implements RowMapper<NaturalPerson> {
        private Map<Long, Map<Long, PersonIdentifier>> identitiesMap = [:]
        private Map<Long, Map<Long, IdDoc>> documentsMap = [:]
        private Map<Long, Map<Long, PersonTb>> personTbMap = [:]

        @Override
        NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            NaturalPerson naturalPerson = buildNaturalPerson(rs)

            addPersonDocument(rs, naturalPerson);

            // инициализируем ДУЛ включаемый в отчетность
            initReportDoc(rs, naturalPerson);

            //Добавляем идентификаторы
            addPersonIdentifier(rs, naturalPerson);

            // добавляем Тербанки
            addPersonTb(rs, naturalPerson);

            //Адрес
            Address address = buildAddress(rs);
            naturalPerson.setAddress(address);

            return naturalPerson
        }

        void addPersonIdentifier(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {

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

                personIdentifier.setInp(rs.getString("inp"));
                RefBookAsnu asnu = new RefBookAsnu();
                asnu.setId(SqlUtils.getLong(rs, "as_nu"));
                personIdentifier.setAsnu(asnu);
                personIdentifier.setPerson(naturalPerson);
                personIdentityMap.put(personIdentifierId, personIdentifier);
                naturalPerson.getPersonIdentityList().add(personIdentifier);
            }
        }

        private NaturalPerson buildNaturalPerson(ResultSet rs) throws SQLException {
                NaturalPerson person = new NaturalPerson();

                //person
                person.setId(SqlUtils.getLong(rs, "ref_book_person_id"));

                person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
                person.setStartDate(rs.getDate("start_date"));
                person.setEndDate(rs.getDate("end_date"));

                person.setLastName(rs.getString("last_name"));
                person.setFirstName(rs.getString("first_name"));
                person.setMiddleName(rs.getString("middle_name"));
                person.setInn(rs.getString("inn"));
                person.setInnForeign(rs.getString("inn_foreign"));
                person.setSnils(rs.getString("snils"));
                person.setBirthDate(rs.getDate("birth_date"));

                //ссылки на справочники
                person.setTaxPayerState(refDataHolder.getTaxpayerStatusById(SqlUtils.getLong(rs, "taxpayer_state")));
                person.setCitizenship(refDataHolder.getCountryById(SqlUtils.getLong(rs, "citizenship")));

                //additional
                person.setSource(refDataHolder.getAsnuMap().get(SqlUtils.getLong(rs, "source_id")));
                person.setRecordId(SqlUtils.getLong(rs, "record_id"));
                person.setOldId(SqlUtils.getLong(rs, "old_id"));
                person.setVip(rs.getBoolean("vip"));

                return person;
        }

        private Address buildAddress(ResultSet rs) throws SQLException {
            Address address = new Address();
            RefBookCountry country = refDataHolder.getCountryById(SqlUtils.getLong(rs, "country_id"));
            address.setCountry(country != null ? country : new RefBookCountry());
            address.setRegionCode(rs.getString("region_code"));
            address.setPostalCode(rs.getString("postal_code"));
            address.setDistrict(rs.getString("district"));
            address.setCity(rs.getString("city"));
            address.setLocality(rs.getString("locality"));
            address.setStreet(rs.getString("street"));
            address.setHouse(rs.getString("house"));
            address.setBuild(rs.getString("build"));
            address.setAppartment(rs.getString("appartment"));
            address.setAddressIno(rs.getString("address_foreign"));
            return address;
        }

        private void addPersonDocument(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
            Long refBookPersonId = naturalPerson.getId();
            Long docId = SqlUtils.getLong(rs, "ref_book_id_doc_id");
            Map<Long, IdDoc> pesonDocumentMap = documentsMap.get(refBookPersonId);

            if (pesonDocumentMap == null) {
                pesonDocumentMap = new HashMap<Long, IdDoc>();
                documentsMap.put(refBookPersonId, pesonDocumentMap);
            }

            if (docId != null && !pesonDocumentMap.containsKey(docId)) {
                Long docTypeId = SqlUtils.getLong(rs, "doc_id");
                RefBookDocType docType = refDataHolder.getDocTypeById(docTypeId);
                IdDoc personDocument = new IdDoc();
                personDocument.setId(docId);

                personDocument.setDocType(docType);
                personDocument.setDocumentNumber(rs.getString("doc_number"));
                personDocument.setPerson(naturalPerson);
                pesonDocumentMap.put(docId, personDocument);
                naturalPerson.getDocuments().add(personDocument);
            }
        }

        private void initReportDoc(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
            Long reportDocId = rs.getLong("report_doc");
            IdDoc personDocument = documentsMap.get(naturalPerson.getId()).get(reportDocId);
            naturalPerson.setReportDoc(personDocument != null ? personDocument: new IdDoc());
        }

        private void addPersonTb(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
            Long refBookPersonId = naturalPerson.getId();
            Long personTbId = SqlUtils.getLong(rs, "ref_book_person_tb_id");
            Map<Long, PersonTb> tbMap = personTbMap.get(refBookPersonId);

            if (tbMap == null) {
                tbMap = new HashMap<>();
                personTbMap.put(refBookPersonId, tbMap);
            }

            if (personTbId != null && !tbMap.containsKey(personTbId)) {
                PersonTb personTb = new PersonTb();
                personTb.setId(personTbId);
                Department department = new Department();
                department.setId(rs.getInt("tb_department_id"));
                personTb.setTbDepartment(department);
                personTb.setImportDate(rs.getDate("import_date"));
                personTb.setPerson(naturalPerson);
                tbMap.put(personTbId, personTb);
                naturalPerson.getPersonTbList().add(personTb);
            }
        }


    }
}