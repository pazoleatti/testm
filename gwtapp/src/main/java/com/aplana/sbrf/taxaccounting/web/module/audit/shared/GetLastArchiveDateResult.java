package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class GetLastArchiveDateResult implements Result {
    private String lastArchiveDate;

    public String getLastArchiveDate() {
        return lastArchiveDate;
    }

    public void setLastArchiveDate(String lastArchiveDate) {
        this.lastArchiveDate = lastArchiveDate;
    }
}
