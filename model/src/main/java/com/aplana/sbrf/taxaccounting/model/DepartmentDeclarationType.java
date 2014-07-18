package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Модельный класс, представляющий информацию о назначениий декларации какому-то подразделению
 * Содержит в себе ссылку на {@link Department подразделение} и {@link DeclarationType вид декларации}
 * @author dsultanbekov
 */
public class DepartmentDeclarationType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int departmentId;
	private int declarationTypeId;

    /** Период действия назначения. Может быть null */
    private Date periodStart;
    private Date periodEnd;

	/**
	 * Получить идентификатор записи
	 * @return идентификатор записи
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Задать идентфикатор записи
	 * @param id идентфикатор записи
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Получить идентификатор {@link Department подразделения}
	 * @return идентификатор подразделения
	 */
	public int getDepartmentId() {
		return departmentId;
	}
	
	/**
	 * Задать идентификатор подразделения
	 * @param departmentId идентификатор подразделения
	 */
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	
	/**
	 * Получить идентификатор {@link DeclarationType вида декларации}
	 * @return идентификатор вида декларации
	 */
	public int getDeclarationTypeId() {
		return declarationTypeId;
	}
	
	/**
	 * Задать идентификатор {@link DeclarationType вида декларации}
	 * @param declarationTypeId идентификатор вида декларации
	 */
	public void setDeclarationTypeId(int declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
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
}
