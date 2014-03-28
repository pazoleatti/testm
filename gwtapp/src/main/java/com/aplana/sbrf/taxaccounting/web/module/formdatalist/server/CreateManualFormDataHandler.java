package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateManualFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateManualFormDataHandler extends AbstractActionHandler<CreateManualFormData, CreateManualFormDataResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FormDataService formDataService;

    public CreateManualFormDataHandler() {
        super(CreateManualFormData.class);
    }

    @Override
    public CreateManualFormDataResult execute(CreateManualFormData action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        CreateManualFormDataResult result = new CreateManualFormDataResult();
        formDataService.createManualFormData(new Logger(), userInfo, action.getFormDataId());
        return result;
    }

    @Override
    public void undo(CreateManualFormData action, CreateManualFormDataResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
