package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetFormDataHandler extends AbstractActionHandler<GetFormData, GetFormDataResult>{
	@Autowired
	private FormDataDao formDataDao;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException {
		GetFormDataResult result = new GetFormDataResult();
		FormData formData = formDataDao.get(action.getFormDataId());
		result.setFormData(formData);
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
