package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.List;

/**
 * Cправочник
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 15:38
 */
public class RefBook implements Serializable {

	private static final Long serialVersionUID = 1L;

	/** Код справочника */
	private Long id;

	/** Наименование справочника */
	private String name;

	/** Атрибуты справочника */
	private List<RefBookAttribute> attributes;

	/**
	 * Возвращает код справочника
	 * @return код справочника
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Устанавливает код справочника
	 * @param id код справочника
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Возвращает наименование справочника
	 * @return наименование справочника
	 */
	public String getName() {
		return name;
	}

	/**
	 * Устанавливает наименование справочника
	 * @param name наименование справочника
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Возвращает список атрибутов справочника
	 * @return список атрибутов справочника
	 */
	public List<RefBookAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Возвращает список атрибутов справочника
	 * @param attributes список атрибутов справочника
	 */
	public void setAttributes(List<RefBookAttribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RefBook{");
		sb.append("id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", attributes=").append(attributes);
		sb.append('}');
		return sb.toString();
	}

}