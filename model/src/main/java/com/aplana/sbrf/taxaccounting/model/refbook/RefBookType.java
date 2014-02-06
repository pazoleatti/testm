package com.aplana.sbrf.taxaccounting.model.refbook;

import java.util.NoSuchElementException;

/**
 * Типы справочников
 *
 * @author Stanislav Yasinskiy
 */
public enum RefBookType {

	LINEAR ("Линейный"),
	HIERARCHICAL ("Иерархический");

	private String title;

	RefBookType(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	/** Возвращает код типа*/
	public int getId() {
		for(int i=0; i<values().length; i++) {
			if (this.equals(values()[i])) {
				return i;
			}
		}
		throw new NoSuchElementException("Id not found");
	}

	/** Возвращает элемент по коду */
	public static RefBookType get(int id) {
		switch (id) {
			case 0:
				return LINEAR;
			case 1:
				return HIERARCHICAL;
			default:
				throw new IllegalArgumentException("Id not found");
		}
	}

}
