package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;

import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TaskFormDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Stetsenko Обработчик запроса для проверки формы.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckFormDataHandler extends AbstractActionHandler<CheckFormDataAction, TaskFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;

    @Autowired
    private RefBookHelper refBookHelper;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

	public CheckFormDataHandler() {
		super(CheckFormDataAction.class);
	}

	@Override
	public TaskFormDataResult execute(final CheckFormDataAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.CHECK_FD;
        TaskFormDataResult result = new TaskFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        LockData lockDataEdit = formDataService.getObjectLock(action.getFormData().getId(), userInfo);
        if (!action.isEditMode() || lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
            if (!action.isEditMode() && lockDataEdit != null) {
                formDataService.locked(action.getFormData().getId(), reportType, new Pair<ReportType, LockData>(ReportType.EDIT_FD, lockDataEdit), logger);
            }
            if (lockType != null && !lockType.getFirst().equals(ReportType.EDIT_FD)) {
                formDataService.locked(action.getFormData().getId(), reportType, lockType, logger);
            }
            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo), userInfo, action.isForce(), logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setLock(true);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setLock(false);
            } else {
                result.setLock(false);
				// Если на текущей странице есть измененные строки, то перед "Проверить" надо их синхронизировать с бд.
                if (action.isEditMode() && !action.getModifiedRows().isEmpty()) {
                        refBookHelper.dataRowsCheck(action.getModifiedRows(), formData.getFormColumns());
                        dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
                }

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("formDataId", action.getFormData().getId());
                params.put("manual", action.getFormData().isManual());
                asyncTaskManagerService.createTask(keyTask, reportType, params, false, PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                    @Override
                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                formDataService.getFormDataFullName(action.getFormData().getId(), action.getFormData().isManual(), null, reportType),
                                LockData.State.IN_QUEUE.getText());
                    }

                    @Override
                    public void executePostCheck() {
                    }

                    @Override
                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                        return false;
                    }

                    @Override
                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                    }

                    @Override
                    public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                        return formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo);
                    }
                });
            }
        } else {
            throw new ActionException("Форма не заблокирована текущим пользователем");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(CheckFormDataAction action, TaskFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
