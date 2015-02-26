package com.aplana.sbrf.taxaccounting.web.module.lock.shared;

import com.aplana.sbrf.taxaccounting.model.LockSearchOrdering;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение списка блокировок
 * @author dloshakarev
 */
public class GetLockListAction extends UnsecuredActionImpl<GetLockListResult> implements ActionName {

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private LockSearchOrdering searchOrdering;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    /* ограничение по имени пользователя или ключу */
    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCountOfRecords() {
        return countOfRecords;
    }

    public void setCountOfRecords(int countOfRecords) {
        this.countOfRecords = countOfRecords;
    }

    public LockSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(LockSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    @Override
    public String getName() {
        return "Получение списка блокировок";
    }
}
