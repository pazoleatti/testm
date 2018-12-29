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
import com.aplana.sbrf.taxaccounting.service.component.operation.DeclarationDataAsyncTaskDescriptor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;

@Component
public class DeclarationDataAsyncTaskDescriptorImpl implements DeclarationDataAsyncTaskDescriptor {
    private DeclarationDataService declarationDataService;
    private DepartmentService departmentService;
    private DeclarationTemplateService declarationTemplateService;
    private DepartmentReportPeriodService departmentReportPeriodService;

    public DeclarationDataAsyncTaskDescriptorImpl(DeclarationDataService declarationDataService, DepartmentService departmentService, DeclarationTemplateService declarationTemplateService, DepartmentReportPeriodService departmentReportPeriodService) {
        this.declarationDataService = declarationDataService;
        this.departmentService = departmentService;
        this.declarationTemplateService = declarationTemplateService;
        this.departmentReportPeriodService = departmentReportPeriodService;
    }

    @Override
    public String createDescription(Long declarationDataId, String name) {
        DeclarationData declaration = declarationDataService.get(Collections.singletonList(declarationDataId)).get(0);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        DeclarationType declarationType = declarationTemplate.getType();
        return String.format("%s. Налоговая форма: Период: \"%s\", \"%s\"%s, Подразделение: \"%s\" Вид: \"%s\", № %s",
                name,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(reportPeriod),
                department.getName(),
                declarationType.getName(),
                declaration.getId());
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ?
                String.format(" корр. %s", new SimpleDateFormat("dd.MM.yyyy").format(reportPeriod.getCorrectionDate())) :
                "";
    }
}
