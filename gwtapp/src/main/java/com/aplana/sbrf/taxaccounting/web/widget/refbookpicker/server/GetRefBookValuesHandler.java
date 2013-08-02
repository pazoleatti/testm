package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.GetRefBookValuesResult;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.RefBookItem;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetRefBookValuesHandler extends
		AbstractActionHandler<GetRefBookValuesAction, GetRefBookValuesResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookValuesHandler() {
		super(GetRefBookValuesAction.class);
	}

	@Override
	public GetRefBookValuesResult execute(GetRefBookValuesAction action,
			ExecutionContext context) throws ActionException {
		RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
		
		RefBookAttribute sortAttribute = refBook.getAttributes().iterator().next();
		
		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(refBook.getId());

		PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
				.getRecords(action.getVersion(), action.getPagingParams(),
						action.getSearchPattern(), sortAttribute);
		
		GetRefBookValuesResult result = new GetRefBookValuesResult();
		result.setPage(asseblRefBookPage(action, refBookPage, refBook));
		return result;
	}

	@Override
	public void undo(GetRefBookValuesAction action, GetRefBookValuesResult result,
			ExecutionContext context) throws ActionException {
		// 

	}

	private PagingResult<RefBookItem> asseblRefBookPage(GetRefBookValuesAction action,
			PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {

		List<RefBookItem> items = new ArrayList<RefBookItem>();
		
		for (Map<String, RefBookValue> record : refBookPage.getRecords()) {
			RefBookItem item = new RefBookItem();
			List<String> values = new ArrayList<String>();
			
			item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()
					.longValue());
			List<RefBookAttribute> attribute = refBook.getAttributes();
			for (RefBookAttribute refBookAttribute : attribute) {
				if (!RefBook.RECORD_ID_ALIAS.equals(refBookAttribute.getAlias())){
					if (RefBookAttributeType.REFERENCE.equals(refBookAttribute.getAttributeType())){
						// TODO: Необходимо разименовать значение ссылки
						values.add("Не разименовано: " + record.get(refBookAttribute.getAlias()));
					} else {
						String derefValue = String.valueOf(record.get(refBookAttribute.getAlias()));
						values.add(derefValue);
						if (refBookAttribute.getId().equals(action.getRefBookAttrId())){
							item.setDereferenceValue(derefValue);
						}
					}
					
				}
			}
			item.setValues(values);
			items.add(item);
		}

		PagingResult<RefBookItem> result = new PagingResult<RefBookItem>();
		result.setRecords(items);
		result.setTotalRecordCount(refBookPage.getTotalRecordCount());
		return result;
	}

}
