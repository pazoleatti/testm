package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.io.Serializable;

/**
 * Вид декларации.
 * @author dsultanbekov
 */
public class DeclarationType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private TaxType taxType;
	private String name;
    private VersionedObjectStatus status;
    private Boolean isIfrs;
    private String ifrsName;

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
	 * Получить идентификатор
	 * @return идентфикатор
	 */
	public int getId() {
		return id;
	}

	/**
	 * Задать идентификатор
	 * @param id значение идентификатора
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Получить вид налога
	 * @return вид налога
	 */
	public TaxType getTaxType() {
		return taxType;
	}
	
	/**
	 *  Задать вид налога
	 * @param taxType вид налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
	
	/**
	 * Получить название вида декларации
	 * @return название вида декларации
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать название вида декларации
	 * @param name название вида декларации
	 */
	public void setName(String name) {
        this.name = StringUtils.cleanString(name);
	}

    public Boolean getIsIfrs() {
        return isIfrs;
    }

    public void setIsIfrs(Boolean isIfrs) {
        this.isIfrs = isIfrs;
    }

    public String getIfrsName() {
        return ifrsName;
    }

    public void setIfrsName(String ifrsName) {
        this.ifrsName = ifrsName;
    }

    @Override
    public String toString() {
        return "DeclarationType{" +
                "name='" + name + '\'' +
                '}';
    }
}
