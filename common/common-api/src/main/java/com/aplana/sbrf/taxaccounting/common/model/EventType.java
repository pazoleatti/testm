package com.aplana.sbrf.taxaccounting.common.model;

import java.io.Serializable;

/**
 * Перечисление событий модуля гарантий
 *
 * @author aivanov
 */
public enum EventType implements Serializable {

    UPLOAD_CONTRACTS(810, "Гарантии: Загрузка данных о договорах обеспечения"),
    UPLOAD_CUSTOMER_DATA(811, "Гарантии: Загрузка данных о клиентах"),
    UPLOAD_PAYMENTS_DATA(812, "Гарантии: Загрузка данных о платежах"),
    UPLOAD_REFBOOK(813, "Гарантии: Загрузка справочника"),

    CREATE_CLIENT_PROFILES(820, "Гарантии: Создание анкеты клиента"),
    EDIT_CLIENT_PROFILES(821, "Гарантии: Редактирование анкеты клиента"),

    CREATE_GUARANTEE(830, "Гарантии: Создание договора гарантии"),
    EDIT_GUARANTEE(831, "Гарантии: Редактирование договора гарантии"),
    CLOSE_GUARANTEE(832, "Гарантии: Закрытие договора гарантии"),

    CREATE_OBSP(840, "Гарантии: Создание договора обеспечения"),
    EDIT_OBSP(841, "Гарантии: Редактирование договора обеспечения"),
    CLOSE_OBSP(842, "Гарантии: Закрытие договора обеспечения"),

    CREATE_TASK_FORMATION_RNU23(850, "Гарантии: Создание задачи формирования РНУ-23"),
    CREATE_TASK_FORMATION_REPORT(860, "Гарантии: Создание задачи формирования отчета");

    /* Название события */
	private String title;
    /* Код события */
	private int code;

	private EventType(int code, String title) {
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
	 * @return код события
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
	public static EventType getByCode(int code) {
		EventType event = null;

		for (EventType e : EventType.values()) {
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
