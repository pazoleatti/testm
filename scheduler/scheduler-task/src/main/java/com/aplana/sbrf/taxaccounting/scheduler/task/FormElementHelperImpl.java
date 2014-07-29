package com.aplana.sbrf.taxaccounting.scheduler.task;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBox;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.SelectBoxItem;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SchedulerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный клас для задач
 */
@Service
@Interceptors(SchedulerInterceptor.class)
public class FormElementHelperImpl implements FormElementHelper {

    @Autowired
    DepartmentService departmentService;

    @Override
    public SelectBox getTBSelectBox(TAUserInfo userInfo, String name){
        // элементы выпадающего списка
        List<SelectBoxItem> selectBoxItems = new ArrayList<SelectBoxItem>();
        List<Department> departments = departmentService.getTBDepartments(userInfo.getUser());
        for (Department department : departments) {
            selectBoxItems.add(new SelectBoxItem(department.getName(), department.getId()));
        }

        // элемент ТБ
        SelectBox selectBox = new SelectBox();
        selectBox.setValues(selectBoxItems);
        selectBox.setRequired(true);
        selectBox.setName(name);
        selectBox.setType(TaskParamType.INT);

        return selectBox;
    }
}
