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
	COMPOSE(4, "Обобщить"),
	CHECK(5, "Проверить"),
	SAVE(6, "Сохранить"),

	MOVE_CREATED_TO_APPROVED(101, "Утвердить"),
	MOVE_APPROVED_TO_CREATED(102, "Вернуть из \"Утверждена\" в \"Создана\""),
	MOVE_APPROVED_TO_ACCEPTED(103, "Принять из \"Утверждена\""),
	MOVE_ACCEPTED_TO_APPROVED(104, "Вернуть из \"Принята\" в \"Утверждена\""),
	MOVE_CREATED_TO_ACCEPTED(105, "Принять из \"Создана\""),
	MOVE_ACCEPTED_TO_CREATED(106, "Вернуть из \"Принята\" в \"Создана\""),
	MOVE_CREATED_TO_PREPARED(107, "Подготовить"),
	MOVE_PREPARED_TO_CREATED(108,  "Вернуть из \"Подготовлена\" в \"Создана\""),
	MOVE_PREPARED_TO_ACCEPTED(109,  "Принять из \"Подготовлена\""),
	MOVE_ACCEPTED_TO_PREPARED(110,  "Вернуть из \"Принята\" в \"Подготовлена\""),

	AFTER_MOVE_APPROVED_TO_ACCEPTED(203, "После принять из \"Утверждена\""),
	AFTER_MOVE_ACCEPTED_TO_APPROVED(204, "После вернуть из \"Принята\" в \"Утверждена\""),
	AFTER_MOVE_CREATED_TO_ACCEPTED(205, "После принять из \"Создана\""),
	AFTER_MOVE_ACCEPTED_TO_CREATED(206, "После вернуть из \"Принята\" в \"Создана\""),

	ADD_ROW(301, "Добавить строку"),
	DELETE_ROW(303, "Удалить строку"),
	AFTER_LOAD(302, "Загрузка");	/* Вызывается после того как форма была получена из БД. */

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
