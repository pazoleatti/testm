package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.LockData;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class SaveConfigurationHandler extends
        AbstractActionHandler<SaveConfigurationAction, SaveConfigurationResult> {

    private final static String UNIQUE_DEPARTMENT_ERROR = "Параметры для ТБ «%s» уже заданы!";
    private final static String NOT_SET_DEPARTMENT_ERROR = "Не задано значение поля «%s»!";
    private final static String NOT_SET_ERROR = "Не задано значение поля «%s» для «%s»!";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    public SaveConfigurationHandler() {
        super(SaveConfigurationAction.class);
    }

    @Override
    public SaveConfigurationResult execute(SaveConfigurationAction action,
                                           ExecutionContext context) throws ActionException {



        String key = "testAsync_" + new Date().getTime();
        lockDataService.lock(key, 1, "testAsync");
        LockData lockData = lockDataService.getLock(key);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AsyncTask.RequiredParams.USER_ID.name(), lockData.getUserId());
        params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), lockData.getKey());
        params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
        try {
            asyncManager.executeAsync(300L, params, BalancingVariants.SHORT);
        } catch (AsyncTaskException e) {
            e.printStackTrace();
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
