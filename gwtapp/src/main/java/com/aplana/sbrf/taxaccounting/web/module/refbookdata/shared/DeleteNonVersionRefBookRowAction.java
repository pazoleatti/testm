package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * User: avanteev
 */
public class DeleteNonVersionRefBookRowAction extends UnsecuredActionImpl<DeleteNonVersionRefBookRowResult> {
    Long refBookId;
    List<Long> recordsId;
    //Выставляет флаг, что можно удалить запись несмотря на все найденные предупреждения
    //Только для подразделения, пока что по крайней мере
    private boolean okDelete;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public List<Long> getRecordsId() {
        return recordsId;
    }

    public void setRecordsId(List<Long> recordsId) {
        this.recordsId = recordsId;
    }

    public boolean isOkDelete() {
        return okDelete;
    }

    public void setOkDelete(boolean okDelete) {
        this.okDelete = okDelete;
    }
}
