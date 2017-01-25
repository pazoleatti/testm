package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.*
import groovy.transform.Field
import groovy.util.slurpersupport.NodeChild

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent

import groovy.xml.MarkupBuilder

//TODO удалить все println

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверить
        logicCheck()
        break
    case FormDataEvent.CALCULATE: //формирование xml
        consolidation()
        generateXml()
        break
    case FormDataEvent.COMPOSE: // Консолидирование
        consolidation()
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

//Идентификаторы справочников

/**
 * Адреса физических лиц
 */
@Field
def REF_BOOK_ADDRESS_ID = 901L

/**
 * Документы, удостоверяющие личность
 */
@Field
def REF_BOOK_ID_DOC_ID = 902L

/**
 * Статусы налогоплательщика
 */
@Field
def REF_BOOK_TAXPAYER_STATE_ID = 903L

/**
 * Физические лица
 */
@Field
def REF_BOOK_PERSON_ID = 904L

/**
 * Идентификаторы налогоплательщика
 */
@Field
def REF_BOOK_ID_TAX_PAYER_ID = 905L

/**
 * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
 * Данный метод выполняет вызов скрипта (GET-SOURCES) и
 *
 */
void consolidation() {
    println "declaration consolidate start!"


    RefBookDataProvider personProvider = refBookFactory.getDataProvider(REF_BOOK_PERSON_ID)

    // RefBookDataProvider documentProvider = refBookFactory.getDataProvider(REF_BOOK_ID_DOC_ID)

    def declarationDataId = declarationData.getId()

    //удаляем рассчитанные данные если есть
    //ndflPersonService.deleteAll(declarationDataId)

    List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, false, false, null, userInfo, logger);

    println "sourcesInfo: " + relationsToStr(sourcesInfo)

    //Формируем карту ID записи о ФЛ из справочника, объект модели для консолидации
    def map = collectNdflPerson(sourcesInfo);

    println "collectNdflPerson: " + map

    //разделы в которых идет сплошная нумерация
    def ndflPersonNum = 1;
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    for (Map.Entry<Long, NdflPerson> entry : map.entrySet()) {

        def personId = entry.getKey()
        def ndflPerson = entry.getValue()

        println "personId=" + personId + ", ndflPerson=" + ndflPerson

        def incomes = ndflPerson.incomes;
        def deductions = ndflPerson.deductions;
        def prepayments = ndflPerson.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
        prepayments.sort { a, b -> a.notifDate <=> b.notifDate }

        Map<String, RefBookValue> recordData = personProvider.getRecordData(personId)

        /*List<Long> documentsIds = documentProvider.getUniqueRecordIds(null, "PERSON_ID = " + personId + " AND INC_REP = 1");
        if (documentsIds == null || documentsIds.isEmpty()) {
            logger.error("В справочнике \"Документы, удостоверяющие личность\" отсутствуют данные о документах для физлица с id: \"%s\", и признаком включения в отчетность: 1", personId)
            continue;
        }*/
        Map<String, RefBookValue> docRecordData// =  documentProvider.getRecordData(documentsIds.first());


        def consolidatePerson = createNdlPersonFromRefBook(ndflPerson, recordData, docRecordData);

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
 * Создает объект NdlPerson заполненный данными из справочника
 */
def createNdlPersonFromRefBook(currentNdflPerson, Map<String, RefBookValue> personData, Map<String, RefBookValue> docData) {
    def ndflPerson = new NdflPerson()

    //Данные о физлице - заполняется на основе справочника физлиц
    ndflPerson.personId = personData.get("RECORD_ID")?.getNumberValue() //Идентификатор ФЛ
    ndflPerson.inp = personData.get("RECORD_ID")?.getNumberValue()
    ndflPerson.snils = personData.get("SNILS")?.getStringValue()
    ndflPerson.lastName = personData.get("LAST_NAME")?.getStringValue()
    ndflPerson.firstName = personData.get("FIRST_NAME")?.getStringValue()
    ndflPerson.middleName = personData.get("MIDDLE_NAME")?.getStringValue()
    ndflPerson.birthDay = personData.get("BIRTH_DATE")?.getDateValue()

    //TODO гражданство из справочника?
    ndflPerson.citizenship = currentNdflPerson.citizenship//personData.get("CITIZENSHIP")?.getNumberValue()
    ndflPerson.innNp = personData.get("INN")?.getStringValue()
    ndflPerson.innForeign = personData.get("INN_FOREIGN")?.getStringValue()

    //ДУЛ - заполняется на основе справочника Документы, удостоверяющие личность
    ndflPerson.idDocType = currentNdflPerson.idDocType//docData.get("DOC_ID")?.getStringValue() //Вид документа
    ndflPerson.idDocNumber = currentNdflPerson.idDocNumber
//docData.get("DOC_NUMBER")?.getStringValue() //Серия и номер документа
    ndflPerson.status = currentNdflPerson.status
    ndflPerson.postIndex = currentNdflPerson.postIndex
    ndflPerson.regionCode = currentNdflPerson.regionCode
    ndflPerson.area = currentNdflPerson.area
    ndflPerson.city = currentNdflPerson.city
    ndflPerson.locality = currentNdflPerson.locality
    ndflPerson.street = currentNdflPerson.street
    ndflPerson.house = currentNdflPerson.house
    ndflPerson.building = currentNdflPerson.building
    ndflPerson.flat = currentNdflPerson.flat
    ndflPerson.countryCode = currentNdflPerson.countryCode
    ndflPerson.address = currentNdflPerson.address
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
 * Функция сохраняет данные о доходаф ФЛ
 * @param sources
 * @return
 */
Map collectNdflPerson(List<Relation> sourcesInfo) {

    def result = [:]

    // собрать данные из источников и собратьхраняем в БД
    for (Relation relation : sourcesInfo) {

        println "consolidate from relation declarationDataId=" + relation.declarationDataId

        if (!relation.declarationState.equals(State.ACCEPTED)) {
            logger.error("Декларация-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", подразделение: \"%s\"", relation.getDeclarationTypeName(), relation.getFullDepartmentName())
            continue
        }

        //получаем все NdflPerson из ПНФ
        def ndflPersonList = ndflPersonService.findNdflPerson(relation.declarationDataId)
        for (NdflPerson ndflPerson : ndflPersonList) {
            //ndflPerson c заполненными данными о доходах
            def ndflPersonData = ndflPersonService.get(ndflPerson.id)

            //Ссылка на справочник физлиц
            def personId = ndflPerson.personId

            //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
            if (result.containsKey(personId)) {
                def consolidatePersonData = result.get(personId)
                consolidatePersonData.incomes.addAll(ndflPersonData.incomes)
                consolidatePersonData.deductions.addAll(ndflPersonData.deductions)
                consolidatePersonData.prepayments.addAll(ndflPersonData.prepayments)
            } else {
                result.put(personId, ndflPersonData)
            }

            println "process ndflPerson=" + ndflPerson.id + ", result=" + result.keySet()
        }
    }
    return result;
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

    //Подразделения которые является подчиненным по отношению к ТБ
    def departments = departmentService.getAllChildren(parentDepartmentId)

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

    //Идентификатор созданной декларации
    relation.declarationDataId = primaryDeclarationData.id
    //Вид декларации
    relation.declarationType = declarationTemplate.type

    relation.declarationTypeName = declarationTemplate.type.name

    //Налоговый орган
    relation.taxOrganCode = primaryDeclarationData.taxOrganCode
    //КПП
    relation.kpp = primaryDeclarationData.kpp

    // Статус ЖЦ
    relation.declarationState = primaryDeclarationData.state

    //Идентификатор АСНУ
    relation.asnuId = primaryDeclarationData.asnuId;


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

def generateXml() {
    def declarationDataId = declarationData.getId()
    println "generateXml by ${declarationDataId}"
    def builder = new MarkupBuilder(xml)
    builder.Файл(имя: "100500")
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