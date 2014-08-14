package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;

import java.io.Serializable;

/**
 * Модель для таблицы
 *
 * @author Stanislav Yasinskiy
 */
public class TableModel implements Serializable {

    private Long id;
    private int rowNumber;
    private String name;
    private RefBookType refBookType;
	private boolean readOnly;
    private boolean visible;
    private RefBookAttribute regionAttribute;

    public TableModel() {
    }

    public TableModel(Long id, int rowNumber, String name, RefBookType refBookType, boolean readOnly, boolean visible, RefBookAttribute regionAttribute) {
        this.id = id;
        //нужно чтобы сортировка на форме совпадала с сортировкой БД
        this.rowNumber = rowNumber;
        this.name = name;
        this.refBookType = refBookType;
        this.readOnly = readOnly;
        this.visible = visible;
        this.regionAttribute = regionAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRowNumber() {
        return rowNumber;
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

    public RefBookAttribute getRegionAttribute() {
        return regionAttribute;
    }

    public void setRegionAttribute(RefBookAttribute regionAttribute) {
        this.regionAttribute = regionAttribute;
    }
}
