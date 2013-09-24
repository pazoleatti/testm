package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookAttributesAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookAttributesResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP')")
public class GetRefBookAttributesHandler extends AbstractActionHandler<GetRefBookAttributesAction, GetRefBookAttributesResult> {

	@Autowired
	RefBookFactory refBookFactory;

	public GetRefBookAttributesHandler() {
		super(GetRefBookAttributesAction.class);
	}

	@Override
	public GetRefBookAttributesResult execute(GetRefBookAttributesAction action, ExecutionContext executionContext) throws ActionException {
		List<RefBookAttribute> attributes = refBookFactory.get(action.getRefBookId()).getAttributes();
		GetRefBookAttributesResult result = new GetRefBookAttributesResult();
		List<RefBookColumn> columns = new ArrayList<RefBookColumn>();
		for (RefBookAttribute attribute : attributes) {
			RefBookColumn col = new RefBookColumn();
			col.setId(attribute.getId());
			col.setAlias(attribute.getAlias());
			col.setAttributeType(attribute.getAttributeType());
			col.setName(attribute.getName());
			col.setRefBookAttributeId(attribute.getRefBookAttributeId());
			col.setWidth(attribute.getWidth());
			col.setAlignment(getHorizontalAlignment(attribute));
			columns.add(col);
		}
		result.setColumns(columns);
		return result;
	}

	private HorizontalAlignment getHorizontalAlignment(RefBookAttribute attribute) {
		HorizontalAlignment alignment;
		switch (attribute.getAttributeType()) {
			case NUMBER:
				alignment = HorizontalAlignment.ALIGN_RIGHT;
				break;
			case STRING:
				alignment = HorizontalAlignment.ALIGN_LEFT;
				break;
			case DATE:
				alignment = HorizontalAlignment.ALIGN_CENTER;
				break;
			case REFERENCE:
				RefBook refBook = refBookFactory.get(attribute.getRefBookId());
				RefBookAttribute refAttr = refBook.getAttribute(attribute.getRefBookAttributeId());
				alignment = getHorizontalAlignment(refAttr);
				break;
			default:
				alignment = HorizontalAlignment.ALIGN_LEFT;
				break;
		}
		return alignment;
	}

	@Override
	public void undo(GetRefBookAttributesAction getRefBookAttributesAction, GetRefBookAttributesResult getRefBookAttributesResult,
	                 ExecutionContext executionContext) throws ActionException {
	}
}
