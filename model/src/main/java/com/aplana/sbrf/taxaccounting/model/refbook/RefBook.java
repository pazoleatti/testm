package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cправочник
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 15:38
 */
public class RefBook implements Serializable {

	private static final Long serialVersionUID = 1L;

	public final static String RECORD_ID_ALIAS = "id";

	public final static String RECORD_PARENT_ID_ALIAS = "parent_id";

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

	/**
	 * Возвращает атрибут по его псевдониму
	 * @param alias
	 * @return
	 */
	public RefBookAttribute getAttribute(String alias) {
		if (alias == null) {
			throw new IllegalArgumentException("Attribute alias must be defined");
		}

		for(RefBookAttribute attribute : attributes) {
			if (alias.equals(attribute.getAlias())) {
				return attribute;
			}
		}
		throw new IllegalArgumentException(String.format("Attribute \"%s\" not found in refbook (id=%d, \"%s\"))", alias, id, name));
	}

	/**
	 * Возвращает атрибут по его коду
	 * @param attributeId
	 * @return
	 */
	public RefBookAttribute getAttribute(Long attributeId) {
		for(RefBookAttribute attribute : attributes) {
			if (attributeId.equals(attribute.getId())) {
				return attribute;
			}
		}
		throw new IllegalArgumentException(String.format("Attribute id=%d not found in refbook (id=%d, \"%s\"))", attributeId, id, name));
	}

	/**
	 * Проверяет, что справочник иерархичный. Проверка осуществляется по псевдонимам. Если есть атрибут
	 * с предопределнным для иерархии псевдонимом, то справочник считается иерархичным.
	 * @return
	 */
	public boolean isHierarchic() {
		for(RefBookAttribute attribute : attributes) {
			if (RECORD_PARENT_ID_ALIAS.equals(attribute.getAlias())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Создает "рыбу" для строки справочника
	 * @return
	 */
	public Map<String, RefBookValue> createRecord() {
		Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
		result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, null));
		result.put(RefBook.RECORD_PARENT_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, null));
		for (RefBookAttribute attribute : getAttributes()) {
			result.put(attribute.getAlias(), new RefBookValue(attribute.getAttributeType(), null));
		}
		return result;
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