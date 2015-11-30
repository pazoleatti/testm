package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

/**
 * Хендлеры формы "Блокировки"
 * @author dloshkarev
 */
public interface LockListUiHandlers extends AplanaUiHandlers {

    /**
     * Выю пытается удалить блокировку
     */
    void onDeleteLock();

    void onFindClicked();
}