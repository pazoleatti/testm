package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetCountFilterValuesAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.GetCountFilterValuesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * обработчик для получения количества данных для компонента выбора из справочника попадающих под фильтр
 *
 * @author aivanov
 */
@Component
@PreAuthorize("isAuthenticated()")
public class GetCountFilterValuesHandler extends AbstractActionHandler<GetCountFilterValuesAction, GetCountFilterValuesResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    SecurityService securityService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    FormDataService formDataService;

    @Autowired
    RefBookPickerFilterBuilder buildFilter;

    public GetCountFilterValuesHandler() {
        super(GetCountFilterValuesAction.class);
    }

    @Override
    public GetCountFilterValuesResult execute(GetCountFilterValuesAction action, ExecutionContext executionContext)
            throws ActionException {
        GetCountFilterValuesResult result = new GetCountFilterValuesResult();

        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());

        String filter;
        if (action.isHierarchy()) {
            filter = buildFilter.buildTreePickerFilter(action.getFilter(), action.getSearchPattern(), refBook);
        } else {
            filter = buildFilter.buildMultiPickerFilter(action.getFilter(), action.getSearchPattern(), refBook, action.getContext());
        }

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(refBook.getId());

        Integer count = refBookDataProvider.getRecordsCount(action.getVersion(), filter);
        result.setCount(count);

        return result;
    }

    @Override
    public void undo(GetCountFilterValuesAction action, GetCountFilterValuesResult result, ExecutionContext context)
            throws ActionException {
    }


}
