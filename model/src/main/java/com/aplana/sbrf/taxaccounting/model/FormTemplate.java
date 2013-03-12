package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 * 
 * @author dsultanbekov
 */
public class FormTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = -8304772615983231523L;
	
	private FormType type;
	private String version;
	private Integer edition;
	private boolean numberedColumns;
    private boolean fixedRows;	

	private List<DataRow> rows = new ArrayList<DataRow>();
	private List<Column> columns = new ArrayList<Column>();
	private List<FormStyle> styles = new ArrayList<FormStyle>();

	/**
	 * Все скрипты формы.
	 */
	private List<Script> scripts = new ArrayList<Script>();

	/**
	 * Маппинг скриптов формы на события. Порядок выполнения гарантируется
	 * <code>java.util.List</code>.
	 */
	private Map<FormDataEvent, List<Script>> eventScripts = new HashMap<FormDataEvent, List<Script>>();

	/**
	 * Возвращает список {@link Column столбцов}, образующих налоговую форму.
	 * Порядок столбцов в коллекции соответствует тому порядку, в котором они
	 * должны выводиться в налоговой форме. Коллекция создаётся в момент
	 * создания объекта FormTemplate и не может быть изменена в ходе жизни
	 * объекта. Если требуется внести изменения в список элементов необходимо
	 * добавлять и удалять элементы из существующего экземпляра списка.
	 * 
	 * @return список столбцов, образующих налоговую форму.
	 */
	public List<Column> getColumns() {
		return columns;
	}
	
	/**
	 * Возвращает коллекцию {@link FormStyle стилей формы} Коллекция создаётся в
	 * момент создания объекта FormTemplate и не может быть изменена в ходе
	 * жизни объекта. Если требуется внести изменения в список элементов
	 * необходимо добавлять и удалять элементы из существующего экземпляра
	 * 
	 * @return список стилей, определённых в налоговой форме
	 */
	public List<FormStyle> getStyles() {
		return styles;
	}

	/**
	 * Задаёт {@link FormType вид налоговой формы}
	 * 
	 * @param type
	 *            вид налоговой формы
	 */
	public void setType(FormType type) {
		this.type = type;
	}

	/**
	 * Возвращает {@link FormType вид налоговой формы}
	 * 
	 * @return вид налоговой формы
	 */
	public FormType getType() {
		return type;
	}

	/**
	 * Получить определение столбца по числовому идентификатору
	 * 
	 * @param columnId
	 *            идентификатор столбца
	 * @return определение столбца
	 * @throws IllegalArgumentException
	 *             если в определении формы отсутствует столбец с указанным
	 *             <code>id</code>
	 */
	public Column getColumn(Integer columnId) {
		if (columnId == null) {
			throw new IllegalArgumentException(
					"Argument columnId can't be null.");
		}

		for (Column col : columns) {
			if (columnId.equals(col.getId())) {
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
	 * @throws NullPointerException
	 *             если <code>alias == null</code>
	 * @throws IllegalArgumentException
	 *             если указан алиас, отсутствующий в определении формы
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
	 * Получить версию формы: для каждого типа формы может существовать
	 * несколько версий
	 * 
	 * @return версия формы
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Возвращает номер редакции данной записи (используется для предотвращения
	 * одновременного редактирования записи несколькими пользователями)
	 * 
	 * @return номер редакции
	 */
	public int getEdition() {
		return edition;
	}

	/**
	 * Задать номер редакции Этот номер используется для предотвращения
	 * одновременного редактирования записи несколькими пользователями. Если при
	 * попытке сохранения FormTemplate обнаружится, что значение поля edition в
	 * БД отличается от того, которое записано в модельном классе, то будет
	 * сгенерировано исключение. Задать значение поля можно только один раз для
	 * каждого экземпляра FormTemplate, поэтому после каждого сохранения объекта
	 * в БД, использовать этот объект далее нельзя, нужно перечитать объект из
	 * БД и создать новый экзепляр.
	 * 
	 * @param edition
	 */
	public void setEdition(int edition) {
		if (this.edition != null) {
			throw new IllegalStateException(
					"Edition property already initialized");
		} else {
			this.edition = edition;
		}

	}

	/**
	 * Установить версию для формы
	 * 
	 * @param version
	 *            номер версии
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

	/**
	 * Возвращает признак того, что столбцы налоговой формы должны быть
	 * пронумерованы Если значание установлено в true, то в заголовке налоговой
	 * формы появляется новая строка (внизу заголовка), в которой каждому
	 * столбцу присваивается номер, начиная с 1. Нумерация соответствует порядку
	 * столбцов в форме и отдельно нигде не хранится.
	 * 
	 * @return если возвращает true, то нумеровать столбцы нужно, если false, то
	 *         не нужно
	 */
	public boolean isNumberedColumns() {
		return numberedColumns;
	}

	/**
	 * Устанавливает признак того, что столбцы налоговой формы должны быть
	 * пронумерованы
	 * 
	 * @param numberedColumns
	 *            true - нумеровать столбцы нужно, false - не нужно
	 */
	public void setNumberedColumns(boolean numberedColumns) {
		this.numberedColumns = numberedColumns;
	}

	/**
	 * Возвращает маппинг скриптов на события формы. в виде отображения события
	 * на списки скриптов. Порядок в списке скриптов соответствует порядку
	 * выполнения скриптов для каждого конкретного события.
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
	 * Добавляет скрипт для события формы. Добавляемый скрипт ранее должен быть
	 * привязан к шаблону формы.
	 * 
	 * @see #addScript(Script)
	 * @param event
	 *            событие формы
	 * @param script
	 *            скрипт
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
	 * Удаляет привязку скрипта к событию формы. Отвязка скрипта от шаблона
	 * формы не происходит.
	 * 
	 * @param event
	 *            событие формы
	 * @param script
	 *            скрипт
	 */
	public void removeEventScript(FormDataEvent event, Script script) {
		if (!scripts.contains(script)) {
			throw new IllegalArgumentException("Form doesn't contain script.");
		}

		List<Script> scriptList = eventScripts.get(event);
		if (scriptList == null || !scriptList.contains(script)) {
			throw new IllegalArgumentException(
					"Form event doesn't contain script.");
		}

		scriptList.remove(script);
	}

	/**
	 * Удаляет колонку из шаблона формы.
	 * @param column удаляемая колонка
	 */
	public void addColumn(Column column) {
		columns.add(column);
		for (DataRow row : rows) {
			row.addColumn(column);
		}
	}

	/**
	 * Добавляет скрипт к шаблону формы. При это не происходит привязки скрипта
	 * на событие.
	 * 
	 * @param script
	 *            скрипт
	 */
	public void addScript(Script script) {
		scripts.add(script);
	}

	/**
	 * Удаляет скрипт из шаблона формы. Удаляются так же все привязки скрипта к
	 * событиям формы.
	 * 
	 * @param script
	 *            скрипт
	 */
	public void removeScript(Script script) {
		if (!scripts.contains(script)) {
			throw new IllegalArgumentException(
					"Form template doesn't contain the script.");
		}

		scripts.remove(script);
		for (Map.Entry<FormDataEvent, List<Script>> entry : eventScripts
				.entrySet()) {
			entry.getValue().remove(script);
		}
	}

	/**
	 * Добавляет колонку в шаблон формы.
	 * @param column добавляемая колонка
	 */
	public void removeColumn(Column column) {
		for (DataRow row : rows) {
			row.removeColumn(column);
		}
		columns.remove(column);
	}

	/**
	 * @param script
	 *            script
	 * @return индекс скрипта в списке скриптов TODO: нужно перетащить в DAO?
	 */
	public int indexOfScript(Script script) {
		return scripts.indexOf(script);
	}

	/**
	 * @param event
	 *            событие формы
	 * @return список скриптов для определенного события формы.
	 */
	public List<Script> getScriptsByEvent(FormDataEvent event) {
		if (eventScripts.containsKey(event)) {
			return eventScripts.get(event);
		} else {
			return new ArrayList<Script>(0);
		}
	}

	/**
	 * Получить определение стиля по его алиасу
	 * 
	 * @param alias
	 *            алиас стиля
	 * @return объект, описывающий {@link стиль FormStyle}, заданный алиасом
	 * @throws NullPointerException
	 *             если в качеcтве аргумента передан null
	 * @throws IllegalArgumentException
	 *             если
	 */
	public FormStyle getStyle(String alias) {
		if (alias == null) {
			throw new NullPointerException("Style alias cannot be null");
		}
		for (FormStyle style : styles) {
			if (alias.equals(style.getAlias())) {
				return style;
			}
		}
		throw new IllegalArgumentException("Wrong style alias: '" + alias
				+ '\'');
	}
	
    /**
     * Определить работаем-ли мы с фиксированным набором строк или нет
     * @return true - пользователь работает с предопределённым набором строк. false -  пользователь может
     * добавлять и удалять строки в/из налоговой формы
     */
    public boolean isFixedRows() {
        return fixedRows;
    }

    /**
     * Задать признак того, что в налоговой форме используется фиксированный набор строк
     * @param fixedRows признак того, что в налоговой форме используется фиксированный набор строк
     */
    public void setFixedRows(boolean fixedRows) {
        this.fixedRows = fixedRows;
    }
}
