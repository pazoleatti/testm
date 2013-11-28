package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Eugene Stetsenko
 */
public class MembersFilterData implements Serializable{
	private static final long serialVersionUID = 5954894694420337600L;

	Boolean active;
	String userName;
	List<Integer> roleIds;
	Set<Integer> departmentIds;

	Integer countOfRecords;
	Integer startIndex;

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

	public List<Integer> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Integer> roleIds) {
		this.roleIds = roleIds;
	}

	public Set<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(Set<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}
}
