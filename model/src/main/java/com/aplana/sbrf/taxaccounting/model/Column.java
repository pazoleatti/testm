package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;


/**
 * Столбец таблицы в объявлении налоговой формы
 * В объявлении столбца задаются параметры отображения, тип данных и другие свойства, которые необходимы при
 * построении интерфейса налоговой формы.
 * 
 * Даный класс является абстрактным и содержит параметры, общие для всех столбцов. Для каждого типа данных, которые могут 
 * встречаться в налоговых формах создаётся класс-наследник, в котором могут быть добавлены дополнительные свойства, специфичные для
 * данного типа данных.
 * 
 * @author dsultanbekov
 */
public abstract class Column implements Ordered, Serializable {
	public interface ValidationStrategy {
		boolean matches(String valueToCheck);
	}

	private static final long serialVersionUID = 1L;

	private Integer id;
	protected ColumnType columnType;
	private String name;
	private String shortName;
	private String alias;
	private int width;
	private boolean checking;
	private int order;

	@JsonIgnore
    transient private ColumnFormatter formatter = new ColumnFormatter();
	/**
	 * Идентификатор столбца в БД
	 * Если значение == null, то считается, что столбец новый и при его сохранении будет сгенерирован новый идентификатор
	 * @return идентификатор столбца
	 */
    @XmlTransient
	public Integer getId() {
		return id;
	}
	
	/**
	 * Задать значение идентификатора столбца.
	 * У новых столбцов нужно задавать id = null
	 * @param id значение идентификатора, для новых столбцов задавать не нужно, т.к. по умолчанию null.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Возвращает наименование столбца (заголовок)
	 * @return наименование столбца
	 */
	public String getName() {
		return name;
	}

	/**
	 * Задаёт наименование столбца
	 * @param name желаемое значение наименования столбца
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Возвращает краткое наименование столбца (заголовок)
     * @return краткое наименование столбца
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Задаёт краткое наименование столбца
     * @param shortName желаемое значение краткого наименования столбца
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
	
	/**
	 * Возвращает алиас столбца. Алиас - это строковый псевдоним, который используется для доступа
	 * к данным столбца из скриптов.
	 * @return алиас столбца
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Задать алиас столбца
	 * @param alias желаемое значение алиаса
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	/**
	 * Получить ширину столбца. Это значение используется при построении визуального представления таблицы налоговой формы
	 * @return ширина столбца в символах
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Задать ширину столбца
	 * @param width желаемое значение ширины столбца в символах.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Возвращает порядковый номер столбца в форме
	 * @return порядковый номер столбца
	 */
	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * Задать порядковый номер столбца
	 * @param order желаемое значение номера столбца
	 */
	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Возвращает признак проверочного столбца
	 * @return true - столбец является проверочным, false - иначе
	 */
	public boolean isChecking() {
		return checking;
	}

	/**
	 * Задать признак проверочного столбца
	 * @param checking значение признака проверочного столбца 
	 */
	public void setChecking(boolean checking) {
		this.checking = checking;
	}

	/**
	 * Возвращает объект, который форматирует значения в ячейках
	 * @return
	 */
	public ColumnFormatter getFormatter() {
        return formatter;
	}

	public ValidationStrategy getValidationStrategy() {
		return new ValidationStrategy() {
			@Override
			public boolean matches(String valueToCheck) {
				return true;
			}
		};
	}

	public ColumnType getColumnType() {
		return columnType;
	}

	public void setColumnType(ColumnType columnType) {
		this.columnType = columnType;
	}
}
