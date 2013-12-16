package com.aplana.sbrf.taxaccounting.web.main.api.rpc;

import com.aplana.sbrf.taxaccounting.web.main.api.client.AuthRemoteServiceProxy;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.rebind.rpc.ProxyCreator;

/**
 * Creator для прокси удаленных сервисов. Здесь переопределен базовый класс для прокси.
 * <p/>
 * Это нужно для корректной обработки события потери сессии пользователем.
 *
 * @author Vitaliy Samolovskikh
 * @see com.google.gwt.user.rebind.rpc.ProxyCreator
 */
public class AuthProxyCreator extends ProxyCreator {
	/**
	 * Переопределяем стандартный конструктор.
	 */
	public AuthProxyCreator(JClassType serviceIntf) {
		super(serviceIntf);
	}

	/**
	 * Возвращает базовый класс для всех прокси удаленных сервисов.
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.main.api.client.AuthRemoteServiceProxy
	 */
	@Override
	protected Class<? extends RemoteServiceProxy> getProxySupertype() {
		return AuthRemoteServiceProxy.class;
	}
}
