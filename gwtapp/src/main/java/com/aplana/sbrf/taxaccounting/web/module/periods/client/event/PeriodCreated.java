package com.aplana.sbrf.taxaccounting.web.module.periods.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class PeriodCreated extends
		GwtEvent<PeriodCreated.OpenPeriodHandler> {

	public static interface OpenPeriodHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onPeriodCreated(PeriodCreated event);
	}

	private static final Type<OpenPeriodHandler> TYPE = new Type<OpenPeriodHandler>();

	public static Type<OpenPeriodHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	boolean success, int year) {
		PeriodCreated event = new PeriodCreated();
		event.setSuccess(success);
		event.setYear(year);
		source.fireEvent(event);
	}

	private boolean success;
	private int year;

	public PeriodCreated() {
	}

	@Override
	protected void dispatch(OpenPeriodHandler handler) {
		handler.onPeriodCreated(this);
	}

	@Override
	public Type<OpenPeriodHandler> getAssociatedType() {
		return getType();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}
}
