package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налоговых форм(declaration)
 */
public enum DeclarationFormKind {
    /**
     * Выходная форма
     * (содержит информацию, которая должна попасть в декларацию, но отсутствует в сводных формах)
     */
    ADDITIONAL(1, "Выходная"),
    /**
     * Консолидированная НФ
     */
    CONSOLIDATED(2, "Консолидированная"),
    /**
     * Первичная НФ
     */
    PRIMARY(3, "Первичная"),
    /**
     * Сводная НФ
     */
    SUMMARY(4, "Сводная"),
    /**
     * Форма УНП
     * (используется для проверки и корректировки данных в декларациях по налогу на прибыль)
     */
    UNP(5, "Форма УНП"),
    /**
     * Расеетная форма
     */
    CALCULATED(6, "Расчетная"),
	/**
	 * Отчетная НФ(declaration)
	 */
	REPORTS(7, "Отчетная");

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
