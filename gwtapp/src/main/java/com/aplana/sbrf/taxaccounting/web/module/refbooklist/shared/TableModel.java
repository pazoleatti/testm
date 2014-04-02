package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;

import java.io.Serializable;

/**
 * Модель для таблицы
 *
 * @author Stanislav Yasinskiy
 */
public class TableModel implements Serializable {

    private Long id;
    private String name;
    private RefBookType refBookType;
	private boolean readOnly;
    private boolean visible;
    private Long regionAttributeId;

    public TableModel() {
    }

    public TableModel(Long id, String name, RefBookType refBookType, boolean readOnly, boolean visible, Long regionAttributeId) {
        this.id = id;
        this.name = name;
        this.refBookType = refBookType;
        this.readOnly = readOnly;
        this.visible = visible;
        this.regionAttributeId = regionAttributeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RefBookType getType() {
		return refBookType;
	}

    public void setType(RefBookType refBookType) {
        this.refBookType = refBookType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

    public RefBookType getRefBookType() {
        return refBookType;
    }

    public void setRefBookType(RefBookType refBookType) {
        this.refBookType = refBookType;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Long getRegionAttributeId() {
        return regionAttributeId;
    }

    public void setRegionAttributeId(Long regionAttributeId) {
        this.regionAttributeId = regionAttributeId;
    }
}
