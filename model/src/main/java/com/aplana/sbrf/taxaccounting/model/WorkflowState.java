package com.aplana.sbrf.taxaccounting.model;


/**
 * Стадии жизненного цикла налоговой формы 
 */
public enum WorkflowState {
	/** Статус "Создана" */
	CREATED(1, "Создана"),
	/** Статус "Подготовлена" */
	PREPARED(2, "Подготовлена"),
	/** Статус "Утверждена" */
	APPROVED(3, "Утверждена"),
	/** Статус "Принята" */
	ACCEPTED(4, "Принята");

	private final int id;
	private final String name;
	
	private WorkflowState(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public static WorkflowState fromId(int id) {
		for(WorkflowState state: values()) {
			if (state.id == id) {
				return state;
			}
		}
		throw new IllegalArgumentException("Wrong WorkflowState id: " + id);
	}
}
