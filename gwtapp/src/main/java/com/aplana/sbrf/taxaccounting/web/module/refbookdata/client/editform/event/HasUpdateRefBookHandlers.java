package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event;

import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Обработчик событий изменения в презентор-виджете
 * {@link com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter}
 */
public interface HasUpdateRefBookHandlers extends HasHandlers {

    HandlerRegistration addUpdateRefBookHandler(UpdateForm.UpdateFormHandler handler);
}
