package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.FormElement;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBox;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBoxItem;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 * Задача на загрузку ТФ налоговых форм
 *
 * @author auldanov
 */
//@Local(UserTaskLocal.class)
//@Remote(UserTaskRemote.class)
//@Stateless
//@Interceptors(SchedulerInterceptor.class)
public class LoadFormDataTask extends AbstractUserTask {

	private static final Log LOG = LogFactory.getLog(LoadFormDataTask.class);
	private static final String TB_NAME = "ТБ";
	private static final int ALL_DEPARTMENTS_ID = -1;
	private static final String ALL_DEPARTMENTS_LABEL = "Все подразделения";

    @Autowired
    LoadFormDataService loadFormDataService;
    @Autowired
    TAUserService userService;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    private LockDataService lockDataService;

    @Override
    public void executeBusinessLogic(Map<String, TaskParam> params, int userId) throws TaskExecutionException {
        if (!params.containsKey(TB_NAME)){
            throw new TaskExecutionException("Не достасточно параметров для запуска задачи");
        }

        TaskParam param = params.get(TB_NAME);
        Integer departmentId;
        try {
            departmentId = (Integer) param.getTypifiedValue();
        } catch (InvalidTaskParamException e) {
            LOG.error(e.getMessage(), e);
            throw new TaskExecutionException("Не верный тип параметра задачи");
        }

        // список выбранных подразделений
        ArrayList<Integer> departmentsIds = new ArrayList<Integer>();

        /**
         * Проверим не выбран ли пункт все подразделения
         */
        if (departmentId.equals(ALL_DEPARTMENTS_ID)){
            TAUser user = userService.getUser(userId);
            List<Department> departments = departmentService.getTBDepartments(user, TaxType.NDFL);
            for (Department department : departments) {
                departmentsIds.add(department.getId());
            }
        } else {
            departmentsIds.add(departmentId);
        }
        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userId,
                LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText());
        try {
            Map<Integer, List<TaxType>> departmentTaxMap = new HashMap<Integer, List<TaxType>>();
            for (Integer depId : departmentsIds) {
                departmentTaxMap.put(depId, Arrays.asList(TaxType.values()));
            }
            loadFormDataService.importFormData(userService.getSystemUserInfo(), departmentTaxMap, new Logger(), lockId, false);
        } finally {
            lockDataService.unlock(key, userId);
        }
    }

    @Override
    public String getTaskName() {
        return "Загрузка ТФ налоговых форм" + PropertyLoader.getVersion();
    }

    @Override
    public String getTaskClassName() {
        return LoadFormDataTask.class.getSimpleName();
    }

    @Override
    public List<FormElement> getParams(TAUserInfo userInfo) {

        // элементы выпадающего списка
        List<SelectBoxItem> selectBoxItems = new ArrayList<SelectBoxItem>();
        List<Department> departments = departmentService.getTBDepartments(userInfo.getUser(), TaxType.NDFL);
        // отсортировать по алфавиту
        Collections.sort(departments, new Comparator<Department>() {
            @Override
            public int compare(Department o1, Department o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Department department : departments) {
            if (department.getType() != DepartmentType.ROOT_BANK) {
                selectBoxItems.add(new SelectBoxItem(department.getName(), department.getId()));
            }
        }
        // добавление элемента "все подразделения"
        selectBoxItems.add(new SelectBoxItem(ALL_DEPARTMENTS_LABEL, ALL_DEPARTMENTS_ID));

        // элемент ТБ
        SelectBox selectBox = new SelectBox();
        selectBox.setValues(selectBoxItems);
        selectBox.setRequired(true);
        selectBox.setName(TB_NAME);
        selectBox.setType(TaskParamType.INT);
        selectBox.setRequired(true);

        // список параметров задачи
        List<FormElement> params = new ArrayList<FormElement>();
        params.add(selectBox);

        return params;
    }
}
