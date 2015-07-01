package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event;

import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * User: avanteev
 */
public interface HasSetFormModeHandlers extends HasHandlers {

    HandlerRegistration addSetFormModeRefBookHandler(SetFormMode.SetFormModeHandler handler);
}
