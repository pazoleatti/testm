package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * НФ назначения
 * (так будем называть)
 * 
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepartmentFormType that = (DepartmentFormType) o;

        if (departmentId != that.departmentId) return false;
        if (formTypeId != that.formTypeId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (kind != that.kind) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + departmentId;
        result = 31 * result + formTypeId;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }
}
