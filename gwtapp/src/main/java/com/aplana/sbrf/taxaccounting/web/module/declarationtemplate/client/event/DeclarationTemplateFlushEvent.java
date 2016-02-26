package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class DeclarationTemplateFlushEvent extends GwtEvent<DeclarationTemplateFlushEvent.MyHandler> {

	public interface MyHandler extends EventHandler {
		void onFlush(DeclarationTemplateFlushEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public DeclarationTemplateFlushEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new DeclarationTemplateFlushEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onFlush(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
