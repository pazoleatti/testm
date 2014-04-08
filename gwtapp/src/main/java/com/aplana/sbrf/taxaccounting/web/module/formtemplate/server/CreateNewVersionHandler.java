package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.CreateNewVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.CreateNewVersionResult;
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
 * Сервис для обработки создания новой версии макета
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class CreateNewVersionHandler extends AbstractActionHandler<CreateNewVersionAction, CreateNewVersionResult> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private SecurityService securityService;
    @Autowired
    LogEntryService logEntryService;


    public CreateNewVersionHandler() {
        super(CreateNewVersionAction.class);
    }

    @Override
    public CreateNewVersionResult execute(CreateNewVersionAction action, ExecutionContext executionContext) throws ActionException {
        CreateNewVersionResult result = new CreateNewVersionResult();
        Logger logger = new Logger();
        makeDates(action);
        formTemplateService.validateFormTemplate(action.getForm(), logger);
        if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceLoggerException("Ошибки при валидации.", logEntryService.save(logger.getEntries()));
        }
        int formTemplateId = mainOperatingService.createNewTemplateVersion(action.getForm(), action.getVersionEndDate(), logger, securityService.currentUserInfo().getUser());
        result.setFormTemplateId(formTemplateId);
        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CreateNewVersionAction createNewVersionAction, CreateNewVersionResult createNewVersionResult, ExecutionContext executionContext) throws ActionException {

    }

    private void makeDates(CreateNewVersionAction action){
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
