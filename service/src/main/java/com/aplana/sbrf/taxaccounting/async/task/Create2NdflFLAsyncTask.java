package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.Create2NdflFLParams;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Создание формы 2-НДФЛ (ФЛ)
 */
@Component("Create2NdflFLAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Create2NdflFLAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private RefBookPersonDao refBookPersonDao;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_2NDFL_FL;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) {
        Create2NdflFLParams params = (Create2NdflFLParams) taskData.getParams().get("params");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        declarationDataService.create2NdflFL(params, userInfo, logger);
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        String taskName = generateTaskName(taskData);
        return String.format("Выполнена операция \"%s\"", taskName);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        String taskName = generateTaskName(taskData);
        if (unexpected) {
            Throwable exceptionThrown = (Throwable) taskData.getParams().get("exceptionThrown");
            if (exceptionThrown != null && exceptionThrown.getMessage() != null) {
                return String.format("Не выполнена операция \"%s\". Причина: %s", taskName, exceptionThrown.getMessage());
            }
        }
        return String.format("Не выполнена операция \"%s\"", taskName);
    }

    private String generateTaskName(AsyncTaskData taskData) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        Create2NdflFLParams params = (Create2NdflFLParams) taskData.getParams().get("params");

        ReportPeriod reportPeriod = periodService.fetchReportPeriod(params.getReportPeriodId());
        RegistryPerson person = refBookPersonDao.fetchPersonVersion(params.getPersonId());
        int activeDeclarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(params.getDeclarationTypeId(), reportPeriod.getId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(activeDeclarationTemplateId);

        return String.format("Создание отчетных форм: \"%s\", для ФЛ: %s, Период: \"%s, %s\"",
                declarationTemplate.getName(),
                person.getFullName(),
                reportPeriod.getTaxPeriod().getYear(),
                reportPeriod.getName()
        );
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }
}
