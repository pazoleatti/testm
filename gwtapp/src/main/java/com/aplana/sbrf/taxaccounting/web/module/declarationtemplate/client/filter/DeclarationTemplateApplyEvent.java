package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class DeclarationTemplateApplyEvent extends GwtEvent<DeclarationTemplateApplyEvent.MyHandler> {

	public interface MyHandler extends EventHandler {
		void onClickFind(DeclarationTemplateApplyEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new DeclarationTemplateApplyEvent());
	}

	public DeclarationTemplateApplyEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onClickFind(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}
}
