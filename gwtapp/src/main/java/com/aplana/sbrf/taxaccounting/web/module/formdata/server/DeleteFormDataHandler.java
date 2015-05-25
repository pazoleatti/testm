package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для удаления формы.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteFormDataHandler extends AbstractActionHandler<DeleteFormDataAction, DeleteFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}

    @Override
    public DeleteFormDataResult execute(DeleteFormDataAction action, ExecutionContext context) throws ActionException {
        // Нажатие на кнопку "Удалить" http://conf.aplana.com/pages/viewpage.action?pageId=11384485
        DeleteFormDataResult result = new DeleteFormDataResult();
        Logger logger = new Logger();
        FormData formData = action.getFormData();

        // Версия ручного ввода удаляется без проверок
        if (action.isManual()) {
            formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), true);
            return result;
        }

        // Удаление автоматической версии
        if (formData.getState() != WorkflowState.CREATED){
            throw new ServiceLoggerException("НФ не может быть удалена, так находится в статусе, отличном от \"Создана\"!", null);
        }
        formDataService.deleteFormData(logger, securityService.currentUserInfo(), action.getFormDataId(), false);
        return result;
    }

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
