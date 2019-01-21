package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.component.operation.DeclarationDataReportingSingleModeAsyncTaskDescriptor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;

@Component
public class DeclarationDataReportingSingleModeAsyncTaskDescriptorImpl implements DeclarationDataReportingSingleModeAsyncTaskDescriptor{
    private DeclarationDataService declarationDataService;
    private DepartmentService departmentService;
    private DeclarationTemplateService declarationTemplateService;
    private DepartmentReportPeriodService departmentReportPeriodService;

    public DeclarationDataReportingSingleModeAsyncTaskDescriptorImpl(DeclarationDataService declarationDataService, DepartmentService departmentService, DeclarationTemplateService declarationTemplateService, DepartmentReportPeriodService departmentReportPeriodService) {
        this.declarationDataService = declarationDataService;
        this.departmentService = departmentService;
        this.declarationTemplateService = declarationTemplateService;
        this.departmentReportPeriodService = departmentReportPeriodService;
    }

    @Override
    public String createDescription(Long declarationDataId, String name) {
        DeclarationData declarationData = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        return String.format("%s. Вид: \"%s\", № %s, Период: \"%s, %s%s\", Подразделение: \"%s\", Налоговый орган: \"%s\", КПП: \"%s\", ОКТМО: \"%s\"",
                name,
                declarationType.getName(),
                declarationDataId,
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(departmentReportPeriod),
                department.getName(),
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
