package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.core.api.LockCoreService;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.TextBox;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.service.PropertyLoader;
import com.aplana.sbrf.taxaccounting.service.SchedulerInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Удаляет все блокировки, которые старше заданого времени
 *
 * @author auldanov
 */
@Local(UserTaskLocal.class)
@Remote(UserTaskRemote.class)
@Stateless
@Interceptors(SchedulerInterceptor.class)
public class RemoveUnnecessaryLocksTask implements UserTask {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String secCountParam = "Время жизни блокировки (секунд)";

    @Autowired
    LockCoreService lockCoreService;

    @Override
    public void execute(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
        if (!params.containsKey(secCountParam)){
            throw new TaskExecutionException("Ошибка получения аргументов задачи");
        } else {
            try {
                Integer sec = (Integer) params.get(secCountParam).getTypifiedValue();
                lockCoreService.unlockIfOlderThan(sec);
            } catch (InvalidTaskParamException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getTaskName() {
        return "Удаление истекших блокировок" + PropertyLoader.getVersion();
    }

    @Override
    public String getTaskClassName() {
        return RemoveUnnecessaryLocksTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {
        List<FormElement> params = new ArrayList<FormElement>();
        FormElement time = new TextBox();
        time.setRequired(true);
        time.setType(TaskParamType.INT);
        time.setName(secCountParam);
        params.add(time);

        return params;
    }
}
