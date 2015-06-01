package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateDataRowsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Eugene Stetsenko Обработчик запроса для пересчета формы.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateFormDataHandler extends AbstractActionHandler<RecalculateDataRowsAction, DataRowResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

	public RecalculateFormDataHandler() {
		super(RecalculateDataRowsAction.class);
	}

	@Override
	public DataRowResult execute(RecalculateDataRowsAction action,
			ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.CALCULATE_FD;
        DataRowResult result = new DataRowResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
        if (lockType != null && ReportType.EDIT_FD.equals(lockType.getFirst())) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            LockData lockData = lockDataService.lock(keyTask,
                        userInfo.getUser().getId(),
                        formDataService.getFormDataFullName(action.getFormData().getId(), null, reportType),
                        lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)
                    );
            if (lockData == null) {
                try {
                    if (!action.getModifiedRows().isEmpty()) {
                        dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
                    }
                    formDataService.doCalc(logger, userInfo, formData);
                } finally {
                    lockDataService.unlock(keyTask, userInfo.getUser().getId());
                }
            } else {
                throw new ActionException("Не удалось запустить расчет. Попробуйте выполнить операцию позже");
            }
        } else {
            formDataService.locked(lockType.getSecond(), logger);
        }
		result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(RecalculateDataRowsAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
