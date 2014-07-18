package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

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

    /** Период действия назначения. Может быть null */
    private Date periodStart;
    private Date periodEnd;
    private int performerId;
	
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

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getPerformerId() {
        return performerId;
    }

    public void setPerformerId(int performerId) {
        this.performerId = performerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepartmentFormType that = (DepartmentFormType) o;

        if (departmentId != that.departmentId) return false;
        if (formTypeId != that.formTypeId) return false;
        if (performerId != that.performerId) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (kind != that.kind) return false;
        if (periodEnd != null ? !periodEnd.equals(that.periodEnd) : that.periodEnd != null) return false;
        if (periodStart != null ? !periodStart.equals(that.periodStart) : that.periodStart != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + departmentId;
        result = 31 * result + formTypeId;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        result = 31 * result + (periodStart != null ? periodStart.hashCode() : 0);
        result = 31 * result + (periodEnd != null ? periodEnd.hashCode() : 0);
        result = 31 * result + performerId;
        return result;
    }

    @Override
    public String toString() {
        return "DepartmentFormType{" +
                "id=" + id +
                ", departmentId=" + departmentId +
                ", formTypeId=" + formTypeId +
                ", kind=" + kind +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                ", performerId=" + performerId +
                '}';
    }
}
