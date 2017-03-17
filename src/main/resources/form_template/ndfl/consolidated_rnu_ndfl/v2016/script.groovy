package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.refbook.*
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.util.slurpersupport.NodeChild

import javax.script.ScriptException
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.log.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException

import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверить
        checkData()
        checkDataConsolidated()
        break
    case FormDataEvent.CALCULATE: //консолидирование с формированием фиктивного xml
        clearData()
        consolidation()
        generateXml()
        break
    case FormDataEvent.AFTER_CALCULATE: // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.GET_SOURCES: //формирование списка ПНФ для консолидации
        getSourcesListForTemporarySolution()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        // Подготовка для последующего формирования спецотчета
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        // Формирование спецотчета
        createSpecificReport()
        break
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

def calcTimeMillis(long time) {
    long currTime = System.currentTimeMillis();
    return " (" + (currTime - time) + " ms)";
}

/**
 * Вид формы "Консолидированная", используется при определении проверок в частях
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataReference(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataCommon(java.util.ArrayList, java.util.ArrayList)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataIncome(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)}
 * {@link form_template.ndfl.consolidated_rnu_ndfl.v2016.script#checkDataDeduction(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList)}
 *
 */
@Field final FormDataKind FORM_DATA_KIND = FormDataKind.CONSOLIDATED;

/**
 * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
 */
@Field final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
@Field final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

//>------------------< CONSOLIDATION >----------------------<

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

    logger.info("Номера первичных НФ включенных в консолидацию: " + declarationDataIdList + ", количество первичных НФ включенных в консолидацию: " + declarationDataIdList.size() + calcTimeMillis(time));

    List<NdflPerson> ndflPersonList = collectNdflPersonList(sourcesInfo);

    if (logger.containsLevel(LogLevel.ERROR)) {
        throw new ServiceException("При получении источников возникли ошибки. Консолидация НФ невозможна.");
    }


    time = System.currentTimeMillis();

    //record_id, Map<String, RefBookValue>
    Map<Long, Map<String, RefBookValue>> refBookPersonMap = getActualRefPersonsByDeclarationDataIdList(declarationDataIdList);
    logger.info("Выгрузка справочника Физические лица: записей найдено "+refBookPersonMap.size() + calcTimeMillis(time));

    //id, Map<String, RefBookValue>
    Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPerson);
    logger.info("Выгрузка справочника Адреса физических лиц: записей найдено "+addressMap.size() + calcTimeMillis(time));

    //id, List<Map<String, RefBookValue>>
    Map<Long, List<Map<String, RefBookValue>>> personDocMap = getActualRefDulByDeclarationDataIdList(declarationDataIdList)
    logger.info("Выгрузка справочника Документы физических лиц: записей найдено "+personDocMap.size() + calcTimeMillis(time));

    logger.info("Инициализация кэша справочников "+ calcTimeMillis(time));


    //Карта в которой хранится актуальный record_id и NdflPerson в котором объединяются данные о даходах
    SortedMap<Long, NdflPerson> ndflPersonMap = consolidateNdflPerson(ndflPersonList, declarationDataIdList);

    logger.info(String.format("Количество физических лиц, загруженных из первичных НФ-источников: %d", ndflPersonList.size()));
    logger.info(String.format("Количество уникальных физических лиц в формах-источниках по справочнику ФЛ: %d", ndflPersonMap.size()));


    time = System.currentTimeMillis();

    //разделы в которых идет сплошная нумерация
    def ndflPersonNum = 1;
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {

        Long refBookPersonRecordId = entry.getKey();

        Map<String, RefBookValue> refBookPersonRecord = refBookPersonMap.get(refBookPersonRecordId);

        Long refBookPersonId = refBookPersonRecord?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();

        if (refBookPersonId == null){
            throw new ServiceException("Ошибка при получение записи справочника 'Физические лица'. Не найдена запись номер: "+refBookPersonRecordId);
        }

        NdflPerson ndflPerson = entry.getValue();

        def incomes = ndflPerson.incomes;
        def deductions = ndflPerson.deductions;
        def prepayments = ndflPerson.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
        prepayments.sort { a, b -> a.notifDate <=> b.notifDate }


        List<Map<String, RefBookValue>> personDocumentsList = personDocMap.get(refBookPersonId)

        //TODO удалить
        logger.info("personDocumentsList "+personDocumentsList)

        //Выбираем ДУЛ с признаком включения в отчетность 1
        Map<String, RefBookValue> personDocumentRecord = personDocumentsList?.find {
            it.get("INC_REP")?.getNumberValue() == 1
        };

        Long addressId = refBookPersonRecord.get("ADDRESS")?.getReferenceValue();
        Map<String, RefBookValue> addressRecord = addressMap.get(addressId);

        //List<Long> documentsIds = documentProvider.getUniqueRecordIds(null, "PERSON_ID = " + personId + " AND INC_REP = 1");
        if (personDocumentRecord == null || personDocumentRecord.isEmpty()) {
            logger.error("Для физического лица: " + buildRefBookNotice(refBookPersonRecord) +  ". Отсутствуют данные в справочнике 'Документы, удостоверяющие личность' с признаком включения в отчетность: 1");
            continue;
        }

        if (addressId != null && addressRecord == null) {
            logger.error("Для физического лица: " + buildRefBookNotice(refBookPersonRecord) +  ". Отсутствуют данные в справочнике 'Адреса физических лиц'");
            continue;
        }

        //Создание консолидированной записи NdflPerson
        def consolidatePerson = buildNdflPerson(refBookPersonRecord, personDocumentRecord, addressRecord);

        consolidatePerson.rowNum = ndflPersonNum;
        consolidatePerson.declarationDataId = declarationDataId

        //Доходы
        List<NdflPersonIncome> consolidatedIncomesList = new ArrayList<NdflPersonIncome>();
        for (NdflPersonIncome income : incomes) {
            NdflPersonIncome consolidatedIncome = consolidateDetail(income, incomesRowNum);
            consolidatedIncomesList.add(consolidatedIncome);
            incomesRowNum++;
        }
        consolidatePerson.setIncomes(consolidatedIncomesList);

        //Вычеты
        List<NdflPersonIncome> consolidatedDeductionsList = new ArrayList<NdflPersonIncome>();
        for (NdflPersonDeduction deduction : deductions) {
            NdflPersonDeduction consolidatedDeduction = consolidateDetail(deduction, deductionRowNum);
            consolidatedDeductionsList.add(consolidatedDeduction);
            deductionRowNum++;
        }
        consolidatePerson.setDeductions(consolidatedDeductionsList);

        //Авансы
        List<NdflPersonPrepayment> consolidatedPrepaymentsList = new ArrayList<NdflPersonPrepayment>();
        for (NdflPersonPrepayment prepayment : prepayments) {
            NdflPersonPrepayment consolidatedPrepayment = consolidateDetail(prepayment, prepaymentRowNum);
            consolidatedPrepaymentsList.add(consolidatedPrepayment);
            prepaymentRowNum++;
        }
        consolidatePerson.setPrepayments(consolidatedPrepaymentsList);

        ndflPersonService.save(consolidatePerson)
        ndflPersonNum++

    }

    logger.info(String.format("Консолидация завершена, новых записей создано: %d", (ndflPersonNum - 1)));

}

String getVal(Map<String, RefBookValue> refBookPersonRecord, String attrName) {
    RefBookValue refBookValue = refBookPersonRecord.get(attrName);
    if (refBookValue != null) {
        return refBookValue.toString();
    } else {
        return null;
    }
}

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
 * Получить "ДУЛ"
 * @return
 */
Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataIdList(List<Long> declarationDataIdList) {
    def result = [:]
    declarationDataIdList.each {
        def personsDocumentsMap = getActualRefDulByDeclarationDataId(it)
        personsDocumentsMap.each { personId, refBookValue ->
            result.put(personId, refBookValue)
        }
    }
    return result
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
def getRefDulByDeclarationDataId(def long declarationDataId) {
    if (dulCache.isEmpty()) {
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
 * Получить список актуальных записей о физлицах, в нф
 * @param declarationDataIdList список id нф
 * @return
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataIdList(List<Long> declarationDataIdList) {
    //Если исходных форм достаточно много то можно переделать запрос на получение списка declarationDataIdList
    def result = [:]
    declarationDataIdList.each {
        Map mapPersons = personRecordIdToValuesMap = getActualRefPersonsByDeclarationDataId(it)
        mapPersons.each { recordId, refBookValue ->
            result.put(recordId, refBookValue)
        }
    }
    return result
}

/**
 * Получить "Физические лица"
 * NDFL_PERSON.PERSON_ID будет ссылаться на актуальную записи справочника ФЛ только после проведения расчета
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersonsByDeclarationDataId(def long declarationDataId) {
    String whereClause = String.format("id in (select person_id from ndfl_person where declaration_data_id = %s)", declarationDataId)
    return getRefBookByRecordWhere(REF_BOOK_PERSON_ID, whereClause)
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
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param ndflPersonList
 * @return
 */
def collectRefBookPersonIds(List<NdflPerson> ndflPersonList) {
    Set<Long> personIdSet = new HashSet<Long>();
    for (NdflPerson ndflPerson : ndflPersonList) {
        if (ndflPerson.personId != null) {
            personIdSet.add(ndflPerson.personId);
        } else {
            throw new ServiceException("Не указан параметр 'Идентификатор ФЛ', необходимо выполнить расчет налоговой формы c id=" + ndflPerson.declarationDataId)
        }
    }
    return new ArrayList<Long>(personIdSet);
}

/**
 * Создает объект NdlPerson заполненный данными из справочника
 */
def buildNdflPerson(Map<String, RefBookValue> personRecord, Map<String, RefBookValue> identityDocumentRecord, Map<String, RefBookValue> addressRecord) {

    Map<Long, String> countryCodes = getRefCountryCode();
    Map<Long, Map<String, RefBookValue>> documentTypeRefBook = getRefDocumentType();
    Map<Long, String> taxpayerStatusCodes = getRefTaxpayerStatusCode();

    NdflPerson ndflPerson = new NdflPerson()

    //Данные о физлице - заполняется на основе справочника физлиц
    ndflPerson.personId = personRecord.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue() //Идентификатор ФЛ
    ndflPerson.inp = personRecord.get("RECORD_ID")?.getNumberValue()
    ndflPerson.snils = personRecord.get("SNILS")?.getStringValue()
    ndflPerson.lastName = personRecord.get("LAST_NAME")?.getStringValue()
    ndflPerson.firstName = personRecord.get("FIRST_NAME")?.getStringValue()
    ndflPerson.middleName = personRecord.get("MIDDLE_NAME")?.getStringValue()
    ndflPerson.birthDay = personRecord.get("BIRTH_DATE")?.getDateValue()
    ndflPerson.innNp = personRecord.get("INN")?.getStringValue()
    ndflPerson.innForeign = personRecord.get("INN_FOREIGN")?.getStringValue()


    Long countryId = personRecord.get("CITIZENSHIP")?.getReferenceValue();

    ndflPerson.citizenship = countryCodes.get(countryId)

    //ДУЛ - заполняется на основе справочника Документы, удостоверяющие личность
    Map<String, RefBookValue> docTypeRecord = documentTypeRefBook.get(identityDocumentRecord.get("DOC_ID")?.getReferenceValue())
    ndflPerson.idDocType = docTypeRecord?.get("CODE")?.getStringValue();

    ndflPerson.idDocNumber = identityDocumentRecord.get("DOC_NUMBER")?.getStringValue()

    ndflPerson.status = taxpayerStatusCodes.get(personRecord.get("TAXPAYER_STATE")?.getReferenceValue())

    //адрес может быть не задан
    if (addressRecord != null){
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
def consolidateDetail(ndflPersonDetail, rowNum) {
    def sourceId = ndflPersonDetail.id;
    ndflPersonDetail.id = null
    ndflPersonDetail.ndflPersonId = null
    ndflPersonDetail.rowNum = rowNum
    ndflPersonDetail.sourceId = sourceId;
    return ndflPersonDetail
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

    logger.info(String.format("НФ-источников выбрано для консолидации: " + i + calcTimeMillis(time)))

    return result;
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
    return result
}

/**
 * Найти все NdflPerson привязанные к НФ вместе с данными о доходах
 * @param declarationDataId
 * @return
 */
List<NdflPerson> findNdflPersonWithData(Long declarationDataId) {

    List<NdflPerson> result = new ArrayList<NdflPerson>();
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationDataId);

    Map<Long, List<NdflPersonOperation>> imcomesList = mapToPesonId(ndflPersonService.findNdflPersonIncome(declarationDataId));
    Map<Long, List<NdflPersonOperation>> deductionList = mapToPesonId(ndflPersonService.findNdflPersonDeduction(declarationDataId));
    Map<Long, List<NdflPersonOperation>> prepaymentList = mapToPesonId(ndflPersonService.findNdflPersonPrepayment(declarationDataId));

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
 * Объединение ndfl-person по record_id, в данном методе происходит объединение ФЛ по record id из одной или нескольких НФ
 * @param refBookPerson
 * @return
 */
Map<Long, NdflPerson> consolidateNdflPerson(List<NdflPerson> ndflPersonList, List<Long> declarationDataIdList) {

    Map<Long, NdflPerson> result = new TreeMap<Long, NdflPerson>();

    for (NdflPerson ndflPerson : ndflPersonList) {

        Long personRecordId = ndflPerson.recordId;

        NdflPerson consNdflPerson = result.get(personRecordId)

        //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
        if (consNdflPerson == null) {
            consNdflPerson = new NdflPerson();
            consNdflPerson.recordId = personRecordId;
            result.put(personRecordId, consNdflPerson);
        }

        consNdflPerson.incomes.addAll(ndflPerson.incomes);
        consNdflPerson.deductions.addAll(ndflPerson.deductions);
        consNdflPerson.prepayments.addAll(ndflPerson.prepayments);

    }

    return result;
}

Map<Long, List<NdflPersonOperation>> mapToPesonId(List<NdflPersonOperation> operationList) {
    Map<Long, List<NdflPersonOperation>> result = new HashMap<Long, List<NdflPersonOperation>>()
    for (NdflPersonOperation personOperation : operationList) {
        Long ndflPersonId = personOperation.getNdflPersonId();
        if (!result.containsKey(ndflPersonId)) {
            result.put(ndflPersonId, new ArrayList<NdflPersonOperation>());
        }
        result.get(ndflPersonId).add(personOperation);
    }
    return result;
}

/**
 * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
 * @param personMap
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    Map<Long, Map<String, RefBookValue>> addressMap = Collections.emptyMap();
    def addressIds = [];
    personMap.each { recordId, person ->
        if (person.get("ADDRESS").value != null) {
            Long addressId = person.get("ADDRESS")?.getReferenceValue();
            //адрес может быть не задан
            if (addressId != null) {
                addressIds.add(addressId);
            }
        }
    }

    if (addressIds.size() > 0) {
        addressMap = getRefAddress(addressIds)
    }
    return addressMap;
}

/**
 * Получить актуальные на дату окончания отчетного периода, записи справочника физлиц по record_id
 * @param personRecordIds
 * @return
 */
Long getActualPersonId(Long recordId) {
    Date version = getReportPeriodEndDate() - 1;
    List<Long> personIds = getProvider(RefBook.Id.PERSON.getId()).getUniqueRecordIds(version, "RECORD_ID = " + recordId);
    if (personIds.isEmpty()) {
        logger.error("Ошибка при получении записи из справочника 'Физические лица' с recordId=" + recordId);
        return null;
    } else {
        return personIds.get(0);
    }
}

/**
 * Создание фиктивной xml для привязки к экземпляру
 * declarationDataId
 * @return
 */
def generateXml() {
    def builder = new MarkupBuilder(xml)
    builder.Файл(имя: declarationData.id)
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

//>------------------< GET SOURCES >----------------------<

/**
 * Система (замена шага 1 ОС для целевого решения):
 * Ищет и включает в КНФ данные налоговых форм, у которых:
 * Вид = РНУ НДФЛ (первичная)
 * Состояние = "Принята"
 * Подразделением = КНФ.Подразделение.
 * Период = КНФ.Период
 * Далее без изменений по сравнению с целевым решением
 * @return
 */
def getSourcesListForTemporarySolution() {

    if (!needSources) {
        return
    }

    //отчетный период в котором выполняется консолидация
    ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения по которому формируется консолидированная форма
    def parentDepartmentId = declarationData.departmentId

    Department department = departmentService.get(parentDepartmentId)

    println "department="+department

    List<DeclarationData> declarationDataList = findConsolidateDeclarationData(parentDepartmentId, declarationDataReportPeriod.id)

    for (DeclarationData declarationData : declarationDataList) {
        //Формируем связь источник-приемник
        def relation = getRelation(declarationData, department, declarationDataReportPeriod)
        sources.sourceList.add(relation)
    }

    sources.sourcesProcessedByScript = true
    //logger.info("sources found: " + sources.sourceList.size)
}



/**
 * Получить набор НФ источников события FormDataEvent.GET_SOURCES.
 *
 * Ищет и включает в КНФ данные налоговых форм, у которых:
 *      Подразделение является подчиненным по отношению к ТБ (уточнить у заказчика - включая сам ТБ?) согласно справочнику подразделений.
 *      Вид = РНУ НДФЛ (первичная)
 *      Состояние = "Принята"
 *      Отчетный период = Отчетный период КНФ или любой предыдущий отчетный период в рамках одного года.
 */
def getSourcesList() {

    if (!needSources) {
        return
    }

    //отчетный период в котором выполняется консолидация
    ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения по которому формируется консолидированная форма
    def parentDepartmentId = declarationData.departmentId

    //Подразделения которые является подчиненным по отношению к ТБ, включая сам ТБ
    List<Department> departments = departmentService.getAllChildren(parentDepartmentId)

    //Список отчетных периодов которые должны быть включены в консолидированную форму (1 квартал, полугодие, 9 месяцев, год)
    List<ReportPeriod> reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.NDFL, declarationDataReportPeriod.startDate, declarationDataReportPeriod.endDate)

    for (Department department : departments) {

        for (ReportPeriod primaryReportPeriod : reportPeriodList) {

            List<DeclarationData> declarationDataList = findConsolidateDeclarationData(department.id, primaryReportPeriod.id)

            for (DeclarationData declarationData : declarationDataList) {
                //Формируем связь источник-приемник
                def relation = getRelation(declarationData, department, primaryReportPeriod)
                sources.sourceList.add(relation)
            }
        }
    }
    sources.sourcesProcessedByScript = true
    //logger.info("sources found: " + sources.sourceList.size)
}

/**
 * Ищет и включает в КНФ данные налоговых форму которых:
 * Вид = РНУ НДФЛ (первичная),
 * Подразделение является подчиненным по отношению к ТБ согласно справочнику подразделений.
 * Отчетный период = Отчетный период КНФ или любой предыдущий отчетный период в рамках одного года.
 * Если в некорректирующем и корректирующем (корректирующих) периодах, относящихся к одному отчетному периоду, найдены группы (множества, наборы) ПНФ с совпадающими параметрами: "Подразделение" И "АСНУ":
 * Система включает в КНФ множество ПНФ, относящихся к периоду с наиболее старшим периодом сдачи корректировки
 */
List<DeclarationData> findConsolidateDeclarationData(departmentId, reportPeriodId) {

    //Список отчетных периодов подразделения
    List<DepartmentReportPeriod> departmentReportPeriodList = new ArrayList<DepartmentReportPeriod>();
    List<DeclarationData> allDeclarationDataList = declarationService.findAllDeclarationData(PRIMARY_RNU_NDFL_TEMPLATE_ID, departmentId, reportPeriodId);

    //Разбивка НФ по АСНУ и отчетным периодам <АСНУ, <Период, <Список НФ созданных в данном периоде>>>
    Map<Long, Map<Integer, List<DeclarationData>>> asnuDataMap = new HashMap<Long, HashMap<Integer, List<DeclarationData>>>();
    for (DeclarationData declarationData : allDeclarationDataList) {
        Long asnuId = declarationData.getAsnuId();
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData?.departmentReportPeriodId) as DepartmentReportPeriod;
        Integer departmentReportPeriodId = departmentReportPeriod.getId();
        departmentReportPeriodList.add(departmentReportPeriod);
        if (asnuId != null) {
            Map<Integer, List<DeclarationData>> asnuMap = asnuDataMap.get(asnuId);
            if (asnuMap == null) {
                asnuMap = new HashMap<Long, DeclarationData>();
                asnuDataMap.put(asnuId, asnuMap);
            }
            List<DeclarationData> declarationDataList = asnuMap.get(departmentReportPeriodId);
            if (declarationDataList == null) {
                declarationDataList = new ArrayList<DeclarationData>();
                asnuMap.put(departmentReportPeriodId, declarationDataList);
            }

            declarationDataList.add(declarationData);
        } else {
            logger.warn("Найдены НФ для которых не заполнено поле АСНУ. Подразделение: " + getDepartmentFullName(departmentId) + ", отчетный период: " + reportPeriodId + ", id: " + declarationData.id);
        }
    }

    //Сортировка "Отчетных периодов" в порядке: Кор.период 1, Кор.период 2, некорректирующий период (не задана дата корректировки)
    departmentReportPeriodList.sort { a, b -> departmentReportPeriodComp(a, b) }

    //Включение в результат НФ с наиболее старшим периодом сдачи корректировки
    List<DeclarationData> result = new ArrayList<DeclarationData>();
    for (Map.Entry<Long, Map<Integer, List<DeclarationData>>> entry : asnuDataMap.entrySet()) {
        //Long asnuId = entry.getKey();
        Map<Long, List<DeclarationData>> asnuDeclarationDataMap = entry.getValue();
        List<DeclarationData> declarationDataList = getLast(asnuDeclarationDataMap, departmentReportPeriodList)
        result.addAll(declarationDataList);
    }
    return result;
}

/**
 * Возвращает список НФ по одной АСНУ, относящихся к периоду с наиболее старшим периодом сдачи корректировки
 * @param declarationDataMap НФ разбитые по периодам
 * @param departmentReportPeriodList список периодов, отстортированный по убыванию даты сдачи корректировки, null last
 * @return список НФ созданный АСНУ в старшем отчетном периоде
 */
List<DeclarationData> getLast(Map<Integer, List<DeclarationData>> declarationDataMap, List<DepartmentReportPeriod> departmentReportPeriodList) {
    for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
        Integer departmentReportPeriodId = departmentReportPeriod.getId()
        if (declarationDataMap.containsKey(departmentReportPeriodId)) {
            return declarationDataMap.get(departmentReportPeriodId)
        }
    }
    return Collections.emptyList();
}

def departmentReportPeriodComp(DepartmentReportPeriod a, DepartmentReportPeriod b) {

    if (a.getCorrectionDate() == null && b.getCorrectionDate() == null) {
        return b.getId().compareTo(a.getId());
    }

    if (a.getCorrectionDate() == null) {
        return 1;
    }

    if (b.getCorrectionDate() == null) {
        return -1;
    }

    int comp = b.getCorrectionDate().compareTo(a.getCorrectionDate());

    if (comp != 0) {
        return comp;
    }

    return b.getId().compareTo(a.getId());
}

/**
 * Получить запись для источника-приемника.
 *
 * @param declarationData первичная форма
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(DeclarationData declarationData, Department department, ReportPeriod period) {

    Relation relation = new Relation()

    //Привязка отчетных периодов к подразделениям
    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData?.departmentReportPeriodId) as DepartmentReportPeriod

    //Макет НФ
    DeclarationTemplate declarationTemplate = getDeclarationTemplateById(declarationData?.declarationTemplateId)

    def isSource = (declarationTemplate.id == PRIMARY_RNU_NDFL_TEMPLATE_ID)
    ReportPeriod rp = departmentReportPeriod.getReportPeriod();

    if (light) {
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
            resultReportParameters.put(key, value)
        }
    }

    // Ограничение числа выводимых записей
    int startIndex = 1
    int pageSize = 100

    PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

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
    switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
        case 'rnu_ndfl_person_db': createSpecificReportPersonDb();
            break;
        case 'report_kpp_oktmo':
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


String firstChar(String str){
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

/**
 * Проверки которые относятся только к консолидированной
 * @return
 */
def checkDataConsolidated(){

    // Общ12
    if (FORM_DATA_KIND.equals(FormDataKind.CONSOLIDATED)) {
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
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]
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

// Страны Мапа <Идентификатор, Код>

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

// ИНП Мапа <person_id, массив_инп>
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
 *
 * @return
 */
def getRefPersons() {
    if (personsCache.size() == 0) {
        throw new ServiceException("Не проинициализированны данные кэша справочника 'Физических лиц'!")
    }
    return personsCache;
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
    def refBookMapResult = [:]
    refBookMap.each { personId, refBookValue ->
        Long refBookRecordId = refBookValue.get(RF_RECORD_ID).value
        refBookMapResult.put(refBookRecordId, refBookValue)
    }
    return refBookMapResult
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
            def inpList = inpActualCache.get(refBook?.PERSON_ID?.referenceValue)
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
Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataId(declarationDataId) {
    if (dulActualCache.isEmpty()) {
        String whereClause = """
            JOIN ref_book_person p ON (frb.person_id = p.id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
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

//>------------------< UTILS >----------------------<

// Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
@Field final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
@Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
@Field final String MESSAGE_ERROR_ABSENT = " Отсутсвуют строки:"


@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" размером %d."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" в колличестве %d."

// Таблицы
@Field final String T_PERSON = "Реквизиты"
@Field final String T_PERSON_INCOME = "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION = "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT = "Сведения о доходах в виде авансовых платежей"

// Справочники
@Field final String R_FIAS = "КЛАДР"
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
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeKindMap.size())

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
    def personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
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
    def dulMap = getActualRefDulByDeclarationDataId(declarationData.id)
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

    //поиск всех адресов формы в справочнике ФИАС
    time = System.currentTimeMillis();
    Map<Long, Long> checkFiasAddressMap = getFiasAddressIdsMap();
    logger.info(SUCCESS_GET_TABLE, R_FIAS, checkFiasAddressMap.size());
    println "Проверки на соответствие справочникам / Выгрузка справочника " + R_FIAS + ": " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / Выгрузка справочника " + R_FIAS + ": (" + (System.currentTimeMillis() - time) + " ms)");

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
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Графы ${address.join(", ")} . $fioAndInp." +
                    " Текст ошибки: 'Адрес регистрации в Российской Федерации' не соответствует справочнику '$R_FIAS'.")
        }
        timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Гражданство (код страны) (Графа 7)='${ndflPerson.citizenship}' . $fioAndInp." +
                    " Текст ошибки: 'Гражданство (код страны) (Графа 7)' не соответствует справочнику '$R_CITIZENSHIP'.")
        }

        // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
        if (!documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}' . $fioAndInp." +
                    " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)' не соответствует справочнику '$R_ID_DOC_TYPE'.")
        }

        // Спр4 Статус (Обязательное поле)
        if (!taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Cтатус (Графа 12)='${ndflPerson.status}' . $fioAndInp." +
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
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Фамилия (Графа 3)='${ndflPerson.lastName}' . $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Фамилия (Графа 3)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр11 Имя (Обязательное поле)
                if (!ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Имя (Графа 4)='${ndflPerson.firstName}' . $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Имя (Графа 4)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр11 Отчество (Необязательное поле)
                if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Отчество (Графа 5)='${ndflPerson.middleName}' . $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Отчество (Графа 5)' не соответствует справочнику '$R_PERSON'.")
                }

                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // Спр12 ИНП первичная (Обязательное поле)
                    def inpList = inpMap.get(personRecord.get("id")?.value)
                    if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Уникальный код клиента (Графа 2)='${ndflPerson.inp}' . $fioAndInp." +
                                " Текст ошибки: 'Уникальный код клиента (Графа 2)' не соответствует справочнику '$R_INP'.")
                    }
                } else {
                    //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                    //if (formType == CONSOLIDATE){}
                    String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                    if (!ndflPerson.inp.equals(recordId)) {
                        //TODO turn_to_error
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Уникальный код клиента (Графа 2)='${ndflPerson.inp}' . $fioAndInp." +
                                " Текст ошибки: 'Уникальный код клиента (Графа 2)' не соответствует справочнику '$R_INP'.")
                    }
                }

                // Спр13 Дата рождения (Обязательное поле)
                if (!ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Налогоплательщик.Дата рождения (Графа 6)='${ndflPerson.birthDay}' . $fioAndInp." +
                            " Текст ошибки: 'Налогоплательщик.Дата рождения (Графа 6)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр14 Гражданство (Обязательное поле)
                def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                if (!ndflPerson.citizenship.equals(citizenship)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Гражданство (код страны) (Графа 7)='${ndflPerson.citizenship}' . $fioAndInp." +
                            " Текст ошибки: 'Гражданство (код страны) (Графа 7)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр15 ИНН.В Российской федерации (Необязательное поле)
                if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В Российской федерации (Графа 8)='${ndflPerson.innNp}' . $fioAndInp." +
                            " Текст ошибки: 'ИНН.В Российской федерации (Графа 8)' не соответствует справочнику '$R_PERSON'.")
                }

                // Спр16 ИНН.В стране гражданства (Необязательное поле)
                if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В стране гражданства (Графа 9)='${ndflPerson.innForeign}' . $fioAndInp." +
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
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}' . $fioAndInp." +
                                " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)' не соответствует справочнику '$R_DUL'.")
                    }
                    if (!personDocNumberList.contains(ndflPerson.idDocNumber)) {
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}' . $fioAndInp." +
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
                        logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Код (Графа 10)='${ndflPerson.idDocType}', Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}' . $fioAndInp." +
                                " Текст ошибки: 'Документ удостоверяющий личность.Код (Графа 10)', 'Документ удостоверяющий личность.Номер (Графа 11)' не соответствует справочнику '$R_DUL'.")
                    } else {
                        int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                        if (incRep != 1) {
                            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Документ удостоверяющий личность.Номер (Графа 11)='${ndflPerson.idDocNumber}' . $fioAndInp." +
                                    " Текст ошибки: 'Документ удостоверяющий личность.Номер (Графа 11)' не включается в отчетность.")
                        }
                    }
                }

                // Спр18 Статус налогоплательщика (Обязательное поле)
                def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                if (!ndflPerson.status.equals(taxpayerStatus)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Cтатус (Графа 12)='${ndflPerson.status}' . $fioAndInp." +
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
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)='${ndflPerson.regionCode}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Район
                if (!ndflPerson.area.equals(area)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Район (Графа 15)='${ndflPerson.area}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Район (Графа 15)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Город
                if (!ndflPerson.city.equals(city)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Город (Графа 16)='${ndflPerson.city}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Город (Графа 16)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Населенный пункт
                if (!ndflPerson.locality.equals(locality)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)='${ndflPerson.locality}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Улица
                if (!ndflPerson.street.equals(street)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Улица (Графа 18)='${ndflPerson.street}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Улица (Графа 18)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Дом
                if (!ndflPerson.house.equals(house)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Дом (Графа 19)='${ndflPerson.house}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Дом (Графа 19)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Корпус
                if (!ndflPerson.building.equals(building)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Корпус (Графа 20)='${ndflPerson.building}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Корпус (Графа 20)' не соответствует справочнику '$R_PERSON'.")
                }

                // Адрес регистрации в Российской Федерации.Квартира
                if (!ndflPerson.flat.equals(flat)) {
                    logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. Адрес регистрации в Российской Федерации.Квартира (Графа 21)='${ndflPerson.flat}' . $fioAndInp." +
                            " Текст ошибки: 'Адрес регистрации в Российской Федерации.Квартира (Графа 21)' не соответствует справочнику '$R_PERSON'.")
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

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find { key, value -> value == ndflPersonIncome.incomeCode }) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Код (Графа 4)='${ndflPersonIncome.incomeCode}' . $fioAndInp." +
                    " Текст ошибки: 'Доход.Вид.Код (Графа 4)' не соответствует справочнику '$R_INCOME_CODE'.")
        }

        // Спр6 Доход.Вид.Признак (Необязательное поле)
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (ndflPersonIncome.incomeType != null) {
            def idIncomeCode = incomeKindMap.get(ndflPersonIncome.incomeType)
            if (!idIncomeCode) {
                logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Признак (Графа 5)='${ndflPersonIncome.incomeType}' . $fioAndInp." +
                        " Текст ошибки: 'Доход.Вид.Признак (Графа 5)' не соответствует справочнику '$R_INCOME_TYPE'.")
            } else if (!incomeCodeMap.get(idIncomeCode.value)) {
                logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. Доход.Вид.Признак (Графа 5)='${ndflPersonIncome.incomeType}' . $fioAndInp." +
                        " Текст ошибки: 'Доход.Вид.Признак (Графа 5)' не соответствует справочнику '$R_INCOME_TYPE'.")
            }
        }

        // Спр7 НДФЛ.Процентная ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate.toString())) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_INCOME}'. Строка ${ndflPersonIncome.rowNum ?: ""}. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate}' . $fioAndInp." +
                    " Текст ошибки: 'НДФЛ.Процентная ставка (Графа 14)' не соответствует справочнику '$R_RATE'.")
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonIncome: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonIncome: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_DEDUCTION}'. Строка ${ndflPersonDeduction.rowNum ?: ""}. Код вычета (Графа 3)='${ndflPersonDeduction.typeCode}' . $fioAndInp." +
                    " Текст ошибки: 'Код вычета (Графа 3)' не соответствует справочнику '$R_TYPE_CODE'.")
        }

        // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_DEDUCTION}'. Строка ${ndflPersonDeduction.rowNum ?: ""}. Документ о праве на налоговый вычет.Код источника (Графа 7)='${ndflPersonDeduction.notifSource}' . $fioAndInp." +
                    " Текст ошибки: 'Документ о праве на налоговый вычет.Код источника (Графа 7)' не соответствует справочнику '$R_NOTIF_SOURCE'.")
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonDeduction: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonDeduction: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
        // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON_PREPAYMENT}'. Строка ${ndflPersonPrepayment.rowNum ?: ""}. Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)='${ndflPersonPrepayment.notifSource}' . $fioAndInp." +
                    " Текст ошибки: 'Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)' не соответствует справочнику '$R_NOTIF_SOURCE'.")
        }
    }
    println "Проверки на соответствие справочникам / NdflPersonPrepayment: " + (System.currentTimeMillis() - time);
    logger.info("Проверки на соответствие справочникам / NdflPersonPrepayment: (" + (System.currentTimeMillis() - time) + " ms)");
}

/**
 * Общие проверки
 */
def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList) {

    // Параметры подразделения
    def mapRefBookNdfl = getRefBookNdfl()
    def mapRefBookNdflDetail = getRefBookNdflDetail(mapRefBookNdfl.id)

    long time = System.currentTimeMillis();
    for (NdflPerson ndflPerson : ndflPersonList) {

        ScriptUtils.checkInterrupted();

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        println "ndflPerson.rowNum=" + ndflPerson.rowNum

        // Общ1 Корректность ИНН (Необязательное поле)
        if (ndflPerson.innNp != null && !ScriptUtils.checkControlSumInn(ndflPerson.innNp)) {
            //TODO turn_to_error
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. ИНН.В Российской федерации (Графа 8)='${ndflPerson.innNp}' . $fioAndInp." +
                    " Текст ошибки: Некорректное значение 'ИНН.В Российской федерации (Графа 8)'.")
        }

        // Общ11 СНИЛС (Необязательное поле)
        if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
            logger.warn("Ошибка в значении: Раздел '${T_PERSON}'. Строка ${ndflPerson.rowNum ?: ""}. СНИЛС='${ndflPerson.snils}' . $fioAndInp." +
                    " Текст ошибки: Некорректное значение 'СНИЛС'.")
        }
    }
    println "Общие проверки / NdflPerson: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / NdflPerson: (" + (System.currentTimeMillis() - time) + " ms)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        ScriptUtils.checkInterrupted();

        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

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
                    //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                    //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_INCOME_CODE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
                // Раздел 2. Графа 5 "Доход.Вид.Признак" должна быть заполнена
                if (ndflPersonIncome.incomeType == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [script.this.C_INCOME_TYPE, emptyField])
                    //TODO turn_to_error
                    //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                    //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", script.this.C_INCOME_TYPE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }

            // если НЕ заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate == null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                emptyField += ", \"" + C_INCOME_PAYOUT_DATE + "\", \"" + C_INCOME_PAYOUT_SUMM + "\""

                // Раздел 2. Графа 21 "НДФЛ.Перечисление в бюджет.Срок" должна быть НЕ заполнена
                if (ndflPersonIncome.taxTransferDate != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }

            // 	Раздел 2. Графа 13 "Налоговая база" должна быть заполнена
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxBase)) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_BASE, emptyField])
                //TODO turn_to_error
                //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 14 "НДФЛ.Процентная ставка" должна быть заполнена
            if (ndflPersonIncome.taxRate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_RATE, emptyField])
                //TODO turn_to_error
                //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 15 "НДФЛ.Расчет.Дата" должна быть заполнена
            if (ndflPersonIncome.taxDate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_DATE, emptyField])
                //TODO turn_to_error
                //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнены Раздел 2. Графы 22-24
            def notEmptyField = "заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // Раздел 2. Графа 12 "Сумма вычета" должна быть НЕ заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TOTAL_DEDUCTIONS_SUMM, notEmptyField])
                //TODO turn_to_error
                //https://jira.aplana.com/browse/SBRFNDFL-581 Не должны выполняться вычеркнутые проверки, остальные проверки аналогичны целевому решению
                //logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }

            // если заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {

                // Раздел 2. Графа 21 "НДФЛ.Перечисление в бюджет.Срок" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 6 "Доход.Дата.Начисление"
        if (ndflPersonIncome.incomeAccruedDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_DATE])

            // Раздел 2. Графа 10 "Доход.Сумма.Начисление" должна быть заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "НДФЛ.Расчет.Сумма.Исчисленный" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_CALCULATED_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "НДФЛ.Расчет.Сумма.Не удержанный" должна быть заполнена
                if (ndflPersonIncome.notHoldingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_NOT_HOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "НДФЛ.Расчет.Сумма.Излишне удержанный" должна быть заполнена
                if (ndflPersonIncome.overholdingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_OVERHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            } else {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_INCOME_ACCRUED_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнена Раздел 2. Графа 10 "Доход.Сумма.Начисление"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 6 "Доход.Дата.Начисление" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                def emptyField = "не заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "НДФЛ.Расчет.Сумма.Исчисленный" должна быть НЕ заполнена
                if (!ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_CALCULATED_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "НДФЛ.Расчет.Сумма.Не удержанный" должна быть НЕ заполнена
                if (ndflPersonIncome.notHoldingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_NOT_HOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "НДФЛ.Расчет.Сумма.Излишне удержанный" должна быть НЕ заполнена
                if (ndflPersonIncome.overholdingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_OVERHOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 7 "Доход.Дата.Выплата"
        if (ndflPersonIncome.incomePayoutDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_DATE])

            // если заполнена Раздел 2. Графа 11 "Доход.Сумма.Выплата"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_PAYOUT_DATE, C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 17 "НДФЛ.Расчет.Сумма.Удержанный" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_WITHHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_WITHHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 20 "НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику" должна быть заполнена
                if (ndflPersonIncome.refoundTax == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_REFOUND_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_REFOUND_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 21 "НДФЛ.Перечисление в бюджет.Срок" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            } else {
                // Раздел 2. Графа 11 "Доход.Сумма.Выплата" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_INCOME_PAYOUT_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если не заполнена Раздел 2. Графа 7 "Доход.Дата.Выплата"

            // если заполнена Раздел 2. Графа 11 "Доход.Сумма.Выплата"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 7 "Доход.Дата.Выплата" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                // если НЕ заполнена Раздел 2. Графа 11 "Доход.Сумма.Выплата"

                if (ndflPersonIncome.paymentDate == null &&
                        ndflPersonIncome.paymentNumber == null &&
                        ndflPersonIncome.taxSumm == null) {

                    // Раздел 2. Графа 21 "НДФЛ.Перечисление в бюджет.Срок" должна быть НЕ заполнена
                    if (ndflPersonIncome.taxTransferDate != null) {
                        def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                        //TODO turn_to_error
                        logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                    }
                }
            }
        }

        // Раздел 2. Графа 22, 23, 24 Должны быть либо заполнены все 3 Графы, либо ни одна их них
        if (ndflPersonIncome.paymentDate != null || ndflPersonIncome.paymentNumber != null || !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
            def allField = getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])
            // Раздел 2. Графа 22 "НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
            if (ndflPersonIncome.paymentDate == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_DATE, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_PAYMENT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 23 "Номер платежного поручения перечисления налога в бюджет"
            if (ndflPersonIncome.paymentNumber == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_NUMBER, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_PAYMENT_NUMBER, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 24 "НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_TAX_SUMM, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "", C_TAX_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
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

    println "Общие проверки / NdflPersonIncome: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / NdflPersonIncome: (" + (System.currentTimeMillis() - time) + " ms)");

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

    println "Общие проверки / Проверки на отсутсвие повторений: " + (System.currentTimeMillis() - time);
    logger.info("Общие проверки / Проверки на отсутсвие повторений: (" + (System.currentTimeMillis() - time) + " ms)");
}

// Кэш для справочников
@Field def refBookCache = [:]

/**
 * Разыменование записи справочника
 */
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}' . НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate ?: ""}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 30» не выполнено ни одно из условий:\\n" +
                            " «Графа 12 Раздел 1» ≥ 2 и «Графа 4 Раздел 2» ≠ 1010\\n" +
                            " («Графа 4 Раздел 2» ≠ 2000 или 2001 или 2010) и «Графа 12 Раздел 1» > 2.")
                }
            } else if (ndflPersonIncome.taxRate == 9) {
                if (!(ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate}' $fioAndInp." +
                            " Текст ошибки: для «Графа 14 Раздел 2 = 9» не выполнено условие: «Графа 7 Раздел 1» = 643 и «Графа 4 Раздел 2» = 1110 и «Графа 12 Раздел 1» = 1.")
                }
            } else {
                if (!(ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    logger.warn("Ошибка в значении: Раздел '$T_PERSON_INCOME'. Строка='${ndflPersonIncome.rowNum ?: ""}'. НДФЛ.Процентная ставка (Графа 14)='${ndflPersonIncome.taxRate}' $fioAndInp." +
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
                && ndflPersonIncome.incomeAccruedDate.format("dd.MM.yyyy") == ndflPersonDeduction.incomeAccrued.format("dd.MM.yyyy")
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
    def dublList = inputList.findAll { inputList.count(it) > 1 }.unique()
    if (dublList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + dublList.sort().join(", ") + "."
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
    def absentList = []
    def sortList = inputList.unique().sort()
    if (sortList != null && sortList.size() > 0) {
        def i = sortList.get(0) == null ? 0 : sortList.get(0)
        sortList.each { item ->
            if (item != null) {
                if (item != i) {
                    absentList.add(i)
                }
                i++
            }
        }
    }
    def resultMsg = ""
    if (absentList.size() > 0) {
        resultMsg = " Раздел \"" + tableName + "\" № " + absentList.sort().join(", ") + "."
    }
    return resultMsg
}