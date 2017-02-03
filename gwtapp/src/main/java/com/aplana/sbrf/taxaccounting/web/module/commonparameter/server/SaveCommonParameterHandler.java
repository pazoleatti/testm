package com.aplana.sbrf.taxaccounting.web.module.commonparameter.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.SaveCommonParameterAction;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared.SaveCommonParameterResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaveCommonParameterHandler extends AbstractActionHandler<SaveCommonParameterAction, SaveCommonParameterResult> {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LogEntryService logEntryService;

    public SaveCommonParameterHandler() {
        super(SaveCommonParameterAction.class);
    }

    @Override
    public SaveCommonParameterResult execute(SaveCommonParameterAction action, ExecutionContext context) throws ActionException {
        SaveCommonParameterResult result = new SaveCommonParameterResult();
        Logger logger = new Logger();

        result.setErrors(configurationService.checkCommonConfigurationParams(action.getConfigurationParamMap(), logger));

        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        } else {
            configurationService.saveCommonConfigurationParams(action.getConfigurationParamMap());
        }

        return result;
    }

    @Override
    public void undo(SaveCommonParameterAction arg0, SaveCommonParameterResult arg1, ExecutionContext arg2) throws ActionException {
        // Ничего не делаем
    }
}
