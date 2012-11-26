package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.*;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 * @author dsultanbekov
 */
public class FormTemplate implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private FormType type;
	private String version;

	private List<DataRow> rows = new ArrayList<DataRow>();
	private List<Column> columns = new ArrayList<Column>();

	private List<Script> scripts = new ArrayList<Script>();
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
	 * @param columnId идентификатор столбца
	 * @return определение столбца
	 * @throws IllegalArgumentException если в определении формы отсутствует столбец с указанным <code>id</code>
	 */
	public Column getColumn(int columnId) {
		for (Column col: columns) {
			if (col.getId() == columnId) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnId: " + columnId);
	}
	
	/**
	 * Получить определение столбца налоговой формы по алиасу
	 * @param columnAlias
	 * @return определение столбца
	 * @throws NullPointerException если <code>alias == null</code>
	 * @throws IllegalArgumentException если указан алиас, отсутствующий в определении формы
	 */
	public Column getColumn(String columnAlias) {
		if (columnAlias == null) {
			throw new NullPointerException("Column alias cannot be null");
		}
		for (Column col: columns) {
			if (columnAlias.equals(col.getAlias())) {
				return col;
			}
		}
		throw new IllegalArgumentException("Wrong columnAlias: " + columnAlias);
	}

	/**
	 * Получить версию формы: для каждого типа формы может существовать несколько версий
	 * @return версия формы
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Установить версию для формы
	 * @param version номер версии
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Don't use for data manipulation.
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

	public Map<FormDataEvent, List<Script>> getEventScripts() {
		return eventScripts;
	}

	/**
	 * Remove all scripts from form.
	 */
	public void clearScripts() {
		scripts.clear();
		if(eventScripts!=null){
			eventScripts.clear();
		}
	}

	/**
	 * Add script to event in order.
	 *
	 * @param event form event
	 * @param script form script
	 */
	public void addEventScript(FormDataEvent event, Script script) {
		if(!scripts.contains(script)){
			throw new IllegalArgumentException("Form doesn't contain script.");
		}

		List<Script> scriptList = eventScripts.get(event);
		if(scriptList==null){
			scriptList = new ArrayList<Script>();
			eventScripts.put(event, scriptList);
		}

		if(!scriptList.contains(script)){
			scriptList.add(script);
		}
	}

	/**
	 * Adds script to from, but doesn't create join with event.
	 *
	 * @param script script
	 */
	public void addScript(Script script){
		scripts.add(script);
	}

	/**
	 * Remove script from form.
	 *
	 * @param script script
	 */
	public void removeScript(Script script){
		// TODO: бросать исключение если скрипта нет
		scripts.remove(script);
		for(Map.Entry<FormDataEvent, List<Script>> entry: eventScripts.entrySet()){
			entry.getValue().remove(script);
		}
	}

	/**
	 *
	 * @param script script
	 * @return index of script in main form script list.
	 * TODO: может перетащить в DAO?
	 */
	public int indexOfScript(Script script){
		return scripts.indexOf(script);
	}

	/**
	 * @param event form event
	 * @return list of scripts joined with the event
	 */
	public List<Script> getScriptsByEvent(FormDataEvent event){
		if(eventScripts.containsKey(event)){
			return eventScripts.get(event);
		}else{
			return new ArrayList<Script>(0);
		}
	}
}
