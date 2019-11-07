package com.aplana.sbrf.taxaccounting.service.impl.component.operation;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.component.operation.CreateReportsAsyncTaskDescriptor;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.springframework.stereotype.Component;

@Component
public class CreateReportsAsyncTaskDescriptorImpl implements CreateReportsAsyncTaskDescriptor {

    private DepartmentReportPeriodService departmentReportPeriodService;
    private PeriodService periodService;
    private DeclarationTypeDao declarationTypeDao;
    private DepartmentService departmentService;
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    public CreateReportsAsyncTaskDescriptorImpl(DepartmentReportPeriodService departmentReportPeriodService,
                                                DeclarationTypeDao declarationTypeDao,
                                                DepartmentService departmentService,
                                                PeriodService periodService,
                                                DepartmentReportPeriodFormatter departmentReportPeriodFormatter) {
        this.departmentReportPeriodService = departmentReportPeriodService;
        this.declarationTypeDao = declarationTypeDao;
        this.departmentService = departmentService;
        this.periodService = periodService;
        this.departmentReportPeriodFormatter = departmentReportPeriodFormatter;
    }

    @Override
    public String createDescription(Integer departmentReportPeriodId, Integer declarationTypeId) {
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        DeclarationType declarationType = declarationTypeDao.get(declarationTypeId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());

        return String.format("Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s\", Подразделение: \"%s\"",
                declarationType.getName(),
                departmentReportPeriodFormatter.getPeriodDescription(departmentReportPeriod),
                department.getName());
    }

    @Override
    public String createShortDescription(Integer reportPeriodId, Integer declarationTypeId) {
        ReportPeriod reportPeriod = periodService.fetchReportPeriod(reportPeriodId);
        DeclarationType declarationType = declarationTypeDao.get(declarationTypeId);

        return String.format("Создание отчетных форм: Вид отчетности: \"%s\", Период: \"%s\", \"%s\"",
                declarationType.getName(),
                reportPeriod.getTaxPeriod().getYear(),
                reportPeriod.getName());
    }
}
