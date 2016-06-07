package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerContext;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookMultiValuesAction extends UnsecuredActionImpl<GetRefMultiBookValuesResult> implements Serializable, ActionName {
	private static final long serialVersionUID = -5419717754608198048L;

	private long refBookAttrId;
	private String searchPattern;
	private String filter;
	private PagingParams pagingParams;
	private Date version;
    private Integer sortAttributeIndex;
    private boolean isSortAscending;
    private PickerContext context;
    private boolean exactSearch;

    // идентификаторы которые нужно выделить
    // может быть пустой
    // используется только при попытке выделения засеченный виджету значений
    private List<Long> idsTofind;

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
	public PagingParams getPagingParams() {
		return pagingParams;
	}
	public void setPagingParams(PagingParams pagingParams) {
		this.pagingParams = pagingParams;
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

    public Integer getSortAttributeIndex() {
        return sortAttributeIndex;
    }

    public void setSortAttributeIndex(Integer sortAttributeIndex) {
        this.sortAttributeIndex = sortAttributeIndex;
    }

    public boolean isSortAscending() {
        return isSortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        isSortAscending = sortAscending;
    }

    public List<Long> getIdsTofind() {
        return idsTofind;
    }

    public void setIdsTofind(List<Long> idsTofind) {
        this.idsTofind = idsTofind;
    }

    public PickerContext getContext() {
        return context;
    }

    public void setContext(PickerContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "Получение значений справочника";
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GetRefBookMultiValuesAction{");
        sb.append("refBookAttrId=").append(refBookAttrId);
        sb.append(", searchPattern='").append(searchPattern).append('\'');
        sb.append(", filter='").append(filter).append('\'');
        sb.append(", pagingParams=").append(pagingParams);
        sb.append(", version=").append(version);
        sb.append(", sortAttributeIndex=").append(sortAttributeIndex);
        sb.append(", isSortAscending=").append(isSortAscending);
        sb.append('}');
        return sb.toString();
    }
}