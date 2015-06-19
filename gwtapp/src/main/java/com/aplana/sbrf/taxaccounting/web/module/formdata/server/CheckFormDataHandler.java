package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

	public CheckFormDataHandler() {
		super(CheckFormDataAction.class);
	}

	@Override
	public TaskFormDataResult execute(CheckFormDataAction action,
			ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.CHECK_FD;
        TaskFormDataResult result = new TaskFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        LockData lockDataEdit = formDataService.getObjectLock(action.getFormData().getId(), userInfo);
        if (!action.isEditMode() || action.isEditMode() && lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
            LockData lockData = lockDataService.getLock(keyTask);
            if (lockType != null && !lockType.getFirst().equals(ReportType.EDIT_FD)) {
                formDataService.locked(action.getFormData().getId(), reportType, lockType, logger);
            }
            if (lockData != null) {
                if (lockData.getUserId() == userInfo.getUser().getId()) {
                    if (action.isForce()) {
                        // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                        lockDataService.interruptTask(lockData, userInfo.getUser().getId(), false);
                    } else {
                        // вызов диалога
                        result.setLock(true);
                        String restartMsg = (lockData.getState().equals(LockData.State.IN_QUEUE.getText())) ?
                                String.format(LockData.CANCEL_MSG, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo)) :
                                String.format(LockData.RESTART_MSG, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo));
                        result.setRestartMsg(restartMsg);
                        return result;
                    }
                } else {
                    // добавление подписчика
                    try {
                        lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                        logger.info(String.format(LockData.LOCK_INFO_MSG,
                                        formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo)),
                                sdf.format(lockData.getDateLock()),
                                userService.getUser(lockData.getUserId()).getName());
                    } catch (ServiceException e) {
                    }
                    result.setLock(false);
                    logger.info(String.format(ReportType.CREATE_TASK, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo)));
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }
            }
            result.setLock(false);

            if (action.isEditMode()) {
                if (!action.getModifiedRows().isEmpty()) {
                    refBookHelper.dataRowsCheck(action.getModifiedRows(), formData.getFormColumns());
                    dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
                }
                // проверка наличия не сохраненных изменений
                if (!dataRowService.compareRows(formData)) {
                    if (action.isSave()) {
                        // сохраняем данные при нажантии "Да"
                        formDataService.saveFormData(logger, securityService.currentUserInfo(), formData);
                        dataRowService.createTemporary(formData);
                    } else {
                        lockDataService.unlock(keyTask, userInfo.getUser().getId());
                        // Вызов диалога, для подтверждения сохранения данных
                        result.setSave(true);
                        return result;
                    }
                }
            }
            result.setSave(false);

            if (lockDataService.lock(keyTask,
                    userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormData().getId(), null, reportType),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormData().getId());
                    params.put("manual", action.getFormData().isManual());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
                    lockData = lockDataService.getLock(keyTask);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(keyTask, lockData.getDateLock(), balancingVariant);
                    logger.info(String.format(ReportType.CREATE_TASK, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo)));
                } catch (Exception e) {
                    lockDataService.unlock(keyTask, userInfo.getUser().getId());
                    if (e instanceof ServiceLoggerException) {
                        throw (ServiceLoggerException) e;
                    } else {
                        throw new ActionException(e);
                    }
                }
            } else {
                throw new ActionException("Не удалось запустить расчет. Попробуйте выполнить операцию позже");
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
