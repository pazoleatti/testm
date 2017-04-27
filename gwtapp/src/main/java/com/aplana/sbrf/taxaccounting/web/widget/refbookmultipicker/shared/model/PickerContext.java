package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model;

import java.io.Serializable;

/**
 * Контекст в котором открывается справочник
 */
public class PickerContext implements Serializable{
    private static final long serialVersionUID = 1437921401222684027L;

    private RegionFilter regionFilter = RegionFilter.DEFAULT;
    private Long attributeId;

    public RegionFilter getRegionFilter() {
        return regionFilter;
    }

    public void setRegionFilter(RegionFilter regionFilter) {
        this.regionFilter = regionFilter;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public enum RegionFilter {
        DEFAULT,
        FORM_FILTER,
        DEPARTMENT_CONFIG_FILTER
    }
}
