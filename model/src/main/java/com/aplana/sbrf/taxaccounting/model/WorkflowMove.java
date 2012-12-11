package com.aplana.sbrf.taxaccounting.model;


import static com.aplana.sbrf.taxaccounting.model.FormDataEvent.*;
import static com.aplana.sbrf.taxaccounting.model.WorkflowState.*;

/**
 * Переход между стадиями жизненного цикла
 */
public enum WorkflowMove {
	CREATED_TO_APPROVED(1, "Утвердить", CREATED, APPROVED, MOVE_CREATED_TO_APPROVED),
	APPROVED_TO_CREATED(2, "Вернуть в \"Создана\"", APPROVED, CREATED, MOVE_APPROVED_TO_CREATED),
	APPROVED_TO_ACCEPTED(3, "Принять", APPROVED, ACCEPTED, MOVE_APPROVED_TO_ACCEPTED, AFTER_MOVE_APPROVED_TO_ACCEPTED),
	ACCEPTED_TO_APPROVED(4, "Вернуть в \"Утверждена\"", ACCEPTED, APPROVED, MOVE_ACCEPTED_TO_APPROVED, AFTER_MOVE_ACCEPTED_TO_APPROVED),
	CREATED_TO_ACCEPTED(5, "Принять", CREATED, ACCEPTED, MOVE_CREATED_TO_ACCEPTED, AFTER_MOVE_CREATED_TO_ACCEPTED),
	ACCEPTED_TO_CREATED(6, "Вернуть в \"Создана\"", ACCEPTED, CREATED, MOVE_ACCEPTED_TO_CREATED, AFTER_MOVE_ACCEPTED_TO_CREATED);
	
	private final int id;
	private final String name;
	private final WorkflowState fromState;
	private final WorkflowState toState;
	private final FormDataEvent event;
	private final FormDataEvent afterEvent;

	private WorkflowMove(int id, String name, WorkflowState fromState, WorkflowState toState, FormDataEvent event, FormDataEvent afterEvent) {
		this.id = id;
		this.name = name;
		this.fromState = fromState;
		this.toState = toState;
		this.event = event;
		this.afterEvent = afterEvent;
	}

	private WorkflowMove(int id, String name, WorkflowState fromState, WorkflowState toState, FormDataEvent event) {
		this(id, name, fromState, toState, event, null);
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

	public FormDataEvent getAfterEvent() {
		return afterEvent;
	}

	public static WorkflowMove fromId(int id) {
		for(WorkflowMove state: values()) {
			if (state.id == id) {
				return state;
			}
		}
		throw new IllegalArgumentException("Wrong WorkflowMove id: " + id);
	}
}
