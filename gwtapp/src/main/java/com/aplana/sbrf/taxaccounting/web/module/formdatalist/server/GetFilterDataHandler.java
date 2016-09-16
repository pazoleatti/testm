package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetFilterDataHandler  extends AbstractActionHandler<GetFilterData, GetFilterDataResult> {
	
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

    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
	    GetFilterDataResult result = new GetFilterDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();

		List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
		kinds.addAll(dataAccessService.getAvailableFormDataKind(securityService.currentUserInfo(), asList(action.getTaxType())));
		result.setDataKinds(kinds);
	    
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());
        // Доступные подразделения
		result.setDepartments(new ArrayList<Department>(
				departmentService.getRequiredForTreeDepartments(filterValues.getDepartmentIds()).values()));

	    result.setFilterValues(filterValues);
        // Периоды, связанные с доступными подразделениями
        result.setReportPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                new ArrayList<Integer>(filterValues.getDepartmentIds())));

	    FormDataFilter filter = new FormDataFilter();
	    filter.setTaxType(action.getTaxType());
	    result.setDefaultFilter(filter);
        result.setUserDepartmentId(userInfo.getUser().getDepartmentId());

        return result;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
