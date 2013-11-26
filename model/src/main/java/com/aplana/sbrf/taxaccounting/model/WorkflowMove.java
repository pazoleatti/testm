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
    CREATED_TO_APPROVED(1, "Утвердить", "Создан - Утвержден", CREATED, APPROVED, MOVE_CREATED_TO_APPROVED, false),
    /** Вернуть из "Утверждена" в "Создана" */
    APPROVED_TO_CREATED(2, "Вернуть в \"Создана\"", "Возврат: Утвержден - Создан", APPROVED, CREATED, MOVE_APPROVED_TO_CREATED, true),
    /** Из "Утверждена" в "Принята" */
    APPROVED_TO_ACCEPTED(3, "Принять", "Утвержден - Принят", APPROVED, ACCEPTED, MOVE_APPROVED_TO_ACCEPTED, AFTER_MOVE_APPROVED_TO_ACCEPTED, false),
    /** Вернуть из "Принята" в "Утверждена" */
    ACCEPTED_TO_APPROVED(4, "Вернуть в \"Утверждена\"", "Возврат: Принят - Утвержден", ACCEPTED, APPROVED, MOVE_ACCEPTED_TO_APPROVED, AFTER_MOVE_ACCEPTED_TO_APPROVED, true),
    /** Из "Создана" в "Принята" */
    CREATED_TO_ACCEPTED(5, "Принять", "Создан - Принят", CREATED, ACCEPTED, MOVE_CREATED_TO_ACCEPTED, AFTER_MOVE_CREATED_TO_ACCEPTED, false),
    /** Вернуть из "Принята" в "Создана" */
    ACCEPTED_TO_CREATED(6, "Вернуть в \"Создана\"", "Возврат: Принят - Создан", ACCEPTED, CREATED, MOVE_ACCEPTED_TO_CREATED, AFTER_MOVE_ACCEPTED_TO_CREATED, true),
    /** Из "Создана" в "Подготовлена" */
    CREATED_TO_PREPARED(7, "Подготовить", "Создан - Подготовлен", CREATED, PREPARED, MOVE_CREATED_TO_PREPARED, false),
    /** Вернуть из "Подготовлена" в "Создана" */
    PREPARED_TO_CREATED(8, "Вернуть в \"Создана\"", "Возврат: Подготовлен - Создан", PREPARED, CREATED, MOVE_PREPARED_TO_CREATED, true),
    /** Из "Подготовлена" в "Принята" */
    PREPARED_TO_ACCEPTED(9, "Принять", "Подготовлен - Принят", PREPARED, ACCEPTED, MOVE_PREPARED_TO_ACCEPTED, AFTER_MOVE_PREPARED_TO_ACCEPTED, false),
    /** Вернуть из "Принята" в "Подготовлена" */
    ACCEPTED_TO_PREPARED(10, "Вернуть в \"Подготовлена\"", "Возврат: Принят - Подготовлен", ACCEPTED, PREPARED, MOVE_ACCEPTED_TO_PREPARED, AFTER_MOVE_ACCEPTED_TO_PREPARED, true),
    /** Из "Подготовлена" в "Утверждена" */
    PREPARED_TO_APPROVED(11, "Утвердить", "Подготовлен - Утвержден", PREPARED, APPROVED, MOVE_PREPARED_TO_APPROVED, AFTER_MOVE_PREPARED_TO_APPROVED, false),
    /** Вернуть из "Утверждена" в "Подготовлена" */
    APPROVED_TO_PREPARED(12, "Вернуть в \"Подготовлена\"", "Возврат: Утвержден - Подготовлен", APPROVED, PREPARED, MOVE_APPROVED_TO_PREPARED, AFTER_MOVE_APPROVED_TO_PREPARED, true);


	private final int id;
	private final String name;
	private final WorkflowState fromState;
	private final WorkflowState toState;
	private final FormDataEvent event;
	private final FormDataEvent afterEvent;
	private final boolean isReasonToMoveShouldBeSpecified;
    private final String route;

    private WorkflowMove(int id, String name, String route, WorkflowState fromState, WorkflowState toState, FormDataEvent event,
                         FormDataEvent afterEvent, boolean isReasonToMoveShouldBeSpecified) {
        this.id = id;
        this.name = name;
        this.fromState = fromState;
        this.toState = toState;
        this.event = event;
        this.afterEvent = afterEvent;
        this.isReasonToMoveShouldBeSpecified = isReasonToMoveShouldBeSpecified;
        this.route = route;
    }

    private WorkflowMove(int id, String name, String route, WorkflowState fromState, WorkflowState toState, FormDataEvent event,
                         boolean isReasonToMoveShouldBeSpecified) {
        this(id, name, route, fromState, toState, event, null, isReasonToMoveShouldBeSpecified);
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

    public String getRoute() {
        return route;
    }

	@Override
	public String toString() {
		return "WorkflowMove{" + name() + '}';
	}
}
