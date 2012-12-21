package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
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
	public GetFormDataListResult execute(GetFormDataList action, ExecutionContext context) throws ActionException {
		if(action == null || action.getFormDataFilter() == null){
			return null;
		}
		GetFormDataListResult res = new GetFormDataListResult();
		PaginatedSearchResult<FormDataSearchResultItem> resultPage = formDataSearchService
				.findDataByUserIdAndFilter(securityService.currentUser(), action.getFormDataFilter());
		res.setTotalCountOfRecords(resultPage.getTotalRecordCount());
		res.setRecords(resultPage.getRecords());
		return res;
	}

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
