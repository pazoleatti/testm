package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DepartmentChange;

import java.util.List;

/**
 * DAO для работы с изменениями подразделений
 */
public interface DepartmentChangeDao {
    List<DepartmentChange> getAllChanges();

    void clear();

    void addChange(DepartmentChange departmentChange);

    boolean checkDepartment(int depId, Integer depParentId);

}
