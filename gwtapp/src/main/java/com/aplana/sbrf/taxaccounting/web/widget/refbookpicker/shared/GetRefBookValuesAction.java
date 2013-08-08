package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookValuesAction extends UnsecuredActionImpl<GetRefBookValuesResult> implements Serializable, ActionName{
	private static final long serialVersionUID = -5419717754608198048L;
	
	private long refBookAttrId;
	private String searchPattern;
	private String filter;
	private PagingParams pagingParams;
	private Date version;
	
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
	
	@Override
	public String getName() {
		return "Получение значений справочника";
	}



}
