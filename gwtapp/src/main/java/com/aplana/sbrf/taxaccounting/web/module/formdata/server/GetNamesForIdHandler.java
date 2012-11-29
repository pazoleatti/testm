package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetNamesForIdAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetNamesForIdResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Eugene Stetsenko
 * Получает некоторые имена по ID
 *
 */
@Service
public class GetNamesForIdHandler extends AbstractActionHandler<GetNamesForIdAction, GetNamesForIdResult> {

	@Autowired
	private ReportPeriodDao reportPeriodDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	public GetNamesForIdHandler() {
		super(GetNamesForIdAction.class);
	}
	
	@Override
	public GetNamesForIdResult execute(GetNamesForIdAction action, ExecutionContext context) throws ActionException {
		GetNamesForIdResult result = new GetNamesForIdResult();
		result.setDepartmenName(departmentDao.getDepartment(action.getDepartmentId()).getName());
		result.setReportPeriod(reportPeriodDao.get(action.getReportPeriodId()).getName());
		return result;
	}

	@Override
	public void undo(GetNamesForIdAction action, GetNamesForIdResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
