package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Типы справочников
 *
 * @author Stanislav Yasinskiy
 */
public enum RefBookType {

    EXTERNAL("Внешний справочник"),
    INTERNAL("Внутренний справочник");

    private final String title;

    private RefBookType(String title) {
        this.title = title;
    }

	@Override
	public String toString() {
		return title;
	}
}
