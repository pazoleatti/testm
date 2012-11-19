package com.aplana.sbrf.taxaccounting.model;

/**
 * Типы налоговых форм 
 */
public enum FormDataKind {
	PRIMARY(1, "Первичная"),
	CONSOLIDATED(2, "Консолидированная"),
	SUMMARY(3, "Сводная");
	
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
