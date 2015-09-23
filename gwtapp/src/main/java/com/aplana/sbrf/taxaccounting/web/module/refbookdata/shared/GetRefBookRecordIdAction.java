package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получает реальный record_id записи, не уникальный
 * User: avanteev
 */
public class GetRefBookRecordIdAction extends UnsecuredActionImpl<GetRefBookRecordIdResult> {
    private Long uniqueRecordId, refBookId;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public Long getUniqueRecordId() {
        return uniqueRecordId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }
}
