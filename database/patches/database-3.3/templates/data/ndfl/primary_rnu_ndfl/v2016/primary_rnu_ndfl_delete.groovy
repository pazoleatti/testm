package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.State
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.SourceService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new Delete(this).run()

@TypeChecked
class Delete extends AbstractScriptClass {

    ReportPeriodService reportPeriodService
    DeclarationData theDeclaration
    DepartmentService departmentService
    DepartmentReportPeriodService departmentReportPeriodService
    SourceService sourceService

    private Delete() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Delete(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.theDeclaration = (DeclarationData) scriptClass.getProperty("declarationData")
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("sourceService")) {
            this.sourceService = (SourceService) scriptClass.getProperty("sourceService")
        }
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.DELETE:
                try {
                    checkTheDeclarationHasNoModificationConflictsWithDestinations()
                } catch (Throwable e) {
                    scriptClass.getBinding().setProperty("exceptionThrown", e)
                    e.printStackTrace()
                }
        }
    }

    // ????????????????????, ???????????????????? ???????????? ???? ????????
    private Department declarationDepartment, declarationTerbank
    private List<Relation> conflictingDestinations
    private List<Department> conflictingDestinationsTerbanks

    /**
     * ????????????????, ?????????? ???? ?????????????????????????? ????????????????????, ???? ?????????????? ???? ?????? ?? ???????????????? ?? ??????????????-??????????????????????
     */
    def checkTheDeclarationHasNoModificationConflictsWithDestinations() {
        if (thereIsConflictingDestinations()) {
            printConflictMessage()
        }
    }

    private boolean thereIsConflictingDestinations() {
        return conflictingDestinations()
    }

    private void printConflictMessage() {
        if (conflictIsInTheSameTerbank()) {
            // TODO: ???? ????????????????????
            // printSameTerbankError()
        } else {
            printOtherTerbanksWarning()
        }
    }

    private boolean conflictIsInTheSameTerbank() {
        Set<Integer> conflictingTerbankIds = conflictingDestinationsTerbanks().collect { it.id }.toSet()
        return theDeclarationTerbank().id in conflictingTerbankIds
    }

    private void printSameTerbankError() {
        logger.error("")
    }

    private void printOtherTerbanksWarning() {
        String conflictingTerbankNames = conflictingDestinationsTerbanks().collect { it.shortName }.join(", ")

        logger.warn("?????? ?????????????????? ?????????? ??? $theDeclaration.id, ????????????: \"${theDeclarationPeriodName()}\", " +
                "??????????????????????????: \"${theDeclarationDepartment().shortName}\", ??????????????: ${theDeclarationTerbank().shortName} " +
                "?? ???????????? ?????????????????? $conflictingTerbankNames ?????????????? ?????????????????? ??????????-??????????????????, ???????????? ?????????????? ?????????????? ???? \"??????????????\""
        )
    }

    // ?????????? ?? ??????????
    private Department theDeclarationTerbank() {
        if (!declarationTerbank) {
            declarationTerbank = departmentService.getParentTB(theDeclaration.departmentId)
        }
        return declarationTerbank
    }

    // ?????????? ?? ??????????
    private Department theDeclarationDepartment() {
        if (!declarationDepartment) {
            declarationDepartment = departmentService.get(theDeclaration.departmentId)
        }
        return declarationDepartment
    }

    // ?????????? ?? ??????????
    private List<Relation> conflictingDestinations() {
        if (!conflictingDestinations) {
            List<Relation> destinations = sourceService.getDestinationsInfo(theDeclaration)
            conflictingDestinations = destinations.findAll { it.declarationState != State.CREATED }
        }
        return conflictingDestinations
    }

    // ?????????? ?? ??????????
    private List<Department> conflictingDestinationsTerbanks() {
        if (!conflictingDestinationsTerbanks) {
            conflictingDestinationsTerbanks = conflictingDestinations().collect {
                departmentService.getParentTB(it.departmentId)
            }
        }
        return conflictingDestinationsTerbanks
    }

    private String theDeclarationPeriodName() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(theDeclaration.departmentReportPeriodId)
        int periodYear = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = departmentReportPeriod.reportPeriod.name
        String correctionDate = departmentReportPeriod.correctionDate ? " (????????. ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")})" : ""
        return periodYear + ", " + periodName + correctionDate
    }
}