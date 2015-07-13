package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.EditPrintFDAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.EditPrintFDResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Обновление имени подразделения в печатных формах
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class EditPrintFDHandler extends AbstractActionHandler<EditPrintFDAction, EditPrintFDResult> {

    @Autowired
    FormDataService formDataService;
    @Autowired
    SecurityService securityService;


    public EditPrintFDHandler() {
        super(EditPrintFDAction.class);
    }

    @Override
    public EditPrintFDResult execute(EditPrintFDAction action, ExecutionContext executionContext) throws ActionException {
        if (!action.isChangeType()){
            //Обновляем имена подразделений в печатных формах
            formDataService.
                    updateFDDepartmentNames(action.getDepId(), action.getDepName(), action.getVersionFrom(), action.getVersionTo(), securityService.currentUserInfo());
        }else {
            //Обновляем имена ТБ в печатных формах
            formDataService.
                    updateFDTBNames(action.getDepId(), action.getDepName(), action.getVersionFrom(), action.getVersionTo(), action.isChangeType(), securityService.currentUserInfo());
        }
        return new EditPrintFDResult();
    }

    @Override
    public void undo(EditPrintFDAction formDataPrintAction, EditPrintFDResult checkCorrectessFDResult, ExecutionContext executionContext) throws ActionException {
        //Nothing
    }
}
