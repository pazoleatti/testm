package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import groovy.transform.Field
import groovy.util.slurpersupport.NodeChild

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent

import groovy.xml.MarkupBuilder

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK: //Проверить
        logicCheck()
        break
    case FormDataEvent.CALCULATE: //формирование xml
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



//Идентификатор вида деклараций из declaration_type
@Field
def declarationTypeId = 100

/**
 * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
 * Данный метод выполняет вызов скрипта (GET-SOURCES) и
 *
 */
void consolidation() {
    println "Консолидация РНУ-НДФЛ"

    def declarationDataId = declarationData.getId()

    //
    List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, false, false, null, userInfo, logger);

    println "Список ПНФ: " + sourcesInfo

    //Формируем карту ID записи о ФЛ из справочника, объект модели для консолидации
    def map = collectNdflPerson(sourcesInfo);

    println "получили map: " + map

    //разделы в которых идет сплошная нумерация
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    map.each { personId, person ->

        def incomes = person.incomes;
        def deductions = person.deductions;
        def prepayments = person.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued}
        prepayments.sort {a, b -> a.notifDate <=> b.notifDate}

        def consolidatePerson = createNdlPersonFromDictionary(personId, person);

        consolidatePerson.declarationDataId = declarationDataId
        consolidatePerson.incomes = incomes.withIndex().collect { detail, i -> consolidateDetail(detail, incomesRowNum) }
        consolidatePerson.deductions = deductions.withIndex().collect { detail, i -> consolidateDetail(detail, deductionRowNum) }
        consolidatePerson.prepayments = prepayments.withIndex().collect { detail, i -> consolidateDetail(detail, prepaymentRowNum) }

        ndflPersonService.save(consolidatePerson)

    }

}

/**
 * Создает объект NdlPerson заполненный данными из справочника
 */
def createNdlPersonFromDictionary(Long personId){
    //NdflPerson ndflPerson = new NdflPerson()
    //TODO Заполнить из справочника
    ndflPerson.id = null
    return ndflPerson
}

def consolidateDetail(ndflPersonDetail, i){
    ndflPersonDetail.id = null
    ndflPersonDetail.ndflPersonId = null
    ndflPersonDetail.rowNum = i
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

        if (!relation.declarationState.equals(State.ACCEPTED)){
            logger.error("Декларация-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", подразделение: \"%s\"", relation.getDeclarationTypeName(), relation.getFullDepartmentName())
        }

        //получаем все NdflPerson из ПНФ
        def ndflPersonList = ndflPersonService.findNdflPerson(relation.declarationDataId)
        for (NdflPerson ndflPerson : ndflPersonList) {
            //ndflPerson c заполненными данными о доходах
            def ndflPersonData = ndflPersonService.get(ndflPerson.id)

            def personId = ndflPerson.id //TODO Ссылка на справочник должна выстовлятся при проверке, ndflPerson.refBookId
            //Консолидируем данные о доходах ФЛ, в одном разделе
            if (result.containsKey(personId)) {
                def consolidatePersonData = result.get(personId)
                consolidatePersonData.incomes.addAll(ndflPersonData.incomes)
                consolidatePersonData.deductions.addAll(ndflPersonData.deductions)
                consolidatePersonData.prepayments.addAll(ndflPersonData.prepayments)
            } else {
                result.put(personId, ndflPersonData)
            }
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

    println "get source"

    //отчетный период в котором выполняется консолидация
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def reportPeriodId = reportPeriod?.id

    //Идентификатор подразделения по которому формируется консолидированная
    def parentDepartmentId = declarationData.departmentId

    def parentDepartment = departmentService.get(parentDepartmentId)

    //Подразделения которые является подчиненным по отношению к ТБ
    def departments = departmentService.getAllChildren(parentDepartmentId)

    if (parentDepartment != null) {
        departments.add(parentDepartment)
    }

    //Список отчетных периодов которые должны быть включены в консолидированную форму (1 квартал, полугодие, 9 месяцев, год)
    List<ReportPeriod> reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.NDFL, reportPeriod.startDate, reportPeriod.endDate)

    for (Department department : departments) {

        def departmentId = department.id

        for (ReportPeriod primaryReportPeriod: reportPeriodList){

            List<DeclarationData> primaryDeclarationDataList = declarationService.findAllLastDeclarationData(departmentId, primaryReportPeriod)

            for (DeclarationData primaryDeclarationData: primaryDeclarationDataList){
                //Формируем связь источник-приемник
                def relation = getRelation(primaryDeclarationData, department, reportPeriod)
                println "Сформировали связь: ${relation}"
                sources.sourceList.add(relation)
            }
        }
    }

    sources.sourcesProcessedByScript = true
}

List<DeclarationData> findAllLastDeclarationData(departmentId, reportPeriodId){
    List<DeclarationData> result = new ArrayList<DeclarationData>()
    def declarationDataList = declarationService.findAllDeclarationData(declarationTypeId, departmentId, reportPeriodId)
    def periodId;
    for (DeclarationData dd: declarationDataList){
        if (periodId != null && periodId != dd.departmentReportPeriodId){
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

def generateXml(){
    def declarationDataId = declarationData.getId()
    println "generateXml by ${declarationDataId}"
    def builder = new MarkupBuilder(xml)
    builder.Файл(имя: "100500")
}


def format(date){
    return date?.format('dd.MM.yyyy')
}

def createSpecificReport(){
    println "createSpecificReport"
    def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.getSubreportParamValues().each { k, v ->
        writer.write(k + "::" + v + "\n")
    }
    writer.close()
    scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias() + ".txt")
}








