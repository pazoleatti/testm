package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Stetsenko Обработчик запроса для пересчета формы.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RecalculateFormDataHandler extends AbstractActionHandler<RecalculateDataRowsAction, RecalculateFormDataResult> {

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

    @Autowired
    private AsyncManager asyncManager;


	public RecalculateFormDataHandler() {
		super(RecalculateDataRowsAction.class);
	}

	@Override
	public RecalculateFormDataResult execute(RecalculateDataRowsAction action,
			ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.CALCULATE_FD;
        RecalculateFormDataResult result = new RecalculateFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        LockData lockDataEdit = formDataService.getObjectLock(action.getFormData().getId(), userInfo);
        if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
            if (lockType != null) {
                if (lockType.getFirst().equals(ReportType.EDIT_FD)) {
                    // всё номально, продолжаем выполнение
                } else if (lockType.getFirst().equals(ReportType.CALCULATE_FD)) {
                    if (lockType.getSecond().getUserId() == userInfo.getUser().getId()) {
                        if (action.isForce()) {
                            // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                            lockDataService.interruptTask(lockType.getSecond(), userInfo.getUser().getId(), false);
                        } else {
                            // вызов диалога
                            result.setLock(true);
                            return result;
                        }
                    } else {
                        // добавление подписчика
                        try {
                            lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                        } catch (ServiceException e) {
                        }
                        result.setLock(false);
                        logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getFormData().getFormType().getTaxType());
                        result.setUuid(logEntryService.save(logger.getEntries()));
                        return result;
                    }
                } else {
                    // ошибка
                    formDataService.locked(lockType.getSecond(), logger);
                }
            }
            result.setLock(false);

            if (!action.getModifiedRows().isEmpty()) {
                dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
            }
            // проверка
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
            result.setSave(false);

            if (lockDataService.lock(keyTask,
                    userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormData().getId(), null, reportType),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormData().getId());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
                    LockData lockData = lockDataService.getLock(keyTask);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(keyTask, lockData.getDateLock(), balancingVariant.getName());
                    logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getFormData().getFormType().getTaxType().getDeclarationShortName());
                    formDataService.doCalc(logger, userInfo, formData);
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
	public void undo(RecalculateDataRowsAction action, RecalculateFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
