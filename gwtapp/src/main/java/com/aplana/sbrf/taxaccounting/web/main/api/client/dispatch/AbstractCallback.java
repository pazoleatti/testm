package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.google.gwt.user.client.rpc.AsyncCallback;

abstract public class AbstractCallback<T> implements AsyncCallback<T>{

	@Override
	public void onFailure(Throwable caught) {
		
	}

}
