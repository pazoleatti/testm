package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class GetRefBookRecordAction extends UnsecuredActionImpl<GetRefBookRecordResult> implements ActionName {
	private Long refBookId;
    private Long refBookRecordId;
    private Date relevanceDate;
    /* при пеообходимости получить данные по последней версии*/
    private Long uniqueRecordId;
    private boolean isCreate;

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

    public Long getUniqueRecordId() {
        return uniqueRecordId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean isCreate) {
        this.isCreate = isCreate;
    }

    @Override
	public String getName() {
		return "Получить запись справочника";
	}
}
