package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * @author estecenko
 */
public class GetRefBookAttributesAction extends UnsecuredActionImpl<GetRefBookAttributesResult> implements ActionName {

	long refBookId;

    Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
	public String getName() {
		return "Получить список атрибутов";
	}
}
