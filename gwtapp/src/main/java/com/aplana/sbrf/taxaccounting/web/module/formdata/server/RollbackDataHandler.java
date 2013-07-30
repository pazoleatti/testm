package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RollbackDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RollbackDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Откат изменений в таблице
 */
@Service
public class RollbackDataHandler extends
		AbstractActionHandler<RollbackDataAction, RollbackDataResult> {

	@Autowired
	DataRowDao dataRowDao;

	public RollbackDataHandler() {
		super(RollbackDataAction.class);
	}

	@Override
	public RollbackDataResult execute(RollbackDataAction action, ExecutionContext context) throws ActionException {
		dataRowDao.rollback(action.getFormDataId());
		return new RollbackDataResult();
	}

	@Override
	public void undo(RollbackDataAction action, RollbackDataResult result, ExecutionContext context) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
