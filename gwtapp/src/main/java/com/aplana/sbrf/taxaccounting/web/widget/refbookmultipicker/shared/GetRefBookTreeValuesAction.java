package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookTreeItem;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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

    private Long formDataId;

    // идентификаторы которые нужно выделить
    // может быть пустой
    // используется только при попытке выделения засеченный виджету значений
    private List<Long> idsTofind;

    private boolean exactSearch;

    public GetRefBookTreeValuesAction() {
    }

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

    public List<Long> getIdsTofind() {
        return idsTofind;
    }

    public void setIdsTofind(List<Long> idsTofind) {
        this.idsTofind = idsTofind;
    }

    public Long getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(Long formDataId) {
        this.formDataId = formDataId;
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    @Override
    public String getName() {
        return "Получение значений иерархичного справочника";
    }
}