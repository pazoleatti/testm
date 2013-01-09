package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Подразделение банка
 * 
 * К подразделениям банка привязываются {@link TAUser сотрудники банка}, а также для каждого подразделения задаётся
 * список видов налоговых форм, с которыми разрешена работа в данному подразделении
 * 
 * @author dsultanbekov
 */
public class Department implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Идентификатор подразделения "Банк", представляющего весь СБРФ
	 */
	public static final int ROOT_BANK_ID = 1;
	
	private int id;
	private String name;
	private Integer parentId;
	private DepartmentType type;
	private Set<Integer> formTypeIds;

	/**
	 * Получить идентификатор подразделения
	 * @return идентификатор подразделения
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Задать идентификатор подразделения
	 * @param id идентификатор подразделения
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Получить наименование подразделения
	 * @return наименование подразделения
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать наименование подразделения
	 * @param name наименование подразделения
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Получить идентификатор родительского подразделения
	 * @return идентификатор родительского подразделения, или null, если подразделение не имеет родителя (верхний уровень)
	 */
	public Integer getParentId() {
		return parentId;
	}
	
	/**
	 * Задать идентификатор родительского подразделения
	 * @param parentId идентификатор родительского подразделения (может быть null)
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	/**
	 * Получить тип подразделения
	 * @return тип подразделения
	 */
	public DepartmentType getType() {
		return type;
	}
	
	/**
	 * Задать тип подразделения
	 * @param type тип подразделения
	 */
	public void setType(DepartmentType type) {
		this.type = type;
	}

	/**
	 * Получить набор идентификаторов {@link FormType видов налогогвых форм}, с которыми разрешена работа в данном подразделении
	 * @return набор идентификаторов {@link FormType видов налогогвых форм}, с которыми разрешена работа в данном подразделении
	 */
	public Set<Integer> getFormTypeIds() {
		return formTypeIds;
	}

	/**
	 * Задать набор идентификаторов {@link FormType видов налогогвых форм}, с которыми разрешена работа в данном подразделении
	 * @param formTypes набор идентификаторов {@link FormType видов налогогвых форм}, с которыми разрешена работа в данном подразделении
	 */
	public void setFormTypeIds(Set<Integer> formTypes) {
		this.formTypeIds = formTypes;
	}
}
