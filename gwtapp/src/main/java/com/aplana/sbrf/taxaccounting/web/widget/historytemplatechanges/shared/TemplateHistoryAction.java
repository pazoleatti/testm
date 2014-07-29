package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Fail Mukhametdinov
 */
public abstract class TemplateHistoryAction<T extends TemplateHistoryResult> extends UnsecuredActionImpl<T> {
    private Integer typeId;
    private Integer templateId;
    private SortFilter sortFilter;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public SortFilter getSortFilter() {
        return sortFilter;
    }

    public void setSortFilter(SortFilter sortFilter) {
        this.sortFilter = sortFilter;
    }
}
