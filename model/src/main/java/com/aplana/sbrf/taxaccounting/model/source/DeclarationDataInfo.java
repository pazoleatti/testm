package com.aplana.sbrf.taxaccounting.model.source;

import java.io.Serializable;


/**
 * Объект с данными декларации, предназначенный исключетельно для предоставления визуальной информации
 * @author Denis Loshkarev
 */
public class DeclarationDataInfo implements Serializable {
    private static final long serialVersionUID = 5426405385593042588L;

    private Long id;
    private Long departmentDeclarationTypeId;
    private String declarationTypeName;
    private String department;
    private String period;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentDeclarationTypeId() {
        return departmentDeclarationTypeId;
    }

    public void setDepartmentDeclarationTypeId(Long departmentDeclarationTypeId) {
        this.departmentDeclarationTypeId = departmentDeclarationTypeId;
    }

    public String getDeclarationTypeName() {
        return declarationTypeName;
    }

    public void setDeclarationTypeName(String declarationTypeName) {
        this.declarationTypeName = declarationTypeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationDataInfo that = (DeclarationDataInfo) o;

        if (declarationTypeName != null ? !declarationTypeName.equals(that.declarationTypeName) : that.declarationTypeName != null)
            return false;
        if (department != null ? !department.equals(that.department) : that.department != null) return false;
        if (departmentDeclarationTypeId != null ? !departmentDeclarationTypeId.equals(that.departmentDeclarationTypeId) : that.departmentDeclarationTypeId != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (period != null ? !period.equals(that.period) : that.period != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (departmentDeclarationTypeId != null ? departmentDeclarationTypeId.hashCode() : 0);
        result = 31 * result + (declarationTypeName != null ? declarationTypeName.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        return result;
    }
}
