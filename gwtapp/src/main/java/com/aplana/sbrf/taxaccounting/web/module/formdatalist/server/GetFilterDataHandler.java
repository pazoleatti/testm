package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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
import java.util.Arrays;

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
        TAUserInfo userInfo = securityService.currentUserInfo();
	    
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(userInfo, action.getTaxType());
        // Доступные подразделения
		res.setDepartments(new ArrayList<Department>(
				departmentService.getRequiredForTreeDepartments(filterValues.getDepartmentIds()).values()));

	    res.setFilterValues(filterValues);
        // Периоды, связанные с доступными подразделениями
        res.setReportPeriods(periodService.getPeriodsByTaxTypeAndDepartments(action.getTaxType(),
                new ArrayList<Integer>(filterValues.getDepartmentIds())));

	    FormDataFilter filter = new FormDataFilter();
	    filter.setTaxType(action.getTaxType());
        filter.setDepartmentIds(Arrays.asList(userInfo.getUser().getDepartmentId()));
	    res.setDefaultFilter(filter);

        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
