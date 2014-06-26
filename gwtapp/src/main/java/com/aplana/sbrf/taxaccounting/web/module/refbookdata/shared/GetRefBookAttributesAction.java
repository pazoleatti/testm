package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author estecenko
 */
public class GetRefBookAttributesAction extends UnsecuredActionImpl<GetRefBookAttributesResult> implements ActionName {

	long refBookId;

    public GetRefBookAttributesAction() {
    }

    public GetRefBookAttributesAction(long refBookId) {
        this.refBookId = refBookId;
    }

    public long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(long refBookId) {
		this.refBookId = refBookId;
	}

	@Override
	public String getName() {
		return "Получить список атрибутов";
	}
}
