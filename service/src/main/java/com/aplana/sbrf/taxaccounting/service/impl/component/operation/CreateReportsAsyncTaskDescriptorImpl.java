package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.component.operation.CreateReportsAsyncTaskDescriptor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public class CreateReportsAsyncTaskDescriptorImpl implements CreateReportsAsyncTaskDescriptor {

    private DepartmentReportPeriodService departmentReportPeriodService;
    private DeclarationTypeDao declarationTypeDao;
    private DepartmentService departmentService;

    public CreateReportsAsyncTaskDescriptorImpl(DepartmentReportPeriodService departmentReportPeriodService, DeclarationTypeDao declarationTypeDao, DepartmentService departmentService) {
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.declarationTypeDao = declarationTypeDao;
        this.departmentService = departmentService;
    }

    @Override
    public String createDescription(Integer departmentReportPeriodId, Integer declarationTypeId) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        DeclarationType declarationType = declarationTypeDao.get(declarationTypeId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

        return String.format("Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s\", \"%s\"%s, Подразделение: \"%s\"",
                declarationType.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                getCorrectionDateString(departmentReportPeriod),
                department.getName());
    }

    private String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        return reportPeriod.getCorrectionDate() != null ?
                String.format(" корр. %s", new SimpleDateFormat("dd.MM.yyyy").format(reportPeriod.getCorrectionDate())) :
                "";
    }
}
