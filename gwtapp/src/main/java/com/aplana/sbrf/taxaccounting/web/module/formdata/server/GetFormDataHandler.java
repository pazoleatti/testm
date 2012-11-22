package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetFormDataHandler extends AbstractActionHandler<GetFormData, GetFormDataResult>{
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private FormDataAccessService accessService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException {
		GetFormDataResult result = new GetFormDataResult();
		FormData formData = formDataDao.get(action.getFormDataId());
		result.setFormData(formData);
		AccessFlags accessFlags = new AccessFlags();
		accessFlags.setCanCreate(true);
		accessFlags.setCanDelete(true);
		accessFlags.setCanEdit(true);
		accessFlags.setCanRead(true);
		result.setAccessFlags(accessFlags);
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
