package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public static final String RECORD_ID_ALIAS = "record_id";

	public static final String RECORD_PARENT_ID_ALIAS = "PARENT_ID";

    public static final String RECORD_VERSION_FROM_ALIAS = "record_version_from";
    public static final String REF_BOOK_VERSION_FROM_TITLE = "Дата начала актуальности";
    public static final int REF_BOOK_VERSION_FROM_WIDTH = 6;

    public static final String RECORD_VERSION_TO_ALIAS = "record_version_to";
    public static final String REF_BOOK_VERSION_TO_TITLE = "Дата окончания актуальности";
    public static final int REF_BOOK_VERSION_TO_WIDTH = 6;

	public static final String RECORD_SORT_ALIAS = "row_number_over";

    public static final Long DEPARTMENT_CONFIG_TRANSPORT = 31L;
    public static final Long DEPARTMENT_CONFIG_PROFIT = 33L;
    public static final Long DEPARTMENT_CONFIG_UKS = 37L;

	/** Код справочника */
	private Long id;

	/** Наименование справочника */
	private String name;

	/** Атрибуты справочника */
	private List<RefBookAttribute> attributes;

    /** Скрипт справочника */
    private String scriptId;

	/** Признак отображения справочника */
	private boolean visible;

	/** Тип справочника (0 - Линейный, 1 - Иерархический) */
	private int type;

	/** Редактируемый (0 - редактирование недоступно пользователю, 1 - редактирование доступно пользователю) */
	private boolean readOnly;

	public static RefBookAttribute getVersionFromAttribute() {
		RefBookAttribute attr = new RefBookAttribute();
		attr.setWidth(REF_BOOK_VERSION_FROM_WIDTH);
		attr.setName(REF_BOOK_VERSION_FROM_TITLE);
		attr.setAttributeType(RefBookAttributeType.DATE);
		attr.setAlias(RECORD_VERSION_FROM_ALIAS);
		return attr;
	}

	public static RefBookAttribute getVersionToAttribute() {
		RefBookAttribute attr = new RefBookAttribute();
		attr.setWidth(REF_BOOK_VERSION_TO_WIDTH);
		attr.setName(REF_BOOK_VERSION_TO_TITLE);
		attr.setAttributeType(RefBookAttributeType.DATE);
		attr.setAlias(RECORD_VERSION_TO_ALIAS);
		return attr;
	}

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
	 * Возвращает атрибут по его псевдониму. Регистронезависимый поиск
	 * @param alias
	 * @throws IllegalArgumentException в случае, если искомого атрибута нет в справочнике
	 * @return
	 */
	public RefBookAttribute getAttribute(final String alias) {
		if (alias == null) {
			throw new IllegalArgumentException("Attribute alias must be defined");
		}

		for(RefBookAttribute attribute : attributes) {
			if (alias.toLowerCase().equals(attribute.getAlias().toLowerCase())) {
				return attribute;
			}
		}
		throw new IllegalArgumentException(new StringBuilder("Attribute \"").append(alias).append("\" not found in refbook (id=").append(id).append(", \"").append(name).append("\"))").toString());
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
		throw new IllegalArgumentException(new StringBuilder("Attribute id=").append(attributeId).append(" not found in refbook (id=").append(id).append(", \"").append(name).append("\"))").toString());
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
		if (isHierarchic()) {
			result.put(RefBook.RECORD_PARENT_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, null));
		}
		for (RefBookAttribute attribute : getAttributes()) {
			result.put(attribute.getAlias(), new RefBookValue(attribute.getAttributeType(), null));
		}
		return result;
	}

    /**
     * Id скрипта справочника в BLOB_DATA
     * @return
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Id скрипта справочника в BLOB_DATA
     * @param scriptId
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

	/**
	 * Возвращает признак видимости справочника
	 * @return true - виден; false - скрыт
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Устанавливает признак видимости справочника
	 * @param visible true - виден; false - скрыт
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/** Возвращает упорядоченный список атрибутов для сортировки по умолчанию. В запросах пока используется только
	 * первый в списке, то есть мультисортировка пока не поддерживается.
	 * @return всегда определен, не может быть null
	 */
	private List<RefBookAttribute> getSortAttributes() {
		List<RefBookAttribute> defaultSort = new ArrayList<RefBookAttribute>();
		for (RefBookAttribute attr : attributes) {
			if (attr.getSortOrder() != null) {
				defaultSort.add(attr);
			}
		}
		if (defaultSort.size() > 0) {
			Collections.sort(defaultSort, new Comparator<RefBookAttribute>() {
				@Override
				public int compare(RefBookAttribute o1, RefBookAttribute o2) {
					return o2.getSortOrder() - o1.getSortOrder();
				}
			});
		}
		return defaultSort;
	}

	/** Возвращает атрибут для сортировки по умолчанию
	 * @return может быть null
	 */
	public RefBookAttribute getSortAttribute() {
		List<RefBookAttribute> list = getSortAttributes();
		return list.size() > 0 ? list.get(0) : null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RefBook{");
		sb.append("attributes=").append(attributes);
		sb.append(", id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", scriptId='").append(scriptId).append('\'');
		sb.append(", visible=").append(visible);
		sb.append(", type=").append(type);
		sb.append(", readOnly=").append(readOnly);
		sb.append('}');
		return sb.toString();
	}
}