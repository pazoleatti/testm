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
	ADDITIONAL(5, "Выходная");
	
	private final int id;
	private final String name;
	
	private FormDataKind(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public static FormDataKind fromId(int kindId) {
		for (FormDataKind kind: values()) {
			if (kind.id == kindId) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Wrong FormDataKind id: " + kindId);
	}
}
