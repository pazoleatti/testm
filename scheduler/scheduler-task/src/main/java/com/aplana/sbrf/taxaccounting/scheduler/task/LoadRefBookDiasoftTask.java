package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Задача по загрузке ТФ справочников Diasoft
 *
 * @author auldanov
 */
//@Local(UserTaskLocal.class)
//@Remote(UserTaskRemote.class)
//@Stateless
//@Interceptors(SchedulerInterceptor.class)
public class LoadRefBookDiasoftTask extends AbstractUserTask {

    @Autowired
    LoadRefBookDataService loadRefBookDataService;

    @Autowired
    TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Override
    public void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userId,
                LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText());
        try {
            loadRefBookDataService.importRefBookDiasoft(userService.getSystemUserInfo(), new Logger(), lockId, false);
        } finally {
            lockDataService.unlock(key, userId);
        }
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
