package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DepartmentChange;

import java.util.List;

/**
 * DAO для работы с изменениями подразделений
 */
public interface DepartmentChangeDao {
    List<DepartmentChange> getAllChanges();

    void clean();

    void clean(int day);

    void addChange(DepartmentChange departmentChange);
}
