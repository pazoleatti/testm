package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public final class RevealContentTypeHolder {
	
	private RevealContentTypeHolder(){
		super();
	}
	
	private static Type<RevealContentHandler<?>> mainContent;
	
	public static Type<RevealContentHandler<?>> getMainContent(){
		return mainContent;
	}
	public static void setMainContent(Type<RevealContentHandler<?>> mainContent) {
		RevealContentTypeHolder.mainContent = mainContent;
	}
	
}
