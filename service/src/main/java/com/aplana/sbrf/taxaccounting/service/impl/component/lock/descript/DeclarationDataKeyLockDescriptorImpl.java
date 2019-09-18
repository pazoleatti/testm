package com.aplana.sbrf.taxaccounting.service.impl.component.lock.descript;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
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
    public String createKeyLockDescription(Long declarationDataId, OperationType operationType) {
        if (operationType.equals(OperationType.LOAD_TRANSPORT_FILE))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.IMPORT_DECLARATION_EXCEL))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.IDENTIFY_PERSON))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.UPDATE_PERSONS_DATA))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.CHECK_DEC))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.ACCEPT_DEC))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.DELETE_DEC))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.CONSOLIDATE))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_DEC))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_TEMPLATE_DEC))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.PDF_DEC)) return createExtendDescription(declarationDataId);
        else if (operationType.equals(OperationType.RETURN_DECLARATION))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EDIT)) return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EDIT_FILE))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_DB))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_PERSON_ALL_DB))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.REPORT_KPP_OKTMO))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_RATE_REPORT))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_PAYMENT_REPORT))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_DETAIL_REPORT))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_XLSX_REPORT))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.RNU_NDFL_2_6_DATA_TXT_REPORT))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.REPORT_2NDFL1))
            return createExtendDescription(declarationDataId);
        else if (operationType.equals(OperationType.REPORT_2NDFL2))
            return createExtendDescription(declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL1))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL2))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_6NDFL))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.DECLARATION_2NDFL_FL))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EXPORT_REPORTS))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.UPDATE_DOC_STATE))
            return createExtendDescription(declarationDataId);
        else if (operationType.equals(OperationType.SEND_EDO))
            return createExtendDescription(declarationDataId);
        else if (operationType.equals(OperationType.TRANSFER))
            return createBaseDescription(declarationDataId);
        else if (operationType.equals(OperationType.EXCEL_UNLOAD_LIST))
            return createBaseDescription(declarationDataId);
        else
            throw new IllegalArgumentException("Unknown operationType type!");
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
