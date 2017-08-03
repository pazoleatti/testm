package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("isAuthenticated()")
public class GetPersonRefBookAttributesHandler extends AbstractActionHandler<GetPersonRefBookAttributesAction, GetPersonRefBookAttributesResult> {

	@Autowired
	RefBookFactory refBookFactory;

    @Autowired
    SecurityService securityService;

	private static final List<String> aliasList = Arrays.asList("RECORD_ID", "LAST_NAME", "FIRST_NAME", "MIDDLE_NAME", "INN", "SNILS", "INN_FOREIGN", "BIRTH_DATE", "DOC_NUMBER");

	public GetPersonRefBookAttributesHandler() {
		super(GetPersonRefBookAttributesAction.class);
	}

	@Override
	public GetPersonRefBookAttributesResult execute(GetPersonRefBookAttributesAction action, ExecutionContext executionContext) throws ActionException {
        RefBook refBook = refBookFactory.get(RefBook.Id.PERSON.getId());
        RefBook idDocRefBook = refBookFactory.get(RefBook.Id.ID_DOC.getId());

		Map<String, RefBookAttribute> attributeMap = new HashMap<String, RefBookAttribute>();
		for(RefBookAttribute refBookAttribute: refBook.getAttributes()) {
			attributeMap.put(refBookAttribute.getAlias(), refBookAttribute);
		}
		for(RefBookAttribute refBookAttribute: idDocRefBook.getAttributes()) {
			attributeMap.put(refBookAttribute.getAlias(), refBookAttribute);
		}

		GetPersonRefBookAttributesResult result = new GetPersonRefBookAttributesResult();
		List<RefBookColumn> columns = new ArrayList<RefBookColumn>();

		for (String alias : aliasList) {
			RefBookAttribute attribute = attributeMap.get(alias);

			RefBookColumn col = new RefBookColumn();
            RefBook attributeRefBook = null;
            if (attribute.getRefBookId() != null){
                attributeRefBook = refBookFactory.get(attribute.getRefBookId());
            }
			col.setId(attribute.getId());
			col.setAlias(attribute.getAlias());
			col.setAttributeType(attribute.getAttributeType());
			col.setName(attribute.getName());
            col.setRefBookName(attributeRefBook == null ? "" : attributeRefBook.getName());
            col.setHierarchical(attributeRefBook != null && attributeRefBook.getType() == 1);
			col.setRefBookAttributeId(attribute.getRefBookAttributeId());
			col.setWidth(attribute.getWidth());
			col.setAlignment(getHorizontalAlignment(attribute));
            col.setRequired(attribute.isRequired());
            col.setReadOnly(attribute.isReadOnly());
            col.setFormat(attribute.getFormat());
            col.setMaxLength(attribute.getMaxLength());
            col.setPrecision(attribute.getPrecision());
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
	public void undo(GetPersonRefBookAttributesAction action, GetPersonRefBookAttributesResult result,
	                 ExecutionContext executionContext) throws ActionException {
	}
}
