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
	DELETE(2, "Удалить"),
	CALCULATE(3, "Рассчитать"),
	COMPOSE(4, "Обобщить"),
	CHECK(5, "Проверить"),
	SAVE(6, "Сохранить"),
	IMPORT(7, "Импорт данных"),
	GET_LEVEL0(8, "Получение данных"),
	GET_LEVEL1(9, "Получение защищенных данных"),
    AFTER_CREATE(12, "После создания"),
    SORT_ROWS (13, "Сортировка строк"),
    GET_SOURCES (14, "Получение источников-приемников"),
    PRE_CALCULATION_CHECK(16, "Расчетные проверки"),
    GET_HEADERS(17, "Получение заголовка таблицы"),
    REFRESH(18, "Обновить"),
    CREATE_SPECIFIC_REPORT(19, "Сформировать специфичный отчет"),
	PREPARE_SPECIFIC_REPORT(15, "Подготовить данные для спец. отчета"),
    GET_SPECIFIC_REPORT_TYPES(20, "Получить список специфичных отчетов"),
    CALCULATE_TASK_COMPLEXITY(21, "Рассчитать сложность задачи формирования специфичных отчетов"),
    CHECK_VISIBILITY_PDF(22, "Проверка видимости формы предварительного просмотра для налоговой формы"),
    AFTER_CALCULATE(25, "После рассчитать"),

    CREATE_FORMS(23, "Создание экземпляров форм"), // Вызывается не для экземпляра формы
    CREATE_REPORTS(24, "Сформировать отчетность"), // Вызывается не для экземпляра формы

    CHANGE_STATUS_ED(26, "Изменить состояние ЭД"),

    CREATE_EXCEL_REPORT(27, "Сформировать Excel отчет"),
	CREATE_PDF_REPORT(28, "Сформировать PDF отчет"),

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
    AFTER_LOAD(302, "Загрузка"),	/* Вызывается после того как форма была получена из БД.
									Если изменяются данные формы в справочных ячейках,
									то нужно вызвать scriptFormDataHolder.setDataChanged(true) */

	IMPORT_TRANSPORT_FILE(401, "Импорт из транспортных файлов"),
    UPLOAD_TRANSPORT_FILE(402, "Загрузка транспортных файлов в каталог загрузки"),

    LOGIN(501, "Вход пользователя в Систему"),
    LOGOUT(502, "Выход пользователя из Системы"),
    EXTERNAL_INTERACTION(503, "Взаимодействие с внешней АС"),
	SUNR_USER_ACTION(504, "Действия пользователя в ФП СУНР"),

	LOG_SYSTEM_BACKUP(601, "Архивация журнала аудита"),

    SEND_EMAIL(650, "Отправка email"),

    TEMPLATE_CREATED(701, "Версия макета создана"),
    TEMPLATE_MODIFIED(702, "Версия макета изменена"),
    TEMPLATE_ACTIVATED(703, "Версия макета введена в действие"),
    TEMPLATE_DEACTIVATED(704, "Версия макета выведена из действия"),
    TEMPLATE_DELETED(705, "Версия макета удалена"),

    ADD_DEPARTMENT(901, "Создание подразделения"),
    UPDATE_DEPARTMENT(902, "Модификация подразделения"),
    DELETE_DEPARTMENT(903, "Удаление подразделения"),

    SCRIPTS_IMPORT(904, "Импорт скриптов"),
    CHECK_SCRIPT(905, "Проверка скриптов"),

    EDIT_CONFIG_PARAMS(951, "Редактирование конфигурационного параметра"),

	DELETE_LOCK(960, "Удаление блокировки");

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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FormDataEvent{");
		sb.append(name());
		sb.append('}');
		return sb.toString();
	}
}
