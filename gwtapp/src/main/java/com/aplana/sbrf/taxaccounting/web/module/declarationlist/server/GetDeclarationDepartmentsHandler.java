package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationDepartmentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDeclarationDepartmentsHandler extends AbstractActionHandler<GetDeclarationDepartmentsAction, GetDeclarationDepartmentsResult> {

    public GetDeclarationDepartmentsHandler() {
        super(GetDeclarationDepartmentsAction.class);
    }

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    public GetDeclarationDepartmentsResult execute(GetDeclarationDepartmentsAction action, ExecutionContext context) throws ActionException {
        GetDeclarationDepartmentsResult result = new GetDeclarationDepartmentsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();

        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(departmentService.getBankDepartment().getId()));
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(action.getReportPeriodId()));
        departmentReportPeriodFilter.setTaxTypeList(Arrays.asList(action.getTaxType()));
        result.setDepartmentReportPeriods(toModel(departmentReportPeriodService.getListByFilter(departmentReportPeriodFilter)));

        // Доступные подразделения
        List<Integer> departments;
        if (action.isCreate()) {
            departments = departmentService.getOpenPeriodDepartments(userInfo.getUser(), action.getTaxType(), action.getReportPeriodId());
        } else {
            departments = departmentService.getNDFLDeclarationDepartments(userInfo.getUser());
        }
        if (departments.isEmpty()){
            result.setDepartments(new ArrayList<Department>());
            result.setDepartmentIds(new HashSet<Integer>());
        } else {
            if (action.isReports()) {
                // Отчеты создаются в только в ТБ
                Set<Integer> departmentIds = new HashSet<Integer>(departmentService.getTBDepartmentIds(userInfo.getUser(), action.getTaxType()));
                result.setDepartments(new ArrayList<Department>(
                        departmentService.getRequiredForTreeDepartments(departmentIds).values()));
                result.setDepartmentIds(departmentIds);
                int userTBId = departmentService.getParentTB(userInfo.getUser().getDepartmentId()).getId();
                if (result.getDepartmentIds().contains(userTBId)) {
                    result.setDefaultDepartmentId(userTBId);
                }
            } else {
                Set<Integer> departmentIds = new HashSet<Integer>(departments);
                result.setDepartments(new ArrayList<Department>(
                        departmentService.getRequiredForTreeDepartments(departmentIds).values()));
                result.setDepartmentIds(departmentIds);
                if (result.getDepartmentIds().contains(userInfo.getUser().getDepartmentId())) {
                    result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
                }
            }
        }
        return result;
    }

    private List<DepartmentReportPeriodViewModel> toModel(List<DepartmentReportPeriod> reportPeriods) {
        List<DepartmentReportPeriodViewModel> periodViewModels = new ArrayList<>();
        for (DepartmentReportPeriod period : reportPeriods){
            periodViewModels.add(new DepartmentReportPeriodViewModel(period.getId(), toReportPeriodViewModel(period.getReportPeriod()), period.getDepartmentId(), period.isActive(), period.getCorrectionDate().toDate()));
        }
        return periodViewModels;
    }

    public ReportPeriodViewModel toReportPeriodViewModel(ReportPeriod selectedPeriod) {
        ReportPeriodViewModel reportPeriod = new ReportPeriodViewModel();
        reportPeriod.setId(selectedPeriod.getId());
        reportPeriod.setName(selectedPeriod.getName());
        reportPeriod.setAccName(selectedPeriod.getAccName());
        reportPeriod.setOrder(selectedPeriod.getOrder());
        reportPeriod.setTaxPeriod(selectedPeriod.getTaxPeriod());
        reportPeriod.setStartDate(selectedPeriod.getStartDate().toDate());
        reportPeriod.setEndDate(selectedPeriod.getEndDate().toDate());
        reportPeriod.setCalendarStartDate(selectedPeriod.getCalendarStartDate().toDate());
        reportPeriod.setDictTaxPeriodId(selectedPeriod.getDictTaxPeriodId());
        return reportPeriod;
    }

    @Override
    public void undo(GetDeclarationDepartmentsAction action, GetDeclarationDepartmentsResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
