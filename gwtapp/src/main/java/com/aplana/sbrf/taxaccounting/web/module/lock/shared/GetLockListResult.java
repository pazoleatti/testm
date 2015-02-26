package com.aplana.sbrf.taxaccounting.web.module.lock.shared;

import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Результат получения списка блокировок
 * @author dloshkarev
 */
public class GetLockListResult implements Result {
    /**
     * Список блокировок
     */
    private List<LockDataItem> locks;
    private long totalCountOfRecords;

    public List<LockDataItem> getLocks() {
        return locks;
    }

    public void setLocks(List<LockDataItem> locks) {
        this.locks = locks;
    }

    public long getTotalCountOfRecords() {
        return totalCountOfRecords;
    }

    public void setTotalCountOfRecords(long totalCountOfRecords) {
        this.totalCountOfRecords = totalCountOfRecords;
    }
}
