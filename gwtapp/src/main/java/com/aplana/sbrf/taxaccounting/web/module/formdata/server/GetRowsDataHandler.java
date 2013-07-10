package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetRowsDataHandler extends
		AbstractActionHandler<GetRowsDataAction, GetRowsDataResult> {

	@Autowired
	DataRowService dataRowService;
	@Autowired
	SecurityService securityService;

	public GetRowsDataHandler() {
		super(GetRowsDataAction.class);
	}

	@Override
	public GetRowsDataResult execute(GetRowsDataAction action, ExecutionContext context) throws ActionException {
		GetRowsDataResult result = new GetRowsDataResult();
		TAUserInfo userInfo = securityService.currentUserInfo();
		DataRowRange dataRowRange = new DataRowRange(action.getRange().getOffset(), action.getRange().getLimit());
		result.setDataRows(dataRowService.getDataRows(userInfo, action.getFormDataId(), dataRowRange, false));
		return result;
	}

	@Override
	public void undo(GetRowsDataAction action, GetRowsDataResult result, ExecutionContext context) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
