package com.aplana.sbrf.taxaccounting.model;

/**
 * Стадии жизненного цикла налоговой формы(declaration)
 */
public enum State {
	/** Статус "Создана" */
	CREATED(1, "Создана", "Создание"),
	/** Статус "Подготовлена" */
	PREPARED(2, "Подготовлена", "Подготовка"),
	/** Статус "Принята" */
	ACCEPTED(3, "Принята", "Принятие"),
    /** Статус "Не создана" */
    NOT_EXIST(null, "Не создана", "Создание");

	private final Integer id;
	private final String title;
    private final String actionName;

	private State(Integer id, String title, String actionName) {
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

    public static State fromId(Integer id) {
        if (id == null) {
            return NOT_EXIST;
        }
		for(State state: values()) {
            if (id.equals(state.id)) {
				return state;
			}
		}
		throw new IllegalArgumentException("Wrong WorkflowState id: " + id);
	}
}
