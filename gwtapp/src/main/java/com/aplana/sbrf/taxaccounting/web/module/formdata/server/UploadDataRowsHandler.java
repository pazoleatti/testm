package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UploadDataRowsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * User: avanteev
 * Хэнлер для обработки загруженного в файловой хранилище файла.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UploadDataRowsHandler extends
        AbstractActionHandler<UploadDataRowsAction, DataRowResult> {

    @Autowired
    BlobDataService blobDataService;

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

    public UploadDataRowsHandler() {
        super(UploadDataRowsAction.class);
    }

    @Override
    public DataRowResult execute(UploadDataRowsAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        DataRowResult result = new DataRowResult();
        Logger logger = new Logger();

        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormData().getId());
        if (lockType != null && ReportType.EDIT_FD.equals(lockType.getFirst())) {
            //Пытаемся установить блокировку на операцию импорта в текущую нф
            String key = formDataService.generateTaskKey(action.getFormData().getId(), ReportType.IMPORT_FD);
            BlobData blobData = blobDataService.get(action.getUuid());
            LockData lockData = lockDataService.lock(key, userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormData().getId(), blobData.getName(), ReportType.IMPORT_FD),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA_IMPORT));
            if (lockData == null) {
                try {
                    FormData formData = action.getFormData();

                    dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());

                    logger.info("Загрузка данных из файла: \"" + blobData.getName() + "\"");
                    //Парсит загруженный в фаловое хранилище xls-файл
                    formDataService.importFormData(logger, userInfo,
                            formData.getId(), formData.isManual(), blobData.getInputStream(), blobData.getName());

                } catch (Exception e) {
                    throw new ServiceLoggerException("Не удалось выполнить операцию импорта данных в налоговую форму", logEntryService.save(logger.getEntries()));
                } finally {
                    try {
                        if (lockDataService.isLockExists(key, false)) {
                            lockDataService.unlock(key, userInfo.getUser().getId());
                        }
                    } catch (Exception e2) {}
                }
            } else {
                throw new ActionException("Операция импорта данных в текущую налоговую форму уже выполняется другим пользователем!");
            }
        } else {
            formDataService.locked(lockType.getSecond(), logger);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(UploadDataRowsAction action, DataRowResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
