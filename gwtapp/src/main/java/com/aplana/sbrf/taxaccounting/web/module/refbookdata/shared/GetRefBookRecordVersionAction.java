package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение списка версий записи справочника
 * @author dloshkarev
 */
public class GetRefBookRecordVersionAction extends UnsecuredActionImpl<GetRefBookRecordVersionResult> implements ActionName {

    private long refBookId;
    private long refBookRecordId;
    PagingParams pagingParams;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public long getRefBookRecordId() {
        return refBookRecordId;
    }

    public void setRefBookRecordId(long refBookRecordId) {
        this.refBookRecordId = refBookRecordId;
    }

    public PagingParams getPagingParams() {
        return pagingParams;
    }

    public void setPagingParams(PagingParams pagingParams) {
        this.pagingParams = pagingParams;
    }

    @Override
    public String getName() {
        return "Получить список версий записи";
    }
}
