package com.aplana.sbrf.taxaccounting.web.module.periods.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class PeriodCreated extends
		GwtEvent<PeriodCreated.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onPeriodCreated(PeriodCreated event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	boolean success) {
		PeriodCreated event = new PeriodCreated();
		event.setSuccess(success);
		source.fireEvent(event);
	}

	private boolean success;

	public PeriodCreated() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onPeriodCreated(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
