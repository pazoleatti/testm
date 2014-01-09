package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class GetRefBookRecordAction extends UnsecuredActionImpl<GetRefBookRecordResult> implements ActionName {
	Long refBookId;
	Long refBookRecordId;
    Date relevanceDate;

	public Long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	public Long getRefBookRecordId() {
		return refBookRecordId;
	}

	public void setRefBookRecordId(Long refBookRecordId) {
		this.refBookRecordId = refBookRecordId;
	}

    public Date getRelevanceDate() {
        return relevanceDate;
    }

    public void setRelevanceDate(Date relevanceDate) {
        this.relevanceDate = relevanceDate;
    }

    @Override
	public String getName() {
		return "Получить запись справочника";
	}
}
