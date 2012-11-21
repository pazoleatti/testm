package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.DataHandlerService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
	@Autowired
	private DataHandlerService dataHandlerService;

	public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}
	
	@Override
	public GetFormDataListResult execute(GetFormDataList action,	ExecutionContext context) throws ActionException {
		/*TODO: тут нужно получать пользовательский ID и передавать в dataHandlerService в качестве параметра,
		* в рамках прототипа это не делается*/

		List<FormData> formDataList = dataHandlerService.findDataByUserIdAndFilter(0L, action.getDataFilter());
        GetFormDataListResult res = new GetFormDataListResult();
		res.setRecords(formDataList);
		return res;
	}

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
