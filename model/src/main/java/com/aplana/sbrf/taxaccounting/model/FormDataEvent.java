package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Считаем этот - enum ScriptEvent 
 * (рефакторить имя пока не будем т.к. на него завязаны скрипты)
 * TODO: [sgoryachkin] Порефакторить.
 * Задача: SBRFACCTAX-1862
 * 
 * События системы. К ним привязываются скрипты.
 *
 * @author Vitalii Samolovskikh
 */
public enum FormDataEvent implements Serializable {
	CREATE(1, "Создать"),
	AFTER_CREATE(12, "После создания"),
	DELETE(2, "Удалить"),
	CALCULATE(3, "Рассчитать"),
	COMPOSE(4, "Обобщить"),
	CHECK(5, "Проверить"),
	SAVE(6, "Сохранить"),
	IMPORT(7, "Импорт данных"),
	GET_LEVEL0(8, "Получение данных"),
	GET_LEVEL1(9, "Получение защищенных данных"),
    TEST(10, "Тестирование скриптов"),
	MIGRATION (11, "Миграция из АС \"Ведение РНУ\""),

	MOVE_CREATED_TO_APPROVED(101, "Утвердить из \"Создана\""),
	MOVE_APPROVED_TO_CREATED(102, "Вернуть из \"Утверждена\" в \"Создана\""),
	MOVE_APPROVED_TO_ACCEPTED(103, "Принять из \"Утверждена\""),
	MOVE_ACCEPTED_TO_APPROVED(104, "Вернуть из \"Принята\" в \"Утверждена\""),
	MOVE_CREATED_TO_ACCEPTED(105, "Принять из \"Создана\""),
	MOVE_ACCEPTED_TO_CREATED(106, "Вернуть из \"Принята\" в \"Создана\""),
	MOVE_CREATED_TO_PREPARED(107, "Подготовить из \"Создана\""),
	MOVE_PREPARED_TO_CREATED(108,  "Вернуть из \"Подготовлена\" в \"Создана\""),
	MOVE_PREPARED_TO_ACCEPTED(109,  "Принять из \"Подготовлена\""),
	MOVE_ACCEPTED_TO_PREPARED(110,  "Вернуть из \"Принята\" в \"Подготовлена\""),
	MOVE_PREPARED_TO_APPROVED(111,  "Утвердить из \"Подготовлена\""),
	MOVE_APPROVED_TO_PREPARED(112,  "Вернуть из \"Утверждена\" в \"Подготовлена\""),

	AFTER_MOVE_APPROVED_TO_ACCEPTED(203, "После принять из \"Утверждена\""),
	AFTER_MOVE_ACCEPTED_TO_APPROVED(204, "После вернуть из \"Принята\" в \"Утверждена\""),
	AFTER_MOVE_CREATED_TO_ACCEPTED(205, "После принять из \"Создана\""),
	AFTER_MOVE_ACCEPTED_TO_CREATED(206, "После вернуть из \"Принята\" в \"Создана\""),
	AFTER_MOVE_PREPARED_TO_ACCEPTED(207, "После принять из \"Подготовлена\""),
    AFTER_MOVE_ACCEPTED_TO_PREPARED(208, "После вернуть из \"Принята\" в \"Подготовлена\""),
	AFTER_MOVE_PREPARED_TO_APPROVED(209, "После утвердить из \"Подготовлена\""),
	AFTER_MOVE_APPROVED_TO_PREPARED(210, "После вернуть \"Подготовлена\" из \"Утверждена\""),

	ADD_ROW(301, "Добавить строку"),
	DELETE_ROW(303, "Удалить строку"),
	AFTER_LOAD(302, "Загрузка"),	/* Вызывается после того как форма была получена из БД. */

	IMPORT_TRANSPORT_FILE(401, "Импорт из транспортных файлов"),
    UPLOAD_TRANSPORT_FILE(402, "Загрузка транспортных файлов в каталог загрузки"),

    LOGIN(501, "Вход пользователя в Систему"),
    LOGOUT(502, "Выход пользователя из Системы"),
    EXTERNAL_INTERACTION(503, "Взаимодействие с внешней АС"),

	LOG_SYSTEM_BACKUP(601, "Архивирование журнала событий"),

    SEND_EMAIL(650, "Отправка email"),

    TEMPLATE_CREATED(701, "Версия создана"),
    TEMPLATE_MODIFIED(702, "Версия изменена"),
    TEMPLATE_ACTIVATED(703, "Версия введена в действие"),
    TEMPLATE_DEACTIVATED(704, "Версия выведена из действия"),
    TEMPLATE_DELETED(705, "Версия удалена"),

    LOGIN_MODULE(801, "Вход пользователя в модуль"),
    LOGOUT_MODULE(802, "Выход пользователя из модуля"),

    UPLOAD_CONTRACTS(810, "Загрузка данных о договорах обеспечения"),
    UPLOAD_CUSTOMER_DATA(811, "Загрузка данных о клиентах"),
    UPLOAD_PAYMENTS_DATA(812, "Загрузка данных о платежах"),
    UPLOAD_REFBOOK(813, "Загрузка справочника"),

    CREATE_CLIENT_PROFILES(820, "Создание анкеты клиента"),
    EDIT_CLIENT_PROFILES(821, "Редактирование анкеты клиента"),

    CREATE_GUARANTEE(830, "Создание договора гарантии"),
    EDIT_GUARANTEE(831, "Редактирование договора гарантии"),
    CLOSE_GUARANTEE(832, "Закрытие договора гарантии"),

    CREATE_SECURITY_AGREEMENTS(840, "Создание договора обеспечения"),
    EDIT_SECURITY_AGREEMENTS(841, "Редактирование договора обеспеченияь"),
    CLOSE_SECURITY_AGREEMENTS(842, "Закрытие договора обеспечения"),

    CREATE_TASK_FORMATION_RNU23(850, "Создание задачи формирования РНУ-23"),
    CREATE_TASK_FORMATION_REPORT(860, "Создание задачи формирования отчета"),

    ADD_DEPARTMENT(901, "Создание подразделения"),
    UPDATE_DEPARTMENT(902, "Модификация подразделения"),
    DELETE_DEPARTMENT(903, "Удаление подразделения");

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
