package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 * Получает последнюю версию справочника
 */
public class GetLastVersionHierarchyAction extends UnsecuredActionImpl<GetLastVersionHierarchyResult> {
    private long refBookId;
    private long refBookRecordId;

    public long getRefBookRecordId() {
        return refBookRecordId;
    }

    public void setRefBookRecordId(long refBookRecordId) {
        this.refBookRecordId = refBookRecordId;
    }

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }
}
