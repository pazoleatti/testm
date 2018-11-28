package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
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
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
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
                checkDeclarationHasNoConflictingDestinations()
        }
    }

    /**
     * Проверяем, нет ли конфликтов с формами-приёмниками при возврате в "Создана"
     */
    def checkDeclarationHasNoConflictingDestinations() {

        // Если декларация не "Принята", то её не надо проверять
        if (declarationData.state != State.ACCEPTED) {
            return
        }

        // Проверяем, все ли формы-приёмники находятся в состоянии "Создана"
        List<Relation> destinations = sourceService.getDestinationsInfo(declarationData)
        List<Relation> notStateCreatedDestinations = destinations.findAll { it.declarationState != State.CREATED }
        // Все формы-приёмники "Созданы", проверка пройдена
        if (notStateCreatedDestinations.isEmpty()) {
            return
        }

        // Имеются формы-приёмники в ином состоянии, формируем предупреждения или ошибки.

        // ТБ декларации
        Department declarationTB = departmentService.getParentTB(declarationData.departmentId)
        logger.info("ТБ декларации: $declarationTB.id")

        // Множество id тербанков форм-приёмников
        Set<Integer> destinationTBIds = notStateCreatedDestinations.collect {
            departmentService.getParentTBId(it.departmentId)
        }.toSet()
        logger.info("ТБ приёмников: ${destinationTBIds.join(", ")}")

        // Если есть форма-приёмник из того же тербанка, что декларация, выдаём ошибку, иначе предупреждение
        MessageKind messageKind = (declarationTB.id in destinationTBIds) ? MessageKind.SAME_TB_ERROR : MessageKind.OTHER_TB_WARNING
        logDeclarationHasNotAcceptedDestinations(messageKind)
    }

    private enum MessageKind {
        OTHER_TB_WARNING, SAME_TB_ERROR
    }

    // Логгирование ситуации возникновения ошибок
    private void logDeclarationHasNotAcceptedDestinations(MessageKind messageKind) {
        // Данные для вывода в логгер
        Department declarationDepartment = departmentService.get(declarationData.departmentId)
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
        int periodYear = departmentReportPeriod.reportPeriod.taxPeriod.year
        String periodName = departmentReportPeriod.reportPeriod.name
        String correctionDate = departmentReportPeriod.correctionDate ? " (корр. ${departmentReportPeriod.correctionDate.format("dd.MM.yyyy")})" : ""

        if (messageKind == MessageKind.SAME_TB_ERROR) {
            Department declarationTB = departmentService.getParentTB(declarationData.departmentId)
            logger.error("Не выполнена операция \"Возврат в Создана\" для налоговой формы: № $declarationData.id, " +
                    "Период: \"$periodYear, $periodName$correctionDate\", Подразделение: \"$declarationDepartment.shortName\". " +
                    "Причина: Одна или несколько налоговых форм - приемников в ТБ $declarationTB.shortName находятся в статусе, " +
                    "отличном от \"Создана\""
            )
        } else {
            logger.warn("Для налоговой формы № $declarationData.id, Период: \"$periodYear, $periodName$correctionDate\", " +
                    "Подразделение: \"$declarationDepartment.shortName\" в других ТБ имеются налоговые формы-приемники," +
                    " статус которых отличен от \"Создана\"")
        }
    }
}