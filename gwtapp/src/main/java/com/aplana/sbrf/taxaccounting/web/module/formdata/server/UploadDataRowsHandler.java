package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadDataRowsAction;
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
 * User: avanteev
 * Хэнлер для обработки загруженного в файловой хранилище файла.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UploadDataRowsHandler extends
        AbstractActionHandler<UploadDataRowsAction, UploadFormDataResult> {

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


    public UploadDataRowsHandler() {
        super(UploadDataRowsAction.class);
    }

    @Override
    public UploadFormDataResult execute(final UploadDataRowsAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.IMPORT_FD;
        final UploadFormDataResult result = new UploadFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        FormData formData = action.getFormData();
        LockData lockDataEdit = formDataService.getObjectLock(action.getFormData().getId(), userInfo);
        if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
            String keyTask = formDataService.generateTaskKey(action.getFormData().getId(), reportType);
            Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
            if (lockType != null && !lockType.getFirst().equals(ReportType.EDIT_FD) && !lockType.getFirst().equals(reportType)) {
                // ошибка
                formDataService.locked(action.getFormData().getId(), reportType, lockType, logger);
            }
            Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, formDataService.getTaskName(reportType, action.getFormData().getId(), userInfo), userInfo, action.isForce(), logger);
            if (restartStatus != null && restartStatus.getFirst()) {
                result.setLock(true);
                result.setRestartMsg(restartStatus.getSecond());
            } else if (restartStatus != null && !restartStatus.getFirst()) {
                result.setLock(false);
            } else {
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
                result.setSave(false);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("formDataId", action.getFormData().getId());
                params.put("manual", action.getFormData().isManual());
                params.put("uuid", action.getUuid());
                asyncTaskManagerService.createTask(keyTask, reportType, params, action.isCancelTask(), PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                    @Override
                    public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                        return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                formDataService.getFormDataFullName(action.getFormData().getId(), null, reportType),
                                LockData.State.IN_QUEUE.getText(),
                                lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA));
                    }

                    @Override
                    public void executePostCheck() {
                        result.setLockTask(true);
                    }

                    @Override
                    public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                        return formDataService.checkExistTask(action.getFormData().getId(), false, reportType, logger, userInfo);
                    }

                    @Override
                    public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                        formDataService.interruptTask(action.getFormData().getId(), false, userInfo.getUser().getId(), reportType);
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
    public void undo(UploadDataRowsAction action, UploadFormDataResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
