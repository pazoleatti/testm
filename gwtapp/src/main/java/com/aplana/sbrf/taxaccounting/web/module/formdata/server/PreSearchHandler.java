package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.PreSearchAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TaskFormDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Stetsenko Обработчик сохранения перед поиском.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class PreSearchHandler extends AbstractActionHandler<PreSearchAction, DataRowResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;

    @Autowired
    private RefBookHelper refBookHelper;

	public PreSearchHandler() {
		super(PreSearchAction.class);
	}

	@Override
	public DataRowResult execute(PreSearchAction action,
			ExecutionContext context) throws ActionException {
        FormData formData = action.getFormData();
        if (!action.getModifiedRows().isEmpty()) {
            refBookHelper.dataRowsCheck(action.getModifiedRows(), formData.getFormColumns());
            dataRowService.update(securityService.currentUserInfo(), formData.getId(), action.getModifiedRows(), formData.isManual());
        }
        return new DataRowResult();
	}

	@Override
	public void undo(PreSearchAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
