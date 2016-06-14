package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class EditRefBookHandler extends AbstractActionHandler<EditRefBookAction, EditRefBookResult> {

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService userService;

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
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        Logger logger = new Logger();

        String key = LockData.LockObjects.REF_BOOK.name() + "_" + refBook.getId();
        LockData lockData = lockDataService.getLock(key);
        if (lockData != null) {
            result.setLock(true);
            if (lockData.isAsync()) {
                logger.info(LockData.LOCK_CURRENT,
                        sdf.get().format(lockData.getDateLock()),
                        userService.getUser(lockData.getUserId()).getName(),
                        refBookFactory.getTaskName(ReportType.IMPORT_REF_BOOK, action.getRefBookId(), null));
            } else {
                logger.info(LockData.LOCK_CURRENT,
                        sdf.get().format(lockData.getDateLock()),
                        userService.getUser(lockData.getUserId()).getName(),
                        refBookFactory.getTaskName(ReportType.EDIT_REF_BOOK, action.getRefBookId(), null));
            }
            result.setLockMsg("Для текущего справочника запущена операция, при которой редактирование невозможно");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(EditRefBookAction action, EditRefBookResult result, ExecutionContext context) throws ActionException {

    }
}
