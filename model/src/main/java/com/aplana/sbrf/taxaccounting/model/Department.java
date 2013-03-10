package com.aplana.sbrf.taxaccounting.model;

import java.io.*;
import java.util.*;

/**
 * Подразделение банка
 * 
 * К подразделениям банка привязываются {@link TAUser сотрудники банка}, а также
 * для каждого подразделения задаётся список видов налоговых форм, с которыми
 * разрешена работа в данному подразделении
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
	private String shortName;
	private Integer parentId;
	private DepartmentType type;
	private Integer dictRegionId;
	private String tbIndex;
	private String sbrfCode;
	private List<DepartmentFormType> departmentFormTypes;
	private List<DepartmentDeclarationType> departmentDeclarationTypes;

	/**
	 * Получить идентификатор подразделения
	 * 
	 * @return идентификатор подразделения
	 */
	public int getId() {
		return id;
	}

	/**
	 * Задать идентификатор подразделения
	 * 
	 * @param id
	 *            идентификатор подразделения
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Получить наименование подразделения
	 * 
	 * @return наименование подразделения
	 */
	public String getName() {
		return name;
	}

	/**
	 * Задать наименование подразделения
	 * 
	 * @param name
	 *            наименование подразделения
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Получить идентификатор родительского подразделения
	 * 
	 * @return идентификатор родительского подразделения, или null, если
	 *         подразделение не имеет родителя (верхний уровень)
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * Задать идентификатор родительского подразделения
	 * 
	 * @param parentId
	 *            идентификатор родительского подразделения (может быть null)
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	/**
	 * Получить тип подразделения
	 * 
	 * @return тип подразделения
	 */
	public DepartmentType getType() {
		return type;
	}

	/**
	 * Задать тип подразделения
	 * 
	 * @param type
	 *            тип подразделения
	 */
	public void setType(DepartmentType type) {
		this.type = type;
	}

	/**
	 * Получить список назначений департамента с налоговыми формами
	 * 
	 * @return список назначений НФ
	 */
	public List<DepartmentFormType> getDepartmentFormTypes() {
		return departmentFormTypes;
	}

	/**
	 * Установить список назначений департамента с налоговыми формами
	 */
	public void setDepartmentFormTypes(
			List<DepartmentFormType> departmentFormTypes) {
		this.departmentFormTypes = departmentFormTypes;
	}

	/**
	 * Получить список назначений департамента с декларациями формами
	 * 
	 * @return список назначений Деклараций
	 */
	public List<DepartmentDeclarationType> getDepartmentDeclarationTypes() {
		return departmentDeclarationTypes;
	}

	/**
	 * Установить список назначений департамента с декларациями формами
	 */
	public void setDepartmentDeclarationTypes(
			List<DepartmentDeclarationType> departmentDeclarationTypes) {
		this.departmentDeclarationTypes = departmentDeclarationTypes;
	}

	/**
	 *  Получить код субъекта РФ
	 */
	public Integer getDictRegionId() {
		return dictRegionId;
	}

	/**
	 *  Установить код субъекта РФ
	 */
	public void setDictRegionId(Integer dictRegionId) {
		this.dictRegionId = dictRegionId;
	}

	/**
	 * Получить сокращенное наименование подразделения
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Установить сокращенное наименование подразделения
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * Получить индекс территориального банка
	 */
	public String getTbIndex() {
		return tbIndex;
	}

	/**
	 * Установить индекс территориального банка
	 */
	public void setTbIndex(String tbIndex) {
		this.tbIndex = tbIndex;
	}

	/**
	 * Получить код подразделения в нотации Сбербанка
	 */
	public String getSbrfCode() {
		return sbrfCode;
	}

	/**
	 * Установить код подразделения в нотации Сбербанка
	 */
	public void setSbrfCode(String sbrfCode) {
		this.sbrfCode = sbrfCode;
	}
}
