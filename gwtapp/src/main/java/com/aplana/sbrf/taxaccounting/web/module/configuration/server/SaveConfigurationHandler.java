package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.SaveConfigurationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaveConfigurationHandler extends
		AbstractActionHandler<SaveConfigurationAction, SaveConfigurationResult> {

    private final static String UNIQUE_DEPARTMENT_ERROR = "Параметры для ТБ «%s» уже заданы!";

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    LogEntryService logEntryService;

	public SaveConfigurationHandler() {
		super(SaveConfigurationAction.class);
	}

    @Override
    public SaveConfigurationResult execute(SaveConfigurationAction action,
                                           ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        if (!action.getDublicateDepartmentIdSet().isEmpty()) {
            for (int departmentId : action.getDublicateDepartmentIdSet()) {
                Department department = departmentService.getDepartment(departmentId);
                logger.error(UNIQUE_DEPARTMENT_ERROR, department.getName());
            }
        } else {
            configurationService.saveAllConfig(securityService.currentUserInfo(), action.getModel(), logger);
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Ошибки при сохранении конфигурационных параметров.",
                    logEntryService.save(logger.getEntries()));
        }
        return new SaveConfigurationResult();
    }

    @Override
    public void undo(SaveConfigurationAction arg0,
			SaveConfigurationResult arg1, ExecutionContext arg2)
			throws ActionException {
		// Ничего не делаем
	}
}
