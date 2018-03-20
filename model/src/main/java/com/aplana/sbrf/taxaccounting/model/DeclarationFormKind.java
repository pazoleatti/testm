package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Типы налоговых форм(declaration)
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
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

	private final long id;
	private final String title;

	private DeclarationFormKind(long id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public long getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public static DeclarationFormKind fromId(long kindId) {
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
