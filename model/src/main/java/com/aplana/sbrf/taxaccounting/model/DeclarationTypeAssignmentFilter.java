package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Модель фильтра в разделе "Назначение налоговых форм"
 */
public class DeclarationTypeAssignmentFilter implements Serializable {

	private List<Long> departmentIds;

    public List<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }
}
