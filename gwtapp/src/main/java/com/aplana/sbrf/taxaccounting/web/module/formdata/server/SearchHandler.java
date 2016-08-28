package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SearchResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author auldanov
 *
 * Created on 28.03.2014.
 */
@Service
public class SearchHandler extends AbstractActionHandler<SearchAction, SearchResult> {

    @Autowired
    private DataRowService dataRowService;

    public SearchHandler() {
        super(SearchAction.class);
    }

    @Override
    public SearchResult execute(SearchAction searchAction, ExecutionContext executionContext) throws ActionException {
        DataRowRange range = new DataRowRange();
        range.setCount(searchAction.getTo());
        range.setOffset(searchAction.getFrom());
        SearchResult searchResult = new SearchResult();

        if (searchAction.isJustDelete()) {
            dataRowService.deleteSearchResults(searchAction.getSessionId(), null);
        } else {
            PagingResult<FormDataSearchResult> result = dataRowService.searchByKey(searchAction.getFormDataId(),
                    range, searchAction.getKey(), searchAction.getSessionId(), searchAction.isCaseSensitive(), searchAction.isManual(), searchAction.isCorrectionDiff());
            searchResult.setResults(result);
            searchResult.setSize(result.getTotalCount());
        }

        return searchResult;
    }

    @Override
    public void undo(SearchAction searchAction, SearchResult searchResult, ExecutionContext executionContext) throws ActionException {
    }

}
