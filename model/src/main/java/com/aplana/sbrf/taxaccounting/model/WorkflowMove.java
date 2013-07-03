package com.aplana.sbrf.taxaccounting.model;

import static com.aplana.sbrf.taxaccounting.model.FormDataEvent.*;
import static com.aplana.sbrf.taxaccounting.model.WorkflowState.ACCEPTED;
import static com.aplana.sbrf.taxaccounting.model.WorkflowState.APPROVED;
import static com.aplana.sbrf.taxaccounting.model.WorkflowState.CREATED;
import static com.aplana.sbrf.taxaccounting.model.WorkflowState.PREPARED;

/**
 * Переход между стадиями жизненного цикла
 */
public enum WorkflowMove {
	/** Из "Создана" в "Утверждена" */
	CREATED_TO_APPROVED(1, "Утвердить", CREATED, APPROVED, MOVE_CREATED_TO_APPROVED, false),
	/** Вернуть из "Утверждена" в "Создана" */
	APPROVED_TO_CREATED(2, "Вернуть в \"Создана\"", APPROVED, CREATED, MOVE_APPROVED_TO_CREATED, true),
	/** Из "Утверждена" в "Принята" */
	APPROVED_TO_ACCEPTED(3, "Принять", APPROVED, ACCEPTED, MOVE_APPROVED_TO_ACCEPTED, AFTER_MOVE_APPROVED_TO_ACCEPTED, false),
	/** Вернуть из "Принята" в "Утверждена" */
	ACCEPTED_TO_APPROVED(4, "Вернуть в \"Утверждена\"", ACCEPTED, APPROVED, MOVE_ACCEPTED_TO_APPROVED, AFTER_MOVE_ACCEPTED_TO_APPROVED, true),
	/** Из "Создана" в "Принята" */
	CREATED_TO_ACCEPTED(5, "Принять", CREATED, ACCEPTED, MOVE_CREATED_TO_ACCEPTED, AFTER_MOVE_CREATED_TO_ACCEPTED, false),
	/** Вернуть из "Принята" в "Создана" */
	ACCEPTED_TO_CREATED(6, "Вернуть в \"Создана\"", ACCEPTED, CREATED, MOVE_ACCEPTED_TO_CREATED, AFTER_MOVE_ACCEPTED_TO_CREATED, true),
	/** Из "Создана" в "Подготовлена" */
	CREATED_TO_PREPARED(7, "Подготовить", CREATED, PREPARED, MOVE_CREATED_TO_PREPARED, false),
	/** Вернуть из "Подготовлена" в "Создана" */
	PREPARED_TO_CREATED(8, "Вернуть в \"Создана\"", PREPARED, CREATED, MOVE_PREPARED_TO_CREATED, true),
	/** Из "Подготовлена" в "Принята" */
	PREPARED_TO_ACCEPTED(9, "Принять", PREPARED, ACCEPTED, MOVE_PREPARED_TO_ACCEPTED, AFTER_MOVE_PREPARED_TO_ACCEPTED, false),
	/** Вернуть из "Принята" в "Подготовлена" */
	ACCEPTED_TO_PREPARED(10, "Вернуть в \"Подготовлена\"", ACCEPTED, PREPARED, MOVE_ACCEPTED_TO_PREPARED, AFTER_MOVE_ACCEPTED_TO_PREPARED, true),
	/** Из "Подготовлена" в "Утверждена" */
	PREPARED_TO_APPROVED(11, "Утвердить", PREPARED, APPROVED, MOVE_PREPARED_TO_APPROVED, AFTER_MOVE_PREPARED_TO_APPROVED, false),
	/** Вернуть из "Утверждена" в "Подготовлена" */
	APPROVED_TO_PREPARED(12, "Вернуть в \"Подготовлена\"", APPROVED, PREPARED, MOVE_APPROVED_TO_PREPARED, AFTER_MOVE_APPROVED_TO_PREPARED, true);


	private final int id;
	private final String name;
	private final WorkflowState fromState;
	private final WorkflowState toState;
	private final FormDataEvent event;
	private final FormDataEvent afterEvent;
	private final boolean isReasonToMoveShouldBeSpecified;

	private WorkflowMove(int id, String name, WorkflowState fromState, WorkflowState toState, FormDataEvent event,
	                     FormDataEvent afterEvent, boolean isReasonToMoveShouldBeSpecified) {
		this.id = id;
		this.name = name;
		this.fromState = fromState;
		this.toState = toState;
		this.event = event;
		this.afterEvent = afterEvent;
		this.isReasonToMoveShouldBeSpecified = isReasonToMoveShouldBeSpecified;
	}

	private WorkflowMove(int id, String name, WorkflowState fromState, WorkflowState toState, FormDataEvent event,
	                     boolean isReasonToMoveShouldBeSpecified) {
		this(id, name, fromState, toState, event, null, isReasonToMoveShouldBeSpecified);
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

	public boolean isReasonToMoveShouldBeSpecified() {
		return isReasonToMoveShouldBeSpecified;
	}

	public static WorkflowMove fromId(int id) {
		for (WorkflowMove state : values()) {
			if (state.id == id) {
				return state;
			}
		}
		throw new IllegalArgumentException("Wrong WorkflowMove id: " + id);
	}

	@Override
	public String toString() {
		return "WorkflowMove{" + name() + '}';
	}



}
