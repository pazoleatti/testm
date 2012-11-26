package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
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
public class DeleteFormDataHandler extends AbstractActionHandler<DeleteFormDataAction, DeleteFormDataResult> {
	
	@Autowired
	private FormDataDao formDataDao;
	
	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}
	
	@Override
	public DeleteFormDataResult execute(DeleteFormDataAction action, ExecutionContext context) throws ActionException {
		formDataDao.delete(action.getFormDataId());
		return new DeleteFormDataResult();
	}

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
