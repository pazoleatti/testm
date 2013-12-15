package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 * 
 * @author dsultanbekov
 */
public class FormTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = -8304772615983231523L;
	
	private FormType type;
	private String version;
	private boolean active;
	private Integer edition;
	private boolean numberedColumns;
    private boolean fixedRows;
    
    private String name;
    private String fullName;
    private String code;
    
    /**
     * Тело скрипта
     */
    private String script;

    /**
     * @return имя отображаемое на форме
     */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return полное наименование необходимое для печатной формы.
	 */
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return идентификатор формы и номер формы
	 */
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	private List<DataRow<Cell>> rows = new ArrayList<DataRow<Cell>>();
	private List<DataRow<HeaderCell>> headers = new ArrayList<DataRow<HeaderCell>>();
	
	private List<Column> columns = new ArrayList<Column>();
	private List<FormStyle> styles = new ArrayList<FormStyle>();

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
	 * @param columnAlias - алиас колонки
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
	 * Признак активности шаблона формы
	 * @return true - шаблон активен, false - шаблон неактивен
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Задать признак активности формы
	 * @param active true - шаблон активен, false - шаблон неактивен
	 */
	public void setActive(boolean active) {
		this.active = active;
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
	 * @param edition - номер редакции
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



	public List<DataRow<Cell>> getRows() {
		return rows;
	}
	
	public List<DataRow<HeaderCell>> getHeaders() {
		return headers;
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

    private HeaderCell createHeaderCell(Column column) {
        HeaderCell cell = new HeaderCell(column);
        if (column.getWidth() > 0) {
            cell.setValue(column.getName());
        } else {
            cell.setValue("");
        }
        return cell;
    }

	/**
	 * Добавляет колонку в шаблон формы.
	 * @param position позиция для вставки
	 * @param column добавляемая колонка
	 */
	public void addColumn(int position, Column column) {
		columns.add(position, column);
		for (DataRow<Cell> row : rows) {
			row.addColumn(position, new Cell(column, styles));
		}
		for (DataRow<HeaderCell> row : headers) {
			row.addColumn(position,  createHeaderCell(column));
		}
	}

	/**
	 * Добавляет колонку в шаблон формы.
	 * @param column добавляемая колонка
	 */
	public void addColumn(Column column) {
		columns.add(column);
		for (DataRow<Cell> row : rows) {
			row.addColumn(new Cell(column, styles));
		}
		for (DataRow<HeaderCell> row : headers) {
			row.addColumn(createHeaderCell(column));
		}
	}

	/**
	 * Удаляет колонку из шаблона формы.
	 * @param column удаляемая колонка
	 */
	public void removeColumn(Column column) {
		for (DataRow<Cell> row : rows) {
			row.removeColumn(column);
		}
		for (DataRow<HeaderCell> row : headers) {
			row.removeColumn(column);
		}
		columns.remove(column);
	}

	/**
	 * Получить определение стиля по его алиасу
	 * 
	 * @param alias
	 *            алиас стиля
	 * @return объект, описывающий стиль {@link FormStyle}, заданный алиасом
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
