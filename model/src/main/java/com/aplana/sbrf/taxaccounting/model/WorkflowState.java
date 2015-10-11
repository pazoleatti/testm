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
	ACCEPTED(4, "Принята", "Принятие"),
    /** Статус "Не создана" */
    NOT_EXIST(null, "Не создана", "Создание");

	private final Integer id;
	private final String title;
    private final String actionName;
	
	private WorkflowState(Integer id, String title, String actionName) {
		this.id = id;
		this.title = title;
        this.actionName = actionName;
	}
	
	public Integer getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
    public String getActionName() {
        return actionName;
    }

    public static WorkflowState fromId(Integer id) {
        if (id == null) {
            return NOT_EXIST;
        }
		for(WorkflowState state: values()) {
            if (id.equals(state.id)) {
				return state;
			}
		}
		throw new IllegalArgumentException("Wrong WorkflowState id: " + id);
	}
}
