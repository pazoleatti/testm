package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Модельный класс, содержащий информацию о значениях, которые допустимы
 * в фильтре по декларациям
 * 
 * Необходимость обусловлена тем, что перечень эти значений зависит от 
 * 1) роли пользователя
 * 2) какие декларации назначены подразделению пользователя,
 * @author dsultanbekov
 */
public class DeclarationFilterAvailableValues implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<Integer> departmentIds;
	private List<DeclarationType> declarationTypes;
	
	/**
	 * Получить набор подразделений
	 * @return набор идентификаторов подразделений
	 */
	public Set<Integer> getDepartmentIds() {
		return departmentIds;
	}
	
	/**
	 * Задать набор подразделений
	 * @param departmentIds набор идентификаторов подразделений
	 */
	public void setDepartmentIds(Set<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}
	
	/**
	 * Получить список видов деклараций
	 * @return виды деклараций
	 */
	public List<DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}
	
	/**
	 * Задать набор видов деклараций
	 * @param formTypeIds набор видов деклараций
	 */
	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}
}
