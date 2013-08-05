package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetRefBookDereferenceValueAction extends UnsecuredActionImpl<GetRefBookDereferenceValueResult> implements Serializable, ActionName{
	private static final long serialVersionUID = -5419717754708198048L;
	
	private long refBookAttrId;
	private long recordId;
	
	public long getRefBookAttrId() {
		return refBookAttrId;
	}

	public void setRefBookAttrId(long refBookAttrId) {
		this.refBookAttrId = refBookAttrId;
	}

	public long getRecordId() {
		return recordId;
	}

	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}

	@Override
	public String getName() {
		return "Разименование значения справочника";
	}


}
