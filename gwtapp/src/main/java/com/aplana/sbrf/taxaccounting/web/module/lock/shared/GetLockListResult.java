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
    private int currentUserId;
    private boolean hasRoleAdmin;

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

    public boolean hasRoleAdmin() {
        return hasRoleAdmin;
    }

    public void setHasRoleAdmin(boolean hasRoleAdmin) {
        this.hasRoleAdmin = hasRoleAdmin;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }
}
