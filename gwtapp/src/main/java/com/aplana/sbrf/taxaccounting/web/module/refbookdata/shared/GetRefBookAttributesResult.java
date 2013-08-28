package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public class GetRefBookAttributesResult implements Result {
	List<RefBookAttribute> attributes;

	public List<RefBookAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<RefBookAttribute> attributes) {
		this.attributes = attributes;
	}
}
