package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Вид налоговой формы (название вида формы, без привязки к версии)
 * Каждому виду формы (FormType) может соответствовать несколько версий формы (FormTemplate).
 * @author dsultanbekov
 */
public class FormType implements Serializable {
	private static final long serialVersionUID = 3632916658352875666L;
	
	private int id;
	private String name;
	private TaxType taxType;
    private VersionedObjectStatus status;
    private String code;

    /**
     * Статус
     * @return статус вида налоговой формы
     */
    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
	 * Получить идентификатор типа налоговой формы
	 * @return идентификатор типа налоговой формы
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Задать идентификатор типа налоговой формы
	 * @param id желаемое значение идентификатора
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * Получить название вида налоговой формы
	 * @return название вида налоговой формы
	 */
	public String getName() {
		return name;
	}
	/**
	 * Задать название вида налоговой формы
	 * @param name желаемое значение названия
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Возвращает {@link TaxType вид налога}
	 * @return вид налога
	 */
	public TaxType getTaxType() {
		return taxType;
	}
	/**
	 * Задать вид налога
	 * @param taxType желаемое значение вида налога
	 */
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

    /**
     * @return идентификатор формы и номер формы
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
