package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import java.util.*;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.InitRefBookMultiAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.InitRefBookMultiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
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
public class InitRefBookMultiHandler extends AbstractActionHandler<InitRefBookMultiAction, InitRefBookMultiResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public InitRefBookMultiHandler() {
		super(InitRefBookMultiAction.class);
	}

	@Override
	public InitRefBookMultiResult execute(InitRefBookMultiAction action,
			ExecutionContext context) throws ActionException {
		InitRefBookMultiResult result = new InitRefBookMultiResult();
		Map<String, Integer> headers = new LinkedHashMap<String, Integer>();

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
	public void undo(InitRefBookMultiAction action, InitRefBookMultiResult result,
			ExecutionContext context) throws ActionException {
		//
	}

}
