package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налоговых форм(declaration)
 */
public enum DeclarationFormKind {
	/**
	 * Первичная НФ(declaration)
	 */
	PRIMARY(1, "Первичная"),
	/**
	 * Консолидированная НФ(declaration)
	 */
	CONSOLIDATED(2, "Консолидированная"),
	/**
	 * Отчетная НФ(declaration)
	 */
	REPORTS(3, "Отчетная");

	private final int id;
	private final String title;

	private DeclarationFormKind(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static DeclarationFormKind fromId(int kindId) {
		for (DeclarationFormKind kind: values()) {
			if (kind.id == kindId) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Wrong DeclarationFormKind id: " + kindId);
	}

    public static DeclarationFormKind fromName(String name) {
        for (DeclarationFormKind kind: values()) {
            if (kind.title.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong DeclarationFormKind id: " + name);
    }
}
