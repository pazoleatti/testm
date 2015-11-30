package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeleteJrxmlAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeleteJrxmlResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class DeleteJrxmlHandler extends AbstractActionHandler<DeleteJrxmlAction, DeleteJrxmlResult> {

    @Autowired
    DeclarationDataService declarationDataService;
    @Autowired
    LockDataService lockDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    DeclarationTemplateService declarationTemplateService;

    public DeleteJrxmlHandler() {
        super(DeleteJrxmlAction.class);
    }

    @Override
    public DeleteJrxmlResult execute(DeleteJrxmlAction action, ExecutionContext context) throws ActionException {
        //Система удаляет все найденные pdf и xlsx отчеты.
        declarationDataService.cleanBlobs(
                declarationTemplateService.getDataIdsThatUseJrxml(action.getDtId(), securityService.currentUserInfo()),
                Arrays.asList(ReportType.EXCEL_DEC, ReportType.PDF_DEC, ReportType.JASPER_DEC));
        for (Long id : declarationTemplateService.getLockDataIdsThatUseJrxml(action.getDtId())){
            String keyPDF = declarationDataService.generateAsyncTaskKey(id, ReportType.PDF_DEC);
            String keyEXCEL = declarationDataService.generateAsyncTaskKey(id, ReportType.EXCEL_DEC);
            LockData pdfLock = lockDataService.getLock(keyPDF);
            LockData excelLock = lockDataService.getLock(keyEXCEL);
            lockDataService.interruptTask(pdfLock, securityService.currentUserInfo().getUser().getId(), false, "Выполнена замена jrxml файла макета декларации");
            lockDataService.interruptTask(excelLock, securityService.currentUserInfo().getUser().getId(), false, "Выполнена замена jrxml файла макета декларации");
        }
        declarationTemplateService.deleteJrxml(action.getDtId());
        return new DeleteJrxmlResult();
    }

    @Override
    public void undo(DeleteJrxmlAction action, DeleteJrxmlResult result, ExecutionContext context) throws ActionException {

    }
}
