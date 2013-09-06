package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.shared;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * @author sgoryachkin
 *
 */
public class GetPeriodsAction extends UnsecuredActionImpl<GetPeriodsResult> implements Serializable, ActionName{
	private static final long serialVersionUID = 3385076853496363265L;
	
	private String taxType;
	
	public String getTaxType() {
		return taxType;
	}

	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}
	
	@Override
	public String getName() {
		return "Получение списка отчетных периодов";
	}

}
