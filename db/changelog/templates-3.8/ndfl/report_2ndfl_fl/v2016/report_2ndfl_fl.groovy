package form_template.ndfl.report_2ndfl_fl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.action.Create2NdflFLAction
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.service.LockDataService
import com.aplana.sbrf.taxaccounting.service.ReportService
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeductionTypeService
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

new Report2NdflFL(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class Report2NdflFL extends AbstractScriptClass {

    DeclarationData declarationData
    DeclarationTemplate declarationTemplate
    DepartmentReportPeriod departmentReportPeriod
    ReportPeriod reportPeriod
    Department department
    NdflPersonService ndflPersonService
    RefBookFactory refBookFactory
    ReportPeriodService reportPeriodService
    DepartmentService departmentService
    DeclarationLocker declarationLocker
    LockDataService lockDataService
    DepartmentReportPeriodService departmentReportPeriodService
    DepartmentConfigService departmentConfigService
    SourceService sourceService
    ReportService reportService
    RefBookService refBookService
    BlobDataService blobDataService
    RefBookDeductionTypeService refBookDeductionTypeService

    Create2NdflFLAction createParams;

    @TypeChecked(TypeCheckingMode.SKIP)
    Report2NdflFL(scriptClass) {
        super(scriptClass)
        this.departmentReportPeriodService = (DepartmentReportPeriodService) getSafeProperty("departmentReportPeriodService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.departmentService = (DepartmentService) getSafeProperty("departmentService")
        this.reportPeriodService = (ReportPeriodService) getSafeProperty("reportPeriodService")
        this.declarationData = (DeclarationData) getSafeProperty("declarationData")
        if (this.declarationData) {
            this.declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        }
        this.ndflPersonService = (NdflPersonService) getSafeProperty("ndflPersonService")
        this.departmentConfigService = (DepartmentConfigService) getSafeProperty("departmentConfigService")
        this.sourceService = (SourceService) getSafeProperty("sourceService")
        this.reportService = (ReportService) getSafeProperty("reportService")
        this.refBookFactory = (RefBookFactory) getSafeProperty("refBookFactory")
        this.refBookService = (RefBookService) getSafeProperty("refBookService")
        this.blobDataService = (BlobDataService) getSafeProperty("blobDataServiceDaoImpl")
        this.declarationLocker = (DeclarationLocker) getSafeProperty("declarationLocker")
        this.lockDataService = (LockDataService) getSafeProperty("lockDataService")
        this.refBookDeductionTypeService = (RefBookDeductionTypeService) getSafeProperty("refBookDeductionTypeService")

        this.createParams = (Create2NdflFLAction) getSafeProperty("createParams")
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.CREATE_FORMS:
                createForms()
                break
        }
    }

    void createForms() {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(createParams.getDepartmentId(), createParams.getReportPeriodId());
        if (departmentReportPeriod != null) {
            int activeTemplateId = declarationService.getActiveDeclarationTemplateId(createParams.getDeclarationTypeId(), createParams.getReportPeriodId());
            DeclarationData declarationData = new DeclarationData();
            declarationData.setDeclarationTemplateId(activeTemplateId);
            declarationData.setDepartmentReportPeriodId(departmentReportPeriod.getId());
            declarationData.setDepartmentId(createParams.getDepartmentId());
            declarationData.setReportPeriodId(createParams.getReportPeriodId());
            declarationData.setPersonId(createParams.getPersonId());
            declarationData.setSignatory(createParams.getSignatory());
            declarationData.setState(State.ISSUED);
            declarationService.createWithoutChecks(declarationData, logger, userInfo, true);
        } else {
            logger.error("Указанный период не создан в системе");
        }
    }
}



