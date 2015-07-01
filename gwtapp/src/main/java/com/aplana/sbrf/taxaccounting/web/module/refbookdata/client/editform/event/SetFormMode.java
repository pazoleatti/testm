package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Lhaziev
 */
public class SetFormMode extends
		GwtEvent<SetFormMode.SetFormModeHandler> {

	public interface SetFormModeHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onSetFormMode(SetFormMode event);
	}

	private static final Type<SetFormModeHandler> TYPE = new Type<SetFormModeHandler>();

	public static Type<SetFormModeHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	FormMode formMode) {
		SetFormMode event = new SetFormMode();
		event.setFormMode(formMode);
		source.fireEvent(event);
	}

	private FormMode formMode;

	public SetFormMode() {
	}

	@Override
	protected void dispatch(SetFormModeHandler handler) {
		handler.onSetFormMode(this);
	}

	@Override
	public Type<SetFormModeHandler> getAssociatedType() {
		return getType();
	}

    public FormMode getFormMode() {
        return formMode;
    }

    public void setFormMode(FormMode formMode) {
        this.formMode = formMode;
    }
}
