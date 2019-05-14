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
	CREATE(1, "Создание"),
	DELETE(2, "Удаление"),
	CALCULATE(3, "Рассчитать"),
	CHECK(5, "Проверка"),
	SAVE(6, "Обновление"),
	IMPORT(7, "Импорт данных"),
	GET_LEVEL0(8, "Получение данных"),
	GET_LEVEL1(9, "Получение защищенных данных"),
	DATA_MODIFYING(10, "Изменение данных"),
	UPDATE_PERSONS_DATA(11, "Обновление данных ФЛ"),
    AFTER_CREATE(12, "После создания"),
    GET_SOURCES (14, "Получение источников-приемников"),
	PREPARE_SPECIFIC_REPORT(15, "Подготовить данные для спец. отчета"),
	PRE_CALCULATION_CHECK(16, "Расчетные проверки"),
	PRE_CREATE_REPORTS(18, "Проверки перед формировнием отчетности"), // Вызывается не для экземпляра формы
	CREATE_SPECIFIC_REPORT(19, "Сформировать специфичный отчет"),
    CALCULATE_TASK_COMPLEXITY(21, "Рассчитать сложность задачи формирования специфичных отчетов"),
    CHECK_VISIBILITY_PDF(22, "Проверка видимости формы предварительного просмотра для налоговой формы"),


    CREATE_FORMS(23, "Создание экземпляров форм"), // Вызывается не для экземпляра формы

    CREATE_REPORTS(24, "Сформировать отчетность"), // Вызывается не для экземпляра формы
	AFTER_CALCULATE(25, "После рассчитать"),

    CHANGE_STATUS_ED(26, "Изменение состояния ЭД"),

    CREATE_EXCEL_REPORT(27, "Сформировать Excel отчет"),
	CREATE_PDF_REPORT(28, "Сформировать PDF отчет"),
	EXPORT_DECLARATION_DATA_TO_EXCEL(29, "Выгрузить данные формы вида РНУ НДФЛ"),
	BUILD_DOC(30, "Формирование документа"),
	CREATE_APPLICATION_2(31, "Создание Приложения 2 к декларации НП"),

	MOVE_CREATED_TO_ACCEPTED(105, "Принять из \"Создана\""),
	MOVE_ACCEPTED_TO_CREATED(106, "Возврат в Создана"),
	MOVE_CREATED_TO_PREPARED(107, "Проверка"),
	MOVE_PREPARED_TO_CREATED(108,  "Вернуть из \"Подготовлена\" в \"Создана\""),
	ACCEPT(109,  "Принятие"),

	ADD_ROW(301, "Добавить строку"),

	IMPORT_TRANSPORT_FILE(401, "Импорт из файла Excel"),
    UPLOAD_TRANSPORT_FILE(402, "Загрузка транспортных файлов в каталог загрузки"),

    LOGIN(501, "Вход пользователя в Систему"),
    LOGOUT(502, "Выход пользователя из Системы"),
    EXTERNAL_INTERACTION(503, "Взаимодействие с внешней АС"),

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

	DELETE_LOCK(960, "Удаление блокировки"),

	CREATE_PERSON(10000, "Создание ФЛ"),
	UPDATE_PERSON(10001, "Изменение данных ФЛ"),
	ATTACH_RESPONSE_FILE(10002, "Загрузка файла ответа ФНС"),
	IDENTIFY(10003, "Идентификация ФЛ"),
	CONSOLIDATE(10004, "Консолидация"),
	CREATE_FROM_XML(10005, "Создание из ТФ xml"),
	NDFL_EDIT(10006, "Редактирование строки РНУ НДФЛ"),
	NDFL_DATES_EDIT(10007, "Массовое редактирование дат"),
	SEND_EDO(10008, "Отправка в ЭДО"),
	EXPORT_REPORT_FROMS(10009, "Выгрузка для отправки в ФНС");

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
