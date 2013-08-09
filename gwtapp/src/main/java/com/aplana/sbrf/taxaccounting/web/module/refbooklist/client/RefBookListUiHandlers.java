package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.Type;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы списка справочников
 *
 * @author Stanislav Yasinskiy
 */
public interface RefBookListUiHandlers extends UiHandlers {
    void init(Type type, String filter);
}
