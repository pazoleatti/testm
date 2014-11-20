package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

import java.io.Serializable;

public class RefBookColumn implements Serializable{
	private Long id;
	private String name;
	private String alias;
    private String refBookName;
	private int width;
	private RefBookAttributeType attributeType;
	private Long refBookAttributeId;
	private HorizontalAlignment alignment;
    private Formats format;
    private boolean isHierarchical = false;
    private boolean readOnly;
    private Integer maxLength;
    private Integer precision;

   /** Обязательность заполнения */
    private boolean required;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

    public String getRefBookName() {
        return refBookName;
    }

    public void setRefBookName(String refBookName) {
        this.refBookName = refBookName;
    }

    public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(RefBookAttributeType attributeType) {
		this.attributeType = attributeType;
	}

	public Long getRefBookAttributeId() {
		return refBookAttributeId;
	}

	public void setRefBookAttributeId(Long refBookAttributeId) {
		this.refBookAttributeId = refBookAttributeId;
	}

	public HorizontalAlignment getAlignment() {
		return alignment;
	}

	public void setAlignment(HorizontalAlignment alignment) {
		this.alignment = alignment;
	}

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Formats getFormat() {
        return format;
    }

    public void setFormat(Formats format) {
        this.format = format;
    }

    public boolean isHierarchical() {
        return isHierarchical;
    }

    public void setHierarchical(boolean isHierarchical) {
        this.isHierarchical = isHierarchical;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefBookColumn column = (RefBookColumn) o;

        if (isHierarchical != column.isHierarchical) return false;
        if (readOnly != column.readOnly) return false;
        if (required != column.required) return false;
        if (width != column.width) return false;
        if (alias != null ? !alias.equals(column.alias) : column.alias != null) return false;
        if (alignment != column.alignment) return false;
        if (attributeType != column.attributeType) return false;
        if (format != column.format) return false;
        if (id != null ? !id.equals(column.id) : column.id != null) return false;
        if (maxLength != null ? !maxLength.equals(column.maxLength) : column.maxLength != null) return false;
        if (name != null ? !name.equals(column.name) : column.name != null) return false;
        if (precision != null ? !precision.equals(column.precision) : column.precision != null) return false;
        if (refBookAttributeId != null ? !refBookAttributeId.equals(column.refBookAttributeId) : column.refBookAttributeId != null)
            return false;
        if (refBookName != null ? !refBookName.equals(column.refBookName) : column.refBookName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (refBookName != null ? refBookName.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + (attributeType != null ? attributeType.hashCode() : 0);
        result = 31 * result + (refBookAttributeId != null ? refBookAttributeId.hashCode() : 0);
        result = 31 * result + (alignment != null ? alignment.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (isHierarchical ? 1 : 0);
        result = 31 * result + (readOnly ? 1 : 0);
        result = 31 * result + (maxLength != null ? maxLength.hashCode() : 0);
        result = 31 * result + (precision != null ? precision.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
    }
}
