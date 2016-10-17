package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.EmailService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessResult;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.DepartmentWS;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws.DepartmentWS_Manager;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CheckAccessHandler extends AbstractActionHandler<CheckAccessAction, CheckAccessResult> {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    LogEntryService logEntryService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuditService auditService;

    @Autowired
    private DepartmentWS_Manager departmentWS_manager;

    public CheckAccessHandler() {
        super(CheckAccessAction.class);
    }

    @Override
    public CheckAccessResult execute(CheckAccessAction action, ExecutionContext context) throws ActionException {
        CheckAccessResult result = new CheckAccessResult();
        Logger logger = new Logger();
        String uuid = null;
        ConfigurationParamModel model = action.getModel();
        ConfigurationParamGroup group = action.getGroup();

        if (group.equals(ConfigurationParamGroup.COMMON) || group.equals(ConfigurationParamGroup.FORM)) {
            checkWebServiceParams(model, logger);
            configurationService.checkReadWriteAccess(securityService.currentUserInfo(), model, logger);
        } else if (group.equals(ConfigurationParamGroup.EMAIL)) {
            boolean success = emailService.testAuth(logger);
            if (!success) {
                auditService.add(FormDataEvent.SEND_EMAIL, securityService.currentUserInfo(), 0, null, null, null, null,
                        logger.getEntries().get(0).getMessage(), uuid);
            }
        } else if (group.equals(ConfigurationParamGroup.ASYNC)) {
            for (Map<String,String> param : action.getAsyncParams()) {
                String type = param.get(ConfigurationParamModel.ASYNC_TYPE);
                int limit = 0;
                int shortLimit = 0;
                String sLimit = param.get(ConfigurationParamModel.ASYNC_LIMIT);
                String sShortLimit = param.get(ConfigurationParamModel.ASYNC_SHORT_LIMIT);
                boolean error = false;

                if (sLimit != null && !sLimit.isEmpty()) {
                    try {
                        limit = Integer.valueOf(sLimit);
                        if (limit <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        logger.error(String.format("%s: Значение параметра \"Ограничение на выполнение задания\" (\"%s\") должно быть числовым (больше нуля)!", type, sLimit));
                        error = true;
                    }
                }

                if (sShortLimit != null && !sShortLimit.isEmpty()) {
                    try {
                        shortLimit = Integer.valueOf(sShortLimit);
                        if (shortLimit <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        logger.error(String.format("%s: Значение параметра \"Ограничение на выполнение задания\" (\"%s\") должно быть числовым (больше нуля)!", type, sShortLimit));
                        error = true;
                    }
                }
                if (error) {
                    continue;
                }

                if (sLimit != null && !sLimit.isEmpty() && +
                        shortLimit >= limit) {
                    logger.error(String.format("%s: Значение параметра \"Ограничение на выполнение задания\" (\"%s\") должно быть больше значения параметра \"Ограничение на выполнение задания в очереди быстрых заданий\" (\"%s\")!",
                            type, sLimit, sShortLimit));
                }
            }
            if (logger.getEntries().isEmpty()) {
                logger.info("Проверка завершена, ошибок не обнаружено");
                result.setHasError(false);
            } else {
                if (logger.containsLevel(LogLevel.ERROR)) {
                    result.setHasError(true);
                }
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    private void checkWebServiceParams(ConfigurationParamModel model, Logger logger) {
        List<String> valuesList = model.get(ConfigurationParam.WSDL_ADDRESS_DEPARTMENT_WS_SUNR, 0);
        if (valuesList != null) {
            for (String value : valuesList) {
                if (!departmentWS_manager.checkServiceAvailable(value)) {
                    logger.error("Сервис " + DepartmentWS.class.getName() + " не доступен по адресу " + value);
                }
            }
        }
        valuesList = model.get(ConfigurationParam.TIMEOUT_SUNR, 0);
        if (valuesList != null) {
            for (String value : valuesList) {
                try{
                    int timeout = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    logger.error("Параметр \"" + ConfigurationParam.TIMEOUT_SUNR.getCaption() + "\" должен содержать целочисленное значение");
                }
            }
        }
    }

    @Override
    public void undo(CheckAccessAction action, CheckAccessResult result, ExecutionContext context) throws ActionException {
    }
}
