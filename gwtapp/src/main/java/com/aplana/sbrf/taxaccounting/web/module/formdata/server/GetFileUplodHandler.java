package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFileUpload;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFileUploadResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 * Хэнлер для обработки загруженного в файловой хранилище файла.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFileUplodHandler extends
        AbstractActionHandler<GetFileUpload, GetFileUploadResult> {

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private SecurityService securityService;

    public GetFileUplodHandler() {
        super(GetFileUpload.class);
    }

    @Override
    public GetFileUploadResult execute(GetFileUpload action, ExecutionContext context) throws ActionException {

        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        BlobData blobData = blobDataService.get(action.getUuid());
        FormData formData = formDataService.getFormData(userInfo, action.getFormDataId(), logger);
        //Парсит загруженный в фаловое хранилище xls-файл
        formDataService.importFormData(logger, userInfo,
                formData.getId(),formData.getFormTemplateId(), formData.getDepartmentId(), formData.getKind(), formData.getReportPeriodId(),
                blobData.getInputStream(), blobData.getName());


        GetFileUploadResult result = new GetFileUploadResult();
        result.setLogEntries(logger.getEntries());
        return result;
    }

    @Override
    public void undo(GetFileUpload action, GetFileUploadResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
