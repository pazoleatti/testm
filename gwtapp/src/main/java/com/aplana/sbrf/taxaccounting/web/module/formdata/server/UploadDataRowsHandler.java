package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

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
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
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

    public UploadDataRowsHandler() {
        super(UploadDataRowsAction.class);
    }

    @Override
    public DataRowResult execute(UploadDataRowsAction action, ExecutionContext context) throws ActionException {

		Logger logger = new Logger();
		FormData formData = action.getFormData();
		TAUserInfo userInfo = securityService.currentUserInfo();

		dataRowService.update(userInfo, formData.getId(), action.getModifiedRows());
		
        BlobData blobData = blobDataService.get(action.getUuid());
        //Парсит загруженный в фаловое хранилище xls-файл
        formDataService.importFormData(logger, userInfo,
                formData.getId(), blobData.getInputStream(), blobData.getName());


        DataRowResult result = new DataRowResult();
        result.setLogEntries(logger.getEntries());
        return result;
    }

    @Override
    public void undo(UploadDataRowsAction action, DataRowResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
