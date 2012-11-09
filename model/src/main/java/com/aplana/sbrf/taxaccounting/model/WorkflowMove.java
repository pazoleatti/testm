package com.aplana.sbrf.taxaccounting.model;

/**
 * Переход между статусами жизненного цикла
 */
public class WorkflowMove {
	int id;
	int name;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getName() {
		return name;
	}
	public void setName(int name) {
		this.name = name;
	}
}
