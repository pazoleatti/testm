package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 * 
 * @author dsultanbekov
 */
public class FormTemplate extends IdentityObject<Integer> implements Cloneable {
	private static final long serialVersionUID = -8304772615983231523L;

	private FormType type;
	private Date version;
    private boolean fixedRows;

    private String name;
    private String fullName;
    private String header;
    private boolean monthly;
    /** Признак использования периода сравнения (false - не используется, true - используется) */
    private boolean comparative;
    /** Признак расчета нарастающим итогом (false - не используется, true - используется)*/
    private boolean accruing;
    /** Отображать кнопку "Обновить" */
    private boolean updating;

    private VersionedObjectStatus status;

    /**
     * Признак статуса шаблона
     * @return статус шаблона
     */
    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
     * Тело скрипта
     * Оставлен для сохранения шаблона налоговой формы.
     * Заполненяться поле должно отдельным вызовом функции dao.
     */
    private String script;

    /**
     * @return имя отображаемое на форме
     */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = StringUtils.cleanString(name);
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
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		if (script == null || script.trim().isEmpty()) {
			this.script = null;
		} else {
			this.script = script;
		}
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
			throw new IllegalArgumentException("Column alias cannot be null");
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
	public Date getVersion() {
		return version;
	}

	/**
	 * Установить дату актуальности шаблона
	 *
	 * @param version
	 *            дата актуальности
	 */
	public void setVersion(Date version) {
		this.version = version;
	}

	/**
	 * Фиксированные строки
	 * @return
	 */
	public List<DataRow<Cell>> getRows() {
		return rows;
	}

	public List<DataRow<HeaderCell>> getHeaders() {
		return headers;
	}

    private HeaderCell createHeaderCell(Column column) {
        HeaderCell cell = new HeaderCell(column);
        if (column.getWidth() > 0) {
            cell.setValue(column.getName(), null);
        } else {
            cell.setValue("", null);
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
			row.addColumn(position, new Cell(column, getStyles()));
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
			row.addColumn(new Cell(column, getStyles()));
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
	@Deprecated // планируется удалить стили из макетов
	public FormStyle getStyle(String alias) {
		if (alias == null) {
			throw new IllegalArgumentException("Style alias cannot be null");
		}
		for (FormStyle style : styles) {
			if (alias.equals(style.getAlias())) {
				return style;
			}
		}
		throw new IllegalArgumentException("Wrong style alias: '" + alias + '\'');
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

    /**
     * Определяет признак ежемесячности налоговой формы
     * @return
     */
    public boolean isMonthly() {
        return monthly;
    }

    public void setMonthly(boolean monthly) {
        this.monthly = monthly;
    }

    public boolean isComparative() {
        return comparative;
    }

    public void setComparative(boolean comparative) {
        this.comparative = comparative;
    }

    public boolean isAccruing() {
        return accruing;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public FormTemplate clone() {
        FormTemplate formTemplateClone = new FormTemplate();

        formTemplateClone.setId(this.getId());
        formTemplateClone.setType(this.getType());
        formTemplateClone.setVersion(this.getVersion());
        formTemplateClone.setFixedRows(this.isFixedRows());
        formTemplateClone.setName(this.getName());
        formTemplateClone.setFullName(this.getFullName());
        formTemplateClone.setHeader(this.getHeader());
        formTemplateClone.setMonthly(this.isMonthly());
        formTemplateClone.setScript(this.getScript());
        formTemplateClone.getColumns().addAll(this.getColumns());
        formTemplateClone.getStyles().addAll(this.getStyles());
        formTemplateClone.getHeaders().addAll(this.getHeaders());
        formTemplateClone.setStatus(this.getStatus());
        formTemplateClone.setComparative(this.isComparative());
        formTemplateClone.setAccruing(this.isAccruing());
        formTemplateClone.setUpdating(this.isUpdating());

        // клонировать строки, иначе измения в них попадут в макет формы
        List<DataRow<Cell>> rows = getCloneRows(this.getRows());
        formTemplateClone.getRows().addAll(rows);

        return formTemplateClone;
    }

	public List<Column> cloneColumns() {
		List<Column> clone = new ArrayList<Column>();
		clone.addAll(this.getColumns());
		return clone;
	}



    /** Получить копию строки. */
    private List<DataRow<Cell>> getCloneRows(List<DataRow<Cell>> dataRows) {
        // клонировать список
        List<DataRow<Cell>> clone = new ArrayList<DataRow<Cell>>(dataRows.size());
        List<Cell> cells = new ArrayList<Cell>();
        List<FormStyle> formStyleList = new ArrayList<FormStyle>();
        for (DataRow<Cell> row : dataRows) {
            for (String key : row.keySet()) {
                if (row.getCell(key).getStyle() != null) {
                    formStyleList.add(row.getCell(key).getStyle());
                }
            }
        }
        // сделать копии строк
        for (DataRow<Cell> row : dataRows) {
            cells.clear();
            for (String key : row.keySet()) {
                Cell cell = new Cell(row.getCell(key).getColumn(), getStyles());
                cells.add(cell);
            }
            DataRow<Cell> newRow = new DataRow<Cell>(row.getAlias(), cells);

			Integer index = row.getIndex();
            newRow.setAlias(row.getAlias() != null && !"".equals(row.getAlias()) ? row.getAlias() : null);
            newRow.setIndex(index);
            newRow.setId(row.getId());
            newRow.setImportIndex(row.getImportIndex());
            for (String alias : row.keySet()) {
                Cell newCell = newRow.getCell(alias);
                Cell cell = row.getCell(alias);

                newCell.setValue(cell.getValue(), index);
                newCell.setEditable(cell.isEditable());
                newCell.setColSpan(cell.getColSpan());
                newCell.setRowSpan(cell.getRowSpan());
                newCell.setStyle(cell.getStyle());
            }
            clone.add(newRow);
        }
        return clone;
    }
}