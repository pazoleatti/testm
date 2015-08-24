package com.aplana.sbrf.taxaccounting.model;

/**
 * Стадии жизненного цикла налоговой формы 
 */
public enum WorkflowState {
	/** Статус "Создана" */
	CREATED(1, "Создана", "Создание"),
	/** Статус "Подготовлена" */
	PREPARED(2, "Подготовлена", "Подготовка"),
	/** Статус "Утверждена" */
	APPROVED(3, "Утверждена", "Утверждение"),
	/** Статус "Принята" */
	ACCEPTED(4, "Принята", "Принятие");

	private final int id;
	private final String title;
    private final String actionName;
	
	private WorkflowState(int id, String title, String actionName) {
		this.id = id;
		this.title = title;
        this.actionName = actionName;
	}
	
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
    public String getActionName() {
        return actionName;
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
