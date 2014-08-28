package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckRecordExistenceAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckRecordExistenceResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONF', 'ROLE_CONTROL_NS')")
public class CheckRecordExistenceHandler extends AbstractActionHandler<CheckRecordExistenceAction, CheckRecordExistenceResult> {

    @Autowired
    private RefBookFactory refBookFactory;

    public CheckRecordExistenceHandler() {
        super(CheckRecordExistenceAction.class);
    }

    @Override
    public CheckRecordExistenceResult execute(CheckRecordExistenceAction action, ExecutionContext context) throws ActionException {
        CheckRecordExistenceResult result = new CheckRecordExistenceResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        if (action.getRecordId() != null) {
            RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
            List<Pair<Long, Long>> checkRecord = provider.checkRecordExistence(null, "record_id = " + action.getRecordId());
            if (checkRecord == null || checkRecord.isEmpty()) {
                result.setRecordExistence(true);
            }
        }

        return result;
    }

    @Override
    public void undo(CheckRecordExistenceAction action, CheckRecordExistenceResult result, ExecutionContext context) throws ActionException {
    }
}