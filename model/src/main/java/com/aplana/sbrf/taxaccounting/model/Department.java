package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

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
	private static final long serialVersionUID = 2144891300700432727L;
    public static final Long REF_BOOK_ID = 30L;

	private int id;
	private String name;
	private String shortName;
	private Integer parentId;
	private DepartmentType type;
	private String tbIndex;
	private String sbrfCode;
    private Long regionId;
    private boolean isActive;
    private Integer code;
    /* Поле признак указывающее что подразделение используется в модуле гарантий */
    private boolean garantUse;

    /**
     * Полное имя подразделения
     * Пример:
     * Московский банк/
     *  Московский банк Управление недвижимости и материально-технического обеспечения/
     *      Отдел транспортного обеспечения/
     *          Центр сопровождения клиентских операций "Южная столица" (ЮЗБ, г.Ростов-на-Дону)/
     *              Центр сопровождения клиентских операций "Южная столица" (ЮЗБ, г.Ростов-на-Дону)
     */
    private String fullName;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

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

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Получить код подразделения в нотации Сбербанка
     */
    public boolean isGarantUse() {
        return garantUse;
    }

    public void setGarantUse(boolean garantUse) {
        this.garantUse = garantUse;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Department that = (Department) o;

		if (id != that.id) return false;
		if (isActive != that.isActive) return false;
		if (garantUse != that.garantUse) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null) return false;
		if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
		if (type != that.type) return false;
		if (tbIndex != null ? !tbIndex.equals(that.tbIndex) : that.tbIndex != null) return false;
		if (sbrfCode != null ? !sbrfCode.equals(that.sbrfCode) : that.sbrfCode != null) return false;
		if (regionId != null ? !regionId.equals(that.regionId) : that.regionId != null) return false;
		if (code != null ? !code.equals(that.code) : that.code != null) return false;
		return fullName != null ? fullName.equals(that.fullName) : that.fullName == null;

	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
		result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (tbIndex != null ? tbIndex.hashCode() : 0);
		result = 31 * result + (sbrfCode != null ? sbrfCode.hashCode() : 0);
		result = 31 * result + (regionId != null ? regionId.hashCode() : 0);
		result = 31 * result + (isActive ? 1 : 0);
		result = 31 * result + (code != null ? code.hashCode() : 0);
		result = 31 * result + (garantUse ? 1 : 0);
		result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
		return result;
	}
}