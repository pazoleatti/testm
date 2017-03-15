package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.*;
import com.aplana.sbrf.taxaccounting.service.PropertyLoader;
import com.aplana.sbrf.taxaccounting.service.SchedulerInterceptor;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@Local(UserTaskLocal.class)
//@Remote(UserTaskRemote.class)
//@Stateless
//@Interceptors(SchedulerInterceptor.class)
public class SimpleUserTask extends AbstractUserTask {

	private static final Log LOG = LogFactory.getLog(SimpleUserTask.class);

    @Override
    public void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
        for (int i = 0; i<10; i++) {
			LOG.info("SimpleUserTask started: " + i);
            try {
                Thread.sleep(60*1000); // 1 минута
            } catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getTaskName() {
        return "Тестовая задача" + PropertyLoader.getVersion();
    }

    @Override
    public String getTaskClassName() {
        return SimpleUserTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {
        List<FormElement> params = new ArrayList<FormElement>();
        FormElement element1 = new TextBox();
        element1.setName("Имя");
        element1.setType(TaskParamType.STRING);
        params.add(element1);

        // значение дата
        FormElement element2 = new DateBox();
        element2.setName("Дата");
        element2.setType(TaskParamType.DATE);
        params.add(element2);

        // значение класс
        SelectBox element3 = new SelectBox();
        element3.setName("Какое то число");
        element3.setType(TaskParamType.INT);
        List<SelectBoxItem> selectBoxValues = new ArrayList<SelectBoxItem>();
        selectBoxValues.add(new SelectBoxItem("ОДин", 1));
        selectBoxValues.add(new SelectBoxItem("Два", 2));
        element3.setValues(selectBoxValues);

        return params;
    }
}
