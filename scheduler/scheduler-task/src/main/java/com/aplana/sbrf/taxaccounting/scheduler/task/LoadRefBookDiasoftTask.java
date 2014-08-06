package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.PropertyLoader;
import com.aplana.sbrf.taxaccounting.service.SchedulerInterceptor;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Задача по загрузке ТФ справочников Diasoft
 *
 * @author auldanov
 */
@Local(UserTaskLocal.class)
@Remote(UserTaskRemote.class)
@Stateless
@Interceptors(SchedulerInterceptor.class)
public class LoadRefBookDiasoftTask implements UserTask {

    @Autowired
    LoadRefBookDataService loadRefBookDataService;

    @Autowired
    TAUserService userService;

    @Override
    public void execute(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
         loadRefBookDataService.importRefBookNsi(userService.getSystemUserInfo(), new Logger());
    }

    @Override
    public String getTaskName() {
        return "Загрузка ТФ справочников Diasoft" + PropertyLoader.getVersion();
    }

    @Override
    public String getTaskClassName() {
        return LoadRefBookDiasoftTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {
        return null;
    }
}
