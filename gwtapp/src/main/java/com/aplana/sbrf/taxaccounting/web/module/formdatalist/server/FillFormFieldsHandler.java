package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FillFormFieldsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FillFormFieldsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * Получает активные виды налоговых форм. ПО идее срабатывает после выбора определенных полей.
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class FillFormFieldsHandler extends AbstractActionHandler<FillFormFieldsAction, FillFormFieldsResult> {
    public FillFormFieldsHandler() {
        super(FillFormFieldsAction.class);
    }

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    FormDataAccessService dataAccessService;

    @Autowired
    DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    SourceService sourceService;

    @Override
    public FillFormFieldsResult execute(FillFormFieldsAction action, ExecutionContext executionContext) throws ActionException {
        FillFormFieldsResult result = new FillFormFieldsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        switch (action.getFieldsNum()){
            case FIRST:
	            List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
	            periodList.addAll(periodService.getOpenForUser(userInfo.getUser(), action.getTaxType()));
                result.setReportPeriods(periodList);
                if (action.getReportPeriodId() != null) {
                    // проверяем доступность для пользователя
                    for(ReportPeriod reportPeriod: periodList)
                        if (reportPeriod.getId().equals(action.getReportPeriodId())) {
                            result.setDefaultReportPeriod(reportPeriod);
                        }
                }
                if (result.getDefaultReportPeriod() == null) {
                    if (periodList != null && !periodList.isEmpty()) {
                        ReportPeriod maxPeriod = periodList.get(0);
                        for (ReportPeriod per : periodList) {
                            if (per.getEndDate().after(maxPeriod.getEndDate())) {
                                maxPeriod = per;
                            }
                        }
                        result.setDefaultReportPeriod(maxPeriod);
                    }
                }
                if (result.getDefaultReportPeriod() == null) {
                    result.setDepartments(new ArrayList<Department>());
                    result.setDepartmentIds(new HashSet<Integer>());
                    break;
                }
                action.setFieldId(result.getDefaultReportPeriod().getId());
            case SECOND:
                List<Integer> departments =
                        departmentService.getOpenPeriodDepartments(userInfo.getUser(), asList(action.getTaxType()), action.getFieldId());
                if (departments.isEmpty()){
                    result.setDepartments(new ArrayList<Department>());
                    result.setDepartmentIds(new HashSet<Integer>());
                } else {
                    Set<Integer> departmentIds = new HashSet<Integer>(departments);
                    result.setDepartments(new ArrayList<Department>(
                            departmentService.getRequiredForTreeDepartments(departmentIds).values()));
                    result.setDepartmentIds(departmentIds);
                }
                if (departments.contains(userInfo.getUser().getDepartmentId()))
                    result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
                break;
            case THIRD:
                List<FormDataKind> kinds = new ArrayList<FormDataKind>();
                List<FormTypeKind> formAssigned = sourceService.getFormAssigned(action.getDepartmentId(), action.getTaxType().getCode());
                List<FormDataKind> formDataKindList = dataAccessService.getAvailableFormDataKind(userInfo, asList(action.getTaxType()));
                for (FormTypeKind formTypeKind: formAssigned) {
                    if (!kinds.contains(formTypeKind.getKind()) && formDataKindList.contains(formTypeKind.getKind())) {
                        kinds.add(formTypeKind.getKind());
                    }
                }
                result.setDataKinds(kinds);
                result.setCorrectionDate(departmentReportPeriodService.getLast(action.getDepartmentId().intValue(), action.getReportPeriodId()).getCorrectionDate());
                break;
            case FORTH:
                result.setFormTypes(formDataSearchService.getActiveFormTypeInReportPeriod(action.getDepartmentId().intValue(), action.getFieldId(), action.getTaxType(), securityService.currentUserInfo(), action.getKinds()));
                break;
        }

        return result;
    }

    @Override
    public void undo(FillFormFieldsAction action, FillFormFieldsResult result, ExecutionContext executionContext) throws ActionException {
    }
}
