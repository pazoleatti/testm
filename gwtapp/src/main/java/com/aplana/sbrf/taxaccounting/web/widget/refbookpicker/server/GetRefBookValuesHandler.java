package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import java.text.NumberFormat;
import java.text.ParsePosition;
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
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
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

	@Autowired
	RefBookHelper refBookHelper;

	public GetRefBookValuesHandler() {
		super(GetRefBookValuesAction.class);
	}

	@Override
	public GetRefBookValuesResult execute(GetRefBookValuesAction action,
			ExecutionContext context) throws ActionException {
		RefBook refBook = refBookFactory.getByAttribute(action
				.getRefBookAttrId());

		RefBookAttribute sortAttribute = refBook.getAttributes().iterator()
				.next();

		RefBookDataProvider refBookDataProvider = refBookFactory
				.getDataProvider(refBook.getId());

		String filter = buildFilter(action.getFilter(),
				action.getSearchPattern(), refBook);

		PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider
				.getRecords(action.getVersion(), action.getPagingParams(),
						filter, sortAttribute);

		GetRefBookValuesResult result = new GetRefBookValuesResult();
		result.setPage(asseblRefBookPage(action, refBookDataProvider,
				refBookPage, refBook));
		return result;
	}

	@Override
	public void undo(GetRefBookValuesAction action,
			GetRefBookValuesResult result, ExecutionContext context)
			throws ActionException {
		//

	}

	private static String buildFilter(String filter, String serachPattern,
			RefBook refBook) {
		StringBuilder resultFilter = new StringBuilder();
		if (filter != null && !filter.trim().isEmpty()) {
			resultFilter.append(filter.trim());
		}

		StringBuilder resultSearch = new StringBuilder();
		if (serachPattern != null && !serachPattern.trim().isEmpty()) {

			for (RefBookAttribute attribute : refBook.getAttributes()) {
				if (RefBookAttributeType.STRING.equals(attribute
						.getAttributeType())) {
					if (resultSearch.length() > 0) {
						resultSearch.append(" or ");
					}
					resultSearch.append(attribute.getAlias()).append(" like ")
							.append("'%" + serachPattern.trim() + "%'");
				}/*
				 * else if
				 * (RefBookAttributeType.NUMBER.equals(attribute.getAttributeType
				 * ()) && isNumeric(serachPattern)){ if (resultSearch.length() >
				 * 0){ resultSearch.append(" or "); }
				 * resultSearch.append(attribute
				 * .getAlias()).append("=").append("\"" + serachPattern + "\"");
				 * }
				 */
			}

		}

		if (resultFilter.length() > 0 && resultSearch.length() > 0) {
			return "(" + resultFilter.toString() + ") and ("
					+ resultSearch.toString() + ")";
		} else if (resultFilter.length() > 0 && resultSearch.length() == 0) {
			return resultFilter.toString();
		} else if (resultSearch.length() > 0 && resultFilter.length() == 0) {
			return resultSearch.toString();
		} else {
			return null;
		}

	}

	public static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	private PagingResult<RefBookItem> asseblRefBookPage(
			GetRefBookValuesAction action, RefBookDataProvider provider,
			PagingResult<Map<String, RefBookValue>> refBookPage, RefBook refBook) {

		List<RefBookItem> items = new ArrayList<RefBookItem>();

		for (Map<String, RefBookValue> record : refBookPage) {
			RefBookItem item = new RefBookItem();
			List<String> values = new ArrayList<String>();

			item.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()
					.longValue());
			List<RefBookAttribute> attribute = refBook.getAttributes();

			Map<String, String> dereferenceRecord = refBookHelper
					.singleRecordDereference(refBook, provider, attribute, record);

			for (RefBookAttribute refBookAttribute : attribute) {
				String dereferanceValue = dereferenceRecord
						.get(refBookAttribute.getAlias());
				if (refBookAttribute.isVisible()) {
					values.add(dereferanceValue);
				}
				if (refBookAttribute.getId().equals(action.getRefBookAttrId())) {
					item.setDereferenceValue(dereferanceValue);
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
