package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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
        System.out.println("UploadDataRowsHandler started");
        //Пытаемся установить блокировку на операцию импорта в текущую нф
        LockData lockData = lockDataService.lock(LockData.LockObjects.FORM_DATA_IMPORT.name() + "_" + action.getFormData().getId() + "_" + action.getFormData().isManual(),
                userInfo.getUser().getId(), LockData.STANDARD_LIFE_TIME * 4);
        System.out.println("lockData: " +lockData);
        if (lockData == null) {
            Logger logger = new Logger();
            FormData formData = action.getFormData();

            dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());

            BlobData blobData = blobDataService.get(action.getUuid());
            logger.info("Загрузка данных из файла: \"" + blobData.getName() + "\"");
            //Парсит загруженный в фаловое хранилище xls-файл
            formDataService.importFormData(logger, userInfo,
                    formData.getId(), formData.isManual(), blobData.getInputStream(), blobData.getName());


            DataRowResult result = new DataRowResult();
            result.setUuid(logEntryService.save(logger.getEntries()));
            return result;
        } else {
            throw new ActionException("Операция импорта данных в текущую налоговую форму уже выполняется другим пользователем!");
        }
    }

    @Override
    public void undo(UploadDataRowsAction action, DataRowResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
