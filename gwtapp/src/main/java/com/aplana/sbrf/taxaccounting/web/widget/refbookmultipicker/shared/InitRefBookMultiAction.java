package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class InitRefBookMultiAction extends UnsecuredActionImpl<InitRefBookMultiResult> implements Serializable, ActionName {
	private static final long serialVersionUID = 3385076853496363265L;
	
	private long refBookAttrId;
	
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
