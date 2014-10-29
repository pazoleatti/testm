package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author lhaziev
 */
public class GetIrfsDataResult implements Result {

    private PagingResult<IfrsRow> ifrsRows;
    private int totalCountOfRecords;

    public PagingResult<IfrsRow> getIfrsRows() {
        return ifrsRows;
    }

    public void setIfrsRows(PagingResult<IfrsRow> ifrsRows) {
        this.ifrsRows = ifrsRows;
    }

    public int getTotalCountOfRecords() {
        return totalCountOfRecords;
    }

    public void setTotalCountOfRecords(int totalCountOfRecords) {
        this.totalCountOfRecords = totalCountOfRecords;
    }
}