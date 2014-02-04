package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefBookMultiDereferenceValueAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetRefBookMultiDereferenceValueResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookMultiDereferenceValueHandler
		extends
		AbstractActionHandler<GetRefBookMultiDereferenceValueAction, GetRefBookMultiDereferenceValueResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookMultiDereferenceValueHandler() {
		super(GetRefBookMultiDereferenceValueAction.class);
	}

	@Override
	public GetRefBookMultiDereferenceValueResult execute(
			GetRefBookMultiDereferenceValueAction action, ExecutionContext context)
			throws ActionException {
		GetRefBookMultiDereferenceValueResult result = new GetRefBookMultiDereferenceValueResult();

		RefBook refBook = refBookFactory.getByAttribute(action
				.getRefBookAttrId());
		RefBookAttribute attribute = refBook.getAttribute(action
				.getRefBookAttrId());
		RefBookDataProvider provider = refBookFactory.getDataProvider(refBook
				.getId());

		Map<String, RefBookValue> record = provider.getRecordData(action
				.getRecordId());
		RefBookValue refBookValue = record.get(attribute.getAlias());
		result.setDereferenceValue(String.valueOf(refBookValue));

		return result;
	}

	@Override
	public void undo(GetRefBookMultiDereferenceValueAction arg0,
			GetRefBookMultiDereferenceValueResult arg1, ExecutionContext arg2)
			throws ActionException {
		// /

	}

}
