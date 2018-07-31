package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.EditRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.EditRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class EditRefBookHandler extends AbstractActionHandler<EditRefBookAction, EditRefBookResult> {

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    public EditRefBookHandler() {
        super(EditRefBookAction.class);
    }

    @Override
    public EditRefBookResult execute(EditRefBookAction action, ExecutionContext context) throws ActionException {
        EditRefBookResult result = new EditRefBookResult();
        RefBook refBook = commonRefBookService.get(action.getRefBookId());
        Logger logger = new Logger();

        LockData lockData = lockDataService.getLock(commonRefBookService.generateTaskKey(refBook.getId()));
        if (lockData != null) {
            result.setLock(true);
            logger.info(commonRefBookService.getRefBookLockDescription(lockData, refBook.getId()));
            result.setLockMsg("Для текущего справочника запущена операция, при которой редактирование невозможно");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(EditRefBookAction action, EditRefBookResult result, ExecutionContext context) throws ActionException {

    }
}
