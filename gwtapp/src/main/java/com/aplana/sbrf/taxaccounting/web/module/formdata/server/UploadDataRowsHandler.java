package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadFormDataResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadDataRowsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: avanteev
 * Хэнлер для обработки загруженного в файловой хранилище файла.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UploadDataRowsHandler extends
        AbstractActionHandler<UploadDataRowsAction, UploadFormDataResult> {

    @Autowired
    private BlobDataService blobDataService;

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

    public UploadDataRowsHandler() {
        super(UploadDataRowsAction.class);
    }

    @Override
    public UploadFormDataResult execute(UploadDataRowsAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.IMPORT_FD;
        UploadFormDataResult result = new UploadFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        LockData lockDataEdit = formDataService.getObjectLock(action.getFormData().getId(), userInfo);
        if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
            if (lockType != null) {
                if (lockType.getFirst().equals(ReportType.EDIT_FD)) {
                    // всё нормально, продолжаем выполнение
                } else if (lockType.getFirst().equals(reportType)) {
                    if (lockType.getSecond().getUserId() == userInfo.getUser().getId()) {
                        if (action.isForce()) {
                            // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                            lockDataService.interruptTask(lockType.getSecond(), userInfo.getUser().getId(), false);
                        } else {
                            // вызов диалога
                            result.setLock(true);
                            lockDataService.lockInfo(lockType.getSecond(), logger);
                            result.setUuid(logEntryService.save(logger.getEntries()));
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
            result.setSave(false);

            if (lockDataService.lock(keyTask,
                    userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormData().getId(), null, reportType),
                    LockData.State.IN_QUEUE.getText(),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                try {
                    List<String> lockKeys = new ArrayList<String>();
                    lockKeys.add(formDataService.generateTaskKey(action.getFormData().getId(), ReportType.CHECK_DEC));
                    lockDataService.interuptAllTasks(lockKeys, userInfo.getUser().getId());
                    formDataService.deleteReport(formData.getId(), formData.isManual(), userInfo.getUser().getId());
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("formDataId", action.getFormData().getId());
                    params.put("manual", action.getFormData().isManual());
                    params.put("uuid", action.getUuid());
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
                    LockData lockData = lockDataService.getLock(keyTask);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                    BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                    lockDataService.updateQueue(keyTask, lockData.getDateLock(), balancingVariant.getName());
                    logger.info(ReportType.CREATE_TASK, reportType.getDescription());
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
    public void undo(UploadDataRowsAction action, UploadFormDataResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
