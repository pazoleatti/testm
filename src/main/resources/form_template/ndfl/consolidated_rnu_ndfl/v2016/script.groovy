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
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException

import groovy.xml.MarkupBuilder

//TODO удалить все println

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
 * Идентификатор вида деклараций из declaration_type
 */
@Field
def declarationTypeId = 100

/**
 * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
 * Данный метод выполняет вызов скрипта (GET-SOURCES) и
 *
 */
void consolidation() {
    println "declaration consolidate start!"

    def declarationDataId = declarationData.id

    //Возвращает список нф-источников для указанной декларации (включая несозданные)

    //декларация-приемник, true - заполнятся только текстовые данные для GUI и сообщений,true - исключить несозданные источники,ограничение по состоянию для созданных экземпляров
    //список нф-источников
    List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, false, false, null, userInfo, logger);

    println "sourcesInfo: " + relationsToStr(sourcesInfo)

    List<NdflPerson> ndflPersonList = collectNdflPersonList(sourcesInfo);



    List<Long> personIds = collectRefBookPersonIds(ndflPersonList)

    //-----<INITIALIZE_CACHE_DATA>-----
    Map<Long, Map<String, RefBookValue>> refBookPerson = getRefPersons(personIds);
    Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPerson);
    //PersonId :  Документы
    Map<Long, List<Map<String, RefBookValue>>> identityDocMap = getRefDul(personIds)
    //-----<INITIALIZE_CACHE_DATA_END>-----


    SortedMap<Long, NdflPerson> ndflPersonMap = consolidateNdflPerson(ndflPersonList);

    //разделы в которых идет сплошная нумерация
    def ndflPersonNum = 1;
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {
        Long record_id = entry.getKey();
        NdflPerson ndflPerson = entry.getValue();

        println "personId=" + record_id + ", ndflPerson=" + ndflPerson

        def incomes = ndflPerson.incomes;
        def deductions = ndflPerson.deductions;
        def prepayments = ndflPerson.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
        prepayments.sort { a, b -> a.notifDate <=> b.notifDate }

        //реализовать поиск в справочнике актуальной версии по record_id
        Long ndflPersonId = ndflPerson.id;
        List<Map<String, RefBookValue>> identityDocList = identityDocMap.get(ndflPersonId)

        Map<String, RefBookValue> personRecord = refBookPerson.get(ndflPersonId);

        Map<String, RefBookValue> identityDocumentRecord = identityDocList?.find { it.get("INC_REP")?.getNumberValue() == 1 };

        Map<String, RefBookValue> addressRecord = addressMap.get(ndflPersonId);

        //List<Long> documentsIds = documentProvider.getUniqueRecordIds(null, "PERSON_ID = " + personId + " AND INC_REP = 1");
        if (identityDocument == null || identityDocument.isEmpty()) {
            logger.error("В справочнике \"Документы, удостоверяющие личность\" отсутствуют данные о документах для физлица с id: \"%s\", и признаком включения в отчетность: 1", ndflPersonId)
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

    println "declaration consolidate end!"

}

/**
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param ndflPersonList
 * @return
 */
def collectRefBookPersonIds(def ndflPersonList) {
    Set<Long> personIdSet = new HashSet<Long>();
    for (NdflPerson ndflPerson : ndflPersonList) {
        if (ndflPerson.personId != null && ndflPerson.personId != 0) {
            personIdSet.add(it.personId);
        } else {
            throw new ServiceException("Не указан идентификатор 'Идентификатор ФЛ', декларация id=" + ndflPerson.declarationDataId)
        }
    }
    return new ArrayList<Long>(personIdSet);
}

/**
 * Создает объект NdlPerson заполненный данными из справочника
 */
def buildNdflPerson(NdflPerson currentNdflPerson, Map<String, RefBookValue> personRecord, Map<String, RefBookValue> identityDocumentRecord, Map<String, RefBookValue> addressRecord) {

    Map<Long, String> countryCodes = getRefCountryCode();
    Map<Long, String> documentCodes = getRefDocumentTypeCodes();
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
    ndflPerson.idDocType = documentCodes.get(identityDocumentRecord.get("DOC_ID")?.getReferenceValue())
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
    //TODO адресс ино, как заполнять?
    ndflPerson.address = currentNdflPerson.address;
    ndflPerson.additionalData = currentNdflPerson.additionalData
    return ndflPerson
}

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
    for (Relation relation : sourcesInfo) {
        Long declarationDataId = relation.declarationDataId;
        println "consolidate from relation declarationDataId=" + declarationDataId
        if (!relation.declarationState.equals(State.ACCEPTED)) {
            logger.error("Декларация-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", подразделение: \"%s\"", relation.getDeclarationTypeName(), relation.getFullDepartmentName())
            continue
        }
        List<NdflPerson> ndflPersonList = findNdflPersonWithData(declarationDataId);
        result.addAll(ndflPersonList);
    }
    return result;
}

List<NdflPerson> findNdflPersonWithData(Long declarationDataId) {

    List<NdflPerson> result = ndflPersonService.findNdflPerson(declarationDataId);

    Map<Long, List<NdflPersonOperation>> imcomesList = mapToPesonId(ndflPersonService.findNdflPersonIncome(declarationDataId));
    Map<Long, List<NdflPersonOperation>> deductionList = mapToPesonId(ndflPersonService.findNdflPersonDeduction(declarationDataId));
    Map<Long, List<NdflPersonOperation>> prepaymentList = mapToPesonId(ndflPersonService.findNdflPersonPrepayment(declarationDataId));

    for (NdflPerson ndflPerson : ndflPersonList) {
        Long ndflPersonId = ndflPerson.getId();
        ndflPerson.setIncomes(imcomesList.get(ndflPersonId));
        ndflPerson.getDeductions(deductionList.get(ndflPersonId));
        ndflPerson.setPrepayments(prepaymentList.get(ndflPersonId));
        result.add(ndflPerson);
        println "process ndflPerson=" + ndflPerson.id + ", result=" + result.size();
    }

}

/**
 * Объединение ndfl-person по record_id
 * @param rebBookPerson
 * @return
 */
Map<Long, NdflPerson> consolidateNdflPerson(List<NdflPerson> ndflPersonList) {

    Map<Long, Map<String, RefBookValue>> rebBookPerson = getRefPersons();

    Map<Long, NdflPerson> result = new TreeMap<Long, NdflPerson>();

    for (NdflPerson ndflPerson : ndflPersonList) {

        //Ссылка на справочник физлиц
        Long personId = ndflPerson.personId

        Map<String, RefBookValue> refBookRecord = rebBookPerson.get(personId);

        if (refBookRecord == null) {
            throw new ServiceException("В справочнике 'Физические лица' не найдена запись с id='" + personId);
        }

        Long recordId = refBookRecord.get("RECORD_ID")?.getReferenceValue();

        if (refBookRecord == null) {
            throw new ServiceException("В справочнике 'Физические лица' отсутствует значение атрибута 'Идентификатор ФЛ' для записи с id=" + personId);
        }

        //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
        if (result.containsKey(recordId)) {
            def consolidatePersonData = result.get(recordId)
            consolidatePersonData.incomes.addAll(ndflPerson.incomes)
            consolidatePersonData.deductions.addAll(ndflPerson.deductions)
            consolidatePersonData.prepayments.addAll(ndflPerson.prepayments)
        } else {
            result.put(recordId, ndflPerson)
        }
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
}

/**
 * Получить набор деклараций  события FormDataEvent.GET_SOURCES.
 *
 * Ищет и включает в КНФ данные налоговых форм, у которых:
 *      Подразделение является подчиненным по отношению к ТБ (уточнить у заказчика - включая сам ТБ?) согласно справочнику подразделений.
 *      Вид = РНУ НДФЛ (первичная)
 *      Состояние = "Принята"
 *      Отчетный период = Отчетный период КНФ или любой предыдущий отчетный период в рамках одного года.
 */
def getSourcesList() {

    //отчетный период в котором выполняется консолидация
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения по которому формируется консолидированная форма
    def parentDepartmentId = declarationData.departmentId

    def parentDepartment = departmentService.get(parentDepartmentId)

    //Подразделения которые является подчиненным по отношению к ТБ, включая сам ТБ
    List<Department> departments = departmentService.getAllChildren(parentDepartmentId)

    println "getSourceList: parentDepartmentId=" + parentDepartmentId + ", reportPeriod=" + reportPeriod.id

    //Список отчетных периодов которые должны быть включены в консолидированную форму (1 квартал, полугодие, 9 месяцев, год)
    List<ReportPeriod> reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.NDFL, reportPeriod.startDate, reportPeriod.endDate)

    println "getReportPeriodsByDate: " + reportPeriodList.collect { p -> "id=" + p.id + ", start=" + p.startDate + ", end=" + p.endDate }

    for (Department department : departments) {

        println "   process department: " + department.id

        for (ReportPeriod primaryReportPeriod : reportPeriodList) {

            println "       process primaryReportPeriod: " + primaryReportPeriod.id

            List<DeclarationData> primaryDeclarationDataList = findLastDeclarationData(department.id, primaryReportPeriod.id)

            for (DeclarationData primaryDeclarationData : primaryDeclarationDataList) {
                println "   process primaryDeclarationData: " + primaryDeclarationData.id
                //Формируем связь источник-приемник
                def relation = getRelation(primaryDeclarationData, department, reportPeriod)
                sources.sourceList.add(relation)
            }
        }
    }


    def sourcesInfo = relationsToStr(sources.sourceList)

    logger.info("getSourceList: " + sourcesInfo)
    println "getSourceList: " + sourcesInfo


    sources.sourcesProcessedByScript = true
}


def relationsToStr(relations) {
    return relations.collect { r ->
        " {departmentId=" + r.departmentId +
                ", declarationDataId=" + r.declarationDataId +
                ", correctionDate=" + r.correctionDate +
                ", asnuId=" + r.asnuId +
                ", relation.departmentReportPeriod=" + r.departmentReportPeriod?.reportPeriod?.id +
                "}"
    }
}


List<DeclarationData> findLastDeclarationData(departmentId, reportPeriodId) {
    List<DeclarationData> result = new ArrayList<DeclarationData>()
    def declarationDataList = declarationService.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId)
    def periodId;
    for (DeclarationData dd : declarationDataList) {
        if (periodId != null && periodId != dd.departmentReportPeriodId) {
            return result;
        }
        periodId = dd.departmentReportPeriodId
        result.add(dd);
    }
    return result;
}

/**
 * Получить запись для источника-приемника.
 *
 * @param primaryDeclarationData первичная форма
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(DeclarationData primaryDeclarationData, Department department, ReportPeriod period) {

    Relation relation = new Relation()

    //Привязка отчетных периодов к подразделениям
    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(primaryDeclarationData?.departmentReportPeriodId) as DepartmentReportPeriod

    //Макет декларации
    DeclarationTemplate declarationTemplate = getDeclarationTemplateById(primaryDeclarationData?.declarationTemplateId)

    // boolean light - заполняются только текстовые данные для GUI и сообщений
    if (light) {
        /**************  Параметры для легкой версии ***************/
        // Идентификатор подразделения
        relation.departmentId = department.id
        // полное название подразделения
        relation.fullDepartmentName = getDepartmentFullName(department.id)
        // Дата корректировки
        relation.correctionDate = departmentReportPeriod?.correctionDate
        //Год налогового периода
        relation.year = period.taxPeriod.year
        //Название периода
        relation.periodName = period.name
    }
    /**************  Общие параметры ***************/
    // подразделение
    relation.department = department
    // Период
    relation.departmentReportPeriod = departmentReportPeriod

    // форма/декларация создана/не создана
    //relation.created = (primaryDeclarationData != null)
    // является ли форма источников, в противном случае приемник? Да формируем список источников
    relation.source = true
    // Введена/выведена в/из действие(-ия)
    relation.status = true
    // Налог
    relation.taxType = TaxType.NDFL

    /**************  Параметры НФ ***************/

    //Вид декларации
    relation.declarationType = declarationTemplate.type

    relation.declarationTypeName = declarationTemplate.type.name

    return relation

}

def getDeclarationTemplateById(def declarationTemplateId) {
    if (declarationTemplateId != null) {
        return declarationService.getTemplate(declarationTemplateId)
    }
    return null
}

def getDepartmentReportPeriodById(def departmentReportPeriodId) {
    if (departmentReportPeriodId != null) {
        return departmentReportPeriodService.get(departmentReportPeriodId)
    }
    return null
}

/**
 * Получить полное название подразделения по id подразделения.
 */
def getDepartmentFullName(def id) {
    return departmentService.getParentsHierarchy(id)
}

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
    println "createSpecificReport"
    def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.getSubreportParamValues().each { k, v ->
        writer.write(k + "::" + v + "\n")
    }
    writer.close()
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".txt")
}

//---<Справочники>---

// Кэш провайдеров
@Field Map<Long, RefBookDataProvider> providerCache = [:]
//Физлица
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]
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
def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
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
def getRefPersons(def personIds) {
    if (personsCache.size() == 0) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordIds(RefBook.Id.PERSON.getId(), personIds)
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)
        }
    }
    return personsCache;
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
def getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    Map<Long, Map<String, RefBookValue>> addressMap = Collections.emptyMap();
    def addressIds = [];
    personMap.each { personId, person ->
        if (person.get("ADDRESS").value != null) {
            addressIds.add(person.get("ADDRESS").value)
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
def getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
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







