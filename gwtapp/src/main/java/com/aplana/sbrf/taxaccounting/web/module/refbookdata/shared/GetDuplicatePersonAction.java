package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class GetDuplicatePersonAction extends UnsecuredActionImpl<GetDuplicatePersonResult> implements ActionName {

    private Date relevanceDate;
    /**
     * Выбранная запись
     */
    private RefBookDataRow record;

    public Date getRelevanceDate() {
        return relevanceDate;
    }

    public void setRelevanceDate(Date relevanceDate) {
        this.relevanceDate = relevanceDate;
    }

    public RefBookDataRow getRecord() {
        return record;
    }

    public void setRecord(RefBookDataRow record) {
        this.record = record;
    }

    @Override
	public String getName() {
		return "Получить строку из справочника";
	}
}
