package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налоговых форм 
 */
public enum FormDataKind {
	/**
	 * Первичная НФ
	 */
	PRIMARY(1, "Первичная"),
	/**
	 * Консолидированная НФ
	 */
	CONSOLIDATED(2, "Консолидированная"),
	/**
	 * Сводная НФ
	 */
	SUMMARY(3, "Сводная"),
	/**
	 * Форма УНП
	 * (используется для проверки и корректировки данных в декларациях по налогу на прибыль)
	 */
	UNP(4, "Форма УНП"),
	/**
	 * Выходная форма
	 * (содержит информацию, которая должна попасть в декларацию, но отсутствует в сводных формах)
	 */
	ADDITIONAL(5, "Выходная"),
    /**
     * Расеетная форма
     */
    CALCULATED(6, "Расчетная");
	
	private final int id;
	private final String title;
	
	private FormDataKind(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static FormDataKind fromId(int kindId) {
		for (FormDataKind kind: values()) {
			if (kind.id == kindId) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Wrong FormDataKind id: " + kindId);
	}

    public static FormDataKind fromName(String name) {
        for (FormDataKind kind: values()) {
            if (kind.title.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong FormDataKind id: " + name);
    }
}
