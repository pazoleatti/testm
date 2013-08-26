package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFilterDataHandler  extends AbstractActionHandler<GetFilterData, GetFilterDataResult> {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private ReportPeriodService reportPeriodService;

    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
	    GetFilterDataResult res = new GetFilterDataResult();
	    FormDataFilterAvailableValues filterValues = formDataSearchService.getAvailableFilterValues(securityService
			    .currentUserInfo(), action.getTaxType());

	    if(filterValues.getDepartmentIds() == null) {
		    //Контролер УНП
		    res.setDepartments(departmentService.listAll());
	    } else {
		    //Контролер или Оператор
		    res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(filterValues
				    .getDepartmentIds()).values()));
	    }
	    res.setFilterValues(filterValues);
	    res.setTaxPeriods(reportPeriodService.listByTaxType(action.getTaxType()));
	    res.setCurrentReportPeriod(null);

        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }

}
