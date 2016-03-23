package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Formats;

import java.io.Serializable;

/**
 * Атрибут справочника
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 17:04
 */
public class RefBookAttribute implements Serializable {

	private static final Long serialVersionUID = 1L;

	/** Код атрибута */
	private Long id;

	/** Наименование атрибута */
	private String name;

	/** Псевдоним атрибута */
	private String alias;

	/** Тип атрибута */
	private RefBookAttributeType attributeType;

    /** Обязательность заполнения */
    private boolean required;

    /** Если тип атрибута "Ссылка", то данное поле задает справочник, из которого будут подставляться данные. Например,
	 * для справочника "Книга" в атрибуте "Автор" указываем код справочника "Человек".
	 */
	private Long refBookId;

	/** Если тип атрибута "Ссылка", то данное поле задает код отображаемого атрибута. Например, у справочника "Книга"
	 * есть поле "Автор", ссылающее на справочник "Человек". При отображении элементов справочника "Книга" в столбце
	 * "Автор" пользователь ожидает увидеть не код человека, а его ФИО. Для указания того, какое поле отображать и
	 * предназначено данное поле.
	 */
	private Long refBookAttributeId;

    private RefBookAttribute refBookAttribute;

    /** Признак видимости атрибута */
	private boolean visible;

	/** Точность. Количество знаков после запятой. Используется для форматирования вещественных значений при выводе
	 * на экран
 	 */
	private Integer precision;

	/** Ширина атрибута в относительных единицах по аналогии с НФ. Используется для задания ширины столбца при
	 * отображении таблицы справочника на экране.
	 */
	private int width;

    /** Признак уникальности атрибута */
    private int unique;

	/** Определяет порядок сортировки данных по умолчанию */
	private Integer sortOrder;

    /** формат отображения даты */
    private Formats format;

    /**
     * Признак «Только для чтения»
     */
    private boolean readOnly;

    /**
     * Максимальная длина строки
     */
    private Integer maxLength;

	/**
	 * Возвращает код атрибута
	 * @return код атрибута
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Устанавливает код атрибута
	 * @param id код атрибута
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Возвращает наименование атрибута
	 * @return наименование атрибута
	 */
	public String getName() {
		return name;
	}

	/**
	 * Устанавливает наименование атрибута
	 * @param name наименование атрибута
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Возвращает тип атрибута
	 * @return тип атрибута
	 */
	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	/**
	 * Устанавливает тип атрибута
	 * @param attributeType тип атрибута
	 */
	public void setAttributeType(RefBookAttributeType attributeType) {
		this.attributeType = attributeType;
	}

	/**
	 * Возвращает код атрибута для отображения
	 * @return код атрибута
	 */
	public Long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	/**
	 * Устанавливает код отображаемого атрибута
	 * @param refBookAttributeId
	 */
	public void setRefBookAttributeId(Long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

    public RefBookAttribute getRefBookAttribute() {
        return refBookAttribute;
    }

    public void setRefBookAttribute(RefBookAttribute refBookAttribute) {
        this.refBookAttribute = refBookAttribute;
    }

    /**
	 * Возвращает код справочника, на которое ссылается атрибут
	 * @return код справочника
	 */
	public Long getRefBookId() {
		return refBookId;
	}

	/**
	 * Устанавливает код справочника, на который будет ссылаться атрибут
	 * @param refBookId код справочника
	 */
	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	/**
	 * Возвращает значение видимости атрибута
	 * @return true - атрибут виден; false - не виден
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Задает видимость атрибута
	 * @param visible true - атрибут виден; false - не виден
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Возвращает значение точности атрибута
	 * @return точность [кол-во знаков после запятой]
	 */
	public Integer getPrecision() {
		return precision;
	}

	/**
	 * Устанавливает точность атрибута
	 * @param precision точность [кол-во знаков после запятой]
	 */
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	/**
	 * Возвращает ширину столбца для отображение значений атрибута
	 * @return ширина столбца в относительных единицах
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Устанавливает ширину столбца
	 * @param width ширина столбца в относительных единицах
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Возвращает псевдоним атрибута
	 * @return псевдоним
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Устанавливает псевдоним атрибута
	 * @param alias псевдоним
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

    /**
     * Возвращает обязательность поля
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Устанавливает обязательность атрибута
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getUnique() {
        return unique;
    }

    public void setUnique(int unique) {
        this.unique = unique;
    }

    public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

    public Formats getFormat() {
        return format;
    }

    public void setFormat(Formats format) {
        this.format = format;
    }

    /**
     * Признак «Только для чтения»
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Признак «Только для чтения»
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookAttribute attribute = (RefBookAttribute) o;

        if (readOnly != attribute.readOnly) return false;
        if (required != attribute.required) return false;
        if (unique != attribute.unique) return false;
        if (visible != attribute.visible) return false;
        if (width != attribute.width) return false;
        if (alias != null ? !alias.equals(attribute.alias) : attribute.alias != null) return false;
        if (attributeType != attribute.attributeType) return false;
        if (format != attribute.format) return false;
        if (id != null ? !id.equals(attribute.id) : attribute.id != null) return false;
        if (maxLength != null ? !maxLength.equals(attribute.maxLength) : attribute.maxLength != null) return false;
        if (name != null ? !name.equals(attribute.name) : attribute.name != null) return false;
        if (precision != null ? !precision.equals(attribute.precision) : attribute.precision != null) return false;
        if (refBookAttributeId != null ? !refBookAttributeId.equals(attribute.refBookAttributeId) : attribute.refBookAttributeId != null)
            return false;
        if (refBookId != null ? !refBookId.equals(attribute.refBookId) : attribute.refBookId != null) return false;
        if (sortOrder != null ? !sortOrder.equals(attribute.sortOrder) : attribute.sortOrder != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (attributeType != null ? attributeType.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (refBookId != null ? refBookId.hashCode() : 0);
        result = 31 * result + (refBookAttributeId != null ? refBookAttributeId.hashCode() : 0);
        result = 31 * result + (visible ? 1 : 0);
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + unique;
        result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (readOnly ? 1 : 0);
        result = 31 * result + (maxLength != null ? maxLength.hashCode() : 0);
        return result;
    }

    @Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RefBookAttribute{");
		sb.append("alias='").append(alias).append('\'');
		sb.append(", id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append(", attributeType=").append(attributeType);
		sb.append(", refBookId=").append(refBookId);
		sb.append(", refBookAttributeId=").append(refBookAttributeId);
		sb.append(", visible=").append(visible);
		sb.append(", precision=").append(precision);
		sb.append(", width=").append(width);
		sb.append(", required=").append(required);
        sb.append(", readOnly=").append(readOnly);
        sb.append(", unique=").append(unique);
		sb.append(", sortOrder=").append(sortOrder);
        sb.append(", format=").append(format != null ? format.getFormat() : null);
        sb.append(", length=").append(maxLength);
		sb.append('}');
		return sb.toString();
	}
}