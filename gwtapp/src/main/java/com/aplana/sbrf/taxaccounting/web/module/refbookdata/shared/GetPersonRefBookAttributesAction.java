package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author lhaziev
 */
public class GetPersonRefBookAttributesAction extends UnsecuredActionImpl<GetPersonRefBookAttributesResult> implements ActionName {

	@Override
	public String getName() {
		return "Получить список атрибутов";
	}
}
