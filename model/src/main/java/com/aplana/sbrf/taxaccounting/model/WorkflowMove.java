package com.aplana.sbrf.taxaccounting.model;

/**
 * ќбъект, представл€ющий информацию о допустимом переходе по стади€м жизненного цикла
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
