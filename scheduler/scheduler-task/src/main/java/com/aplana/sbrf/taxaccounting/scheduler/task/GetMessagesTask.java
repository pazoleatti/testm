package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.service.MessageServiceLocal;
import com.aplana.sbrf.taxaccounting.service.PropertyLoader;
import com.aplana.sbrf.taxaccounting.service.SchedulerInterceptor;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.List;
import java.util.Map;

/**
 * Задача для получения данных из КСШ по расписанию
 * @author dloshkarev
 */
@Local(UserTaskLocal.class)
@Remote(UserTaskRemote.class)
@Stateless
@Interceptors(SchedulerInterceptor.class)
public class GetMessagesTask extends AbstractUserTask {

    @EJB
    private MessageServiceLocal messageService;

    @Override
    protected void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
        messageService.getRateMessages();
    }

    @Override
    public String getTaskName() {
        return "Получение данных из КСШ "+ PropertyLoader.getVersion();
    }

    @Override
    public String getTaskClassName() {
        return GetMessagesTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {
        return null;
    }
}