package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;

	public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}
	
	@Override
	public GetFormDataListResult execute(GetFormDataList action,	ExecutionContext context) throws ActionException {
        GetFormDataListResult res = new GetFormDataListResult();
		res.setRecords(formDataSearchService.findDataByUserIdAndFilter(securityService.currentUser(),
				action.getFormDataFilter()));
		res.setDepartments(formDataSearchService.listAllDepartmentsByParentDepartmentId(securityService.currentUser()
				.getDepartmentId()));
		res.setReportPeriods(formDataSearchService.listReportPeriodsByTaxType(action.getFormDataFilter().getTaxType()));
		return res;
	}

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
