package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author auldanov
 * Created on 28.03.2014.
 */
public class SearchResult implements Result {
    private List<FormDataSearchResult> results;
    private int size;

    public List<FormDataSearchResult> getResults() {
        return results;
    }

    public void setResults(List<FormDataSearchResult> results) {
        this.results = results;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
