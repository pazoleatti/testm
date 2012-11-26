package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
/**
 * 
 * @author Eugene Stetsenko
 * Обработчик запроса для удаления формы.
 *
 */
@Service
public class DeleteFormHandler extends AbstractActionHandler<DeleteFormAction, DeleteFormResult> {
	
	@Autowired
	private FormDataDao formDataDao;
	
	public DeleteFormHandler() {
		super(DeleteFormAction.class);
	}
	
	@Override
	public DeleteFormResult execute(DeleteFormAction action, ExecutionContext context) throws ActionException {
		formDataDao.delete(action.getFormDataId());
		return new DeleteFormResult();
	}

	@Override
	public void undo(DeleteFormAction action, DeleteFormResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
