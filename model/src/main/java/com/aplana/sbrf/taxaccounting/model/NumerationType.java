package com.aplana.sbrf.taxaccounting.model;

/**
 * @author Fail Mukhametdinov
 */
public enum NumerationType {
	/** Последовательная */
    SERIAL(0, "Последовательная"),
    /** Сквозная */
	CROSS(1, "Сквозная");

    private int id;
    private String title;

    NumerationType(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

	public static NumerationType getById(int id) {
		for (NumerationType numType : values()) {
			if (numType.getId() == id) {
				return numType;
			}
		}
		throw new IllegalArgumentException("Doesn't exist numeration type with id = " + id);
	}
}