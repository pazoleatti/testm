package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Утилита для удобства формирования AsyncCallback
 * 
 * @author sgoryachkin
 *
 */
public final class CallbackUtils {
	
	private CallbackUtils(){};
	 
	/**
	 * Добавляет к callback следующую функциональность:
	 *  - блокировка разблокировка экрана
	 *  - в случае ошибки добавляет информацию в лог и отображает сообщение об ошибке
	 *  
	 *  Подразумевается, что эта утилита используется, если в случае ошибки выполнения операции 
	 *  состояние презентора останется нормальным, и редирект на страницу ошибки не требуется.
	 * 
	 * @param callback
	 * @return
	 */
	public static <T> CompositeCallback<T> defaultCallback(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback)
		.addCallback(MessageOnFailureCallback.create())
		.addCallback(LockScrCallback.create());
	}
	
	
	public static <T> CompositeCallback<T> defaultCallbackNoLock(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback)
		.addCallback(MessageOnFailureCallback.create());
	}
	
	/**
	 * Добавляет к callback следующую функциональность:
	 *  - в случае ошибки добавляет информацию в лог и отображает сообщение об ошибке
	 *  
	 *  Подразумевается, что эта утилита используется, если в случае ошибки выполнения операции 
	 *  состояние презентора портится и необходим редирект на страницу ошибки.
	 * 
	 * @param callback
	 * @return
	 */
	public static <T> CompositeCallback<T> wrongStateCallback(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback)
		.addCallback(ErrorOnFailureCallback.create())
		.addCallback(LockScrCallback.create());
	}
	
	
	public static <T> CompositeCallback<T> wrongStateCallbackNoLock(AsyncCallback<T> callback){
		return CompositeCallback
		.create(callback)
		.addCallback(ErrorOnFailureCallback.create());
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
