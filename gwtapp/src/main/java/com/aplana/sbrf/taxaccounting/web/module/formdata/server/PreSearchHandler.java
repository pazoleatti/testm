package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.PreSearchAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
        dataRowService.createSearchPartition(action.getSessionId(), action.getFormData().getId());
        return new DataRowResult();
	}

	@Override
	public void undo(PreSearchAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
