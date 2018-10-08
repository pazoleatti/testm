package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO подразделения с дочерними элементами
 */
public class RefBookDepartmentDTO {
    // само подразделение
    private RefBookDepartment department;

    public RefBookDepartmentDTO(RefBookDepartment department) {
        this.department = department;
    }

    public String getName() {
        return department.getName();
    }

    public List<RefBookDepartmentDTO> getChildren() {
        List<RefBookDepartmentDTO> children = new ArrayList<>();
        if (department.getChildren() != null) {
            for (RefBookDepartment child : department.getChildren()) {
                children.add(new RefBookDepartmentDTO(child));
            }
        }
        return children;
    }
}
