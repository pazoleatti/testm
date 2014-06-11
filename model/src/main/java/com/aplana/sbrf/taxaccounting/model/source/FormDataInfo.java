package com.aplana.sbrf.taxaccounting.model.source;

import java.io.Serializable;

/**
 * Объект с данными нф, предназначенный исключетельно для предоставления визуальной информации
 * @author Denis Loshkarev
 */
public class FormDataInfo implements Serializable {
    private static final long serialVersionUID = -8997760915262888601L;

    private Long id;
    private Long departmentFormTypeId;
    private String formTypeName;
    private String formKindName;
    private String period;
    private String department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentFormTypeId() {
        return departmentFormTypeId;
    }

    public void setDepartmentFormTypeId(Long departmentFormTypeId) {
        this.departmentFormTypeId = departmentFormTypeId;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    public String getFormKindName() {
        return formKindName;
    }

    public void setFormKindName(String formKindName) {
        this.formKindName = formKindName;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormDataInfo that = (FormDataInfo) o;

        if (department != null ? !department.equals(that.department) : that.department != null) return false;
        if (departmentFormTypeId != null ? !departmentFormTypeId.equals(that.departmentFormTypeId) : that.departmentFormTypeId != null)
            return false;
        if (formKindName != null ? !formKindName.equals(that.formKindName) : that.formKindName != null) return false;
        if (formTypeName != null ? !formTypeName.equals(that.formTypeName) : that.formTypeName != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (period != null ? !period.equals(that.period) : that.period != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (departmentFormTypeId != null ? departmentFormTypeId.hashCode() : 0);
        result = 31 * result + (formTypeName != null ? formTypeName.hashCode() : 0);
        result = 31 * result + (formKindName != null ? formKindName.hashCode() : 0);
        result = 31 * result + (period != null ? period.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        return result;
    }
}
