package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;

import java.util.Collection;

/**
 * Хендлеры формы "Блокировки"
 * @author dloshkarev
 */
public interface LockListUiHandlers extends AplanaUiHandlers {

    /**
     * Выю пытается продлить блокировку
     */
    void onExtendLock();

    /**
     * Выю пытается удалить блокировку
     */
    void onDeleteLock();

    void onFindClicked();

    void onStopAsync();
}
