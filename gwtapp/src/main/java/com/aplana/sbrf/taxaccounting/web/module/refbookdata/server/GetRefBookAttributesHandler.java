package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
@PreAuthorize("isAuthenticated()")
public class GetRefBookAttributesHandler extends AbstractActionHandler<GetRefBookAttributesAction, GetRefBookAttributesResult> {

	@Autowired
	RefBookFactory refBookFactory;

    @Autowired
    SecurityService securityService;

    @Autowired
    DepartmentService departmentService;

    private static final Long ORGANIZATION_REF_BOOL_ID = 9L;

	public GetRefBookAttributesHandler() {
		super(GetRefBookAttributesAction.class);
	}

	@Override
	public GetRefBookAttributesResult execute(GetRefBookAttributesAction action, ExecutionContext executionContext) throws ActionException {
        RefBook refBook = refBookFactory.get(action.getRefBookId());
		List<RefBookAttribute> attributes = refBook.getAttributes();
        GetRefBookAttributesResult result = new GetRefBookAttributesResult();
		result.setRefBookName(refBook.getName());
        result.setRefBookType(refBook.getType());
		List<RefBookColumn> columns = new ArrayList<RefBookColumn>();
		for (RefBookAttribute attribute : attributes) {
            if (!attribute.isVisible())
                continue;
			RefBookColumn col = new RefBookColumn();
            RefBook attributeRefBook = null;
            if(attribute.getRefBookId()!=null){
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

        TAUser currentUser = securityService.currentUserInfo().getUser();
        if (currentUser.hasRole(TARole.ROLE_CONTROL_UNP)){ // Контроллер УНП
            // Контроллер УНП может редактировать все справочники (даже "ОУКС")
            result.setReadOnly(refBook.isReadOnly() && !refBook.getId().equals(ORGANIZATION_REF_BOOL_ID));
        } else { // Оператор, Контролёр, Контролёр НС
            if (currentUser.hasRole(TARole.ROLE_CONTROL_NS) && refBook.getRegionAttribute() != null){
                /*
                 * контролер НС может редактировать данные справочника, сделано без фильтра
                 * так как при показе строки уже фильтруются
                 */
                result.setReadOnly(refBook.isReadOnly());
            } else{  // Оператор, Контролёр не имеют прав редактирования региональных справочников
                result.setReadOnly(true);
            }
        }

        // доступность  кнопки-ссылка "Создать запрос на изменение..." для справочника "Организации-участники контролируемых сделок"
        result.setSendQuery(refBook.getId().equals(ORGANIZATION_REF_BOOL_ID) &&
                (currentUser.hasRole(TARole.ROLE_CONTROL_NS) || currentUser.hasRole(TARole.ROLE_CONTROL)));

		result.setColumns(columns);

        result.setSpecificReportTypes(refBookFactory.getSpecificReportTypes(refBook.getId(), securityService.currentUserInfo(), new Logger()));
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
