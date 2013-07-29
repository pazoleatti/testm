package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookAction extends UnsecuredActionImpl<GetRefBookResult> implements Serializable, ActionName{
	private static final long serialVersionUID = 3385076853496363265L;
	
	private long refBookAttrId;
	private Long formDataId;
	
	public Long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}

	public long getRefBookAttrId() {
		return refBookAttrId;
	}
	public void setRefBookAttrId(long refBookAttrId) {
		this.refBookAttrId = refBookAttrId;
	}
	
	@Override
	public String getName() {
		return "Получение справочника";
	}


}
