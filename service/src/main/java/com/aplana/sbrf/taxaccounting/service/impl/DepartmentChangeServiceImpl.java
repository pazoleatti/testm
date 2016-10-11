package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentChangeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentChange;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.service.DepartmentChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lhaziev
 */
@Service
@Transactional
public class DepartmentChangeServiceImpl implements DepartmentChangeService {

    @Autowired
    private DepartmentChangeDao departmentChangeDao;

    @Override
    public List<DepartmentChange> getAllChanges() {
        return departmentChangeDao.getAllChanges();
    }

    @Override
    public void clean() {
        departmentChangeDao.clean();
    }

    @Override
    public void clean(int day) {
        departmentChangeDao.clean(day);
    }

    @Override
    public void addChange(DepartmentChange departmentChange) {
        departmentChangeDao.addChange(departmentChange);
    }

    @Override
    public String generateTaskKey(ReportType reportType) {
        return LockData.LockObjects.DEPARTMENT_HISTORY.name() + "_" + reportType.getName();
    }

}
