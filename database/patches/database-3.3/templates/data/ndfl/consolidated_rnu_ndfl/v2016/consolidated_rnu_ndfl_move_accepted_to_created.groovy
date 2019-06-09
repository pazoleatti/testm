package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import com.aplana.sbrf.taxaccounting.script.service.*

new MoveAcceptedToCreated(this).run()

@TypeChecked
class MoveAcceptedToCreated extends AbstractScriptClass {

    ReportPeriodService reportPeriodService
    DeclarationData theDeclaration
    DepartmentService departmentService
    DepartmentReportPeriodService departmentReportPeriodService
    SourceService sourceService

    private MoveAcceptedToCreated() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    MoveAcceptedToCreated(scriptClass) {
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
            case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
                checkTheDeclarationHasNoModificationConflictsWithDestinations()
        }
    }

    // переменные, кэширующие данные из базы
    private Department declarationDepartment, declarationTerbank
    private List<Relation> conflictingDestinations

    /**
     * Проверка, можно ли редактировать декларацию, не вступит ли это в конфликт с формами-приёмниками
     */
    def checkTheDeclarationHasNoModificationConflictsWithDestinations() {
        if (theDeclaration.is(State.ACCEPTED) && thereIsConflictingDestinations()) {
            printConflictMessage()
        }
    }

    private boolean thereIsConflictingDestinations() {
        return conflictingDestinations()
    }

    private void printConflictMessage() {
        printSameTerbankError()
    }

    private void printSameTerbankError() {
        logger.error("Не выполнена операция \"Возврат в Создана\" для налоговой формы: № $theDeclaration.id, " +
                "Период: \"${theDeclarationPeriodName()}\", Подразделение: \"${theDeclarationDepartment().shortName}\". " +
                "Причина: Одна или несколько налоговых форм - приемников в Тербанке ${theDeclarationTerbank().shortName} находятся в статусе, " +
                "отличном от \"Создана\""
        )
    }

    // метод с кэшем
    private Department theDeclarationTerbank() {
        if (!declarationTerbank) {
            declarationTerbank = departmentService.getParentTB(theDeclaration.departmentId)
        }
        return declarationTerbank
    }

    // метод с кэшем
    private Department theDeclarationDepartment() {
        if (!declarationDepartment) {
            declarationDepartment = departmentService.get(theDeclaration.departmentId)
        }
        return declarationDepartment
    }

    // метод с кэшем
    private List<Relation> conflictingDestinations() {
        if (!conflictingDestinations) {
            List<Relation> destinations = sourceService.getDestinationsInfo(theDeclaration)
            conflictingDestinations = destinations.findAll { it.declarationState != State.CREATED }
        }
        return conflictingDestinations
    }

    private String theDeclarationPeriodName() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(theDeclaration.departmentReportPeriodId)
        int periodYear = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = departmentReportPeriod.reportPeriod.name
        String correctionDate = departmentReportPeriod.correctionDate ? " (корр. ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")})" : ""
        return periodYear + ", " + periodName + correctionDate
    }
}