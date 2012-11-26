package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * События формы. К ним привязываются скрипты.
 *
 * @author Vitalii Samolovskikh
 */
public enum FormDataEvent implements Serializable {
	CREATE(1, "Создать"),
	DELETE(2, "Удалить"),
	CALCULATE(3, "Рассчитать"),
	INTEGRATION(4, "Обобщить"),

	MOVE_CREATED_TO_APPROVED(101, "Утвердить"),
	MOVE_APPROVED_TO_CREATED(102, "Вернуть в \"Создана\""),
	MOVE_APPROVED_TO_ACCEPTED(103, "Принять"),
	MOVE_ACCEPTED_TO_APPROVED(104, "Вернуть в \"Принята\""),
	MOVE_CREATED_TO_ACCEPTED(105, "Принять"),
	MOVE_ACCEPTED_TO_CREATED(106, "Вернуть в \"Создана\"");

	private String title;
	private int code;

	private FormDataEvent(int code, String title) {
		this.code = code;
		this.title = title;
	}

	/**
	 * @return название события на русском человекопонятном языке
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return код события. Используется для маппинга в БД.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Возвращает событие формы по идентификатору.
	 *
	 * @param code идентификатор события
	 * @return событие
	 */
	public static FormDataEvent getByCode(int code) {
		FormDataEvent event = null;

		for (FormDataEvent e : FormDataEvent.values()) {
			if (e.getCode() == code) {
				event = e;
				break;
			}
		}

		if (event != null) {
			return event;
		} else {
			throw new IllegalArgumentException("Event with code " + code + " doesn't exist.");
		}
	}
}
