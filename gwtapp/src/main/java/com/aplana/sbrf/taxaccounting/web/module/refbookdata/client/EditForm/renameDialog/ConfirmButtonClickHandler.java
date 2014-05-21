package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog;

import java.util.Date;

/**
 * Обработчик который вызывается после нажатия на кнопку ОК вьюхи
 * диалогового окна "Период применения изменений в печатных формах"
 *
 * @author aivanov
 * @since 21.05.2014
 */
public interface ConfirmButtonClickHandler {
    void onClick(Date dateFrom, Date dateTo);
}
