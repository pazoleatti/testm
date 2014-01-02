package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetNameAction extends UnsecuredActionImpl<GetNameResult> implements ActionName {
	Long refBookId;

    /** Идентификатор выбранной записи справочника. Используется в режиме версионирования */
    Long recordId;

	public Long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Override
	public String getName() {
		return "Получить название справочника";
	}
}
