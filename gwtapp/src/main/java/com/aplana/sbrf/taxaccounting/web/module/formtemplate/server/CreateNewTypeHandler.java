package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.CreateNewTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.CreateNewTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * User: avanteev
 * Сервис для обработки создания нового макета
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class CreateNewTypeHandler extends AbstractActionHandler<CreateNewTypeAction, CreateNewTypeResult> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private SecurityService securityService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    FormTemplateDao formTemplateDao;

    public CreateNewTypeHandler() {
        super(CreateNewTypeAction.class);
    }

    @Override
    public CreateNewTypeResult execute(CreateNewTypeAction action, ExecutionContext executionContext) throws ActionException {
        Logger logger = new Logger();
        FormTemplate formTemplate = action.getForm();
        CreateNewTypeResult result = new CreateNewTypeResult();
        makeDates(action);
        int formTemplateId = mainOperatingService.createNewType(formTemplate, action.getVersionEndDate(), logger, securityService.currentUserInfo());
        result.setFormTemplateId(formTemplateId);
        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        result.setFormType(formTemplate.getType());

        return result;
    }

    @Override
    public void undo(CreateNewTypeAction createNewTypeAction, CreateNewTypeResult createNewTypeResult, ExecutionContext executionContext) throws ActionException {

    }

    private void makeDates(CreateNewTypeAction action){
        Calendar calendar = Calendar.getInstance();
        FormTemplate formTemplate = action.getForm();
        calendar.setTime(formTemplate.getVersion());
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        formTemplate.setVersion(calendar.getTime());
        if (action.getVersionEndDate() != null){
            calendar.clear();
            calendar.setTime(action.getVersionEndDate());
            calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
            action.setVersionEndDate(calendar.getTime());
        }
    }
}
