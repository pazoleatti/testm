package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class GetRefBookRecordAction extends UnsecuredActionImpl<GetRefBookRecordResult> implements ActionName {
	Long refBookDataId;
	Long refBookRecordId;

	public Long getRefBookDataId() {
		return refBookDataId;
	}

	public void setRefBookDataId(Long refBookDataId) {
		this.refBookDataId = refBookDataId;
	}

	public Long getRefBookRecordId() {
		return refBookRecordId;
	}

	public void setRefBookRecordId(Long refBookRecordId) {
		this.refBookRecordId = refBookRecordId;
	}

	@Override
	public String getName() {
		return "Получить запись справочника";
	}
}
