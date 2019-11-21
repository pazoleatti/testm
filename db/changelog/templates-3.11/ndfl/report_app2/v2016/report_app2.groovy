package form_template.ndfl.report_app2.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate
import com.aplana.sbrf.taxaccounting.model.Department
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.LockData
import com.aplana.sbrf.taxaccounting.model.OperationType
import com.aplana.sbrf.taxaccounting.model.ReportFormsCreationParams
import com.aplana.sbrf.taxaccounting.model.ReportTypeModeEnum
import com.aplana.sbrf.taxaccounting.model.State
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService
import com.aplana.sbrf.taxaccounting.script.service.SourceService
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.LockDataService
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import static java.util.Collections.singletonList

new ReportApp2(this).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class ReportApp2 extends AbstractScriptClass {

    ReportFormsCreationParams reportFormsCreationParams
    NdflPersonService ndflPersonService
    DeclarationTemplate declarationTemplate
    Department department
    DepartmentService departmentService
    DepartmentReportPeriodFormatter departmentReportPeriodFormatter
    DepartmentReportPeriod departmentReportPeriod
    DepartmentReportPeriodService departmentReportPeriodService
    SourceService sourceService
    DeclarationLocker declarationLocker
    LockDataService lockDataService

    DeclarationData declarationData

    DeclarationData sourceKnf

    @TypeChecked(TypeCheckingMode.SKIP)
    ReportApp2(scriptClass) {
        super(scriptClass)
        this.reportFormsCreationParams = (ReportFormsCreationParams) getSafeProperty("reportFormsCreationParams")
        this.ndflPersonService = (NdflPersonService) getSafeProperty("ndflPersonService")
        this.departmentService = (DepartmentService) getSafeProperty("departmentService")
        this.departmentReportPeriodFormatter = (DepartmentReportPeriodFormatter) getSafeProperty("departmentReportPeriodFormatter")
        this.departmentReportPeriodService = (DepartmentReportPeriodService) getSafeProperty("departmentReportPeriodService")
        this.sourceService = (SourceService) getSafeProperty("sourceService")
        this.declarationLocker = (DeclarationLocker) getSafeProperty("declarationLocker")
        this.lockDataService = (LockDataService) getSafeProperty("lockDataService")
        
        Integer declarationTypeId = reportFormsCreationParams.declarationTypeId
        this.declarationTemplate = declarationService.getTemplate(declarationTypeId)

        Integer departmentId = reportFormsCreationParams.departmentId
        this.department = departmentService.get(departmentId)

        Integer departmentReportPeriodId = reportFormsCreationParams.departmentReportPeriodId
        this.departmentReportPeriod = departmentReportPeriodService.get(departmentReportPeriodId)

    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.CREATE_FORMS:
                createApplication2()
        }
    }

    void createApplication2() {
        sourceKnf = declarationService.getDeclarationData(reportFormsCreationParams.sourceKnfId)

        if (!ndflPersonService.incomeExistsByDeclarationId(sourceKnf.id)) {
            logger.error("Отчетность $declarationTemplate.name для $department.name за период ${formatPeriod(departmentReportPeriod)} не сформирована. " +
                    "В форме №: $sourceKnf.id, Период: \"${formatPeriod(departmentReportPeriod)}\", " +
                    "Подразделение: \"$department.name\", Вид: \"${declarationService.getTemplate(sourceKnf.declarationTemplateId).name}\" отсутствуют операции.")
            return
        }

        List<DeclarationData> existingDeclarations = declarationService.findApplication2ByReportYear(departmentReportPeriod.reportPeriod.taxPeriod.year)

        DeclarationData lastSentForm = existingDeclarations.findAll({ it.docStateId != RefBookDocState.NOT_SENT_TO_NP.id}).max({ it.correctionNum })

        declarationData = new DeclarationData()
        declarationData.declarationTemplateId = declarationTemplate.id
        declarationData.docStateId = RefBookDocState.NOT_SENT_TO_NP.id
        declarationData.correctionNum = lastSentForm ? lastSentForm.correctionNum + 1 : 0
        declarationData.departmentReportPeriodId = departmentReportPeriod.id
        declarationData.reportPeriodId = departmentReportPeriod.reportPeriod.id
        declarationData.departmentId = departmentReportPeriod.departmentId
        declarationData.state = State.CREATED

        try {
            buildApp2() // TODO: SBRFNDFL-9078

            if (existingDeclarations.size() > 0
                    && hasDifference(existingDeclarations.first(), declarationData)) {
                logger.warn("Не удалось создать форму \"$declarationTemplate.name\", " +
                        "за период \"${formatPeriod(departmentReportPeriod)}\". " +
                        "Причина: Существует Приложение 2, в заданном периоде с такими же параметрами: " +
                        "№: $declarationData.id, Период: \"${formatPeriod(departmentReportPeriod)}\", " +
                        "Подразделение: \"$department.name\", Вид: \"$declarationTemplate.name\"")
            }
            def formsToDelete = existingDeclarations.findAll({
                it.docStateId != RefBookDocState.NOT_SENT_TO_NP.id &&
                departmentReportPeriodService.get(it.departmentReportPeriodId).isActive()})

            if (deleteForms(formsToDelete)) {
                if (create(declarationData)) {
                    sourceService.addDeclarationConsolidationInfo(declarationData.id, singletonList(sourceKnf.id))
                    logger.info("Успешно выполнено создание отчетной формы №: $declarationData.id, " +
                            "Период: \"${formatPeriod(departmentReportPeriod)}\", " +
                            "Подразделение: \"$department.name\", Вид: \"$declarationTemplate.name\"")
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e)
            logger.warn("Не удалось создать форму \"$declarationTemplate.name\" за период \"${formatPeriod(departmentReportPeriod)}\", " +
                    "подразделение: \"$department.name\", КПП: \"$declarationData.kpp\", ОКТМО: \"$declarationData.oktmo\". Ошибка: $e.message")
        }

    }

    /**
     * Формирует описание периода в виде "<Период.Год> <Период.Наим>[ с датой сдачи корректировки <Период.ДатаКорр>]"
     */
    String formatPeriod(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriodFormatter.getPeriodDescription(departmentReportPeriod)
    }

    boolean create(DeclarationData declaration) {
        ScriptUtils.checkInterrupted()
        Logger localLogger = new Logger()
        localLogger.setLogId(logger.getLogId())
        try {
            declarationService.createWithoutChecks(declaration, localLogger, userInfo, true)
        } finally {
            logger.entries.addAll(localLogger.entries)
        }
        return !localLogger.containsLevel(LogLevel.ERROR)
    }

    boolean deleteForms(List<DeclarationData> formsToDelete) {
        ScriptUtils.checkInterrupted()
        if (formsToDelete && !reportFormsCreationParams.getReportTypeMode().equals(ReportTypeModeEnum.ANNULMENT)) {
            List<LockData> locks = []
            List<DeclarationData> errorForms = []
            try {
                Logger localLogger = new Logger()
                for (def formToDelete : formsToDelete) {
                    LockData lockData = declarationLocker.establishLock(formToDelete.id, OperationType.DELETE_DEC, userInfo, localLogger)
                    if (lockData) {
                        locks.add(lockData)
                    } else {
                        errorForms.add(formToDelete)
                    }
                }
                if (errorForms.isEmpty()) {
                    for (def formToDelete : formsToDelete) {
                        declarationService.delete(formToDelete.id, userInfo)
                    }
                } else {
                    String error = "Не удалось создать форму $declarationTemplate.name, за период ${formatPeriod(departmentReportPeriod)}, " +
                            "подразделение: $department.name, КПП: $declarationData.kpp, ОКТМО: $declarationData.oktmo. Невозможно удалить старые ОНФ:\n"
                    for (def entry : localLogger.entries) {
                        error += entry.message + "\n"
                    }
                    logger.error(error + "Дождитесь завершения выполнения операций, заблокировавших формы или выполните их отмену вручную.")
                    return false
                }
            } finally {
                // удаляем блокировки
                for (LockData lockData : locks) {
                    lockDataService.unlock(lockData.getKey())
                }
            }
        }
        return true
    }

    void buildApp2() {} // TODO: SBRFNDFL-9078 алгоритм формирования Приложения 2

    boolean hasDifference(DeclarationData lastForm, DeclarationData currentForm) { return false } // TODO: SBRFNDFL-9078 метод определения различий ранее созданной и текущей ОНФ

}



