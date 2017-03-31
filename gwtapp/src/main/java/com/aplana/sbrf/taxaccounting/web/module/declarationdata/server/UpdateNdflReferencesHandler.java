package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.dao.refbook.NdflReferenceDao;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateNdflReferenceResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateNdflReferencesAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class UpdateNdflReferencesHandler extends AbstractActionHandler<UpdateNdflReferencesAction, UpdateNdflReferenceResult> {

    @Autowired
    private NdflReferenceDao ndflReferenceDao;

    private final String ERRTEXT = "ERRTEXT";

    public UpdateNdflReferencesHandler() {
        super(UpdateNdflReferencesAction.class);
    }

    @Override
    public UpdateNdflReferenceResult execute(UpdateNdflReferencesAction action, ExecutionContext context) throws ActionException {
        UpdateNdflReferenceResult result = new UpdateNdflReferenceResult();
        int rowsUpdated = ndflReferenceDao.updateField(action.getNdflReferences(), ERRTEXT, action.getNote());
        result.setRowsUpdated(rowsUpdated);
        return result;
    }

    @Override
    public void undo(UpdateNdflReferencesAction action, UpdateNdflReferenceResult result, ExecutionContext context) throws ActionException {

    }
}
