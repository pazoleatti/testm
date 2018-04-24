package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;

/**
 * Данные по элементу списка справочников из настройщика
 */
public class RefBookConfListItem {

    private Long id;
    private String name;
    private RefBookType refBookType;
    private boolean readOnly;
    private boolean visible;
    private String regionality;

    public RefBookConfListItem() {
    }

    public RefBookConfListItem(Long id, String name, RefBookType refBookType, boolean readOnly, boolean visible, String regionality) {
        this.id = id;
        this.name = name;
        this.refBookType = refBookType;
        this.readOnly = readOnly;
        this.visible = visible;
        this.regionality = regionality;
    }

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

    public RefBookType getRefBookType() {
        return refBookType;
    }

    public void setRefBookType(RefBookType refBookType) {
        this.refBookType = refBookType;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getRegionality() {
        return regionality;
    }

    public void setRegionality(String regionality) {
        this.regionality = regionality;
    }
}
