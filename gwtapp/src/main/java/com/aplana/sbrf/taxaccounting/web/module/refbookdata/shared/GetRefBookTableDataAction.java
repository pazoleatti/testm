package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class GetRefBookTableDataAction extends UnsecuredActionImpl<GetRefBookTableDataResult> implements ActionName {

	long refBookId;
	PagingParams pagingParams;
	Date relevanceDate;
    private String searchPattern;
    private boolean exactSearch;
    private Long recordId;
    private int sortColumnIndex;
    private boolean ascSorting;
    private String filter;

    public long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(long refBookId) {
		this.refBookId = refBookId;
	}

	public PagingParams getPagingParams() {
		return pagingParams;
	}

	public void setPagingParams(PagingParams pagingParams) {
		this.pagingParams = pagingParams;
	}

	public Date getRelevanceDate() {
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public int getSortColumnIndex() {
        return sortColumnIndex;
    }

    public void setSortColumnIndex(int sortColumnIndex) {
        this.sortColumnIndex = sortColumnIndex;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
	public String getName() {
		return "Получить строку из справочника";
	}
}
