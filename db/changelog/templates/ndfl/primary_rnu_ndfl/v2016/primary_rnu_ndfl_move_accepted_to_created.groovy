package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.script.service.*
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new MoveAcceptedToCreated(this).run()

@TypeChecked
class MoveAcceptedToCreated extends AbstractScriptClass {

    ReportPeriodService reportPeriodService
    DeclarationData declarationData
    DepartmentService departmentService
    DepartmentReportPeriodService departmentReportPeriodService

    final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
    final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100

    Map<Integer, DepartmentReportPeriod> departmentReportPeriodMap = [:]
    Map<Integer, DeclarationTemplate> declarationTemplateMap = [:]
    Map<Integer, String> departmentFullNameMap = [:]

    private MoveAcceptedToCreated() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    MoveAcceptedToCreated(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
                moveAcceptedToCreated();
        }
    }

    def moveAcceptedToCreated() {
        List<Relation> destinationInfo = getDestinationInfo(false);
        for (Relation relation : destinationInfo) {
            if (relation.declarationState.equals(State.ACCEPTED)) {
                throw new ServiceException("Ошибка изменения состояния формы. Данная форма не может быть возвращена в состояние 'Создана', так как используется в КНФ с состоянием 'Принята', номер формы: " + relation.declarationDataId);
            }
        }
    }

    List<Relation> getDestinationInfo(boolean isLight) {

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
            Relation relation = getRelation(declarationDataDestination, department, declarationDataReportPeriod, isLight)
            destinationInfo.add(relation)
        }

        return destinationInfo;
    }

    DepartmentReportPeriod getDepartmentReportPeriodById(int id) {
        if (id != null && departmentReportPeriodMap.get(id) == null) {
            departmentReportPeriodMap.put(id, departmentReportPeriodService.get(id))
        }
        return departmentReportPeriodMap.get(id)
    }

    /**
     * Получить запись для источника-приемника.
     *
     * @param declarationData первичная форма
     * @param department подразделение
     * @param period период нф
     * @param monthOrder номер месяца (для ежемесячной формы)
     */
    Relation getRelation(DeclarationData declarationData, Department department, ReportPeriod period, boolean isLight) {

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

    DeclarationTemplate getDeclarationTemplateById(Integer id) {
        if (id != null && declarationTemplateMap.get(id) == null) {
            declarationTemplateMap.put(id, (DeclarationTemplate) declarationService.getTemplate(id))
        }
        return declarationTemplateMap.get(id)
    }

    /** Получить полное название подразделения по id подразделения. */
    String getDepartmentFullName(Integer id) {
        if (departmentFullNameMap.get(id) == null) {
            departmentFullNameMap.put(id, departmentService.getParentsHierarchy(id))
        }
        return departmentFullNameMap.get(id)
    }
}