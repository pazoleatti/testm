package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;

import java.util.LinkedList;
import java.util.List;

/**
 * Подразделения
 *
 * @author dloshkarev
 */
public class RefBookDepartment extends RefBookSimple<Integer> {
    private String name;
    private String shortName;
    private Integer parentId;
    private DepartmentType type;
    private String tbIndex;
    private String sbrfCode;
    private Long regionId;
    private boolean isActive;
    private Long code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public DepartmentType getType() {
        return type;
    }

    public void setType(DepartmentType type) {
        this.type = type;
    }

    public String getTbIndex() {
        return tbIndex;
    }

    public void setTbIndex(String tbIndex) {
        this.tbIndex = tbIndex;
    }

    public String getSbrfCode() {
        return sbrfCode;
    }

    public void setSbrfCode(String sbrfCode) {
        this.sbrfCode = sbrfCode;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }
}
