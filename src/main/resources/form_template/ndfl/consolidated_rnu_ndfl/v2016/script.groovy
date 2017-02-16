package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.*
import groovy.transform.Field
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
        logicCheck()
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
def declarationTypeId = 100

//------------------ Consolidation ----------------------

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
    Map<Long, Map<String, RefBookValue>> documentTypeRefBook= getRefDocumentTypeCodes();
    Map<Long, String> taxpayerStatusCodes = getRefTaxpayerStatusCodes();

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

    ndflPerson.status = taxpayerStatusCodes.get(identityDocumentRecord.get("TAXPAYER_STATE")?.getReferenceValue())

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

//------------------ Get Sources ----------------------

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
    List<DeclarationData> allDeclarationDataList = declarationService.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId);

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
            logger.warn("Найдены НФ для которых не заполнено поле АСНУ. Подразделение: " + getDepartmentFullName(departmentId) + ", отчетный период: " + reportPeriodId + ", id: "+declarationData.id);
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

    def isSource = (declarationTemplate.id == declarationTypeId)
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

//------------------ Check data ----------------------

// --- stubs ---
def logicCheck() {
    println "logic check"
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

def format(date) {
    return date?.format('dd.MM.yyyy')
}

def createSpecificReport() {
    def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.getSubreportParamValues().each { k, v ->
        writer.write(k + "::" + v + "\n")
    }
    writer.close()
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".txt")
}

//---<Справочники, кэш>---

// Дата начала отчетного периода
@Field def periodStartDate = null

// Дата окончания отчетного периода
@Field def periodEndDate = null

// Кэш провайдеров
@Field Map<Long, RefBookDataProvider> providerCache = [:]
//Физлица
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]

@Field Map<Long, Map<String, RefBookValue>> recordPersonsCache = [:]

//Коды страны
@Field Map<Long, String> countryCodeCache = [:]
//Виды документов, удостоверяющих личность
@Field Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]

//Коды статуса налогоплатильщика
@Field Map<Long, String> taxpayerStatusCodeCache = [:]
//Адреса физлиц
@Field Map<Long, Map<String, RefBookValue>> addressCache = [:]
//<person_id:  <id: <string:object>>>
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulCache = [:]

@Field def sourceReportPeriod = null

@Field Map<Long, DepartmentReportPeriod> departmentReportPeriodMap = [:]

@Field Map<Long, DeclarationTemplate> declarationTemplateMap = [:]


@Field Map<Long, String> departmentFullNameMap = [:]


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
 *
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

def clearPersonCache() {
    personsCache.clear();
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
 * Получить "Коды документов, удостоверяющих личность"
 */
def getRefDocumentTypeCodes() {
    if (documentTypeCache.size() == 0) {
        def refBookList = getRefBook(RefBook.Id.DOCUMENT_CODES.getId())
        refBookList.each { refBook ->
            documentTypeCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return documentTypeCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefTaxpayerStatusCodes() {
    if (taxpayerStatusCodeCache.size() == 0) {
        def refBookMap = getRefBook(RefBook.Id.TAXPAYER_STATUS.getId())
        refBookMap.each { refBook ->
            taxpayerStatusCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return taxpayerStatusCodeCache;
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







