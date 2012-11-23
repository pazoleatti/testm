package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
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
	private TAUserDao userDao;
	
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private FormDataAccessService accessService;

	public GetFormDataHandler() {
		super(GetFormData.class);
	}
	
	@Override
	public GetFormDataResult execute(GetFormData action, ExecutionContext context) throws ActionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String login = auth.getName();
		TAUser user = userDao.getUser(login);
		Integer userId = user.getId();
		GetFormDataResult result = new GetFormDataResult();
		FormData formData = formDataDao.get(action.getFormDataId());
		result.setFormData(formData);

		Long formDataId = formData.getId();
		AccessFlags accessFlags = new AccessFlags();
		accessFlags.setCanCreate(false);
		accessFlags.setCanDelete(accessService.canDelete(userId, formDataId));
		accessFlags.setCanEdit(accessService.canEdit(userId, formDataId));
		accessFlags.setCanRead(accessService.canRead(userId, formDataId));
		result.setAccessFlags(accessFlags);
		return result;
	}

	@Override
	public void undo(GetFormData action, GetFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
