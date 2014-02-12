package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;
import java.util.Date;

/**
 * @author aivanov
 */
public class GetRefBookTreeValuesAction extends UnsecuredActionImpl<GetRefBookTreeValuesResult> implements Serializable, ActionName {
    private static final long serialVersionUID = -5419717754666198048L;

    private RefBookTreeItem parent;
    private long refBookAttrId;
    private String searchPattern;
    private String filter;
    private Date version;

    public RefBookTreeItem getParent() {
        return parent;
    }

    public void setParent(RefBookTreeItem parent) {
        this.parent = parent;
    }

    public long getRefBookAttrId() {
        return refBookAttrId;
    }

    public void setRefBookAttrId(long refBookAttrId) {
        this.refBookAttrId = refBookAttrId;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GetRefBookTreeValuesAction{");
        sb.append("parent=").append(parent);
        sb.append(", refBookAttrId=").append(refBookAttrId);
        sb.append(", searchPattern='").append(searchPattern).append('\'');
        sb.append(", filter='").append(filter).append('\'');
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getName() {
        return "Получение значений иерархичного справочника";
    }
}