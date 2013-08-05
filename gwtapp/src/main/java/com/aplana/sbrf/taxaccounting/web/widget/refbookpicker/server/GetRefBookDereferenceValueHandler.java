package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookDereferenceValueAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookDereferenceValueResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookDereferenceValueHandler
		extends
		AbstractActionHandler<GetRefBookDereferenceValueAction, GetRefBookDereferenceValueResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookDereferenceValueHandler() {
		super(GetRefBookDereferenceValueAction.class);
	}

	@Override
	public GetRefBookDereferenceValueResult execute(
			GetRefBookDereferenceValueAction action, ExecutionContext context)
			throws ActionException {
		GetRefBookDereferenceValueResult result = new GetRefBookDereferenceValueResult();

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
	public void undo(GetRefBookDereferenceValueAction arg0,
			GetRefBookDereferenceValueResult arg1, ExecutionContext arg2)
			throws ActionException {
		// /

	}

}
