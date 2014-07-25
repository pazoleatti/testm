package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LoadFormDataService;
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
 * Задача на загрузку ТФ налоговых форм
 *
 * @author auldanov
 */
@Local(UserTaskLocal.class)
@Remote(UserTaskRemote.class)
@Stateless
@Interceptors(SchedulerInterceptor.class)
public class LoadFormDataTask implements UserTask{

    @Autowired
    LoadFormDataService loadFormDataService;

    @Autowired
    TAUserService userService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    TaskUtils taskUtils;

    @Override
    public void execute(Map<String, TaskParam> params) throws TaskExecutionException {
        if (!params.containsKey("ТБ")){
            throw new TaskExecutionException("Не достасточно параметров для запуска задачи");
        }

        TaskParam param = params.get("TБ");
        Integer departmentId;
        try {
            departmentId = (Integer) param.getTypifiedValue();
        } catch (InvalidTaskParamException e) {
            e.printStackTrace();
            throw new TaskExecutionException("Не верный тип параметра задачи");
        }

        ArrayList<Integer> departmentsIds = new ArrayList<Integer>();
        departmentsIds.add(departmentId);
        loadFormDataService.importFormData(userService.getSystemUserInfo(), departmentsIds, new Logger());
    }

    @Override
    public String getTaskName() {
        return "Загрузка ТФ налоговых форм";
    }

    @Override
    public String getTaskClassName() {
        return LoadFormDataTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {
        // список параметров задачи
        List<FormElement> params = new ArrayList<FormElement>();
        params.add(taskUtils.getTBSelectBox(userInfo));

        return params;
    }
}
