package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonOperation
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils

import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.xml.MarkupBuilder
import org.joda.time.LocalDateTime

new Calculate(this).run()

@TypeChecked
class Calculate extends AbstractScriptClass {

    NdflPersonService ndflPersonService
    DeclarationData declarationData
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    FileWriter xml

    final String TEMPLATE_PERSON_FL = "%s, ИНП: %s"

    final String R_PERSON = "Физические лица"

    final String RF_RECORD_ID = "RECORD_ID"

    // Кэш провайдеров cправочников
    Map<Long, RefBookDataProvider> providerCache = [:]
    //Коды стран из справочника
    Map<Long, String> countryCodeCache = [:]
    //Виды документов, удостоверяющих личность
    Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
    //Коды статуса налогоплательщика
    Map<Long, String> taxpayerStatusCodeCache = [:]

    // Дата окончания отчетного периода
    Date periodEndDate = null

    private Calculate() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    public Calculate(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("xml")) {
            this.xml = (FileWriter) scriptClass.getProperty("xml");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
    }

    @Override
    void run() {
        initConfiguration()
        switch (formDataEvent) {
            case FormDataEvent.CALCULATE:
                clearData()
                consolidation()
                generateXml()
        }
    }

    /**
     * Очистка данных налоговой формы
     * @return
     */
    def clearData() {
        //удаляем рассчитанные данные если есть
        ndflPersonService.deleteAll(declarationData.id)
        //Удаляем все сформированные ранее отчеты налоговой формы
        declarationService.deleteReport(declarationData.id)
    }

    /**
     * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
     * Данный метод выполняет вызов скрипта (GET-SOURCES) и
     *
     */
    void consolidation() {

        long time = System.currentTimeMillis();

        def declarationDataId = declarationData.id

        //декларация-приемник, true - заполнятся только текстовые данные для GUI и сообщений,true - исключить несозданные источники,ограничение по состоянию для созданных экземпляров список нф-источников
        List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, true, false, null, userInfo, logger);

        List<Long> declarationDataIdList = collectDeclarationDataIdList(sourcesInfo);

        if (declarationDataIdList.isEmpty()) {
            throw new ServiceException("Ошибка консолидации. Не найдено ни одной формы-источника.");
        }

        logForDebug("Номера первичных НФ, включенных в консолидацию: " + declarationDataIdList + " (" + declarationDataIdList.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

        List<NdflPerson> ndflPersonList = collectNdflPersonList(sourcesInfo);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceException("При получении источников возникли ошибки. Консолидация НФ невозможна.");
        }


        time = System.currentTimeMillis();

        Map<Long, List<Long>> deletedPersonMap = getDeletedPersonMap(declarationDataIdList)

        //record_id, Map<String, RefBookValue>
        Map<Long, Map<String, RefBookValue>> refBookPersonMap = getActualRefPersonsByDeclarationDataIdList(declarationDataIdList);
        logForDebug("Выгрузка справочника Физические лица (" + refBookPersonMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

        //id, Map<String, RefBookValue>
        Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPersonMap);
        logForDebug("Выгрузка справочника Адреса физических лиц (" + addressMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

        //id, List<Map<String, RefBookValue>>
        Map<Long, List<Map<String, RefBookValue>>> personDocMap = getActualRefDulByDeclarationDataIdList(declarationDataIdList)
        logForDebug("Выгрузка справочника Документы физических лиц (" + personDocMap.size() + " записей, " + ScriptUtils.calcTimeMillis(time));

        logForDebug("Инициализация кэша справочников (" + ScriptUtils.calcTimeMillis(time));

        //Карта в которой хранится актуальный record_id и NdflPerson в котором объединяются данные о даходах
        Map<Long, NdflPerson> ndflPersonMap = consolidateNdflPerson(ndflPersonList, declarationDataIdList);

        logForDebug(String.format("Количество физических лиц, загруженных из первичных НФ-источников: %d", ndflPersonList.size()));
        logForDebug(String.format("Количество уникальных физических лиц в формах-источниках по справочнику ФЛ: %d", ndflPersonMap.size()));


        time = System.currentTimeMillis();

        //разделы в которых идет сплошная нумерация
        def ndflPersonNum = 1L;
        BigDecimal incomesRowNum = new BigDecimal(1);
        BigDecimal deductionRowNum = new BigDecimal(1);
        BigDecimal prepaymentRowNum = new BigDecimal(1);

        for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {
            ScriptUtils.checkInterrupted()

            Long refBookPersonRecordId = entry.getKey();

            Map<String, RefBookValue> refBookPersonRecord = refBookPersonMap.get(refBookPersonRecordId);

            Long refBookPersonId = refBookPersonRecord?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
            NdflPerson ndflPerson = entry.getValue();

            if (refBookPersonId == null) {
                String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                String fioAndInp = String.format(TEMPLATE_PERSON_FL, fio, ndflPerson.inp)
                deletedPersonMap.get(refBookPersonRecordId).each { def personDeclarationDataId ->
                    logger.errorExp("%s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp,
                            "В налоговой форме № ${personDeclarationDataId} не удалось установить связь со справочником \"$R_PERSON\"")
                }
                continue
            }

            def incomes = ndflPerson.incomes;
            def deductions = ndflPerson.deductions;
            def prepayments = ndflPerson.prepayments;

            //Сортируем сначала по дате начисления, затем по дате выплаты
            incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
            deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
            prepayments.sort { a, b -> a.notifDate <=> b.notifDate }


            List<Map<String, RefBookValue>> personDocumentsList = personDocMap.get(refBookPersonId)

            //Выбираем ДУЛ с признаком включения в отчетность 1
            Map<String, RefBookValue> personDocumentRecord = personDocumentsList?.find {
                it.get("INC_REP")?.getNumberValue() == 1
            };

            Long addressId = refBookPersonRecord.get("ADDRESS")?.getReferenceValue();
            Map<String, RefBookValue> addressRecord = addressMap.get(addressId);

            if (personDocumentsList == null || personDocumentsList.isEmpty()) {
                logger.warn("Физическое лицо: " + buildRefBookPersonFio(refBookPersonRecord) + ", Идентификатор ФЛ: " + buildRefBookPersonId(refBookPersonRecord) + ", включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность'")
            } else if (personDocumentRecord == null || personDocumentRecord.isEmpty()) {
                logger.warn("Физическое лицо: " + buildRefBookPersonFio(refBookPersonRecord) + ", Идентификатор ФЛ: " + buildRefBookPersonId(refBookPersonRecord) + ", включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность' с признаком включения в отчетность: 1")
            }

            if (addressId != null && addressRecord == null) {
                logger.warn("Для физического лица: " + buildRefBookNotice(refBookPersonRecord) + ". Отсутствуют данные в справочнике 'Адреса физических лиц'");
                continue;
            }

            //Создание консолидированной записи NdflPerson
            NdflPerson consolidatePerson = buildNdflPerson(refBookPersonRecord, personDocumentRecord, addressRecord);

            consolidatePerson.rowNum = ndflPersonNum;
            consolidatePerson.declarationDataId = declarationDataId

            //Доходы
            List<NdflPersonIncome> consolidatedIncomesList = new ArrayList<NdflPersonIncome>();
            for (NdflPersonIncome income : incomes) {
                NdflPersonIncome consolidatedIncome = consolidateDetail(income, incomesRowNum);
                consolidatedIncomesList.add(consolidatedIncome);
                incomesRowNum = incomesRowNum.add(new BigDecimal(1));
            }
            consolidatePerson.setIncomes(consolidatedIncomesList);

            //Вычеты
            List<NdflPersonDeduction> consolidatedDeductionsList = new ArrayList<NdflPersonDeduction>();
            for (NdflPersonDeduction deduction : deductions) {
                NdflPersonDeduction consolidatedDeduction = consolidateDetail(deduction, deductionRowNum);
                consolidatedDeductionsList.add(consolidatedDeduction);
                deductionRowNum = deductionRowNum.add(new BigDecimal(1));
            }
            consolidatePerson.setDeductions(consolidatedDeductionsList);

            //Авансы
            List<NdflPersonPrepayment> consolidatedPrepaymentsList = new ArrayList<NdflPersonPrepayment>();
            for (NdflPersonPrepayment prepayment : prepayments) {
                NdflPersonPrepayment consolidatedPrepayment = consolidateDetail(prepayment, prepaymentRowNum);
                consolidatedPrepaymentsList.add(consolidatedPrepayment);
                prepaymentRowNum = prepaymentRowNum.add(new BigDecimal(1));
            }
            consolidatePerson.setPrepayments(consolidatedPrepaymentsList);

            ndflPersonService.save(consolidatePerson)
            ndflPersonNum++

        }

        logForDebug("Консолидация завершена, новых записей создано: " + (ndflPersonNum - 1) + ", " + ScriptUtils.calcTimeMillis(time));
        logger.info("Номера первичных НФ, включенных в консолидацию: " + declarationDataIdList.join(", ") + " (всего " + declarationDataIdList.size() + " " + ScriptUtils.getFirstDeclensionByNumeric("форма", declarationDataIdList.size()) + ")")
    }


    /**
     * Получаем список идентификаторов деклараций которые попадут в консолидированную форму
     * @param sourcesInfo
     * @return
     */
    List<Long> collectDeclarationDataIdList(List<Relation> sourcesInfo) {
        def result = []
        for (Relation relation : sourcesInfo) {
            if (!result.contains(relation.declarationDataId)) {
                result.add(relation.declarationDataId)
            }
        }

        return result.sort()
    }

    /**
     * Получаем список NdflPerson которые попадут в консолидированную форму
     * @param sourcesInfo
     * @return
     */
    List<NdflPerson> collectNdflPersonList(List<Relation> sourcesInfo) {

        long time = System.currentTimeMillis();

        List<NdflPerson> result = new ArrayList<NdflPerson>();
        // собираем данные из источников
        int i = 0;
        for (Relation relation : sourcesInfo) {
            Long declarationDataId = relation.declarationDataId;
            if (!relation.declarationState.equals(State.ACCEPTED)) {
                logger.error(String.format("Налоговая форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", подразделение: \"%s\", id=\"%d\"", relation.getDeclarationTypeName(), relation.getFullDepartmentName(), declarationDataId))
                continue
            }
            List<NdflPerson> ndflPersonList = findNdflPersonWithData(declarationDataId);

            //logger.info("Физических лиц в НФ "+declarationDataId+ ": " + ndflPersonList.size());

            result.addAll(ndflPersonList);
            i++;
        }

        return result;
    }

    /**
     * Получение списка удаленных ФЛ в виде мапы personId: List<declarationDataId>
     * @param declarationDataIdList
     */
    Map<Long, List<Long>> getDeletedPersonMap(List<Long> declarationDataIdList) {
        Map<Long, List<Long>> result = [:]
        declarationDataIdList.each { Long it ->
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${it} AND ref_book_person.id = np.person_id)"
            Map<Long, Map<String, RefBookValue>> refPersonMap = getProvider(RefBook.Id.PERSON.id).getRecordDataWhere(whereClause)
            refPersonMap.each { Long k, Map<String, RefBookValue> v ->
                Long personId = v.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue()
                if (!result.containsKey(personId)) {
                    result.put(personId, new ArrayList<Long>())
                }
                result.get(personId).add(it)
            }
        }
        return result
    }

    /**
     * Получить список актуальных записей о физлицах, в нф
     * @param declarationDataIdList список id нф
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataIdList(List<Long> declarationDataIdList) {
        //Если исходных форм достаточно много то можно переделать запрос на получение списка declarationDataIdList
        def result = new HashMap<Long, Map<String, RefBookValue>>()
        declarationDataIdList.each {
            Map<Long, Map<String, RefBookValue>> mapPersons = getActualRefPersonsByDeclarationDataId(it)
            mapPersons.each { recordId, refBookValue ->
                result.put(recordId, refBookValue)
            }
        }
        return result
    }

    /**
     * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
     * @param personMap
     * @return
     */
    Map<Long, Map<String, RefBookValue>> getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
        Map<Long, Map<String, RefBookValue>> result = new HashMap<Long, Map<String, RefBookValue>>()
        def addressIds = []
        def count = 0
        personMap.each { recordId, person ->
            if (person.get("ADDRESS").value != null) {
                Long addressId = person.get("ADDRESS")?.getReferenceValue()
                // Адрес может быть не задан
                if (addressId != null) {
                    addressIds.add(addressId)
                    count++
                    if (count >= 1000) {
                        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
                        refBookMap.each { id, address ->
                            result.put(id, address)
                        }
                        addressIds.clear()
                        count = 0
                    }
                }
            }
        }

        if (addressIds.size() > 0) {
            Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
            refBookMap.each { addressId, address ->
                result.put(addressId, address)
            }
        }

        return result
    }

    /**
     * Получить "ДУЛ" по всем физлицам указвнных в НФ
     * @return
     */
    Map<Long, List<Map<String, RefBookValue>>> getActualRefDulByDeclarationDataIdList(List<Long> declarationDataIdList) {
        Map<Long, List<Map<String, RefBookValue>>> result = new HashMap<Long, List<Map<String, RefBookValue>>>();
        declarationDataIdList.each {
            String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${it} AND ref_book_id_doc.person_id = np.person_id) AND ref_book_id_doc.status = 0"
            Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(RefBook.Id.ID_DOC.id, whereClause)

            refBookMap.each { personId, refBookValues ->
                Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
                List<Map<String, RefBookValue>> dulList = result.get(refBookPersonId);
                if (dulList == null) {
                    dulList = new ArrayList<Map<String, RefBookValue>>();
                    result.put(refBookPersonId, dulList);
                }
                dulList.add(refBookValues);
            }
        }
        return result
    }

    /**
     * Объединение ndfl-person по record_id, в данном методе происходит объединение ФЛ по record id из одной или нескольких НФ
     * @param refBookPerson
     * @return
     */
    Map<Long, NdflPerson> consolidateNdflPerson(List<NdflPerson> ndflPersonList, List<Long> declarationDataIdList) {

        Map<Long, NdflPerson> result = new TreeMap<Long, NdflPerson>();

        for (NdflPerson ndflPerson : ndflPersonList) {

            if (ndflPerson.personId == null || ndflPerson.recordId == null) {
                throw new ServiceException("Ошибка при консолидации данных. Необходимо повторно выполнить расчет формы " + ndflPerson.declarationDataId);
            }

            Long personRecordId = ndflPerson.recordId;

            NdflPerson consNdflPerson = result.get(personRecordId)

            //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
            if (consNdflPerson == null) {
                consNdflPerson = new NdflPerson()
                consNdflPerson.recordId = personRecordId;
                consNdflPerson.inp = ndflPerson.inp
                consNdflPerson.lastName = ndflPerson.lastName
                consNdflPerson.firstName = ndflPerson.firstName
                consNdflPerson.middleName = ndflPerson.middleName
                result.put(personRecordId, consNdflPerson)
            }
            consNdflPerson.incomes.addAll(ndflPerson.incomes);
            consNdflPerson.deductions.addAll(ndflPerson.deductions);
            consNdflPerson.prepayments.addAll(ndflPerson.prepayments);
        }

        return result;
    }

    /**
     * @param refBookPersonRecord
     * @return
     */
    String buildRefBookPersonId(Map<String, RefBookValue> refBookPersonRecord) {
        return getVal(refBookPersonRecord, "RECORD_ID");
    }

    /**
     * TODO Использовать метод com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils#buildRefBookNotice(java.util.Map)
     * @param refBookPersonRecord
     * @return
     */
    String buildRefBookPersonFio(Map<String, RefBookValue> refBookPersonRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append(getVal(refBookPersonRecord, "LAST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "FIRST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "MIDDLE_NAME"));
        return sb.toString();
    }

    /**
     * TODO Использовать метод com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils#buildRefBookNotice(java.util.Map)
     * @param refBookPersonRecord
     * @return
     */
    String buildRefBookNotice(Map<String, RefBookValue> refBookPersonRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("Номер '").append(getVal(refBookPersonRecord, "RECORD_ID")).append("': ");
        sb.append(getVal(refBookPersonRecord, "LAST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "FIRST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "MIDDLE_NAME")).append(" ");
        sb.append(" [id=").append(getVal(refBookPersonRecord, RefBook.RECORD_ID_ALIAS)).append("]");
        return sb.toString();
    }

    /**
     * Создает объект NdlPerson заполненный данными из справочника
     */
    NdflPerson buildNdflPerson(Map<String, RefBookValue> personRecord, Map<String, RefBookValue> identityDocumentRecord, Map<String, RefBookValue> addressRecord) {

        Map<Long, String> countryCodes = getRefCountryCode();
        Map<Long, Map<String, RefBookValue>> documentTypeRefBook = getRefDocumentType();
        Map<Long, String> taxpayerStatusCodes = getRefTaxpayerStatusCode();

        NdflPerson ndflPerson = new NdflPerson()

        //Данные о физлице - заполняется на основе справочника физлиц
        ndflPerson.personId = (Long) personRecord.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue() //Идентификатор ФЛ
        ndflPerson.inp = personRecord.get("RECORD_ID")?.getNumberValue()
        ndflPerson.snils = personRecord.get("SNILS")?.getStringValue()
        ndflPerson.lastName = personRecord.get("LAST_NAME")?.getStringValue()
        ndflPerson.firstName = personRecord.get("FIRST_NAME")?.getStringValue()
        ndflPerson.middleName = personRecord.get("MIDDLE_NAME")?.getStringValue()
        ndflPerson.birthDay = new LocalDateTime(personRecord.get("BIRTH_DATE")?.getDateValue())
        ndflPerson.innNp = personRecord.get("INN")?.getStringValue()
        ndflPerson.innForeign = personRecord.get("INN_FOREIGN")?.getStringValue()


        Long countryId = personRecord.get("CITIZENSHIP")?.getReferenceValue();

        ndflPerson.citizenship = countryCodes.get(countryId)

        //ДУЛ - заполняется на основе справочника Документы, удостоверяющие личность
        Map<String, RefBookValue> docTypeRecord = (identityDocumentRecord != null) ? documentTypeRefBook.get(identityDocumentRecord.get("DOC_ID")?.getReferenceValue()) : null
        ndflPerson.idDocType = docTypeRecord?.get("CODE")?.getStringValue();

        ndflPerson.idDocNumber = identityDocumentRecord?.get("DOC_NUMBER")?.getStringValue()

        ndflPerson.status = taxpayerStatusCodes.get(personRecord.get("TAXPAYER_STATE")?.getReferenceValue())

        //адрес может быть не задан
        if (addressRecord != null) {
            ndflPerson.postIndex = addressRecord.get("POSTAL_CODE")?.getStringValue()
            ndflPerson.regionCode = addressRecord.get("REGION_CODE")?.getStringValue()
            ndflPerson.area = addressRecord.get("DISTRICT")?.getStringValue()
            ndflPerson.city = addressRecord.get("CITY")?.getStringValue()
            ndflPerson.locality = addressRecord.get("LOCALITY")?.getStringValue()
            ndflPerson.street = addressRecord.get("STREET")?.getStringValue()
            ndflPerson.house = addressRecord.get("HOUSE")?.getStringValue()
            ndflPerson.building = addressRecord.get("BUILD")?.getStringValue()
            ndflPerson.flat = addressRecord.get("APPARTMENT")?.getStringValue()
            ndflPerson.countryCode = countryCodes.get(addressRecord.get("COUNTRY_ID")?.getReferenceValue())
            ndflPerson.address = addressRecord.get("ADDRESS")?.getStringValue()
            //ndflPerson.additionalData = currentNdflPerson.additionalData
        }

        return ndflPerson
    }

    /**
     * При
     * @param ndflPersonDetail
     * @param i
     * @return
     */
    static <T extends NdflPersonOperation> T consolidateDetail(T ndflPersonDetail, BigDecimal rowNum) {
        def sourceId = ndflPersonDetail.id;
        ndflPersonDetail.id = null
        ndflPersonDetail.ndflPersonId = null
        ndflPersonDetail.rowNum = rowNum
        ndflPersonDetail.sourceId = sourceId;
        return ndflPersonDetail
    }

    /**
     * Найти все NdflPerson привязанные к НФ вместе с данными о доходах
     * @param declarationDataId
     * @return
     */
    List<NdflPerson> findNdflPersonWithData(Long declarationDataId) {

        List<NdflPerson> result = new ArrayList<NdflPerson>();
        List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationDataId);

        Map<Long, List<NdflPersonIncome>> imcomesList = mapToPesonId(ndflPersonService.findNdflPersonIncome(declarationDataId));
        Map<Long, List<NdflPersonDeduction>> deductionList = mapToPesonId(ndflPersonService.findNdflPersonDeduction(declarationDataId));
        Map<Long, List<NdflPersonPrepayment>> prepaymentList = mapToPesonId(ndflPersonService.findNdflPersonPrepayment(declarationDataId));

        for (NdflPerson ndflPerson : ndflPersonList) {
            Long ndflPersonId = ndflPerson.getId();
            ndflPerson.setIncomes(imcomesList.get(ndflPersonId));
            ndflPerson.setDeductions(deductionList.get(ndflPersonId));
            ndflPerson.setPrepayments(prepaymentList.get(ndflPersonId));
            result.add(ndflPerson);
        }
        return result;
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
     * Получить актуальные на отчетную дату записи справочника "Физические лица"
     * @return Map < person_id , Map < имя_поля , значение_поля > >
     */
    Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId(declarationDataId) {
        String whereClause = """
                    JOIN ref_book_person p ON (frb.record_id = p.record_id)
                    JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
                """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(RefBook.Id.PERSON.id, whereClause, getReportPeriodEndDate() - 1)
        Map<Long, Map<String, RefBookValue>> refBookMapResult = new HashMap<Long, Map<String, RefBookValue>>();
        refBookMap.each { Long personId, Map<String, RefBookValue> refBookValue ->
            Long refBookRecordId = (Long) refBookValue.get(RF_RECORD_ID).value
            refBookMapResult.put(refBookRecordId, refBookValue)
        }
        return refBookMapResult
    }

    String getVal(Map<String, RefBookValue> refBookPersonRecord, String attrName) {
        RefBookValue refBookValue = refBookPersonRecord.get(attrName);
        if (refBookValue != null) {
            return refBookValue.toString();
        } else {
            return null;
        }
    }

    /**
     * Получить "Страны"
     * @return
     */
    Map<Long, String> getRefCountryCode() {
        if (countryCodeCache.size() == 0) {
            List<Map<String, RefBookValue>> refBookMap = getRefBookAll(RefBook.Id.COUNTRY.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                countryCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return countryCodeCache;
    }

    /**
     * Получить "Виды документов"
     */
    Map<Long, Map<String, RefBookValue>> getRefDocumentType() {
        if (documentTypeCache.size() == 0) {
            List<Map<String, RefBookValue>> refBookList = getRefBookAll(RefBook.Id.DOCUMENT_CODES.getId())
            refBookList.each { Map<String, RefBookValue> refBook ->
                documentTypeCache.put((Long) refBook?.id?.numberValue, refBook)
            }
        }
        return documentTypeCache;
    }

    /**
     * Получить "Статусы налогоплательщика"
     * @return
     */
    Map<Long, String> getRefTaxpayerStatusCode() {
        if (taxpayerStatusCodeCache.size() == 0) {
            List<Map<String, RefBookValue>> refBookMap = getRefBookAll(RefBook.Id.TAXPAYER_STATUS.getId())
            refBookMap.each { Map<String, RefBookValue> refBook ->
                taxpayerStatusCodeCache.put((Long) refBook?.id?.numberValue, refBook?.CODE?.stringValue)
            }
        }
        return taxpayerStatusCodeCache;
    }

    /**
     * Выгрузка из справочников по условию
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordWhere(Long refBookId, String whereClause) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap();
        }
        return refBookMap
    }

    static <T extends NdflPersonOperation> Map<Long, List<T>> mapToPesonId(List<T> operationList) {
        Map<Long, List<T>> result = new HashMap<Long, List<T>>()
        for (T personOperation : operationList) {
            Long ndflPersonId = personOperation.getNdflPersonId();
            if (!result.containsKey(ndflPersonId)) {
                result.put(ndflPersonId, new ArrayList<T>());
            }
            result.get(ndflPersonId).add(personOperation);
        }
        return result;
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
     * Выгрузка из справочников по условию и версии
     * @param refBookId
     * @param whereClause
     * @return
     * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
     */
    Map<Long, Map<String, RefBookValue>> getRefBookByRecordVersionWhere(Long refBookId, String whereClause, Date version) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
        if (refBookMap == null || refBookMap.size() == 0) {
            //throw new ScriptException("Не найдены записи справочника " + refBookId)
            return Collections.emptyMap();
        }
        return refBookMap
    }

    /**
     * Создание фиктивной xml для привязки к экземпляру
     * declarationDataId
     * @return
     */

    def generateXml() {
        MarkupBuilder builder = new MarkupBuilder((FileWriter) xml)
        createXmlNode(builder, "Файл", ["имя": declarationData.id])
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    def createXmlNode(builder, name, attributes) {
        builder.name(attributes)
    }

}
