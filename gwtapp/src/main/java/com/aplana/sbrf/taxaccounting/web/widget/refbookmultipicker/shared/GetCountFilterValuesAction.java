package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;
import java.util.Date;

/**
 * @author aivanov
 */
public class GetCountFilterValuesAction extends UnsecuredActionImpl<GetCountFilterValuesResult> implements Serializable, ActionName {
    private static final long serialVersionUID = -5419454454608198048L;

    private long refBookAttrId;
    private String searchPattern;
    private String filter;
    private Date version;
    private PickerContext context;
    private boolean isHierarchy = false;
    private boolean exactSearch = false;

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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public PickerContext getContext() {
        return context;
    }

    public void setContext(PickerContext context) {
        this.context = context;
    }

    public boolean isHierarchy() {
        return isHierarchy;
    }

    public void setHierarchy(boolean isHierarchy) {
        this.isHierarchy = isHierarchy;
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    @Override
    public String getName() {
        return "Получение количества отфильтрованных данных для справочника";
    }
}