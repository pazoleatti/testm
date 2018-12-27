package com.aplana.sbrf.taxaccounting.service.impl.component;

import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.LockTaskType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.component.DeclarationDataKeyLockDescriptor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;

@Component
public class DeclarationDataKeyLockDescriptorImpl implements DeclarationDataKeyLockDescriptor {

    private DeclarationDataService declarationDataService;
    private DepartmentService departmentService;
    private DeclarationTemplateService declarationTemplateService;
    private DepartmentReportPeriodService departmentReportPeriodService;

    public DeclarationDataKeyLockDescriptorImpl(DeclarationDataService declarationDataService, DepartmentService departmentService, DeclarationTemplateService declarationTemplateService, DepartmentReportPeriodService departmentReportPeriodService) {
        this.declarationDataService = declarationDataService;
        this.departmentService = departmentService;
        this.declarationTemplateService = declarationTemplateService;
        this.departmentReportPeriodService = departmentReportPeriodService;
    }

    @Override
    public String createKeyLockDescription(Long declarationDataId, LockTaskType task) {
        if (task.equals(AsyncTaskType.LOAD_TRANSPORT_FILE))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.IMPORT_DECLARATION_EXCEL))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.IDENTIFY_PERSON))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.UPDATE_PERSONS_DATA))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.CHECK_DEC))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.ACCEPT_DEC))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.DELETE_DEC))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.CONSOLIDATE))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.EXCEL_DEC))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.EXCEL_TEMPLATE_DEC))
            return createBaseDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.PDF_DEC)) return createExtendDescription(declarationDataId);
        else if (task.equals(AsyncTaskType.DEPT_NOTICE_DEC))
            return createExtendDescription(declarationDataId);
        else if (task.equals(OperationType.RETURN_DECLARATION))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.EDIT)) return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.EDIT_FILE))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_PERSON_DB))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.REPORT_KPP_OKTMO))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_RATE_REPORT))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_PAYMENT_REPORT))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return createBaseDescription(declarationDataId);
        else if (task.equals(OperationType.REPORT_2NDFL1))
            return createExtendDescription(declarationDataId);
        else if (task.equals(OperationType.REPORT_2NDFL2))
            return createExtendDescription(declarationDataId);
        else
            throw new IllegalArgumentException("Unknown task type!");
    }

    private String createBaseDescription(Long declarationDataId) {
        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        return String.format("Налоговая форма: Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\", № %s",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(departmentReportPeriod),
                department.getName(),
                declarationType.getName(),
                declarationDataId);
    }

    private String createExtendDescription(Long declarationDataId) {
        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        return String.format("Налоговая форма: Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\", № %s, Налоговый орган: \"%s\", КПП: \"%s\", ОКТМО: \"%s\"",
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(departmentReportPeriod),
                department.getName(),
                declarationType.getName(),
                declarationDataId,
                declarationData.getTaxOrganCode(),
                declarationData.getKpp(),
                declarationData.getOktmo());
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ?
                String.format(" корр. %s", new SimpleDateFormat("dd.MM.yyyy").format(reportPeriod.getCorrectionDate())) :
                "";
    }

}
