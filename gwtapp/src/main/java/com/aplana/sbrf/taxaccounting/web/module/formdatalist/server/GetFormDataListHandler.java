package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
	@Autowired
	private FormDataDao formDataDao;

	public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}
	
	@Override
	public GetFormDataListResult execute(GetFormDataList action,	ExecutionContext context) throws ActionException {
		List<FormData> formDataList = formDataDao.getAll();
		GetFormDataListResult res = new GetFormDataListResult();
		res.setRecords(formDataList);
		return res;
	}

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
