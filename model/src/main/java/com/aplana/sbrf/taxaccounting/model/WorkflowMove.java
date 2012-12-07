package com.aplana.sbrf.taxaccounting.model;


/**
 * Переход между стадиями жизненного цикла
 */
public enum WorkflowMove {
	CREATED_TO_APPROVED(1, "Утвердить", WorkflowState.CREATED, WorkflowState.APPROVED, FormDataEvent.MOVE_CREATED_TO_APPROVED),
	APPROVED_TO_CREATED(2, "Вернуть в \"Создана\"", WorkflowState.APPROVED, WorkflowState.CREATED, FormDataEvent.MOVE_APPROVED_TO_CREATED),
	APPROVED_TO_ACCEPTED(3, "Принять", WorkflowState.APPROVED, WorkflowState.ACCEPTED, FormDataEvent.MOVE_APPROVED_TO_ACCEPTED),
	ACCEPTED_TO_APPROVED(4, "Вернуть в \"Утверждена\"", WorkflowState.ACCEPTED, WorkflowState.APPROVED, FormDataEvent.MOVE_ACCEPTED_TO_APPROVED),
	CREATED_TO_ACCEPTED(5, "Принять", WorkflowState.CREATED, WorkflowState.ACCEPTED, FormDataEvent.MOVE_CREATED_TO_ACCEPTED),
	ACCEPTED_TO_CREATED(6, "Вернуть в \"Создана\"", WorkflowState.ACCEPTED, WorkflowState.CREATED, FormDataEvent.MOVE_ACCEPTED_TO_CREATED);
	
	private final int id;
	private final String name;
	private final WorkflowState fromState;
	private final WorkflowState toState;
	private final FormDataEvent event;

	private WorkflowMove(int id, String name, WorkflowState fromState, WorkflowState toState, FormDataEvent event) {
		this.id = id;
		this.name = name;
		this.fromState = fromState;
		this.toState = toState;
		this.event = event;
	}
	
	public int getId() {
		return id;
	}	
	public String getName() {
		return name;
	}
	public WorkflowState getFromState() {
		return fromState;
	}
	public WorkflowState getToState() {
		return toState;
	}

	public FormDataEvent getEvent() {
		return event;
	}
}
