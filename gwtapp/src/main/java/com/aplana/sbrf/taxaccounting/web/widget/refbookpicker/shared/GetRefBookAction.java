package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookAction extends UnsecuredActionImpl<GetRefBookResult> {
	
	private Long refBookId;
	private String searchPattern;
	private PagingParams pagingParams;
	
	public Long getRefBookId() {
		return refBookId;
	}
	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
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

}
