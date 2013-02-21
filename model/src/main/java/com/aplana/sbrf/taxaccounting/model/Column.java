package com.aplana.sbrf.taxaccounting.model;

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
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String name;
	private String alias;
	private int width;
	private boolean checking;
	private int order;
	private String groupName;
	
	/**
	 * Идентификатор столбца в БД
	 * Если значение == null, то считается, что столбец новый и при его сохранении будет сгенерирован новый идентификатор
	 * @return идентификатор столбца
	 */
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
	 * Возвращает алиас столбца. Алиас - это строковый псевдоним, который используется для доступа
	 * к данным столбца из скриптов.
	 * @return алиас столбца
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Задать алиас столбца
	 * @param alias жедаемое значение алиаса
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
	 * Возвращает имя группы столбцов, к которой относится данный столбец
	 * Если в списке столбцов у нескольких столбцов стоящих рядом задано одинаковое значение названия группы столбцов, то эти
	 * столбцы будут объекдинены в таблице под общей шапкой верхнего уровня. 
	 * @return имя группы столбцов
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Задать имя группы столбцов, к которой относится данный столбец
	 * @param groupName имя группы столбцов
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
}
