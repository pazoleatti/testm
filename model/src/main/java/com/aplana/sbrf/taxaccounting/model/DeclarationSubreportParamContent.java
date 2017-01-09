package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;


/**
 * Параметр для спецю отчета декларации
 * 
 * @author lhaziev
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeclarationSubreportParamContent implements Ordered, Serializable {
	private static final long serialVersionUID = 1L;

    private DeclarationSubreportParamType type;
	private String name;
	private String alias;
	private int order;
    private String filter;
    private Long refBookAttributeId;
    /** Обязательность заполнения */
    private boolean required;


	/**
	 * Возвращает наименование параметра
	 * @return наименование столбца
	 */
	public String getName() {
		return name;
	}

	/**
	 * Задаёт наименование параметра
	 * @param name желаемое значение наименования столбца
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Возвращает алиас параметра. Алиас - это строковый псевдоним, который используется для доступа
	 * к данным параметра из скриптов.
	 * @return алиас столбца
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Задать алиас параметра
	 * @param alias желаемое значение алиаса
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	/**
	 * Возвращает порядковый номер параметра в форме
	 * @return порядковый номер столбца
	 */
	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * Задать порядковый номер параметра
	 * @param order желаемое значение номера столбца
	 */
	@Override
	public void setOrder(int order) {
		this.order = order;
	}

    /**
     * Возвразщает тип параметра
     * @return
     */
    public DeclarationSubreportParamType getType() {
        return type;
    }

    /**
     * Задает тип параметра
     * @param type
     */
    public void setType(DeclarationSubreportParamType type) {
        this.type = type;
    }

    /**
     * Возвращает фильтр для ссылочных параметроа
     * @return
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Задает фильтр для ссылочных параметроа
     * @param filter
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Long getRefBookAttributeId() {
        return refBookAttributeId;
    }

    public void setRefBookAttributeId(Long refBookAttributeId) {
        this.refBookAttributeId = refBookAttributeId;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
