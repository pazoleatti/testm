package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CallbackUtils {
	
	private CallbackUtils(){};
	
	/**
	 * Добавляет к callback следующую функциональность:
	 *  - блокировка разблокировка экрана
	 *  - в случае ошибки добавляет информацию в лог и отображает сообщение об ошибке
	 * 
	 * @param callback
	 * @return
	 */
	public static <T> CompositeCallback<T> defaultCallback(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback)
		.addCallback(MsgOnFailureCallback.create())
		.addCallback(LockScrCallback.create());
	}
	
	public static <T> CompositeCallback<T> wrongStateCallback(AsyncCallback<T> callback){
		throw new UnsupportedOperationException();
	}
	
	public static <T> CompositeCallback<T> simpleCallback(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback);
	}
	
	@SuppressWarnings("rawtypes")
	public static  CompositeCallback emptyCallback(){
		return CompositeCallback.create();
	}

}
