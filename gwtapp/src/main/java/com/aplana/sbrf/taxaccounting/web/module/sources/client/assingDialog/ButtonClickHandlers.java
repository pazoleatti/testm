package com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;

/**
 * Обработчик который вызывается после нажатия на кнопку ОК вьюхи
 * диалогового окна
 *
 * @author aivanov
 * @since 21.05.2014
 */
public interface ButtonClickHandlers {
    void ok(PeriodsInterval periodsInterval);
    void cancel();
}
