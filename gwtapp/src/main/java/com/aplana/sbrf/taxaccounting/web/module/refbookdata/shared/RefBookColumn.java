package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

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
}
