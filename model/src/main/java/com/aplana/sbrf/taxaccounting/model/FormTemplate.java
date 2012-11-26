package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.*;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 *
 * @author dsultanbekov
 */
public class FormTemplate implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	private FormType type;
	private String version;

	private List<DataRow> rows = new ArrayList<DataRow>();
	private List<Column> columns = new ArrayList<Column>();

	/**
	 * Все скрипты формы.
	 */
	private List<Script> scripts = new ArrayList<Script>();

	/**
	 * Маппинг скриптов формы на события. Порядок выполнения гарантируется <code>java.util.List</code>.
	 */
	private Map<FormDataEvent, List<Script>> eventScripts = new EnumMap<FormDataEvent, List<Script>>(FormDataEvent.class);

	private int workflowId;

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setType(FormType type) {
		this.type = type;
	}

	public FormType getType() {
		return type;
	}

	/**
	 * Получить определение столбца по числовому идентификатору
	 *
	 * @param columnId идентификатор столбца
	 * @return определение столбца
	 * @throws IllegalArgumentException если в определении формы отсутствует столбец с указанным <code>id</code>
	 */
	public Column getColumn(int columnId) {
		for (Column col : columns) {
			if (col.getId() == columnId) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnId: " + columnId);
	}

	/**
	 * Получить определение столбца налоговой формы по алиасу
	 *
	 * @param columnAlias
	 * @return определение столбца
	 * @throws NullPointerException     если <code>alias == null</code>
	 * @throws IllegalArgumentException если указан алиас, отсутствующий в определении формы
	 */
	public Column getColumn(String columnAlias) {
		if (columnAlias == null) {
			throw new NullPointerException("Column alias cannot be null");
		}
		for (Column col : columns) {
			if (columnAlias.equals(col.getAlias())) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnAlias: " + columnAlias);
	}

	/**
	 * Получить версию формы: для каждого типа формы может существовать несколько версий
	 *
	 * @return версия формы
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Установить версию для формы
	 *
	 * @param version номер версии
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Не использовать для манипуляций сриптами!!!
	 *
	 * @return список скриптов формы.
	 */
	public List<Script> getScripts() {
		return scripts;
	}

	public List<DataRow> getRows() {
		return rows;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	/**
	 * Возвращает маппинг скриптов на события формы. в виде отображения события на списки скриптов.
	 * Порядок в списке скриптов соответствует порядку выполнения скриптов для каждого конкретного события.
	 *
	 * @return маппинг скриптов на события формы.
	 */
	public Map<FormDataEvent, List<Script>> getEventScripts() {
		return eventScripts;
	}

	/**
	 * Удаляет все скрипты в шаблоне формы. Так же очищает маппинг скриптов.
	 */
	public void clearScripts() {
		scripts.clear();
		if (eventScripts != null) {
			eventScripts.clear();
		}
	}

	/**
	 * Добавляет скрипт для события формы. Добавляемый скрипт ранее должен быть привязан к шаблону формы.
	 *
	 * @see #addScript(Script)
	 * @param event  событие формы
	 * @param script скрипт
	 */
	public void addEventScript(FormDataEvent event, Script script) {
		if (!scripts.contains(script)) {
			throw new IllegalArgumentException("Form doesn't contain script.");
		}

		List<Script> scriptList = eventScripts.get(event);
		if (scriptList == null) {
			scriptList = new ArrayList<Script>();
			eventScripts.put(event, scriptList);
		}

		if (!scriptList.contains(script)) {
			scriptList.add(script);
		}
	}

	/**
	 * Удаляет привязку скрипта к событию формы. Отвязка скрипта от шаблона формы не происходит.
	 *
	 * @param event событие формы
	 * @param script скрипт
	 */
	public void removeEventScript(FormDataEvent event, Script script) {
		if (!scripts.contains(script)) {
			throw new IllegalArgumentException("Form doesn't contain script.");
		}

		List<Script> scriptList = eventScripts.get(event);
		if (scriptList == null || !scriptList.contains(script)) {
			throw new IllegalArgumentException("Form event doesn't contain script.");
		}

		scriptList.remove(script);
	}

	/**
	 * Добавляет скрипт к шаблону формы. При это не происходит привязки скрипта на событие.
	 *
	 * @param script скрипт
	 */
	public void addScript(Script script) {
		scripts.add(script);
	}

	/**
	 * Удаляет скрипт из шаблона формы. Удаляются так же все привязки скрипта к событиям формы.
	 *
	 * @param script скрипт
	 */
	public void removeScript(Script script) {
		if(!scripts.contains(script)){
			throw new IllegalArgumentException("Form template doesn't contain the script.");
		}

		scripts.remove(script);
		for (Map.Entry<FormDataEvent, List<Script>> entry : eventScripts.entrySet()) {
			entry.getValue().remove(script);
		}
	}

	/**
	 * @param script script
	 * @return индекс скрипта в списке скриптов
	 *         TODO: может перетащить в DAO?
	 */
	public int indexOfScript(Script script) {
		return scripts.indexOf(script);
	}

	/**
	 * @param event событие формы
	 * @return список скриптов для определенного события формы.
	 */
	public List<Script> getScriptsByEvent(FormDataEvent event) {
		if (eventScripts.containsKey(event)) {
			return eventScripts.get(event);
		} else {
			return new ArrayList<Script>(0);
		}
	}
}
