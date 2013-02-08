package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс, представляющий собой запись о назначении возможности работать с налоговой формой в конкретном департаменте.
 * Указывается: подразделение банка, вид налоговой формы, тип налоговой формы
 * @author dsultanbekov
 */
public class DepartmentFormType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private int departmentId;
	private int formTypeId;
	private FormDataKind kind;
	
	/**
	 * Получить идентификатор записи
	 * @return идентификатор записи
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * Задать идентификатор записи
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Получить идентификатор подразделения
	 * @return идентфикатор подразделения
	 */
	public int getDepartmentId() {
		return departmentId;
	}
	
	/**
	 * Задать идентификатор подразделения
	 * @param departmentId идентфикатор подразделения
	 */
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	
	/**
	 * Получить вид налоговой формы
	 * @return идентфикатор вида налоговой формы
	 */
	public int getFormTypeId() {
		return formTypeId;
	}
	
	/**
	 * Задать вид налоговой формы
	 * @param formTypeId идентификатор вида налоговой формы
	 */
	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}
	
	/**
	 * Получить тип налоговой формы
	 * @return тип налоговой формы
	 */
	public FormDataKind getKind() {
		return kind;
	}
	
	/**
	 * Задать тип налоговой формы
	 * @param kind тип налоговой формы
	 */
	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		return "DepartmentFormType [id=" + id + ", departmentId="
				+ departmentId + ", formTypeId=" + formTypeId + ", kind="
				+ kind + "]";
	}	
}
