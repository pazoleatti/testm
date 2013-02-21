package com.aplana.sbrf.taxaccounting.model;

import java.util.List;
import java.util.Set;

/**
 * Модельный класс, содержащий информацию о значениях, которые допустимы
 * в фильтре по налоговым формам.
 * 
 * Необходимость обусловлена тем, что перечень этих значений зависит от 
 * 1) роли пользователя
 * 2) какие формы и декларации назначены подразделению пользователя,
 * 3) какие настройки есть у источников для налоговых форм и деклараций подразделения пользователя
 * @author dsultanbekov
 */
public class FormDataFilterAvailableValues {
	private Set<Integer> departmentIds;
	private List<FormType> formTypes;
	private List<FormDataKind> kinds;

	/**
	 * Получить набор подразделений
	 * В случае, если возвращает null, это означает, что у пользователя есть 
	 * доступ ко всем существующим подразделениям
	 * @return набор идентификаторов подразделений
	 */
	public Set<Integer> getDepartmentIds() {
		return departmentIds;
	}
	
	/**
	 * Задать набор подразделений
	 * В случае, если у пользователя есть доступ ко всем подразделениям, нужно присваивать null
	 * @param departmentIds набор идентификаторов подразделений
	 */
	public void setDepartmentIds(Set<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}
	
	/**
	 * Получить список видов налоговых форм
	 * @return виды налоговых форм
	 */
	public List<FormType> getFormTypes() {
		return formTypes;
	}
	
	/**
	 * Задать набор видов налоговых форм
	 * @param formTypeIds набор видов налоговых форм
	 */
	public void setFormTypeIds(List<FormType> formTypes) {
		this.formTypes = formTypes;
	}

	/**
	 * Получить набор типов налоговых форм
	 * @return типы налоговых форм
	 */
	public List<FormDataKind> getKinds() {
		return kinds;
	}

	/**
	 * Задать набор типов налоговых форм
	 * @param kinds типы налоговых форм
	 */
	public void setKinds(List<FormDataKind> kinds) {
		this.kinds = kinds;
	}
}
