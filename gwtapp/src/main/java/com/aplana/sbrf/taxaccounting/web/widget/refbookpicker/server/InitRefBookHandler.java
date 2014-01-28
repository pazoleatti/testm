package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.server;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.InitRefBookAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared.InitRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 * 
 */
@Component
@PreAuthorize("isAuthenticated()")
public class InitRefBookHandler extends
		AbstractActionHandler<InitRefBookAction, InitRefBookResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public InitRefBookHandler() {
		super(InitRefBookAction.class);
	}

	@Override
	public InitRefBookResult execute(InitRefBookAction action,
			ExecutionContext context) throws ActionException {
		InitRefBookResult result = new InitRefBookResult();
		Map<String, Integer> headers = new HashMap<String, Integer>();

		RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
		for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
			if (refBookAttribute.isVisible()) {
				headers.put(refBookAttribute.getName(), refBookAttribute.getWidth());
			}
		}
		
		result.setRefBookId(refBook.getId());
		result.setHeaders(headers);
		
		return result;
	}

	@Override
	public void undo(InitRefBookAction action, InitRefBookResult result,
			ExecutionContext context) throws ActionException {
		//
	}

}
