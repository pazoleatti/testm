package com.aplana.sbrf.taxaccounting.model;

/**
 * Переход между статусами жизненного цикла
 */
public class WorkflowMove {
	private int id;
	private int name;
	private int fromStateId;
	private int toStateId;
	
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
	public int getFromStateId() {
		return fromStateId;
	}
	public void setFromStateId(int fromStateId) {
		this.fromStateId = fromStateId;
	}
	public int getToStateId() {
		return toStateId;
	}
	public void setToStateId(int toStateId) {
		this.toStateId = toStateId;
	}
}
