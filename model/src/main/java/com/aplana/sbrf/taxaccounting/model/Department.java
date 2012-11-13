package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Set;


/**
 * Подразделение банка
 * @author dsultanbekov
 */
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;	
	private int id;
	private String name;
	private Integer parentId;
	private DepartmentType type;
	private Set<Integer> formTypes;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
}
