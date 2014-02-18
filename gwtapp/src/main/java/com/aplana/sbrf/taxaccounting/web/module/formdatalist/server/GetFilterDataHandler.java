package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

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

    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
	    GetFilterDataResult res = new GetFilterDataResult();
	    TAUserInfo currentUser = securityService.currentUserInfo();
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(currentUser, action.getTaxType());
        // Доступные подразделения
		res.setDepartments(new ArrayList<Department>(
				departmentService.getRequiredForTreeDepartments(filterValues.getDepartmentIds()).values()));

	    res.setFilterValues(filterValues);
        // Периоды, связанные с доступными подразделениями
	    List<ReportPeriod> periodList = new ArrayList<ReportPeriod>();
	    periodList.addAll(periodService.getOpenForUser(currentUser.getUser(), action.getTaxType()));
	    res.setReportPeriods(periodList);

	    FormDataFilter filter = new FormDataFilter();
	    filter.setTaxType(action.getTaxType());
	    res.setDefaultFilter(filter);

        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
