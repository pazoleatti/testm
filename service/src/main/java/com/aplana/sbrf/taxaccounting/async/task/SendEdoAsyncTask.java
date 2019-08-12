package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.impl.transport.edo.SendToEdoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Отправка ЭД в ЭДО
 */
@Component("SendEdoAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SendEdoAsyncTask extends AbstractAsyncTask {

    @Autowired
    private DeclarationDataService declarationDataService;

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.SEND_EDO;
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        List<Long> declarationDataIds = (List<Long>) params.get("noLockDeclarationDataIds");
        SendToEdoResult result = declarationDataService.sendToEdo(declarationDataIds, userInfo, logger);
        params.put("result", result);
        return new BusinessLogicResult(defineLogLevelByResult(result), true);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        SendToEdoResult result = (SendToEdoResult) taskData.getParams().get("result");
        if (result != null) {
            List succeedDeclarations = result.getDeclarationsByLogLevel(LogLevel.INFO);
            List warnDeclarations = result.getDeclarationsByLogLevel(LogLevel.WARNING);
            List errorDeclarations = result.getDeclarationsByLogLevel(LogLevel.ERROR);
            if (isEmpty(errorDeclarations)) {
                if (isEmpty(warnDeclarations)) {
                    return "Выполнена операция \"Отправка xml-файлов в ЭДО\".";
                } else {
                    return "Выполнена операция \"Отправка xml-файлов в ЭДО\" (присутствуют нефатальные ошибки).";
                }
            } else {
                if (!isEmpty(succeedDeclarations) || !isEmpty(warnDeclarations)) {
                    return "Частично выполнена операция \"Отправка xml-файлов в ЭДО\" " +
                            "(для некоторых форм имеются фатальные ошибки).";
                } else {
                    return "Не выполнена операция \"Отправка xml-файлов в ЭДО\" " +
                            "(для всех форм имеются фатальные ошибки).";
                }
            }
        } else {
            return "Выполнение операции \"" + getAsyncTaskType().getDescription() + "\" завершено.";
        }
    }

    private LogLevel defineLogLevelByResult(SendToEdoResult result) {
        List succeedDeclarations = result.getDeclarationsByLogLevel(LogLevel.INFO);
        List warnDeclarations = result.getDeclarationsByLogLevel(LogLevel.WARNING);
        List errorDeclarations = result.getDeclarationsByLogLevel(LogLevel.ERROR);
        if (isEmpty(errorDeclarations)) {
            return LogLevel.INFO;
        } else {
            if (!isEmpty(succeedDeclarations) || !isEmpty(warnDeclarations)) {
                return LogLevel.WARNING;
            } else {
                return LogLevel.ERROR;
            }
        }
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return "Не выполнена операция \"" + getAsyncTaskType().getDescription() + "\"." + (throwable != null ? " Причина: " + throwable.getMessage() : "");
    }
}