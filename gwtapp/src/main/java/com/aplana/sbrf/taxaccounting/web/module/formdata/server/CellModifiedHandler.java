package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CellModifiedAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CellModifiedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CellModifiedHandler extends
		AbstractActionHandler<CellModifiedAction, CellModifiedResult> {

	@Autowired
	private DataRowService dataRowService;
	@Autowired
	private SecurityService securityService;

	public CellModifiedHandler() {
		super(CellModifiedAction.class);
	}

	@Override
	public CellModifiedResult execute(CellModifiedAction action, ExecutionContext context) throws ActionException {
		CellModifiedResult result = new CellModifiedResult();
		TAUserInfo userInfo = securityService.currentUserInfo();
		dataRowService.update(userInfo, action.getFormDataId(), action.getDataRows());
		return result;
	}

	@Override
	public void undo(CellModifiedAction action, CellModifiedResult result, ExecutionContext context) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
