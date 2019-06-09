package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import com.aplana.sbrf.taxaccounting.script.service.*

new MoveAcceptedToCreated(this).run()

@TypeChecked
class MoveAcceptedToCreated extends AbstractScriptClass {

    ReportPeriodService reportPeriodService
    DeclarationData declarationData
    DepartmentService departmentService
    DepartmentReportPeriodService departmentReportPeriodService
    SourceService sourceService

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
        if (scriptClass.getBinding().hasVariable("sourceService")) {
            this.sourceService = (SourceService) scriptClass.getProperty("sourceService")
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
        List<Relation> destinationInfo = sourceService.getDestinationsInfo(declarationData)
        List<Long> notCreatedDestinationIds = new ArrayList<>()
        for (Relation relation : destinationInfo) {
            if (!relation.declarationState.equals(State.CREATED)) {
                notCreatedDestinationIds.add(relation.declarationDataId)
            }
        }
        if (notCreatedDestinationIds.size() != 0) {
            StringBuilder destinationIdsString = new StringBuilder();
            for (int i = 0; i < notCreatedDestinationIds.size(); i++) {
                destinationIdsString.append(notCreatedDestinationIds.get(i))
                if (i < notCreatedDestinationIds.size() - 1) {
                    destinationIdsString.append(", ")
                }
            }
            logger.error(String.format("Отмена принятия текущей формы невозможна. Формы-приёмники %s имеют состояние, отличное от \"Создана\" . Выполните \"Возврат в Создана\" для перечисленных форм и повторите операцию.", destinationIdsString.toString()))
        }
    }

}