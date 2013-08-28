package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public class GetRefBookAttributesAction extends UnsecuredActionImpl<GetRefBookAttributesResult> implements ActionName {

	long refbookId;

	public long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(long refbookId) {
		this.refbookId = refbookId;
	}

	@Override
	public String getName() {
		return "Получить список атрибутов";
	}
}
