package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FillFormFieldsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FillFormFieldsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    private FormDataAccessService formDataAccessService;

    @Override
    public FillFormFieldsResult execute(FillFormFieldsAction action, ExecutionContext executionContext) throws ActionException {
        FillFormFieldsResult result = new FillFormFieldsResult();
        switch (action.getFieldsNum()){
            case FIRST:
                HashSet<Integer> departmentIds = new HashSet<Integer>(departmentService.getTaxFormDepartments(securityService.currentUserInfo().getUser(),
                        asList(action.getTaxType())));
                result.setReportPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                        new ArrayList<Integer>(departmentIds)));
                break;
            case SECOND:
                List<Integer> departments =
                        departmentService.getOpenPeriodDepartments(securityService.currentUserInfo().getUser(), asList(action.getTaxType()), action.getFieldId());
                if (departments.isEmpty()){
                    result.setDepartments(new ArrayList<Department>());
                    result.setDepartmentIds(new HashSet<Integer>());
                } else {
                    departmentIds = new HashSet<Integer>(departments);
                    result.setDepartments(new ArrayList<Department>(
                            departmentService.getRequiredForTreeDepartments(departmentIds).values()));
                    result.setDepartmentIds(departmentIds);
                }
                break;
            case THIRD:
                List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
                kinds.addAll(formDataAccessService.getAvailableFormDataKind(securityService.currentUserInfo(), asList(action.getTaxType())));
                result.setDataKinds(kinds);
                break;
            case FORTH:
                result.setFormTypes(formDataSearchService.getActiveFormTypeInReportPeriod(action.getFieldId(), action.getTaxType()));
                break;
        }

        return result;
    }

    @Override
    public void undo(FillFormFieldsAction action, FillFormFieldsResult result, ExecutionContext executionContext) throws ActionException {

    }
}
