package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ScriptSpecificDeclarationDataReportHolder
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.identification.*
import com.aplana.sbrf.taxaccounting.dao.identification.*
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.service.impl.DeclarationDataScriptParams
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.script.*
import groovy.transform.CompileStatic
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.transform.TypeChecked
import groovy.util.slurpersupport.NodeChild
import groovy.xml.MarkupBuilder
import org.springframework.jdbc.core.RowMapper
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

    import javax.script.ScriptException
    import javax.xml.namespace.QName
    import javax.xml.stream.XMLEventReader
    import javax.xml.stream.XMLInputFactory
    import javax.xml.stream.events.*
import javax.xml.ws.LogicalMessage
import java.sql.ResultSet
    import java.sql.SQLException
import java.text.ParseException
import java.text.SimpleDateFormat
    import java.util.regex.Matcher
    import java.util.regex.Pattern

    /**
     * Скрипт макета декларации РНУ-НДФЛ(первичная)
     */
    initConfiguration()
    switch (formDataEvent) {
        case FormDataEvent.CREATE:
            checkCreate()
            break
        case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
            moveAcceptedToCreated();
            break

        case FormDataEvent.IMPORT_TRANSPORT_FILE:
            importData()
            // Формирование pdf-отчета формы
            declarationService.createPdfReport(logger, declarationData, userInfo)
            break
        case FormDataEvent.PREPARE_SPECIFIC_REPORT:
            // Подготовка для последующего формирования спецотчета
            prepareSpecificReport()
            break
        case FormDataEvent.GET_SOURCES: //формирование списка приемников
            getSourcesListForTemporarySolution()
            break
        case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
            createXlsxReport()
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

    @Field
    final Logger logger = getProperty("logger")
    @Field
    final DeclarationData declarationData = getProperty("declarationData")
    @Field
    final DepartmentReportPeriodService departmentReportPeriodService = getProperty("departmentReportPeriodService")
    @Field
    final DeclarationService declarationService = getProperty("declarationService")
    @Field
    final ReportPeriodService reportPeriodService = getProperty("reportPeriodService")
    @Field
    final DepartmentService departmentService = getProperty("departmentService")
    @Field
    final CalendarService calendarService = getProperty("calendarService")
    @Field
    final String DATE_FORMAT = "dd.MM.yyyy"
    @Field
    final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
    @Field
    Boolean showTiming = false

    def initConfiguration() {
        final ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
        String showTiming = configurationParamModel?.get(ConfigurationParam.SHOW_TIMING)?.get(0)?.get(0)
        String limitIdent = configurationParamModel?.get(ConfigurationParam.LIMIT_IDENT)?.get(0)?.get(0)
        if (showTiming.equals("1")) {
            this.showTiming = true
        }
        SIMILARITY_THRESHOLD = limitIdent? (Double.valueOf(limitIdent) * 1000).intValue() : 0
    }

    def logForDebug(String message, Object... args) {
        if (showTiming) {
            logger.info(message, args)
        }
    }

    def getProperty(String name) {
        try{
            return super.getProperty(name)
        } catch (MissingPropertyException e) {
            return null
        }
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

    def moveAcceptedToCreated(){
        List<Relation> destinationInfo = getDestinationInfo(false);
        for (Relation relation : destinationInfo) {
            if (relation.declarationState.equals(State.ACCEPTED)){
                throw new ServiceException("Ошибка изменения состояния формы. Данная форма не может быть возвращена в состояние 'Создана', так как используется в КНФ с состоянием 'Принята', номер формы: "+relation.declarationDataId);
            }
        }
    }


    //------------------ Calculate ----------------------
    /**
     * Порог схожести при идентификации физлиц 0..1000, 1000 - совпадение по всем параметрам
     */
    @Field
    int SIMILARITY_THRESHOLD;

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

    @Field List<DocType> docTypeRefBookCache = [];

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

    @Field List<TaxpayerStatus> taxpayerStatusRefBookCache = [];

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

    /**
     * Карта соответствия адреса формы адресу в справочнике ФИАС
     */
    @Field Map<Long, FiasCheckInfo> fiasAddressIdsCache = [:];

    Map<Long, FiasCheckInfo> getFiasAddressIdsMap() {
        if (fiasAddressIdsCache.isEmpty()) {
            fiasAddressIdsCache = fiasRefBookService.checkAddressByFias(declarationData.id, 1);
        }
        return fiasAddressIdsCache;
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
                String inp = ndflPerson.getPersonIdentifier()?.inp?:""
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

        Map<Long, FiasCheckInfo> fiasAddressIdsMap = getFiasAddressIdsMap();
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
            localCalendar.add(Calendar.YEAR, 100);
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

        logForDebug("Начало расчета ПНФ");

        if (declarationData.asnuId == null) {
            throw new ServiceException("Для " + declarationData.id + ", " + declarationData.fileName + " не указан код АСНУ загрузившей данные!");
        }

        //выставляем параметр что скрипт не формирует новый xml-файл
        calculateParams.put(DeclarationDataScriptParams.NOT_REPLACE_XML, Boolean.TRUE);

        refBookPersonService.clearRnuNdflPerson(declarationData.id)

        //Получаем список всех ФЛ в первичной НФ
        List<NaturalPerson> primaryPersonDataList = refBookPersonService.findNaturalPersonPrimaryDataFromNdfl(declarationData.id, createPrimaryRowMapper(false));
        if (logger.containsLevel(LogLevel.ERROR)) {
            return
        }

        logForDebug("В ПНФ номер " + declarationData.id + " получены записи о физ. лицах (" + primaryPersonDataList.size() + " записей, " + calcTimeMillis(time));

        //println "Find primary data in " + declarationData.id + " found: " + primaryPersonDataList.size() + " person " + calcTimeMillis(time)

        Map<Long, NaturalPerson> primaryPersonMap = primaryPersonDataList.collectEntries {
            [it.getPrimaryPersonId(), it]
        }

        //Заполнени временной таблицы версий
        time = System.currentTimeMillis();
        refBookPersonService.fillRecordVersions(getRefBookPersonVersionTo());
        logForDebug("Заполнение таблицы версий (" + calcTimeMillis(time));

        //println "Fill version table " + calcTimeMillis(time)

        //Шаг 1. список физлиц первичной формы для создания записей в справочниках
        time = System.currentTimeMillis();
        List<NaturalPerson> insertPersonList = refBookPersonService.findPersonForInsertFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createPrimaryRowMapper(true));
        logForDebug("Предварительная выборка новых данных (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

        //println "Select for insert " + insertPersonList.size() + " person " + calcTimeMillis(time)

        time = System.currentTimeMillis();
        createNaturalPersonRefBookRecords(insertPersonList);
        logForDebug("Создание (" + insertPersonList.size() + " записей, " + calcTimeMillis(time));

        //println "Insert: " + insertPersonList.size() + calcTimeMillis(time)

        //Шаг 2. идентификатор записи в первичной форме - список подходящих записей для идентификации по весам и обновления справочников
        time = System.currentTimeMillis();
        Map<Long, Map<Long, NaturalPerson>> similarityPersonMap = refBookPersonService.findPersonForUpdateFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
        logForDebug("Предварительная выборка по значимым параметрам (" + similarityPersonMap.size() + " записей, " + calcTimeMillis(time));

        //println "Select for update: " + similarityPersonMap.size() + calcTimeMillis(time)

        time = System.currentTimeMillis();
        updateNaturalPersonRefBookRecords(primaryPersonMap, similarityPersonMap);
        logForDebug("Обновление записей (" + calcTimeMillis(time));

        //println "Update ref: " + calcTimeMillis(time)

        time = System.currentTimeMillis();
        Map<Long, Map<Long, NaturalPerson>> checkSimilarityPersonMap = refBookPersonService.findPersonForCheckFromPrimaryRnuNdfl(declarationData.id, declarationData.asnuId, getRefBookPersonVersionTo(), createRefbookHandler());
        logForDebug("Основная выборка по всем параметрам (" + checkSimilarityPersonMap.size() + " записей, " + calcTimeMillis(time));

        //println "Select for check: " + calcTimeMillis(time)

        time = System.currentTimeMillis();
        updateNaturalPersonRefBookRecords(primaryPersonMap, checkSimilarityPersonMap);
        logForDebug("Обновление записей (" + calcTimeMillis(time));

        //println "Update reference: " + calcTimeMillis(time)
        //println "End: " + calcTimeMillis(timeFull)

        logForDebug("Завершение расчета ПНФ (" + calcTimeMillis(timeFull));
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
                if (personDocument != null && personDocument.docType != null) {
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
                logForDebug(noticeMsg);
                createCnt++;
            }

        }

        logForDebug("Создано записей: " + createCnt)

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

            conformityMap.put(primaryPersonId, refBookPerson);

            //Адрес нужно создать заранее и получить Id
            if (refBookPerson != null) {
                if (primaryPerson.getAddress() != null && refBookPerson.getAddress() == null) {
                    insertAddressList.add(primaryPerson.getAddress());
                }
            }

            if (msgCnt <= maxMsgCnt){
                logForDebug("Идентификация (" + calcTimeMillis(inTime));
            }

            msgCnt++;
        }

        logForDebug("Идентификация ФЛ, обновление адресов (" + calcTimeMillis(time));

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
                /*
               Если загружаемая НФ находится в периоде который заканчивается раньше чем версия записи в справочнике,
               тогда версия записи в справочнике меняется на более раннюю дату, без изменения атрибутов. Такая ситуация
               вряд ли может возникнуть на практике и проверка создана по заданию тестировщиков.
              */
                if (refBookPerson.getVersion() > getReportPeriodEndDate()) {
                    Map<String, RefBookValue> downgradePerson = mapPersonAttr(refBookPerson)
                    downGradeRefBookVersion(downgradePerson, refBookPerson.getId(), RefBook.Id.PERSON.getId())
                }

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
                        if (primaryPersonDocument.docType != null) {
                            insertDocumentList.add(primaryPersonDocument);
                            refBookPerson.getPersonDocumentList().add(primaryPersonDocument);
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

                        String primaryInp = BaseWeigthCalculator.prepareString(primaryPersonIdentifier.getInp());
                        String refbookInp = BaseWeigthCalculator.prepareString(refBookPersonIdentifier.getInp());

                        if (!BaseWeigthCalculator.isEqualsNullSafeStr(primaryInp, refbookInp)) {

                            AttributeChangeEvent changeEvent = new AttributeChangeEvent("INP", primaryInp);
                            changeEvent.setCurrentValue(new RefBookValue(RefBookAttributeType.STRING, refbookInp));
                            changeEvent.setType(AttributeChangeEventType.REFRESHED);
                            taxpayerIdentityAttrCnt.processAttr(changeEvent);

                            Map<String, RefBookValue> refBookPersonIdentifierValues = mapPersonIdentifierAttr(primaryPersonIdentifier);
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

                    logForDebug(String.format("Обновлена запись в справочнике 'Физические лица': %d, %s %s %s", recordId,
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
                logForDebug("Обновление (" + calcTimeMillis(inTime));
            }

            msgCnt++;

        }

        logForDebug("Обновление ФЛ, документов (" + calcTimeMillis(time));
        time = System.currentTimeMillis();
        //println "crete and update reference"

        //crete and update reference
        createNaturalPersonRefBookRecords(insertPersonList);

        //update reference to ref book
        if (!updatePersonReferenceList.isEmpty()) {
            updatePrimaryToRefBookPersonReferences(updatePersonReferenceList);
        }

        logForDebug("Обновление справочников (" + calcTimeMillis(time));
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

        logForDebug("Идентификация и обновление (" + calcTimeMillis(time));

        logForDebug("Обновлено записей: " + updCnt);

    }

    def downGradeRefBookVersion(Map<String, RefBookValue> refBookValue, Long uniqueRecordId, Long refBookId) {
        Date newVersion = getReportPeriodStartDate()
        refBookValue.put("VERSION", new RefBookValue(RefBookAttributeType.DATE, newVersion))
        getProvider(refBookId).updateRecordVersionWithoutLock(logger, uniqueRecordId, newVersion, null, refBookValue)
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

        if (personDocumentList != null && !personDocumentList.isEmpty()) {
            //индекс документа в списке personDocumentList который выбран главным, всем остальным необходимо выставить статус incRep 0
            int incRepIndex = IdentificationUtils.selectIncludeReportDocumentIndex(naturalPerson, personDocumentList);

            for (int i = 0; i < personDocumentList.size(); i++) {

                PersonDocument personDocument = personDocumentList.get(i);
                String docInf = new StringBuilder().append(personDocument.getId()?:"0").append(", ").append(personDocument.getDocumentNumber()).append(" ").toString();

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
        putOrUpdate(values, "COUNTRY_ID", RefBookAttributeType.REFERENCE, address.getCountry()?.getId(), {val -> findCountryRecordId(val)}, attributeChangeListener);
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
        putOrUpdate(values, "LAST_NAME", RefBookAttributeType.STRING, person.getLastName(), null, attributeChangeListener);
        putOrUpdate(values, "FIRST_NAME", RefBookAttributeType.STRING, person.getFirstName(), null, attributeChangeListener);
        putOrUpdate(values, "MIDDLE_NAME", RefBookAttributeType.STRING, person.getMiddleName(), null, attributeChangeListener);
        putOrUpdate(values, "INN", RefBookAttributeType.STRING, person.getInn(), null, attributeChangeListener);
        putOrUpdate(values, "INN_FOREIGN", RefBookAttributeType.STRING, person.getInnForeign(), null, attributeChangeListener);
        putOrUpdate(values, "SNILS", RefBookAttributeType.STRING, person.getSnils(),null, attributeChangeListener);
        putOrUpdate(values, "RECORD_ID", RefBookAttributeType.NUMBER, person.getRecordId(), null, attributeChangeListener);
        putOrUpdate(values, "BIRTH_DATE", RefBookAttributeType.DATE, person.getBirthDate(), null, attributeChangeListener);
        putOrUpdate(values, "BIRTH_PLACE", RefBookAttributeType.STRING, null, null, attributeChangeListener);
        putOrUpdate(values, "ADDRESS", RefBookAttributeType.REFERENCE, person.getAddress()?.getId(), null, attributeChangeListener);
        putOrUpdate(values, "EMPLOYEE", RefBookAttributeType.NUMBER, person.getEmployee(), null, attributeChangeListener);
        putOrUpdate(values, "CITIZENSHIP", RefBookAttributeType.REFERENCE, person.getCitizenship()?.getId(), {val -> findCountryRecordId(val)}, attributeChangeListener);
        putOrUpdate(values, "TAXPAYER_STATE", RefBookAttributeType.REFERENCE, person.getTaxPayerStatus()?.getId(), {val -> findTaxpayerStatusById(val)}, attributeChangeListener);
        putOrUpdate(values, "SOURCE_ID", RefBookAttributeType.REFERENCE, declarationData.asnuId, {val -> findAsnuCodeById(val)}, attributeChangeListener);
        putOrUpdate(values, "OLD_ID", RefBookAttributeType.REFERENCE, null, null, attributeChangeListener);
    }

    def mapPersonAttr(NaturalPerson person) {
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
        putValue(values, "OLD_ID", RefBookAttributeType.REFERENCE, null);
        return values;
    }

    def mapPersonDocumentAttr(PersonDocument personDocument) {
        Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
        putValue(values, "PERSON_ID", RefBookAttributeType.REFERENCE, personDocument.getNaturalPerson().getId());
        putValue(values, "DOC_NUMBER", RefBookAttributeType.STRING, personDocument.getDocumentNumber());
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

            def refBookName = getProvider(refBookId).refBook.name
            logForDebug("Добавление записей: cправочник «${refBookName}», количество ${identityObjectList.size()}")

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

    def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, getValue, AttributeChangeListener attributeChangedListener) {
        putOrUpdate(valuesMap, attrName, type, value, getValue != null ? getValue: ({val -> val?.toString()}), attributeChangedListener, { attrType, valueA, valueB ->
            isAttrEquals(attrType, valueA, valueB);
        });
    }

    def putOrUpdate(Map<String, RefBookValue> valuesMap, String attrName, RefBookAttributeType type, Object value, getValue, AttributeChangeListener attributeChangedListener, attrEquator) {

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
    Map<Long, Map<String, RefBookValue>> getActualRefInpMapByDeclarationDataId() {
        if (inpActualCache.isEmpty()) {
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${declarationData.id} AND ref_book_id_tax_payer.person_id = np.person_id)"
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(REF_BOOK_ID_TAX_PAYER_ID, whereClause)

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
     * По id статуса налогоплательщика найти код статуса в кэше справочника
     * @param String code
     * @return Long id
     */
    def findTaxpayerStatusById(Long id) {
        def taxpayerStatusMap = getRefTaxpayerStatusCode()
        return id != null ? (taxpayerStatusMap.get(id)?:"") : ""
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

    /**
     * По id страны найти код записи в кэше справочника
     * @param String code
     * @return Long id
     */
    def findCountryRecordId(Long id) {
        def citizenshipCodeMap = getRefCountryCode()
        return id != null ? (citizenshipCodeMap.get(id)?:"") : ""
    }

    /**
     * По id АСНУ найти код записи в кэше справочника
     * @param String code
     * @return Long id
     */
    def findAsnuCodeById(Long id) {
        def asnuCodeMap = getRefAsnu()
        return id != null ? (asnuCodeMap.get(id)?:"") : ""
    }
    //------------------ GET_SOURCES ----------------------

    List<Relation> getDestinationInfo(boolean isLight){

        List<Relation> destinationInfo = new ArrayList<Relation>();

        //отчетный период в котором выполняется консолидация
        ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)

        //Идентификатор подразделения по которому формируется консолидированная форма
        def parentDepartmentId = declarationData.departmentId
        Department department = departmentService.get(parentDepartmentId)
        List<DeclarationData> declarationDataList = declarationService.findAllDeclarationData(CONSOLIDATED_RNU_NDFL_TEMPLATE_ID, department.id, declarationDataReportPeriod.id);
        for (DeclarationData declarationDataDestination : declarationDataList) {
            if (departmentReportPeriod.correctionDate != null) {
                DepartmentReportPeriod departmentReportPeriodDestination = getDepartmentReportPeriodById(declarationDataDestination.departmentReportPeriodId)
                if (departmentReportPeriodDestination.correctionDate == null || departmentReportPeriod.correctionDate > departmentReportPeriodDestination.correctionDate) {
                    continue
                }
            }
            //Формируем связь источник-приемник
            def relation = getRelation(declarationDataDestination, department, declarationDataReportPeriod, isLight)
            destinationInfo.add(relation)
        }

        return destinationInfo;
    }


    def getSourcesListForTemporarySolution() {
        if (needSources) {
            return
        }

        for (Relation relation : getDestinationInfo(light)) {
            sources.sourceList.add(relation)
        }
        sources.sourcesProcessedByScript = true
    }

    /**
     * Получить запись для источника-приемника.
     *
     * @param declarationData первичная форма
     * @param department подразделение
     * @param period период нф
     * @param monthOrder номер месяца (для ежемесячной формы)
     */
    def getRelation(DeclarationData declarationData, Department department, ReportPeriod period, boolean isLight) {

        Relation relation = new Relation()

        //Привязка отчетных периодов к подразделениям
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData?.departmentReportPeriodId) as DepartmentReportPeriod

        //Макет НФ
        DeclarationTemplate declarationTemplate = getDeclarationTemplateById(declarationData?.declarationTemplateId)

        def isSource = (declarationTemplate.id == PRIMARY_RNU_NDFL_TEMPLATE_ID)
        ReportPeriod rp = departmentReportPeriod.getReportPeriod();

        if (isLight) {
            //Идентификатор подразделения
            relation.departmentId = department.id
            //полное название подразделения
            relation.fullDepartmentName = getDepartmentFullName(department.id)
            //Дата корректировки
            relation.correctionDate = departmentReportPeriod?.correctionDate
            //Вид нф
            relation.declarationTypeName = declarationTemplate?.name
            //Год налогового периода
            relation.year = period.taxPeriod.year
            //Название периода
            relation.periodName = period.name
        }

        //Общие параметры

        //подразделение
        relation.department = department
        //Период
        relation.departmentReportPeriod = departmentReportPeriod
        //Статус ЖЦ
        relation.declarationState = declarationData?.state
        //форма/декларация создана/не создана
        relation.created = (declarationData != null)
        //является ли форма источников, в противном случае приемник
        relation.source = isSource;
        // Введена/выведена в/из действие(-ия)
        relation.status = declarationTemplate.status == VersionedObjectStatus.NORMAL
        // Налог
        relation.taxType = TaxType.NDFL

        //Параметры НФ

        // Идентификатор созданной формы
        relation.declarationDataId = declarationData?.id
        // Вид НФ
        relation.declarationTemplate = declarationTemplate
        return relation

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
                def val = value;
                if (!(key in ["fromBirthDay", "toBirthDay"])) {
                    val = '%'+value+'%'
                }
                resultReportParameters.put(key, val)
            }
        }

        // Ограничение числа выводимых записей
        int startIndex = 1
        int pageSize = 10

        PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

        //Если записи не найдены, то система формирует предупреждение:
        //Заголовок: "Предупреждение"
        //Текст: "Физическое лицо: <Данные ФЛ> не найдено в форме", где <Данные ФЛ> - значение полей формы, по которым выполнялся поиск физического лица, через разделитель "; "
        //Кнопки: "Закрыть"

        if (pagingResult.isEmpty()) {
            subreportParamsToString = { it.collect { (it.value != null ? (((it.value instanceof Date)?it.value.format('dd.MM.yyyy'):it.value) + ";") : "") } join " " }
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
            row.inp = ndflPerson.inp
            row.birthDay = ndflPerson.birthDay
            row.idDocNumber = ndflPerson.idDocNumber
            row.statusNp = getPersonStatusName(ndflPerson.status)
            row.innForeign = ndflPerson.innForeign
            dataRows.add(row)
        }

        int countOfAvailableNdflPerson = pagingResult.size()

        if (countOfAvailableNdflPerson >= pageSize) {
            countOfAvailableNdflPerson = ndflPersonService.findNdflPersonCountByParameters(declarationData.id, resultReportParameters);
        }

        result.setTableColumns(tableColumns);
        result.setDataRows(dataRows);
        result.setCountAvailableDataRows(countOfAvailableNdflPerson)
        scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
        scriptSpecificReportHolder.setSubreportParamValues(params)
    }

    String getPersonStatusName(String statusCode) {
        RefBookDataProvider provider = getProvider(RefBook.Id.TAXPAYER_STATUS.getId())
        PagingResult<Long, Map<String, RefBookValue>> record = provider.getRecords(getReportPeriodEndDate(), null, "CODE = '$statusCode'", null)
        return record.get(0).get("NAME").getValue()
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
        column5.setName("ИНН РФ")
        column5.setWidth(10)
        tableColumns.add(column5)

        Column column6 = new StringColumn()
        column6.setAlias("inp")
        column6.setName("ИНП")
        column6.setWidth(10)
        tableColumns.add(column6)

        Column column7 = new DateColumn()
        column7.setAlias("birthDay")
        column7.setName("Дата рождения")
        column7.setWidth(10)
        tableColumns.add(column7)

        Column column8 = new StringColumn()
        column8.setAlias("idDocNumber")
        column8.setName("№ ДУЛ")
        column8.setWidth(10)
        tableColumns.add(column8)

        Column column9 = new StringColumn()
        column9.setAlias("statusNp")
        column9.setName("Статус налогоплательщика")
        column9.setWidth(30)
        tableColumns.add(column9)

        Column column10 = new StringColumn()
        column10.setAlias("innForeign")
        column10.setName("ИНН Страны гражданства")
        column10.setWidth(10)
        tableColumns.add(column10)

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
        column5.setName("ИНН РФ")
        column5.setWidth(10)
        tableColumns.add(column5)

        Column column6 = new StringColumn()
        column6.setAlias("inp")
        column6.setName("ИНП")
        column6.setWidth(10)
        tableColumns.add(column6)

        Column column7 = new DateColumn()
        column7.setAlias("birthDay")
        column7.setName("Дата рождения")
        column7.setWidth(10)
        tableColumns.add(column7)

        Column column8 = new StringColumn()
        column8.setAlias("idDocNumber")
        column8.setName("№ ДУЛ")
        column8.setWidth(10)
        tableColumns.add(column8)

        Column column9 = new StringColumn()
        column9.setAlias("statusNp")
        column9.setName("Статус налогоплательщика")
        column9.setWidth(30)
        tableColumns.add(column9)

        Column column10 = new StringColumn()
        column10.setAlias("innForeign")
        column10.setName("ИНН Страны гражданства")
        column10.setWidth(10)
        tableColumns.add(column10)

        return tableColumns;
    }

    //------------------ Create Report ----------------------
/**
 * Создать XLSX отчет
 * @return
 */
@TypeChecked
def createXlsxReport() {
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder)getProperty("scriptSpecificReportHolder")
    def params = new HashMap<String, Object>()
    params.put("declarationId", declarationData.getId());

    JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, declarationService.getXmlStream(declarationData.id));

    StringBuilder fileName = new StringBuilder("Реестр_загруженных_данных_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
    exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(fileName.toString())
}

    def createSpecificReport() {
        switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
            case 'rnu_ndfl_person_db':
                createSpecificReportPersonDb();
                break;
            case 'rnu_ndfl_person_all_db':
                createSpecificReportDb();
                scriptSpecificReportHolder.setFileName("РНУ_НДФЛ_${declarationData.id}_${new Date().format('yyyy-MM-dd_HH-mm-ss' )}.xlsx")
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

        def subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
        subReportViewParams['Фамилия'] = row.lastName
        subReportViewParams['Имя'] = row.firstName
        subReportViewParams['Отчество'] = row.middleName
        subReportViewParams['Дата рождения'] = row.birthDay ? row.birthDay?.format(DATE_FORMAT) : ""
        subReportViewParams['№ ДУЛ'] = row.idDocNumber
        if (ndflPerson != null) {
            def params = [NDFL_PERSON_ID : ndflPerson.id];

            def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
            exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
            scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
        } else {
            throw new ServiceException("Не найдены данные для формирования отчета!");
        }
    }

    @TypeChecked
    void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
        try {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT,
                    jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, data);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                    Boolean.TRUE);
            exporter.setParameter(
                    JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                    Boolean.FALSE);

            exporter.exportReport();
            exporter.reset();
        } catch (Exception e) {
            throw new ServiceException(
                    "Невозможно экспортировать отчет в XLSX", e) as Throwable
        }
    }

    /**
     * Формирует спец. отчеты, данные для которых макет извлекает непосредственно из бд
     */
    def createSpecificReportDb() {
        def params = [declarationId : declarationData.id]
        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
        exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
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

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        logForDebug("Начало загрузки данных первичной налоговой формы "+declarationData.id+". Дата начала отчетного периода: "+sdf.format(getReportPeriodStartDate())+", дата окончания: "+sdf.format(getReportPeriodEndDate()));

        //валидация по схеме
        declarationService.validateDeclaration(declarationData, userInfo, logger, dataFile, UploadFileName.substring(0, UploadFileName.lastIndexOf('.')))
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("ТФ не соответствует XSD-схеме. Загрузка невозможна.");
        }

        InputStream xmlInputStream = ImportInputStream;

        if (xmlInputStream == null) {
            throw new ServiceException("Отсутствует значение параметра ImportInputStream!");
        }

        // "Загрузка ТФ РНУ НДФЛ" п.9
        // Проверка соответствия атрибута ДатаОтч периоду в наименовании файла
        // reportPeriodEndDate создаётся на основании периода из имени файла

        File dFile = dataFile

        if(dFile == null){
            throw new ServiceException("Отсутствует значение параметра dataFile!")
        }

        def reportPeriodEndDate = getReportPeriodEndDate().format(DATE_FORMAT)

        def Файл = new XmlSlurper().parse(dFile)
        String reportDate = Файл.СлЧасть.'@ДатаОтч'

        if(reportPeriodEndDate != reportDate ){
            logger.error("В ТФ неверно указана «Отчетная дата»: «${reportDate}». Должна быть указана дата окончания периода ТФ, равная «${reportPeriodEndDate}»")
        }

        //Каждый элемент ИнфЧасть содержит данные об одном физ лице, максимальное число элементов в документе 15000
        QName infoPartName = QName.valueOf('ИнфЧасть')

        //Используем StAX парсер для импорта
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
        XMLEventReader reader = xmlFactory.createXMLEventReader(xmlInputStream)

        def ndflPersonNum = 1;
        def success = 0
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
                        if(processInfoPart(infoPart, ndflPersonNum)) {
                            success++
                        }
                        ndflPersonNum++
                    }
                }
            }
        } finally {
            reader?.close()
        }
        if (success == 0){
            logger.error("В ТФ отсутствуют операции, принадлежащие отчетному периоду. Налоговая форма не создана")
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

    boolean processInfoPart(infoPart, rowNum) {

        def ndflPersonNode = infoPart.'ПолучДох'[0]

        NdflPerson ndflPerson = transformNdflPersonNode(ndflPersonNode)

        def familia = ndflPerson.lastName != null ? ndflPerson.lastName + " ": ""
        def imya = ndflPerson.firstName != null ? ndflPerson.firstName + " " : ""
        def otchestvo = ndflPerson.middleName != null ? ndflPerson.middleName : ""
        def fio = familia + imya + otchestvo
        def ndflPersonOperations = infoPart.'СведОпер'

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        def incomeCodeMap = getRefIncomeCode()

        // Коды видов вычетов
        def deductionTypeList = getRefDeductionType()

        ndflPersonOperations.each {
            processNdflPersonOperation(ndflPerson, it, fio, incomeCodeMap, deductionTypeList)
        }

        //Идентификатор декларации для которой загружаются данные
        if (ndflPerson.incomes != null && !ndflPerson.incomes.isEmpty()) {
            ndflPerson.declarationDataId = declarationData.getId()
            ndflPerson.rowNum = rowNum
            ndflPersonService.save(ndflPerson)
        } else {
            logger.warn("ФЛ ФИО = $fio ФЛ ИНП = ${ndflPerson.inp} Не загружен в систему поскольку не имеет операций в отчетном периоде")
            return false
        }
        return true
    }

    void processNdflPersonOperation(NdflPerson ndflPerson, NodeChild ndflPersonOperationsNode, String fio, def incomeCodeMap, def deductionTypeList) {

        List<NdflPersonIncome> incomes = new ArrayList<NdflPersonIncome>();
        // При создание объекто операций доходов выполняется проверка на соответствие дат отчетному периоду
        incomes.addAll(ndflPersonOperationsNode.'СведДохНал'.collect {
            transformNdflPersonIncome(it, ndflPerson, toString(ndflPersonOperationsNode.'@КПП'), toString(ndflPersonOperationsNode.'@ОКТМО'), ndflPerson.inp, fio, incomeCodeMap)
        });
        // Если проверка на даты не прошла, то операция не добавляется.
        // https://jira.aplana.com/browse/SBRFNDFL-1350 - если дата не прошла то ничего не загружаем и выводим сообщение
        if (incomes.contains(null)) {
            return
        }

        incomes.each {
            if (it != null){
                ndflPerson.incomes.add(it);
            }
        }

        List<NdflPersonDeduction> deductions = new ArrayList<NdflPersonDeduction>();
        deductions.addAll(ndflPersonOperationsNode.'СведВыч'.collect {
            transformNdflPersonDeduction(it, ndflPerson, fio, deductionTypeList)
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

    NdflPersonIncome transformNdflPersonIncome(NodeChild node, NdflPerson ndflPerson, String kpp, String oktmo, String inp, String fio, def incomeCodeMap) {
        def operationNode = node.parent();

        Date incomeAccruedDate = toDate(node.'@ДатаДохНач')
        Date incomePayoutDate = toDate(node.'@ДатаДохВыпл')
        Date taxDate = toDate(node.'@ДатаНалог')

        NdflPersonIncome personIncome = new NdflPersonIncome()
        personIncome.rowNum = toBigDecimal(node.'@НомСтр')
        personIncome.incomeCode = toString(node.'@КодДох')
        personIncome.incomeType = toString(node.'@ТипДох')

        personIncome.operationId = toString(operationNode.'@ИдОпер')
        personIncome.oktmo = toString(operationNode.'@ОКТМО')
        personIncome.kpp = toString(operationNode.'@КПП')

        if (operationNotRelateToCurrentPeriod(incomeAccruedDate, incomePayoutDate, taxDate,
                kpp, oktmo, inp, fio, personIncome)) {
            return null
        }

        personIncome.incomeAccruedDate = toDate(node.'@ДатаДохНач')
        personIncome.incomePayoutDate = toDate(node.'@ДатаДохВыпл')
        personIncome.incomeAccruedSumm = toBigDecimal(node.'@СуммДохНач')
        personIncome.incomePayoutSumm = toBigDecimal(node.'@СуммДохВыпл')
        personIncome.totalDeductionsSumm = toBigDecimal(node.'@СумВыч')
        personIncome.taxBase = toBigDecimal(node.'@НалБаза')
        personIncome.taxRate = toInteger(node.'@Ставка')
        personIncome.taxDate = toDate(node.'@ДатаНалог')
        personIncome.calculatedTax = toBigDecimal(node.'@НИ')
        personIncome.withholdingTax = toBigDecimal(node.'@НУ')
        personIncome.notHoldingTax = toBigDecimal(node.'@ДолгНП')
        personIncome.overholdingTax = toBigDecimal(node.'@ДолгНА')
        personIncome.refoundTax = toLong(node.'@ВозврНал')
        personIncome.taxTransferDate = toDate(node.'@СрокПрчслНал')
        personIncome.paymentDate = toDate(node.'@ПлПоручДат')
        personIncome.paymentNumber = toString(node.'@ПлатПоручНом')
        personIncome.taxSumm = toLong(node.'@НалПерСумм')

        // Спр5 Код вида дохода (Необязательное поле)
        if (personIncome.incomeCode != null && personIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
            value.CODE?.stringValue == personIncome.incomeCode &&
                    personIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                    personIncome.incomeAccruedDate <= value.record_version_to?.dateValue
        }) {
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_INCOME_CODE, personIncome.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, personIncome.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError,
                    errMsg)
        }

        return personIncome
    }

    // Проверка на принадлежность операций периоду при загрузке ТФ
    @TypeChecked
    boolean operationNotRelateToCurrentPeriod(Date incomeAccruedDate, Date incomePayoutDate, Date taxDate,
                                              String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
        // Доход.Дата.Начисление
        boolean incomeAccruedDateOk = dateRelateToCurrentPeriod(C_INCOME_ACCRUED_DATE, incomeAccruedDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        // Доход.Дата.Выплата
        boolean incomePayoutDateOk = dateRelateToCurrentPeriod(C_INCOME_PAYOUT_DATE, incomePayoutDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        // НДФЛ.Расчет.Дата
        boolean taxDateOk = dateRelateToCurrentPeriod(C_TAX_DATE, taxDate, kpp, oktmo, inp, fio, ndflPersonIncome)
        if (incomeAccruedDateOk && incomePayoutDateOk && taxDateOk) {
            return false
        }
        return true
    }

    @TypeChecked
    boolean dateRelateToCurrentPeriod(String paramName, Date date, String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
        //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
        if (date == null || (date >= getReportPeriodStartDate() && date <= getReportPeriodEndDate())) {
            return true
        }
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, (ndflPersonIncome.rowNum ?ndflPersonIncome.rowNum.longValue(): ""))
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)
        String errMsg = String.format("Значение гр. %s (\"%s\") не входит в отчетный период налоговой формы (%s), операция %s не загружена в налоговую форму. ФЛ %s, ИНП: %s",
                paramName, formatDate(date),
                departmentReportPeriod.reportPeriod.taxPeriod.year + ", " + departmentReportPeriod.reportPeriod.name,
                ndflPersonIncome.operationId,
                fio, inp
        )
        logger.warnExp("%s. %s.", "Проверка соответствия дат операций РНУ НДФЛ отчетному периоду", "", pathError,
                errMsg)
        return false
    }

    NdflPersonDeduction transformNdflPersonDeduction(NodeChild node, NdflPerson ndflPerson, String fio, def deductionTypeList) {

        NdflPersonDeduction personDeduction = new NdflPersonDeduction()
        personDeduction.rowNum = toBigDecimal(node.'@НомСтр')
        personDeduction.operationId = toString(node.parent().'@ИдОпер')
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

        if (!deductionTypeList.contains(personDeduction.typeCode)) {
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_TYPE_CODE, personDeduction.typeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, personDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError,
                    errMsg)
        }

        return personDeduction
    }

    NdflPersonPrepayment transformNdflPersonPrepayment(NodeChild node) {
        NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
        personPrepayment.rowNum = toBigDecimal(node.'@НомСтр')
        personPrepayment.operationId = toString(node.parent().'@ИдОпер')
        personPrepayment.summ = toBigDecimal(node.'@Аванс')
        personPrepayment.notifNum = toString(node.'@УведНом')
        personPrepayment.notifDate = toDate(node.'@УведДата')
        personPrepayment.notifSource = toString(node.'@УведИФНС')
        return personPrepayment;
    }

    Integer toInteger(xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Integer.valueOf(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    Long toLong(xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? Long.valueOf(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    BigDecimal toBigDecimal(xmlNode) throws NumberFormatException {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            try {
                return xmlNode.text() != null && !xmlNode.text().isEmpty() ? new BigDecimal(xmlNode.text()) : null;
            } catch (NumberFormatException ex) {
                throw new NumberFormatException("Значение атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не является числом. Проверьте отсутствие пробелов, переводов строки, печатных символов в значении атрибута.")
            }
        } else {
            return null;
        }
    }

    Date toDate(xmlNode) {
        if (xmlNode != null && !xmlNode.isEmpty()) {
            SimpleDateFormat format = new java.text.SimpleDateFormat(DATE_FORMAT)
            if (xmlNode.text() != null && !xmlNode.text().isEmpty()) {
                Date date = format.parse(xmlNode.text())
                if (format.format(date) != xmlNode.text()) {
                    throw new ServiceException("Значения атрибута \"${xmlNode.name()}\": \"${xmlNode.text()}\" не существует.")
                }
                return date
            } else {
                return null
            }
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
        return ScriptUtils.formatDate(date, DATE_FORMAT)
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

// Мапа <ID_Данные о физическом лице - получателе дохода, NdflPersonFL>
@Field def ndflPersonFLMap = [:]
@Field final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"
@Field final String SECTION_LINE_MSG = "Раздел %s. Строка %s"

@CompileStatic
class NdflPersonFL {
    String fio
    String inp
    NdflPersonFL(String fio, String inp) {
        this.fio = fio
        this.inp = inp
    }
}

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
     * Карта
     */
    @Field Map<Long, CheckAddressResult> fiasAddressCheckCache = [:];

    Map<Long, CheckAddressResult> getFiasAddressCheckResultMap() {
        if (fiasAddressCheckCache.isEmpty()) {
            fiasAddressCheckCache = fiasRefBookService.checkExistsAddressByFias(declarationData.id);
        }
        return fiasAddressCheckCache;
    }

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


    DepartmentReportPeriod getDepartmentReportPeriodById(int id) {
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
    Date getReportPeriodStartDate() {
        if (reportPeriodStartDate == null) {
            reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
        }
        return reportPeriodStartDate
    }

    /**
     * Получить календарную дату начала отчетного периода
     * @return
     */
    Date getReportPeriodCalendarStartDate() {
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
     * Выгрузка из справочников по условию
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
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
        // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        def mapResult = [:]
        def refBookMap = getRefBook(REF_BOOK_INCOME_CODE_ID)
        refBookMap.each { refBook ->
            mapResult.put(refBook?.id?.numberValue, refBook)
        }
        return mapResult;
    }

    /**
     * Получить "Виды доходов"
     * @return
     */
    def getRefIncomeType() {
        // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        Map<String, List<Long>> mapResult = [:]
        def refBookList = getRefBook(REF_BOOK_INCOME_KIND_ID)
        refBookList.each { refBook ->
            String mark = refBook?.MARK?.stringValue
            List<Long> incomeTypeIdList = mapResult.get(mark)
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
    Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataId() {
        if (dulActualCache.isEmpty()) {
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${declarationData.id} AND ref_book_id_doc.person_id = np.person_id)"
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(REF_BOOK_ID_DOC_ID, whereClause)

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
     * Получить записи справочника по его идентификатору в отчётном периоде
     * @param refBookId - идентификатор справочника
     * @return - список записей справочника
     */
    def getRefBook(def long refBookId) {
        // Передаем как аргумент только срок действия версии справочника
        def refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
        if (refBookList == null || refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
        return refBookList
    }

    /**
     * Получить все записи справочника по его идентификатору
     * @param refBookId - идентификатор справочника
     * @return - список всех версий всех записей справочника
     */
    def getRefBookAll(long refBookId) {
        def recordData = getProvider(refBookId).getRecordDataWhere("1 = 1")
        def refBookList = []
        if (recordData != null) {
            recordData.each { key, value ->
                refBookList.add(value)
            }
        }

        if (refBookList.size() == 0) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
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

    //>------------------< UTILS >----------------------<

    // Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
    @Field final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id
    @Field final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

    @Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
    @Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
    @Field final String MESSAGE_ERROR_ABSENT = " Отсутствуют строки:"


    @Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" (%d записей)."
    @Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" (%d записей)."

    // Таблицы
    @Field final String T_PERSON = "1" //"Реквизиты"
    @Field final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
    @Field final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
    @Field final String T_PERSON_PREPAYMENT = "4" //"Сведения о доходах в виде авансовых платежей"

    @Field final String T_PERSON_NAME = "Реквизиты"
    @Field final String T_PERSON_INCOME_NAME  = "Сведения о доходах и НДФЛ"
    @Field final String T_PERSON_DEDUCTION_NAME  =  "Сведения о вычетах"
    @Field final String T_PERSON_PREPAYMENT_NAME  = "Сведения о доходах в виде авансовых платежей"

    // Справочники
    @Field final String R_FIAS = "КЛАДР" //TODO замена
    @Field final String R_PERSON = "Физические лица"
    @Field final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
    @Field final String R_ID_DOC_TYPE = "Коды документов"
    @Field final String R_STATUS = "Статусы налогоплательщика"
    @Field final String R_INCOME_CODE = "Коды видов доходов"
    @Field final String R_INCOME_TYPE = "Виды дохода"
    @Field final String R_RATE = "Ставки"
    @Field final String R_TYPE_CODE = "Коды видов вычетов"
    @Field final String R_NOTIF_SOURCE = "Налоговые инспекции"
    @Field final String R_ADDRESS = "Адреса"
    @Field final String R_INP = "Идентификаторы налогоплательщиков"
    @Field final String R_DUL = "Документы, удостоверяющие личность"
    @Field final String R_DETAIL = "Настройки подразделений"

    // Реквизиты
    @Field final String C_ADDRESS = "Адрес регистрации в Российской Федерации "
    @Field final String C_CITIZENSHIP = "Гражданство (код страны)"
    @Field final String C_ID_DOC = "Документ удостоверяющий личность.Номер"
    @Field final String C_ID_DOC_TYPE = "Документ удостоверяющий личность.Код"
@Field final String C_STATUS = "Статус (код)"
    @Field final String C_RATE = "Ставка"
@Field final String C_TYPE_CODE = "Код вычета" //" Код вычета"
@Field final String C_NOTIF_SOURCE = "Подтверждающий документ. Код источника" //" Документ о праве на налоговый вычет.Код источника"
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
@Field final String C_INCOME_CODE = "Код дохода" //"Доход.Вид.Код"
@Field final String C_INCOME_TYPE = "Признак дохода" //"Доход.Вид.Признак"
@Field final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода" //"Доход.Дата.Начисление"
@Field final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода" //"Доход.Дата.Выплата"
@Field final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода" //"Доход.Сумма.Начисление"
@Field final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода" //"Доход.Сумма.Выплата"
@Field final String C_TOTAL_DEDUCTIONS_SUMM = "Сумма вычета" //"Сумма вычета"
@Field final String C_TAX_BASE = "Налоговая база" //"Налоговая база"
@Field final String C_TAX_RATE = "Процентная ставка (%)" //"НДФЛ.Процентная ставка"
@Field final String C_TAX_DATE = "Дата НДФЛ" //"НДФЛ.Расчет.Дата"
@Field final String C_CALCULATED_TAX = "НДФЛ исчисленный" //" НДФЛ.Расчет.Сумма.Исчисленный"
@Field final String C_WITHHOLDING_TAX = "НДФЛ удержанный" //"НДФЛ.Расчет.Сумма.Удержанный"
@Field final String C_NOT_HOLDING_TAX = "НДФЛ не удержанный" //"НДФЛ.Расчет.Сумма.Не удержанный"
@Field final String C_OVERHOLDING_TAX = "НДФЛ излишне удержанный" //"НДФЛ.Расчет.Сумма.Излишне удержанный"
@Field final String C_REFOUND_TAX = "НДФЛ возвращенный НП" //C_REFOUND_TAX
@Field final String C_TAX_TRANSFER_DATE = "Срок перечисления в бюджет" //"НДФЛ.Перечисление в бюджет.Срок"
@Field final String C_PAYMENT_DATE = "Дата платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
@Field final String C_PAYMENT_NUMBER = "Номер платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
@Field final String C_TAX_SUMM = "Сумма платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
@Field final String C_OKTMO = "ОКТМО" //"Доход.Источник выплаты.ОКТМО"
@Field final String C_KPP = "КПП" //"Доход.Источник выплаты.КПП"

// Сведения о вычетах
@Field final String C_NOTIF_DATE = "Подтверждающий документ. Дата" //" Документ о праве на налоговый вычет.Дата"
@Field final String C_NOTIF_SUMM = "Подтверждающий документ. Сумма" //" Документ о праве на налоговый вычет.Сумма"
@Field final String C_NOTIF_NUMBER = "Подтверждающий документ. Номер" //" Документ о праве на налоговый вычет.Номер"
@Field final String C_INCOME_ACCRUED = "Доход. Дата" //" Начисленный доход.Дата"
@Field final String C_INCOME_ACCRUED_P_SUMM = "Доход. Сумма" //" Начисленный доход.Сумма"
@Field final String C_INCOME_ACCRUED_CODE = "Доход. Код дохода" //" Начисленный доход.Код дохода"
@Field final String C_PERIOD_PREV_DATE = "Вычет. Предыдущий период. Дата" //" Применение вычета.Предыдущий период.Дата"
@Field final String C_PERIOD_CURR_DATE = "Вычет. Текущий период. Дата" //" Применение вычета.Текущий период.Дата"
@Field final String C_PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма" //" Применение вычета.Текущий период.Сумма"

// Сведения о доходах в виде авансовых платежей
@Field final String P_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление" //"Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление"

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


    @Field final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
    @Field final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""
    @Field final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""

    @Field final String LOG_TYPE_2_6 = "Дата начисления дохода указана некорректно"
    @Field final String LOG_TYPE_2_12 = "Сумма вычета указана некорректно"
    @Field final String LOG_TYPE_2_14 = "\"Налоговая ставка\" указана некорректно"
    @Field final String LOG_TYPE_2_14_MSG = "Значение гр. \"%s\" (\"%s\") указано некорректно. Для \"Кода дохода\" (\"%s\") и \"Статуса НП\" (\"%s\") предусмотрены ставки: %s"
    @Field final String LOG_TYPE_2_16 = "\"НДФЛ исчисленный\" рассчитан некорректно"
    @Field final String LOG_TYPE_2_17 = "\"НДФЛ удержанный\" рассчитан некорректно"
    @Field final String LOG_TYPE_2_18 = "\"НДФЛ не удержанный\" рассчитан некорректно"
    @Field final String LOG_TYPE_2_19 = "\"НДФЛ излишне удержанный\" рассчитан некорректно"
    @Field final String LOG_TYPE_2_20 = "\"НДФЛ возвращеный НП\" рассчитан некорректно"
    @Field final String LOG_TYPE_2_21 = "\"Срок перечисления в бюджет\" рассчитан некорректно"
    @Field final String LOG_TYPE_NOT_ZERO = "Значение не может быть \"0\""

    @Field final String LOG_TYPE_3_7 = "\"Код источника подтверждающего документа\" указан некорректно"
    @Field final String LOG_TYPE_3_10 = "\"Дата начисленного дохода\" указана некорректно"
    @Field final String LOG_TYPE_3_10_2 = "\"Дата применения вычета в текущем периоде\" не соответствует \"Дате начисления дохода\""
    @Field final String LOG_TYPE_3_11= "\"Код начисленного дохода\" указан некорректно"
    @Field final String LOG_TYPE_3_12 = "\"Сумма начисленного дохода\" указана некорректно"
    @Field final String LOG_TYPE_3_16 = "\"Сумма применения вычета\" указана некорректно"

    /**
     * Проверки НДФЛ (первичная и консолидированная)
     * @return
     */
    def checkData() {

        ScriptUtils.checkInterrupted();

        long time = System.currentTimeMillis();
        // Реквизиты
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_NAME, ndflPersonList.size())

        // Сведения о доходах и НДФЛ
        List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_INCOME_NAME, ndflPersonIncomeList.size())

        // Сведения о вычетах
        List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION_NAME, ndflPersonDeductionList.size())

        // Сведения о доходах в виде авансовых платежей
        List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT_NAME, ndflPersonPrepaymentList.size())

        logForDebug("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        // ФЛ Map<person_id, RefBook>
        Map<Long, Map<String, RefBookValue>> personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
        logForDebug(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Физические лица (" + (System.currentTimeMillis() - time) + " мс)");

        ScriptUtils.checkInterrupted();

        // Проверки на соответствие справочникам
        checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

        ScriptUtils.checkInterrupted();

        // Общие проверки
        checkDataCommon(ndflPersonList, ndflPersonIncomeList, personMap)

        ScriptUtils.checkInterrupted();

        // Проверки сведений о доходах
        checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

        ScriptUtils.checkInterrupted();

        // Проверки Сведения о вычетах
        checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, personMap)

        logForDebug("Все проверки (" + (System.currentTimeMillis() - time) + " мс)");
    }

    /**
     * Проверки на соответствие справочникам
     * @return
     */
    def checkDataReference(
            List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
            List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

        long time = System.currentTimeMillis();
        // Страны
        def citizenshipCodeMap = getRefCountryCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

        // Виды документов, удостоверяющих личность
        def documentTypeMap = getRefDocumentTypeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

        // Статус налогоплательщика
        def taxpayerStatusMap = getRefTaxpayerStatusCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

        // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
        def incomeCodeMap = getRefIncomeCode()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

        // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
        def incomeTypeMap = getRefIncomeType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

        // Коды видов вычетов
        def deductionTypeList = getRefDeductionType()
        logForDebug(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

        // Коды налоговых органов
        def taxInspectionList = getRefNotifSource()
        logForDebug(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        // ИНП Map<person_id, List<RefBook>>
        def inpMap = getActualRefInpMapByDeclarationDataId()
        logForDebug(SUCCESS_GET_TABLE, R_INP, inpMap.size())
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ИНП (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        // ДУЛ Map<person_id, List<RefBook>>
        def dulMap = getActualRefDulByDeclarationDataId()
        logForDebug(SUCCESS_GET_TABLE, R_DUL, dulMap.size())
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ДУЛ (" + (System.currentTimeMillis() - time) + " мс)");

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
            logForDebug(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
        }
        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Адреса (" + (System.currentTimeMillis() - time) + " мс)");

        //поиск всех адресов формы в справочнике ФИАС
        time = System.currentTimeMillis();

        //первый запрос, проверяет что адрес присутствует в фиас
        Map<Long, FiasCheckInfo> checkFiasExistAddressMap = getFiasAddressIdsMap();

        logForDebug("Проверки на соответствие справочникам / Выгрузка справочника $R_FIAS (" + (System.currentTimeMillis() - time) + " мс)");

        long timeIsExistsAddress = 0
        time = System.currentTimeMillis();
        //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
        for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = (ndflPerson.lastName?:"") + " " + (ndflPerson.firstName?:"") + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp?:"")
                } else {
                    // РНУ-НДФЛ консолидированная
                    def personRecord = personMap.get(ndflPerson.recordId)
                    String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр1 ФИАС
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-448

            long tIsExistsAddress = System.currentTimeMillis();
            if (!isPersonAddressEmpty(ndflPerson)) {

                List<String> address = []
                FiasCheckInfo fiasCheckInfo = checkFiasExistAddressMap.get(ndflPerson.id)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                if (!ScriptUtils.isEmpty(ndflPerson.regionCode) && !fiasCheckInfo.validRegion) {
                    logFiasError(fioAndInp, pathError, "Код субъекта", ndflPerson.regionCode)
                } else if (!ScriptUtils.isEmpty(ndflPerson.area) && !fiasCheckInfo.validArea ) {
                    logFiasError(fioAndInp, pathError, "Район", ndflPerson.area)
                } else if (!ScriptUtils.isEmpty(ndflPerson.city) && !fiasCheckInfo.validCity) {
                    logFiasError(fioAndInp, pathError, "Город", ndflPerson.city)
                } else if (!ScriptUtils.isEmpty(ndflPerson.locality) && !fiasCheckInfo.validLoc) {
                    logFiasError(fioAndInp, pathError, "Населенный пункт", ndflPerson.locality)
                } else if (!ScriptUtils.isEmpty(ndflPerson.street) && !fiasCheckInfo.validStreet) {
                    logFiasError(fioAndInp, pathError, "Улица", ndflPerson.street)
                }
                if (ndflPerson.postIndex != null && !ndflPerson.postIndex.matches("[0-9]{6}")){
                    logFiasIndexError(fioAndInp, pathError, "Индекс", ndflPerson.postIndex)
                }
            }
            timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

            // Спр2 Гражданство (Обязательное поле)
            if (ndflPerson.citizenship != null && !citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_CITIZENSHIP, ndflPerson.citizenship ?: "",
                        R_CITIZENSHIP
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_CITIZENSHIP), fioAndInp, pathError, errMsg)
            }

            // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
            if (ndflPerson.idDocType != null && !documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2, "ДУЛ Код", ndflPerson.idDocType ?: "", R_ID_DOC_TYPE)
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_ID_DOC_TYPE), fioAndInp, pathError, errMsg)
            }

            // Спр4 Статус (Обязательное поле)
            if (ndflPerson.status != "0" && !taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_STATUS, ndflPerson.status ?: "",
                        R_STATUS
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_STATUS), fioAndInp, pathError, errMsg)
            }

            // Спр10 Наличие связи с "Физическое лицо"
            if (ndflPerson.personId == null || ndflPerson.personId == 0) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                        "Не удалось установить связь со справочником \"$R_PERSON\"")
            } else {
                def personRecord = personMap.get(ndflPerson.recordId)

                if (!personRecord) {
                    //TODO turn_to_error
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                            "Не удалось установить связь со справочником \"$R_PERSON\"")
                } else {
                    // Спр11 Фамилия (Обязательное поле)
                    if (personRecord.get(RF_LAST_NAME).value != null && !ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Фамилия", ndflPerson.lastName ?: "", R_PERSON))
                    }

                    // Спр11 Имя (Обязательное поле)
                    if (personRecord.get(RF_FIRST_NAME).value != null && !ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Имя", ndflPerson.firstName ?: "", R_PERSON))
                    }

                    // Спр11 Отчество (Необязательное поле)
                    if (personRecord.get(RF_MIDDLE_NAME).value != null && ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Отчество", ndflPerson.middleName ?: "", R_PERSON))
                    }

                    if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                        // Спр12 ИНП первичная (Обязательное поле)
                        def inpList = inpMap.get(personRecord.get("id")?.value)
                        if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                        }
                    } else {
                        //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                        //if (formType == CONSOLIDATE){}
                        String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                        if (!ndflPerson.inp.equals(recordId)) {
                            //TODO turn_to_error
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                        }
                    }

                    // Спр13 Дата рождения (Обязательное поле)
                    if (personRecord.get(RF_BIRTH_DATE).value != null && !ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Дата рождения не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Дата рождения", ndflPerson.birthDay ?(ndflPerson.birthDay?.format("dd.MM.yyyy")): "", R_PERSON))
                    }

                    // Спр14 Гражданство (Обязательное поле)
                    def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                    if (ndflPerson.citizenship != null && !ndflPerson.citizenship.equals(citizenship)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код гражданства не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_CITIZENSHIP, ndflPerson.citizenship ?: "", R_PERSON))
                    }

                    // Спр15 ИНН.В Российской федерации (Необязательное поле)
                    if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в РФ не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в РФ", ndflPerson.innNp ?: "", R_PERSON))
                    }

                    // Спр16 ИНН.В стране гражданства (Необязательное поле)
                    if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНН в ИНО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНН в ИНО", ndflPerson.innForeign ?: "", R_PERSON))
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
                        if (ndflPerson.idDocType != null && !personDocTypeList.contains(ndflPerson.idDocType)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код", ndflPerson.idDocType ?: "", R_PERSON))
                        }
                        if (ndflPerson.idDocNumber != null && !personDocNumberList.contains(ndflPerson.idDocNumber)) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
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
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код\" (\"${ndflPerson.idDocType ?: ""}\"), \"ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
                        } else {
                            int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                            if (incRep != 1) {
                                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                                logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                        "\"ДУЛ Номер\" не включается в отчетность")
                            }
                        }
                    }

                    // Спр18 Статус налогоплательщика (Обязательное поле)
                    def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                    if (ndflPerson.status!= null && !ndflPerson.status.equals(taxpayerStatus)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Статус налогоплательщица не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, C_STATUS, ndflPerson.status ?: "", R_PERSON))
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

                    List<String> ndflPersonAddress = []

                    // Адрес регистрации в Российской Федерации.Код субъекта
                    if (ndflPerson.regionCode != null && !ndflPerson.regionCode.equals(regionCode)) {
                        ndflPersonAddress.add("Код субъекта='${ndflPerson.regionCode ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Район
                    if (ndflPerson.area != null && !ndflPerson.area.equals(area)) {
                        ndflPersonAddress.add("Район='${ndflPerson.area ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Город
                    if (ndflPerson.city != null && !ndflPerson.city.equals(city)) {
                        ndflPersonAddress.add("Город='${ndflPerson.city ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Населенный пункт
                    if (ndflPerson.locality != null && !ndflPerson.locality.equals(locality)) {
                        ndflPersonAddress.add("Населенный пункт='${ndflPerson.locality ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Улица
                    if (ndflPerson.street != null && !ndflPerson.street.equals(street)) {
                        ndflPersonAddress.add("Улица='${ndflPerson.street ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Дом
                    if (ndflPerson.house != null && !ndflPerson.house.equals(house)) {
                        ndflPersonAddress.add("Дом='${ndflPerson.house ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Корпус
                    if (ndflPerson.building != null && !ndflPerson.building.equals(building)) {
                        ndflPersonAddress.add("Корпус='${ndflPerson.building ?: ""}'")
                    }

                    // Адрес регистрации в Российской Федерации.Квартира
                    if (ndflPerson.flat != null && !ndflPerson.flat.equals(flat)) {
                        ndflPersonAddress.add("Квартира='${ndflPerson.flat ?: ""}'")
                    }
                    if (!ndflPersonAddress.isEmpty()) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Адрес не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "Форма.Реквизиты.Адрес регистрации в Российской Федерации", ndflPersonAddress.join(", "), R_PERSON))

                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        logForDebug("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр5 Код вида дохода (Необязательное поле)
            if (ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
                value.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                        ndflPersonIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                        ndflPersonIncome.incomeAccruedDate <= value.record_version_to?.dateValue
            }) {
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                        R_INCOME_CODE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
            }

            /*
                Спр6
                При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода

                Доход.Вид.Признак (Графа 5) - (Необязательное поле)
                incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>

                Доход.Вид.Код (Графа 4) - (Необязательное поле)
                incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
                 */
            if (ndflPersonIncome.incomeType != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)) {
                List<Long> incomeTypeIdList = incomeTypeMap.get(ndflPersonIncome.incomeType)
                if (incomeTypeIdList == null || incomeTypeIdList.isEmpty()) {
                    String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                            C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                            R_INCOME_TYPE
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError, errMsg)
                } else {
                    if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeAccruedDate != null) {
                        def incomeCodeRefList = []
                        incomeTypeIdList.each { incomeTypeId ->
                            def incomeCodeRef = incomeCodeMap.get(incomeTypeId)
                            incomeCodeRefList.add(incomeCodeRef)
                        }
                        def incomeCodeRef = incomeCodeRefList.find {
                            it?.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                                    ndflPersonIncome.incomeAccruedDate >= it.record_version_from?.dateValue &&
                                    ndflPersonIncome.incomeAccruedDate <= it.record_version_to?.dateValue
                        }
                        if (!incomeCodeRef) {
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\"), \"%s\" (\"%s\") отсутствует в справочнике \"%s\"",
                                    C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                    C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                    R_INCOME_TYPE
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError,
                                    errMsg)
                        }
                    }
                }
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_INCOME_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр8 Код вычета (Обязательное поле)
            if (ndflPersonDeduction.typeCode != "000" && ndflPersonDeduction.typeCode != null && !deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
                String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                        C_TYPE_CODE, ndflPersonDeduction.typeCode ?: "",
                        R_TYPE_CODE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError, errMsg)
            }

            // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
            if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                        C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
            if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
                //TODO turn_to_error
                String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                        P_NOTIF_SOURCE, ndflPersonPrepayment.notifSource ?: "",
                        R_NOTIF_SOURCE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");
    }

void logFiasError (fioAndInp, pathError, name, value) {
    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "КЛАДР"), fioAndInp, pathError,
            "Значение гр. \"" + name + "\" (\""+ (value?:"") + "\") отсутствует в справочнике \"КЛАДР\"")
}

void logFiasIndexError (fioAndInp, pathError, name, value) {
    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "КЛАДР"), fioAndInp, pathError,
                    "Значение гр. \"" + name + "\" (\""+ (value?:"") + "\") не соответствует требуемому формату")
}

/**
 * Общие проверки
 */
def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, Map<Long, Map<String, RefBookValue>> personMap) {
    long time = System.currentTimeMillis();
    long timeTotal = time

    logForDebug("Общие проверки: инициализация (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();

    for (NdflPerson ndflPerson : ndflPersonList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
            if (ndflPersonFL == null) {
                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // РНУ-НДФЛ первичная
                    String fio = (ndflPerson.lastName?:"") + " " + (ndflPerson.firstName?:"") + " " + (ndflPerson.middleName ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp?:"")
                } else {
                    // РНУ-НДФЛ консолидированная
                    def personRecord = personMap.get(ndflPerson.recordId)
                    String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                    ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
                }
                ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
            }
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Общ1 Корректность ИНН
            if (ndflPerson.citizenship == "643") {
                if (ndflPerson.innNp == null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ИНН не указан", fioAndInp, pathError,
                            "Значение гр. \"ИНН в РФ\" не указано. Прием налоговым органом обеспечивается, может быть предупреждение")
                } else {
                    String checkInn = ScriptUtils.checkInn(ndflPerson.innNp)
                    if (checkInn != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.errorExp("%s. %s.", "ИНН не соответствует формату", fioAndInp, pathError,
                                checkInn)
                    }
                }
            }

            //Общ2 Наличие обязательных реквизитов для формирования отчетности
            boolean checkLastName = checkRequiredAttribute(ndflPerson, fioAndInp, "lastName", "Фамилия")
            boolean checkFirstName = checkRequiredAttribute(ndflPerson, fioAndInp, "firstName", "Имя")
            checkRequiredAttribute(ndflPerson, fioAndInp, "birthDay", "Дата рождения")
            boolean checkCitizenship = checkRequiredAttribute(ndflPerson, fioAndInp, "citizenship", C_CITIZENSHIP)
            boolean checkIdDocType = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocType", "ДУЛ Код")
            boolean checkIdDocNumber = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocNumber", "ДУЛ Номер")
            checkRequiredAttribute(ndflPerson, fioAndInp, "status", C_STATUS)
            if (checkCitizenship) {
                if (ndflPerson.citizenship == "643") {
                    checkRequiredAttribute(ndflPerson, fioAndInp, "regionCode", "Код субъекта")
                } else {
                    checkRequiredAttribute(ndflPerson, fioAndInp, "countryCode", "Код страны проживания вне РФ")
                    checkRequiredAttribute(ndflPerson, fioAndInp, "address", "Адрес проживания вне РФ ")
                }
            }

            if (ndflPerson.citizenship == "643") {
                if (checkLastName) {
                    String checkName = ScriptUtils.checkName(ndflPerson.lastName, "Фамилия")
                    if (checkName != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Фамилия, Имя не соответствует формату", fioAndInp, pathError,
                                checkName)
                    }
                }
                if (checkFirstName) {
                    String checkName = ScriptUtils.checkName(ndflPerson.firstName, "Имя")
                    if (checkName != null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Фамилия, Имя не соответствует формату", fioAndInp, pathError,
                                checkName)
                    }
                }
            }
            if (checkIdDocType && checkIdDocNumber) {
                String checkDul = ScriptUtils.checkDul(ndflPerson.idDocType, ndflPerson.idDocNumber, "ДУЛ Номер")
                if (checkDul != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ДУЛ не соответствует формату", fioAndInp, pathError,
                            checkDul)
                }
            }

            // Общ11 СНИЛС (Необязательное поле)
            if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату",
                        "СНИЛС", ndflPerson.snils?:""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", "СНИЛС не соответствует формату", fioAndInp, pathError,
                        errMsg)
            }
        }
        logForDebug("Общие проверки / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

        time = System.currentTimeMillis();
        Department department = departmentService.get(declarationData.departmentId)
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

            ScriptUtils.checkInterrupted();

            boolean applyTemporalySolution = false
            if (ndflPersonIncome.incomeAccruedSumm == ndflPersonIncome.totalDeductionsSumm) {
                applyTemporalySolution = true
            }

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

            // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
            List<ColumnFillConditionData> columnFillConditionDataList = []
            //1 Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column4Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_INCOME_CODE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column5Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_INCOME_CODE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column10Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_INCOME_ACCRUED_SUMM
                    )
            )
            //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column11Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM
                    )
            )
            //3 Раздел 2. Графа 8 Должна быть всегда заполнена
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column8Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Не заполнена гр. \"%s\"",
                            C_OKTMO
                    )
            )
            //3 Раздел 2. Графа 9 Должна быть всегда заполнена
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column8Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Не заполнена гр. \"%s\"",
                            C_KPP
                    )
            )
            //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column6Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_INCOME_ACCRUED_DATE
                    )
            )
            //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_INCOME_PAYOUT_DATE
                    )
            )
            //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24Fill(),
                    new Column12NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?:"",
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 13 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column13Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_BASE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 14 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column14Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_RATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //7 Раздел 2. Графы 15 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24NotFill(),
                    new Column15Fill(applyTemporalySolution),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_DATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )
            )
            //8 Раздел 2. Графы 6 должны быть заполнены, если заполнена Раздел 2. Графа 16
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column16Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_CALCULATED_TAX
                    )
            )
            //8 Раздел 2. Графы 10 должны быть заполнены, если заполнена Раздел 2. Графа 16
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column16Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_CALCULATED_TAX
                    )
            )
            //9 Раздел 2. Графа 6 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column18Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_NOT_HOLDING_TAX
                    )
            )
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column19Fill(),
                    new Column6Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_DATE,
                            C_OVERHOLDING_TAX
                    )
            )
            //9 Раздел 2. Графа 10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column18Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_NOT_HOLDING_TAX
                    )
            )
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column19Fill(),
                    new Column10Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_ACCRUED_SUMM,
                            C_OVERHOLDING_TAX
                    )
            )
            //10 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 17
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column17Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_WITHHOLDING_TAX
                    )

            )
            //10 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 17
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column17Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_WITHHOLDING_TAX
                    )
            )
            //11 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 20
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column20Fill(),
                    new Column7Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_DATE,
                            C_REFOUND_TAX
                    )

            )
            //11 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 20
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column20Fill(),
                    new Column11Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                            C_INCOME_PAYOUT_SUMM,
                            C_REFOUND_TAX
                    )
            )
            //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7, 11
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7And11Fill(),
                    new Column21Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\"",
                            C_TAX_TRANSFER_DATE,
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM
                    )

            )
            //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 23, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column22And23And24Fill(),
                    new Column21Fill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                            C_TAX_TRANSFER_DATE,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )

            )
            //12 Раздел 2. Графа 21 должна быть НЕ заполнена, если НЕ заполнены Раздел 2. Графы 7, 11 и 22, 23, 24
            columnFillConditionDataList << new ColumnFillConditionData(
                    new Column7And11And22And23And24NotFill(),
                    new Column21NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как заполнены гр. \"%s\", гр. \"%s\", и не заполнены гр. \"%s\", гр. \"%s\", гр. \"%s\"",
                            C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                            C_INCOME_PAYOUT_DATE,
                            C_INCOME_PAYOUT_SUMM,
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )

            )
            //13 Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
            columnFillConditionDataList << new ColumnFillConditionData(
                    new ColumnTrueFillOrNotFill(),
                    new Column22And23And24FillOrColumn22And23And24NotFill(),
                    String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                    String.format("Гр. \"%s\", гр. \"%s\", гр. \"%s\" должны быть заполнены одновременно или не заполнена ни одна из них",
                            C_PAYMENT_DATE,
                            C_PAYMENT_NUMBER,
                            C_TAX_SUMM
                    )

            )
            columnFillConditionDataList.each { columnFillConditionData ->
                if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                        !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                    logger.errorExp("%s. %s.", "Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ",
                            fioAndInp, columnFillConditionData.conditionPath, columnFillConditionData.conditionMessage)
                }
            }
        }

        ScriptUtils.checkInterrupted();

        logForDebug("Общие проверки / '$T_PERSON_INCOME_NAME' (" + (System.currentTimeMillis() - time) + " мс)");

        logForDebug("Общие проверки всего (" + (System.currentTimeMillis() - timeTotal) + " мс)");
    }

boolean checkRequiredAttribute(def ndflPerson, String fioAndInp, String alias, String attributeName) {
    if (ndflPerson[alias] == null || (ndflPerson[alias]) instanceof String && (org.apache.commons.lang3.StringUtils.isBlank(ndflPerson[alias]) || ndflPerson[alias] == "0")) {
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
        String msg
        if (ndflPerson[alias] == "0") {
            msg = "Значение гр. \"$attributeName\" не может быть равно \"0\""
        } else {
            msg = "Значение гр. \"$attributeName\" не указано"
        }
        logger.warnExp("%s. %s.", "Не указан обязательный реквизит ФЛ", fioAndInp, pathError, msg)
        return false
    }
    return true
}

/**
 * Класс для проверки заполненности полей
 */
@CompileStatic
class ColumnFillConditionData {
    ColumnFillConditionChecker columnConditionCheckerAsIs
    ColumnFillConditionChecker columnConditionCheckerToBe
    String conditionPath
    String conditionMessage

        ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionPath, String conditionMessage) {
            this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
            this.columnConditionCheckerToBe = columnConditionCheckerToBe
            this.conditionPath = conditionPath
            this.conditionMessage = conditionMessage
        }
    }
    interface ColumnFillConditionChecker {
        boolean check(NdflPersonIncome ndflPersonIncome)
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    @TypeChecked
    class Column4Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.incomeCode)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 4,5 заполнены"
     */
    @TypeChecked
    class Column5Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 6 заполнена"
     */
    @TypeChecked
    class Column6Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 7 заполнена"
     */
    @TypeChecked
    class Column7Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 8 заполнена"
     */
    @TypeChecked
    class Column8Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.oktmo != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 9 заполнена"
     */
    @TypeChecked
    class Column9Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.kpp != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 10 заполнена"
     */
    @TypeChecked
    class Column10Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomeAccruedSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 11 заполнена"
     */
    @TypeChecked
    class Column11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 заполнены"
     */
    @TypeChecked
    class Column7And11Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 12 НЕ заполнена"
     */
    @TypeChecked
    class Column12NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 13 заполнены"
     */
    @TypeChecked
    class Column13Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column13Fill() {
            temporalySolution = false
        }

        Column13Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxBase != null
            }
            return !ScriptUtils.isEmpty(ndflPersonIncome.taxBase)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы  14 заполнены"
     */
    @TypeChecked
    class Column14Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column14Fill() {
            temporalySolution = false
        }

        Column14Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxRate != null
            }
            return ndflPersonIncome.taxRate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 15 заполнены"
     */
    @TypeChecked
    class Column15Fill implements ColumnFillConditionChecker {
        boolean temporalySolution

        Column15Fill() {
            temporalySolution = false
        }

        Column15Fill(boolean temporalySolution) {
            this.temporalySolution = temporalySolution
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            if (temporalySolution) {
                return ndflPersonIncome.taxDate != null
            }
            return ndflPersonIncome.taxDate != null
        }
    }

    /**
     * Проверка: "Раздел 2. Графы 16 заполнена"
     */
    @TypeChecked
    class Column16Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 17 заполнена"
     */
    @TypeChecked
    class Column17Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 18 заполнена"
     */
    @TypeChecked
    class Column18Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.notHoldingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графа 19 заполнена"
     */
    @TypeChecked
    class Column19Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.overholdingTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 20 заполнена"
     */
    @TypeChecked
    class Column20Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !ScriptUtils.isEmpty(ndflPersonIncome.refoundTax)
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 заполнена"
     */
    @TypeChecked
    class Column21Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate != null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 21 НЕ заполнена"
     */
    @TypeChecked
    class Column21NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.taxTransferDate == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 ИЛИ 22, 23, 24 заполнены"
     */
    @TypeChecked
    class Column7And11Or22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column7And11Fill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 7, 11 И 22, 23, 24 НЕ заполнены"
     */
    @TypeChecked
    class Column7And11And22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return !(new Column7And11Fill().check(ndflPersonIncome)) && (new Column22And23And24NotFill().check(ndflPersonIncome))
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 НЕ заполнены"
     */
    @TypeChecked
    class Column22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm == null
        }
    }
    /**
     * Проверка: "Раздел 2. Графы 22, 23, 24 заполнены"
     */
    @TypeChecked
    class Column22And23And24Fill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm != null
        }
    }
    /**
     * 	Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна их них
     */
    @TypeChecked
    class Column22And23And24FillOrColumn22And23And24NotFill implements ColumnFillConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome) {
            return (new Column22And23And24NotFill().check(ndflPersonIncome)) || (new Column22And23And24Fill().check(ndflPersonIncome))
        }
    }
    /**
     * 	Всегда возвращает true
     */
    @TypeChecked
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
                        List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

    long time = System.currentTimeMillis()

    def personsCache = [:]
    ndflPersonList.each { ndflPerson ->
        personsCache.put(ndflPerson.id, ndflPerson)

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                // РНУ-НДФЛ первичная
                String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                // РНУ-НДФЛ консолидированная
                def personRecord = personMap.get(ndflPerson.recordId)
                String fio = personRecord.get(RF_LAST_NAME).value + " " + personRecord.get(RF_FIRST_NAME).value + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

    def ndflPersonPrepaymentCache = [:]
    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: []
        ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
        ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
    }

    List<DateConditionData> dateConditionDataList = []
    List<DateConditionData> dateConditionDataListForBudget = []

    DateConditionWorkDay dateConditionWorkDay = new DateConditionWorkDay(calendarService)

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010", "2012",
                                                    "2300", "2710", "2760", "2762", "2770", "2900", "4800"],
            ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                    "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                    "1548", "1549", "1551", "1552", "1554"],
            ["01", "02"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Соответствует маске 31.12.20**
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                    "1541", "1542", "1543"],
            ["04"], new MatchMask("31.12.20\\d{2}"), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000"], ["05"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["07"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["11"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, в котором утверждён авансовый отчёт о командировке")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthWorkDayIncomeAccruedDate(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню каждого месяца, в течение срока, на который были предоставлены кредитные (заёмные) средства")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2640", "2641"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2800"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010",
                                                             "2710", "2760", "2762", "2770", "2900", "4800"], ["00"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 3,4 "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
    dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                             "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                             "1548", "1549", "1551", "1552", "1553", "1554"], ["01", "02", "03", "04"],
            new Column21EqualsColumn7Plus30WorkingDays(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 30 календарных дней")

    // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2000"], ["05", "06", "07", "08", "09", "10", "11", "12"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 7 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2002"], ["07", "08", "09", "10"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 8 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2003"], ["13"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 9 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
    dateConditionDataListForBudget << new DateConditionData(["2012", "2300"], ["00"],
            new Column21EqualsColumn7LastDayOfMonth(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца выплаты дохода")

    // 10 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2520", "2740", "2750", "2790", "4800"], ["13"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 12,13,14 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2610", "2640", "2641", "2800"], ["00"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

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

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdAndOperationIdList = ndflPersonIncomeCurrentByPersonIdList.findAll {
                it.operationId == ndflPersonIncome.operationId
            } ?: []

            // СведДох1 Доход.Дата.Начисление (Графа 6)
            if (dateConditionDataList != null && !(ndflPersonIncome.incomeAccruedSumm == null || ndflPersonIncome.incomeAccruedSumm == 0)) {
                dateConditionDataList.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(dateConditionData.conditionMessage,
                                    C_INCOME_ACCRUED_DATE, ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT) : ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_6, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            // СведДох2 Сумма вычета (Графа 12)
            if (ndflPersonIncome.totalDeductionsSumm != null && ndflPersonIncome.totalDeductionsSumm != 0) {
                BigDecimal sumNdflDeduction = getDeductionSumForIncome(ndflPersonIncome, ndflPersonDeductionList)
                if (!comparNumbEquals(ndflPersonIncome.totalDeductionsSumm ?: 0, sumNdflDeduction)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме гр. \"%s\" (\"%s\") раздела 3",
                            C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?: 0,
                            C_PERIOD_CURR_SUMM, sumNdflDeduction ?: 0)
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInp, pathError, errMsg)
                }
                if (comparNumbGreater(sumNdflDeduction, ndflPersonIncome.incomeAccruedSumm ?: 0)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть не меньше значение гр. \"%s\" (\"%s\")",
                            C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: 0,
                            C_PERIOD_CURR_SUMM, sumNdflDeduction
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох4 НДФЛ.Процентная ставка (Графа 14)
            if ((ndflPersonIncome.taxRate ?: 0) > 0) {
                boolean checkNdflPersonIncomingTaxRateTotal = false;

                boolean presentCitizenship = ndflPerson.citizenship != null && ndflPerson.citizenship != "0"
                boolean presentIncomeCode = ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeCode != "0"
                boolean presentStatus = ndflPerson.status != null && ndflPerson.status != "0"
                boolean presentTaxRate = ndflPersonIncome.taxRate != null && ndflPersonIncome.taxRate != 0
                def ndflPersonIncomingTaxRates = []
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_13:
                {
                    if (presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) {
                        Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                        Boolean conditionB = ndflPerson.citizenship == "643" && ["1010", "1011"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status == "1"
                        Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010", "2002", "2003"].contains(ndflPersonIncome.incomeCode) && Integer.parseInt(ndflPerson.status ?: 0) >= 3
                        if (conditionA || conditionB || conditionC) {
                            if (ndflPersonIncome.taxRate == 13) {
                                checkNdflPersonIncomingTaxRateTotal = true
                            } else {
                                ndflPersonIncomingTaxRates << "\"13\""
                            }
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_15:
                {
                    if ((presentIncomeCode && presentStatus && presentTaxRate) && (ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                        if (ndflPersonIncome.taxRate == 15) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"15\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_35:
                {
                    if ((presentIncomeCode && presentStatus && presentTaxRate) && (["2740", "3020", "2610"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                        if (ndflPersonIncome.taxRate == 35) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"35\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_30:
                {
                    if (presentIncomeCode && presentStatus && presentTaxRate) {
                        def conditionA = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && ndflPersonIncome.incomeCode != "1010"
                        def conditionB = Integer.parseInt(ndflPerson.status ?: 0) > 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                        if (conditionA || conditionB) {
                            if (ndflPersonIncome.taxRate == 30) {
                                checkNdflPersonIncomingTaxRateTotal = true
                            } else {
                                ndflPersonIncomingTaxRates << "\"30\""
                            }
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_9:
                {
                    if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                        if (ndflPersonIncome.taxRate == 9) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"9\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_OTHER:
                {
                    if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                        if (![13, 15, 35, 30, 9].contains(ndflPersonIncome.taxRate)) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"Специальная ставка\""
                        }
                    }
                }
                if (!checkNdflPersonIncomingTaxRateTotal && !ndflPersonIncomingTaxRates.isEmpty()) {
                    String errMsg = String.format(LOG_TYPE_2_14_MSG, "Процентная ставка (%)", ndflPersonIncome.taxRate ?: "",
                            ndflPersonIncome.incomeCode, ndflPerson.status,
                            ndflPersonIncomingTaxRates.join(", ")
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_14, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох5 НДФЛ.Расчет.Дата (Графа 15)
            if (ndflPersonIncome.taxDate != null) {

                boolean checkTaxDate = true
                List<Pair<String, String>> logTypeMessagePairList = []
                boolean calculatedTaxPresented = isPresentedByTempSolution(ndflPersonIncome.calculatedTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                boolean withholdingTaxPresented = isPresentedByTempSolution(ndflPersonIncome.withholdingTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                // СведДох5.1
                if (calculatedTaxPresented && (ndflPersonIncome.calculatedTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"${"Дата исчисленного налога"}\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.2
                if (withholdingTaxPresented && (ndflPersonIncome.withholdingTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"${"Дата удержанного налога"}\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.3
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null &&
                        !["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode)) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.4
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode) &&
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate()) {
                    // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.5
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542"].contains(ndflPersonIncome.incomeCode) &&
                        (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
                    // «Графа 15 Раздел 2"» = "31.12.20**"
                    if (ndflPersonIncome.taxDate != null) {
                        Calendar calendarPayout = Calendar.getInstance()
                        calendarPayout.setTime(ndflPersonIncome.taxDate)
                        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                        int month = calendarPayout.get(Calendar.MONTH)
                        if (!(dayOfMonth == 31 && month == 12)) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно \"31.12.20**\""))
                        }
                    }
                }
                // СведДох5.6
                if (ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.overholdingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата излишне удержанного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.7
                if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.refoundTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                        (ndflPersonIncome.overholdingTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата расчета возвращенного налогоплательщику налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT): ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""}\")"))
                    }
                }
                if (!checkTaxDate) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    for (Pair<String, String> logTypeMessagePair : logTypeMessagePairList) {
                        logger.warnExp("%s. %s.", logTypeMessagePair.getFirst(), fioAndInp, pathError, logTypeMessagePair.getSecond())
                    }
                }
            }

            // СведДох6 НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
            if (ndflPersonIncome.calculatedTax != null) {
                // СведДох6.1
                if (ndflPersonIncome.taxRate != 13) {
                    if ((ndflPersonIncome.calculatedTax ?: 0) != ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * (ndflPersonIncome.taxRate ?: 0))/100, 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно произведению значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") с округлением до целого числа",
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                "Процентная ставка", (ndflPersonIncome.taxRate ?: 0)
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
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
                        it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                        it.incomeAccruedSumm != null && it.incomeAccruedSumm != 0 &&
                                ndflPersonIncome.incomeAccruedDate >= getReportPeriodStartDate() && ndflPersonIncome.incomeAccruedDate <= getReportPeriodEndDate() &&
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
                    List<NdflPersonIncome> S2List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate &&
                        ndflPersonIncome.incomeAccruedDate >= getReportPeriodStartDate() && ndflPersonIncome.incomeAccruedDate <= getReportPeriodEndDate() &&
                                it.taxRate == 13 && it.incomeCode != "1010"
                    } ?: []
                    BigDecimal S2 = S2List.sum { it.calculatedTax ?: 0 } ?: 0
                    // Сумма по «Графа 16» текущей операции = S1 x 13% - S2
                    if (ndflPersonIncome.calculatedTax != ScriptUtils.round((S1 * 0.13 - S2), 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: Сумма значений гр. \"%s\" с начала периода на отчетную дату х 13%% - сумма значений гр. \"%s\" за предыдущие отчетные периоды",
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_BASE,
                                C_CALCULATED_TAX
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
                    }
                }
                // СведДох6.3
                if (ndflPersonIncome.taxRate == 13 && ndflPerson.status == "6") {
                    List<NdflPersonPrepayment> ndflPersonPrepaymentListByBersonIdList = ndflPersonPrepaymentCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    if (!ndflPersonPrepaymentListByBersonIdList.isEmpty()) {
                        List<NdflPersonPrepayment> ndflPersonPrepaymentCurrentList = ndflPersonPrepaymentListByBersonIdList.findAll { it.operationId == ndflPersonIncome.operationId } ?: []
                        Long ndflPersonPrepaymentSum = ndflPersonPrepaymentCurrentList.sum { it.summ } ?: 0
                        if (!(ndflPersonIncome.calculatedTax ==
                                ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * 0.13 - ndflPersonPrepaymentSum ?: 0), 0))
                        ) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: гр. \"%s\" (\"%s\") х 13%% - \"%s\" (\"%s\")",
                                    C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                    C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                    "Сумма фиксированного авансового платежа", ndflPersonPrepaymentSum
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            // СведДох7 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
            if (ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                // СведДох7.1
                if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                        && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                ) {
                    // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                            && ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значениям гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                        && ndflPersonIncome.overholdingTax > 0
                ) {
                    // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» <= ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    NdflPersonIncome ndflPersonIncomePreview = null
                    if (!ndflPersonIncomeCurrentList.isEmpty()) {
                        ndflPersonIncomePreview = ndflPersonIncomeCurrentList.find {
                            it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                    (ndflPersonIncomePreview == null || ndflPersonIncomePreview.incomeAccruedDate < it.incomeAccruedDate)
                        }
                    }
                    if (!(ndflPersonIncome.withholdingTax == (ndflPersonIncome.calculatedTax ?: 0) + (ndflPersonIncomePreview.calculatedTax ?: 0))) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") предыдущей записи",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncomePreview.calculatedTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                    if (!(ndflPersonIncome.withholdingTax <= (ScriptUtils.round(ndflPersonIncome.taxBase ?: 0, 0) - ndflPersonIncome.calculatedTax ?: 0) * 0.50)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не должно превышать 50%% от разности значение гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                             "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                ) {
                    if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"0\"",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if (!(ndflPersonIncome.incomeCode != null)) {
                    if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                }
            }

//              // "Сумма Граф 16"
            Long calculatedTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.calculatedTax ?: 0 } ?: 0
            // "Сумма Граф 17"
            Long withholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.withholdingTax ?: 0 } ?: 0
            // "Сумма Граф 18"
            Long notHoldingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.notHoldingTax ?: 0 } ?: 0
            // "Сумма Граф 19"
            Long overholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.overholdingTax ?: 0 } ?: 0
            // "Сумма Граф 20"
            Long refoundTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.refoundTax ?: 0 } ?: 0
            // "Сумма Граф 24"
            // Отменил изменения https://jira.aplana.com/browse/SBRFNDFL-1307, поскольку они привели к https://jira.aplana.com/browse/SBRFNDFL-1483
            //Long taxSumm = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {it.taxSumm?: 0} ?: 0

            // СведДох8 НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
            if (ndflPersonIncome.notHoldingTax != null && calculatedTaxSum > withholdingTaxSum) {
                if (!(notHoldingTaxSum == calculatedTaxSum - withholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_NOT_HOLDING_TAX, notHoldingTaxSum ?: "",
                            C_CALCULATED_TAX, calculatedTaxSum ?: "",
                            C_WITHHOLDING_TAX, withholdingTaxSum ?: ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_18, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох9 НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
            if (ndflPersonIncome.overholdingTax != null && calculatedTaxSum < withholdingTaxSum) {
                if (!(overholdingTaxSum == withholdingTaxSum - calculatedTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_OVERHOLDING_TAX, overholdingTaxSum ?: "",
                            C_WITHHOLDING_TAX, withholdingTaxSum ?: "",
                            C_CALCULATED_TAX, calculatedTaxSum ?: ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_19, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох10 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
            if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.refoundTax > 0) {
                if (!(refoundTaxSum <= overholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") не должна превышать сумму значений гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_REFOUND_TAX, refoundTaxSum ?: "",
                            C_OVERHOLDING_TAX, overholdingTaxSum ?: ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_20, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох11 НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
            // Заменил проверку заполненности 2.24, на проверку заполненности 2.21
            if (ndflPersonIncome.taxTransferDate != null) {
                dateConditionDataListForBudget.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        // Все подпункты, кроме 11-го
                        if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(dateConditionData.conditionMessage,
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                                    C_INCOME_PAYOUT_DATE, ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    }
                }
                if (["2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14") {
                    // 11 подпункт "Графа 21" = "Графа 7" + "1 рабочий день"
                    /*
                        Найти следующую за текущей строкой, удовлетворяющую условиям:
                        "Графа 10" > "0"
                        "Графа 5" не равно "02"
                        "Графа 5"не равно "14"
                        "Графа 7" является минимальной из "Граф 7", удовлетворяющих условию: ("Графа 7" (следующей строки) >= "Графа 7" (текущей строки))
                        "Графа 7" <= "31.12.20**" + "1 календарный день", где 31.12.20** - последний день текущего года
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
                        if ((it.incomeAccruedSumm ?: 0 > 0) && !["02", "14"].contains(it.incomeType)
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
                        if (!column7Plus1WorkingDay.check(ndflPersonIncomeFind, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день",
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                                    C_INCOME_PAYOUT_DATE, ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    } else {
                        // ToDo https://jira.aplana.com/browse/SBRFNDFL-1448
                        if (false) {
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"00.00.0000\"",
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT) : ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            //СведДох12	 Отсутствие нулевых значений
            LOG_TYPE_NOT_ZERO_CHECK: {
                if (ndflPersonIncome.incomeAccruedSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.incomePayoutSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_INCOME_PAYOUT_SUMM, ndflPersonIncome.incomePayoutSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.taxRate != null && ScriptUtils.isEmpty(ndflPersonIncome.taxRate)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_TAX_RATE, ndflPersonIncome.taxRate
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.taxSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_TAX_SUMM, ndflPersonIncome.taxSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
            }
        }
    }

    logForDebug("Проверки сведений о доходах (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Проверяется заполненность согласно временному решению
 * Если сумма начисленного дохода равна сумме вычетов, будет ноль в графах:
    Раздел 2. Графа 13. Налоговая база
    Раздел 2. Графа 16. Сумма исчисленного налога
    Раздел 2. Графа 17. Сумма удержанного налога
 * @param checkingValue
 * @param incomeAccruedSum
 * @param totalDeductionSum
 * @return true - заполнен, false - не заполнен
 */
boolean isPresentedByTempSolution(BigDecimal checkingValue, BigDecimal incomeAccruedSum, BigDecimal totalDeductionSum) {
    if (checkingValue == null) {
        return false
    }
    if (incomeAccruedSum != totalDeductionSum && checkingValue == new BigDecimal(0)) {
        return false
    }
    return true
}
    /**
     * Возвращает "Сумму применения вычета в текущем периоде"
     * @param ndflPersonIncome
     * @param ndflPersonDeductionList
     * @return
     */
    @TypeChecked
    BigDecimal getDeductionSumForIncome(NdflPersonIncome ndflPersonIncome, List<NdflPersonDeduction> ndflPersonDeductionList) {
        BigDecimal sumNdflDeduction = new BigDecimal(0)
        for (NdflPersonDeduction ndflPersonDeduction in ndflPersonDeductionList) {
            if (ndflPersonIncome.operationId == ndflPersonDeduction.operationId
                    && ndflPersonIncome.incomeAccruedDate?.format(DATE_FORMAT) == ndflPersonDeduction.incomeAccrued?.format(DATE_FORMAT)
                    && ndflPersonIncome.ndflPersonId == ndflPersonDeduction.ndflPersonId) {
                sumNdflDeduction += ndflPersonDeduction.periodCurrSumm ?: 0
            }
        }
        return sumNdflDeduction
    }

    /**
     * Класс для получения рабочих дней
     */
    @TypeChecked
    class DateConditionWorkDay {

        // Мапа рабочих дней со сдвигом
        private Map<Date, Date> workDayWithOffset0Cache
        private Map<Date, Date> workDayWithOffset1Cache
        private Map<Date, Date> workDayWithOffset30Cache
        CalendarService calendarService

        DateConditionWorkDay(CalendarService calendarService) {
            workDayWithOffset0Cache = [:]
            workDayWithOffset1Cache = [:]
            workDayWithOffset30Cache = [:]
            this.calendarService = calendarService
        }

        /**
         * Возвращает дату рабочего дня, смещенного относительно даты startDate.
         *
         * @param startDate начальная дата, может быть и рабочим днем и выходным
         * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
         * @return смещенная на offset рабочих дней дата
         */
        Date getWorkDay(Date startDate, int offset) {
            Date resultDate
            if (offset == 0) {
                resultDate = workDayWithOffset0Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset0Cache.put(startDate, resultDate)
                }
            } else if (offset == 1) {
                resultDate = workDayWithOffset1Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset1Cache.put(startDate, resultDate)
                }
            } else if (offset == 30) {
                resultDate = workDayWithOffset30Cache.get(startDate)
                if (resultDate == null) {
                    resultDate = calendarService.getWorkDay(startDate, offset)
                    workDayWithOffset30Cache.put(startDate, resultDate)
                }
            }
            return resultDate
        }
    }

    /**
     * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
     */
    @TypeChecked
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
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay)
    }

    /**
     * Проверка: "Графа 6" = "Графе 7"
     */
    @TypeChecked
    class Column6EqualsColumn7 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            String accrued = ndflPersonIncome.incomeAccruedDate?.format("dd.MM.yyyy")
            String payout = ndflPersonIncome.incomePayoutDate?.format("dd.MM.yyyy")
            return accrued == payout
        }
    }

    /**
     * Проверка: Соответствия маске
     */
    @TypeChecked
    class MatchMask implements DateConditionChecker {
        String maskRegex

        MatchMask(String maskRegex) {
            this.maskRegex = maskRegex
        }

        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
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
    @TypeChecked
    class LastMonthCalendarDay implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomeAccruedDate == null) {
                return true
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
     * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7»
     */
    @TypeChecked
    class Column7LastDayOfYear1 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendarPayout = Calendar.getInstance()
            calendarPayout.setTime(ndflPersonIncome.incomePayoutDate)
            int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
            int month = calendarPayout.get(Calendar.MONTH)
            if (dayOfMonth != 31 || month != 11) {
                return new Column6EqualsColumn7().check(ndflPersonIncome, dateConditionWorkDay)
            } else {
                return true
            }
        }
    }

    /**
     * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = 31.12.20**
     */
    @TypeChecked
    class Column7LastDayOfYear2 implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendarPayout = Calendar.getInstance()
            calendarPayout.setTime(ndflPersonIncome.incomePayoutDate)
            int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
            int month = calendarPayout.get(Calendar.MONTH)
            if (dayOfMonth != 31 || month != 11) {
                return true
            } else {
                return new MatchMask("31.12.20\\d{2}").check(ndflPersonIncome, dateConditionWorkDay)
            }
        }
    }

    /**
     * Проверка: Доход.Дата.Начисление (Графа 6) последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
     */
    @TypeChecked
    class LastMonthWorkDayIncomeAccruedDate implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.incomeAccruedDate == null) {
                return false
            }
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(ndflPersonIncome.incomeAccruedDate)
            // находим последний день месяца
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            return workDay.getTime() == ndflPersonIncome.incomeAccruedDate.getTime()
        }
    }

    /**
     * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
     */
    @TypeChecked
    class Column21EqualsColumn7Plus1WorkingDay implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate);

            // "Графа 7" + "1 рабочий день"
            int offset = 1
            Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate, offset)
            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(workDay);

            return calendar21.equals(calendar7);
        }
    }

    /**
     * Проверка: "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
     */
    @TypeChecked
    class Column21EqualsColumn7Plus30WorkingDays implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate);

            // "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
            int offset = 30
            Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate, offset)
            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(workDay);

            return calendar21.before(calendar7) || calendar21.equals(calendar7);
        }
    }

    /**
     * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
     */
    @TypeChecked
    class Column21EqualsColumn7LastDayOfMonth implements DateConditionChecker {
        @Override
        boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
            if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
                return false
            }
            Calendar calendar21 = Calendar.getInstance();
            calendar21.setTime(ndflPersonIncome.taxTransferDate);

            Calendar calendar7 = Calendar.getInstance();
            calendar7.setTime(ndflPersonIncome.incomePayoutDate);

            // находим последний день месяца
            calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date workDay = calendar7.getTime()
            // если последний день месяца приходится на выходной, то следующий первый рабочий день
            int offset = 0
            workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
            calendar7.setTime(workDay);

            return calendar21.equals(calendar7);
        }
    }

    /**
     * Проверки Сведения о вычетах
     * @param ndflPersonList
     * @param ndflPersonIncomeList
     * @param ndflPersonDeductionList
     * @param personMap
     */
    def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList,
                           List<NdflPersonDeduction> ndflPersonDeductionList, Map<Long, Map<String, RefBookValue>> personMap) {

    long time = System.currentTimeMillis()

    for (NdflPerson ndflPerson : ndflPersonList) {
        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                // РНУ-НДФЛ первичная
                String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                // РНУ-НДФЛ консолидированная
                def personRecord = personMap.get(ndflPerson.recordId)
                String fio = personRecord.get(RF_LAST_NAME).value + " " + personRecord.get(RF_FIRST_NAME).value + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

        def mapNdflPersonIncome = [:]
        for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
            String operationIdNdflPersonId = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}"
            if (!mapNdflPersonIncome.containsKey(operationIdNdflPersonId)) {
                mapNdflPersonIncome.put(operationIdNdflPersonId, [:])
            }
            mapNdflPersonIncome.get(operationIdNdflPersonId).put(ndflPersonIncome.incomeAccruedDate ? formatDate(ndflPersonIncome.incomeAccruedDate): "", ndflPersonIncome)
        }

        for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

            ScriptUtils.checkInterrupted();

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // Выч14 Документ о праве на налоговый вычет.Код источника (Графа 7)
            if (ndflPersonDeduction.notifType == "1" && ndflPersonDeduction.notifSource != "0000") {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                       C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                       C_TYPE_CODE, ndflPersonDeduction.typeCode ?:""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_7, fioAndInp, pathError, errMsg)
            }

            // Выч15 (Графы 9)
            // Выч16 (Графы 10)
            String operationIdNdflPersonIdDate = "${ndflPersonDeduction.operationId}_${ndflPersonDeduction.ndflPersonId}"
            Map<String, NdflPersonIncome> mapNdflPersonIncomeDate = mapNdflPersonIncome.get(operationIdNdflPersonIdDate)
            if (mapNdflPersonIncomeDate == null) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Для гр. \"%s\" (\"%s\") отсутствуют операция или физическое лицо в разделе 2",
                        C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_10, fioAndInp, pathError, errMsg)
            } else {
                NdflPersonIncome ndflPersonIncome = mapNdflPersonIncomeDate.get(ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "")
                if (ndflPersonIncome == null) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    ndflPersonIncome = mapNdflPersonIncomeDate.values().find{
                        it.incomeAccruedDate != null
                    }
                    String errMsg
                    if (ndflPersonIncome != null) {
                        errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                                C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "",
                                C_INCOME_ACCRUED_DATE, formatDate(ndflPersonIncome.incomeAccruedDate)
                        )
                    } else {
                        errMsg = String.format("Для гр. \"%s\" (\"%s\") не найдено заполненных гр. \"%s\" Раздела 2",
                                C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "",
                                C_INCOME_ACCRUED_DATE
                        )
                    }
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_3_10, fioAndInp, pathError, errMsg)
                } else {
                    // Выч17 Начисленный доход.Код дохода (Графы 11)
                    if (ndflPersonDeduction.incomeCode != ndflPersonIncome.incomeCode) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                                C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                C_INCOME_ACCRUED_CODE, ndflPersonDeduction.incomeCode ?: ""
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_3_11, fioAndInp, pathError, errMsg)
                    }

                    // Выч18 Начисленный доход.Сумма (Графы 12)
                    if (!comparNumbEquals(ndflPersonDeduction.incomeSumm, ndflPersonIncome.incomeAccruedSumm)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                                C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: "",
                                C_INCOME_ACCRUED_P_SUMM, ndflPersonDeduction.incomeSumm ?: ""
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_3_12, fioAndInp, pathError, errMsg)
                    }
                }
            }

            // Выч20 Начисленный доход.Дата (Графы 10)
            if (ndflPersonDeduction.periodCurrDate != ndflPersonDeduction.incomeAccrued) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (\"%s\")\" не соответствует значению гр. \"%s\" (\"%s\")",
                        C_PERIOD_CURR_DATE, ndflPersonDeduction.periodCurrDate ? formatDate(ndflPersonDeduction.periodCurrDate): "",
                        C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued): ""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_10_2, fioAndInp, pathError, errMsg)
            }

            // Выч21 Документ о праве на налоговый вычет.Сумма (Графы 16) (Графы 8)
            if (comparNumbGreater(ndflPersonDeduction.periodCurrSumm ?: 0, ndflPersonDeduction.notifSumm ?: 0)) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                String errMsg = String.format("Значение гр. \"%s\" (%s) не должно превышать значение гр. \"%s\" (%s) согласно подтверждающему документу",
                        C_PERIOD_CURR_SUMM, ndflPersonDeduction.periodCurrSumm ?: "",
                        C_NOTIF_SUMM, ndflPersonDeduction.notifSumm ?:""
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_16, fioAndInp, pathError, errMsg)
            }
        }
        logForDebug("Проверки сведений о вычетах (" + (System.currentTimeMillis() - time) + " мс)");
    }

    /**
     * Сравнение чисел с плавающей точкой через эпсилон-окрестности
     */
    boolean comparNumbEquals(def d1, def d2) {
        if (d1 == null || d2 == null) return false
        return (Math.abs(d1 - d2) < 0.001)
    }
    boolean comparNumbGreater(double d1, double d2) {
        if (d1 == null || d2 == null) return false
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
            departmentParamException(departmentId, declarationData.reportPeriodId)
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
            departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
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
                JOIN REF_BOOK_NDFL n ON (n.ID = nd.ref_book_ndfl_id)
                    where n.department_id = ${declarationData.departmentId}
                           and nd.ref_book_ndfl_id = n.ID
                           and exists(select 1 from t where t.record_id = frb.record_id and t.version = frb.version)
                           and frb.status = 0
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
        Map<Long, FiasCheckInfo> checkFiasAddressMap = getFiasAddressIdsMap();
        return (checkFiasAddressMap.get(ndflPersonId) != null)
    }

    /**
    * Проверка адреса на пустоту
    * @param Данные о ФЛ из формы
    * @return
    */
    boolean isPersonAddressEmpty(NdflPerson ndflPerson) {
        boolean  emptyAddress = ScriptUtils.isEmpty(ndflPerson.regionCode) && ScriptUtils.isEmpty(ndflPerson.area) &&
                                ScriptUtils.isEmpty(ndflPerson.city) &&  ScriptUtils.isEmpty(ndflPerson.locality) &&
                                ScriptUtils.isEmpty(ndflPerson.street) && ScriptUtils.isEmpty(ndflPerson.house) &&
                                ScriptUtils.isEmpty(ndflPerson.building) &&  ScriptUtils.isEmpty(ndflPerson.flat);
        return emptyAddress;
    }

    //TODO вынес handler в скрипт, чтобы не обновлять ядро на нексте

    /**
     * @author Andrey Drunk
     */
    @TypeChecked
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
            ScriptUtils.checkInterrupted();

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
            Integer docStatus = SqlUtils.getInteger(rs, "doc_status")

            if (pesonDocumentMap == null) {
                pesonDocumentMap = new HashMap<Long, PersonDocument>();
                documentsMap.put(refBookPersonId, pesonDocumentMap);
            }

            if (docId != null && !pesonDocumentMap.containsKey(docId) && docStatus == 0) {
                Long docTypeId = SqlUtils.getLong(rs, "doc_id");
                DocType docType = getDocTypeById(docTypeId);
                PersonDocument personDocument = new PersonDocument();
                personDocument.setId(docId);

                personDocument.setRecordId(SqlUtils.getLong(rs, "doc_record_id"));
                personDocument.setStatus(docStatus);
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
                person.setInn(rs.getString("inn"));
                person.setInnForeign(rs.getString("inn_foreign"));
                person.setSnils(rs.getString("snils"));
                person.setBirthDate(rs.getDate("birth_date"));

                //ссылки на справочники
                person.setTaxPayerStatus(getTaxpayerStatusById(SqlUtils.getLong(rs, "taxpayer_state")));
                person.setCitizenship(getCountryById(SqlUtils.getLong(rs, "citizenship")));

                //additional
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

    @TypeChecked
    void checkCreate() {
        def departmentReportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
        if (departmentReportPeriod.correctionDate != null) {
            def prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(declarationData.getDepartmentId(), declarationData.getReportPeriodId())
            def declarationList = declarationService.find(102, prevDepartmentReportPeriod.getId())
            declarationList.addAll(declarationService.find(103, prevDepartmentReportPeriod.getId()))
            declarationList.addAll(declarationService.find(104, prevDepartmentReportPeriod.getId()))
            if (declarationList.isEmpty()) {
                logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде. Отчетные налоговые формы не будут сформированы текущем периоде")
            }
        }
    }

    @TypeChecked
    void departmentParamException(int departmentId, int reportPeriodId) {
        ReportPeriod reportPeriod = reportPeriodService.get(reportPeriodId)
        throw new ServiceException("Отсутствуют настройки подразделения \"%s\" периода \"%s\". Необходимо выполнить настройку в разделе меню \"Налоги->НДФЛ->Настройки подразделений\"",
                departmentService.get(departmentId).getName(),
                reportPeriod.getTaxPeriod().getYear() + ", " + reportPeriod.getName()
        ) as Throwable
    }