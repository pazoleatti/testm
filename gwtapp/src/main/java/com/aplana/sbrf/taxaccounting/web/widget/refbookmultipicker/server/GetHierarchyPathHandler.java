package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.exception.TAException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * обработчик для загрузки списка родителей в ирерахрхии итема
 *
 * @author aivanov
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetHierarchyPathHandler extends AbstractActionHandler<GetHierarchyPathAction, GetHierarchyPathResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    LogEntryService logEntryService;

    public GetHierarchyPathHandler() {
        super(GetHierarchyPathAction.class);
    }

    @Override
    public GetHierarchyPathResult execute(GetHierarchyPathAction action,
                                          ExecutionContext context) throws ActionException {

        GetHierarchyPathResult result = new GetHierarchyPathResult();
        Logger logger = new Logger();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());

        List<Long> list = new LinkedList<Long>();

        try {
            list = refBookDataProvider.getParentsHierarchy(action.getUniqueRecordId());
        } catch (TAException e) {
            logger.error(e.getMessage());
        }
        result.setIds(list);
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(GetHierarchyPathAction action, GetHierarchyPathResult result, ExecutionContext context)
            throws ActionException {
    }


}
