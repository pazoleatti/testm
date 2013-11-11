package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Расширенная информация о подразделении выбранном в дереве. Включает связку подразделений родительское-дочернее
 * @author dloshkarev
 */
public class DepartmentPair implements Serializable {
    private static final long serialVersionUID = 2040877223129099468L;

    /** Подразделение, выбранное в дереве */
    private Integer departmentId;

    /** Родительское подразделение у выбранного */
    private Integer parentDepartmentId;

    /** Название выбранного подразделения */
    private String departmentName;

    public DepartmentPair(Integer departmentId, Integer parentDepartmentId, String departmentName) {
        this.departmentId = departmentId;
        this.parentDepartmentId = parentDepartmentId;
        this.departmentName = departmentName;
    }

    /**
     * Этот конструктор используется только при прямой установке значения в виджет
     */
    public DepartmentPair(Integer departmentId, Integer parentDepartmentId) {
        this.departmentId = departmentId;
        this.parentDepartmentId = parentDepartmentId;
    }

    public DepartmentPair() {
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(Integer parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepartmentPair that = (DepartmentPair) o;

        if (departmentId != null ? !departmentId.equals(that.departmentId) : that.departmentId != null) return false;
        if (parentDepartmentId != null ? !parentDepartmentId.equals(that.parentDepartmentId) : that.parentDepartmentId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = departmentId != null ? departmentId.hashCode() : 0;
        result = 31 * result + (parentDepartmentId != null ? parentDepartmentId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DepartmentPair{" +
                "departmentId=" + departmentId +
                ", parentDepartmentId=" + parentDepartmentId +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}
