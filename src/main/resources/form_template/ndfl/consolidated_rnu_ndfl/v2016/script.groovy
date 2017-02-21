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

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверить
        checkData()
        break
    case FormDataEvent.CALCULATE: //консолидирование с формированием фиктивного xml
        clearData()
        consolidation()
        generateXml()
        break
    case FormDataEvent.GET_SOURCES: //формирование списка ПНФ для консолидации
        getSourcesList()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT: //создание спецефичного отчета
        createSpecificReport();
        break
}

/**
 * Идентификатор шаблона РНУ-НДФЛ (первичная)
 */
@Field
def PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

//>------------------< CONSOLIDATION >----------------------<

/**
 * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
 * Данный метод выполняет вызов скрипта (GET-SOURCES) и
 *
 */
void consolidation() {

    def declarationDataId = declarationData.id

    //Возвращает список нф-источников для указанной декларации (включая несозданные)

    //декларация-приемник, true - заполнятся только текстовые данные для GUI и сообщений,true - исключить несозданные источники,ограничение по состоянию для созданных экземпляров
    //список нф-источников
    List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, true, false, null, userInfo, logger);

    logger.info(String.format("НФ-источников найдено: %d", sourcesInfo.size()))

    List<NdflPerson> ndflPersonList = collectNdflPersonList(sourcesInfo);

    logger.info(String.format("ФЛ в ПНФ найдено: %d", ndflPersonList.size()));

    if (logger.containsLevel(LogLevel.ERROR)) {
        throw new ServiceException("При получении источников возникли ошибки. Консолидация НФ не возможна.");
    }

    //Карта в которой хранится record_id и NdflPerson в котором объединяются данные о даходах
    SortedMap<Long, NdflPerson> ndflPersonMap = consolidateNdflPerson(ndflPersonList);

    logger.info(String.format("ФЛ в КНФ найдено: %d", ndflPersonMap.size()))

    //Актуализация первичного ключа запись, все одинаковые записи должны указывать на последнюю актуальную на данный момент версию
    println "consolidation start actualize"
    long time = System.currentTimeMillis();
    List<Long> personIds = new ArrayList<Long>();
    for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {
        Long record_id = entry.getKey();
        NdflPerson ndflPerson = entry.getValue();
        Long personId = getActualPersonId(record_id);
        personIds.add(personId);
        ndflPerson.personId = personId;
    }
    println "consolidation end actualize(" + (System.currentTimeMillis() - time) + ")"

    //-----<INITIALIZE_CACHE_DATA>-----
    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefPersons(personIds);
    Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPerson);
    //PersonId :  Документы
    Map<Long, List<Map<String, RefBookValue>>> identityDocMap = getRefDul(personIds)
    //-----<INITIALIZE_CACHE_DATA_END>-----

    //разделы в которых идет сплошная нумерация
    def ndflPersonNum = 1;
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {
        Long record_id = entry.getKey();

        NdflPerson ndflPerson = entry.getValue();

        def incomes = ndflPerson.incomes;
        def deductions = ndflPerson.deductions;
        def prepayments = ndflPerson.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
        prepayments.sort { a, b -> a.notifDate <=> b.notifDate }

        //Если объединяли одно ФЛ из разных версий то сдесь будет уже актуальная версия установленная на предыдущем шаге
        Long ndflPersonId = ndflPerson.personId;

        List<Map<String, RefBookValue>> identityDocList = identityDocMap.get(ndflPersonId)

        Map<String, RefBookValue> personRecord = refBookPerson.get(ndflPersonId);

        //Выбираем ДУЛ с признаком включения в отчетность 1
        Map<String, RefBookValue> identityDocumentRecord = identityDocList?.find {
            it.get("INC_REP")?.getNumberValue() == 1
        };

        Long addressId = personRecord.get("ADDRESS")?.getReferenceValue();
        Map<String, RefBookValue> addressRecord = addressMap.get(addressId);

        //List<Long> documentsIds = documentProvider.getUniqueRecordIds(null, "PERSON_ID = " + personId + " AND INC_REP = 1");
        if (identityDocumentRecord == null || identityDocumentRecord.isEmpty()) {
            logger.error(String.format("В справочнике 'Документы, удостоверяющие личность' отсутствуют данные о документах для физлица с id: \"%s\", и признаком включения в отчетность: 1", ndflPersonId))
            continue;
        }

        if (addressId != null && addressRecord == null) {
            logger.error(String.format("В справочнике 'Адреса физических лиц' отсутствуют данные для записи с id: \"%d\"", ndflPersonId))
            continue;
        }

        //Создание консолидированной записи NdflPerson
        def consolidatePerson = buildNdflPerson(ndflPerson, personRecord, identityDocumentRecord, addressRecord);

        consolidatePerson.rowNum = ndflPersonNum;
        consolidatePerson.declarationDataId = declarationDataId
        consolidatePerson.incomes = incomes.withIndex().collect { detail, i -> consolidateDetail(detail, incomesRowNum) }

        consolidatePerson.deductions = deductions.withIndex().collect { detail, i -> consolidateDetail(detail, deductionRowNum) }
        consolidatePerson.prepayments = prepayments.withIndex().collect { detail, i -> consolidateDetail(detail, prepaymentRowNum) }

        ndflPersonService.save(consolidatePerson)
        ndflPersonNum++

    }

    logger.info(String.format("Консолидация завершена, новых записей создано: %d", (ndflPersonNum - 1)));

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
def buildNdflPerson(NdflPerson currentNdflPerson, Map<String, RefBookValue> personRecord, Map<String, RefBookValue> identityDocumentRecord, Map<String, RefBookValue> addressRecord) {

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
    ndflPerson.additionalData = currentNdflPerson.additionalData
    return ndflPerson
}

/**
 * При
 * @param ndflPersonDetail
 * @param i
 * @return
 */
def consolidateDetail(ndflPersonDetail, i) {
    def sourceId = ndflPersonDetail.id;
    ndflPersonDetail.id = null
    ndflPersonDetail.ndflPersonId = null
    ndflPersonDetail.rowNum = i
    ndflPersonDetail.sourceId = sourceId;
    i++;

    return ndflPersonDetail
}

/**
 * Получаем список NdflPerson которые попадут в консолидированную форму
 * @param sourcesInfo
 * @return
 */
List<NdflPerson> collectNdflPersonList(List<Relation> sourcesInfo) {
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
        result.addAll(ndflPersonList);
        i++;
    }

    logger.info(String.format("НФ-источников выбрано для консолидации: %d", i))

    return result;
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
 * Объединение ndfl-person по record_id
 * @param refBookPerson
 * @return
 */
Map<Long, NdflPerson> consolidateNdflPerson(List<NdflPerson> ndflPersonList) {

    //список уникальных id физлиц из справочника
    List<Long> personIds = collectRefBookPersonIds(ndflPersonList)

    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefBookByRecordIds(RefBook.Id.PERSON.getId(), personIds)

    Map<Long, NdflPerson> result = new TreeMap<Long, NdflPerson>();

    for (NdflPerson ndflPerson : ndflPersonList) {

        //Ссылка на справочник физлиц
        Long personId = ndflPerson.personId

        Map<String, RefBookValue> refBookRecord = refBookPerson.get(personId);

        if (refBookRecord == null) {
            throw new ServiceException("В справочнике 'Физические лица' не найдена запись с id='" + personId);
        }

        Long recordId = refBookRecord.get("RECORD_ID")?.getNumberValue()?.longValue();

        if (refBookRecord == null) {
            throw new ServiceException("В справочнике 'Физические лица' отсутствует значение атрибута 'Идентификатор ФЛ' для записи с id=" + personId);
        }

        NdflPerson consNdflPerson = result.get(recordId)

        //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
        if (consNdflPerson == null) {
            consNdflPerson = new NdflPerson();
            consNdflPerson.personId = personId;
            result.put(recordId, consNdflPerson)
        }

        consNdflPerson.incomes.addAll(ndflPerson.incomes)
        consNdflPerson.deductions.addAll(ndflPerson.deductions)
        consNdflPerson.prepayments.addAll(ndflPerson.prepayments)

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
    personMap.each { personId, person ->
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
    if (personIds.size() != 1) {
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

//>------------------< CREATE SPECIFIC REPORT >----------------------<

def createSpecificReport() {
    def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.getSubreportParamValues().each { k, v ->
        writer.write(k + "::" + v + "\n")
    }
    writer.close()
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".txt")
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
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]

//Коды Асну
@Field Map<Long, String> asnuCache = [:]

//Коды стран из справочника
@Field Map<Long, String> countryCodeCache = [:]

//Виды документов, удостоверяющих личность
@Field Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
@Field Map<Long, String> documentTypeCodeCache = [:]

//Коды статуса налогоплатильщика
@Field Map<Long, String> taxpayerStatusCodeCache = [:]

//Адреса физлиц
@Field Map<Long, Map<String, RefBookValue>> addressCache = [:]
//<person_id:  list<id: <record>>>
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulCache = [:]

@Field def sourceReportPeriod = null

@Field Map<Long, DepartmentReportPeriod> departmentReportPeriodMap = [:]

@Field Map<Long, DeclarationTemplate> declarationTemplateMap = [:]

@Field Map<Long, String> departmentFullNameMap = [:]

// Мапа <ID_Данные о физическом лице - получателе дохода, Физическое лицо: <ФИО> ИНП:<ИНП>>
@Field def ndflPersonFLMap = [:]
@Field final TEMPLATE_PERSON_FL = "Физическое лицо: \"%s\", ИНП: \"%s\""

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
@Field def inpCache = [:]
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
 * Получить "Физические лица"
 * @param personIds
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefPersons(def personIds) {
    if (personsCache.isEmpty()) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordIds(RefBook.Id.PERSON.getId(), personIds)
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)
        }
    }
    return personsCache;
}

// todo добавить обработку дублей
/*def getRefPersons(def personIds) {
    if (personsCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
//        def dublMap = [:]
//        def dublList = []
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)

            // Добавим дубликат в Мапу
//            if (person.get(RF_DUBLICATES).value != null && !dublMap.get(person.get(RF_DUBLICATES).value)) {
//                dublMap.put(person.get(RF_DUBLICATES).value, personId)
//                dublList.add(person.get(RF_DUBLICATES).value)
//            }
        }
        // Получим оригинальные записи, если имеются дубликаты
//        if (dublMap.size() > 0) {
//            def refBookDublMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, dublList)
//            refBookDublMap.each { originalPersonId, person ->
//                dublPersonId = dublMap.get(originalPersonId).value
//            }
//        }
    }
    return personsCache;
}*/




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
 * todo Получение ИНП реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefINP(def personIds) {
    if (inpCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId.toString())
            def inpList = []
            refBookMap.each { refBook ->
                inpList.add(refBook?.INP?.stringValue)
            }
            inpCache.put(personId, inpList)
        }
    }
    return inpCache;
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
 * Получить "Статусы налогоплательщика"
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
def getRefDul(def personIds) {
    if (dulCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(RefBook.Id.ID_DOC.getId(), "PERSON_ID = " + personId.toString())
            def dulList = []
            refBookMap.each { refBook ->
                dulList.add(refBook)
            }
            dulCache.put(personId, dulList)
        }
    }
    return dulCache;
}

/**
 * Получить набор тербанков
 */
def getTerBank() {
    if (terBankCache.size() == 0) {
        def refBookMap = getRefBookByFilter(REF_DEPARTMENT_ID, "PARENT_ID = 0")
        refBookMap.each { refBook ->
            terBankCache.put(refBook?.id?.numberValue, refBook?.NAME?.stringValue)
        }
    }
    return terBankCache
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

//>------------------< UTILS >----------------------<

// Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
@Field final long REF_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_NOT_FOUND_REF = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
@Field final String MESSAGE_ERROR_VALUE = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: %s."
@Field final String MESSAGE_ERROR_INN = "Некорректный ИНН"
@Field final String MESSAGE_ERROR_SNILS = "Некорректный СНИЛС"
@Field final String MESSAGE_ERROR_BIRTHDAY = "Дата рождения налогоплательщика превышает дату отчетного периода"
@Field final String MESSAGE_ERROR_DATE = "Дата не входит в отчетный период формы"
@Field final String MESSAGE_ERROR_NOT_MATCH_RULE = "Значение не соответствует правилу: "
@Field final String MESSAGE_ERROR_MUST_FILL = "Поле \"%s\" должно быть заполнено, если %s"
@Field final String MESSAGE_ERROR_NOT_FILL = "Поле \"%s\" не заполнено. Должны быть заполнены либо все поля %s, либо не заполнено ни одно из полей."
@Field final String MESSAGE_ERROR_NOT_MUST_FILL = "Поле \"%s\" не должно быть заполнено, если %s"
@Field final String MESSAGE_ERROR_NOT_FOUND_PERSON = "Не удалось установить связь со справочником \"%s\" для Раздел: \"%s\". Строка: \"%s\", ИНП: \"%s\"."
@Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
@Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
@Field final String MESSAGE_ERROR_ABSENT = " Отсутсвуют строки:"
@Field final String MESSAGE_ERROR_NOT_FOUND_PARAM = "Указанные значения не найдены в Справочнике \"Настройки подразделений\" для \"%s\""
@Field final String MESSAGE_ERROR_DUL = "Указанный документ не включается в отчетность"


@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" размером %d."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" в колличестве %d."

// Таблицы
@Field final String T_PERSON = "Реквизиты"
@Field final String T_PERSON_INCOME = "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION = "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT = "Сведения о доходах в виде авансовых платежей"

// Справочники
@Field final String R_FIAS = "ФИАС"
@Field final String R_PERSON = "Физические лица"
@Field final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
@Field final String R_ID_DOC_TYPE = "Коды документов"
@Field final String R_STATUS = "Статусы налогоплательщика"
@Field final String R_INCOME_CODE = "Коды видов доходов"
@Field final String R_INCOME_KIND = "Виды доходов"
@Field final String R_RATE = "Ставки"
@Field final String R_TYPE_CODE = "Коды видов вычетов"
@Field final String R_NOTIF_SOURCE = "Коды налоговых органов"
@Field final String R_ADDRESS = "Адреса"
@Field final String R_INP = "Идентификаторы налогоплательщиков"
@Field final String R_DUL = "Документы, удостоверяющий личность"

// Реквизиты
@Field final String C_ADDRESS = "Адрес регистрации в Российской Федерации "
@Field final String C_CITIZENSHIP = "Гражданство (код страны)"
@Field final String C_ID_DOC = "Документ, удостоверяющий личность"
@Field final String C_ID_DOC_TYPE = "Код вида документа"
@Field final String C_INC_REP = "Включается в отчетность"
@Field final String C_ID_DOC_NUMBER = "Серия и номер документа"
@Field final String C_STATUS = "Статус"
@Field final String C_INCOME_CODE = "Код вида дохода"
@Field final String C_INCOME_KIND = "Вид дохода"
@Field final String C_RATE = "Ставка"
@Field final String C_TYPE_CODE = "Код вычета"
@Field final String C_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление"
@Field final String C_LAST_NAME = "Фамилия"
@Field final String C_FIRST_NAME = "Имя"
@Field final String C_MIDDLE_NAME = "Отчество"
@Field final String C_INP = "Уникальный код клиента"
@Field final String C_BIRTH_DATE = "Дата рождения"
@Field final String C_INN_NP = "ИНН  физического лица"
@Field final String C_SNILS = "СНИЛС"
@Field final String C_INN_FOREIGN = "ИНН  иностранного гражданина"
@Field final String C_REGION_CODE = "Код Региона"
@Field final String C_AREA = "Район"
@Field final String C_CITY = "Город"
@Field final String C_LOCALITY = "Населенный пункт"
@Field final String C_STREET = "Улица"
@Field final String C_HOUSE = "Дом"
@Field final String C_BUILDING = "Корпус"
@Field final String C_FLAT = "Квартира"

// Сведения о доходах и НДФЛ
@Field final String C_INCOME_TYPE = "Признак вида дохода"
@Field final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода"
@Field final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода"
@Field final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода"
@Field final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода"
@Field final String C_TOTAL_DEDUCTIONS_SUMM = "Общая сумма вычетов"
@Field final String C_TAX_BASE = "Налоговая база"
@Field final String C_TAX_RATE = "Ставка налога"
@Field final String C_TAX_DATE = "Дата налога"
@Field final String C_CALCULATED_TAX = "Сумма налога исчисленная"
@Field final String C_WITHHOLDING_TAX = "Сумма налога удержанная"
@Field final String C_NOT_HOLDING_TAX = "Сумма налога, не удержанная налоговым агентом"
@Field final String C_OVERHOLDING_TAX = "Сумма налога, излишне удержанная налоговым агентом"
@Field final String C_REFOUND_TAX = "Сумма возвращенного налога"
@Field final String C_TAX_TRANSFER_DATE = "Срок (дата) перечисления налога"
@Field final String C_PAYMENT_DATE = "Дата платежного поручения"
@Field final String C_PAYMENT_NUMBER = "Номер платежного поручения перечисления налога в бюджет"
@Field final String C_TAX_SUMM = "Сумма налога перечисленная"
@Field final String C_OKTMO = "ОКТМО"
@Field final String C_KPP = "КПП"

// Сведения о вычетах
@Field final String C_NOTIF_DATE = "Дата выдачи уведомления"
@Field final String C_INCOME_ACCRUED = "Дата начисления дохода"
@Field final String C_PERIOD_PREV_DATE = "Дата применения вычета в предыдущем периоде"
@Field final String C_PERIOD_CURR_DATE = "Дата применения вычета в текущем периоде"

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
@Field final String RF_OLD_ID = "OLD_ID"

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

    // Проверки на соответствие справочникам
    checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Общие проверки
    checkDataCommon(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList)

    // Проверки сведений о доходах
    checkDataIncome(ndflPersonList, ndflPersonIncomeList)
}

/**
 * Проверки на соответствие справочникам
 * @return
 */
def checkDataReference(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {
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
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_KIND, incomeKindMap.size())

    // Ставки
    def rateList = getRefRate()
    logger.info(SUCCESS_GET_REF_BOOK, R_RATE, rateList.size())

    // Коды видов вычетов
    def deductionTypeList = getRefDeductionType()
    logger.info(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

    // Коды налоговых органов
    def taxInspectionList = getRefNotifSource()
    logger.info(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

    // Физические лица
    def personIds = collectNdflPersonIds(ndflPersonList)

    //Map<Long, Map<String, RefBookValue>>
    def personMap = [:]

    // ИНП <person_id, массив_ИНП>
    def inpMap = [:]

    // ДУЛ <person_id, массив_ДУЛ>
    def dulMap = [:]

    // Адреса
    def addressIds = []
    def addressMap = [:]

    if (personIds.size() > 0) {
        // todo Сделать получение записей оригиналов, если текущая запись - дубликат
        personMap = getRefPersons(personIds)
        logger.info(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

        // Получим мапу ИНП
        inpMap = getRefINP(personIds)
        logger.info(SUCCESS_GET_TABLE, R_INP, inpMap.size())

        // Получим мапу ДУЛ
        dulMap = getRefDul(personIds)
        logger.info(SUCCESS_GET_TABLE, R_DUL, dulMap.size())

        // Получим Мапу адресов
        personMap.each { personId, person ->
            // Сохраним идентификаторы адресов в коллекцию
            if (person.get(RF_ADDRESS).value != null) {
                addressIds.add(person.get(RF_ADDRESS).value)
            }
        }
        if (addressIds.size() > 0) {
            addressMap = getRefAddress(addressIds)
            logger.info(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
        }
    }

    //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}

    for (NdflPerson ndflPerson : ndflPersonList) {

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");

        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        // Спр1 ФИАС
        // todo oshelepeav Исправить на фатальную ошибку https://jira.aplana.com/browse/SBRFNDFL-448
        if (!isExistsAddress(ndflPerson.regionCode, ndflPerson.area, ndflPerson.city, ndflPerson.locality, ndflPerson.street)) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_ADDRESS, fioAndInp, C_ADDRESS, R_FIAS);
        }

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_CITIZENSHIP);
        }

        // Спр3 Документ удостоверяющий личность (Обязательное поле)
        if (!documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {

            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_ID_DOC_TYPE);
        }

        // Спр4 Статусы налогоплательщиков (Обязательное поле)
        if (!taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_STATUS);
        }

        // Спр10 Наличие связи с "Физическое лицо"
        if (ndflPerson.personId == null) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_PERSON, R_PERSON, T_PERSON, fio, ndflPerson.inp);
        } else {
            //Справочник ФЛ
            def personRecord = personMap.get(ndflPerson.personId)

            // Спр11 Фамилия (Обязательное поле)
            if (!ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_LAST_NAME, fioAndInp, C_LAST_NAME, R_PERSON);
            }

            // Спр11 Имя (Обязательное поле)
            if (!ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_FIRST_NAME, fioAndInp, C_FIRST_NAME, R_PERSON);
            }

            // Спр11 Отчество (Необязательное поле)
            if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_MIDDLE_NAME, fioAndInp, C_MIDDLE_NAME, R_PERSON);
            }


            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)){
                // Спр12 ИНП первичная (Обязательное поле)
                def inpList = inpMap.get(ndflPerson.personId)
                if (!ndflPerson.inp.equals(personRecord.get(RF_SNILS).value) && !inpList.contains(ndflPerson.inp)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                            T_PERSON, ndflPerson.rowNum, C_INP, fioAndInp, C_INP, R_INP);
                }
            } else {
                //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                //if (formType == CONSOLIDATE){}
                String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                if (!ndflPerson.inp.equals(recordId)) {
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_INP, fioAndInp, C_INP, T_PERSON);
                }
            }

            // todo Спр12.1 ИНП консолидированная - Реализовать позже

            // Спр13 Дата рождения (Обязательное поле)
            if (!ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, C_BIRTH_DATE, R_PERSON);
            }

            // Спр14 Гражданство (Обязательное поле)
            def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
            if (!ndflPerson.citizenship.equals(citizenship)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_PERSON);
            }

            // Спр15 ИНН (Необязательное поле)
            if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, C_INN_NP, R_PERSON);
            }

            // Спр16 ИНН в стране Гражданства (Необязательное поле)
            if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_INN_FOREIGN, fioAndInp, C_INN_FOREIGN, R_PERSON);
            }


            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)){
                // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                def allDocList = dulMap.get(ndflPerson.personId)
                // Вид документа
                def personDocTypeList = []
                // Серия и номер документа
                def personDocNumberList = []
                allDocList.each { dul ->
                    personDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                    personDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
                }
                if (!personDocTypeList.contains(ndflPerson.idDocType)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                            T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_PERSON);
                }
                if (!personDocNumberList.contains(ndflPerson.idDocNumber)) {
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                            T_PERSON, ndflPerson.rowNum, C_ID_DOC_NUMBER, fioAndInp, C_ID_DOC_NUMBER, R_PERSON);
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
                    //"Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
                    logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_ID_DOC, fioAndInp, C_ID_DOC, R_DUL);
                } else {
                    int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                    if (incRep != 1) {
                        //MESSAGE_ERROR_VALUE = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: %s."
                        logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_ID_DOC, fioAndInp, C_ID_DOC, MESSAGE_ERROR_DUL);

                    }
                }
            }


            // Спр18 Статус налогоплательщика (Обязательное поле)
            def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
            if (!ndflPerson.status.equals(taxpayerStatus)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_PERSON);
            }

            // Спр19 Адрес (Необязательное поле)
            // Сравнение должны быть проведено даже с учетом пропусков
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

            // Код Региона
            if (!ndflPerson.regionCode.equals(regionCode)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_REGION_CODE, fioAndInp, C_REGION_CODE, R_PERSON);
            }

            // Район
            if (!ndflPerson.area.equals(area)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_AREA, fioAndInp, C_AREA, R_PERSON);
            }

            // Город
            if (!ndflPerson.city.equals(city)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_CITY, fioAndInp, C_CITY, R_PERSON);
            }

            // Населенный пункт
            if (!ndflPerson.locality.equals(locality)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_LOCALITY, fioAndInp, C_LOCALITY, R_PERSON);
            }

            // Улица
            if (!ndflPerson.street.equals(street)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_STREET, fioAndInp, C_STREET, R_PERSON);
            }

            // Дом
            if (!ndflPerson.house.equals(house)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_HOUSE, fioAndInp, C_HOUSE, R_PERSON);
            }

            // Корпус
            if (!ndflPerson.building.equals(building)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_BUILDING, fioAndInp, C_BUILDING, R_PERSON);
            }

            // Квартира
            if (!ndflPerson.flat.equals(flat)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON, ndflPerson.rowNum, C_FLAT, fioAndInp, C_FLAT, R_PERSON);
            }
        }
    }

    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find { key, value -> value == ndflPersonIncome.incomeCode }) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, C_INCOME_CODE, R_INCOME_CODE);
        }

        // Спр6 Вида дохода (Необязательное поле)
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (ndflPersonIncome.incomeType != null) {
            def idIncomeCode = incomeKindMap.get(ndflPersonIncome.incomeType)
            if (!idIncomeCode) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            } else if (!incomeCodeMap.get(idIncomeCode.value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            }
        }

        // Спр7 Ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate.toString())) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_RATE, fioAndInp, C_RATE, R_RATE);
        }
    }

    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_TYPE_CODE, fioAndInp, C_TYPE_CODE, R_TYPE_CODE);
        }

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }

    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {
        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_NOT_FOUND_REF, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }

}

/**
 * Общие проверки
 */
def checkDataCommon(
        def ndflPersonList, def ndflPersonIncomeList, def ndflPersonDeductionList, def ndflPersonPrepaymentList) {

    // Порядковые номера строк в "Реквизиты"
    def rowNumPersonList = []

    // Порядковые номера строк в "Сведения о доходах и НДФЛ"
    def rowNumPersonIncomeList = []

    // Порядковые номера строк в "Сведения о вычетах"
    def rowNumPersonDeductionList = []

    // Порядковые номера строк в "Сведения о доходах в виде авансовых платежей"
    def rowNumPersonPrepaymentList = []

    // Тербанки
    //def mapTerBank = getTerBank()

    // Параметры подразделения
    // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-263
//    def departmentParam = getDepartmentParam()
//    def mapOktmoAndKpp = getOktmoAndKpp(departmentParam.record_id.value)

    for (NdflPerson ndflPerson : ndflPersonList) {

        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        println "ndflPerson.rowNum="+ndflPerson.rowNum

        rowNumPersonList.add(ndflPerson.rowNum)

        // Общ1 Корректность ИНН (Необязательное поле)
        if (ndflPerson.innNp != null && !ScriptUtils.checkControlSumInn(ndflPerson.innNp)) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, MESSAGE_ERROR_INN);
        }

        // Общ2 Корректность КПП, Пока необходимость данной проверки под вопросом
        // Общ3 Корректность ОКТМО, Пока необходимость данной проверки под вопросом

        // Общ4 Дата рождения (Обязательное поле)
        //if (ndflPerson.birthDay > getReportPeriodEndDate()) {
        //    logger.error(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, MESSAGE_ERROR_BIRTHDAY);
        //}

        // Общ11 СНИЛС (Необязательное поле)
        if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON, ndflPerson.rowNum, C_SNILS, fioAndInp, MESSAGE_ERROR_SNILS);
        }
    }


    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        rowNumPersonIncomeList.add(ndflPersonIncome.rowNum)

        // Общ5 Принадлежность дат операций к отчетному периоду
        // Дата начисления дохода (Необязательное поле)
        if (ndflPersonIncome.incomeAccruedDate != null && (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }

        // Дата выплаты дохода (Необязательное поле)
        if (ndflPersonIncome.incomePayoutDate != null && (ndflPersonIncome.incomePayoutDate < getReportPeriodStartDate() || ndflPersonIncome.incomePayoutDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата налога (Необязательное поле)
        if (ndflPersonIncome.taxDate != null && (ndflPersonIncome.taxDate < getReportPeriodStartDate() || ndflPersonIncome.taxDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Срок (дата) перечисления налога (Необязательное поле)
        if (ndflPersonIncome.taxTransferDate != null && (ndflPersonIncome.taxTransferDate < getReportPeriodStartDate() || ndflPersonIncome.taxTransferDate > getReportPeriodEndDate())) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }

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
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
                // Раздел 2. Графа 5 "Признак вида дохода" должна быть заполнена
                if (ndflPersonIncome.incomeType == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_TYPE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_TYPE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }

            // если НЕ заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate == null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                emptyField += ", \"" + C_INCOME_PAYOUT_DATE + "\", \"" + C_INCOME_PAYOUT_SUMM + "\""

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть НЕ заполнена
                if (ndflPersonIncome.taxTransferDate != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }

            // 	Раздел 2. Графа 13 "Налоговая база" должна быть заполнена
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxBase)) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_BASE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 14 "Ставка налога" должна быть заполнена
            if (ndflPersonIncome.taxRate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_RATE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
            // 	Раздел 2. Графа 15 "Дата налога" должна быть заполнена
            if (ndflPersonIncome.taxDate == null) {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_DATE, emptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнены Раздел 2. Графы 22-24
            def notEmptyField = "заполнены " + getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])

            // Раздел 2. Графа 12 "Общая сумма вычетов" должна быть НЕ заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TOTAL_DEDUCTIONS_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TOTAL_DEDUCTIONS_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 13 "Налоговая база" должна быть НЕ заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.taxBase)) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_BASE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_BASE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 14 "Ставка налога" должна быть НЕ заполнена
            if (ndflPersonIncome.taxRate != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_RATE, notEmptyField])
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_RATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }
            // 	Раздел 2. Графа 15 "Дата налога" должна быть НЕ заполнена
            if (ndflPersonIncome.taxDate != null) {
                def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
            }

            // если заполнены Раздел 2. Графы 7 и 11
            if (ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 6 "Дата начисления дохода"
        if (ndflPersonIncome.incomeAccruedDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_DATE])

            // Раздел 2. Графа 10 "Сумма начисленного дохода" должна быть заполнена
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_CALCULATED_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.notHoldingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_NOT_HOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть заполнена
                if (ndflPersonIncome.overholdingTax == null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_OVERHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            } else {
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если заполнена Раздел 2. Графа 10 "Сумма начисленного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 6 "Дата начисления дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_ACCRUED_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_ACCRUED_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                def emptyField = "не заполнены " + getQuotedFields([C_INCOME_ACCRUED_DATE, C_INCOME_ACCRUED_SUMM])

                // Раздел 2. Графа 16 "Сумма налога исчисленная" должна быть НЕ заполнена
                if (!ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_CALCULATED_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_CALCULATED_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 18 "Сумма налога, не удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.notHoldingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_NOT_HOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_NOT_HOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
                // Раздел 2. Графа 19 "Сумма налога, излишне удержанная налоговым агентом" должна быть НЕ заполнена
                if (ndflPersonIncome.overholdingTax != null) {
                    def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_OVERHOLDING_TAX, emptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OVERHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                }
            }
        }

        // если заполнена Раздел 2. Графа 7 "Дата выплаты дохода"
        if (ndflPersonIncome.incomePayoutDate != null) {
            def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_DATE])

            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                notEmptyField = "заполнены " + getQuotedFields([C_INCOME_PAYOUT_DATE, C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 17 "Сумма налога удержанная" должна быть заполнена
                if (ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_WITHHOLDING_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_WITHHOLDING_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 20 "Сумма возвращенного налога" должна быть заполнена
                if (ndflPersonIncome.refoundTax == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_REFOUND_TAX, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_REFOUND_TAX, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }

                // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть заполнена
                if (ndflPersonIncome.taxTransferDate == null) {
                    def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_TAX_TRANSFER_DATE, notEmptyField])
                    //TODO turn_to_error
                    logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
                }
            } else {
                // Раздел 2. Графа 11 "Сумма выплаченного дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_SUMM, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            }
        } else {
            // если не заполнена Раздел 2. Графа 7 "Дата выплаты дохода"

            // если заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
            if (!ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                def notEmptyField = "заполнена " + getQuotedFields([C_INCOME_PAYOUT_SUMM])

                // Раздел 2. Графа 7 "Дата выплаты дохода" должна быть заполнена
                def msgErrMustFill = sprintf(MESSAGE_ERROR_MUST_FILL, [C_INCOME_PAYOUT_DATE, notEmptyField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_PAYOUT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrMustFill);
            } else {
                // если НЕ заполнена Раздел 2. Графа 11 "Сумма выплаченного дохода"
                if (ndflPersonIncome.paymentDate == null &&
                        ndflPersonIncome.paymentNumber == null &&
                        ndflPersonIncome.taxSumm == null) {

                    // Раздел 2. Графа 21 "Срок (дата) перечисления налога" должна быть НЕ заполнена
                    if (ndflPersonIncome.taxTransferDate != null) {
                        def msgErrNotMustFill = sprintf(MESSAGE_ERROR_NOT_MUST_FILL, [C_TAX_TRANSFER_DATE, emptyField])
                        //TODO turn_to_error
                        logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_TRANSFER_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrNotMustFill);
                    }
                }
            }
        }

        // Раздел 2. Графа 22, 23, 24 Должны быть либо заполнены все 3 Графы, либо ни одна их них
        if (ndflPersonIncome.paymentDate != null || ndflPersonIncome.paymentNumber != null || !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
            def allField = getQuotedFields([C_PAYMENT_DATE, C_PAYMENT_NUMBER, C_TAX_SUMM])
            // Раздел 2. Графа 22 "Дата платежного поручения"
            if (ndflPersonIncome.paymentDate == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_DATE, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_DATE, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 23 "Номер платежного поручения перечисления налога в бюджет"
            if (ndflPersonIncome.paymentNumber == null) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_PAYMENT_NUMBER, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_PAYMENT_NUMBER, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
            // Раздел 2. Графа 24 "Сумма налога перечисленная"
            if (ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                def msgErrFill = sprintf(MESSAGE_ERROR_NOT_FILL, [C_TAX_SUMM, allField])
                //TODO turn_to_error
                logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_INCOME, ndflPersonIncome.rowNum, C_TAX_SUMM, fioAndInp, MESSAGE_ERROR_NOT_MATCH_RULE + msgErrFill);
            }
        }

        // Общ10 Соответствие КПП и ОКТМО Тербанку
        // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-263
//        def kppList = mapOktmoAndKpp.get(ndflPersonIncome.oktmo)
//        def msgErr = sprintf(MESSAGE_ERROR_NOT_FOUND_PARAM, [mapTerBank.get(declarationData.departmentId).value])
//        if (kppList == null) {
//            logger.error(MESSAGE_ERROR_VALUE,
//                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_OKTMO, fioAndInp, msgErr);
//        } else {
//            if (!kppList.contains(ndflPersonIncome.kpp)) {
//                logger.error(MESSAGE_ERROR_VALUE,
//                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_KPP, fioAndInp, msgErr);
//            }
//        }
    }

    for (NdflPersonDeduction ndflPersonDeduction: ndflPersonDeductionList){

        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        rowNumPersonDeductionList.add(ndflPersonDeduction.rowNum)

        // Общ6 Принадлежность дат налоговых вычетов к отчетному периоду
        // Дата выдачи уведомления (Обязательное поле)
        if (ndflPersonDeduction.notifDate < getReportPeriodStartDate() || ndflPersonDeduction.notifDate > getReportPeriodEndDate()) {
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата начисления дохода (Обязательное поле)
        if (ndflPersonDeduction.incomeAccrued < getReportPeriodStartDate() || ndflPersonDeduction.incomeAccrued > getReportPeriodEndDate()) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_INCOME_ACCRUED, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в предыдущем периоде (Необязательное поле)
        if (ndflPersonDeduction.periodPrevDate != null &&
                (ndflPersonDeduction.periodPrevDate < getReportPeriodStartDate() || ndflPersonDeduction.periodPrevDate > getReportPeriodEndDate())) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_PREV_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
        // Дата применения вычета в текущем периоде (Обязательное поле)
        if (ndflPersonDeduction.periodCurrDate < getReportPeriodStartDate() || ndflPersonDeduction.periodCurrDate > getReportPeriodEndDate()) {
            //TODO turn_to_error
            logger.warn(MESSAGE_ERROR_VALUE, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_PERIOD_CURR_DATE, fioAndInp, MESSAGE_ERROR_DATE);
        }
    }

    for (NdflPersonPrepayment ndflPersonPrepayment: ndflPersonPrepaymentList){
        rowNumPersonPrepaymentList.add(ndflPersonPrepayment.rowNum)
    }

    // Общ8 Отсутствие пропусков и повторений в нумерации строк
    def msgErrDubl = getErrorMsgDubl(rowNumPersonList, T_PERSON)
    msgErrDubl += getErrorMsgDubl(rowNumPersonIncomeList, T_PERSON_INCOME)
    msgErrDubl += getErrorMsgDubl(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    msgErrDubl += getErrorMsgDubl(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrDubl = msgErrDubl == "" ? "" : MESSAGE_ERROR_DUBL + msgErrDubl
    def msgErrAbsent = getErrorMsgAbsent(rowNumPersonList, T_PERSON)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonIncomeList, T_PERSON_INCOME)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonDeductionList, T_PERSON_DEDUCTION)
    msgErrAbsent += getErrorMsgAbsent(rowNumPersonPrepaymentList, T_PERSON_PREPAYMENT)
    msgErrAbsent = msgErrAbsent == "" ? "" : MESSAGE_ERROR_ABSENT + msgErrAbsent
    if (msgErrDubl != "" || msgErrAbsent != "") {
        //В ТФ имеются пропуски или повторы в нумерации строк.
        logger.warn(MESSAGE_ERROR_DUBL_OR_ABSENT + msgErrDubl + msgErrAbsent);
    }
}

/**
 * Проверки сведений о доходах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 */
def checkDataIncome(ndflPersonList, ndflPersonIncomeList) {

    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "");
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)
    }

    ndflPersonIncomeList.each { ndflPersonIncome ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
        // СведДох1 Дата начисления дохода



    }
}


//>------------------< CHECK DATA UTILS >----------------------<



/**
 * Получить параметры для конкретного тербанка
 * @return
 */
def getDepartmentParam() {
    def departmentId = declarationData.departmentId
    def departmentParamList = getProvider(REF_NDFL_ID).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
def getOktmoAndKpp(def departmentParamId) {
    def mapNdflDetail = [:]
    def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
    def departmentParamTableList = getProvider(REF_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }
    def kppList = []
    departmentParamTableList.each { departmentParamTable ->
        kppList = mapNdflDetail.get(departmentParamTable?.OKTMO?.stringValue)
        if (kppList == null) {
            kppList = []
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(departmentParamTable?.OKTMO?.stringValue, kppList)
        } else if (!kppList.contains(departmentParamTable?.KPP?.stringValue)) {
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(departmentParamTable?.OKTMO?.stringValue, kppList)
        }
    }
    return mapNdflDetail
}

/**
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param ndflPersonList
 * @return
 */
def collectNdflPersonIds(List<NdflPerson> ndflPersonList) {
    def result = []
    ndflPersonList.each { ndflPerson ->
        if (ndflPerson.personId != null) {
            result.add(ndflPerson.personId)
        }
    }
    return result;
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
boolean isExistsAddress(regionCode, area, city, locality, street) {
    if (!regionCode || !area || !city || !locality || !street) {
        return false
    }

    return fiasRefBookService.findAddress(regionCode, area, city, locality, street).size() > 0
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