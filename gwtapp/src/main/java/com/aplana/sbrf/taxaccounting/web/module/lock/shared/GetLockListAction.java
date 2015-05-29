package com.aplana.sbrf.taxaccounting.web.module.lock.shared;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение списка блокировок
 * @author dloshakarev
 */
public class GetLockListAction extends UnsecuredActionImpl<GetLockListResult> implements ActionName {

    /* Параметры пэйджинга */
    private PagingParams pagingParams;

    /* ограничение по имени пользователя или ключу */
    private String filter;

    private LockData.LockQueues queues;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public PagingParams getPagingParams() {
        return pagingParams;
    }

    public void setPagingParams(PagingParams pagingParams) {
        this.pagingParams = pagingParams;
    }

    public LockData.LockQueues getQueues() {
        return queues;
    }

    public void setQueues(LockData.LockQueues queues) {
        this.queues = queues;
    }

    @Override
    public String getName() {
        return "Получение списка блокировок";
    }
}
