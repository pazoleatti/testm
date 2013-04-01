package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Видимость DialogBox
 */
public class DialogBoxChangeVisibilityEvent extends GwtEvent<DialogBoxChangeVisibilityEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onChangeVisibility(DialogBoxChangeVisibilityEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();
	private final boolean visible;

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, boolean visible) {
		DialogBoxChangeVisibilityEvent errorEvent = new DialogBoxChangeVisibilityEvent(visible);
		source.fireEvent(errorEvent);
	}

	public DialogBoxChangeVisibilityEvent(boolean visible) {
		this.visible = visible;
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onChangeVisibility(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public boolean isVisible() {
		return visible;
	}

}
