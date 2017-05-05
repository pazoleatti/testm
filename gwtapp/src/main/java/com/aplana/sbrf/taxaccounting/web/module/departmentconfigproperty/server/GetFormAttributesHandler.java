package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetFormAttributesAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared.GetFormAttributesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetFormAttributesHandler extends AbstractActionHandler<GetFormAttributesAction, GetFormAttributesResult> {

    @Autowired
    RefBookFactory rbFactory;
    @Autowired
    SecurityService securityService;

    public GetFormAttributesHandler() {
        super(GetFormAttributesAction.class);
    }

    @Override
    public GetFormAttributesResult execute(GetFormAttributesAction action, ExecutionContext executionContext) throws ActionException {
        GetFormAttributesResult res = new GetFormAttributesResult();

        // Текущий пользователь
        TAUser currUser = securityService.currentUserInfo().getUser();

        if (currUser.hasRoles(action.getTaxType(), TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            res.setCanEdit(true);
        } else if (currUser.hasRoles(action.getTaxType(), TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            res.setCanEdit(false);
        }

        List<RefBookAttribute> attributes = rbFactory.get(action.getRefBookId()).getAttributes();
        res.setAttributes(attributes);

        List<RefBookAttribute> tableAttributes = rbFactory.get(action.getTableRefBookId()).getAttributes();
        List<Column> tableColumns = new ArrayList<Column>();
        for (RefBookAttribute attribute : tableAttributes) {
            switch (attribute.getAttributeType()) {
                case STRING:
                    StringColumn textColumn = new StringColumn();
                    textColumn.setId(attribute.getId().intValue());
                    textColumn.setAlias(attribute.getAlias());
                    textColumn.setName(attribute.getName());
                    textColumn.setMaxLength(attribute.getMaxLength());
                    textColumn.setWidth(attribute.getWidth());
                    tableColumns.add(textColumn);
                    break;
                case REFERENCE:
                    RefBook refBook = rbFactory.get(attribute.getRefBookId());
                    RefBookColumn refColumn = new RefBookColumn();
                    refColumn.setId(attribute.getId().intValue());
                    refColumn.setRefBookAttributeId(attribute.getRefBookAttributeId());
                    refColumn.setRefBookAttribute(attribute.getRefBookAttribute());
                    refColumn.setAlias(attribute.getAlias());
                    refColumn.setName(attribute.getName());
                    refColumn.setWidth(attribute.getWidth());
                    refColumn.setVersioned(refBook.isVersioned());
                    tableColumns.add(refColumn);
                    break;
                case NUMBER:
                    NumericColumn numericColumn = new NumericColumn();
                    numericColumn.setId(attribute.getId().intValue());
                    numericColumn.setAlias(attribute.getAlias());
                    numericColumn.setName(attribute.getName());
                    numericColumn.setWidth(attribute.getWidth());
                    numericColumn.setMaxLength(attribute.getMaxLength());
                    numericColumn.setPrecision(attribute.getPrecision());
                    tableColumns.add(numericColumn);
                    break;
            }
        }
        res.setTableColumns(tableColumns);
        return res;
    }

    @Override
    public void undo(GetFormAttributesAction getFormAttributesAction, GetFormAttributesResult getFormAttributesResult, ExecutionContext executionContext) throws ActionException {

    }
}
