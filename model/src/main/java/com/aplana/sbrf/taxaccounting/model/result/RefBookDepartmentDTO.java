package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, RefBookValue> getMapValues() {
        Map<String, RefBookValue> record = new HashMap<>();
        record.put("CODE", new RefBookValue(RefBookAttributeType.NUMBER, department.getCode()));
        record.put("NAME", new RefBookValue(RefBookAttributeType.STRING, department.getName()));
        record.put("SHORTNAME", new RefBookValue(RefBookAttributeType.STRING, department.getShortName()));
        record.put("PARENT_ID", new RefBookValue(RefBookAttributeType.REFERENCE, getParentValue()));
        record.put("TYPE", new RefBookValue(RefBookAttributeType.REFERENCE, getTypeValue()));
        record.put("TB_INDEX", new RefBookValue(RefBookAttributeType.STRING, department.getTbIndex()));
        record.put("SBRF_CODE", new RefBookValue(RefBookAttributeType.STRING, department.getSbrfCode()));
        record.put("IS_ACTIVE", new RefBookValue(RefBookAttributeType.NUMBER, department.isActive() ? 1 : 0));
        return record;
    }

    private Map<String, RefBookValue> getParentValue() {
        Map<String, RefBookValue> parentRecord = new HashMap<>();
        if (department.getParent() != null) {
            parentRecord.put("NAME", new RefBookValue(RefBookAttributeType.STRING, department.getParent().getName()));
        }
        return parentRecord;
    }

    private Map<String, RefBookValue> getTypeValue() {
        Map<String, RefBookValue> parentRecord = new HashMap<>();
        if (department.getType() != null) {
            parentRecord.put("NAME", new RefBookValue(RefBookAttributeType.STRING, department.getType().getLabel()));
        }
        return parentRecord;
    }
}
