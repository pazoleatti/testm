package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Модель фильтра для выборки пользователей
 * @author Eugene Stetsenko
 */
public class MembersFilterData implements Serializable {
    private static final long serialVersionUID = 5954894694420337600L;

    public enum SortField {
        NAME,
        LOGIN,
        MAIL,
        ACTIVE,
        DEPARTMENT,
        ROLE,
        ASNU
    }

    private Boolean active;
    private String userName;
    private List<Long> roleIds;
    private Set<Integer> departmentIds;

    private Integer countOfRecords;
    private Integer startIndex;

    private SortField sortField;
    private boolean isAsc = true;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCountOfRecords() {
        return countOfRecords;
    }

    public void setCountOfRecords(Integer countOfRecords) {
        this.countOfRecords = countOfRecords;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public SortField getSortField() {
        return sortField;
    }

    public void setSortField(SortField sortField) {
        this.sortField = sortField;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public void setAsc(boolean isAsc) {
        this.isAsc = isAsc;
    }
}
